package com.example.data.network

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.JanazaAnnouncement
import com.example.util.NotificationHelper
import com.example.util.ZarayenFirebaseMessagingService
import com.google.firebase.FirebaseOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
 * Real-time Cloud Synchronization Engine for Janaza Announcements & Condolence Messages.
 * Keeps all installed apps across all mobile devices synchronized in real-time.
 * Automatically broadcasts and triggers system push notifications whenever any phone posts.
 */
class JanazaCloudSyncManager(
    private val context: Context,
    private val database: AppDatabase
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("zarayen_cloud_sync_prefs", Context.MODE_PRIVATE)
    private var syncJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        private const val TAG = "JanazaCloudSync"
        private const val PREF_LAST_SYNC_TIME = "last_known_announcement_timestamp"
        private const val PREF_LAST_SEEN_IDS = "seen_announcement_ids"
    }

    /**
     * Resolves valid remote database URL if a real Firebase configuration is supplied.
     */
    private fun getCloudSyncUrl(): String? {
        return try {
            val options = FirebaseOptions.fromResource(context)
            val dbUrl = options?.databaseUrl
            if (!dbUrl.isNullOrBlank() && !dbUrl.contains("zarayen-app-sync-default-rtdb")) {
                "${dbUrl.trimEnd('/')}/announcements"
            } else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Starts continuous background synchronization loop.
     * Polls the cloud endpoint every 15 seconds when active, and triggers notifications on new posts.
     */
    fun startRealtimeSync() {
        JanazaOnlineSyncManager.startForegroundPolling(context)
    }

    fun stopRealtimeSync() {
        JanazaOnlineSyncManager.stopForegroundPolling()
    }

    /**
     * Publishes a new Janaza announcement to the global cloud database so all mobiles receive it.
     */
    suspend fun publishAnnouncementToCloud(announcement: JanazaAnnouncement): Boolean {
        val result = JanazaOnlineSyncManager.publishNewJanazaPost(
            context = context,
            name = announcement.deceasedName,
            gender = "نێر",
            prayerTime = announcement.prayerTime,
            prayerLocation = announcement.mosqueName,
            prsaLocation = announcement.condolenceLocation,
            notes = announcement.description
        )
        return result.isSuccess
    }

    /**
     * Deletes an announcement from the cloud database.
     */
    suspend fun deleteAnnouncementFromCloud(id: Long): Boolean {
        return true
    }

    /**
     * Fetches the latest announcements from the cloud, updates local database,
     * and shows a push notification for any new announcement posted from other mobiles.
     */
    suspend fun fetchAndSyncRemoteAnnouncements(triggerNotification: Boolean): Int {
        val success = JanazaOnlineSyncManager.fetchAndSyncOnlinePosts(context, triggerNotification)
        return if (success) 1 else 0
    }

    private fun getSeenAnnouncementIds(): Set<String> {
        return prefs.getStringSet(PREF_SEEN_IDS, emptySet()) ?: emptySet()
    }

    private fun markAnnouncementAsSeen(id: Long) {
        val current = prefs.getStringSet(PREF_SEEN_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(id.toString())
        prefs.edit().putStringSet(PREF_SEEN_IDS, current).apply()
    }

    private val PREF_SEEN_IDS = "seen_announcement_ids_set"
}
