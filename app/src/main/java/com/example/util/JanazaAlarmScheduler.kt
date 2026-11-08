package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import com.example.data.network.JanazaOnlineSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Background Alarm Scheduler for Periodic Janaza Online Synchronization.
 * Ensures all installed apps regularly check for newly published funeral announcements
 * from the live cloud database, even when the app is in the background or closed.
 */
object JanazaAlarmScheduler {

    private const val TAG = "JanazaAlarmScheduler"
    const val ACTION_JANAZA_SYNC = "com.example.zarayen.ACTION_JANAZA_SYNC"
    const val REQUEST_CODE_JANAZA_SYNC = 9920
    private const val SYNC_INTERVAL_MILLIS = 5 * 60 * 1000L // Every 5 minutes in background

    /**
     * Schedules periodic background sync alarm.
     */
    fun schedulePeriodicSync(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, JanazaSyncReceiver::class.java).apply {
            action = ACTION_JANAZA_SYNC
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_JANAZA_SYNC,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerAtMillis = System.currentTimeMillis() + SYNC_INTERVAL_MILLIS

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Periodic Janaza online sync scheduled in 5 minutes")
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling Janaza periodic sync: ${e.message}")
        }
    }

    fun cancelPeriodicSync(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, JanazaSyncReceiver::class.java).apply {
            action = ACTION_JANAZA_SYNC
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_JANAZA_SYNC,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}

/**
 * BroadcastReceiver triggered by JanazaAlarmScheduler.
 * Fetches latest posts from Convex and pops notifications if new posts exist.
 */
class JanazaSyncReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("JanazaSyncReceiver", "Background Janaza sync alarm received")

        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val wakeLock = powerManager?.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "zarayen:JanazaSyncWakeLock"
        )

        try {
            wakeLock?.acquire(10000L) // 10 seconds timeout
        } catch (_: Exception) {}

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                JanazaOnlineSyncManager.fetchAndSyncOnlinePosts(context, triggerNotification = true)
            } catch (e: Exception) {
                Log.w("JanazaSyncReceiver", "Sync error: ${e.message}")
            } finally {
                // Reschedule for next 15-minute interval
                JanazaAlarmScheduler.schedulePeriodicSync(context)
                try {
                    if (wakeLock?.isHeld == true) {
                        wakeLock.release()
                    }
                } catch (_: Exception) {}
                pendingResult.finish()
            }
        }
    }
}
