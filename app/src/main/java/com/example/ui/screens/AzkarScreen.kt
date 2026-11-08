package com.example.ui.screens

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.data.model.DhikrCategory
import com.example.data.model.DhikrItem
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepPineGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState
import com.example.util.ZikrAlarmScheduler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AzkarScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    azkarList: List<DhikrItem>,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val filteredAzkar = remember(azkarList, uiState.selectedAzkarCategory) {
        azkarList.filter { it.category == uiState.selectedAzkarCategory }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "زیکر و تەسبیحات و سوننەتەکان",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("azkar_back_btn")
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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 🔔 Scheduled Zikr Notification Banner & Controls
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("zikr_notifications_banner_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isDarkMode) Color(0xFF1E2824) else Color(0xFFEFF5F1)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
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
                                    color = DeepPineGreen.copy(alpha = 0.15f),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = DeepPineGreen,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "ئاگاداریی زیکرەکان (شارۆچکەی زەڕایەن)",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "ئاگاداری لە کاتی شەرعی بۆ زیکرەکان",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.isZikrNotificationEnabled,
                                onCheckedChange = { viewModel.toggleZikrNotifications(context) },
                                modifier = Modifier.testTag("zikr_master_switch"),
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

                        // 3 Zikr Schedules (Morning Fajr+50m, Evening Asr+30m, Sleep 21:00)
                        val zikrItems = remember(uiState.todayPrayers) {
                            ZikrAlarmScheduler.getZikrScheduleItems(context)
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            zikrItems.forEach { item ->
                                val isThisCategory = when (item.type) {
                                    ZikrAlarmScheduler.TYPE_MORNING -> uiState.selectedAzkarCategory == DhikrCategory.MORNING
                                    ZikrAlarmScheduler.TYPE_EVENING -> uiState.selectedAzkarCategory == DhikrCategory.EVENING
                                    else -> uiState.selectedAzkarCategory == DhikrCategory.BEDTIME
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isThisCategory) DeepPineGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            when (item.type) {
                                                ZikrAlarmScheduler.TYPE_MORNING -> viewModel.selectAzkarCategory(DhikrCategory.MORNING)
                                                ZikrAlarmScheduler.TYPE_EVENING -> viewModel.selectAzkarCategory(DhikrCategory.EVENING)
                                                else -> viewModel.selectAzkarCategory(DhikrCategory.BEDTIME)
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = when (item.type) {
                                                        ZikrAlarmScheduler.TYPE_MORNING -> "🌅 "
                                                        ZikrAlarmScheduler.TYPE_EVENING -> "🌇 "
                                                        else -> "🌙 "
                                                    } + item.titleKurdish,
                                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isThisCategory) DeepPineGreen else MaterialTheme.colorScheme.onSurface
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = AntiqueGold.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        text = item.formattedTime,
                                                        style = MaterialTheme.typography.labelSmall.copy(
                                                            color = if (uiState.isDarkMode) AntiqueGold else Color(0xFF8B6508),
                                                            fontWeight = FontWeight.Bold
                                                        ),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = item.subtitleKurdish,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        FilledTonalButton(
                                            onClick = { viewModel.testZikrNotificationNow(context, item.type) },
                                            modifier = Modifier.testTag("test_zikr_${item.type}_btn"),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "تێست",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "💡 بە دەستنان لە ئاگادارییەکە ڕاستەوخۆ دەچیتە سەر لاپەڕە و بەشی زیکرەکە.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Category Chips Row
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(DhikrCategory.values()) { category ->
                        val isSelected = category == uiState.selectedAzkarCategory
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectAzkarCategory(category) },
                            label = {
                                Text(
                                    text = category.titleKurdish,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DeepPineGreen,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            // Dhikr Items
            if (filteredAzkar.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "هیچ زیکرێک لەم بەشەدا نییە",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredAzkar, key = { it.id }) { item ->
                    DhikrItemCard(
                        item = item,
                        onIncrement = { viewModel.incrementDhikrCount(item) },
                        onReset = { viewModel.resetDhikrProgress(item) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun DhikrItemCard(
    item: DhikrItem,
    onIncrement: () -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("dhikr_card_${item.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Status & Reset
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (item.isCompleted) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "تەواوکراوە",
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "بە تەواوی خوێندراوە (${item.targetCount} جار)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E7D32)
                        )
                    }
                } else {
                    Text(
                        text = "ژمارەی داواکراو: ${item.targetCount} جار",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                IconButton(
                    onClick = onReset,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                        .testTag("reset_dhikr_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "دەستپێکردنەوە",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Arabic Text
            Text(
                text = item.arabicText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 19.sp,
                    lineHeight = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Spacer(modifier = Modifier.height(8.dp))

            // Kurdish Translation
            Text(
                text = item.kurdishTranslation,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (item.benefit.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "فەزڵ و بەڵگە: ${item.benefit}",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Bar
            val progress = if (item.targetCount > 0) {
                (item.currentCount.toFloat() / item.targetCount).coerceIn(0f, 1f)
            } else 0f

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (item.isCompleted) Color(0xFF2E7D32) else AntiqueGold,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Big Interactive Tap Counter Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onIncrement() }
                    .testTag("tap_dhikr_${item.id}"),
                color = if (item.isCompleted) Color(0xFF2E7D32).copy(alpha = 0.15f) else DeepPineGreen,
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = "ژماردن",
                        tint = if (item.isCompleted) Color(0xFF2E7D32) else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.currentCount} / ${item.targetCount}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = if (item.isCompleted) Color(0xFF2E7D32) else Color.White
                    )
                }
            }
        }
    }
}
