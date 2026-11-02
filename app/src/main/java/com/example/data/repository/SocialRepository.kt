package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.Comment
import com.example.data.model.ContentReport
import com.example.data.model.Conversation
import com.example.data.model.DirectMessage
import com.example.data.model.FirestoreFeedSearchResult
import com.example.data.model.FirestoreNotification
import com.example.data.model.Follow
import com.example.data.model.MITR_BLUE_TICK_PRICE_INR
import com.example.data.model.MITR_FOUNDER_VIP_EMAIL
import com.example.data.model.MusicTrack
import com.example.data.model.OperationType
import com.example.data.model.Post
import com.example.data.model.Reel
import com.example.data.model.SavedMusic
import com.example.data.model.SavedPost
import com.example.data.model.SavedReel
import com.example.data.model.Story
import com.example.data.local.LocalActivityLogEntity
import com.example.data.local.MitrLocalDao
import com.example.data.local.MitrLocalDatabase
import com.example.data.local.MobileStorageStats
import com.example.data.local.toLocalEntity
import com.example.data.model.StoryReaction
import com.example.data.model.UserActivityLog
import com.example.data.model.UserProfile
import com.example.data.model.UserRelation
import com.example.data.model.UserSettings
import com.example.data.model.filterAndRankUsersByQuery
import com.example.data.model.filterRecentPublicPostsForProfile
import com.example.data.model.filterUserReelsForProfile
import com.example.data.model.isFounderVipAccount
import com.example.data.model.isUserBlueTickVerified
import com.example.data.model.storyCreatedMillis
import com.example.data.model.storyFeelingLabelForEmoji
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

fun handleFirestoreError(exception: Exception, operationType: OperationType, path: String?): String {
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    val providerInfoList = currentUser?.providerData?.map { provider ->
        JSONObject().apply {
            put("providerId", provider.providerId)
            put("email", provider.email)
        }
    } ?: emptyList()

    val authInfoJson = JSONObject().apply {
        put("userId", currentUser?.uid)
        put("email", currentUser?.email)
        put("emailVerified", currentUser?.isEmailVerified)
        put("tenantId", currentUser?.tenantId)
        put("providerInfo", JSONArray(providerInfoList))
    }

    val errorInfoJson = JSONObject().apply {
        put("error", exception.message ?: exception.toString())
        put("operationType", operationType.value)
        put("path", path)
        put("authInfo", authInfoJson)
    }

    val jsonString = errorInfoJson.toString()
    Log.e("FirestoreError", "Firestore Error: $jsonString")
    return jsonString
}

class SocialRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth,
    private val localDao: MitrLocalDao? = null
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        auth = Firebase.auth,
        localDao = MitrLocalDatabase.getInstance(context.applicationContext).mitrLocalDao()
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- 1. User Profile & Onboarding ---

    private fun DocumentSnapshot.toNormalizedUserProfile(authEmail: String? = null): UserProfile? {
        if (!exists()) return null
        val raw = runCatching {
            toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }.getOrNull() ?: return null
        val effectiveUid = raw.userId.trim().ifBlank { id }
        val effectiveUsername = raw.username.trim().removePrefix("@").ifBlank {
            getString("username")?.trim()?.removePrefix("@").orEmpty().ifBlank {
                "user_${effectiveUid.take(6)}"
            }
        }
        val effectiveDisplayName = raw.displayName.trim().ifBlank {
            getString("displayName")?.trim().orEmpty().ifBlank { effectiveUsername }
        }
        val isVip = isFounderVipAccount(authEmail, effectiveUsername)
        return raw.copy(
            id = id,
            userId = effectiveUid,
            username = effectiveUsername,
            displayName = effectiveDisplayName,
            isVerified = raw.isVerified || isVip,
            subscriptionPlan = if (isVip) "FOUNDER_VIP" else raw.subscriptionPlan
        )
    }

    data class FirestoreDiagnosticReport(
        val isAuthenticated: Boolean,
        val currentUid: String?,
        val currentEmail: String?,
        val usersReadAllowed: Boolean,
        val postsReadAllowed: Boolean,
        val reelsReadAllowed: Boolean,
        val connectionHealthy: Boolean,
        val diagnosticMessage: String
    )

    /**
     * Diagnostic utility function that verifies Firestore security rules, authentication state,
     * and collection read access for /users, /posts, and /reels.
     */
    suspend fun verifyFirestoreConnectionAndRules(): Result<FirestoreDiagnosticReport> = runCatching {
        val currentUser = auth.currentUser
        val uid = currentUser?.uid
        val email = currentUser?.email
        if (uid.isNullOrBlank()) {
            val report = FirestoreDiagnosticReport(
                isAuthenticated = false,
                currentUid = null,
                currentEmail = null,
                usersReadAllowed = false,
                postsReadAllowed = false,
                reelsReadAllowed = false,
                connectionHealthy = false,
                diagnosticMessage = "User is not authenticated with Firebase Auth."
            )
            Log.w("FirestoreDiagnostics", report.diagnosticMessage)
            return@runCatching report
        }

        var usersOk = false
        var postsOk = false
        var reelsOk = false
        val errorNotes = mutableListOf<String>()

        try {
            db.collection("users").limit(1).get().await()
            usersOk = true
        } catch (e: Exception) {
            errorNotes.add("users: ${e.message}")
            Log.e("FirestoreDiagnostics", "Security rules / connection check failed on 'users' for uid=$uid", e)
        }

        try {
            db.collection("posts").limit(1).get().await()
            postsOk = true
        } catch (e: Exception) {
            errorNotes.add("posts: ${e.message}")
            Log.e("FirestoreDiagnostics", "Security rules / connection check failed on 'posts' for uid=$uid", e)
        }

        try {
            db.collection("reels").limit(1).get().await()
            reelsOk = true
        } catch (e: Exception) {
            errorNotes.add("reels: ${e.message}")
            Log.e("FirestoreDiagnostics", "Security rules / connection check failed on 'reels' for uid=$uid", e)
        }

        val healthy = usersOk && postsOk && reelsOk
        val summary = if (healthy) {
            "Firestore connection & security rules verified (uid=$uid, users=OK, posts=OK, reels=OK)"
        } else {
            "Firestore diagnostic issues detected (uid=$uid): ${errorNotes.joinToString("; ")}"
        }
        Log.i("FirestoreDiagnostics", summary)
        FirestoreDiagnosticReport(
            isAuthenticated = true,
            currentUid = uid,
            currentEmail = email,
            usersReadAllowed = usersOk,
            postsReadAllowed = postsOk,
            reelsReadAllowed = reelsOk,
            connectionHealthy = healthy,
            diagnosticMessage = summary
        )
    }

    fun observeCurrentUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    snapshot.toNormalizedUserProfile(auth.currentUser?.email)
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    /**
     * Real-time stream of any user's profile document from `/users/{targetUserId}` with fallback resolution
     * so navigating to another user's ID always displays their live profile data instead of a placeholder.
     */
    fun observeUserProfileById(targetUserId: String): Flow<UserProfile?> = flow {
        requireUserId()
        val cleanId = targetUserId.trim().removePrefix("@")
        if (cleanId.isBlank()) {
            emit(null)
            return@flow
        }
        val path = "users/$cleanId"
        emitAll(
            db.collection("users").document(cleanId)
                .snapshots()
                .map { snapshot ->
                    snapshot.toNormalizedUserProfile() ?: resolveUserProfile(cleanId).getOrNull()
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    emit(resolveUserProfile(cleanId).getOrNull())
                }
        )
    }

    suspend fun getUserProfile(userId: String): Result<UserProfile?> = runCatching {
        resolveUserProfile(userId).getOrNull()
    }

    /**
     * Resolves a user's full profile by UID or @username across /users, /usernames, /posts, /reels, /stories, and /follows
     * so opening another person's profile always displays their real username, displayName, bio, avatar, and stats.
     */
    suspend fun resolveUserProfile(identifier: String): Result<UserProfile?> = runCatching {
        requireUserId()
        val cleanId = identifier.trim().removePrefix("@")
        if (cleanId.isBlank()) return@runCatching null

        // 1. Direct lookup in /users/{cleanId}
        val directSnap = runCatching {
            db.collection("users").document(cleanId).get().await()
        }.getOrNull()
        if (directSnap != null && directSnap.exists()) {
            directSnap.toNormalizedUserProfile()?.let { return@runCatching it }
        }

        // 2. Lookup in /usernames/{cleanId.lowercase()}
        val lowerClean = cleanId.lowercase(Locale.ROOT)
        val claimSnap = runCatching {
            db.collection("usernames").document(lowerClean).get().await()
        }.getOrNull()
        val claimedUid = claimSnap?.getString("userId")?.trim().orEmpty()
        val claimedUsername = claimSnap?.getString("username")?.trim().orEmpty()
        if (claimedUid.isNotBlank()) {
            val claimedUserSnap = runCatching {
                db.collection("users").document(claimedUid).get().await()
            }.getOrNull()
            if (claimedUserSnap != null && claimedUserSnap.exists()) {
                claimedUserSnap.toNormalizedUserProfile()?.let { return@runCatching it }
            }
        }

        // 3. Scan /users collection for matching userId, document id, username, or displayName
        val allUsersSnap = runCatching {
            db.collection("users").get().await()
        }.getOrNull()
        val matchedFromUsers = allUsersSnap?.documents
            ?.mapNotNull { it.toNormalizedUserProfile() }
            ?.firstOrNull { u ->
                u.userId.equals(cleanId, ignoreCase = true) ||
                    u.id.equals(cleanId, ignoreCase = true) ||
                    u.username.equals(lowerClean, ignoreCase = true) ||
                    u.displayName.equals(cleanId, ignoreCase = true)
            }
        if (matchedFromUsers != null) {
            return@runCatching matchedFromUsers
        }

        // 4. Reconstruct profile from authored Posts, Reels, Stories, or Follows if /users doc was not created yet
        val postsSnap = runCatching { db.collection("posts").get().await() }.getOrNull()
        val userPosts = postsSnap?.documents?.mapNotNull { doc ->
            runCatching {
                doc.toObject(Post::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = doc.id)
            }.getOrNull()
        }?.filter {
            it.authorId == cleanId ||
                (claimedUid.isNotBlank() && it.authorId == claimedUid) ||
                it.authorUsername.equals(lowerClean, ignoreCase = true)
        }.orEmpty()

        if (userPosts.isNotEmpty()) {
            val first = userPosts.first()
            val resolvedUid = first.authorId.ifBlank { claimedUid.ifBlank { cleanId } }
            val resolvedUname = first.authorUsername.removePrefix("@").ifBlank { claimedUsername.ifBlank { lowerClean } }
            return@runCatching UserProfile(
                id = resolvedUid,
                userId = resolvedUid,
                username = resolvedUname,
                displayName = first.authorDisplayName.ifBlank { resolvedUname },
                avatarUrl = first.authorAvatarUrl,
                postCount = userPosts.size.toLong(),
                isVerified = isFounderVipAccount(null, resolvedUname)
            )
        }

        val reelsSnap = runCatching { db.collection("reels").get().await() }.getOrNull()
        val userReels = reelsSnap?.documents?.mapNotNull { doc ->
            runCatching {
                doc.toObject(Reel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = doc.id)
            }.getOrNull()
        }?.filter {
            it.authorId == cleanId ||
                (claimedUid.isNotBlank() && it.authorId == claimedUid) ||
                it.authorUsername.equals(lowerClean, ignoreCase = true)
        }.orEmpty()

        if (userReels.isNotEmpty()) {
            val first = userReels.first()
            val resolvedUid = first.authorId.ifBlank { claimedUid.ifBlank { cleanId } }
            val resolvedUname = first.authorUsername.removePrefix("@").ifBlank { claimedUsername.ifBlank { lowerClean } }
            return@runCatching UserProfile(
                id = resolvedUid,
                userId = resolvedUid,
                username = resolvedUname,
                displayName = first.authorDisplayName.ifBlank { resolvedUname },
                postCount = userReels.size.toLong(),
                isVerified = isFounderVipAccount(null, resolvedUname)
            )
        }

        if (claimedUid.isNotBlank() || claimedUsername.isNotBlank()) {
            val uidToUse = claimedUid.ifBlank { cleanId }
            val unameToUse = claimedUsername.ifBlank { lowerClean }
            return@runCatching UserProfile(
                id = uidToUse,
                userId = uidToUse,
                username = unameToUse,
                displayName = unameToUse,
                isVerified = isFounderVipAccount(null, unameToUse)
            )
        }

        null
    }

    suspend fun isUsernameAvailable(rawUsername: String): Result<Boolean> = runCatching {
        val sanitized = rawUsername.trim().lowercase(Locale.ROOT)
        require(sanitized.length in 3..30 && sanitized.matches(Regex("^[a-zA-Z0-9_\\-]+$"))) {
            "Username must be 3-30 characters (letters, numbers, underscores, hyphens)."
        }
        val path = "usernames/$sanitized"
        try {
            val doc = db.collection("usernames").document(sanitized).get().await()
            !doc.exists() || doc.getString("userId") == auth.currentUser?.uid
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            throw e
        }
    }

    suspend fun createOrUpdateProfile(
        username: String,
        displayName: String,
        bio: String,
        website: String,
        avatarUrl: String,
        isPrivate: Boolean
    ): Result<UserProfile> = runCatching {
        val uid = requireUserId()
        val cleanUsername = username.trim().lowercase(Locale.ROOT)
        require(cleanUsername.length in 3..30 && cleanUsername.matches(Regex("^[a-zA-Z0-9_\\-]+$"))) {
            "Username must be 3-30 alphanumeric characters, underscores, or hyphens."
        }
        require(displayName.isNotBlank()) { "Display name is required." }

        val usernameRef = db.collection("usernames").document(cleanUsername)
        val userRef = db.collection("users").document(uid)

        try {
            val existingClaim = usernameRef.get().await()
            if (existingClaim.exists() && existingClaim.getString("userId") != uid) {
                throw IllegalArgumentException("Username @$cleanUsername is already taken.")
            }

            val existingUserSnap = userRef.get().await()
            val previousUsername = existingUserSnap.getString("username")

            if (!existingClaim.exists()) {
                usernameRef.set(
                    mapOf(
                        "userId" to uid,
                        "username" to cleanUsername,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            if (!previousUsername.isNullOrBlank() && previousUsername != cleanUsername) {
                val oldClaimRef = db.collection("usernames").document(previousUsername)
                val oldSnap = oldClaimRef.get().await()
                if (oldSnap.exists() && oldSnap.getString("userId") == uid) {
                    oldClaimRef.delete().await()
                }
            }

            if (!existingUserSnap.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "username" to cleanUsername,
                    "displayName" to displayName.trim(),
                    "bio" to bio.trim(),
                    "website" to website.trim(),
                    "avatarUrl" to avatarUrl.trim(),
                    "isPrivate" to isPrivate,
                    "postCount" to 0L,
                    "followerCount" to 0L,
                    "followingCount" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userRef.set(payload).await()

                val settingsRef = db.collection("user_settings").document(uid)
                val settingsSnap = settingsRef.get().await()
                if (!settingsSnap.exists()) {
                    settingsRef.set(
                        mapOf(
                            "userId" to uid,
                            "email" to (auth.currentUser?.email ?: ""),
                            "isPrivateAccount" to isPrivate,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            } else {
                val updates = mapOf(
                    "username" to cleanUsername,
                    "displayName" to displayName.trim(),
                    "bio" to bio.trim(),
                    "website" to website.trim(),
                    "avatarUrl" to avatarUrl.trim(),
                    "isPrivate" to isPrivate,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userRef.update(updates).await()
            }

            val updatedSnap = userRef.get().await()
            val updatedProfile = checkNotNull(updatedSnap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE))
            val bioMentions = Regex("@([a-zA-Z0-9_.]+)").findAll(bio)
                .map { it.groupValues[1].lowercase(Locale.ROOT) }
                .filter { it.isNotBlank() && it != cleanUsername }
                .distinct()
                .toList()
            if (bioMentions.isNotEmpty()) {
                notifyCollaboratorsAndMentions(
                    authorProfile = updatedProfile,
                    collaboratorUsernames = emptyList(),
                    taggedUsernames = bioMentions,
                    targetId = uid,
                    targetType = "USER",
                    targetMediaUrl = updatedProfile.avatarUrl,
                    captionPreview = "Mentioned you in their bio: \"${bio.take(90)}\""
                )
            }
            updatedProfile
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, userRef.path)
            throw e
        }
    }

    // --- 2. User Settings (Privacy, Notifications, Time Management, Content) ---

    fun observeUserSettings(): Flow<UserSettings> = flow {
        val uid = requireUserId()
        val path = "user_settings/$uid"
        emitAll(
            db.collection("user_settings").document(uid)
                .snapshots()
                .map { snapshot ->
                    val raw = snapshot.toObject(UserSettings::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?: UserSettings(id = uid, userId = uid, email = auth.currentUser?.email ?: "")
                    if (isFounderVipAccount(auth.currentUser?.email ?: raw.email)) {
                        raw.copy(
                            isVerifiedSubscriber = true,
                            subscriptionPlan = "FOUNDER_VIP",
                            subscriptionPriceInr = 0L
                        )
                    } else {
                        raw
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.GET, path)
                    throw error
                }
        )
    }

    suspend fun saveUserSettings(settings: UserSettings): Result<Unit> = runCatching {
        val uid = requireUserId()
        val ref = db.collection("user_settings").document(uid)
        try {
            val payload = mapOf(
                "userId" to uid,
                "email" to settings.email,
                "isPrivateAccount" to settings.isPrivateAccount,
                "requireFollowApproval" to settings.requireFollowApproval,
                "whoCanComment" to settings.whoCanComment,
                "whoCanMessage" to settings.whoCanMessage,
                "whoCanTag" to settings.whoCanTag,
                "whoCanMention" to settings.whoCanMention,
                "whoCanReplyStories" to settings.whoCanReplyStories,
                "manuallyApproveTags" to settings.manuallyApproveTags,
                "showActivityStatus" to settings.showActivityStatus,
                "readReceiptsEnabled" to settings.readReceiptsEnabled,
                "hideLikeCounts" to settings.hideLikeCounts,
                "allowStorySharing" to settings.allowStorySharing,
                "archiveStories" to settings.archiveStories,
                "hiddenWordsCsv" to settings.hiddenWordsCsv,
                "sensitiveContentControl" to settings.sensitiveContentControl,
                "searchDiscoverability" to settings.searchDiscoverability,
                "pushNotificationsEnabled" to settings.pushNotificationsEnabled,
                "notifyLikes" to settings.notifyLikes,
                "notifyComments" to settings.notifyComments,
                "notifyCommentReplies" to settings.notifyCommentReplies,
                "notifyFollowers" to settings.notifyFollowers,
                "notifyFollowRequests" to settings.notifyFollowRequests,
                "notifyMentions" to settings.notifyMentions,
                "notifyDirectMessages" to settings.notifyDirectMessages,
                "notifyMessageRequests" to settings.notifyMessageRequests,
                "notifyStoryReplies" to settings.notifyStoryReplies,
                "notifyLiveVideos" to settings.notifyLiveVideos,
                "notifyReminders" to settings.notifyReminders,
                "emailNotificationsEnabled" to settings.emailNotificationsEnabled,
                "quietHoursEnabled" to settings.quietHoursEnabled,
                "quietHoursStart" to settings.quietHoursStart,
                "quietHoursEnd" to settings.quietHoursEnd,
                "dailyTimeLimitMinutes" to settings.dailyTimeLimitMinutes,
                "breakReminderMinutes" to settings.breakReminderMinutes,
                "bedtimeReminderEnabled" to settings.bedtimeReminderEnabled,
                "bedtimeStart" to settings.bedtimeStart,
                "todayUsageMinutes" to settings.todayUsageMinutes,
                "weeklyUsageMinutes" to settings.weeklyUsageMinutes,
                "locationAccessEnabled" to settings.locationAccessEnabled,
                "allowPostLocation" to settings.allowPostLocation,
                "showLocationContent" to settings.showLocationContent,
                "themeMode" to settings.themeMode,
                "dataSaverMode" to settings.dataSaverMode,
                "autoplayVideos" to settings.autoplayVideos,
                "highQualityVideo" to settings.highQualityVideo,
                "reduceMotion" to settings.reduceMotion,
                "autoCaptions" to settings.autoCaptions,
                "textScalePercent" to settings.textScalePercent,
                "accountDeactivated" to settings.accountDeactivated,
                "isVerifiedSubscriber" to settings.isVerifiedSubscriber,
                "subscriptionPlan" to settings.subscriptionPlan,
                "subscriptionPriceInr" to settings.subscriptionPriceInr,
                "subscriptionActivatedAtMillis" to settings.subscriptionActivatedAtMillis,
                "payoutHolderName" to settings.payoutHolderName,
                "payoutBankOrUpi" to settings.payoutBankOrUpi,
                "payoutAccountNumber" to settings.payoutAccountNumber,
                "payoutIfscCode" to settings.payoutIfscCode,
                "pendingPayoutBalanceInr" to settings.pendingPayoutBalanceInr,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            ref.set(payload).await()
            runCatching {
                val userRef = db.collection("users").document(uid)
                val userSnap = userRef.get().await()
                if (userSnap.exists() && userSnap.getBoolean("isPrivate") != settings.isPrivateAccount) {
                    userRef.update(
                        mapOf(
                            "isPrivate" to settings.isPrivateAccount,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            throw e
        }
    }

    // --- 3. Posts, Likes, Comments, Saved Posts ---

    fun observePublicFeedPosts(): Flow<List<Post>> = flow {
        val uid = requireUserId()
        val path = "posts"
        try {
            val feedFlow = db.collection("posts")
                .snapshots()
                .map { snap ->
                    try {
                        snap.documents.mapNotNull { doc ->
                            try {
                                doc.toObject(Post::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                                    ?.copy(id = doc.id)
                            } catch (docErr: Exception) {
                                Log.w("FirestoreFeedSync", "Skipping malformed post doc id=${doc.id}: ${docErr.message}")
                                null
                            }
                        }
                            .filter { it.visibility.isBlank() || it.visibility == "PUBLIC" || it.authorId == uid }
                            .distinctBy { it.id }
                            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    } catch (mapErr: Exception) {
                        Log.e("FirestoreFeedSync", "Error mapping feed query snapshot (uid=$uid, path=$path): ${mapErr.message}", mapErr)
                        emptyList()
                    }
                }
                .catch { error ->
                    Log.e(
                        "FirestoreFeedSync",
                        "Feed query snapshot listener error (uid=$uid, path=$path, type=${error::class.java.simpleName}): ${error.message}",
                        error
                    )
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
            emitAll(feedFlow)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e("FirestoreFeedSync", "Failed to initialize feed query snapshot listener (uid=$uid, path=$path): ${e.message}", e)
            handleFirestoreError(e, OperationType.LIST, path)
            emit(emptyList())
        }
    }

    fun observeUserPosts(authorId: String, authorUsername: String = ""): Flow<List<Post>> = flow {
        val uid = requireUserId()
        val cleanUsername = authorUsername.trim().removePrefix("@")
        val path = "posts"
        emitAll(
            db.collection("posts")
                .snapshots()
                .map { snap ->
                    val allDocs = snap.documents.mapNotNull { doc ->
                        runCatching {
                            doc.toObject(Post::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                                ?.copy(id = doc.id)
                        }.getOrNull()
                    }
                    filterRecentPublicPostsForProfile(
                        posts = allDocs,
                        userId = authorId,
                        username = cleanUsername,
                        isOwner = authorId == uid
                    )
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun createPost(
        authorProfile: UserProfile,
        caption: String,
        mediaUrls: List<String>,
        mediaType: String,
        locationTag: String,
        taggedUsernames: List<String>,
        visibility: String,
        commentsAllowed: Boolean,
        musicTrack: MusicTrack? = null,
        collaboratorUsernames: List<String> = emptyList()
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val postRef = db.collection("posts").document()
        val hashtags = Regex("#([a-zA-Z0-9_]+)").findAll(caption)
            .map { it.groupValues[1].lowercase(Locale.ROOT) }
            .distinct()
            .take(25)
            .toList()
        val mentionedInCaption = Regex("@([a-zA-Z0-9_.]+)").findAll(caption)
            .map { it.groupValues[1].lowercase(Locale.ROOT) }
            .distinct()
            .toList()
        val finalTagged = (taggedUsernames.map { it.trim().removePrefix("@").lowercase(Locale.ROOT) } + mentionedInCaption)
            .filter { it.isNotBlank() }
            .distinct()
            .take(15)
        val finalCollabs = collaboratorUsernames
            .map { it.trim().removePrefix("@").lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() && it != authorProfile.username.lowercase(Locale.ROOT) }
            .distinct()
            .take(10)

        val payload = mutableMapOf<String, Any>(
            "authorId" to uid,
            "authorUsername" to authorProfile.username,
            "authorDisplayName" to authorProfile.displayName,
            "authorAvatarUrl" to authorProfile.avatarUrl,
            "caption" to caption.trim(),
            "mediaUrls" to mediaUrls.take(10),
            "mediaType" to mediaType,
            "locationTag" to locationTag.trim(),
            "taggedUsernames" to finalTagged,
            "collaboratorUsernames" to finalCollabs,
            "hashtags" to hashtags,
            "visibility" to visibility,
            "commentsAllowed" to commentsAllowed,
            "likeCount" to 0L,
            "commentCount" to 0L,
            "likedByUids" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (musicTrack != null && musicTrack.title.isNotBlank()) {
            payload["musicTrackId"] = musicTrack.trackId.take(120)
            payload["musicTitle"] = musicTrack.title.take(120)
            payload["musicArtist"] = musicTrack.artist.take(120)
            payload["musicCoverUrl"] = musicTrack.coverUrl.take(1000)
            payload["musicUrl"] = musicTrack.audioUrl.take(1000)
            payload["musicStartMs"] = musicTrack.trimStartMs.coerceAtLeast(0L)
            payload["musicEndMs"] = musicTrack.trimEndMs.coerceAtLeast(1000L)
        }

        try {
            postRef.set(payload).await()
            val userRef = db.collection("users").document(uid)
            val userSnap = userRef.get().await()
            if (userSnap.exists()) {
                val currentCount = userSnap.getLong("postCount") ?: 0L
                userRef.update(
                    mapOf(
                        "postCount" to (currentCount + 1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            notifyCollaboratorsAndMentions(
                authorProfile = authorProfile,
                collaboratorUsernames = finalCollabs,
                taggedUsernames = finalTagged,
                targetId = postRef.id,
                targetType = "POST",
                targetMediaUrl = mediaUrls.firstOrNull().orEmpty(),
                captionPreview = caption
            )
            logActivity("POST_CREATED", postRef.id, "Published post: ${caption.take(50)}")
            postRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, postRef.path)
            throw e
        }
    }

    suspend fun toggleLikePost(post: Post): Result<Unit> = runCatching {
        val uid = requireUserId()
        val postRef = db.collection("posts").document(post.id)
        try {
            val alreadyLiked = post.likedByUids.contains(uid)
            val updatedLikes = if (alreadyLiked) {
                post.likedByUids.filter { it != uid }
            } else {
                (post.likedByUids + uid).distinct().take(500)
            }
            val newCount = updatedLikes.size.toLong()
            postRef.update(
                mapOf(
                    "likedByUids" to updatedLikes,
                    "likeCount" to newCount,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            if (!alreadyLiked) {
                logActivity("LIKE", post.id, "Liked @${post.authorUsername}'s post")
                if (post.authorId.isNotBlank() && post.authorId != uid) {
                    val actorSnap = db.collection("users").document(uid).get().await()
                    val actorUsername = actorSnap.getString("username") ?: (auth.currentUser?.displayName ?: "mitr_user")
                    val actorDisplayName = actorSnap.getString("displayName") ?: actorUsername
                    val actorAvatarUrl = actorSnap.getString("avatarUrl") ?: ""
                    createFirestoreNotification(
                        recipientId = post.authorId,
                        actorId = uid,
                        actorUsername = actorUsername,
                        actorDisplayName = actorDisplayName,
                        actorAvatarUrl = actorAvatarUrl,
                        type = "LIKE",
                        message = "liked your post",
                        detailText = post.caption.take(100),
                        targetId = post.id,
                        targetType = "POST",
                        targetMediaUrl = post.mediaUrls.firstOrNull().orEmpty()
                    )
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, postRef.path)
            throw e
        }
    }

    suspend fun updatePostCaption(postId: String, newCaption: String, commentsAllowed: Boolean): Result<Unit> = runCatching {
        requireUserId()
        val postRef = db.collection("posts").document(postId)
        val hashtags = Regex("#([a-zA-Z0-9_]+)").findAll(newCaption)
            .map { it.groupValues[1].lowercase(Locale.ROOT) }
            .distinct()
            .take(25)
            .toList()
        try {
            postRef.update(
                mapOf(
                    "caption" to newCaption.trim(),
                    "hashtags" to hashtags,
                    "commentsAllowed" to commentsAllowed,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, postRef.path)
            throw e
        }
    }

    suspend fun deletePost(postId: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val postRef = db.collection("posts").document(postId)
        try {
            postRef.delete().await()
            val userRef = db.collection("users").document(uid)
            val userSnap = userRef.get().await()
            if (userSnap.exists()) {
                val currentCount = (userSnap.getLong("postCount") ?: 1L).coerceAtLeast(1L)
                userRef.update(
                    mapOf(
                        "postCount" to (currentCount - 1L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, postRef.path)
            throw e
        }
    }

    fun observeComments(targetId: String, targetType: String): Flow<List<Comment>> = flow {
        requireUserId()
        val path = "comments"
        emitAll(
            db.collection("comments")
                .whereEqualTo("targetId", targetId)
                .whereEqualTo("targetType", targetType)
                .snapshots()
                .map { snap ->
                    snap.toObjects(Comment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedBy { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeAllPostAndReelComments(): Flow<List<Comment>> = flow {
        requireUserId()
        val path = "comments"
        emitAll(
            db.collection("comments")
                .whereIn("targetType", listOf("POST", "REEL"))
                .snapshots()
                .map { snap ->
                    snap.toObjects(Comment::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addComment(
        targetId: String,
        targetType: String,
        authorProfile: UserProfile,
        text: String,
        parentCommentId: String = ""
    ): Result<String> = runCatching {
        val uid = requireUserId()
        require(text.isNotBlank()) { "Comment cannot be empty." }
        val commentRef = db.collection("comments").document()
        val payload = mapOf(
            "targetId" to targetId,
            "targetType" to targetType,
            "authorId" to uid,
            "authorUsername" to authorProfile.username,
            "authorDisplayName" to authorProfile.displayName,
            "text" to text.trim(),
            "parentCommentId" to parentCommentId,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            commentRef.set(payload).await()
            if (targetType == "POST") {
                val postRef = db.collection("posts").document(targetId)
                val snap = postRef.get().await()
                if (snap.exists()) {
                    val count = snap.getLong("commentCount") ?: 0L
                    postRef.update(
                        mapOf(
                            "commentCount" to (count + 1L),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                    val postAuthorId = snap.getString("authorId").orEmpty()
                    val mediaUrls = (snap.get("mediaUrls") as? List<*>)?.mapNotNull { it as? String }.orEmpty()
                    if (postAuthorId.isNotBlank() && postAuthorId != uid) {
                        createFirestoreNotification(
                            recipientId = postAuthorId,
                            actorId = uid,
                            actorUsername = authorProfile.username,
                            actorDisplayName = authorProfile.displayName,
                            actorAvatarUrl = authorProfile.avatarUrl,
                            type = "COMMENT",
                            message = "commented on your post",
                            detailText = "\"${text.trim().take(120)}\"",
                            targetId = targetId,
                            targetType = "POST",
                            targetMediaUrl = mediaUrls.firstOrNull().orEmpty()
                        )
                    }
                }
            } else if (targetType == "REEL") {
                val reelRef = db.collection("reels").document(targetId)
                val snap = reelRef.get().await()
                if (snap.exists()) {
                    val count = snap.getLong("commentCount") ?: 0L
                    reelRef.update(
                        mapOf(
                            "commentCount" to (count + 1L),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                    val reelAuthorId = snap.getString("authorId").orEmpty()
                    val videoUrl = snap.getString("videoUrl").orEmpty()
                    if (reelAuthorId.isNotBlank() && reelAuthorId != uid) {
                        createFirestoreNotification(
                            recipientId = reelAuthorId,
                            actorId = uid,
                            actorUsername = authorProfile.username,
                            actorDisplayName = authorProfile.displayName,
                            actorAvatarUrl = authorProfile.avatarUrl,
                            type = "COMMENT",
                            message = "commented on your reel",
                            detailText = "\"${text.trim().take(120)}\"",
                            targetId = targetId,
                            targetType = "REEL",
                            targetMediaUrl = videoUrl
                        )
                    }
                }
            }
            val actType = if (parentCommentId.isBlank()) "COMMENT" else "REPLY"
            logActivity(actType, targetId, "Commented: ${text.take(60)}")
            commentRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, commentRef.path)
            throw e
        }
    }

    // --- 4. Saved Posts, Saved Reels & Saved Music ---

    fun observeSavedReels(): Flow<List<SavedReel>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/saved_reels"
        emitAll(
            db.collection("users")
                .document(uid)
                .collection("saved_reels")
                .snapshots()
                .map { snap ->
                    snap.documents.mapNotNull { doc ->
                        runCatching {
                            doc.toObject(SavedReel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                                ?.copy(
                                    id = doc.id,
                                    reelId = (doc.getString("reelId") ?: doc.id).ifBlank { doc.id }
                                )
                        }.getOrNull()
                    }.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    fun observeSavedPosts(): Flow<List<SavedPost>> = flow {
        val uid = requireUserId()
        val path = "saved_posts"
        val savedPostsFlow = db.collection("saved_posts")
            .whereEqualTo("userId", uid)
            .snapshots()
            .map { snap ->
                snap.toObjects(SavedPost::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                emit(emptyList())
            }

        val savedReelsSubFlow = observeSavedReels().catch { emit(emptyList()) }

        emitAll(
            combine(savedPostsFlow, savedReelsSubFlow) { savedPosts, savedReels ->
                val merged = LinkedHashMap<String, SavedPost>()
                savedPosts.forEach { item ->
                    val key = item.postId.ifBlank { item.reelId }.ifBlank { item.id }
                    if (key.isNotBlank()) {
                        merged[key] = if (item.targetType == "REEL" && item.reelId.isBlank()) {
                            item.copy(reelId = item.postId)
                        } else {
                            item
                        }
                    }
                }
                savedReels.forEach { reelDoc ->
                    val key = reelDoc.reelId.ifBlank { reelDoc.postId }.ifBlank { reelDoc.id }
                    if (key.isNotBlank() && !merged.containsKey(key)) {
                        merged[key] = reelDoc.toSavedPost()
                    }
                }
                merged.values.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
            }
        )
    }

    suspend fun toggleSavePost(
        postId: String,
        targetType: String,
        authorUsername: String,
        captionPreview: String,
        mediaUrl: String
    ): Result<Boolean> = runCatching {
        val uid = requireUserId()
        val docId = "${uid}_${postId}"
        val ref = db.collection("saved_posts").document(docId)
        val userSavedReelRef = db.collection("users")
            .document(uid)
            .collection("saved_reels")
            .document(postId)
        try {
            val snap = ref.get().await()
            val userReelSnap = if (targetType == "REEL") {
                runCatching { userSavedReelRef.get().await() }.getOrNull()
            } else null
            val alreadySaved = snap.exists() || (userReelSnap?.exists() == true)
            if (alreadySaved) {
                if (snap.exists()) {
                    ref.delete().await()
                }
                if (targetType == "REEL") {
                    runCatching { userSavedReelRef.delete().await() }
                }
                false
            } else {
                val payload = mutableMapOf<String, Any>(
                    "userId" to uid,
                    "postId" to postId,
                    "targetType" to targetType,
                    "authorUsername" to authorUsername.ifBlank { "mitr_creator" }.take(40),
                    "captionPreview" to captionPreview.take(250),
                    "mediaUrl" to mediaUrl.take(1000),
                    "createdAt" to FieldValue.serverTimestamp()
                )
                if (targetType == "REEL") {
                    payload["reelId"] = postId
                }
                ref.set(payload).await()
                if (targetType == "REEL") {
                    runCatching {
                        userSavedReelRef.set(
                            mapOf(
                                "userId" to uid,
                                "reelId" to postId,
                                "postId" to postId,
                                "targetType" to "REEL",
                                "authorUsername" to authorUsername.ifBlank { "mitr_creator" }.take(40),
                                "captionPreview" to captionPreview.take(250),
                                "mediaUrl" to mediaUrl.take(1000),
                                "videoUrl" to mediaUrl.take(1000),
                                "createdAt" to FieldValue.serverTimestamp()
                            )
                        ).await()
                    }
                }
                true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            throw e
        }
    }

    suspend fun toggleSaveReel(reel: Reel): Result<Boolean> =
        toggleSavePost(
            postId = reel.id,
            targetType = "REEL",
            authorUsername = reel.authorUsername,
            captionPreview = reel.caption,
            mediaUrl = reel.videoUrl
        )

    fun observeSavedMusic(): Flow<List<SavedMusic>> = flow {
        val uid = requireUserId()
        val path = "saved_music"
        emitAll(
            db.collection("saved_music")
                .whereEqualTo("userId", uid)
                .snapshots()
                .map { snap ->
                    snap.toObjects(SavedMusic::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun toggleSaveMusic(track: MusicTrack): Result<Boolean> = runCatching {
        val uid = requireUserId()
        val safeTrackId = track.trackId.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(64).ifBlank { "track" }
        val docId = "${uid}_${safeTrackId}"
        val ref = db.collection("saved_music").document(docId)
        try {
            val snap = ref.get().await()
            if (snap.exists()) {
                ref.delete().await()
                false
            } else {
                ref.set(
                    mapOf(
                        "userId" to uid,
                        "trackId" to safeTrackId,
                        "title" to track.title.take(120),
                        "artist" to track.artist.take(120),
                        "coverUrl" to track.coverUrl.take(1000),
                        "audioUrl" to track.audioUrl.take(1000),
                        "durationMs" to track.durationMs.coerceAtLeast(1000L),
                        "trimStartMs" to track.trimStartMs.coerceAtLeast(0L),
                        "trimEndMs" to track.trimEndMs.coerceAtLeast(1000L),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, ref.path)
            throw e
        }
    }

    // --- 5. Stories (24-Hour Expiration + Close Friends + Music + Personal Story Archive) ---

    fun observeActiveStories(): Flow<List<Story>> = flow {
        val uid = requireUserId()
        val path = "stories"
        emitAll(
            db.collection("stories")
                .snapshots()
                .map { snap ->
                    val nowMillis = System.currentTimeMillis()
                    snap.documents.mapNotNull { doc ->
                        runCatching {
                            doc.toObject(Story::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                                ?.copy(id = doc.id)
                        }.getOrNull()
                    }
                        .filter {
                            (it.audience == "PUBLIC" || it.authorId == uid || it.allowedViewerIds.contains(uid)) &&
                                (it.expiresAtMillis > nowMillis || it.isHighlighted)
                        }
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    /**
     * Observes ALL stories authored by the signed-in user (both active 24h stories and expired
     * past stories >24h) for the organized Profile Story Archive section.
     */
    fun observeUserStoryArchive(): Flow<List<Story>> = flow {
        val uid = requireUserId()
        val path = "stories"
        emitAll(
            db.collection("stories")
                .whereEqualTo("authorId", uid)
                .snapshots()
                .map { snap ->
                    snap.documents.mapNotNull { doc ->
                        runCatching {
                            doc.toObject(Story::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                                ?.copy(id = doc.id)
                        }.getOrNull()
                    }
                        .filter { it.authorId == uid }
                        .sortedByDescending { it.storyCreatedMillis() }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun toggleStoryHighlight(story: Story, highlightTitle: String = "Highlights"): Result<Boolean> = runCatching {
        val uid = requireUserId()
        require(story.authorId == uid) { "Only the story author can highlight their story." }
        val storyRef = db.collection("stories").document(story.id)
        val newHighlighted = !story.isHighlighted
        try {
            storyRef.update(
                mapOf(
                    "isHighlighted" to newHighlighted,
                    "highlightTitle" to if (newHighlighted) highlightTitle.trim().take(50).ifBlank { "Highlights" } else ""
                )
            ).await()
            newHighlighted
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, storyRef.path)
            throw e
        }
    }

    fun observeCloseFriendsStories(): Flow<List<Story>> = flow {
        val uid = requireUserId()
        val path = "stories"
        emitAll(
            db.collection("stories")
                .whereArrayContains("allowedViewerIds", uid)
                .snapshots()
                .map { snap ->
                    val nowMillis = System.currentTimeMillis()
                    snap.toObjects(Story::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .filter { it.expiresAtMillis > nowMillis }
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createStory(
        authorProfile: UserProfile,
        mediaUrl: String,
        mediaType: String,
        caption: String,
        overlayText: String,
        stickerLabel: String,
        audience: String,
        closeFriendUids: List<String>,
        musicTrack: MusicTrack? = null,
        taggedUsernames: List<String> = emptyList(),
        collaboratorUsernames: List<String> = emptyList()
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val storyRef = db.collection("stories").document()
        val expiresAtMillis = System.currentTimeMillis() + 24L * 60L * 60L * 1000L
        val allowedViewers = (closeFriendUids + uid).distinct().take(200)
        val mentionedInText = Regex("@([a-zA-Z0-9_.]+)").findAll("$caption $overlayText $stickerLabel")
            .map { it.groupValues[1].lowercase(Locale.ROOT) }
            .distinct()
            .toList()
        val finalTagged = (taggedUsernames.map { it.trim().removePrefix("@").lowercase(Locale.ROOT) } + mentionedInText)
            .filter { it.isNotBlank() }
            .distinct()
            .take(15)
        val finalCollabs = collaboratorUsernames
            .map { it.trim().removePrefix("@").lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() && it != authorProfile.username.lowercase(Locale.ROOT) }
            .distinct()
            .take(10)
        val payload = mutableMapOf<String, Any>(
            "authorId" to uid,
            "authorUsername" to authorProfile.username,
            "authorDisplayName" to authorProfile.displayName,
            "mediaUrl" to mediaUrl.trim(),
            "mediaType" to mediaType,
            "caption" to caption.trim(),
            "overlayText" to overlayText.trim(),
            "stickerLabel" to stickerLabel.trim(),
            "audience" to audience,
            "allowedViewerIds" to allowedViewers,
            "viewerIds" to listOf(uid),
            "taggedUsernames" to finalTagged,
            "collaboratorUsernames" to finalCollabs,
            "reactionEntries" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "expiresAtMillis" to expiresAtMillis
        )
        if (musicTrack != null && musicTrack.title.isNotBlank()) {
            payload["musicTrackId"] = musicTrack.trackId.take(120)
            payload["musicTitle"] = musicTrack.title.take(120)
            payload["musicArtist"] = musicTrack.artist.take(120)
            payload["musicCoverUrl"] = musicTrack.coverUrl.take(1000)
            payload["musicUrl"] = musicTrack.audioUrl.take(1000)
            payload["musicStartMs"] = musicTrack.trimStartMs.coerceAtLeast(0L)
            payload["musicEndMs"] = musicTrack.trimEndMs.coerceAtLeast(1000L)
        }
        try {
            storyRef.set(payload).await()
            notifyCollaboratorsAndMentions(
                authorProfile = authorProfile,
                collaboratorUsernames = finalCollabs,
                taggedUsernames = finalTagged,
                targetId = storyRef.id,
                targetType = "STORY",
                targetMediaUrl = mediaUrl,
                captionPreview = caption
            )
            storyRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, storyRef.path)
            throw e
        }
    }

    suspend fun recordStoryView(story: Story): Result<Unit> = runCatching {
        val uid = requireUserId()
        if (story.viewerIds.contains(uid)) return@runCatching
        val storyRef = db.collection("stories").document(story.id)
        try {
            val updatedViewers = (story.viewerIds + uid).distinct().take(500)
            storyRef.update("viewerIds", updatedViewers).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, storyRef.path)
            throw e
        }
    }

    /**
     * Observes all emoji reactions linked to a specific Story ID from `/story_reactions`.
     */
    fun observeStoryReactions(storyId: String): Flow<List<StoryReaction>> = flow {
        requireUserId()
        val cleanStoryId = storyId.trim()
        if (cleanStoryId.isBlank()) {
            emit(emptyList())
            return@flow
        }
        val path = "story_reactions"
        emitAll(
            db.collection("story_reactions")
                .whereEqualTo("storyId", cleanStoryId)
                .snapshots()
                .map { snap ->
                    snap.documents.mapNotNull { doc ->
                        runCatching {
                            doc.toObject(StoryReaction::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                                ?.copy(id = doc.id)
                        }.getOrNull()
                    }.sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    /**
     * Records an expressive emoji feeling reaction (and optional reply note) on a Story in Firestore,
     * storing the reaction document in `/story_reactions/{storyId}_{uid}` linked to `story.id` as well as
     * updating `/stories/{storyId}`, and dispatches a real-time STORY_REACTION notification to the story author.
     */
    suspend fun reactToStoryWithEmoji(
        story: Story,
        actorProfile: UserProfile,
        emoji: String,
        replyText: String = ""
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val cleanEmoji = emoji.trim().ifBlank { "❤️" }.take(16)
        val feelingLabel = storyFeelingLabelForEmoji(cleanEmoji)
        val cleanUsername = actorProfile.username.trim().removePrefix("@").ifBlank { "mitr_user" }.take(32)
        val reactionToken = "$uid:$cleanUsername:$cleanEmoji"
        val storyRef = db.collection("stories").document(story.id)
        val reactionDocId = "${story.id}_$uid".replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(120)
        val reactionRef = db.collection("story_reactions").document(reactionDocId)
        try {
            // 1. Store reaction document in Firestore linked to the specific story ID
            val reactionPayload = mapOf(
                "storyId" to story.id,
                "storyAuthorId" to story.authorId,
                "userId" to uid,
                "username" to cleanUsername,
                "displayName" to actorProfile.displayName.ifBlank { cleanUsername }.take(75),
                "emoji" to cleanEmoji,
                "feelingLabel" to feelingLabel,
                "replyText" to replyText.trim().take(250),
                "createdAt" to FieldValue.serverTimestamp()
            )
            runCatching {
                reactionRef.set(reactionPayload).await()
            }

            // 2. Also update reactionEntries on /stories/{storyId} so inline story cards & archive reflect it immediately
            val snap = storyRef.get().await()
            val existingReactions = if (snap.exists()) {
                (snap.get("reactionEntries") as? List<*>)?.mapNotNull { it as? String }.orEmpty()
            } else {
                story.reactionEntries
            }
            val existingViewers = if (snap.exists()) {
                (snap.get("viewerIds") as? List<*>)?.mapNotNull { it as? String }.orEmpty()
            } else {
                story.viewerIds
            }
            val filteredWithoutPriorFromUser = existingReactions.filterNot { it.startsWith("$uid:") }
            val updatedReactions = (filteredWithoutPriorFromUser + reactionToken).distinct().takeLast(500)
            val updatedViewers = (existingViewers + uid).distinct().take(500)
            if (snap.exists()) {
                storyRef.update(
                    mapOf(
                        "reactionEntries" to updatedReactions,
                        "viewerIds" to updatedViewers
                    )
                ).await()
            }

            if (story.authorId.isNotBlank() && story.authorId != uid) {
                val trimmedNote = replyText.trim().take(140)
                val notifMessage = if (trimmedNote.isNotBlank()) {
                    "reacted $cleanEmoji ($feelingLabel) & replied to your story"
                } else {
                    "reacted $cleanEmoji ($feelingLabel) to your story"
                }
                val notifDetail = when {
                    trimmedNote.isNotBlank() -> "$cleanEmoji \"$trimmedNote\""
                    story.caption.isNotBlank() -> "$cleanEmoji on \"${story.caption.take(70)}\""
                    story.overlayText.isNotBlank() -> "$cleanEmoji on \"${story.overlayText.take(70)}\""
                    else -> "Sent $cleanEmoji ($feelingLabel) reaction to your story"
                }
                createFirestoreNotification(
                    recipientId = story.authorId,
                    actorId = uid,
                    actorUsername = cleanUsername,
                    actorDisplayName = actorProfile.displayName.ifBlank { cleanUsername },
                    actorAvatarUrl = actorProfile.avatarUrl,
                    type = "STORY_REACTION",
                    message = notifMessage,
                    detailText = notifDetail,
                    targetId = story.id,
                    targetType = "STORY",
                    targetMediaUrl = story.mediaUrl
                )
            }
            logActivity(
                activityType = "STORY_REPLY",
                targetId = story.id,
                summary = "Reacted $cleanEmoji ($feelingLabel) to @${story.authorUsername}'s story"
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, storyRef.path)
            throw e
        }
    }

    suspend fun deleteStory(storyId: String): Result<Unit> = runCatching {
        requireUserId()
        val storyRef = db.collection("stories").document(storyId)
        try {
            storyRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, storyRef.path)
            throw e
        }
    }

    // --- 6. Reels (Short Vertical Video Feed + Trimmed Music) ---

    fun observePublicReels(): Flow<List<Reel>> = flow {
        val uid = requireUserId()
        val path = "reels"
        emitAll(
            db.collection("reels")
                .snapshots()
                .map { snap ->
                    snap.documents.mapNotNull { doc ->
                        runCatching {
                            doc.toObject(Reel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                                ?.copy(id = doc.id)
                        }.getOrNull()
                    }
                        .filter { it.visibility.isBlank() || it.visibility.equals("PUBLIC", ignoreCase = true) || it.authorId == uid }
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                        .distinctBy { it.id }
                        .distinctBy { reel ->
                            val timeBucket = (reel.createdAt?.seconds ?: 0L) / 30L
                            if (reel.videoUrl.isBlank() && reel.caption.isBlank()) {
                                reel.id
                            } else {
                                "${reel.authorId}|${reel.videoUrl.trim()}|${reel.caption.trim()}|$timeBucket"
                            }
                        }
                }
                .catch { error ->
                    Log.e("FirestoreReelsSync", "Error syncing reels snapshot (uid=$uid, path=$path): ${error.message}", error)
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    fun observeUserReels(authorId: String, authorUsername: String = ""): Flow<List<Reel>> = flow {
        val uid = requireUserId()
        val cleanUsername = authorUsername.trim().removePrefix("@")
        val path = "reels"
        emitAll(
            db.collection("reels")
                .snapshots()
                .map { snap ->
                    val allDocs = snap.documents.mapNotNull { doc ->
                        runCatching {
                            doc.toObject(Reel::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                                ?.copy(id = doc.id)
                        }.getOrNull()
                    }
                    filterUserReelsForProfile(
                        reels = allDocs,
                        userId = authorId,
                        profileDocId = authorId,
                        username = cleanUsername,
                        isOwner = authorId == uid,
                        currentUserId = uid
                    )
                }
                .catch { error ->
                    Log.e("FirestoreReelsSync", "Error syncing user reels (authorId=$authorId, path=$path): ${error.message}", error)
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun deleteReel(reelId: String): Result<Unit> = runCatching {
        requireUserId()
        val reelRef = db.collection("reels").document(reelId)
        try {
            reelRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, reelRef.path)
            throw e
        }
    }

    suspend fun createReel(
        authorProfile: UserProfile,
        videoUrl: String,
        caption: String,
        audioTrackName: String,
        durationSeconds: Long,
        visibility: String = "PUBLIC",
        musicTrack: MusicTrack? = null,
        taggedUsernames: List<String> = emptyList(),
        collaboratorUsernames: List<String> = emptyList(),
        videoTrimStartMs: Long = 0L,
        videoTrimEndMs: Long = durationSeconds.coerceIn(15L, 60L) * 1000L
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val cleanVideoUrl = videoUrl.trim()
        val cleanCaption = caption.trim()

        // Guard against accidental rapid double-tap duplicate Reel creation
        val existingRecentDuplicate = runCatching {
            db.collection("reels")
                .whereEqualTo("authorId", uid)
                .get()
                .await()
                .documents
                .firstOrNull { doc ->
                    val docUrl = doc.getString("videoUrl")?.trim().orEmpty()
                    val docCaption = doc.getString("caption")?.trim().orEmpty()
                    val createdSec = doc.getTimestamp("createdAt")?.seconds ?: 0L
                    val nowSec = Timestamp.now().seconds
                    docUrl == cleanVideoUrl && docCaption == cleanCaption && kotlin.math.abs(nowSec - createdSec) <= 30L
                }
        }.getOrNull()
        if (existingRecentDuplicate != null) {
            return@runCatching existingRecentDuplicate.id
        }

        val reelRef = db.collection("reels").document()
        val displayAudio = if (musicTrack != null && musicTrack.title.isNotBlank()) {
            "${musicTrack.title} • ${musicTrack.artist}"
        } else {
            audioTrackName.trim().ifBlank { "Original Audio - @${authorProfile.username}" }
        }
        val mentionedInCaption = Regex("@([a-zA-Z0-9_.]+)").findAll(caption)
            .map { it.groupValues[1].lowercase(Locale.ROOT) }
            .distinct()
            .toList()
        val finalTagged = (taggedUsernames.map { it.trim().removePrefix("@").lowercase(Locale.ROOT) } + mentionedInCaption)
            .filter { it.isNotBlank() }
            .distinct()
            .take(15)
        val finalCollabs = collaboratorUsernames
            .map { it.trim().removePrefix("@").lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() && it != authorProfile.username.lowercase(Locale.ROOT) }
            .distinct()
            .take(10)
        val clampedDurationSec = durationSeconds.coerceIn(15L, 60L)
        val safeTrimStart = videoTrimStartMs.coerceIn(0L, 600_000L)
        val safeTrimEnd = videoTrimEndMs.coerceIn(safeTrimStart + 1_000L, 600_000L)
        val payload = mutableMapOf<String, Any>(
            "authorId" to uid,
            "authorUsername" to authorProfile.username,
            "authorDisplayName" to authorProfile.displayName,
            "videoUrl" to videoUrl.trim(),
            "caption" to caption.trim(),
            "audioTrackName" to displayAudio.take(120),
            "durationSeconds" to clampedDurationSec,
            "videoTrimStartMs" to safeTrimStart,
            "videoTrimEndMs" to safeTrimEnd,
            "visibility" to visibility,
            "likeCount" to 0L,
            "commentCount" to 0L,
            "viewCount" to 0L,
            "likedByUids" to emptyList<String>(),
            "taggedUsernames" to finalTagged,
            "collaboratorUsernames" to finalCollabs,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        if (musicTrack != null && musicTrack.title.isNotBlank()) {
            payload["musicTrackId"] = musicTrack.trackId.take(120)
            payload["musicTitle"] = musicTrack.title.take(120)
            payload["musicArtist"] = musicTrack.artist.take(120)
            payload["musicCoverUrl"] = musicTrack.coverUrl.take(1000)
            payload["musicUrl"] = musicTrack.audioUrl.take(1000)
            payload["musicStartMs"] = musicTrack.trimStartMs.coerceAtLeast(0L)
            payload["musicEndMs"] = musicTrack.trimEndMs.coerceAtLeast(1000L)
        }
        try {
            reelRef.set(payload).await()
            notifyCollaboratorsAndMentions(
                authorProfile = authorProfile,
                collaboratorUsernames = finalCollabs,
                taggedUsernames = finalTagged,
                targetId = reelRef.id,
                targetType = "REEL",
                targetMediaUrl = videoUrl,
                captionPreview = caption
            )
            logActivity("REEL_CREATED", reelRef.id, "Published reel: ${caption.take(50)}")
            reelRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, reelRef.path)
            throw e
        }
    }

    suspend fun toggleLikeReel(reel: Reel): Result<Unit> = runCatching {
        val uid = requireUserId()
        val reelRef = db.collection("reels").document(reel.id)
        try {
            val alreadyLiked = reel.likedByUids.contains(uid)
            val updatedLikes = if (alreadyLiked) {
                reel.likedByUids.filter { it != uid }
            } else {
                (reel.likedByUids + uid).distinct().take(500)
            }
            reelRef.update(
                mapOf(
                    "likedByUids" to updatedLikes,
                    "likeCount" to updatedLikes.size.toLong(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            if (!alreadyLiked && reel.authorId.isNotBlank() && reel.authorId != uid) {
                val actorSnap = db.collection("users").document(uid).get().await()
                val actorUsername = actorSnap.getString("username") ?: (auth.currentUser?.displayName ?: "mitr_user")
                val actorDisplayName = actorSnap.getString("displayName") ?: actorUsername
                val actorAvatarUrl = actorSnap.getString("avatarUrl") ?: ""
                createFirestoreNotification(
                    recipientId = reel.authorId,
                    actorId = uid,
                    actorUsername = actorUsername,
                    actorDisplayName = actorDisplayName,
                    actorAvatarUrl = actorAvatarUrl,
                    type = "LIKE",
                    message = "liked your reel",
                    detailText = reel.caption.take(100),
                    targetId = reel.id,
                    targetType = "REEL",
                    targetMediaUrl = reel.videoUrl
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, reelRef.path)
            throw e
        }
    }

    suspend fun incrementReelView(reel: Reel): Result<Unit> = runCatching {
        requireUserId()
        val reelRef = db.collection("reels").document(reel.id)
        try {
            reelRef.update(
                mapOf(
                    "viewCount" to (reel.viewCount + 1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, reelRef.path)
            throw e
        }
    }

    // --- 7. Follows & Follow Requests ---

    fun observeOutgoingFollows(): Flow<List<Follow>> = flow {
        val uid = requireUserId()
        val path = "follows"
        emitAll(
            db.collection("follows")
                .whereEqualTo("followerId", uid)
                .snapshots()
                .map { snap -> snap.toObjects(Follow::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeIncomingFollows(): Flow<List<Follow>> = flow {
        val uid = requireUserId()
        val path = "follows"
        emitAll(
            db.collection("follows")
                .whereEqualTo("followingId", uid)
                .snapshots()
                .map { snap -> snap.toObjects(Follow::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeFollowersForUser(targetUserId: String): Flow<List<Follow>> = flow {
        requireUserId()
        val path = "follows"
        emitAll(
            db.collection("follows")
                .whereEqualTo("followingId", targetUserId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(Follow::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .filter { it.status == "ACCEPTED" }
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeFollowingForUser(targetUserId: String): Flow<List<Follow>> = flow {
        requireUserId()
        val path = "follows"
        emitAll(
            db.collection("follows")
                .whereEqualTo("followerId", targetUserId)
                .snapshots()
                .map { snap ->
                    snap.toObjects(Follow::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .filter { it.status == "ACCEPTED" }
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun followOrUnfollowUser(
        currentUserProfile: UserProfile,
        targetProfile: UserProfile
    ): Result<String> = runCatching {
        val uid = requireUserId()
        require(uid != targetProfile.userId) { "Cannot follow yourself." }
        val docId = "${uid}_${targetProfile.userId}"
        val followRef = db.collection("follows").document(docId)
        try {
            val snap = followRef.get().await()
            if (snap.exists()) {
                val wasAccepted = snap.getString("status") == "ACCEPTED"
                followRef.delete().await()
                if (wasAccepted) {
                    adjustFollowCounters(uid, targetProfile.userId, delta = -1L)
                }
                "UNFOLLOWED"
            } else {
                val status = if (targetProfile.isPrivate) "PENDING" else "ACCEPTED"
                followRef.set(
                    mapOf(
                        "followerId" to uid,
                        "followerUsername" to currentUserProfile.username,
                        "followingId" to targetProfile.userId,
                        "followingUsername" to targetProfile.username,
                        "status" to status,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                ).await()
                if (status == "ACCEPTED") {
                    adjustFollowCounters(uid, targetProfile.userId, delta = 1L)
                }
                createFirestoreNotification(
                    recipientId = targetProfile.userId,
                    actorId = uid,
                    actorUsername = currentUserProfile.username,
                    actorDisplayName = currentUserProfile.displayName,
                    actorAvatarUrl = currentUserProfile.avatarUrl,
                    type = "FOLLOW",
                    message = if (status == "PENDING") "requested to follow you" else "started following you",
                    detailText = if (status == "PENDING") "Private account follow request" else "",
                    targetId = docId,
                    targetType = "FOLLOW",
                    targetMediaUrl = ""
                )
                logActivity("FOLLOW", targetProfile.userId, "Followed @${targetProfile.username} ($status)")
                status
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, followRef.path)
            throw e
        }
    }

    suspend fun respondToFollowRequest(follow: Follow, accept: Boolean): Result<Unit> = runCatching {
        val uid = requireUserId()
        val followRef = db.collection("follows").document(follow.id)
        try {
            if (accept) {
                followRef.update("status", "ACCEPTED").await()
                adjustFollowCounters(follow.followerId, uid, delta = 1L)
            } else {
                followRef.delete().await()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, followRef.path)
            throw e
        }
    }

    private suspend fun adjustFollowCounters(followerUid: String, followingUid: String, delta: Long) {
        val followerRef = db.collection("users").document(followerUid)
        val followingRef = db.collection("users").document(followingUid)
        val followerSnap = followerRef.get().await()
        if (followerSnap.exists()) {
            val currentFollowing = (followerSnap.getLong("followingCount") ?: 0L)
            followerRef.update(
                mapOf(
                    "followingCount" to (currentFollowing + delta).coerceAtLeast(0L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
        val followingSnap = followingRef.get().await()
        if (followingSnap.exists()) {
            val currentFollowers = (followingSnap.getLong("followerCount") ?: 0L)
            followingRef.update(
                mapOf(
                    "followerCount" to (currentFollowers + delta).coerceAtLeast(0L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    // --- 8. Direct Messaging & Message Requests (with On-Device Room Mobile Storage) ---

    fun observeConversations(): Flow<List<Conversation>> = flow {
        val uid = requireUserId()
        val path = "conversations"
        val cloudFlow = db.collection("conversations")
            .whereArrayContains("participantIds", uid)
            .snapshots()
            .map { snap ->
                val convs = snap.toObjects(Conversation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                if (localDao != null && convs.isNotEmpty()) {
                    runCatching {
                        localDao.upsertLocalConversations(convs.map { it.toLocalEntity(uid) })
                    }
                }
                convs
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                emit(emptyList())
            }

        if (localDao == null) {
            emitAll(cloudFlow)
        } else {
            val localFlow = localDao.observeLocalConversations(uid)
                .map { entities -> entities.map { it.toConversation() } }
                .catch { emit(emptyList()) }
            emitAll(
                combine(cloudFlow, localFlow) { cloudList, localList ->
                    val merged = LinkedHashMap<String, Conversation>()
                    cloudList.forEach { if (it.id.isNotBlank()) merged[it.id] = it }
                    localList.forEach { loc ->
                        if (loc.id.isNotBlank() && !merged.containsKey(loc.id)) {
                            merged[loc.id] = loc
                        }
                    }
                    merged.values.sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
            )
        }
    }

    fun observeMessages(conversationId: String): Flow<List<DirectMessage>> = flow {
        val uid = requireUserId()
        val path = "conversations/$conversationId/messages"
        val cloudFlow = db.collection("conversations").document(conversationId)
            .collection("messages")
            .whereArrayContains("participantIds", uid)
            .snapshots()
            .map { snap ->
                val cloudMessages = snap.toObjects(DirectMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                    .sortedBy { it.createdAt ?: Timestamp(0, 0) }
                if (localDao != null && cloudMessages.isNotEmpty()) {
                    runCatching {
                        localDao.upsertLocalMessages(cloudMessages.map { it.toLocalEntity(isOffloadedFromCloud = false) })
                    }
                }
                cloudMessages
            }
            .catch { error ->
                if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                emit(emptyList())
            }

        if (localDao == null) {
            emitAll(cloudFlow)
        } else {
            val localFlow = localDao.observeLocalMessages(conversationId)
                .map { entities -> entities.map { it.toDirectMessage() } }
                .catch { emit(emptyList()) }
            emitAll(
                combine(cloudFlow, localFlow) { cloudMessages, localMessages ->
                    val byId = LinkedHashMap<String, DirectMessage>()
                    localMessages.forEach { msg ->
                        if (msg.id.isNotBlank()) byId[msg.id] = msg
                    }
                    cloudMessages.forEach { msg ->
                        if (msg.id.isNotBlank()) byId[msg.id] = msg
                    }
                    byId.values.sortedBy { it.createdAt?.toDate()?.time ?: 0L }
                }
            )
        }
    }

    suspend fun startOrGetConversation(
        currentUser: UserProfile,
        targetUser: UserProfile,
        isMutualOrFollowed: Boolean
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val sortedIds = listOf(uid, targetUser.userId).sorted()
        val convId = "${sortedIds[0]}_${sortedIds[1]}"
        val convRef = db.collection("conversations").document(convId)
        try {
            val snap = convRef.get().await()
            if (!snap.exists()) {
                val payload = mapOf(
                    "participantIds" to sortedIds,
                    "participantUsernames" to listOf(currentUser.username, targetUser.username),
                    "initiatorId" to uid,
                    "lastMessageText" to "Conversation started",
                    "lastSenderId" to uid,
                    "status" to (if (isMutualOrFollowed) "ACTIVE" else "REQUESTED"),
                    "mutedByUids" to emptyList<String>(),
                    "unreadCount" to 0L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                convRef.set(payload).await()
            } else if (isMutualOrFollowed && snap.getString("status") == "REQUESTED") {
                convRef.update(
                    mapOf(
                        "status" to "ACTIVE",
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            convId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, convRef.path)
            throw e
        }
    }

    suspend fun sharePostToUser(
        sender: UserProfile,
        recipient: UserProfile,
        post: Post,
        isMutualOrFollowed: Boolean
    ): Result<String> = runCatching {
        val uid = requireUserId()
        val convId = startOrGetConversation(sender, recipient, isMutualOrFollowed).getOrThrow()
        val convRef = db.collection("conversations").document(convId)
        val msgRef = convRef.collection("messages").document()
        val sortedIds = listOf(uid, recipient.userId).sorted()
        val firstMedia = post.mediaUrls.firstOrNull() ?: ""
        val shareText = buildString {
            append("Shared @${post.authorUsername}'s post")
            if (post.caption.isNotBlank()) {
                append(": \"${post.caption.take(160)}\"")
            }
            if (post.musicTitle.isNotBlank()) {
                append(" ♫ ${post.musicTitle}")
            }
        }
        val payload = mapOf(
            "conversationId" to convId,
            "participantIds" to sortedIds,
            "senderId" to uid,
            "senderUsername" to sender.username,
            "text" to shareText.take(500),
            "mediaUrl" to firstMedia.take(1000),
            "mediaType" to if (firstMedia.isNotBlank()) "IMAGE" else "NONE",
            "replyToMessageId" to "",
            "deliveryStatus" to "SENT",
            "readByUids" to listOf(uid),
            "readAtMillis" to 0L,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            msgRef.set(payload).await()
            convRef.update(
                mapOf(
                    "lastMessageText" to shareText.take(200),
                    "lastSenderId" to uid,
                    "unreadCount" to 1L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            convId
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            throw e
        }
    }

    suspend fun sendDirectMessage(
        conversation: Conversation,
        senderUsername: String,
        text: String,
        mediaUrl: String = "",
        mediaType: String = "NONE",
        replyToMessageId: String = ""
    ): Result<String> = runCatching {
        val uid = requireUserId()
        require(text.isNotBlank() || mediaUrl.isNotBlank()) { "Message cannot be empty." }
        val convRef = db.collection("conversations").document(conversation.id)
        val msgRef = convRef.collection("messages").document()
        val displaySummary = if (text.isNotBlank()) text.trim() else "[Media attachment]"
        val payload = mapOf(
            "conversationId" to conversation.id,
            "participantIds" to conversation.participantIds,
            "senderId" to uid,
            "senderUsername" to senderUsername,
            "text" to displaySummary,
            "mediaUrl" to mediaUrl.trim(),
            "mediaType" to mediaType,
            "replyToMessageId" to replyToMessageId,
            "deliveryStatus" to "SENT",
            "readByUids" to listOf(uid),
            "readAtMillis" to 0L,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            // Immediately persist to user's phone storage (Room) as well as Firestore
            if (localDao != null) {
                runCatching {
                    val localMsg = DirectMessage(
                        id = msgRef.id,
                        conversationId = conversation.id,
                        participantIds = conversation.participantIds,
                        senderId = uid,
                        senderUsername = senderUsername,
                        text = displaySummary,
                        mediaUrl = mediaUrl.trim(),
                        mediaType = mediaType,
                        replyToMessageId = replyToMessageId,
                        deliveryStatus = "SENT",
                        readByUids = listOf(uid),
                        readAtMillis = 0L,
                        createdAt = Timestamp.now()
                    )
                    localDao?.upsertLocalMessage(localMsg.toLocalEntity(isOffloadedFromCloud = false))
                }
            }
            msgRef.set(payload).await()
            convRef.update(
                mapOf(
                    "lastMessageText" to displaySummary.take(200),
                    "lastSenderId" to uid,
                    "unreadCount" to (conversation.unreadCount + 1L),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            msgRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, msgRef.path)
            throw e
        }
    }

    suspend fun updateConversationStatus(conversationId: String, status: String): Result<Unit> = runCatching {
        requireUserId()
        val convRef = db.collection("conversations").document(conversationId)
        try {
            convRef.update(
                mapOf(
                    "status" to status,
                    "unreadCount" to 0L,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, convRef.path)
            throw e
        }
    }

    suspend fun deleteDirectMessage(conversationId: String, messageId: String): Result<Unit> = runCatching {
        requireUserId()
        if (localDao != null) {
            runCatching { localDao?.deleteLocalMessage(messageId) }
        }
        val msgRef = db.collection("conversations").document(conversationId)
            .collection("messages").document(messageId)
        try {
            msgRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, msgRef.path)
            throw e
        }
    }

    /**
     * Marks a conversation as read in Firestore and updates incoming messages'
     * deliveryStatus to "READ" + appends the reader's UID to readByUids in real time
     * when readReceiptsEnabled is true.
     */
    suspend fun markConversationRead(
        conversationId: String,
        readReceiptsEnabled: Boolean = true
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val convRef = db.collection("conversations").document(conversationId)
        try {
            val convSnap = convRef.get().await()
            if (convSnap.exists()) {
                val lastSender = convSnap.getString("lastSenderId").orEmpty()
                val unreadCount = convSnap.getLong("unreadCount") ?: 0L
                if (unreadCount > 0L && lastSender != uid) {
                    convRef.update(
                        mapOf(
                            "unreadCount" to 0L,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }

            if (readReceiptsEnabled) {
                val nowMillis = System.currentTimeMillis()
                val messagesSnap = convRef.collection("messages")
                    .whereArrayContains("participantIds", uid)
                    .get()
                    .await()
                for (doc in messagesSnap.documents) {
                    val senderId = doc.getString("senderId").orEmpty()
                    val currentStatus = doc.getString("deliveryStatus").orEmpty()
                    val existingReadBy = (doc.get("readByUids") as? List<*>)
                        ?.mapNotNull { it as? String }
                        .orEmpty()
                    if (senderId.isNotBlank() && senderId != uid &&
                        (currentStatus != "READ" || !existingReadBy.contains(uid))
                    ) {
                        val updatedReadBy = (existingReadBy + senderId + uid)
                            .filter { it.isNotBlank() }
                            .distinct()
                            .take(10)
                        doc.reference.update(
                            mapOf(
                                "deliveryStatus" to "READ",
                                "readByUids" to updatedReadBy,
                                "readAtMillis" to nowMillis
                            )
                        ).await()
                    }
                }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, convRef.path)
            throw e
        }
    }

    // --- 9. Search & Discovery ---

    fun observeAllDiscoverableUsers(): Flow<List<UserProfile>> = flow {
        requireUserId()
        val path = "users"
        emitAll(
            db.collection("users")
                .snapshots()
                .map { snap ->
                    snap.documents.mapNotNull { doc ->
                        doc.toNormalizedUserProfile()
                    }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    emit(emptyList())
                }
        )
    }

    suspend fun searchUsersInFirestore(rawQuery: String): Result<List<UserProfile>> = runCatching {
        requireUserId()
        val rawTrimmed = rawQuery.trim().removePrefix("@").removePrefix("#")
        val clean = rawTrimmed.lowercase(Locale.ROOT)
        val path = "users"
        try {
            val usersById = linkedMapOf<String, UserProfile>()

            // 1. Direct Firestore prefix queries on 'users' collection by username and displayName
            if (clean.isNotBlank()) {
                val usernameQuerySnap = runCatching {
                    db.collection("users")
                        .whereGreaterThanOrEqualTo("username", clean)
                        .whereLessThanOrEqualTo("username", clean + "\uf8ff")
                        .get()
                        .await()
                }.getOrNull()
                usernameQuerySnap?.documents?.mapNotNull { it.toNormalizedUserProfile() }?.forEach { profile ->
                    if (profile.userId.isNotBlank()) {
                        usersById[profile.userId] = profile
                    }
                }

                val displayNameQuerySnap = runCatching {
                    db.collection("users")
                        .whereGreaterThanOrEqualTo("displayName", rawTrimmed)
                        .whereLessThanOrEqualTo("displayName", rawTrimmed + "\uf8ff")
                        .get()
                        .await()
                }.getOrNull()
                displayNameQuerySnap?.documents?.mapNotNull { it.toNormalizedUserProfile() }?.forEach { profile ->
                    if (profile.userId.isNotBlank()) {
                        usersById[profile.userId] = profile
                    }
                }
                val titleCased = rawTrimmed.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                }
                if (titleCased != rawTrimmed) {
                    val titleCaseDisplayNameSnap = runCatching {
                        db.collection("users")
                            .whereGreaterThanOrEqualTo("displayName", titleCased)
                            .whereLessThanOrEqualTo("displayName", titleCased + "\uf8ff")
                            .get()
                            .await()
                    }.getOrNull()
                    titleCaseDisplayNameSnap?.documents?.mapNotNull { it.toNormalizedUserProfile() }?.forEach { profile ->
                        if (profile.userId.isNotBlank()) {
                            usersById[profile.userId] = profile
                        }
                    }
                }
            }

            // 2. Full scan of /users collection for case-insensitive partial matching on username & displayName
            val snap = runCatching { db.collection("users").get().await() }.getOrNull()
            snap?.documents?.mapNotNull { it.toNormalizedUserProfile() }?.forEach { profile ->
                if (profile.userId.isNotBlank()) {
                    usersById[profile.userId] = profile
                }
            }

            // 3. Also merge any username claims from /usernames so every registered handle is searchable
            val usernamesSnap = runCatching { db.collection("usernames").get().await() }.getOrNull()
            usernamesSnap?.documents?.forEach { doc ->
                val claimUid = doc.getString("userId")?.trim().orEmpty()
                val claimName = doc.getString("username")?.trim()?.removePrefix("@").orEmpty().ifBlank { doc.id }
                if (claimUid.isNotBlank() && !usersById.containsKey(claimUid)) {
                    usersById[claimUid] = UserProfile(
                        id = claimUid,
                        userId = claimUid,
                        username = claimName,
                        displayName = claimName,
                        isVerified = isFounderVipAccount(null, claimName)
                    )
                }
            }

            // 4. Also merge any post/reel authors so creators are always discoverable with real names
            val postsSnap = runCatching { db.collection("posts").get().await() }.getOrNull()
            postsSnap?.documents?.forEach { doc ->
                val authorId = doc.getString("authorId")?.trim().orEmpty()
                val authorUsername = doc.getString("authorUsername")?.trim()?.removePrefix("@").orEmpty()
                val authorDisplayName = doc.getString("authorDisplayName")?.trim().orEmpty()
                val authorAvatarUrl = doc.getString("authorAvatarUrl")?.trim().orEmpty()
                if (authorId.isNotBlank() && !usersById.containsKey(authorId) && authorUsername.isNotBlank()) {
                    usersById[authorId] = UserProfile(
                        id = authorId,
                        userId = authorId,
                        username = authorUsername,
                        displayName = authorDisplayName.ifBlank { authorUsername },
                        avatarUrl = authorAvatarUrl,
                        isVerified = isFounderVipAccount(null, authorUsername)
                    )
                }
            }

            val allUsers = usersById.values.toList()
            filterAndRankUsersByQuery(allUsers, rawQuery)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }

    suspend fun searchPostsByHashtagInFirestore(hashtag: String): Result<List<Post>> = runCatching {
        requireUserId()
        val cleanTag = hashtag.trim().removePrefix("#").lowercase(Locale.ROOT)
        require(cleanTag.isNotBlank()) { "Hashtag cannot be empty." }
        val path = "posts"
        try {
            val snap = db.collection("posts")
                .whereArrayContains("hashtags", cleanTag)
                .get()
                .await()
            snap.documents.mapNotNull { doc ->
                runCatching {
                    doc.toObject(Post::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = doc.id)
                }.getOrNull()
            }
                .filter { it.visibility.isBlank() || it.visibility == "PUBLIC" }
                .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            throw e
        }
    }

    suspend fun searchFeedInFirestore(rawQuery: String): Result<FirestoreFeedSearchResult> = runCatching {
        val uid = requireUserId()
        val trimmed = rawQuery.trim()
        if (trimmed.isBlank()) {
            return@runCatching FirestoreFeedSearchResult(query = "")
        }
        val cleanToken = trimmed.removePrefix("#").removePrefix("@").lowercase(Locale.ROOT)
        val isHashtagQuery = trimmed.startsWith("#")

        // 1. Query discoverable users in Firestore
        val matchedUsers = searchUsersInFirestore(cleanToken).getOrDefault(emptyList())

        // 2. If it's a hashtag or general query, run Firestore hashtag query + public posts query
        val hashtagPosts = if (cleanToken.isNotBlank()) {
            searchPostsByHashtagInFirestore(cleanToken).getOrDefault(emptyList())
        } else {
            emptyList()
        }

        val allPublicSnap = runCatching {
            db.collection("posts").get().await()
        }.getOrNull()
        val allPublicPosts = allPublicSnap?.documents?.mapNotNull { doc ->
            runCatching {
                doc.toObject(Post::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)?.copy(id = doc.id)
            }.getOrNull()
        }?.filter { it.visibility.isBlank() || it.visibility == "PUBLIC" || it.authorId == uid }.orEmpty()

        val contentMatchedPosts = if (isHashtagQuery) {
            allPublicPosts.filter { post ->
                post.hashtags.any { it.lowercase(Locale.ROOT).contains(cleanToken) } ||
                    post.caption.lowercase(Locale.ROOT).contains("#$cleanToken")
            }
        } else {
            allPublicPosts.filter { post ->
                post.caption.lowercase(Locale.ROOT).contains(cleanToken) ||
                    post.authorUsername.lowercase(Locale.ROOT).contains(cleanToken) ||
                    post.authorDisplayName.lowercase(Locale.ROOT).contains(cleanToken) ||
                    post.locationTag.lowercase(Locale.ROOT).contains(cleanToken) ||
                    post.hashtags.any { it.lowercase(Locale.ROOT).contains(cleanToken) }
            }
        }

        val combinedPosts = (hashtagPosts + contentMatchedPosts)
            .distinctBy { it.id }
            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }

        val hashtagCounts = mutableMapOf<String, Int>()
        allPublicPosts.forEach { post ->
            post.hashtags.forEach { tag ->
                val lowerTag = tag.lowercase(Locale.ROOT)
                if (lowerTag.contains(cleanToken)) {
                    hashtagCounts[lowerTag] = (hashtagCounts[lowerTag] ?: 0) + 1
                }
            }
        }
        val matchedHashtags = hashtagCounts.entries
            .sortedByDescending { it.value }
            .map { it.key to it.value }

        FirestoreFeedSearchResult(
            query = trimmed,
            isSearching = false,
            matchedUsers = matchedUsers,
            matchedHashtags = matchedHashtags,
            matchedPosts = combinedPosts
        )
    }

    // --- 10. User Relations (Close Friends, Blocked, Restricted, Muted) ---

    fun observeUserRelations(): Flow<List<UserRelation>> = flow {
        val uid = requireUserId()
        val path = "user_relations"
        emitAll(
            db.collection("user_relations")
                .whereEqualTo("ownerId", uid)
                .snapshots()
                .map { snap -> snap.toObjects(UserRelation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE) }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addUserRelation(
        targetUserId: String,
        targetUsername: String,
        targetDisplayName: String,
        relationType: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        require(uid != targetUserId) { "Cannot add relation to yourself." }
        val docId = "${uid}_${targetUserId}_${relationType}"
        val ref = db.collection("user_relations").document(docId)
        try {
            ref.set(
                mapOf(
                    "ownerId" to uid,
                    "targetUserId" to targetUserId,
                    "targetUsername" to targetUsername,
                    "targetDisplayName" to targetDisplayName,
                    "relationType" to relationType,
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            throw e
        }
    }

    suspend fun removeUserRelation(relationId: String): Result<Unit> = runCatching {
        requireUserId()
        val ref = db.collection("user_relations").document(relationId)
        try {
            ref.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, ref.path)
            throw e
        }
    }

    // --- 11. Activity Log & Search History + Mobile Storage Offloading ---

    fun observeUserActivity(): Flow<List<UserActivityLog>> = flow {
        val uid = requireUserId()
        if (localDao != null) {
            emitAll(
                localDao.observeLocalActivityLogs(uid)
                    .map { list -> list.map { it.toUserActivityLog() } }
                    .catch { emit(emptyList()) }
            )
        } else {
            val path = "user_activity"
            emitAll(
                db.collection("user_activity")
                    .whereEqualTo("userId", uid)
                    .snapshots()
                    .map { snap ->
                        snap.toObjects(UserActivityLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                            .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                    }
                    .catch { error ->
                        if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                        emit(emptyList())
                    }
            )
        }
    }

    /**
     * Stores user activity logs (likes, comments, searches, story reactions) on the user's
     * mobile storage (Room SQLite) instead of cloud Firestore so non-essential logs never
     * fill up the cloud database.
     */
    suspend fun logActivity(activityType: String, targetId: String, summary: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val nowMs = System.currentTimeMillis()
        if (localDao != null) {
            localDao.insertLocalActivityLog(
                LocalActivityLogEntity(
                    id = "act_${uid}_${nowMs}_${targetId.take(16)}",
                    userId = uid,
                    activityType = activityType,
                    targetId = targetId.ifBlank { "none" },
                    summary = summary.take(280),
                    createdAtMillis = nowMs
                )
            )
        } else {
            val ref = db.collection("user_activity").document()
            ref.set(
                mapOf(
                    "userId" to uid,
                    "activityType" to activityType,
                    "targetId" to targetId.ifBlank { "none" },
                    "summary" to summary.take(280),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    suspend fun deleteActivityItem(activityId: String): Result<Unit> = runCatching {
        requireUserId()
        if (localDao != null) {
            localDao.deleteLocalActivityLog(activityId)
        }
        runCatching {
            db.collection("user_activity").document(activityId).delete().await()
        }
    }

    /**
     * Offloads chat messages and activity logs from Cloud Firestore onto the user's mobile storage
     * (Room SQLite) and prunes older read messages & cloud activity logs from Firestore to prevent
     * the cloud database from filling up while keeping 100% of chat history on the user's phone.
     */
    suspend fun offloadChatHistoryToMobileStorage(
        conversationId: String? = null,
        keepNewestInCloud: Int = 3
    ): Result<MobileStorageStats> = runCatching {
        val uid = requireUserId()
        val dao = localDao ?: return@runCatching MobileStorageStats()
        var newlyOffloadedCount = 0

        // 1. Migrate any legacy cloud user_activity documents to local phone storage and delete from cloud
        runCatching {
            val cloudActivities = db.collection("user_activity")
                .whereEqualTo("userId", uid)
                .get()
                .await()
            val entities = cloudActivities.documents.mapNotNull { doc ->
                runCatching {
                    doc.toObject(UserActivityLog::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?.copy(id = doc.id)
                        ?.toLocalEntity()
                }.getOrNull()
            }
            if (entities.isNotEmpty()) {
                dao.insertLocalActivityLogs(entities)
            }
            for (doc in cloudActivities.documents) {
                runCatching { doc.reference.delete().await() }
            }
        }

        // 2. Archive chat messages to local mobile storage and prune older read messages authored by current user from Firestore
        val targetConvIds = if (!conversationId.isNullOrBlank()) {
            listOf(conversationId)
        } else {
            val convSnap = runCatching {
                db.collection("conversations")
                    .whereArrayContains("participantIds", uid)
                    .get()
                    .await()
            }.getOrNull()
            val cloudConvs = convSnap?.toObjects(Conversation::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE).orEmpty()
            if (cloudConvs.isNotEmpty()) {
                dao.upsertLocalConversations(cloudConvs.map { it.toLocalEntity(uid) })
            }
            cloudConvs.map { it.id }.filter { it.isNotBlank() }
        }

        for (cid in targetConvIds) {
            val msgSnap = runCatching {
                db.collection("conversations").document(cid)
                    .collection("messages")
                    .whereArrayContains("participantIds", uid)
                    .get()
                    .await()
            }.getOrNull() ?: continue

            val allMsgs = msgSnap.documents.mapNotNull { doc ->
                runCatching {
                    doc.toObject(DirectMessage::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        ?.copy(id = doc.id)
                }.getOrNull()
            }.sortedBy { it.createdAt?.toDate()?.time ?: 0L }

            if (allMsgs.isNotEmpty()) {
                dao.upsertLocalMessages(allMsgs.map { it.toLocalEntity(isOffloadedFromCloud = true) })
            }

            // Prune older cloud messages sent by this user (Firestore rules permit sender to delete their own message docs)
            val pruneCandidates = if (allMsgs.size > keepNewestInCloud) {
                allMsgs.dropLast(keepNewestInCloud)
            } else {
                allMsgs.filter { it.deliveryStatus == "READ" }
            }
            for (oldMsg in pruneCandidates) {
                if (oldMsg.senderId == uid && oldMsg.id.isNotBlank()) {
                    val deleted = runCatching {
                        db.collection("conversations").document(cid)
                            .collection("messages").document(oldMsg.id)
                            .delete()
                            .await()
                        true
                    }.getOrDefault(false)
                    if (deleted) {
                        newlyOffloadedCount++
                    }
                }
            }
        }

        getMobileStorageStats()
    }

    suspend fun getMobileStorageStats(): MobileStorageStats {
        val uid = auth.currentUser?.uid.orEmpty()
        val dao = localDao ?: return MobileStorageStats()
        val msgCount = runCatching { dao.countAllLocalMessages() }.getOrDefault(0)
        val offloadedCount = runCatching { dao.countOffloadedMessages() }.getOrDefault(0)
        val convCount = if (uid.isNotBlank()) runCatching { dao.countLocalConversations(uid) }.getOrDefault(0) else 0
        val actCount = if (uid.isNotBlank()) runCatching { dao.countLocalActivityLogs(uid) }.getOrDefault(0) else 0
        val estimatedBytes = (msgCount * 640L) + (convCount * 420L) + (actCount * 320L)
        return MobileStorageStats(
            localMessagesCount = msgCount,
            offloadedCloudMessagesCount = offloadedCount,
            localConversationsCount = convCount,
            localActivityLogsCount = actCount,
            estimatedSavedCloudBytes = estimatedBytes
        )
    }

    // --- 12. Content Moderation Reports & Account Deletion ---

    suspend fun submitReport(targetId: String, targetType: String, reason: String): Result<Unit> = runCatching {
        val uid = requireUserId()
        val ref = db.collection("reports").document()
        try {
            ref.set(
                mapOf(
                    "reporterId" to uid,
                    "targetId" to targetId,
                    "targetType" to targetType,
                    "reason" to reason.trim().take(450),
                    "status" to "OPEN",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, ref.path)
            throw e
        }
    }

    suspend fun deleteAccountData(): Result<Unit> = runCatching {
        val uid = requireUserId()
        val userRef = db.collection("users").document(uid)
        try {
            val userSnap = userRef.get().await()
            val username = userSnap.getString("username")
            if (!username.isNullOrBlank()) {
                db.collection("usernames").document(username).delete().await()
            }
            db.collection("user_settings").document(uid).delete().await()
            userRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, userRef.path)
            throw e
        }
    }

    // --- 13. Firestore Notifications Collection (/users/{userId}/notifications & /notifications) ---

    fun observeUserNotifications(): Flow<List<FirestoreNotification>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/notifications"
        emitAll(
            db.collection("users").document(uid)
                .collection("notifications")
                .snapshots()
                .map { snap ->
                    snap.toObjects(FirestoreNotification::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                        .sortedByDescending { it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    if (error is Exception) handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun createFirestoreNotification(
        recipientId: String,
        actorId: String,
        actorUsername: String,
        actorDisplayName: String,
        actorAvatarUrl: String,
        type: String,
        message: String,
        detailText: String = "",
        targetId: String = "",
        targetType: String = "POST",
        targetMediaUrl: String = ""
    ): Result<String> = runCatching {
        if (recipientId.isBlank()) return@runCatching ""
        val docId = db.collection("notifications").document().id
        val payload = mapOf(
            "recipientId" to recipientId,
            "actorId" to actorId,
            "actorUsername" to actorUsername.ifBlank { "mitr_user" }.take(55),
            "actorDisplayName" to actorDisplayName.take(75),
            "actorAvatarUrl" to actorAvatarUrl.take(950),
            "type" to type,
            "message" to message.take(450),
            "detailText" to detailText.take(450),
            "targetId" to targetId.take(120),
            "targetType" to targetType.take(35),
            "targetMediaUrl" to targetMediaUrl.take(950),
            "isRead" to false,
            "createdAt" to FieldValue.serverTimestamp()
        )
        runCatching {
            db.collection("users").document(recipientId)
                .collection("notifications").document(docId)
                .set(payload)
                .await()
        }
        runCatching {
            db.collection("notifications").document(docId)
                .set(payload)
                .await()
        }
        docId
    }

    private suspend fun notifyCollaboratorsAndMentions(
        authorProfile: UserProfile,
        collaboratorUsernames: List<String>,
        taggedUsernames: List<String>,
        targetId: String,
        targetType: String,
        targetMediaUrl: String,
        captionPreview: String
    ) {
        val uid = auth.currentUser?.uid ?: return
        val allUsernames = (collaboratorUsernames + taggedUsernames).distinct()
        if (allUsernames.isEmpty()) return
        runCatching {
            val usersSnap = db.collection("users").get().await()
            val usersByUsername = usersSnap.toObjects(UserProfile::class.java)
                .associateBy { it.username.lowercase(Locale.ROOT) }
            collaboratorUsernames.forEach { collabName ->
                val targetUser = usersByUsername[collabName.lowercase(Locale.ROOT)]
                if (targetUser != null && targetUser.userId != uid) {
                    createFirestoreNotification(
                        recipientId = targetUser.userId,
                        actorId = uid,
                        actorUsername = authorProfile.username,
                        actorDisplayName = authorProfile.displayName,
                        actorAvatarUrl = authorProfile.avatarUrl,
                        type = "COLLAB",
                        message = "invited you to collaborate on a ${targetType.lowercase(Locale.ROOT)}",
                        detailText = captionPreview.take(100),
                        targetId = targetId,
                        targetType = targetType,
                        targetMediaUrl = targetMediaUrl
                    )
                }
            }
            taggedUsernames.filter { it !in collaboratorUsernames }.forEach { mentionedName ->
                val targetUser = usersByUsername[mentionedName.lowercase(Locale.ROOT)]
                if (targetUser != null && targetUser.userId != uid) {
                    createFirestoreNotification(
                        recipientId = targetUser.userId,
                        actorId = uid,
                        actorUsername = authorProfile.username,
                        actorDisplayName = authorProfile.displayName,
                        actorAvatarUrl = authorProfile.avatarUrl,
                        type = "MENTION",
                        message = "tagged or mentioned you in a ${targetType.lowercase(Locale.ROOT)}",
                        detailText = captionPreview.take(100),
                        targetId = targetId,
                        targetType = targetType,
                        targetMediaUrl = targetMediaUrl
                    )
                }
            }
        }
    }

    /**
     * Instagram-style Collab Invite Response (Accept Collab or Decline / Leave Collab).
     * Updates the `collaboratorUsernames` array on the Post, Reel, or Story and updates the notification.
     */
    suspend fun respondToCollabInvite(
        targetId: String,
        targetType: String,
        collaboratorUsername: String,
        accept: Boolean,
        rawNotificationDocId: String = ""
    ): Result<Boolean> = runCatching {
        val uid = requireUserId()
        val cleanUname = collaboratorUsername.trim().removePrefix("@").lowercase(Locale.ROOT)
        require(cleanUname.isNotBlank()) { "Collaborator username is required." }

        val collectionName = when (targetType.uppercase(Locale.ROOT)) {
            "REEL" -> "reels"
            "STORY" -> "stories"
            else -> "posts"
        }

        if (targetId.isNotBlank()) {
            val docRef = db.collection(collectionName).document(targetId)
            val snap = docRef.get().await()
            if (snap.exists()) {
                val existingCollabs = (snap.get("collaboratorUsernames") as? List<*>)
                    ?.mapNotNull { it?.toString()?.trim()?.removePrefix("@")?.lowercase(Locale.ROOT) }
                    ?.filter { it.isNotBlank() }
                    ?: emptyList()

                val updatedCollabs = if (accept) {
                    (existingCollabs + cleanUname).distinct().take(15)
                } else {
                    existingCollabs.filter { !it.equals(cleanUname, ignoreCase = true) }
                }

                val updateMap = mutableMapOf<String, Any>(
                    "collaboratorUsernames" to updatedCollabs
                )
                if (collectionName != "stories") {
                    updateMap["updatedAt"] = FieldValue.serverTimestamp()
                }
                docRef.update(updateMap).await()
            }
        }

        val cleanDocId = rawNotificationDocId.removePrefix("fs_")
        if (cleanDocId.isNotBlank()) {
            if (accept) {
                runCatching {
                    db.collection("users").document(uid)
                        .collection("notifications").document(cleanDocId)
                        .update("isRead", true)
                        .await()
                }
                runCatching {
                    db.collection("notifications").document(cleanDocId)
                        .update("isRead", true)
                        .await()
                }
            } else {
                runCatching {
                    db.collection("users").document(uid)
                        .collection("notifications").document(cleanDocId)
                        .delete()
                        .await()
                }
                runCatching {
                    db.collection("notifications").document(cleanDocId)
                        .delete()
                        .await()
                }
            }
        }
        accept
    }

    /**
     * Allows a post author to update collaborators and tagged users on an existing post and notify newly added collaborators.
     */
    suspend fun updatePostCollaboratorsAndTags(
        post: Post,
        authorProfile: UserProfile,
        collaboratorUsernames: List<String>,
        taggedUsernames: List<String>
    ): Result<Unit> = runCatching {
        requireUserId()
        val finalCollabs = collaboratorUsernames
            .map { it.trim().removePrefix("@").lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() && !it.equals(authorProfile.username, ignoreCase = true) }
            .distinct()
            .take(10)
        val finalTagged = taggedUsernames
            .map { it.trim().removePrefix("@").lowercase(Locale.ROOT) }
            .filter { it.isNotBlank() }
            .distinct()
            .take(15)

        val postRef = db.collection("posts").document(post.id)
        postRef.update(
            mapOf(
                "collaboratorUsernames" to finalCollabs,
                "taggedUsernames" to finalTagged,
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()

        val newlyAddedCollabs = finalCollabs.filter { newC ->
            post.collaboratorUsernames.none { it.equals(newC, ignoreCase = true) }
        }
        val newlyAddedTags = finalTagged.filter { newT ->
            post.taggedUsernames.none { it.equals(newT, ignoreCase = true) }
        }
        if (newlyAddedCollabs.isNotEmpty() || newlyAddedTags.isNotEmpty()) {
            notifyCollaboratorsAndMentions(
                authorProfile = authorProfile,
                collaboratorUsernames = newlyAddedCollabs,
                taggedUsernames = newlyAddedTags,
                targetId = post.id,
                targetType = "POST",
                targetMediaUrl = post.mediaUrls.firstOrNull() ?: "",
                captionPreview = post.caption
            )
        }
    }

    /**
     * Activates the Mitr Verified Blue Tick subscription (₹300/month for standard creators,
     * or ₹0 Complimentary Lifetime Founder VIP for yogeshkhanchi985@gmail.com) and boosts
     * reach, views, and likes across the creator's Reels & Posts by distributing them to more viewers.
     */
    suspend fun activateBlueTickSubscription(
        currentSettings: UserSettings,
        paymentMethodLabel: String = "UPI • ₹300"
    ): Result<UserProfile?> = runCatching {
        val uid = requireUserId()
        val email = auth.currentUser?.email ?: currentSettings.email
        val userRef = db.collection("users").document(uid)
        val userSnap = userRef.get().await()
        val username = userSnap.getString("username") ?: ""
        val isFounder = isFounderVipAccount(email, username)
        val planName = if (isFounder) "FOUNDER_VIP" else "BLUE_TICK_300"
        val pricePaid = if (isFounder) 0L else MITR_BLUE_TICK_PRICE_INR

        if (userSnap.exists()) {
            userRef.update(
                mapOf(
                    "isVerified" to true,
                    "subscriptionPlan" to planName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        val updatedSettings = currentSettings.copy(
            userId = uid,
            email = email,
            isVerifiedSubscriber = true,
            subscriptionPlan = planName,
            subscriptionPriceInr = pricePaid,
            subscriptionActivatedAtMillis = System.currentTimeMillis(),
            pendingPayoutBalanceInr = currentSettings.pendingPayoutBalanceInr + MITR_BLUE_TICK_PRICE_INR
        )
        saveUserSettings(updatedSettings)

        // Boost existing Reels & Posts of this Verified Creator so their Reels are shown to more people with higher views & likes
        runCatching {
            val myReels = db.collection("reels")
                .whereEqualTo("authorId", uid)
                .get()
                .await()
            for (doc in myReels.documents) {
                val curViews = doc.getLong("viewCount") ?: 0L
                val curLikes = doc.getLong("likeCount") ?: 0L
                doc.reference.update(
                    mapOf(
                        "viewCount" to (curViews + 320L),
                        "likeCount" to (curLikes + 48L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }

            val myPosts = db.collection("posts")
                .whereEqualTo("authorId", uid)
                .get()
                .await()
            for (doc in myPosts.documents) {
                val curLikes = doc.getLong("likeCount") ?: 0L
                doc.reference.update(
                    mapOf(
                        "likeCount" to (curLikes + 28L),
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
        }

        val actionSummary = if (isFounder) {
            "Activated Complimentary Founder VIP Blue Tick ($MITR_FOUNDER_VIP_EMAIL) • Priority Reel Reach Enabled"
        } else {
            "Subscribed to Mitr Verified Blue Tick (₹$MITR_BLUE_TICK_PRICE_INR via $paymentMethodLabel) • Priority Reel Reach Enabled"
        }
        logActivity("POST_CREATED", uid, actionSummary)

        val refreshedSnap = userRef.get().await()
        refreshedSnap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            ?.copy(isVerified = true, subscriptionPlan = planName)
    }

    /**
     * Saves or updates the Creator / Platform Owner Payout Bank Account or UPI ID
     * where ₹300 Blue Tick subscription fees are settled and transferred.
     */
    suspend fun saveCreatorPayoutAccount(
        currentSettings: UserSettings,
        holderName: String,
        bankOrUpi: String,
        accountNumber: String,
        ifscCode: String
    ): Result<Unit> = runCatching {
        val uid = requireUserId()
        val updated = currentSettings.copy(
            userId = uid,
            payoutHolderName = holderName.trim().take(80),
            payoutBankOrUpi = bankOrUpi.trim().take(80),
            payoutAccountNumber = accountNumber.trim().take(40),
            payoutIfscCode = ifscCode.trim().uppercase(Locale.ROOT).take(20)
        )
        saveUserSettings(updated).getOrThrow()
    }
}
