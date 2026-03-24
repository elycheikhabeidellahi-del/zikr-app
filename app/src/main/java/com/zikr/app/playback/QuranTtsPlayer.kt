package com.zikr.app.playback

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.zikr.app.model.Surah
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class PlaybackUiState(
    val engineReady: Boolean = false,
    val isPlaying: Boolean = false,
    val currentSurahIndex: Int? = null,
    val currentRepetition: Int = 0,
    val completedSurahIndices: Set<Int> = emptySet(),
    val finished: Boolean = false,
    val errorMessage: String? = null
)

private data class QueueItem(
    val surahIndex: Int,
    val repetition: Int
) {
    val utteranceId: String
        get() = "$surahIndex-$repetition"
}

class QuranTtsPlayer(
    context: Context,
    private val repeatCount: Int = 7
) : TextToSpeech.OnInitListener {

    private val mainHandler = Handler(Looper.getMainLooper())
    private val tts = TextToSpeech(context.applicationContext, this)
    private val _state = MutableStateFlow(PlaybackUiState())
    val state: StateFlow<PlaybackUiState> = _state.asStateFlow()

    private var initialized = false
    private var currentSurahs: List<Surah> = emptyList()
    private var currentQueue: List<QueueItem> = emptyList()
    private var pendingStartIndex: Int? = null

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                val item = parseItem(utteranceId) ?: return
                mainHandler.post {
                    _state.value = _state.value.copy(
                        isPlaying = true,
                        currentSurahIndex = item.surahIndex,
                        currentRepetition = item.repetition,
                        finished = false,
                        errorMessage = null
                    )
                }
            }

            override fun onDone(utteranceId: String?) {
                val item = parseItem(utteranceId) ?: return
                mainHandler.post {
                    val completed = _state.value.completedSurahIndices.toMutableSet()
                    if (item.repetition == repeatCount) {
                        completed.add(item.surahIndex)
                    }

                    val isLast = currentQueue.lastOrNull()?.utteranceId == utteranceId
                    _state.value = _state.value.copy(
                        completedSurahIndices = completed,
                        isPlaying = if (isLast) false else _state.value.isPlaying,
                        finished = isLast,
                        currentSurahIndex = if (isLast) item.surahIndex else _state.value.currentSurahIndex,
                        currentRepetition = if (isLast) repeatCount else _state.value.currentRepetition
                    )
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                onError(utteranceId, TextToSpeech.ERROR)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                mainHandler.post {
                    _state.value = _state.value.copy(
                        isPlaying = false,
                        errorMessage = "تعذر تشغيل القراءة الصوتية على هذا الجهاز."
                    )
                }
            }
        })
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS) {
            _state.value = _state.value.copy(
                engineReady = false,
                errorMessage = "تعذر تهيئة محرك القراءة الصوتية."
            )
            return
        }

        val result = tts.setLanguage(Locale.forLanguageTag("ar"))
        tts.setSpeechRate(0.85f)

        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            _state.value = _state.value.copy(
                engineReady = false,
                errorMessage = "اللغة العربية غير مدعومة في محرك النطق الحالي."
            )
            return
        }

        initialized = true
        _state.value = _state.value.copy(engineReady = true, errorMessage = null)
        pendingStartIndex?.let {
            enqueueSequence(it)
        }
    }

    fun playSequence(surahs: List<Surah>, startIndex: Int = 0) {
        currentSurahs = surahs
        pendingStartIndex = startIndex

        _state.value = PlaybackUiState(
            engineReady = initialized,
            isPlaying = initialized,
            currentSurahIndex = startIndex,
            currentRepetition = 0,
            completedSurahIndices = emptySet(),
            finished = false,
            errorMessage = if (initialized) null else _state.value.errorMessage
        )

        if (initialized) {
            enqueueSequence(startIndex)
        }
    }

    fun stop() {
        tts.stop()
        _state.value = _state.value.copy(isPlaying = false)
    }

    fun shutdown() {
        tts.stop()
        tts.shutdown()
    }

    private fun enqueueSequence(startIndex: Int) {
        if (currentSurahs.isEmpty()) return

        tts.stop()

        val queue = buildList {
            for (surahIndex in startIndex until currentSurahs.size) {
                for (repetition in 1..repeatCount) {
                    add(QueueItem(surahIndex = surahIndex, repetition = repetition))
                }
            }
        }

        currentQueue = queue
        if (queue.isEmpty()) {
            _state.value = _state.value.copy(isPlaying = false, finished = true)
            return
        }

        queue.forEachIndexed { index, item ->
            val queueMode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts.speak(
                currentSurahs[item.surahIndex].speechText,
                queueMode,
                null,
                item.utteranceId
            )
        }
    }

    private fun parseItem(utteranceId: String?): QueueItem? {
        if (utteranceId.isNullOrBlank()) return null
        val parts = utteranceId.split("-")
        if (parts.size != 2) return null
        val surahIndex = parts[0].toIntOrNull() ?: return null
        val repetition = parts[1].toIntOrNull() ?: return null
        return QueueItem(surahIndex, repetition)
    }
}
