package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.datasource.QuranData
import com.example.data.model.AppUser
import com.example.data.model.DhikrCategory
import com.example.data.model.DhikrItem
import com.example.data.model.JanazaAnnouncement
import com.example.data.model.NameOfAllah
import com.example.data.model.PlaceCategory
import com.example.data.model.QuranSurah
import com.example.data.model.TodayPrayerTimes
import com.example.data.model.ZarayenPlace
import com.example.data.model.ZarayenWeather
import com.example.data.repository.ZarayenRepository
import com.example.data.util.CalendarConverter
import com.example.data.util.PrayerTimesCalculator
import com.example.data.util.TripleCalendarData
import com.example.util.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

enum class AppScreen {
    HOME,
    PRAYER_TIMES,
    NAMES_OF_ALLAH,
    AZKAR,
    QURAN_LIST,
    QURAN_READER,
    JANAZA,
    CURRENCY,
    DASTY2,
    QIBLA,
    PLACES,
    ABOUT_ZARAYEN,
    ADMIN_USERS,
    NEWS_PHOTOS,
    LIVE_LOCATION
}

data class UiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val isDarkMode: Boolean = true,
    val userName: String = "",
    val isRegistered: Boolean = false,
    val showRegistrationDialog: Boolean = false,
    val showAdminLoginDialog: Boolean = false,
    val showNewJanazaDialog: Boolean = false,
    val showAddUserDialog: Boolean = false,
    val editingJanaza: JanazaAnnouncement? = null,
    val deletingJanaza: JanazaAnnouncement? = null,
    val deletingUser: AppUser? = null,
    val isAdmin: Boolean = false,
    val tripleCalendar: TripleCalendarData = CalendarConverter.getCurrentTripleCalendar(),
    val todayPrayers: TodayPrayerTimes = PrayerTimesCalculator.getTodayPrayerTimes(),
    val clockHours: Float = 0f,
    val clockMinutes: Float = 0f,
    val clockSeconds: Float = 0f,
    val digitalTimeStr: String = "12:00:00",
    val isEarlyAlertEnabled: Boolean = true,
    val isAlwaysOnNotificationEnabled: Boolean = true,
    val isZikrNotificationEnabled: Boolean = true,
    val selectedQuranSurah: QuranSurah? = null,
    val selectedQuranPage: Int = 1,
    val quranFontSize: Float = 20f,
    val quranSearchQuery: String = "",
    val namesSearchQuery: String = "",
    val namesFavoritesOnly: Boolean = false,
    val userSearchQuery: String = "",
    val userFilterNeighborhood: String = "هەموو گەڕەکەکان",
    val selectedAzkarCategory: DhikrCategory = DhikrCategory.MORNING,
    val selectedPlacesCategory: PlaceCategory? = null,
    val placesSearchQuery: String = "",
    val weather: ZarayenWeather = ZarayenWeather(),
    val selectedMonthForCalendar: Int = Calendar.getInstance().get(Calendar.MONTH) + 1,
    val selectedYearForCalendar: Int = Calendar.getInstance().get(Calendar.YEAR),
    val snackbarMessage: String? = null,
    val sharedLocationSenderName: String? = null,
    val sharedLocationLat: Double? = null,
    val sharedLocationLng: Double? = null,
    val sharedLocationDurationHours: Int? = null,
    val sharedLocationExpiresAt: Long? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ZarayenRepository(application)
    private val appContext = application.applicationContext
    private var lastAlertedPrayer: String = ""

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = application.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        application.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _uiState = MutableStateFlow(
        UiState(
            userName = repository.getUserName(),
            isRegistered = repository.isUserRegistered(),
            showRegistrationDialog = !repository.isUserRegistered(),
            isAdmin = repository.isAdminUser(),
            isDarkMode = repository.isDarkModeEnabled(true),
            isEarlyAlertEnabled = repository.isAzanEarlyAlertEnabled(),
            isAlwaysOnNotificationEnabled = repository.isAlwaysOnNotificationEnabled(),
            isZikrNotificationEnabled = repository.isZikrNotificationEnabled(),
            quranFontSize = repository.getQuranFontSize()
        )
    )
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // Reactive DB Streams
    val janazaList: StateFlow<List<JanazaAnnouncement>> = repository.janazaAnnouncements
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestJanaza: StateFlow<JanazaAnnouncement?> = repository.latestJanazaAnnouncement
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val namesOfAllahList: StateFlow<List<NameOfAllah>> = repository.namesOfAllahFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val azkarList: StateFlow<List<DhikrItem>> = repository.allAzkarFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val registeredUsersList: StateFlow<List<AppUser>> = repository.allRegisteredUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wikiSections = repository.getWikiSections()
    val allSurahs = QuranData.surahs
    val juzList = QuranData.juzList

    init {
        NotificationHelper.createNotificationChannels(appContext)
        com.example.util.PrayerAlarmScheduler.scheduleAllPrayerAlarms(appContext)
        com.example.util.ZikrAlarmScheduler.scheduleAllZikrAlarms(appContext)
        if (_uiState.value.isAlwaysOnNotificationEnabled) {
            val initialPrayers = repository.getTodayPrayerTimes()
            NotificationHelper.showOngoingPrayerNotification(
                context = appContext,
                nextPrayerName = initialPrayers.nextPrayerNameKurdish,
                nextPrayerTime = initialPrayers.nextPrayerTimeFormatted,
                timeRemainingFormatted = initialPrayers.timeRemainingFormatted
            )
        }
        startClockAndCountdownLoop()
    }

    private fun startClockAndCountdownLoop() {
        viewModelScope.launch {
            val iraqTz = java.util.TimeZone.getTimeZone("Asia/Baghdad")
            while (isActive) {
                val cal = Calendar.getInstance(iraqTz)
                val hour = cal.get(Calendar.HOUR)
                val minute = cal.get(Calendar.MINUTE)
                val second = cal.get(Calendar.SECOND)
                val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)

                val digitalStr = String.format(
                    java.util.Locale.getDefault(),
                    "%02d:%02d:%02d",
                    hourOfDay, minute, second
                )

                val prayers = repository.getTodayPrayerTimes()
                val calendar = repository.getTripleCalendar()

                // Trigger notification and sound at the exact second of Bang
                val remainingSec = prayers.remainingMillis / 1000L
                val nextPrayerKey = "${prayers.nextPrayerNameKurdish}_${prayers.nextPrayerTimeFormatted}"

                if (_uiState.value.isEarlyAlertEnabled && remainingSec == 0L) {
                    if (lastAlertedPrayer != nextPrayerKey) {
                        lastAlertedPrayer = nextPrayerKey
                        NotificationHelper.showPrayerAlertNotification(
                            appContext,
                            prayerNameKurdish = prayers.nextPrayerNameKurdish,
                            prayerTimeFormatted = prayers.nextPrayerTimeFormatted
                        )
                        triggerSuccessHaptic()
                    }
                } else if (remainingSec > 10L) {
                    if (lastAlertedPrayer == nextPrayerKey) {
                        lastAlertedPrayer = ""
                    }
                }

                // Keep the Always-on persistent notification synced (refreshes smoothly every 5 seconds)
                if (_uiState.value.isAlwaysOnNotificationEnabled && (second % 5 == 0 || remainingSec == 0L)) {
                    NotificationHelper.showOngoingPrayerNotification(
                        context = appContext,
                        nextPrayerName = prayers.nextPrayerNameKurdish,
                        nextPrayerTime = prayers.nextPrayerTimeFormatted,
                        timeRemainingFormatted = prayers.timeRemainingFormatted
                    )
                }

                _uiState.update { current ->
                    current.copy(
                        clockHours = (hour % 12) + (minute / 60f),
                        clockMinutes = minute + (second / 60f),
                        clockSeconds = second.toFloat(),
                        digitalTimeStr = digitalStr,
                        todayPrayers = prayers,
                        tripleCalendar = calendar
                    )
                }
                delay(1000)
            }
        }
    }

    // Navigation
    fun navigateTo(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setSharedLocation(name: String, lat: Double, lng: Double, durationHours: Int, expiresAt: Long) {
        _uiState.update {
            it.copy(
                currentScreen = AppScreen.LIVE_LOCATION,
                sharedLocationSenderName = name,
                sharedLocationLat = lat,
                sharedLocationLng = lng,
                sharedLocationDurationHours = durationHours,
                sharedLocationExpiresAt = expiresAt
            )
        }
    }

    fun clearSharedLocation() {
        _uiState.update {
            it.copy(
                sharedLocationSenderName = null,
                sharedLocationLat = null,
                sharedLocationLng = null,
                sharedLocationDurationHours = null,
                sharedLocationExpiresAt = null
            )
        }
    }

    // Theme Toggle
    fun toggleDarkMode() {
        val newDark = !_uiState.value.isDarkMode
        repository.setDarkMode(newDark)
        _uiState.update { it.copy(isDarkMode = newDark) }
    }

    // User Registration & Management
    fun registerUser(
        name: String,
        phone: String = "",
        email: String = "",
        neighborhood: String = "شارۆچکەی زەڕایەن"
    ) {
        if (name.isNotBlank()) {
            val isAdmin = name.trim().equals("zanyar2", ignoreCase = true) || name.contains("ئەدمین", ignoreCase = true)
            val role = if (isAdmin) "ئەدمینی سەرەکی" else "بەکارهێنەر"
            viewModelScope.launch {
                repository.registerNewUser(
                    fullName = if (isAdmin) "ئەدمین زانیار" else name.trim(),
                    phoneNumber = phone.trim(),
                    email = email.trim(),
                    neighborhood = neighborhood.trim().ifEmpty { "شارۆچکەی زەڕایەن" },
                    role = role
                )
            }
            _uiState.update {
                it.copy(
                    userName = if (isAdmin) "ئەدمین زانیار" else name.trim(),
                    isRegistered = true,
                    showRegistrationDialog = false,
                    isAdmin = isAdmin,
                    snackbarMessage = if (isAdmin) "بەخێربێیت ئەدمینی بەڕێز بۆ ئەپی زەڕایەن" else "بەخێربێیت بۆ ئەپی زەڕایەن، $name"
                )
            }
        }
    }

    fun dismissRegistrationDialog() {
        if (!_uiState.value.isRegistered) {
            registerUser("میوانی بەڕێز", "", "", "شارۆچکەی زەڕایەن")
        }
        _uiState.update { it.copy(showRegistrationDialog = false) }
    }

    fun openAdminLoginDialog() {
        _uiState.update { it.copy(showAdminLoginDialog = true) }
    }

    fun closeAdminLoginDialog() {
        _uiState.update { it.copy(showAdminLoginDialog = false) }
    }

    fun loginAsAdmin(secretName: String) {
        if (secretName.trim().equals("zanyar2", ignoreCase = true)) {
            repository.saveUserProfile("ئەدمین")
            _uiState.update {
                it.copy(
                    userName = "ئەدمین",
                    isRegistered = true,
                    isAdmin = true,
                    showAdminLoginDialog = false,
                    snackbarMessage = "چوونەژوورەوەی ئەدمین بە سەرکەوتوویی ئەنجامدرا!"
                )
            }
        } else {
            _uiState.update { it.copy(snackbarMessage = "کۆدی چوونەژوورەوەی ئەدمین هەڵەیە!") }
        }
    }

    // Admin Users Management
    fun setUserSearchQuery(query: String) {
        _uiState.update { it.copy(userSearchQuery = query) }
    }

    fun setUserFilterNeighborhood(neighborhood: String) {
        _uiState.update { it.copy(userFilterNeighborhood = neighborhood) }
    }

    fun openAddUserDialog() {
        _uiState.update { it.copy(showAddUserDialog = true) }
    }

    fun closeAddUserDialog() {
        _uiState.update { it.copy(showAddUserDialog = false) }
    }

    fun addNewUserByAdmin(
        name: String,
        phone: String,
        email: String,
        neighborhood: String,
        role: String,
        notes: String
    ) {
        if (name.isNotBlank()) {
            viewModelScope.launch {
                repository.registerNewUser(
                    fullName = name.trim(),
                    phoneNumber = phone.trim(),
                    email = email.trim(),
                    neighborhood = neighborhood.trim().ifEmpty { "شارۆچکەی زەڕایەن" },
                    role = role.trim().ifEmpty { "بەکارهێنەر" },
                    notes = notes.trim()
                )
                _uiState.update {
                    it.copy(
                        showAddUserDialog = false,
                        snackbarMessage = "بەکارهێنەر ($name) بە سەرکەوتوویی زیادکرا"
                    )
                }
            }
        }
    }

    fun openDeleteUserDialog(user: AppUser) {
        _uiState.update { it.copy(deletingUser = user) }
    }

    fun cancelDeleteUser() {
        _uiState.update { it.copy(deletingUser = null) }
    }

    fun confirmDeleteUser() {
        val user = _uiState.value.deletingUser ?: return
        viewModelScope.launch {
            repository.deleteUser(user.id)
            _uiState.update {
                it.copy(
                    deletingUser = null,
                    snackbarMessage = "بەکارهێنەر (${user.fullName}) سڕایەوە"
                )
            }
        }
    }

    // Janaza Post Dialog & Actions
    fun openNewJanazaDialog() {
        _uiState.update { it.copy(showNewJanazaDialog = true) }
    }

    fun closeNewJanazaDialog() {
        _uiState.update { it.copy(showNewJanazaDialog = false) }
    }

    fun openEditJanazaDialog(announcement: JanazaAnnouncement) {
        _uiState.update { it.copy(editingJanaza = announcement) }
    }

    fun closeEditJanazaDialog() {
        _uiState.update { it.copy(editingJanaza = null) }
    }

    fun requestDeleteJanaza(announcement: JanazaAnnouncement) {
        _uiState.update { it.copy(deletingJanaza = announcement) }
    }

    fun cancelDeleteJanaza() {
        _uiState.update { it.copy(deletingJanaza = null) }
    }

    fun confirmDeleteJanaza() {
        val toDelete = _uiState.value.deletingJanaza ?: return
        viewModelScope.launch {
            repository.deleteJanazaAnnouncement(toDelete.id, toDelete.deceasedName)
            _uiState.update {
                it.copy(
                    deletingJanaza = null,
                    snackbarMessage = "ئاگادارییەکە بە سەرکەوتوویی سڕایەوە."
                )
            }
        }
    }

    fun publishJanazaAnnouncement(
        deceasedName: String,
        description: String,
        mosque: String,
        cemetery: String,
        prayerTime: String,
        condolencePlace: String,
        phone: String
    ) {
        viewModelScope.launch {
            repository.postJanazaAnnouncement(
                deceasedName = deceasedName,
                description = description,
                mosque = mosque,
                cemetery = cemetery,
                prayerTime = prayerTime,
                condolencePlace = condolencePlace,
                phone = phone
            )

            // Trigger system notification
            NotificationHelper.showJanazaNotification(
                context = appContext,
                deceasedName = deceasedName,
                mosqueName = mosque,
                prayerTime = prayerTime
            )

            _uiState.update {
                it.copy(
                    showNewJanazaDialog = false,
                    snackbarMessage = "ئاگاداری نوێژی مردوو بە سەرکەوتوویی بڵاوکرایەوە."
                )
            }
        }
    }

    fun saveEditedJanaza(
        deceasedName: String,
        description: String,
        mosque: String,
        cemetery: String,
        prayerTime: String,
        condolencePlace: String,
        phone: String
    ) {
        val existing = _uiState.value.editingJanaza ?: return
        viewModelScope.launch {
            repository.updateJanazaAnnouncement(
                id = existing.id,
                deceasedName = deceasedName,
                description = description,
                mosque = mosque,
                cemetery = cemetery,
                prayerTime = prayerTime,
                condolencePlace = condolencePlace,
                phone = phone,
                existingDate = existing.dateString,
                existingLikes = existing.likesCount,
                existingIsLiked = existing.isLiked
            )

            NotificationHelper.showJanazaNotification(
                context = appContext,
                deceasedName = "$deceasedName (دەستکاریکراو)",
                mosqueName = mosque,
                prayerTime = prayerTime
            )

            _uiState.update {
                it.copy(
                    editingJanaza = null,
                    snackbarMessage = "دەستکاری ئاگادارییەکە بە سەرکەوتوویی پاشەکەوتکرا."
                )
            }
        }
    }

    fun toggleJanazaLike(announcement: JanazaAnnouncement) {
        viewModelScope.launch {
            triggerHapticFeedback()
            repository.toggleLike(announcement)
        }
    }

    fun syncJanazaOnline() {
        viewModelScope.launch {
            val newCount = repository.syncJanazaOnline()
            if (newCount > 0) {
                _uiState.update { it.copy(snackbarMessage = "$newCount ئاگاداری نوێ لە سێرڤەری ئۆنلاین وەرگیرا") }
            } else {
                _uiState.update { it.copy(snackbarMessage = "تەواوی ئاگادارییەکان لەگەڵ سێرڤەری ئۆنلاین هاوکاتن") }
            }
        }
    }

    fun resendJanazaPushNotification(context: Context, announcement: JanazaAnnouncement) {
        NotificationHelper.showJanazaNotification(
            context = context,
            deceasedName = announcement.deceasedName,
            mosqueName = announcement.mosqueName,
            prayerTime = announcement.prayerTime
        )
        viewModelScope.launch {
            com.example.util.ZarayenFirebaseMessagingService.broadcastJanazaPushNotification(context, announcement)
        }
        _uiState.update {
            it.copy(snackbarMessage = "ئاگاداری نوێژی مردوو لە ڕێگەی Firebase FCM نێردرایەوە بۆ هەموو مۆبایلەکان.")
        }
    }

    fun sendTestFcmPushNotification(context: Context, isPrayerAlert: Boolean) {
        viewModelScope.launch {
            if (isPrayerAlert) {
                val nextPrayer = _uiState.value.todayPrayers.nextPrayerName
                val nextTime = _uiState.value.todayPrayers.nextPrayerTimeFormatted
                NotificationHelper.showPrayerAlertNotification(
                    context = context,
                    prayerNameKurdish = nextPrayer,
                    prayerTimeFormatted = nextTime
                )
                com.example.util.ZarayenFirebaseMessagingService.broadcastPrayerTimePushNotification(
                    prayerName = nextPrayer,
                    prayerTime = nextTime
                )
                _uiState.update { it.copy(snackbarMessage = "ئاگاداری تێستی بانگ لە ڕێگەی FCM بۆ هەموو مۆبایلەکان نێردرا") }
            } else {
                val testAnnouncement = janazaList.value.firstOrNull() ?: JanazaAnnouncement(
                    id = System.currentTimeMillis(),
                    deceasedName = "تاقیکردنەوەی ئاگاداری فەرمی زەڕایەن",
                    description = "ئەمە تاقیکردنەوەی ئاگاداری ڕاستەوخۆیە لە ڕێگەی Firebase Cloud Messaging",
                    mosqueName = "مزگەوتی گەورەی زەڕایەن",
                    cemeteryName = "گۆڕستانی شارۆچکەی زەڕایەن",
                    prayerTime = "دوای نوێژی نیوەڕۆ",
                    condolenceLocation = "هۆڵی مزگەوتی گەورە",
                    dateString = "ئەمڕۆ",
                    timestamp = System.currentTimeMillis(),
                    contactPhone = "07500000000",
                    authorName = "ئەدمینی زەڕایەن"
                )
                NotificationHelper.showJanazaNotification(
                    context = context,
                    deceasedName = testAnnouncement.deceasedName,
                    mosqueName = testAnnouncement.mosqueName,
                    prayerTime = testAnnouncement.prayerTime
                )
                com.example.util.ZarayenFirebaseMessagingService.broadcastJanazaPushNotification(context, testAnnouncement)
                _uiState.update { it.copy(snackbarMessage = "ئاگاداری تێستی نوێژی مردوو لە ڕێگەی FCM بۆ هەموو مۆبایلەکان نێردرا") }
            }
        }
    }

    fun shareJanazaAnnouncement(context: Context, announcement: JanazaAnnouncement) {
        val shareText = """
            📢 ئاگاداری کۆچی دوایی و نوێژی مردوو لە شارۆچکەی زەڕایەن
            ━━━━━━━━━━━━━━━━━
            ▪️ ${announcement.deceasedName}
            ▪️ ${announcement.description}
            🕌 مزگەوت: ${announcement.mosqueName}
            ⏰ کاتی نوێژ: ${announcement.prayerTime}
            🪦 گۆڕستان: ${announcement.cemeteryName}
            📍 شوێنی پرسە: ${announcement.condolenceLocation}
            ${if (announcement.contactPhone.isNotBlank()) "📞 پەیوەندی: " + announcement.contactPhone else ""}
            ━━━━━━━━━━━━━━━━━
            ئەپی شارۆچکەی زەڕایەن
        """.trimIndent()

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "هاوبەشکردنی نوێژی مردوو")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    // 99 Names of Allah
    fun setNamesSearchQuery(query: String) {
        _uiState.update { it.copy(namesSearchQuery = query) }
    }

    fun toggleNamesFavoritesOnly() {
        _uiState.update { it.copy(namesFavoritesOnly = !it.namesFavoritesOnly) }
    }

    fun toggleFavoriteName(name: NameOfAllah) {
        viewModelScope.launch {
            triggerHapticFeedback()
            repository.toggleFavoriteName(name.number, name.isFavorite)
        }
    }

    // Azkar
    fun selectAzkarCategory(category: DhikrCategory) {
        _uiState.update { it.copy(selectedAzkarCategory = category) }
    }

    fun incrementDhikrCount(item: DhikrItem) {
        viewModelScope.launch {
            triggerHapticFeedback()
            val updated = repository.incrementDhikr(item)
            if (updated.isCompleted && !item.isCompleted) {
                // Completed target alert
                triggerSuccessHaptic()
            }
        }
    }

    fun resetDhikrProgress(item: DhikrItem) {
        viewModelScope.launch {
            repository.resetDhikr(item.id)
        }
    }

    // Quran
    fun selectQuranSurah(surahNumber: Int) {
        val surah = repository.getSurahByNumber(surahNumber)
        _uiState.update {
            it.copy(
                selectedQuranSurah = surah,
                selectedQuranPage = surah.pageNumber,
                currentScreen = AppScreen.QURAN_READER
            )
        }
    }

    fun selectQuranPage(pageNumber: Int) {
        val validPage = pageNumber.coerceIn(1, 604)
        val surah = repository.getSurahForPage(validPage)
        _uiState.update {
            it.copy(
                selectedQuranSurah = surah,
                selectedQuranPage = validPage,
                currentScreen = AppScreen.QURAN_READER
            )
        }
    }

    fun selectQuranJuz(juzNumber: Int) {
        val juz = QuranData.juzList.find { it.number == juzNumber } ?: QuranData.juzList.first()
        selectQuranPage(juz.startPage)
    }

    fun nextQuranPage() {
        val current = _uiState.value.selectedQuranPage
        if (current < 604) {
            selectQuranPage(current + 1)
        }
    }

    fun prevQuranPage() {
        val current = _uiState.value.selectedQuranPage
        if (current > 1) {
            selectQuranPage(current - 1)
        }
    }

    fun setQuranSearchQuery(query: String) {
        _uiState.update { it.copy(quranSearchQuery = query) }
    }

    fun changeQuranFontSize(delta: Float) {
        val current = _uiState.value.quranFontSize
        val newSize = (current + delta).coerceIn(14f, 36f)
        repository.setQuranFontSize(newSize)
        _uiState.update { it.copy(quranFontSize = newSize) }
    }

    // Places Directory
    fun selectPlacesCategory(category: PlaceCategory?) {
        _uiState.update { it.copy(selectedPlacesCategory = category) }
    }

    fun setPlacesSearchQuery(query: String) {
        _uiState.update { it.copy(placesSearchQuery = query) }
    }

    fun getFilteredPlaces(): List<ZarayenPlace> {
        val state = _uiState.value
        return repository.getPlaces(state.selectedPlacesCategory, state.placesSearchQuery)
    }

    // Prayer Bang Alert & Alarm Toggle
    fun toggleEarlyAlert() {
        val newAlert = !_uiState.value.isEarlyAlertEnabled
        repository.setAzanEarlyAlert(newAlert)
        if (newAlert) {
            com.example.util.PrayerAlarmScheduler.scheduleAllPrayerAlarms(appContext)
        } else {
            com.example.util.PrayerAlarmScheduler.cancelAllPrayerAlarms(appContext)
        }
        _uiState.update {
            it.copy(
                isEarlyAlertEnabled = newAlert,
                snackbarMessage = if (newAlert) "زەنگ و ئاگاداری لە ساتی دروستی بانگ چالاککرا" else "زەنگ و ئاگاداری بانگ ناچالاککرا"
            )
        }
    }

    // Always-on Status Notification Toggle
    fun toggleAlwaysOnNotification(context: Context = appContext) {
        val newSetting = !_uiState.value.isAlwaysOnNotificationEnabled
        repository.setAlwaysOnNotification(newSetting)
        _uiState.update {
            it.copy(
                isAlwaysOnNotificationEnabled = newSetting,
                snackbarMessage = if (newSetting) "ئاگاداریی هەمیشەیی نوێژەکان لە شریتی سەرەوە چالاک کرا" else "ئاگاداریی هەمیشەیی ناچالاک کرا"
            )
        }
        if (newSetting) {
            val prayers = repository.getTodayPrayerTimes()
            NotificationHelper.showOngoingPrayerNotification(
                context = context,
                nextPrayerName = prayers.nextPrayerNameKurdish,
                nextPrayerTime = prayers.nextPrayerTimeFormatted,
                timeRemainingFormatted = prayers.timeRemainingFormatted
            )
        } else {
            NotificationHelper.cancelOngoingPrayerNotification(context)
        }
    }

    /**
     * Guarantees all notifications are turned ON (Alarms + Persistent Status Notification + Azkar Alarms).
     */
    fun ensureNotificationsAlwaysOn(context: Context = appContext) {
        repository.setAzanEarlyAlert(true)
        repository.setAlwaysOnNotification(true)
        repository.setZikrNotification(true)
        com.example.util.PrayerAlarmScheduler.scheduleAllPrayerAlarms(context)
        com.example.util.ZikrAlarmScheduler.scheduleAllZikrAlarms(context)
        val prayers = repository.getTodayPrayerTimes()
        NotificationHelper.showOngoingPrayerNotification(
            context = context,
            nextPrayerName = prayers.nextPrayerNameKurdish,
            nextPrayerTime = prayers.nextPrayerTimeFormatted,
            timeRemainingFormatted = prayers.timeRemainingFormatted
        )
        _uiState.update {
            it.copy(
                isEarlyAlertEnabled = true,
                isAlwaysOnNotificationEnabled = true,
                isZikrNotificationEnabled = true
            )
        }
    }

    fun toggleZikrNotifications(context: Context = appContext) {
        val newSetting = !_uiState.value.isZikrNotificationEnabled
        repository.setZikrNotification(newSetting)
        _uiState.update {
            it.copy(
                isZikrNotificationEnabled = newSetting,
                snackbarMessage = if (newSetting) "ئاگادارییەکانی زیکر (بەیانیان، ئێواران، خەوتن) چالاک کران" else "ئاگادارییەکانی زیکر ناچالاک کران"
            )
        }
        if (newSetting) {
            com.example.util.ZikrAlarmScheduler.scheduleAllZikrAlarms(context)
        } else {
            com.example.util.ZikrAlarmScheduler.cancelAllZikrAlarms(context)
        }
    }

    fun testZikrNotificationNow(context: Context = appContext, zikrType: String) {
        NotificationHelper.showZikrNotification(context, zikrType)
        com.example.util.ZikrAlarmScheduler.scheduleTestZikrAlarm(context, zikrType)
        val name = when (zikrType) {
            com.example.util.ZikrAlarmScheduler.TYPE_MORNING -> "زیکری بەیانیان"
            com.example.util.ZikrAlarmScheduler.TYPE_EVENING -> "زیکری ئێواران"
            else -> "زیکری خەوتن"
        }
        showSnackbar("ئاگاداریی تاقیکردنەوەی $name نێردرا")
        triggerSuccessHaptic()
    }

    fun testPrayerNotificationNow(context: Context) {
        val prayers = _uiState.value.todayPrayers
        val currentPrayerName = prayers.nextPrayerName
        val currentPrayerTime = prayers.nextPrayerTimeFormatted
        NotificationHelper.showPrayerAlertNotification(
            context = context,
            prayerNameKurdish = currentPrayerName,
            prayerTimeFormatted = currentPrayerTime
        )
        // Also schedule a quick 3-second test alarm to verify background wake-up
        com.example.util.PrayerAlarmScheduler.scheduleTestAlarm(context)
        triggerSuccessHaptic()
        _uiState.update {
            it.copy(snackbarMessage = "ئاگاداری بانگی $currentPrayerName تاقیکرایەوە بە سەرکەوتوویی")
        }
    }

    fun selectCalendarMonth(month: Int) {
        _uiState.update { it.copy(selectedMonthForCalendar = month) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    // Intents
    fun openUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            _uiState.update { it.copy(snackbarMessage = "نەتوانرا بەستەرەکە بکرێتەوە") }
        }
    }

    private fun triggerHapticFeedback() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        } catch (_: Exception) {}
    }

    private fun triggerSuccessHaptic() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 80), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(100)
            }
        } catch (_: Exception) {}
    }
}
