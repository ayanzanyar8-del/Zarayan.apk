package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_users")
data class AppUser(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fullName: String,
    val phoneNumber: String = "",
    val email: String = "",
    val neighborhood: String = "شارۆچکەی زەڕایەن",
    val signupDateFormatted: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val role: String = "بەکارهێنەر", // "ئەدمین" or "بەکارهێنەر"
    val notes: String = ""
)

@Entity(tableName = "janaza_announcements")
data class JanazaAnnouncement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val deceasedName: String,
    val description: String,
    val mosqueName: String,
    val cemeteryName: String,
    val prayerTime: String, // e.g., "دوای نوێژی عەسر"
    val condolenceLocation: String, // شوێنی پرسە
    val dateString: String, // e.g., "٢٤ ئابی ٢٠٢٦"
    val timestamp: Long = System.currentTimeMillis(),
    val contactPhone: String = "",
    val authorName: String = "ئەدمینی زەڕایەن",
    val likesCount: Int = 0,
    val isLiked: Boolean = false
)

@Entity(tableName = "dhikr_progress")
data class DhikrProgress(
    @PrimaryKey
    val dhikrId: String,
    val currentCount: Int = 0,
    val targetCount: Int = 33,
    val isCompleted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_names")
data class FavoriteName(
    @PrimaryKey
    val nameNumber: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quran_bookmarks")
data class QuranBookmark(
    @PrimaryKey
    val surahNumber: Int,
    val ayahNumber: Int = 1,
    val surahNameKurdish: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class NameOfAllah(
    val number: Int,
    val arabicName: String,
    val kurdishName: String,
    val kurdishMeaning: String,
    val isFavorite: Boolean = false
)

enum class DhikrCategory(val titleKurdish: String) {
    MORNING("زیکری بەیانیان"),
    EVENING("زیکری ئێواران"),
    BEDTIME("زیکری خەوتن"),
    DAILY_SUNNAH("سوننەت و ئادابەکانی ڕۆژانە"),
    ESSENTIAL("زیکرە پێویستەکان"),
    TASBIH("تەسبیحاتی گشتی")
}

data class DhikrItem(
    val id: String,
    val category: DhikrCategory,
    val arabicText: String,
    val kurdishTranslation: String,
    val benefit: String,
    val targetCount: Int,
    val currentCount: Int = 0,
    val isCompleted: Boolean = false
)

data class QuranSurah(
    val number: Int,
    val nameArabic: String,
    val nameKurdish: String,
    val revelationType: String, // "مەککە" / "مەدینە"
    val versesCount: Int,
    val pageNumber: Int,
    val summaryKurdish: String,
    val verses: List<QuranVerse> = emptyList()
)

data class QuranVerse(
    val numberInSurah: Int,
    val textArabic: String,
    val textKurdish: String
)

data class JuzInfo(
    val number: Int,
    val nameArabic: String,
    val nameKurdish: String,
    val startSurahNumber: Int,
    val startSurahName: String,
    val startAyah: Int,
    val startPage: Int
)

enum class PlaceCategory(val titleKurdish: String, val iconName: String) {
    MOSQUES("مزگەوتەکان", "mosque"),
    SCHOOLS("قوتابخانە و پەروەردە", "school"),
    HEALTH("کەرتی تەندروستی و خزمەتگوزاری", "hospital"),
    SPORTS("کەرتی وەرزش و یاریگاکان", "sports"),
    MARKETS("مارکێت و بازاڕەکان", "store"),
    FINANCE_FUEL("دارایی و سووتەمەنی", "local_gas_station"),
    PARK_GOV("پارک و فەرمانگە گشتییەکان", "park")
}

data class ZarayenPlace(
    val id: String,
    val name: String,
    val category: PlaceCategory,
    val address: String,
    val description: String,
    val phone: String = "",
    val googleMapsUrl: String = ""
)

data class CurrencyRate(
    val code: String,
    val nameKurdish: String,
    val buyPrice: String,
    val sellPrice: String,
    val unit: String,
    val change: String,
    val isPositive: Boolean
)

data class ZarayenWeather(
    val tempC: Int = 34,
    val highC: Int = 38,
    val lowC: Int = 23,
    val conditionKurdish: String = "ساماڵ و گەرم",
    val humidity: Int = 22,
    val windSpeedKmH: Int = 14,
    val uvIndex: Int = 8,
    val cityName: String = "شارۆچکەی زەڕایەن"
) {
    val temperatureC: Int get() = tempC
}

data class PrayerTimeItem(
    val nameKurdish: String,
    val nameArabic: String,
    val azanTime12h: String,
    val azanTime24h: String,
    val iqamahTime12h: String,
    val qamatMinutesOffset: Int = 0,
    val isPassed: Boolean = false,
    val isNext: Boolean = false,
    val isQamatPassed: Boolean = false
) {
    val formatted12Hours: String get() = azanTime12h
    val qamatFormatted: String get() = iqamahTime12h
}

data class TodayPrayerTimes(
    val fajr: PrayerTimeItem,
    val sunrise: PrayerTimeItem,
    val dhuhr: PrayerTimeItem,
    val asr: PrayerTimeItem,
    val maghrib: PrayerTimeItem,
    val isha: PrayerTimeItem,
    val nextPrayerName: String,
    val nextPrayerTimeFormatted: String,
    val remainingMillis: Long,
    val activeQamatStatus: String = "",
    val nextQamatTimeFormatted: String = ""
) {
    val prayers: List<PrayerTimeItem> get() = listOf(fajr, sunrise, dhuhr, asr, maghrib, isha)
    val nextPrayerNameKurdish: String get() = nextPrayerName
    val timeRemainingFormatted: String
        get() {
            val totalSeconds = (remainingMillis / 1000).coerceAtLeast(0)
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            val seconds = totalSeconds % 60
            return String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        }
}
