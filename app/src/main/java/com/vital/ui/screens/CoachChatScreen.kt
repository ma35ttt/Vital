package com.vital.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vital.data.entity.AiProvider
import com.vital.ui.CoachMsg
import com.vital.ui.CoachUiState
import com.vital.ui.CoachViewModel
import com.vital.ui.ImageEncoding
import com.vital.ui.ImageEncoding.toDisplay
import com.vital.ui.components.EditorialButton
import com.vital.ui.components.EditorialEyebrow
import com.vital.ui.components.Hairline
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts

private val SUGGESTIONS = listOf(
    "What should I eat to hit my protein?",
    "Am I on track for today?",
    "Review my last workout",
    "Plan a 3-day training week"
)

/** A picked or captured photo waiting to be sent, with its encoded payload. */
private data class PendingImage(val image: ImageBitmap, val base64: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachChatScreen(
    onClose: () -> Unit,
    viewModel: CoachViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val keyboard = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()

    var input by remember { mutableStateOf("") }
    var pending by remember { mutableStateOf<PendingImage?>(null) }
    var sheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    val gallery = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            ImageEncoding.uriToImage(context, uri)?.let { bmp ->
                pending = PendingImage(bmp.toDisplay(), ImageEncoding.toBase64Jpeg(bmp))
            }
        }
    }
    val camera = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            pending = PendingImage(bmp.toDisplay(), ImageEncoding.toBase64Jpeg(bmp))
        }
    }
    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> if (granted) camera.launch(null) }

    val launchCamera: () -> Unit = {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) camera.launch(null) else cameraPermission.launch(Manifest.permission.CAMERA)
    }

    // Keep the newest turn in view as the conversation grows or a reply lands.
    LaunchedEffect(state.messages.size, state.sending) {
        val count = state.messages.size + if (state.sending) 1 else 0
        if (count > 0) listState.animateScrollToItem(count - 1)
    }

    val submit: () -> Unit = {
        val p = pending
        viewModel.send(input, p?.image, p?.base64)
        input = ""
        pending = null
        keyboard?.hide()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VitalColors.paper)
            .imePadding()
            .padding(horizontal = 24.dp)
            .padding(top = 18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "‹ More",
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp,
                letterSpacing = 0.02.em,
                modifier = Modifier
                    .clickable(onClick = onClose)
                    .padding(vertical = 4.dp, horizontal = 1.dp)
            )
            Text(
                text = providerLabel(state.provider, state.hasKey),
                color = if (state.hasKey) VitalColors.orange else VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 11.sp,
                letterSpacing = 0.04.em
            )
        }

        EditorialEyebrow(
            text = "Coach",
            modifier = Modifier.padding(top = 18.dp)
        )
        Text(
            text = if (state.hasKey) "Coach" else "Connect a model",
            color = VitalColors.ink,
            fontFamily = VitalFonts.InterTight,
            fontSize = 23.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = (-0.025).em,
            modifier = Modifier.padding(top = 6.dp)
        )
        Hairline(modifier = Modifier.padding(top = 14.dp))

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                !state.hasKey -> ConnectPanel(
                    provider = state.provider,
                    onConnect = { provider, key -> viewModel.connect(provider, key) }
                )
                state.messages.isEmpty() && !state.sending -> EmptyState(
                    onSuggest = { viewModel.send(it, null, null) }
                )
                else -> Thread(state = state, listState = listState)
            }
        }

        if (state.hasKey) {
            Composer(
                error = state.error,
                input = input,
                pending = pending,
                onInput = { input = it },
                onAttach = { sheetOpen = true },
                onRemoveImage = { pending = null },
                onSubmit = submit,
                onRetry = viewModel::retry
            )
        }
    }

    if (sheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { sheetOpen = false },
            sheetState = sheetState,
            containerColor = VitalColors.paper,
            dragHandle = { SheetHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 30.dp)
            ) {
                Text(
                    text = "ADD A PHOTO",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.16.em,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                SheetRow(
                    icon = Icons.Outlined.PhotoCamera,
                    label = "Take photo",
                    tag = "CAMERA"
                ) {
                    sheetOpen = false
                    launchCamera()
                }
                SheetRow(
                    icon = Icons.Outlined.PhotoLibrary,
                    label = "Choose from library",
                    tag = "GALLERY"
                ) {
                    sheetOpen = false
                    gallery.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            }
        }
    }
}

// ---- states --------------------------------------------------------------------------------

