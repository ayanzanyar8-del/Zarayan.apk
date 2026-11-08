package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.ZarayenApp
import com.example.data.util.PrayerTimesCalculator
import java.util.Calendar

/**
 * Scheduled Alarms for Daily Azkar (ئاگاداریی زیکرەکانی ڕۆژانەی زەڕایەن):
 * 1. Morning Zikr (زیکری بەیانیان): 50 minutes after Fajr prayer (٥٠ دەقە دوای نوێژی بەیانی).
 * 2. Evening Zikr (زیکری ئێواران): 30 minutes after Asr prayer (٣٠ دەقە دوای بانگی عەسر).
 * 3. Sleep Zikr (زیکری خەوتن): Every night at 9:00 PM (21:00) (هەموو شەوێک کاتژمێر ٩ی شەو).
 */
object ZikrAlarmScheduler {

    private const val TAG = "ZikrAlarmScheduler"

    const val REQ_ZIKR_MORNING = 8901
    const val REQ_ZIKR_EVENING = 8902
    const val REQ_ZIKR_SLEEP = 8903
    const val REQ_ZIKR_TEST = 8999

    const val ACTION_ZIKR_ALARM = "com.example.zarayen.ACTION_ZIKR_ALARM"

    const val TYPE_MORNING = "morning"
    const val TYPE_EVENING = "evening"
    const val TYPE_SLEEP = "sleep"

    val ZIKR_CODES = listOf(REQ_ZIKR_MORNING, REQ_ZIKR_EVENING, REQ_ZIKR_SLEEP)

    data class ZikrScheduleItem(
        val requestCode: Int,
        val type: String,
        val titleKurdish: String,
        val subtitleKurdish: String,
        val formattedTime: String,
        val targetMillis: Long
    )

