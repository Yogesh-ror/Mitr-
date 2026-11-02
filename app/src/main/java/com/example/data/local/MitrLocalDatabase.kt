package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.Conversation
import com.example.data.model.DirectMessage
import com.example.data.model.UserActivityLog
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Local Room entity storing Direct Messages on the user's phone storage
 * so chat histories remain available on-device even when older read messages
 * are compacted/offloaded from Firestore to save cloud database storage.
 */
@Entity(tableName = "local_chat_messages")
data class LocalChatMessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val participantIdsCsv: String,
    val senderId: String,
    val senderUsername: String,
    val text: String,
    val mediaUrl: String,
    val mediaType: String,
    val replyToMessageId: String,
    val deliveryStatus: String,
    val readByUidsCsv: String,
    val readAtMillis: Long,
    val createdAtMillis: Long,
    val isOffloadedFromCloud: Boolean = false
) {
    fun toDirectMessage(): DirectMessage = DirectMessage(
        id = id,
        conversationId = conversationId,
        participantIds = participantIdsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
        senderId = senderId,
        senderUsername = senderUsername,
        text = text,
        mediaUrl = mediaUrl,
        mediaType = mediaType,
        replyToMessageId = replyToMessageId,
        deliveryStatus = deliveryStatus,
        readByUids = readByUidsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
        readAtMillis = readAtMillis,
        createdAt = Timestamp(Date(createdAtMillis.coerceAtLeast(1L)))
    )
}

fun DirectMessage.toLocalEntity(isOffloadedFromCloud: Boolean = false): LocalChatMessageEntity {
    val createdMs = createdAt?.toDate()?.time?.takeIf { it > 0L } ?: System.currentTimeMillis()
    return LocalChatMessageEntity(
        id = id.ifBlank { "${conversationId}_${senderId}_$createdMs" },
        conversationId = conversationId,
        participantIdsCsv = participantIds.joinToString(","),
        senderId = senderId,
        senderUsername = senderUsername,
        text = text,
        mediaUrl = mediaUrl,
        mediaType = mediaType,
        replyToMessageId = replyToMessageId,
        deliveryStatus = deliveryStatus,
        readByUidsCsv = readByUids.joinToString(","),
        readAtMillis = readAtMillis,
        createdAtMillis = createdMs,
        isOffloadedFromCloud = isOffloadedFromCloud
    )
}

/**
 * Local Room entity storing Conversation metadata on the user's mobile storage.
 */
@Entity(tableName = "local_conversations")
data class LocalConversationEntity(
    @PrimaryKey val id: String,
    val ownerUserId: String,
    val participantIdsCsv: String,
    val participantUsernamesCsv: String,
    val initiatorId: String,
    val lastMessageText: String,
    val lastSenderId: String,
    val status: String,
    val mutedByUidsCsv: String,
    val unreadCount: Long,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
) {
    fun toConversation(): Conversation = Conversation(
        id = id,
        participantIds = participantIdsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
        participantUsernames = participantUsernamesCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
        initiatorId = initiatorId,
        lastMessageText = lastMessageText,
        lastSenderId = lastSenderId,
        status = status,
        mutedByUids = mutedByUidsCsv.split(",").map { it.trim() }.filter { it.isNotBlank() },
        unreadCount = unreadCount,
        createdAt = Timestamp(Date(createdAtMillis.coerceAtLeast(1L))),
        updatedAt = Timestamp(Date(updatedAtMillis.coerceAtLeast(1L)))
    )
}

fun Conversation.toLocalEntity(ownerUserId: String): LocalConversationEntity {
    val createdMs = createdAt?.toDate()?.time?.takeIf { it > 0L } ?: System.currentTimeMillis()
    val updatedMs = updatedAt?.toDate()?.time?.takeIf { it > 0L } ?: createdMs
    return LocalConversationEntity(
        id = id,
        ownerUserId = ownerUserId,
        participantIdsCsv = participantIds.joinToString(","),
        participantUsernamesCsv = participantUsernames.joinToString(","),
        initiatorId = initiatorId,
        lastMessageText = lastMessageText,
        lastSenderId = lastSenderId,
        status = status,
        mutedByUidsCsv = mutedByUids.joinToString(","),
        unreadCount = unreadCount,
        createdAtMillis = createdMs,
        updatedAtMillis = updatedMs
    )
}

