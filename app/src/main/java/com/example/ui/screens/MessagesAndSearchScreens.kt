package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.material3.LinearProgressIndicator
import com.example.data.model.Conversation
import com.example.data.model.DirectMessage
import com.example.data.model.FirestoreFeedSearchResult
import com.example.data.model.Follow
import com.example.data.model.NotificationCategory
import com.example.data.model.Post
import com.example.data.model.Reel
import com.example.data.model.SocialNotificationItem
import com.example.data.model.UiState
import com.example.data.model.UserActivityLog
import com.example.data.model.UserProfile
import com.example.ui.components.AuroraAmbientBackground
import com.example.ui.components.LuminaGlassCard
import com.example.ui.components.LuminaPillChip
import com.example.ui.components.MitrIcons
import com.example.ui.components.MitrTopHeaderBar
import com.example.ui.components.VerifiedBadgeIcon
import com.example.ui.theme.LuminaAmber
import com.example.ui.theme.LuminaCoral
import com.example.ui.theme.LuminaEmerald
import com.example.ui.theme.LuminaPrismGradient
import com.example.ui.theme.LuminaViolet
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun formatChatTimestamp(timestamp: Timestamp?): String {
    val date = timestamp?.toDate() ?: return "Just now"
    val now = System.currentTimeMillis()
    val diffMs = now - date.time
    val oneDayMs = 24L * 60L * 60L * 1000L
    return if (diffMs < oneDayMs) {
        SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    } else {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(date)
    }
}

internal fun formatNotificationTimeMillis(millis: Long): String {
    if (millis <= 0L) return "Recent"
    val diffSec = ((System.currentTimeMillis() - millis) / 1000L).coerceAtLeast(0L)
    return when {
        diffSec < 60L -> "Just now"
        diffSec < 3600L -> "${diffSec / 60L}m ago"
        diffSec < 86400L -> "${diffSec / 3600L}h ago"
        diffSec < 604800L -> "${diffSec / 86400L}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))
    }
}

/**
 * Firestore-backed ChatList & Real-Time Messaging Screen.
 * Stores message threads in `/conversations/{conversationId}` and real-time messages in
 * `/conversations/{conversationId}/messages/{messageId}` with live text and timestamp display.
 */