    /**
     * Calculates the exact scheduled times for all three Zikrs.
     */
    fun getZikrScheduleItems(context: Context): List<ZikrScheduleItem> {
        val nowCal = Calendar.getInstance(PrayerTimesCalculator.IRAQ_TIMEZONE)
        val nowMillis = nowCal.timeInMillis
        val currentYear = nowCal.get(Calendar.YEAR)
        val currentMonth = nowCal.get(Calendar.MONTH) + 1
        val currentDay = nowCal.get(Calendar.DAY_OF_MONTH)

        // Today's prayer times
        val todayTimes = PrayerTimesCalculator.calculateForDate(currentYear, currentMonth, currentDay)

        // Tomorrow's prayer times
        val tomorrowCal = (nowCal.clone() as Calendar).apply { add(Calendar.DAY_OF_MONTH, 1) }
        val tomorrowYear = tomorrowCal.get(Calendar.YEAR)
        val tomorrowMonth = tomorrowCal.get(Calendar.MONTH) + 1
        val tomorrowDay = tomorrowCal.get(Calendar.DAY_OF_MONTH)
        val tomorrowTimes = PrayerTimesCalculator.calculateForDate(tomorrowYear, tomorrowMonth, tomorrowDay)

        // 1. Morning Zikr: 50 minutes after Fajr (٥٠ دەقە دوای نوێژی بەیانی)
        val morningTodayMinutes = todayTimes.fajrMinutes + 50
        val morningTodayCal = Calendar.getInstance(PrayerTimesCalculator.IRAQ_TIMEZONE).apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth - 1)
            set(Calendar.DAY_OF_MONTH, currentDay)
            set(Calendar.HOUR_OF_DAY, morningTodayMinutes / 60)
            set(Calendar.MINUTE, morningTodayMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val morningTargetMillis: Long
        val morningMinutesToFormat: Int
        if (morningTodayCal.timeInMillis > nowMillis + 2000L) {
            morningTargetMillis = morningTodayCal.timeInMillis
            morningMinutesToFormat = morningTodayMinutes
        } else {
            val morningTmrMinutes = tomorrowTimes.fajrMinutes + 50
            morningMinutesToFormat = morningTmrMinutes
            val tmrCal = Calendar.getInstance(PrayerTimesCalculator.IRAQ_TIMEZONE).apply {
                set(Calendar.YEAR, tomorrowYear)
                set(Calendar.MONTH, tomorrowMonth - 1)
                set(Calendar.DAY_OF_MONTH, tomorrowDay)
                set(Calendar.HOUR_OF_DAY, morningTmrMinutes / 60)
                set(Calendar.MINUTE, morningTmrMinutes % 60)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            morningTargetMillis = tmrCal.timeInMillis
        }
        val morningFormatted = PrayerTimesCalculator.format12Hour(morningMinutesToFormat)

        // 2. Evening Zikr: 30 minutes after Asr (٣٠ دەقە دوای بانگی عەسر)
        val eveningTodayMinutes = todayTimes.asrMinutes + 30
        val eveningTodayCal = Calendar.getInstance(PrayerTimesCalculator.IRAQ_TIMEZONE).apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth - 1)
            set(Calendar.DAY_OF_MONTH, currentDay)
            set(Calendar.HOUR_OF_DAY, eveningTodayMinutes / 60)
            set(Calendar.MINUTE, eveningTodayMinutes % 60)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val eveningTargetMillis: Long
        val eveningMinutesToFormat: Int
        if (eveningTodayCal.timeInMillis > nowMillis + 2000L) {
            eveningTargetMillis = eveningTodayCal.timeInMillis
            eveningMinutesToFormat = eveningTodayMinutes
        } else {
            val eveningTmrMinutes = tomorrowTimes.asrMinutes + 30
            eveningMinutesToFormat = eveningTmrMinutes
            val tmrCal = Calendar.getInstance(PrayerTimesCalculator.IRAQ_TIMEZONE).apply {
                set(Calendar.YEAR, tomorrowYear)
                set(Calendar.MONTH, tomorrowMonth - 1)
                set(Calendar.DAY_OF_MONTH, tomorrowDay)
                set(Calendar.HOUR_OF_DAY, eveningTmrMinutes / 60)
                set(Calendar.MINUTE, eveningTmrMinutes % 60)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            eveningTargetMillis = tmrCal.timeInMillis
        }
        val eveningFormatted = PrayerTimesCalculator.format12Hour(eveningMinutesToFormat)

        // 3. Sleep Zikr: 9:00 PM (21:00) every night (هەموو شەوێک کاتژمێر ٩ی شەو)
        val sleepTodayCal = Calendar.getInstance(PrayerTimesCalculator.IRAQ_TIMEZONE).apply {
            set(Calendar.YEAR, currentYear)
            set(Calendar.MONTH, currentMonth - 1)
            set(Calendar.DAY_OF_MONTH, currentDay)
            set(Calendar.HOUR_OF_DAY, 21)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val sleepTargetMillis: Long = if (sleepTodayCal.timeInMillis > nowMillis + 2000L) {
            sleepTodayCal.timeInMillis
        } else {
            val tmrCal = Calendar.getInstance(PrayerTimesCalculator.IRAQ_TIMEZONE).apply {
                set(Calendar.YEAR, tomorrowYear)
                set(Calendar.MONTH, tomorrowMonth - 1)
                set(Calendar.DAY_OF_MONTH, tomorrowDay)
                set(Calendar.HOUR_OF_DAY, 21)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            tmrCal.timeInMillis
        }
        val sleepFormatted = "٠٩:٠٠ ی شەو (21:00)"

        return listOf(
            ZikrScheduleItem(
                requestCode = REQ_ZIKR_MORNING,
                type = TYPE_MORNING,
                titleKurdish = "زیکری بەیانیان",
                subtitleKurdish = "٥٠ دەقە دوای نوێژی بەیانی",
                formattedTime = morningFormatted,
                targetMillis = morningTargetMillis
            ),
            ZikrScheduleItem(
                requestCode = REQ_ZIKR_EVENING,
                type = TYPE_EVENING,
                titleKurdish = "زیکری ئێواران",
                subtitleKurdish = "٣٠ دەقە دوای بانگی عەسر",
                formattedTime = eveningFormatted,
                targetMillis = eveningTargetMillis
            ),
            ZikrScheduleItem(
                requestCode = REQ_ZIKR_SLEEP,
                type = TYPE_SLEEP,
                titleKurdish = "زیکری خەوتن",
                subtitleKurdish = "هەموو شەوێک کاتژمێر ٩ی شەو",
                formattedTime = sleepFormatted,
                targetMillis = sleepTargetMillis
            )
        )
    }

    /**
     * Schedules alarms for all 3 daily Zikrs.
     */
    fun scheduleAllZikrAlarms(context: Context) {
        val prefs = context.getSharedPreferences(ZarayenApp.PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean("zikr_notifications_enabled", true)
        if (!isEnabled) {
            Log.d(TAG, "Zikr notifications disabled by user. Cancelling alarms.")
            cancelAllZikrAlarms(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val items = getZikrScheduleItems(context)

        for (item in items) {
            scheduleSingleZikrAlarm(
                context = context,
                alarmManager = alarmManager,
                requestCode = item.requestCode,
                zikrType = item.type,
                titleKurdish = item.titleKurdish,
                formattedTime = item.formattedTime,
                targetMillis = item.targetMillis
            )
        }

        Log.d(TAG, "All 3 Zikr alarms scheduled successfully.")
    }

    private fun scheduleSingleZikrAlarm(
        context: Context,
        alarmManager: AlarmManager,
        requestCode: Int,
        zikrType: String,
        titleKurdish: String,
        formattedTime: String,
        targetMillis: Long
    ) {
        val intent = Intent(context, ZikrAlarmReceiver::class.java).apply {
            action = ACTION_ZIKR_ALARM
            putExtra("ZIKR_TYPE", zikrType)
            putExtra("ZIKR_TITLE", titleKurdish)
            putExtra("ZIKR_TIME", formattedTime)
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
            Log.d(TAG, "Zikr alarm scheduled for $titleKurdish at $formattedTime ($targetMillis)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule exact alarm for $titleKurdish: ${e.message}")
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
                Log.e(TAG, "Fallback alarm failed for $titleKurdish: ${e2.message}")
            }
        }
    }

    fun cancelAllZikrAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        for (code in ZIKR_CODES) {
            val intent = Intent(context, ZikrAlarmReceiver::class.java).apply {
                action = ACTION_ZIKR_ALARM
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
     * Schedules a quick 2-second test alarm for the specified Zikr type to verify wakeup and routing
     */
    fun scheduleTestZikrAlarm(context: Context, zikrType: String) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val testTargetMillis = System.currentTimeMillis() + 2000L

        val title = when (zikrType) {
            TYPE_MORNING -> "زیکری بەیانیان"
            TYPE_EVENING -> "زیکری ئێواران"
            else -> "زیکری خەوتن"
        }

        val intent = Intent(context, ZikrAlarmReceiver::class.java).apply {
            action = ACTION_ZIKR_ALARM
            putExtra("ZIKR_TYPE", zikrType)
            putExtra("ZIKR_TITLE", title)
            putExtra("ZIKR_TIME", "ئێستا (تاقیکردنەوە)")
            putExtra("REQUEST_CODE", REQ_ZIKR_TEST)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQ_ZIKR_TEST,
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
            Log.e(TAG, "Error scheduling test zikr alarm: ${e.message}")
        }
    }
}
