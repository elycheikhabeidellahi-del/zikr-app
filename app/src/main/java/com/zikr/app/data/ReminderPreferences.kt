package com.zikr.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalTime

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "zikr_settings")

data class ReminderSettings(
    val morningHour: Int = 7,
    val morningMinute: Int = 0,
    val eveningHour: Int = 18,
    val eveningMinute: Int = 0
) {
    val morningTime: LocalTime
        get() = LocalTime.of(morningHour, morningMinute)

    val eveningTime: LocalTime
        get() = LocalTime.of(eveningHour, eveningMinute)
}

class ReminderPreferences(
    private val context: Context
) {
    private object Keys {
        val morningHour = intPreferencesKey("morning_hour")
        val morningMinute = intPreferencesKey("morning_minute")
        val eveningHour = intPreferencesKey("evening_hour")
        val eveningMinute = intPreferencesKey("evening_minute")
    }

    val settings: Flow<ReminderSettings> = context.dataStore.data.map { prefs ->
        ReminderSettings(
            morningHour = prefs[Keys.morningHour] ?: 7,
            morningMinute = prefs[Keys.morningMinute] ?: 0,
            eveningHour = prefs[Keys.eveningHour] ?: 18,
            eveningMinute = prefs[Keys.eveningMinute] ?: 0
        )
    }

    suspend fun setMorningTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.morningHour] = hour
            prefs[Keys.morningMinute] = minute
        }
    }

    suspend fun setEveningTime(hour: Int, minute: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.eveningHour] = hour
            prefs[Keys.eveningMinute] = minute
        }
    }
}
