package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepPineGreen
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AnalogDigitalClock(
    hours: Float,
    minutes: Float,
    seconds: Float,
    digitalTimeStr: String,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.red < 0.3f
    val ringColor = if (isDark) Color(0xFF383227) else Color(0xFFE8DFD1)
    val numberColor = if (isDark) Color(0xFFFFFAEE) else Color(0xFF281C14)
    val hourHandColor = if (isDark) Color(0xFFFFF7ED) else Color(0xFF1E3A2B)
    val minuteHandColor = if (isDark) Color(0xFF68D391) else DeepPineGreen
    val secondHandColor = AntiqueGold
    val centerDotColor = AntiqueGold

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Outer Shadowed Clock Frame with Zarayen Panorama Background
        Box(
            modifier = Modifier
                .size(236.dp)
                .shadow(16.dp, CircleShape, spotColor = Color(0x40000000))
                .clip(CircleShape)
                .border(6.dp, Brush.sweepGradient(listOf(AntiqueGold, ringColor, AntiqueGold)), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // 1. Zarayen Photographic Background Image
            Image(
                painter = painterResource(id = R.drawable.img_zarayen_clock_bg_1787773131632),
                contentDescription = "دیمەنی شارۆچکەی زەڕایەن و ئاڵای کوردستان",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // 2. Translucent Aesthetic Scrim for Contrast & Readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                (if (isDark) Color(0x99000000) else Color(0x66FFFFFF)),
                                (if (isDark) Color(0xCC110E0A) else Color(0xAAEDE2CD))
                            )
                        )
                    )
            )

            // 3. Hands and Numerals Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(6.dp)
            ) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.minDimension / 2f

                // Draw 60 Ticks & 12 Numerals
                drawClockTicksAndNumerals(
                    center = center,
                    radius = radius,
                    numberColor = numberColor,
                    isDark = isDark
                )

                // Hour Hand Shadow & Hand
                val hourAngle = ((hours % 12) / 12f * 360f) - 90f
                val hourLength = radius * 0.52f
                val hourEnd = Offset(
                    center.x + hourLength * cos(degToRad(hourAngle)).toFloat(),
                    center.y + hourLength * sin(degToRad(hourAngle)).toFloat()
                )

                // Hour shadow
                drawLine(
                    color = Color.Black.copy(alpha = 0.35f),
                    start = center + Offset(2f, 2f),
                    end = hourEnd + Offset(2f, 2f),
                    strokeWidth = 7.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Hour hand
                drawLine(
                    color = hourHandColor,
                    start = center - Offset(
                        radius * 0.1f * cos(degToRad(hourAngle)).toFloat(),
                        radius * 0.1f * sin(degToRad(hourAngle)).toFloat()
                    ),
                    end = hourEnd,
                    strokeWidth = 6.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Minute Hand Shadow & Hand
                val minuteAngle = (minutes / 60f * 360f) - 90f
                val minuteLength = radius * 0.73f
                val minuteEnd = Offset(
                    center.x + minuteLength * cos(degToRad(minuteAngle)).toFloat(),
                    center.y + minuteLength * sin(degToRad(minuteAngle)).toFloat()
                )

                // Minute shadow
                drawLine(
                    color = Color.Black.copy(alpha = 0.35f),
                    start = center + Offset(2f, 2f),
                    end = minuteEnd + Offset(2f, 2f),
                    strokeWidth = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
                // Minute hand
                drawLine(
                    color = minuteHandColor,
                    start = center - Offset(
                        radius * 0.12f * cos(degToRad(minuteAngle)).toFloat(),
                        radius * 0.12f * sin(degToRad(minuteAngle)).toFloat()
                    ),
                    end = minuteEnd,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Second Hand
                val secondAngle = (seconds / 60f * 360f) - 90f
                val secondLength = radius * 0.86f
                val secondEnd = Offset(
                    center.x + secondLength * cos(degToRad(secondAngle)).toFloat(),
                    center.y + secondLength * sin(degToRad(secondAngle)).toFloat()
                )
                drawLine(
                    color = secondHandColor,
                    start = center - Offset(
                        radius * 0.18f * cos(degToRad(secondAngle)).toFloat(),
                        radius * 0.18f * sin(degToRad(secondAngle)).toFloat()
                    ),
                    end = secondEnd,
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Center Cap
                drawCircle(
                    color = centerDotColor,
                    radius = 6.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = if (isDark) Color(0xFF141311) else Color.White,
                    radius = 2.5.dp.toPx(),
                    center = center
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Digital Time Display Badge
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp,
            modifier = Modifier.border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = digitalTimeStr,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "کاتی فەرمی زەڕایەن",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun DrawScope.drawClockTicksAndNumerals(
    center: Offset,
    radius: Float,
    numberColor: Color,
    isDark: Boolean
) {
    // 60 Ticks
    for (i in 0 until 60) {
        val angle = (i / 60f * 360f) - 90f
        val isHourTick = i % 5 == 0
        val tickLength = if (isHourTick) radius * 0.12f else radius * 0.05f
        val tickWidth = if (isHourTick) 2.5.dp.toPx() else 1.dp.toPx()
        val tickColor = if (isHourTick) AntiqueGold else if (isDark) Color(0xFF5A5245) else Color(0xFFBFB4A2)

        val outerPoint = Offset(
            center.x + (radius - 8.dp.toPx()) * cos(degToRad(angle)).toFloat(),
            center.y + (radius - 8.dp.toPx()) * sin(degToRad(angle)).toFloat()
        )
        val innerPoint = Offset(
            center.x + (radius - 8.dp.toPx() - tickLength) * cos(degToRad(angle)).toFloat(),
            center.y + (radius - 8.dp.toPx() - tickLength) * sin(degToRad(angle)).toFloat()
        )

        drawLine(
            color = tickColor,
            start = innerPoint,
            end = outerPoint,
            strokeWidth = tickWidth,
            cap = StrokeCap.Round
        )
    }

    // 12 Numerals: 12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11
    val numeralStrings = listOf("12", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11")
    val textPaint = android.graphics.Paint().apply {
        color = numberColor.toArgb()
        textSize = (radius * 0.18f)
        textAlign = android.graphics.Paint.Align.CENTER
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        isAntiAlias = true
    }

    drawIntoCanvas { canvas ->
        for (i in 0 until 12) {
            val angle = (i / 12f * 360f) - 90f
            val numRadius = radius * 0.70f
            val x = center.x + numRadius * cos(degToRad(angle)).toFloat()
            // Adjust vertical centering for font baseline
            val y = center.y + numRadius * sin(degToRad(angle)).toFloat() + (textPaint.textSize / 2.8f)

            canvas.nativeCanvas.drawText(
                numeralStrings[i],
                x,
                y,
                textPaint
            )
        }
    }
}

private fun degToRad(deg: Float): Double = deg * PI / 180.0