@Composable
private fun EmptyState(onSuggest: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ask about today's food, your last session, or what to do next. " +
                "The coach reads your live targets and logs.",
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.InterTight,
            fontSize = 13.5.sp,
            lineHeight = 21.sp,
            modifier = Modifier.fillMaxWidth(0.86f)
        )
        Text(
            text = "TRY ASKING",
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.InterTight,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.18.em,
            modifier = Modifier.padding(top = 24.dp)
        )
        Spacer(Modifier.height(4.dp))
        Hairline()
        SUGGESTIONS.forEach { suggestion ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSuggest(suggestion) }
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = suggestion,
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "›",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 12.sp
                )
            }
            Hairline()
        }
    }
}

@Composable
private fun Thread(state: CoachUiState, listState: LazyListState) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        items(state.messages, key = { it.id }) { message ->
            if (message.isUser) UserTurn(message) else CoachTurn(message)
        }
        if (state.sending) {
            item(key = "thinking") {
                Column {
                    CoachWho()
                    Text(
                        text = "Thinking…",
                        color = VitalColors.inkMuted,
                        fontFamily = VitalFonts.InterTight,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(top = 7.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserTurn(message: CoachMsg) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        if (message.image != null) {
            Image(
                bitmap = message.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(180.dp)
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, VitalColors.rule, RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.height(8.dp))
        }
        Box(
            modifier = Modifier
                .widthIn(max = 296.dp)
                .background(VitalColors.sheet, RoundedCornerShape(14.dp, 14.dp, 4.dp, 14.dp))
                .border(1.dp, VitalColors.rule, RoundedCornerShape(14.dp, 14.dp, 4.dp, 14.dp))
                .padding(horizontal = 14.dp, vertical = 11.dp)
        ) {
            Text(
                text = message.text,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 14.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun CoachTurn(message: CoachMsg) {
    Column(modifier = Modifier.fillMaxWidth(0.94f)) {
        CoachWho()
        Text(
            text = message.text,
            color = VitalColors.ink,
            fontFamily = VitalFonts.InterTight,
            fontSize = 14.sp,
            lineHeight = 21.sp,
            modifier = Modifier.padding(top = 7.dp)
        )
        if (message.chips.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 9.dp)
            ) {
                message.chips.forEach { chip ->
                    Box(
                        modifier = Modifier
                            .border(1.dp, VitalColors.ruleStrong, RoundedCornerShape(5.dp))
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = chip,
                            color = VitalColors.orange,
                            fontFamily = VitalFonts.JetBrainsMonoRegular,
                            fontSize = 8.sp,
                            letterSpacing = 0.08.em
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachWho() {
    Text(
        text = "COACH",
        color = VitalColors.orange,
        fontFamily = VitalFonts.InterTight,
        fontSize = 9.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.18.em
    )
}

// ---- no key --------------------------------------------------------------------------------

@Composable
private fun ConnectPanel(
    provider: AiProvider,
    onConnect: (AiProvider, String) -> Unit
) {
    var selected by remember(provider) { mutableStateOf(provider) }
    var key by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "The coach runs on your own AI account. Pick a provider and paste an API key " +
                "once — it's stored on this device and used only for your messages.",
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.InterTight,
            fontSize = 13.5.sp,
            lineHeight = 21.sp,
            modifier = Modifier.fillMaxWidth(0.92f)
        )
        Spacer(Modifier.height(20.dp))
        ProviderTabs(selected = selected, onSelect = { selected = it })
        Spacer(Modifier.height(16.dp))
        KeyField(value = key, onValueChange = { key = it })
        Spacer(Modifier.height(18.dp))
        EditorialButton(
            text = "Save key",
            enabled = key.isNotBlank(),
            onClick = { onConnect(selected, key) }
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Your key never leaves the device except to call the provider you chose.",
            color = VitalColors.navInactive,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 11.sp,
            lineHeight = 16.sp
        )
    }
}

@Composable
private fun ProviderTabs(selected: AiProvider, onSelect: (AiProvider) -> Unit) {
    val options = listOf(AiProvider.GEMINI to "Gemini", AiProvider.OPENROUTER to "OpenRouter")
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
            options.forEach { (option, label) ->
                val isSelected = option == selected
                Column(
                    modifier = Modifier
                        .width(IntrinsicSize.Min)
                        .clickable { onSelect(option) }
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) VitalColors.ink else VitalColors.inkMuted,
                        fontFamily = VitalFonts.JetBrainsMonoRegular,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(if (isSelected) VitalColors.orange else Color.Transparent)
                    )
                }
            }
        }
        Hairline()
    }
}

@Composable
private fun KeyField(value: String, onValueChange: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .border(1.dp, VitalColors.rule, RoundedCornerShape(8.dp))
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = VitalColors.ink,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 12.sp
            ),
            cursorBrush = SolidColor(VitalColors.orange),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(
                        text = "Paste API key…",
                        color = VitalColors.inkMuted,
                        fontFamily = VitalFonts.JetBrainsMonoRegular,
                        fontSize = 12.sp
                    )
                }
                inner()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ---- composer ------------------------------------------------------------------------------

@Composable
private fun Composer(
    error: String?,
    input: String,
    pending: PendingImage?,
    onInput: (String) -> Unit,
    onAttach: () -> Unit,
    onRemoveImage: () -> Unit,
    onSubmit: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        if (error != null) {
            Hairline()
            ErrorRow(message = error, onRetry = onRetry)
        }
        Hairline()
        if (pending != null) {
            AttachRow(image = pending.image, onRemove = onRemoveImage)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .border(1.dp, VitalColors.rule, RoundedCornerShape(16.dp))
                .padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(onClick = onAttach),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = "Add a photo",
                    tint = VitalColors.inkMuted,
                    modifier = Modifier.size(21.dp)
                )
            }

            val canSend = input.isNotBlank() || pending != null

            BasicTextField(
                value = input,
                onValueChange = onInput,
                textStyle = TextStyle(
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 13.sp
                ),
                cursorBrush = SolidColor(VitalColors.orange),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { if (canSend) onSubmit() }
                ),
                decorationBox = { inner ->
                    Box(modifier = Modifier.padding(vertical = 9.dp)) {
                        if (input.isEmpty()) {
                            Text(
                                text = "Ask your coach",
                                color = VitalColors.inkMuted,
                                fontFamily = VitalFonts.InterTight,
                                fontSize = 13.sp
                            )
                        }
                        inner()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp)
            )

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(enabled = canSend, onClick = onSubmit),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Send",
                    tint = if (canSend) VitalColors.orange else VitalColors.navInactive,
                    modifier = Modifier.size(21.dp)
                )
            }
        }
    }
}

