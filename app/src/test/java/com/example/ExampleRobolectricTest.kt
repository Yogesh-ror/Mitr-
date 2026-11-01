package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MITR_BLUE_TICK_PRICE_INR
import com.example.data.model.MITR_FOUNDER_VIP_EMAIL
import com.example.data.model.MusicTrack
import com.example.data.model.UserProfile
import com.example.data.model.UserSettings
import com.example.data.model.isFounderVipAccount
import com.example.data.model.isUserBlueTickVerified
import com.example.ui.components.renderMitrTricolorLogoBitmap
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
}


