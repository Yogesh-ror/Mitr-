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
    val reactionEntries: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    val expiresAtMillis: Long = 0L
)

data class StoryReactionEntry(
    val userId: String,
    val username: String,
    val emoji: String,
    val feelingLabel: String = storyFeelingLabelForEmoji(emoji)
)

@IgnoreExtraProperties
data class StoryReaction(
    @DocumentId val id: String = "",
    val storyId: String = "",
    val storyAuthorId: String = "",
    val userId: String = "",
    val username: String = "",
    val displayName: String = "",
    val emoji: String = "❤️",
    val feelingLabel: String = "Love",
    val replyText: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

val STORY_FEELING_REACTIONS: List<Pair<String, String>> = listOf(
    "❤️" to "Love",
    "🔥" to "Lit",
    "😂" to "Haha",
    "😮" to "Wow",
    "😢" to "Sad",
    "👏" to "Applause",
    "🎉" to "Celebrate",
    "💯" to "100%"
)

fun storyFeelingLabelForEmoji(emoji: String): String {
    val clean = emoji.trim()
    return STORY_FEELING_REACTIONS.firstOrNull { it.first == clean }?.second ?: "Reaction"
}

fun Story.parsedReactions(): List<StoryReactionEntry> =
    reactionEntries.mapNotNull { raw ->
        val parts = raw.split(":")
        if (parts.size >= 3) {
            val parsedEmoji = parts.drop(2).joinToString(":").trim()
            StoryReactionEntry(
                userId = parts[0].trim(),
                username = parts[1].trim().removePrefix("@"),
                emoji = parsedEmoji,
                feelingLabel = storyFeelingLabelForEmoji(parsedEmoji)
            )
        } else null
    }

fun Story.reactionCountsByEmoji(extraFirestoreReactions: List<StoryReaction> = emptyList()): Map<String, Int> {
    val byUser = LinkedHashMap<String, String>()
    parsedReactions().forEach { entry ->
        if (entry.userId.isNotBlank() && entry.emoji.isNotBlank()) {
            byUser[entry.userId] = entry.emoji
        }
    }
    extraFirestoreReactions
        .filter { it.storyId.isBlank() || it.storyId == id }
        .forEach { reaction ->
            if (reaction.userId.isNotBlank() && reaction.emoji.isNotBlank()) {
                byUser[reaction.userId] = reaction.emoji
            }
        }
    val counts = LinkedHashMap<String, Int>()
    byUser.values.forEach { em ->
        counts[em] = (counts[em] ?: 0) + 1
    }
    return counts
}

/**
 * Returns the creation timestamp in milliseconds for sorting and grouping in the Story Archive.
 * Falls back to (expiresAtMillis - 24h) when server timestamp is pending.
 */
fun Story.storyCreatedMillis(): Long {
    val fromCreatedAt = createdAt?.toDate()?.time ?: 0L
    if (fromCreatedAt > 0L) return fromCreatedAt
    val estimatedFromExpiry = expiresAtMillis - 24L * 60L * 60L * 1000L
    return if (estimatedFromExpiry > 0L) estimatedFromExpiry else System.currentTimeMillis()
}

fun Story.isStoryExpired(nowMillis: Long = System.currentTimeMillis()): Boolean =
    expiresAtMillis in 1..nowMillis

fun Story.archiveMonthYearHeader(): String {
    val sdf = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.ENGLISH)
    return sdf.format(java.util.Date(storyCreatedMillis()))
}

fun Story.archiveDayBadge(): String {
    val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.ENGLISH)
    return sdf.format(java.util.Date(storyCreatedMillis()))
}

data class StoryArchiveGroup(
    val monthYearHeader: String,
    val stories: List<Story>
)

/**
 * Organizes a user's past and active stories into chronological Month-Year archive sections,
 * sorted newest-first both across groups and within each group.
 */
