package com.zikr.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.zikr.app.model.DhikrPeriod
import com.zikr.app.notifications.EXTRA_PERIOD
import com.zikr.app.notifications.NotificationHelper
import com.zikr.app.ui.ZikrApp

class MainActivity : ComponentActivity() {

    private val launchTarget = mutableStateOf<DhikrPeriod?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificationHelper.createNotificationChannels(this)
        launchTarget.value = extractLaunchTarget(intent)

        setContent {
            ZikrApp(
                launchTarget = launchTarget.value,
                onDeepLinkConsumed = { launchTarget.value = null }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        launchTarget.value = extractLaunchTarget(intent)
    }

    private fun extractLaunchTarget(intent: Intent?): DhikrPeriod? {
        val value = intent?.getStringExtra(EXTRA_PERIOD) ?: return null
        return DhikrPeriod.fromString(value)
    }
}
