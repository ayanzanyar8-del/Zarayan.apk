package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.ZarayenApp
import com.example.data.datasource.AzkarData
import com.example.data.datasource.CurrencyData
import com.example.data.datasource.NamesOfAllahData
import com.example.data.datasource.QuranData
import com.example.data.datasource.ZarayenPlacesData
import com.example.data.datasource.ZarayenWikiData
import com.example.data.local.AppDatabase
import com.example.data.network.JanazaCloudSyncManager
import com.example.data.model.AppUser
import com.example.data.model.DhikrCategory
import com.example.data.model.DhikrItem
import com.example.data.model.DhikrProgress
import com.example.data.model.FavoriteName
import com.example.data.model.JanazaAnnouncement
import com.example.data.model.NameOfAllah
import com.example.data.model.PlaceCategory
import com.example.data.model.QuranBookmark
import com.example.data.model.QuranSurah
import com.example.data.model.QuranVerse
import com.example.data.model.TodayPrayerTimes
import com.example.data.model.ZarayenPlace
import com.example.data.model.ZarayenWeather
import com.example.data.util.CalendarConverter
import com.example.data.util.PrayerTimesCalculator
import com.example.data.util.TripleCalendarData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ZarayenRepository(private val context: Context) {

    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "zarayen_database.db"
    ).fallbackToDestructiveMigration().build()

    private val userDao = db.userDao()
    private val janazaDao = db.janazaDao()
    private val dhikrDao = db.dhikrDao()
    private val favoritesDao = db.favoritesDao()
    private val prefs = context.getSharedPreferences(ZarayenApp.PREFS_NAME, Context.MODE_PRIVATE)

    val cloudSyncManager = JanazaCloudSyncManager(context.applicationContext, db)
    val mrduFirestoreService = com.example.data.network.MrduFirestoreService(context.applicationContext, db)

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialJanazaData()
            seedInitialUsers()
            mrduFirestoreService.startRealtimeListener()
            cloudSyncManager.startRealtimeSync()
        }
    }

    private suspend fun seedInitialUsers() {
        val userSeeded = prefs.getInt("users_seeded_v1", 0)
        if (userSeeded == 0) {
            val initialUsers = listOf(
                AppUser(
                    fullName = "ئەدمین زانیار (Zanyar)",
                    phoneNumber = "07701458923",
                    email = "zanyarba@gmail.com",
                    neighborhood = "سەنتەری زەڕایەن",
                    signupDateFormatted = "٢٠٢٦/٠٨/٢٥ - ٠٩:١٥",
                    timestamp = System.currentTimeMillis() - 86400000 * 5,
                    role = "ئەدمینی سەرەکی",
                    notes = "بەڕێوەبەری سەرەکی ئەپڵیکەیشنی شارۆچکەی زەڕایەن"
                ),
                AppUser(
                    fullName = "مامۆستا ئاراس کەریم",
                    phoneNumber = "07503124578",
                    email = "aras.karim@zarayen.org",
                    neighborhood = "گەڕەکی مەولەوی",
                    signupDateFormatted = "٢٠٢٦/٠٨/٢٥ - ٠٨:٣٠",
                    timestamp = System.currentTimeMillis() - 86400000 * 3,
                    role = "بەکارهێنەر",
                    notes = "مامۆستای قوتابخانەی زەڕایەنی کوڕان"
                ),
                AppUser(
                    fullName = "دکتۆر هەڵمەت محەمەد",
                    phoneNumber = "07718903412",
                    email = "dr.halmat@gmail.com",
                    neighborhood = "گەڕەکی کانی ئاسکان",
                    signupDateFormatted = "٢٠٢٦/٠٨/٢٤ - ١٨:٤٥",
                    timestamp = System.currentTimeMillis() - 86400000 * 2,
                    role = "بەکارهێنەر",
                    notes = "پزیشک لە مەڵبەندی تەندروستی زەڕایەن"
                ),
                AppUser(
                    fullName = "دیار عوسمان عەلی",
                    phoneNumber = "07705421980",
                    email = "diyar.osman94@gmail.com",
                    neighborhood = "گەڕەکی سۆران",
                    signupDateFormatted = "٢٠٢٦/٠٨/٢٤ - ١٤:٢٠",
                    timestamp = System.currentTimeMillis() - 86400000 * 1,
                    role = "بەکارهێنەر",
                    notes = "کاسبکار لە بازاڕی زەڕایەن"
                ),
                AppUser(
                    fullName = "ڕێبین شێرکۆ فەتاح",
                    phoneNumber = "07512349087",
                    email = "rebin.sherko@yahoo.com",
                    neighborhood = "گەڕەکی ڕزگاری",
                    signupDateFormatted = "٢٠٢٦/٠٨/٢٣ - ١١:١٠",
                    timestamp = System.currentTimeMillis() - 86400000 * 4,
                    role = "بەکارهێنەر",
                    notes = "چالاکوانی کۆمەڵگەی مەدەنی"
                ),
                AppUser(
                    fullName = "خاتوو سازان عەبدوڵڵا",
                    phoneNumber = "07724567890",
                    email = "sazan.abdulla@gmail.com",
                    neighborhood = "گەڕەکی نەورۆز",
                    signupDateFormatted = "٢٠٢٦/٠٨/٢٢ - ١٦:٠٠",
                    timestamp = System.currentTimeMillis() - 86400000 * 6,
                    role = "بەکارهێنەر",
                    notes = "فەرمانبەر لە شارەوانی زەڕایەن"
                )
            )
            userDao.insertAll(initialUsers)
            prefs.edit().putInt("users_seeded_v1", 1).apply()
        }
    }

    private suspend fun seedInitialJanazaData() {
        val count = prefs.getInt("seeded_version", 0)
        if (count == 0) {
            val initialList = listOf(
                JanazaAnnouncement(
                    deceasedName = "کۆچی دوایی: خوالێخۆشبوو (حاجی حەمە ئەمین زەڕایەنی)",
                    description = "بە داخ و پەژارەیەکی زۆرەوە هەواڵی کۆچی دوایی خوالێخۆشبوو حاجی حەمە ئەمین ڕادەگەیەنین، کەسایەتی دیاری شارۆچکەکەمان. داواکارین لە خوای گەورە جێگای بەهەشتی بەرین بێت.",
                    mosqueName = "مزگەوتی گەورەی زەڕایەن",
                    cemeteryName = "گۆڕستانی گشتی زەڕایەن",
                    prayerTime = "دوای نوێژی عەسر",
                    condolenceLocation = "پرسەی پیاوان لە مزگەوتی گەورەی زەڕایەن و ژنان لە ماڵی خوالێخۆشبوو لە گەڕەکی مەولەوی",
                    dateString = "ئەمڕۆ",
                    timestamp = System.currentTimeMillis() - 3600000 * 2,
                    authorName = "ئەدمینی زەڕایەن (zanyar2)",
                    likesCount = 42,
                    isLiked = false
                ),
                JanazaAnnouncement(
                    deceasedName = "کۆچی دوایی: خاتوو (فاتمە مەحموود)",
                    description = "کۆچی دوایی دایکی شەهیدان خاتوو فاتمە ڕادەگەیەنین، مەراسیمی نوێژ و بەخاکسپاردن بەڕێوەدەچێت. خوای گەورە سەبووری بە هەموو لایەک ببەخشێت.",
                    mosqueName = "مزگەوتی مەولەوی",
                    cemeteryName = "گۆڕستانی پیر خدری شاهۆ",
                    prayerTime = "دوای نوێژی نیوەڕۆ",
                    condolenceLocation = "مزگەوتی کانی ئاسکان",
                    dateString = "دوێنێ",
                    timestamp = System.currentTimeMillis() - 3600000 * 26,
                    authorName = "ئەدمینی زەڕایەن",
                    likesCount = 28,
                    isLiked = false
                )
            )
            janazaDao.insertAll(initialList)
            prefs.edit().putInt("seeded_version", 1).apply()
        }
    }

    // 1. User & Admin State
    val allRegisteredUsers: Flow<List<AppUser>> = userDao.getAllUsers()
    val totalUsersCount: Flow<Int> = userDao.getUserCount()

    fun isUserRegistered(): Boolean = prefs.getBoolean(ZarayenApp.KEY_IS_REGISTERED, false)
    fun getUserName(): String = prefs.getString(ZarayenApp.KEY_USER_NAME, "") ?: ""
    
    suspend fun registerNewUser(
        fullName: String,
        phoneNumber: String = "",
        email: String = "",
        neighborhood: String = "شارۆچکەی زەڕایەن",
        role: String = "بەکارهێنەر",
        notes: String = ""
    ): Long {
        val sdf = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault())
        val dateStr = sdf.format(Date())
        val user = AppUser(
            fullName = fullName.trim(),
            phoneNumber = phoneNumber.trim(),
            email = email.trim(),
            neighborhood = neighborhood.trim().ifEmpty { "شارۆچکەی زەڕایەن" },
            signupDateFormatted = dateStr,
            timestamp = System.currentTimeMillis(),
            role = role,
            notes = notes.trim()
        )
        prefs.edit()
            .putString(ZarayenApp.KEY_USER_NAME, fullName.trim())
            .putBoolean(ZarayenApp.KEY_IS_REGISTERED, true)
            .apply()
        return userDao.insertUser(user)
    }

    suspend fun deleteUser(id: Long) {
        userDao.deleteUserById(id)
    }

    suspend fun insertUser(user: AppUser): Long {
        return userDao.insertUser(user)
    }

    fun saveUserProfile(name: String) {
        prefs.edit()
            .putString(ZarayenApp.KEY_USER_NAME, name.trim())
            .putBoolean(ZarayenApp.KEY_IS_REGISTERED, true)
            .apply()
    }
    fun isAdminUser(): Boolean = getUserName().equals("zanyar2", ignoreCase = true) || getUserName().contains("ئەدمین", ignoreCase = true)

    // Dark Mode preference (default enabled on startup)
    fun isDarkModeEnabled(defaultDark: Boolean = true): Boolean {
        return prefs.getBoolean(ZarayenApp.KEY_DARK_MODE, defaultDark)
    }
    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean(ZarayenApp.KEY_DARK_MODE, enabled).apply()
    }

    // Early Azan Alert preference (default enabled at all times)
    fun isAzanEarlyAlertEnabled(): Boolean = prefs.getBoolean(ZarayenApp.KEY_AZAN_EARLY_ALERT, true)
    fun setAzanEarlyAlert(enabled: Boolean) {
        prefs.edit().putBoolean(ZarayenApp.KEY_AZAN_EARLY_ALERT, enabled).apply()
    }

    // Always-on Persistent Status Notification preference (default always true)
    fun isAlwaysOnNotificationEnabled(): Boolean = prefs.getBoolean(ZarayenApp.KEY_ALWAYS_ON_NOTIFICATION, true)
    fun setAlwaysOnNotification(enabled: Boolean) {
        prefs.edit().putBoolean(ZarayenApp.KEY_ALWAYS_ON_NOTIFICATION, enabled).apply()
    }

    // Daily Azkar Notifications preference (default enabled at all times)
    fun isZikrNotificationEnabled(): Boolean = prefs.getBoolean(ZarayenApp.KEY_ZIKR_NOTIFICATIONS, true)
    fun setZikrNotification(enabled: Boolean) {
        prefs.edit().putBoolean(ZarayenApp.KEY_ZIKR_NOTIFICATIONS, enabled).apply()
    }

    // 2. Janaza Feed
    val janazaAnnouncements: Flow<List<JanazaAnnouncement>> = janazaDao.getAllAnnouncements()
    val latestJanazaAnnouncement: Flow<JanazaAnnouncement?> = janazaDao.getLatestAnnouncement()

    suspend fun postJanazaAnnouncement(
        deceasedName: String,
        description: String,
        mosque: String,
        cemetery: String,
        prayerTime: String,
        condolencePlace: String,
        phone: String
    ): Long {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateStr = sdf.format(Date())
        val item = JanazaAnnouncement(
            deceasedName = deceasedName,
            description = description,
            mosqueName = mosque,
            cemeteryName = cemetery,
            prayerTime = prayerTime,
            condolenceLocation = condolencePlace,
            dateString = dateStr,
            timestamp = System.currentTimeMillis(),
            contactPhone = phone,
            authorName = "ئەدمینی زەڕایەن",
            likesCount = 1,
            isLiked = true
        )
        val insertedId = janazaDao.insertAnnouncement(item)
        val finalItem = item.copy(id = insertedId)
        CoroutineScope(Dispatchers.IO).launch {
            mrduFirestoreService.createMrduPost(finalItem)
            cloudSyncManager.publishAnnouncementToCloud(finalItem)
        }
        return insertedId
    }

    suspend fun updateJanazaAnnouncement(
        id: Long,
        deceasedName: String,
        description: String,
        mosque: String,
        cemetery: String,
        prayerTime: String,
        condolencePlace: String,
        phone: String,
        existingDate: String,
        existingLikes: Int,
        existingIsLiked: Boolean
    ) {
        val item = JanazaAnnouncement(
            id = id,
            deceasedName = deceasedName,
            description = description,
            mosqueName = mosque,
            cemeteryName = cemetery,
            prayerTime = prayerTime,
            condolenceLocation = condolencePlace,
            dateString = existingDate,
            timestamp = System.currentTimeMillis(),
            contactPhone = phone,
            authorName = "ئەدمینی زەڕایەن (دەستکاریکراو)",
            likesCount = existingLikes,
            isLiked = existingIsLiked
        )
        janazaDao.updateAnnouncement(item)
        CoroutineScope(Dispatchers.IO).launch {
            mrduFirestoreService.updateMrduPost(item)
            cloudSyncManager.publishAnnouncementToCloud(item)
        }
    }

    suspend fun deleteJanazaAnnouncement(id: Long, deceasedName: String = "") {
        janazaDao.deleteById(id)
        CoroutineScope(Dispatchers.IO).launch {
            mrduFirestoreService.deleteMrduPost(id, deceasedName)
            cloudSyncManager.deleteAnnouncementFromCloud(id)
        }
    }

    suspend fun syncJanazaOnline(): Int {
        val firestoreCount = mrduFirestoreService.fetchAndSyncFirestore(triggerNotification = true)
        val cloudCount = cloudSyncManager.fetchAndSyncRemoteAnnouncements(triggerNotification = true)
        return maxOf(firestoreCount, cloudCount)
    }

    suspend fun toggleLike(announcement: JanazaAnnouncement) {
        if (announcement.isLiked) {
            janazaDao.unlikeAnnouncement(announcement.id)
        } else {
            janazaDao.likeAnnouncement(announcement.id)
        }
    }

    // 3. Calendars and Prayer Times
    fun getTripleCalendar(): TripleCalendarData = CalendarConverter.getCurrentTripleCalendar()

    fun getTodayPrayerTimes(): TodayPrayerTimes = PrayerTimesCalculator.getTodayPrayerTimes()

    fun getMonthPrayerCalendar(year: Int, month: Int) = PrayerTimesCalculator.getMonthPrayerTimes(year, month)

    // 4. 99 Names of Allah
    val namesOfAllahFlow: Flow<List<NameOfAllah>> = favoritesDao.getFavoriteNames().map { favList ->
        val favSet = favList.map { it.nameNumber }.toSet()
        NamesOfAllahData.names.map { name ->
            name.copy(isFavorite = favSet.contains(name.number))
        }
    }

    suspend fun toggleFavoriteName(number: Int, isFavorite: Boolean) {
        if (isFavorite) {
            favoritesDao.removeFavoriteName(number)
        } else {
            favoritesDao.addFavoriteName(FavoriteName(number))
        }
    }

    // 5. Azkar & Tasbih
    val allAzkarFlow: Flow<List<DhikrItem>> = dhikrDao.getAllProgress().map { progressList ->
        val progressMap = progressList.associateBy { it.dhikrId }
        AzkarData.azkarList.map { item ->
            val saved = progressMap[item.id]
            if (saved != null) {
                item.copy(
                    currentCount = saved.currentCount,
                    isCompleted = saved.isCompleted
                )
            } else {
                item
            }
        }
    }

    suspend fun incrementDhikr(item: DhikrItem): DhikrItem {
        val newCount = item.currentCount + 1
        val isDone = newCount >= item.targetCount
        dhikrDao.saveProgress(
            DhikrProgress(
                dhikrId = item.id,
                currentCount = newCount,
                targetCount = item.targetCount,
                isCompleted = isDone
            )
        )
        return item.copy(currentCount = newCount, isCompleted = isDone)
    }

    suspend fun resetDhikr(id: String) {
        dhikrDao.resetProgress(id)
    }

    // 6. Quran
    fun getAllSurahs(): List<QuranSurah> = QuranData.surahs

    fun getSurahByNumber(number: Int): QuranSurah {
        return QuranData.getSurahByNumber(number)
    }

    fun getSurahForPage(pageNumber: Int): QuranSurah {
        return QuranData.getSurahForPage(pageNumber)
    }

    fun getJuzList() = QuranData.juzList

    val latestQuranBookmark: Flow<QuranBookmark?> = favoritesDao.getLatestQuranBookmark()
    suspend fun saveBookmark(surahNumber: Int, ayahNumber: Int, surahNameKurdish: String) {
        favoritesDao.saveQuranBookmark(QuranBookmark(surahNumber, ayahNumber, surahNameKurdish))
    }

    fun getQuranFontSize(): Float = prefs.getFloat(ZarayenApp.KEY_QURAN_FONT_SIZE, 20f)
    fun setQuranFontSize(size: Float) {
        prefs.edit().putFloat(ZarayenApp.KEY_QURAN_FONT_SIZE, size).apply()
    }

    // 7. Places Directory
    fun getPlaces(category: PlaceCategory? = null, query: String = ""): List<ZarayenPlace> {
        return ZarayenPlacesData.places.filter { place ->
            val matchesCat = category == null || place.category == category
            val matchesQuery = query.isBlank() ||
                    place.name.contains(query, ignoreCase = true) ||
                    place.description.contains(query, ignoreCase = true) ||
                    place.address.contains(query, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    // 8. Weather & Currency
    fun getZarayenWeather(): ZarayenWeather = ZarayenWeather()
    fun getCurrencyRates() = CurrencyData.defaultRates
    fun getGoldRates() = CurrencyData.goldRates
    fun getWikiSections() = ZarayenWikiData.sections
}
