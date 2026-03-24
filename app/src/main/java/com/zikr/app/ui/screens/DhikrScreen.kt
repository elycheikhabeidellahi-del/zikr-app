package com.zikr.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zikr.app.data.QuranRepository
import com.zikr.app.model.DhikrPeriod
import com.zikr.app.playback.QuranTtsPlayer

@Composable
fun DhikrScreen(
    period: DhikrPeriod,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val surahs = remember(period) { QuranRepository.surahsFor(period) }
    val player = remember(period) { QuranTtsPlayer(context) }
    val playbackState by player.state.collectAsState()

    DisposableEffect(Unit) {
        onDispose {
            player.shutdown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = period.label,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("رجوع")
                    }
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
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "طريقة العمل",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "عند الضغط على ابدأ الكل سيقرأ التطبيق كل عنصر 7 مرات، ثم ينتقل تلقائيًا إلى العنصر التالي.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "يمكنك أيضًا الضغط على ابدأ من هنا لأي عنصر ليبدأ منه ثم يكمل بقية القائمة.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "التحكم",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { player.playSequence(surahs, startIndex = 0) }
                        ) {
                            Text("ابدأ الكل")
                        }

                        OutlinedButton(
                            onClick = { player.stop() }
                        ) {
                            Text("إيقاف")
                        }
                    }

                    val statusText = when {
                        playbackState.errorMessage != null -> playbackState.errorMessage
                        playbackState.isPlaying && playbackState.currentSurahIndex != null -> {
                            val currentTitle = surahs[playbackState.currentSurahIndex].title
                            "يُقرأ الآن: $currentTitle - التكرار ${playbackState.currentRepetition} من 7"
                        }
                        playbackState.finished -> "اكتمل الذكر الحالي."
                        playbackState.engineReady -> "محرك القراءة جاهز."
                        else -> "جاري تجهيز محرك القراءة..."
                    }

                    Text(
                        text = statusText ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            surahs.forEachIndexed { index, surah ->
                val current = playbackState.currentSurahIndex == index && playbackState.isPlaying
                val done = index in playbackState.completedSurahIndices

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = surah.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = when {
                                        current -> "قيد التشغيل: ${playbackState.currentRepetition} / 7"
                                        done -> "تمت قراءتها 7 مرات"
                                        else -> "جاهزة للقراءة 7 مرات"
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            OutlinedButton(
                                onClick = { player.playSequence(surahs, startIndex = index) }
                            ) {
                                Text("ابدأ من هنا")
                            }
                        }

                        SelectionContainer {
                            Text(
                                text = surah.text,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }
    }
}
