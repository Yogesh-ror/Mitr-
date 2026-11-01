package com.example.ui.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Comment
import com.example.data.model.Conversation
import com.example.data.model.DirectMessage
import com.example.data.model.FirestoreFeedSearchResult
import com.example.data.model.FirestoreNotification
import com.example.data.model.Follow
import com.example.data.model.MusicTrack
import com.example.data.model.NotificationCategory
import com.example.data.model.Post
import com.example.data.model.Reel
import com.example.data.model.SavedAccountSession
import com.example.data.model.SavedMusic
import com.example.data.model.SavedPost
import com.example.data.model.SocialNotificationItem
import com.example.data.model.Story
import com.example.data.model.UiState
import com.example.data.model.UserActivityLog
import com.example.data.model.UserProfile
import com.example.data.model.UserRelation
import com.example.data.model.UserSettings
import com.example.data.model.isFounderVipAccount
import com.example.data.model.isUserBlueTickVerified
import com.example.data.repository.MusicRepository
import com.example.data.repository.SocialRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class SocialViewModel(
    private val repository: SocialRepository,
    val currentUserId: String,
    private val prefs: SharedPreferences? = null,
    private val musicRepository: MusicRepository = MusicRepository()
) : ViewModel() {

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _usernameCheckState = MutableStateFlow<Boolean?>(null)
    val usernameCheckState: StateFlow<Boolean?> = _usernameCheckState.asStateFlow()

    private val _sessionMinutes = MutableStateFlow(0L)
    val sessionMinutes: StateFlow<Long> = _sessionMinutes.asStateFlow()

    private val _savedAccounts = MutableStateFlow<List<SavedAccountSession>>(emptyList())
    val savedAccounts: StateFlow<List<SavedAccountSession>> = _savedAccounts.asStateFlow()

    private val _musicSearchState = MutableStateFlow<UiState<List<MusicTrack>>>(UiState.Loading)
    val musicSearchState: StateFlow<UiState<List<MusicTrack>>> = _musicSearchState.asStateFlow()

    private var musicSearchJob: Job? = null

    private val _feedSearchResultState = MutableStateFlow(FirestoreFeedSearchResult())
    val feedSearchResultState: StateFlow<FirestoreFeedSearchResult> = _feedSearchResultState.asStateFlow()

    private var feedSearchJob: Job? = null

    private val _inspectedUserProfile = MutableStateFlow<UserProfile?>(null)
    val inspectedUserProfileState: StateFlow<UserProfile?> = _inspectedUserProfile.asStateFlow()
    private var inspectProfileJob: Job? = null

    private val seenNotificationDocIds = mutableSetOf<String>()
    private var initialNotificationsLoaded = false
    private val _latestFirestoreNotificationEvent = MutableStateFlow<FirestoreNotification?>(null)
    val latestFirestoreNotificationEvent: StateFlow<FirestoreNotification?> = _latestFirestoreNotificationEvent.asStateFlow()

    val currentUserProfileState: StateFlow<UiState<UserProfile?>> = repository.observeCurrentUserProfile()
        .map<UserProfile?, UiState<UserProfile?>> { profile ->
            if (profile != null) {
                recordLocalSessionAccount(profile)
            }
            UiState.Success(profile)
        }
        .catch { emit(UiState.Error(it.message ?: "Failed to load profile")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val userSettingsState: StateFlow<UserSettings> = repository.observeUserSettings()
        .catch { emit(UserSettings(id = currentUserId, userId = currentUserId)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UserSettings(id = currentUserId, userId = currentUserId)
        )

    val userRelationsState: StateFlow<List<UserRelation>> = repository.observeUserRelations()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val feedPostsState: StateFlow<UiState<List<Post>>> = combine(
        repository.observePublicFeedPosts(),
        repository.observeAllDiscoverableUsers().catch { emit(emptyList()) },
        userRelationsState
    ) { posts, users, relations ->
        val blockedIds = relations.filter { it.relationType == "BLOCKED" }.map { it.targetUserId }.toSet()
        val verifiedUserIds = users.filter { isUserBlueTickVerified(it) }.map { it.userId }.toSet()
        val filtered = posts
            .filter { !blockedIds.contains(it.authorId) }
            .sortedWith(
                compareByDescending<Post> { post ->
                    verifiedUserIds.contains(post.authorId) || isFounderVipAccount(null, post.authorUsername)
                }.thenByDescending { it.createdAt?.seconds ?: 0L }
            )
        UiState.Success(filtered) as UiState<List<Post>>
    }.catch { emit(UiState.Success(emptyList())) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val ownPostsState: StateFlow<List<Post>> = repository.observeUserPosts(currentUserId)
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val savedPostsState: StateFlow<List<SavedPost>> = repository.observeSavedPosts()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val savedMusicState: StateFlow<List<SavedMusic>> = repository.observeSavedMusic()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val outgoingFollowsState: StateFlow<List<Follow>> = repository.observeOutgoingFollows()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val incomingFollowsState: StateFlow<List<Follow>> = repository.observeIncomingFollows()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    /**
     * All accessible stories (active 24h stories + highlighted stories) for viewing on Profile screens.
     * Anyone visiting a user's Profile can tap their avatar or Highlights row to view their stories.
     */
    val allAccessibleStoriesState: StateFlow<List<Story>> = combine(
        repository.observeActiveStories(),
        repository.observeCloseFriendsStories(),
        userRelationsState
    ) { publicStories, cfStories, relations ->
        val blockedIds = relations
            .filter { it.relationType == "BLOCKED" }
            .map { it.targetUserId }
            .toSet()
        (publicStories + cfStories)
            .distinctBy { it.id }
            .filter { !blockedIds.contains(it.authorId) }
            .sortedByDescending { it.expiresAtMillis }
    }.catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    /**
     * Home Screen Stories Bar: ONLY shows active 24h stories from yourself or accounts you actively follow.
     * Stories from unfollowed accounts are never shown on the Home Screen.
     */
    val activeStoriesState: StateFlow<List<Story>> = combine(
        allAccessibleStoriesState,
        outgoingFollowsState,
        userRelationsState
    ) { stories, outgoingFollows, relations ->
        val nowMillis = System.currentTimeMillis()
        val mutedIds = relations
            .filter { it.relationType == "MUTED_STORIES" || it.relationType == "BLOCKED" }
            .map { it.targetUserId }
            .toSet()
        val acceptedFollowingIds = outgoingFollows
            .filter { it.status == "ACCEPTED" }
            .map { it.followingId }
            .toSet()
        stories
            .filter { story ->
                story.expiresAtMillis > nowMillis &&
                    !mutedIds.contains(story.authorId) &&
                    (story.authorId == currentUserId || acceptedFollowingIds.contains(story.authorId))
            }
            .sortedByDescending { it.expiresAtMillis }
    }.catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val reelsState: StateFlow<UiState<List<Reel>>> = combine(
        repository.observePublicReels(),
        repository.observeAllDiscoverableUsers().catch { emit(emptyList()) },
        userRelationsState
    ) { reels, users, relations ->
        val blockedIds = relations.filter { it.relationType == "BLOCKED" }.map { it.targetUserId }.toSet()
        val verifiedUserIds = users.filter { isUserBlueTickVerified(it) }.map { it.userId }.toSet()
        val rankedReels = reels
            .filter { !blockedIds.contains(it.authorId) }
            .sortedWith(
                compareByDescending<Reel> { reel ->
                    verifiedUserIds.contains(reel.authorId) || isFounderVipAccount(null, reel.authorUsername)
                }.thenByDescending { it.createdAt?.seconds ?: 0L }
            )
        UiState.Success(rankedReels) as UiState<List<Reel>>
    }.catch { emit(UiState.Success(emptyList())) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val conversationsState: StateFlow<List<Conversation>> = combine(
        repository.observeConversations(),
        userRelationsState
    ) { convs, relations ->
        val blockedIds = relations.filter { it.relationType == "BLOCKED" }.map { it.targetUserId }.toSet()
        convs.filter { conv -> conv.participantIds.none { blockedIds.contains(it) } }
    }.catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val discoverUsersState: StateFlow<List<UserProfile>> = combine(
        repository.observeAllDiscoverableUsers().catch { emit(emptyList()) },
        repository.observePublicFeedPosts().catch { emit(emptyList()) },
        repository.observePublicReels().catch { emit(emptyList()) },
        outgoingFollowsState,
        userRelationsState
    ) { users, posts, reels, follows, relations ->
        val blockedIds = relations.filter { it.relationType == "BLOCKED" }.map { it.targetUserId }.toSet()
        val byId = LinkedHashMap<String, UserProfile>()
        users.forEach { u ->
            val uid = u.userId.ifBlank { u.id }
            if (uid.isNotBlank() && !blockedIds.contains(uid)) {
                byId[uid] = u.copy(id = uid, userId = uid)
            }
        }
        posts.forEach { post ->
            val uid = post.authorId
            if (uid.isNotBlank() && !blockedIds.contains(uid) && !byId.containsKey(uid)) {
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
        reels.forEach { reel ->
            val uid = reel.authorId
            if (uid.isNotBlank() && !blockedIds.contains(uid) && !byId.containsKey(uid)) {
                val uname = reel.authorUsername.ifBlank { "user_${uid.take(6)}" }
                byId[uid] = UserProfile(
                    id = uid,
                    userId = uid,
                    username = uname,
                    displayName = uname,
                    bio = "Creator on Mitr"
                )
            }
        }
        follows.forEach { f ->
            val uid = f.followingId
            if (uid.isNotBlank() && !blockedIds.contains(uid) && !byId.containsKey(uid)) {
                val uname = f.followingUsername.ifBlank { "user_${uid.take(6)}" }
                byId[uid] = UserProfile(
                    id = uid,
                    userId = uid,
                    username = uname,
                    displayName = uname,
                    bio = "Mitr Community Member"
                )
            }
        }
        byId.values.toList()
    }.catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val activityLogsState: StateFlow<List<UserActivityLog>> = repository.observeUserActivity()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val allTargetCommentsState: StateFlow<List<Comment>> = repository.observeAllPostAndReelComments()
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val firestoreNotificationsState: StateFlow<List<FirestoreNotification>> = repository.observeUserNotifications()
        .map { docs ->
            if (!initialNotificationsLoaded) {
                docs.forEach { seenNotificationDocIds.add(it.id) }
                initialNotificationsLoaded = true
            } else {
                val newestUnseen = docs.firstOrNull { it.id.isNotBlank() && !seenNotificationDocIds.contains(it.id) }
                docs.forEach { if (it.id.isNotBlank()) seenNotificationDocIds.add(it.id) }
                if (newestUnseen != null) {
                    _latestFirestoreNotificationEvent.value = newestUnseen
                }
            }
            docs
        }
        .catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    /**
     * Real-time Notifications stream combining:
     * 0. Explicit documents in the user's Firestore 'notifications' collection
     * 1. Who liked the current user's posts and reels (NotificationCategory.LIKES)
     * 2. Who followed or requested to follow the current user (NotificationCategory.FOLLOWS)
     * 3. Who commented on the current user's posts and reels (NotificationCategory.COMMENTS)
     * 4. Collaborations & @mentions (NotificationCategory.COLLABS)
     */
    val notificationsState: StateFlow<List<SocialNotificationItem>> = combine(
        ownPostsState,
        reelsState,
        incomingFollowsState,
        outgoingFollowsState,
        combine(allTargetCommentsState, discoverUsersState, firestoreNotificationsState) { comments, users, fsNotifs ->
            Triple(comments, users, fsNotifs)
        }
    ) { ownPosts, reelsUi, incomingFollows, outgoingFollows, (allComments, discoverUsers, fsNotifs) ->
        val userById = discoverUsers.associateBy { it.userId }
        val acceptedFollowingIds = outgoingFollows
            .filter { it.status == "ACCEPTED" }
            .map { it.followingId }
            .toSet()

        val items = mutableListOf<SocialNotificationItem>()

        // 0. Documents from Firestore Notifications collection
        fsNotifs.forEach { doc ->
            val category = when (doc.type) {
                "LIKE" -> NotificationCategory.LIKES
                "FOLLOW" -> NotificationCategory.FOLLOWS
                "COMMENT" -> NotificationCategory.COMMENTS
                "COLLAB", "MENTION" -> NotificationCategory.COLLABS
                else -> NotificationCategory.ALL
            }
            val actor = userById[doc.actorId]
            val uname = doc.actorUsername.ifBlank { actor?.username ?: "user_${doc.actorId.take(5)}" }
            val dname = doc.actorDisplayName.ifBlank { actor?.displayName ?: "@$uname" }
            val avatar = doc.actorAvatarUrl.ifBlank { actor?.avatarUrl ?: "" }
            val timeMillis = doc.createdAt?.toDate()?.time ?: System.currentTimeMillis()
            val matchingFollow = if (doc.type == "FOLLOW") {
                incomingFollows.firstOrNull { it.followerId == doc.actorId }
            } else null
            items.add(
                SocialNotificationItem(
                    id = "fs_${doc.id}",
                    category = category,
                    actorUserId = doc.actorId,
                    actorUsername = uname,
                    actorDisplayName = dname,
                    actorAvatarUrl = avatar,
                    message = doc.message,
                    detailText = doc.detailText,
                    targetId = doc.targetId,
                    targetType = doc.targetType,
                    targetMediaUrl = doc.targetMediaUrl,
                    followRecord = matchingFollow,
                    isFollowingBack = acceptedFollowingIds.contains(doc.actorId),
                    timestampMillis = timeMillis
                )
            )
        }

        // 1. Likes on user's Posts
        ownPosts.forEach { post ->
            val postThumb = post.mediaUrls.firstOrNull() ?: ""
            val postTime = (post.updatedAt ?: post.createdAt)?.toDate()?.time ?: 0L
            post.likedByUids.forEach { likerUid ->
                val actor = userById[likerUid]
                val uname = actor?.username ?: if (likerUid == currentUserId) post.authorUsername else "user_${likerUid.take(5)}"
                val dname = actor?.displayName ?: if (likerUid == currentUserId) post.authorDisplayName else "@$uname"
                val avatar = actor?.avatarUrl ?: ""
                items.add(
                    SocialNotificationItem(
                        id = "like_post_${post.id}_$likerUid",
                        category = NotificationCategory.LIKES,
                        actorUserId = likerUid,
                        actorUsername = uname,
                        actorDisplayName = dname,
                        actorAvatarUrl = avatar,
                        message = if (likerUid == currentUserId) "You liked your post" else "liked your post",
                        detailText = post.caption.take(80),
                        targetId = post.id,
                        targetType = "POST",
                        targetMediaUrl = postThumb,
                        isFollowingBack = acceptedFollowingIds.contains(likerUid),
                        timestampMillis = postTime
                    )
                )
            }
        }

        // 2. Likes on user's Reels
        val allReels = (reelsUi as? UiState.Success)?.data ?: emptyList()
        val ownReels = allReels.filter { it.authorId == currentUserId }
        ownReels.forEach { reel ->
            val reelTime = (reel.updatedAt ?: reel.createdAt)?.toDate()?.time ?: 0L
            reel.likedByUids.forEach { likerUid ->
                val actor = userById[likerUid]
                val uname = actor?.username ?: if (likerUid == currentUserId) reel.authorUsername else "user_${likerUid.take(5)}"
                val dname = actor?.displayName ?: if (likerUid == currentUserId) reel.authorDisplayName else "@$uname"
                val avatar = actor?.avatarUrl ?: ""
                items.add(
                    SocialNotificationItem(
                        id = "like_reel_${reel.id}_$likerUid",
                        category = NotificationCategory.LIKES,
                        actorUserId = likerUid,
                        actorUsername = uname,
                        actorDisplayName = dname,
                        actorAvatarUrl = avatar,
                        message = if (likerUid == currentUserId) "You liked your reel" else "liked your reel",
                        detailText = reel.caption.take(80),
                        targetId = reel.id,
                        targetType = "REEL",
                        targetMediaUrl = reel.musicCoverUrl,
                        isFollowingBack = acceptedFollowingIds.contains(likerUid),
                        timestampMillis = reelTime
                    )
                )
            }
        }

        // 3. Followers & Follow Requests
        incomingFollows.forEach { follow ->
            val actor = userById[follow.followerId]
            val uname = follow.followerUsername.ifBlank { actor?.username ?: "user_${follow.followerId.take(5)}" }
            val dname = actor?.displayName ?: "@$uname"
            val avatar = actor?.avatarUrl ?: ""
            val followTime = follow.createdAt?.toDate()?.time ?: 0L
            val msg = if (follow.status == "PENDING") {
                "requested to follow you"
            } else {
                "started following you"
            }
            items.add(
                SocialNotificationItem(
                    id = "follow_${follow.id}",
                    category = NotificationCategory.FOLLOWS,
                    actorUserId = follow.followerId,
                    actorUsername = uname,
                    actorDisplayName = dname,
                    actorAvatarUrl = avatar,
                    message = msg,
                    detailText = if (follow.status == "PENDING") "Tap Accept to approve follow request" else "Following you on Mitr",
                    targetId = follow.followerId,
                    targetType = "FOLLOW",
                    followRecord = follow,
                    isFollowingBack = acceptedFollowingIds.contains(follow.followerId),
                    timestampMillis = followTime
                )
            )
        }

        // 4. Comments on user's Posts and Reels
        val ownPostMap = ownPosts.associateBy { it.id }
        val ownReelMap = ownReels.associateBy { it.id }
        allComments.forEach { comment ->
            val matchedPost = ownPostMap[comment.targetId]
            val matchedReel = ownReelMap[comment.targetId]
            if (matchedPost != null || matchedReel != null) {
                val actor = userById[comment.authorId]
                val uname = comment.authorUsername.ifBlank { actor?.username ?: "user_${comment.authorId.take(5)}" }
                val dname = comment.authorDisplayName.ifBlank { actor?.displayName ?: "@$uname" }
                val avatar = actor?.avatarUrl ?: ""
                val commentTime = comment.createdAt?.toDate()?.time ?: 0L
                val thumb = matchedPost?.mediaUrls?.firstOrNull() ?: matchedReel?.musicCoverUrl ?: ""
                val targetLabel = if (matchedPost != null) "post" else "reel"
                items.add(
                    SocialNotificationItem(
                        id = "comment_${comment.id}",
                        category = NotificationCategory.COMMENTS,
                        actorUserId = comment.authorId,
                        actorUsername = uname,
                        actorDisplayName = dname,
                        actorAvatarUrl = avatar,
                        message = "commented on your $targetLabel:",
                        detailText = "\"${comment.text}\"",
                        targetId = comment.targetId,
                        targetType = comment.targetType,
                        targetMediaUrl = thumb,
                        isFollowingBack = acceptedFollowingIds.contains(comment.authorId),
                        timestampMillis = commentTime
                    )
                )
            }
        }

        items
            .distinctBy { "${it.category}_${it.actorUserId}_${it.targetId}_${it.detailText}" }
            .sortedByDescending { it.timestampMillis }
    }.catch { emit(emptyList()) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    private var usageTimerJob: Job? = null

    init {
        loadSavedAccountsFromPrefs()
        startRealSessionTimeTracker()
        searchMusic(query = "", genre = "Indian Hits")
    }

    fun consumeLatestFirestoreNotificationEvent() {
        _latestFirestoreNotificationEvent.value = null
    }

    fun observeFollowersForUser(targetUserId: String) = repository.observeFollowersForUser(targetUserId)

    fun observeFollowingForUser(targetUserId: String) = repository.observeFollowingForUser(targetUserId)

    private fun startRealSessionTimeTracker() {
        usageTimerJob?.cancel()
        usageTimerJob = viewModelScope.launch {
            while (isActive) {
                delay(60_000L)
                _sessionMinutes.value += 1L
                val currentSettings = userSettingsState.value
                val updatedSettings = currentSettings.copy(
                    todayUsageMinutes = currentSettings.todayUsageMinutes + 1L,
                    weeklyUsageMinutes = currentSettings.weeklyUsageMinutes + 1L
                )
                repository.saveUserSettings(updatedSettings)
            }
        }
    }

    fun searchMusic(query: String, genre: String = "Indian Hits") {
        musicSearchJob?.cancel()
        musicSearchJob = viewModelScope.launch {
            if (query.isNotBlank()) {
                delay(280L)
            }
            _musicSearchState.value = UiState.Loading
            musicRepository.searchMusicTracks(query = query, genreFilter = genre)
                .onSuccess { tracks ->
                    _musicSearchState.value = UiState.Success(tracks)
                }
                .onFailure { err ->
                    _musicSearchState.value = UiState.Error(err.localizedMessage ?: "Failed to fetch music tracks")
                }
        }
    }

    fun toggleSaveMusic(track: MusicTrack) {
        viewModelScope.launch {
            repository.toggleSaveMusic(track)
                .onSuccess { saved ->
                    _statusBanner.value = if (saved) {
                        "Saved \"${track.title}\" to your music library"
                    } else {
                        "Removed \"${track.title}\" from saved music"
                    }
                }
                .onFailure { err ->
                    _statusBanner.value = err.localizedMessage ?: "Could not update saved music"
                }
        }
    }

    fun clearBanner() {
        _statusBanner.value = null
    }

    fun showBanner(msg: String) {
        _statusBanner.value = msg
    }

    fun triggerPullToRefresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            delay(500L)
            _isRefreshing.value = false
        }
    }

    fun checkUsernameAvailability(username: String) {
        if (username.trim().length < 3) {
            _usernameCheckState.value = null
            return
        }
        viewModelScope.launch {
            val res = repository.isUsernameAvailable(username)
            _usernameCheckState.value = res.getOrNull()
        }
    }

    fun saveProfile(
        username: String,
        displayName: String,
        bio: String,
        website: String,
        avatarUrl: String,
        isPrivate: Boolean,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val res = repository.createOrUpdateProfile(
                username = username,
                displayName = displayName,
                bio = bio,
                website = website,
                avatarUrl = avatarUrl,
                isPrivate = isPrivate
            )
            res.onSuccess { profile ->
                recordLocalSessionAccount(profile)
                _statusBanner.value = "Profile @${profile.username} saved"
                onSuccess()
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Failed to save profile"
            }
        }
    }

    fun publishPost(
        caption: String,
        mediaUrls: List<String>,
        mediaType: String,
        locationTag: String,
        taggedUsernames: List<String>,
        visibility: String,
        commentsAllowed: Boolean,
        musicTrack: MusicTrack? = null,
        collaboratorUsernames: List<String> = emptyList(),
        onComplete: () -> Unit = {}
    ) {
        val author = (currentUserProfileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            val res = repository.createPost(
                authorProfile = author,
                caption = caption,
                mediaUrls = mediaUrls,
                mediaType = mediaType,
                locationTag = locationTag,
                taggedUsernames = taggedUsernames,
                visibility = visibility,
                commentsAllowed = commentsAllowed,
                musicTrack = musicTrack,
                collaboratorUsernames = collaboratorUsernames
            )
            res.onSuccess {
                _statusBanner.value = if (collaboratorUsernames.isNotEmpty()) {
                    "Collab post published with ${collaboratorUsernames.joinToString { "@$it" }}"
                } else {
                    "Post published to feed"
                }
                onComplete()
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Failed to publish post"
            }
        }
    }

    fun toggleLikePost(post: Post) {
        viewModelScope.launch {
            repository.toggleLikePost(post).onFailure {
                _statusBanner.value = it.localizedMessage ?: "Could not update like"
            }
        }
    }

    fun updatePostCaption(postId: String, caption: String, commentsAllowed: Boolean) {
        viewModelScope.launch {
            repository.updatePostCaption(postId, caption, commentsAllowed)
                .onSuccess { _statusBanner.value = "Post updated" }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to update post" }
        }
    }

    fun deletePost(postId: String) {
        viewModelScope.launch {
            repository.deletePost(postId)
                .onSuccess { _statusBanner.value = "Post deleted" }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to delete post" }
        }
    }

    fun observeComments(targetId: String, targetType: String) = repository.observeComments(targetId, targetType)

    fun addComment(targetId: String, targetType: String, text: String, parentCommentId: String = "") {
        val author = (currentUserProfileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.addComment(targetId, targetType, author, text, parentCommentId)
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to post comment" }
        }
    }

    fun toggleSavePost(postId: String, targetType: String, authorUsername: String, captionPreview: String, mediaUrl: String) {
        viewModelScope.launch {
            repository.toggleSavePost(postId, targetType, authorUsername, captionPreview, mediaUrl)
                .onSuccess { saved ->
                    _statusBanner.value = if (saved) "Saved to collection" else "Removed from saved"
                }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Failed to update bookmark" }
        }
    }

    fun publishStory(
        mediaUrl: String,
        mediaType: String,
        caption: String,
        overlayText: String,
        stickerLabel: String,
        audience: String,
        musicTrack: MusicTrack? = null,
        taggedUsernames: List<String> = emptyList(),
        collaboratorUsernames: List<String> = emptyList(),
        onComplete: () -> Unit = {}
    ) {
        val author = (currentUserProfileState.value as? UiState.Success)?.data ?: return
        val closeFriends = userRelationsState.value
            .filter { it.relationType == "CLOSE_FRIEND" }
            .map { it.targetUserId }
        viewModelScope.launch {
            repository.createStory(
                authorProfile = author,
                mediaUrl = mediaUrl,
                mediaType = mediaType,
                caption = caption,
                overlayText = overlayText,
                stickerLabel = stickerLabel,
                audience = audience,
                closeFriendUids = closeFriends,
                musicTrack = musicTrack,
                taggedUsernames = taggedUsernames,
                collaboratorUsernames = collaboratorUsernames
            ).onSuccess {
                _statusBanner.value = if (audience == "CLOSE_FRIENDS") "Close Friends story shared" else "Story shared (24h)"
                onComplete()
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Failed to publish story"
            }
        }
    }

    fun recordStoryView(story: Story) {
        viewModelScope.launch {
            repository.recordStoryView(story)
        }
    }

    fun deleteStory(storyId: String) {
        viewModelScope.launch {
            repository.deleteStory(storyId)
                .onSuccess { _statusBanner.value = "Story deleted" }
        }
    }

    fun toggleStoryHighlight(story: Story, highlightTitle: String = "Highlights") {
        viewModelScope.launch {
            repository.toggleStoryHighlight(story, highlightTitle)
                .onSuccess { highlighted ->
                    _statusBanner.value = if (highlighted) {
                        "Added story to Profile Highlights"
                    } else {
                        "Removed story from Highlights"
                    }
                }
                .onFailure {
                    _statusBanner.value = it.localizedMessage ?: "Could not update story highlight"
                }
        }
    }

    fun sharePostToUser(recipient: UserProfile, post: Post, onComplete: (String) -> Unit = {}) {
        val currentProfile = (currentUserProfileState.value as? UiState.Success)?.data ?: return
        val isFollowing = outgoingFollowsState.value.any {
            it.followingId == recipient.userId && it.status == "ACCEPTED"
        }
        viewModelScope.launch {
            repository.sharePostToUser(
                sender = currentProfile,
                recipient = recipient,
                post = post,
                isMutualOrFollowed = isFollowing
            ).onSuccess { convId ->
                _statusBanner.value = "Shared post with @${recipient.username}"
                onComplete(convId)
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Could not share post"
            }
        }
    }

    fun publishReel(
        videoUrl: String,
        caption: String,
        audioTrackName: String,
        durationSeconds: Long,
        musicTrack: MusicTrack? = null,
        taggedUsernames: List<String> = emptyList(),
        collaboratorUsernames: List<String> = emptyList(),
        onComplete: () -> Unit = {}
    ) {
        val author = (currentUserProfileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.createReel(
                authorProfile = author,
                videoUrl = videoUrl,
                caption = caption,
                audioTrackName = audioTrackName,
                durationSeconds = durationSeconds,
                musicTrack = musicTrack,
                taggedUsernames = taggedUsernames,
                collaboratorUsernames = collaboratorUsernames
            ).onSuccess {
                _statusBanner.value = if (collaboratorUsernames.isNotEmpty()) {
                    "Collab reel published with ${collaboratorUsernames.joinToString { "@$it" }}"
                } else {
                    "Reel published"
                }
                onComplete()
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Failed to publish reel"
            }
        }
    }

    fun toggleLikeReel(reel: Reel) {
        viewModelScope.launch {
            repository.toggleLikeReel(reel)
        }
    }

    fun incrementReelView(reel: Reel) {
        viewModelScope.launch {
            repository.incrementReelView(reel)
        }
    }

    fun toggleFollowUser(targetProfile: UserProfile) {
        val currentProfile = (currentUserProfileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.followOrUnfollowUser(currentProfile, targetProfile)
                .onSuccess { state ->
                    _statusBanner.value = when (state) {
                        "PENDING" -> "Follow request sent to @${targetProfile.username}"
                        "ACCEPTED" -> "Following @${targetProfile.username}"
                        else -> "Unfollowed @${targetProfile.username}"
                    }
                }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Action failed" }
        }
    }

    fun respondToFollowRequest(follow: Follow, accept: Boolean) {
        viewModelScope.launch {
            repository.respondToFollowRequest(follow, accept)
                .onSuccess {
                    _statusBanner.value = if (accept) "Approved @${follow.followerUsername}" else "Declined follow request"
                }
        }
    }

    fun respondToCollabInvite(notification: SocialNotificationItem, accept: Boolean) {
        val currentProfile = (currentUserProfileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.respondToCollabInvite(
                targetId = notification.targetId,
                targetType = notification.targetType.ifBlank { "POST" },
                collaboratorUsername = currentProfile.username,
                accept = accept,
                rawNotificationDocId = notification.id
            ).onSuccess { accepted ->
                _statusBanner.value = if (accepted) {
                    "Collab accepted! Post/Reel is now co-authored on your profile & feed"
                } else {
                    "Collab invite declined"
                }
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Could not update collaboration"
            }
        }
    }

    fun leaveCollaboration(targetId: String, targetType: String) {
        val currentProfile = (currentUserProfileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.respondToCollabInvite(
                targetId = targetId,
                targetType = targetType,
                collaboratorUsername = currentProfile.username,
                accept = false
            ).onSuccess {
                _statusBanner.value = "You left the collaboration"
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Could not leave collaboration"
            }
        }
    }

    fun updatePostCollaboratorsAndTags(
        post: Post,
        collaboratorUsernames: List<String>,
        taggedUsernames: List<String>
    ) {
        val currentProfile = (currentUserProfileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.updatePostCollaboratorsAndTags(
                post = post,
                authorProfile = currentProfile,
                collaboratorUsernames = collaboratorUsernames,
                taggedUsernames = taggedUsernames
            ).onSuccess {
                _statusBanner.value = if (collaboratorUsernames.isNotEmpty()) {
                    "Updated collaborators: ${collaboratorUsernames.joinToString { "@${it.removePrefix("@")}" }}"
                } else {
                    "Updated post tags & collaborators"
                }
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Could not update collaborators"
            }
        }
    }

    fun startConversationWith(targetProfile: UserProfile, onReady: (String) -> Unit) {
        val currentProfile = (currentUserProfileState.value as? UiState.Success)?.data ?: return
        val isFollowing = outgoingFollowsState.value.any {
            it.followingId == targetProfile.userId && it.status == "ACCEPTED"
        }
        viewModelScope.launch {
            repository.startOrGetConversation(currentProfile, targetProfile, isMutualOrFollowed = isFollowing)
                .onSuccess { convId -> onReady(convId) }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Could not start conversation" }
        }
    }

    fun observeMessages(conversationId: String): kotlinx.coroutines.flow.Flow<List<DirectMessage>> =
        repository.observeMessages(conversationId)

    fun sendDirectMessage(
        conversation: Conversation,
        text: String,
        mediaUrl: String = "",
        mediaType: String = "NONE",
        replyToMessageId: String = ""
    ) {
        val currentProfile = (currentUserProfileState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            repository.sendDirectMessage(
                conversation = conversation,
                senderUsername = currentProfile.username,
                text = text,
                mediaUrl = mediaUrl,
                mediaType = mediaType,
                replyToMessageId = replyToMessageId
            ).onFailure {
                _statusBanner.value = it.localizedMessage ?: "Failed to send message"
            }
        }
    }

    fun acceptOrDeclineMessageRequest(conversationId: String, accept: Boolean) {
        viewModelScope.launch {
            val newStatus = if (accept) "ACTIVE" else "DECLINED"
            repository.updateConversationStatus(conversationId, newStatus)
                .onSuccess {
                    _statusBanner.value = if (accept) "Message request accepted" else "Message request declined"
                }
        }
    }

    fun deleteMessage(conversationId: String, messageId: String) {
        viewModelScope.launch {
            repository.deleteDirectMessage(conversationId, messageId)
                .onSuccess { _statusBanner.value = "Message deleted" }
        }
    }

    fun markConversationRead(conversationId: String) {
        viewModelScope.launch {
            repository.markConversationRead(conversationId)
        }
    }

    fun loadInspectedUserProfile(identifier: String?) {
        inspectProfileJob?.cancel()
        val clean = identifier?.trim() ?: ""
        if (clean.isBlank()) {
            _inspectedUserProfile.value = null
            return
        }
        // Immediately populate from existing in-memory cache if available
        val cached = discoverUsersState.value.find {
            it.userId == clean ||
                it.id == clean ||
                it.username.equals(clean.removePrefix("@"), ignoreCase = true)
        } ?: _feedSearchResultState.value.matchedUsers.find {
            it.userId == clean ||
                it.id == clean ||
                it.username.equals(clean.removePrefix("@"), ignoreCase = true)
        }
        if (cached != null) {
            _inspectedUserProfile.value = cached
        }
        // Also fetch fresh profile from Firestore (resolving by UID or @username)
        inspectProfileJob = viewModelScope.launch {
            repository.resolveUserProfile(clean).onSuccess { resolved ->
                if (resolved != null) {
                    _inspectedUserProfile.value = resolved
                }
            }
        }
    }

    fun executeFeedFirestoreSearch(rawQuery: String) {
        feedSearchJob?.cancel()
        val trimmed = rawQuery.trim()
        if (trimmed.isBlank()) {
            _feedSearchResultState.value = FirestoreFeedSearchResult(query = "", isSearching = false)
            return
        }
        _feedSearchResultState.value = _feedSearchResultState.value.copy(
            query = trimmed,
            isSearching = true
        )
        feedSearchJob = viewModelScope.launch {
            delay(180L)
            repository.searchFeedInFirestore(trimmed)
                .onSuccess { result ->
                    val blockedIds = userRelationsState.value
                        .filter { it.relationType == "BLOCKED" }
                        .map { it.targetUserId }
                        .toSet()
                    _feedSearchResultState.value = result.copy(
                        isSearching = false,
                        matchedUsers = result.matchedUsers.filter { !blockedIds.contains(it.userId) },
                        matchedPosts = result.matchedPosts.filter { !blockedIds.contains(it.authorId) }
                    )
                }
                .onFailure {
                    _feedSearchResultState.value = _feedSearchResultState.value.copy(isSearching = false)
                }
        }
    }

    fun recordSearchQuery(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            repository.logActivity("SEARCH", "search", "Searched for \"${query.trim()}\"")
        }
    }

    fun deleteActivityItem(activityId: String) {
        viewModelScope.launch {
            repository.deleteActivityItem(activityId)
        }
    }

    fun saveSettings(updated: UserSettings) {
        viewModelScope.launch {
            repository.saveUserSettings(updated)
                .onSuccess { _statusBanner.value = "Settings saved" }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Could not save settings" }
        }
    }

    fun activateBlueTickSubscription(
        paymentMethodLabel: String = "UPI • ₹300",
        onComplete: () -> Unit = {}
    ) {
        val currentSettings = userSettingsState.value
        viewModelScope.launch {
            repository.activateBlueTickSubscription(
                currentSettings = currentSettings,
                paymentMethodLabel = paymentMethodLabel
            ).onSuccess {
                val isFounder = isFounderVipAccount(currentSettings.email)
                _statusBanner.value = if (isFounder) {
                    "Founder VIP Blue Tick Active (Free for yogeshkhanchi985@gmail.com) • Priority Reel Reach Enabled!"
                } else {
                    "Mitr Verified Blue Tick Activated (₹300) • Your Reels & Posts are now boosted!"
                }
                onComplete()
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Could not activate Blue Tick subscription"
            }
        }
    }

    fun saveCreatorPayoutAccount(
        holderName: String,
        bankOrUpi: String,
        accountNumber: String,
        ifscCode: String,
        onComplete: () -> Unit = {}
    ) {
        val currentSettings = userSettingsState.value
        viewModelScope.launch {
            repository.saveCreatorPayoutAccount(
                currentSettings = currentSettings,
                holderName = holderName,
                bankOrUpi = bankOrUpi,
                accountNumber = accountNumber,
                ifscCode = ifscCode
            ).onSuccess {
                _statusBanner.value = "Payout account saved for ₹300 Blue Tick revenue transfers"
                onComplete()
            }.onFailure { err ->
                _statusBanner.value = err.localizedMessage ?: "Could not save payout account"
            }
        }
    }

    fun addRelation(targetProfile: UserProfile, relationType: String) {
        viewModelScope.launch {
            repository.addUserRelation(
                targetUserId = targetProfile.userId,
                targetUsername = targetProfile.username,
                targetDisplayName = targetProfile.displayName,
                relationType = relationType
            ).onSuccess {
                _statusBanner.value = "Added @${targetProfile.username} to $relationType"
            }.onFailure {
                _statusBanner.value = it.localizedMessage ?: "Could not update relation"
            }
        }
    }

    fun removeRelation(relationId: String) {
        viewModelScope.launch {
            repository.removeUserRelation(relationId)
                .onSuccess { _statusBanner.value = "Updated list" }
        }
    }

    fun submitReport(targetId: String, targetType: String, reason: String) {
        viewModelScope.launch {
            repository.submitReport(targetId, targetType, reason)
                .onSuccess { _statusBanner.value = "Report submitted for moderation review" }
                .onFailure { _statusBanner.value = it.localizedMessage ?: "Could not submit report" }
        }
    }

    fun deleteOwnAccountData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAccountData()
                .onSuccess {
                    removeSavedAccountSession(currentUserId)
                    onComplete()
                }
                .onFailure {
                    _statusBanner.value = it.localizedMessage ?: "Failed to delete account data"
                }
        }
    }

    // --- Multi-Account Switcher Local Session Management ---

    private fun loadSavedAccountsFromPrefs() {
        val raw = prefs?.getString("saved_accounts_json", "[]") ?: "[]"
        try {
            val arr = JSONArray(raw)
            val list = mutableListOf<SavedAccountSession>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    SavedAccountSession(
                        uid = obj.optString("uid"),
                        email = obj.optString("email"),
                        displayName = obj.optString("displayName"),
                        username = obj.optString("username"),
                        avatarUrl = obj.optString("avatarUrl"),
                        lastActiveMillis = obj.optLong("lastActiveMillis")
                    )
                )
            }
            _savedAccounts.value = list
        } catch (e: Exception) {
            _savedAccounts.value = emptyList()
        }
    }

    private fun recordLocalSessionAccount(profile: UserProfile) {
        val existing = _savedAccounts.value.filter { it.uid != profile.userId }
        val updated = listOf(
            SavedAccountSession(
                uid = profile.userId,
                email = userSettingsState.value.email,
                displayName = profile.displayName,
                username = profile.username,
                avatarUrl = profile.avatarUrl,
                lastActiveMillis = System.currentTimeMillis()
            )
        ) + existing
        _savedAccounts.value = updated
        persistSavedAccounts(updated)
    }

    fun removeSavedAccountSession(uid: String) {
        val updated = _savedAccounts.value.filter { it.uid != uid }
        _savedAccounts.value = updated
        persistSavedAccounts(updated)
    }

    fun clearAllSavedAccountSessions() {
        _savedAccounts.value = emptyList()
        prefs?.edit()?.remove("saved_accounts_json")?.apply()
    }

    private fun persistSavedAccounts(list: List<SavedAccountSession>) {
        val arr = JSONArray()
        list.forEach { acc ->
            arr.put(
                JSONObject().apply {
                    put("uid", acc.uid)
                    put("email", acc.email)
                    put("displayName", acc.displayName)
                    put("username", acc.username)
                    put("avatarUrl", acc.avatarUrl)
                    put("lastActiveMillis", acc.lastActiveMillis)
                }
            )
        }
        prefs?.edit()?.putString("saved_accounts_json", arr.toString())?.apply()
    }
}
