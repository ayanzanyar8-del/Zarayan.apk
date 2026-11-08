package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class ZarayenApp : Application() {

    companion object {
        const val CHANNEL_AZAN_ID = "zarayen_azan_channel"
        const val CHANNEL_JANAZA_ID = "zarayen_janaza_channel"
        const val PREFS_NAME = "zarayen_app_prefs"
        const val KEY_USER_NAME = "user_name"
        const val KEY_IS_REGISTERED = "is_registered"
        const val KEY_DARK_MODE = "dark_mode_preference"
        const val KEY_AZAN_EARLY_ALERT = "azan_early_alert"
        const val KEY_ALWAYS_ON_NOTIFICATION = "always_on_notification"
        const val KEY_ZIKR_NOTIFICATIONS = "zikr_notifications_enabled"
        const val KEY_QURAN_FONT_SIZE = "quran_font_size"
        const val KEY_QURAN_THEME = "quran_theme_index"
    }

    override fun onCreate() {
        super.onCreate()
        initFirebase(this)
        com.example.util.NotificationHelper.createNotificationChannels(this)
        com.example.util.PrayerAlarmScheduler.scheduleAllPrayerAlarms(this)
        com.example.util.ZikrAlarmScheduler.scheduleAllZikrAlarms(this)
        com.example.util.JanazaAlarmScheduler.schedulePeriodicSync(this)
        com.example.data.network.JanazaOnlineSyncManager.startForegroundPolling(this)
        com.example.util.ZarayenFirebaseMessagingService.initializeFcm(this)

        // Ensure Always-on notification is shown on app startup
        try {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val alwaysOn = prefs.getBoolean(KEY_ALWAYS_ON_NOTIFICATION, true)
            if (alwaysOn) {
                val todayPrayers = com.example.data.util.PrayerTimesCalculator.getTodayPrayerTimes()
                com.example.util.NotificationHelper.showOngoingPrayerNotification(
                    context = this,
                    nextPrayerName = todayPrayers.nextPrayerNameKurdish,
                    nextPrayerTime = todayPrayers.nextPrayerTimeFormatted,
                    timeRemainingFormatted = todayPrayers.timeRemainingFormatted
                )
            }
        } catch (_: Exception) {}
    }

    private fun initFirebase(context: Context) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                val options = try {
                    FirebaseOptions.fromResource(context)
                } catch (e: Exception) {
                    null
                }

                if (options != null) {
                    FirebaseApp.initializeApp(context, options)
                } else {
                    val fallbackOptions = FirebaseOptions.Builder()
                        .setApplicationId(context.packageName)
                        .setProjectId("zarayen-app-sync")
                        .setApiKey("AIzaSyDummyDevKeyForZarayenAppRuntime00")
                        .build()
                    FirebaseApp.initializeApp(context, fallbackOptions)
                }
                Log.d("ZarayenApp", "Firebase initialized successfully")
            }
        } catch (e: Exception) {
            Log.e("ZarayenApp", "Firebase initialization error: ${e.message}")
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val azanChannel = NotificationChannel(
                CHANNEL_AZAN_ID,
                "ئاگادارکردنەوەی کاتی بانگ",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "ئاگادارکردنەوە ٢٠ چرکە پێش کاتی بانگەکان لە شارۆچکەی زەڕایەن"
                enableVibration(true)
            }

            val janazaChannel = NotificationChannel(
                CHANNEL_JANAZA_ID,
                "ئاگادارکردنەوەی نوێژی مردوو",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "ئاگادارکردنەوە بۆ کۆچی دوایی و نوێژی مردوو لە شارۆچکەی زەڕایەن"
                enableVibration(true)
            }

            notificationManager.createNotificationChannel(azanChannel)
            notificationManager.createNotificationChannel(janazaChannel)
        }
    }
}
