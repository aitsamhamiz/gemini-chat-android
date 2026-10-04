package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.LanguageOption
import com.example.ui.theme.GeminiAccentGold
import com.example.ui.theme.GeminiBlue
import com.example.ui.theme.GeminiPink
import com.example.ui.theme.GeminiPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    selectedModel: String,
    onModelSelect: (String) -> Unit,
    isWebSearchEnabled: Boolean,
    onToggleWebSearch: () -> Unit,
    isMapsGroundingEnabled: Boolean,
    onToggleMapsGrounding: () -> Unit,
    currentLanguage: LanguageOption,
    onLanguageClick: () -> Unit,
    onDrawerClick: () -> Unit,
    onProClick: () -> Unit,
    isPro: Boolean
) {
    var modelMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { modelMenuExpanded = true }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(GeminiBlue, GeminiPurple, GeminiPink)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Gemini AI",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Select Model",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = when (selectedModel) {
                                    "gemini-3.1-pro-preview" -> "Gemini 3.1 Pro (Complex Tasks & STEM)"
                                    "gemini-3.1-flash-lite-preview" -> "Gemini 3.1 Flash Lite (Ultra Fast)"
                                    "gemini-3.1-flash-image-preview" -> "Gemini Image (Create & Edit)"
                                    "veo-3.1-fast-generate-preview" -> "Veo 3 (Text & Image to Video)"
                                    "gemini-3.5-transcribe" -> "Gemini Audio Transcribe"
                                    else -> "Gemini 3.5 Flash (General & Search)"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Model selection dropdown
                        DropdownMenu(
                            expanded = modelMenuExpanded,
                            onDismissRequest = { modelMenuExpanded = false }
                        ) {
                            // 1. Gemini 3.5 Flash
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("⚡ Gemini 3.5 Flash", fontWeight = FontWeight.SemiBold)
                                        Text("عمومی سوالات اور لائیو سرچ (General Tasks)", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                trailingIcon = {
                                    if (selectedModel == "gemini-3.5-flash") {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = {
                                    onModelSelect("gemini-3.5-flash")
                                    modelMenuExpanded = false
                                }
                            )

                            // 2. Gemini 3.1 Pro
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("🧠 Gemini 3.1 Pro", fontWeight = FontWeight.SemiBold)
                                        Text("پیچیدہ کوڈنگ، حساب اور سائنس (Particularly Complex Tasks)", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                trailingIcon = {
                                    if (selectedModel == "gemini-3.1-pro-preview") {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = {
                                    onModelSelect("gemini-3.1-pro-preview")
                                    modelMenuExpanded = false
                                }
                            )

                            // 3. Gemini 3.1 Flash Lite
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("🚀 Gemini 3.1 Flash Lite", fontWeight = FontWeight.SemiBold)
                                        Text("انتہائی تیز ترین فوری جوابات (Fast Responses)", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                trailingIcon = {
                                    if (selectedModel == "gemini-3.1-flash-lite-preview") {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = {
                                    onModelSelect("gemini-3.1-flash-lite-preview")
                                    modelMenuExpanded = false
                                }
                            )

                            // 4. Gemini 3.1 Flash Image Preview (Create & Edit)
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("🎨 Gemini 3.1 Image Studio", fontWeight = FontWeight.SemiBold)
                                        Text("تصاویر بنائیں اور ایڈٹ کریں (Create & Edit Images)", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                trailingIcon = {
                                    if (selectedModel == "gemini-3.1-flash-image-preview") {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = {
                                    onModelSelect("gemini-3.1-flash-image-preview")
                                    modelMenuExpanded = false
                                }
                            )

                            // 5. Veo 3 Video Generator
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("🎬 Veo 3 Video Generator", fontWeight = FontWeight.SemiBold)
                                        Text("متن اور تصویر سے ویڈیو بنائیں (veo-3.1-fast-generate-preview)", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                trailingIcon = {
                                    if (selectedModel == "veo-3.1-fast-generate-preview") {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = {
                                    onModelSelect("veo-3.1-fast-generate-preview")
                                    modelMenuExpanded = false
                                }
                            )

                            // 6. Gemini 3.5 Transcribe
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text("🎙️ Gemini 3.5 Transcribe", fontWeight = FontWeight.SemiBold)
                                        Text("آڈیو اور مائیک آواز کو متن میں بدلیں (Transcribe Audio)", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                trailingIcon = {
                                    if (selectedModel == "gemini-3.5-transcribe") {
                                        Icon(Icons.Default.Check, contentDescription = "Selected", tint = MaterialTheme.colorScheme.primary)
                                    }
                                },
                                onClick = {
                                    onModelSelect("gemini-3.5-transcribe")
                                    modelMenuExpanded = false
                                }
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onDrawerClick,
                        modifier = Modifier.testTag("menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Chat History",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isPro) Brush.horizontalGradient(listOf(GeminiAccentGold, Color(0xFFFF8C00)))
                                else Brush.horizontalGradient(listOf(Color(0xFF374151), Color(0xFF1F2937)))
                            )
                            .clickable { onProClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Pro Plan",
                                tint = if (isPro) Color.Black else Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isPro) "PRO" else "UPGRADE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPro) Color.Black else Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )

            // Scrollable Quick Actions Strip: Google Search Grounding, Google Maps Grounding, Language
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 1. Google Search Grounding
                FilterChip(
                    selected = isWebSearchEnabled,
                    onClick = onToggleWebSearch,
                    label = {
                        Text(
                            text = if (isWebSearchEnabled) "🌐 سرچ: آن" else "سرچ: آف",
                            fontSize = 11.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isWebSearchEnabled) Icons.Default.Public else Icons.Default.PublicOff,
                            contentDescription = "Search Grounding",
                            modifier = Modifier.size(13.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                // 2. Google Maps Grounding
                FilterChip(
                    selected = isMapsGroundingEnabled,
                    onClick = onToggleMapsGrounding,
                    label = {
                        Text(
                            text = if (isMapsGroundingEnabled) "📍 میپس: آن" else "میپس: آف",
                            fontSize = 11.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Google Maps Grounding",
                            modifier = Modifier.size(13.dp)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                )

                // 3. Language Selector
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.clickable { onLanguageClick() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Change Language",
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentLanguage.nativeName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
