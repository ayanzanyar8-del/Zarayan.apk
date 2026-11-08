package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepPineGreen

/**
 * لاپەڕەی دەستی دوو (Dasty 2 - Direct Live Site)
 * Direct online view for https://dasty2.freebuff.app/
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun Dasty2Screen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    siteUrl: String = "https://dasty2.freebuff.app/"
) {
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }
    var hasError by remember { mutableStateOf(false) }

    // File upload callback for WebView input file elements
    var fileUploadCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }

    // File / Media chooser launcher
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

    // Permission launcher for media & camera access when uploading photos and videos
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Permission response handled by Android OS */ }

    // Request permissions for photo and video uploads
    LaunchedEffect(Unit) {
        val permissions = buildList {
            add(Manifest.permission.CAMERA)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
                add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        permissionsLauncher.launch(permissions.toTypedArray())
    }

    // Handle back press inside web history
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
                                modifier = Modifier.size(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingBag,
                                    contentDescription = "دەستی دوو",
                                    tint = AntiqueGold,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "دەستی دوو",
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
                            modifier = Modifier.testTag("dasty2_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "گەڕانەوە",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                hasError = false
                                isLoading = true
                                webViewInstance?.clearCache(true)
                                webViewInstance?.reload()
                            },
                            modifier = Modifier.testTag("dasty2_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "نوێکردنەوە",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

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
                            cacheMode = WebSettings.LOAD_DEFAULT
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
                                    false
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
                                    val chooserIntent = Intent.createChooser(intent, "هەڵبژاردنی وێنە یان ڤیدیۆ")
                                    fileChooserLauncher.launch(chooserIntent)
                                    true
                                } catch (e: Exception) {
                                    fileUploadCallback?.onReceiveValue(null)
                                    fileUploadCallback = null
                                    false
                                }
                            }
                        }

                        loadUrl(siteUrl)
                        webViewInstance = this
                    }
                },
                update = { webView ->
                    webViewInstance = webView
                }
            )

            // Loading indicator overlay
            if (isLoading && progress < 0.25f && !hasError) {
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
                            text = "بارکردنی دەستی دوو بە شێوەی ڕاستەوخۆ...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Offline or error screen
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
                                text = "پەیوەندی هێڵ بەردەست نییە",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "تکایە دڵنیابەرەوە لە هەبوونی ئینتەرنێت بۆ کردنەوەی ماڵپەڕی دەستی دوو.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    hasError = false
                                    isLoading = true
                                    webViewInstance?.clearCache(true)
                                    webViewInstance?.loadUrl(siteUrl)
                                },
                                modifier = Modifier.fillMaxWidth(),
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
