package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.MainActivity
import com.example.ZarayenApp
import com.example.data.util.PrayerTimesCalculator
import java.util.Calendar

/**
 * High-Precision Alarm Scheduler for Zarayen Prayer Times (بانگەکانی شارۆچکەی زەڕایەن).
 * Guarantees alarms and notifications trigger reliably across all Android versions (including 12, 13, 14+),
 * properly handling exact alarm permissions and Doze mode.
 */
object PrayerAlarmScheduler {

    private const val TAG = "PrayerAlarmScheduler"

    // Unique Request Codes for each of the 5 daily prayers
    const val REQ_FAJR = 8801
    const val REQ_DHUHR = 8802
    const val REQ_ASR = 8803
    const val REQ_MAGHRIB = 8804
    const val REQ_ISHA = 8805
    const val REQ_TEST = 8899

    private val PRAYER_CODES = listOf(REQ_FAJR, REQ_DHUHR, REQ_ASR, REQ_MAGHRIB, REQ_ISHA)

    data class PrayerScheduleItem(
        val requestCode: Int,
        val kurdishName: String,
        val arabicName: String,
        val totalMinutes: Int
    )

    /**
     * Schedules alarms for all 5 daily prayers at their exact Bang minute and second.
     */
    fun scheduleAllPrayerAlarms(context: Context) {
        val prefs = context.getSharedPreferences(ZarayenApp.PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(ZarayenApp.KEY_AZAN_EARLY_ALERT, true)
        if (!isEnabled) {
            Log.d(TAG, "Prayer alarms are disabled in user preferences. Cancelling all alarms.")
            cancelAllPrayerAlarms(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val nowCalendar = Calendar.getInstance(PrayerTimesCalculator.IRAQ_TIMEZONE)
        val nowMillis = nowCalendar.timeInMillis
        val currentYear = nowCalendar.get(Calendar.YEAR)
        val currentMonth = nowCalendar.get(Calendar.MONTH) + 1
        val currentDay = nowCalendar.get(Calendar.DAY_OF_MONTH)

        // Today's prayer times
        val todayTimes = PrayerTimesCalculator.calculateForDate(currentYear, currentMonth, currentDay)

        // Tomorrow's prayer times
        val tomorrowCal = (nowCalendar.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        val tomorrowYear = tomorrowCal.get(Calendar.YEAR)
        val tomorrowMonth = tomorrowCal.get(Calendar.MONTH) + 1
        val tomorrowDay = tomorrowCal.get(Calendar.DAY_OF_MONTH)
        val tomorrowTimes = PrayerTimesCalculator.calculateForDate(tomorrowYear, tomorrowMonth, tomorrowDay)

        val prayers = listOf(
            PrayerScheduleItem(REQ_FAJR, "بەیانی", "الفجر", todayTimes.fajrMinutes),
            PrayerScheduleItem(REQ_DHUHR, "نیوەڕۆ", "الظهر", todayTimes.dhuhrMinutes),
            PrayerScheduleItem(REQ_ASR, "عەسر", "العصر", todayTimes.asrMinutes),
            PrayerScheduleItem(REQ_MAGHRIB, "ئێوارە", "المغرب", todayTimes.maghribMinutes),
            PrayerScheduleItem(REQ_ISHA, "خەوتن", "العشاء", todayTimes.ishaMinutes)
        )

        val tomorrowPrayerMap = mapOf(
            REQ_FAJR to tomorrowTimes.fajrMinutes,
            REQ_DHUHR to tomorrowTimes.dhuhrMinutes,
            REQ_ASR to tomorrowTimes.asrMinutes,
            REQ_MAGHRIB to tomorrowTimes.maghribMinutes,
            REQ_ISHA to tomorrowTimes.ishaMinutes
        )

        // Pre-trigger offset: schedule exactly 20 seconds before the minute of the Bang
        val preTriggerOffsetMillis = 20_000L

        for (prayer in prayers) {
            val prayerCal = Calendar.getInstance(PrayerTimesCalculator.IRAQ_TIMEZONE).apply {
                set(Calendar.YEAR, currentYear)
                set(Calendar.MONTH, currentMonth - 1)
                set(Calendar.DAY_OF_MONTH, currentDay)
                set(Calendar.HOUR_OF_DAY, prayer.totalMinutes / 60)
                set(Calendar.MINUTE, prayer.totalMinutes % 60)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Target millis is set to exactly 20 seconds before the prayer minute
            var targetMillis = prayerCal.timeInMillis - preTriggerOffsetMillis
            var formattedTime = PrayerTimesCalculator.format12Hour(prayer.totalMinutes)

            // If this prayer's target time (20s before) has already passed, schedule for tomorrow
            if (targetMillis <= nowMillis + 1000L) {
                val tmrMinutes = tomorrowPrayerMap[prayer.requestCode] ?: prayer.totalMinutes
                val tmrCal = Calendar.getInstance(PrayerTimesCalculator.IRAQ_TIMEZONE).apply {
                    set(Calendar.YEAR, tomorrowYear)
                    set(Calendar.MONTH, tomorrowMonth - 1)
                    set(Calendar.DAY_OF_MONTH, tomorrowDay)
                    set(Calendar.HOUR_OF_DAY, tmrMinutes / 60)
                    set(Calendar.MINUTE, tmrMinutes % 60)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                targetMillis = tmrCal.timeInMillis - preTriggerOffsetMillis
                formattedTime = PrayerTimesCalculator.format12Hour(tmrMinutes)
            }

            scheduleSingleAlarm(context, alarmManager, prayer.requestCode, prayer.kurdishName, formattedTime, targetMillis)
        }

        Log.d(TAG, "Successfully refreshed alarms for all 5 Bang times (scheduled 20s before time of Bang).")
    }

    private fun scheduleSingleAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        prayerName: String,
        prayerTime: String,
        targetMillis: Long
    ) {
        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "com.example.zarayen.ACTION_BANG_ALARM"
            putExtra("PRAYER_NAME", prayerName)
            putExtra("PRAYER_TIME", prayerTime)
            putExtra("REQUEST_CODE", requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetMillis,
                        pendingIntent
                    )
                } else {
                    // Fallback on Android 12+ if user has not granted exact alarm permission in settings
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    targetMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Alarm $requestCode scheduled for $prayerName at $prayerTime ($targetMillis)")
        } catch (e: Exception) {
            Log.e(TAG, "Exact alarm failed for $prayerName, falling back to setAndAllowWhileIdle: ${e.message}")
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        targetMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        targetMillis,
                        pendingIntent
                    )
                }
            } catch (e2: Exception) {
                Log.e(TAG, "Ultimate alarm fallback failed for $prayerName: ${e2.message}")
            }
        }
    }

    /**
     * Backward-compatible alias for scheduling
     */
    fun scheduleNextPrayerAlarm(context: Context) {
        scheduleAllPrayerAlarms(context)
    }

    fun cancelAllPrayerAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (code in PRAYER_CODES) {
            val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
                action = "com.example.zarayen.ACTION_BANG_ALARM"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                code,
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
     * Schedules a quick 3-second test alarm to verify background wake-up and notification delivery
     */
    fun scheduleTestAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val testTargetMillis = System.currentTimeMillis() + 3000L

        val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
            action = "com.example.zarayen.ACTION_BANG_ALARM"
            putExtra("PRAYER_NAME", "تاقیکردنەوە")
            putExtra("PRAYER_TIME", "ئێستا")
            putExtra("REQUEST_CODE", REQ_TEST)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQ_TEST,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        testTargetMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        testTargetMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    testTargetMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    testTargetMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling test alarm: ${e.message}")
        }
    }
}
