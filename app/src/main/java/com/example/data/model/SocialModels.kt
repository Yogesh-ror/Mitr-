package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class MusicTrack(
    val trackId: String,
    val title: String,
    val artist: String,
    val albumName: String = "",
    val coverUrl: String = "",
    val audioUrl: String = "",
    val durationMs: Long = 30_000L,
    val genre: String = "Music",
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 15_000L
) {
    val trimmedDurationSeconds: Long
        get() = ((trimEndMs - trimStartMs).coerceAtLeast(1000L) / 1000L)
}

@IgnoreExtraProperties
data class SavedMusic(
    @DocumentId val id: String = "",
    val userId: String = "",
    val trackId: String = "",
    val title: String = "",
    val artist: String = "",
    val coverUrl: String = "",
    val audioUrl: String = "",
    val durationMs: Long = 30_000L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 15_000L,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toMusicTrack(): MusicTrack = MusicTrack(
        trackId = trackId,
        title = title,
        artist = artist,
        coverUrl = coverUrl,
        audioUrl = audioUrl,
        durationMs = durationMs,
        trimStartMs = trimStartMs,
        trimEndMs = trimEndMs
    )
}

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val website: String = "",
    val avatarUrl: String = "",
    val isPrivate: Boolean = false,
    val isVerified: Boolean = false,
    val subscriptionPlan: String = "FREE", // FREE, BLUE_TICK_300, FOUNDER_VIP
    val postCount: Long = 0L,
    val followerCount: Long = 0L,
    val followingCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UsernameClaim(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserSettings(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val isPrivateAccount: Boolean = false,
    val requireFollowApproval: Boolean = false,
    val whoCanComment: String = "EVERYONE", // EVERYONE, FOLLOWERS, NO_ONE
    val whoCanMessage: String = "EVERYONE", // EVERYONE, FOLLOWERS, NO_ONE
    val whoCanTag: String = "EVERYONE", // EVERYONE, FOLLOWERS, NO_ONE
    val whoCanMention: String = "EVERYONE", // EVERYONE, FOLLOWERS, NO_ONE
    val whoCanReplyStories: String = "EVERYONE", // EVERYONE, FOLLOWERS, OFF
    val manuallyApproveTags: Boolean = false,
    val showActivityStatus: Boolean = true,
    val readReceiptsEnabled: Boolean = true,
    val hideLikeCounts: Boolean = false,
    val allowStorySharing: Boolean = true,
    val archiveStories: Boolean = true,
    val hiddenWordsCsv: String = "",
    val sensitiveContentControl: String = "STANDARD", // STANDARD, LESS, MORE
    val searchDiscoverability: Boolean = true,
    val pushNotificationsEnabled: Boolean = true,
    val notifyLikes: Boolean = true,
    val notifyComments: Boolean = true,
    val notifyCommentReplies: Boolean = true,
    val notifyFollowers: Boolean = true,
    val notifyFollowRequests: Boolean = true,
    val notifyMentions: Boolean = true,
    val notifyDirectMessages: Boolean = true,
    val notifyMessageRequests: Boolean = true,
    val notifyStoryReplies: Boolean = true,
    val notifyLiveVideos: Boolean = false,
    val notifyReminders: Boolean = true,
    val emailNotificationsEnabled: Boolean = false,
    val quietHoursEnabled: Boolean = false,
    val quietHoursStart: String = "22:00",
    val quietHoursEnd: String = "07:00",
    val dailyTimeLimitMinutes: Long = 60L,
    val breakReminderMinutes: Long = 30L,
    val bedtimeReminderEnabled: Boolean = false,
    val bedtimeStart: String = "23:00",
    val todayUsageMinutes: Long = 0L,
    val weeklyUsageMinutes: Long = 0L,
    val locationAccessEnabled: Boolean = false,
    val allowPostLocation: Boolean = true,
    val showLocationContent: Boolean = true,
    val themeMode: String = "LIGHT", // SYSTEM, LIGHT, DARK
    val dataSaverMode: Boolean = false,
    val autoplayVideos: Boolean = true,
    val highQualityVideo: Boolean = true,
    val reduceMotion: Boolean = false,
    val autoCaptions: Boolean = true,
    val textScalePercent: Long = 100L,
    val accountDeactivated: Boolean = false,
    val isVerifiedSubscriber: Boolean = false,
    val subscriptionPlan: String = "FREE", // FREE, BLUE_TICK_300, FOUNDER_VIP
    val subscriptionPriceInr: Long = 300L,
    val subscriptionActivatedAtMillis: Long = 0L,
    val payoutHolderName: String = "",
    val payoutBankOrUpi: String = "",
    val payoutAccountNumber: String = "",
    val payoutIfscCode: String = "",
    val pendingPayoutBalanceInr: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Post(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorDisplayName: String = "",
    val authorAvatarUrl: String = "",
    val caption: String = "",
    val mediaUrls: List<String> = emptyList(),
    val mediaType: String = "IMAGE", // IMAGE, VIDEO, CAROUSEL
    val locationTag: String = "",
    val taggedUsernames: List<String> = emptyList(),
    val collaboratorUsernames: List<String> = emptyList(),
    val hashtags: List<String> = emptyList(),
    val visibility: String = "PUBLIC", // PUBLIC, FOLLOWERS, CLOSE_FRIENDS
    val commentsAllowed: Boolean = true,
    val likeCount: Long = 0L,
    val commentCount: Long = 0L,
    val likedByUids: List<String> = emptyList(),
    val musicTrackId: String = "",
    val musicTitle: String = "",
    val musicArtist: String = "",
    val musicCoverUrl: String = "",
    val musicUrl: String = "",
    val musicStartMs: Long = 0L,
    val musicEndMs: Long = 15_000L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Comment(
    @DocumentId val id: String = "",
    val targetId: String = "",
    val targetType: String = "POST", // POST, REEL
    val authorId: String = "",
    val authorUsername: String = "",
    val authorDisplayName: String = "",
    val text: String = "",
    val parentCommentId: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Story(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorDisplayName: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "IMAGE", // IMAGE, VIDEO
    val caption: String = "",
    val overlayText: String = "",
    val stickerLabel: String = "",
    val audience: String = "PUBLIC", // PUBLIC, CLOSE_FRIENDS
    val allowedViewerIds: List<String> = emptyList(),
    val viewerIds: List<String> = emptyList(),
    val taggedUsernames: List<String> = emptyList(),
    val collaboratorUsernames: List<String> = emptyList(),
    val musicTrackId: String = "",
    val musicTitle: String = "",
    val musicArtist: String = "",
    val musicCoverUrl: String = "",
    val musicUrl: String = "",
    val musicStartMs: Long = 0L,
    val musicEndMs: Long = 15_000L,
    val isHighlighted: Boolean = false,
    val highlightTitle: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    val expiresAtMillis: Long = 0L
)

@IgnoreExtraProperties
data class Reel(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorUsername: String = "",
    val authorDisplayName: String = "",
    val videoUrl: String = "",
    val caption: String = "",
    val audioTrackName: String = "Original Audio",
    val durationSeconds: Long = 15L,
    val visibility: String = "PUBLIC", // PUBLIC, FOLLOWERS
    val likeCount: Long = 0L,
    val commentCount: Long = 0L,
    val viewCount: Long = 0L,
    val likedByUids: List<String> = emptyList(),
    val taggedUsernames: List<String> = emptyList(),
    val collaboratorUsernames: List<String> = emptyList(),
    val musicTrackId: String = "",
    val musicTitle: String = "",
    val musicArtist: String = "",
    val musicCoverUrl: String = "",
    val musicUrl: String = "",
    val musicStartMs: Long = 0L,
    val musicEndMs: Long = 15_000L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Follow(
    @DocumentId val id: String = "",
    val followerId: String = "",
    val followerUsername: String = "",
    val followingId: String = "",
    val followingUsername: String = "",
    val status: String = "ACCEPTED", // ACCEPTED, PENDING
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class SavedPost(
    @DocumentId val id: String = "",
    val userId: String = "",
    val postId: String = "",
    val targetType: String = "POST", // POST, REEL
    val authorUsername: String = "",
    val captionPreview: String = "",
    val mediaUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Conversation(
    @DocumentId val id: String = "",
    val participantIds: List<String> = emptyList(),
    val participantUsernames: List<String> = emptyList(),
    val initiatorId: String = "",
    val lastMessageText: String = "",
    val lastSenderId: String = "",
    val status: String = "ACTIVE", // ACTIVE, REQUESTED, DECLINED
    val mutedByUids: List<String> = emptyList(),
    val unreadCount: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class DirectMessage(
    @DocumentId val id: String = "",
    val conversationId: String = "",
    val participantIds: List<String> = emptyList(),
    val senderId: String = "",
    val senderUsername: String = "",
    val text: String = "",
    val mediaUrl: String = "",
    val mediaType: String = "NONE", // NONE, IMAGE, VIDEO
    val replyToMessageId: String = "",
    val deliveryStatus: String = "SENT", // SENT, DELIVERED, READ
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserRelation(
    @DocumentId val id: String = "",
    val ownerId: String = "",
    val targetUserId: String = "",
    val targetUsername: String = "",
    val targetDisplayName: String = "",
    val relationType: String = "CLOSE_FRIEND", // CLOSE_FRIEND, BLOCKED, RESTRICTED, MUTED_STORIES
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserActivityLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val activityType: String = "LIKE", // LIKE, COMMENT, REPLY, FOLLOW, SEARCH, STORY_REPLY, POST_CREATED, REEL_CREATED
    val targetId: String = "",
    val summary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ContentReport(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val targetId: String = "",
    val targetType: String = "POST", // POST, REEL, STORY, USER, MESSAGE
    val reason: String = "",
    val status: String = "OPEN", // OPEN, REVIEWED
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class SavedAccountSession(
    val uid: String,
    val email: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String,
    val lastActiveMillis: Long
)

enum class NotificationCategory {
    ALL,
    LIKES,
    FOLLOWS,
    COMMENTS,
    COLLABS
}

@IgnoreExtraProperties
data class FirestoreNotification(
    @DocumentId val id: String = "",
    val recipientId: String = "",
    val actorId: String = "",
    val actorUsername: String = "",
    val actorDisplayName: String = "",
    val actorAvatarUrl: String = "",
    val type: String = "LIKE", // LIKE, COMMENT, FOLLOW, COLLAB, MENTION
    val message: String = "",
    val detailText: String = "",
    val targetId: String = "",
    val targetType: String = "POST", // POST, REEL, STORY, FOLLOW
    val targetMediaUrl: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class SocialNotificationItem(
    val id: String,
    val category: NotificationCategory,
    val actorUserId: String,
    val actorUsername: String,
    val actorDisplayName: String,
    val actorAvatarUrl: String = "",
    val message: String,
    val detailText: String = "",
    val targetId: String = "",
    val targetType: String = "", // POST, REEL, FOLLOW
    val targetMediaUrl: String = "",
    val followRecord: Follow? = null,
    val isFollowingBack: Boolean = false,
    val timestampMillis: Long = 0L
)

data class FirestoreFeedSearchResult(
    val query: String = "",
    val isSearching: Boolean = false,
    val matchedUsers: List<UserProfile> = emptyList(),
    val matchedHashtags: List<Pair<String, Int>> = emptyList(),
    val matchedPosts: List<Post> = emptyList()
)

const val MITR_FOUNDER_VIP_EMAIL = "yogeshkhanchi985@gmail.com"
const val MITR_BLUE_TICK_PRICE_INR = 300L

/**
 * Returns true if the account belongs to the platform founder (yogeshkhanchi985@gmail.com),
 * granting complimentary lifetime Verified Blue Tick + Priority Reach without any payment.
 */
fun isFounderVipAccount(email: String?, username: String? = null): Boolean {
    val cleanEmail = email?.trim()?.lowercase() ?: ""
    val cleanUsername = username?.trim()?.removePrefix("@")?.lowercase() ?: ""
    return cleanEmail == MITR_FOUNDER_VIP_EMAIL ||
        cleanUsername == "yogeshkhanchi985" ||
        cleanUsername == "yogeshkhanchi"
}

/**
 * Checks whether a user profile has an active Verified Blue Tick subscription
 * (either via ₹300/month subscription or complimentary Founder VIP access for yogeshkhanchi985@gmail.com).
 */
fun isUserBlueTickVerified(
    profile: UserProfile?,
    email: String? = null,
    settings: UserSettings? = null
): Boolean {
    if (isFounderVipAccount(email ?: settings?.email, profile?.username)) return true
    if (profile?.isVerified == true) return true
    if (settings?.isVerifiedSubscriber == true) return true
    if (profile?.subscriptionPlan in listOf("BLUE_TICK_300", "FOUNDER_VIP")) return true
    if (settings?.subscriptionPlan in listOf("BLUE_TICK_300", "FOUNDER_VIP")) return true
    if (profile?.website?.contains("verified.mitr", ignoreCase = true) == true) return true
    return false
}

/**
 * Resolves whether an author on a Post, Reel, Story, or Comment has the Verified Blue Tick.
 */
fun isCreatorHandleBlueTickVerified(
    authorId: String,
    authorUsername: String,
    discoverableUsers: List<UserProfile>,
    currentUserId: String = "",
    currentUserEmail: String = "",
    currentUserSettings: UserSettings? = null
): Boolean {
    if (isFounderVipAccount(
            email = if (authorId == currentUserId) currentUserEmail else null,
            username = authorUsername
        )
    ) {
        return true
    }
    if (authorId.isNotBlank() && authorId == currentUserId) {
        if (currentUserSettings?.isVerifiedSubscriber == true ||
            currentUserSettings?.subscriptionPlan in listOf("BLUE_TICK_300", "FOUNDER_VIP")
        ) {
            return true
        }
    }
    val matchedProfile = discoverableUsers.firstOrNull {
        (authorId.isNotBlank() && it.userId == authorId) ||
            it.username.equals(authorUsername.removePrefix("@"), ignoreCase = true)
    }
    return isUserBlueTickVerified(
        profile = matchedProfile,
        email = if (authorId == currentUserId) currentUserEmail else null,
        settings = if (authorId == currentUserId) currentUserSettings else null
    )
}