@Composable
private fun ErrorRow(message: String, onRetry: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 5.dp)
                .size(7.dp)
                .background(VitalColors.orange, CircleShape)
        )
        Text(
            text = message,
            color = VitalColors.ink,
            fontFamily = VitalFonts.InterTight,
            fontSize = 12.5.sp,
            lineHeight = 18.sp,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
        )
        Text(
            text = "RETRY",
            color = VitalColors.orange,
            fontFamily = VitalFonts.JetBrainsMonoRegular,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.08.em,
            modifier = Modifier
                .clickable(onClick = onRetry)
                .padding(top = 3.dp)
        )
    }
}

@Composable
private fun AttachRow(image: ImageBitmap, onRemove: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Box(modifier = Modifier.size(58.dp)) {
            Image(
                bitmap = image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(11.dp))
                    .border(1.dp, VitalColors.rule, RoundedCornerShape(11.dp))
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .size(19.dp)
                    .background(VitalColors.ink, CircleShape)
                    .clickable(onClick = onRemove),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "×",
                    color = VitalColors.paper,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---- attach sheet --------------------------------------------------------------------------

@Composable
private fun SheetHandle() {
    Box(
        modifier = Modifier
            .padding(top = 10.dp, bottom = 14.dp)
            .size(width = 36.dp, height = 4.dp)
            .background(VitalColors.ruleStrong, RoundedCornerShape(2.dp))
    )
}

@Composable
private fun SheetRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tag: String,
    onClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Hairline()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = VitalColors.ink,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = label,
                color = VitalColors.ink,
                fontFamily = VitalFonts.InterTight,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp)
            )
            Text(
                text = tag,
                color = VitalColors.inkMuted,
                fontFamily = VitalFonts.JetBrainsMonoRegular,
                fontSize = 12.sp
            )
        }
    }
}

// ---- helpers -------------------------------------------------------------------------------

private fun providerLabel(provider: AiProvider, connected: Boolean): String {
    if (!connected) return "NOT CONNECTED"
    return when (provider) {
        AiProvider.GEMINI -> "GEMINI · FLASH"
        AiProvider.OPENROUTER -> "OPENROUTER · LLAMA 3.1"
    }
}
