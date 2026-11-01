package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Share
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
import com.example.data.model.isFounderVipAccount
import com.example.data.model.isUserBlueTickVerified
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
    onRecordStoryView: (Story) -> Unit = {},
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
    val isOwner = viewedProfile.userId == currentUserId
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

    LaunchedEffect(selectedTab, viewedProfile.userId) {
        audioPlayer.pause()
    }

    // Followers / Following sheet state ("FOLLOWERS" or "FOLLOWING")
    var followersFollowingSheetMode by remember { mutableStateOf<String?>(null) }
    var showPrivateAccountFollowLockNotice by remember { mutableStateOf(false) }

    val profileFollowers by observeFollowersForUser(viewedProfile.userId)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val profileFollowing by observeFollowingForUser(viewedProfile.userId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    // Upload viewer dialog states when tapping any upload tile in Profile
    var openedPost by remember { mutableStateOf<Post?>(null) }
    var openedReel by remember { mutableStateOf<Reel?>(null) }
    var commentTargetPost by remember { mutableStateOf<Post?>(null) }
    var shareTargetPost by remember { mutableStateOf<Post?>(null) }

    // Story & Highlight states on Profile
    var openedProfileStories by remember { mutableStateOf<List<Story>?>(null) }
    var showAddHighlightPicker by remember { mutableStateOf(false) }

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

    val userPosts = remember(posts, viewedProfile.userId, viewedProfile.username) {
        posts.filter {
            it.authorId == viewedProfile.userId ||
                (viewedProfile.username.isNotBlank() && it.authorUsername.equals(viewedProfile.username, ignoreCase = true)) ||
                it.collaboratorUsernames.any { c ->
                    c.removePrefix("@").equals(viewedProfile.username, ignoreCase = true)
                }
        }
    }
    val userReels = remember(reels, viewedProfile.userId, viewedProfile.username) {
        reels.filter {
            it.authorId == viewedProfile.userId ||
                (viewedProfile.username.isNotBlank() && it.authorUsername.equals(viewedProfile.username, ignoreCase = true)) ||
                it.collaboratorUsernames.any { c ->
                    c.removePrefix("@").equals(viewedProfile.username, ignoreCase = true)
                }
        }
    }
    val taggedPosts = remember(posts, viewedProfile.username) {
        posts.filter {
            it.taggedUsernames.any { tag -> tag.removePrefix("@").equals(viewedProfile.username, ignoreCase = true) } ||
                it.collaboratorUsernames.any { c -> c.removePrefix("@").equals(viewedProfile.username, ignoreCase = true) }
        }
    }

    val profileAllStories = remember(userStories, viewedProfile.userId) {
        userStories.filter { it.authorId == viewedProfile.userId }
    }
    val activeUserStories = remember(profileAllStories) {
        val now = System.currentTimeMillis()
        profileAllStories.filter { it.expiresAtMillis > now }
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

            // Profile Header Card matching Screenshot 2
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp)
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
                                testTag = "profile_stat_posts"
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

                if (viewedProfile.bio.isNotBlank()) {
                    Text(
                        text = viewedProfile.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 6.dp)
                    )
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

                // Story Highlights Row (Matches Screenshot 2 circular highlights)
                if (highlightedStories.isNotEmpty() || (isOwner && profileAllStories.isNotEmpty())) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .testTag("profile_highlights_row"),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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

            // Segmented Pill Tab Bar matching Screenshot 2 (Grid | Reels | Saved | Tagged)
            val tabs = if (isOwner) {
                listOf("Posts", "Reels", "Saved", "Tagged")
            } else {
                listOf("Posts", "Reels", "Tagged")
            }

            val safeTabIndex = selectedTab.coerceIn(0, tabs.lastIndex)
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = safeTabIndex == index
                        val tabIcon = when (title) {
                            "Posts" -> Icons.Default.GridOn
                            "Reels" -> Icons.Default.PlayCircleFilled
                            "Saved" -> Icons.Default.BookmarkBorder
                            else -> Icons.Default.AccountBox
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                            shadowElevation = if (isSelected) 2.dp else 0.dp,
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { selectedTab = index }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = tabIcon,
                                    contentDescription = title,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Tab Content Grid
            val activeTabTitle = tabs.getOrElse(safeTabIndex) { "Posts" }
            when (activeTabTitle) {
                "Posts" -> {
                    if (userPosts.isEmpty()) {
                        EmptyOrErrorCard(
                            title = "No posts yet",
                            subtitle = "Published photo and carousel posts will appear here."
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(userPosts, key = { it.id }) { post ->
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
                    }
                }

                "Reels" -> {
                    if (userReels.isEmpty()) {
                        EmptyOrErrorCard(
                            title = "No reels yet",
                            subtitle = "Published vertical short videos will appear here."
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(userReels, key = { it.id }) { reel ->
                                ProfileGridTile(
                                    imageUrl = reel.videoUrl,
                                    caption = "${reel.viewCount} views",
                                    hasMusic = reel.musicUrl.isNotBlank(),
                                    isCollab = reel.collaboratorUsernames.isNotEmpty(),
                                    testTag = "profile_reel_tile_${reel.id}",
                                    onClick = { openedReel = reel }
                                )
                            }
                        }
                    }
                }

                "Saved" -> {
                    // Unified Saved Tab: Shows both Saved Songs AND Saved Posts/Reels together!
                    if (savedMusicList.isEmpty() && savedPosts.isEmpty()) {
                        EmptyOrErrorCard(
                            title = "Nothing saved yet",
                            subtitle = "Saved songs, posts, and reels are all kept together right here."
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("profile_unified_saved_grid"),
                            contentPadding = PaddingValues(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (savedMusicList.isNotEmpty()) {
                                item(span = { GridItemSpan(3) }) {
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
                                item(span = { GridItemSpan(3) }) {
                                    Text(
                                        text = "Saved Posts & Reels (${savedPosts.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                                    )
                                }
                                items(savedPosts, key = { "saved_${it.id}" }) { saved ->
                                    ProfileGridTile(
                                        imageUrl = saved.mediaUrl,
                                        caption = "@${saved.authorUsername}: ${saved.captionPreview}",
                                        testTag = "profile_saved_tile_${saved.id}",
                                        onClick = {
                                            val matchingPost = posts.find { it.id == saved.postId }
                                            val matchingReel = reels.find { it.id == saved.postId }
                                            when {
                                                matchingPost != null -> openedPost = matchingPost
                                                matchingReel != null -> openedReel = matchingReel
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
                }

                "Tagged" -> {
                    if (taggedPosts.isEmpty()) {
                        EmptyOrErrorCard(
                            title = "No tagged posts",
                            subtitle = "Posts where @${viewedProfile.username} is tagged or collaborating appear here."
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(taggedPosts, key = { it.id }) { post ->
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

    // Full Reel Upload Viewer Dialog when user taps any Reel tile in Profile
    val currentOpenedReel = openedReel?.let { target -> reels.find { it.id == target.id } ?: target }
    if (currentOpenedReel != null) {
        ProfileReelDetailDialog(
            reel = currentOpenedReel,
            currentUserId = currentUserId,
            savedMusicIds = remember(savedMusicList) { savedMusicList.map { it.trackId }.toSet() },
            audioPlayer = audioPlayer,
            onDismiss = {
                audioPlayer.pause()
                openedReel = null
            },
            onLike = { onLikeReel(currentOpenedReel) },
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

    // Story Viewer Dialog opened from Profile Avatar or Story Highlights Row
    if (openedProfileStories != null && openedProfileStories!!.isNotEmpty()) {
        FullScreenStoryViewerDialog(
            stories = openedProfileStories!!,
            initialIndex = 0,
            currentUserId = currentUserId,
            savedMusicList = savedMusicList,
            discoverableUsers = discoverableUsers,
            onToggleSaveMusic = onToggleSaveMusic,
            onToggleHighlightStory = onToggleHighlightStory,
            onDismiss = {
                audioPlayer.pause()
                openedProfileStories = null
            },
            onRecordView = onRecordStoryView,
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
    savedMusicIds: Set<String>,
    audioPlayer: com.example.ui.components.AudioClipPlayerController,
    onDismiss: () -> Unit,
    onLike: () -> Unit,
    onToggleSaveMusic: (MusicTrack) -> Unit
) {
    val isLiked = reel.likedByUids.contains(currentUserId)
    val ownerKey = remember(reel.id) { "profile_reel_${reel.id}" }

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
            Column(modifier = Modifier.fillMaxWidth()) {
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
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = {
                        audioPlayer.stopForOwner(ownerKey)
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                if (reel.videoUrl.isNotBlank()) {
                    AsyncImage(
                        model = reel.videoUrl,
                        contentDescription = reel.caption,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .background(Color.Black),
                        contentScale = ContentScale.Crop
                    )
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
                        modifier = Modifier.padding(14.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = reel.caption,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onLike) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like Reel",
                                tint = if (isLiked) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text("${reel.likeCount}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                }
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
