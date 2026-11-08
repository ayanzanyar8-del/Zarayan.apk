package com.example.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import android.util.Log

class ZikrAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val zikrType = intent?.getStringExtra("ZIKR_TYPE") ?: ZikrAlarmScheduler.TYPE_MORNING
        val zikrTitle = intent?.getStringExtra("ZIKR_TITLE") ?: "زیکر"
        val zikrTime = intent?.getStringExtra("ZIKR_TIME") ?: ""
        val requestCode = intent?.getIntExtra("REQUEST_CODE", 0) ?: 0

        Log.d("ZikrAlarmReceiver", "Zikr Alarm triggered: $zikrType ($zikrTitle - $zikrTime)")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "zarayen:ZikrAlarmWakeLock"
        )
        try {
            wakeLock?.acquire(6000L)
        } catch (_: Exception) {}

        try {
            // 1. Post notification for this specific Zikr
            NotificationHelper.showZikrNotification(
                context = context,
                zikrType = zikrType
            )

            // 2. Play gentle notification sound
            try {
                val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                val ringtone = RingtoneManager.getRingtone(context.applicationContext, soundUri)
                if (ringtone != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ringtone.audioAttributes = AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    }
                    ringtone.play()
                }
            } catch (e: Exception) {
                Log.e("ZikrAlarmReceiver", "Failed to play sound: ${e.message}")
            }
        } finally {
            // Reschedule future Zikr alarms if this was not a quick test
            if (requestCode != ZikrAlarmScheduler.REQ_ZIKR_TEST) {
                ZikrAlarmScheduler.scheduleAllZikrAlarms(context)
                com.example.data.network.JanazaOnlineSyncManager.triggerImmediateSync(context)
            }

            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock.release()
                }
            } catch (_: Exception) {}
        }
    }
}
