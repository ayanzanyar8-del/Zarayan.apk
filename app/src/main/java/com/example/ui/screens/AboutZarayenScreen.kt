package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.data.datasource.WikiSection
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.DeepEspressoBrown
import com.example.ui.theme.DeepPineGreen
import com.example.ui.viewmodel.MainViewModel
import java.io.File

/**
 * پەڕەی دەربارەی شارۆچکەی زەڕایەن
 * لەگەڵ بەشی داگرتن و هاوبەشکردنی فایلی تەواوی ئەپەکە (APK Installer ~31 MB)
 * و ناردنی لینکی ڕاستەوخۆ بۆ واتسئەپ، تێلیگرام، ماسنجەر و ڤایبەر.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutZarayenScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val wikiSections = viewModel.wikiSections
    val prefs = remember { context.getSharedPreferences("zarayen_app_prefs", Context.MODE_PRIVATE) }

    // Working Direct Download URL for the 31MB APK
    val defaultDownloadUrl = "https://temp.sh/pAtnm/Zarayen_App_Installer.apk"
    var currentDownloadUrl by remember {
        mutableStateOf(prefs.getString("custom_apk_download_url", defaultDownloadUrl) ?: defaultDownloadUrl)
    }
    var showEditUrlDialog by remember { mutableStateOf(false) }
    var editedUrlInput by remember { mutableStateOf(currentDownloadUrl) }

    // Helper: Share full text message with download link
    val shareDownloadLink = { targetPackage: String?, fallbackScheme: String? ->
        val shareMessage = buildString {
            append("📲 داگرتنی ئەپی فەرمی شارۆچکەی زەڕایەن (Zarayen App):\n\n")
            append("📦 قەبارەی تەواوی ئەپ: ٣١ مێگابایت (31 MB APK)\n")
            append("🕌 خزمەتگوزارییەکان: کاتەکانی بانگ، قورئانی پیرۆز، زیکرەکانی بەیانیان و ئێواران و خەوتن، ئاگاداری نوێژی مردوو، قیبلەنما، کەشوهەوا، نرخی دراو و ڕێبەری شارۆچکە.\n\n")
            append("📥 بۆ داگرتنی ڕاستەوخۆ کلیک لەم لینکە بکە:\n")
            append(currentDownloadUrl)
        }

        var opened = false
        if (targetPackage != null) {
            try {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                    setPackage(targetPackage)
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
                putExtra(Intent.EXTRA_SUBJECT, "داگرتنی ئەپی زەڕایەن")
                putExtra(Intent.EXTRA_TEXT, shareMessage)
            }
            context.startActivity(Intent.createChooser(shareIntent, "هاوبەشکردنی لینکی داگرتن لەگەڵ هاوڕێیان لە ڕێگەی..."))
        }
    }

    // Helper: Share the installed 31MB APK file directly via FileProvider
    val shareDirectApkFile = {
        try {
            val appInfo = context.applicationInfo
            val sourceApk = File(appInfo.sourceDir)
            if (sourceApk.exists()) {
                val cacheDir = File(context.cacheDir, "shared_apk")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val targetApk = File(cacheDir, "Zarayen_App_31MB.apk")

                if (!targetApk.exists() || targetApk.length() != sourceApk.length()) {
                    sourceApk.copyTo(targetApk, overwrite = true)
                }

                val apkUri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    targetApk
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/vnd.android.package-archive"
                    putExtra(Intent.EXTRA_STREAM, apkUri)
                    putExtra(Intent.EXTRA_SUBJECT, "ئەپی فەرمی شارۆچکەی زەڕایەن (31 MB APK)")
                    putExtra(Intent.EXTRA_TEXT, "فایلی ئەپی فەرمی شارۆچکەی زەڕایەن (قەبارە: ٣١ مێگابایت). دایبەزێنە و دایمەزرێنە.")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "ناردنی فایلی APK بۆ هاوڕێیان لە ڕێگەی..."))
            } else {
                Toast.makeText(context, "فایلی ئەپەکە لەم مۆبایلەدا نەدۆزرایەوە", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "کێشە لە هاوبەشکردنی فایلی ئەپ: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "دەربارەی شارۆچکەی زەڕایەن",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("about_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "گەڕانەوە"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { shareDownloadLink(null, null) },
                        modifier = Modifier.testTag("about_top_share_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "هاوبەشکردن",
                            tint = AntiqueGold
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. Hero Header Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = DeepPineGreen)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AntiqueGold,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationCity,
                                contentDescription = "زەڕایەن",
                                tint = DeepEspressoBrown,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "شارۆچکەی زەڕایەن (شارەزوور)",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )

                        Text(
                            text = "پارێزگای سلێمانی • هەرێمی کوردستان - عێراق",
                            style = MaterialTheme.typography.bodySmall,
                            color = AntiqueGold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "پێگەی فەرمی و زانیارینامەی شارۆچکەی زەڕایەن بۆ دانیشتووان و هاوڵاتیانی ناوچەکە",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // 2. Comprehensive APK Download & Share Card (~31 MB)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("apk_download_share_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, AntiqueGold)
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
                                    color = DeepPineGreen,
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.InstallMobile,
                                        contentDescription = null,
                                        tint = AntiqueGold,
                                        modifier = Modifier.padding(9.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "داگرتن و هاوبەشکردنی ئەپ",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = AntiqueGold.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "31 MB",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF8A6D1C),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "فایلی دامەزرێنەری ئەپەکە (APK Installer)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Edit Custom Link icon button
                            IconButton(
                                onClick = {
                                    editedUrlInput = currentDownloadUrl
                                    showEditUrlDialog = true
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("edit_download_url_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "دەستکاری بەستەر",
                                    tint = DeepPineGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "لێرەوە دەتوانیت بە یەک کلیک لینکی داگرتنی ڕاستەوخۆ بۆ هاوڕێکەت بنێریت یان فایلی APKـی تەواوی ئەپەکە (٣١ مێگابایت) بە ڕاستەوخۆ هاوبەش بکەیت:",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Share App Buttons (WhatsApp, Telegram, Messenger, Viber)
                        Text(
                            text = "ناردنی لینکی داگرتن لە ڕێگەی تۆڕە کۆمەڵایەتییەکان:",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // WhatsApp
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable {
                                        shareDownloadLink(
                                            "com.whatsapp",
                                            "https://api.whatsapp.com/send?text=${Uri.encode("داگرتنی ئەپی شارۆچکەی زەڕایەن (31MB):\n$currentDownloadUrl")}"
                                        )
                                    }
                                    .testTag("share_apk_whatsapp_btn"),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF25D366)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("واتسئەپ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                    Text("WhatsApp", fontSize = 8.sp, color = Color.White.copy(alpha = 0.9f))
                                }
                            }

                            // Telegram
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable {
                                        shareDownloadLink(
                                            "org.telegram.messenger",
                                            "https://t.me/share/url?url=${Uri.encode(currentDownloadUrl)}&text=${Uri.encode("داگرتنی ئەپی شارۆچکەی زەڕایەن (31MB)")}"
                                        )
                                    }
                                    .testTag("share_apk_telegram_btn"),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF229ED9)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("تێلیگرام", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                    Text("Telegram", fontSize = 8.sp, color = Color.White.copy(alpha = 0.9f))
                                }
                            }

                            // Messenger
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable {
                                        shareDownloadLink(
                                            "com.facebook.orca",
                                            "https://www.facebook.com/sharer/sharer.php?u=${Uri.encode(currentDownloadUrl)}"
                                        )
                                    }
                                    .testTag("share_apk_messenger_btn"),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF0084FF)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("ماسنجەر", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                    Text("Messenger", fontSize = 8.sp, color = Color.White.copy(alpha = 0.9f))
                                }
                            }

                            // Viber
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .clickable {
                                        shareDownloadLink(
                                            "com.viber.voip",
                                            "viber://forward?text=${Uri.encode("داگرتنی ئەپی زەڕایەن:\n$currentDownloadUrl")}"
                                        )
                                    }
                                    .testTag("share_apk_viber_btn"),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF7360F2)
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text("ڤایبەر", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                                    Text("Viber", fontSize = 8.sp, color = Color.White.copy(alpha = 0.9f))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Secondary actions: Copy link & Open in browser
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("Zarayen App Download Link", currentDownloadUrl)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "لینکی داگرتنی ئەپ کۆپیکرا", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("copy_apk_link_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("کۆپیکردنی بەستەر", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.openUrl(context, currentDownloadUrl)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("open_apk_link_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("تاقیکردنەوە لە وێب", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Primary Action: Share Real APK File (31 MB) directly from phone
                        Button(
                            onClick = { shareDirectApkFile() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("share_full_apk_file_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DeepPineGreen)
                        ) {
                            Icon(Icons.Default.Android, contentDescription = null, tint = AntiqueGold, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ناردنی فایلی APK ڕاستەوخۆ (قەبارەی ٣١ MB)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // 3. Official Social Channels
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "پەڕە فەرمییەکان و هەواڵی زەڕایەن",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.openUrl(context, "https://www.facebook.com/share/19DjMcB7nF/") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("about_fb_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("فەیسبووک", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = { viewModel.openUrl(context, "https://www.tiktok.com/@zarayannews?_r=1&_t=ZS-98uRiXqxrYf") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("about_tt_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("تیکتۆکی هەواڵ", style = MaterialTheme.typography.labelSmall)
                            }

                            OutlinedButton(
                                onClick = { viewModel.openUrl(context, "https://maps.app.goo.gl/VnyXna76pJKSCxyw6") },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("about_map_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("شوێن لە نەخشە", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // 4. Town History & Geography Wiki Sections
            items(wikiSections) { section ->
                WikiSectionCard(section = section)
            }

            // 5. Offline & App Integrity Note
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ئەم ئەپە تەواوی داتاکانی (بانگ، قورئان، زیکر، شوێنەکان، قیبلە) بەبێ هێڵی ئینتەرنێت (Offline) کار دەکات.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }

        // Custom Download URL Edit Dialog
        if (showEditUrlDialog) {
            AlertDialog(
                onDismissRequest = { showEditUrlDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Download, contentDescription = null, tint = DeepPineGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("دەستکاریکردنی بەستەری داگرتن", fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column {
                        Text(
                            text = "دەتوانیت بەستەری داگرتنی فایلی APK (وەک گووگڵ درایڤ، مێدیافایەر، یان کاناڵی تێلیگرام) لێرە دابنێیت بۆ ئەوەی لە کاتی هاوبەشکردندا بڕوات بۆ هاوڕێکانت:",
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 20.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = editedUrlInput,
                            onValueChange = { editedUrlInput = it },
                            label = { Text("بەستەری داگرتن (URL)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = { editedUrlInput = defaultDownloadUrl }
                        ) {
                            Text("گەڕاندنەوە بۆ بەستەری سەرەکی")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val trimmed = editedUrlInput.trim()
                            if (trimmed.isNotBlank()) {
                                currentDownloadUrl = trimmed
                                prefs.edit().putString("custom_apk_download_url", trimmed).apply()
                                Toast.makeText(context, "بەستەری داگرتن نوێکرایەوە", Toast.LENGTH_SHORT).show()
                            }
                            showEditUrlDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DeepPineGreen)
                    ) {
                        Text("پاشەکەوتکردن", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditUrlDialog = false }) {
                        Text("داخستن")
                    }
                },
                shape = RoundedCornerShape(18.dp)
            )
        }
    }
}

@Composable
private fun WikiSectionCard(section: WikiSection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = section.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = section.content,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
