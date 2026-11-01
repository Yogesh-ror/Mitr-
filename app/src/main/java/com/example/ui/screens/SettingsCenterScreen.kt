package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.core.content.ContextCompat
import com.example.data.model.UserActivityLog
import com.example.data.model.UserProfile
import com.example.data.model.UserRelation
import com.example.data.model.UserSettings
import com.example.data.model.isFounderVipAccount
import com.example.data.model.isUserBlueTickVerified
import com.example.ui.components.AuroraAmbientBackground
import com.example.ui.components.InteractiveTricolorMitrLogo
import com.example.ui.components.LuminaGlassCard
import com.example.ui.components.LuminaGradientButton
import com.example.ui.components.LuminaPillChip
import com.example.ui.components.MitrBlueTickSubscriptionSheet
import com.example.ui.components.MitrInteractiveLogoStudioDialog
import com.example.ui.components.VerifiedBadgeIcon
import com.example.ui.theme.LuminaCoral
import com.example.ui.theme.LuminaEmerald

enum class SettingsPage(val title: String) {
    HOME("Settings"),
    ACCOUNT("Account Management"),
    TIME_SPENT("Time Spent"),
    ACTIVITY_LOG("Your Activity"),
    NOTIFICATIONS("Notifications"),
    PRIVACY_RELATIONS("Privacy & Relations"),
    HIDE_LIKES("Like & Share Counts"),
    STORY_CONTROLS("Story Controls"),
    MESSAGES_REPLIES("Messages & Replies"),
    TAGS_MENTIONS("Tags & Mentions"),
    LOCATION("Location & Permissions"),
    CONTENT_ACCESSIBILITY("Appearance & Media"),
    SECURITY_SUPPORT("Security & Support")
}