@Composable
fun ChatListScreen(
    currentUserId: String,
    currentUserProfile: UserProfile = UserProfile(),
    conversations: List<Conversation>,
    discoverableUsers: List<UserProfile>,
    readReceiptsEnabled: Boolean,
    showActivityStatus: Boolean,
    notificationBadgeCount: Int = 0,
    initialConversationId: String? = null,
    onConsumeInitialConversation: () -> Unit = {},
    observeMessages: (String) -> Flow<List<DirectMessage>>,
    onMarkConversationRead: (String) -> Unit = {},
    onSendMessage: (Conversation, String, String, String, String) -> Unit,
    onDeleteMessage: (String, String) -> Unit,
    onAcceptOrDeclineRequest: (String, Boolean) -> Unit,
    onStartConversationWithUser: (UserProfile) -> Unit,
    onBlockUser: (UserProfile) -> Unit,
    onReportConversation: (String, String) -> Unit,
    onOpenSearch: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenUserProfile: (String) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Primary Inbox, 1 = Message Requests
    var activeConversationId by remember { mutableStateOf<String?>(initialConversationId) }
    var showNewChatPicker by remember { mutableStateOf(false) }
    var threadSearchQuery by remember { mutableStateOf("") }

    LaunchedEffect(initialConversationId) {
        if (!initialConversationId.isNullOrBlank()) {
            activeConversationId = initialConversationId
            onMarkConversationRead(initialConversationId)
            onConsumeInitialConversation()
        }
    }

    val userById = remember(discoverableUsers) {
        discoverableUsers.associateBy { it.userId }
    }

    val activeConversation = conversations.find { it.id == activeConversationId }

    if (activeConversation != null) {
        val peerUserId = activeConversation.participantIds.firstOrNull { it != currentUserId }
            ?: activeConversation.participantIds.firstOrNull().orEmpty()
        val peerProfile = userById[peerUserId]

        LaunchedEffect(activeConversation.id, activeConversation.unreadCount) {
            if (activeConversation.unreadCount > 0 && activeConversation.lastSenderId != currentUserId) {
                onMarkConversationRead(activeConversation.id)
            }
        }

        IndividualChatView(
            currentUserId = currentUserId,
            conversation = activeConversation,
            peerProfile = peerProfile,
            readReceiptsEnabled = readReceiptsEnabled,
            showActivityStatus = showActivityStatus,
            observeMessages = observeMessages,
            onSendMessage = { text, replyId ->
                onSendMessage(activeConversation, text, "", "NONE", replyId)
            },
            onDeleteMessage = { msgId ->
                onDeleteMessage(activeConversation.id, msgId)
            },
            onAcceptOrDeclineRequest = { accept ->
                onAcceptOrDeclineRequest(activeConversation.id, accept)
            },
            onReport = { reason ->
                onReportConversation(activeConversation.id, reason)
            },
            onOpenPeerProfile = {
                if (peerUserId.isNotBlank()) {
                    onOpenUserProfile(peerUserId)
                }
            },
            onBack = { activeConversationId = null }
        )
        return
    }

    val primaryList = conversations.filter { it.status == "ACTIVE" }
    val requestList = conversations.filter { it.status == "REQUESTED" }
    val baseThreads = if (selectedTab == 0) primaryList else requestList

    val filteredThreads = remember(baseThreads, threadSearchQuery, userById, currentUserId) {
        if (threadSearchQuery.isBlank()) {
            baseThreads
        } else {
            val q = threadSearchQuery.trim().lowercase(Locale.ROOT)
            baseThreads.filter { conv ->
                val peerId = conv.participantIds.firstOrNull { it != currentUserId }.orEmpty()
                val peer = userById[peerId]
                conv.participantUsernames.any { it.lowercase(Locale.ROOT).contains(q) } ||
                    (peer?.displayName?.lowercase(Locale.ROOT)?.contains(q) == true) ||
                    conv.lastMessageText.lowercase(Locale.ROOT).contains(q)
            }
        }
    }

    val otherUsers = remember(discoverableUsers, currentUserId) {
        discoverableUsers.filter { it.userId != currentUserId }
    }

    AuroraAmbientBackground(intensity = 0.06f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("chat_list_screen")
        ) {
            // Unified Top Bar with Working Bell Icon & Avatar
            MitrTopHeaderBar(
                sectionTitle = "Chats",
                notificationBadgeCount = notificationBadgeCount,
                onSearchClick = onOpenSearch,
                onNotificationsClick = onOpenNotifications,
                onQuickMessageClick = { showNewChatPicker = true },
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

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            // Thread Search Bar + New Chat Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = threadSearchQuery,
                    onValueChange = { threadSearchQuery = it },
                    placeholder = { Text("Search message threads or @users...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (threadSearchQuery.isNotBlank()) {
                            IconButton(onClick = { threadSearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_list_search_input")
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(LuminaPrismGradient)
                        .clickable { showNewChatPicker = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                        .testTag("new_message_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = MitrIcons.PaperPlaneSend,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "New Chat",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Active Users Quick-Chat Horizontal Row
            if (otherUsers.isNotEmpty()) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .testTag("chat_active_users_row"),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(otherUsers, key = { it.userId }) { user ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .width(68.dp)
                                .clickable { onStartConversationWithUser(user) }
                                .testTag("quick_chat_user_${user.userId}")
                        ) {
                            Box(contentAlignment = Alignment.BottomEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .border(2.dp, LuminaPrismGradient, CircleShape)
                                        .padding(2.5.dp)
                                        .clip(CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    UserAvatarBadge(
                                        avatarUrl = user.avatarUrl,
                                        displayName = user.displayName,
                                        size = 50.dp
                                    )
                                }
                                if (showActivityStatus) {
                                    Box(
                                        modifier = Modifier
                                            .size(13.dp)
                                            .clip(CircleShape)
                                            .background(LuminaEmerald)
                                            .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = user.username.ifBlank { user.displayName },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Pill Selector for Primary vs Requests
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                LuminaPillChip(
                    label = "Primary (${primaryList.size})",
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("messages_primary_tab")
                )
                LuminaPillChip(
                    label = if (requestList.isNotEmpty()) "Requests (${requestList.size})" else "Requests",
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("messages_requests_tab")
                )
            }

            if (filteredThreads.isEmpty()) {
                EmptyOrErrorCard(
                    title = if (selectedTab == 0) "No message threads yet" else "No message requests",
                    subtitle = if (selectedTab == 0) {
                        "Tap any user above or press New Chat to start a real-time Firestore conversation."
                    } else {
                        "Messages from accounts you don't follow appear here for review."
                    },
                    actionLabel = if (selectedTab == 0) "Start a Conversation" else null,
                    onAction = if (selectedTab == 0) ({ showNewChatPicker = true }) else null
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("chat_thread_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredThreads, key = { it.id }) { conv ->
                        val peerId = conv.participantIds.firstOrNull { it != currentUserId }
                            ?: conv.participantIds.firstOrNull().orEmpty()
                        val peerUser = userById[peerId]
                        val peerUsername = peerUser?.username
                            ?: conv.participantUsernames.firstOrNull { it != currentUserProfile.username }
                            ?: conv.participantUsernames.firstOrNull()
                            ?: "mitr_user"
                        val peerDisplayName = peerUser?.displayName?.ifBlank { "@$peerUsername" } ?: "@$peerUsername"
                        val threadTimeText = remember(conv.updatedAt, conv.createdAt) {
                            formatChatTimestamp(conv.updatedAt ?: conv.createdAt)
                        }
                        val hasUnread = conv.unreadCount > 0 && conv.lastSenderId != currentUserId

                        LuminaGlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onMarkConversationRead(conv.id)
                                    activeConversationId = conv.id
                                }
                                .testTag("conversation_item_${conv.id}"),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                                    .testTag("chat_thread_item_${conv.id}"),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        UserAvatarBadge(
                                            avatarUrl = peerUser?.avatarUrl ?: "",
                                            displayName = peerDisplayName,
                                            size = 52.dp,
                                            showBorder = true
                                        )
                                        if (showActivityStatus) {
                                            Box(
                                                modifier = Modifier
                                                    .size(12.dp)
                                                    .clip(CircleShape)
                                                    .background(LuminaEmerald)
                                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = peerDisplayName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onSurface,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                VerifiedBadgeIcon(size = 14.dp)
                                            }
                                            Text(
                                                text = threadTimeText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (hasUnread) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier.testTag("chat_thread_timestamp_${conv.id}")
                                            )
                                        }
                                        Text(
                                            text = "@$peerUsername",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            val previewPrefix = if (conv.lastSenderId == currentUserId) "You: " else ""
                                            Text(
                                                text = "$previewPrefix${conv.lastMessageText}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = if (hasUnread) FontWeight.Bold else FontWeight.Normal,
                                                color = if (hasUnread) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (hasUnread) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Badge(containerColor = Color(0xFFE1306C)) {
                                                    Text(
                                                        text = "${conv.unreadCount}",
                                                        color = Color.White,
                                                        modifier = Modifier.padding(horizontal = 5.dp)
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
            }
        }
    }

    if (showNewChatPicker) {
        AlertDialog(
            onDismissRequest = { showNewChatPicker = false },
            title = { Text("Start New Conversation", fontWeight = FontWeight.ExtraBold) },
            text = {
                if (otherUsers.isEmpty()) {
                    Text("No other discoverable accounts registered yet.")
                } else {
                    LazyColumn(
                        modifier = Modifier.height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(otherUsers, key = { it.userId }) { user ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable {
                                        showNewChatPicker = false
                                        onStartConversationWithUser(user)
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatarBadge(
                                    avatarUrl = user.avatarUrl,
                                    displayName = user.displayName,
                                    size = 42.dp,
                                    showBorder = true
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        user.displayName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        "@${user.username}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = MitrIcons.PaperPlaneSend,
                                    contentDescription = "Message",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNewChatPicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun MessagesScreen(
    currentUserId: String,
    currentUserProfile: UserProfile = UserProfile(),
    conversations: List<Conversation>,
    discoverableUsers: List<UserProfile>,
    readReceiptsEnabled: Boolean,
    showActivityStatus: Boolean,
    notificationBadgeCount: Int = 0,
    initialConversationId: String? = null,
    onConsumeInitialConversation: () -> Unit = {},
    observeMessages: (String) -> Flow<List<DirectMessage>>,
    onMarkConversationRead: (String) -> Unit = {},
    onSendMessage: (Conversation, String, String, String, String) -> Unit,
    onDeleteMessage: (String, String) -> Unit,
    onAcceptOrDeclineRequest: (String, Boolean) -> Unit,
    onStartConversationWithUser: (UserProfile) -> Unit,
    onBlockUser: (UserProfile) -> Unit,
    onReportConversation: (String, String) -> Unit,
    onOpenSearch: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenUserProfile: (String) -> Unit = {}
) {
    ChatListScreen(
        currentUserId = currentUserId,
        currentUserProfile = currentUserProfile,
        conversations = conversations,
        discoverableUsers = discoverableUsers,
        readReceiptsEnabled = readReceiptsEnabled,
        showActivityStatus = showActivityStatus,
        notificationBadgeCount = notificationBadgeCount,
        initialConversationId = initialConversationId,
        onConsumeInitialConversation = onConsumeInitialConversation,
        observeMessages = observeMessages,
        onMarkConversationRead = onMarkConversationRead,
        onSendMessage = onSendMessage,
        onDeleteMessage = onDeleteMessage,
        onAcceptOrDeclineRequest = onAcceptOrDeclineRequest,
        onStartConversationWithUser = onStartConversationWithUser,
        onBlockUser = onBlockUser,
        onReportConversation = onReportConversation,
        onOpenSearch = onOpenSearch,
        onOpenNotifications = onOpenNotifications,
        onOpenUserProfile = onOpenUserProfile
    )
}

@Composable
private fun IndividualChatView(
    currentUserId: String,
    conversation: Conversation,
    peerProfile: UserProfile?,
    readReceiptsEnabled: Boolean,
    showActivityStatus: Boolean,
    observeMessages: (String) -> Flow<List<DirectMessage>>,
    onSendMessage: (String, String) -> Unit,
    onDeleteMessage: (String) -> Unit,
    onAcceptOrDeclineRequest: (Boolean) -> Unit,
    onReport: (String) -> Unit,
    onOpenPeerProfile: () -> Unit = {},
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    val messages by observeMessages(conversation.id).collectAsState(initial = emptyList())
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var searchInChat by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var replyingToMsg by remember { mutableStateOf<DirectMessage?>(null) }
    var showReportDialog by remember { mutableStateOf(false) }

    val filteredMessages = remember(messages, searchInChat) {
        if (searchInChat.isBlank()) messages
        else messages.filter { it.text.contains(searchInChat, ignoreCase = true) }
    }

    LaunchedEffect(filteredMessages.size) {
        if (filteredMessages.isNotEmpty()) {
            listState.animateScrollToItem(filteredMessages.lastIndex)
        }
    }

    val headerTitle = peerProfile?.displayName?.ifBlank {
        conversation.participantUsernames.joinToString(" & ") { "@$it" }
    } ?: conversation.participantUsernames.joinToString(" & ") { "@$it" }

    val headerSubtitle = peerProfile?.username?.let { "@$it" }
        ?: conversation.participantUsernames.joinToString(" • ") { "@$it" }

    AuroraAmbientBackground(intensity = 0.06f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .testTag("realtime_chat_screen")
        ) {
            // Chat Header with Peer Avatar & Online Status
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.testTag("chat_back_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { onOpenPeerProfile() }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                UserAvatarBadge(
                                    avatarUrl = peerProfile?.avatarUrl ?: "",
                                    displayName = headerTitle,
                                    size = 40.dp,
                                    showBorder = true
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = headerTitle,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        VerifiedBadgeIcon(size = 14.dp)
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (showActivityStatus) {
                                            Box(
                                                modifier = Modifier
                                                    .size(7.dp)
                                                    .clip(CircleShape)
                                                    .background(LuminaEmerald)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = "$headerSubtitle • Active now",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = LuminaEmerald
                                            )
                                        } else {
                                            Text(
                                                text = headerSubtitle,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Row {
                            IconButton(onClick = { isSearching = !isSearching }) {
                                Icon(Icons.Default.Search, contentDescription = "Search in conversation")
                            }
                            IconButton(onClick = { showReportDialog = true }) {
                                Icon(Icons.Default.Flag, contentDescription = "Report Conversation")
                            }
                        }
                    }

                    if (isSearching) {
                        OutlinedTextField(
                            value = searchInChat,
                            onValueChange = { searchInChat = it },
                            placeholder = { Text("Search messages in this chat...") },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            // Message Request Banner if status == REQUESTED
            if (conversation.status == "REQUESTED" && conversation.initiatorId != currentUserId) {
                LuminaGlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Message Request",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Accepting moves this conversation to your Primary inbox.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Button(onClick = { onAcceptOrDeclineRequest(true) }) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Accept")
                            }
                            OutlinedButton(onClick = {
                                onAcceptOrDeclineRequest(false)
                                onBack()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Decline")
                            }
                        }
                    }
                }
            }

            // Real-Time Messages List with Text & Timestamp Display
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("chat_message_list"),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (filteredMessages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No messages yet. Say hello to start the real-time chat!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredMessages, key = { it.id }) { msg ->
                        val isMine = msg.senderId == currentUserId
                        val bubbleShape = RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomStart = if (isMine) 20.dp else 4.dp,
                            bottomEnd = if (isMine) 4.dp else 20.dp
                        )
                        val timeStr = remember(msg.createdAt) {
                            formatChatTimestamp(msg.createdAt)
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("chat_message_bubble_${msg.id}"),
                            horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(bubbleShape)
                                    .background(
                                        if (isMine) {
                                            LuminaPrismGradient
                                        } else {
                                            Brush.linearGradient(
                                                listOf(
                                                    MaterialTheme.colorScheme.surface,
                                                    MaterialTheme.colorScheme.surfaceVariant
                                                )
                                            )
                                        }
                                    )
                                    .border(
                                        width = if (isMine) 0.dp else 1.dp,
                                        color = if (isMine) Color.Transparent else MaterialTheme.colorScheme.outline,
                                        shape = bubbleShape
                                    )
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Column {
                                    if (!isMine && msg.senderUsername.isNotBlank()) {
                                        Text(
                                            text = "@${msg.senderUsername}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                    if (msg.replyToMessageId.isNotBlank()) {
                                        Text(
                                            text = "↪ Replying to message",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isMine) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                    if (msg.mediaUrl.isNotBlank()) {
                                        AsyncImage(
                                            model = msg.mediaUrl,
                                            contentDescription = "Shared media",
                                            modifier = Modifier
                                                .size(width = 210.dp, height = 150.dp)
                                                .clip(RoundedCornerShape(14.dp))
                                                .padding(bottom = 6.dp),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Text(
                                        text = msg.text,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = if (isMine) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.testTag("chat_message_text_${msg.id}")
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.padding(top = 4.dp)
                                    ) {
                                        Text(
                                            text = timeStr,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isMine) Color.White.copy(alpha = 0.82f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.testTag("chat_message_timestamp_${msg.id}")
                                        )
                                        if (isMine && readReceiptsEnabled) {
                                            Icon(
                                                imageVector = Icons.Default.DoneAll,
                                                contentDescription = msg.deliveryStatus,
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Row {
                                TextButton(onClick = { replyingToMsg = msg }) {
                                    Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Reply", style = MaterialTheme.typography.labelSmall)
                                }
                                if (isMine) {
                                    TextButton(onClick = { onDeleteMessage(msg.id) }) {
                                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Unsend", style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Message Composer Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    if (replyingToMsg != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Replying to: ${replyingToMsg!!.text.take(40)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(onClick = { replyingToMsg = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel Reply", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it.take(1800) },
                            placeholder = { Text("Write a message...") },
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_message_input"),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    onSendMessage(inputText, replyingToMsg?.id ?: "")
                                    inputText = ""
                                    replyingToMsg = null
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(LuminaPrismGradient)
                                .testTag("chat_send_button")
                        ) {
                            Icon(
                                imageVector = MitrIcons.PaperPlaneSend,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text("Report Conversation") },
            text = {
                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it.take(400) },
                    label = { Text("Reason") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onReport(reason.ifBlank { "Harassment or spam in messages" })
                    showReportDialog = false
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * NotificationScreen displaying a list of interactions (likes, comments, follows, collabs/mentions)
 * sorted by timestamp descending, backed by Firestore 'notifications' collection.
 */
@Composable
fun NotificationScreen(
    currentUserId: String,
    notifications: List<SocialNotificationItem>,
    discoverableUsers: List<UserProfile>,
    onToggleFollowUser: (UserProfile) -> Unit,
    onRespondFollowRequest: (Follow, Boolean) -> Unit,
    onRespondCollabInvite: (SocialNotificationItem, Boolean) -> Unit = { _, _ -> },
    onOpenUserProfile: (String) -> Unit,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(NotificationCategory.ALL) }
    val userById = remember(discoverableUsers) { discoverableUsers.associateBy { it.userId } }
    val sortedNotifications = remember(notifications) {
        notifications.sortedByDescending { it.timestampMillis }
    }

    val likesCount = remember(sortedNotifications) {
        sortedNotifications.count { it.category == NotificationCategory.LIKES }
    }
    val followsCount = remember(sortedNotifications) {
        sortedNotifications.count { it.category == NotificationCategory.FOLLOWS }
    }
    val commentsCount = remember(sortedNotifications) {
        sortedNotifications.count { it.category == NotificationCategory.COMMENTS }
    }
    val collabsCount = remember(sortedNotifications) {
        sortedNotifications.count { it.category == NotificationCategory.COLLABS }
    }

    val filteredNotifications = remember(sortedNotifications, selectedCategory) {
        if (selectedCategory == NotificationCategory.ALL) {
            sortedNotifications
        } else {
            sortedNotifications.filter { it.category == selectedCategory }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 8.dp)
            .testTag("notification_screen")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(LuminaPrismGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Activity & Notifications",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Likes, comments, follows & collaborations sorted by time",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (onClose != null) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close Notifications")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Pills: All, Likes, Follows, Comments, Collabs & Mentions
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                LuminaPillChip(
                    label = "All (${sortedNotifications.size})",
                    selected = selectedCategory == NotificationCategory.ALL,
                    onClick = { selectedCategory = NotificationCategory.ALL },
                    modifier = Modifier.testTag("notifications_tab_all")
                )
            }
            item {
                LuminaPillChip(
                    label = "Likes ($likesCount)",
                    selected = selectedCategory == NotificationCategory.LIKES,
                    leadingIcon = Icons.Default.Favorite,
                    onClick = { selectedCategory = NotificationCategory.LIKES },
                    modifier = Modifier.testTag("notifications_tab_likes")
                )
            }
            item {
                LuminaPillChip(
                    label = "Followers ($followsCount)",
                    selected = selectedCategory == NotificationCategory.FOLLOWS,
                    leadingIcon = Icons.Default.PersonAdd,
                    onClick = { selectedCategory = NotificationCategory.FOLLOWS },
                    modifier = Modifier.testTag("notifications_tab_follows")
                )
            }
            item {
                LuminaPillChip(
                    label = "Comments ($commentsCount)",
                    selected = selectedCategory == NotificationCategory.COMMENTS,
                    leadingIcon = Icons.Outlined.ModeComment,
                    onClick = { selectedCategory = NotificationCategory.COMMENTS },
                    modifier = Modifier.testTag("notifications_tab_comments")
                )
            }
            item {
                LuminaPillChip(
                    label = "Collabs & Tags ($collabsCount)",
                    selected = selectedCategory == NotificationCategory.COLLABS,
                    leadingIcon = Icons.Default.AlternateEmail,
                    onClick = { selectedCategory = NotificationCategory.COLLABS },
                    modifier = Modifier.testTag("notifications_tab_collabs")
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

        if (filteredNotifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 42.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = when (selectedCategory) {
                            NotificationCategory.LIKES -> Icons.Default.Favorite
                            NotificationCategory.FOLLOWS -> Icons.Default.PersonAdd
                            NotificationCategory.COMMENTS -> Icons.Outlined.ModeComment
                            NotificationCategory.COLLABS -> Icons.Default.AlternateEmail
                            NotificationCategory.ALL -> Icons.Default.Notifications
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Text(
                        text = when (selectedCategory) {
                            NotificationCategory.LIKES -> "No likes on your posts yet"
                            NotificationCategory.FOLLOWS -> "No new followers yet"
                            NotificationCategory.COMMENTS -> "No comments on your posts yet"
                            NotificationCategory.COLLABS -> "No collaborations or mentions yet"
                            NotificationCategory.ALL -> "No notifications yet"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "When people like your posts, follow you, comment, or collaborate with you, they will appear right here in real time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
                    .testTag("notifications_list"),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredNotifications, key = { it.id }) { item ->
                    val isPendingCollabInvite = item.category == NotificationCategory.COLLABS &&
                        item.targetId.isNotBlank() &&
                        item.message.contains("invited you to collaborate", ignoreCase = true)

                    LuminaGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onClose?.invoke()
                                onOpenUserProfile(item.actorUserId)
                            }
                            .testTag("notification_item_${item.id}"),
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
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(contentAlignment = Alignment.BottomEnd) {
                                        UserAvatarBadge(
                                            avatarUrl = item.actorAvatarUrl,
                                            displayName = item.actorDisplayName,
                                            size = 46.dp,
                                            showBorder = true
                                        )
                                        val badgeColor = when (item.category) {
                                            NotificationCategory.LIKES -> Color(0xFFE1306C)
                                            NotificationCategory.FOLLOWS -> LuminaCoral
                                            NotificationCategory.COMMENTS -> LuminaViolet
                                            NotificationCategory.COLLABS -> LuminaAmber
                                            NotificationCategory.ALL -> LuminaCoral
                                        }
                                        val badgeIcon = when (item.category) {
                                            NotificationCategory.LIKES -> Icons.Default.Favorite
                                            NotificationCategory.FOLLOWS -> Icons.Default.PersonAdd
                                            NotificationCategory.COMMENTS -> Icons.Outlined.ModeComment
                                            NotificationCategory.COLLABS -> Icons.Default.AlternateEmail
                                            NotificationCategory.ALL -> Icons.Default.Notifications
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(badgeColor)
                                                .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = badgeIcon,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = buildAnnotatedString {
                                                withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold)) {
                                                    append("@${item.actorUsername} ")
                                                }
                                                append(item.message)
                                            },
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (item.detailText.isNotBlank()) {
                                            Text(
                                                text = item.detailText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                        Text(
                                            text = formatNotificationTimeMillis(item.timestampMillis),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(top = 3.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Right-Side Context Action: Follow Request buttons, Follow Back pill, or Post Thumbnail
                                when {
                                    item.category == NotificationCategory.FOLLOWS && item.followRecord?.status == "PENDING" -> {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Button(
                                                onClick = { onRespondFollowRequest(item.followRecord, true) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text("Accept", style = MaterialTheme.typography.labelSmall)
                                            }
                                            OutlinedButton(
                                                onClick = { onRespondFollowRequest(item.followRecord, false) },
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                            ) {
                                                Text("Decline", style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }

                                    item.category == NotificationCategory.FOLLOWS && item.actorUserId != currentUserId -> {
                                        val targetProfile = userById[item.actorUserId] ?: UserProfile(
                                            id = item.actorUserId,
                                            userId = item.actorUserId,
                                            username = item.actorUsername,
                                            displayName = item.actorDisplayName,
                                            avatarUrl = item.actorAvatarUrl
                                        )
                                        LuminaPillChip(
                                            label = if (item.isFollowingBack) "Following" else "Follow Back",
                                            selected = !item.isFollowingBack,
                                            onClick = { onToggleFollowUser(targetProfile) }
                                        )
                                    }

                                    item.targetMediaUrl.isNotBlank() -> {
                                        AsyncImage(
                                            model = item.targetMediaUrl,
                                            contentDescription = "Post thumbnail",
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }

                            // Instagram-style Collab Invitation Action Row
                            if (isPendingCollabInvite) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Button(
                                        onClick = { onRespondCollabInvite(item, true) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("accept_collab_invite_${item.id}")
                                    ) {
                                        Text(
                                            text = "Accept Collab",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { onRespondCollabInvite(item, false) },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("decline_collab_invite_${item.id}")
                                    ) {
                                        Text(
                                            text = "Decline",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            } else if (item.category == NotificationCategory.COLLABS && item.targetId.isNotBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = "Active Co-Author • Shown on your Profile & Feed",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                    TextButton(
                                        onClick = { onRespondCollabInvite(item, false) }
                                    ) {
                                        Text(
                                            text = "Remove from Profile",
                                            style = MaterialTheme.typography.labelSmall
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
}

/**
 * Interactive Bell Notifications Bottom Sheet wrapping NotificationScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsBottomSheet(
    currentUserId: String,
    notifications: List<SocialNotificationItem>,
    discoverableUsers: List<UserProfile>,
    onToggleFollowUser: (UserProfile) -> Unit,
    onRespondFollowRequest: (Follow, Boolean) -> Unit,
    onRespondCollabInvite: (SocialNotificationItem, Boolean) -> Unit = { _, _ -> },
    onOpenUserProfile: (String) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("notifications_bottom_sheet")
    ) {
        NotificationScreen(
            currentUserId = currentUserId,
            notifications = notifications,
            discoverableUsers = discoverableUsers,
            onToggleFollowUser = onToggleFollowUser,
            onRespondFollowRequest = onRespondFollowRequest,
            onRespondCollabInvite = onRespondCollabInvite,
            onOpenUserProfile = onOpenUserProfile,
            onClose = onDismiss
        )
    }
}

@Composable
fun SearchAndDiscoveryScreen(
    currentUserId: String,
    discoverableUsers: List<UserProfile>,
    feedPostsState: UiState<List<Post>>,
    reelsState: UiState<List<Reel>>,
    outgoingFollows: List<Follow>,
    activityLogs: List<UserActivityLog>,
    firestoreSearchResult: FirestoreFeedSearchResult = FirestoreFeedSearchResult(),
    onExecuteFirestoreSearch: (String) -> Unit = {},
    onRecordSearch: (String) -> Unit,
    onDeleteSearchItem: (String) -> Unit,
    onToggleFollow: (UserProfile) -> Unit,
    onMessageUser: (UserProfile) -> Unit = {},
    onOpenUserProfile: (String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filterCategory by remember { mutableStateOf("ALL") } // ALL, ACCOUNTS, HASHTAGS, POSTS, REELS

    val recentSearches = remember(activityLogs) {
        activityLogs.filter { it.activityType == "SEARCH" }.take(8)
    }

    val posts = (feedPostsState as? UiState.Success)?.data ?: emptyList()
    val reels = (reelsState as? UiState.Success)?.data ?: emptyList()

    val cleanQuery = remember(query) { query.trim().removePrefix("#").removePrefix("@") }

    val matchingUsers = remember(discoverableUsers, firestoreSearchResult, posts, reels, cleanQuery) {
        val byId = LinkedHashMap<String, UserProfile>()
        if (cleanQuery.isBlank()) {
            discoverableUsers.forEach { u ->
                val uid = u.userId.ifBlank { u.id }
                if (uid.isNotBlank()) byId[uid] = u.copy(id = uid, userId = uid)
            }
        } else {
            firestoreSearchResult.matchedUsers.forEach { u ->
                val uid = u.userId.ifBlank { u.id }
                if (uid.isNotBlank() && (
                        u.username.contains(cleanQuery, ignoreCase = true) ||
                            u.displayName.contains(cleanQuery, ignoreCase = true) ||
                            u.bio.contains(cleanQuery, ignoreCase = true) ||
                            u.userId.equals(cleanQuery, ignoreCase = true)
                        )
                ) {
                    byId[uid] = u.copy(id = uid, userId = uid)
                }
            }
            discoverableUsers.forEach { u ->
                val uid = u.userId.ifBlank { u.id }
                if (uid.isNotBlank() && !byId.containsKey(uid) && (
                        u.username.contains(cleanQuery, ignoreCase = true) ||
                            u.displayName.contains(cleanQuery, ignoreCase = true) ||
                            u.bio.contains(cleanQuery, ignoreCase = true) ||
                            u.userId.equals(cleanQuery, ignoreCase = true)
                        )
                ) {
                    byId[uid] = u.copy(id = uid, userId = uid)
                }
            }
            posts.forEach { p ->
                val uid = p.authorId
                if (uid.isNotBlank() && !byId.containsKey(uid) && (
                        p.authorUsername.contains(cleanQuery, ignoreCase = true) ||
                            p.authorDisplayName.contains(cleanQuery, ignoreCase = true)
                        )
                ) {
                    val uname = p.authorUsername.ifBlank { "user_${uid.take(6)}" }
                    byId[uid] = UserProfile(
                        id = uid,
                        userId = uid,
                        username = uname,
                        displayName = p.authorDisplayName.ifBlank { uname },
                        avatarUrl = p.authorAvatarUrl,
                        bio = "Creator on Mitr"
                    )
                }
            }
            reels.forEach { r ->
                val uid = r.authorId
                if (uid.isNotBlank() && !byId.containsKey(uid) && r.authorUsername.contains(cleanQuery, ignoreCase = true)) {
                    val uname = r.authorUsername.ifBlank { "user_${uid.take(6)}" }
                    byId[uid] = UserProfile(
                        id = uid,
                        userId = uid,
                        username = uname,
                        displayName = uname,
                        bio = "Creator on Mitr"
                    )
                }
            }
        }
        byId.values.toList()
    }

    val matchingHashtags = remember(posts, firestoreSearchResult, cleanQuery) {
        if (cleanQuery.isNotBlank() && firestoreSearchResult.matchedHashtags.isNotEmpty()) {
            firestoreSearchResult.matchedHashtags
        } else {
            val counts = mutableMapOf<String, Int>()
            posts.forEach { post ->
                post.hashtags.forEach { tag ->
                    val lower = tag.lowercase(Locale.ROOT)
                    if (cleanQuery.isBlank() || lower.contains(cleanQuery.lowercase(Locale.ROOT))) {
                        counts[lower] = (counts[lower] ?: 0) + 1
                    }
                }
            }
            counts.entries.sortedByDescending { it.value }.map { it.key to it.value }
        }
    }

    val matchingPosts = remember(posts, firestoreSearchResult, cleanQuery) {
        if (cleanQuery.isBlank()) posts
        else {
            val localMatches = posts.filter {
                it.caption.contains(cleanQuery, ignoreCase = true) ||
                    it.authorUsername.contains(cleanQuery, ignoreCase = true) ||
                    it.authorDisplayName.contains(cleanQuery, ignoreCase = true) ||
                    it.locationTag.contains(cleanQuery, ignoreCase = true) ||
                    it.hashtags.any { tag -> tag.contains(cleanQuery, ignoreCase = true) }
            }
            (firestoreSearchResult.matchedPosts + localMatches).distinctBy { it.id }
        }
    }

    val matchingReels = remember(reels, cleanQuery) {
        if (cleanQuery.isBlank()) reels
        else reels.filter {
            it.caption.contains(cleanQuery, ignoreCase = true) ||
                it.authorUsername.contains(cleanQuery, ignoreCase = true)
        }
    }

    AuroraAmbientBackground(intensity = 0.08f) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Discover on Mitr",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Explore real people, #hashtags & posts across Mitr",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    onExecuteFirestoreSearch(it)
                },
                placeholder = { Text("Search @usernames, #hashtags, or post content...") },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (query.isNotBlank()) {
                        IconButton(onClick = {
                            query = ""
                            onExecuteFirestoreSearch("")
                        }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_query_input")
            )

            if (firestoreSearchResult.isSearching && query.isNotBlank()) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (query.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onRecordSearch(query) }) {
                        Text("Save search to history")
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(listOf("ALL", "ACCOUNTS", "HASHTAGS", "POSTS", "REELS")) { tab ->
                    LuminaPillChip(
                        label = tab,
                        selected = filterCategory == tab,
                        onClick = { filterCategory = tab }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("search_results_list"),
                contentPadding = PaddingValues(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (query.isBlank() && recentSearches.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recent Searches",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(recentSearches, key = { it.id }) { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.History,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(item.summary, style = MaterialTheme.typography.bodyMedium)
                            }
                            IconButton(onClick = { onDeleteSearchItem(item.id) }) {
                                Icon(Icons.Default.Close, contentDescription = "Remove search", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                    item { HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)) }
                }

                // Hashtags Section
                if ((filterCategory == "ALL" || filterCategory == "HASHTAGS") && matchingHashtags.isNotEmpty()) {
                    item {
                        Text(
                            text = "Trending & Matching Hashtags (${matchingHashtags.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    item {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(matchingHashtags, key = { it.first }) { (tag, count) ->
                                LuminaPillChip(
                                    label = "#$tag ($count)",
                                    selected = query.equals("#$tag", ignoreCase = true),
                                    onClick = { query = "#$tag" }
                                )
                            }
                        }
                    }
                }

                // Accounts Section
                if (filterCategory == "ALL" || filterCategory == "ACCOUNTS") {
                    item {
                        Text(
                            text = "Accounts (${matchingUsers.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    if (matchingUsers.isEmpty()) {
                        item {
                            Text(
                                text = "No matching accounts found.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(matchingUsers, key = { it.userId.ifBlank { it.id.ifBlank { it.username } } }) { user ->
                            val targetUid = user.userId.ifBlank { user.id.ifBlank { user.username } }
                            val followRecord = outgoingFollows.find { it.followingId == targetUid }
                            LuminaGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenUserProfile(targetUid) },
                                shape = RoundedCornerShape(18.dp)
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
                                            avatarUrl = user.avatarUrl,
                                            displayName = user.displayName,
                                            size = 46.dp,
                                            showBorder = true
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = user.displayName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "@${user.username}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (user.userId != currentUserId) {
                                        val isFollowing = followRecord?.status == "ACCEPTED"
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            IconButton(
                                                onClick = { onMessageUser(user) },
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .testTag("search_message_user_${user.userId}")
                                            ) {
                                                Icon(
                                                    MitrIcons.MessageOutlined,
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
                                                onClick = { onToggleFollow(user) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Posts Discovery Section
                if (filterCategory == "ALL" || filterCategory == "POSTS") {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Posts & Hashtags (${matchingPosts.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    if (matchingPosts.isEmpty()) {
                        item {
                            Text(
                                text = "No matching posts found.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(matchingPosts, key = { it.id }) { post ->
                            LuminaGlassCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenUserProfile(post.authorId) },
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "@${post.authorUsername}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = post.caption,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${post.likeCount} likes • ${post.commentCount} comments",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Reels Discovery Section
                if (filterCategory == "ALL" || filterCategory == "REELS") {
                    item {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Discoverable Reels (${matchingReels.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    if (matchingReels.isEmpty()) {
                        item {
                            Text(
                                text = "No matching reels found.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(matchingReels, key = { it.id }) { reel ->
                            LuminaGlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "@${reel.authorUsername}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = reel.caption,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${reel.viewCount} views • ${reel.audioTrackName}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(top = 4.dp)
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

/**
 * Live @mention suggestion strip that appears whenever the user types '@...' in a Bio, Story, Reel, or Post text field,
 * or wants to quickly mention suggested related accounts.
 */
@Composable
fun InlineMentionSuggestionBar(
    currentText: String,
    currentUserId: String,
    discoverableUsers: List<UserProfile>,
    onInsertMention: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeAtToken = remember(currentText) {
        val lastAt = currentText.lastIndexOf('@')
        if (lastAt >= 0) {
            val afterAt = currentText.substring(lastAt + 1)
            if (!afterAt.contains(' ') && !afterAt.contains('\n')) {
                afterAt.lowercase(Locale.ROOT)
            } else null
        } else null
    }

    val suggestedUsers = remember(discoverableUsers, currentUserId, activeAtToken) {
        val peers = discoverableUsers.filter { it.userId != currentUserId }
        if (activeAtToken == null) {
            peers.take(8)
        } else {
            peers.filter {
                it.username.lowercase(Locale.ROOT).contains(activeAtToken) ||
                    it.displayName.lowercase(Locale.ROOT).contains(activeAtToken)
            }.take(10)
        }
    }

    if (suggestedUsers.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AlternateEmail,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = if (activeAtToken != null) {
                    "Matching people for @$activeAtToken (tap to mention)"
                } else {
                    "Quick @Mention people (tap to insert in text)"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(suggestedUsers, key = { it.userId }) { user ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.75f),
                    modifier = Modifier
                        .clickable {
                            val lastAt = currentText.lastIndexOf('@')
                            val newText = if (lastAt >= 0 && activeAtToken != null) {
                                currentText.substring(0, lastAt) + "@${user.username} "
                            } else {
                                val sep = if (currentText.isBlank() || currentText.endsWith(" ")) "" else " "
                                "$currentText$sep@${user.username} "
                            }
                            onInsertMention(newText)
                        }
                        .testTag("mention_suggestion_${user.username}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UserAvatarBadge(
                            avatarUrl = user.avatarUrl,
                            displayName = user.displayName,
                            size = 22.dp,
                            showBorder = false
                        )
                        Column {
                            Text(
                                text = "@${user.username}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = user.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive Collab & Tag People selector card for Posts, Stories, and Reels.
 * Suggests related people while searching or collaborating, and lets the author pick collaborators & tags.
 */
@Composable
fun CollabAndTagPeopleSelectorCard(
    currentUserId: String,
    discoverableUsers: List<UserProfile>,
    selectedCollaborators: List<String>,
    onCollaboratorsChanged: (List<String>) -> Unit,
    selectedTags: List<String>,
    onTagsChanged: (List<String>) -> Unit,
    contentLabel: String = "Post"
) {
    var searchQuery by remember { mutableStateOf("") }
    var activePickerMode by remember { mutableStateOf("COLLAB") } // COLLAB or TAG

    val relatedSuggestions = remember(discoverableUsers, currentUserId, searchQuery) {
        val clean = searchQuery.trim().removePrefix("@").lowercase(Locale.ROOT)
        val peers = discoverableUsers.filter { it.userId != currentUserId }
        if (clean.isBlank()) {
            peers
        } else {
            peers.filter {
                it.username.lowercase(Locale.ROOT).contains(clean) ||
                    it.displayName.lowercase(Locale.ROOT).contains(clean) ||
                    it.bio.lowercase(Locale.ROOT).contains(clean)
            }
        }
    }

    LuminaGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("collab_and_tag_card"),
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = "Collab & Tag People on $contentLabel",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Collaborators appear in the header & share this $contentLabel in their feed",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LuminaPillChip(
                    label = "Invite Collaborators (${selectedCollaborators.size})",
                    selected = activePickerMode == "COLLAB",
                    onClick = { activePickerMode = "COLLAB" },
                    modifier = Modifier.testTag("mode_collab_chip")
                )
                LuminaPillChip(
                    label = "Tag / Mention (${selectedTags.size})",
                    selected = activePickerMode == "TAG",
                    onClick = { activePickerMode = "TAG" },
                    modifier = Modifier.testTag("mode_tag_chip")
                )
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = {
                    Text(
                        if (activePickerMode == "COLLAB") {
                            "Search person to collab with (@username or name)"
                        } else {
                            "Search person to tag or mention (@username)"
                        }
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("collab_search_input")
            )

            Text(
                text = if (searchQuery.isBlank()) "Suggested People to ${if (activePickerMode == "COLLAB") "Collab With" else "Tag"}" else "Matching People (${relatedSuggestions.size})",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            if (relatedSuggestions.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(relatedSuggestions.take(12), key = { it.userId }) { user ->
                        val uname = user.username.lowercase(Locale.ROOT)
                        val isSelected = if (activePickerMode == "COLLAB") {
                            selectedCollaborators.contains(uname)
                        } else {
                            selectedTags.contains(uname)
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                            ),
                            modifier = Modifier
                                .clickable {
                                    if (activePickerMode == "COLLAB") {
                                        val updated = if (isSelected) {
                                            selectedCollaborators - uname
                                        } else {
                                            (selectedCollaborators + uname).distinct()
                                        }
                                        onCollaboratorsChanged(updated)
                                    } else {
                                        val updated = if (isSelected) {
                                            selectedTags - uname
                                        } else {
                                            (selectedTags + uname).distinct()
                                        }
                                        onTagsChanged(updated)
                                    }
                                }
                                .testTag("collab_suggest_user_${user.username}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                UserAvatarBadge(
                                    avatarUrl = user.avatarUrl,
                                    displayName = user.displayName,
                                    size = 28.dp,
                                    showBorder = isSelected
                                )
                                Column {
                                    Text(
                                        text = user.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "@${user.username}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.AddCircleOutline,
                                    contentDescription = null,
                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (selectedCollaborators.isNotEmpty() || selectedTags.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (selectedCollaborators.isNotEmpty()) {
                        Text(
                            text = "Collaborating with: " + selectedCollaborators.joinToString(", ") { "@$it" },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (selectedTags.isNotEmpty()) {
                        Text(
                            text = "Tagged: " + selectedTags.joinToString(", ") { "@$it" },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
