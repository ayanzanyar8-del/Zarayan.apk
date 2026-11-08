package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {

    private const val TAG = "NotificationHelper"

    // Fresh channel IDs so Android applies updated high-priority and sound configurations
    const val CHANNEL_PRAYER_ID = "zarayen_prayer_alarms_channel_v4"
    const val CHANNEL_JANAZA_ID = "zarayen_janaza_alerts_channel_v4"
    const val CHANNEL_ONGOING_ID = "zarayen_ongoing_prayer_channel_v1"
    const val CHANNEL_ZIKR_ID = "zarayen_zikr_channel_v1"

    private const val NOTIFICATION_PRAYER_BASE_ID = 2001
    private const val NOTIFICATION_JANAZA_BASE_ID = 3001
    const val NOTIFICATION_ONGOING_ID = 1001
    const val NOTIFICATION_ZIKR_MORNING_ID = 4001
    const val NOTIFICATION_ZIKR_EVENING_ID = 4002
    const val NOTIFICATION_ZIKR_SLEEP_ID = 4003

    fun isNotificationPermissionGranted(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build()

            // Prayer times exact alert channel (HIGH priority, heads-up, sound & vibration)
            val prayerChannel = NotificationChannel(
                CHANNEL_PRAYER_ID,
                "بانگەکانی شارۆچکەی زەڕایەن",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "ئاگاداری و زەنگی ڕاستەوخۆ لە ساتی بانگەکان لە شارۆچکەی زەڕایەن"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
                setSound(alarmSound, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
                setBypassDnd(true)
            }

            // Janaza Announcements channel
            val janazaChannel = NotificationChannel(
                CHANNEL_JANAZA_ID,
                "ئاگاداری نوێژی مردوو (زەڕایەن)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "ئاگاداری کۆچی دوایی و کاتی نوێژی مردوو لە مزگەوتەکانی زەڕایەن"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 300, 150, 300)
                setSound(alarmSound, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // Ongoing Prayer Times Notification Channel (Low priority, silent, persistent in status bar)
            val ongoingChannel = NotificationChannel(
                CHANNEL_ONGOING_ID,
                "ئاگاداریی هەمیشەیی نوێژەکان (زەڕایەن)",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "ئاگاداریی هەمیشەیی و نەبڕاوە لە شریتی سەرەوە بۆ کاتەکانی بانگ"
                setShowBadge(false)
                enableLights(false)
                enableVibration(false)
                setSound(null, null)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            // Daily Azkar Channel (High priority with sound & vibration)
            val zikrChannel = NotificationChannel(
                CHANNEL_ZIKR_ID,
                "زیکر و ویردەکان (شارۆچکەی زەڕایەن)",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "ئاگاداری بۆ زیکری بەیانیان، زیکری ئێواران، و زیکری خەوتن لە کاتی خۆیاندا"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400)
                setSound(alarmSound, audioAttributes)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(prayerChannel)
            notificationManager.createNotificationChannel(janazaChannel)
            notificationManager.createNotificationChannel(ongoingChannel)
            notificationManager.createNotificationChannel(zikrChannel)
            Log.d(TAG, "Notification channels verified and created.")
        }
    }

    fun showPrayerAlertNotification(
        context: Context,
        prayerNameKurdish: String,
        prayerTimeFormatted: String
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SCREEN", "prayer")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_PRAYER_BASE_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notification = NotificationCompat.Builder(context, CHANNEL_PRAYER_ID)
            .setSmallIcon(R.drawable.ic_notification_azan)
            .setContentTitle("🕌 کاتی بانگی پیرۆزی $prayerNameKurdish (زەڕایەن)")
            .setContentText("کاتی بانگی $prayerNameKurdish گەیشت • $prayerTimeFormatted")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("کاتی بانگی پیرۆزی $prayerNameKurdish لە شارۆچکەی زەڕایەن و دەشتی شارەزوور گەیشت ($prayerTimeFormatted).\n﴿ إِنَّ الصَّلَاةَ كَانَتْ عَلَى الْمُؤْمِنِينَ كِتَابًا مَوْقُوتًا ﴾")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 700))
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(NOTIFICATION_PRAYER_BASE_ID, notification)
                Log.d(TAG, "Prayer notification successfully shown for: $prayerNameKurdish ($prayerTimeFormatted)")
            } else {
                Log.w(TAG, "Notifications are disabled in system settings for app package")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error posting prayer notification: ${e.message}", e)
        }
    }

    fun showJanazaNotification(
        context: Context,
        deceasedName: String,
        mosqueName: String,
        prayerTime: String,
        customTitle: String? = null,
        customBody: String? = null
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SCREEN", "janaza")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_JANAZA_BASE_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val title = customTitle ?: "📢 ئاگاداری نوێژی مردوو لە زەڕایەن"
        val content = customBody ?: "﴿ إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ ﴾ $deceasedName"
        val bigText = buildString {
            append("﴿ إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ ﴾\n")
            append("پۆستێکی نوێی نوێژی مردوو لە زەڕایەن بڵاوکرایەوە.\n")
            if (deceasedName.isNotBlank()) {
                append("کۆچی دوایی: $deceasedName\n")
            }
            if (mosqueName.isNotBlank()) {
                append("مزگەوت: $mosqueName\n")
            }
            if (prayerTime.isNotBlank()) {
                append("کاتی نوێژ: $prayerTime\n")
            }
            append("داواکارین لە خوای گەورە جێگای بەهەشتی بەرین بێت.")
        }

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notification = NotificationCompat.Builder(context, CHANNEL_JANAZA_ID)
            .setSmallIcon(R.drawable.ic_notification_janaza)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setAutoCancel(true)
            .setSound(alarmSound)
            .setVibrate(longArrayOf(0, 500, 250, 500, 250, 700))
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notifId = (System.currentTimeMillis() % 10000).toInt() + NOTIFICATION_JANAZA_BASE_ID
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(notifId, notification)
                Log.d(TAG, "Janaza notification successfully shown: $title")
            } else {
                Log.w(TAG, "Notifications are disabled in system settings for app package")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error posting janaza notification: ${e.message}", e)
        }

        // Play loud audible alert like Bang notification
        try {
            val ringtone = RingtoneManager.getRingtone(context.applicationContext, alarmSound)
            if (ringtone != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                }
                ringtone.play()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play Janaza ringtone: ${e.message}")
        }
    }

    /**
     * Shows a persistent, sticky "Always-on" notification in the status bar & notification shade.
     * Displays current/upcoming prayer time and live countdown for Zarayen town.
     */
    fun showOngoingPrayerNotification(
        context: Context,
        nextPrayerName: String,
        nextPrayerTime: String,
        timeRemainingFormatted: String
    ) {
        createNotificationChannels(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SCREEN", "prayer")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ONGOING_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = "🕌 کاتی بانگی داهاتوو: $nextPrayerName ($nextPrayerTime)"
        val text = "ماوە بۆ بانگ: $timeRemainingFormatted • شارۆچکەی زەڕایەن"

        val notification = NotificationCompat.Builder(context, CHANNEL_ONGOING_ID)
            .setSmallIcon(R.drawable.ic_notification_azan)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "🕌 کاتی بانگی داهاتوو: $nextPrayerName لە کاتژمێر $nextPrayerTime\n" +
                    "⏳ ماوە بۆ بانگ: $timeRemainingFormatted\n" +
                    "📍 کاتەکان بەپێی شارۆچکەی زەڕایەن و دەشتی شارەزوور"
                )
            )
            .setOngoing(true) // Always on - persists in notification tray
            .setOnlyAlertOnce(true) // Updates smoothly without repeatedly alerting the user
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(NOTIFICATION_ONGOING_ID, notification)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying ongoing prayer notification: ${e.message}")
        }
    }

    fun cancelOngoingPrayerNotification(context: Context) {
        try {
            val notificationManager = NotificationManagerCompat.from(context)
            notificationManager.cancel(NOTIFICATION_ONGOING_ID)
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling ongoing prayer notification: ${e.message}")
        }
    }

    /**
     * Shows a scheduled notification for Azkar (Morning, Evening, or Bedtime).
     * Tapping the notification takes the user directly to that specific Zikr category in the app.
     */
    fun showZikrNotification(
        context: Context,
        zikrType: String
    ) {
        createNotificationChannels(context)

        val title: String
        val content: String
        val bigText: String
        val notifId: Int
        val targetCategory: String

        when (zikrType) {
            ZikrAlarmScheduler.TYPE_MORNING -> {
                title = "🌅 کاتی زیکری بەیانیان (زەڕایەن)"
                content = "کاتی زیکرەکانی بەیانیان گەیشت • ٥٠ دەقە دوای نوێژی بەیانی"
                bigText = "🌅 کاتی پیرۆزی زیکری بەیانیان لە شارۆچکەی زەڕایەن گەیشت (٥٠ دەقە دوای نوێژی بەیانی).\n" +
                        "پێغەمبەری خوا ﷺ دەفەرموێت: هەركەس لە بەیانیاندا زیکر بکات، خودا دەیپارێزێت تا ئێوارە.\n" +
                        "دەست لێرە بدە بۆ خوێندنەوەی ڕاستەوخۆی زیکرەکانی بەیانیان."
                notifId = NOTIFICATION_ZIKR_MORNING_ID
                targetCategory = "morning"
            }
            ZikrAlarmScheduler.TYPE_EVENING -> {
                title = "🌇 کاتی زیکری ئێواران (زەڕایەن)"
                content = "کاتی زیکرەکانی ئێواران گەیشت • ٣٠ دەقە دوای بانگی عەسر"
                bigText = "🌇 کاتی پیرۆزی زیکری ئێواران لە شارۆچکەی زەڕایەن گەیشت (٣٠ دەقە دوای بانگی عەسر).\n" +
                        "﴿ أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ ﴾\n" +
                        "ئێوارەکەت بە یادی خوای گەورە و زیکرەکانی ئێواران ئاوەدان بکەرەوە.\n" +
                        "دەست لێرە بدە بۆ خوێندنەوەی ڕاستەوخۆی زیکرەکانی ئێواران."
                notifId = NOTIFICATION_ZIKR_EVENING_ID
                targetCategory = "evening"
            }
            else -> {
                title = "🌙 کاتی زیکری خەوتن (زەڕایەن)"
                content = "کاتژمێر ٩ی شەوە • کاتی خوێندنی زیکری خەوتن"
                bigText = "🌙 کاتژمێر ٩:٠٠ی شەو - کاتی پیرۆزی زیکری خەوتن و سوننەتەکان گەیشت.\n" +
                        "پێش خەوتن زیکرەکان و ئایەتی کورسی بخوێنە بۆ ئەوەی بە درێژایی شەو پارێزراو بیت بە پاراستنی خوا.\n" +
                        "دەست لێرە بدە بۆ خوێندنەوەی ڕاستەوخۆی زیکرەکانی خەوتن."
                notifId = NOTIFICATION_ZIKR_SLEEP_ID
                targetCategory = "sleep"
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_SCREEN", "azkar")
            putExtra("ZIKR_TYPE", targetCategory)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notifId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        val notification = NotificationCompat.Builder(context, CHANNEL_ZIKR_ID)
            .setSmallIcon(R.drawable.ic_notification_azan)
            .setContentTitle(title)
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400))
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (notificationManager.areNotificationsEnabled()) {
                notificationManager.notify(notifId, notification)
                Log.d(TAG, "Zikr notification posted successfully for $zikrType (id=$notifId)")
            } else {
                Log.w(TAG, "Notifications are disabled in system settings for app package")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error posting zikr notification: ${e.message}", e)
        }
    }
}
