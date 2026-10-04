package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.ChatMessageEntity
import com.example.data.model.CitationParser
import com.example.data.model.WebCitation
import com.example.ui.theme.GeminiAccentGold
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiCardBorder
import com.example.ui.theme.GeminiCodeBg
import com.example.ui.theme.GeminiCyan
import com.example.ui.theme.GeminiPink
import com.example.ui.theme.GeminiPurple
import com.example.ui.theme.GeminiUserBubble
import org.json.JSONArray

@Composable
fun ChatMessageItem(
    message: ChatMessageEntity,
    isSpeaking: Boolean,
    isSpeakingThis: Boolean,
    onCopy: (String) -> Unit,
    onSpeakToggle: (String, String) -> Unit,
    onRegenerate: (String) -> Unit,
    onFeedback: (String, Int) -> Unit
) {
    val context = LocalContext.current
    val isUser = message.role == "USER"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .animateContentSize(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        if (isUser) {
            // USER MESSAGE BUBBLE
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.fillMaxWidth(0.88f)
            ) {
                // Attached image thumbnail if present
                if (!message.imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.parse(message.imageUri))
                            .crossfade(true)
                            .build(),
                        contentDescription = "Uploaded Image",
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .size(160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, GeminiCardBorder, RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Surface(
                    color = GeminiUserBubble,
                    shape = RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp),
                    tonalElevation = 2.dp,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.isTranscribedAudio) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice",
                                tint = GeminiCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = message.text,
                            color = Color.White,
                            fontSize = 15.sp,
                            lineHeight = 22.sp,
                            modifier = Modifier.testTag("user_message_text")
                        )
                    }
                }
            }
        } else {
            // MODEL (GEMINI) MESSAGE BUBBLE
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(listOf(GeminiBlue, GeminiPurple, GeminiPink))
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Gemini AI",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Veo 3 Video Card if present
                if (!message.videoUrl.isNullOrBlank()) {
                    VeoVideoCard(
                        videoUrl = message.videoUrl,
                        aspectRatio = message.videoAspectRatio ?: "16:9",
                        onPlayClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(message.videoUrl)).apply {
                                    setDataAndType(Uri.parse(message.videoUrl), "video/*")
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(message.videoUrl))
                                context.startActivity(browserIntent)
                            }
                        },
                        onShare = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Veo 3 AI Generated Video:\n${message.videoUrl}")
                            }
                            context.startActivity(Intent.createChooser(intent, "ویڈیو شیئر کریں (Share Video)"))
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // AI Generated / Edited Image display if present
                if (!message.generatedImageUrl.isNullOrBlank()) {
                    GeneratedImageCard(
                        imageUrl = message.generatedImageUrl,
                        onShare = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Generated with Gemini AI:\n${message.generatedImageUrl}")
                            }
                            context.startActivity(Intent.createChooser(intent, "تصویر شیئر کریں (Share Image)"))
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Message Text Content with Rich Formatting & Code Blocks
                RichMessageContent(
                    text = message.text,
                    onCopyCode = onCopy
                )

                // Google Maps Grounding Section if present
                if (!message.mapsDataJson.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    GoogleMapsGroundingSection(
                        mapsJson = message.mapsDataJson,
                        onMapClick = { uri ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    )
                }

                // Web Search Grounding Citations Section
                val citations = remember(message.citationsJson) {
                    CitationParser.parseCitations(message.citationsJson)
                }
                if (citations.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CitationsSection(
                        citations = citations,
                        onCitationClick = { uri ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    )
                }

                // Action Bar for AI response (TTS, Copy, Regenerate, Feedback, Share)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { onCopy(message.text) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy message",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSpeakToggle(message.id, message.text) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeakingThis) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = if (isSpeakingThis) "Stop audio" else "Listen",
                            tint = if (isSpeakingThis) GeminiPink else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onRegenerate(message.id) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Regenerate",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onFeedback(message.id, if (message.feedback == 1) 0 else 1) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (message.feedback == 1) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                            contentDescription = "Helpful",
                            tint = if (message.feedback == 1) GeminiBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { onFeedback(message.id, if (message.feedback == -1) 0 else -1) },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (message.feedback == -1) Icons.Default.ThumbDown else Icons.Default.ThumbDownOffAlt,
                            contentDescription = "Not helpful",
                            tint = if (message.feedback == -1) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, message.text)
                            }
                            context.startActivity(Intent.createChooser(intent, "شیئر کریں (Share)"))
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VeoVideoCard(
    videoUrl: String,
    aspectRatio: String,
    onPlayClick: () -> Unit,
    onShare: () -> Unit
) {
    val ratioFloat = if (aspectRatio == "9:16") 9f / 16f else 16f / 9f

    Surface(
        color = Color(0xFF0F1523),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiAccentGold.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Movie,
                        contentDescription = null,
                        tint = GeminiAccentGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🎬 Veo 3 Video Generator",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GeminiAccentGold
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color.Black.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = aspectRatio,
                        fontSize = 10.sp,
                        color = Color.LightGray,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Player Preview Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratioFloat)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF192238))
                    .clickable { onPlayClick() },
                contentAlignment = Alignment.Center
            ) {
                // Play Button
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(GeminiAccentGold, Color(0xFFFF8C00)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play Video",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Text(
                    text = "ویڈیو چلانے کے لیے ٹیپ کریں (Play Video)",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 10.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ماڈل: veo-3.1-fast-generate-preview",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onShare() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "شیئر کریں (Share)", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun GoogleMapsGroundingSection(
    mapsJson: String,
    onMapClick: (String) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "گوگل میپس مقامات (Google Maps Grounding):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "نقشہ اور درست لوکیشن کی تصدیق گوگل میپس ڈیٹا سے کی گئی ہے۔",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun RichMessageContent(
    text: String,
    onCopyCode: (String) -> Unit
) {
    val codeBlockRegex = Regex("```([a-zA-Z0-9_-]*)\\n?([\\s\\S]*?)```")
    val matches = codeBlockRegex.findAll(text).toList()

    if (matches.isEmpty()) {
        FormattedMarkdownText(text)
    } else {
        var lastIndex = 0
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (match in matches) {
                val preText = text.substring(lastIndex, match.range.first)
                if (preText.isNotBlank()) {
                    FormattedMarkdownText(preText)
                }

                val language = match.groupValues[1].ifBlank { "code" }
                val codeContent = match.groupValues[2].trimEnd()

                CodeBlockCard(
                    language = language,
                    code = codeContent,
                    onCopy = { onCopyCode(codeContent) }
                )

                lastIndex = match.range.last + 1
            }

            val postText = text.substring(lastIndex)
            if (postText.isNotBlank()) {
                FormattedMarkdownText(postText)
            }
        }
    }
}

