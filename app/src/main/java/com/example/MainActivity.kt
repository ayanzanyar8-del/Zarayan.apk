package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.AddNewUserDialog
import com.example.ui.components.AdminLoginDialog
import com.example.ui.components.DeleteUserConfirmDialog
import com.example.ui.components.UserRegistrationDialog
import com.example.ui.screens.AboutZarayenScreen
import com.example.ui.screens.AdminUsersScreen
import com.example.ui.screens.AzkarScreen
import com.example.ui.screens.CurrencyScreen
import com.example.ui.screens.Dasty2Screen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.JanazaScreen
import com.example.ui.screens.LiveLocationScreen
import com.example.ui.screens.NamesOfAllahScreen
import com.example.ui.screens.NewsPhotosScreen
import com.example.ui.screens.PlacesScreen
import com.example.ui.screens.PrayerTimesScreen
import com.example.ui.screens.QiblaScreen
import com.example.ui.screens.QuranListScreen
import com.example.ui.screens.QuranReaderScreen
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepPineGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private var currentIntentState = mutableStateOf<Intent?>(null)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        currentIntentState.value = intent
    }

    override fun onResume() {
        super.onResume()
        com.example.data.network.JanazaOnlineSyncManager.triggerImmediateSync(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        currentIntentState.value = intent
        enableEdgeToEdge()
        com.example.util.PrayerAlarmScheduler.scheduleAllPrayerAlarms(this)
        com.example.util.ZikrAlarmScheduler.scheduleAllZikrAlarms(this)
        com.example.data.network.JanazaOnlineSyncManager.triggerImmediateSync(this)
        setContent {
            val viewModel: MainViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()
            val latestJanaza by viewModel.latestJanaza.collectAsState()
            val namesOfAllahList by viewModel.namesOfAllahList.collectAsState()
            val azkarList by viewModel.azkarList.collectAsState()
            val registeredUsersList by viewModel.registeredUsersList.collectAsState()

            val context = LocalContext.current
            val snackbarHostState = remember { SnackbarHostState() }

            // Handle notification and deep link routing
            val currentIntent by currentIntentState
            LaunchedEffect(currentIntent) {
                val intentData = currentIntent?.data
                val openScreen = currentIntent?.getStringExtra("OPEN_SCREEN")

                // Handle live location deep link: zarayen://location?... or https://zarayen.app/location?...
                if (intentData != null && (intentData.host == "location" || intentData.path?.startsWith("/location") == true)) {
                    val lat = intentData.getQueryParameter("lat")?.toDoubleOrNull()
                    val lng = intentData.getQueryParameter("lng")?.toDoubleOrNull()
                    val name = intentData.getQueryParameter("name") ?: "هاوڕێ"
                    val dur = intentData.getQueryParameter("dur")?.toIntOrNull() ?: 24
                    val exp = intentData.getQueryParameter("exp")?.toLongOrNull() ?: (System.currentTimeMillis() + dur * 3600 * 1000L)
                    if (lat != null && lng != null) {
                        viewModel.setSharedLocation(name, lat, lng, dur, exp)
                    } else {
                        viewModel.navigateTo(AppScreen.LIVE_LOCATION)
                    }
                } else if (openScreen == "location" || openScreen == "live_location") {
                    val lat = currentIntent?.getDoubleExtra("LAT", 0.0) ?: 0.0
                    val lng = currentIntent?.getDoubleExtra("LNG", 0.0) ?: 0.0
                    val name = currentIntent?.getStringExtra("NAME") ?: "هاوڕێ"
                    val dur = currentIntent?.getIntExtra("DUR", 24) ?: 24
                    val exp = currentIntent?.getLongExtra("EXP", 0L) ?: 0L
                    if (lat != 0.0 && lng != 0.0) {
                        viewModel.setSharedLocation(name, lat, lng, dur, exp)
                    } else {
                        viewModel.navigateTo(AppScreen.LIVE_LOCATION)
                    }
                } else if (openScreen == "janaza") {
                    viewModel.navigateTo(AppScreen.JANAZA)
                } else if (openScreen == "prayer") {
                    viewModel.navigateTo(AppScreen.PRAYER_TIMES)
                } else if (openScreen == "azkar") {
                    val zikrType = currentIntent?.getStringExtra("ZIKR_TYPE")
                    when (zikrType) {
                        "morning" -> viewModel.selectAzkarCategory(com.example.data.model.DhikrCategory.MORNING)
                        "evening" -> viewModel.selectAzkarCategory(com.example.data.model.DhikrCategory.EVENING)
                        "sleep", "bedtime" -> viewModel.selectAzkarCategory(com.example.data.model.DhikrCategory.BEDTIME)
                        else -> {}
                    }
                    viewModel.navigateTo(AppScreen.AZKAR)
                }
            }

            // Check if notification permission is granted
            val prefs = remember { context.getSharedPreferences("zarayen_app_prefs", Context.MODE_PRIVATE) }
            val isNotifGranted = com.example.util.NotificationHelper.isNotificationPermissionGranted(context)
            var showNotificationPermissionDialog by remember {
                mutableStateOf(!isNotifGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            }

            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                prefs.edit().putBoolean("notif_permission_asked_v1", true).apply()
                if (isGranted) {
                    viewModel.ensureNotificationsAlwaysOn(context)
                    viewModel.showSnackbar("ئاگادارییەکان هەمیشە چالاک کران بە سەرکەوتوویی")
                }
            }

            // Location Permission Launcher for First Launch (GPS requirement)
            val locationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { perms ->
                prefs.edit().putBoolean("location_permission_asked_first_launch", true).apply()
            }

            LaunchedEffect(Unit) {
                if (isNotifGranted) {
                    viewModel.ensureNotificationsAlwaysOn(context)
                }
                // Proactively request GPS / Location permission on first launch
                val hasLocationPermission = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
                val hasAskedLocation = prefs.getBoolean("location_permission_asked_first_launch", false)

                if (!hasLocationPermission && !hasAskedLocation) {
                    prefs.edit().putBoolean("location_permission_asked_first_launch", true).apply()
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                }
            }

            LaunchedEffect(uiState.snackbarMessage) {
                uiState.snackbarMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearSnackbar()
                }
            }

            // Android Back Button Navigation
            BackHandler(enabled = uiState.currentScreen != AppScreen.HOME) {
                if (uiState.currentScreen == AppScreen.QURAN_READER) {
                    viewModel.navigateTo(AppScreen.QURAN_LIST)
                } else {
                    viewModel.navigateTo(AppScreen.HOME)
                }
            }

            MyApplicationTheme(darkTheme = uiState.isDarkMode) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
                ) { innerPadding ->
                    when (uiState.currentScreen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                latestJanaza = latestJanaza,
                                onNavigate = { screen -> viewModel.navigateTo(screen) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.PRAYER_TIMES -> {
                            PrayerTimesScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                                onNavigateToQibla = { viewModel.navigateTo(AppScreen.QIBLA) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.JANAZA -> {
                            JanazaScreen(
                                isAdmin = uiState.isAdmin,
                                onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                                onAdminLogin = { secret -> viewModel.loginAsAdmin(secret) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.CURRENCY -> {
                            CurrencyScreen(
                                onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.DASTY2 -> {
                            Dasty2Screen(
                                onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.QURAN_LIST -> {
                            QuranListScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.QURAN_READER -> {
                            QuranReaderScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onNavigateBack = { viewModel.navigateTo(AppScreen.QURAN_LIST) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.AZKAR -> {
                            AzkarScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                azkarList = azkarList,
                                onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.NAMES_OF_ALLAH -> {
                            NamesOfAllahScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                namesList = namesOfAllahList,
                                onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.QIBLA -> {
                            QiblaScreen(
                                onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.PLACES -> {
                            PlacesScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.ABOUT_ZARAYEN -> {
                            AboutZarayenScreen(
                                viewModel = viewModel,
                                onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.ADMIN_USERS -> {
                            AdminUsersScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                usersList = registeredUsersList,
                                onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.NEWS_PHOTOS -> {
                            NewsPhotosScreen(
                                onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.LIVE_LOCATION -> {
                            LiveLocationScreen(
                                viewModel = viewModel,
                                uiState = uiState,
                                onNavigateBack = { viewModel.navigateTo(AppScreen.HOME) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }

                    // First-Time Notification Permission Request Dialog
                    if (showNotificationPermissionDialog) {
                        AlertDialog(
                            onDismissRequest = {
                                prefs.edit().putBoolean("notif_permission_asked_v1", true).apply()
                                showNotificationPermissionDialog = false
                            },
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = DeepPineGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "چالاککردنی ئاگادارییەکان",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            },
                            text = {
                                Text(
                                    text = "ئایا دەتەوێت ئاگاداری نوێژی مردوو و کاتی بانگەکانت بۆ بێت کاتێک ئەدمین پەیامی نوێ بڵاودەکاتەوە؟",
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        showNotificationPermissionDialog = false
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        } else {
                                            prefs.edit().putBoolean("notif_permission_asked_v1", true).apply()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DeepPineGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.testTag("enable_notifications_btn")
                                ) {
                                    Text("بەڵێ، چالاککردن", fontWeight = FontWeight.Bold)
                                }
                            },
                            dismissButton = {
                                TextButton(
                                    onClick = {
                                        prefs.edit().putBoolean("notif_permission_asked_v1", true).apply()
                                        showNotificationPermissionDialog = false
                                    },
                                    modifier = Modifier.testTag("dismiss_notifications_btn")
                                ) {
                                    Text("دواتر")
                                }
                            },
                            shape = RoundedCornerShape(18.dp)
                        )
                    }

                    // Dialogs
                    if (uiState.showRegistrationDialog) {
                        UserRegistrationDialog(
                            onDismiss = { viewModel.dismissRegistrationDialog() },
                            onSaveName = { name ->
                                viewModel.registerUser(name)
                            }
                        )
                    }

                    if (uiState.showAdminLoginDialog) {
                        AdminLoginDialog(
                            onDismiss = { viewModel.closeAdminLoginDialog() },
                            onLogin = { code -> viewModel.loginAsAdmin(code) }
                        )
                    }

                    if (uiState.showAddUserDialog) {
                        AddNewUserDialog(
                            onDismiss = { viewModel.closeAddUserDialog() },
                            onAddUser = { name, phone, email, neighborhood, role, notes ->
                                viewModel.addNewUserByAdmin(
                                    name = name,
                                    phone = phone,
                                    email = email,
                                    neighborhood = neighborhood,
                                    role = role,
                                    notes = notes
                                )
                            }
                        )
                    }

                    uiState.deletingUser?.let { userToDelete ->
                        DeleteUserConfirmDialog(
                            userName = userToDelete.fullName,
                            onDismiss = { viewModel.cancelDeleteUser() },
                            onConfirmDelete = { viewModel.confirmDeleteUser() }
                        )
                    }
                }
            }
        }
    }
}
