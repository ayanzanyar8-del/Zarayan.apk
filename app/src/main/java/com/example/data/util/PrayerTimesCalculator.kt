package com.example.data.util

import com.example.data.model.PrayerTimeItem
import com.example.data.model.TodayPrayerTimes
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

object PrayerTimesCalculator {
    // Zarayen, Sulaymaniyah, Kurdistan Coordinates
    const val ZARAYEN_LATITUDE = 35.3486
    const val ZARAYEN_LONGITUDE = 45.6711
    const val TIMEZONE_OFFSET = 3.0 // UTC+3 (Kurdistan/Iraq)

    val IRAQ_TIMEZONE: TimeZone = TimeZone.getTimeZone("Asia/Baghdad")

    // Kurdistan Awqaf & Sulaymaniyah/Zarayen standards (35.3486° N, 45.6711° E, UTC+3)
    private const val FAJR_ANGLE = -18.0
    private const val SUNRISE_ANGLE = -0.833
    private const val ISHA_ANGLE = -17.5

    data class DayPrayerTimes(
        val day: Int,
        val month: Int, // 1-12
        val year: Int,
        val fajrMinutes: Int,
        val sunriseMinutes: Int,
        val dhuhrMinutes: Int,
        val asrMinutes: Int,
        val maghribMinutes: Int,
        val ishaMinutes: Int
    )

