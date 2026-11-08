package com.example.data.network

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.JanazaAnnouncement
import com.example.util.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Live Cloud Synchronization & Push Notification Engine for Nwezhi Mrdu (نوێژی مردوو).
 *
 * Connected directly to the live backend:
 * https://valuable-seal-114.convex.cloud
 *
 * Whenever the admin publishes an announcement (from the app's button or website),
 * it is stored in the cloud, and EVERY installed app across all phones with internet
 * automatically receives and displays a high-priority Kurdish notification with sound and vibration!
 */
object JanazaOnlineSyncManager {

    private const val TAG = "JanazaOnlineSync"
    private const val CONVEX_BASE_URL = "https://valuable-seal-114.convex.cloud"
    private const val QUERY_URL = "$CONVEX_BASE_URL/api/query"
    private const val MUTATION_URL = "$CONVEX_BASE_URL/api/mutation"

    private const val PREFS_NAME = "zarayen_online_janaza_prefs"
    private const val KEY_NOTIFIED_IDS = "notified_janaza_post_ids"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val scope = CoroutineScope(Dispatchers.IO)
    private var pollingJob: Job? = null

    private val _onlinePosts = MutableStateFlow<List<JanazaAnnouncement>>(emptyList())
    val onlinePosts: StateFlow<List<JanazaAnnouncement>> = _onlinePosts.asStateFlow()

    /**
     * Starts foreground periodic polling (checks every 15 seconds while app is active).
     */
    fun startForegroundPolling(context: Context) {
        if (pollingJob?.isActive == true) return
        val appContext = context.applicationContext

        pollingJob = scope.launch {
            Log.d(TAG, "Starting foreground polling for Janaza online announcements...")
            // Immediate initial check
            fetchAndSyncOnlinePosts(appContext, triggerNotification = true)

            while (isActive) {
                delay(15000)
                try {
                    fetchAndSyncOnlinePosts(appContext, triggerNotification = true)
                } catch (e: Exception) {
                    Log.w(TAG, "Foreground polling cycle notice: ${e.message}")
                }
            }
        }
    }

    fun stopForegroundPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * Triggers a one-off immediate synchronization on Dispatchers.IO.
     */
    fun triggerImmediateSync(context: Context, onComplete: ((Boolean) -> Unit)? = null) {
        val appContext = context.applicationContext
        scope.launch {
            val success = fetchAndSyncOnlinePosts(appContext, triggerNotification = true)
            withContext(Dispatchers.Main) {
                onComplete?.invoke(success)
            }
        }
    }

    /**
     * Queries the live Convex cloud database for all Janaza announcements.
     * Identifies newly published posts from Freebuff, saves them to local Room DB,
     * and displays loud, high-priority notifications across all installed devices!
     */
    suspend fun fetchAndSyncOnlinePosts(
        context: Context,
        triggerNotification: Boolean = true
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val requestBodyJson = JSONObject().apply {
                put("path", "posts:list")
                put("args", JSONObject())
            }

            val body = requestBodyJson.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(QUERY_URL)
                .post(body)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Failed to query online posts, HTTP code: ${response.code}")
                    return@withContext false
                }

                val responseStr = response.body?.string() ?: return@withContext false
                val rootJson = JSONObject(responseStr)
                if (rootJson.optString("status") != "success") {
                    Log.w(TAG, "Convex returned error status: $responseStr")
                    return@withContext false
                }

                val postsArray = rootJson.optJSONArray("value") ?: JSONArray()
                val parsedList = mutableListOf<JanazaAnnouncement>()
                val remoteIds = mutableSetOf<String>()

                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val notifiedIds = prefs.getStringSet(KEY_NOTIFIED_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()

                val newPostsToNotify = mutableListOf<JanazaAnnouncement>()
                val db = AppDatabase.getDatabase(context)
                val nowTime = System.currentTimeMillis()

                for (i in 0 until postsArray.length()) {
                    val obj = postsArray.getJSONObject(i)
                    val idStr = obj.optString("_id", "")
                    if (idStr.isBlank()) continue
                    remoteIds.add(idStr)

                    val name = obj.optString("name", "کۆچی دوایی لە زەڕایەن").trim()
                    val prayerLoc = obj.optString("prayerLocation", "مزگەوتی گەورەی زەڕایەن").trim()
                    val prayerTime = obj.optString("prayerTime", "دوای نوێژی داهاتوو").trim()
                    val prsaLoc = obj.optString("prsaLocation", "شارۆچکەی زەڕایەن").trim()
                    val notes = obj.optString("notes", "").trim()
                    val creationTime = obj.optDouble("_creationTime", nowTime.toDouble()).toLong()

                    val positiveId = Math.abs(idStr.hashCode().toLong())

                    val announcement = JanazaAnnouncement(
                        id = positiveId,
                        deceasedName = name,
                        description = if (notes.isNotBlank()) notes else "پۆستێکی نوێی نوێژی مردوو لە زەڕایەن بڵاوکرایەوە.",
                        mosqueName = prayerLoc,
                        cemeteryName = "گۆڕستانی شارۆچکەی زەڕایەن",
                        prayerTime = prayerTime,
                        condolenceLocation = prsaLoc,
                        dateString = "نوێ",
                        timestamp = creationTime,
                        authorName = "ئەدمینی زەڕایەن"
                    )

                    parsedList.add(announcement)

                    // Check if this post has not been notified yet on this device
                    val hasBeenNotified = notifiedIds.contains(idStr)
                    // Consider posts within last 48 hours as recent eligible for notification
                    val isRecent = (nowTime - creationTime) < (48 * 60 * 60 * 1000L)

                    if (!hasBeenNotified) {
                        if (isRecent && triggerNotification) {
                            newPostsToNotify.add(announcement)
                        }
                        // Mark as notified on this device to prevent duplicate notification popups
                        notifiedIds.add(idStr)
                    }

                    // Save or update in local Room DB for offline viewing
                    try {
                        db.janazaDao().insertAnnouncement(announcement)
                    } catch (e: Exception) {
                        Log.w(TAG, "Error caching announcement to Room: ${e.message}")
                    }
                }

                // Persist updated notified IDs set
                prefs.edit()
                    .putStringSet(KEY_NOTIFIED_IDS, notifiedIds)
                    .apply()

                _onlinePosts.value = parsedList
                Log.d(TAG, "Synced ${parsedList.size} Janaza announcements. New posts to notify: ${newPostsToNotify.size}")

                // Trigger Kurdish Sorani System Notifications for new posts like Bang notifications
                for (newPost in newPostsToNotify) {
                    NotificationHelper.showJanazaNotification(
                        context = context,
                        deceasedName = newPost.deceasedName,
                        mosqueName = newPost.mosqueName,
                        prayerTime = newPost.prayerTime,
                        customTitle = "📢 ئاگاداری نوێ: نوێژی مردوو لە زەڕایەن",
                        customBody = "﴿ إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ ﴾ ${newPost.deceasedName} • ${newPost.mosqueName} (${newPost.prayerTime})"
                    )
                }

                true
            }
        } catch (e: java.net.UnknownHostException) {
            // Normal offline state when device has no internet or DNS is resolving; handle gracefully
            Log.d(TAG, "Convex backend currently unreachable (device offline / DNS): ${e.message}")
            false
        } catch (e: java.io.IOException) {
            Log.d(TAG, "Network IO issue syncing Janaza announcements: ${e.message}")
            false
        } catch (e: Exception) {
            Log.w(TAG, "Notice syncing Janaza announcements: ${e.message}")
            false
        }
    }

    /**
     * Publishes a new Janaza announcement to the global cloud database.
     * When this succeeds, the post is live on the internet, and all other users' apps
     * will automatically receive the notification!
     */
    suspend fun publishNewJanazaPost(
        context: Context,
        name: String,
        gender: String = "نێر",
        prayerTime: String,
        prayerLocation: String,
        prsaLocation: String = "هۆڵی پرسەی زەڕایەن",
        notes: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val argsJson = JSONObject().apply {
                put("name", name.trim())
                put("gender", if (gender.isNotBlank()) gender.trim() else "نێر")
                put("prayerTime", prayerTime.trim())
                put("prayerLocation", prayerLocation.trim())
                put("prsaLocation", prsaLocation.trim())
                if (!notes.isNullOrBlank()) {
                    put("notes", notes.trim())
                }
            }

            val requestBodyJson = JSONObject().apply {
                put("path", "posts:create")
                put("args", argsJson)
            }

            val body = requestBodyJson.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(MUTATION_URL)
                .post(body)
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val err = "Server responded with HTTP ${response.code}"
                    Log.e(TAG, "Failed to publish post: $err")
                    return@withContext Result.failure(Exception(err))
                }

                val responseStr = response.body?.string() ?: ""
                val rootJson = JSONObject(responseStr)
                if (rootJson.optString("status") != "success") {
                    val errMsg = rootJson.optString("errorMessage", "Unknown server error")
                    Log.e(TAG, "Convex mutation error: $errMsg")
                    return@withContext Result.failure(Exception(errMsg))
                }

                val newPostId = rootJson.optString("value")
                Log.d(TAG, "Successfully published post to Convex with ID: $newPostId")

                // Mark as notified on admin's device so admin isn't re-notified as an incoming post
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val notifiedIds = prefs.getStringSet(KEY_NOTIFIED_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
                if (newPostId.isNotBlank()) {
                    notifiedIds.add(newPostId)
                    prefs.edit().putStringSet(KEY_NOTIFIED_IDS, notifiedIds).apply()
                }

                // Also save to Room DB locally
                try {
                    val positiveId = Math.abs(newPostId.hashCode().toLong())
                    val localAnnouncement = JanazaAnnouncement(
                        id = if (positiveId != 0L) positiveId else System.currentTimeMillis(),
                        deceasedName = name.trim(),
                        description = notes?.trim() ?: "پۆستی نوێی نوێژی مردوو لە زەڕایەن",
                        mosqueName = prayerLocation.trim(),
                        cemeteryName = "گۆڕستانی شارۆچکەی زەڕایەن",
                        prayerTime = prayerTime.trim(),
                        condolenceLocation = prsaLocation.trim(),
                        dateString = "ئەمڕۆ",
                        timestamp = System.currentTimeMillis(),
                        authorName = "ئەدمینی زەڕایەن"
                    )
                    AppDatabase.getDatabase(context).janazaDao().insertAnnouncement(localAnnouncement)
                } catch (e: Exception) {
                    Log.w(TAG, "Room caching notice after publish: ${e.message}")
                }

                Result.success(newPostId)
            }
        } catch (e: java.net.UnknownHostException) {
            Log.d(TAG, "Cannot publish post while offline: ${e.message}")
            Result.failure(Exception("هێڵی ئینتەرنێت پەیوەست نییە، تکایە دڵنیابەرەوە لە پەیوەستبوونی ئینتەرنێت."))
        } catch (e: java.io.IOException) {
            Log.d(TAG, "Network IO issue publishing post: ${e.message}")
            Result.failure(Exception("کێشەی پەیوەندی تۆڕ، تکایە دووبارە هەوڵبدەرەوە."))
        } catch (e: Exception) {
            Log.w(TAG, "Notice publishing post: ${e.message}")
            Result.failure(e)
        }
    }
}
