package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.MusicTrack
import com.example.data.model.SavedMusic
import com.example.data.model.UiState
import com.example.data.repository.MusicRepository
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaCoral
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaEmerald
import com.example.ui.theme.LuminaPrismGradient
import com.example.ui.theme.LuminaViolet
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@Volatile
private var activeGlobalAudioController: AudioClipPlayerController? = null

/**
 * Immediately stops and releases any audio playing or preparing anywhere in the app.
 * Used when switching bottom navigation tabs, scrolling past items, or going offline.
 */
fun stopAllGlobalAudio() {
    try {
        activeGlobalAudioController?.release()
        activeGlobalAudioController = null
    } catch (_: Exception) {
    }
}

/**
 * Lifecycle-aware audio clip preview & trim loop player backed by Android MediaPlayer.
 * Ensures only a single audio clip plays at a time and immediately cancels async preparation
 * or active playback the instant a user scrolls past a Post, Reel, or Story.
 */
class AudioClipPlayerController(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private var playbackGeneration: Int = 0
    var currentOwnerKey by mutableStateOf("")
        private set
    var activeAudioUrl by mutableStateOf("")
        private set
    var isPlaying by mutableStateOf(false)
        private set
    var isPreparing by mutableStateOf(false)
        private set
    var currentPositionMs by mutableLongStateOf(0L)
        private set
    var trimStartMs by mutableLongStateOf(0L)
        private set
    var trimEndMs by mutableLongStateOf(15_000L)
        private set

    private fun claimGlobalAudioFocus() {
        val currentGlobal = activeGlobalAudioController
        if (currentGlobal != null && currentGlobal !== this) {
            currentGlobal.release()
        }
        activeGlobalAudioController = this
    }

    fun playTrimmedClip(url: String, startMs: Long, endMs: Long, ownerKey: String = "") {
        if (url.isBlank()) return
        val safeStart = startMs.coerceAtLeast(0L)
        val safeEnd = endMs.coerceAtLeast(safeStart + 1000L)
        trimStartMs = safeStart
        trimEndMs = safeEnd
        if (ownerKey.isNotBlank()) {
            currentOwnerKey = ownerKey
        }

        if (activeAudioUrl == url && mediaPlayer != null && !isPreparing) {
            try {
                val mp = mediaPlayer!!
                if (mp.isPlaying) {
                    mp.pause()
                    isPlaying = false
                } else {
                    claimGlobalAudioFocus()
                    mp.seekTo(safeStart.toInt())
                    mp.start()
                    isPlaying = true
                }
                return
            } catch (_: Exception) {
                release()
            }
        }

        startFreshPlayback(url, safeStart, safeEnd, ownerKey)
    }

    /**
     * Plays a trimmed song strictly synced to a specific visible Post, Reel, or Story [ownerKey].
     * If another item was playing or preparing, it is immediately stopped.
     */
    fun playSyncedForOwner(ownerKey: String, url: String, startMs: Long, endMs: Long) {
        if (url.isBlank() || ownerKey.isBlank()) {
            stopForOwner(ownerKey)
            return
        }
        val safeStart = startMs.coerceAtLeast(0L)
        val safeEnd = endMs.coerceAtLeast(safeStart + 1000L)
        trimStartMs = safeStart
        trimEndMs = safeEnd

        if (currentOwnerKey == ownerKey && activeAudioUrl == url && mediaPlayer != null) {
            if (isPreparing) return
            try {
                val mp = mediaPlayer!!
                claimGlobalAudioFocus()
                if (!mp.isPlaying) {
                    mp.seekTo(safeStart.toInt())
                    mp.start()
                }
                isPlaying = true
                return
            } catch (_: Exception) {
                release()
            }
        }

        startFreshPlayback(url, safeStart, safeEnd, ownerKey)
    }

    /**
     * Immediately stops playback (including cancelling any in-flight prepareAsync) if [ownerKey]
     * matches the current owner or if [ownerKey] is blank.
     */
    fun stopForOwner(ownerKey: String) {
        if (ownerKey.isBlank() || currentOwnerKey == ownerKey || currentOwnerKey.isBlank()) {
            release()
        }
    }

    /**
     * Ensures the trimmed audio clip is actively playing from [startMs] to [endMs]
     * whenever the user is trimming the song.
     */
    fun previewWhileTrimming(url: String, startMs: Long, endMs: Long) {
        if (url.isBlank()) return
        val safeStart = startMs.coerceAtLeast(0L)
        val safeEnd = endMs.coerceAtLeast(safeStart + 1000L)
        trimStartMs = safeStart
        trimEndMs = safeEnd

        if (activeAudioUrl == url && mediaPlayer != null && !isPreparing) {
            try {
                claimGlobalAudioFocus()
                val mp = mediaPlayer!!
                mp.seekTo(safeStart.toInt())
                if (!mp.isPlaying) {
                    mp.start()
                }
                isPlaying = true
                currentPositionMs = safeStart
                return
            } catch (_: Exception) {
                release()
            }
        }
        startFreshPlayback(url, safeStart, safeEnd, "trimmer_preview")
    }

    private fun startFreshPlayback(url: String, safeStart: Long, safeEnd: Long, ownerKey: String) {
        release()
        claimGlobalAudioFocus()
        val generation = ++playbackGeneration
        currentOwnerKey = ownerKey
        activeAudioUrl = url
        trimStartMs = safeStart
        trimEndMs = safeEnd
        isPreparing = true

        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                if (url.startsWith("content://") || url.startsWith("file://")) {
                    setDataSource(context, Uri.parse(url))
                } else {
                    setDataSource(url)
                }
                setOnPreparedListener { player ->
                    // If scrolled away or paused while preparing, immediately abort and release!
                    if (generation != playbackGeneration || !isPreparing || mediaPlayer !== player) {
                        try {
                            player.release()
                        } catch (_: Exception) {
                        }
                        return@setOnPreparedListener
                    }
                    isPreparing = false
                    try {
                        player.seekTo(safeStart.toInt())
                        player.start()
                        this@AudioClipPlayerController.isPlaying = true
                        currentPositionMs = safeStart
                    } catch (_: Exception) {
                        this@AudioClipPlayerController.isPlaying = false
                    }
                }
                setOnCompletionListener { player ->
                    if (generation != playbackGeneration || mediaPlayer !== player) return@setOnCompletionListener
                    try {
                        player.seekTo(trimStartMs.toInt())
                        player.start()
                    } catch (_: Exception) {
                        this@AudioClipPlayerController.isPlaying = false
                    }
                }
                setOnErrorListener { _, _, _ ->
                    if (generation == playbackGeneration) {
                        isPreparing = false
                        this@AudioClipPlayerController.isPlaying = false
                    }
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (_: Exception) {
            isPreparing = false
            isPlaying = false
        }
    }

    fun updateTrimWindow(startMs: Long, endMs: Long) {
        val safeStart = startMs.coerceAtLeast(0L)
        val safeEnd = endMs.coerceAtLeast(safeStart + 1000L)
        trimStartMs = safeStart
        trimEndMs = safeEnd
        try {
            val mp = mediaPlayer
            if (mp != null && mp.isPlaying) {
                val pos = mp.currentPosition.toLong()
                if (pos < safeStart || pos > safeEnd) {
                    mp.seekTo(safeStart.toInt())
                }
            }
        } catch (_: Exception) {
        }
    }

    fun tickLoopCheck() {
        val mp = mediaPlayer ?: return
        try {
            if (mp.isPlaying) {
                val pos = mp.currentPosition.toLong()
                currentPositionMs = pos
                if (pos >= trimEndMs) {
                    mp.seekTo(trimStartMs.toInt())
                    currentPositionMs = trimStartMs
                }
            }
        } catch (_: Exception) {
        }
    }

    fun pause() {
        playbackGeneration++
        if (isPreparing) {
            // If still preparing asynchronously when scrolled away/paused, release immediately
            release()
            return
        }
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: Exception) {
        }
        isPlaying = false
        isPreparing = false
    }

    fun release() {
        playbackGeneration++
        val playerToRelease = mediaPlayer
        mediaPlayer = null
        isPlaying = false
        isPreparing = false
        activeAudioUrl = ""
        currentOwnerKey = ""
        if (activeGlobalAudioController === this) {
            activeGlobalAudioController = null
        }
        if (playerToRelease != null) {
            try {
                playerToRelease.setOnPreparedListener(null)
                playerToRelease.setOnCompletionListener(null)
                playerToRelease.setOnErrorListener(null)
                if (playerToRelease.isPlaying) {
                    playerToRelease.stop()
                }
            } catch (_: Exception) {
            }
            try {
                playerToRelease.reset()
            } catch (_: Exception) {
            }
            try {
                playerToRelease.release()
            } catch (_: Exception) {
            }
        }
    }
}

