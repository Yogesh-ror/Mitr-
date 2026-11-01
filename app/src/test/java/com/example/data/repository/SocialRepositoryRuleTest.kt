package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.UserProfile
import com.example.data.model.UserSettings
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class SocialRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun createOrUpdateProfile_and_usernameUniqueness_enforced() = runBlocking {
        val uniqueSuffix = UUID.randomUUID().toString().take(6)
        val aliceUid = signInTestUser("alice_$uniqueSuffix@lumina.test")
        val aliceRepo = SocialRepository(firestore, auth)

        val desiredUsername = "alice_$uniqueSuffix"
        val availBefore = withTimeout(5000L) { aliceRepo.isUsernameAvailable(desiredUsername).getOrThrow() }
        assertTrue(availBefore)

        val profile = withTimeout(5000L) {
            aliceRepo.createOrUpdateProfile(
                username = desiredUsername,
                displayName = "Alice Rivera",
                bio = "Visual storyteller",
                website = "https://lumina.app",
                avatarUrl = "",
                isPrivate = false
            ).getOrThrow()
        }
        assertEquals(aliceUid, profile.userId)
        assertEquals(desiredUsername, profile.username)

        // Sign in as Bob and verify Bob cannot claim Alice's username
        signInTestUser("bob_$uniqueSuffix@lumina.test")
        val bobRepo = SocialRepository(firestore, auth)
        val availForBob = withTimeout(5000L) { bobRepo.isUsernameAvailable(desiredUsername).getOrThrow() }
        assertFalse(availForBob)

        val duplicateClaimResult = withTimeout(5000L) {
            bobRepo.createOrUpdateProfile(
                username = desiredUsername,
                displayName = "Bob Impersonator",
                bio = "",
                website = "",
                avatarUrl = "",
                isPrivate = false
            )
        }
        assertTrue(duplicateClaimResult.isFailure)
    }

    @Test
    fun createPost_likePost_and_comment_updatesRealtimeFeed() = runBlocking {
        val suffix = UUID.randomUUID().toString().take(6)
        val aliceUid = signInTestUser("creator_$suffix@lumina.test")
        val repo = SocialRepository(firestore, auth)

        val authorProfile = UserProfile(
            id = aliceUid,
            userId = aliceUid,
            username = "creator_$suffix",
            displayName = "Creator $suffix"
        )

        val postId = withTimeout(5000L) {
            repo.createPost(
                authorProfile = authorProfile,
                caption = "Golden hour in Kyoto #travel #lumina",
                mediaUrls = listOf("https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e"),
                mediaType = "IMAGE",
                locationTag = "Kyoto, Japan",
                taggedUsernames = emptyList(),
                visibility = "PUBLIC",
                commentsAllowed = true
            ).getOrThrow()
        }
        assertTrue(postId.isNotEmpty())

        val posts = withTimeout(4000L) {
            repo.observePublicFeedPosts().first { list -> list.any { it.id == postId } }
        }
        val createdPost = posts.first { it.id == postId }
        assertEquals(0L, createdPost.likeCount)

        // Like post
        withTimeout(5000L) { repo.toggleLikePost(createdPost).getOrThrow() }

        // Add comment
        val commentId = withTimeout(5000L) {
            repo.addComment(
                targetId = postId,
                targetType = "POST",
                authorProfile = authorProfile,
                text = "Incredible colors!"
            ).getOrThrow()
        }
        assertTrue(commentId.isNotEmpty())
    }

    @Test
    fun userSettings_and_savedPosts_crossUserAccess_deniedByRules() = runBlocking {
        val suffix = UUID.randomUUID().toString().take(6)
        val aliceUid = signInTestUser("alice_priv_$suffix@lumina.test")
        val aliceRepo = SocialRepository(firestore, auth)

        withTimeout(5000L) {
            aliceRepo.saveUserSettings(
                UserSettings(
                    id = aliceUid,
                    userId = aliceUid,
                    email = "alice_priv_$suffix@lumina.test",
                    isPrivateAccount = true
                )
            ).getOrThrow()
        }

        // Bob signs in and attempts to read Alice's private user_settings document directly
        signInTestUser("bob_priv_$suffix@lumina.test")
        var caughtException: Exception? = null
        try {
            withTimeout(5000L) {
                firestore.collection("user_settings").document(aliceUid).get().await()
            }
        } catch (e: Exception) {
            caughtException = e
        }
        assertNotNull(caughtException)
        val fsEx = caughtException?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)
    }

    @Test
    fun unauthenticatedCaller_rejectedWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = SocialRepository(firestore, auth)

        var caughtException: Exception? = null
        try {
            withTimeout(4000L) {
                firestore.collection("posts").whereEqualTo("visibility", "PUBLIC").get().await()
            }
        } catch (e: Exception) {
            caughtException = e
        }
        assertNotNull(caughtException)
        val fsEx = caughtException?.unwrapFirestoreException()
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx?.code)

        // Also verify fail-fast requireUserId() when signed out
        var illegalState: IllegalStateException? = null
        try {
            withTimeout(3000L) {
                repo.observePublicFeedPosts().first()
            }
        } catch (e: IllegalStateException) {
            illegalState = e
        }
        assertNotNull(illegalState)
    }

    @Test
    fun directMessaging_enforcesConversationParticipantIsolation() = runBlocking {
        val suffix = UUID.randomUUID().toString().take(6)
        val aliceUid = signInTestUser("dm_alice_$suffix@lumina.test")
        val bobUid = signInTestUser("dm_bob_$suffix@lumina.test")

        // Switch back to Alice to start conversation with Bob
        signInTestUser("dm_alice_$suffix@lumina.test")
        val aliceRepo = SocialRepository(firestore, auth)
        val aliceProfile = UserProfile(userId = aliceUid, username = "alice_$suffix", displayName = "Alice")
        val bobProfile = UserProfile(userId = bobUid, username = "bob_$suffix", displayName = "Bob")

        val convId = withTimeout(5000L) {
            aliceRepo.startOrGetConversation(aliceProfile, bobProfile, isMutualOrFollowed = true).getOrThrow()
        }

        val convs = withTimeout(4000L) {
            aliceRepo.observeConversations().first { list -> list.any { it.id == convId } }
        }
        val conv = convs.first { it.id == convId }

        val msgId = withTimeout(5000L) {
            aliceRepo.sendDirectMessage(conv, "alice_$suffix", "Hey Bob!").getOrThrow()
        }
        assertTrue(msgId.isNotEmpty())

        // Mallory signs in and attempts to read the conversation between Alice and Bob
        signInTestUser("dm_mallory_$suffix@lumina.test")
        var caughtException: Exception? = null
        try {
            withTimeout(5000L) {
                firestore.collection("conversations").document(convId).get().await()
            }
        } catch (e: Exception) {
            caughtException = e
        }
        assertNotNull(caughtException)
        assertEquals(
            FirebaseFirestoreException.Code.PERMISSION_DENIED,
            caughtException?.unwrapFirestoreException()?.code
        )
    }
}
