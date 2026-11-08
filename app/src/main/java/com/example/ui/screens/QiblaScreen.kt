package com.example.ui.screens

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepEspressoBrown
import com.example.ui.theme.DeepPineGreen
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QiblaScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var azimuth by remember { mutableFloatStateOf(0f) }

    // Sensor listener for orientation/compass
    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        val listener = object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val orientation = FloatArray(3)

            override fun onSensorChanged(event: SensorEvent?) {
                if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientation)
                    val azimuthInDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
                    azimuth = (azimuthInDegrees + 360) % 360
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        if (rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    // Zarayen Qibla Bearing is ~208.4 degrees (South-Southwest)
    val qiblaAngleZarayen = 208.4f
    val smoothAzimuth by animateFloatAsState(targetValue = azimuth, label = "azimuth")

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "قیبلەنمای شارۆچکەی زەڕایەن",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("qibla_back_btn")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Location Info Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepPineGreen)
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
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mosque,
                                contentDescription = "کەعبەی پیرۆز",
                                tint = DeepEspressoBrown,
                                modifier = Modifier.padding(8.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "ئاراستەی کەعبەی پیرۆز لە زەڕایەن",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "پلە: ٢٠٨.٤° (باشووری ڕۆژئاوا) • دووری: ١,٨٢٠ کم",
                                style = MaterialTheme.typography.bodySmall,
                                color = AntiqueGold
                            )
                        }
                    }
                }
            }

            // Big Custom Compass Canvas Dial
            item {
                Card(
                    modifier = Modifier
                        .size(290.dp)
                        .testTag("qibla_compass_card"),
                    shape = CircleShape,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                            val centerX = size.width / 2f
                            val centerY = size.height / 2f
                            val radius = size.width / 2f

                            // Draw Compass Outer Ring
                            drawCircle(
                                color = Color(0xFF1B3D2F),
                                radius = radius,
                                style = Stroke(width = 3.dp.toPx())
                            )
                            drawCircle(
                                color = Color(0xFFC5952B).copy(alpha = 0.3f),
                                radius = radius - 8.dp.toPx(),
                                style = Stroke(width = 1.dp.toPx())
                            )

                            // Rotate dial according to device heading
                            rotate(-smoothAzimuth, pivot = Offset(centerX, centerY)) {
                                // Draw 12 Ticks
                                for (i in 0 until 12) {
                                    val angleRad = Math.toRadians((i * 30).toDouble())
                                    val tickLength = if (i % 3 == 0) 14.dp.toPx() else 8.dp.toPx()
                                    val startX = centerX + (radius - tickLength) * sin(angleRad).toFloat()
                                    val startY = centerY - (radius - tickLength) * cos(angleRad).toFloat()
                                    val endX = centerX + radius * sin(angleRad).toFloat()
                                    val endY = centerY - radius * cos(angleRad).toFloat()

                                    drawLine(
                                        color = if (i % 3 == 0) Color(0xFFC5952B) else Color(0xFF8D8276),
                                        start = Offset(startX, startY),
                                        end = Offset(endX, endY),
                                        strokeWidth = if (i % 3 == 0) 2.5.dp.toPx() else 1.5.dp.toPx()
                                    )
                                }

                                // Draw North Marker (Red triangle)
                                val northPath = Path().apply {
                                    moveTo(centerX, centerY - radius + 18.dp.toPx())
                                    lineTo(centerX - 8.dp.toPx(), centerY - radius + 32.dp.toPx())
                                    lineTo(centerX + 8.dp.toPx(), centerY - radius + 32.dp.toPx())
                                    close()
                                }
                                drawPath(northPath, color = Color(0xFFE53935))

                                // Draw Qibla Pointer Indicator (Golden Kaaba direction at 208.4 deg)
                                rotate(qiblaAngleZarayen, pivot = Offset(centerX, centerY)) {
                                    val qiblaPath = Path().apply {
                                        moveTo(centerX, centerY - radius + 12.dp.toPx())
                                        lineTo(centerX - 10.dp.toPx(), centerY - radius + 36.dp.toPx())
                                        lineTo(centerX + 10.dp.toPx(), centerY - radius + 36.dp.toPx())
                                        close()
                                    }
                                    drawPath(qiblaPath, color = Color(0xFFC5952B))

                                    // Kaaba Line to center
                                    drawLine(
                                        color = Color(0xFFC5952B),
                                        start = Offset(centerX, centerY - radius + 36.dp.toPx()),
                                        end = Offset(centerX, centerY),
                                        strokeWidth = 2.dp.toPx()
                                    )
                                }
                            }

                            // Center Pivot Circle
                            drawCircle(
                                color = Color(0xFF1B3D2F),
                                radius = 12.dp.toPx()
                            )
                            drawCircle(
                                color = Color(0xFFC5952B),
                                radius = 6.dp.toPx()
                            )
                        }

                        // Text overlay in Center Bottom
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 130.dp)
                        ) {
                            Text(
                                text = "ڕووگەی کەعبە",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = AntiqueGold
                            )
                            Text(
                                text = "208°",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Calibration Guide Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CompassCalibration,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ڕێنمایی بۆ دۆزینەوەی وردی قیبلە",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "١. مۆبایلەکەت بە تەختی (ئاسۆیی) لەسەر دەستت دابنێ.\n" +
                                   "٢. لە کەرەستە و شوێنی ئاسن و موگناتیسی دوور بکەوە.\n" +
                                   "٣. مۆبایلەکەت بە شێوەی ژمارە (8) لە هەوادا بسووڕێنە بۆ کالیبرەیکردنی هەستەوەرەکان.\n" +
                                   "٤. کاتێک سەری نیشاندەرە زێڕینەکە ڕووەو سەرەوە بوو، ئەوە ئاراستەی تەواوی کەعبەی پیرۆزە.",
                            style = MaterialTheme.typography.bodySmall.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
