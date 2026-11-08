package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WindPower
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.JanazaAnnouncement
import com.example.data.model.PrayerTimeItem
import com.example.ui.components.AnalogDigitalClock
import com.example.ui.components.CalendarTripleCard
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepPineGreen
import com.example.ui.theme.WarmAmber
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    latestJanaza: JanazaAnnouncement?,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        // 1. Top App Bar & Identity Header
        item {
            TownHeaderCard(
                uiState = uiState,
                onToggleDark = { viewModel.toggleDarkMode() },
                onAdminClick = { viewModel.openAdminLoginDialog() },
                onOpenAdminUsers = { onNavigate(AppScreen.ADMIN_USERS) }
            )
        }

        // Admin Special Access Banner (if logged in as admin)
        if (uiState.isAdmin) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(AppScreen.ADMIN_USERS) }
                        .testTag("admin_users_top_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = AntiqueGold.copy(alpha = 0.15f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = AntiqueGold,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = "ئەندامان",
                                    tint = Color.Black,
                                    modifier = Modifier.padding(7.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "بەشی ئەدمین: لیستی تۆماربووان",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "بینین و بەڕێوەبردنی تەواوی کەسانی تۆماربووی ئەپ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AntiqueGold.copy(alpha = 0.3f)
                        ) {
                            Text(
                                text = "بینین",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // 2. Analog & Digital Clock
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("clock_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnalogDigitalClock(
                        hours = uiState.clockHours,
                        minutes = uiState.clockMinutes,
                        seconds = uiState.clockSeconds,
                        digitalTimeStr = uiState.digitalTimeStr
                    )
                }
            }
        }

        // 3. Triple Calendar Card (زایینی، کۆچی، کوردی)
        item {
            CalendarTripleCard(calendarData = uiState.tripleCalendar)
        }

        // 4. Latest Janaza Banner (Directly links to live site)
        item {
            JanazaAlertBanner(
                announcement = latestJanaza,
                onOpenJanaza = { onNavigate(AppScreen.JANAZA) }
            )
        }

        // Live Location Sharing Banner (شوێنی ئێستام)
        item {
            LiveLocationBanner(
                onOpenLiveLocation = { onNavigate(AppScreen.LIVE_LOCATION) }
            )
        }

        // 5. Next Prayer Countdown Card
        item {
            NextPrayerCountdownCard(
                uiState = uiState,
                onViewAllPrayers = { onNavigate(AppScreen.PRAYER_TIMES) }
            )
        }

        // 6. Today's 6 Prayer Times Horizontal Strip
        item {
            TodayPrayersStrip(
                prayers = uiState.todayPrayers.prayers,
                onViewDetails = { onNavigate(AppScreen.PRAYER_TIMES) }
            )
        }

        // 7. Quick Navigation Grid (باسی سەرەکی و تایبەتمەندییەکان)
        item {
            QuickNavigationGrid(onNavigate = onNavigate)
        }

        // 8. Zarayen Weather Card
        item {
            ZarayenWeatherCard(weather = uiState.weather)
        }

        // 9. Official Social & Map Links
        item {
            OfficialSocialLinksCard(
                onOpenUrl = { url -> viewModel.openUrl(context, url) }
            )
        }

        item { Spacer(modifier = Modifier.height(28.dp)) }
    }
}