@Composable
fun FormattedMarkdownText(content: String) {
    val annotated = remember(content) {
        buildAnnotatedString {
            val lines = content.split("\n")
            for (line in lines) {
                when {
                    line.startsWith("### ") -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp)) {
                            append(line.removePrefix("### "))
                        }
                    }
                    line.startsWith("## ") -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, fontSize = 17.sp)) {
                            append(line.removePrefix("## "))
                        }
                    }
                    line.startsWith("# ") -> {
                        withStyle(SpanStyle(fontWeight = FontWeight.Black, fontSize = 19.sp)) {
                            append(line.removePrefix("# "))
                        }
                    }
                    line.startsWith("* ") || line.startsWith("- ") -> {
                        append("  • ")
                        appendFormattedInline(line.drop(2))
                    }
                    else -> {
                        appendFormattedInline(line)
                    }
                }
                append("\n")
            }
        }
    }

    Text(
        text = annotated,
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 15.sp,
        lineHeight = 23.sp
    )
}

private fun androidx.compose.ui.text.AnnotatedString.Builder.appendFormattedInline(raw: String) {
    val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
    var last = 0
    for (match in boldRegex.findAll(raw)) {
        append(raw.substring(last, match.range.first))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
            append(match.groupValues[1])
        }
        last = match.range.last + 1
    }
    append(raw.substring(last))
}

@Composable
fun CodeBlockCard(
    language: String,
    code: String,
    onCopy: () -> Unit
) {
    Surface(
        color = GeminiCodeBg,
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GeminiCardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF131722))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.lowercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = GeminiBlue,
                    fontFamily = FontFamily.Monospace
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onCopy() }
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = Color.LightGray,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Copy",
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }
            }

            Text(
                text = code,
                color = Color(0xFFD6DEEB),
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CitationsSection(
    citations: List<WebCitation>,
    onCitationClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.OpenInBrowser,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "حوالہ جات اور ویب سورسز (Live Sources):",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            citations.take(6).forEachIndexed { index, citation ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, GeminiCardBorder),
                    modifier = Modifier.clickable { onCitationClick(citation.uri) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[${index + 1}]",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (citation.title.length > 24) citation.title.take(24) + "..." else citation.title,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GeneratedImageCard(
    imageUrl: String,
    onShare: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF131722))
            .border(1.dp, GeminiCardBorder, RoundedCornerShape(16.dp))
            .padding(8.dp)
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "AI Generated Visual",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(12.dp))
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "✨ Gemini 3.1 Image Studio",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GeminiPink
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onShare() }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "شیئر کریں (Share)",
                    fontSize = 11.sp,
                    color = Color.White
                )
            }
        }
    }
}