fun groupStoriesForArchive(stories: List<Story>): List<StoryArchiveGroup> {
    val sorted = stories
        .distinctBy { it.id }
        .sortedByDescending { it.storyCreatedMillis() }
    val grouped = LinkedHashMap<String, MutableList<Story>>()
    for (story in sorted) {
        val header = story.archiveMonthYearHeader()
        grouped.getOrPut(header) { mutableListOf() }.add(story)
    }
    return grouped.map { (header, items) ->
        StoryArchiveGroup(monthYearHeader = header, stories = items)
    }
}

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
    val videoTrimStartMs: Long = 0L,
    val videoTrimEndMs: Long = 15_000L,
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

const val MIN_REEL_DURATION_SECONDS = 15L
const val MAX_REEL_DURATION_SECONDS = 60L
const val MIN_REEL_DURATION_MS = MIN_REEL_DURATION_SECONDS * 1000L
const val MAX_REEL_DURATION_MS = MAX_REEL_DURATION_SECONDS * 1000L

/**
 * Validates whether a real MUSIC_API_KEY has been configured via BuildConfig / .env
 * (as opposed to being blank or the default placeholder in .env.example).
 */
fun isValidMusicApiKey(rawKey: String?): Boolean {
    if (rawKey.isNullOrBlank()) return false
    val clean = rawKey.trim()
    return clean.isNotEmpty() &&
        clean != "YOUR_MUSIC_API_KEY" &&
        clean != "REPLACE_WITH_MUSIC_API_KEY" &&
        clean != "null"
}

/**
 * Synchronizes and overlays a selected [MusicTrack] onto a recorded Reel video of [reelDurationSeconds] (15–60s),
 * clamping the music trim window to match the recorded video duration.
 */
fun overlayMusicTrackOnReelVideo(
    track: MusicTrack,
    reelDurationSeconds: Long,
    startOffsetMs: Long = track.trimStartMs
): MusicTrack {
    val clampedReelSec = reelDurationSeconds.coerceIn(MIN_REEL_DURATION_SECONDS, MAX_REEL_DURATION_SECONDS)
    val safeTrackDurationMs = track.durationMs.coerceAtLeast(clampedReelSec * 1000L)
    val desiredOverlayMs = (clampedReelSec * 1000L).coerceAtMost(safeTrackDurationMs)
    val maxStartMs = (safeTrackDurationMs - desiredOverlayMs).coerceAtLeast(0L)
    val alignedStartMs = startOffsetMs.coerceIn(0L, maxStartMs)
    val alignedEndMs = (alignedStartMs + desiredOverlayMs).coerceAtMost(safeTrackDurationMs)
    return track.copy(
        durationMs = safeTrackDurationMs,
        trimStartMs = alignedStartMs,
        trimEndMs = alignedEndMs
    )
}

/**
 * Applies an overlaid background [MusicTrack] to a [Reel] model, populating all audio overlay fields.
 */
fun applyMusicOverlayToReel(reel: Reel, track: MusicTrack?): Reel {
    if (track == null || track.title.isBlank()) return reel
    val syncedTrack = overlayMusicTrackOnReelVideo(track, reel.durationSeconds, track.trimStartMs)
    return reel.copy(
        audioTrackName = "${syncedTrack.title} • ${syncedTrack.artist}".take(120),
        musicTrackId = syncedTrack.trackId,
        musicTitle = syncedTrack.title,
        musicArtist = syncedTrack.artist,
        musicCoverUrl = syncedTrack.coverUrl,
        musicUrl = syncedTrack.audioUrl,
        musicStartMs = syncedTrack.trimStartMs,
        musicEndMs = syncedTrack.trimEndMs
    )
}

/**
 * Clamps a Reel video trim selection [rawStartMs, rawEndMs] within [0L, sourceDurationMs]
 * while enforcing the 15-60 second short-form Reel duration window.
 */
