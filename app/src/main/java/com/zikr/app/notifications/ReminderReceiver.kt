package com.zikr.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zikr.app.data.ReminderPreferences
import com.zikr.app.model.DhikrPeriod
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.createNotificationChannels(context)

        val period = DhikrPeriod.fromString(intent.getStringExtra(EXTRA_PERIOD))
        NotificationHelper.showReminderNotification(context, period)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = ReminderPreferences(context).settings.first()
                val scheduler = ReminderScheduler(context)
                val time = when (period) {
                    DhikrPeriod.MORNING -> settings.morningTime
                    DhikrPeriod.EVENING -> settings.eveningTime
                }
                scheduler.schedule(period, time)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
