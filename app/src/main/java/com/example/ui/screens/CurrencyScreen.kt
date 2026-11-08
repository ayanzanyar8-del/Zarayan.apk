package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Refresh
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
 * لاپەڕەی نرخی دراو و زێڕ (Live Online Currency & Market Rates)
 * Directly loads https://rudawindex.net/ online.
 * Injects custom CSS/JS to cleanly remove/hide the Rudaw logo and brand names.
 */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CurrencyScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    initialUrl: String = "https://rudawindex.net/"
) {
    val context = LocalContext.current
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0f) }
    var hasError by remember { mutableStateOf(false) }

    // Android back navigation inside WebView history
    BackHandler(enabled = true) {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            onNavigateBack()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewInstance?.destroy()
            webViewInstance = null
        }
    }

    // Safe CSS injection to hide Rudaw logos and replace branding with 'شارۆچکەی زەڕایەن'
    val hideRudawScript = """
        (function() {
            try {
                if (!document.getElementById('zarayen-custom-style')) {
                    var style = document.createElement('style');
                    style.id = 'zarayen-custom-style';
                    style.textContent = `
                        header img, .logo img, [class*="logo"] img, img[alt*="rudaw" i], img[src*="rudaw" i], img[alt*="ڕووداو"] {
                            display: none !important;
                        }
                        .logo, .site-logo, .navbar-brand {
                            font-size: 0 !important;
                        }
                        .logo::after, .site-logo::after, .navbar-brand::after {
                            content: 'شارۆچکەی زەڕایەن' !important;
                            font-size: 16px !important;
                            font-weight: bold !important;
                            color: #281C14 !important;
                            background: #D4AF37 !important;
                            padding: 4px 10px !important;
                            border-radius: 8px !important;
                            display: inline-block !important;
                        }
                    `;
                    (document.head || document.documentElement).appendChild(style);
                }
            } catch (e) {}
        })();
    """.trimIndent()

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
                                    imageVector = Icons.Default.CurrencyExchange,
                                    contentDescription = "نرخی دراو",
                                    tint = AntiqueGold,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "نرخی دراو و زێڕ",
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
                            modifier = Modifier.testTag("currency_back_button")
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
                            modifier = Modifier.testTag("currency_refresh_button")
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
                                view?.evaluateJavascript(hideRudawScript, null)
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
                                    view?.evaluateJavascript(hideRudawScript, null)
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

            // Loading overlay
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
                            text = "بارکردنی نرخی دراو و زێڕ بە شێوەی ڕاستەوخۆ...",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }

            // Error state
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
                                text = "تکایە دڵنیابەرەوە لە هەبوونی ئینتەرنێت بۆ بارکردنی نوێترین نرخی دراو و بازاڕ.",
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
                                    webViewInstance?.loadUrl(initialUrl)
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