fun clampReelVideoTrimWindow(
    rawStartMs: Long,
    rawEndMs: Long,
    sourceDurationMs: Long = 60_000L
): Pair<Long, Long> {
    val safeSourceMs = sourceDurationMs.coerceIn(MIN_REEL_DURATION_MS, 180_000L)
    val minClipMs = MIN_REEL_DURATION_MS.coerceAtMost(safeSourceMs)
    val maxClipMs = MAX_REEL_DURATION_MS.coerceAtMost(safeSourceMs)

    var startMs = rawStartMs.coerceIn(0L, (safeSourceMs - minClipMs).coerceAtLeast(0L))
    var endMs = rawEndMs.coerceIn(startMs + minClipMs, safeSourceMs)

    val currentSpan = endMs - startMs
    if (currentSpan > maxClipMs) {
        endMs = (startMs + maxClipMs).coerceAtMost(safeSourceMs)
    } else if (currentSpan < minClipMs) {
        endMs = (startMs + minClipMs).coerceAtMost(safeSourceMs)
        if (endMs - startMs < minClipMs) {
            startMs = (endMs - minClipMs).coerceAtLeast(0L)
        }
    }
    return startMs to endMs
}

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
    val reelId: String = "",
    val targetType: String = "POST", // POST, REEL
    val authorUsername: String = "",
    val captionPreview: String = "",
    val mediaUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val resolvedReelId: String
        get() = reelId.ifBlank { if (targetType.equals("REEL", ignoreCase = true)) postId else "" }
}

@IgnoreExtraProperties
data class SavedReel(
    @DocumentId val id: String = "",
    val userId: String = "",
    val reelId: String = "",
    val postId: String = "",
    val targetType: String = "REEL",
    val authorUsername: String = "",
    val captionPreview: String = "",
    val mediaUrl: String = "",
    val videoUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    fun toSavedPost(): SavedPost = SavedPost(
        id = id.ifBlank { "${userId}_${reelId}" },
        userId = userId,
        postId = reelId.ifBlank { postId },
        reelId = reelId.ifBlank { postId },
        targetType = "REEL",
        authorUsername = authorUsername,
        captionPreview = captionPreview,
        mediaUrl = videoUrl.ifBlank { mediaUrl },
        createdAt = createdAt
    )
}

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
    val readByUids: List<String> = emptyList(),
    val readAtMillis: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

fun DirectMessage.isReadByPeer(currentUserId: String): Boolean =
    deliveryStatus.equals("READ", ignoreCase = true) ||
        readByUids.any { it.isNotBlank() && it != senderId && it != currentUserId } ||
        (senderId == currentUserId && readByUids.any { it.isNotBlank() && it != currentUserId })

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

/**
 * Filters and sorts a user's recent public posts (newest-first) for display on their Profile screen.
 */
fun filterRecentPublicPostsForProfile(
    posts: List<Post>,
    userId: String,
    username: String,
    isOwner: Boolean = false,
    profileDocId: String = "",
    currentUserId: String = ""
): List<Post> {
    val effectiveUid = userId.trim().ifBlank { profileDocId.trim() }
    val cleanDocId = profileDocId.trim()
    val cleanCurrentUid = currentUserId.trim()
    val cleanUsername = username.trim().removePrefix("@")
    return posts.filter { post ->
        val postAuthorUid = post.authorId.trim()
        val isAuthorOrCollab = (effectiveUid.isNotBlank() && postAuthorUid == effectiveUid) ||
            (cleanDocId.isNotBlank() && postAuthorUid == cleanDocId) ||
            (isOwner && cleanCurrentUid.isNotBlank() && postAuthorUid == cleanCurrentUid) ||
            (cleanUsername.isNotBlank() && post.authorUsername.trim().removePrefix("@").equals(cleanUsername, ignoreCase = true)) ||
            (cleanUsername.isNotBlank() && post.collaboratorUsernames.any { c ->
                c.trim().removePrefix("@").equals(cleanUsername, ignoreCase = true)
            })
        val isPublicOrAllowed = isOwner ||
            (cleanCurrentUid.isNotBlank() && postAuthorUid == cleanCurrentUid) ||
            post.visibility.isBlank() ||
            post.visibility.equals("PUBLIC", ignoreCase = true)
        isAuthorOrCollab && isPublicOrAllowed
    }
        .distinctBy { it.id.ifBlank { "${it.authorId}|${it.caption}|${it.createdAt?.seconds ?: 0L}" } }
        .sortedWith(
            compareByDescending<Post> { it.createdAt?.seconds ?: 0L }
                .thenByDescending { it.createdAt?.nanoseconds ?: 0 }
        )
}