    fun calculateForDate(
        year: Int,
        month: Int,
        day: Int,
        fajrOffset: Int = 0,
        dhuhrOffset: Int = 0,
        asrOffset: Int = 0,
        maghribOffset: Int = 0,
        ishaOffset: Int = 0
    ): DayPrayerTimes {
        // First check exact Darwaza Mosque 2026 Timetable
        val exactSchedule = com.example.data.datasource.ZarayenDarwazaMosqueSchedule.getDayPrayerTimes(
            year = year,
            month = month,
            day = day,
            fajrOffset = fajrOffset,
            dhuhrOffset = dhuhrOffset,
            asrOffset = asrOffset,
            maghribOffset = maghribOffset,
            ishaOffset = ishaOffset
        )
        if (exactSchedule != null) {
            return exactSchedule
        }

        val julianDate = calculateJulianDate(year, month, day)
        val d = julianDate - 2451545.0

        // Anomalies & Sun coordinates
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2 * g)))

        val e = 23.439 - 0.00000036 * d
        val ra = radToDeg(atan2(cos(degToRad(e)) * sin(degToRad(l)), cos(degToRad(l)))) / 15.0
        val rightAscension = fixHour(ra)

        val declination = radToDeg(asin(sin(degToRad(e)) * sin(degToRad(l))))
        val eqOfTime = q / 15.0 - fixHour(rightAscension)

        // Base Solar Noon in local time (UTC+3)
        // Solar transit at Zarayen longitude (45.6711° E)
        val transit = 12.0 + TIMEZONE_OFFSET - (ZARAYEN_LONGITUDE / 15.0) - eqOfTime

        // Kurdistan Awqaf & Sulaymaniyah Governorate Standard Calculations:
        // Dhuhr: Solar transit + 2.0 minutes (Zawwal offset)
        val dhuhr = transit + (2.0 / 60.0)

        // Sunrise: Sun altitude -0.833° (standard refraction & solar disc)
        val sunriseDiff = calculateHourAngle(declination, SUNRISE_ANGLE) / 15.0
        val sunrise = transit - sunriseDiff

        // Fajr: Kurdistan Awqaf astronomical twilight angle -18.0°
        val fajrDiff = calculateHourAngle(declination, FAJR_ANGLE) / 15.0
        val fajr = transit - fajrDiff

        // Asr: Shafi'i juristic shadow length = 1.0 + tan(abs(latitude - declination))
        val asrAltitude = radToDeg(atan(1.0 / (1.0 + tan(degToRad(abs(ZARAYEN_LATITUDE - declination))))))
        val asrDiff = calculateHourAngle(declination, asrAltitude) / 15.0
        val asr = transit + asrDiff + (1.0 / 60.0)

        // Sunset (Maghrib): Sun altitude -0.833° + 2.0 minutes safety buffer
        val sunsetDiff = calculateHourAngle(declination, SUNRISE_ANGLE) / 15.0
        val maghrib = transit + sunsetDiff + (2.0 / 60.0)

        // Isha: Kurdistan Awqaf standard night twilight angle -17.5° + 1.0 min
        val ishaDiff = calculateHourAngle(declination, ISHA_ANGLE) / 15.0
        val isha = transit + ishaDiff + (1.0 / 60.0)

        return DayPrayerTimes(
            day = day,
            month = month,
            year = year,
            fajrMinutes = (hourToMinutes(fajr) + fajrOffset).coerceIn(0, 1439),
            sunriseMinutes = hourToMinutes(sunrise).coerceIn(0, 1439),
            dhuhrMinutes = (hourToMinutes(dhuhr) + dhuhrOffset).coerceIn(0, 1439),
            asrMinutes = (hourToMinutes(asr) + asrOffset).coerceIn(0, 1439),
            maghribMinutes = (hourToMinutes(maghrib) + maghribOffset).coerceIn(0, 1439),
            ishaMinutes = (hourToMinutes(isha) + ishaOffset).coerceIn(0, 1439)
        )
    }

    fun getTodayPrayerTimes(
        fajrOffset: Int = 0,
        dhuhrOffset: Int = 0,
        asrOffset: Int = 0,
        maghribOffset: Int = 0,
        ishaOffset: Int = 0
    ): TodayPrayerTimes {
        val calendar = Calendar.getInstance(IRAQ_TIMEZONE)
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val currentSeconds = calendar.get(Calendar.SECOND)
        val currentTotalSeconds = currentMinutes * 60 + currentSeconds

        val today = calculateForDate(year, month, day, fajrOffset, dhuhrOffset, asrOffset, maghribOffset, ishaOffset)

        // Iqamah / Qamat offsets in minutes according to Zarayen Awqaf & user specification:
        // بەیانی: ٢٠ دەقیقە، نیوەڕۆ: ١٠ دەقیقە، عەسر: ١٠ دەقیقە، ئێوارە: ٥ دەقیقە، خەوتن: ١٠ دەقیقە
        val fajrIqamahOffset = 20
        val dhuhrIqamahOffset = 10
        val asrIqamahOffset = 10
        val maghribIqamahOffset = 5
        val ishaIqamahOffset = 10

        val fajrIqamah = today.fajrMinutes + fajrIqamahOffset
        val dhuhrIqamah = today.dhuhrMinutes + dhuhrIqamahOffset
        val asrIqamah = today.asrMinutes + asrIqamahOffset
        val maghribIqamah = today.maghribMinutes + maghribIqamahOffset
        val ishaIqamah = today.ishaMinutes + ishaIqamahOffset

        val fajrPassed = currentMinutes >= today.fajrMinutes
        val sunrisePassed = currentMinutes >= today.sunriseMinutes
        val dhuhrPassed = currentMinutes >= today.dhuhrMinutes
        val asrPassed = currentMinutes >= today.asrMinutes
        val maghribPassed = currentMinutes >= today.maghribMinutes
        val ishaPassed = currentMinutes >= today.ishaMinutes

        val fajrQamatPassed = currentMinutes >= fajrIqamah
        val dhuhrQamatPassed = currentMinutes >= dhuhrIqamah
        val asrQamatPassed = currentMinutes >= asrIqamah
        val maghribQamatPassed = currentMinutes >= maghribIqamah
        val ishaQamatPassed = currentMinutes >= ishaIqamah

        // Determine next upcoming prayer
        var nextPrayerName = "بەیانی"
        var nextPrayerTimeMinutes = today.fajrMinutes
        var isNextDay = false

        // Determine active Qamat waiting state (between Azan and Qamat)
        var activeQamatStatus = ""
        var nextQamatTimeFormatted = ""

        if (currentMinutes in today.fajrMinutes until fajrIqamah) {
            val minsLeft = fajrIqamah - currentMinutes
            activeQamatStatus = "کاتی قامەتی نوێژی بەیانی: $minsLeft خولەکی ماوە"
            nextQamatTimeFormatted = format12Hour(fajrIqamah)
        } else if (currentMinutes in today.dhuhrMinutes until dhuhrIqamah) {
            val minsLeft = dhuhrIqamah - currentMinutes
            activeQamatStatus = "کاتی قامەتی نوێژی نیوەڕۆ: $minsLeft خولەکی ماوە"
            nextQamatTimeFormatted = format12Hour(dhuhrIqamah)
        } else if (currentMinutes in today.asrMinutes until asrIqamah) {
            val minsLeft = asrIqamah - currentMinutes
            activeQamatStatus = "کاتی قامەتی نوێژی عەسر: $minsLeft خولەکی ماوە"
            nextQamatTimeFormatted = format12Hour(asrIqamah)
        } else if (currentMinutes in today.maghribMinutes until maghribIqamah) {
            val minsLeft = maghribIqamah - currentMinutes
            activeQamatStatus = "کاتی قامەتی نوێژی ئێوارە: $minsLeft خولەکی ماوە"
            nextQamatTimeFormatted = format12Hour(maghribIqamah)
        } else if (currentMinutes in today.ishaMinutes until ishaIqamah) {
            val minsLeft = ishaIqamah - currentMinutes
            activeQamatStatus = "کاتی قامەتی نوێژی خەوتن: $minsLeft خولەکی ماوە"
            nextQamatTimeFormatted = format12Hour(ishaIqamah)
        }

        if (currentMinutes < today.fajrMinutes) {
            nextPrayerName = "بەیانی"
            nextPrayerTimeMinutes = today.fajrMinutes
        } else if (currentMinutes < today.sunriseMinutes) {
            nextPrayerName = "خۆرهەڵاتن"
            nextPrayerTimeMinutes = today.sunriseMinutes
        } else if (currentMinutes < today.dhuhrMinutes) {
            nextPrayerName = "نیوەڕۆ"
            nextPrayerTimeMinutes = today.dhuhrMinutes
        } else if (currentMinutes < today.asrMinutes) {
            nextPrayerName = "عەسر"
            nextPrayerTimeMinutes = today.asrMinutes
        } else if (currentMinutes < today.maghribMinutes) {
            nextPrayerName = "ئێوارە"
            nextPrayerTimeMinutes = today.maghribMinutes
        } else if (currentMinutes < today.ishaMinutes) {
            nextPrayerName = "خەوتن"
            nextPrayerTimeMinutes = today.ishaMinutes
        } else {
            // Next is tomorrow's Fajr
            val tomorrow = Calendar.getInstance(IRAQ_TIMEZONE).apply { add(Calendar.DAY_OF_MONTH, 1) }
            val tomorrowTimes = calculateForDate(
                tomorrow.get(Calendar.YEAR),
                tomorrow.get(Calendar.MONTH) + 1,
                tomorrow.get(Calendar.DAY_OF_MONTH),
                fajrOffset, dhuhrOffset, asrOffset, maghribOffset, ishaOffset
            )
            nextPrayerName = "بەیانی (سبەی)"
            nextPrayerTimeMinutes = tomorrowTimes.fajrMinutes
            isNextDay = true
        }

        val targetSeconds = if (isNextDay) {
            (24 * 60 * 60) + (nextPrayerTimeMinutes * 60)
        } else {
            nextPrayerTimeMinutes * 60
        }
        val remainingSeconds = (targetSeconds - currentTotalSeconds).coerceAtLeast(0)
        val remainingMillis = remainingSeconds * 1000L

        return TodayPrayerTimes(
            fajr = PrayerTimeItem(
                nameKurdish = "بەیانی",
                nameArabic = "الفجر",
                azanTime12h = format12Hour(today.fajrMinutes),
                azanTime24h = format24Hour(today.fajrMinutes),
                iqamahTime12h = format12Hour(fajrIqamah),
                qamatMinutesOffset = fajrIqamahOffset,
                isPassed = fajrPassed,
                isNext = nextPrayerName == "بەیانی",
                isQamatPassed = fajrQamatPassed
            ),
            sunrise = PrayerTimeItem(
                nameKurdish = "خۆرهەڵاتن",
                nameArabic = "الشروق",
                azanTime12h = format12Hour(today.sunriseMinutes),
                azanTime24h = format24Hour(today.sunriseMinutes),
                iqamahTime12h = "-",
                qamatMinutesOffset = 0,
                isPassed = sunrisePassed,
                isNext = nextPrayerName == "خۆرهەڵاتن",
                isQamatPassed = sunrisePassed
            ),
            dhuhr = PrayerTimeItem(
                nameKurdish = "نیوەڕۆ",
                nameArabic = "الظهر",
                azanTime12h = format12Hour(today.dhuhrMinutes),
                azanTime24h = format24Hour(today.dhuhrMinutes),
                iqamahTime12h = format12Hour(dhuhrIqamah),
                qamatMinutesOffset = dhuhrIqamahOffset,
                isPassed = dhuhrPassed,
                isNext = nextPrayerName == "نیوەڕۆ",
                isQamatPassed = dhuhrQamatPassed
            ),
            asr = PrayerTimeItem(
                nameKurdish = "عەسر",
                nameArabic = "العصر",
                azanTime12h = format12Hour(today.asrMinutes),
                azanTime24h = format24Hour(today.asrMinutes),
                iqamahTime12h = format12Hour(asrIqamah),
                qamatMinutesOffset = asrIqamahOffset,
                isPassed = asrPassed,
                isNext = nextPrayerName == "عەسر",
                isQamatPassed = asrQamatPassed
            ),
            maghrib = PrayerTimeItem(
                nameKurdish = "ئێوارە",
                nameArabic = "المغرب",
                azanTime12h = format12Hour(today.maghribMinutes),
                azanTime24h = format24Hour(today.maghribMinutes),
                iqamahTime12h = format12Hour(maghribIqamah),
                qamatMinutesOffset = maghribIqamahOffset,
                isPassed = maghribPassed,
                isNext = nextPrayerName == "ئێوارە",
                isQamatPassed = maghribQamatPassed
            ),
            isha = PrayerTimeItem(
                nameKurdish = "خەوتن",
                nameArabic = "العشاء",
                azanTime12h = format12Hour(today.ishaMinutes),
                azanTime24h = format24Hour(today.ishaMinutes),
                iqamahTime12h = format12Hour(ishaIqamah),
                qamatMinutesOffset = ishaIqamahOffset,
                isPassed = ishaPassed,
                isNext = nextPrayerName == "خەوتن",
                isQamatPassed = ishaQamatPassed
            ),
            nextPrayerName = nextPrayerName,
            nextPrayerTimeFormatted = format12Hour(nextPrayerTimeMinutes),
            remainingMillis = remainingMillis,
            activeQamatStatus = activeQamatStatus,
            nextQamatTimeFormatted = nextQamatTimeFormatted
        )
    }

    fun getMonthPrayerTimes(
        year: Int,
        month: Int,
        fajrOffset: Int = 0,
        dhuhrOffset: Int = 0,
        asrOffset: Int = 0,
        maghribOffset: Int = 0,
        ishaOffset: Int = 0
    ): List<DayPrayerTimes> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val list = mutableListOf<DayPrayerTimes>()
        for (day in 1..maxDays) {
            list.add(calculateForDate(year, month, day, fajrOffset, dhuhrOffset, asrOffset, maghribOffset, ishaOffset))
        }
        return list
    }

    fun format12Hour(totalMinutes: Int): String {
        var hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60
        val isPm = hours >= 12
        if (hours == 0) hours = 12
        else if (hours > 12) hours -= 12
        val periodKurdish = if (isPm) "د.ن" else "ب.ن" // پاش نیوەڕۆ / پێش نیوەڕۆ
        return String.format(Locale.getDefault(), "%02d:%02d %s", hours, minutes, periodKurdish)
    }

    fun format24Hour(totalMinutes: Int): String {
        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60
        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes)
    }

    private fun hourToMinutes(hour: Double): Int {
        val normalized = fixHour(hour)
        return (normalized * 60).toInt()
    }

    private fun calculateHourAngle(declination: Double, altitude: Double): Double {
        val latRad = degToRad(ZARAYEN_LATITUDE)
        val decRad = degToRad(declination)
        val altRad = degToRad(altitude)
        val cosHA = (sin(altRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val clamped = cosHA.coerceIn(-1.0, 1.0)
        return radToDeg(acos(clamped))
    }

    private fun calculateJulianDate(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun degToRad(degree: Double) = degree * Math.PI / 180.0
    private fun radToDeg(radian: Double) = radian * 180.0 / Math.PI
    private fun fixHour(hour: Double): Double {
        var h = hour - 24.0 * floor(hour / 24.0)
        if (h < 0) h += 24.0
        return h
    }
    private fun fixAngle(angle: Double): Double {
        var a = angle - 360.0 * floor(angle / 360.0)
        if (a < 0) a += 360.0
        return a
    }
}
