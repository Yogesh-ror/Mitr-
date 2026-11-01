package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.MainActivity
import com.example.R

private const val SOCIAL_NOTIFICATION_CHANNEL_ID = "mitr_social_interactions_channel"

class SocialNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val notificationId = inputData.getString("notification_id") ?: System.currentTimeMillis().toString()
        val title = inputData.getString("title") ?: "Mitr Activity"
        val message = inputData.getString("message") ?: "You have a new interaction on Mitr"
        val category = inputData.getString("category") ?: "ALL"

        ensureNotificationChannel(applicationContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                return Result.success()
            }
        }

        val launchIntent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_notifications", true)
            putExtra("notification_category", category)
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            notificationId.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, SOCIAL_NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        runCatching {
            NotificationManagerCompat.from(applicationContext)
                .notify(notificationId.hashCode(), notification)
        }

        return Result.success()
    }

    private fun ensureNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return
            val existing = manager.getNotificationChannel(SOCIAL_NOTIFICATION_CHANNEL_ID)
            if (existing == null) {
                val channel = NotificationChannel(
                    SOCIAL_NOTIFICATION_CHANNEL_ID,
                    "Mitr Social Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Real-time notifications for likes, comments, follows, and collaborations"
                }
                manager.createNotificationChannel(channel)
            }
        }
    }
}

/**
 * Triggers a local notification using the Android WorkManager API whenever a new document
 * is added to the user's specific notification collection in Firestore.
 */
fun triggerLocalNotificationWithWorkManager(
    context: Context,
    notificationId: String,
    title: String,
    message: String,
    category: String = "ALL"
) {
    val input = workDataOf(
        "notification_id" to notificationId,
        "title" to title,
        "message" to message,
        "category" to category
    )
    val workRequest = OneTimeWorkRequestBuilder<SocialNotificationWorker>()
        .setInputData(input)
        .addTag("mitr_firestore_notification")
        .build()

    WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
        "firestore_notif_$notificationId",
        ExistingWorkPolicy.KEEP,
        workRequest
    )
}
