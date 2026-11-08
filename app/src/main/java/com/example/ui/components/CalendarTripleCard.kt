package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.util.TripleCalendarData
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepPineGreen
import com.example.ui.theme.WarmAmber

@Composable
fun CalendarTripleCard(
    calendarData: TripleCalendarData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("triple_calendar_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "ڕۆژمێر",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ڕۆژمێری سێینەی زەڕایەن",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = calendarData.gregorian.dayNameKurdish,
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3 Calendar Horizontal Columns
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Gregorian (زایینی)
                SingleCalendarPill(
                    title = "زایینی",
                    day = calendarData.gregorian.dayNumber.toString(),
                    month = calendarData.gregorian.monthNameKurdish,
                    year = calendarData.gregorian.yearNumber.toString(),
                    icon = Icons.Default.WbSunny,
                    accentColor = Color(0xFFD97706),
                    modifier = Modifier.weight(1f)
                )

                // 2. Hijri (کۆچی)
                SingleCalendarPill(
                    title = "کۆچی",
                    day = calendarData.hijri.dayNumber.toString(),
                    month = calendarData.hijri.monthNameKurdish,
                    year = "${calendarData.hijri.yearNumber} ک",
                    icon = Icons.Default.NightsStay,
                    accentColor = DeepPineGreen,
                    modifier = Modifier.weight(1f)
                )

                // 3. Kurdish Solar (کوردی)
                SingleCalendarPill(
                    title = "کوردی",
                    day = calendarData.kurdish.dayNumber.toString(),
                    month = calendarData.kurdish.monthNameKurdish,
                    year = "${calendarData.kurdish.yearNumber} ک",
                    icon = Icons.Default.DateRange,
                    accentColor = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SingleCalendarPill(
    title: String,
    day: String,
    month: String,
    year: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = day,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                text = month,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )

            Text(
                text = year,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
