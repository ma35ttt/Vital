package com.vital.ui.update

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vital.data.update.UpdateState
import com.vital.ui.components.EditorialButton
import com.vital.ui.components.EditorialEyebrow
import com.vital.ui.theme.VitalColors
import com.vital.ui.theme.VitalFonts
import kotlin.math.roundToInt

/**
 * Hosts the launch check and the release-notes dialog for the whole app. Placed once in the
 * app shell so the dialog can appear over any screen, including a manual check from Settings.
 */
@Composable
fun UpdateHost(viewModel: UpdateViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.check(manual = false) }
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) { viewModel.onResume() }

    UpdateDialog(
        state = state,
        currentVersion = viewModel.currentVersion,
        onUpdateNow = viewModel::updateNow,
        onOpenInstaller = viewModel::installReady,
        onOpenSettings = viewModel::openSettings,
        onRetry = viewModel::retry,
        onLater = viewModel::dismiss
    )
}

@Composable
fun UpdateDialog(
    state: UpdateState,
    currentVersion: String,
    onUpdateNow: () -> Unit,
    onOpenInstaller: () -> Unit,
    onOpenSettings: () -> Unit,
    onRetry: () -> Unit,
    onLater: () -> Unit
) {
    val manifest = when (state) {
        is UpdateState.Available -> state.manifest
        is UpdateState.Downloading -> state.manifest
        is UpdateState.Ready -> state.manifest
        is UpdateState.NeedsPermission -> state.manifest
        is UpdateState.Failed -> state.manifest
        else -> null
    } ?: return

    Dialog(
        onDismissRequest = onLater,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            color = VitalColors.sheet,
            border = BorderStroke(1.dp, VitalColors.rule)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                EditorialEyebrow(text = "Update")
                Text(
                    text = "Vital ${manifest.versionName}",
                    color = VitalColors.ink,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.02).em,
                    modifier = Modifier.padding(top = 10.dp)
                )
                Text(
                    text = "You're on $currentVersion",
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )

                when (state) {
                    is UpdateState.Downloading -> DownloadingBody(state.progress)
                    is UpdateState.Ready -> BodyText(
                        "The update is verified. The system installer will open next."
                    )
                    is UpdateState.NeedsPermission -> BodyText(
                        "Allow Vital to install updates, then come back. Android will still ask you to confirm the install."
                    )
                    is UpdateState.Failed -> BodyText(state.message, isError = true)
                    else -> NotesBody(manifest.notes)
                }

                Spacer(Modifier.height(22.dp))
                when (state) {
                    is UpdateState.Downloading -> {
                        SecondaryButton(text = "Cancel", onClick = onLater)
                    }
                    is UpdateState.Ready -> {
                        EditorialButton(text = "Open installer", onClick = onOpenInstaller)
                        Spacer(Modifier.height(8.dp))
                        SecondaryButton(text = "Later", onClick = onLater)
                    }
                    is UpdateState.NeedsPermission -> {
                        EditorialButton(text = "Open settings", onClick = onOpenSettings)
                        Spacer(Modifier.height(8.dp))
                        SecondaryButton(text = "Later", onClick = onLater)
                    }
                    is UpdateState.Failed -> {
                        EditorialButton(text = "Try again", onClick = onRetry)
                        Spacer(Modifier.height(8.dp))
                        SecondaryButton(text = "Later", onClick = onLater)
                    }
                    else -> {
                        EditorialButton(text = "Update now", onClick = onUpdateNow)
                        Spacer(Modifier.height(8.dp))
                        SecondaryButton(text = "Later", onClick = onLater)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotesBody(notes: List<String>) {
    if (notes.isEmpty()) {
        BodyText("Bug fixes and performance improvements.")
        return
    }
    Column(
        modifier = Modifier
            .padding(top = 18.dp)
            .heightIn(max = 260.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        notes.forEach { note ->
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    text = "—",
                    color = VitalColors.orange,
                    fontFamily = VitalFonts.JetBrainsMonoRegular,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(end = 10.dp)
                )
                Text(
                    text = note,
                    color = VitalColors.inkMuted,
                    fontFamily = VitalFonts.InterTight,
                    fontSize = 14.sp,
                    lineHeight = 21.sp
                )
            }
        }
    }
}

@Composable
private fun DownloadingBody(progress: Float?) {
    Column(modifier = Modifier.padding(top = 20.dp)) {
        Text(
            text = if (progress == null) {
                "Downloading…"
            } else {
                "Downloading… ${(progress * 100).roundToInt()}%"
            },
            color = VitalColors.inkMuted,
            fontFamily = VitalFonts.InterTight,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .background(VitalColors.rule, RoundedCornerShape(3.dp))
        ) {
            if (progress != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .height(5.dp)
                        .background(VitalColors.orange, RoundedCornerShape(3.dp))
                )
            }
        }
    }
}

@Composable
private fun BodyText(text: String, isError: Boolean = false) {
    Text(
        text = text,
        color = if (isError) VitalColors.orange else VitalColors.inkMuted,
        fontFamily = VitalFonts.InterTight,
        fontSize = 14.sp,
        lineHeight = 21.sp,
        modifier = Modifier.padding(top = 18.dp)
    )
}

@Composable
private fun SecondaryButton(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = VitalColors.inkMuted,
        fontFamily = VitalFonts.InterTight,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp)
    )
}
