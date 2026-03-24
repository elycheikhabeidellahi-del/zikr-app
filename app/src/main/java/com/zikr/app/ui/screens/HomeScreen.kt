package com.zikr.app.ui.screens

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zikr.app.model.DhikrPeriod
import com.zikr.app.notifications.NotificationHelper
import com.zikr.app.viewmodel.MainViewModel
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToDhikr: (DhikrPeriod) -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var permissionRefresh by remember { mutableIntStateOf(0) }
    val notificationsGranted = remember(permissionRefresh) {
        NotificationHelper.hasNotificationPermission(context)
    }
    val exactAlarmsGranted = remember(permissionRefresh) {
        viewModel.canScheduleExactAlarms()
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) {
        permissionRefresh++
    }

    val exactAlarmPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.rescheduleAlarms()
        permissionRefresh++
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ذكر",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "تطبيق بسيط للذكر اليومي صباحًا ومساءً مع تكرار كل عنصر 7 مرات.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "يمكنك فتح الذكر الصباحي أو المسائي، ثم تشغيل التسلسل كاملًا أو البدء من أي سورة.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            DhikrCard(
                title = "الذكر الصباحي",
                subtitle = "الفاتحة، الإخلاص، الفلق، الناس، آية الكرسي، قريش، الشمس",
                buttonText = "افتح الذكر الصباحي",
                onClick = { onNavigateToDhikr(DhikrPeriod.MORNING) }
            )

            DhikrCard(
                title = "الذكر المسائي",
                subtitle = "الفاتحة، الإخلاص، الفلق، الناس، آية الكرسي، قريش، الليل",
                buttonText = "افتح الذكر المسائي",
                onClick = { onNavigateToDhikr(DhikrPeriod.EVENING) }
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "أوقات التذكير",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    ReminderTimeRow(
                        label = "وقت التنبيه الصباحي",
                        time = formatTime(uiState.morningTime),
                        onChange = {
                            showTimePicker(
                                context = context,
                                initialTime = uiState.morningTime
                            ) { hour, minute ->
                                viewModel.setMorningTime(hour, minute)
                            }
                        }
                    )

                    ReminderTimeRow(
                        label = "وقت التنبيه المسائي",
                        time = formatTime(uiState.eveningTime),
                        onChange = {
                            showTimePicker(
                                context = context,
                                initialTime = uiState.eveningTime
                            ) { hour, minute ->
                                viewModel.setEveningTime(hour, minute)
                            }
                        }
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "الأذونات",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    PermissionRow(
                        label = "إذن الإشعارات",
                        granted = notificationsGranted,
                        buttonText = "تفعيل"
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        } else {
                            permissionRefresh++
                        }
                    }

                    HorizontalDivider()

                    PermissionRow(
                        label = "التنبيهات الدقيقة",
                        granted = exactAlarmsGranted,
                        buttonText = "السماح"
                    ) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            exactAlarmPermissionLauncher.launch(
                                viewModel.exactAlarmPermissionIntent()
                            )
                        } else {
                            permissionRefresh++
                        }
                    }

                    Text(
                        text = "إذا لم يتم منح إذن التنبيهات الدقيقة فالتطبيق سيستخدم تنبيهًا أقل دقة لكنه سيظل يعمل.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "معلومة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "القراءة الحالية تعتمد على محرك النطق في الهاتف. يمكن لاحقًا استبدالها بملفات صوتية قرآنية داخل التطبيق.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DhikrCard(
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium
            )
            Button(onClick = onClick) {
                Text(buttonText)
            }
        }
    }
}

@Composable
private fun ReminderTimeRow(
    label: String,
    time: String,
    onChange: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = time,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        TextButton(onClick = onChange) {
            Text("تغيير")
        }
    }
}

@Composable
private fun PermissionRow(
    label: String,
    granted: Boolean,
    buttonText: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = if (granted) "مفعل" else "غير مفعل",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Button(onClick = onAction) {
            Text(buttonText)
        }
    }
}

private fun showTimePicker(
    context: Context,
    initialTime: LocalTime,
    onTimeSelected: (Int, Int) -> Unit
) {
    TimePickerDialog(
        context,
        { _, hour, minute -> onTimeSelected(hour, minute) },
        initialTime.hour,
        initialTime.minute,
        DateFormat.is24HourFormat(context)
    ).show()
}

private fun formatTime(time: LocalTime): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a", Locale("ar"))
    return time.format(formatter)
}
