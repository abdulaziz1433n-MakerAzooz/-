package com.example

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppSettings
import com.example.data.model.UserProfile
import com.example.data.notification.QuranNotificationManager
import com.example.data.repository.QuranRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext<Application>()
        shadowOf(context as Application).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS)
    }

    @Test
    fun `read string from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("القرآن الكريم PDF", appName)
    }

    @Test
    fun `test notification channels creation`() {
        QuranNotificationManager.createNotificationChannels(context)

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val dailyChannel = notificationManager.getNotificationChannel(QuranNotificationManager.CHANNEL_DAILY_REMINDER)
        val wellbeingChannel = notificationManager.getNotificationChannel(QuranNotificationManager.CHANNEL_DIGITAL_WELLBEING)

        assertNotNull("Daily reminder channel should exist", dailyChannel)
        assertNotNull("Digital wellbeing channel should exist", wellbeingChannel)
    }

    @Test
    fun `test digital wellbeing notifications dispatch`() {
        QuranNotificationManager.createNotificationChannels(context)

        val dailySent = QuranNotificationManager.sendDailyReminderNotification(context)
        val eyeBreakSent = QuranNotificationManager.sendEyeBreakNotification(context, 20)
        val goalSent = QuranNotificationManager.sendGoalAchievedNotification(context, 30)
        val bedtimeSent = QuranNotificationManager.sendBedtimeReminderNotification(context)
        val limitSent = QuranNotificationManager.sendReadingLimitNotification(context, 60)

        assertTrue(dailySent)
        assertTrue(eyeBreakSent)
        assertTrue(goalSent)
        assertTrue(bedtimeSent)
        assertTrue(limitSent)
    }

    @Test
    fun `test digital wellbeing repository settings persistence`() {
        val repository = QuranRepository(context)

        val updatedSettings = AppSettings(
            dailyGoalMinutes = 45,
            eyeBreakReminderEnabled = true,
            eyeBreakIntervalMinutes = 20,
            bedtimeReminderEnabled = true,
            bedtimeHour = 23,
            bedtimeMinute = 0,
            goalAchievedNotificationEnabled = true,
            readingLimitEnabled = true,
            readingLimitMinutes = 60,
            todaySessionsCount = 5
        )

        repository.updateSettings(updatedSettings)
        val loaded = repository.settings.value

        assertEquals(45, loaded.dailyGoalMinutes)
        assertTrue(loaded.eyeBreakReminderEnabled)
        assertEquals(20, loaded.eyeBreakIntervalMinutes)
        assertTrue(loaded.bedtimeReminderEnabled)
        assertEquals(23, loaded.bedtimeHour)
        assertEquals(0, loaded.bedtimeMinute)
        assertTrue(loaded.goalAchievedNotificationEnabled)
        assertTrue(loaded.readingLimitEnabled)
        assertEquals(60, loaded.readingLimitMinutes)
        assertEquals(5, loaded.todaySessionsCount)
    }

    @Test
    fun `test mandatory authentication state persistence and clearance`() {
        val prefs = context.getSharedPreferences("email_auth_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        // Fresh install: no user logged in
        assertNull(prefs.getString("email", null))

        // Simulate account creation
        val user = UserProfile(
            userId = "email_test_example_com",
            displayName = "أحمد محمد",
            email = "test@example.com",
            khatmahGoal = "ختمة كل شهر (جزء يومياً)"
        )
        prefs.edit()
            .putString("email", user.email)
            .putString("name", user.displayName)
            .putString("goal", user.khatmahGoal)
            .commit()

        assertEquals("test@example.com", prefs.getString("email", null))
        assertEquals("أحمد محمد", prefs.getString("name", null))

        // Simulate sign out: clears session
        prefs.edit().clear().commit()
        assertNull(prefs.getString("email", null))
    }
}
