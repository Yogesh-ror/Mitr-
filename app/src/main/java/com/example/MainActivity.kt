package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.local.MitrLocalDatabase
import com.example.data.model.UiState
import com.example.data.model.UserProfile
import com.example.data.model.isFounderVipAccount
import com.example.data.repository.SocialRepository
import com.example.notifications.MitrPushNotificationHelper
import com.example.ui.components.MitrIcons
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.CreateContentScreen
import com.example.ui.screens.EmptyOrErrorCard
import com.example.ui.screens.HomeFeedScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.NotificationsBottomSheet
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReelsFeedScreen
import com.example.ui.screens.SearchAndDiscoveryScreen
import com.example.ui.screens.SettingsCenterScreen
import com.example.ui.screens.UserAvatarBadge
import com.example.ui.screens.signOut
import com.example.ui.theme.LuminaCoral
import com.example.ui.theme.LuminaPrismGradient
import com.example.ui.theme.LuminaTheme
import com.example.ui.viewmodel.SocialViewModel
import com.example.ui.components.stopAllGlobalAudio
import com.example.util.MitrOnlineRequiredScreen
import com.example.util.isCurrentlyOnline
import com.example.util.observeOnlineStatus
import com.example.util.triggerLocalNotificationWithWorkManager
import com.google.firebase.Firebase
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

