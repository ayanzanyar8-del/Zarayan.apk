package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.network.JanazaOnlineSyncManager
import com.example.data.local.AppDatabase
import com.example.data.model.JanazaAnnouncement
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepPineGreen
import com.example.util.NotificationHelper
import com.example.util.ZarayenFirebaseMessagingService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * نوێژی مردوو (Janaza Screen)
 * Directly renders the live, official online website inside an in-app WebView:
 * https://janaza.freebuff.app/
 *
 * Clean header without site URL, external browser or share icons.
 * Features a direct test broadcast button at the top right to broadcast push notifications
 * to all installed devices even when the app is closed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun JanazaScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialUrl: String = "https://janaza.freebuff.app/",
    isAdmin: Boolean = false,
    onAdminLogin: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }
    var hasError by remember { mutableStateOf(false) }

    var localIsAdmin by remember { mutableStateOf(isAdmin) }
    LaunchedEffect(isAdmin) {
        localIsAdmin = isAdmin
    }

    LaunchedEffect(Unit) {
        JanazaOnlineSyncManager.triggerImmediateSync(context)
    }

    var showAdminAuthDialog by remember { mutableStateOf(false) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var isBroadcasting by remember { mutableStateOf(false) }

    // File upload callback for WebView
    var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val callback = fileUploadCallback
        fileUploadCallback = null
        if (callback != null) {
            val uris: Array<Uri>? = try {
                WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
                    ?: result.data?.data?.let { arrayOf(it) }
                    ?: result.data?.clipData?.let { clip ->
                        Array(clip.itemCount) { i -> clip.getItemAt(i).uri }
                    }
            } catch (e: Exception) {
                result.data?.data?.let { arrayOf(it) }
            }
            callback.onReceiveValue(uris)
        }
    }

    // Android back navigation inside WebView history if available
    BackHandler(enabled = true) {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onNavigateBack()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            fileUploadCallback?.onReceiveValue(null)
            fileUploadCallback = null
            webViewInstance?.destroy()
            webViewInstance = null
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = DeepPineGreen,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Campaign,
                                    contentDescription = "نوێژی مردوو",
                                    tint = AntiqueGold,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "نوێژی مردوو",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (webViewInstance?.canGoBack() == true) {
                                    webViewInstance?.goBack()
                                } else {
                                    onNavigateBack()
                                }
                            },
                            modifier = Modifier.testTag("janaza_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "گەڕانەوە",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        // Admin Notification Broadcast Button
                        Button(
                            onClick = {
                                if (localIsAdmin) {
                                    showBroadcastDialog = true
                                } else {
                                    showAdminAuthDialog = true
                                }
                            },
                            enabled = !isBroadcasting,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (localIsAdmin) Color(0xFFB71C1C) else Color(0xFFC0392B),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("janaza_broadcast_all_button")
                        ) {
                            if (isBroadcasting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "ناردنی ئاگاداری بۆ هەمووان",
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(horizontalAlignment = Alignment.Start) {
                                Text(
                                    text = "ناردنی ئاگاداری",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                if (localIsAdmin) {
                                    Text(
                                        text = "ئەدمین",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            color = AntiqueGold,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Loading progress indicator strip
                AnimatedVisibility(
                    visible = isLoading,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LinearProgressIndicator(
                        progress = { if (progress > 0f) progress else 0.1f },
                        modifier = Modifier.fillMaxWidth(),
                        color = AntiqueGold,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Live In-App WebView
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    WebView(ctx).apply {
                        setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            databaseEnabled = true
                            useWideViewPort = true
                            loadWithOverviewMode = true
                            setSupportZoom(true)
                            builtInZoomControls = true
                            displayZoomControls = false
                            // Always fetch live directly from network so changes are immediate
                            cacheMode = WebSettings.LOAD_NO_CACHE
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            allowContentAccess = true
                            allowFileAccess = true
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                super.onPageStarted(view, url, favicon)
                                isLoading = true
                                hasError = false
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?
                            ) {
                                super.onReceivedError(view, request, error)
                                if (request?.isForMainFrame == true) {
                                    hasError = true
                                    isLoading = false
                                }
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean {
                                val url = request?.url?.toString() ?: return false
                                return if (url.startsWith("http://") || url.startsWith("https://")) {
                                    false // Keep inside WebView
                                } else {
                                    true
                                }
                            }

                            override fun onRenderProcessGone(
                                view: WebView?,
                                detail: RenderProcessGoneDetail?
                            ): Boolean {
                                try {
                                    view?.let {
                                        val parent = it.parent as? ViewGroup
                                        parent?.removeView(it)
                                        it.destroy()
                                    }
                                } catch (e: Exception) {
                                    // ignore
                                }
                                webViewInstance = null
                                hasError = true
                                isLoading = false
                                return true
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress / 100f
                                if (newProgress >= 100) {
                                    isLoading = false
                                }
                            }

                            override fun onShowFileChooser(
                                webView: WebView?,
                                filePathCallback: ValueCallback<Array<Uri>>?,
                                fileChooserParams: FileChooserParams?
                            ): Boolean {
                                fileUploadCallback?.onReceiveValue(null)
                                fileUploadCallback = filePathCallback

                                return try {
                                    val intent = fileChooserParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "*/*"
                                        putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
                                    }
                                    val chooserIntent = Intent.createChooser(intent, "هەڵبژاردنی پەڕگە یان وێنە")
                                    fileChooserLauncher.launch(chooserIntent)
                                    true
                                } catch (e: Exception) {
                                    fileUploadCallback?.onReceiveValue(null)
                                    fileUploadCallback = null
                                    false
                                }
                            }
                        }

                        loadUrl(initialUrl)
                        webViewInstance = this
                    }
                },
                update = { webView ->
                    webViewInstance = webView
                }
            )

            // Fullscreen Loading Overlay on initial start
            if (isLoading && progress < 0.2f && !hasError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = DeepPineGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "کردنەوەی ماڵپەڕی نوێژی مردوو...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Error / Offline State with Retry
            if (hasError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.errorContainer,
                                modifier = Modifier.size(64.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.padding(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "پەیوەندی ماڵپەڕ بەردەست نییە",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "تکایە دڵنیابەرەوە لە هەبوونی هێڵی ئینتەرنێت بۆ بارکردنی نوێترین زانیارییەکانی ماڵپەڕی نوێژی مردوو.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = {
                                        hasError = false
                                        isLoading = true
                                        webViewInstance?.clearCache(true)
                                        webViewInstance?.loadUrl(initialUrl)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = DeepPineGreen
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("دووبارە هەوڵبدەرەوە")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // 1. Admin Authentication Dialog if not currently admin
    if (showAdminAuthDialog) {
        var adminCodeInput by remember { mutableStateOf("") }
        var isAuthError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAdminAuthDialog = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = DeepPineGreen.copy(alpha = 0.15f),
                    modifier = Modifier.size(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = DeepPineGreen,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "دەسەڵاتی ئەدمین پێویستە",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "ناردنی ئاگاداری ڕاستەوخۆ بۆ هەموو مۆبایل و بەکارهێنەرانی ئەپ تەنها تایبەتە بە ئەدمینی زەڕایەن. تکایە کۆدی ئەدمین بنووسە بۆ بەردەوامبوون:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = adminCodeInput,
                        onValueChange = {
                            adminCodeInput = it
                            isAuthError = false
                        },
                        label = { Text("کۆدی نهێنی ئەدمین") },
                        placeholder = { Text("کۆد بنووسە...") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        isError = isAuthError,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("janaza_admin_auth_input"),
                        leadingIcon = {
                            Icon(Icons.Default.Key, contentDescription = null, tint = AntiqueGold)
                        },
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (isAuthError) {
                        Text(
                            text = "کۆدی ئەدمین نادروستە! تکایە کۆدی ڕاست بنووسە.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(top = 6.dp, start = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (adminCodeInput.trim().equals("zanyar2", ignoreCase = true)) {
                            localIsAdmin = true
                            onAdminLogin?.invoke(adminCodeInput.trim())
                            showAdminAuthDialog = false
                            showBroadcastDialog = true
                            Toast.makeText(context, "بەخێربێیت ئەدمینی بەڕێز!", Toast.LENGTH_SHORT).show()
                        } else {
                            isAuthError = true
                        }
                    },
                    modifier = Modifier.testTag("janaza_admin_auth_confirm"),
                    colors = ButtonDefaults.buttonColors(containerColor = DeepPineGreen),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("چوونەژوورەوە و ناردن")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminAuthDialog = false }) {
                    Text("داخستن")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }

    // 2. Admin Broadcast Janaza Notification Dialog in Kurdish Sorani
    if (showBroadcastDialog) {
        var deceasedNameInput by remember { mutableStateOf("پۆستی نوێی نوێژی مردوو لە زەڕایەن") }
        var mosqueNameInput by remember { mutableStateOf("مزگەوتی گەورەی زەڕایەن") }
        var prayerTimeInput by remember { mutableStateOf("دوای نوێژی داهاتوو") }
        var customNoteInput by remember { mutableStateOf("پۆستێکی نوێی نوێژی مردوو لە زەڕایەن بڵاوکرایەوە. بۆ بینینی تەواوی زانیارییەکان سەردانی پەڕەی نوێژی مردوو بکەن.") }
        val scrollState = rememberScrollState()

        AlertDialog(
            onDismissRequest = { if (!isBroadcasting) showBroadcastDialog = false },
            icon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFB71C1C).copy(alpha = 0.12f),
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = Color(0xFFB71C1C),
                        modifier = Modifier.padding(10.dp)
                    )
                }
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ناردنی ئاگاداری بۆ هەمووان",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AntiqueGold.copy(alpha = 0.2f),
                        modifier = Modifier.border(0.5.dp, AntiqueGold, RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = "ئەدمین",
                            color = AntiqueGold,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                ) {
                    Text(
                        text = "وەک ئەدمین دەتوانیت ئاگاداری نوێ لەبارەی نوێژی مردوو بە زمانی کوردی سۆرانی بۆ سەرجەم بەکارهێنەرانی ئەپەکە بنێریت بە شێوەی پۆش نۆتفیکەیشن (Push Notification):",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Live Preview Card of Kurdish Sorani Notification
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, AntiqueGold.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    tint = AntiqueGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "ئەپی زەڕایەن • ئاگاداری نوێژی مردوو",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = AntiqueGold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "📢 ئاگاداری: پۆستی نوێی نوێژی مردوو لە زەڕایەن",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "﴿ إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ ﴾ ${deceasedNameInput.ifBlank { "کۆچی دوایی لە زەڕایەن" }}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "📍 مزگەوت: ${mosqueNameInput.ifBlank { "مزگەوتی گەورەی زەڕایەن" }} • ⏰ کاتی نوێژ: ${prayerTimeInput.ifBlank { "دوای نوێژی داهاتوو" }}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = deceasedNameInput,
                        onValueChange = { deceasedNameInput = it },
                        label = { Text("ناوی خوالێخۆشبوو یان سەردێڕ") },
                        placeholder = { Text("ناوی کۆچکردوو بنووسە...") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("janaza_notif_name_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = mosqueNameInput,
                        onValueChange = { mosqueNameInput = it },
                        label = { Text("مزگەوتی نوێژ") },
                        placeholder = { Text("ناوی مزگەوت...") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Mosque, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("janaza_notif_mosque_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = prayerTimeInput,
                        onValueChange = { prayerTimeInput = it },
                        label = { Text("کاتی نوێژ") },
                        placeholder = { Text("بۆ نموونە: دوای نوێژی نیوەڕۆ / عەسر") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("janaza_notif_time_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = customNoteInput,
                        onValueChange = { customNoteInput = it },
                        label = { Text("دەقی ئاگاداری بە کوردی سۆرانی") },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("janaza_notif_body_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isBroadcasting) {
                            isBroadcasting = true
                            coroutineScope.launch {
                                val finalDeceased = deceasedNameInput.trim().ifBlank { "پۆستی نوێی نوێژی مردوو لە زەڕایەن" }
                                val finalMosque = mosqueNameInput.trim().ifBlank { "مزگەوتی گەورەی زەڕایەن" }
                                val finalPrayer = prayerTimeInput.trim().ifBlank { "دوای نوێژی داهاتوو" }
                                val finalNote = customNoteInput.trim().ifBlank { "پۆستێکی نوێی نوێژی مردوو لە زەڕایەن بڵاوکرایەوە." }

                                val newAnnouncement = JanazaAnnouncement(
                                    id = System.currentTimeMillis(),
                                    deceasedName = finalDeceased,
                                    description = finalNote,
                                    mosqueName = finalMosque,
                                    cemeteryName = "گۆڕستانی شارۆچکەی زەڕایەن",
                                    prayerTime = finalPrayer,
                                    condolenceLocation = "شارۆچکەی زەڕایەن",
                                    dateString = "ئەمڕۆ",
                                    timestamp = System.currentTimeMillis(),
                                    authorName = "ئەدمینی زەڕایەن"
                                )

                                // 1. Publish to live Convex cloud database so all installed devices receive the notification
                                val publishResult = JanazaOnlineSyncManager.publishNewJanazaPost(
                                    context = context,
                                    name = finalDeceased,
                                    gender = "نێر",
                                    prayerTime = finalPrayer,
                                    prayerLocation = finalMosque,
                                    prsaLocation = "هۆڵی پرسەی زەڕایەن",
                                    notes = finalNote
                                )

                                // 2. Show immediate local notification for the admin
                                NotificationHelper.showJanazaNotification(
                                    context = context,
                                    deceasedName = finalDeceased,
                                    mosqueName = finalMosque,
                                    prayerTime = finalPrayer,
                                    customTitle = "📢 ئاگاداری: پۆستی نوێی نوێژی مردوو لە زەڕایەن",
                                    customBody = "﴿ إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ ﴾ $finalDeceased"
                                )

                                // 3. Cache in local Room DB
                                try {
                                    withContext(Dispatchers.IO) {
                                        val db = AppDatabase.getDatabase(context)
                                        db.janazaDao().insertAnnouncement(newAnnouncement)
                                    }
                                } catch (_: Exception) {}

                                // 4. Reload WebView so new post is rendered immediately
                                webViewInstance?.reload()

                                if (publishResult.isSuccess) {
                                    Toast.makeText(
                                        context,
                                        "📢 ئاگاداری نوێژی مردوو بە سەرکەوتوویی لە سێرڤەری گشتی بڵاوکرایەوە و بۆ هەموو مۆبایلەکان پەخشکرا!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    Toast.makeText(
                                        context,
                                        "ئاگاداری بە شێوەی خۆجێیی پاشەکەوت کرا، بەڵام کێشە لە پەیوەندی هێڵی ئینتەرنێت هەیە.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }

                                isBroadcasting = false
                                showBroadcastDialog = false
                            }
                        }
                    },
                    enabled = !isBroadcasting,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("janaza_notif_send_button")
                ) {
                    if (isBroadcasting) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ناردن...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("ناردنی ئاگاداری بۆ هەمووان")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBroadcastDialog = false },
                    enabled = !isBroadcasting
                ) {
                    Text("پاشگەزبوونەوە")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}
