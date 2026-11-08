package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            PrayerAlarmScheduler.scheduleAllPrayerAlarms(context)
            ZikrAlarmScheduler.scheduleAllZikrAlarms(context)
            JanazaAlarmScheduler.schedulePeriodicSync(context)
            com.example.data.network.JanazaOnlineSyncManager.triggerImmediateSync(context)
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
        }
    }
}
