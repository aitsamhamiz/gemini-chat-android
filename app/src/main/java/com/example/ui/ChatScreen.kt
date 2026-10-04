package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ChatDrawer
import com.example.ui.components.ChatInputBar
import com.example.ui.components.ChatMessageItem
import com.example.ui.components.ChatTopBar
import com.example.ui.components.LanguageSelectionDialog
import com.example.ui.components.ProSubscriptionDialog
import com.example.ui.theme.GeminiAccentGold
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiCardBorder
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiPink
import com.example.ui.theme.GeminiPurple
import com.example.ui.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val currentSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
    val messages by viewModel.messages.collectAsStateWithLifecycle()

    val inputText by viewModel.inputText.collectAsStateWithLifecycle()
    val selectedImageUri by viewModel.selectedImageUri.collectAsStateWithLifecycle()

    val isWebSearchEnabled by viewModel.isWebSearchEnabled.collectAsStateWithLifecycle()
    val isMapsGroundingEnabled by viewModel.isMapsGroundingEnabled.collectAsStateWithLifecycle()

    val selectedModel by viewModel.selectedModel.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val videoAspectRatio by viewModel.videoAspectRatio.collectAsStateWithLifecycle()
    val isRecordingAudio by viewModel.isRecordingAudio.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()

    val isSpeaking by viewModel.ttsManager.isSpeaking.collectAsStateWithLifecycle()
    val currentSpeakingMessageId by viewModel.ttsManager.currentMessageId.collectAsStateWithLifecycle()

    val isPro by viewModel.isProUser.collectAsStateWithLifecycle()
    val showProDialog by viewModel.showProDialog.collectAsStateWithLifecycle()
    val showLanguageDialog by viewModel.showLanguageDialog.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    // Auto-scroll on new message
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ChatDrawer(
                sessions = sessions,
                currentSessionId = currentSessionId,
                onSessionClick = { id ->
                    viewModel.selectSession(id)
                    scope.launch { drawerState.close() }
                },
                onNewChatClick = {
                    viewModel.createNewSession()
                    scope.launch { drawerState.close() }
                },
                onDeleteSession = { id -> viewModel.deleteSession(id) },
                onClearAll = { viewModel.clearAllSessions() },
                onProClick = { viewModel.showProDialog.value = true },
                onLanguageClick = { viewModel.showLanguageDialog.value = true },
                isPro = isPro
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                ChatTopBar(
                    selectedModel = selectedModel,
                    onModelSelect = { viewModel.selectedModel.value = it },
                    isWebSearchEnabled = isWebSearchEnabled,
                    onToggleWebSearch = { viewModel.isWebSearchEnabled.value = !isWebSearchEnabled },
                    isMapsGroundingEnabled = isMapsGroundingEnabled,
                    onToggleMapsGrounding = { viewModel.isMapsGroundingEnabled.value = !isMapsGroundingEnabled },
                    currentLanguage = selectedLanguage,
                    onLanguageClick = { viewModel.showLanguageDialog.value = true },
                    onDrawerClick = { scope.launch { drawerState.open() } },
                    onProClick = { viewModel.showProDialog.value = true },
                    isPro = isPro
                )
            },
            bottomBar = {
                ChatInputBar(
                    text = inputText,
                    onTextChange = { viewModel.inputText.value = it },
                    selectedImageUri = selectedImageUri,
                    onImageSelected = { viewModel.onImageSelected(it) },
                    onBitmapCaptured = { viewModel.onBitmapCaptured(it) },
                    onRemoveImage = { viewModel.removeSelectedImage() },
                    onSend = { viewModel.sendMessage() },
                    isLoading = isLoading,
                    selectedModel = selectedModel,
                    videoAspectRatio = videoAspectRatio,
                    onToggleAspectRatio = { viewModel.toggleVideoAspectRatio() },
                    isRecordingAudio = isRecordingAudio,
                    onToggleAudioRecording = { viewModel.toggleAudioRecording() },
                    languageLocale = selectedLanguage.speechLocale
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (messages.isEmpty() && !isLoading) {
                    WelcomeSuggestionsView(
                        onSuggestionClick = { prompt, model ->
                            if (model != null) {
                                viewModel.selectedModel.value = model
                            }
                            viewModel.sendMessage(promptOverride = prompt)
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            ChatMessageItem(
                                message = message,
                                isSpeaking = isSpeaking,
                                isSpeakingThis = isSpeaking && currentSpeakingMessageId == message.id,
                                onCopy = { viewModel.copyToClipboard(it) },
                                onSpeakToggle = { id, text -> viewModel.toggleSpeak(id, text) },
                                onRegenerate = { id -> viewModel.regenerateResponse(id) },
                                onFeedback = { id, feedback -> viewModel.setFeedback(id, feedback) }
                            )
                        }

                        if (isLoading) {
                            item {
                                GeminiThinkingIndicator(
                                    selectedModel = selectedModel,
                                    isSearching = isWebSearchEnabled,
                                    isMaps = isMapsGroundingEnabled
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = selectedLanguage,
            onLanguageSelect = { viewModel.selectedLanguage.value = it },
            onDismiss = { viewModel.showLanguageDialog.value = false }
        )
    }

    if (showProDialog) {
        ProSubscriptionDialog(
            isPro = isPro,
            onTogglePro = { viewModel.toggleProStatus() },
            onDismiss = { viewModel.showProDialog.value = false }
        )
    }
}

@Composable
fun WelcomeSuggestionsView(
    onSuggestionClick: (String, String?) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(GeminiBlue, GeminiPurple, GeminiPink))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "Gemini AI",
                tint = Color.White,
                modifier = Modifier.size(34.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "السلام علیکم! Gemini AI میں خوش آمدید",
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "چیٹ، لائیو سرچ، میپس ڈیٹا، Veo 3 ویڈیو، امیج اسٹوڈیو، اور آڈیو ٹرانسکرپشن",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp,
            modifier = Modifier.padding(horizontal = 10.dp)
        )

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "تجویز کردہ خصوصیات (Suggested Features):",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // 1. Veo 3 Video Generation
            SuggestionCard(
                icon = Icons.Default.Movie,
                accentColor = GeminiAccentGold,
                title = "🎬 Veo 3 ویڈیو جنریشن (veo-3.1-fast-generate-preview)",
                description = "ٹیکسٹ یا تصویر سے 16:9 یا 9:16 سنیماٹک ویڈیو بنائیں",
                prompt = "A breathtaking cinematic drone shot of northern Pakistan Karakoram mountains at sunset with snow covered peaks",
                model = "veo-3.1-fast-generate-preview",
                onClick = onSuggestionClick
            )

            // 2. Google Maps Grounding
            SuggestionCard(
                icon = Icons.Default.Map,
                accentColor = GeminiCyan,
                title = "📍 گوگل میپس گراؤنڈنگ (Google Maps Data)",
                description = "اسلام آباد اور لاہور کے مشہور ریسٹورنٹس اور مقامات مع لنکس",
                prompt = "اسلام آباد اور راولپنڈی کے بہترین روایتی کھانوں کے مشہور مقامات اور ان کی تفصیلات گوگل میپس ڈیٹا کے ساتھ بتائیں۔",
                model = "gemini-3.5-flash",
                onClick = onSuggestionClick
            )

            // 3. Gemini 3.1 Flash Image Preview (Create & Edit)
            SuggestionCard(
                icon = Icons.Default.Image,
                accentColor = GeminiPink,
                title = "🎨 تصاویر بنائیں اور ایڈٹ کریں (gemini-3.1-flash-image)",
                description = "ہائی کوالٹی AI تصویر تخلیق یا ترمیم کریں",
                prompt = "A futuristic Pakistani tech city with neon calligraphy billboards and flying electric cars",
                model = "gemini-3.1-flash-image-preview",
                onClick = onSuggestionClick
            )

            // 4. Gemini 3.1 Pro (Complex Tasks & STEM)
            SuggestionCard(
                icon = Icons.Default.Code,
                accentColor = GeminiBlue,
                title = "🧠 Gemini 3.1 Pro (Complex Tasks & STEM)",
                description = "پیچیدہ الگورتھمز، کوڈنگ اور تفصیلی تجزیہ",
                prompt = "Python میں ایک مکمل Asynchronous WebSocket چیٹ سرور کا کوڈ مع مکمل تفصیلی وضاحت لکھیں۔",
                model = "gemini-3.1-pro-preview",
                onClick = onSuggestionClick
            )
        }
    }
}

@Composable
fun SuggestionCard(
    icon: ImageVector,
    accentColor: Color,
    title: String,
    description: String,
    prompt: String,
    model: String?,
    onClick: (String, String?) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiCardBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(prompt, model) }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

@Composable
fun GeminiThinkingIndicator(
    selectedModel: String,
    isSearching: Boolean,
    isMaps: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(GeminiBlue, GeminiPurple))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            strokeWidth = 2.dp,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = when (selectedModel) {
                "veo-3.1-fast-generate-preview" -> "Veo 3 ویڈیو تیار کی جا رہی ہے... (Generating Video...)"
                "gemini-3.1-flash-image-preview" -> "تصویر کی پروسیسنگ جاری ہے... (Processing Image...)"
                "gemini-3.5-transcribe" -> "آڈیو کو ٹیکسٹ میں تبدیل کیا جا رہا ہے... (Transcribing Audio...)"
                else -> when {
                    isMaps -> "گوگل میپس ڈیٹا کی تصدیق اور جواب... (Maps Grounding...)"
                    isSearching -> "ویب تلاش اور جواب مرتب کیا جا رہا ہے... (Web Search Grounding...)"
                    else -> "Gemini سوچ رہا ہے... (Thinking...)"
                }
            },
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
