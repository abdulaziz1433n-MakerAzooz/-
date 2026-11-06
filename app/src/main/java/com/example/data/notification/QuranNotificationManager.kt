package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object QuranNotificationManager {

    const val CHANNEL_DAILY_REMINDER = "quran_daily_reminder_channel"
    const val CHANNEL_DIGITAL_WELLBEING = "digital_wellbeing_channel"

    const val NOTIFICATION_ID_DAILY = 1001
    const val NOTIFICATION_ID_EYE_BREAK = 1002
    const val NOTIFICATION_ID_GOAL_ACHIEVED = 1003
    const val NOTIFICATION_ID_BEDTIME = 1004
    const val NOTIFICATION_ID_READING_LIMIT = 1005

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Daily Quran reminder channel
            val dailyChannel = NotificationChannel(
                CHANNEL_DAILY_REMINDER,
                "تذكير ورد القرآن الكريم",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات يومية لتذكيرك بقراءة وردك اليومي من كتاب الله الكريم"
                enableVibration(true)
            }

            // Digital Wellbeing & Screen Time channel
            val wellbeingChannel = NotificationChannel(
                CHANNEL_DIGITAL_WELLBEING,
                "الرفاهية الرقمية وصحة العينين",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "تنبيهات استراحة العين، اكتمال هدف التلاوة، ووقت النوم والسكينة"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(dailyChannel)
            notificationManager.createNotificationChannel(wellbeingChannel)
        }
    }

    // Backward-compatible channel creation
    fun createNotificationChannel(context: Context) {
        createNotificationChannels(context)
    }

    fun canSendNotification(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // 1. Daily Quran Reminder Notification
    fun sendDailyReminderNotification(
        context: Context,
        title: String = "حان موعد وردك اليومي 📖",
        message: String = "«اقْرَؤُوا القُرْآنَ فإنَّه يَأْتي يَومَ القِيامَةِ شَفِيعًا لأَصْحابِهِ» افتح مصحفك الآن وواصل التلاوة."
    ): Boolean {
        createNotificationChannels(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_DAILY_REMINDER)
            .setSmallIcon(R.drawable.ic_notification_quran)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent(context))

        return tryNotify(context, NOTIFICATION_ID_DAILY, builder)
    }

    // 2. Eye Break Notification (20-20-20 Rule)
    fun sendEyeBreakNotification(
        context: Context,
        readingMinutes: Int = 20
    ): Boolean {
        createNotificationChannels(context)
        val title = "استراحة لصحة العينين 👀 (قاعدة 20-20-20)"
        val message = "مضت $readingMinutes دقيقة من التلاوة المتواصلة. أرح عينيك بالنظر لمسافة بعيدة (6 أمتار) لمدة 20 ثانية للحفاظ على صحة نظرك وتجديد طاقتك."

        val builder = NotificationCompat.Builder(context, CHANNEL_DIGITAL_WELLBEING)
            .setSmallIcon(R.drawable.ic_notification_quran)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent(context))

        return tryNotify(context, NOTIFICATION_ID_EYE_BREAK, builder)
    }

    // 3. Daily Reading Goal Achieved Notification
    fun sendGoalAchievedNotification(
        context: Context,
        goalMinutes: Int = 30
    ): Boolean {
        createNotificationChannels(context)
        val title = "مبارك! تم إنجاز هدف التلاوة اليومي 🎉"
        val message = "أتممت بحمد الله $goalMinutes دقيقة من تلاوة وتدبر القرآن الكريم اليوم. نسأل الله أن يجعله شفيعاً لك ونوراً في قلبك."

        val builder = NotificationCompat.Builder(context, CHANNEL_DIGITAL_WELLBEING)
            .setSmallIcon(R.drawable.ic_notification_quran)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent(context))

        return tryNotify(context, NOTIFICATION_ID_GOAL_ACHIEVED, builder)
    }

    // 4. Bedtime / Wind-down Notification
    fun sendBedtimeReminderNotification(
        context: Context
    ): Boolean {
        createNotificationChannels(context)
        val title = "حان وقت النوم والسكينة 🌙"
        val message = "اختم يومك المبارك بتلاوة سورة الملك وأذكار النوم المأثورة لتنعم بنوم هانئ وسكينة مطمئنة في رعاية الله."

        val builder = NotificationCompat.Builder(context, CHANNEL_DIGITAL_WELLBEING)
            .setSmallIcon(R.drawable.ic_notification_quran)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent(context))

        return tryNotify(context, NOTIFICATION_ID_BEDTIME, builder)
    }

    // 5. Reading Limit Notification
    fun sendReadingLimitNotification(
        context: Context,
        limitMinutes: Int = 60
    ): Boolean {
        createNotificationChannels(context)
        val title = "تنبيه الرفاهية الرقمية والوقت ⏳"
        val message = "بلغت الحد اليومي المحدد للقراءة ($limitMinutes دقيقة). خذ قسطاً كافياً من الراحة لسلامة عينيك وبدنك."

        val builder = NotificationCompat.Builder(context, CHANNEL_DIGITAL_WELLBEING)
            .setSmallIcon(R.drawable.ic_notification_quran)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(getPendingIntent(context))

        return tryNotify(context, NOTIFICATION_ID_READING_LIMIT, builder)
    }

    // Backward-compatible test method
    fun sendTestNotification(
        context: Context,
        title: String = "حان موعد وردك اليومي 📖",
        message: String = "«اقْرَؤُوا القُرْآنَ فإنَّه يَأْتي يَومَ القِيامَةِ شَفِيعًا لأَصْحابِهِ» افتح مصحفك الآن وواصل التلاوة."
    ): Boolean {
        return sendDailyReminderNotification(context, title, message)
    }

    private fun tryNotify(
        context: Context,
        notificationId: Int,
        builder: NotificationCompat.Builder
    ): Boolean {
        return try {
            val manager = NotificationManagerCompat.from(context)
            if (canSendNotification(context)) {
                manager.notify(notificationId, builder.build())
                true
            } else {
                false
            }
        } catch (e: SecurityException) {
            false
        }
    }

    // Open System Digital Wellbeing Settings
    fun openSystemDigitalWellbeing(context: Context): Boolean {
        val candidates = listOf(
            // Direct Digital Wellbeing App Usage
            Intent("com.google.android.apps.wellbeing.action.APP_USAGE").apply {
                putExtra("android.intent.extra.PACKAGE_NAME", context.packageName)
            },
            // Digital Wellbeing main activity
            Intent("com.google.android.apps.wellbeing.action.WELLBEING"),
            // Android System App Usage Settings
            Intent(Settings.ACTION_APP_USAGE_SETTINGS),
            // Usage access settings
            Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS),
            // Android Main Settings
            Intent(Settings.ACTION_SETTINGS)
        )

        for (intent in candidates) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            if (intent.resolveActivity(context.packageManager) != null) {
                try {
                    context.startActivity(intent)
                    return true
                } catch (e: Exception) {
                    // Try next candidate
                }
            }
        }

        // Direct fallback
        return try {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
            true
        } catch (e: Exception) {
            false
        }
    }
}