@Composable
fun SettingsCenterScreen(
    currentUserProfile: UserProfile,
    settings: UserSettings,
    currentUserEmail: String = "",
    sessionMinutes: Long,
    activityLogs: List<UserActivityLog>,
    userRelations: List<UserRelation>,
    discoverableUsers: List<UserProfile>,
    onSaveSettings: (UserSettings) -> Unit,
    onActivateBlueTickSubscription: (String) -> Unit = {},
    onSaveCreatorPayoutAccount: (String, String, String, String) -> Unit = { _, _, _, _ -> },
    onDeleteActivityItem: (String) -> Unit,
    onAddRelation: (UserProfile, String) -> Unit,
    onRemoveRelation: (String) -> Unit,
    onReportProblem: (String) -> Unit,
    onSignOutCurrent: () -> Unit,
    onSignOutAll: () -> Unit,
    onDeleteAccountPermanent: () -> Unit,
    onBack: () -> Unit
) {
    var currentPage by remember { mutableStateOf(SettingsPage.HOME) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showReportProblemDialog by remember { mutableStateOf(false) }
    var showBlueTickSubscriptionSheet by remember { mutableStateOf(false) }
    var showInteractiveLogoStudioDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Live Hardware Permission States (Location, Microphone, Camera, Gallery)
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasMicPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }
    var galleryVerified by remember { mutableStateOf(false) }

    val locationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        hasLocationPermission = res[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            res[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val micLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasMicPermission = granted
    }

    val galleryCheckLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) galleryVerified = true
    }

    BackHandler {
        if (currentPage == SettingsPage.HOME) {
            onBack()
        } else {
            currentPage = SettingsPage.HOME
        }
    }

    AuroraAmbientBackground(intensity = 0.08f) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Compact Top Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentPage == SettingsPage.HOME) onBack()
                            else currentPage = SettingsPage.HOME
                        },
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = currentPage.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            when (currentPage) {
                SettingsPage.HOME -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("settings_directory_list"),
                        contentPadding = PaddingValues(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Category 0: Mitr Blue Tick Subscription (₹300/mo • Free for yogeshkhanchi985@gmail.com), Payout Transfer & Founder Logo Copy
                        item {
                            val isVerified = isUserBlueTickVerified(
                                profile = currentUserProfile,
                                email = currentUserEmail,
                                settings = settings
                            )
                            val isFounderAdmin = isFounderVipAccount(
                                email = currentUserEmail.ifBlank { settings.email },
                                username = currentUserProfile.username
                            )
                            SettingsCategoryGroupCard(
                                categoryTitle = if (isFounderAdmin) {
                                    "Mitr Blue Tick, Revenue Payout & Official Logo"
                                } else {
                                    "Mitr Blue Tick & Creator Boost"
                                }
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(Color(0xFF0095F6).copy(alpha = 0.10f))
                                            .clickable { showBlueTickSubscriptionSheet = true }
                                            .padding(12.dp)
                                            .testTag("settings_open_blue_tick_button"),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            VerifiedBadgeIcon(size = 22.dp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = if (isVerified) {
                                                        "Blue Tick Active & Boosted Reach"
                                                    } else {
                                                        "Get Blue Tick (₹300/mo) & Boost Reach"
                                                    },
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                Text(
                                                    text = if (isFounderAdmin) {
                                                        "Complimentary Founder VIP • Configure Bank/UPI payout transfer"
                                                    } else {
                                                        "Get Verified Blue Tick & 3.5x more Reel views and likes"
                                                    },
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = "Open Blue Tick"
                                        )
                                    }

                                    if (isFounderAdmin) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                                                .clickable { showInteractiveLogoStudioDialog = true }
                                                .padding(12.dp)
                                                .testTag("settings_open_logo_studio_button"),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                InteractiveTricolorMitrLogo(
                                                    size = 36.dp,
                                                    onLogoClick = { showInteractiveLogoStudioDialog = true }
                                                )
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text(
                                                        text = "Interactive Tiranga Logo & Get PNG Copy",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = "Founder Exclusive • Save or share 1024×1024 PNG copy",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = "Open Logo Studio"
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Category 1: Appearance & Theme (Quick Black / White / Blue Theme Toggle)
                        item {
                            SettingsCategoryGroupCard(categoryTitle = "Theme & Appearance") {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Text(
                                        text = "Black, White & Electric Blue Theme",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        listOf("DARK" to "Black & Blue", "LIGHT" to "White & Blue", "SYSTEM" to "System").forEach { (mode, label) ->
                                            LuminaPillChip(
                                                label = label,
                                                selected = settings.themeMode == mode,
                                                onClick = { onSaveSettings(settings.copy(themeMode = mode)) }
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                SettingsGroupRowItem(
                                    icon = Icons.Default.AccessibilityNew,
                                    title = "Media & Accessibility",
                                    subtitle = "Data saver, autoplay, video quality, captions",
                                    onClick = { currentPage = SettingsPage.CONTENT_ACCESSIBILITY }
                                )
                            }
                        }

                        // Category 2: Account & Activity
                        item {
                            SettingsCategoryGroupCard(categoryTitle = "Account & Activity") {
                                SettingsGroupRowItem(
                                    icon = Icons.Default.PersonOutline,
                                    title = "Account Management",
                                    subtitle = "Profile details, account status, sign out or delete",
                                    onClick = { currentPage = SettingsPage.ACCOUNT }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                SettingsGroupRowItem(
                                    icon = Icons.Default.Schedule,
                                    title = "Time Spent",
                                    subtitle = "${sessionMinutes + settings.todayUsageMinutes} min today • Daily limit & quiet mode",
                                    onClick = { currentPage = SettingsPage.TIME_SPENT }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                SettingsGroupRowItem(
                                    icon = Icons.Default.History,
                                    title = "Your Activity",
                                    subtitle = "Likes, comments, searches, and story history",
                                    onClick = { currentPage = SettingsPage.ACTIVITY_LOG }
                                )
                            }
                        }

                        // Category 3: Privacy, Audience & Interactions
                        item {
                            SettingsCategoryGroupCard(categoryTitle = "Privacy & Interactions") {
                                SettingsSwitchTile(
                                    title = "Private Account",
                                    subtitle = if (settings.isPrivateAccount || currentUserProfile.isPrivate) {
                                        "Private: Only approved followers can see your followers & following lists"
                                    } else {
                                        "Public: Anyone on Mitr can see your followers & following lists"
                                    },
                                    checked = settings.isPrivateAccount || currentUserProfile.isPrivate,
                                    onCheckedChange = { isPriv ->
                                        onSaveSettings(settings.copy(isPrivateAccount = isPriv, requireFollowApproval = isPriv))
                                    }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                SettingsGroupRowItem(
                                    icon = Icons.Default.Lock,
                                    title = "Account Privacy & Relations",
                                    subtitle = "${if (settings.isPrivateAccount || currentUserProfile.isPrivate) "Private" else "Public"} • Close Friends, Blocked, Restricted, Muted",
                                    onClick = { currentPage = SettingsPage.PRIVACY_RELATIONS }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                SettingsGroupRowItem(
                                    icon = Icons.Default.StarBorder,
                                    title = "Story Controls",
                                    subtitle = "Story viewers, Close Friends, archive & sharing",
                                    onClick = { currentPage = SettingsPage.STORY_CONTROLS }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                SettingsGroupRowItem(
                                    icon = Icons.Default.ChatBubbleOutline,
                                    title = "Messages & Replies",
                                    subtitle = "Who can message you (${settings.whoCanMessage}), read receipts",
                                    onClick = { currentPage = SettingsPage.MESSAGES_REPLIES }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                SettingsGroupRowItem(
                                    icon = Icons.Default.AlternateEmail,
                                    title = "Tags & Mentions",
                                    subtitle = "Tag permissions (${settings.whoCanTag}) & manual review",
                                    onClick = { currentPage = SettingsPage.TAGS_MENTIONS }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                SettingsGroupRowItem(
                                    icon = Icons.Default.FavoriteBorder,
                                    title = "Like & Share Counts",
                                    subtitle = if (settings.hideLikeCounts) "Hidden on feed" else "Visible on feed",
                                    onClick = { currentPage = SettingsPage.HIDE_LIKES }
                                )
                            }
                        }

                        // Category 4: Device Permissions (Location, Microphone, Gallery, Camera)
                        item {
                            SettingsCategoryGroupCard(categoryTitle = "Device Permissions") {
                                SettingsPermissionRow(
                                    icon = Icons.Default.LocationOn,
                                    title = "Location Access",
                                    granted = hasLocationPermission,
                                    onRequest = {
                                        locationLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                SettingsPermissionRow(
                                    icon = Icons.Default.Mic,
                                    title = "Microphone Access",
                                    granted = hasMicPermission,
                                    onRequest = {
                                        micLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                SettingsPermissionRow(
                                    icon = Icons.Default.CameraAlt,
                                    title = "Camera Access",
                                    granted = hasCameraPermission,
                                    onRequest = {
                                        cameraLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                SettingsPermissionRow(
                                    icon = Icons.Default.PhotoLibrary,
                                    title = "Gallery Media Picker",
                                    granted = galleryVerified,
                                    actionLabel = if (galleryVerified) "Ready" else "Verify",
                                    onRequest = {
                                        galleryCheckLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                                        )
                                    }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                SettingsGroupRowItem(
                                    icon = Icons.Default.LocationOn,
                                    title = "Location Tagging Preferences",
                                    subtitle = if (settings.allowPostLocation) "In-app location tags enabled" else "Disabled",
                                    onClick = { currentPage = SettingsPage.LOCATION }
                                )
                            }
                        }

                        // Category 5: Notifications, Security & Support
                        item {
                            SettingsCategoryGroupCard(categoryTitle = "Notifications & Security") {
                                SettingsGroupRowItem(
                                    icon = Icons.Default.Notifications,
                                    title = "Notifications",
                                    subtitle = if (!settings.pushNotificationsEnabled) "All notifications paused" else "Likes, comments, follows & messages",
                                    onClick = { currentPage = SettingsPage.NOTIFICATIONS }
                                )
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                                SettingsGroupRowItem(
                                    icon = Icons.Default.Security,
                                    title = "Security & Support",
                                    subtitle = "Session security, report a problem, community policies",
                                    onClick = { currentPage = SettingsPage.SECURITY_SUPPORT }
                                )
                            }
                        }
                    }
                }

                SettingsPage.ACCOUNT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsCategoryGroupCard(categoryTitle = "Authenticated Account") {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(currentUserProfile.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("@${currentUserProfile.username}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    "Account Status: ${if (settings.accountDeactivated) "DEACTIVATED" else "ACTIVE"}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LuminaEmerald
                                )
                            }
                        }

                        SettingsCategoryGroupCard(categoryTitle = "Session Controls") {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        onSaveSettings(settings.copy(accountDeactivated = !settings.accountDeactivated))
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(if (!settings.accountDeactivated) "Temporarily Deactivate Account" else "Reactivate Account")
                                }

                                OutlinedButton(
                                    onClick = onSignOutCurrent,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("settings_logout_current_button")
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Log Out Current Account")
                                }

                                OutlinedButton(
                                    onClick = onSignOutAll,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Log Out of All Accounts")
                                }

                                Button(
                                    onClick = { showDeleteConfirmDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("settings_delete_account_button")
                                ) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Permanently Delete Account")
                                }
                            }
                        }
                    }
                }

                SettingsPage.TIME_SPENT -> {
                    val totalMinutes = sessionMinutes + settings.todayUsageMinutes
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsCategoryGroupCard(categoryTitle = "Daily Time Usage") {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text("$totalMinutes min today", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                Text("Daily Limit: ${settings.dailyTimeLimitMinutes} min", style = MaterialTheme.typography.bodySmall)
                                Slider(
                                    value = settings.dailyTimeLimitMinutes.toFloat(),
                                    onValueChange = { onSaveSettings(settings.copy(dailyTimeLimitMinutes = it.toLong())) },
                                    valueRange = 15f..240f,
                                    steps = 14
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile(
                                title = "Break Reminders",
                                subtitle = "Remind after ${settings.breakReminderMinutes} min of continuous scrolling",
                                checked = settings.breakReminderMinutes > 0L,
                                onCheckedChange = { enabled ->
                                    onSaveSettings(settings.copy(breakReminderMinutes = if (enabled) 20L else 0L))
                                }
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile(
                                title = "Quiet / Sleep Mode",
                                subtitle = "Mute notifications during (${settings.quietHoursStart} – ${settings.quietHoursEnd})",
                                checked = settings.quietHoursEnabled,
                                onCheckedChange = { onSaveSettings(settings.copy(quietHoursEnabled = it)) }
                            )
                        }
                    }
                }

                SettingsPage.ACTIVITY_LOG -> {
                    var filterType by remember { mutableStateOf("ALL") }
                    val filtered = remember(activityLogs, filterType) {
                        if (filterType == "ALL") activityLogs
                        else activityLogs.filter { it.activityType == filterType }
                    }
                    Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("ALL", "LIKE", "COMMENT", "SEARCH", "STORY_UPLOAD").forEach { tab ->
                                LuminaPillChip(
                                    label = tab,
                                    selected = filterType == tab,
                                    onClick = { filterType = tab }
                                )
                            }
                        }

                        if (filtered.isEmpty()) {
                            EmptyOrErrorCard(
                                title = "No activity records",
                                subtitle = "Your real-time likes, comments, searches, and uploads are logged here."
                            )
                        } else {
                            SettingsCategoryGroupCard(categoryTitle = "Activity History (${filtered.size})") {
                                LazyColumn(modifier = Modifier.height(420.dp)) {
                                    items(filtered, key = { it.id }) { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(item.activityType, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                Text(item.summary, style = MaterialTheme.typography.bodySmall)
                                            }
                                            IconButton(onClick = { onDeleteActivityItem(item.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete activity", modifier = Modifier.size(16.dp))
                                            }
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    }
                                }
                            }
                        }
                    }
                }

                SettingsPage.NOTIFICATIONS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsCategoryGroupCard(categoryTitle = "Notification Categories") {
                            SettingsSwitchTile("Pause All Notifications", "Temporarily mute all alerts", !settings.pushNotificationsEnabled) {
                                onSaveSettings(settings.copy(pushNotificationsEnabled = !it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("Likes", "Notify when someone likes your post or reel", settings.notifyLikes) {
                                onSaveSettings(settings.copy(notifyLikes = it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("Comments", "Notify when someone comments on your content", settings.notifyComments) {
                                onSaveSettings(settings.copy(notifyComments = it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("New Followers & Requests", "Notify on new followers or follow requests", settings.notifyFollowers) {
                                onSaveSettings(settings.copy(notifyFollowers = it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("Direct Messages", "Notify on new chat messages and requests", settings.notifyDirectMessages) {
                                onSaveSettings(settings.copy(notifyDirectMessages = it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("Stories & Reels", "Notify when close friends publish new stories", settings.notifyStoryReplies) {
                                onSaveSettings(settings.copy(notifyStoryReplies = it))
                            }
                        }
                    }
                }

                SettingsPage.PRIVACY_RELATIONS -> {
                    var selectedRelTab by remember { mutableIntStateOf(0) }
                    val relTypes = listOf("CLOSE_FRIEND", "BLOCKED", "RESTRICTED", "MUTED_POSTS", "MUTED_STORIES")
                    val activeRelType = relTypes[selectedRelTab]
                    val currentList = userRelations.filter { it.relationType == activeRelType }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsCategoryGroupCard(categoryTitle = "Account Visibility") {
                            SettingsSwitchTile(
                                title = "Private Account",
                                subtitle = if (settings.isPrivateAccount || currentUserProfile.isPrivate) {
                                    "Only people who follow you can view your followers & following lists"
                                } else {
                                    "Public account — anyone can view your followers & following lists"
                                },
                                checked = settings.isPrivateAccount || currentUserProfile.isPrivate,
                                onCheckedChange = { isPriv ->
                                    onSaveSettings(settings.copy(isPrivateAccount = isPriv, requireFollowApproval = isPriv))
                                }
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            relTypes.forEachIndexed { index, type ->
                                LuminaPillChip(
                                    label = type.replace("_", " "),
                                    selected = selectedRelTab == index,
                                    onClick = { selectedRelTab = index }
                                )
                            }
                        }

                        SettingsCategoryGroupCard(categoryTitle = "$activeRelType (${currentList.size})") {
                            if (currentList.isEmpty()) {
                                Text(
                                    text = "No accounts in $activeRelType.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(14.dp)
                                )
                            } else {
                                Column {
                                    currentList.forEach { rel ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(rel.targetDisplayName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                                Text("@${rel.targetUsername}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            OutlinedButton(onClick = { onRemoveRelation(rel.id) }) {
                                                Text("Remove")
                                            }
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                    }
                                }
                            }
                        }
                    }
                }

                SettingsPage.HIDE_LIKES -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsCategoryGroupCard(categoryTitle = "Like & Share Visibility") {
                            SettingsSwitchTile(
                                title = "Hide Like & Share Counts",
                                subtitle = "Hide numeric like counters on posts across your feed",
                                checked = settings.hideLikeCounts,
                                onCheckedChange = { onSaveSettings(settings.copy(hideLikeCounts = it)) }
                            )
                        }
                    }
                }

                SettingsPage.STORY_CONTROLS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsCategoryGroupCard(categoryTitle = "Story Privacy & Sharing") {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Who Can Reply to Your Stories", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("EVERYONE", "FOLLOWERS", "OFF").forEach { opt ->
                                        LuminaPillChip(
                                            label = opt,
                                            selected = settings.whoCanReplyStories == opt,
                                            onClick = { onSaveSettings(settings.copy(whoCanReplyStories = opt)) }
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("Allow Story Sharing", "Let viewers share your public stories", settings.allowStorySharing) {
                                onSaveSettings(settings.copy(allowStorySharing = it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("Save Stories to Archive", "Automatically archive stories after 24h", settings.archiveStories) {
                                onSaveSettings(settings.copy(archiveStories = it))
                            }
                        }
                    }
                }

                SettingsPage.MESSAGES_REPLIES -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsCategoryGroupCard(categoryTitle = "Direct Messaging Controls") {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Who Can Send You Direct Messages", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("EVERYONE", "FOLLOWERS", "NO_ONE").forEach { opt ->
                                        LuminaPillChip(
                                            label = opt,
                                            selected = settings.whoCanMessage == opt,
                                            onClick = { onSaveSettings(settings.copy(whoCanMessage = opt)) }
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("Read Receipts", "Show when you have read direct messages", settings.readReceiptsEnabled) {
                                onSaveSettings(settings.copy(readReceiptsEnabled = it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("Activity Status", "Show real-time active status in chat", settings.showActivityStatus) {
                                onSaveSettings(settings.copy(showActivityStatus = it))
                            }
                        }
                    }
                }

                SettingsPage.TAGS_MENTIONS -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsCategoryGroupCard(categoryTitle = "Tag & Mention Controls") {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Who Can Tag You", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("EVERYONE", "FOLLOWERS", "NO_ONE").forEach { opt ->
                                        LuminaPillChip(
                                            label = opt,
                                            selected = settings.whoCanTag == opt,
                                            onClick = { onSaveSettings(settings.copy(whoCanTag = opt)) }
                                        )
                                    }
                                }

                                Text("Who Can @Mention You", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("EVERYONE", "FOLLOWERS", "NO_ONE").forEach { opt ->
                                        LuminaPillChip(
                                            label = opt,
                                            selected = settings.whoCanMention == opt,
                                            onClick = { onSaveSettings(settings.copy(whoCanMention = opt)) }
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("Manually Approve Tags", "Review tagged posts before they appear on your profile", settings.manuallyApproveTags) {
                                onSaveSettings(settings.copy(manuallyApproveTags = it))
                            }
                        }
                    }
                }

                SettingsPage.LOCATION -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsCategoryGroupCard(categoryTitle = "Location Preferences") {
                            SettingsSwitchTile("Enable In-App Location Tagging", "Allow adding location tags to posts", settings.allowPostLocation) {
                                onSaveSettings(settings.copy(allowPostLocation = it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("Display Location Tags on Feed", "Show location labels on posts in your feed", settings.showLocationContent) {
                                onSaveSettings(settings.copy(showLocationContent = it))
                            }
                        }
                    }
                }

                SettingsPage.CONTENT_ACCESSIBILITY -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsCategoryGroupCard(categoryTitle = "Theme & Playback") {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Theme Mode (Black, White & Blue)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    listOf("DARK", "LIGHT", "SYSTEM").forEach { mode ->
                                        LuminaPillChip(
                                            label = mode,
                                            selected = settings.themeMode == mode,
                                            onClick = { onSaveSettings(settings.copy(themeMode = mode)) }
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("Data-Saving Mode", "Reduce high-resolution media preloading on cellular", settings.dataSaverMode) {
                                onSaveSettings(settings.copy(dataSaverMode = it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("Autoplay Reels & Videos", "Automatically start video playback in Reels", settings.autoplayVideos) {
                                onSaveSettings(settings.copy(autoplayVideos = it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("High-Quality Video Uploads", "Prefer maximum resolution when publishing reels", settings.highQualityVideo) {
                                onSaveSettings(settings.copy(highQualityVideo = it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("Reduce Motion", "Minimize screen transition animations", settings.reduceMotion) {
                                onSaveSettings(settings.copy(reduceMotion = it))
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                            SettingsSwitchTile("Auto-Captions", "Display captions on videos when available", settings.autoCaptions) {
                                onSaveSettings(settings.copy(autoCaptions = it))
                            }
                        }
                    }
                }

                SettingsPage.SECURITY_SUPPORT -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsCategoryGroupCard(categoryTitle = "Security & Support") {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Active Session", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                Text("Account UID: ${currentUserProfile.userId}", style = MaterialTheme.typography.bodySmall)
                                Text("Database: Cloud Firestore (Zero-Trust Rules Active)", style = MaterialTheme.typography.bodySmall)
                                Text("Version: Mitr 2.0 (API 36)", style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        LuminaGradientButton(
                            text = "Report a Problem or Security Issue",
                            onClick = { showReportProblemDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Permanently Delete Account?") },
            text = {
                Text(
                    "This will permanently delete your profile (@${currentUserProfile.username}), release your username claim, delete your private settings in Firestore, and sign you out."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAccountPermanent()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showReportProblemDialog) {
        var issueText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportProblemDialog = false },
            title = { Text("Report a Problem") },
            text = {
                OutlinedTextField(
                    value = issueText,
                    onValueChange = { issueText = it.take(400) },
                    label = { Text("Describe the bug or safety concern") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    onReportProblem(issueText.ifBlank { "General support report" })
                    showReportProblemDialog = false
                }) {
                    Text("Submit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportProblemDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showBlueTickSubscriptionSheet) {
        MitrBlueTickSubscriptionSheet(
            currentUserProfile = currentUserProfile,
            currentUserSettings = settings,
            currentUserEmail = currentUserEmail,
            onActivateSubscription = { paymentMethod ->
                onActivateBlueTickSubscription(paymentMethod)
            },
            onSavePayoutAccount = { holder, upiOrBank, accNum, ifsc ->
                onSaveCreatorPayoutAccount(holder, upiOrBank, accNum, ifsc)
            },
            onDismiss = { showBlueTickSubscriptionSheet = false }
        )
    }

    if (showInteractiveLogoStudioDialog) {
        val isFounderAdmin = isFounderVipAccount(
            email = currentUserEmail.ifBlank { settings.email },
            username = currentUserProfile.username
        )
        MitrInteractiveLogoStudioDialog(
            canDownloadLogo = isFounderAdmin,
            onDismiss = { showInteractiveLogoStudioDialog = false }
        )
    }
}

@Composable
private fun SettingsCategoryGroupCard(
    categoryTitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = categoryTitle.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 6.dp, bottom = 6.dp)
        )
        LuminaGlassCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth(), content = content)
        }
    }
}

@Composable
private fun SettingsGroupRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun SettingsPermissionRow(
    icon: ImageVector,
    title: String,
    granted: Boolean,
    actionLabel: String = if (granted) "Granted" else "Grant Access",
    onRequest: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (granted) LuminaEmerald.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (granted) LuminaEmerald else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(17.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    text = if (granted) "Permission granted on device" else "Tap to authorize before use",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (granted) LuminaEmerald.copy(alpha = 0.16f) else MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable(onClick = onRequest)
        ) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium,
                color = if (granted) LuminaEmerald else Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun SettingsSwitchTile(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