/**
 * Local Room entity storing user activity logs, search history, and non-critical metadata
 * on the user's phone storage instead of filling up the cloud Firestore database.
 */
@Entity(tableName = "local_activity_logs")
data class LocalActivityLogEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val activityType: String,
    val targetId: String,
    val summary: String,
    val createdAtMillis: Long
) {
    fun toUserActivityLog(): UserActivityLog = UserActivityLog(
        id = id,
        userId = userId,
        activityType = activityType,
        targetId = targetId,
        summary = summary,
        createdAt = Timestamp(Date(createdAtMillis.coerceAtLeast(1L)))
    )
}

fun UserActivityLog.toLocalEntity(): LocalActivityLogEntity {
    val createdMs = createdAt?.toDate()?.time?.takeIf { it > 0L } ?: System.currentTimeMillis()
    return LocalActivityLogEntity(
        id = id.ifBlank { "act_${userId}_$createdMs" },
        userId = userId,
        activityType = activityType,
        targetId = targetId,
        summary = summary,
        createdAtMillis = createdMs
    )
}

data class MobileStorageStats(
    val localMessagesCount: Int = 0,
    val offloadedCloudMessagesCount: Int = 0,
    val localConversationsCount: Int = 0,
    val localActivityLogsCount: Int = 0,
    val estimatedSavedCloudBytes: Long = 0L
)

@Dao
interface MitrLocalDao {
    @Query("SELECT * FROM local_chat_messages WHERE conversationId = :conversationId ORDER BY createdAtMillis ASC")
    fun observeLocalMessages(conversationId: String): Flow<List<LocalChatMessageEntity>>

    @Query("SELECT * FROM local_chat_messages WHERE conversationId = :conversationId ORDER BY createdAtMillis ASC")
    suspend fun getLocalMessagesForConversation(conversationId: String): List<LocalChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalMessages(messages: List<LocalChatMessageEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalMessage(message: LocalChatMessageEntity)

    @Query("DELETE FROM local_chat_messages WHERE id = :messageId")
    suspend fun deleteLocalMessage(messageId: String)

    @Query("DELETE FROM local_chat_messages WHERE conversationId = :conversationId")
    suspend fun clearLocalMessagesForConversation(conversationId: String)

    @Query("SELECT COUNT(*) FROM local_chat_messages")
    suspend fun countAllLocalMessages(): Int

    @Query("SELECT COUNT(*) FROM local_chat_messages WHERE isOffloadedFromCloud = 1")
    suspend fun countOffloadedMessages(): Int

    @Query("SELECT * FROM local_conversations WHERE ownerUserId = :ownerUserId ORDER BY updatedAtMillis DESC")
    fun observeLocalConversations(ownerUserId: String): Flow<List<LocalConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLocalConversations(conversations: List<LocalConversationEntity>)

    @Query("SELECT COUNT(*) FROM local_conversations WHERE ownerUserId = :ownerUserId")
    suspend fun countLocalConversations(ownerUserId: String): Int

    @Query("SELECT * FROM local_activity_logs WHERE userId = :userId ORDER BY createdAtMillis DESC LIMIT 250")
    fun observeLocalActivityLogs(userId: String): Flow<List<LocalActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalActivityLog(log: LocalActivityLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalActivityLogs(logs: List<LocalActivityLogEntity>)

    @Query("DELETE FROM local_activity_logs WHERE id = :activityId")
    suspend fun deleteLocalActivityLog(activityId: String)

    @Query("DELETE FROM local_activity_logs WHERE userId = :userId")
    suspend fun clearLocalActivityLogsForUser(userId: String)

    @Query("SELECT COUNT(*) FROM local_activity_logs WHERE userId = :userId")
    suspend fun countLocalActivityLogs(userId: String): Int
}

@Database(
    entities = [
        LocalChatMessageEntity::class,
        LocalConversationEntity::class,
        LocalActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MitrLocalDatabase : RoomDatabase() {
    abstract fun mitrLocalDao(): MitrLocalDao

    companion object {
        @Volatile
        private var INSTANCE: MitrLocalDatabase? = null

        fun getInstance(context: Context): MitrLocalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MitrLocalDatabase::class.java,
                    "mitr_mobile_storage.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
