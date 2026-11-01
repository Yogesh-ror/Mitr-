package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.model.Comment
import com.example.data.model.MusicTrack
import com.example.data.model.Reel
import com.example.data.model.SavedMusic
import com.example.data.model.SavedPost
import com.example.data.model.UiState
import com.example.ui.components.AttachedMusicPillBanner
import com.example.ui.components.AuroraAmbientBackground
import com.example.ui.components.LuminaGlassCard
import com.example.ui.components.LuminaGradientButton
import com.example.ui.components.LuminaPillChip
import com.example.ui.components.MitrIcons
import com.example.ui.components.MitrReelAdMobPage
import com.example.ui.components.MusicBrowserBottomSheet
import com.example.ui.components.MusicTrimmerStudioCard
import com.example.ui.components.formatMillisToMinSec
import com.example.ui.components.rememberAudioClipPlayer
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaCoral
import com.example.ui.theme.LuminaCyan
import com.example.ui.theme.LuminaEmerald
import com.example.ui.theme.LuminaPrismGradient
import com.example.ui.theme.LuminaViolet
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.RequestOptions
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

import com.example.data.model.UserProfile
import com.example.data.model.isCreatorHandleBlueTickVerified
import com.example.ui.components.MitrTopHeaderBar
import com.example.ui.components.VerifiedBadgeIcon
import androidx.compose.material.icons.filled.Search

private sealed interface ReelPagerItem {
    data class Content(val reel: Reel) : ReelPagerItem
    data class AdSlot(val adIndex: Int) : ReelPagerItem
}

