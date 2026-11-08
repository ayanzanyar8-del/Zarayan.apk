package com.example.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerTimeItem
import com.example.data.util.PrayerTimesCalculator
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepPineGreen
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    onNavigateBack: () -> Unit,
    onNavigateToQibla: () -> Unit,
    modifier: Modifier = Modifier
) {
    val monthSchedule = remember(uiState.selectedMonthForCalendar, uiState.selectedYearForCalendar) {
        PrayerTimesCalculator.getMonthPrayerTimes(uiState.selectedYearForCalendar, uiState.selectedMonthForCalendar)
    }

    val monthNamesKurdish = listOf(
        "کانوونی دووەم (١)", "شوبات (٢)", "ئازار (٣)", "نیسان (٤)", "ئایار (٥)", "حوزەیران (٦)",
        "تەممووز (٧)", "ئاب (٨)", "ئەیلوول (٩)", "تشرینی یەکەم (١٠)", "تشرینی دووەم (١١)", "کانوونی یەکەم (١٢)"
    )

    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(com.example.util.NotificationHelper.isNotificationPermissionGranted(context))
    }

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
        if (isGranted) {
            viewModel.showSnackbar("مۆڵەتی ئاگادارییەکان درا بە سەرکەوتوویی")
            if (!uiState.isEarlyAlertEnabled) {
                viewModel.toggleEarlyAlert()
            }
        } else {
            viewModel.showSnackbar("مۆڵەتی ئاگادارییەکان ڕەتکرایەوە لەلایەن بەکارهێنەرەوە")
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "کاتەکانی بانگ - شارۆچکەی زەڕایەن",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("prayer_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "گەڕانەوە"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Next Prayer Countdown Hero Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("prayer_countdown_card"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepPineGreen),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "بانگی داهاتوو: ${uiState.todayPrayers.nextPrayerNameKurdish}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = uiState.todayPrayers.timeRemainingFormatted,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 2.sp
                            ),
                            color = AntiqueGold
                        )

                        Text(
                            text = "کاتی بانگی زەڕایەن: ${uiState.todayPrayers.nextPrayerTimeFormatted}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            // 2. Early Notification Setting Toggle & Test Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (uiState.isEarlyAlertEnabled && hasNotificationPermission) AntiqueGold.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isEarlyAlertEnabled && hasNotificationPermission) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                        contentDescription = null,
                                        tint = if (uiState.isEarlyAlertEnabled && hasNotificationPermission) AntiqueGold else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "زەنگ و ئاگاداری لە کاتی بانگ",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (uiState.isEarlyAlertEnabled) "ئاگادارکردنەوە و زەنگ لە ساتی دروست لە هەر ٥ بانگەکەدا چالاکە" else "ئاگادارکردنەوە ناچالاکە",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (uiState.isEarlyAlertEnabled) DeepPineGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.isEarlyAlertEnabled,
                                onCheckedChange = {
                                    if (!uiState.isEarlyAlertEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.toggleEarlyAlert()
                                    }
                                },
                                modifier = Modifier.testTag("early_alert_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DeepPineGreen
                                )
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )

                        // Always-On Notification in Status Bar Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (uiState.isAlwaysOnNotificationEnabled) DeepPineGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = if (uiState.isAlwaysOnNotificationEnabled) DeepPineGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "ئاگاداریی هەمیشەیی (Always On)",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (uiState.isAlwaysOnNotificationEnabled) DeepPineGreen else MaterialTheme.colorScheme.surfaceVariant
                                        ) {
                                            Text(
                                                text = if (uiState.isAlwaysOnNotificationEnabled) "چالاکە" else "ناچالاکە",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = if (uiState.isAlwaysOnNotificationEnabled) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "ئاگاداریی بەردەوام و نەبڕاوە لە شریتی سەرەوە بۆ کاتی بانگی داهاتوو و ژماردنی کات",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.isAlwaysOnNotificationEnabled,
                                onCheckedChange = {
                                    if (!uiState.isAlwaysOnNotificationEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.toggleAlwaysOnNotification(context)
                                    }
                                },
                                modifier = Modifier.testTag("always_on_notif_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DeepPineGreen
                                )
                            )
                        }

                        // Permission alert banner if notifications are blocked
                        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "مۆڵەتی ئاگاداری نەدراوە! کلیک لێرە بکە بۆ پێدانی مۆڵەت",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                            }
                                    )
                                }
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )

                        // Action Buttons: Instant Test Alarm & FCM Test
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotificationPermission) {
                                        notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.testPrayerNotificationNow(context)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepPineGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("test_prayer_notification_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "تێستی دەستبەجێی زەنگی بانگ",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }

                            OutlinedButton(
                                onClick = { viewModel.sendTestFcmPushNotification(context, isPrayerAlert = true) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("fcm_test_prayer_alert_btn")
                            ) {
                                Text(
                                    text = "تێستی FCM",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Today's All 6 Prayer Times Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Mosque,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "کاتەکانی نوێژی ئەمڕۆی زەڕایەن",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "قامەت (دەستپێک)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        uiState.todayPrayers.prayers.forEachIndexed { index, prayer ->
                            PrayerDetailRow(prayer = prayer)
                            if (index < uiState.todayPrayers.prayers.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }

            // 4. Qibla Direction Shortcut Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToQibla() }
                        .testTag("qibla_banner_shortcut"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
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
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CompassCalibration,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "ئاراستەی قیبلە لە زەڕایەن",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Text(
                                    text = "پلەی ٢٠٨° (باشووری ڕۆژئاوا)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondary
                        ) {
                            Text(
                                text = "کردنەوەی قیبلەنما",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 5. Monthly Schedule Title & Month Selector Chips
            item {
                Column {
                    Text(
                        text = "خشتەی مانگانەی بانگەکان بۆ زەڕایەن",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(monthNamesKurdish.indices.toList()) { index ->
                            val monthNum = index + 1
                            val isSelected = monthNum == uiState.selectedMonthForCalendar
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.selectCalendarMonth(monthNum) },
                                label = {
                                    Text(
                                        text = monthNamesKurdish[index],
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = DeepPineGreen,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // 6. Monthly Schedule Table Header & Rows
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Table header
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TableHeaderCell("ڕۆژ", 0.8f)
                            TableHeaderCell("بەیانی", 1.1f)
                            TableHeaderCell("نیوەڕۆ", 1.1f)
                            TableHeaderCell("عەسر", 1.1f)
                            TableHeaderCell("ئێوارە", 1.1f)
                            TableHeaderCell("خەوتن", 1.1f)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Table rows (all days of the selected month)
                        val todayCalendar = remember { Calendar.getInstance(PrayerTimesCalculator.IRAQ_TIMEZONE) }
                        val currentDay = todayCalendar.get(Calendar.DAY_OF_MONTH)
                        val currentMonth = todayCalendar.get(Calendar.MONTH) + 1
                        val isCurrentMonthSelected = uiState.selectedMonthForCalendar == currentMonth

                        monthSchedule.forEachIndexed { idx, dayPrayer ->
                            val isToday = isCurrentMonthSelected && dayPrayer.day == currentDay
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        when {
                                            isToday -> DeepPineGreen.copy(alpha = 0.2f)
                                            idx % 2 == 0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                            else -> Color.Transparent
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    )
                                    .padding(vertical = 7.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 2.dp)
                                ) {
                                    if (isToday) {
                                        Surface(
                                            shape = CircleShape,
                                            color = AntiqueGold,
                                            modifier = Modifier.size(6.dp)
                                        ) {}
                                        Spacer(modifier = Modifier.width(3.dp))
                                    }
                                    TableDataCell(
                                        text = "${dayPrayer.day}",
                                        weight = 0.8f,
                                        isBold = isToday,
                                        textColor = if (isToday) DeepPineGreen else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                TableDataCell(PrayerTimesCalculator.format24Hour(dayPrayer.fajrMinutes), 1.1f, isBold = isToday)
                                TableDataCell(PrayerTimesCalculator.format24Hour(dayPrayer.dhuhrMinutes), 1.1f, isBold = isToday)
                                TableDataCell(PrayerTimesCalculator.format24Hour(dayPrayer.asrMinutes), 1.1f, isBold = isToday)
                                TableDataCell(PrayerTimesCalculator.format24Hour(dayPrayer.maghribMinutes), 1.1f, isBold = isToday)
                                TableDataCell(PrayerTimesCalculator.format24Hour(dayPrayer.ishaMinutes), 1.1f, isBold = isToday)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun PrayerDetailRow(prayer: PrayerTimeItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (prayer.isNext) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = CircleShape,
                color = if (prayer.isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = when (prayer.nameKurdish) {
                        "خۆرهەڵاتن" -> Icons.Default.WbSunny
                        else -> Icons.Default.AccessTime
                    },
                    contentDescription = null,
                    tint = if (prayer.isNext) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(7.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = prayer.nameKurdish,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = if (prayer.isNext) FontWeight.ExtraBold else FontWeight.Bold
                        ),
                        color = if (prayer.isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${prayer.nameArabic})",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                if (prayer.isNext) {
                    Text(
                        text = "بانگی داهاتوو",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = prayer.formatted12Hours,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = if (prayer.isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (prayer.iqamahTime12h != "-") {
                    val offsetText = if (prayer.qamatMinutesOffset > 0) " (+${prayer.qamatMinutesOffset} دەقە)" else ""
                    Text(
                        text = "قامەت$offsetText: ${prayer.iqamahTime12h}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                        color = AntiqueGold
                    )
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onPrimaryContainer,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}

@Composable
private fun TableDataCell(
    text: String,
    weight: Float,
    isBold: Boolean = false,
    textColor: Color = Color.Unspecified
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            fontFamily = FontFamily.Monospace
        ),
        color = if (textColor != Color.Unspecified) textColor else MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 2.dp)
    )
}