@Composable
private fun TownHeaderCard(
    uiState: UiState,
    onToggleDark: () -> Unit,
    onAdminClick: () -> Unit,
    onOpenAdminUsers: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .size(46.dp)
                        .border(1.5.dp, AntiqueGold, CircleShape),
                    shadowElevation = 2.dp
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.zarayen_new_logo_1787987620082),
                        contentDescription = "لۆگۆی زەڕایەن",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "شارۆچکەی زەڕایەن",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "زەڕایەن نیوز • قەزای شارەزوور",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Theme switcher
                IconButton(
                    onClick = onToggleDark,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (uiState.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "گۆڕینی ڕەنگ",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Admin Status Badge
                IconButton(
                    onClick = {
                        if (uiState.isAdmin) {
                            onOpenAdminUsers()
                        } else {
                            onAdminClick()
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isAdmin) AntiqueGold.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                        .testTag("admin_badge_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "ئەدمین",
                        tint = if (uiState.isAdmin) AntiqueGold else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun JanazaAlertBanner(
    announcement: JanazaAnnouncement?,
    onOpenJanaza: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenJanaza() }
            .testTag("home_janaza_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2B1F17)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AntiqueGold,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "نوێژی مردوو",
                    tint = Color(0xFF281C14),
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ئاگاداری نوێژی مردوو",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = AntiqueGold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "بۆ ئاگاداربوون لە نوێژی مردوو کلیک لێرە بکە",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "بینین لە ماڵپەڕ",
                tint = AntiqueGold,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun LiveLocationBanner(
    onOpenLiveLocation: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenLiveLocation() }
            .testTag("home_live_location_banner"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF00695C)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = AntiqueGold,
                modifier = Modifier.size(42.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "شوێنی ئێستام",
                    tint = Color.Black,
                    modifier = Modifier.padding(9.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "شوێنی ئێستام (Live Location)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AntiqueGold.copy(alpha = 0.3f)
                    ) {
                        Text(
                            text = "نوێ",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = AntiqueGold
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "هاوبەشکردنی شوێنی ڕاستەوخۆ بۆ ٤ تا ٢٤ کاتژمێر و نەخشەی ئۆنلاین",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "کردنەوە",
                tint = AntiqueGold,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun NextPrayerCountdownCard(
    uiState: UiState,
    onViewAllPrayers: () -> Unit
) {
    val prayersData = uiState.todayPrayers

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewAllPrayers() }
            .testTag("next_prayer_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = DeepPineGreen
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "کاتی بانگ",
                        tint = AntiqueGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "بانگی داهاتوو: ${prayersData.nextPrayerNameKurdish}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.White.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "هەموو کاتەکان",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "کاتی ماوە بۆ بانگ:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = prayersData.timeRemainingFormatted,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.5.sp
                        ),
                        color = AntiqueGold
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "کاتی بانگی زەڕایەن:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = prayersData.nextPrayerTimeFormatted,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }
            }

            if (prayersData.activeQamatStatus.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AntiqueGold.copy(alpha = 0.25f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = prayersData.activeQamatStatus,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AntiqueGold
                        )
                        Text(
                            text = "قامەت: ${prayersData.nextQamatTimeFormatted}",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayPrayersStrip(
    prayers: List<PrayerTimeItem>,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewDetails() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "کاتی ٦ نوێژەکەی ئەمڕۆ لە زەڕایەن",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                prayers.forEach { prayer ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (prayer.isNext) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                            )
                            .padding(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = prayer.nameKurdish,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (prayer.isNext) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = if (prayer.isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = prayer.formatted12Hours,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickNavigationGrid(
    onNavigate: (AppScreen) -> Unit
) {
    Column {
        Text(
            text = "خزمەتگوزاری و بەشەکانی ئەپ",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
        )

        val navItems = listOf(
            NavShortcut("کاتی بانگەکان", Icons.Default.Schedule, DeepPineGreen, AppScreen.PRAYER_TIMES),
            NavShortcut("شوێنی ئێستام (Live)", Icons.Default.MyLocation, Color(0xFF00897B), AppScreen.LIVE_LOCATION),
            NavShortcut("وێنە و هەواڵ", Icons.Default.Newspaper, Color(0xFFC2185B), AppScreen.NEWS_PHOTOS),
            NavShortcut("نوێژی مردوو", Icons.Default.Campaign, Color(0xFF8B4513), AppScreen.JANAZA),
            NavShortcut("دەستی دوو", Icons.Default.ShoppingBag, Color(0xFFE65100), AppScreen.DASTY2),
            NavShortcut("نرخی دراو و زێڕ", Icons.Default.CurrencyExchange, Color(0xFF00796B), AppScreen.CURRENCY),
            NavShortcut("قورئانی پیرۆز", Icons.Default.MenuBook, Color(0xFF2E7D32), AppScreen.QURAN_LIST),
            NavShortcut("زیکر و تەسبیح", Icons.Default.Bookmark, Color(0xFF4A6B82), AppScreen.AZKAR),
            NavShortcut("ناوەکانی خوای گەورە (٩٩)", Icons.Default.AutoAwesome, AntiqueGold, AppScreen.NAMES_OF_ALLAH),
            NavShortcut("قیبلەنما", Icons.Default.CompassCalibration, Color(0xFFD97706), AppScreen.QIBLA),
            NavShortcut("لیستی ئەندامان (ئەدمین)", Icons.Default.Group, Color(0xFF1E3A8A), AppScreen.ADMIN_USERS),
            NavShortcut("ڕێبەری شوێنەکان", Icons.Default.Place, Color(0xFF1976D2), AppScreen.PLACES),
            NavShortcut("دەربارەی زەڕایەن", Icons.Default.Info, Color(0xFF6D4C41), AppScreen.ABOUT_ZARAYEN)
        )

        // 4x2 Grid (2 columns for clean, large, readable cards)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in navItems.indices step 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (j in 0 until 2) {
                        if (i + j < navItems.size) {
                            val item = navItems[i + j]
                            QuickNavTile(
                                item = item,
                                onClick = { onNavigate(item.screen) },
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

private data class NavShortcut(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val screen: AppScreen
)

@Composable
private fun QuickNavTile(
    item: NavShortcut,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(84.dp)
            .clickable { onClick() }
            .testTag("nav_tile_${item.screen.name.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = item.color.copy(alpha = 0.15f),
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.color,
                    modifier = Modifier.padding(7.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ZarayenWeatherCard(
    weather: com.example.data.model.ZarayenWeather
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.WbSunny,
                        contentDescription = "کەشناسی",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "کەشوهەوای زەڕایەن",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${weather.conditionKurdish} • ${weather.temperatureC}°C",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = "شێ",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${weather.humidity}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.WindPower,
                        contentDescription = "با",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${weather.windSpeedKmH} km/h",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun OfficialSocialLinksCard(
    onOpenUrl: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .size(36.dp)
                        .border(1.dp, AntiqueGold, CircleShape),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.zarayen_new_logo_1787987620082),
                        contentDescription = "Zarayen News Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "زەڕایەن نیوز • سەرچاوە و پەڕە فەرمییەکان",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "دەنگی ڕاستەقینەی شارۆچکەی زەڕایەن و دەشتی شارەزوور",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Facebook
                OutlinedButton(
                    onClick = { onOpenUrl("https://www.facebook.com/share/19DjMcB7nF/") },
                    modifier = Modifier.weight(1f).testTag("facebook_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("فەیسبووک", style = MaterialTheme.typography.labelSmall)
                }

                // TikTok Zarayen News
                OutlinedButton(
                    onClick = { onOpenUrl("https://www.tiktok.com/@zarayannews?_r=1&_t=ZS-98uRiXqxrYf") },
                    modifier = Modifier.weight(1f).testTag("tiktok_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("تیکتۆک", style = MaterialTheme.typography.labelSmall)
                }

                // Maps
                OutlinedButton(
                    onClick = { onOpenUrl("https://maps.app.goo.gl/VnyXna76pJKSCxyw6") },
                    modifier = Modifier.weight(1f).testTag("maps_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("نەخشە", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

