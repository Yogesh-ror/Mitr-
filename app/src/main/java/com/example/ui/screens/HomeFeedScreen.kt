package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwipeUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.Comment
import com.example.data.model.FirestoreFeedSearchResult
import com.example.data.model.Follow
import com.example.data.model.MusicTrack
import com.example.data.model.Post
import com.example.data.model.SavedMusic
import com.example.data.model.SavedPost
import com.example.data.model.Story
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.isCreatorHandleBlueTickVerified
import com.example.data.model.isUserBlueTickVerified
import com.example.ui.components.AttachedMusicPillBanner
import com.example.ui.components.AuroraAmbientBackground
import com.example.ui.components.LuminaGlassCard
import com.example.ui.components.LuminaGradientButton
import com.example.ui.components.LuminaPillChip
import com.example.ui.components.LuminaPrismArtwork
import com.example.ui.components.MitrFeedAdMobCard
import com.example.ui.components.MitrIcons
import com.example.ui.components.MitrTopHeaderBar
import com.example.ui.components.VerifiedBadgeIcon
import com.example.ui.components.rememberAudioClipPlayer
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaCoral
import com.example.ui.theme.LuminaEmerald
import com.example.ui.theme.LuminaPrismGradient
import com.example.ui.theme.LuminaViolet
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFeedScreen(
    currentUserId: String,
    currentUserProfile: UserProfile,
    feedPostsState: UiState<List<Post>>,
    activeStories: List<Story>,
    savedPosts: List<SavedPost>,
    savedMusicList: List<SavedMusic> = emptyList(),
    discoverableUsers: List<UserProfile> = emptyList(),
    outgoingFollows: List<Follow> = emptyList(),
    notificationBadgeCount: Int = 0,
    firestoreSearchResult: FirestoreFeedSearchResult = FirestoreFeedSearchResult(),
    onExecuteFirestoreSearch: (String) -> Unit = {},
    onToggleFollowUser: (UserProfile) -> Unit = {},
    onMessageUser: (UserProfile) -> Unit = {},
    hideLikeCounts: Boolean,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onOpenCreatePost: () -> Unit,
    onOpenCreateStory: () -> Unit,
    onLikePost: (Post) -> Unit,
    onSavePost: (Post) -> Unit,
    onToggleSaveMusic: (MusicTrack) -> Unit = {},
    onSharePostToUser: (UserProfile, Post) -> Unit = { _, _ -> },
    onToggleHighlightStory: (Story, String) -> Unit = { _, _ -> },
    onUpdatePost: (String, String, Boolean) -> Unit,
    onUpdatePostCollaborators: (Post, List<String>, List<String>) -> Unit = { _, _, _ -> },
    onLeaveCollaboration: (String, String) -> Unit = { _, _ -> },
    onDeletePost: (String) -> Unit,
    onReportContent: (String, String, String) -> Unit,
    onMuteUserStories: (String, String, String) -> Unit,
    onOpenUserProfile: (String) -> Unit,
    onOpenSearch: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenMessages: () -> Unit = {},
    observeComments: (String, String) -> Flow<List<Comment>>,
    onAddComment: (String, String, String, String) -> Unit,
    onRecordStoryView: (Story) -> Unit,
    onDeleteStory: (String) -> Unit
) {
    var selectedStoryIndex by remember { mutableStateOf<Int?>(null) }
    var activeCommentPost by remember { mutableStateOf<Post?>(null) }
    var sharingPost by remember { mutableStateOf<Post?>(null) }
    var editingPost by remember { mutableStateOf<Post?>(null) }
    var coAuthorsSheetPost by remember { mutableStateOf<Post?>(null) }
    var reportingTarget by remember { mutableStateOf<Pair<String, String>?>(null) }
    var feedSearchQuery by remember { mutableStateOf("") }
    var feedSearchCategory by remember { mutableStateOf("ALL") } // ALL, USERS, HASHTAGS, POSTS
    val audioPlayer = rememberAudioClipPlayer()
    val feedListState = rememberLazyListState()

    // Determine which Post item in the LazyColumn is currently centered in the viewport
    val focusedPostId by remember {
        derivedStateOf {
            val layoutInfo = feedListState.layoutInfo
            val visibleItems = layoutInfo.visibleItemsInfo
            if (visibleItems.isEmpty()) {
                null
            } else {
                val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
                visibleItems
                    .filter { item ->
                        val k = item.key as? String ?: ""
                        k.isNotBlank() &&
                            !k.startsWith("search_") &&
                            !k.startsWith("tag_") &&
                            !k.startsWith("admob_")
                    }
                    .minByOrNull { item ->
                        val itemCenter = item.offset + item.size / 2
                        abs(itemCenter - viewportCenter)
                    }
                    ?.key as? String
            }
        }
    }

    // Immediately stop any playing post song the instant the user starts scrolling or opens a story
    LaunchedEffect(feedListState.isScrollInProgress, focusedPostId, selectedStoryIndex) {
        if (feedListState.isScrollInProgress || focusedPostId == null || selectedStoryIndex != null) {
            audioPlayer.pause()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioPlayer.release()
        }
    }

    val allFeedPosts = (feedPostsState as? UiState.Success)?.data ?: emptyList()

    // Discover popular hashtags from Firestore posts
    val popularHashtags = remember(allFeedPosts) {
        val counts = mutableMapOf<String, Int>()
        allFeedPosts.forEach { post ->
            post.hashtags.forEach { tag ->
                val clean = tag.trim().lowercase(Locale.ROOT)
                if (clean.isNotBlank()) {
                    counts[clean] = (counts[clean] ?: 0) + 1
                }
            }
        }
        counts.entries.sortedByDescending { it.value }.take(12).map { it.key to it.value }
    }

    val cleanToken = remember(feedSearchQuery) {
        feedSearchQuery.trim().removePrefix("#").removePrefix("@").lowercase(Locale.ROOT)
    }

    val matchedUsers = remember(discoverableUsers, firestoreSearchResult, allFeedPosts, cleanToken) {
        if (cleanToken.isBlank()) {
            emptyList()
        } else {
            val byId = LinkedHashMap<String, UserProfile>()
            firestoreSearchResult.matchedUsers.forEach { user ->
                val uid = user.userId.ifBlank { user.id }
                if (uid.isNotBlank() && (
                        user.username.lowercase(Locale.ROOT).contains(cleanToken) ||
                            user.displayName.lowercase(Locale.ROOT).contains(cleanToken) ||
                            user.bio.lowercase(Locale.ROOT).contains(cleanToken) ||
                            uid.lowercase(Locale.ROOT) == cleanToken
                        )
                ) {
                    byId[uid] = user.copy(id = uid, userId = uid)
                }
            }
            discoverableUsers.forEach { user ->
                val uid = user.userId.ifBlank { user.id }
                if (uid.isNotBlank() && !byId.containsKey(uid) && (
                        user.username.lowercase(Locale.ROOT).contains(cleanToken) ||
                            user.displayName.lowercase(Locale.ROOT).contains(cleanToken) ||
                            user.bio.lowercase(Locale.ROOT).contains(cleanToken) ||
                            uid.lowercase(Locale.ROOT) == cleanToken
                        )
                ) {
                    byId[uid] = user.copy(id = uid, userId = uid)
                }
            }
            allFeedPosts.forEach { post ->
                val uid = post.authorId
                if (uid.isNotBlank() && !byId.containsKey(uid) && (
                        post.authorUsername.lowercase(Locale.ROOT).contains(cleanToken) ||
                            post.authorDisplayName.lowercase(Locale.ROOT).contains(cleanToken)
                        )
                ) {
                    val uname = post.authorUsername.ifBlank { "user_${uid.take(6)}" }
                    byId[uid] = UserProfile(
                        id = uid,
                        userId = uid,
                        username = uname,
                        displayName = post.authorDisplayName.ifBlank { uname },
                        avatarUrl = post.authorAvatarUrl,
                        bio = "Creator on Mitr"
                    )
                }
            }
            byId.values.toList()
        }
    }

    val matchedHashtags = remember(allFeedPosts, firestoreSearchResult, cleanToken) {
        if (cleanToken.isBlank()) emptyList()
        else if (firestoreSearchResult.query.equals(feedSearchQuery.trim(), ignoreCase = true) &&
            firestoreSearchResult.matchedHashtags.isNotEmpty()
        ) {
            firestoreSearchResult.matchedHashtags
        } else {
            val counts = mutableMapOf<String, Int>()
            allFeedPosts.forEach { post ->
                post.hashtags.forEach { tag ->
                    val lower = tag.lowercase(Locale.ROOT)
                    if (lower.contains(cleanToken)) {
                        counts[lower] = (counts[lower] ?: 0) + 1
                    }
                }
            }
            counts.entries.sortedByDescending { it.value }.map { it.key to it.value }
        }
    }

    val matchedPosts = remember(allFeedPosts, firestoreSearchResult, feedSearchQuery, cleanToken) {
        if (cleanToken.isBlank()) allFeedPosts
        else if (firestoreSearchResult.query.equals(feedSearchQuery.trim(), ignoreCase = true) &&
            firestoreSearchResult.matchedPosts.isNotEmpty()
        ) {
            firestoreSearchResult.matchedPosts
        } else {
            val isHashtagOnly = feedSearchQuery.trim().startsWith("#")
            if (isHashtagOnly) {
                allFeedPosts.filter { post ->
                    post.hashtags.any { it.lowercase(Locale.ROOT).contains(cleanToken) } ||
                        post.caption.lowercase(Locale.ROOT).contains("#$cleanToken")
                }
            } else {
                allFeedPosts.filter { post ->
                    post.caption.lowercase(Locale.ROOT).contains(cleanToken) ||
                        post.authorUsername.lowercase(Locale.ROOT).contains(cleanToken) ||
                        post.authorDisplayName.lowercase(Locale.ROOT).contains(cleanToken) ||
                        post.locationTag.lowercase(Locale.ROOT).contains(cleanToken) ||
                        post.hashtags.any { it.lowercase(Locale.ROOT).contains(cleanToken) }
                }
            }
        }
    }

    AuroraAmbientBackground(intensity = 0.05f) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header matching Screenshot 1: [Orb Logo] Mitr | Home Feed + Search, Bell, Chat+, Profile Avatar
            MitrTopHeaderBar(
                sectionTitle = "Home Feed",
                notificationBadgeCount = notificationBadgeCount,
                onSearchClick = onOpenSearch,
                onNotificationsClick = onOpenNotifications,
                onQuickMessageClick = onOpenMessages,
                avatarContent = {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .clickable { onOpenUserProfile(currentUserId) }
                            .testTag("home_top_profile_avatar"),
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

            // Search Bar at the Top of the Feed (Firestore Queries for Users, #Hashtags & Post Content)
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    OutlinedTextField(
                        value = feedSearchQuery,
                        onValueChange = { newQuery ->
                            feedSearchQuery = newQuery
                            onExecuteFirestoreSearch(newQuery)
                        },
                        placeholder = {
                            Text(
                                text = "Search @users, #hashtags, or post content...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Feed",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (feedSearchQuery.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        feedSearchQuery = ""
                                        onExecuteFirestoreSearch("")
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Search"
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("feed_top_search_bar")
                    )

                    if (feedSearchQuery.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(listOf("ALL", "USERS", "HASHTAGS", "POSTS")) { cat ->
                                LuminaPillChip(
                                    label = when (cat) {
                                        "USERS" -> "Users (${matchedUsers.size})"
                                        "HASHTAGS" -> "Hashtags (${matchedHashtags.size})"
                                        "POSTS" -> "Posts (${matchedPosts.size})"
                                        else -> "All Results"
                                    },
                                    selected = feedSearchCategory == cat,
                                    onClick = { feedSearchCategory = cat },
                                    modifier = Modifier.testTag("feed_search_tab_${cat.lowercase(Locale.ROOT)}")
                                )
                            }
                        }
                    } else if (popularHashtags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("feed_trending_hashtags_row")
                        ) {
                            items(popularHashtags, key = { it.first }) { (tag, _) ->
                                LuminaPillChip(
                                    label = "#$tag",
                                    selected = false,
                                    onClick = {
                                        val tagQuery = "#$tag"
                                        feedSearchQuery = tagQuery
                                        onExecuteFirestoreSearch(tagQuery)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            LazyColumn(
                state = feedListState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("home_feed_list"),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Classic Circular Stories Bar with Clear Profile Picture
                if (feedSearchQuery.isBlank()) {
                    item(key = "stories_header_row") {
                        StoriesBar(
                            currentUserProfile = currentUserProfile,
                            stories = activeStories,
                            onAddStoryClick = onOpenCreateStory,
                            onStoryClick = { index ->
                                audioPlayer.pause()
                                selectedStoryIndex = index
                            }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                    }
                }

                // Active Firestore Search Results (Users + Hashtags) at Top of Feed
                if (feedSearchQuery.isNotBlank()) {
                    if (firestoreSearchResult.isSearching) {
                        item(key = "search_loading_bar") {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // 1. Matching Users from Firestore
                    if ((feedSearchCategory == "ALL" || feedSearchCategory == "USERS") && matchedUsers.isNotEmpty()) {
                        item(key = "search_users_header") {
                            Text(
                                text = "Matching Users (${matchedUsers.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        items(matchedUsers, key = { "search_user_${it.userId}" }) { user ->
                            val followRecord = outgoingFollows.find { it.followingId == user.userId }
                            val isFollowing = followRecord?.status == "ACCEPTED"
                            LuminaGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                                    .clickable { onOpenUserProfile(user.userId) }
                                    .testTag("feed_search_user_${user.userId}"),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        UserAvatarBadge(
                                            avatarUrl = user.avatarUrl,
                                            displayName = user.displayName,
                                            size = 44.dp,
                                            showBorder = true
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = user.displayName,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                VerifiedBadgeIcon(size = 14.dp)
                                            }
                                            Text(
                                                text = "@${user.username}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (user.userId != currentUserId) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            IconButton(
                                                onClick = { onMessageUser(user) },
                                                modifier = Modifier
                                                    .size(34.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                            ) {
                                                Icon(
                                                    imageVector = MitrIcons.MessageOutlined,
                                                    contentDescription = "Message @${user.username}",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            LuminaPillChip(
                                                label = when (followRecord?.status) {
                                                    "ACCEPTED" -> "Following"
                                                    "PENDING" -> "Requested"
                                                    else -> "Follow"
                                                },
                                                selected = !isFollowing,
                                                onClick = { onToggleFollowUser(user) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Matching Hashtags from Firestore
                    if ((feedSearchCategory == "ALL" || feedSearchCategory == "HASHTAGS") && matchedHashtags.isNotEmpty()) {
                        item(key = "search_hashtags_section") {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .testTag("feed_search_hashtags_section")
                            ) {
                                Text(
                                    text = "Matching Hashtags (${matchedHashtags.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(matchedHashtags, key = { "tag_${it.first}" }) { (tag, count) ->
                                        LuminaPillChip(
                                            label = "#$tag ($count posts)",
                                            selected = feedSearchQuery.equals("#$tag", ignoreCase = true),
                                            onClick = {
                                                val tagQuery = "#$tag"
                                                feedSearchQuery = tagQuery
                                                onExecuteFirestoreSearch(tagQuery)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                when (feedPostsState) {
                    is UiState.Loading -> {
                        items(3, key = { "search_skeleton_$it" }) {
                            PostSkeletonCard()
                        }
                    }

                    is UiState.Error -> {
                        item(key = "search_error_card") {
                            EmptyOrErrorCard(
                                title = "Unable to sync feed",
                                subtitle = feedPostsState.message,
                                actionLabel = "Retry Connection",
                                onAction = onRefresh
                            )
                        }
                    }

                    is UiState.Success -> {
                        val postsToShow = if (feedSearchQuery.isBlank()) {
                            val followedUsernames = outgoingFollows
                                .filter { it.status == "ACCEPTED" }
                                .mapNotNull { follow ->
                                    discoverableUsers.find { it.userId == follow.followingId }?.username?.lowercase()
                                }
                                .toSet()
                            val myUsernameLower = currentUserProfile.username.lowercase()
                            feedPostsState.data.sortedWith(
                                compareByDescending<Post> { post ->
                                    post.collaboratorUsernames.any { collab ->
                                        val clean = collab.removePrefix("@").lowercase()
                                        clean == myUsernameLower || followedUsernames.contains(clean)
                                    }
                                }.thenByDescending { it.createdAt?.toDate()?.time ?: 0L }
                            )
                        } else if (feedSearchCategory == "USERS" || feedSearchCategory == "HASHTAGS") {
                            emptyList()
                        } else {
                            matchedPosts
                        }

                        if (feedSearchQuery.isBlank() && postsToShow.isEmpty()) {
                            item(key = "search_empty_welcome") {
                                EmptyOrErrorCard(
                                    title = "Welcome to Mitr",
                                    subtitle = "Mitr never displays fake or sample posts. Share your first photo, carousel, or 24-hour story to start the community feed.",
                                    actionLabel = "Create First Post",
                                    onAction = onOpenCreatePost
                                )
                            }
                            item(key = "admob_empty_feed_slot") {
                                MitrFeedAdMobCard(slotIndex = 0)
                            }
                        } else if (feedSearchQuery.isNotBlank() &&
                            matchedUsers.isEmpty() &&
                            matchedHashtags.isEmpty() &&
                            matchedPosts.isEmpty()
                        ) {
                            item(key = "search_no_matches") {
                                EmptyOrErrorCard(
                                    title = "No matches for \"$feedSearchQuery\"",
                                    subtitle = "Try searching another @username, #hashtag, or post keyword.",
                                    actionLabel = "Clear Search",
                                    onAction = {
                                        feedSearchQuery = ""
                                        onExecuteFirestoreSearch("")
                                    }
                                )
                            }
                        } else {
                            if (feedSearchQuery.isNotBlank() && postsToShow.isNotEmpty()) {
                                item(key = "search_matching_posts_header") {
                                    Text(
                                        text = "Matching Posts (${postsToShow.size})",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                            itemsIndexed(postsToShow, key = { _, post -> post.id }) { index, post ->
                                val isSaved = savedPosts.any { it.postId == post.id }
                                val isMusicSaved = savedMusicList.any {
                                    (post.musicTrackId.isNotBlank() && it.trackId == post.musicTrackId) ||
                                        (post.musicTitle.isNotBlank() && it.title.equals(post.musicTitle, ignoreCase = true))
                                }
                                val isFocusedAndStationary = focusedPostId == post.id &&
                                    !feedListState.isScrollInProgress &&
                                    selectedStoryIndex == null

                                PostItemCard(
                                    post = post,
                                    currentUserId = currentUserId,
                                    currentUsername = currentUserProfile.username,
                                    discoverableUsers = discoverableUsers,
                                    isFocusedAndStationary = isFocusedAndStationary,
                                    isSaved = isSaved,
                                    isMusicSaved = isMusicSaved,
                                    audioPlayer = audioPlayer,
                                    hideLikeCounts = hideLikeCounts,
                                    onLikeClick = { onLikePost(post) },
                                    onCommentClick = { activeCommentPost = post },
                                    onSaveClick = { onSavePost(post) },
                                    onToggleSaveMusic = onToggleSaveMusic,
                                    onShareClick = {
                                        sharingPost = post
                                    },
                                    onAuthorClick = {
                                        if (post.collaboratorUsernames.isNotEmpty()) {
                                            coAuthorsSheetPost = post
                                        } else {
                                            onOpenUserProfile(post.authorId)
                                        }
                                    },
                                    onOpenCoAuthorsSheet = {
                                        coAuthorsSheetPost = post
                                    },
                                    onOpenCollaboratorProfile = { collabProfile ->
                                        if (collabProfile.userId.isNotBlank() && !collabProfile.userId.startsWith("collab_")) {
                                            onOpenUserProfile(collabProfile.userId)
                                        } else {
                                            coAuthorsSheetPost = post
                                        }
                                    },
                                    onLeaveCollaboration = {
                                        onLeaveCollaboration(post.id, "POST")
                                    },
                                    onHashtagClick = { clickedTag ->
                                        val tagQuery = "#$clickedTag"
                                        feedSearchQuery = tagQuery
                                        onExecuteFirestoreSearch(tagQuery)
                                    },
                                    onEditPost = { editingPost = post },
                                    onDeletePost = { onDeletePost(post.id) },
                                    onReportPost = { reportingTarget = post.id to "POST" }
                                )

                                // Show AdMob Sponsored Feed Card between posts (after the 1st post or every 5 posts)
                                val shouldShowAdHere = feedSearchQuery.isBlank() &&
                                    ((index == 0 && postsToShow.size < 5) || ((index + 1) % 5 == 0))
                                if (shouldShowAdHere) {
                                    MitrFeedAdMobCard(slotIndex = index + 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Instagram-style Co-Authors & Collaborators Profile Chooser Bottom Sheet
    if (coAuthorsSheetPost != null) {
        val post = coAuthorsSheetPost!!
        val acceptedFollowingIds = remember(outgoingFollows) {
            outgoingFollows.filter { it.status == "ACCEPTED" }.map { it.followingId }
        }
        InstagramCoAuthorsBottomSheet(
            authorId = post.authorId,
            authorUsername = post.authorUsername,
            authorDisplayName = post.authorDisplayName,
            authorAvatarUrl = post.authorAvatarUrl,
            collaboratorUsernames = post.collaboratorUsernames,
            taggedUsernames = post.taggedUsernames,
            discoverableUsers = discoverableUsers,
            currentUserId = currentUserId,
            currentUsername = currentUserProfile.username,
            followingIds = acceptedFollowingIds,
            onToggleFollow = onToggleFollowUser,
            onSelectProfile = { profile ->
                coAuthorsSheetPost = null
                onOpenUserProfile(profile.userId)
            },
            onLeaveCollaboration = {
                coAuthorsSheetPost = null
                onLeaveCollaboration(post.id, "POST")
            },
            onDismiss = { coAuthorsSheetPost = null }
        )
    }

    // Share Post to Another Person Bottom Sheet
    if (sharingPost != null) {
        SharePostToPersonBottomSheet(
            post = sharingPost!!,
            currentUserId = currentUserId,
            discoverableUsers = discoverableUsers,
            onShareToUser = { recipient, postToShare ->
                onSharePostToUser(recipient, postToShare)
                sharingPost = null
            },
            onDismiss = { sharingPost = null }
        )
    }

    // Full-Screen Story Viewer
    if (selectedStoryIndex != null && activeStories.isNotEmpty()) {
        val safeIndex = selectedStoryIndex!!.coerceIn(0, activeStories.lastIndex)
        FullScreenStoryViewerDialog(
            stories = activeStories,
            initialIndex = safeIndex,
            currentUserId = currentUserId,
            savedMusicList = savedMusicList,
            discoverableUsers = discoverableUsers,
            onToggleSaveMusic = onToggleSaveMusic,
            onToggleHighlightStory = onToggleHighlightStory,
            onDismiss = { selectedStoryIndex = null },
            onRecordView = onRecordStoryView,
            onDeleteStory = { storyId ->
                onDeleteStory(storyId)
                selectedStoryIndex = null
            },
            onMuteStoryAuthor = { story ->
                onMuteUserStories(story.authorId, story.authorUsername, story.authorDisplayName)
                selectedStoryIndex = null
            },
            onReportStory = { story ->
                reportingTarget = story.id to "STORY"
                selectedStoryIndex = null
            }
        )
    }

    // Comments Bottom Sheet
    if (activeCommentPost != null) {
        val post = activeCommentPost!!
        CommentsBottomSheet(
            targetId = post.id,
            targetType = "POST",
            commentsAllowed = post.commentsAllowed,
            observeComments = observeComments,
            onAddComment = onAddComment,
            onDismiss = { activeCommentPost = null }
        )
    }

    // Edit Post & Manage Instagram-Style Collaborators Dialog
    if (editingPost != null) {
        val post = editingPost!!
        var newCaption by remember(post.id) { mutableStateOf(post.caption) }
        var allowComments by remember(post.id) { mutableStateOf(post.commentsAllowed) }
        var editedCollaborators by remember(post.id) { mutableStateOf(post.collaboratorUsernames) }
        var editedTagged by remember(post.id) { mutableStateOf(post.taggedUsernames) }

        AlertDialog(
            onDismissRequest = { editingPost = null },
            title = { Text("Edit Post & Collaborators") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newCaption,
                        onValueChange = { newCaption = it.take(2200) },
                        label = { Text("Caption") },
                        minLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    CollabAndTagPeopleSelectorCard(
                        currentUserId = currentUserId,
                        discoverableUsers = discoverableUsers,
                        selectedCollaborators = editedCollaborators,
                        onCollaboratorsChanged = { editedCollaborators = it },
                        selectedTags = editedTagged,
                        onTagsChanged = { editedTagged = it },
                        contentLabel = "Post"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Allow Comments")
                        Switch(checked = allowComments, onCheckedChange = { allowComments = it })
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdatePost(post.id, newCaption, allowComments)
                    onUpdatePostCollaborators(post, editedCollaborators, editedTagged)
                    editingPost = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingPost = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Report Content Dialog
    if (reportingTarget != null) {
        val (targetId, targetType) = reportingTarget!!
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { reportingTarget = null },
            title = { Text("Report $targetType") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Tell us why this content violates Mitr Community Guidelines:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it.take(450) },
                        label = { Text("Reason (e.g., spam, harassment)") },
                        minLines = 3,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReportContent(targetId, targetType, reason.ifBlank { "Inappropriate content" })
                        reportingTarget = null
                    }
                ) {
                    Text("Submit Report")
                }
            },
            dismissButton = {
                TextButton(onClick = { reportingTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun StoriesBar(
    currentUserProfile: UserProfile,
    stories: List<Story>,
    onAddStoryClick: () -> Unit,
    onStoryClick: (Int) -> Unit
) {
    val ownStoryIndex = remember(stories, currentUserProfile.userId) {
        stories.indexOfFirst { it.authorId == currentUserProfile.userId }
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .testTag("stories_row"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(76.dp)
                    .clickable {
                        if (ownStoryIndex >= 0) {
                            onStoryClick(ownStoryIndex)
                        } else {
                            onAddStoryClick()
                        }
                    }
                    .testTag("add_story_button")
            ) {
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .then(
                                if (ownStoryIndex >= 0) {
                                    Modifier
                                        .border(2.5.dp, LuminaPrismGradient, CircleShape)
                                        .padding(3.dp)
                                } else {
                                    Modifier
                                }
                            )
                            .clip(CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatarBadge(
                            avatarUrl = currentUserProfile.avatarUrl,
                            displayName = currentUserProfile.displayName,
                            size = if (ownStoryIndex >= 0) 64.dp else 68.dp,
                            showBorder = ownStoryIndex < 0
                        )
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                            .clickable(onClick = onAddStoryClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Story",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Your Story",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        items(stories.size) { index ->
            val story = stories[index]
            val isCloseFriends = story.audience == "CLOSE_FRIENDS"
            val isVideoStory = story.mediaType.equals("VIDEO", ignoreCase = true) ||
                story.mediaUrl.endsWith(".mp4", ignoreCase = true) ||
                story.mediaUrl.endsWith(".webm", ignoreCase = true)
            val ringBrush = if (isCloseFriends) {
                Brush.linearGradient(listOf(LuminaEmerald, Color(0xFF34D399)))
            } else {
                LuminaPrismGradient
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .width(76.dp)
                    .clickable { onStoryClick(index) }
                    .testTag("story_avatar_$index")
            ) {
                Box(
                    modifier = Modifier.size(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .border(2.5.dp, ringBrush, CircleShape)
                            .padding(3.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatarBadge(
                            avatarUrl = story.mediaUrl,
                            displayName = story.authorDisplayName,
                            size = 64.dp
                        )
                    }
                    if (isVideoStory) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(LuminaCoral)
                                .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Story",
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = story.authorUsername,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun PostItemCard(
    post: Post,
    currentUserId: String,
    currentUsername: String = "",
    discoverableUsers: List<UserProfile> = emptyList(),
    isSaved: Boolean,
    isMusicSaved: Boolean = false,
    isFocusedAndStationary: Boolean = false,
    audioPlayer: com.example.ui.components.AudioClipPlayerController,
    hideLikeCounts: Boolean,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onSaveClick: () -> Unit,
    onToggleSaveMusic: (MusicTrack) -> Unit = {},
    onShareClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onOpenCoAuthorsSheet: () -> Unit = {},
    onOpenCollaboratorProfile: (UserProfile) -> Unit = {},
    onLeaveCollaboration: () -> Unit = {},
    onHashtagClick: (String) -> Unit = {},
    onEditPost: () -> Unit,
    onDeletePost: () -> Unit,
    onReportPost: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isLiked = post.likedByUids.contains(currentUserId)
    val isOwnPost = post.authorId == currentUserId
    val normalizedCurrentUser = currentUsername.trim().removePrefix("@")
    val isCollaboratorOnPost = remember(post.collaboratorUsernames, normalizedCurrentUser) {
        normalizedCurrentUser.isNotBlank() && post.collaboratorUsernames.any {
            it.removePrefix("@").equals(normalizedCurrentUser, ignoreCase = true)
        }
    }

    val primaryCollaboratorProfile = remember(post.collaboratorUsernames, discoverableUsers) {
        val firstCollabUsername = post.collaboratorUsernames.firstOrNull()?.removePrefix("@") ?: ""
        if (firstCollabUsername.isBlank()) null
        else discoverableUsers.firstOrNull {
            it.username.removePrefix("@").equals(firstCollabUsername, ignoreCase = true)
        }
    }

    val ownerKey = remember(post.id) { "post_${post.id}" }

    // Immediately play only when this post is the centered stationary post; immediately stop when scrolling!
    LaunchedEffect(isFocusedAndStationary, post.id, post.musicUrl, post.musicStartMs, post.musicEndMs) {
        if (isFocusedAndStationary && post.musicUrl.isNotBlank()) {
            audioPlayer.playSyncedForOwner(
                ownerKey = ownerKey,
                url = post.musicUrl,
                startMs = post.musicStartMs,
                endMs = post.musicEndMs
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

    val likeScale by animateFloatAsState(
        targetValue = if (isLiked) 1.18f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "likeBounce"
    )

    LuminaGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("post_card_${post.id}"),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Creator Header with Instagram-style Dual Overlapping Avatars for Collab Posts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            if (post.collaboratorUsernames.isNotEmpty()) {
                                onOpenCoAuthorsSheet()
                            } else {
                                onAuthorClick()
                            }
                        }
                ) {
                    if (post.collaboratorUsernames.isNotEmpty()) {
                        OverlappingCollabAvatarsBadge(
                            authorAvatarUrl = post.authorAvatarUrl,
                            authorDisplayName = post.authorDisplayName.ifBlank { post.authorUsername },
                            collaboratorAvatarUrl = primaryCollaboratorProfile?.avatarUrl ?: "",
                            collaboratorDisplayName = primaryCollaboratorProfile?.displayName
                                ?: post.collaboratorUsernames.firstOrNull().orEmpty()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .border(2.dp, LuminaPrismGradient, CircleShape)
                                .padding(2.5.dp)
                                .clip(CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            UserAvatarBadge(
                                avatarUrl = post.authorAvatarUrl,
                                displayName = post.authorDisplayName,
                                size = 38.dp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val headerAuthorsText = if (post.collaboratorUsernames.isNotEmpty()) {
                                val collabs = post.collaboratorUsernames.joinToString(", ") {
                                    it.removePrefix("@")
                                }
                                "${post.authorUsername.ifBlank { post.authorDisplayName }} and $collabs"
                            } else {
                                post.authorUsername.ifBlank { post.authorDisplayName }
                            }
                            val isAuthorBlueTick = remember(post.authorId, post.authorUsername, discoverableUsers) {
                                isCreatorHandleBlueTickVerified(
                                    authorId = post.authorId,
                                    authorUsername = post.authorUsername,
                                    discoverableUsers = discoverableUsers
                                )
                            }
                            Text(
                                text = headerAuthorsText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (isAuthorBlueTick) {
                                Spacer(modifier = Modifier.width(5.dp))
                                VerifiedBadgeIcon(size = 15.dp)
                            }
                            if (post.collaboratorUsernames.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                                    modifier = Modifier.clickable { onOpenCoAuthorsSheet() }
                                ) {
                                    Text(
                                        text = "Collab",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        val subLine = buildString {
                            if (post.locationTag.isNotBlank()) {
                                append(post.locationTag)
                            } else if (post.collaboratorUsernames.isNotEmpty()) {
                                append("Original & Co-Author Collaboration")
                            } else {
                                append(post.authorDisplayName)
                            }
                            if (post.taggedUsernames.isNotEmpty()) {
                                append(" • Tagged: ")
                                append(post.taggedUsernames.joinToString(", ") { "@${it.removePrefix("@")}" })
                            }
                        }
                        Text(
                            text = subLine,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Post Options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        if (post.collaboratorUsernames.isNotEmpty()) {
                            DropdownMenuItem(
                                text = { Text("View Co-Authors (${post.collaboratorUsernames.size + 1})") },
                                leadingIcon = { Icon(Icons.Default.Group, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onOpenCoAuthorsSheet()
                                }
                            )
                        }
                        if (isOwnPost) {
                            DropdownMenuItem(
                                text = { Text("Edit Post & Collabs") },
                                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onEditPost()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Post") },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onDeletePost()
                                }
                            )
                        } else {
                            if (isCollaboratorOnPost) {
                                DropdownMenuItem(
                                    text = { Text("Leave Collaboration") },
                                    leadingIcon = { Icon(Icons.Default.PersonRemove, contentDescription = null) },
                                    onClick = {
                                        menuExpanded = false
                                        onLeaveCollaboration()
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Text("Report Post") },
                                leadingIcon = { Icon(Icons.Default.Flag, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onReportPost()
                                }
                            )
                        }
                    }
                }
            }

            // Media Carousel with bottom-left translucent location pill (Matches Screenshot 1)
            if (post.mediaUrls.isNotEmpty()) {
                val pagerState = rememberPagerState(pageCount = { post.mediaUrls.size })
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.92f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        AsyncImage(
                            model = post.mediaUrls[page],
                            contentDescription = post.caption,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (post.mediaUrls.size > 1) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp),
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.65f)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${post.mediaUrls.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    if (post.locationTag.isNotBlank()) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp),
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF121420).copy(alpha = 0.72f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = post.locationTag,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    if (post.collaboratorUsernames.isNotEmpty() || post.taggedUsernames.isNotEmpty()) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp)
                                .clickable { onOpenCoAuthorsSheet() },
                            shape = RoundedCornerShape(50),
                            color = Color(0xFF121420).copy(alpha = 0.78f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = "Collaborators & Tagged People",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (post.collaboratorUsernames.isNotEmpty()) {
                                        "Collab (${post.collaboratorUsernames.size + 1})"
                                    } else {
                                        "${post.taggedUsernames.size} tagged"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Attached Trimmed Music Pill Banner on Post
            if (post.musicTitle.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 10.dp)
                ) {
                    AttachedMusicPillBanner(
                        musicTrackId = post.musicTrackId,
                        musicTitle = post.musicTitle,
                        musicArtist = post.musicArtist.ifBlank { post.authorDisplayName },
                        musicCoverUrl = post.musicCoverUrl.ifBlank { post.mediaUrls.firstOrNull() ?: "" },
                        musicUrl = post.musicUrl,
                        musicStartMs = post.musicStartMs,
                        musicEndMs = post.musicEndMs,
                        isMusicSaved = isMusicSaved,
                        audioPlayer = audioPlayer,
                        onToggleSaveMusic = onToggleSaveMusic,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Engagement Action Bar matching Screenshot 1 (Heart, Comment, Paper-Plane Share on left; Bookmark on right)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = onLikeClick,
                        modifier = Modifier.testTag("like_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) LuminaAmber else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .size(25.dp)
                                .scale(likeScale)
                        )
                    }

                    IconButton(
                        onClick = onCommentClick,
                        modifier = Modifier.testTag("comment_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ModeComment,
                            contentDescription = "Comments",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(23.dp)
                        )
                    }

                    IconButton(
                        onClick = onShareClick,
                        modifier = Modifier.testTag("share_button_${post.id}")
                    ) {
                        Icon(
                            imageVector = MitrIcons.PaperPlaneSend,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(23.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onSaveClick,
                    modifier = Modifier.testTag("save_button_${post.id}")
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Likes Count + Bold Co-Author Usernames & Inline Caption
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!hideLikeCounts) {
                    val isAuthorBlueTick = remember(post.authorId, post.authorUsername, discoverableUsers) {
                        isCreatorHandleBlueTickVerified(
                            authorId = post.authorId,
                            authorUsername = post.authorUsername,
                            discoverableUsers = discoverableUsers
                        )
                    }
                    val displayLikes = if (isAuthorBlueTick) {
                        post.likeCount.coerceAtLeast(180L) + 42L
                    } else {
                        post.likeCount
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "%,d likes".format(displayLikes),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isAuthorBlueTick) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• Blue Tick Reach Boost",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF0095F6),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (post.caption.isNotBlank()) {
                    val captionAuthorPrefix = if (post.collaboratorUsernames.isNotEmpty()) {
                        val collabs = post.collaboratorUsernames.joinToString(", ") { it.removePrefix("@") }
                        "${post.authorUsername.ifBlank { post.authorDisplayName }} and $collabs"
                    } else {
                        post.authorUsername.ifBlank { post.authorDisplayName }
                    }
                    Text(
                        text = buildAnnotatedString {
                            withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                                append(captionAuthorPrefix)
                                append("  ")
                            }
                            append(post.caption)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Quick clickable Collaborator & Tag pills
                if (post.collaboratorUsernames.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        post.collaboratorUsernames.forEach { collabUsername ->
                            val clean = collabUsername.removePrefix("@")
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
                                modifier = Modifier.clickable {
                                    val found = discoverableUsers.firstOrNull {
                                        it.username.removePrefix("@").equals(clean, ignoreCase = true)
                                    } ?: UserProfile(
                                        userId = "collab_$clean",
                                        username = clean,
                                        displayName = clean.replaceFirstChar { it.uppercase() }
                                    )
                                    onOpenCollaboratorProfile(found)
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Group,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Collab with @$clean",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                if (post.hashtags.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        post.hashtags.take(4).forEach { tag ->
                            Text(
                                text = "#$tag",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onHashtagClick(tag) }
                            )
                        }
                    }
                }

                if (post.commentCount > 0) {
                    Text(
                        text = "View all ${post.commentCount} comments",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable(onClick = onCommentClick)
                    )
                }
            }
        }
    }
}

@Composable
fun OverlappingCollabAvatarsBadge(
    authorAvatarUrl: String,
    authorDisplayName: String,
    collaboratorAvatarUrl: String,
    collaboratorDisplayName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.width(58.dp).height(44.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .border(2.dp, LuminaPrismGradient, CircleShape)
                .padding(2.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            UserAvatarBadge(
                avatarUrl = authorAvatarUrl,
                displayName = authorDisplayName,
                size = 32.dp
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(36.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .border(2.dp, LuminaPrismGradient, CircleShape)
                .padding(2.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            UserAvatarBadge(
                avatarUrl = collaboratorAvatarUrl,
                displayName = collaboratorDisplayName.ifBlank { "Collab" },
                size = 32.dp
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InstagramCoAuthorsBottomSheet(
    title: String = "Collaborators & Co-Authors",
    authorId: String,
    authorUsername: String,
    authorDisplayName: String,
    authorAvatarUrl: String,
    collaboratorUsernames: List<String>,
    taggedUsernames: List<String> = emptyList(),
    discoverableUsers: List<UserProfile>,
    currentUserId: String,
    currentUsername: String,
    followingIds: List<String>,
    onToggleFollow: (UserProfile) -> Unit,
    onSelectProfile: (UserProfile) -> Unit,
    onLeaveCollaboration: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val normalizedCurrentUsername = currentUsername.trim().removePrefix("@")
    val isCurrentUserCollaborator = remember(collaboratorUsernames, normalizedCurrentUsername) {
        normalizedCurrentUsername.isNotBlank() && collaboratorUsernames.any {
            it.removePrefix("@").equals(normalizedCurrentUsername, ignoreCase = true)
        }
    }

    val authorProfile = remember(authorId, authorUsername, discoverableUsers) {
        discoverableUsers.firstOrNull {
            it.userId == authorId || it.username.equals(authorUsername, ignoreCase = true)
        } ?: UserProfile(
            userId = authorId,
            username = authorUsername,
            displayName = authorDisplayName.ifBlank { authorUsername },
            avatarUrl = authorAvatarUrl
        )
    }

    val collaboratorProfiles = remember(collaboratorUsernames, discoverableUsers) {
        collaboratorUsernames.map { rawUsername ->
            val clean = rawUsername.trim().removePrefix("@")
            discoverableUsers.firstOrNull {
                it.username.removePrefix("@").equals(clean, ignoreCase = true)
            } ?: UserProfile(
                userId = "collab_$clean",
                username = clean,
                displayName = clean.replaceFirstChar { it.uppercase() }
            )
        }
    }

    val taggedProfiles = remember(taggedUsernames, discoverableUsers) {
        taggedUsernames.map { rawUsername ->
            val clean = rawUsername.trim().removePrefix("@")
            discoverableUsers.firstOrNull {
                it.username.removePrefix("@").equals(clean, ignoreCase = true)
            } ?: UserProfile(
                userId = "tag_$clean",
                username = clean,
                displayName = clean.replaceFirstChar { it.uppercase() }
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Shared on all co-authors' profiles & feeds",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isCurrentUserCollaborator && onLeaveCollaboration != null) {
                    OutlinedButton(
                        onClick = onLeaveCollaboration,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonRemove,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Leave Collab", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = "CO-AUTHORS",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            // Original Author Row
            CoAuthorPersonRow(
                person = authorProfile,
                roleLabel = "Original Creator",
                currentUserId = currentUserId,
                isFollowing = followingIds.contains(authorProfile.userId),
                onToggleFollow = { onToggleFollow(authorProfile) },
                onClickProfile = { onSelectProfile(authorProfile) }
            )

            // Co-Authors Rows
            collaboratorProfiles.forEach { collabProfile ->
                CoAuthorPersonRow(
                    person = collabProfile,
                    roleLabel = "Co-Author • Collaborator",
                    currentUserId = currentUserId,
                    isFollowing = followingIds.contains(collabProfile.userId),
                    onToggleFollow = { onToggleFollow(collabProfile) },
                    onClickProfile = { onSelectProfile(collabProfile) }
                )
            }

            if (taggedProfiles.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "TAGGED IN THIS POST",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                taggedProfiles.forEach { taggedProfile ->
                    CoAuthorPersonRow(
                        person = taggedProfile,
                        roleLabel = "Tagged Person",
                        currentUserId = currentUserId,
                        isFollowing = followingIds.contains(taggedProfile.userId),
                        onToggleFollow = { onToggleFollow(taggedProfile) },
                        onClickProfile = { onSelectProfile(taggedProfile) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CoAuthorPersonRow(
    person: UserProfile,
    roleLabel: String,
    currentUserId: String,
    isFollowing: Boolean,
    onToggleFollow: () -> Unit,
    onClickProfile: () -> Unit
) {
    LuminaGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClickProfile),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                UserAvatarBadge(
                    avatarUrl = person.avatarUrl,
                    displayName = person.displayName.ifBlank { person.username },
                    size = 44.dp,
                    showBorder = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = person.displayName.ifBlank { person.username },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        VerifiedBadgeIcon(size = 14.dp)
                    }
                    Text(
                        text = "@${person.username.removePrefix("@")} • $roleLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (person.userId != currentUserId && !person.userId.startsWith("collab_") && !person.userId.startsWith("tag_")) {
                Button(
                    onClick = onToggleFollow,
                    shape = RoundedCornerShape(12.dp),
                    colors = if (isFollowing) {
                        ButtonDefaults.filledTonalButtonColors()
                    } else {
                        ButtonDefaults.buttonColors()
                    }
                ) {
                    Text(
                        text = if (isFollowing) "Following" else "Follow",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharePostToPersonBottomSheet(
    post: Post,
    currentUserId: String,
    discoverableUsers: List<UserProfile>,
    onShareToUser: (UserProfile, Post) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var searchFilter by remember { mutableStateOf("") }
    val recipients = remember(discoverableUsers, currentUserId, searchFilter) {
        val others = discoverableUsers.filter { it.userId != currentUserId }
        if (searchFilter.isBlank()) others
        else others.filter {
            it.username.contains(searchFilter, ignoreCase = true) ||
                it.displayName.contains(searchFilter, ignoreCase = true)
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Share Post to a Person",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Send @${post.authorUsername}'s post directly in Mitr Messages",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "Check out @${post.authorUsername}'s post on Mitr: ${post.caption}"
                            )
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Post"))
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("External")
                }
            }

            OutlinedTextField(
                value = searchFilter,
                onValueChange = { searchFilter = it },
                placeholder = { Text("Search person (@username or name)...") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("share_post_search_person_input")
            )

            if (recipients.isEmpty()) {
                Text(
                    text = "No matching people found to share with.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 18.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recipients, key = { it.userId }) { person ->
                        LuminaGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    UserAvatarBadge(
                                        avatarUrl = person.avatarUrl,
                                        displayName = person.displayName,
                                        size = 42.dp,
                                        showBorder = true
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = person.displayName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "@${person.username}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Button(
                                    onClick = { onShareToUser(person, post) },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.testTag("send_post_to_${person.username}")
                                ) {
                                    Icon(
                                        imageVector = MitrIcons.PaperPlaneSend,
                                        contentDescription = "Send Post",
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Send", fontWeight = FontWeight.Bold)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullScreenStoryViewerDialog(
    stories: List<Story>,
    initialIndex: Int,
    currentUserId: String,
    savedMusicList: List<SavedMusic> = emptyList(),
    discoverableUsers: List<UserProfile> = emptyList(),
    onToggleSaveMusic: (MusicTrack) -> Unit = {},
    onToggleHighlightStory: (Story, String) -> Unit = { _, _ -> },
    onDismiss: () -> Unit,
    onRecordView: (Story) -> Unit,
    onDeleteStory: (String) -> Unit,
    onMuteStoryAuthor: (Story) -> Unit,
    onReportStory: (Story) -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(initialIndex) }
    var isPaused by remember { mutableStateOf(false) }
    var isVideoMuted by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    var showViewersSheet by remember { mutableStateOf(false) }
    val storyAudioPlayer = rememberAudioClipPlayer()

    DisposableEffect(Unit) {
        onDispose {
            storyAudioPlayer.release()
        }
    }

    val currentStory = stories.getOrNull(currentIndex) ?: return
    val storyOwnerKey = remember(currentStory.id) { "story_${currentStory.id}" }

    DisposableEffect(storyOwnerKey) {
        onDispose {
            storyAudioPlayer.stopForOwner(storyOwnerKey)
        }
    }

    val isVideoStory = remember(currentStory.id, currentStory.mediaType, currentStory.mediaUrl) {
        currentStory.mediaType.equals("VIDEO", ignoreCase = true) ||
            currentStory.mediaUrl.endsWith(".mp4", ignoreCase = true) ||
            currentStory.mediaUrl.endsWith(".webm", ignoreCase = true) ||
            currentStory.mediaUrl.endsWith(".mov", ignoreCase = true)
    }

    LaunchedEffect(currentStory.id) {
        progress = 0f
        onRecordView(currentStory)
        if (currentStory.musicUrl.isNotBlank()) {
            storyAudioPlayer.playSyncedForOwner(
                ownerKey = storyOwnerKey,
                url = currentStory.musicUrl,
                startMs = currentStory.musicStartMs,
                endMs = currentStory.musicEndMs
            )
        } else {
            storyAudioPlayer.pause()
        }
        val step = if (isVideoStory) 0.005f else 0.01f
        while (progress < 1f) {
            delay(50L)
            if (!isPaused && !showViewersSheet) {
                progress += step
            }
        }
        storyAudioPlayer.stopForOwner(storyOwnerKey)
        if (currentIndex < stories.lastIndex) {
            currentIndex += 1
        } else {
            storyAudioPlayer.pause()
            onDismiss()
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 50, easing = LinearEasing),
        label = "storyProgress"
    )

    var dragStartOffset by remember { mutableStateOf(Offset.Zero) }
    var totalDragDelta by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = {
            storyAudioPlayer.pause()
            onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("fullscreen_story_viewer")
                .pointerInput(currentStory.id) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            dragStartOffset = startOffset
                            totalDragDelta = Offset.Zero
                            isPaused = true
                        },
                        onDragEnd = {
                            isPaused = false
                            // Swipe from the bottom-left corner upwards or rightwards opens the Story Viewers Board!
                            val startedInBottomLeft = dragStartOffset.x < size.width * 0.55f &&
                                dragStartOffset.y > size.height * 0.58f
                            val swipedUpOrRight = totalDragDelta.y < -45f || totalDragDelta.x > 45f
                            if (startedInBottomLeft && swipedUpOrRight) {
                                showViewersSheet = true
                            }
                        },
                        onDragCancel = {
                            isPaused = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            totalDragDelta += dragAmount
                        }
                    )
                }
                .pointerInput(currentStory.id) {
                    detectTapGestures(
                        onPress = {
                            isPaused = true
                            tryAwaitRelease()
                            isPaused = false
                        },
                        onTap = { offset ->
                            if (offset.x < size.width / 3f) {
                                if (currentIndex > 0) currentIndex -= 1
                            } else {
                                if (currentIndex < stories.lastIndex) {
                                    currentIndex += 1
                                } else {
                                    storyAudioPlayer.pause()
                                    onDismiss()
                                }
                            }
                        }
                    )
                },
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // Full-screen Story Media Renderer: supports both Story Images and Story Videos
                if (isVideoStory && (
                        currentStory.mediaUrl.startsWith("http") ||
                            currentStory.mediaUrl.startsWith("content://") ||
                            currentStory.mediaUrl.startsWith("file://")
                        )
                ) {
                    AndroidView(
                        factory = { ctx ->
                            VideoView(ctx).apply {
                                setVideoURI(Uri.parse(currentStory.mediaUrl))
                                setOnPreparedListener { mp ->
                                    mp.isLooping = false
                                    val vol = if (isVideoMuted || currentStory.musicUrl.isNotBlank()) 0f else 1f
                                    mp.setVolume(vol, vol)
                                    if (!isPaused && !showViewersSheet) start()
                                }
                                setOnCompletionListener {
                                    progress = 1f
                                }
                                setOnErrorListener { _, _, _ -> true }
                            }
                        },
                        update = { view ->
                            if ((isPaused || showViewersSheet) && view.isPlaying) {
                                view.pause()
                            } else if (!isPaused && !showViewersSheet && !view.isPlaying) {
                                view.start()
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("story_video_player")
                    )
                } else {
                    AsyncImage(
                        model = currentStory.mediaUrl,
                        contentDescription = currentStory.caption,
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("story_image_viewer"),
                        contentScale = ContentScale.Crop
                    )
                }

                // Top progress bars & Author header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Black.copy(alpha = 0.78f), Color.Transparent)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        stories.forEachIndexed { idx, _ ->
                            LinearProgressIndicator(
                                progress = {
                                    when {
                                        idx < currentIndex -> 1f
                                        idx == currentIndex -> animatedProgress
                                        else -> 0f
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val storyHeaderText = if (currentStory.collaboratorUsernames.isNotEmpty()) {
                                "@${currentStory.authorUsername} & " +
                                    currentStory.collaboratorUsernames.joinToString(" & ") { "@${it.removePrefix("@")}" }
                            } else {
                                "@${currentStory.authorUsername}"
                            }
                            Text(
                                text = storyHeaderText,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            if (currentStory.taggedUsernames.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "with " + currentStory.taggedUsernames.joinToString(", ") { "@${it.removePrefix("@")}" },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                            if (isVideoStory) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = LuminaCoral.copy(alpha = 0.85f),
                                    shape = CircleShape
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "Video",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            if (currentStory.isHighlighted) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = "★ ${currentStory.highlightTitle.ifBlank { "Highlight" }}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            } else if (currentStory.audience == "CLOSE_FRIENDS") {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = LuminaEmerald,
                                    shape = CircleShape
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Close Friends", style = MaterialTheme.typography.labelSmall, color = Color.White)
                                    }
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isVideoStory) {
                                IconButton(onClick = { isVideoMuted = !isVideoMuted }) {
                                    Icon(
                                        imageVector = if (isVideoMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Mute or Unmute Video Story",
                                        tint = Color.White
                                    )
                                }
                            }
                            if (currentStory.authorId == currentUserId) {
                                IconButton(
                                    onClick = { onToggleHighlightStory(currentStory, "Highlights") },
                                    modifier = Modifier.testTag("story_toggle_highlight_button")
                                ) {
                                    Icon(
                                        imageVector = if (currentStory.isHighlighted) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Highlight Story on Profile",
                                        tint = if (currentStory.isHighlighted) LuminaAmber else Color.White
                                    )
                                }
                                IconButton(onClick = { showViewersSheet = true }) {
                                    Icon(Icons.Default.Visibility, contentDescription = "Viewers Board", tint = Color.White)
                                }
                                IconButton(onClick = { onDeleteStory(currentStory.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Story", tint = Color.White)
                                }
                            } else {
                                TextButton(onClick = { onMuteStoryAuthor(currentStory) }) {
                                    Text("Mute", color = Color.White)
                                }
                                IconButton(onClick = { onReportStory(currentStory) }) {
                                    Icon(Icons.Default.Flag, contentDescription = "Report Story", tint = Color.White)
                                }
                            }
                            IconButton(onClick = {
                                storyAudioPlayer.pause()
                                onDismiss()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close Story", tint = Color.White)
                            }
                        }
                    }
                }

            // Story Overlay Text & Sticker
            if (currentStory.overlayText.isNotBlank() || currentStory.stickerLabel.isNotBlank()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (currentStory.stickerLabel.isNotBlank()) {
                        Surface(
                            color = LuminaViolet,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "✦ ${currentStory.stickerLabel}",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    if (currentStory.overlayText.isNotBlank()) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = currentStory.overlayText,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(18.dp)
                            )
                        }
                    }
                }
            }

            // Story Caption, Attached Music Pill & Bottom-Left Corner Swipe Viewers Board Trigger
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                        )
                    )
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (currentStory.musicTitle.isNotBlank()) {
                    val isStoryMusicSaved = savedMusicList.any {
                        (currentStory.musicTrackId.isNotBlank() && it.trackId == currentStory.musicTrackId) ||
                            it.title.equals(currentStory.musicTitle, ignoreCase = true)
                    }
                    AttachedMusicPillBanner(
                        musicTrackId = currentStory.musicTrackId,
                        musicTitle = currentStory.musicTitle,
                        musicArtist = currentStory.musicArtist.ifBlank { currentStory.authorDisplayName },
                        musicCoverUrl = currentStory.musicCoverUrl.ifBlank { currentStory.mediaUrl },
                        musicUrl = currentStory.musicUrl,
                        musicStartMs = currentStory.musicStartMs,
                        musicEndMs = currentStory.musicEndMs,
                        isMusicSaved = isStoryMusicSaved,
                        audioPlayer = storyAudioPlayer,
                        onToggleSaveMusic = onToggleSaveMusic,
                        modifier = Modifier.fillMaxWidth(),
                        darkOverlayStyle = true
                    )
                }

                if (currentStory.caption.isNotBlank()) {
                    Text(
                        text = currentStory.caption,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }

                // Bottom-Left Corner Swipe Zone & Pill to open Story Viewers Board + Highlight Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = { showViewersSheet = true },
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier
                            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                            .testTag("story_bottom_left_viewers_trigger")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwipeUp,
                                contentDescription = "Swipe from bottom-left for Viewers Board",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Seen by ${currentStory.viewerIds.size} • Swipe bottom-left",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (currentStory.authorId == currentUserId) {
                        Surface(
                            onClick = { onToggleHighlightStory(currentStory, "Highlights") },
                            shape = RoundedCornerShape(16.dp),
                            color = if (currentStory.isHighlighted) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.16f),
                            modifier = Modifier.border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentStory.isHighlighted) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (currentStory.isHighlighted) "Highlighted" else "Highlight",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }

    if (showViewersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showViewersSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
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
                            text = "Story Viewers Board (${currentStory.viewerIds.size})",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Opened by swiping from the bottom-left corner",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (currentStory.authorId == currentUserId) {
                        OutlinedButton(
                            onClick = { onToggleHighlightStory(currentStory, "Highlights") },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (currentStory.isHighlighted) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (currentStory.isHighlighted) "In Highlights" else "Add to Highlights")
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))

                if (currentStory.viewerIds.isEmpty()) {
                    Text(
                        text = "No viewers recorded for this story yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 20.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(currentStory.viewerIds, key = { it }) { viewerUid ->
                            val viewerProfile = discoverableUsers.find { it.userId == viewerUid }
                            LuminaGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        UserAvatarBadge(
                                            avatarUrl = viewerProfile?.avatarUrl ?: "",
                                            displayName = viewerProfile?.displayName ?: "Mitr User",
                                            size = 40.dp,
                                            showBorder = true
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = viewerProfile?.displayName ?: "Mitr Member",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = if (viewerProfile != null) "@${viewerProfile.username}" else "UID: ${viewerUid.take(10)}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = "Viewed",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsBottomSheet(
    targetId: String,
    targetType: String,
    commentsAllowed: Boolean,
    observeComments: (String, String) -> Flow<List<Comment>>,
    onAddComment: (String, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val comments by observeComments(targetId, targetType).collectAsState(initial = emptyList())
    var commentText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = "Comments (${comments.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (comments.isEmpty()) {
                Text(
                    text = "No comments yet. Share the first thought!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        LuminaGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = if (comment.parentCommentId.isNotBlank()) 24.dp else 0.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "@${comment.authorUsername}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    TextButton(onClick = { replyingTo = comment }) {
                                        Text("Reply", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Text(
                                    text = comment.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!commentsAllowed) {
                Text(
                    text = "Comments are disabled for this post.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                if (replyingTo != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Replying to @${replyingTo!!.authorUsername}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { replyingTo = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel Reply", modifier = Modifier.size(16.dp))
                        }
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 22.dp)
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it.take(900) },
                        placeholder = { Text("Add a comment...") },
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_field"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onAddComment(
                                    targetId,
                                    targetType,
                                    commentText,
                                    replyingTo?.id ?: ""
                                )
                                commentText = ""
                                replyingTo = null
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(LuminaPrismGradient)
                            .testTag("send_comment_button")
                    ) {
                        Icon(
                            imageVector = MitrIcons.PaperPlaneSend,
                            contentDescription = "Send Comment",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * High-visibility Circular Profile Picture component:
 * - Always renders a vibrant, high-contrast circular avatar with proportional initial or silhouette fallback
 * - Overlays the user's photo (`avatarUrl`) when present and successfully loaded
 * - Supports an optional outer gradient ring (`showBorder`)
 */
@Composable
fun UserAvatarBadge(
    avatarUrl: String,
    displayName: String,
    size: Dp = 44.dp,
    showBorder: Boolean = false
) {
    val cleanInitial = displayName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "M"
    val dynamicFontSize = (size.value * 0.40f).coerceIn(12f, 38f).sp

    Box(
        modifier = Modifier
            .size(size)
            .then(
                if (showBorder) {
                    Modifier
                        .border(2.dp, LuminaPrismGradient, CircleShape)
                        .padding(2.5.dp)
                } else {
                    Modifier
                }
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    colors = listOf(LuminaCoral, LuminaViolet)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // High-visibility fallback monogram / avatar icon ALWAYS rendered underneath so it is never blank
        if (cleanInitial.isNotBlank()) {
            Text(
                text = cleanInitial,
                fontSize = dynamicFontSize,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        } else {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = displayName,
                tint = Color.White,
                modifier = Modifier.size(size * 0.55f)
            )
        }

        // Render actual profile picture on top when URL/URI is present
        if (avatarUrl.isNotBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = "$displayName profile picture",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
fun EmptyOrErrorCard(
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    LuminaGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LuminaPrismArtwork(
                size = 88.dp,
                icon = Icons.Default.AutoAwesome
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(20.dp))
                LuminaGradientButton(
                    text = actionLabel,
                    onClick = onAction,
                    icon = Icons.Default.Add
                )
            }
        }
    }
}

@Composable
private fun PostSkeletonCard() {
    LuminaGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .width(130.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}
