package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.GpsOff
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PersonPinCircle
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepPineGreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiState
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * پەڕەی شوێنی ئێستام (Live Location Sharing)
 * - نەخشەی ڕاستەوخۆ و ئەنیمەیشنداری ناوخۆیی (Interactive Native Radar & Map) بەبێ شاشەی سپی
 * - بەستەری فەرمی Google Maps کە ڕاستەوخۆ شوێنەکە دەکاتەوە لە کاتی ناردنی بەستەردا
 * - بەستەری داگرتنی ڕاستەوخۆی فایلی APKـی ئەپ بۆ هاوڕێیان
 * - دوگمەی هاوبەشکردن بۆ واتسئەپ، تێلیگرام، ماسنجەر، ڤایبەر
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveLocationScreen(
    viewModel: MainViewModel,
    uiState: UiState,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val livePrefs = remember { context.getSharedPreferences("zarayen_live_location_prefs", Context.MODE_PRIVATE) }
    val appPrefs = remember { context.getSharedPreferences("zarayen_app_prefs", Context.MODE_PRIVATE) }
    val locationManager = remember { context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager }

    // Direct APK download link for friends
    val apkDownloadUrl = appPrefs.getString(
        "custom_apk_download_url",
        "https://temp.sh/pAtnm/Zarayen_App_Installer.apk"
    ) ?: "https://temp.sh/pAtnm/Zarayen_App_Installer.apk"

    // Check location permissions
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Check if hardware GPS is enabled
    var isGpsHardwareEnabled by remember {
        mutableStateOf(locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true)
    }

    // Live Device Coordinates (Zarayen town center: 35.2934, 45.6982)
    var currentLatitude by remember { mutableDoubleStateOf(35.2934) }
    var currentLongitude by remember { mutableDoubleStateOf(45.6982) }
    var locationAccuracy by remember { mutableFloatStateOf(0f) }
    var locationSpeed by remember { mutableFloatStateOf(0f) }
    var locationAltitude by remember { mutableDoubleStateOf(0.0) }
    var isLocating by remember { mutableStateOf(true) }
    var isGpsFixAccurate by remember { mutableStateOf(false) }
    var lastUpdatedTimeStr by remember { mutableStateOf("چاوەڕوانی GPS...") }

    // Live Sharing State
    var isSharingActive by remember {
        mutableStateOf(livePrefs.getBoolean("is_live_sharing_active", false))
    }
    var selectedDurationHours by remember {
        mutableIntStateOf(livePrefs.getInt("live_sharing_duration_hours", 24))
    }
    var sharingExpiresAt by remember {
        mutableLongStateOf(livePrefs.getLong("live_sharing_expires_at", 0L))
    }
    var remainingTimeFormatted by remember { mutableStateOf("") }

    // Map Display Mode: 0 = Native Radar & Map (100% reliable, never white), 1 = Web Live Map
    var mapMode by remember { mutableIntStateOf(0) }
    var mapZoomLevel by remember { mutableFloatStateOf(1f) }

    // Shared location from a friend
    val friendName = uiState.sharedLocationSenderName
    val friendLat = uiState.sharedLocationLat
    val friendLng = uiState.sharedLocationLng
    val isViewingFriend = friendLat != null && friendLng != null

    // Effective display coordinates
    val displayLat = if (isViewingFriend) friendLat!! else currentLatitude
    val displayLng = if (isViewingFriend) friendLng!! else currentLongitude

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val fineGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = fineGranted || coarseGranted
        isGpsHardwareEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true
    }

    // Auto-request permission on screen entry if missing
    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Distance to friend
    val distanceToFriendMeters = remember(currentLatitude, currentLongitude, friendLat, friendLng) {
        if (isViewingFriend && friendLat != null && friendLng != null) {
            val results = FloatArray(1)
            Location.distanceBetween(currentLatitude, currentLongitude, friendLat, friendLng, results)
            results[0]
        } else null
    }

    BackHandler {
        if (isViewingFriend) {
            viewModel.clearSharedLocation()
        } else {
            onNavigateBack()
        }
    }

    // GPS Listener for 100% accurate fix
    DisposableEffect(hasLocationPermission) {
        if (!hasLocationPermission || locationManager == null) {
            return@DisposableEffect onDispose {}
        }

        isGpsHardwareEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                val isGps = location.provider == LocationManager.GPS_PROVIDER
                val accuracy = location.accuracy

                if (isLocating || isGps || accuracy < locationAccuracy || locationAccuracy == 0f) {
                    currentLatitude = location.latitude
                    currentLongitude = location.longitude
                    locationAccuracy = accuracy
                    locationSpeed = location.speed * 3.6f
                    locationAltitude = location.altitude
                    isLocating = false
                    isGpsFixAccurate = accuracy > 0f && accuracy <= 20f
                    lastUpdatedTimeStr = if (isGps) {
                        "GPS مانگی دەستکرد: ${String.format(Locale.ENGLISH, "%.1f", accuracy)} مەتر (١٠٠٪ تەواو)"
                    } else {
                        "تۆڕ: ${String.format(Locale.ENGLISH, "%.1f", accuracy)} مەتر"
                    }
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {
                isGpsHardwareEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            }
            override fun onProviderDisabled(provider: String) {
                isGpsHardwareEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            }
        }

        try {
            val lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            val best = when {
                lastGps != null && lastNet != null -> if (lastGps.accuracy <= lastNet.accuracy) lastGps else lastNet
                lastGps != null -> lastGps
                else -> lastNet
            }

            if (best != null) {
                currentLatitude = best.latitude
                currentLongitude = best.longitude
                locationAccuracy = best.accuracy
                locationSpeed = best.speed * 3.6f
                locationAltitude = best.altitude
                isLocating = false
                isGpsFixAccurate = best.accuracy > 0f && best.accuracy <= 25f
                lastUpdatedTimeStr = "شوێنی دەستنیشانکراو (${String.format(Locale.ENGLISH, "%.0f", best.accuracy)} مەتر)"
            }

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    0f,
                    listener
                )
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    2000L,
                    0f,
                    listener
                )
            }
        } catch (_: SecurityException) {}

        onDispose {
            try {
                locationManager.removeUpdates(listener)
            } catch (_: SecurityException) {}
        }
    }

    // Live countdown timer
    LaunchedEffect(isSharingActive, sharingExpiresAt) {
        while (isSharingActive && sharingExpiresAt > 0L) {
            val now = System.currentTimeMillis()
            val diff = sharingExpiresAt - now
            if (diff <= 0) {
                isSharingActive = false
                livePrefs.edit().putBoolean("is_live_sharing_active", false).apply()
                remainingTimeFormatted = "کاتی هاوبەشکردن بەسەرچوو"
                break
            } else {
                val totalSec = diff / 1000
                val hours = totalSec / 3600
                val minutes = (totalSec % 3600) / 60
                val seconds = totalSec % 60
                remainingTimeFormatted = String.format(Locale.ENGLISH, "%02d:%02d:%02d", hours, minutes, seconds)
            }
            delay(1000)
        }
    }

    // Animation for Live Pulse & Radar
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFE53935),
        targetValue = Color(0xFFE53935).copy(alpha = 0.2f),
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseColor"
    )

    val radarRadiusScale by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarRadius"
    )

    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200),
            repeatMode = RepeatMode.Restart
        ),
        label = "radarAlpha"
    )

    // Build the share message with Google Maps Link + APK Download Link
    val userName = if (uiState.userName.isNotBlank()) uiState.userName else "هاوڕێیەکەت لە زەڕایەن"
    val durationLabel = when (selectedDurationHours) {
        4 -> "٤ کاتژمێر"
        8 -> "٨ کاتژمێر"
        12 -> "١٢ کاتژمێر"
        else -> "٢٤ کاتژمێر"
    }

    val googleMapsUrl = "https://www.google.com/maps?q=$currentLatitude,$currentLongitude"
    val deepLinkUrl = "https://zarayen.app/location?lat=$currentLatitude&lng=$currentLongitude&name=${Uri.encode(userName)}&dur=$selectedDurationHours&exp=$sharingExpiresAt"

    val fullShareMessage = buildString {
        append("📍 شوێنی ڕاستەوخۆم (Live Location) لە ئەپی زەڕایەن:\n\n")
        append("👤 لەلایەن: $userName\n")
        append("⏱️ ماوەی هاوبەشکردن: $durationLabel\n")
        append("🌐 هێڵی پانی و درێژی: ${String.format(Locale.ENGLISH, "%.5f", currentLatitude)}, ${String.format(Locale.ENGLISH, "%.5f", currentLongitude)}\n\n")
        append("🗺️ بۆ کردنەوەی ڕاستەوخۆ لە گووگڵ ماپس (Google Maps) کلیک بکە:\n")
        append("$googleMapsUrl\n\n")
        append("📲 ئەگەر ئەپەکەت هەیە، کلیک بکە بۆ بینینی لەناو ئەپی زەڕایەن:\n")
        append("$deepLinkUrl\n\n")
        append("📥 بۆ داگرتنی ئەپی فەرمی شارۆچکەی زەڕایەن (فایلی APK ٣١ مێگابایت):\n")
        append(apkDownloadUrl)
    }

    // Function to open location directly in Google Maps
    val openInGoogleMaps = { lat: Double, lng: Double, label: String ->
        try {
            val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
            }
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val webMapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps?q=$lat,$lng"))
                context.startActivity(webMapIntent)
            }
        } catch (_: Exception) {
            val webMapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps?q=$lat,$lng"))
            context.startActivity(webMapIntent)
        }
    }

    // Helper to share to specific apps
    val shareToSpecificApp = { targetAppPackage: String?, fallbackScheme: String? ->
        var opened = false
        if (targetAppPackage != null) {
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, fullShareMessage)
                    setPackage(targetAppPackage)
                }
                context.startActivity(intent)
                opened = true
            } catch (_: Exception) {}
        }

        if (!opened && fallbackScheme != null) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fallbackScheme))
                context.startActivity(intent)
                opened = true
            } catch (_: Exception) {}
        }

        if (!opened) {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "شوێنی ڕاستەوخۆ لە زەڕایەن")
                putExtra(Intent.EXTRA_TEXT, fullShareMessage)
            }
            context.startActivity(Intent.createChooser(shareIntent, "هاوبەشکردن لەگەڵ هاوڕێیان لە ڕێگەی..."))
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isViewingFriend) "شوێنی هاوبەشکراوی هاوڕێ" else "شوێنی ئێستام (Live GPS)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (isSharingActive || isViewingFriend) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFE53935).copy(alpha = 0.2f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(pulseColor)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "LIVE",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp,
                                                color = Color(0xFFE53935)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = if (isViewingFriend) "شوێنی ڕاستەوخۆی $friendName" else "دیاریکردنی ١٠٠٪ و هاوبەشکردن",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (isViewingFriend) {
                                viewModel.clearSharedLocation()
                            } else {
                                onNavigateBack()
                            }
                        },
                        modifier = Modifier.testTag("live_location_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "گەڕانەوە"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            openInGoogleMaps(displayLat, displayLng, if (isViewingFriend) "$friendName" else "شوێنی من")
                        },
                        modifier = Modifier.testTag("open_external_map_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "گووگڵ ماپس",
                            tint = AntiqueGold
                        )
                    }

                    IconButton(
                        onClick = { shareToSpecificApp(null, null) },
                        modifier = Modifier.testTag("live_location_top_share_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "هاوبەشکردن"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Permission Alert
            if (!hasLocationPermission) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ڕێگەپێدانی شوێن (GPS) پێویستە",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "بۆ دەرهێنانی ناونیشان و هاوبەشکردنی شوێنی ئێستات، تکایە ڕێگە بە شوێن بدە.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = DeepPineGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("چالاککردنی ڕێگەپێدانی GPS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Hardware GPS Disabled Alert
            if (hasLocationPermission && !isGpsHardwareEnabled) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF9800))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.GpsOff,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "جی پی ئێس (GPS) لە مۆبایلەکەتدا کوژاوەتەوە",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFE65100)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "بۆ دەستنیشانکردنی شوێنی تەواو بە مانگی دەستکرد بە وردیی ١٠٠٪، تکایە شوێن (Location) لە ڕێکخستنی مۆبایلەکەتدا داگیرسێنە.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFBF360C)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("داگیرساندنی ڕاستەوخۆی GPS", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Friend Viewing Banner
            if (isViewingFriend) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AntiqueGold.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AntiqueGold)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
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
                                        imageVector = Icons.Default.PersonPinCircle,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "شوێنی هاوبەشکراوی هاوڕێ",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = AntiqueGold
                                    )
                                    Text(
                                        text = friendName ?: "هاوڕێ",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { viewModel.clearSharedLocation() },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("شوێنی من", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (distanceToFriendMeters != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(DeepPineGreen.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NearMe,
                                        contentDescription = null,
                                        tint = DeepPineGreen,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "دووری لە شوێنی تۆوە:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = DeepPineGreen
                                    )
                                }

                                val distStr = if (distanceToFriendMeters >= 1000f) {
                                    String.format(Locale.ENGLISH, "%.2f کم", distanceToFriendMeters / 1000f)
                                } else {
                                    String.format(Locale.ENGLISH, "%.0f مەتر", distanceToFriendMeters)
                                }

                                Text(
                                    text = distStr,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = DeepPineGreen
                                )
                            }
                        }
                    }
                }
            }

            // 100% Reliable Native Live Map Layout (Never White Screen)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .testTag("live_map_main_card"),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, DeepPineGreen.copy(alpha = 0.5f))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (mapMode == 0) {
                        // Native Compose Animated Topographic & Radar Map
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF14241C))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val maxRadius = size.width.coerceAtMost(size.height) / 2f * 0.85f * mapZoomLevel

                                // 1. Concentric Range Rings
                                val ringFractions = listOf(0.25f, 0.50f, 0.75f, 1.0f)
                                for (frac in ringFractions) {
                                    drawCircle(
                                        color = AntiqueGold.copy(alpha = 0.20f),
                                        radius = maxRadius * frac,
                                        center = Offset(centerX, centerY),
                                        style = Stroke(
                                            width = 1.5f,
                                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                        )
                                    )
                                }

                                // 2. Crosshair Grid Lines
                                drawLine(
                                    color = AntiqueGold.copy(alpha = 0.25f),
                                    start = Offset(0f, centerY),
                                    end = Offset(size.width, centerY),
                                    strokeWidth = 1.5f
                                )
                                drawLine(
                                    color = AntiqueGold.copy(alpha = 0.25f),
                                    start = Offset(centerX, 0f),
                                    end = Offset(centerX, size.height),
                                    strokeWidth = 1.5f
                                )

                                // 3. Radar Sweep / Pulsing Ring
                                drawCircle(
                                    color = Color(0xFFE53935).copy(alpha = radarAlpha * 0.4f),
                                    radius = maxRadius * radarRadiusScale,
                                    center = Offset(centerX, centerY),
                                    style = Stroke(width = 3.5f)
                                )

                                // 4. Zarayen Main Highway Representation (Diagonal road)
                                drawLine(
                                    color = Color(0xFFFFB300).copy(alpha = 0.40f),
                                    start = Offset(0f, size.height * 0.85f),
                                    end = Offset(size.width, size.height * 0.15f),
                                    strokeWidth = 5f
                                )

                                // 5. User Center Beacon
                                drawCircle(
                                    color = Color(0xFFE53935),
                                    radius = 9f,
                                    center = Offset(centerX, centerY)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 4f,
                                    center = Offset(centerX, centerY)
                                )
                            }

                            // Town Landmark Pins on Canvas
                            // A. Grand Mosque Pin (North East of center)
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(bottom = 90.dp, start = 70.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = DeepPineGreen.copy(alpha = 0.9f),
                                shadowElevation = 3.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🕌 مزگەوتی گەورە", fontSize = 10.sp, color = AntiqueGold, fontWeight = FontWeight.Bold)
                                }
                            }

                            // B. Bazaar & Dasty2 (South West of center)
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(top = 90.dp, end = 70.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF263238).copy(alpha = 0.9f),
                                shadowElevation = 3.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("🛍️ بازاڕی زەڕایەن", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                            // C. Highway Label
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 12.dp, bottom = 48.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF37474F).copy(alpha = 0.85f)
                            ) {
                                Text(
                                    text = "🛣️ ڕێگای سلێمانی - شارەزوور",
                                    fontSize = 9.sp,
                                    color = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // Center Marker Badge (You Are Here)
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(bottom = 36.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFE53935),
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isViewingFriend) "$friendName" else "شوێنی ئێستات",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    } else {
                        // Native Compose Satellite Dark Mode (Zero WebViews, Zero Chromium crashes)
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0D1B14))
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                val centerX = size.width / 2f
                                val centerY = size.height / 2f
                                val maxRadius = size.width.coerceAtMost(size.height) / 2f * 0.85f * mapZoomLevel

                                // Dense Satellite Grid lines
                                for (i in -4..4) {
                                    val offset = i * (size.width / 8f) * mapZoomLevel
                                    drawLine(
                                        color = Color(0xFF00E676).copy(alpha = 0.08f),
                                        start = Offset(centerX + offset, 0f),
                                        end = Offset(centerX + offset, size.height),
                                        strokeWidth = 1f
                                    )
                                    drawLine(
                                        color = Color(0xFF00E676).copy(alpha = 0.08f),
                                        start = Offset(0f, centerY + offset),
                                        end = Offset(size.width, centerY + offset),
                                        strokeWidth = 1f
                                    )
                                }

                                // Satellite Orbit Circle
                                drawCircle(
                                    color = Color(0xFF00E676).copy(alpha = 0.25f),
                                    radius = maxRadius,
                                    center = Offset(centerX, centerY),
                                    style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f))
                                )

                                // Pulsing Radar Ring
                                drawCircle(
                                    color = Color(0xFF00E676).copy(alpha = radarAlpha * 0.5f),
                                    radius = maxRadius * radarRadiusScale,
                                    center = Offset(centerX, centerY),
                                    style = Stroke(width = 2.5f)
                                )

                                // Highway
                                drawLine(
                                    color = Color(0xFFFFD54F).copy(alpha = 0.5f),
                                    start = Offset(0f, size.height * 0.80f),
                                    end = Offset(size.width, size.height * 0.20f),
                                    strokeWidth = 4f
                                )

                                // Center Target
                                drawCircle(
                                    color = Color(0xFF00E676),
                                    radius = 8f,
                                    center = Offset(centerX, centerY)
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 3f,
                                    center = Offset(centerX, centerY)
                                )
                            }

                            // Satellite Mode Label & Coordinates Overlay
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(start = 12.dp, bottom = 48.dp),
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.8f)
                            ) {
                                Text(
                                    text = "🛰️ مانگی دەستکرد: ${String.format(Locale.ENGLISH, "%.4f, %.4f", displayLat, displayLng)}",
                                    fontSize = 10.sp,
                                    color = Color(0xFF00E676),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // Center Marker Badge
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(bottom = 36.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF00E676),
                                shadowElevation = 4.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isViewingFriend) "$friendName" else "شوێنی ڕاستەوخۆ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }

                    // Top Bar Controls inside the Map Card
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.75f)
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (mapMode == 0) DeepPineGreen else Color.Transparent,
                                modifier = Modifier
                                    .clickable { mapMode = 0 }
                                    .testTag("map_mode_native_btn")
                            ) {
                                Text(
                                    text = "نەخشەی ڕادار",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (mapMode == 1) DeepPineGreen else Color.Transparent,
                                modifier = Modifier
                                    .clickable { mapMode = 1 }
                                    .testTag("map_mode_satellite_btn")
                            ) {
                                Text(
                                    text = "مانگی دەستکرد",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Zoom Controls on the Left
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.75f),
                            modifier = Modifier
                                .size(34.dp)
                                .clickable {
                                    if (mapZoomLevel < 2.0f) mapZoomLevel += 0.25f
                                }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "گەورەکردن", tint = Color.White, modifier = Modifier.padding(6.dp))
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.75f),
                            modifier = Modifier
                                .size(34.dp)
                                .clickable {
                                    if (mapZoomLevel > 0.6f) mapZoomLevel -= 0.25f
                                }
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "بچووککردن", tint = Color.White, modifier = Modifier.padding(6.dp))
                        }
                    }

                    // Prominent Bottom Floating Action: Open Directly in Google Maps App
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                            .clickable {
                                openInGoogleMaps(displayLat, displayLng, if (isViewingFriend) "$friendName" else "شوێنی من")
                            }
                            .testTag("directions_maps_btn"),
                        shape = RoundedCornerShape(14.dp),
                        color = DeepPineGreen,
                        shadowElevation = 6.dp,
                        border = androidx.compose.foundation.BorderStroke(1.dp, AntiqueGold)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Directions,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = AntiqueGold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "کردنەوە لە گووگڵ ماپس (ناڤیگەیشن)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // GPS Telemetry Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isGpsFixAccurate) Icons.Default.GpsFixed else Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = if (isGpsFixAccurate) DeepPineGreen else AntiqueGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "دۆخی شوێن و GPS",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = if (isGpsFixAccurate) Color(0xFFE8F5E9) else Color(0xFFFFF8E1)
                                    ) {
                                        Text(
                                            text = if (isGpsFixAccurate) "١٠٠٪ تەواو" else "ئۆنلاین",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isGpsFixAccurate) Color(0xFF2E7D32) else Color(0xFFF57F17),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = lastUpdatedTimeStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                try {
                                    val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                                    if (lastGps != null) {
                                        currentLatitude = lastGps.latitude
                                        currentLongitude = lastGps.longitude
                                        locationAccuracy = lastGps.accuracy
                                        locationSpeed = lastGps.speed * 3.6f
                                        locationAltitude = lastGps.altitude
                                        isGpsFixAccurate = lastGps.accuracy > 0f && lastGps.accuracy <= 25f
                                    }
                                } catch (_: SecurityException) {}
                                Toast.makeText(context, "نوێکردنەوەی GPS ئەنجامدرا", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("refresh_gps_data_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "نوێکردنەوەی شوێن",
                                tint = DeepPineGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "هێڵی پانی (Latitude)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format(Locale.ENGLISH, "%.6f°", displayLat),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column {
                            Text(
                                text = "هێڵی درێژی (Longitude)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = String.format(Locale.ENGLISH, "%.6f°", displayLng),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "وردی (Accuracy)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (locationAccuracy > 0f) "${locationAccuracy.toInt()} مەتر" else "GPS بەستراوە",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = DeepPineGreen
                            )
                        }

                        Column {
                            Text(
                                text = "خێرایی (Speed)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${locationSpeed.toInt()} کم/ک",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column {
                            Text(
                                text = "بەرزی (Altitude)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${locationAltitude.toInt()} مەتر",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Social Media Share Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = AntiqueGold,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.padding(7.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "هاوبەشکردن بۆ تۆڕە کۆمەڵایەتییەکان",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "شوێنەکە لە گووگڵ ماپس دەکرێتەوە لەگەڵ لینکی ئەپ:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // WhatsApp Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clickable {
                                    shareToSpecificApp(
                                        "com.whatsapp",
                                        "https://api.whatsapp.com/send?text=${Uri.encode(fullShareMessage)}"
                                    )
                                }
                                .testTag("share_whatsapp_btn"),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF25D366)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("واتسئەپ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                Text("WhatsApp", fontSize = 9.sp, color = Color.White.copy(alpha = 0.9f))
                            }
                        }

                        // Telegram Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clickable {
                                    shareToSpecificApp(
                                        "org.telegram.messenger",
                                        "https://t.me/share/url?url=${Uri.encode(googleMapsUrl)}&text=${Uri.encode(fullShareMessage)}"
                                    )
                                }
                                .testTag("share_telegram_btn"),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF229ED9)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("تێلیگرام", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                Text("Telegram", fontSize = 9.sp, color = Color.White.copy(alpha = 0.9f))
                            }
                        }

                        // Messenger Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clickable {
                                    shareToSpecificApp(
                                        "com.facebook.orca",
                                        "https://www.facebook.com/sharer/sharer.php?u=${Uri.encode(googleMapsUrl)}"
                                    )
                                }
                                .testTag("share_messenger_btn"),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0084FF)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("ماسنجەر", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                Text("Messenger", fontSize = 9.sp, color = Color.White.copy(alpha = 0.9f))
                            }
                        }

                        // Viber Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clickable {
                                    shareToSpecificApp(
                                        "com.viber.voip",
                                        "viber://forward?text=${Uri.encode(fullShareMessage)}"
                                    )
                                }
                                .testTag("share_viber_btn"),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF7360F2)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("ڤایبەر", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                Text("Viber", fontSize = 9.sp, color = Color.White.copy(alpha = 0.9f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Live Location Link", fullShareMessage)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "بەستەری شوێنی ڕاستەوخۆ و گووگڵ ماپس کۆپیکرا", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("copy_location_share_link_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("کۆپیکردن", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { shareToSpecificApp(null, null) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .testTag("share_all_apps_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepPineGreen)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("هەموو ئەپەکان", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Duration Controls Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSharingActive) Color(0xFF1B2E24) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSharingActive) AntiqueGold else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (isSharingActive) AntiqueGold else DeepPineGreen,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = if (isSharingActive) Color.Black else Color.White,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "هاوبەشکردنی بەردەوام (Live Timer)",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSharingActive) AntiqueGold else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isSharingActive) "هاوبەشکردن چالاکە • $remainingTimeFormatted" else "ماوەی بەردەوامی دیاریبکە:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSharingActive) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (isSharingActive) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE53935)
                            ) {
                                Text(
                                    text = "LIVE",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    ),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!isSharingActive) {
                        Text(
                            text = "ماوەی بەردەوامی هاوبەشکردن دیاریبکە:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val durationOptions = listOf(4, 8, 12, 24)
                            durationOptions.forEach { hours ->
                                FilterChip(
                                    selected = selectedDurationHours == hours,
                                    onClick = {
                                        selectedDurationHours = hours
                                        livePrefs.edit().putInt("live_sharing_duration_hours", hours).apply()
                                    },
                                    label = {
                                        Text(
                                            text = "$hours کاتژمێر",
                                            fontWeight = if (selectedDurationHours == hours) FontWeight.Bold else FontWeight.Normal,
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = DeepPineGreen,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val now = System.currentTimeMillis()
                                val expiresAt = now + (selectedDurationHours * 3600 * 1000L)
                                isSharingActive = true
                                sharingExpiresAt = expiresAt
                                livePrefs.edit()
                                    .putBoolean("is_live_sharing_active", true)
                                    .putInt("live_sharing_duration_hours", selectedDurationHours)
                                    .putLong("live_sharing_expires_at", expiresAt)
                                    .apply()

                                Toast.makeText(context, "هاوبەشکردنی شوێنی ڕاستەوخۆ دەستیپێکرد بۆ $selectedDurationHours کاتژمێر", Toast.LENGTH_SHORT).show()
                                shareToSpecificApp(null, null)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_live_share_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepPineGreen)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "دەستپێکردنی هاوبەشکردن ($selectedDurationHours کاتژمێر)",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.HourglassBottom,
                                    contentDescription = null,
                                    tint = AntiqueGold,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ماوەی ماوە بۆ بەسەرچوون:",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = remainingTimeFormatted,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = AntiqueGold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { shareToSpecificApp(null, null) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("send_share_link_again_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ناردنی بەستەر", color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    isSharingActive = false
                                    sharingExpiresAt = 0L
                                    livePrefs.edit().putBoolean("is_live_sharing_active", false).apply()
                                    Toast.makeText(context, "هاوبەشکردنی شوێنی ڕاستەوخۆ وەستێنرا", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("stop_live_share_btn"),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350))
                            ) {
                                Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("وەستاندن", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Quick Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "💡 چۆنیەتی کارکردنی شوێنی ڕاستەوخۆ:",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• کاتێک کلیک لەسەر واتسئەپ، تێلیگرام، ماسنجەر یان ڤایبەر دەکەیت، بەستەری Google Maps بۆ هاوڕێکەت دەنێردرێت و ڕاستەوخۆ شوێنەکەت لەسەر نەخشەی گووگڵ ماپس دەبینێت.\n• هاوکات لینکی داگرتنی فایلی APKـی ئەپەکەشی بۆ دەڕوات بۆ ئەوەی ئەپەکە دابەزێنێت.\n• نەخشەی ناوخۆیی هەمیشە کاردەکات و ڕێگری دەکات لە دروستبوونی شاشەی سپی.",
                        style = MaterialTheme.typography.bodySmall,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
