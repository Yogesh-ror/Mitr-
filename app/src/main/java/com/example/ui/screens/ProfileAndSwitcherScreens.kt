package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.VideoView
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Comment
import com.example.data.model.Follow
import com.example.data.model.MusicTrack
import com.example.data.model.Post
import com.example.data.model.Reel
import com.example.data.model.SavedAccountSession
import com.example.data.model.SavedMusic
import com.example.data.model.SavedPost
import com.example.data.model.Story
import com.example.data.model.UserProfile
import com.example.data.model.UserSettings
import com.example.data.model.archiveDayBadge
import com.example.data.model.filterRecentPublicPostsForProfile
import com.example.data.model.filterUserReelsForProfile
import com.example.data.model.groupStoriesForArchive
import com.example.data.model.isFounderVipAccount
import com.example.data.model.isStoryExpired
import com.example.data.model.isUserBlueTickVerified
import com.example.data.model.parsedReactions
import com.example.data.model.storyCreatedMillis
import com.example.ui.components.AttachedMusicPillBanner
import com.example.ui.components.AuroraAmbientBackground
import com.example.ui.components.LuminaGlassCard
import com.example.ui.components.LuminaGradientButton
import com.example.ui.components.MitrBlueTickSubscriptionSheet
import com.example.ui.components.MitrIcons
import com.example.ui.components.MitrTopHeaderBar
import com.example.ui.components.VerifiedBadgeIcon
import com.example.ui.components.rememberAudioClipPlayer
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaCoral
import com.example.ui.theme.LuminaEmerald
import com.example.ui.theme.LuminaGlassBorderGradient
import com.example.ui.theme.LuminaIndigo
import com.example.ui.theme.LuminaPrismGradient
import com.example.ui.theme.LuminaViolet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    currentUserId: String,
    viewedProfile: UserProfile,
    posts: List<Post>,
    reels: List<Reel>,
    savedPosts: List<SavedPost>,
    savedMusicList: List<SavedMusic> = emptyList(),
    userStories: List<Story> = emptyList(),
    discoverableUsers: List<UserProfile> = emptyList(),
    hideLikeCounts: Boolean = false,
    incomingFollowRequests: List<Follow>,
    outgoingFollows: List<Follow>,
    savedAccounts: List<SavedAccountSession>,
    usernameAvailable: Boolean?,
    onCheckUsername: (String) -> Unit,
    onSaveProfileEdits: (String, String, String, String, String, Boolean) -> Unit,
    onToggleFollow: (UserProfile) -> Unit,
    onToggleSaveMusic: (MusicTrack) -> Unit = {},
    onRespondFollowRequest: (Follow, Boolean) -> Unit,
    onMessageUser: (UserProfile) -> Unit,
    onAddRelation: (UserProfile, String) -> Unit,
    onLikePost: (Post) -> Unit = {},
    onSavePost: (Post) -> Unit = {},
    onDeletePost: (String) -> Unit = {},
    onSharePostToUser: (UserProfile, Post) -> Unit = { _, _ -> },
    onLikeReel: (Reel) -> Unit = {},
    onSaveReel: (Reel) -> Unit = {},
    onDeleteReel: (String) -> Unit = {},
    onOpenReelInFeed: (Reel) -> Unit = {},
    onCreateReel: () -> Unit = {},
    archivedStories: List<Story> = emptyList(),
    onRecordStoryView: (Story) -> Unit = {},
    onReactToStory: (Story, String, String) -> Unit = { _, _, _ -> },
    onDeleteStory: (String) -> Unit = {},
    onToggleHighlightStory: (Story, String) -> Unit = { _, _ -> },
    observeComments: (String, String) -> Flow<List<Comment>> = { _, _ -> emptyFlow() },
    observeFollowersForUser: (String) -> Flow<List<Follow>> = { emptyFlow() },
    observeFollowingForUser: (String) -> Flow<List<Follow>> = { emptyFlow() },
    onAddComment: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onOpenUserProfile: (String) -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenSearch: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    notificationBadgeCount: Int = 0,
    onOpenMessages: () -> Unit = {},
    onSwitchOrAddAccount: () -> Unit,
    onRemoveSavedAccount: (String) -> Unit,
    onSignOutCurrent: () -> Unit,
    onSignOutAll: () -> Unit,
    onBackIfOtherUser: (() -> Unit)? = null,
    userSettings: UserSettings = UserSettings(userId = currentUserId),
    currentUserEmail: String = "",
    onActivateBlueTickSubscription: (String) -> Unit = {},
    onSaveCreatorPayoutAccount: (String, String, String, String) -> Unit = { _, _, _, _ -> }
) {
    if (onBackIfOtherUser != null) {
        BackHandler { onBackIfOtherUser() }
    }

    val context = LocalContext.current
    val audioPlayer = rememberAudioClipPlayer()
    val effectiveProfileUid = viewedProfile.userId.ifBlank { viewedProfile.id }
    val isOwner = effectiveProfileUid == currentUserId || viewedProfile.id == currentUserId
    val isViewedProfileVerified = isUserBlueTickVerified(
        profile = viewedProfile,
        email = if (isOwner) currentUserEmail.ifBlank { userSettings.email } else null,
        settings = if (isOwner) userSettings else null
    )
    val isFounderVip = isOwner && isFounderVipAccount(
        email = currentUserEmail.ifBlank { userSettings.email },
        username = viewedProfile.username
    )
    var selectedTab by remember { mutableIntStateOf(0) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showAccountSwitcherSheet by remember { mutableStateOf(false) }
    var showBlueTickSubscriptionSheet by remember { mutableStateOf(false) }
    var optionsMenuExpanded by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.release()
        }
    }

    LaunchedEffect(selectedTab, effectiveProfileUid) {
        audioPlayer.pause()
    }

    // Followers / Following sheet state ("FOLLOWERS" or "FOLLOWING")
    var followersFollowingSheetMode by remember { mutableStateOf<String?>(null) }
    var showPrivateAccountFollowLockNotice by remember { mutableStateOf(false) }

    val profileFollowers by observeFollowersForUser(effectiveProfileUid)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val profileFollowing by observeFollowingForUser(effectiveProfileUid)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Upload viewer dialog states when tapping any upload tile in Profile
    var openedPost by remember { mutableStateOf<Post?>(null) }
    var openedReel by remember { mutableStateOf<Reel?>(null) }
    var commentTargetPost by remember { mutableStateOf<Post?>(null) }
    var commentTargetReel by remember { mutableStateOf<Reel?>(null) }
    var shareTargetPost by remember { mutableStateOf<Post?>(null) }

    // Story & Highlight states on Profile
    var openedProfileStories by remember { mutableStateOf<List<Story>?>(null) }
    var openedProfileStoriesInitialIndex by remember { mutableIntStateOf(0) }
    var showAddHighlightPicker by remember { mutableStateOf(false) }
    var savedContentFilter by remember { mutableStateOf("ALL") } // ALL, REELS, POSTS
    var storyArchiveFilter by remember { mutableStateOf("ALL") } // ALL, PAST, ACTIVE, HIGHLIGHTED

    // Direct quick-photo-picker so the user can update their profile picture directly from the header
    val directAvatarPicker = rememberLauncherForActivityResult(
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
            onSaveProfileEdits(
                viewedProfile.username,
                viewedProfile.displayName,
                viewedProfile.bio,
                viewedProfile.website,
                uri.toString(),
                viewedProfile.isPrivate
            )
        }
    }

    var postsDisplayMode by remember { mutableStateOf("BOTH") } // BOTH, GRID, FEED
    var reelsDisplayMode by remember { mutableStateOf("BOTH") } // BOTH, GRID, FEED

    val userPosts = remember(posts, viewedProfile.userId, viewedProfile.id, viewedProfile.username, isOwner, currentUserId) {
        filterRecentPublicPostsForProfile(
            posts = posts,
            userId = viewedProfile.userId,
            username = viewedProfile.username,
            isOwner = isOwner,
            profileDocId = viewedProfile.id,
            currentUserId = currentUserId
        )
    }
    val userReels = remember(reels, viewedProfile.userId, viewedProfile.id, viewedProfile.username, isOwner, currentUserId) {
        filterUserReelsForProfile(
            reels = reels,
            userId = viewedProfile.userId,
            profileDocId = viewedProfile.id,
            username = viewedProfile.username,
            isOwner = isOwner,
            currentUserId = currentUserId
        )
    }
    val taggedPosts = remember(posts, viewedProfile.username) {
        val cleanUname = viewedProfile.username.trim().removePrefix("@")
        posts.filter {
            it.taggedUsernames.any { tag -> tag.removePrefix("@").equals(cleanUname, ignoreCase = true) } ||
                it.collaboratorUsernames.any { c -> c.removePrefix("@").equals(cleanUname, ignoreCase = true) }
        }.distinctBy { it.id }
    }

    val profileAllStories = remember(userStories, archivedStories, effectiveProfileUid) {
        (userStories + archivedStories)
            .filter { it.authorId == effectiveProfileUid || (isOwner && it.authorId == currentUserId) }
            .distinctBy { it.id }
            .sortedByDescending { it.storyCreatedMillis() }
    }
    val activeUserStories = remember(profileAllStories) {
        val now = System.currentTimeMillis()
        profileAllStories.filter { it.expiresAtMillis > now }
    }
    val expiredUserStories = remember(profileAllStories) {
        val now = System.currentTimeMillis()
        profileAllStories.filter { it.isStoryExpired(now) }
    }
    val highlightedStories = remember(profileAllStories) {
        profileAllStories.filter { it.isHighlighted }
    }

    val followRelation = outgoingFollows.find { it.followingId == viewedProfile.userId }
    val pendingIncoming = incomingFollowRequests.filter { it.status == "PENDING" }
    val canViewFollowersAndFollowing = !viewedProfile.isPrivate || isOwner || followRelation?.status == "ACCEPTED"

    AuroraAmbientBackground(intensity = 0.05f) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar matching Screenshot 2: [Orb Logo] Mitr | Profile + Search, Bell, Chat+, Profile Avatar
            MitrTopHeaderBar(
                sectionTitle = if (onBackIfOtherUser != null) "@${viewedProfile.username}" else "Profile",
                notificationBadgeCount = notificationBadgeCount,
                onSearchClick = onOpenSearch,
                onNotificationsClick = onOpenNotifications,
                onQuickMessageClick = onOpenMessages,
                avatarContent = {
                    if (onBackIfOtherUser != null) {
                        IconButton(onClick = onBackIfOtherUser, modifier = Modifier.size(34.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { showAccountSwitcherSheet = true }
                                .testTag("profile_account_switcher_trigger"),
                            contentAlignment = Alignment.Center
                        ) {
                            UserAvatarBadge(
                                avatarUrl = viewedProfile.avatarUrl,
                                displayName = viewedProfile.displayName,
                                size = 32.dp,
                                showBorder = true
                            )
                        }
                    }
                }
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            val tabs = if (isOwner) {
                listOf("Posts", "Reels", "Saved", "Archive", "Tagged")
            } else {
                listOf("Posts", "Reels", "Tagged")
            }
            val safeTabIndex = selectedTab.coerceIn(0, tabs.lastIndex)
            val activeTabTitle = tabs.getOrElse(safeTabIndex) { "Posts" }

            // Unified scrollable LazyVerticalGrid so the entire Profile header, tabs, and posts/empty states scroll smoothly!
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("profile_scrollable_grid"),
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp, bottom = 36.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item(key = "profile_header_section", span = { GridItemSpan(3) }) {
                    // Profile Header Card matching Screenshot 2
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Large Circular Avatar with Violet-to-Magenta Ring & Pink 'LIVE' Pill Badge at Bottom
                    val hasActiveStories = activeUserStories.isNotEmpty()
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clickable {
                                if (hasActiveStories) {
                                    openedProfileStories = activeUserStories
                                    activeUserStories.firstOrNull()?.let(onRecordStoryView)
                                } else if (isOwner) {
                                    directAvatarPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            }
                            .testTag("profile_avatar_story_trigger"),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .border(3.dp, LuminaPrismGradient, CircleShape)
                                .padding(3.5.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            UserAvatarBadge(
                                avatarUrl = viewedProfile.avatarUrl,
                                displayName = viewedProfile.displayName,
                                size = 82.dp
                            )
                        }

                        // Pink-Magenta 'LIVE' Pill Badge at bottom of avatar (Matches Screenshot 2)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = Color(0xFFD81B60),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .border(1.5.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(50))
                        ) {
                            Text(
                                text = if (hasActiveStories) "STORY" else "LIVE",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    // Right: Soft Rounded Pill Container for Posts | Followers | Following (Matches Screenshot 2)
                    Surface(
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ProfileStatColumn(
                                count = userPosts.size.toLong(),
                                label = "Posts",
                                testTag = "profile_stat_posts",
                                onClick = { selectedTab = 0 }
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(26.dp)
                                    .background(MaterialTheme.colorScheme.outline)
                            )
                            ProfileStatColumn(
                                count = userReels.size.toLong(),
                                label = "Reels",
                                testTag = "profile_stat_reels",
                                onClick = {
                                    val reelIdx = tabs.indexOf("Reels")
                                    if (reelIdx >= 0) selectedTab = reelIdx
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(26.dp)
                                    .background(MaterialTheme.colorScheme.outline)
                            )
                            ProfileStatColumn(
                                count = maxOf(viewedProfile.followerCount, profileFollowers.size.toLong()),
                                label = "Followers",
                                testTag = "profile_stat_followers",
                                onClick = {
                                    if (canViewFollowersAndFollowing) {
                                        followersFollowingSheetMode = "FOLLOWERS"
                                    } else {
                                        showPrivateAccountFollowLockNotice = true
                                    }
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(26.dp)
                                    .background(MaterialTheme.colorScheme.outline)
                            )
                            ProfileStatColumn(
                                count = maxOf(viewedProfile.followingCount, profileFollowing.size.toLong()),
                                label = "Following",
                                testTag = "profile_stat_following",
                                onClick = {
                                    if (canViewFollowersAndFollowing) {
                                        followersFollowingSheetMode = "FOLLOWING"
                                    } else {
                                        showPrivateAccountFollowLockNotice = true
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Name + Verified Blue Tick + Username + Bio + Website Link
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = viewedProfile.displayName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            if (isViewedProfileVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                VerifiedBadgeIcon(
                                    size = 18.dp,
                                    modifier = Modifier
                                        .clickable { if (isOwner) showBlueTickSubscriptionSheet = true }
                                        .testTag("profile_verified_blue_tick_badge")
                                )
                            }
                            if (viewedProfile.isPrivate) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = "Private Account",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Text(
                                text = "@${viewedProfile.username}",
                                style = MaterialTheme.typography.bodySmall,
                                color = LuminaIndigo,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (isViewedProfileVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                VerifiedBadgeIcon(size = 13.dp)
                            }
                            Text(
                                text = if (isViewedProfileVerified) {
                                    " • Verified Creator (3.5x Priority Reach)"
                                } else {
                                    " • Creator & Visual Storyteller"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = LuminaIndigo,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    if (activeUserStories.isNotEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                            modifier = Modifier
                                .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                                .clickable {
                                    openedProfileStories = activeUserStories
                                    activeUserStories.firstOrNull()?.let(onRecordStoryView)
                                }
                                .testTag("profile_watch_story_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Default.PlayCircleFilled,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "Story (${activeUserStories.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .testTag("profile_bio_section")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bio",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold
                            )
                            if (isOwner && viewedProfile.bio.isBlank()) {
                                Text(
                                    text = "Tap to add bio",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LuminaIndigo,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { showEditProfileDialog = true }
                                )
                            }
                        }
                        Text(
                            text = viewedProfile.bio.ifBlank {
                                "Sharing public moments, reels & stories on Mitr."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.testTag("profile_bio_text")
                        )
                    }
                }

                if (viewedProfile.website.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = LuminaIndigo,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = viewedProfile.website,
                            style = MaterialTheme.typography.labelMedium,
                            color = LuminaIndigo,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons Row matching Screenshot 2:
                // [Gradient Pill: Edit Profile] + [Soft Pill: Share] + [Circular Settings/Switcher Icon]
                if (isOwner) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(50))
                                .background(LuminaPrismGradient)
                                .clickable { showEditProfileDialog = true }
                                .testTag("edit_profile_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Edit Profile",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Follow @${viewedProfile.username} (${viewedProfile.displayName}) on Mitr!"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Profile"))
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Share",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .clickable(onClick = onOpenSettings)
                                .testTag("open_settings_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = "Settings Center",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .clickable { showAccountSwitcherSheet = true }
                                .testTag("account_switcher_icon_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.SwitchAccount,
                                    contentDescription = "Switch Account",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mitr Verified Blue Tick Subscription & Priority Reel Reach Pill Banner
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFF1A73E8).copy(alpha = 0.12f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF1A73E8).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable { showBlueTickSubscriptionSheet = true }
                            .testTag("profile_blue_tick_subscription_button")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                VerifiedBadgeIcon(size = 18.dp)
                                Column {
                                    Text(
                                        text = when {
                                            isFounderVip -> "Founder VIP Blue Tick Active (Free for yogeshkhanchi985@gmail.com)"
                                            isViewedProfileVerified -> "Mitr Verified Blue Tick Active • 3.5x Reel & Feed Reach"
                                            else -> "Get Verified Blue Tick & Boost Reel Views • ₹300/mo"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF1A73E8),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = if (isFounderVip || isViewedProfileVerified) {
                                            "Tap to boost Reel reach or manage ₹300 payout bank/UPI account"
                                        } else {
                                            "Add Blue Tick to @${viewedProfile.username} & show your Reels to more people"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = Color(0xFF1A73E8)
                            ) {
                                Text(
                                    text = when {
                                        isFounderVip -> "VIP ₹0"
                                        isViewedProfileVerified -> "Manage"
                                        else -> "₹300"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(50))
                                .background(LuminaPrismGradient)
                                .clickable { onToggleFollow(viewedProfile) }
                                .testTag("profile_follow_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (followRelation?.status) {
                                    "ACCEPTED" -> "Following"
                                    "PENDING" -> "Requested"
                                    else -> "Follow"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { onMessageUser(viewedProfile) }
                                .testTag("profile_message_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    MitrIcons.MessageOutlined,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Message", fontWeight = FontWeight.Bold)
                            }
                        }

                        Box {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .clickable { optionsMenuExpanded = true }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.MoreVert, contentDescription = "User Options")
                                }
                            }
                            DropdownMenu(
                                expanded = optionsMenuExpanded,
                                onDismissRequest = { optionsMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Add to Close Friends") },
                                    leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                                    onClick = {
                                        optionsMenuExpanded = false
                                        onAddRelation(viewedProfile, "CLOSE_FRIEND")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Restrict Account") },
                                    onClick = {
                                        optionsMenuExpanded = false
                                        onAddRelation(viewedProfile, "RESTRICTED")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Block Account") },
                                    leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) },
                                    onClick = {
                                        optionsMenuExpanded = false
                                        onAddRelation(viewedProfile, "BLOCKED")
                                    }
                                )
                            }
                        }
                    }
                }

                // Story Highlights & Personal Story Archive Row
                if (highlightedStories.isNotEmpty() || isOwner) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .testTag("profile_highlights_row"),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isOwner) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable {
                                        val archiveIdx = tabs.indexOf("Archive")
                                        if (archiveIdx >= 0) {
                                            selectedTab = archiveIdx
                                        }
                                    }
                                    .testTag("profile_open_story_archive_button")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                listOf(
                                                    LuminaViolet.copy(alpha = 0.22f),
                                                    LuminaCoral.copy(alpha = 0.22f)
                                                )
                                            )
                                        )
                                        .border(1.5.dp, LuminaPrismGradient, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = "Story Archive",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Archive (${profileAllStories.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        if (isOwner && profileAllStories.isNotEmpty()) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clickable { showAddHighlightPicker = true }
                                    .testTag("add_story_highlight_button")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "New Highlight",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "New",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        highlightedStories.forEach { story ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(64.dp)
                                    .clickable {
                                        openedProfileStories = listOf(story)
                                        onRecordStoryView(story)
                                    }
                                    .testTag("highlight_badge_${story.id}")
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(58.dp)
                                        .clip(CircleShape)
                                        .border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                        .padding(2.5.dp)
                                ) {
                                    if (story.mediaUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = story.mediaUrl,
                                            contentDescription = story.highlightTitle,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                Icons.Default.AutoAwesome,
                                                contentDescription = null,
                                                tint = LuminaAmber,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = story.highlightTitle.ifBlank { "Highlight" },
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Pending Follow Requests Card for Owner
                if (isOwner && pendingIncoming.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    LuminaGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Follow Requests (${pendingIncoming.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            pendingIncoming.forEach { req ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("@${req.followerUsername}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { onRespondFollowRequest(req, true) }) {
                                            Text("Approve")
                                        }
                                        OutlinedButton(onClick = { onRespondFollowRequest(req, false) }) {
                                            Text("Decline")
                                        }
                                    }
                                }
                            }
                        }
                    }
                    }
                }
                }

                // Segmented Pill Tab Bar matching Screenshot 2 (Posts | Reels | Saved | Archive | Tagged) with clear full height & count badges
                item(key = "profile_tabs_bar", span = { GridItemSpan(3) }) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("profile_tabs_container")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            tabs.forEachIndexed { index, title ->
                                val isSelected = safeTabIndex == index
                                val tabIcon = when (title) {
                                    "Posts" -> Icons.Default.GridOn
                                    "Reels" -> Icons.Default.PlayCircleFilled
                                    "Saved" -> Icons.Default.BookmarkBorder
                                    "Archive" -> Icons.Default.History
                                    else -> Icons.Default.AccountBox
                                }
                                val displayTabLabel = when (title) {
                                    "Posts" -> if (userPosts.isNotEmpty()) "Posts (${userPosts.size})" else "Posts"
                                    "Reels" -> if (userReels.isNotEmpty()) "Reels (${userReels.size})" else "Reels"
                                    else -> title
                                }
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(50))
                                        .clickable { selectedTab = index }
                                        .testTag("profile_tab_${title.lowercase()}")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Icon(
                                            imageVector = tabIcon,
                                            contentDescription = title,
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = displayTabLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab Content inside the unified scrollable LazyVerticalGrid
                when (activeTabTitle) {
                    "Posts" -> {
                        item(key = "recent_public_posts_header", span = { GridItemSpan(3) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .testTag("profile_recent_public_posts_header"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Recent Public Posts (${userPosts.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Live synced from Firestore for @${viewedProfile.username}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf(
                                        "BOTH" to "All",
                                        "GRID" to "Grid",
                                        "FEED" to "Cards"
                                    ).forEach { (modeKey, modeLabel) ->
                                        val selected = postsDisplayMode == modeKey
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .clickable { postsDisplayMode = modeKey }
                                                .testTag("profile_posts_mode_${modeKey.lowercase()}")
                                        ) {
                                            Text(
                                                text = modeLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (userPosts.isEmpty()) {
                            item(key = "empty_posts_card", span = { GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                ) {
                                    EmptyOrErrorCard(
                                        title = "No public posts yet",
                                        subtitle = "Recent public photo and carousel posts fetched from Firestore for @${viewedProfile.username} will appear here."
                                    )
                                }
                            }
                        } else {
                            if (postsDisplayMode == "BOTH" || postsDisplayMode == "GRID") {
                                items(userPosts, key = { "post_${it.id}" }) { post ->
                                    ProfileGridTile(
                                        imageUrl = post.mediaUrls.firstOrNull() ?: "",
                                        caption = post.caption,
                                        hasMusic = post.musicUrl.isNotBlank(),
                                        isCollab = post.collaboratorUsernames.isNotEmpty(),
                                        testTag = "profile_post_tile_${post.id}",
                                        onClick = { openedPost = post }
                                    )
                                }
                            }

                            if (postsDisplayMode == "BOTH" || postsDisplayMode == "FEED") {
                                item(key = "recent_public_posts_cards_label", span = { GridItemSpan(3) }) {
                                    Text(
                                        text = "Recent Public Posts Feed",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                                    )
                                }
                                items(
                                    items = userPosts.take(15),
                                    key = { "recent_card_${it.id}" },
                                    span = { GridItemSpan(3) }
                                ) { post ->
                                    val isLiked = post.likedByUids.contains(currentUserId)
                                    val isSaved = savedPosts.any { it.postId == post.id }
                                    LuminaGlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { openedPost = post }
                                            .testTag("profile_recent_post_card_${post.id}"),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    UserAvatarBadge(
                                                        avatarUrl = viewedProfile.avatarUrl.ifBlank { post.authorAvatarUrl },
                                                        displayName = viewedProfile.displayName.ifBlank { post.authorDisplayName },
                                                        size = 36.dp,
                                                        showBorder = true
                                                    )
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = viewedProfile.displayName.ifBlank { post.authorDisplayName },
                                                                style = MaterialTheme.typography.titleSmall,
                                                                fontWeight = FontWeight.ExtraBold
                                                            )
                                                            if (isViewedProfileVerified) {
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                VerifiedBadgeIcon(size = 14.dp)
                                                            }
                                                        }
                                                        Text(
                                                            text = "@${viewedProfile.username.ifBlank { post.authorUsername }} • ${formatNotificationTimeMillis((post.createdAt?.seconds ?: 0L) * 1000L)}",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = LuminaEmerald.copy(alpha = 0.14f)
                                                ) {
                                                    Text(
                                                        text = post.visibility.ifBlank { "PUBLIC" },
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = LuminaEmerald,
                                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }

                                            val firstMedia = post.mediaUrls.firstOrNull().orEmpty()
                                            if (firstMedia.isNotBlank()) {
                                                AsyncImage(
                                                    model = firstMedia,
                                                    contentDescription = post.caption,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(190.dp)
                                                        .clip(RoundedCornerShape(14.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }

                                            if (post.caption.isNotBlank()) {
                                                Text(
                                                    text = post.caption,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .clickable { onLikePost(post) }
                                                            .padding(vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                            contentDescription = "Like",
                                                            tint = if (isLiked) LuminaCoral else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = if (hideLikeCounts) "Like" else "${post.likeCount}",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .clickable { commentTargetPost = post }
                                                            .padding(vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.ChatBubbleOutline,
                                                            contentDescription = "Comments",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "${post.commentCount}",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                IconButton(
                                                    onClick = { onSavePost(post) },
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                        contentDescription = "Save Post",
                                                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Reels" -> {
                        item(key = "profile_reels_tab_header", span = { GridItemSpan(3) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                                    .testTag("profile_reels_tab_header"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isOwner) "Your Reels (${userReels.size})" else "@${viewedProfile.username}'s Reels (${userReels.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                    Text(
                                        text = "Tap any Reel to play with audio or watch in full-screen Reels feed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    listOf(
                                        "BOTH" to "All",
                                        "GRID" to "Grid",
                                        "FEED" to "Cards"
                                    ).forEach { (modeKey, modeLabel) ->
                                        val selected = reelsDisplayMode == modeKey
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .clickable { reelsDisplayMode = modeKey }
                                                .testTag("profile_reels_mode_${modeKey.lowercase()}")
                                        ) {
                                            Text(
                                                text = modeLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (userReels.isEmpty()) {
                            item(key = "empty_reels_card", span = { GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                ) {
                                    EmptyOrErrorCard(
                                        title = "No reels yet",
                                        subtitle = if (isOwner) {
                                            "Record or upload a 15–60s vertical Reel with background music to showcase it on your Profile Reels tab."
                                        } else {
                                            "Published vertical short videos from @${viewedProfile.username} will appear here."
                                        },
                                        actionLabel = if (isOwner) "Create Reel" else null,
                                        onAction = if (isOwner) onCreateReel else null
                                    )
                                }
                            }
                        } else {
                            if (reelsDisplayMode == "BOTH" || reelsDisplayMode == "GRID") {
                                items(userReels, key = { "reel_${it.id}" }) { reel ->
                                    ProfileReelGridTile(
                                        reel = reel,
                                        testTag = "profile_reel_tile_${reel.id}",
                                        onClick = { openedReel = reel },
                                        onOpenInReelsFeed = { onOpenReelInFeed(reel) }
                                    )
                                }
                            }

                            if (reelsDisplayMode == "BOTH" || reelsDisplayMode == "FEED") {
                                item(key = "profile_reels_cards_label", span = { GridItemSpan(3) }) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 6.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Accessible Reels Player Cards",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        if (isOwner) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = LuminaCoral.copy(alpha = 0.14f),
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .clickable { onCreateReel() }
                                                    .testTag("profile_reels_create_button")
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Videocam,
                                                        contentDescription = "New Reel",
                                                        tint = LuminaCoral,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Text(
                                                        text = "+ New Reel",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = LuminaCoral
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                items(
                                    items = userReels,
                                    key = { "profile_reel_card_${it.id}" },
                                    span = { GridItemSpan(3) }
                                ) { reel ->
                                    val isLiked = reel.likedByUids.contains(currentUserId)
                                    val isSaved = savedPosts.any {
                                        it.postId == reel.id || it.resolvedReelId == reel.id
                                    }
                                    LuminaGlassCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable { openedReel = reel }
                                            .testTag("profile_reel_card_${reel.id}"),
                                        shape = RoundedCornerShape(18.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    UserAvatarBadge(
                                                        avatarUrl = viewedProfile.avatarUrl,
                                                        displayName = viewedProfile.displayName.ifBlank { reel.authorDisplayName },
                                                        size = 36.dp,
                                                        showBorder = true
                                                    )
                                                    Column {
                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Text(
                                                                text = viewedProfile.displayName.ifBlank { reel.authorDisplayName.ifBlank { "@${reel.authorUsername}" } },
                                                                style = MaterialTheme.typography.titleSmall,
                                                                fontWeight = FontWeight.ExtraBold
                                                            )
                                                            if (isViewedProfileVerified) {
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                VerifiedBadgeIcon(size = 14.dp)
                                                            }
                                                        }
                                                        Text(
                                                            text = "@${reel.authorUsername.ifBlank { viewedProfile.username }} • ${reel.durationSeconds.coerceIn(15L, 60L)}s Reel • ${reel.viewCount} views",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }

                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(50))
                                                        .clickable { onOpenReelInFeed(reel) }
                                                        .testTag("profile_reel_watch_in_feed_${reel.id}")
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayCircleFilled,
                                                            contentDescription = "Open in Reels Feed",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                        Text(
                                                            text = "Open in Reels",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }
                                            }

                                            // Reel Preview Banner with Play Button Overlay
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(210.dp)
                                                    .clip(RoundedCornerShape(14.dp))
                                                    .background(
                                                        Brush.linearGradient(
                                                            listOf(
                                                                Color(0xFF141829),
                                                                LuminaIndigo.copy(alpha = 0.85f),
                                                                LuminaViolet.copy(alpha = 0.85f),
                                                                LuminaCoral.copy(alpha = 0.78f)
                                                            )
                                                        )
                                                    )
                                                    .clickable { openedReel = reel },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                val previewArt = reel.musicCoverUrl.ifBlank { reel.videoUrl }
                                                if (previewArt.isNotBlank()) {
                                                    AsyncImage(
                                                        model = previewArt,
                                                        contentDescription = reel.caption,
                                                        modifier = Modifier.fillMaxSize(),
                                                        contentScale = ContentScale.Crop
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .background(
                                                            Brush.verticalGradient(
                                                                listOf(
                                                                    Color.Black.copy(alpha = 0.30f),
                                                                    Color.Black.copy(alpha = 0.15f),
                                                                    Color.Black.copy(alpha = 0.72f)
                                                                )
                                                            )
                                                        )
                                                )

                                                // Center Play Action Pill
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = Color.Black.copy(alpha = 0.65f),
                                                    modifier = Modifier
                                                        .border(1.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(50))
                                                ) {
                                                    Row(
                                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.PlayArrow,
                                                            contentDescription = "Play Reel",
                                                            tint = Color.White,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Text(
                                                            text = "Tap to Play Reel",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Color.White
                                                        )
                                                    }
                                                }

                                                // Bottom Audio Track Pill inside preview
                                                Row(
                                                    modifier = Modifier
                                                        .align(Alignment.BottomStart)
                                                        .fillMaxWidth()
                                                        .padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.MusicNote,
                                                            contentDescription = null,
                                                            tint = LuminaAmber,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                        Text(
                                                            text = if (reel.musicTitle.isNotBlank()) {
                                                                "${reel.musicTitle} • ${reel.musicArtist}"
                                                            } else {
                                                                reel.audioTrackName.ifBlank { "Original Audio - @${reel.authorUsername}" }
                                                            },
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Color.Black.copy(alpha = 0.65f)
                                                    ) {
                                                        Text(
                                                            text = "${reel.durationSeconds.coerceIn(15L, 60L)}s",
                                                            style = MaterialTheme.typography.labelSmall,
                                                            color = Color.White,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }

                                            if (reel.caption.isNotBlank()) {
                                                Text(
                                                    text = reel.caption,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 3,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .clickable { onLikeReel(reel) }
                                                            .padding(vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                            contentDescription = "Like Reel",
                                                            tint = if (isLiked) LuminaCoral else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = if (hideLikeCounts) "Like" else "${reel.likeCount}",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier
                                                            .clickable { commentTargetReel = reel }
                                                            .padding(vertical = 4.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Outlined.ChatBubbleOutline,
                                                            contentDescription = "Reel Comments",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = "${reel.commentCount}",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                    }
                                                }
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    if (reel.authorId == currentUserId || isOwner) {
                                                        IconButton(
                                                            onClick = { onDeleteReel(reel.id) },
                                                            modifier = Modifier.size(32.dp)
                                                        ) {
                                                            Icon(
                                                                imageVector = Icons.Default.Delete,
                                                                contentDescription = "Delete Reel",
                                                                tint = MaterialTheme.colorScheme.error,
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                    IconButton(
                                                        onClick = { onSaveReel(reel) },
                                                        modifier = Modifier.size(32.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                            contentDescription = "Save Reel",
                                                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                            modifier = Modifier.size(18.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    "Saved" -> {
                        val savedReelItems = savedPosts.filter {
                            it.targetType.equals("REEL", ignoreCase = true) || it.reelId.isNotBlank()
                        }
                        val savedPostItems = savedPosts.filter {
                            !it.targetType.equals("REEL", ignoreCase = true) && it.reelId.isBlank()
                        }
                        val displayedSavedItems = when (savedContentFilter) {
                            "REELS" -> savedReelItems
                            "POSTS" -> savedPostItems
                            else -> savedPosts
                        }

                        if (savedMusicList.isEmpty() && savedPosts.isEmpty()) {
                            item(key = "empty_saved_card", span = { GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                ) {
                                    EmptyOrErrorCard(
                                        title = "Nothing saved yet",
                                        subtitle = "Tap Save on any Reel, Post, or Song to keep it in your personal Firestore collection for later viewing."
                                    )
                                }
                            }
                        } else {
                            if (savedMusicList.isNotEmpty()) {
                                item(key = "saved_songs_header", span = { GridItemSpan(3) }) {
                                    Text(
                                        text = "Saved Songs (${savedMusicList.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                                    )
                                }
                                items(
                                    items = savedMusicList,
                                    key = { "music_${it.id}" },
                                    span = { GridItemSpan(3) }
                                ) { savedTrack ->
                                    AttachedMusicPillBanner(
                                        musicTrackId = savedTrack.trackId,
                                        musicTitle = savedTrack.title,
                                        musicArtist = savedTrack.artist,
                                        musicCoverUrl = savedTrack.coverUrl,
                                        musicUrl = savedTrack.audioUrl,
                                        musicStartMs = savedTrack.trimStartMs,
                                        musicEndMs = savedTrack.trimEndMs,
                                        isMusicSaved = true,
                                        audioPlayer = audioPlayer,
                                        onToggleSaveMusic = onToggleSaveMusic,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 4.dp)
                                    )
                                }
                            }

                            if (savedPosts.isNotEmpty()) {
                                item(key = "saved_posts_header", span = { GridItemSpan(3) }) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 6.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Saved Reels & Posts (${savedPosts.size})",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.horizontalScroll(rememberScrollState())
                                        ) {
                                            listOf(
                                                "ALL" to "All (${savedPosts.size})",
                                                "REELS" to "Saved Reels (${savedReelItems.size})",
                                                "POSTS" to "Saved Posts (${savedPostItems.size})"
                                            ).forEach { (filterKey, label) ->
                                                val selected = savedContentFilter == filterKey
                                                Surface(
                                                    shape = RoundedCornerShape(50),
                                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(50))
                                                        .clickable { savedContentFilter = filterKey }
                                                        .testTag("saved_filter_chip_${filterKey.lowercase()}")
                                                ) {
                                                    Text(
                                                        text = label,
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                items(displayedSavedItems, key = { "saved_${it.id}_${it.postId}" }) { saved ->
                                    val isSavedReelItem = saved.targetType.equals("REEL", ignoreCase = true) || saved.reelId.isNotBlank()
                                    val targetReelOrPostId = saved.resolvedReelId.ifBlank { saved.postId }
                                    ProfileGridTile(
                                        imageUrl = saved.mediaUrl,
                                        caption = if (isSavedReelItem) {
                                            "🎬 Reel • @${saved.authorUsername}: ${saved.captionPreview}"
                                        } else {
                                            "@${saved.authorUsername}: ${saved.captionPreview}"
                                        },
                                        testTag = "profile_saved_tile_${saved.id}",
                                        onClick = {
                                            val matchingReel = reels.find { it.id == targetReelOrPostId || it.id == saved.postId }
                                            val matchingPost = posts.find { it.id == saved.postId }
                                            when {
                                                matchingReel != null -> openedReel = matchingReel
                                                isSavedReelItem -> {
                                                    openedReel = Reel(
                                                        id = targetReelOrPostId,
                                                        authorId = saved.userId,
                                                        authorUsername = saved.authorUsername,
                                                        authorDisplayName = saved.authorUsername,
                                                        videoUrl = saved.mediaUrl,
                                                        caption = saved.captionPreview
                                                    )
                                                }
                                                matchingPost != null -> openedPost = matchingPost
                                                else -> {
                                                    openedPost = Post(
                                                        id = saved.postId,
                                                        authorId = saved.userId,
                                                        authorUsername = saved.authorUsername,
                                                        authorDisplayName = saved.authorUsername,
                                                        caption = saved.captionPreview,
                                                        mediaUrls = if (saved.mediaUrl.isNotBlank()) listOf(saved.mediaUrl) else emptyList()
                                                    )
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    "Archive" -> {
                        val filteredArchiveStories = when (storyArchiveFilter) {
                            "PAST" -> expiredUserStories
                            "ACTIVE" -> activeUserStories
                            "HIGHLIGHTED" -> highlightedStories
                            else -> profileAllStories
                        }
                        val archiveGroups = groupStoriesForArchive(filteredArchiveStories)

                        item(key = "story_archive_header_card", span = { GridItemSpan(3) }) {
                            LuminaGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .testTag("profile_story_archive_section"),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(LuminaPrismGradient),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.History,
                                                    contentDescription = "Story Archive",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                            Column {
                                                Text(
                                                    text = "Personal Story Archive",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Text(
                                                    text = "${profileAllStories.size} total • ${expiredUserStories.size} past (>24h) • ${activeUserStories.size} active",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        if (filteredArchiveStories.isNotEmpty()) {
                                            FilledTonalButton(
                                                onClick = {
                                                    openedProfileStoriesInitialIndex = 0
                                                    openedProfileStories = filteredArchiveStories
                                                },
                                                shape = RoundedCornerShape(50),
                                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                                modifier = Modifier.testTag("story_archive_play_all_button")
                                            ) {
                                                Icon(
                                                    Icons.Default.PlayCircleFilled,
                                                    contentDescription = "Play All",
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Play All", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }

                                    Text(
                                        text = "Only you can see your Story Archive. Past stories are automatically saved here after 24 hours so you can revisit memories or feature them in Profile Highlights anytime.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState())
                                    ) {
                                        listOf(
                                            "ALL" to "All Stories (${profileAllStories.size})",
                                            "PAST" to "Past / Expired (${expiredUserStories.size})",
                                            "ACTIVE" to "Active 24h (${activeUserStories.size})",
                                            "HIGHLIGHTED" to "Highlighted (${highlightedStories.size})"
                                        ).forEach { (filterKey, label) ->
                                            val isSelected = storyArchiveFilter == filterKey
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(50))
                                                    .clickable { storyArchiveFilter = filterKey }
                                                    .testTag("story_archive_filter_${filterKey.lowercase()}")
                                            ) {
                                                Text(
                                                    text = label,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        if (filteredArchiveStories.isEmpty()) {
                            item(key = "empty_story_archive_card", span = { GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                ) {
                                    EmptyOrErrorCard(
                                        title = "No stories in this archive view",
                                        subtitle = "Stories you share are automatically organized here by month and date so you can view past stories anytime."
                                    )
                                }
                            }
                        } else {
                            archiveGroups.forEach { group ->
                                item(
                                    key = "archive_month_${group.monthYearHeader}",
                                    span = { GridItemSpan(3) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = group.monthYearHeader,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "${group.stories.size} ${if (group.stories.size == 1) "story" else "stories"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                items(
                                    items = group.stories,
                                    key = { "archive_story_${it.id}" }
                                ) { archivedStory ->
                                    StoryArchiveTileCard(
                                        story = archivedStory,
                                        onOpenStory = {
                                            val indexInFiltered = filteredArchiveStories.indexOfFirst { it.id == archivedStory.id }.coerceAtLeast(0)
                                            openedProfileStoriesInitialIndex = indexInFiltered
                                            openedProfileStories = filteredArchiveStories
                                            onRecordStoryView(archivedStory)
                                        },
                                        onToggleHighlight = {
                                            onToggleHighlightStory(
                                                archivedStory,
                                                archivedStory.highlightTitle.ifBlank { "Memories" }
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    "Tagged" -> {
                        if (taggedPosts.isEmpty()) {
                            item(key = "empty_tagged_card", span = { GridItemSpan(3) }) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 12.dp)
                                ) {
                                    EmptyOrErrorCard(
                                        title = "No tagged posts",
                                        subtitle = "Posts where @${viewedProfile.username} is tagged or collaborating appear here."
                                    )
                                }
                            }
                        } else {
                            items(taggedPosts, key = { "tagged_${it.id}" }) { post ->
                                ProfileGridTile(
                                    imageUrl = post.mediaUrls.firstOrNull() ?: "",
                                    caption = post.caption,
                                    hasMusic = post.musicUrl.isNotBlank(),
                                    isCollab = post.collaboratorUsernames.isNotEmpty(),
                                    testTag = "profile_tagged_tile_${post.id}",
                                    onClick = { openedPost = post }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Full Post Upload Viewer Dialog when user taps any Post tile in Profile
    val currentOpenedPost = openedPost?.let { target -> posts.find { it.id == target.id } ?: target }
    if (currentOpenedPost != null) {
        ProfilePostDetailDialog(
            post = currentOpenedPost,
            currentUserId = currentUserId,
            isSaved = savedPosts.any { it.postId == currentOpenedPost.id },
            savedMusicIds = remember(savedMusicList) { savedMusicList.map { it.trackId }.toSet() },
            hideLikeCounts = hideLikeCounts,
            audioPlayer = audioPlayer,
            onDismiss = {
                audioPlayer.pause()
                openedPost = null
            },
            onLike = { onLikePost(currentOpenedPost) },
            onSave = { onSavePost(currentOpenedPost) },
            onOpenComments = { commentTargetPost = currentOpenedPost },
            onShareToPerson = { shareTargetPost = currentOpenedPost },
            onToggleSaveMusic = onToggleSaveMusic,
            onDelete = if (currentOpenedPost.authorId == currentUserId) {
                {
                    audioPlayer.pause()
                    onDeletePost(currentOpenedPost.id)
                    openedPost = null
                }
            } else null
        )
    }

    // Full Reel Upload Viewer Dialog when user taps any Reel tile in Profile or Saved Reels
    val currentOpenedReel = openedReel?.let { target -> reels.find { it.id == target.id } ?: target }
    if (currentOpenedReel != null) {
        val isCurrentReelSaved = savedPosts.any {
            it.postId == currentOpenedReel.id || it.resolvedReelId == currentOpenedReel.id
        }
        ProfileReelDetailDialog(
            reel = currentOpenedReel,
            currentUserId = currentUserId,
            isSaved = isCurrentReelSaved,
            savedMusicIds = remember(savedMusicList) { savedMusicList.map { it.trackId }.toSet() },
            audioPlayer = audioPlayer,
            onDismiss = {
                audioPlayer.pause()
                openedReel = null
            },
            onLike = { onLikeReel(currentOpenedReel) },
            onSave = { onSaveReel(currentOpenedReel) },
            onOpenComments = { commentTargetReel = currentOpenedReel },
            onOpenInReelsFeed = {
                audioPlayer.pause()
                val targetReel = currentOpenedReel
                openedReel = null
                onOpenReelInFeed(targetReel)
            },
            onDelete = if (currentOpenedReel.authorId == currentUserId || isOwner) {
                {
                    audioPlayer.pause()
                    onDeleteReel(currentOpenedReel.id)
                    openedReel = null
                }
            } else null,
            onToggleSaveMusic = onToggleSaveMusic
        )
    }

    if (showBlueTickSubscriptionSheet) {
        MitrBlueTickSubscriptionSheet(
            currentUserProfile = viewedProfile,
            currentUserSettings = userSettings,
            currentUserEmail = currentUserEmail.ifBlank { userSettings.email },
            onActivateSubscription = { paymentLabel ->
                onActivateBlueTickSubscription(paymentLabel)
                showBlueTickSubscriptionSheet = false
            },
            onSavePayoutAccount = { holder, upiOrBank, accNum, ifsc ->
                onSaveCreatorPayoutAccount(holder, upiOrBank, accNum, ifsc)
            },
            onDismiss = { showBlueTickSubscriptionSheet = false }
        )
    }

    // Comments Sheet from Profile Post Viewer
    if (commentTargetPost != null) {
        val post = commentTargetPost!!
        CommentsBottomSheet(
            targetId = post.id,
            targetType = "POST",
            commentsAllowed = post.commentsAllowed,
            observeComments = observeComments,
            onAddComment = onAddComment,
            onDismiss = { commentTargetPost = null }
        )
    }

    // Comments Sheet from Profile Reel Viewer
    if (commentTargetReel != null) {
        val reel = commentTargetReel!!
        CommentsBottomSheet(
            targetId = reel.id,
            targetType = "REEL",
            commentsAllowed = true,
            observeComments = observeComments,
            onAddComment = onAddComment,
            onDismiss = { commentTargetReel = null }
        )
    }

    // Share Post to Another Person Sheet from Profile Post Viewer
    if (shareTargetPost != null) {
        val post = shareTargetPost!!
        SharePostToPersonBottomSheet(
            post = post,
            currentUserId = currentUserId,
            discoverableUsers = discoverableUsers,
            onShareToUser = { recipient, sharedPost ->
                onSharePostToUser(recipient, sharedPost)
                shareTargetPost = null
            },
            onDismiss = { shareTargetPost = null }
        )
    }

    // Story Viewer Dialog opened from Profile Avatar, Story Highlights Row, or Story Archive
    if (openedProfileStories != null && openedProfileStories!!.isNotEmpty()) {
        FullScreenStoryViewerDialog(
            stories = openedProfileStories!!,
            initialIndex = openedProfileStoriesInitialIndex.coerceIn(0, (openedProfileStories!!.size - 1).coerceAtLeast(0)),
            currentUserId = currentUserId,
            savedMusicList = savedMusicList,
            discoverableUsers = discoverableUsers,
            onToggleSaveMusic = onToggleSaveMusic,
            onToggleHighlightStory = onToggleHighlightStory,
            onDismiss = {
                audioPlayer.pause()
                openedProfileStories = null
                openedProfileStoriesInitialIndex = 0
            },
            onRecordView = onRecordStoryView,
            onReactToStory = onReactToStory,
            onDeleteStory = onDeleteStory,
            onMuteStoryAuthor = {},
            onReportStory = {}
        )
    }

    // Add Story Highlight Picker Modal for Profile Owner
    if (showAddHighlightPicker) {
        var highlightLabel by remember { mutableStateOf("Highlights") }
        AlertDialog(
            onDismissRequest = { showAddHighlightPicker = false },
            title = { Text("Manage Story Highlights", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = highlightLabel,
                        onValueChange = { highlightLabel = it.take(30) },
                        label = { Text("Highlight Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "Tap any of your stories below to feature or remove from Profile Highlights:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyColumn(
                        modifier = Modifier.height(220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(profileAllStories, key = { it.id }) { story ->
                            LuminaGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onToggleHighlightStory(story, highlightLabel.ifBlank { "Highlights" })
                                    },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = story.caption.ifBlank { story.overlayText.ifBlank { "Story" } },
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${story.viewerIds.size} views",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (story.isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                    ) {
                                        Text(
                                            text = if (story.isHighlighted) "Highlighted ✓" else "Highlight",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (story.isHighlighted) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAddHighlightPicker = false }) {
                    Text("Done")
                }
            }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var username by remember { mutableStateOf(viewedProfile.username) }
        var displayName by remember { mutableStateOf(viewedProfile.displayName) }
        var bio by remember { mutableStateOf(viewedProfile.bio) }
        var website by remember { mutableStateOf(viewedProfile.website) }
        var avatarUrl by remember { mutableStateOf(viewedProfile.avatarUrl) }
        var isPrivate by remember { mutableStateOf(viewedProfile.isPrivate) }

        val picker = rememberLauncherForActivityResult(
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
                avatarUrl = uri.toString()
            }
        }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .clickable {
                                picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatarBadge(
                            avatarUrl = avatarUrl,
                            displayName = displayName,
                            size = 86.dp,
                            showBorder = true
                        )
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = "Change Photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            val clean = it.lowercase().filter { c -> c.isLetterOrDigit() || c == '_' || c == '-' }.take(30)
                            username = clean
                            onCheckUsername(clean)
                        },
                        label = { Text("Username") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = displayName,
                        onValueChange = { displayName = it.take(60) },
                        label = { Text("Display Name") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it.take(300) },
                        label = { Text("Bio (supports @mentions & collabs)") },
                        maxLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Live user suggestions when typing @mention in Bio
                    InlineMentionSuggestionBar(
                        currentText = bio,
                        discoverableUsers = discoverableUsers,
                        currentUserId = currentUserId,
                        onInsertMention = { updatedBio ->
                            bio = updatedBio
                        }
                    )

                    OutlinedTextField(
                        value = website,
                        onValueChange = { website = it.take(200) },
                        label = { Text("Website") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Private Account")
                        Switch(checked = isPrivate, onCheckedChange = { isPrivate = it })
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveProfileEdits(username, displayName, bio, website, avatarUrl, isPrivate)
                        showEditProfileDialog = false
                    },
                    enabled = username.length >= 3 && displayName.isNotBlank() && usernameAvailable != false
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Followers & Following List Bottom Sheet (Accessible to everyone if Public, or to Accepted Followers if Private)
    if (followersFollowingSheetMode != null) {
        val isFollowersMode = followersFollowingSheetMode == "FOLLOWERS"
        val relationships = if (isFollowersMode) profileFollowers else profileFollowing
        ModalBottomSheet(onDismissRequest = { followersFollowingSheetMode = null }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isFollowersMode) {
                                "@${viewedProfile.username}'s Followers"
                            } else {
                                "@${viewedProfile.username} Following"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = if (viewedProfile.isPrivate) {
                                "Private Account • Visible to approved followers"
                            } else {
                                "Public Account • Visible to everyone on Mitr"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = { followersFollowingSheetMode = "FOLLOWERS" },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Followers (${profileFollowers.size})")
                        }
                        FilledTonalButton(
                            onClick = { followersFollowingSheetMode = "FOLLOWING" },
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Following (${profileFollowing.size})")
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                if (relationships.isEmpty()) {
                    EmptyOrErrorCard(
                        title = if (isFollowersMode) "No followers yet" else "Not following anyone yet",
                        subtitle = if (isFollowersMode) {
                            "People who follow @${viewedProfile.username} will appear here."
                        } else {
                            "Accounts followed by @${viewedProfile.username} will appear here."
                        }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.height(300.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(relationships, key = { it.id }) { rel ->
                            val targetUid = if (isFollowersMode) rel.followerId else rel.followingId
                            val matchedProfile = discoverableUsers.find { it.userId == targetUid }
                            val uname = matchedProfile?.username
                                ?: (if (isFollowersMode) rel.followerUsername else rel.followingUsername)
                            val dname = matchedProfile?.displayName ?: uname
                            val avatar = matchedProfile?.avatarUrl ?: ""

                            LuminaGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        followersFollowingSheetMode = null
                                        if (targetUid.isNotBlank()) {
                                            onOpenUserProfile(targetUid)
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        UserAvatarBadge(
                                            avatarUrl = avatar,
                                            displayName = dname,
                                            size = 42.dp,
                                            showBorder = true
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = dname,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "@$uname",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (matchedProfile != null && matchedProfile.userId != currentUserId) {
                                        val myFollow = outgoingFollows.find { it.followingId == matchedProfile.userId }
                                        OutlinedButton(
                                            onClick = { onToggleFollow(matchedProfile) },
                                            shape = RoundedCornerShape(50)
                                        ) {
                                            Text(
                                                when (myFollow?.status) {
                                                    "ACCEPTED" -> "Following"
                                                    "PENDING" -> "Requested"
                                                    else -> "Follow"
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showPrivateAccountFollowLockNotice) {
        AlertDialog(
            onDismissRequest = { showPrivateAccountFollowLockNotice = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Private Account", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "@${viewedProfile.username} has a Private Account. Only people who follow @${viewedProfile.username} can view their Followers and Following lists."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPrivateAccountFollowLockNotice = false
                        if (followRelation == null) {
                            onToggleFollow(viewedProfile)
                        }
                    }
                ) {
                    Text(if (followRelation == null) "Follow @${viewedProfile.username}" else "OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrivateAccountFollowLockNotice = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Multi-Account Switcher Bottom Sheet (Tap directly on any account card to switch immediately — no "Switch" button)
    if (showAccountSwitcherSheet) {
        ModalBottomSheet(onDismissRequest = { showAccountSwitcherSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Accounts",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Tap any account below to switch to it immediately.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn(
                    modifier = Modifier.height(210.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(savedAccounts, key = { it.uid }) { acc ->
                        val isActive = acc.uid == currentUserId
                        LuminaGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!isActive) {
                                        showAccountSwitcherSheet = false
                                        onSwitchOrAddAccount()
                                    }
                                }
                                .testTag("account_row_${acc.uid}"),
                            shape = RoundedCornerShape(18.dp),
                            borderBrush = if (isActive) LuminaPrismGradient else LuminaGlassBorderGradient
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    UserAvatarBadge(
                                        avatarUrl = acc.avatarUrl,
                                        displayName = acc.displayName,
                                        size = 44.dp,
                                        showBorder = isActive
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = acc.displayName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isActive) "@${acc.username}" else "@${acc.username} • Tap to use",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (isActive) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Active Account",
                                            tint = LuminaEmerald
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            "Active",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = LuminaEmerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    IconButton(onClick = { onRemoveSavedAccount(acc.uid) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove saved account")
                                    }
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                LuminaGradientButton(
                    text = "Add Another Account",
                    icon = Icons.Default.PersonAdd,
                    onClick = {
                        showAccountSwitcherSheet = false
                        onSwitchOrAddAccount()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_account_button")
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = {
                        showAccountSwitcherSheet = false
                        onSignOutCurrent()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Log Out Current")
                    }
                    TextButton(onClick = {
                        showAccountSwitcherSheet = false
                        onSignOutAll()
                    }) {
                        Text("Log Out All Accounts", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfilePostDetailDialog(
    post: Post,
    currentUserId: String,
    isSaved: Boolean,
    savedMusicIds: Set<String>,
    hideLikeCounts: Boolean,
    audioPlayer: com.example.ui.components.AudioClipPlayerController,
    onDismiss: () -> Unit,
    onLike: () -> Unit,
    onSave: () -> Unit,
    onOpenComments: () -> Unit,
    onShareToPerson: () -> Unit,
    onToggleSaveMusic: (MusicTrack) -> Unit,
    onDelete: (() -> Unit)?
) {
    val isLiked = post.likedByUids.contains(currentUserId)
    val ownerKey = remember(post.id) { "profile_post_${post.id}" }

    DisposableEffect(ownerKey) {
        onDispose {
            audioPlayer.stopForOwner(ownerKey)
        }
    }

    Dialog(
        onDismissRequest = {
            audioPlayer.stopForOwner(ownerKey)
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatarBadge(
                            avatarUrl = post.authorAvatarUrl,
                            displayName = post.authorDisplayName,
                            size = 38.dp,
                            showBorder = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            val titleText = if (post.collaboratorUsernames.isNotEmpty()) {
                                val collabs = post.collaboratorUsernames.joinToString(", ") { "@${it.removePrefix("@")}" }
                                "${post.authorDisplayName} and $collabs"
                            } else {
                                post.authorDisplayName
                            }
                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "@${post.authorUsername}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Row {
                        if (onDelete != null) {
                            IconButton(onClick = onDelete) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete Post",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        IconButton(onClick = {
                            audioPlayer.stopForOwner(ownerKey)
                            onDismiss()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Media
                val mediaUrl = post.mediaUrls.firstOrNull().orEmpty()
                if (mediaUrl.isNotBlank()) {
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = post.caption,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .background(Color.Black),
                        contentScale = ContentScale.Crop
                    )
                }

                // Attached Music Banner
                if (post.musicUrl.isNotBlank() && post.musicTitle.isNotBlank()) {
                    AttachedMusicPillBanner(
                        musicTrackId = post.musicTrackId,
                        musicTitle = post.musicTitle,
                        musicArtist = post.musicArtist,
                        musicCoverUrl = post.musicCoverUrl,
                        musicUrl = post.musicUrl,
                        musicStartMs = post.musicStartMs,
                        musicEndMs = post.musicEndMs,
                        isMusicSaved = savedMusicIds.contains(post.musicTrackId),
                        audioPlayer = audioPlayer,
                        onToggleSaveMusic = onToggleSaveMusic,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }

                //Caption
                if (post.caption.isNotBlank()) {
                    Text(
                        text = post.caption,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                }

                // Actions Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(onClick = onLike) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (isLiked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (!hideLikeCounts) {
                            Text("${post.likeCount}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = onOpenComments) {
                            Icon(Icons.Outlined.ChatBubbleOutline, contentDescription = "Comments")
                        }
                        Text("${post.commentCount}", style = MaterialTheme.typography.labelLarge)
                        IconButton(onClick = onShareToPerson) {
                            Icon(MitrIcons.PaperPlaneSend, contentDescription = "Share to Person")
                        }
                    }

                    IconButton(onClick = onSave) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileReelDetailDialog(
    reel: Reel,
    currentUserId: String,
    isSaved: Boolean,
    savedMusicIds: Set<String>,
    audioPlayer: com.example.ui.components.AudioClipPlayerController,
    onDismiss: () -> Unit,
    onLike: () -> Unit,
    onSave: () -> Unit,
    onOpenComments: () -> Unit = {},
    onOpenInReelsFeed: () -> Unit = {},
    onDelete: (() -> Unit)? = null,
    onToggleSaveMusic: (MusicTrack) -> Unit
) {
    val isLiked = reel.likedByUids.contains(currentUserId)
    val ownerKey = remember(reel.id) { "profile_reel_${reel.id}" }
    var isPaused by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }

    val syncedReelEndMs = remember(reel.musicStartMs, reel.musicEndMs, reel.durationSeconds) {
        if (reel.durationSeconds > 0L) {
            (reel.musicStartMs + reel.durationSeconds * 1000L).coerceAtLeast(reel.musicStartMs + 1000L)
        } else {
            reel.musicEndMs.coerceAtLeast(reel.musicStartMs + 15_000L)
        }
    }

    LaunchedEffect(reel.id, isPaused, isMuted, syncedReelEndMs) {
        if (reel.musicUrl.isNotBlank() && !isPaused && !isMuted) {
            audioPlayer.playSyncedForOwner(
                ownerKey = ownerKey,
                url = reel.musicUrl,
                startMs = reel.musicStartMs,
                endMs = syncedReelEndMs
            )
        } else {
            audioPlayer.stopForOwner(ownerKey)
        }
    }

    DisposableEffect(ownerKey) {
        onDispose {
            audioPlayer.stopForOwner(ownerKey)
        }
    }

    Dialog(
        onDismissRequest = {
            audioPlayer.stopForOwner(ownerKey)
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("profile_reel_detail_dialog_${reel.id}"),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val headerText = if (reel.collaboratorUsernames.isNotEmpty()) {
                        val collabs = reel.collaboratorUsernames.joinToString(", ") { "@${it.removePrefix("@")}" }
                        "@${reel.authorUsername} & $collabs's Reel"
                    } else {
                        "@${reel.authorUsername}'s Reel"
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = headerText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${reel.durationSeconds.coerceIn(15L, 60L)}s • ${reel.viewCount} views",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onDelete != null) {
                            IconButton(
                                onClick = onDelete,
                                modifier = Modifier.testTag("profile_reel_delete_button_${reel.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Reel",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        IconButton(onClick = {
                            audioPlayer.stopForOwner(ownerKey)
                            onDismiss()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                // Interactive Video & Audio Player Stage
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF101322),
                                    LuminaIndigo.copy(alpha = 0.85f),
                                    LuminaViolet.copy(alpha = 0.80f),
                                    LuminaCoral.copy(alpha = 0.75f)
                                )
                            )
                        )
                        .clickable { isPaused = !isPaused }
                        .testTag("profile_reel_video_stage_${reel.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    val coverOrVideo = reel.musicCoverUrl.ifBlank { reel.videoUrl }
                    if (coverOrVideo.isNotBlank()) {
                        AsyncImage(
                            model = coverOrVideo,
                            contentDescription = reel.caption,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    if (reel.videoUrl.startsWith("http") ||
                        reel.videoUrl.startsWith("content://") ||
                        reel.videoUrl.startsWith("file://")
                    ) {
                        val trimStartMs = reel.videoTrimStartMs.coerceAtLeast(0L).toInt()
                        AndroidView(
                            factory = { ctx ->
                                VideoView(ctx).apply {
                                    setVideoURI(Uri.parse(reel.videoUrl))
                                    setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        val vol = if (isMuted || reel.musicUrl.isNotBlank()) 0f else 1f
                                        mp.setVolume(vol, vol)
                                        if (trimStartMs > 0) {
                                            seekTo(trimStartMs)
                                        }
                                        if (!isPaused) start() else pause()
                                    }
                                    setOnErrorListener { _, _, _ -> true }
                                }
                            },
                            update = { view ->
                                if (isPaused && view.isPlaying) {
                                    view.pause()
                                } else if (!isPaused && !view.isPlaying) {
                                    if (trimStartMs > 0 && view.currentPosition < trimStartMs) {
                                        view.seekTo(trimStartMs)
                                    }
                                    view.start()
                                } else if (!isPaused && reel.videoTrimEndMs > reel.videoTrimStartMs + 1000L) {
                                    if (view.currentPosition > reel.videoTrimEndMs.toInt()) {
                                        view.seekTo(trimStartMs)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Subtle top/bottom gradient scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Black.copy(alpha = 0.35f),
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.68f)
                                    )
                                )
                            )
                    )

                    // Play/Pause indicator & Mute button
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.62f),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { isPaused = !isPaused }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                    contentDescription = if (isPaused) "Play" else "Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.62f),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { isMuted = !isMuted }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = if (isMuted) "Unmute" else "Mute",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Watch in Full Reels Feed button inside the player bottom-end
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable { onOpenInReelsFeed() }
                            .testTag("profile_reel_open_in_feed_button_${reel.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = "Watch in Full Reels Feed",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Watch in Reels Feed",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                    }
                }

                if (reel.musicUrl.isNotBlank() && reel.musicTitle.isNotBlank()) {
                    AttachedMusicPillBanner(
                        musicTrackId = reel.musicTrackId,
                        musicTitle = reel.musicTitle,
                        musicArtist = reel.musicArtist,
                        musicCoverUrl = reel.musicCoverUrl,
                        musicUrl = reel.musicUrl,
                        musicStartMs = reel.musicStartMs,
                        musicEndMs = reel.musicEndMs,
                        isMusicSaved = savedMusicIds.contains(reel.musicTrackId),
                        audioPlayer = audioPlayer,
                        onToggleSaveMusic = onToggleSaveMusic,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    )
                }

                if (reel.caption.isNotBlank()) {
                    Text(
                        text = reel.caption,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(onClick = onLike) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like Reel",
                                tint = if (isLiked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text("${reel.likeCount}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

                        IconButton(
                            onClick = onOpenComments,
                            modifier = Modifier.testTag("profile_reel_comments_button_${reel.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Reel Comments"
                            )
                        }
                        Text("${reel.commentCount}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onSave,
                        modifier = Modifier.testTag("profile_reel_save_button_${reel.id}")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = if (isSaved) "Saved Reel" else "Save Reel",
                            tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileReelGridTile(
    reel: Reel,
    testTag: String = "",
    onClick: () -> Unit,
    onOpenInReelsFeed: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF141829),
                        LuminaIndigo.copy(alpha = 0.85f),
                        LuminaViolet.copy(alpha = 0.85f),
                        LuminaCoral.copy(alpha = 0.78f)
                    )
                )
            )
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        val previewModel = reel.musicCoverUrl.ifBlank { reel.videoUrl }
        if (previewModel.isNotBlank()) {
            AsyncImage(
                model = previewModel,
                contentDescription = reel.caption.ifBlank { "Reel by @${reel.authorUsername}" },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Top & bottom gradient scrim so badges and text are always crisp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.50f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.78f)
                        )
                    )
                )
        )

        // Top-Left Duration Badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.68f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Videocam,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = "${reel.durationSeconds.coerceIn(15L, 60L)}s",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        // Top-Right Music / Collab Badge
        if (reel.musicUrl.isNotBlank() || reel.collaboratorUsernames.isNotEmpty()) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (reel.musicUrl.isNotBlank()) Icons.Default.MusicNote else Icons.Default.Group,
                        contentDescription = "Reel Audio or Collab",
                        tint = if (reel.musicUrl.isNotBlank()) LuminaCoral else Color.White,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }

        // Center Play Button Icon
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.55f),
            modifier = Modifier
                .align(Alignment.Center)
                .size(40.dp)
                .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Reel",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Bottom Metadata Bar (Caption + Views + Likes)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(7.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (reel.caption.isNotBlank() || reel.musicTitle.isNotBlank()) {
                Text(
                    text = reel.caption.ifBlank { reel.musicTitle },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircleFilled,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${reel.viewCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.clickable { onOpenInReelsFeed() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = LuminaCoral,
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "${reel.likeCount}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun StoryArchiveTileCard(
    story: Story,
    onOpenStory: () -> Unit,
    onToggleHighlight: () -> Unit
) {
    val isExpired = story.isStoryExpired()
    val reactionCount = story.parsedReactions().size
    Box(
        modifier = Modifier
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                width = if (story.isHighlighted) 1.5.dp else 1.dp,
                color = if (story.isHighlighted) LuminaAmber else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onOpenStory)
            .testTag("story_archive_tile_${story.id}")
    ) {
        if (story.mediaUrl.isNotBlank()) {
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = story.caption.ifBlank { "Archived Story" },
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                LuminaIndigo.copy(alpha = 0.85f),
                                LuminaViolet.copy(alpha = 0.85f),
                                LuminaCoral.copy(alpha = 0.80f)
                            )
                        )
                    )
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = story.overlayText.ifBlank { story.caption.ifBlank { "Story" } },
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Top & bottom gradient scrim for legible badges
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.72f)
                        )
                    )
                )
        )

        // Top-Left Date Badge (e.g. "31 Oct")
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.Black.copy(alpha = 0.68f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
        ) {
            Text(
                text = story.archiveDayBadge(),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        // Top-Right Highlight Toggle Button
        Surface(
            shape = CircleShape,
            color = if (story.isHighlighted) LuminaAmber else Color.Black.copy(alpha = 0.58f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(26.dp)
                .clickable(onClick = onToggleHighlight)
                .testTag("story_archive_highlight_toggle_${story.id}")
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = if (story.isHighlighted) "Remove from Highlights" else "Add to Highlights",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Bottom Metadata Bar (Status + Views + Reactions)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(7.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (story.caption.isNotBlank() || story.overlayText.isNotBlank()) {
                Text(
                    text = story.caption.ifBlank { story.overlayText },
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(11.dp)
                    )
                    Text(
                        text = "${story.viewerIds.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    if (reactionCount > 0) {
                        Text(
                            text = "• 😍$reactionCount",
                            style = MaterialTheme.typography.labelSmall,
                            color = LuminaAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = if (isExpired) "Past" else "24h",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isExpired) Color.White.copy(alpha = 0.78f) else LuminaEmerald,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun ProfileStatColumn(
    count: Long,
    label: String,
    testTag: String = "",
    onClick: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ProfileGridTile(
    imageUrl: String,
    caption: String,
    hasMusic: Boolean = false,
    isCollab: Boolean = false,
    testTag: String = "",
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .then(if (testTag.isNotBlank()) Modifier.testTag(testTag) else Modifier)
    ) {
        if (imageUrl.isNotBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = caption,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            listOf(
                                LuminaCoral.copy(alpha = 0.16f),
                                LuminaViolet.copy(alpha = 0.16f)
                            )
                        )
                    )
                    .padding(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        if (isCollab) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.72f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = "Collab",
                        tint = Color.White,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Collab",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (hasMusic) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.65f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.MusicNote,
                        contentDescription = "Has Music",
                        tint = LuminaCoral,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }
        }
    }
}