/**
 * Filters and sorts a user's Reels (newest-first) for display and playback in their Profile screen's Reels tab.
 * Matches by userId, profileDocId, currentUserId (when isOwner = true), normalized @username,
 * or collaboratorUsernames so users can always access their own Reels and co-authored Reels.
 */
fun filterUserReelsForProfile(
    reels: List<Reel>,
    userId: String,
    profileDocId: String = "",
    username: String = "",
    isOwner: Boolean = false,
    currentUserId: String = ""
): List<Reel> {
    val effectiveUid = userId.trim().ifBlank { profileDocId.trim() }
    val cleanDocId = profileDocId.trim()
    val cleanCurrentUid = currentUserId.trim()
    val cleanUsername = username.trim().removePrefix("@")

    return reels
        .filter { reel ->
            val reelAuthorUid = reel.authorId.trim()
            val reelAuthorUsername = reel.authorUsername.trim().removePrefix("@")

            val matchesUid = (effectiveUid.isNotBlank() && reelAuthorUid == effectiveUid) ||
                (cleanDocId.isNotBlank() && reelAuthorUid == cleanDocId) ||
                (isOwner && cleanCurrentUid.isNotBlank() && reelAuthorUid == cleanCurrentUid)

            val matchesUsername = cleanUsername.isNotBlank() &&
                reelAuthorUsername.equals(cleanUsername, ignoreCase = true)

            val matchesCollab = cleanUsername.isNotBlank() &&
                reel.collaboratorUsernames.any { c ->
                    c.trim().removePrefix("@").equals(cleanUsername, ignoreCase = true)
                }

            val isVisible = isOwner ||
                (cleanCurrentUid.isNotBlank() && reelAuthorUid == cleanCurrentUid) ||
                reel.visibility.isBlank() ||
                reel.visibility.equals("PUBLIC", ignoreCase = true)

            (matchesUid || matchesUsername || matchesCollab) && isVisible
        }
        .distinctBy { it.id.ifBlank { "${it.authorId}|${it.videoUrl}|${it.caption}|${it.createdAt?.seconds ?: 0L}" } }
        .distinctBy { reel ->
            val timeBucket = (reel.createdAt?.seconds ?: 0L) / 30L
            if (reel.videoUrl.isBlank() && reel.caption.isBlank()) {
                reel.id
            } else {
                "${reel.authorId}|${reel.videoUrl.trim()}|${reel.caption.trim()}|$timeBucket"
            }
        }
        .sortedWith(
            compareByDescending<Reel> { it.createdAt?.seconds ?: 0L }
                .thenByDescending { it.createdAt?.nanoseconds ?: 0 }
        )
}

/**
 * Filters and ranks UserProfile results by username or display name query.
 */
fun filterAndRankUsersByQuery(
    users: List<UserProfile>,
    rawQuery: String
): List<UserProfile> {
    val rawTrimmed = rawQuery.trim().removePrefix("@").removePrefix("#")
    val clean = rawTrimmed.lowercase()
    if (clean.isBlank()) return users.distinctBy { it.userId.ifBlank { it.id.ifBlank { it.username } } }
    return users
        .distinctBy { it.userId.ifBlank { it.id.ifBlank { it.username } } }
        .filter { user ->
            user.username.lowercase().contains(clean) ||
                user.displayName.lowercase().contains(clean) ||
                user.bio.lowercase().contains(clean) ||
                user.userId.lowercase().contains(clean)
        }
        .sortedWith(
            compareByDescending<UserProfile> {
                it.username.equals(clean, ignoreCase = true) ||
                    it.displayName.equals(rawTrimmed, ignoreCase = true)
            }.thenByDescending {
                it.username.lowercase().startsWith(clean) ||
                    it.displayName.lowercase().startsWith(clean)
            }.thenByDescending {
                it.isVerified
            }
        )
}
