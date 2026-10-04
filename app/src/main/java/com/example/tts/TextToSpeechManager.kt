package com.example.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TextToSpeechManager(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentMessageId = MutableStateFlow<String?>(null)
    val currentMessageId: StateFlow<String?> = _currentMessageId.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentMessageId.value = null
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    _currentMessageId.value = null
                }
            })
        } else {
            Log.e("TTSManager", "Initialization failed with status: $status")
        }
    }

    fun speak(messageId: String, rawText: String, langCode: String = "auto") {
        if (!isInitialized || tts == null) return

        if (_currentMessageId.value == messageId && _isSpeaking.value) {
            stop()
            return
        }

        stop()

        // Clean markdown artifacts for smooth natural speech
        val cleanedText = cleanMarkdownForSpeech(rawText)

        val locale = when (langCode) {
            "ur", "pa", "sd", "skr", "bal" -> Locale("ur", "PK")
            "ar" -> Locale("ar", "SA")
            "hi" -> Locale("hi", "IN")
            else -> Locale.getDefault()
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            // Fallback to English or default
            tts?.setLanguage(Locale.US)
        }

        _currentMessageId.value = messageId
        _isSpeaking.value = true

        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, messageId)
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        _currentMessageId.value = null
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }

    private fun cleanMarkdownForSpeech(text: String): String {
        return text
            .replace(Regex("```[a-zA-Z]*\\n[\\s\\S]*?```"), "کوڈ کا نمونہ") // summarize code blocks
            .replace(Regex("[#*`_\\[\\]()]"), "")
            .replace(Regex("https?://\\S+"), "")
            .trim()
    }
}
