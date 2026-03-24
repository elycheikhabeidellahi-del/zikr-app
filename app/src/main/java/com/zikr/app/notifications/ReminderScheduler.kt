package com.zikr.app.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.zikr.app.data.ReminderSettings
import com.zikr.app.model.DhikrPeriod
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

class ReminderScheduler(
    private val context: Context
) {
    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    fun scheduleAll(settings: ReminderSettings) {
        schedule(DhikrPeriod.MORNING, settings.morningTime)
        schedule(DhikrPeriod.EVENING, settings.eveningTime)
    }

    fun schedule(period: DhikrPeriod, time: LocalTime) {
        val pendingIntent = reminderPendingIntent(period)
        alarmManager.cancel(pendingIntent)

        val triggerAtMillis = nextTriggerAtMillis(time)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    fun exactAlarmPermissionIntent(): Intent {
        return Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:${context.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    private fun reminderPendingIntent(period: DhikrPeriod): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_PERIOD, period.name)
        }

        return PendingIntent.getBroadcast(
            context,
            period.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerAtMillis(time: LocalTime): Long {
        val now = ZonedDateTime.now(ZoneId.systemDefault())
        var nextTrigger = now
            .withHour(time.hour)
            .withMinute(time.minute)
            .withSecond(0)
            .withNano(0)

        if (!nextTrigger.isAfter(now)) {
            nextTrigger = nextTrigger.plusDays(1)
        }

        return nextTrigger.toInstant().toEpochMilli()
    }
}