private const val TAG = "AppCheckInit"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)
            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = FirebaseApp.getInstance().persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
            }
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
        } else {
            val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                }
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        configureAppCheck(this, intent)
        enableEdgeToEdge()
        setContent {
            LuminaAppRoot()
        }
    }

    override fun onStop() {
        super.onStop()
        stopAllGlobalAudio()
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun LuminaAppRoot(auth: FirebaseAuth = Firebase.auth) {
    val context = LocalContext.current
    val isOnlineFromObserver by remember(context) {
        observeOnlineStatus(context)
    }.collectAsStateWithLifecycle(initialValue = isCurrentlyOnline(context))
    var manualOnlineCheck by remember { mutableStateOf(isCurrentlyOnline(context)) }
    val isOnline = isOnlineFromObserver || manualOnlineCheck

    LaunchedEffect(isOnlineFromObserver) {
        manualOnlineCheck = isOnlineFromObserver
        if (!isOnlineFromObserver) {
            stopAllGlobalAudio()
        }
    }

    if (!isOnline) {
        LuminaTheme {
            MitrOnlineRequiredScreen(
                onRetryClick = {
                    manualOnlineCheck = isCurrentlyOnline(context)
                }
            )
        }
        return
    }

    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser

    if (user == null) {
        LuminaTheme {
            AuthScreen(onAuthSuccess = {})
        }
    } else {
        AuthenticatedLuminaShell(
            currentUserId = user.uid,
            defaultDisplayName = user.displayName ?: "Mitr Creator",
            defaultAvatarUrl = user.photoUrl?.toString() ?: ""
        )
    }
}

enum class MainNavDestination {
    HOME,
    REELS,
    MESSAGES,
    SEARCH,
    PROFILE
}

@Composable
fun AuthenticatedLuminaShell(
    currentUserId: String,
    defaultDisplayName: String,
    defaultAvatarUrl: String = ""
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: SocialViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                val localDao = MitrLocalDatabase.getInstance(app).mitrLocalDao()
                val prefs = app.getSharedPreferences("lumina_accounts_prefs", Context.MODE_PRIVATE)
                SocialViewModel(
                    repository = SocialRepository(db = db, localDao = localDao),
                    currentUserId = currentUserId,
                    prefs = prefs
                )
            }
        }
    )

    val profileState by viewModel.currentUserProfileState.collectAsStateWithLifecycle()
    val settings by viewModel.userSettingsState.collectAsStateWithLifecycle()
    val feedPostsState by viewModel.feedPostsState.collectAsStateWithLifecycle()
    val activeStories by viewModel.activeStoriesState.collectAsStateWithLifecycle()
    val allAccessibleStories by viewModel.allAccessibleStoriesState.collectAsStateWithLifecycle()
    val storyArchive by viewModel.storyArchiveState.collectAsStateWithLifecycle()
    val savedPosts by viewModel.savedPostsState.collectAsStateWithLifecycle()
    val reelsState by viewModel.reelsState.collectAsStateWithLifecycle()
    val conversations by viewModel.conversationsState.collectAsStateWithLifecycle()
    val discoverUsers by viewModel.discoverUsersState.collectAsStateWithLifecycle()
    val outgoingFollows by viewModel.outgoingFollowsState.collectAsStateWithLifecycle()
    val incomingFollows by viewModel.incomingFollowsState.collectAsStateWithLifecycle()
    val userRelations by viewModel.userRelationsState.collectAsStateWithLifecycle()
    val activityLogs by viewModel.activityLogsState.collectAsStateWithLifecycle()
    val savedAccounts by viewModel.savedAccounts.collectAsStateWithLifecycle()
    val musicSearchState by viewModel.musicSearchState.collectAsStateWithLifecycle()
    val savedMusicList by viewModel.savedMusicState.collectAsStateWithLifecycle()
    val usernameAvailable by viewModel.usernameCheckState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val sessionMinutes by viewModel.sessionMinutes.collectAsStateWithLifecycle()
    val notifications by viewModel.notificationsState.collectAsStateWithLifecycle()
    val latestFirestoreNotificationEvent by viewModel.latestFirestoreNotificationEvent.collectAsStateWithLifecycle()
    val latestIncomingDirectMessageEvent by viewModel.latestIncomingDirectMessageEvent.collectAsStateWithLifecycle()
    val mobileStorageStats by viewModel.mobileStorageStatsState.collectAsStateWithLifecycle()
    val feedSearchResult by viewModel.feedSearchResultState.collectAsStateWithLifecycle()
    val inspectedUserProfile by viewModel.inspectedUserProfileState.collectAsStateWithLifecycle()
    val inspectedUserPosts by viewModel.inspectedUserPostsState.collectAsStateWithLifecycle()
    val inspectedUserReels by viewModel.inspectedUserReelsState.collectAsStateWithLifecycle()
    val ownPosts by viewModel.ownPostsState.collectAsStateWithLifecycle()
    val ownReels by viewModel.ownReelsState.collectAsStateWithLifecycle()

    var currentTab by rememberSaveable { mutableStateOf(MainNavDestination.HOME) }
    var showCreateScreenTab by rememberSaveable { mutableStateOf<Int?>(null) }
    var showSettingsCenter by rememberSaveable { mutableStateOf(false) }
    var showNotificationsSheet by rememberSaveable { mutableStateOf(false) }
    var inspectedUserId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingOpenConversationId by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingOpenReelId by rememberSaveable { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        MitrPushNotificationHelper.refreshAndSyncFcmToken(context)
    }

    // Initialize FCM token registration & request POST_NOTIFICATIONS permission on Android 13+
    LaunchedEffect(currentUserId) {
        MitrPushNotificationHelper.ensureNotificationChannels(context)
        MitrPushNotificationHelper.refreshAndSyncFcmToken(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Immediately stop any active song whenever switching tabs or opening sub-screens
    LaunchedEffect(currentTab, showCreateScreenTab, showSettingsCenter, inspectedUserId) {
        stopAllGlobalAudio()
    }

    // Load full user profile from Firestore whenever opening another user's profile
    LaunchedEffect(inspectedUserId) {
        viewModel.loadInspectedUserProfile(inspectedUserId)
    }

    // Trigger real-time FCM / High-Importance Direct Message push notification when a user receives a new direct message
    LaunchedEffect(latestIncomingDirectMessageEvent) {
        val incomingConv = latestIncomingDirectMessageEvent
        if (incomingConv != null) {
            if (settings.pushNotificationsEnabled && settings.notifyDirectMessages) {
                val senderUid = incomingConv.lastSenderId
                val senderProfile = discoverUsers.find { it.userId == senderUid }
                val senderUsername = senderProfile?.username
                    ?: incomingConv.participantUsernames.firstOrNull {
                        !it.equals((profileState as? UiState.Success)?.data?.username.orEmpty(), ignoreCase = true)
                    }.orEmpty().ifBlank { "mitr_user" }
                val messageIdKey = "${incomingConv.id}_${incomingConv.updatedAt?.seconds ?: System.currentTimeMillis()}"
                MitrPushNotificationHelper.showDirectMessageNotification(
                    context = context,
                    conversationId = incomingConv.id,
                    messageId = messageIdKey,
                    senderUsername = senderUsername,
                    messageText = incomingConv.lastMessageText
                )
            }
            viewModel.consumeLatestIncomingDirectMessageEvent()
        }
    }

    // Trigger local notification via WorkManager whenever a new document is added to the user's Firestore notifications collection
    LaunchedEffect(latestFirestoreNotificationEvent) {
        val notif = latestFirestoreNotificationEvent
        if (notif != null) {
            val title = when (notif.type.uppercase()) {
                "LIKE" -> "New Like on Mitr"
                "STORY_REACTION" -> "Story Reaction on Mitr"
                "COMMENT" -> "New Comment on Mitr"
                "FOLLOW" -> "New Follower on Mitr"
                "COLLAB" -> "New Collaboration on Mitr"
                "MENTION" -> "New Mention on Mitr"
                else -> "Mitr Notification"
            }
            triggerLocalNotificationWithWorkManager(
                context = context,
                notificationId = notif.id,
                title = title,
                message = "@${notif.actorUsername} ${notif.message.ifBlank { "interacted with your content" }}",
                category = notif.type
            )
            viewModel.consumeLatestFirestoreNotificationEvent()
        }
    }

    LaunchedEffect(statusBanner) {
        val msg = statusBanner
        if (!msg.isNullOrBlank()) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearBanner()
        }
    }

    LuminaTheme(themeMode = settings.themeMode) {
        when (val pState = profileState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = LuminaCoral)
                }
            }

            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyOrErrorCard(
                        title = "Profile Sync Error",
                        subtitle = pState.message,
                        actionLabel = "Sign Out",
                        onAction = {
                            stopAllGlobalAudio()
                            signOut(context, credentialManager, {}, coroutineScope)
                        }
                    )
                }
            }

            is UiState.Success -> {
                val rawProfile = pState.data
                if (rawProfile == null) {
                    // Mandatory Onboarding Flow for New Users
                    OnboardingScreen(
                        initialDisplayName = defaultDisplayName,
                        initialAvatarUrl = defaultAvatarUrl,
                        usernameAvailable = usernameAvailable,
                        onCheckUsername = { viewModel.checkUsernameAvailability(it) },
                        onCompleteOnboarding = { username, displayName, bio, website, avatarUrl, isPrivate ->
                            viewModel.saveProfile(
                                username = username,
                                displayName = displayName,
                                bio = bio,
                                website = website,
                                avatarUrl = avatarUrl.ifBlank { defaultAvatarUrl },
                                isPrivate = isPrivate
                            )
                        },
                        onSignOut = {
                            stopAllGlobalAudio()
                            signOut(context, credentialManager, {}, coroutineScope)
                        }
                    )
                } else {
                    val currentProfile = if (rawProfile.avatarUrl.isBlank() && defaultAvatarUrl.isNotBlank()) {
                        rawProfile.copy(avatarUrl = defaultAvatarUrl)
                    } else {
                        rawProfile
                    }
                    val activeAuthEmail = remember(currentUserId, settings.email) {
                        Firebase.auth.currentUser?.email?.ifBlank { settings.email } ?: settings.email
                    }

                    // Automatically activate complimentary Blue Tick & Priority Reach for yogeshkhanchi985@gmail.com without any payment
                    LaunchedEffect(currentProfile.userId, currentProfile.isVerified, activeAuthEmail) {
                        if (isFounderVipAccount(
                                email = activeAuthEmail,
                                username = currentProfile.username
                            ) && !currentProfile.isVerified
                        ) {
                            viewModel.activateBlueTickSubscription("Founder VIP Auto-Grant (₹0)")
                        }
                    }

                    val unreadConversationsCount = remember(conversations, currentUserId) {
                        conversations.count { it.unreadCount > 0 && it.lastSenderId != currentUserId }
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets.safeDrawing,
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        bottomBar = {
                            if (showCreateScreenTab == null && !showSettingsCenter) {
                                LuminaBottomNavigationBar(
                                    currentDestination = currentTab,
                                    currentUserProfile = currentProfile,
                                    unreadMessagesCount = unreadConversationsCount,
                                    onSelectDestination = { dest ->
                                        stopAllGlobalAudio()
                                        inspectedUserId = null
                                        currentTab = dest
                                    },
                                    onOpenCreate = {
                                        stopAllGlobalAudio()
                                        showCreateScreenTab = if (currentTab == MainNavDestination.REELS) 2 else 0
                                    }
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when {
                                showCreateScreenTab != null -> {
                                    CreateContentScreen(
                                        initialTab = showCreateScreenTab ?: 0,
                                        currentUserId = currentUserId,
                                        discoverableUsers = discoverUsers,
                                        allowLocationBySettings = settings.allowPostLocation,
                                        musicSearchState = musicSearchState,
                                        savedMusicList = savedMusicList,
                                        onSearchMusic = { query, genre ->
                                            viewModel.searchMusic(query, genre)
                                        },
                                        onToggleSaveMusic = { track ->
                                            viewModel.toggleSaveMusic(track)
                                        },
                                        onBack = {
                                            stopAllGlobalAudio()
                                            showCreateScreenTab = null
                                        },
                                        onPublishPost = { caption, mediaUrls, mediaType, locationTag, taggedUsers, collabUsers, visibility, commentsAllowed, musicTrack ->
                                            viewModel.publishPost(
                                                caption = caption,
                                                mediaUrls = mediaUrls,
                                                mediaType = mediaType,
                                                locationTag = locationTag,
                                                taggedUsernames = taggedUsers,
                                                collaboratorUsernames = collabUsers,
                                                visibility = visibility,
                                                commentsAllowed = commentsAllowed,
                                                musicTrack = musicTrack,
                                                onComplete = {
                                                    stopAllGlobalAudio()
                                                    showCreateScreenTab = null
                                                }
                                            )
                                        },
                                        onPublishStory = { mediaUrl, mediaType, caption, overlayText, sticker, audience, musicTrack, collabUsers, taggedUsers ->
                                            viewModel.publishStory(
                                                mediaUrl = mediaUrl,
                                                mediaType = mediaType,
                                                caption = caption,
                                                overlayText = overlayText,
                                                stickerLabel = sticker,
                                                audience = audience,
                                                musicTrack = musicTrack,
                                                collaboratorUsernames = collabUsers,
                                                taggedUsernames = taggedUsers,
                                                onComplete = {
                                                    stopAllGlobalAudio()
                                                    showCreateScreenTab = null
                                                }
                                            )
                                        },
                                        onPublishReel = { videoUrl, caption, audioName, durationSec, musicTrack, collabUsers, taggedUsers, trimStartMs, trimEndMs ->
                                            viewModel.publishReel(
                                                videoUrl = videoUrl,
                                                caption = caption,
                                                audioTrackName = audioName,
                                                durationSeconds = durationSec,
                                                musicTrack = musicTrack,
                                                collaboratorUsernames = collabUsers,
                                                taggedUsernames = taggedUsers,
                                                videoTrimStartMs = trimStartMs,
                                                videoTrimEndMs = trimEndMs,
                                                onComplete = {
                                                    stopAllGlobalAudio()
                                                    showCreateScreenTab = null
                                                    currentTab = MainNavDestination.REELS
                                                }
                                            )
                                        }
                                    )
                                }

                                showSettingsCenter -> {
                                    SettingsCenterScreen(
                                        currentUserProfile = currentProfile,
                                        settings = settings,
                                        currentUserEmail = activeAuthEmail,
                                        sessionMinutes = sessionMinutes,
                                        activityLogs = activityLogs,
                                        userRelations = userRelations,
                                        discoverableUsers = discoverUsers,
                                        onSaveSettings = { viewModel.saveSettings(it) },
                                        onActivateBlueTickSubscription = { method ->
                                            viewModel.activateBlueTickSubscription(method)
                                        },
                                        onSaveCreatorPayoutAccount = { holder, upi, acc, ifsc ->
                                            viewModel.saveCreatorPayoutAccount(holder, upi, acc, ifsc)
                                        },
                                        onDeleteActivityItem = { viewModel.deleteActivityItem(it) },
                                        onAddRelation = { targetUser, relType ->
                                            viewModel.addRelation(targetUser, relType)
                                        },
                                        onRemoveRelation = { viewModel.removeRelation(it) },
                                        onReportProblem = { reason ->
                                            viewModel.submitReport(currentUserId, "USER", reason)
                                        },
                                        onSignOutCurrent = {
                                            stopAllGlobalAudio()
                                            showSettingsCenter = false
                                            signOut(context, credentialManager, {}, coroutineScope)
                                        },
                                        onSignOutAll = {
                                            stopAllGlobalAudio()
                                            showSettingsCenter = false
                                            viewModel.clearAllSavedAccountSessions()
                                            signOut(context, credentialManager, {}, coroutineScope)
                                        },
                                        onDeleteAccountPermanent = {
                                            stopAllGlobalAudio()
                                            viewModel.deleteOwnAccountData {
                                                showSettingsCenter = false
                                                signOut(context, credentialManager, {}, coroutineScope)
                                            }
                                        },
                                        mobileStorageStats = mobileStorageStats,
                                        onOffloadChatsToMobileStorage = {
                                            viewModel.offloadChatsToMobileStorage()
                                        },
                                        onBack = { showSettingsCenter = false }
                                    )
                                }

                                inspectedUserId != null && inspectedUserId != currentUserId -> {
                                    val feedPosts = (feedPostsState as? UiState.Success)?.data ?: emptyList()
                                    val allPosts = remember(inspectedUserPosts, feedPosts) {
                                        (inspectedUserPosts + feedPosts).distinctBy { it.id }
                                    }
                                    val publicReels = (reelsState as? UiState.Success)?.data ?: emptyList()
                                    val allReels = remember(inspectedUserReels, publicReels) {
                                        (inspectedUserReels + publicReels).distinctBy { it.id }
                                    }
                                    val cleanTarget = inspectedUserId!!.trim().removePrefix("@")

                                    val targetUser = remember(
                                        inspectedUserId,
                                        inspectedUserProfile,
                                        discoverUsers,
                                        feedSearchResult,
                                        allPosts,
                                        allReels,
                                        allAccessibleStories,
                                        notifications,
                                        outgoingFollows,
                                        incomingFollows
                                    ) {
                                        val fromResolved = inspectedUserProfile?.takeIf {
                                            it.userId == inspectedUserId ||
                                                it.id == inspectedUserId ||
                                                it.username.equals(cleanTarget, ignoreCase = true)
                                        }
                                        val fromDiscover = discoverUsers.find {
                                            it.userId == inspectedUserId ||
                                                it.id == inspectedUserId ||
                                                it.username.equals(cleanTarget, ignoreCase = true)
                                        }
                                        val fromSearch = feedSearchResult.matchedUsers.find {
                                            it.userId == inspectedUserId ||
                                                it.id == inspectedUserId ||
                                                it.username.equals(cleanTarget, ignoreCase = true)
                                        }
                                        val fromPost = allPosts.firstOrNull {
                                            it.authorId == inspectedUserId ||
                                                it.authorUsername.equals(cleanTarget, ignoreCase = true)
                                        }?.let { p ->
                                            val uname = p.authorUsername.ifBlank { cleanTarget }
                                            UserProfile(
                                                id = p.authorId.ifBlank { inspectedUserId!! },
                                                userId = p.authorId.ifBlank { inspectedUserId!! },
                                                username = uname,
                                                displayName = p.authorDisplayName.ifBlank { uname },
                                                avatarUrl = p.authorAvatarUrl,
                                                bio = "Creator on Mitr"
                                            )
                                        }
                                        val fromReel = allReels.firstOrNull {
                                            it.authorId == inspectedUserId ||
                                                it.authorUsername.equals(cleanTarget, ignoreCase = true)
                                        }?.let { r ->
                                            val uname = r.authorUsername.ifBlank { cleanTarget }
                                            UserProfile(
                                                id = r.authorId.ifBlank { inspectedUserId!! },
                                                userId = r.authorId.ifBlank { inspectedUserId!! },
                                                username = uname,
                                                displayName = uname,
                                                bio = "Creator on Mitr"
                                            )
                                        }
                                        val fromStory = allAccessibleStories.firstOrNull {
                                            it.authorId == inspectedUserId ||
                                                it.authorUsername.equals(cleanTarget, ignoreCase = true)
                                        }?.let { s ->
                                            val uname = s.authorUsername.ifBlank { cleanTarget }
                                            UserProfile(
                                                id = s.authorId.ifBlank { inspectedUserId!! },
                                                userId = s.authorId.ifBlank { inspectedUserId!! },
                                                username = uname,
                                                displayName = uname,
                                                bio = "Creator on Mitr"
                                            )
                                        }
                                        val fromNotif = notifications.firstOrNull {
                                            it.actorUserId == inspectedUserId ||
                                                it.actorUsername.equals(cleanTarget, ignoreCase = true)
                                        }?.let { n ->
                                            val uname = n.actorUsername.ifBlank { cleanTarget }
                                            UserProfile(
                                                id = n.actorUserId.ifBlank { inspectedUserId!! },
                                                userId = n.actorUserId.ifBlank { inspectedUserId!! },
                                                username = uname,
                                                displayName = n.actorDisplayName.ifBlank { uname },
                                                avatarUrl = n.actorAvatarUrl,
                                                bio = "Active on Mitr"
                                            )
                                        }
                                        val fromFollow = outgoingFollows.firstOrNull {
                                            it.followingId == inspectedUserId ||
                                                it.followingUsername.equals(cleanTarget, ignoreCase = true)
                                        }?.let { f ->
                                            val uname = f.followingUsername.ifBlank { cleanTarget }
                                            UserProfile(
                                                id = f.followingId.ifBlank { inspectedUserId!! },
                                                userId = f.followingId.ifBlank { inspectedUserId!! },
                                                username = uname,
                                                displayName = uname,
                                                bio = "Active on Mitr"
                                            )
                                        } ?: incomingFollows.firstOrNull {
                                            it.followerId == inspectedUserId ||
                                                it.followerUsername.equals(cleanTarget, ignoreCase = true)
                                        }?.let { f ->
                                            val uname = f.followerUsername.ifBlank { cleanTarget }
                                            UserProfile(
                                                id = f.followerId.ifBlank { inspectedUserId!! },
                                                userId = f.followerId.ifBlank { inspectedUserId!! },
                                                username = uname,
                                                displayName = uname,
                                                bio = "Active on Mitr"
                                            )
                                        }

                                        fromResolved
                                            ?: fromDiscover
                                            ?: fromSearch
                                            ?: fromPost
                                            ?: fromReel
                                            ?: fromStory
                                            ?: fromNotif
                                            ?: fromFollow
                                            ?: UserProfile(
                                                id = inspectedUserId!!,
                                                userId = inspectedUserId!!,
                                                username = cleanTarget.take(24),
                                                displayName = cleanTarget.replaceFirstChar {
                                                    if (it.isLowerCase()) it.titlecase() else it.toString()
                                                },
                                                bio = "Mitr Profile"
                                            )
                                    }
                                    ProfileScreen(
                                        currentUserId = currentUserId,
                                        viewedProfile = targetUser,
                                        userSettings = settings,
                                        currentUserEmail = activeAuthEmail,
                                        posts = allPosts,
                                        reels = allReels,
                                        savedPosts = savedPosts,
                                        savedMusicList = savedMusicList,
                                        userStories = allAccessibleStories,
                                        discoverableUsers = discoverUsers,
                                        hideLikeCounts = settings.hideLikeCounts,
                                        incomingFollowRequests = emptyList(),
                                        outgoingFollows = outgoingFollows,
                                        savedAccounts = savedAccounts,
                                        usernameAvailable = usernameAvailable,
                                        onCheckUsername = {},
                                        onSaveProfileEdits = { _, _, _, _, _, _ -> },
                                        onToggleFollow = { viewModel.toggleFollowUser(it) },
                                        onToggleSaveMusic = { viewModel.toggleSaveMusic(it) },
                                        onRespondFollowRequest = { _, _ -> },
                                        onMessageUser = { peer ->
                                            stopAllGlobalAudio()
                                            viewModel.startConversationWith(peer) { convId ->
                                                pendingOpenConversationId = convId
                                                inspectedUserId = null
                                                currentTab = MainNavDestination.MESSAGES
                                            }
                                        },
                                        onAddRelation = { peer, relType ->
                                            viewModel.addRelation(peer, relType)
                                        },
                                        onLikePost = { viewModel.toggleLikePost(it) },
                                        onSavePost = { post ->
                                            viewModel.toggleSavePost(
                                                postId = post.id,
                                                targetType = "POST",
                                                authorUsername = post.authorUsername,
                                                captionPreview = post.caption,
                                                mediaUrl = post.mediaUrls.firstOrNull() ?: ""
                                            )
                                        },
                                        onDeletePost = { viewModel.deletePost(it) },
                                        onSharePostToUser = { recipient, post ->
                                            viewModel.sharePostToUser(recipient, post)
                                        },
                                        onLikeReel = { viewModel.toggleLikeReel(it) },
                                        onSaveReel = { viewModel.toggleSaveReel(it) },
                                        onDeleteReel = { viewModel.deleteReel(it) },
                                        onOpenReelInFeed = { reel ->
                                            stopAllGlobalAudio()
                                            pendingOpenReelId = reel.id
                                            inspectedUserId = null
                                            currentTab = MainNavDestination.REELS
                                        },
                                        onCreateReel = {
                                            stopAllGlobalAudio()
                                            inspectedUserId = null
                                            showCreateScreenTab = 2
                                        },
                                        onRecordStoryView = { viewModel.recordStoryView(it) },
                                        onReactToStory = { story, emoji, note ->
                                            viewModel.reactToStory(story, emoji, note)
                                        },
                                        onDeleteStory = { viewModel.deleteStory(it) },
                                        onToggleHighlightStory = { story, title ->
                                            viewModel.toggleStoryHighlight(story, title)
                                        },
                                        observeComments = { targetId, targetType ->
                                            viewModel.observeComments(targetId, targetType)
                                        },
                                        observeFollowersForUser = { uid ->
                                            viewModel.observeFollowersForUser(uid)
                                        },
                                        observeFollowingForUser = { uid ->
                                            viewModel.observeFollowingForUser(uid)
                                        },
                                        onAddComment = { targetId, targetType, text, parentId ->
                                            viewModel.addComment(targetId, targetType, text, parentId)
                                        },
                                        onOpenUserProfile = { uid ->
                                            stopAllGlobalAudio()
                                            if (uid == currentUserId) {
                                                inspectedUserId = null
                                                currentTab = MainNavDestination.PROFILE
                                            } else {
                                                inspectedUserId = uid
                                            }
                                        },
                                        onOpenSettings = {},
                                        onOpenSearch = {
                                            stopAllGlobalAudio()
                                            inspectedUserId = null
                                            currentTab = MainNavDestination.SEARCH
                                        },
                                        onOpenNotifications = { showNotificationsSheet = true },
                                        notificationBadgeCount = notifications.size,
                                        onOpenMessages = {
                                            stopAllGlobalAudio()
                                            inspectedUserId = null
                                            currentTab = MainNavDestination.MESSAGES
                                        },
                                        onSwitchOrAddAccount = {},
                                        onRemoveSavedAccount = {},
                                        onSignOutCurrent = {},
                                        onSignOutAll = {},
                                        onBackIfOtherUser = {
                                            stopAllGlobalAudio()
                                            inspectedUserId = null
                                        }
                                    )
                                }

                                else -> {
                                    if (currentTab != MainNavDestination.HOME) {
                                        BackHandler {
                                            stopAllGlobalAudio()
                                            currentTab = MainNavDestination.HOME
                                        }
                                    }

                                    when (currentTab) {
                                        MainNavDestination.HOME -> {
                                            HomeFeedScreen(
                                                currentUserId = currentUserId,
                                                currentUserProfile = currentProfile,
                                                feedPostsState = feedPostsState,
                                                activeStories = activeStories,
                                                savedPosts = savedPosts,
                                                savedMusicList = savedMusicList,
                                                discoverableUsers = discoverUsers,
                                                outgoingFollows = outgoingFollows,
                                                firestoreSearchResult = feedSearchResult,
                                                notificationBadgeCount = notifications.size,
                                                hideLikeCounts = settings.hideLikeCounts,
                                                isRefreshing = isRefreshing,
                                                onRefresh = { viewModel.triggerPullToRefresh() },
                                                onExecuteFirestoreSearch = { query ->
                                                    viewModel.executeFeedFirestoreSearch(query)
                                                },
                                                onToggleFollowUser = { targetUser ->
                                                    viewModel.toggleFollowUser(targetUser)
                                                },
                                                onMessageUser = { peer ->
                                                    stopAllGlobalAudio()
                                                    viewModel.startConversationWith(peer) { convId ->
                                                        pendingOpenConversationId = convId
                                                        currentTab = MainNavDestination.MESSAGES
                                                    }
                                                },
                                                onOpenCreatePost = {
                                                    stopAllGlobalAudio()
                                                    showCreateScreenTab = 0
                                                },
                                                onOpenCreateStory = {
                                                    stopAllGlobalAudio()
                                                    showCreateScreenTab = 1
                                                },
                                                onLikePost = { viewModel.toggleLikePost(it) },
                                                onSavePost = { post ->
                                                    viewModel.toggleSavePost(
                                                        postId = post.id,
                                                        targetType = "POST",
                                                        authorUsername = post.authorUsername,
                                                        captionPreview = post.caption,
                                                        mediaUrl = post.mediaUrls.firstOrNull() ?: ""
                                                    )
                                                },
                                                onSharePostToUser = { recipient, post ->
                                                    viewModel.sharePostToUser(recipient, post)
                                                },
                                                onToggleSaveMusic = { track ->
                                                    viewModel.toggleSaveMusic(track)
                                                },
                                                onUpdatePost = { postId, caption, allowComments ->
                                                    viewModel.updatePostCaption(postId, caption, allowComments)
                                                },
                                                onUpdatePostCollaborators = { post, collabs, tags ->
                                                    viewModel.updatePostCollaboratorsAndTags(post, collabs, tags)
                                                },
                                                onLeaveCollaboration = { targetId, targetType ->
                                                    viewModel.leaveCollaboration(targetId, targetType)
                                                },
                                                onDeletePost = { viewModel.deletePost(it) },
                                                onReportContent = { id, type, reason ->
                                                    viewModel.submitReport(id, type, reason)
                                                },
                                                onMuteUserStories = { uid, uname, dname ->
                                                    viewModel.addRelation(
                                                        UserProfile(userId = uid, username = uname, displayName = dname),
                                                        "MUTED_STORIES"
                                                    )
                                                },
                                                onOpenUserProfile = { uid ->
                                                    stopAllGlobalAudio()
                                                    if (uid == currentUserId) {
                                                        currentTab = MainNavDestination.PROFILE
                                                    } else {
                                                        inspectedUserId = uid
                                                    }
                                                },
                                                onOpenSearch = {
                                                    stopAllGlobalAudio()
                                                    currentTab = MainNavDestination.SEARCH
                                                },
                                                onOpenNotifications = { showNotificationsSheet = true },
                                                onOpenMessages = {
                                                    stopAllGlobalAudio()
                                                    currentTab = MainNavDestination.MESSAGES
                                                },
                                                observeComments = { targetId, targetType ->
                                                    viewModel.observeComments(targetId, targetType)
                                                },
                                                onAddComment = { targetId, targetType, text, parentId ->
                                                    viewModel.addComment(targetId, targetType, text, parentId)
                                                },
                                                onRecordStoryView = { viewModel.recordStoryView(it) },
                                                onReactToStory = { story, emoji, note ->
                                                    viewModel.reactToStory(story, emoji, note)
                                                },
                                                onDeleteStory = { viewModel.deleteStory(it) },
                                                onToggleHighlightStory = { story, title ->
                                                    viewModel.toggleStoryHighlight(story, title)
                                                }
                                            )
                                        }

                                        MainNavDestination.REELS -> {
                                            ReelsFeedScreen(
                                                currentUserId = currentUserId,
                                                currentUserProfile = currentProfile,
                                                reelsState = reelsState,
                                                savedPosts = savedPosts,
                                                savedMusicList = savedMusicList,
                                                discoverableUsers = discoverUsers,
                                                followingIds = remember(outgoingFollows) {
                                                    outgoingFollows.filter { it.status == "ACCEPTED" }.map { it.followingId }
                                                },
                                                autoplayVideos = settings.autoplayVideos,
                                                initialReelId = pendingOpenReelId,
                                                onConsumeInitialReel = {
                                                    pendingOpenReelId = null
                                                },
                                                onLikeReel = { viewModel.toggleLikeReel(it) },
                                                onSaveReel = { reel ->
                                                    viewModel.toggleSaveReel(reel)
                                                },
                                                onToggleSaveMusic = { track ->
                                                    viewModel.toggleSaveMusic(track)
                                                },
                                                onIncrementView = { viewModel.incrementReelView(it) },
                                                onOpenUploadReel = {
                                                    stopAllGlobalAudio()
                                                    showCreateScreenTab = 2
                                                },
                                                onOpenUserProfile = { uid ->
                                                    stopAllGlobalAudio()
                                                    if (uid == currentUserId) {
                                                        currentTab = MainNavDestination.PROFILE
                                                    } else {
                                                        inspectedUserId = uid
                                                    }
                                                },
                                                onToggleFollowUser = { targetUser ->
                                                    viewModel.toggleFollowUser(targetUser)
                                                },
                                                onLeaveCollaboration = { targetId, targetType ->
                                                    viewModel.leaveCollaboration(targetId, targetType)
                                                },
                                                onOpenSearch = {
                                                    stopAllGlobalAudio()
                                                    currentTab = MainNavDestination.SEARCH
                                                },
                                                onOpenNotifications = { showNotificationsSheet = true },
                                                notificationBadgeCount = notifications.size,
                                                onOpenMessages = {
                                                    stopAllGlobalAudio()
                                                    currentTab = MainNavDestination.MESSAGES
                                                },
                                                observeComments = { targetId, targetType ->
                                                    viewModel.observeComments(targetId, targetType)
                                                },
                                                onAddComment = { targetId, targetType, text, parentId ->
                                                    viewModel.addComment(targetId, targetType, text, parentId)
                                                },
                                                onReportReel = { reelId, reason ->
                                                    viewModel.submitReport(reelId, "REEL", reason)
                                                }
                                            )
                                        }

                                        MainNavDestination.MESSAGES -> {
                                            MessagesScreen(
                                                currentUserId = currentUserId,
                                                currentUserProfile = currentProfile,
                                                conversations = conversations,
                                                discoverableUsers = discoverUsers,
                                                notificationBadgeCount = notifications.size,
                                                readReceiptsEnabled = settings.readReceiptsEnabled,
                                                showActivityStatus = settings.showActivityStatus,
                                                initialConversationId = pendingOpenConversationId,
                                                onConsumeInitialConversation = {
                                                    pendingOpenConversationId = null
                                                },
                                                observeMessages = { convId -> viewModel.observeMessages(convId) },
                                                onSendMessage = { conv, text, mediaUrl, mediaType, replyId ->
                                                    viewModel.sendDirectMessage(conv, text, mediaUrl, mediaType, replyId)
                                                },
                                                onDeleteMessage = { convId, msgId ->
                                                    viewModel.deleteMessage(convId, msgId)
                                                },
                                                onMarkConversationRead = { convId ->
                                                    viewModel.markConversationRead(convId)
                                                },
                                                onAcceptOrDeclineRequest = { convId, accept ->
                                                    viewModel.acceptOrDeclineMessageRequest(convId, accept)
                                                },
                                                onStartConversationWithUser = { peer ->
                                                    viewModel.startConversationWith(peer) { convId ->
                                                        pendingOpenConversationId = convId
                                                    }
                                                },
                                                onBlockUser = { peer ->
                                                    viewModel.addRelation(peer, "BLOCKED")
                                                },
                                                onReportConversation = { convId, reason ->
                                                    viewModel.submitReport(convId, "MESSAGE", reason)
                                                },
                                                mobileStorageStats = mobileStorageStats,
                                                onOffloadChatsToMobileStorage = {
                                                    viewModel.offloadChatsToMobileStorage()
                                                },
                                                onOpenSearch = { currentTab = MainNavDestination.SEARCH },
                                                onOpenNotifications = { showNotificationsSheet = true },
                                                onOpenUserProfile = { uid ->
                                                    if (uid == currentUserId) {
                                                        currentTab = MainNavDestination.PROFILE
                                                    } else {
                                                        inspectedUserId = uid
                                                    }
                                                }
                                            )
                                        }

                                        MainNavDestination.SEARCH -> {
                                            SearchAndDiscoveryScreen(
                                                currentUserId = currentUserId,
                                                discoverableUsers = discoverUsers,
                                                feedPostsState = feedPostsState,
                                                reelsState = reelsState,
                                                outgoingFollows = outgoingFollows,
                                                activityLogs = activityLogs,
                                                firestoreSearchResult = feedSearchResult,
                                                onExecuteFirestoreSearch = { query ->
                                                    viewModel.executeFeedFirestoreSearch(query)
                                                },
                                                onRecordSearch = { viewModel.recordSearchQuery(it) },
                                                onDeleteSearchItem = { viewModel.deleteActivityItem(it) },
                                                onToggleFollow = { viewModel.toggleFollowUser(it) },
                                                onMessageUser = { peer ->
                                                    viewModel.startConversationWith(peer) { convId ->
                                                        pendingOpenConversationId = convId
                                                        currentTab = MainNavDestination.MESSAGES
                                                    }
                                                },
                                                onOpenUserProfile = { uid ->
                                                    if (uid == currentUserId) {
                                                        currentTab = MainNavDestination.PROFILE
                                                    } else {
                                                        inspectedUserId = uid
                                                    }
                                                }
                                            )
                                        }

                                        MainNavDestination.PROFILE -> {
                                            val feedPosts = (feedPostsState as? UiState.Success)?.data ?: emptyList()
                                            val allPosts = remember(ownPosts, feedPosts) {
                                                (ownPosts + feedPosts).distinctBy { it.id }
                                            }
                                            val publicReels = (reelsState as? UiState.Success)?.data ?: emptyList()
                                            val allReels = remember(ownReels, publicReels) {
                                                (ownReels + publicReels).distinctBy { it.id }
                                            }
                                            ProfileScreen(
                                                currentUserId = currentUserId,
                                                viewedProfile = currentProfile,
                                                userSettings = settings,
                                                currentUserEmail = activeAuthEmail,
                                                posts = allPosts,
                                                reels = allReels,
                                                savedPosts = savedPosts,
                                                savedMusicList = savedMusicList,
                                                userStories = allAccessibleStories,
                                                discoverableUsers = discoverUsers,
                                                hideLikeCounts = settings.hideLikeCounts,
                                                incomingFollowRequests = incomingFollows,
                                                outgoingFollows = outgoingFollows,
                                                savedAccounts = savedAccounts,
                                                usernameAvailable = usernameAvailable,
                                                onCheckUsername = { viewModel.checkUsernameAvailability(it) },
                                                onSaveProfileEdits = { uname, dname, bio, web, avatar, priv ->
                                                    viewModel.saveProfile(uname, dname, bio, web, avatar, priv)
                                                },
                                                onActivateBlueTickSubscription = { method ->
                                                    viewModel.activateBlueTickSubscription(method)
                                                },
                                                onSaveCreatorPayoutAccount = { holder, upi, acc, ifsc ->
                                                    viewModel.saveCreatorPayoutAccount(holder, upi, acc, ifsc)
                                                },
                                                onToggleFollow = {},
                                                onToggleSaveMusic = { track ->
                                                    viewModel.toggleSaveMusic(track)
                                                },
                                                onRespondFollowRequest = { req, accept ->
                                                    viewModel.respondToFollowRequest(req, accept)
                                                },
                                                onMessageUser = {},
                                                onAddRelation = { _, _ -> },
                                                onLikePost = { viewModel.toggleLikePost(it) },
                                                onSavePost = { post ->
                                                    viewModel.toggleSavePost(
                                                        postId = post.id,
                                                        targetType = "POST",
                                                        authorUsername = post.authorUsername,
                                                        captionPreview = post.caption,
                                                        mediaUrl = post.mediaUrls.firstOrNull() ?: ""
                                                    )
                                                },
                                                onDeletePost = { viewModel.deletePost(it) },
                                                onSharePostToUser = { recipient, post ->
                                                    viewModel.sharePostToUser(recipient, post)
                                                },
                                                onLikeReel = { viewModel.toggleLikeReel(it) },
                                                onSaveReel = { viewModel.toggleSaveReel(it) },
                                                onDeleteReel = { viewModel.deleteReel(it) },
                                                onOpenReelInFeed = { reel ->
                                                    stopAllGlobalAudio()
                                                    pendingOpenReelId = reel.id
                                                    currentTab = MainNavDestination.REELS
                                                },
                                                onCreateReel = {
                                                    stopAllGlobalAudio()
                                                    showCreateScreenTab = 2
                                                },
                                                archivedStories = storyArchive,
                                                onRecordStoryView = { viewModel.recordStoryView(it) },
                                                onReactToStory = { story, emoji, note ->
                                                    viewModel.reactToStory(story, emoji, note)
                                                },
                                                onDeleteStory = { viewModel.deleteStory(it) },
                                                onToggleHighlightStory = { story, title ->
                                                    viewModel.toggleStoryHighlight(story, title)
                                                },
                                                observeComments = { targetId, targetType ->
                                                    viewModel.observeComments(targetId, targetType)
                                                },
                                                observeFollowersForUser = { uid ->
                                                    viewModel.observeFollowersForUser(uid)
                                                },
                                                observeFollowingForUser = { uid ->
                                                    viewModel.observeFollowingForUser(uid)
                                                },
                                                onAddComment = { targetId, targetType, text, parentId ->
                                                    viewModel.addComment(targetId, targetType, text, parentId)
                                                },
                                                onOpenUserProfile = { uid ->
                                                    stopAllGlobalAudio()
                                                    if (uid != currentUserId) {
                                                        inspectedUserId = uid
                                                    }
                                                },
                                                onOpenSettings = {
                                                    stopAllGlobalAudio()
                                                    showSettingsCenter = true
                                                },
                                                onOpenSearch = {
                                                    stopAllGlobalAudio()
                                                    currentTab = MainNavDestination.SEARCH
                                                },
                                                onOpenNotifications = { showNotificationsSheet = true },
                                                notificationBadgeCount = notifications.size,
                                                onOpenMessages = {
                                                    stopAllGlobalAudio()
                                                    currentTab = MainNavDestination.MESSAGES
                                                },
                                                onSwitchOrAddAccount = {
                                                    stopAllGlobalAudio()
                                                    signOut(context, credentialManager, {}, coroutineScope)
                                                },
                                                onRemoveSavedAccount = { uid ->
                                                    viewModel.removeSavedAccountSession(uid)
                                                },
                                                onSignOutCurrent = {
                                                    stopAllGlobalAudio()
                                                    signOut(context, credentialManager, {}, coroutineScope)
                                                },
                                                onSignOutAll = {
                                                    stopAllGlobalAudio()
                                                    viewModel.clearAllSavedAccountSessions()
                                                    signOut(context, credentialManager, {}, coroutineScope)
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            if (showNotificationsSheet) {
                                NotificationsBottomSheet(
                                    currentUserId = currentUserId,
                                    notifications = notifications,
                                    discoverableUsers = discoverUsers,
                                    onToggleFollowUser = { targetUser ->
                                        viewModel.toggleFollowUser(targetUser)
                                    },
                                    onRespondFollowRequest = { req, accept ->
                                        viewModel.respondToFollowRequest(req, accept)
                                    },
                                    onRespondCollabInvite = { notif, accept ->
                                        viewModel.respondToCollabInvite(notif, accept)
                                    },
                                    onOpenUserProfile = { uid ->
                                        stopAllGlobalAudio()
                                        showNotificationsSheet = false
                                        if (uid == currentUserId) {
                                            currentTab = MainNavDestination.PROFILE
                                        } else {
                                            inspectedUserId = uid
                                        }
                                    },
                                    onDismiss = { showNotificationsSheet = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Floating Rounded-Rectangle Bottom Navigation Bar:
 * - Styled as a floating rectangular dock with rounded corners at the bottom of the screen
 * - Uses the same PlayCircleFilled icon for Reels as the Profile screen
 */
@Composable
fun LuminaBottomNavigationBar(
    currentDestination: MainNavDestination,
    currentUserProfile: UserProfile,
    unreadMessagesCount: Int,
    onSelectDestination: (MainNavDestination) -> Unit,
    onOpenCreate: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            shape = RoundedCornerShape(26.dp),
            tonalElevation = 8.dp,
            shadowElevation = 14.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                    shape = RoundedCornerShape(26.dp)
                )
                .testTag("lumina_bottom_navigation_bar")
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 1. Home
                BottomNavPrimaryItem(
                    label = "Home",
                    selected = currentDestination == MainNavDestination.HOME,
                    selectedIcon = Icons.Filled.Home,
                    unselectedIcon = Icons.Outlined.Home,
                    testTag = "nav_item_home",
                    onClick = { onSelectDestination(MainNavDestination.HOME) }
                )

                // 2. Reels (Uses the exact same PlayCircleFilled icon used in ProfileScreen)
                BottomNavPrimaryItem(
                    label = "Reels",
                    selected = currentDestination == MainNavDestination.REELS,
                    selectedIcon = Icons.Default.PlayCircleFilled,
                    unselectedIcon = Icons.Default.PlayCircleFilled,
                    testTag = "nav_item_reels",
                    onClick = { onSelectDestination(MainNavDestination.REELS) }
                )

                // 3. Center Circular Purple-to-Magenta '+' Create Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(LuminaPrismGradient)
                        .clickable(onClick = onOpenCreate)
                        .testTag("nav_item_create_fab"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // 4. Chats (with pink unread badge)
                BottomNavPrimaryItem(
                    label = "Chats",
                    selected = currentDestination == MainNavDestination.MESSAGES,
                    selectedIcon = MitrIcons.MessageFilled,
                    unselectedIcon = MitrIcons.MessageOutlined,
                    badgeCount = unreadMessagesCount,
                    testTag = "nav_item_messages",
                    onClick = { onSelectDestination(MainNavDestination.MESSAGES) }
                )

                // 5. Profile
                val isProfileSelected = currentDestination == MainNavDestination.PROFILE
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onSelectDestination(MainNavDestination.PROFILE) }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("nav_item_profile_avatar")
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .then(
                                if (isProfileSelected) {
                                    Modifier
                                        .border(2.dp, LuminaPrismGradient, CircleShape)
                                        .padding(1.5.dp)
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        UserAvatarBadge(
                            avatarUrl = currentUserProfile.avatarUrl,
                            displayName = currentUserProfile.displayName,
                            size = if (isProfileSelected) 22.dp else 24.dp
                        )
                    }
                    Text(
                        text = "Profile",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isProfileSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isProfileSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavPrimaryItem(
    label: String,
    selected: Boolean,
    selectedIcon: ImageVector,
    unselectedIcon: ImageVector,
    badgeCount: Int = 0,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .testTag(testTag)
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(containerColor = Color(0xFFD81B60)) {
                        Text("$badgeCount", color = Color.White)
                    }
                }
            }
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else unselectedIcon,
                contentDescription = label,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
        AnimatedVisibility(visible = true) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

