package com.example.ui.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioRecorder
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.model.SupportedLanguages
import com.example.data.remote.GeminiClient
import com.example.tts.TextToSpeechManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val dao = db.chatDao()
    val ttsManager = TextToSpeechManager(application)
    val audioRecorder = AudioRecorder(application)

    // Sessions Flow
    val sessions: StateFlow<List<ChatSessionEntity>> = dao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    // Current Messages Flow
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessageEntity>> = _currentSessionId
        .flatMapLatest { sessionId ->
            if (sessionId != null) dao.getMessagesForSession(sessionId)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Input States
    val inputText = MutableStateFlow("")
    val selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageBase64 = MutableStateFlow<String?>(null)

    // Grounding Tools toggles
    val isWebSearchEnabled = MutableStateFlow(true)
    val isMapsGroundingEnabled = MutableStateFlow(false)

    // Models: gemini-3.5-flash, gemini-3.1-pro-preview, gemini-3.1-flash-lite-preview, gemini-3.1-flash-image-preview, veo-3.1-fast-generate-preview, gemini-3.5-transcribe
    val selectedModel = MutableStateFlow("gemini-3.5-flash")
    val selectedLanguage = MutableStateFlow(SupportedLanguages.languages[0]) // Auto-detect default

    // Veo Video Aspect Ratio: "16:9" (landscape) or "9:16" (portrait)
    val videoAspectRatio = MutableStateFlow("16:9")

    // Audio recording state
    val isRecordingAudio = MutableStateFlow(false)
    val isLoading = MutableStateFlow(false)

    // Pro Subscription State & Dialogs
    val isProUser = MutableStateFlow(true)
    val showProDialog = MutableStateFlow(false)
    val showLanguageDialog = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            sessions.collect { list ->
                if (_currentSessionId.value == null && list.isNotEmpty()) {
                    _currentSessionId.value = list.first().id
                } else if (_currentSessionId.value == null && list.isEmpty()) {
                    createNewSession()
                }
            }
        }
    }

    fun createNewSession(initialTitle: String = "نئی گفتگو (New Chat)") {
        viewModelScope.launch {
            val newId = UUID.randomUUID().toString()
            val newSession = ChatSessionEntity(
                id = newId,
                title = initialTitle,
                modelUsed = selectedModel.value,
                languageCode = selectedLanguage.value.code
            )
            dao.insertSession(newSession)
            _currentSessionId.value = newId
            ttsManager.stop()
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
        ttsManager.stop()
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            dao.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
            }
        }
    }

    fun clearAllSessions() {
        viewModelScope.launch {
            dao.deleteAllSessions()
            _currentSessionId.value = null
            createNewSession()
        }
    }

    fun onImageSelected(uri: Uri?) {
        selectedImageUri.value = uri
        if (uri != null) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val context = getApplication<Application>()
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val originalBitmap = BitmapFactory.decodeStream(stream)
                        val maxDimension = 1024
                        val scale = minOf(
                            maxDimension.toFloat() / originalBitmap.width,
                            maxDimension.toFloat() / originalBitmap.height,
                            1.0f
                        )
                        val scaledBitmap = Bitmap.createScaledBitmap(
                            originalBitmap,
                            (originalBitmap.width * scale).toInt(),
                            (originalBitmap.height * scale).toInt(),
                            true
                        )
                        val outputStream = ByteArrayOutputStream()
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                        val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                        selectedImageBase64.value = base64
                    }
                } catch (e: Exception) {
                    selectedImageBase64.value = null
                }
            }
        } else {
            selectedImageBase64.value = null
        }
    }

    fun onBitmapCaptured(bitmap: Bitmap) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val file = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
                }
                selectedImageUri.value = Uri.fromFile(file)

                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                val base64 = Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
                selectedImageBase64.value = base64
            } catch (e: Exception) {
                // Ignore failure
            }
        }
    }

    fun removeSelectedImage() {
        selectedImageUri.value = null
        selectedImageBase64.value = null
    }

    fun toggleVideoAspectRatio() {
        videoAspectRatio.value = if (videoAspectRatio.value == "16:9") "9:16" else "16:9"
    }

    fun toggleAudioRecording() {
        if (!isRecordingAudio.value) {
            val file = audioRecorder.startRecording()
            if (file != null) {
                isRecordingAudio.value = true
                Toast.makeText(getApplication(), "آواز ریکارڈ ہو رہی ہے... بولیں", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(getApplication(), "ریکارڈنگ شروع نہیں ہو سکی", Toast.LENGTH_SHORT).show()
            }
        } else {
            val file = audioRecorder.stopRecording()
            isRecordingAudio.value = false
            if (file != null && file.exists()) {
                transcribeRecordedAudio(file)
            }
        }
    }

    private fun transcribeRecordedAudio(audioFile: File) {
        val activeSessionId = _currentSessionId.value ?: return
        val audioBase64 = audioRecorder.fileToBase64(audioFile) ?: return

        isLoading.value = true
        viewModelScope.launch {
            // Post a user audio bubble
            val userMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                sessionId = activeSessionId,
                role = "USER",
                text = "🎙️ [صوتی پیغام (Audio Voice)]",
                audioUri = audioFile.absolutePath,
                isTranscribedAudio = true
            )
            dao.insertMessage(userMsg)

            // Call gemini-3.5-transcribe
            val result = GeminiClient.transcribeAudio(audioBase64, getApplication())

            val modelMsg = ChatMessageEntity(
                id = UUID.randomUUID().toString(),
                sessionId = activeSessionId,
                role = "MODEL",
                text = "📝 **آڈیو ٹرانسکرپشن (gemini-3.5-transcribe):**\n\n${result.text}",
                isError = result.isError
            )
            dao.insertMessage(modelMsg)
            isLoading.value = false
        }
    }

    fun sendMessage(promptOverride: String? = null) {
        val prompt = (promptOverride ?: inputText.value).trim()
        val imageUriString = selectedImageUri.value?.toString()
        val imageBase64String = selectedImageBase64.value

        if (prompt.isEmpty() && imageBase64String == null) return

        val activeSessionId = _currentSessionId.value ?: return
        val currentModel = selectedModel.value

        // Clear input state immediately
        inputText.value = ""
        removeSelectedImage()
        isLoading.value = true

        viewModelScope.launch {
            val userMsgId = UUID.randomUUID().toString()
            val userMsg = ChatMessageEntity(
                id = userMsgId,
                sessionId = activeSessionId,
                role = "USER",
                text = prompt,
                imageUri = imageUriString,
                imageBase64 = imageBase64String
            )
            dao.insertMessage(userMsg)

            // Update session title dynamically
            val history = dao.getMessagesListForSession(activeSessionId)
            if (history.size <= 2) {
                val cleanTitle = if (prompt.length > 28) prompt.take(28) + "..." else prompt
                dao.updateSession(
                    ChatSessionEntity(
                        id = activeSessionId,
                        title = cleanTitle.ifBlank { "گفتگو (Chat)" },
                        lastModifiedAt = System.currentTimeMillis(),
                        modelUsed = currentModel,
                        languageCode = selectedLanguage.value.code
                    )
                )
            }

            when {
                // 1. Veo 3 Video Generation (Text to Video or Animate Image)
                currentModel == "veo-3.1-fast-generate-preview" ||
                        prompt.startsWith("ویڈیو بنائیں", ignoreCase = true) ||
                        prompt.startsWith("animate video", ignoreCase = true) ||
                        prompt.startsWith("generate video", ignoreCase = true) -> {
                    val result = GeminiClient.generateVeoVideo(
                        prompt = prompt,
                        sourceImageBase64 = imageBase64String,
                        aspectRatio = videoAspectRatio.value
                    )
                    val modelMsg = ChatMessageEntity(
                        id = UUID.randomUUID().toString(),
                        sessionId = activeSessionId,
                        role = "MODEL",
                        text = result.text,
                        videoUrl = result.videoUrl,
                        videoAspectRatio = result.videoAspectRatio,
                        isError = result.isError
                    )
                    dao.insertMessage(modelMsg)
                }

                // 2. Create & Edit Images using gemini-3.1-flash-image-preview
                currentModel == "gemini-3.1-flash-image-preview" ||
                        prompt.startsWith("تصویر بنائیں", ignoreCase = true) ||
                        prompt.startsWith("تصویر ایڈٹ", ignoreCase = true) ||
                        prompt.startsWith("edit image", ignoreCase = true) -> {
                    val result = GeminiClient.createOrEditImage(
                        prompt = prompt,
                        sourceImageBase64 = imageBase64String,
                        context = getApplication()
                    )
                    val modelMsg = ChatMessageEntity(
                        id = UUID.randomUUID().toString(),
                        sessionId = activeSessionId,
                        role = "MODEL",
                        text = result.text,
                        generatedImageUrl = result.generatedImageUrl,
                        isError = result.isError
                    )
                    dao.insertMessage(modelMsg)
                }

                // 3. Multi-turn Chat with Gemini 3.5 Flash / 3.1 Pro / 3.1 Flash Lite + Grounding
                else -> {
                    val result = GeminiClient.generateResponse(
                        history = history,
                        userPrompt = prompt,
                        imageBase64 = imageBase64String,
                        enableWebSearch = isWebSearchEnabled.value,
                        enableMapsGrounding = isMapsGroundingEnabled.value,
                        modelName = currentModel,
                        languageOption = selectedLanguage.value
                    )

                    val citationsJson = if (result.citations.isNotEmpty()) {
                        val array = JSONArray()
                        for (c in result.citations) {
                            val obj = JSONObject()
                            obj.put("title", c.title)
                            obj.put("uri", c.uri)
                            array.put(obj)
                        }
                        array.toString()
                    } else null

                    val searchQueriesJson = if (result.searchQueries.isNotEmpty()) {
                        val array = JSONArray()
                        for (q in result.searchQueries) array.put(q)
                        array.toString()
                    } else null

                    val modelMsg = ChatMessageEntity(
                        id = UUID.randomUUID().toString(),
                        sessionId = activeSessionId,
                        role = "MODEL",
                        text = result.text,
                        citationsJson = citationsJson,
                        searchQueriesJson = searchQueriesJson,
                        mapsDataJson = result.mapsDataJson,
                        isError = result.isError
                    )
                    dao.insertMessage(modelMsg)
                }
            }

            isLoading.value = false
        }
    }

    fun regenerateResponse(lastModelMessageId: String) {
        val activeSessionId = _currentSessionId.value ?: return
        isLoading.value = true

        viewModelScope.launch {
            val allMsgs = dao.getMessagesListForSession(activeSessionId)
            val modelMsgIndex = allMsgs.indexOfFirst { it.id == lastModelMessageId }
            if (modelMsgIndex <= 0) {
                isLoading.value = false
                return@launch
            }

            val userMsg = allMsgs[modelMsgIndex - 1]
            val historyPrior = allMsgs.take(modelMsgIndex - 1)

            val result = GeminiClient.generateResponse(
                history = historyPrior,
                userPrompt = userMsg.text,
                imageBase64 = userMsg.imageBase64,
                enableWebSearch = isWebSearchEnabled.value,
                enableMapsGrounding = isMapsGroundingEnabled.value,
                modelName = selectedModel.value,
                languageOption = selectedLanguage.value
            )

            val citationsJson = if (result.citations.isNotEmpty()) {
                val array = JSONArray()
                for (c in result.citations) {
                    val obj = JSONObject()
                    obj.put("title", c.title)
                    obj.put("uri", c.uri)
                    array.put(obj)
                }
                array.toString()
            } else null

            val updatedMsg = ChatMessageEntity(
                id = lastModelMessageId,
                sessionId = activeSessionId,
                role = "MODEL",
                text = result.text,
                citationsJson = citationsJson,
                searchQueriesJson = null,
                mapsDataJson = result.mapsDataJson,
                isError = result.isError,
                timestamp = System.currentTimeMillis()
            )
            dao.updateMessage(updatedMsg)
            isLoading.value = false
        }
    }

    fun setFeedback(messageId: String, feedback: Int) {
        viewModelScope.launch {
            dao.updateFeedback(messageId, feedback)
            val context = getApplication<Application>()
            val feedbackMsg = if (feedback > 0) "شکریہ! فیڈ بیک ریکارڈ کر لیا گیا" else "فیڈ بیک موصول ہوا۔"
            Toast.makeText(context, feedbackMsg, Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard(text: String) {
        val context = getApplication<Application>()
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Gemini AI Response", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "کاپی ہو گیا (Copied)", Toast.LENGTH_SHORT).show()
    }

    fun toggleSpeak(messageId: String, text: String) {
        ttsManager.speak(messageId, text, selectedLanguage.value.code)
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun toggleProStatus() {
        isProUser.value = !isProUser.value
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
