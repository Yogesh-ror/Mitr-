package com.example.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

const val MITR_DM_NOTIFICATION_CHANNEL_ID = "mitr_direct_messages_channel"
const val MITR_STORY_REACTION_CHANNEL_ID = "mitr_story_reactions_channel"

data class DirectMessagePushPayload(
    val notificationId: Int,
    val conversationId: String,
    val senderUsername: String,
    val title: String,
    val body: String
)

fun buildDirectMessagePushPayload(
    conversationId: String,
    messageId: String,
    senderUsername: String,
    messageText: String,
    mediaType: String = "NONE"
): DirectMessagePushPayload {
    val cleanHandle = senderUsername.trim().removePrefix("@").ifBlank { "mitr_friend" }
    val cleanBody = when {
        messageText.isNotBlank() -> messageText.trim()
        mediaType.equals("IMAGE", ignoreCase = true) -> "📷 Sent you a photo"
        mediaType.equals("VIDEO", ignoreCase = true) -> "🎬 Sent you a video"
        else -> "Sent you a new direct message"
    }
    val stableId = (conversationId + "_" + messageId).hashCode()
    return DirectMessagePushPayload(
        notificationId = stableId,
        conversationId = conversationId,
        senderUsername = cleanHandle,
        title = "New message from @$cleanHandle",
        body = cleanBody
    )
}

object MitrPushNotificationHelper {
    private val notifiedMessageIds = LinkedHashSet<String>()

    fun ensureNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
            val dmChannel = NotificationChannel(
                MITR_DM_NOTIFICATION_CHANNEL_ID,
                "Direct Messages (FCM Real-Time)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time push notifications when you receive a new direct message"
                enableVibration(true)
            }
            val storyChannel = NotificationChannel(
                MITR_STORY_REACTION_CHANNEL_ID,
                "Story Emoji Reactions",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications when someone reacts with an emoji to your story"
            }
            manager.createNotificationChannel(dmChannel)
            manager.createNotificationChannel(storyChannel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showDirectMessageNotification(
        context: Context,
        conversationId: String,
        messageId: String,
        senderUsername: String,
        messageText: String,
        mediaType: String = "NONE"
    ): Boolean {
        val key = "${conversationId}_${messageId}"
        synchronized(notifiedMessageIds) {
            if (messageId.isNotBlank() && notifiedMessageIds.contains(key)) {
                return false
            }
            if (messageId.isNotBlank()) {
                notifiedMessageIds.add(key)
                if (notifiedMessageIds.size > 300) {
                    val first = notifiedMessageIds.firstOrNull()
                    if (first != null) notifiedMessageIds.remove(first)
                }
            }
        }

        ensureNotificationChannels(context)
        if (!hasNotificationPermission(context)) {
            Log.i("MitrFCM", "POST_NOTIFICATIONS permission not granted yet; skipping system tray notification for $key")
            return false
        }

        val payload = buildDirectMessagePushPayload(
            conversationId = conversationId,
            messageId = messageId,
            senderUsername = senderUsername,
            messageText = messageText,
            mediaType = mediaType
        )

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_conversation_id", conversationId)
            putExtra("sender_username", payload.senderUsername)
        }
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        val pendingIntent = PendingIntent.getActivity(
            context,
            payload.notificationId,
            tapIntent,
            pendingIntentFlags
        )

        val notification = NotificationCompat.Builder(context, MITR_DM_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.sym_action_chat)
            .setContentTitle(payload.title)
            .setContentText(payload.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(payload.body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return runCatching {
            NotificationManagerCompat.from(context).notify(payload.notificationId, notification)
            true
        }.getOrDefault(false)
    }

    /**
     * Fetches the device's FCM registration token and syncs it to Firestore (/fcm_tokens/{uid})
     * so FCM Cloud Messaging & real-time Direct Message push notifications are linked to the signed-in user.
     */
    fun refreshAndSyncFcmToken(context: Context, onTokenSynced: (String) -> Unit = {}) {
        ensureNotificationChannels(context)
        runCatching {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                if (!token.isNullOrBlank()) {
                    persistFcmTokenToFirestore(context, token)
                    onTokenSynced(token)
                }
            }
        }
    }

    fun persistFcmTokenToFirestore(context: Context, token: String) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        if (token.isBlank()) return
        runCatching {
            val dbId = context.applicationContext.getString(R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(dbId)
            db.collection("fcm_tokens").document(uid).set(
                mapOf(
                    "userId" to uid,
                    "token" to token.take(512),
                    "platform" to "ANDROID",
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )
        }.onFailure { e ->
            Log.w("MitrFCM", "Could not persist FCM token: ${e.message}")
        }
    }
}

/**
 * Firebase Cloud Messaging service that receives real-time push notifications for new
 * Direct Messages and Story Reactions even when the user is on another screen.
 */
class MitrFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i("MitrFCM", "Refreshed FCM registration token")
        MitrPushNotificationHelper.persistFcmTokenToFirestore(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data = remoteMessage.data
        val convId = data["conversationId"] ?: data["conversation_id"] ?: "fcm_dm"
        val msgId = data["messageId"] ?: remoteMessage.messageId ?: System.currentTimeMillis().toString()
        val senderUsername = data["senderUsername"]
            ?: data["sender_username"]
            ?: remoteMessage.notification?.title?.removePrefix("New message from @")
            ?: "mitr_user"
        val text = data["text"]
            ?: data["body"]
            ?: remoteMessage.notification?.body
            ?: "Sent you a new direct message"
        val mediaType = data["mediaType"] ?: "NONE"

        MitrPushNotificationHelper.showDirectMessageNotification(
            context = applicationContext,
            conversationId = convId,
            messageId = msgId,
            senderUsername = senderUsername,
            messageText = text,
            mediaType = mediaType
        )
    }
}
