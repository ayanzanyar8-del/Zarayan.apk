package com.example.data.network

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.JanazaAnnouncement
import com.example.util.NotificationHelper
import com.example.util.ZarayenFirebaseMessagingService
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Firestore Real-time Single Source of Truth Service for Nwezhi Mrdu ("mrdu" collection).
 * Integrates native Firebase Firestore snapshot listeners, Room persistence, and Cloud Functions triggers.
 */
class MrduFirestoreService(
    private val context: Context,
    private val database: AppDatabase
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val prefs = context.getSharedPreferences("mrdu_firestore_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.IO)
    private var syncJob: Job? = null
    private var firestoreListener: ListenerRegistration? = null

    private fun isFirebaseConfigured(): Boolean {
        return try {
            val options = FirebaseOptions.fromResource(context)
            options != null && !options.projectId.isNullOrBlank() && options.projectId != "zarayen-app-sync"
        } catch (e: Exception) {
            false
        }
    }

    private fun getCloudDatabaseUrl(): String? {
        return try {
            val options = FirebaseOptions.fromResource(context)
            val dbUrl = options?.databaseUrl
            if (!dbUrl.isNullOrBlank() && !dbUrl.contains("zarayen-app-sync-default-rtdb")) {
                dbUrl.trimEnd('/')
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private val firestore: FirebaseFirestore? by lazy {
        try {
            if (!isFirebaseConfigured()) {
                null
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseFirestore instance init notice: ${e.message}")
            null
        }
    }

    private val _realtimePosts = MutableStateFlow<List<JanazaAnnouncement>>(emptyList())
    val realtimePosts: StateFlow<List<JanazaAnnouncement>> = _realtimePosts.asStateFlow()

    companion object {
        private const val TAG = "MrduFirestore"
        const val COLLECTION_MRDU = "mrdu"
        const val PREF_KNOWN_POST_IDS = "known_mrdu_post_ids"
    }

    /**
     * Starts continuous real-time listener on the Firestore "mrdu" collection.
     */
    fun startRealtimeListener() {
        if (!isFirebaseConfigured()) {
            Log.d(TAG, "Firebase not configured via google-services.json. Offline-first local database active.")
            return
        }

        // 1. Attach native Firestore SDK Snapshot Listener
        try {
            firestore?.let { db ->
                if (firestoreListener == null) {
                    firestoreListener = db.collection(COLLECTION_MRDU)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .addSnapshotListener { snapshot, error ->
                            if (error != null) {
                                Log.w(TAG, "Firestore snapshot warning: ${error.message}")
                                return@addSnapshotListener
                            }
                            if (snapshot != null) {
                                scope.launch(Dispatchers.IO) {
                                    handleFirestoreSnapshot(snapshot.documents)
                                }
                            }
                        }
                    Log.d(TAG, "Native FirebaseFirestore snapshot listener registered on '$COLLECTION_MRDU'")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to attach native snapshot listener: ${e.message}")
        }

        // 2. Background polling fallback only if cloud database url is configured
        val cloudUrl = getCloudDatabaseUrl() ?: return
        if (syncJob?.isActive == true) return
        syncJob = scope.launch {
            fetchAndSyncFirestore(triggerNotification = false)
            while (isActive) {
                delay(15000)
                try {
                    fetchAndSyncFirestore(triggerNotification = true)
                } catch (e: Exception) {
                    Log.w(TAG, "Sync loop notice: ${e.message}")
                }
            }
        }
    }

    private suspend fun handleFirestoreSnapshot(documents: List<com.google.firebase.firestore.DocumentSnapshot>) {
        try {
            val list = mutableListOf<JanazaAnnouncement>()
            val knownIds = getKnownPostIds()
            val remoteIdSet = mutableSetOf<Long>()

            for (doc in documents) {
                val id = doc.getLong("id") ?: doc.id.toLongOrNull() ?: continue
                val deceasedName = doc.getString("deceasedName") ?: doc.getString("title") ?: continue
                if (deceasedName.isBlank()) continue

                val announcement = JanazaAnnouncement(
                    id = id,
                    deceasedName = deceasedName,
                    description = doc.getString("description") ?: "",
                    mosqueName = doc.getString("mosqueName") ?: doc.getString("location") ?: "مزگەوتی گەورەی زەڕایەن",
                    cemeteryName = doc.getString("cemeteryName") ?: "گۆڕستانی زەڕایەن",
                    prayerTime = doc.getString("prayerTime") ?: "دوای نوێژ",
                    condolenceLocation = doc.getString("condolenceLocation") ?: "شارۆچکەی زەڕایەن",
                    dateString = doc.getString("dateString") ?: doc.getString("date") ?: "",
                    timestamp = doc.getLong("timestamp") ?: doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    contactPhone = doc.getString("contactPhone") ?: "",
                    authorName = doc.getString("authorName") ?: doc.getString("createdBy") ?: "ئەدمینی زەڕایەن",
                    likesCount = (doc.getLong("likesCount") ?: 1L).toInt(),
                    isLiked = false
                )

                remoteIdSet.add(id)
                list.add(announcement)
                database.janazaDao().insertAnnouncement(announcement)

                if (!knownIds.contains(id.toString())) {
                    markPostAsKnown(id)
                    NotificationHelper.showJanazaNotification(
                        context = context,
                        deceasedName = announcement.deceasedName,
                        mosqueName = announcement.mosqueName,
                        prayerTime = announcement.prayerTime
                    )
                }
            }

            if (list.isNotEmpty()) {
                val localItems = database.janazaDao().getAllAnnouncementsList()
                for (local in localItems) {
                    if (!remoteIdSet.contains(local.id) && local.timestamp < (System.currentTimeMillis() - 60000)) {
                        database.janazaDao().deleteById(local.id)
                    }
                }
                _realtimePosts.value = list
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling Firestore snapshot: ${e.message}")
        }
    }

    fun stopRealtimeListener() {
        firestoreListener?.remove()
        firestoreListener = null
        syncJob?.cancel()
        syncJob = null
    }

    /**
     * Publishes a new MRDU post to Firestore "mrdu" collection.
     */
    suspend fun createMrduPost(announcement: JanazaAnnouncement): Boolean {
        return withContext(Dispatchers.IO) {
            var directFirestoreSuccess = false
            try {
                firestore?.let { db ->
                    val data = hashMapOf(
                        "id" to announcement.id,
                        "title" to announcement.deceasedName,
                        "deceasedName" to announcement.deceasedName,
                        "description" to announcement.description,
                        "location" to announcement.mosqueName,
                        "mosqueName" to announcement.mosqueName,
                        "cemeteryName" to announcement.cemeteryName,
                        "prayerTime" to announcement.prayerTime,
                        "condolenceLocation" to announcement.condolenceLocation,
                        "date" to announcement.dateString,
                        "dateString" to announcement.dateString,
                        "timestamp" to announcement.timestamp,
                        "createdAt" to announcement.timestamp,
                        "updatedAt" to announcement.timestamp,
                        "contactPhone" to announcement.contactPhone,
                        "createdBy" to announcement.authorName,
                        "authorName" to announcement.authorName,
                        "likesCount" to announcement.likesCount,
                        "imageUrl" to ""
                    )
                    db.collection(COLLECTION_MRDU)
                        .document(announcement.id.toString())
                        .set(data, SetOptions.merge())
                        .await()
                    directFirestoreSuccess = true
                    Log.d(TAG, "Direct Firestore write succeeded for doc ${announcement.id}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Direct Firestore write fallback: ${e.message}")
            }

            // HTTP Realtime DB fallback & sync only if real cloud database configured
            val cloudUrl = getCloudDatabaseUrl()
            val httpSuccess = if (cloudUrl != null) {
                try {
                    val json = JSONObject().apply {
                        put("id", announcement.id)
                        put("title", announcement.deceasedName)
                        put("deceasedName", announcement.deceasedName)
                        put("description", announcement.description)
                        put("location", announcement.mosqueName)
                        put("mosqueName", announcement.mosqueName)
                        put("cemeteryName", announcement.cemeteryName)
                        put("prayerTime", announcement.prayerTime)
                        put("condolenceLocation", announcement.condolenceLocation)
                        put("date", announcement.dateString)
                        put("dateString", announcement.dateString)
                        put("timestamp", announcement.timestamp)
                        put("createdAt", announcement.timestamp)
                        put("updatedAt", announcement.timestamp)
                        put("contactPhone", announcement.contactPhone)
                        put("createdBy", announcement.authorName)
                        put("authorName", announcement.authorName)
                        put("likesCount", announcement.likesCount)
                        put("imageUrl", "")
                    }

                    val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                    val mrduUrl = "$cloudUrl/mrdu/${announcement.id}.json"
                    val requestMrdu = Request.Builder().url(mrduUrl).put(body).build()
                    val annUrl = "$cloudUrl/announcements/${announcement.id}.json"
                    val requestAnn = Request.Builder().url(annUrl).put(body).build()

                    val mrduOk = client.newCall(requestMrdu).execute().use { it.isSuccessful }
                    client.newCall(requestAnn).execute().close()
                    mrduOk
                } catch (e: Exception) {
                    Log.w(TAG, "HTTP publish notice: ${e.message}")
                    false
                }
            } else {
                false
            }

            val success = directFirestoreSuccess || httpSuccess || !isFirebaseConfigured()
            if (success) {
                markPostAsKnown(announcement.id)
                if (isFirebaseConfigured()) {
                    ZarayenFirebaseMessagingService.broadcastJanazaPushNotification(
                        context = context,
                        announcement = announcement,
                        action = ZarayenFirebaseMessagingService.ACTION_CREATE
                    )
                }
            }
            success
        }
    }

    /**
     * Updates an existing MRDU post in Firestore "mrdu" collection.
     */
    suspend fun updateMrduPost(announcement: JanazaAnnouncement): Boolean {
        return withContext(Dispatchers.IO) {
            var directFirestoreSuccess = false
            try {
                firestore?.let { db ->
                    val data = hashMapOf(
                        "id" to announcement.id,
                        "title" to announcement.deceasedName,
                        "deceasedName" to announcement.deceasedName,
                        "description" to announcement.description,
                        "location" to announcement.mosqueName,
                        "mosqueName" to announcement.mosqueName,
                        "cemeteryName" to announcement.cemeteryName,
                        "prayerTime" to announcement.prayerTime,
                        "condolenceLocation" to announcement.condolenceLocation,
                        "date" to announcement.dateString,
                        "dateString" to announcement.dateString,
                        "timestamp" to announcement.timestamp,
                        "updatedAt" to System.currentTimeMillis(),
                        "contactPhone" to announcement.contactPhone,
                        "createdBy" to announcement.authorName,
                        "authorName" to announcement.authorName,
                        "likesCount" to announcement.likesCount
                    )
                    db.collection(COLLECTION_MRDU)
                        .document(announcement.id.toString())
                        .set(data, SetOptions.merge())
                        .await()
                    directFirestoreSuccess = true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Direct Firestore update notice: ${e.message}")
            }

            val cloudUrl = getCloudDatabaseUrl()
            val httpSuccess = if (cloudUrl != null) {
                try {
                    val json = JSONObject().apply {
                        put("id", announcement.id)
                        put("title", announcement.deceasedName)
                        put("deceasedName", announcement.deceasedName)
                        put("description", announcement.description)
                        put("location", announcement.mosqueName)
                        put("mosqueName", announcement.mosqueName)
                        put("cemeteryName", announcement.cemeteryName)
                        put("prayerTime", announcement.prayerTime)
                        put("condolenceLocation", announcement.condolenceLocation)
                        put("date", announcement.dateString)
                        put("dateString", announcement.dateString)
                        put("timestamp", announcement.timestamp)
                        put("updatedAt", System.currentTimeMillis())
                        put("contactPhone", announcement.contactPhone)
                        put("createdBy", announcement.authorName)
                        put("authorName", announcement.authorName)
                        put("likesCount", announcement.likesCount)
                    }

                    val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                    val mrduUrl = "$cloudUrl/mrdu/${announcement.id}.json"
                    val requestMrdu = Request.Builder().url(mrduUrl).put(body).build()
                    val annUrl = "$cloudUrl/announcements/${announcement.id}.json"
                    val requestAnn = Request.Builder().url(annUrl).put(body).build()

                    val mrduOk = client.newCall(requestMrdu).execute().use { it.isSuccessful }
                    client.newCall(requestAnn).execute().close()
                    mrduOk
                } catch (e: Exception) {
                    Log.w(TAG, "HTTP update notice: ${e.message}")
                    false
                }
            } else {
                false
            }

            val success = directFirestoreSuccess || httpSuccess || !isFirebaseConfigured()
            if (success) {
                if (isFirebaseConfigured()) {
                    ZarayenFirebaseMessagingService.broadcastJanazaPushNotification(
                        context = context,
                        announcement = announcement,
                        action = ZarayenFirebaseMessagingService.ACTION_UPDATE
                    )
                }
            }
            success
        }
    }

    /**
     * Deletes an MRDU post from Firestore "mrdu" collection.
     */
    suspend fun deleteMrduPost(id: Long, deceasedName: String = ""): Boolean {
        return withContext(Dispatchers.IO) {
            var directFirestoreSuccess = false
            try {
                firestore?.let { db ->
                    db.collection(COLLECTION_MRDU)
                        .document(id.toString())
                        .delete()
                        .await()
                    directFirestoreSuccess = true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Direct Firestore delete notice: ${e.message}")
            }

            val cloudUrl = getCloudDatabaseUrl()
            val httpSuccess = if (cloudUrl != null) {
                try {
                    val mrduUrl = "$cloudUrl/mrdu/$id.json"
                    val requestMrdu = Request.Builder().url(mrduUrl).delete().build()
                    val annUrl = "$cloudUrl/announcements/$id.json"
                    val requestAnn = Request.Builder().url(annUrl).delete().build()

                    val mrduOk = client.newCall(requestMrdu).execute().use { it.isSuccessful }
                    client.newCall(requestAnn).execute().close()
                    mrduOk
                } catch (e: Exception) {
                    Log.w(TAG, "HTTP delete notice: ${e.message}")
                    false
                }
            } else {
                false
            }

            if (isFirebaseConfigured()) {
                val dummy = JanazaAnnouncement(
                    id = id,
                    deceasedName = deceasedName.ifEmpty { "ئاگاداری سڕاوە" },
                    description = "",
                    mosqueName = "",
                    cemeteryName = "",
                    prayerTime = "",
                    condolenceLocation = "",
                    dateString = "",
                    timestamp = System.currentTimeMillis(),
                    authorName = ""
                )
                ZarayenFirebaseMessagingService.broadcastJanazaPushNotification(
                    context = context,
                    announcement = dummy,
                    action = ZarayenFirebaseMessagingService.ACTION_DELETE
                )
            }
            directFirestoreSuccess || httpSuccess || !isFirebaseConfigured()
        }
    }

    /**
     * Fetches current Firestore "mrdu" collection, reconciles with Room, and triggers push notifications.
     */
    suspend fun fetchAndSyncFirestore(triggerNotification: Boolean): Int {
        val success = JanazaOnlineSyncManager.fetchAndSyncOnlinePosts(context, triggerNotification)
        return if (success) 1 else 0
    }

    private fun parseFirestoreJson(jsonStr: String): List<JanazaAnnouncement> {
        val list = mutableListOf<JanazaAnnouncement>()
        try {
            if (jsonStr.trim().startsWith("{")) {
                val root = JSONObject(jsonStr)
                val keys = root.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val item = root.optJSONObject(key) ?: continue
                    val id = item.optLong("id", key.toLongOrNull() ?: System.currentTimeMillis())
                    val deceasedName = item.optString("deceasedName", item.optString("title", ""))
                    if (deceasedName.isBlank()) continue

                    val announcement = JanazaAnnouncement(
                        id = id,
                        deceasedName = deceasedName,
                        description = item.optString("description", ""),
                        mosqueName = item.optString("mosqueName", item.optString("location", "مزگەوتی گەورەی زەڕایەن")),
                        cemeteryName = item.optString("cemeteryName", "گۆڕستانی زەڕایەن"),
                        prayerTime = item.optString("prayerTime", "دوای نوێژی داهاتوو"),
                        condolenceLocation = item.optString("condolenceLocation", "شارۆچکەی زەڕایەن"),
                        dateString = item.optString("dateString", item.optString("date", "")),
                        timestamp = item.optLong("timestamp", item.optLong("createdAt", System.currentTimeMillis())),
                        contactPhone = item.optString("contactPhone", ""),
                        authorName = item.optString("authorName", item.optString("createdBy", "ئەدمینی زەڕایەن")),
                        likesCount = item.optInt("likesCount", 1),
                        isLiked = false
                    )
                    list.add(announcement)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Firestore JSON: ${e.message}")
        }
        return list.sortedByDescending { it.timestamp }
    }

    private fun getKnownPostIds(): Set<String> {
        return prefs.getStringSet(PREF_KNOWN_POST_IDS, emptySet()) ?: emptySet()
    }

    private fun markPostAsKnown(id: Long) {
        val current = prefs.getStringSet(PREF_KNOWN_POST_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(id.toString())
        prefs.edit().putStringSet(PREF_KNOWN_POST_IDS, current).apply()
    }
}
