package com.vital.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vital.ui.components.BigKcalDisplay
import com.vital.ui.components.MacroRow
import com.vital.ui.components.SectionHeader
import com.vital.ui.components.capsuleRow
import com.vital.ui.components.ShowMoreButton
import com.vital.ui.components.HeroLineGraph
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalTypography
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.LocalDate
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vital.ui.MainViewModel
import androidx.compose.material.icons.Icons
import com.vital.ui.components.Tag
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.filled.SmartToy

@Composable
fun CoachChatScreen(
    onClose: () -> Unit,
    viewModel: MainViewModel = viewModel()
) {
    val today = LocalDate.now()
    val dayTag = today.dayOfWeek.name.take(3)
    val dateTag = today.month.name.take(3) + ".${today.dayOfMonth}"

    val messages by viewModel.coachMessages.collectAsStateWithLifecycle(emptyList())
    val isLoading by viewModel.coachLoading.collectAsStateWithLifecycle(false)

    var inputText by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = VitalColors.Background
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Top bar with close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Tag(text = dayTag, backgroundColor = VitalColors.AccentSET)
                Text(
                    text = "COACH",
                    color = VitalColors.PrimaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
                androidx.compose.material3.Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(onClick = onClose),
                    color = VitalColors.NearBlackRow,
                    shape = CircleShape,
                    border = BorderStroke(1.5.dp, VitalColors.NearBlackRowStroke)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = VitalColors.PrimaryText, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            // Chat messages
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                reverseLayout = true,
                contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                itemsIndexed(messages.reversed()) { index, message ->
                    ChatBubble(
                        text = message.text,
                        isUser = message.isUser,
                        accentColor = if (message.isUser) VitalColors.AccentTDY else VitalColors.AccentMLS
                    )
                }

                if (isLoading) {
                    item {
                        ChatBubble(
                            text = "Thinking...",
                            isUser = false,
                            accentColor = VitalColors.AccentMLS,
                            isLoading = true
                        )
                    }
                }
            }

            // Input area
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                color = VitalColors.Card,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, VitalColors.NearBlackRowStroke)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Attach photo",
                        tint = VitalColors.MutedText,
                        modifier = Modifier.padding(end = 8.dp).clickable { /* TODO: Attach photo for food analysis */ }
                    )

                    TextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 8.dp),
                        placeholder = { Text(text = "ASK COACH...", color = VitalColors.MutedText, fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 1.sp) },
                        singleLine = false,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = androidx.compose.ui.text.input.ImeAction.Send),
                        visualTransformation = VisualTransformation.None,
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            cursorColor = VitalColors.AccentTDY,
                            focusedPlaceholderColor = VitalColors.MutedText,                             unfocusedPlaceholderColor = VitalColors.MutedText,
                            focusedTextColor = VitalColors.PrimaryText,                             unfocusedTextColor = VitalColors.PrimaryText,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            errorContainerColor = Color.Transparent,
                            errorTextColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    androidx.compose.material3.Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .clickable {
                                if (inputText.isNotBlank()) {
                                    /* viewModel.sendCoachMessage(inputText) */
                                    inputText = ""
                                }
                            },
                        color = if (inputText.isNotBlank()) VitalColors.AccentTDY else VitalColors.NearBlackRow,
                        shape = CircleShape
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = if (inputText.isNotBlank()) Color.Black else VitalColors.MutedText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    text: String,
    isUser: Boolean,
    accentColor: Color,
    isLoading: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // Avatar
            Surface(
                modifier = Modifier.size(32.dp),
                color = accentColor.copy(alpha = 0.2f),
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.SmartToy, contentDescription = "Coach", tint = accentColor, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(vertical = 4.dp),
                color = if (isUser) accentColor else VitalColors.NearBlackRow,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                border = BorderStroke(1.dp, if (isUser) Color.Transparent else VitalColors.NearBlackRowStroke)
            ) {
                Text(
                    text = text,
                    color = if (isUser) Color.Black else VitalColors.PrimaryText,
                    fontSize = 14.sp,
                    fontWeight = if (isLoading) FontWeight.Normal else FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User avatar
            Surface(
                modifier = Modifier.size(32.dp),
                color = VitalColors.AccentTDY.copy(alpha = 0.2f),
                shape = CircleShape
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "U", color = VitalColors.AccentTDY, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

object CoachChatScreenPreviewData {
    val sampleMessages = listOf(
        ChatMessage("Hey! How can I help you with your fitness goals today?", false),
        ChatMessage("I want to lose 5kg in 8 weeks", true),
        ChatMessage("Great goal! That's about 0.6kg per week - very sustainable. Let me help you set up your targets. What's your current weight and activity level?", false),
        ChatMessage("I'm 85kg, work desk job, gym 3x week", true),
        ChatMessage("Perfect. For 85kg with 3x/week lifting, I'd suggest ~2200 kcal with 180g protein, 200g carbs, 75g fat. Want me to set these as your targets?", false)
    )
}