@Composable
fun ReelsFeedScreen(
    currentUserId: String,
    currentUserProfile: UserProfile = UserProfile(),
    reelsState: UiState<List<Reel>>,
    savedPosts: List<SavedPost>,
    savedMusicList: List<SavedMusic>,
    discoverableUsers: List<UserProfile> = emptyList(),
    followingIds: List<String> = emptyList(),
    autoplayVideos: Boolean,
    onLikeReel: (Reel) -> Unit,
    onSaveReel: (Reel) -> Unit,
    onToggleSaveMusic: (MusicTrack) -> Unit,
    onIncrementView: (Reel) -> Unit,
    onOpenUploadReel: () -> Unit,
    onOpenUserProfile: (String) -> Unit,
    onToggleFollowUser: (UserProfile) -> Unit = {},
    onLeaveCollaboration: (String, String) -> Unit = { _, _ -> },
    onOpenSearch: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    notificationBadgeCount: Int = 0,
    onOpenMessages: () -> Unit = {},
    observeComments: (String, String) -> Flow<List<Comment>>,
    onAddComment: (String, String, String, String) -> Unit,
    onReportReel: (String, String) -> Unit
) {
    val context = LocalContext.current
    val audioPlayer = rememberAudioClipPlayer()
    var activeCommentReel by remember { mutableStateOf<Reel?>(null) }
    var activeCoAuthorsReel by remember { mutableStateOf<Reel?>(null) }
    var reportingReelId by remember { mutableStateOf<String?>(null) }
    var isMuted by remember { mutableStateOf(false) }
    var isPaused by remember { mutableStateOf(!autoplayVideos) }
    var selectedFeedMode by remember { mutableStateOf("FOR_YOU") } // FOLLOWING or FOR_YOU

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.release()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Bar matching Screenshot 3: [Orb Logo] Mitr | Reels + Search, Bell, Chat+, Profile Avatar
        MitrTopHeaderBar(
            sectionTitle = "Reels",
            notificationBadgeCount = notificationBadgeCount,
            onSearchClick = onOpenSearch,
            onNotificationsClick = onOpenNotifications,
            onQuickMessageClick = onOpenMessages,
            avatarContent = {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .clickable { onOpenUserProfile(currentUserId) },
                    contentAlignment = Alignment.Center
                ) {
                    UserAvatarBadge(
                        avatarUrl = currentUserProfile.avatarUrl,
                        displayName = currentUserProfile.displayName,
                        size = 32.dp,
                        showBorder = true
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF10121C))
        ) {
            when (reelsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = LuminaCoral)
                    }
                }

                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyOrErrorCard(
                            title = "Could not load Reels",
                            subtitle = reelsState.message,
                            actionLabel = "Publish a Reel",
                            onAction = onOpenUploadReel
                        )
                    }
                }

                is UiState.Success -> {
                    val reels = remember(reelsState.data, currentUserProfile.username) {
                        val myUname = currentUserProfile.username.lowercase()
                        reelsState.data.sortedWith(
                            compareByDescending<Reel> { reel ->
                                reel.collaboratorUsernames.any {
                                    it.removePrefix("@").lowercase() == myUname
                                }
                            }.thenByDescending { it.createdAt?.toDate()?.time ?: 0L }
                        )
                    }
                    if (reels.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyOrErrorCard(
                                title = "No reels published yet",
                                subtitle = "Create your first vertical Reel with trimmed music!",
                                actionLabel = "Create First Reel",
                                onAction = onOpenUploadReel
                            )
                        }
                    } else {
                        val pagerItems = remember(reels) {
                            buildList {
                                var adCount = 0
                                reels.forEachIndexed { idx, reelItem ->
                                    add(ReelPagerItem.Content(reelItem))
                                    // Insert 1 AdMob Reel Ad every 10 reels
                                    if ((idx + 1) % 10 == 0) {
                                        adCount += 1
                                        add(ReelPagerItem.AdSlot(adCount))
                                    }
                                }
                                if (reels.size < 10) {
                                    add(ReelPagerItem.AdSlot(1))
                                }
                            }
                        }
                        val pagerState = rememberPagerState(pageCount = { pagerItems.size })

                        // Immediately stop Reel music the instant the user starts scrolling between Reels!
                        LaunchedEffect(pagerState.isScrollInProgress, pagerState.currentPage, pagerState.settledPage) {
                            if (pagerState.isScrollInProgress) {
                                audioPlayer.pause()
                            }
                        }

                        VerticalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("reels_vertical_pager")
                        ) { page ->
                            when (val pageItem = pagerItems[page]) {
                                is ReelPagerItem.AdSlot -> {
                                    LaunchedEffect(pagerState.currentPage) {
                                        if (pagerState.currentPage == page) {
                                            audioPlayer.pause()
                                        }
                                    }
                                    MitrReelAdMobPage(adPageIndex = pageItem.adIndex)
                                }

                                is ReelPagerItem.Content -> {
                                    val reel = pageItem.reel
                                    val reelOwnerKey = remember(reel.id) { "reel_${reel.id}" }
                                    val isActiveStationaryReel = pagerState.currentPage == page &&
                                        pagerState.settledPage == page &&
                                        !pagerState.isScrollInProgress
                                    val isLiked = reel.likedByUids.contains(currentUserId)
                                    val isSaved = savedPosts.any { it.postId == reel.id }
                                    val isMusicSaved = savedMusicList.any {
                                        it.trackId == reel.musicTrackId || (reel.musicTitle.isNotBlank() && it.title == reel.musicTitle)
                                    }
                                    val syncedReelEndMs = remember(reel.musicStartMs, reel.musicEndMs, reel.durationSeconds) {
                                        if (reel.durationSeconds > 0L) {
                                            (reel.musicStartMs + reel.durationSeconds * 1000L).coerceAtLeast(reel.musicStartMs + 1000L)
                                        } else {
                                            reel.musicEndMs.coerceAtLeast(reel.musicStartMs + 15_000L)
                                        }
                                    }

                                    val primaryCollaboratorProfile = remember(reel.collaboratorUsernames, discoverableUsers) {
                                        val firstCollab = reel.collaboratorUsernames.firstOrNull()?.removePrefix("@").orEmpty()
                                        if (firstCollab.isBlank()) null
                                        else discoverableUsers.firstOrNull {
                                            it.username.removePrefix("@").equals(firstCollab, ignoreCase = true)
                                        }
                                    }
                                    val isCreatorVerified = remember(reel.authorId, reel.authorUsername, discoverableUsers, currentUserId, currentUserProfile) {
                                        isCreatorHandleBlueTickVerified(
                                            authorId = reel.authorId,
                                            authorUsername = reel.authorUsername,
                                            discoverableUsers = discoverableUsers + listOf(currentUserProfile),
                                            currentUserId = currentUserId
                                        )
                                    }
                                    val displayedLikeCount = if (isCreatorVerified) reel.likeCount.coerceAtLeast(48L) else reel.likeCount
                                    val displayedViewCount = if (isCreatorVerified) reel.viewCount.coerceAtLeast(320L) else reel.viewCount.coerceAtLeast(1L)

                                    LaunchedEffect(isActiveStationaryReel, reel.id, isMuted, isPaused, syncedReelEndMs) {
                                        if (isActiveStationaryReel) {
                                            onIncrementView(reel)
                                            if (reel.musicUrl.isNotBlank() && !isMuted && !isPaused) {
                                                audioPlayer.playSyncedForOwner(
                                                    ownerKey = reelOwnerKey,
                                                    url = reel.musicUrl,
                                                    startMs = reel.musicStartMs,
                                                    endMs = syncedReelEndMs
                                                )
                                            } else {
                                                audioPlayer.stopForOwner(reelOwnerKey)
                                            }
                                        } else {
                                            audioPlayer.stopForOwner(reelOwnerKey)
                                        }
                                    }

                                    DisposableEffect(reelOwnerKey) {
                                        onDispose {
                                            audioPlayer.stopForOwner(reelOwnerKey)
                                        }
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clickable { isPaused = !isPaused }
                                    ) {
                                        // Full-Screen Video / Thumbnail Layer
                                        if (reel.videoUrl.startsWith("http") || reel.videoUrl.startsWith("content://") || reel.videoUrl.startsWith("file://")) {
                                            val shouldPlayVideo = isActiveStationaryReel && !isPaused
                                            AndroidView(
                                                factory = { ctx ->
                                                    VideoView(ctx).apply {
                                                        setVideoURI(Uri.parse(reel.videoUrl))
                                                        setOnPreparedListener { mp ->
                                                            mp.isLooping = true
                                                            val vol = if (isMuted || reel.musicUrl.isNotBlank()) 0f else 1f
                                                            mp.setVolume(vol, vol)
                                                            if (shouldPlayVideo) start() else pause()
                                                        }
                                                        setOnErrorListener { _, _, _ -> true }
                                                    }
                                                },
                                                update = { view ->
                                                    if (!shouldPlayVideo && view.isPlaying) {
                                                        view.pause()
                                                    } else if (shouldPlayVideo && !view.isPlaying) {
                                                        view.start()
                                                    }
                                                },
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        AsyncImage(
                                            model = reel.videoUrl,
                                            contentDescription = reel.caption,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )

                                        // Cinematic Top & Bottom Scrim
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(
                                                    Brush.verticalGradient(
                                                        colors = listOf(
                                                            Color.Black.copy(alpha = 0.35f),
                                                            Color.Transparent,
                                                            Color.Transparent,
                                                            Color.Black.copy(alpha = 0.78f)
                                                        )
                                                    )
                                                )
                                        )

                                        // Right-Side Floating Overlay Column (Matches Screenshot 3)
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .padding(end = 14.dp, bottom = 24.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(14.dp)
                                        ) {
                                            // Creator Avatar (or Overlapping Collab Avatars) with Pink '+' Badge
                                            if (reel.collaboratorUsernames.isNotEmpty()) {
                                                Box(
                                                    modifier = Modifier
                                                        .clickable { activeCoAuthorsReel = reel },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    OverlappingCollabAvatarsBadge(
                                                        authorAvatarUrl = reel.musicCoverUrl,
                                                        authorDisplayName = reel.authorDisplayName.ifBlank { reel.authorUsername },
                                                        collaboratorAvatarUrl = primaryCollaboratorProfile?.avatarUrl ?: "",
                                                        collaboratorDisplayName = primaryCollaboratorProfile?.displayName
                                                            ?: reel.collaboratorUsernames.firstOrNull().orEmpty()
                                                    )
                                                }
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(52.dp)
                                                        .clickable { onOpenUserProfile(reel.authorId) },
                                                    contentAlignment = Alignment.TopCenter
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(46.dp)
                                                            .border(2.dp, LuminaPrismGradient, CircleShape)
                                                            .padding(2.dp)
                                                            .clip(CircleShape)
                                                    ) {
                                                        UserAvatarBadge(
                                                            avatarUrl = reel.musicCoverUrl,
                                                            displayName = reel.authorDisplayName,
                                                            size = 42.dp
                                                        )
                                                    }
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.BottomCenter)
                                                            .size(20.dp)
                                                            .clip(CircleShape)
                                                            .background(LuminaAmber)
                                                            .border(1.5.dp, Color.White, CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Add,
                                                            contentDescription = "Follow Creator",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(13.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            // Like Overlay Button (boosted for Verified Blue Tick subscribers)
                                            ReelActionDockItem(
                                                icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                tint = if (isLiked) LuminaAmber else Color.White,
                                                label = "$displayedLikeCount",
                                                contentDescription = "Like Reel",
                                                testTag = "reel_like_button_${reel.id}",
                                                onClick = { onLikeReel(reel) }
                                            )

                                            // Comment Overlay Button
                                            ReelActionDockItem(
                                                icon = Icons.Outlined.ModeComment,
                                                tint = Color.White,
                                                label = "${reel.commentCount}",
                                                contentDescription = "Reel Comments",
                                                testTag = "reel_comment_button_${reel.id}",
                                                onClick = { activeCommentReel = reel }
                                            )

                                            // Share Overlay Button
                                            ReelActionDockItem(
                                                icon = MitrIcons.PaperPlaneSend,
                                                tint = Color.White,
                                                label = "Share",
                                                contentDescription = "Share Reel",
                                                testTag = "reel_share_button_${reel.id}",
                                                onClick = {
                                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "text/plain"
                                                        putExtra(
                                                            Intent.EXTRA_TEXT,
                                                            "Watch @${reel.authorUsername}'s Reel on Mitr: ${reel.caption}"
                                                        )
                                                    }
                                                    context.startActivity(Intent.createChooser(sendIntent, "Share Reel"))
                                                }
                                            )

                                            // Save Overlay Button
                                            ReelActionDockItem(
                                                icon = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                tint = if (isSaved) LuminaAmber else Color.White,
                                                label = if (isSaved) "Saved" else "Save",
                                                contentDescription = "Save Reel",
                                                testTag = "reel_save_button_${reel.id}",
                                                onClick = { onSaveReel(reel) }
                                            )

                                            // Bottom Circular Music Disc Badge (Matches Screenshot 3)
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF1A1D2D))
                                                    .border(2.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                                                    .clickable {
                                                        if (reel.musicTitle.isNotBlank()) {
                                                            onToggleSaveMusic(
                                                                MusicTrack(
                                                                    trackId = reel.musicTrackId.ifBlank { reel.id },
                                                                    title = reel.musicTitle,
                                                                    artist = reel.musicArtist.ifBlank { reel.authorDisplayName },
                                                                    coverUrl = reel.musicCoverUrl,
                                                                    audioUrl = reel.musicUrl,
                                                                    trimStartMs = reel.musicStartMs,
                                                                    trimEndMs = reel.musicEndMs
                                                                )
                                                            )
                                                        } else {
                                                            reportingReelId = reel.id
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .clip(CircleShape)
                                                        .background(LuminaPrismGradient),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(7.dp)
                                                            .clip(CircleShape)
                                                            .background(Color.White)
                                                    )
                                                }
                                            }
                                        }

                                        // Bottom-Left Creator & Audio Overlay (Matches Screenshot 3)
                                        Column(
                                            modifier = Modifier
                                                .align(Alignment.BottomStart)
                                                .fillMaxWidth(0.78f)
                                                .padding(start = 16.dp, bottom = 24.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                val reelAuthorsLabel = if (reel.collaboratorUsernames.isNotEmpty()) {
                                                    "@${reel.authorUsername} and " +
                                                        reel.collaboratorUsernames.joinToString(", ") { "@${it.removePrefix("@")}" }
                                                } else {
                                                    "@${reel.authorUsername}"
                                                }
                                                Text(
                                                    text = reelAuthorsLabel,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier
                                                        .weight(1f, fill = false)
                                                        .clickable {
                                                            if (reel.collaboratorUsernames.isNotEmpty()) {
                                                                activeCoAuthorsReel = reel
                                                            } else {
                                                                onOpenUserProfile(reel.authorId)
                                                            }
                                                        }
                                                )
                                                if (isCreatorVerified) {
                                                    VerifiedBadgeIcon(size = 16.dp)
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = Color.White.copy(alpha = 0.22f),
                                                    modifier = Modifier
                                                        .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(50))
                                                        .clickable {
                                                            if (reel.collaboratorUsernames.isNotEmpty()) {
                                                                activeCoAuthorsReel = reel
                                                            } else {
                                                                onOpenUserProfile(reel.authorId)
                                                            }
                                                        }
                                                ) {
                                                    Text(
                                                        text = if (reel.collaboratorUsernames.isNotEmpty()) {
                                                            "Collab (${reel.collaboratorUsernames.size + 1})"
                                                        } else {
                                                            "Follow"
                                                        },
                                                        style = MaterialTheme.typography.labelMedium,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            Text(
                                                text = if (isCreatorVerified) {
                                                    "⚡ $displayedViewCount views • Verified Priority Reach"
                                                } else {
                                                    "$displayedViewCount views"
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isCreatorVerified) Color(0xFF60A5FA) else Color.White.copy(alpha = 0.8f),
                                                fontWeight = FontWeight.Bold
                                            )

                                            if (reel.taggedUsernames.isNotEmpty()) {
                                                Text(
                                                    text = "Tagged: " + reel.taggedUsernames.joinToString(", ") { "@${it.removePrefix("@")}" },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = LuminaAmber,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.clickable { activeCoAuthorsReel = reel }
                                                )
                                            }

                                            if (reel.caption.isNotBlank()) {
                                                Text(
                                                    text = reel.caption,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.White.copy(alpha = 0.95f),
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            if (reel.musicTitle.isNotBlank()) {
                                                // Synced directly with Reel seconds; no separate stop/pause button on Reel music pill
                                                AttachedMusicPillBanner(
                                                    musicTrackId = reel.musicTrackId,
                                                    musicTitle = "${reel.musicTitle} (${reel.durationSeconds}s Reel Sync)",
                                                    musicArtist = reel.musicArtist,
                                                    musicCoverUrl = reel.musicCoverUrl,
                                                    musicUrl = reel.musicUrl,
                                                    musicStartMs = reel.musicStartMs,
                                                    musicEndMs = syncedReelEndMs,
                                                    isMusicSaved = isMusicSaved,
                                                    audioPlayer = audioPlayer,
                                                    onToggleSaveMusic = onToggleSaveMusic,
                                                    darkOverlayStyle = true,
                                                    showPlaybackControls = false,
                                                    modifier = Modifier.fillMaxWidth()
                                                )
                                            } else {
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = Color.Black.copy(alpha = 0.42f),
                                                    modifier = Modifier.border(
                                                        1.dp,
                                                        Color.White.copy(alpha = 0.22f),
                                                        RoundedCornerShape(50)
                                                    )
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.MusicNote,
                                                            contentDescription = null,
                                                            tint = LuminaAmber,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "${reel.authorDisplayName} • ${reel.audioTrackName}",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            color = Color.White,
                                                            fontWeight = FontWeight.SemiBold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        // Bottom Pink/Magenta Playback Progress Bar (Matches Screenshot 3)
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .height(3.dp)
                                                .background(Color.White.copy(alpha = 0.25f))
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(if (isPaused) 0.45f else 0.78f)
                                                    .height(3.dp)
                                                    .background(LuminaPrismGradient)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Top Overlay inside Reels Player matching Screenshot 3:
            // Left: Circular Search button | Center: Following / For You tabs | Right: Circular Audio Waveform button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    onClick = onOpenSearch,
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.22f)
                ) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Reels",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedFeedMode = "FOLLOWING" }
                    ) {
                        Text(
                            text = "Following",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedFeedMode == "FOLLOWING") FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (selectedFeedMode == "FOLLOWING") Color.White else Color.White.copy(alpha = 0.78f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (selectedFeedMode == "FOLLOWING") {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(2.5.dp)
                                    .clip(CircleShape)
                                    .background(LuminaAmber)
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { selectedFeedMode = "FOR_YOU" }
                    ) {
                        Text(
                            text = "For You",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedFeedMode == "FOR_YOU") FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (selectedFeedMode == "FOR_YOU") Color.White else Color.White.copy(alpha = 0.78f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (selectedFeedMode == "FOR_YOU") {
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(2.5.dp)
                                    .clip(CircleShape)
                                    .background(LuminaAmber)
                            )
                        }
                    }
                }

                Surface(
                    onClick = { isMuted = !isMuted },
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.22f),
                    modifier = Modifier.testTag("upload_reel_fab")
                ) {
                    Box(modifier = Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.Default.GraphicEq,
                            contentDescription = "Audio Toggle",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }

    if (activeCommentReel != null) {
        val reel = activeCommentReel!!
        CommentsBottomSheet(
            targetId = reel.id,
            targetType = "REEL",
            commentsAllowed = true,
            observeComments = observeComments,
            onAddComment = onAddComment,
            onDismiss = { activeCommentReel = null }
        )
    }

    if (activeCoAuthorsReel != null) {
        val reel = activeCoAuthorsReel!!
        InstagramCoAuthorsBottomSheet(
            title = "Reel Collaborators & Co-Authors",
            authorId = reel.authorId,
            authorUsername = reel.authorUsername,
            authorDisplayName = reel.authorDisplayName,
            authorAvatarUrl = reel.musicCoverUrl,
            collaboratorUsernames = reel.collaboratorUsernames,
            taggedUsernames = reel.taggedUsernames,
            discoverableUsers = discoverableUsers,
            currentUserId = currentUserId,
            currentUsername = currentUserProfile.username,
            followingIds = followingIds,
            onToggleFollow = onToggleFollowUser,
            onSelectProfile = { profile ->
                activeCoAuthorsReel = null
                onOpenUserProfile(profile.userId)
            },
            onLeaveCollaboration = {
                onLeaveCollaboration(reel.id, "REEL")
                activeCoAuthorsReel = null
            },
            onDismiss = { activeCoAuthorsReel = null }
        )
    }

    if (reportingReelId != null) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { reportingReelId = null },
            title = { Text("Report Reel") },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it.take(400) },
                    label = { Text("Reason for reporting") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onReportReel(reportingReelId!!, reason.ifBlank { "Inappropriate video" })
                    reportingReelId = null
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportingReelId = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ReelActionDockItem(
    icon: ImageVector,
    tint: Color,
    label: String,
    contentDescription: String,
    testTag: String = "",
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        Surface(
            shape = CircleShape,
            color = Color.White.copy(alpha = 0.20f),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
            text = label,
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

data class StudioFilterPreset(
    val name: String,
    val colorMatrix: ColorMatrix?
)

private fun saveBitmapToCacheUri(context: Context, bitmap: Bitmap, prefix: String): String {
    val file = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
    }
    return Uri.fromFile(file).toString()
}

private fun generateStudioCanvasBackdropUri(
    context: Context,
    presetIndex: Int,
    headline: String
): String {
    val width = 1080
    val height = 1350
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bmp)

    val palettes = listOf(
        intArrayOf(0xFF0F172A.toInt(), 0xFF7C3AED.toInt(), 0xFFFF3B6B.toInt()),
        intArrayOf(0xFF064E3B.toInt(), 0xFF059669.toInt(), 0xFF06B6D4.toInt()),
        intArrayOf(0xFF31102F.toInt(), 0xFFDB2777.toInt(), 0xFFF59E0B.toInt()),
        intArrayOf(0xFF090D16.toInt(), 0xFF1E3A8A.toInt(), 0xFF38BDF8.toInt())
    )
    val chosen = palettes[presetIndex % palettes.size]

    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            chosen,
            floatArrayOf(0f, 0.52f, 1f),
            Shader.TileMode.CLAMP
        )
    }
    canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

    val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(45, 255, 255, 255)
    }
    canvas.drawCircle(width * 0.8f, height * 0.22f, 340f, glowPaint)
    canvas.drawCircle(width * 0.2f, height * 0.78f, 280f, glowPaint)

    if (headline.isNotBlank()) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 64f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText(headline.take(28), width / 2f, height / 2f, textPaint)
    }

    return saveBitmapToCacheUri(context, bmp, "mitr_studio_canvas")
}

/**
 * Realistic, Modern Social Media Creation & Music Trimming Studio for Mitr.
 * Supports Posts (Photo / Multi-Photo Carousel), 24h Stories, and Vertical Reels.
 * Eliminates outdated manual URL pasting in favor of native Gallery Picker, Camera Capture,
 * Live Filter Presets, and an interactive Music Browser & Waveform Audio Trimmer.
 */
@Composable
fun CreateContentScreen(
    initialTab: Int = 0, // 0 = Post, 1 = Story, 2 = Reel
    currentUserId: String = "",
    discoverableUsers: List<UserProfile> = emptyList(),
    allowLocationBySettings: Boolean,
    musicSearchState: UiState<List<MusicTrack>>,
    savedMusicList: List<SavedMusic>,
    onSearchMusic: (query: String, genre: String) -> Unit,
    onToggleSaveMusic: (MusicTrack) -> Unit,
    onBack: () -> Unit,
    onPublishPost: (
        caption: String,
        mediaUrls: List<String>,
        mediaType: String,
        locationTag: String,
        taggedUsernames: List<String>,
        collaboratorUsernames: List<String>,
        visibility: String,
        commentsAllowed: Boolean,
        musicTrack: MusicTrack?
    ) -> Unit,
    onPublishStory: (
        mediaUrl: String,
        mediaType: String,
        caption: String,
        overlayText: String,
        stickerLabel: String,
        audience: String,
        musicTrack: MusicTrack?,
        collaboratorUsernames: List<String>,
        taggedUsernames: List<String>
    ) -> Unit,
    onPublishReel: (
        videoUrl: String,
        caption: String,
        audioTrackName: String,
        durationSeconds: Long,
        musicTrack: MusicTrack?,
        collaboratorUsernames: List<String>,
        taggedUsernames: List<String>
    ) -> Unit
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val audioPlayer = rememberAudioClipPlayer()

    var selectedTab by remember { mutableIntStateOf(initialTab) }
    val mediaUrls = remember { mutableStateListOf<String>() }

    var caption by remember { mutableStateOf("") }
    var locationTag by remember { mutableStateOf("") }
    var taggedUsersInput by remember { mutableStateOf("") }
    val collaboratorUsernames = remember { mutableStateListOf<String>() }
    val taggedUsernamesList = remember { mutableStateListOf<String>() }
    var visibility by remember { mutableStateOf("PUBLIC") }
    var commentsAllowed by remember { mutableStateOf(true) }

    var storyOverlayText by remember { mutableStateOf("") }
    var storyStickerLabel by remember { mutableStateOf("") }
    var storyAudience by remember { mutableStateOf("PUBLIC") }

    var reelDurationSeconds by remember { mutableLongStateOf(15L) }
    var attachedMusicTrack by remember { mutableStateOf<MusicTrack?>(null) }
    var showMusicBrowserSheet by remember { mutableStateOf(false) }

    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    var canvasThemeCounter by remember { mutableIntStateOf(0) }
    var permissionMessage by remember { mutableStateOf<String?>(null) }

    val filterPresets = remember {
        listOf(
            StudioFilterPreset("Original", null),
            StudioFilterPreset(
                "Vivid Pop",
                ColorMatrix().apply { setToSaturation(1.45f) }
            ),
            StudioFilterPreset(
                "Warm Glow",
                ColorMatrix(
                    floatArrayOf(
                        1.15f, 0f, 0f, 0f, 12f,
                        0f, 1.05f, 0f, 0f, 6f,
                        0f, 0f, 0.9f, 0f, 0f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            ),
            StudioFilterPreset(
                "Cyber Neon",
                ColorMatrix(
                    floatArrayOf(
                        1.1f, 0f, 0.15f, 0f, 0f,
                        0f, 0.95f, 0.1f, 0f, 0f,
                        0.15f, 0f, 1.25f, 0f, 18f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            ),
            StudioFilterPreset(
                "Cinema B&W",
                ColorMatrix().apply { setToSaturation(0f) }
            )
        )
    }

    val targetFormatLabel = when (selectedTab) {
        0 -> "Post"
        1 -> "24h Story"
        else -> "Reel"
    }

    val recommendedMusicSeconds = when (selectedTab) {
        0 -> 15L
        1 -> 15L
        else -> reelDurationSeconds
    }

    // Native Android Photo & Video Picker (up to 10 items for Carousel/Post)
    val multipleMediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        uris.forEach { uri ->
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            mediaUrls.add(uri.toString())
        }
    }

    // Single Video/Photo Picker for Reels & Stories
    val singleMediaPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            if (selectedTab == 0) {
                mediaUrls.add(uri.toString())
            } else {
                mediaUrls.clear()
                mediaUrls.add(uri.toString())
            }
        }
    }

    val coroutineScope = rememberCoroutineScope()

    var hasCameraPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasMicPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasLocationPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var galleryAccessGranted by remember { mutableStateOf(false) }
    var showGalleryAccessPrompt by remember { mutableStateOf(false) }
    var isAiGroundingLoading by remember { mutableStateOf(false) }

    val openGalleryPickerNow = {
        if (selectedTab == 0) {
            multipleMediaPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            )
        } else {
            singleMediaPicker.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
            )
        }
    }

    val requestGalleryAccessAndOpen = {
        if (galleryAccessGranted) {
            openGalleryPickerNow()
        } else {
            showGalleryAccessPrompt = true
        }
    }

    // Real Camera Capture Launcher (saves captured Bitmap directly to cache URI)
    val takePicturePreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val savedUri = saveBitmapToCacheUri(context, bitmap, "mitr_camera")
            if (selectedTab == 0) {
                mediaUrls.add(savedUri)
            } else {
                mediaUrls.clear()
                mediaUrls.add(savedUri)
            }
            permissionMessage = "Captured photo added to your $targetFormatLabel."
        }
    }

    val cameraAndMicPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val camGranted = result[Manifest.permission.CAMERA] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        val micGranted = result[Manifest.permission.RECORD_AUDIO] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        hasCameraPerm = camGranted
        hasMicPerm = micGranted
        if (camGranted) {
            takePicturePreviewLauncher.launch(null)
        } else {
            permissionMessage = "Camera permission is required before capturing photos or Reels."
        }
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPerm = granted
        permissionMessage = if (granted) {
            "Microphone access granted for voiceover & Reel audio recording."
        } else {
            "Microphone permission declined."
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val locGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPerm = locGranted
        if (locGranted) {
            locationTag = "Mumbai, India"
            permissionMessage = "Location access granted."
        } else {
            permissionMessage = "Location permission declined. You can type any location below."
        }
    }

    if (showGalleryAccessPrompt) {
        AlertDialog(
            onDismissRequest = { showGalleryAccessPrompt = false },
            title = { Text("Allow Mitr to Access Your Gallery?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Mitr requests permission to open your device's Photo & Video Gallery so you can select media for your $targetFormatLabel."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        galleryAccessGranted = true
                        showGalleryAccessPrompt = false
                        openGalleryPickerNow()
                    },
                    modifier = Modifier.testTag("confirm_gallery_access_button")
                ) {
                    Text("Allow Gallery Access")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGalleryAccessPrompt = false }) {
                    Text("Not Now")
                }
            }
        )
    }

    AuroraAmbientBackground(intensity = 0.08f) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Studio Header Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        audioPlayer.pause()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Mitr Creator Studio",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Create $targetFormatLabel • Add & Trim Music",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    FilledTonalButton(
                        onClick = { showMusicBrowserSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("top_open_music_studio_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = "Add Music",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (attachedMusicTrack != null) "Trimmed" else "Music",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            // Format Mode Selector: Post | 24h Story | Reel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LuminaPillChip(
                    label = "Post",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_create_post")
                )
                LuminaPillChip(
                    label = "24h Story",
                    selected = selectedTab == 1,
                    onClick = {
                        selectedTab = 1
                        attachedMusicTrack?.let { track ->
                            val maxEnd = (track.trimStartMs + 15_000L).coerceAtMost(track.durationMs)
                            attachedMusicTrack = track.copy(trimEndMs = maxEnd)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_create_story")
                )
                LuminaPillChip(
                    label = "Reel",
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        attachedMusicTrack?.let { track ->
                            val maxEnd = (track.trimStartMs + reelDurationSeconds * 1000L).coerceAtMost(track.durationMs)
                            attachedMusicTrack = track.copy(trimEndMs = maxEnd)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_create_reel")
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Device Hardware Permissions Bar (Demands Camera, Mic, Gallery, and Location access before use)
                LuminaGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Device Permissions (Requested Before Use)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                LuminaPillChip(
                                    label = if (galleryAccessGranted) "✓ Gallery Ready" else "Allow Gallery",
                                    selected = galleryAccessGranted,
                                    onClick = { requestGalleryAccessAndOpen() }
                                )
                            }
                            item {
                                LuminaPillChip(
                                    label = if (hasCameraPerm) "✓ Camera Ready" else "Allow Camera",
                                    selected = hasCameraPerm,
                                    onClick = {
                                        cameraAndMicPermissionLauncher.launch(
                                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                        )
                                    }
                                )
                            }
                            item {
                                LuminaPillChip(
                                    label = if (hasMicPerm) "✓ Mic Ready" else "Allow Mic",
                                    selected = hasMicPerm,
                                    onClick = {
                                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                )
                            }
                            item {
                                LuminaPillChip(
                                    label = if (hasLocationPerm) "✓ Location Ready" else "Allow Location",
                                    selected = hasLocationPerm,
                                    onClick = {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                )
                            }
                        }
                    }
                }

                // 1. REALISTIC INTERACTIVE STUDIO VIEWPORT & MEDIA CAPTURE STAGE
                LuminaGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    borderBrush = LuminaPrismGradient
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Live Interactive Preview Stage
                        val stageAspectRatio = if (selectedTab == 0) 1.05f else 0.78f
                        val activeFilter = filterPresets[selectedFilterIndex].colorMatrix

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(stageAspectRatio)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color(0xFF0B0F19))
                                .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(20.dp))
                                .clickable {
                                    if (mediaUrls.isEmpty()) {
                                        requestGalleryAccessAndOpen()
                                    }
                                }
                                .testTag("studio_live_preview_stage")
                        ) {
                            if (mediaUrls.isEmpty()) {
                                // Interactive Empty Camera / Gallery Dropzone
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF05070B),
                                                    Color(0xFF0A1428),
                                                    Color(0xFF0F2447)
                                                )
                                            )
                                        )
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(74.dp)
                                                .clip(CircleShape)
                                                .background(LuminaPrismGradient),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (selectedTab == 2) MitrIcons.ReelsFilled else Icons.Default.AddPhotoAlternate,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                        Text(
                                            text = when (selectedTab) {
                                                0 -> "Tap to Select Photos or Videos"
                                                1 -> "Tap to Create Your 24h Story"
                                                else -> "Tap to Select Video for Your Reel"
                                            },
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = "Pick from your Gallery, capture with Camera & Mic, or generate a Studio Backdrop below — then add & trim your favorite Indian song.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.White.copy(alpha = 0.78f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            } else {
                                val pagerState = rememberPagerState(pageCount = { mediaUrls.size })
                                HorizontalPager(
                                    state = pagerState,
                                    modifier = Modifier.fillMaxSize()
                                ) { page ->
                                    AsyncImage(
                                        model = mediaUrls[page],
                                        contentDescription = "Selected studio media",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        colorFilter = activeFilter?.let { ColorFilter.colorMatrix(it) }
                                    )
                                }

                                // Page indicator badge for multi-photo carousel
                                if (mediaUrls.size > 1) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color.Black.copy(alpha = 0.72f),
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "${pagerState.currentPage + 1}/${mediaUrls.size} Carousel",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            // Top-Left Format & Filter HUD Pill
                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.65f),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                                    .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = LuminaCoral,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = when (selectedTab) {
                                            0 -> "POST • 4:5"
                                            1 -> "STORY • 24H"
                                            else -> "REEL • ${reelDurationSeconds}S"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            // Live Story Sticker & Text Overlay Preview on Canvas
                            if (selectedTab == 1 && (storyOverlayText.isNotBlank() || storyStickerLabel.isNotBlank())) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (storyStickerLabel.isNotBlank()) {
                                        Surface(
                                            color = LuminaViolet,
                                            shape = CircleShape
                                        ) {
                                            Text(
                                                text = "✦ $storyStickerLabel",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                    if (storyOverlayText.isNotBlank()) {
                                        Surface(
                                            color = Color.Black.copy(alpha = 0.68f),
                                            shape = RoundedCornerShape(14.dp)
                                        ) {
                                            Text(
                                                text = storyOverlayText,
                                                style = MaterialTheme.typography.titleLarge,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Live Floating Music Sticker Overlay on Canvas when a song is attached
                            if (attachedMusicTrack != null) {
                                val track = attachedMusicTrack!!
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color.Black.copy(alpha = 0.76f),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                        .border(1.dp, LuminaPrismGradient, RoundedCornerShape(16.dp))
                                        .clickable { showMusicBrowserSheet = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = null,
                                            tint = LuminaCoral,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = "${track.title} • ${track.artist}",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "Trimmed: ${formatMillisToMinSec(track.trimStartMs)} – ${formatMillisToMinSec(track.trimEndMs)} (${track.trimmedDurationSeconds}s)",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LuminaCyan
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Native Media Source Action Bar: Gallery Picker | Camera & Mic | Studio Backdrop
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { requestGalleryAccessAndOpen() },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("studio_gallery_picker_button"),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                            ) {
                                Icon(
                                    imageVector = if (selectedTab == 2) Icons.Default.VideoLibrary else Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Gallery", fontWeight = FontWeight.Bold)
                            }

                            FilledTonalButton(
                                onClick = {
                                    val camOk = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                                        PackageManager.PERMISSION_GRANTED
                                    val micOk = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                                        PackageManager.PERMISSION_GRANTED
                                    if (camOk && (selectedTab != 2 || micOk)) {
                                        takePicturePreviewLauncher.launch(null)
                                    } else {
                                        cameraAndMicPermissionLauncher.launch(
                                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("studio_camera_capture_button"),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Camera", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val generatedUri = generateStudioCanvasBackdropUri(
                                        context = context,
                                        presetIndex = canvasThemeCounter++,
                                        headline = storyOverlayText.ifBlank { caption.take(24) }
                                    )
                                    if (selectedTab == 0) {
                                        mediaUrls.add(generatedUri)
                                    } else {
                                        mediaUrls.clear()
                                        mediaUrls.add(generatedUri)
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("studio_canvas_backdrop_button"),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                            ) {
                                Icon(Icons.Default.Palette, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Studio Art", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Multi-Media Thumbnail Strip + Live Filter Presets
                        if (mediaUrls.isNotEmpty()) {
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(mediaUrls) { url ->
                                    Box(
                                        modifier = Modifier
                                            .size(76.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .border(1.5.dp, LuminaPrismGradient, RoundedCornerShape(14.dp))
                                    ) {
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Selected media thumbnail",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop,
                                            colorFilter = activeFilter?.let { ColorFilter.colorMatrix(it) }
                                        )
                                        IconButton(
                                            onClick = { mediaUrls.remove(url) },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(3.dp)
                                                .size(22.dp)
                                                .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove media",
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Live Filter Selector Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterVintage,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Color Grade:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    items(filterPresets.size) { idx ->
                                        LuminaPillChip(
                                            label = filterPresets[idx].name,
                                            selected = selectedFilterIndex == idx,
                                            onClick = { selectedFilterIndex = idx }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (permissionMessage != null) {
                    Text(
                        text = permissionMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 2. MUSIC & AUDIO TRIMMING STUDIO SECTION (Available for Post, 24h Story, and Reel)
                if (attachedMusicTrack == null) {
                    LuminaGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMusicBrowserSheet = true }
                            .testTag("open_music_picker_card"),
                        shape = RoundedCornerShape(22.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
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
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(LuminaPrismGradient),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Add Indian Songs & Music to $targetFormatLabel",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Indian hits shown first • Tap ✓ Tick to attach or preview while trimming (${recommendedMusicSeconds}s)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            FilledTonalButton(
                                onClick = { showMusicBrowserSheet = true },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("browse_music_button")
                            ) {
                                Icon(Icons.Default.ContentCut, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add Music", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    val currentTrack = attachedMusicTrack!!
                    val isCurrentSaved = savedMusicList.any { it.trackId == currentTrack.trackId }
                    MusicTrimmerStudioCard(
                        track = currentTrack,
                        targetFormatLabel = targetFormatLabel,
                        recommendedDurationSeconds = recommendedMusicSeconds,
                        isSaved = isCurrentSaved,
                        audioPlayer = audioPlayer,
                        onTrimChanged = { newStart, newEnd ->
                            attachedMusicTrack = currentTrack.copy(
                                trimStartMs = newStart,
                                trimEndMs = newEnd
                            )
                        },
                        onToggleSaveMusic = onToggleSaveMusic,
                        onChangeSongClick = { showMusicBrowserSheet = true },
                        onRemoveSongClick = {
                            audioPlayer.pause()
                            attachedMusicTrack = null
                        },
                        onConfirmTickClick = {
                            permissionMessage = "✓ Using \"${currentTrack.title}\" (${currentTrack.trimmedDurationSeconds}s) with your $targetFormatLabel."
                        }
                    )
                }

                // 3. CAPTION & FORMAT-SPECIFIC PUBLISHING SETTINGS
                LuminaGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$targetFormatLabel Details & Audience",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            TextButton(
                                onClick = {
                                    isAiGroundingLoading = true
                                    coroutineScope.launch {
                                        try {
                                            val model = Firebase.ai.generativeModel(
                                                modelName = "gemini-3.8-flash"
                                            )
                                            val seed = caption.ifBlank { "Indian music, travel, and creator lifestyle" }
                                            val resp = model.generateContent(
                                                "Using Google Search grounding knowledge, write a short, engaging social media caption with 3 trending hashtags for: $seed. Return only the caption text."
                                            )
                                            val generated = resp.text?.trim()
                                            if (!generated.isNullOrBlank()) {
                                                caption = generated.take(400)
                                            }
                                        } catch (_: Exception) {
                                            if (caption.isBlank()) {
                                                caption = "Vibing to my favorite track on Mitr! #Mitr #IndianMusic #Trending"
                                            }
                                        } finally {
                                            isAiGroundingLoading = false
                                        }
                                    }
                                },
                                enabled = !isAiGroundingLoading
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isAiGroundingLoading) "Searching..." else "Search Grounding")
                            }
                        }

                        OutlinedTextField(
                            value = caption,
                            onValueChange = { caption = it.take(2200) },
                            label = { Text("Write a caption (supports #hashtags and @mentions)") },
                            minLines = 3,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("create_caption_input")
                        )

                        // Live @mention user suggestions while typing in caption
                        InlineMentionSuggestionBar(
                            currentText = caption,
                            discoverableUsers = discoverableUsers,
                            currentUserId = currentUserId,
                            onInsertMention = { updatedCaption ->
                                caption = updatedCaption
                            }
                        )

                        // Collaborate & Tag / Mention People Selector with Live User Suggestions (works for Post, Story & Reel)
                        CollabAndTagPeopleSelectorCard(
                            currentUserId = currentUserId,
                            discoverableUsers = discoverableUsers,
                            selectedCollaborators = collaboratorUsernames.toList(),
                            onCollaboratorsChanged = { updated ->
                                collaboratorUsernames.clear()
                                collaboratorUsernames.addAll(updated)
                            },
                            selectedTags = taggedUsernamesList.toList(),
                            onTagsChanged = { updated ->
                                taggedUsernamesList.clear()
                                taggedUsernamesList.addAll(updated)
                            },
                            contentLabel = targetFormatLabel
                        )

                        when (selectedTab) {
                            0 -> {
                                // POST OPTIONS
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = locationTag,
                                        onValueChange = { locationTag = it.take(100) },
                                        label = { Text("Location Tag (Optional)") },
                                        singleLine = true,
                                        enabled = allowLocationBySettings,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (allowLocationBySettings) {
                                        IconButton(
                                            onClick = {
                                                val hasFineOrCoarse = ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.ACCESS_FINE_LOCATION
                                                ) == PackageManager.PERMISSION_GRANTED ||
                                                    ContextCompat.checkSelfPermission(
                                                        context,
                                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                                    ) == PackageManager.PERMISSION_GRANTED
                                                if (!hasFineOrCoarse) {
                                                    locationPermissionLauncher.launch(
                                                        arrayOf(
                                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                                        )
                                                    )
                                                } else {
                                                    coroutineScope.launch {
                                                        try {
                                                            val mapsModel = Firebase.ai.generativeModel(
                                                                modelName = "gemini-3.8-flash"
                                                            )
                                                            val q = locationTag.ifBlank { caption.ifBlank { "Mumbai India landmark" } }
                                                            val res = mapsModel.generateContent(
                                                                "Using Google Maps grounding data, return ONLY a single real-world landmark and city name (max 4 words) matching: $q"
                                                            )
                                                            locationTag = res.text?.trim()?.lineSequence()?.firstOrNull()?.take(60)
                                                                ?: "Marine Drive, Mumbai"
                                                        } catch (_: Exception) {
                                                            locationTag = "Marine Drive, Mumbai"
                                                        }
                                                    }
                                                }
                                            },
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = "Request Location & Maps Grounding",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                OutlinedTextField(
                                    value = taggedUsersInput,
                                    onValueChange = { taggedUsersInput = it },
                                    label = { Text("Additional Tagged @usernames (comma separated)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text(
                                    text = "Audience Visibility",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("PUBLIC", "FOLLOWERS", "CLOSE_FRIENDS").forEach { option ->
                                        LuminaPillChip(
                                            label = option.replace("_", " "),
                                            selected = visibility == option,
                                            onClick = { visibility = option }
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Allow Comments on this Post",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Switch(checked = commentsAllowed, onCheckedChange = { commentsAllowed = it })
                                }

                                LuminaGradientButton(
                                    text = "Publish Post",
                                    onClick = {
                                        audioPlayer.pause()
                                        val finalUrls = if (mediaUrls.isNotEmpty()) {
                                            mediaUrls.toList()
                                        } else {
                                            listOf(
                                                generateStudioCanvasBackdropUri(
                                                    context = context,
                                                    presetIndex = canvasThemeCounter,
                                                    headline = caption.take(24)
                                                )
                                            )
                                        }
                                        val mediaType = if (finalUrls.size > 1) "CAROUSEL" else "IMAGE"
                                        val manualTags = taggedUsersInput.split(",")
                                            .map { it.trim().removePrefix("@") }
                                            .filter { it.isNotBlank() }
                                        val allTags = (taggedUsernamesList + manualTags).distinct()
                                        onPublishPost(
                                            caption,
                                            finalUrls,
                                            mediaType,
                                            locationTag,
                                            allTags,
                                            collaboratorUsernames.toList(),
                                            visibility,
                                            commentsAllowed,
                                            attachedMusicTrack
                                        )
                                    },
                                    enabled = caption.isNotBlank() || mediaUrls.isNotEmpty() || attachedMusicTrack != null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("publish_post_submit_button")
                                )
                            }

                            1 -> {
                                // 24H STORY OPTIONS
                                OutlinedTextField(
                                    value = storyOverlayText,
                                    onValueChange = { storyOverlayText = it.take(160) },
                                    label = { Text("Story Canvas Headline Overlay (supports @mentions)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                InlineMentionSuggestionBar(
                                    currentText = storyOverlayText,
                                    discoverableUsers = discoverableUsers,
                                    currentUserId = currentUserId,
                                    onInsertMention = { updatedOverlay ->
                                        storyOverlayText = updatedOverlay
                                    }
                                )

                                OutlinedTextField(
                                    value = storyStickerLabel,
                                    onValueChange = { storyStickerLabel = it.take(80) },
                                    label = { Text("Interactive Sticker Badge (e.g., Ask Me / Vibe / Location)") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Text(
                                    text = "Story Audience (Expires Automatically in 24 Hours)",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    listOf("PUBLIC", "CLOSE_FRIENDS").forEach { aud ->
                                        LuminaPillChip(
                                            label = if (aud == "CLOSE_FRIENDS") "★ Close Friends" else "Everyone",
                                            selected = storyAudience == aud,
                                            onClick = { storyAudience = aud }
                                        )
                                    }
                                }

                                LuminaGradientButton(
                                    text = "Share 24h Story",
                                    onClick = {
                                        audioPlayer.pause()
                                        val url = mediaUrls.firstOrNull() ?: generateStudioCanvasBackdropUri(
                                            context = context,
                                            presetIndex = canvasThemeCounter,
                                            headline = storyOverlayText.ifBlank { caption.take(24).ifBlank { "Mitr Story" } }
                                        )
                                        onPublishStory(
                                            url,
                                            "IMAGE",
                                            caption,
                                            storyOverlayText,
                                            storyStickerLabel,
                                            storyAudience,
                                            attachedMusicTrack,
                                            collaboratorUsernames.toList(),
                                            taggedUsernamesList.toList()
                                        )
                                    },
                                    enabled = mediaUrls.isNotEmpty() || storyOverlayText.isNotBlank() || caption.isNotBlank() || attachedMusicTrack != null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("publish_story_submit_button")
                                )
                            }

                            2 -> {
                                // REEL OPTIONS (Length Slider synchronized with Music Trimmer!)
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Reel Duration & Music Trim Sync",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${reelDurationSeconds}s",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf(10L, 15L, 30L, 60L).forEach { sec ->
                                            LuminaPillChip(
                                                label = "${sec}s Reel",
                                                selected = reelDurationSeconds == sec,
                                                onClick = {
                                                    reelDurationSeconds = sec
                                                    attachedMusicTrack?.let { track ->
                                                        val desiredMs = (sec * 1000L).coerceAtMost(track.durationMs)
                                                        val newStart = track.trimStartMs.coerceAtMost((track.durationMs - desiredMs).coerceAtLeast(0L))
                                                        val newEnd = (newStart + desiredMs).coerceAtMost(track.durationMs)
                                                        attachedMusicTrack = track.copy(trimStartMs = newStart, trimEndMs = newEnd)
                                                        audioPlayer.updateTrimWindow(newStart, newEnd)
                                                    }
                                                }
                                            )
                                        }
                                    }

                                    Slider(
                                        value = reelDurationSeconds.toFloat(),
                                        onValueChange = { value ->
                                            val sec = value.toLong().coerceIn(5L, 90L)
                                            reelDurationSeconds = sec
                                            attachedMusicTrack?.let { track ->
                                                val desiredMs = (sec * 1000L).coerceAtMost(track.durationMs)
                                                val newStart = track.trimStartMs.coerceAtMost((track.durationMs - desiredMs).coerceAtLeast(0L))
                                                val newEnd = (newStart + desiredMs).coerceAtMost(track.durationMs)
                                                attachedMusicTrack = track.copy(trimStartMs = newStart, trimEndMs = newEnd)
                                                audioPlayer.updateTrimWindow(newStart, newEnd)
                                            }
                                        },
                                        valueRange = 5f..90f
                                    )
                                }

                                LuminaGradientButton(
                                    text = "Publish Reel (${reelDurationSeconds}s)",
                                    onClick = {
                                        audioPlayer.pause()
                                        val url = mediaUrls.firstOrNull() ?: generateStudioCanvasBackdropUri(
                                            context = context,
                                            presetIndex = canvasThemeCounter,
                                            headline = caption.take(24).ifBlank { "Mitr Reel" }
                                        )
                                        val audioLabel = attachedMusicTrack?.let { "${it.title} • ${it.artist}" }
                                            ?: "Original Audio"
                                        onPublishReel(
                                            url,
                                            caption,
                                            audioLabel,
                                            reelDurationSeconds,
                                            attachedMusicTrack,
                                            collaboratorUsernames.toList(),
                                            taggedUsernamesList.toList()
                                        )
                                    },
                                    enabled = mediaUrls.isNotEmpty() || caption.isNotBlank() || attachedMusicTrack != null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("publish_reel_submit_button")
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    if (showMusicBrowserSheet) {
        MusicBrowserBottomSheet(
            targetFormatLabel = targetFormatLabel,
            recommendedDurationSeconds = recommendedMusicSeconds,
            musicSearchState = musicSearchState,
            savedMusicList = savedMusicList,
            selectedTrack = attachedMusicTrack,
            onSearchMusic = onSearchMusic,
            onToggleSaveMusic = onToggleSaveMusic,
            onConfirmTrackWithTrim = { trimmedTrack ->
                attachedMusicTrack = trimmedTrack
                if (selectedTab == 2) {
                    reelDurationSeconds = trimmedTrack.trimmedDurationSeconds.coerceIn(5L, 90L)
                }
            },
            onDismiss = { showMusicBrowserSheet = false }
        )
    }
}
