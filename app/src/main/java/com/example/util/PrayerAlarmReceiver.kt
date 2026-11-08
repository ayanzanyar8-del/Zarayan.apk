package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log

class PrayerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val prayerName = intent?.getStringExtra("PRAYER_NAME") ?: "بانگی فەرز"
        val prayerTime = intent?.getStringExtra("PRAYER_TIME") ?: ""
        val requestCode = intent?.getIntExtra("REQUEST_CODE", 0) ?: 0

        Log.d("PrayerAlarmReceiver", "Bang Alarm triggered for: $prayerName ($prayerTime), code: $requestCode")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "zarayen:PrayerBangAlarmWakeLock"
        )
        try {
            wakeLock?.acquire(6000L)
        } catch (_: Exception) {}

        try {
            // 1. Show the heads-up notification with sound, lights, and vibration
            NotificationHelper.showPrayerAlertNotification(
                context = context,
                prayerNameKurdish = prayerName,
                prayerTimeFormatted = prayerTime
            )

            // 2. Play audible ringtone alert
            try {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val ringtone = RingtoneManager.getRingtone(context.applicationContext, soundUri)
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
                Log.e("PrayerAlarmReceiver", "Failed to play ringtone: ${e.message}")
            }
        } finally {
            // Reschedule upcoming alarms for tomorrow if it wasn't a manual test
            if (requestCode != PrayerAlarmScheduler.REQ_TEST) {
                PrayerAlarmScheduler.scheduleAllPrayerAlarms(context)
                com.example.data.network.JanazaOnlineSyncManager.triggerImmediateSync(context)
            }

            // Keep the Always-on notification up to date with next upcoming prayer
            try {
                val prefs = context.getSharedPreferences(com.example.ZarayenApp.PREFS_NAME, Context.MODE_PRIVATE)
                val alwaysOn = prefs.getBoolean(com.example.ZarayenApp.KEY_ALWAYS_ON_NOTIFICATION, true)
                if (alwaysOn) {
                    val updatedPrayers = com.example.data.util.PrayerTimesCalculator.getTodayPrayerTimes()
                    NotificationHelper.showOngoingPrayerNotification(
                        context = context,
                        nextPrayerName = updatedPrayers.nextPrayerNameKurdish,
                        nextPrayerTime = updatedPrayers.nextPrayerTimeFormatted,
                        timeRemainingFormatted = updatedPrayers.timeRemainingFormatted
                    )
                }
            } catch (_: Exception) {}

            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock.release()
                }
            } catch (_: Exception) {}
        }
    }
}
