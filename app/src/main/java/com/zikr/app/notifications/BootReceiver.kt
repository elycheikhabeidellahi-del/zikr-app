package com.zikr.app.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zikr.app.data.ReminderPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val shouldHandle = action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED

        if (!shouldHandle) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settings = ReminderPreferences(context).settings.first()
                ReminderScheduler(context).scheduleAll(settings)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
