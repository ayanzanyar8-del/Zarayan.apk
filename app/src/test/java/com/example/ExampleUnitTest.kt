package com.example

import com.example.data.datasource.AzkarData
import com.example.data.model.DhikrCategory
import com.example.data.util.CalendarConverter
import com.example.data.util.PrayerTimesCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testAzkarMorningAndEveningContent() {
        val allAzkar = AzkarData.azkarList
        assertTrue("Azkar list should not be empty", allAzkar.isNotEmpty())

        val morningAzkar = allAzkar.filter { it.category == DhikrCategory.MORNING }
        assertTrue("Morning Azkar should contain items", morningAzkar.size >= 15)

        val eveningAzkar = allAzkar.filter { it.category == DhikrCategory.EVENING }
        assertTrue("Evening Azkar should contain items", eveningAzkar.size >= 25)

        // Verify Ayat Al-Kursi is present in morning and evening
        assertTrue(morningAzkar.any { it.arabicText.contains("اللَّهُ لا إِلٰهَ إِلّا هُوَ الحَىُّ القَيّومُ") })
        assertTrue(eveningAzkar.any { it.arabicText.contains("اللّهُ لاَ إِلَـهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ") })

        // Verify Surah Al-Ikhlas is 3x
        val ikhlasMorning = morningAzkar.firstOrNull { it.arabicText.contains("قُلۡ هُوَ ٱللَّهُ أَحَدٌ") }
        assertNotNull(ikhlasMorning)
        assertEquals(3, ikhlasMorning?.targetCount)

        // Verify Sayyidul Istighfar is present
        assertTrue(morningAzkar.any { it.arabicText.contains("اللهم أنت ربي لا إله إلا أنت") })
        assertTrue(eveningAzkar.any { it.arabicText.contains("اللّهـمَّ أَنْتَ رَبِّـي لا إلهَ إلاّ أَنْتَ") })
    }

    @Test
    fun testBedtimeAzkarContent() {
        val bedtimeAzkar = AzkarData.azkarList.filter { it.category == DhikrCategory.BEDTIME }
        assertEquals("Bedtime Azkar should contain 13 items", 13, bedtimeAzkar.size)

        // 1. Mu'awwidhat (Ikhlas, Falaq, Nas) 3x
        val item1 = bedtimeAzkar.find { it.id == "b_1" }
        assertNotNull(item1)
        assertTrue(item1!!.arabicText.contains("قُلْ هُوَ اللَّهُ أَحَدٌ"))
        assertTrue(item1.arabicText.contains("قُلْ أَعُوذُ بِرَبِّ الْفَلَقِ"))
        assertTrue(item1.arabicText.contains("قُلْ أَعُوذُ بِرَبِّ النَّاسِ"))
        assertEquals(3, item1.targetCount)

        // 2. Ayat Al-Kursi
        val item2 = bedtimeAzkar.find { it.id == "b_2" }
        assertNotNull(item2)
        assertTrue(item2!!.arabicText.contains("اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ"))
        assertEquals(1, item2.targetCount)

        // 3. Amanar-Rasulu (Baqarah 285-286)
        val item3 = bedtimeAzkar.find { it.id == "b_3" }
        assertNotNull(item3)
        assertTrue(item3!!.arabicText.contains("آمَنَ الرَّسُولُ بِمَا أُنزِلَ إِلَيْهِ"))
        assertEquals(1, item3.targetCount)

        // 4. Bismika Rabbi wada'tu janbi
        val item4 = bedtimeAzkar.find { it.id == "b_4" }
        assertNotNull(item4)
        assertTrue(item4!!.arabicText.contains("بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي"))

        // 6. Allahumma qini 'adhabaka 3x
        val item6 = bedtimeAzkar.find { it.id == "b_6" }
        assertNotNull(item6)
        assertEquals(3, item6!!.targetCount)

        // 8. Tasbih 33 SubhanAllah, 33 Alhamdulillah, 34 Allahu Akbar
        val item8 = bedtimeAzkar.find { it.id == "b_8" }
        assertNotNull(item8)
        assertTrue(item8!!.arabicText.contains("سُبْحَانَ اللَّهِ"))
        assertTrue(item8.arabicText.contains("وَالْحَمْدُ لِلَّهِ"))
        assertTrue(item8.arabicText.contains("وَاللَّهُ أَكْبَرُ"))

        // 12. As-Sajdah and Al-Mulk
        val item12 = bedtimeAzkar.find { it.id == "b_12" }
        assertNotNull(item12)
        assertTrue(item12!!.arabicText.contains("السَّجْدَة"))
        assertTrue(item12.arabicText.contains("الْمُلْكُ"))

        // 13. Allahumma aslamtu nafsi ilayk
        val item13 = bedtimeAzkar.find { it.id == "b_13" }
        assertNotNull(item13)
        assertTrue(item13!!.arabicText.contains("اللَّهُمَّ أَسْلَمْتُ نَفْسِي إِلَيْكَ"))
    }

    @Test
    fun testPrayerTimesCalculator() {
        val today = PrayerTimesCalculator.getTodayPrayerTimes()
        assertNotNull(today)
        assertTrue(today.fajr.azanTime12h.isNotEmpty())
        assertTrue(today.dhuhr.azanTime12h.isNotEmpty())
        assertTrue(today.asr.azanTime12h.isNotEmpty())
        assertTrue(today.maghrib.azanTime12h.isNotEmpty())
        assertTrue(today.isha.azanTime12h.isNotEmpty())
        assertTrue(today.nextPrayerName.isNotEmpty())
        assertEquals(0, today.remainingMillis % 1000L)
    }

    @Test
    fun testPrayerAlarmSchedulerRequestCodes() {
        assertEquals(8801, com.example.util.PrayerAlarmScheduler.REQ_FAJR)
        assertEquals(8802, com.example.util.PrayerAlarmScheduler.REQ_DHUHR)
        assertEquals(8803, com.example.util.PrayerAlarmScheduler.REQ_ASR)
        assertEquals(8804, com.example.util.PrayerAlarmScheduler.REQ_MAGHRIB)
        assertEquals(8805, com.example.util.PrayerAlarmScheduler.REQ_ISHA)
        assertEquals(8899, com.example.util.PrayerAlarmScheduler.REQ_TEST)
        assertEquals("zarayen_prayer_alarms_channel_v4", com.example.util.NotificationHelper.CHANNEL_PRAYER_ID)
        assertEquals("zarayen_ongoing_prayer_channel_v1", com.example.util.NotificationHelper.CHANNEL_ONGOING_ID)
        assertEquals(1001, com.example.util.NotificationHelper.NOTIFICATION_ONGOING_ID)
    }

    @Test
    fun testTripleCalendarConverter() {
        val calendar = CalendarConverter.getCurrentTripleCalendar()
        assertNotNull(calendar)
        assertTrue(calendar.hijri.fullFormatted.isNotEmpty())
        assertTrue(calendar.kurdish.fullFormatted.isNotEmpty())
        assertTrue(calendar.gregorian.fullFormatted.isNotEmpty())
    }

    @Test
    fun testNewsPhotosScreenEnum() {
        val screen = com.example.ui.viewmodel.AppScreen.NEWS_PHOTOS
        assertEquals("NEWS_PHOTOS", screen.name)
        assertTrue(com.example.ui.viewmodel.AppScreen.values().contains(com.example.ui.viewmodel.AppScreen.NEWS_PHOTOS))
        assertTrue(com.example.ui.viewmodel.AppScreen.values().contains(com.example.ui.viewmodel.AppScreen.DASTY2))
    }
}
