package com.example.data.util

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.floor

data class CalendarDetail(
    val dayNumber: Int,
    val monthNumber: Int,
    val yearNumber: Int,
    val monthNameKurdish: String,
    val dayNameKurdish: String,
    val fullFormatted: String
)

data class TripleCalendarData(
    val gregorian: CalendarDetail,
    val hijri: CalendarDetail,
    val kurdish: CalendarDetail
)

object CalendarConverter {

    val IRAQ_TIMEZONE: TimeZone = TimeZone.getTimeZone("Asia/Baghdad")

    private val GREGORIAN_MONTHS_KURDISH = listOf(
        "کانوونی دووەم", "شوبات", "ئازار", "نیسان", "ئایار", "حوزەیران",
        "تەممووز", "ئاب", "ئەیلوول", "تشرینی یەکەم", "تشرینی دووەم", "کانوونی یەکەم"
    )

    private val HIJRI_MONTHS_KURDISH = listOf(
        "موحەڕڕەم", "سەفەر", "ڕەبیعی یەکەم", "ڕەبیعی دووەم", "جومادای یەکەم", "جومادای دووەم",
        "ڕەجەب", "شەعبان", "ڕەمەزان", "شەووال", "زولقەعدە", "زولحیججە"
    )

    private val KURDISH_MONTHS = listOf(
        "نەورۆز", "گوڵان", "جۆزەردان", "پووشپەڕ", "گەلاوێژ", "خەرمانان",
        "ڕەزبەر", "خەزەڵوەر", "سەرماوەز", "بەفرانبار", "ڕێبەندان", "ڕەشەمە"
    )

    private val DAYS_KURDISH = listOf(
        "یەکشەممە", "دووشەممە", "سێشەممە", "چوارشەممە", "پێنجشەممە", "هەینی", "شەممە"
    )

    fun getCurrentTripleCalendar(): TripleCalendarData {
        val calendar = Calendar.getInstance(IRAQ_TIMEZONE)
        return getTripleCalendarForDate(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH) + 1,
            calendar.get(Calendar.DAY_OF_MONTH),
            calendar.get(Calendar.DAY_OF_WEEK)
        )
    }

    fun getTripleCalendarForDate(gy: Int, gm: Int, gd: Int, dayOfWeekCalendar: Int): TripleCalendarData {
        // Kurdish Day Name (Calendar.SUNDAY is 1)
        val dayIndex = (dayOfWeekCalendar - 1).coerceIn(0, 6)
        val dayNameKurdish = DAYS_KURDISH[dayIndex]

        // 1. Gregorian
        val gregMonthName = GREGORIAN_MONTHS_KURDISH[(gm - 1).coerceIn(0, 11)]
        val gregFull = "$gd $gregMonthName $gy"
        val gregDetail = CalendarDetail(
            dayNumber = gd,
            monthNumber = gm,
            yearNumber = gy,
            monthNameKurdish = gregMonthName,
            dayNameKurdish = dayNameKurdish,
            fullFormatted = gregFull
        )

        // 2. Hijri (Kuwaiti algorithm / Astronomical conversion)
        val hijriArray = gregorianToHijri(gy, gm, gd)
        val hy = hijriArray[0]
        val hm = hijriArray[1]
        val hd = hijriArray[2]
        val hijriMonthName = HIJRI_MONTHS_KURDISH[(hm - 1).coerceIn(0, 11)]
        val hijriFull = "$hd $hijriMonthName $hy کۆچی"
        val hijriDetail = CalendarDetail(
            dayNumber = hd,
            monthNumber = hm,
            yearNumber = hy,
            monthNameKurdish = hijriMonthName,
            dayNameKurdish = dayNameKurdish,
            fullFormatted = hijriFull
        )

        // 3. Kurdish Solar (ڕۆژژمێری کوردی)
        val kurdishArray = gregorianToKurdishSolar(gy, gm, gd)
        val ky = kurdishArray[0]
        val km = kurdishArray[1]
        val kd = kurdishArray[2]
        val kurdishMonthName = KURDISH_MONTHS[(km - 1).coerceIn(0, 11)]
        val kurdishFull = "$kd $kurdishMonthName $ky کوردی"
        val kurdishDetail = CalendarDetail(
            dayNumber = kd,
            monthNumber = km,
            yearNumber = ky,
            monthNameKurdish = kurdishMonthName,
            dayNameKurdish = dayNameKurdish,
            fullFormatted = kurdishFull
        )

        return TripleCalendarData(
            gregorian = gregDetail,
            hijri = hijriDetail,
            kurdish = kurdishDetail
        )
    }

    private fun gregorianToHijri(gy: Int, gm: Int, gd: Int): IntArray {
        var m = gm
        var y = gy
        if (m < 3) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        val jd = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + gd + b - 1524.5

        val z = jd - 1948439.5
        val cyc = floor(z / 10631.0)
        val rem = z - cyc * 10631.0
        val j = floor((rem - 1.0) / 354.36667)
        val v = rem - floor(j * 354.36667)
        val hm = floor((v + 28.5) / 29.5).toInt()
        val hy = (cyc * 30 + j).toInt() + 1
        var hd = (v - floor(hm * 29.5) + 29).toInt()

        if (hd > 30) hd = 30
        if (hd < 1) hd = 1
        val finalMonth = hm.coerceIn(1, 12)

        return intArrayOf(hy, finalMonth, hd)
    }

    private fun gregorianToKurdishSolar(gy: Int, gm: Int, gd: Int): IntArray {
        // Kurdish year is Gregorian Year + 700 (e.g., 2026 -> 2726)
        // Standard Persian/Kurdish Solar day conversion
        val gDaysInMonth = intArrayOf(31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        val jDaysInMonth = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)

        var gy2 = gy - 1600
        var gm2 = gm - 1
        var gd2 = gd - 1

        var gDayNo = 365 * gy2 + floor((gy2 + 3) / 4.0).toInt() - floor((gy2 + 99) / 100.0).toInt() + floor((gy2 + 399) / 400.0).toInt()
        for (i in 0 until gm2) {
            gDayNo += gDaysInMonth[i]
        }
        if (gm2 > 1 && ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0))) {
            gDayNo += 1
        }
        gDayNo += gd2

        var jDayNo = gDayNo - 79
        val jNp = floor(jDayNo / 12053.0).toInt()
        jDayNo %= 12053

        var jy = 979 + 33 * jNp + 4 * floor(jDayNo / 1461.0).toInt()
        jDayNo %= 1461

        if (jDayNo >= 366) {
            jy += floor((jDayNo - 1) / 365.0).toInt()
            jDayNo = (jDayNo - 1) % 365
        }

        var jm = 0
        while (jm < 11 && jDayNo >= jDaysInMonth[jm]) {
            jDayNo -= jDaysInMonth[jm]
            jm++
        }
        val jd = jDayNo + 1
        val kurdishMonth = jm + 1
        // Kurdish year is Solar Hijri + 1321, which equals Gregorian + 700
        val kurdishYear = jy + 1321

        return intArrayOf(kurdishYear, kurdishMonth, jd)
    }

    fun toKurdishDigits(number: Int): String {
        val digits = listOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")
        return number.toString().map { char ->
            if (char.isDigit()) digits[char.digitToInt()] else char
        }.joinToString("")
    }
}
