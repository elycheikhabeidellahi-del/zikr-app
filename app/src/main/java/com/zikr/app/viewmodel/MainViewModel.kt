package com.zikr.app.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zikr.app.data.ReminderPreferences
import com.zikr.app.data.ReminderSettings
import com.zikr.app.notifications.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.time.LocalTime

data class HomeUiState(
    val morningTime: LocalTime = LocalTime.of(7, 0),
    val eveningTime: LocalTime = LocalTime.of(18, 0)
)

class MainViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val reminderPreferences = ReminderPreferences(application)
    private val reminderScheduler = ReminderScheduler(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var latestSettings = ReminderSettings()

    init {
        viewModelScope.launch {
            reminderPreferences.settings.collect { settings ->
                latestSettings = settings
                _uiState.value = HomeUiState(
                    morningTime = settings.morningTime,
                    eveningTime = settings.eveningTime
                )
                reminderScheduler.scheduleAll(settings)
            }
        }
    }

    fun setMorningTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            reminderPreferences.setMorningTime(hour, minute)
        }
    }

    fun setEveningTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            reminderPreferences.setEveningTime(hour, minute)
        }
    }

    fun canScheduleExactAlarms(): Boolean {
        return reminderScheduler.canScheduleExactAlarms()
    }

    fun exactAlarmPermissionIntent(): Intent {
        return reminderScheduler.exactAlarmPermissionIntent()
    }

    fun rescheduleAlarms() {
        reminderScheduler.scheduleAll(latestSettings)
    }
}
