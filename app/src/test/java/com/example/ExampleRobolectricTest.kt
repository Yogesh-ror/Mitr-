package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MITR_BLUE_TICK_PRICE_INR
import com.example.data.model.MITR_FOUNDER_VIP_EMAIL
import com.example.data.model.MusicTrack
import com.example.data.model.UserProfile
import com.example.data.model.UserSettings
import com.example.data.model.isFounderVipAccount
import com.example.data.model.isReadByPeer
import com.example.data.model.isStoryExpired
import com.example.data.model.isUserBlueTickVerified
import com.example.data.model.parsedReactions
import com.example.data.model.reactionCountsByEmoji
import com.example.ui.components.renderMitrTricolorLogoBitmap
import com.google.firebase.Timestamp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Mitr", appName)
    }

    @Test
    fun `music track trimmed duration calculates exact seconds for post story and reel`() {
        val track = MusicTrack(
            trackId = "itunes_101",
            title = "Neon Skyline",
            artist = "Mitr Studio",
            durationMs = 30_000L,
            trimStartMs = 4_000L,
            trimEndMs = 19_000L
        )
        assertEquals(15L, track.trimmedDurationSeconds)
    }

    @Test
    fun `founder email yogeshkhanchi985 receives free blue tick without payment`() {
        assertEquals("yogeshkhanchi985@gmail.com", MITR_FOUNDER_VIP_EMAIL)
        assertTrue(isFounderVipAccount(email = "yogeshkhanchi985@gmail.com"))
        assertTrue(isFounderVipAccount(email = "YOGESHKHANCHI985@GMAIL.COM"))

        val unverifiedProfile = UserProfile(
            userId = "founder_uid",
            username = "yogesh_creator",
            displayName = "Yogesh Khanchi",
            isVerified = false,
            subscriptionPlan = "FREE"
        )
        assertTrue(
            isUserBlueTickVerified(
                profile = unverifiedProfile,
                email = "yogeshkhanchi985@gmail.com",
                settings = UserSettings(email = "yogeshkhanchi985@gmail.com")
            )
        )
    }

    @Test
    fun `paid 300 rupees subscription activates blue tick and free user remains unverified`() {
        assertEquals(300L, MITR_BLUE_TICK_PRICE_INR)

        val freeUser = UserProfile(
            userId = "user_free",
            username = "regular_creator",
            displayName = "Regular Creator",
            isVerified = false,
            subscriptionPlan = "FREE"
        )
        assertFalse(isUserBlueTickVerified(profile = freeUser, email = "someone@example.com", settings = UserSettings()))

        val paidSubscriber = freeUser.copy(
            isVerified = true,
            subscriptionPlan = "BLUE_TICK_300"
        )
        assertTrue(isUserBlueTickVerified(profile = paidSubscriber, email = "someone@example.com", settings = UserSettings()))
    }

    @Test
    fun `renderMitrTricolorLogoBitmap creates high resolution Indian Flag logo copy`() {
        val bitmap = renderMitrTricolorLogoBitmap(sizePx = 256)
        assertNotNull(bitmap)
        assertEquals(256, bitmap.width)
        assertEquals(256, bitmap.height)
    }

    @Test
    fun `uploaded reels are deduplicated by id and by author videoUrl caption`() {
        val rawReels = listOf(
            com.example.data.model.Reel(
                id = "reel_1",
                authorId = "uid_123",
                authorUsername = "aarav",
                videoUrl = "file:///cache/reel_video.png",
                caption = "Sunset vibes #Mitr"
            ),
            com.example.data.model.Reel(
                id = "reel_2",
                authorId = "uid_123",
                authorUsername = "aarav",
                videoUrl = "file:///cache/reel_video.png",
                caption = "Sunset vibes #Mitr"
            ),
            com.example.data.model.Reel(
                id = "reel_1",
                authorId = "uid_123",
                authorUsername = "aarav",
                videoUrl = "file:///cache/reel_video.png",
                caption = "Sunset vibes #Mitr"
            )
        )
        val deduplicated = rawReels
            .distinctBy { it.id }
            .distinctBy { "${it.authorId}|${it.videoUrl.trim()}|${it.caption.trim()}" }
        assertEquals(1, deduplicated.size)
        assertEquals("reel_1", deduplicated.first().id)
    }

    @Test
    fun `story emoji reaction entries parse userId username and emoji accurately`() {
        val story = com.example.data.model.Story(
            id = "story_100",
            authorId = "author_uid",
            authorUsername = "creator_one",
            reactionEntries = listOf(
                "viewer_1:priya_sharma:🔥",
                "viewer_2:rohan_verma:❤️"
            )
        )
        val parsed = story.parsedReactions()
        assertEquals(2, parsed.size)
        assertEquals("viewer_1", parsed[0].userId)
        assertEquals("priya_sharma", parsed[0].username)
        assertEquals("🔥", parsed[0].emoji)
        assertEquals("❤️", parsed[1].emoji)
    }

    @Test
    fun `direct message read receipt indicator reflects SENT and READ status in real time`() {
        val sentMsg = com.example.data.model.DirectMessage(
            id = "msg_1",
            conversationId = "uidA_uidB",
            participantIds = listOf("uidA", "uidB"),
            senderId = "uidA",
            deliveryStatus = "SENT",
            readByUids = listOf("uidA")
        )
        assertFalse(sentMsg.isReadByPeer("uidA"))

        val readMsgByStatus = sentMsg.copy(deliveryStatus = "READ")
        assertTrue(readMsgByStatus.isReadByPeer("uidA"))

        val readMsgByUidList = sentMsg.copy(
            deliveryStatus = "SENT",
            readByUids = listOf("uidA", "uidB"),
            readAtMillis = 1730000000000L
        )
        assertTrue(readMsgByUidList.isReadByPeer("uidA"))
    }

    @Test
    fun `saved reel persists reelId and converts cleanly to SavedPost for later viewing`() {
        val savedReel = com.example.data.model.SavedReel(
            id = "reel_999",
            userId = "user_1",
            reelId = "reel_999",
            authorUsername = "kiran_creator",
            captionPreview = "Monsoon dance reel",
            videoUrl = "https://example.com/reel_999.mp4"
        )
        val converted = savedReel.toSavedPost()
        assertEquals("reel_999", converted.postId)
        assertEquals("reel_999", converted.resolvedReelId)
        assertEquals("REEL", converted.targetType)
        assertEquals("https://example.com/reel_999.mp4", converted.mediaUrl)
    }

    @Test
    fun `story archive groups past and active stories chronologically by month and year`() {
        val nowMillis = 1_760_000_000_000L
        val pastStory = com.example.data.model.Story(
            id = "story_past_1",
            authorId = "uid_me",
            authorUsername = "me",
            caption = "Past Festival",
            expiresAtMillis = nowMillis - 5L * 24L * 60L * 60L * 1000L
        )
        val activeStory = com.example.data.model.Story(
            id = "story_active_2",
            authorId = "uid_me",
            authorUsername = "me",
            caption = "Today Live",
            expiresAtMillis = nowMillis + 12L * 60L * 60L * 1000L
        )
        assertTrue(pastStory.isStoryExpired(nowMillis))
        assertFalse(activeStory.isStoryExpired(nowMillis))

        val groups = com.example.data.model.groupStoriesForArchive(listOf(pastStory, activeStory))
        assertTrue(groups.isNotEmpty())
        val allGroupedStories = groups.flatMap { it.stories }
        assertEquals(2, allGroupedStories.size)
        assertEquals("story_active_2", allGroupedStories.first().id)
        assertEquals("story_past_1", allGroupedStories.last().id)
    }

    @Test
    fun `clampReelVideoTrimWindow enforces 15 to 60 second short-form video duration bounds`() {
        // Too-short selection (5s) is automatically expanded to at least 15s (15_000L ms)
        val (shortStart, shortEnd) = com.example.data.model.clampReelVideoTrimWindow(
            rawStartMs = 2_000L,
            rawEndMs = 7_000L,
            sourceDurationMs = 60_000L
        )
        assertEquals(2_000L, shortStart)
        assertEquals(17_000L, shortEnd)
        assertEquals(15_000L, shortEnd - shortStart)

        // Too-long selection (75s on a 90s source) is automatically clamped to 60s max (60_000L ms)
        val (longStart, longEnd) = com.example.data.model.clampReelVideoTrimWindow(
            rawStartMs = 5_000L,
            rawEndMs = 80_000L,
            sourceDurationMs = 90_000L
        )
        assertEquals(5_000L, longStart)
        assertEquals(65_000L, longEnd)
        assertEquals(60_000L, longEnd - longStart)

        // Valid 30s selection within bounds is preserved
        val (validStart, validEnd) = com.example.data.model.clampReelVideoTrimWindow(
            rawStartMs = 10_000L,
            rawEndMs = 40_000L,
            sourceDurationMs = 60_000L
        )
        assertEquals(10_000L, validStart)
        assertEquals(40_000L, validEnd)
    }

    @Test
    fun `direct messaging conversation thread and individual message models preserve real-time state`() {
        val conv = com.example.data.model.Conversation(
            id = "uid_a_uid_b",
            participantIds = listOf("uid_a", "uid_b"),
            participantUsernames = listOf("arjun", "priya"),
            initiatorId = "uid_a",
            lastMessageText = "Check out this 30s Reel!",
            lastSenderId = "uid_a",
            status = "ACTIVE",
            unreadCount = 1L
        )
        assertEquals("uid_a_uid_b", conv.id)
        assertTrue(conv.participantIds.containsAll(listOf("uid_a", "uid_b")))

        val msg = com.example.data.model.DirectMessage(
            id = "msg_101",
            conversationId = conv.id,
            participantIds = conv.participantIds,
            senderId = "uid_a",
            senderUsername = "arjun",
            text = "Check out this 30s Reel!",
            mediaUrl = "content://media/external/images/media/42",
            mediaType = "IMAGE",
            deliveryStatus = "READ",
            readByUids = listOf("uid_a", "uid_b")
        )
        assertTrue(msg.isReadByPeer("uid_a"))
        assertEquals("IMAGE", msg.mediaType)
    }

    @Test
    fun `story feeling emoji reaction model links to specific storyId and computes per-emoji counts`() {
        val storyReaction = com.example.data.model.StoryReaction(
            id = "story_777_uid_viewer",
            storyId = "story_777",
            storyAuthorId = "uid_author",
            userId = "uid_viewer",
            username = "ananya",
            displayName = "Ananya Verma",
            emoji = "🔥",
            feelingLabel = com.example.data.model.storyFeelingLabelForEmoji("🔥"),
            replyText = "Amazing sunset!"
        )
        assertEquals("story_777", storyReaction.storyId)
        assertEquals("Lit", storyReaction.feelingLabel)
        assertTrue(com.example.data.model.STORY_FEELING_REACTIONS.any { it.first == "🔥" && it.second == "Lit" })

        val story = com.example.data.model.Story(
            id = "story_777",
            authorId = "uid_author",
            authorUsername = "rohan",
            reactionEntries = listOf(
                "uid_1:ananya:🔥",
                "uid_2:kabir:🔥",
                "uid_3:meera:❤️"
            )
        )
        val counts = story.reactionCountsByEmoji()
        assertEquals(2, counts["🔥"])
        assertEquals(1, counts["❤️"])
    }

    @Test
    fun `fcm direct message push payload and notification helper build real-time DM alert`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val payload = com.example.notifications.buildDirectMessagePushPayload(
            conversationId = "uid_1_uid_2",
            messageId = "msg_501",
            senderUsername = "arjun_k",
            messageText = "Hey! Did you see my story reaction?"
        )
        assertEquals("uid_1_uid_2", payload.conversationId)
        assertEquals("arjun_k", payload.senderUsername)
        assertTrue(payload.title.contains("@arjun_k"))
        assertEquals("Hey! Did you see my story reaction?", payload.body)

        // Verify notification channel creation and notification dispatch do not throw
        com.example.notifications.MitrPushNotificationHelper.ensureNotificationChannels(context)
        com.example.notifications.MitrPushNotificationHelper.showDirectMessageNotification(
            context = context,
            conversationId = "uid_1_uid_2",
            messageId = "msg_501",
            senderUsername = "arjun_k",
            messageText = "Hey! Did you see my story reaction?"
        )
    }

    @Test
    fun `mobile room database stores chat history and activity logs on device storage to save cloud db space`() = kotlinx.coroutines.runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = androidx.room.Room.inMemoryDatabaseBuilder(
            context,
            com.example.data.local.MitrLocalDatabase::class.java
        ).allowMainThreadQueries().build()
        val dao = db.mitrLocalDao()

        val localMsg = com.example.data.local.LocalChatMessageEntity(
            id = "msg_local_1",
            conversationId = "conv_100",
            participantIdsCsv = "uid_me,uid_peer",
            senderId = "uid_peer",
            senderUsername = "priya",
            text = "Stored directly on phone storage!",
            mediaUrl = "",
            mediaType = "TEXT",
            replyToMessageId = "",
            deliveryStatus = "READ",
            readByUidsCsv = "uid_me,uid_peer",
            readAtMillis = 1730000000L,
            createdAtMillis = 1730000000L,
            isOffloadedFromCloud = true
        )
        dao.upsertLocalMessages(listOf(localMsg))

        val localLog = com.example.data.local.LocalActivityLogEntity(
            id = "log_local_1",
            userId = "uid_me",
            activityType = "STORY_REACTION",
            targetId = "story_777",
            summary = "Reacted 🔥 (Lit) to @rohan's story",
            createdAtMillis = 1730000100L
        )
        dao.insertLocalActivityLog(localLog)

        assertEquals(1, dao.countAllLocalMessages())
        assertEquals(1, dao.countOffloadedMessages())
        assertEquals(1, dao.countLocalActivityLogs("uid_me"))
        val domainMsg = localMsg.toDirectMessage()
        assertEquals("Stored directly on phone storage!", domainMsg.text)
        assertTrue(domainMsg.readByUids.contains("uid_me"))
        db.close()
    }

    @Test
    fun `profile screen helper filters and sorts recent public posts for user bio and avatar display`() {
        val profile = com.example.data.model.UserProfile(
            id = "uid_creator_1",
            userId = "uid_creator_1",
            username = "arjun_photo",
            displayName = "Arjun Kapoor",
            bio = "Visual storyteller & travel photographer",
            avatarUrl = "https://example.com/arjun.jpg"
        )
        assertEquals("Visual storyteller & travel photographer", profile.bio)
        assertEquals("https://example.com/arjun.jpg", profile.avatarUrl)

        val posts = listOf(
            com.example.data.model.Post(
                id = "post_older",
                authorId = "uid_creator_1",
                authorUsername = "arjun_photo",
                caption = "Older public post",
                visibility = "PUBLIC",
                createdAt = Timestamp(1700000100L, 0)
            ),
            com.example.data.model.Post(
                id = "post_private",
                authorId = "uid_creator_1",
                authorUsername = "arjun_photo",
                caption = "Followers-only post",
                visibility = "FOLLOWERS",
                createdAt = Timestamp(1700000500L, 0)
            ),
            com.example.data.model.Post(
                id = "post_newer",
                authorId = "uid_creator_1",
                authorUsername = "arjun_photo",
                caption = "Newest public post from Firestore",
                visibility = "PUBLIC",
                createdAt = Timestamp(1700000900L, 0)
            ),
            com.example.data.model.Post(
                id = "post_other_user",
                authorId = "uid_someone_else",
                authorUsername = "other_user",
                caption = "Unrelated post",
                visibility = "PUBLIC",
                createdAt = Timestamp(1700001000L, 0)
            )
        )

        val recentPublic = com.example.data.model.filterRecentPublicPostsForProfile(
            posts = posts,
            userId = profile.userId,
            username = profile.username,
            isOwner = false
        )
        assertEquals(2, recentPublic.size)
        assertEquals("post_newer", recentPublic[0].id)
        assertEquals("post_older", recentPublic[1].id)
    }

    @Test
    fun `user search helper finds and ranks users by username or display name`() {
        val users = listOf(
            com.example.data.model.UserProfile(
                id = "u1",
                userId = "u1",
                username = "rohan_mehta",
                displayName = "Rohan Mehta",
                bio = "Filmmaker"
            ),
            com.example.data.model.UserProfile(
                id = "u2",
                userId = "u2",
                username = "ananya_sharma",
                displayName = "Ananya Sharma",
                bio = "Designer & illustrator"
            ),
            com.example.data.model.UserProfile(
                id = "u3",
                userId = "u3",
                username = "rohan",
                displayName = "Rohan Official",
                bio = "Music producer",
                isVerified = true
            )
        )

        val byUsername = com.example.data.model.filterAndRankUsersByQuery(users, "@rohan")
        assertEquals(2, byUsername.size)
        assertEquals("u3", byUsername.first().userId) // Exact username match ranked first

        val byDisplayName = com.example.data.model.filterAndRankUsersByQuery(users, "Ananya Sharma")
        assertEquals(1, byDisplayName.size)
        assertEquals("ananya_sharma", byDisplayName.first().username)
    }

    @Test
    fun `MUSIC_API_KEY integration and Reel background music overlay synchronize track with recorded video`() {
        assertFalse(com.example.data.model.isValidMusicApiKey(null))
        assertFalse(com.example.data.model.isValidMusicApiKey(""))
        assertFalse(com.example.data.model.isValidMusicApiKey("YOUR_MUSIC_API_KEY"))
        assertTrue(com.example.data.model.isValidMusicApiKey("live_music_key_998877"))

        val musicRepo = com.example.data.repository.MusicRepository()
        assertNotNull(BuildConfig.MUSIC_API_KEY)
        val overlayLibraryTracks = musicRepo.getReelOverlayTracks(query = "Kesariya", reelDurationSeconds = 30L)
        assertTrue(overlayLibraryTracks.isNotEmpty())
        val selectedTrack = overlayLibraryTracks.first()
        assertEquals(30L, selectedTrack.trimmedDurationSeconds)

        val recordedReel = com.example.data.model.Reel(
            id = "reel_recorded_1",
            authorId = "uid_creator_1",
            authorUsername = "arjun_photo",
            authorDisplayName = "Arjun Kapoor",
            videoUrl = "file:///cache/mitr_reel_30s.mp4",
            caption = "Dancing under the stars #Reels",
            durationSeconds = 30L,
            videoTrimStartMs = 0L,
            videoTrimEndMs = 30_000L
        )
        val reelWithMusicOverlay = com.example.data.model.applyMusicOverlayToReel(recordedReel, selectedTrack)
        assertEquals(selectedTrack.trackId, reelWithMusicOverlay.musicTrackId)
        assertEquals(selectedTrack.title, reelWithMusicOverlay.musicTitle)
        assertEquals(selectedTrack.artist, reelWithMusicOverlay.musicArtist)
        assertEquals(0L, reelWithMusicOverlay.musicStartMs)
        assertEquals(30_000L, reelWithMusicOverlay.musicEndMs)
        assertTrue(reelWithMusicOverlay.audioTrackName.contains(selectedTrack.title))
    }

    @Test
    fun `profile reels tab filter returns own and collaborator reels across uid docId and username`() {
        val allReels = listOf(
            com.example.data.model.Reel(
                id = "reel_own_1",
                authorId = "uid_yogesh",
                authorUsername = "yogeshkhanchi985",
                videoUrl = "content://media/external/video/media/101",
                caption = "First studio reel",
                durationSeconds = 30L,
                createdAt = Timestamp(1000L, 0)
            ),
            com.example.data.model.Reel(
                id = "reel_own_2_blank_caption",
                authorId = "uid_yogesh",
                authorUsername = "@yogeshkhanchi985",
                videoUrl = "content://media/external/video/media/102",
                caption = "",
                durationSeconds = 15L,
                createdAt = Timestamp(2000L, 0)
            ),
            com.example.data.model.Reel(
                id = "reel_collab_3",
                authorId = "uid_partner",
                authorUsername = "arjun_music",
                collaboratorUsernames = listOf("@yogeshkhanchi985"),
                videoUrl = "https://cdn.mitr.app/reels/collab3.mp4",
                caption = "Collab jam",
                durationSeconds = 45L,
                createdAt = Timestamp(3000L, 0)
            ),
            com.example.data.model.Reel(
                id = "reel_other_user",
                authorId = "uid_stranger",
                authorUsername = "stranger_user",
                videoUrl = "https://cdn.mitr.app/reels/other.mp4",
                caption = "Other reel",
                durationSeconds = 20L,
                createdAt = Timestamp(4000L, 0)
            )
        )

        // Case 1: Profile where userId is empty on deserialization but profileDocId & currentUserId are set
        val matchedForOwner = com.example.data.model.filterUserReelsForProfile(
            reels = allReels,
            userId = "",
            profileDocId = "uid_yogesh",
            username = "yogeshkhanchi985",
            isOwner = true,
            currentUserId = "uid_yogesh"
        )
        assertEquals(3, matchedForOwner.size)
        assertEquals("reel_collab_3", matchedForOwner[0].id)
        assertEquals("reel_own_2_blank_caption", matchedForOwner[1].id)
        assertEquals("reel_own_1", matchedForOwner[2].id)
    }
}