@Composable
fun rememberAudioClipPlayer(): AudioClipPlayerController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val controller = remember { AudioClipPlayerController(context.applicationContext) }

    LaunchedEffect(controller.isPlaying) {
        while (controller.isPlaying) {
            controller.tickLoopCheck()
            delay(120L)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE ||
                event == Lifecycle.Event.ON_STOP ||
                event == Lifecycle.Event.ON_DESTROY
            ) {
                controller.release()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            controller.release()
        }
    }

    return controller
}

fun formatMillisToMinSec(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L) / 1000L)
    val mins = totalSeconds / 60L
    val secs = totalSeconds % 60L
    return String.format(Locale.US, "%d:%02d", mins, secs)
}

/**
 * Interactive Music Trimmer & Waveform Scrubber Card.
 * Automatically plays a live preview of the trimmed window when trimming and includes a Tick (✓) confirm button.
 */
@Composable
fun MusicTrimmerStudioCard(
    track: MusicTrack,
    targetFormatLabel: String, // e.g., "Post", "24h Story", "Reel"
    recommendedDurationSeconds: Long,
    isSaved: Boolean,
    audioPlayer: AudioClipPlayerController,
    onTrimChanged: (startMs: Long, endMs: Long) -> Unit,
    onToggleSaveMusic: (MusicTrack) -> Unit,
    onChangeSongClick: () -> Unit,
    onRemoveSongClick: () -> Unit,
    onConfirmTickClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val maxTrackDurationMs = track.durationMs.coerceAtLeast(15_000L)
    val startFraction = (track.trimStartMs.toFloat() / maxTrackDurationMs.toFloat()).coerceIn(0f, 0.9f)
    val endFraction = (track.trimEndMs.toFloat() / maxTrackDurationMs.toFloat()).coerceIn(startFraction + 0.05f, 1f)

    val isThisPlaying = audioPlayer.isPlaying && audioPlayer.activeAudioUrl == track.audioUrl
    val isThisPreparing = audioPlayer.isPreparing && audioPlayer.activeAudioUrl == track.audioUrl

    // Auto-start preview when the trimmer opens for a song so the user immediately hears the trimmed segment
    LaunchedEffect(track.trackId, track.audioUrl) {
        if (track.audioUrl.isNotBlank() && !isThisPlaying && !isThisPreparing) {
            audioPlayer.previewWhileTrimming(track.audioUrl, track.trimStartMs, track.trimEndMs)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "vinylSpin")
    val discRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "discRotationAngle"
    )

    LuminaGlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        borderBrush = LuminaPrismGradient
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top Track Header + Tick (✓) Use Song, Save & Remove Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(LuminaPrismGradient)
                            .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape)
                            .rotate(if (isThisPlaying) discRotation else 0f),
                        contentAlignment = Alignment.Center
                    ) {
                        if (track.coverUrl.isNotBlank()) {
                            AsyncImage(
                                model = track.coverUrl,
                                contentDescription = track.title,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.75f))
                                .border(1.5.dp, Color.White, CircleShape)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Live Trim Preview: ${formatMillisToMinSec(track.trimStartMs)} – ${formatMillisToMinSec(track.trimEndMs)} (${track.trimmedDurationSeconds}s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = LuminaEmerald,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (onConfirmTickClick != null) {
                        Surface(
                            onClick = onConfirmTickClick,
                            shape = CircleShape,
                            color = LuminaCoral,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("trimmer_tick_confirm_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Use This Song",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    IconButton(
                        onClick = { onToggleSaveMusic(track) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("trimmer_save_music_button")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSaved) "Saved" else "Save Song",
                            tint = if (isSaved) LuminaAmber else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = onRemoveSongClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove Music",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Clip Length Sync Presets according to Post / Story / Reel length
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentCut,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Trim Length for $targetFormatLabel (Auto-Previews)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "${track.trimmedDurationSeconds}s clip",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                val durationPresets = remember(targetFormatLabel, recommendedDurationSeconds) {
                    listOf(5L, 10L, 15L, 30L, recommendedDurationSeconds)
                        .distinct()
                        .sorted()
                }

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(durationPresets) { presetSec ->
                        val isMatchTarget = presetSec == recommendedDurationSeconds
                        val label = if (isMatchTarget) "${presetSec}s ($targetFormatLabel)" else "${presetSec}s"
                        val isSelected = track.trimmedDurationSeconds == presetSec
                        LuminaPillChip(
                            label = label,
                            selected = isSelected,
                            onClick = {
                                val desiredMs = (presetSec * 1000L).coerceAtMost(maxTrackDurationMs)
                                val newStart = track.trimStartMs.coerceAtMost((maxTrackDurationMs - desiredMs).coerceAtLeast(0L))
                                val newEnd = (newStart + desiredMs).coerceAtMost(maxTrackDurationMs)
                                onTrimChanged(newStart, newEnd)
                                audioPlayer.previewWhileTrimming(track.audioUrl, newStart, newEnd)
                            }
                        )
                    }
                }
            }

            // Visual Multi-Bar Studio Audio Waveform with Active Trim Highlight
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                val playheadFraction = if (isThisPlaying) {
                    (audioPlayer.currentPositionMs.toFloat() / maxTrackDurationMs.toFloat()).coerceIn(0f, 1f)
                } else {
                    startFraction
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val barCount = 44
                    val spacing = 4.dp.toPx()
                    val totalSpacing = spacing * (barCount - 1)
                    val barWidth = ((size.width - totalSpacing) / barCount).coerceAtLeast(3f)

                    for (i in 0 until barCount) {
                        val barFraction = i.toFloat() / (barCount - 1).toFloat()
                        val inTrimRange = barFraction in startFraction..endFraction
                        val isNearPlayhead = isThisPlaying && kotlin.math.abs(barFraction - playheadFraction) < 0.03f

                        val seed = (track.trackId.hashCode() + i * 19).toDouble()
                        val normalizedHeight = (0.28f + 0.68f * kotlin.math.abs(sin(seed)).toFloat()).coerceIn(0.25f, 1f)
                        val barHeight = size.height * normalizedHeight
                        val topOffset = (size.height - barHeight) / 2f
                        val x = i * (barWidth + spacing)

                        val barColor = when {
                            isNearPlayhead -> Color.White
                            inTrimRange -> LuminaCoral
                            else -> Color.Gray.copy(alpha = 0.32f)
                        }

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, topOffset),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
                        )
                    }
                }
            }

            // RangeSlider to scrub exact Start and End points with live audio preview
            RangeSlider(
                value = startFraction..endFraction,
                onValueChange = { range ->
                    val newStart = (range.start * maxTrackDurationMs).toLong().coerceAtLeast(0L)
                    val newEnd = (range.endInclusive * maxTrackDurationMs).toLong()
                        .coerceAtLeast(newStart + 2000L)
                        .coerceAtMost(maxTrackDurationMs)
                    onTrimChanged(newStart, newEnd)
                    audioPlayer.updateTrimWindow(newStart, newEnd)
                },
                onValueChangeFinished = {
                    audioPlayer.previewWhileTrimming(track.audioUrl, track.trimStartMs, track.trimEndMs)
                },
                valueRange = 0f..1f,
                colors = SliderDefaults.colors(
                    thumbColor = LuminaCoral,
                    activeTrackColor = LuminaViolet,
                    inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("music_trim_range_slider")
            )

            // Bottom Action Row: Play/Pause Trimmed Loop & Change Song
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        if (isThisPlaying) {
                            audioPlayer.pause()
                        } else {
                            audioPlayer.previewWhileTrimming(
                                url = track.audioUrl,
                                startMs = track.trimStartMs,
                                endMs = track.trimEndMs
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("preview_trimmed_music_button")
                ) {
                    if (isThisPreparing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(15.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = if (isThisPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isThisPlaying) "Pause Preview" else "Preview Trimmed Song",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onChangeSongClick,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Browse Songs", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Full-Featured Music Discovery (Indian Songs First), Lyria AI Music Generator, Save & Trim Bottom Sheet.
 * Includes a one-tap Tick (✓) button on every song row so users can immediately attach the song to their Post, Story, or Reel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicBrowserBottomSheet(
    targetFormatLabel: String,
    recommendedDurationSeconds: Long,
    musicSearchState: UiState<List<MusicTrack>>,
    savedMusicList: List<SavedMusic>,
    selectedTrack: MusicTrack?,
    onSearchMusic: (query: String, genre: String) -> Unit,
    onToggleSaveMusic: (MusicTrack) -> Unit,
    onConfirmTrackWithTrim: (MusicTrack) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val audioPlayer = rememberAudioClipPlayer()
    val musicRepository = remember { MusicRepository() }

    var activeTab by remember { mutableIntStateOf(0) } // 0 = Indian & Global Songs, 1 = Saved, 2 = AI Lyria, 3 = Device
    var searchQuery by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf("Indian Hits") }
    var stagingTrack by remember(selectedTrack) { mutableStateOf(selectedTrack) }

    // AI Music (Lyria) state
    var lyriaPrompt by remember { mutableStateOf("Upbeat Bollywood dance beat with sitar, dhol, and modern bass") }
    var lyriaFullLength by remember { mutableStateOf(false) }
    var isGeneratingLyria by remember { mutableStateOf(false) }
    var lyriaStatusMessage by remember { mutableStateOf<String?>(null) }

    val genres = remember {
        listOf(
            "Indian Hits",
            "Bollywood",
            "Punjabi",
            "Arijit Singh",
            "AR Rahman",
            "Indie India",
            "Trending",
            "Pop",
            "Lo-Fi"
        )
    }

    val localAudioPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            val clipDurationMs = (recommendedDurationSeconds * 1000L).coerceIn(5_000L, 60_000L)
            val localTrack = MusicTrack(
                trackId = "local_${uri.lastPathSegment ?: System.currentTimeMillis()}",
                title = "Device Audio (${uri.lastPathSegment?.takeLast(18) ?: "Custom MP3"})",
                artist = "Local Device Audio",
                coverUrl = "",
                audioUrl = uri.toString(),
                durationMs = 60_000L,
                genre = "Local",
                trimStartMs = 0L,
                trimEndMs = clipDurationMs
            )
            stagingTrack = localTrack
            audioPlayer.previewWhileTrimming(localTrack.audioUrl, localTrack.trimStartMs, localTrack.trimEndMs)
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            audioPlayer.pause()
            onDismiss()
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(LuminaPrismGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Mitr Music Studio (Indian Songs First)",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Tap ✓ on any song to attach to your $targetFormatLabel, or tap Trim to scrub & preview",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = {
                    audioPlayer.pause()
                    onDismiss()
                }) {
                    Icon(Icons.Default.Close, contentDescription = "Close Music Sheet")
                }
            }

            // Tabs: Indian & Global | Saved | AI Music (Lyria) | Device
            TabRow(selectedTabIndex = activeTab) {
                Tab(
                    selected = activeTab == 0,
                    onClick = { activeTab = 0 },
                    text = { Text("Indian & Hits", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 1,
                    onClick = { activeTab = 1 },
                    text = { Text("Saved (${savedMusicList.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 2,
                    onClick = { activeTab = 2 },
                    text = { Text("AI Lyria", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeTab == 3,
                    onClick = { activeTab = 3 },
                    text = { Text("Device", fontWeight = FontWeight.Bold) }
                )
            }

            when (activeTab) {
                0 -> {
                    // Search Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                onSearchMusic(it, selectedGenre)
                            },
                            placeholder = { Text("Search Indian songs, Bollywood, Arijit, Punjabi...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        onSearchMusic("", selectedGenre)
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear")
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("music_search_input")
                        )
                        IconButton(
                            onClick = { onSearchMusic(searchQuery, selectedGenre) },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Songs")
                        }
                    }

                    // Genre Chips (Indian Hits first)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(genres) { genre ->
                            LuminaPillChip(
                                label = genre,
                                selected = selectedGenre == genre && searchQuery.isBlank(),
                                onClick = {
                                    selectedGenre = genre
                                    searchQuery = ""
                                    onSearchMusic("", genre)
                                }
                            )
                        }
                    }

                    // Results List
                    when (musicSearchState) {
                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = LuminaCoral)
                            }
                        }

                        is UiState.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Could not load online music tracks",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = musicSearchState.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Button(onClick = { onSearchMusic(searchQuery, selectedGenre) }) {
                                    Text("Retry Fetch")
                                }
                            }
                        }

                        is UiState.Success -> {
                            val tracks = musicSearchState.data
                            if (tracks.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No songs found for \"$searchQuery\". Try another Indian artist or song title.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(if (stagingTrack != null) 180.dp else 290.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    contentPadding = PaddingValues(bottom = 8.dp)
                                ) {
                                    items(tracks, key = { it.trackId }) { item ->
                                        val isSaved = savedMusicList.any { it.trackId == item.trackId }
                                        val isSelected = stagingTrack?.trackId == item.trackId
                                        val isPlayingItem = audioPlayer.isPlaying && audioPlayer.activeAudioUrl == item.audioUrl

                                        MusicTrackListRow(
                                            track = item,
                                            isSaved = isSaved,
                                            isSelected = isSelected,
                                            isPlaying = isPlayingItem,
                                            onPlayPauseClick = {
                                                if (isPlayingItem) {
                                                    audioPlayer.pause()
                                                } else {
                                                    val clipLenMs = (recommendedDurationSeconds * 1000L)
                                                        .coerceAtMost(item.durationMs)
                                                    audioPlayer.playTrimmedClip(item.audioUrl, 0L, clipLenMs)
                                                }
                                            },
                                            onSaveClick = { onToggleSaveMusic(item) },
                                            onSelectForTrimClick = {
                                                val defaultEndMs = (recommendedDurationSeconds * 1000L)
                                                    .coerceIn(5_000L, item.durationMs)
                                                val initialized = item.copy(
                                                    trimStartMs = 0L,
                                                    trimEndMs = defaultEndMs
                                                )
                                                stagingTrack = initialized
                                                audioPlayer.previewWhileTrimming(
                                                    initialized.audioUrl,
                                                    initialized.trimStartMs,
                                                    initialized.trimEndMs
                                                )
                                            },
                                            onTickUseClick = {
                                                val defaultEndMs = (recommendedDurationSeconds * 1000L)
                                                    .coerceIn(5_000L, item.durationMs)
                                                val chosen = if (stagingTrack?.trackId == item.trackId) {
                                                    stagingTrack!!
                                                } else {
                                                    item.copy(trimStartMs = 0L, trimEndMs = defaultEndMs)
                                                }
                                                audioPlayer.pause()
                                                onConfirmTrackWithTrim(chosen)
                                                onDismiss()
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // SAVED MUSIC TAB
                    if (savedMusicList.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkBorder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No saved songs yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Tap the bookmark icon on any song to save it to your unified Saved collection.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (stagingTrack != null) 180.dp else 290.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(savedMusicList, key = { it.id }) { saved ->
                                val track = saved.toMusicTrack()
                                val isSelected = stagingTrack?.trackId == track.trackId
                                val isPlayingItem = audioPlayer.isPlaying && audioPlayer.activeAudioUrl == track.audioUrl

                                MusicTrackListRow(
                                    track = track,
                                    isSaved = true,
                                    isSelected = isSelected,
                                    isPlaying = isPlayingItem,
                                    onPlayPauseClick = {
                                        if (isPlayingItem) {
                                            audioPlayer.pause()
                                        } else {
                                            audioPlayer.playTrimmedClip(
                                                track.audioUrl,
                                                track.trimStartMs,
                                                track.trimEndMs
                                            )
                                        }
                                    },
                                    onSaveClick = { onToggleSaveMusic(track) },
                                    onSelectForTrimClick = {
                                        stagingTrack = track
                                        audioPlayer.previewWhileTrimming(
                                            track.audioUrl,
                                            track.trimStartMs,
                                            track.trimEndMs
                                        )
                                    },
                                    onTickUseClick = {
                                        audioPlayer.pause()
                                        onConfirmTrackWithTrim(track)
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // AI LYRIA MUSIC GENERATION TAB (lyria-3-clip-preview / lyria-3-pro-preview)
                    LuminaGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Generate Custom Music with Lyria 3",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Text(
                                text = "Uses lyria-3-clip-preview (30s clip) or lyria-3-pro-preview (full track) to compose custom background music for your $targetFormatLabel.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedTextField(
                                value = lyriaPrompt,
                                onValueChange = { lyriaPrompt = it.take(240) },
                                label = { Text("Describe instruments, mood, or Indian rhythm") },
                                minLines = 2,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (lyriaFullLength) "Model: lyria-3-pro-preview (Full Track)" else "Model: lyria-3-clip-preview (30s Clip)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Switch(
                                    checked = lyriaFullLength,
                                    onCheckedChange = { lyriaFullLength = it }
                                )
                            }
                            if (lyriaStatusMessage != null) {
                                Text(
                                    text = lyriaStatusMessage!!,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Button(
                                onClick = {
                                    isGeneratingLyria = true
                                    lyriaStatusMessage = "Composing music with ${if (lyriaFullLength) "lyria-3-pro-preview" else "lyria-3-clip-preview"}..."
                                    coroutineScope.launch {
                                        musicRepository.generateMusicWithLyria(
                                            context = context,
                                            prompt = lyriaPrompt,
                                            fullLength = lyriaFullLength
                                        ).onSuccess { generatedTrack ->
                                            isGeneratingLyria = false
                                            val endMs = (recommendedDurationSeconds * 1000L).coerceAtMost(generatedTrack.durationMs)
                                            val trimmed = generatedTrack.copy(trimStartMs = 0L, trimEndMs = endMs)
                                            stagingTrack = trimmed
                                            lyriaStatusMessage = "Generated! Previewing trimmed track below — tap ✓ to use."
                                            audioPlayer.previewWhileTrimming(trimmed.audioUrl, trimmed.trimStartMs, trimmed.trimEndMs)
                                        }.onFailure { err ->
                                            isGeneratingLyria = false
                                            lyriaStatusMessage = "Generation error: ${err.message}"
                                        }
                                    }
                                },
                                enabled = !isGeneratingLyria && lyriaPrompt.isNotBlank(),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (isGeneratingLyria) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generating Track...")
                                } else {
                                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Generate & Preview Track", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // DEVICE AUDIO PICKER TAB
                    LuminaGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AudioFile,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(34.dp)
                            )
                            Text(
                                text = "Import Audio from Device",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Select any MP3, M4A, or WAV file on your phone and trim it with live preview.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = { localAudioPicker.launch("audio/*") },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.MusicNote, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Browse Device Audio Files", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Inline Waveform Trimmer when a song is staged
            if (stagingTrack != null) {
                val activeStaged = stagingTrack!!
                val isStagedSaved = savedMusicList.any { it.trackId == activeStaged.trackId }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                MusicTrimmerStudioCard(
                    track = activeStaged,
                    targetFormatLabel = targetFormatLabel,
                    recommendedDurationSeconds = recommendedDurationSeconds,
                    isSaved = isStagedSaved,
                    audioPlayer = audioPlayer,
                    onTrimChanged = { newStart, newEnd ->
                        stagingTrack = activeStaged.copy(
                            trimStartMs = newStart,
                            trimEndMs = newEnd
                        )
                    },
                    onToggleSaveMusic = onToggleSaveMusic,
                    onChangeSongClick = { stagingTrack = null },
                    onRemoveSongClick = {
                        audioPlayer.pause()
                        stagingTrack = null
                    },
                    onConfirmTickClick = {
                        audioPlayer.pause()
                        onConfirmTrackWithTrim(activeStaged)
                        onDismiss()
                    }
                )

                LuminaGradientButton(
                    text = "✓ Use Song Along With $targetFormatLabel (${activeStaged.trimmedDurationSeconds}s)",
                    icon = Icons.Default.Check,
                    onClick = {
                        audioPlayer.pause()
                        onConfirmTrackWithTrim(activeStaged)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("confirm_trimmed_music_button")
                )
            }
        }
    }
}

/**
 * Song Row with Play/Pause, Save Bookmark, Trim Button, AND a prominent Tick (✓) Button
 * so users can tap the Tick to immediately attach the song to their Post, Story, or Reel.
 */
@Composable
private fun MusicTrackListRow(
    track: MusicTrack,
    isSaved: Boolean,
    isSelected: Boolean,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onSaveClick: () -> Unit,
    onSelectForTrimClick: () -> Unit,
    onTickUseClick: () -> Unit
) {
    LuminaGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectForTrimClick),
        shape = RoundedCornerShape(14.dp),
        borderBrush = if (isSelected) LuminaPrismGradient else Brush.linearGradient(
            listOf(
                MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable(onClick = onPlayPauseClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (track.coverUrl.isNotBlank()) {
                        AsyncImage(
                            model = track.coverUrl,
                            contentDescription = track.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.38f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Preview",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = track.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = track.artist,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${track.genre} • ${track.durationMs / 1000L}s",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onSaveClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save Music",
                        tint = if (isSaved) LuminaAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                FilledTonalButton(
                    onClick = onSelectForTrimClick,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Trim",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Prominent Tick (✓) Button: One tap uses this song along with the Post/Story/Reel!
                Surface(
                    onClick = onTickUseClick,
                    shape = CircleShape,
                    color = LuminaCoral,
                    modifier = Modifier
                        .size(34.dp)
                        .testTag("use_song_tick_${track.trackId}")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Use This Song",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Music Pill Banner displayed on Posts, Stories, Reels, and Saved Tab.
 */
@Composable
fun AttachedMusicPillBanner(
    musicTrackId: String,
    musicTitle: String,
    musicArtist: String,
    musicCoverUrl: String,
    musicUrl: String,
    musicStartMs: Long,
    musicEndMs: Long,
    isMusicSaved: Boolean,
    audioPlayer: AudioClipPlayerController,
    onToggleSaveMusic: (MusicTrack) -> Unit,
    modifier: Modifier = Modifier,
    darkOverlayStyle: Boolean = false,
    showPlaybackControls: Boolean = true
) {
    if (musicTitle.isBlank()) return

    val isPlayingThis = audioPlayer.isPlaying && audioPlayer.activeAudioUrl == musicUrl && musicUrl.isNotBlank()
    val clipSec = ((musicEndMs - musicStartMs).coerceAtLeast(1000L) / 1000L)

    val containerBg = if (darkOverlayStyle) {
        Color.Black.copy(alpha = 0.72f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (darkOverlayStyle) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerBg,
        modifier = modifier
            .border(
                width = 1.dp,
                brush = Brush.horizontalGradient(listOf(LuminaCoral.copy(alpha = 0.75f), LuminaViolet.copy(alpha = 0.75f))),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clickable(enabled = showPlaybackControls && musicUrl.isNotBlank()) {
                        if (isPlayingThis) {
                            audioPlayer.pause()
                        } else {
                            audioPlayer.playTrimmedClip(musicUrl, musicStartMs, musicEndMs)
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(LuminaPrismGradient),
                    contentAlignment = Alignment.Center
                ) {
                    if (musicCoverUrl.isNotBlank()) {
                        AsyncImage(
                            model = musicCoverUrl,
                            contentDescription = musicTitle,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Text(
                        text = "$musicTitle • $musicArtist",
                        style = MaterialTheme.typography.labelLarge,
                        color = contentColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "♫ ${formatMillisToMinSec(musicStartMs)}–${formatMillisToMinSec(musicEndMs)} (${clipSec}s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (darkOverlayStyle) LuminaAmber else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (showPlaybackControls && musicUrl.isNotBlank()) {
                    IconButton(
                        onClick = {
                            if (isPlayingThis) {
                                audioPlayer.pause()
                            } else {
                                audioPlayer.playTrimmedClip(musicUrl, musicStartMs, musicEndMs)
                            }
                        },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlayingThis) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlayingThis) "Pause Song" else "Play Trimmed Song",
                            tint = LuminaCoral,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                IconButton(
                    onClick = {
                        onToggleSaveMusic(
                            MusicTrack(
                                trackId = musicTrackId.ifBlank { "trk_${musicTitle.hashCode()}" },
                                title = musicTitle,
                                artist = musicArtist.ifBlank { "Mitr Artist" },
                                coverUrl = musicCoverUrl,
                                audioUrl = musicUrl,
                                durationMs = 30_000L,
                                trimStartMs = musicStartMs,
                                trimEndMs = musicEndMs
                            )
                        )
                    },
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = if (isMusicSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isMusicSaved) "Saved Music" else "Save Music",
                        tint = if (isMusicSaved) LuminaAmber else contentColor,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
