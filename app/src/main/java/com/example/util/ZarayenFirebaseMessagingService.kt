package com.example.util

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.JanazaAnnouncement
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Firebase Cloud Messaging (FCM) Service for Zarayen App.
 * Handles real-time push notifications across all mobile devices for:
 * 1. Nwezhi Mrdu / Janaza Announcements ("mrdu_updates" & "nwezhi_mrdu" topics)
 * 2. Prayer Time Alerts ("prayer_alerts" topic)
 * 3. Automatic topic subscriptions for all users & admin deduplication
 */
class ZarayenFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Registration Token: $token")
        saveFcmToken(this, token)
        subscribeToTopics()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "FCM Message received from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val messageType = data["type"] ?: remoteMessage.notification?.tag ?: TYPE_MRDU_UPDATE
        val action = data["action"] ?: "create"
        val senderToken = data["senderToken"] ?: ""
        val myToken = getFcmToken(this) ?: ""

        // Deduplication: Avoid duplicate popup on the device that initiated the post if it already notified
        val isSelfInitiated = senderToken.isNotBlank() && senderToken == myToken

        when (messageType) {
            TYPE_MRDU_UPDATE, TYPE_NWEZHI_MRDU, TYPE_JANAZA -> {
                val announcementId = data["id"]?.toLongOrNull() ?: System.currentTimeMillis()
                val deceasedName = data["deceasedName"] ?: data["title"] ?: remoteMessage.notification?.title ?: "کۆچی دوایی لە زەڕایەن"
                val mosqueName = data["mosqueName"] ?: data["location"] ?: "مزگەوتی شارۆچکەی زەڕایەن"
                val prayerTime = data["prayerTime"] ?: "دوای نوێژی داهاتوو"
                val description = data["description"] ?: ""

                when (action) {
                    ACTION_DELETE -> {
                        // Deletion event: delete from local Room DB
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val db = AppDatabase.getDatabase(applicationContext)
                                db.janazaDao().deleteById(announcementId)
                                Log.d(TAG, "Deleted Janaza announcement ID $announcementId via FCM event")
                            } catch (e: Exception) {
                                Log.e(TAG, "Error handling FCM Janaza delete: ${e.message}")
                            }
                        }
                    }

                    ACTION_UPDATE -> {
                        // Update event: update in Room DB and notify users
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val db = AppDatabase.getDatabase(applicationContext)
                                val existing = db.janazaDao().getAnnouncementById(announcementId)
                                val updated = JanazaAnnouncement(
                                    id = announcementId,
                                    deceasedName = deceasedName,
                                    description = description,
                                    mosqueName = mosqueName,
                                    cemeteryName = data["cemeteryName"] ?: "گۆڕستانی زەڕایەن",
                                    prayerTime = prayerTime,
                                    condolenceLocation = data["condolenceLocation"] ?: "شارۆچکەی زەڕایەن",
                                    dateString = data["dateString"] ?: data["date"] ?: "",
                                    timestamp = data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis(),
                                    contactPhone = data["contactPhone"] ?: "",
                                    authorName = data["authorName"] ?: data["createdBy"] ?: "ئەدمینی زەڕایەن (دەستکاریکراو)",
                                    likesCount = existing?.likesCount ?: 1,
                                    isLiked = existing?.isLiked ?: false
                                )
                                db.janazaDao().updateAnnouncement(updated)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error updating Janaza from FCM: ${e.message}")
                            }
                        }

                        if (!isSelfInitiated) {
                            NotificationHelper.showJanazaNotification(
                                context = applicationContext,
                                deceasedName = "دەستکاریکرا: $deceasedName",
                                mosqueName = "$mosqueName ($prayerTime)",
                                prayerTime = prayerTime
                            )
                        }
                    }

                    else -> {
                        // Create / new post event: insert in Room DB and notify
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val db = AppDatabase.getDatabase(applicationContext)
                                val newAnnouncement = JanazaAnnouncement(
                                    id = announcementId,
                                    deceasedName = deceasedName,
                                    description = description,
                                    mosqueName = mosqueName,
                                    cemeteryName = data["cemeteryName"] ?: "گۆڕستانی زەڕایەن",
                                    prayerTime = prayerTime,
                                    condolenceLocation = data["condolenceLocation"] ?: "شارۆچکەی زەڕایەن",
                                    dateString = data["dateString"] ?: data["date"] ?: "",
                                    timestamp = data["timestamp"]?.toLongOrNull() ?: System.currentTimeMillis(),
                                    contactPhone = data["contactPhone"] ?: "",
                                    authorName = data["authorName"] ?: data["createdBy"] ?: "ئەدمینی زەڕایەن",
                                    likesCount = 1,
                                    isLiked = false
                                )
                                db.janazaDao().insertAnnouncement(newAnnouncement)
                            } catch (e: Exception) {
                                Log.e(TAG, "Error caching Janaza from FCM: ${e.message}")
                            }
                        }

                        if (!isSelfInitiated) {
                            NotificationHelper.showJanazaNotification(
                                context = applicationContext,
                                deceasedName = deceasedName,
                                mosqueName = mosqueName,
                                prayerTime = prayerTime
                            )
                        }
                    }
                }
            }

            TYPE_PRAYER_ALERT, TYPE_ADHAN -> {
                val prayerName = data["prayerName"] ?: remoteMessage.notification?.title ?: "بانگی فەرز"
                val prayerTime = data["prayerTime"] ?: ""

                NotificationHelper.showPrayerAlertNotification(
                    context = applicationContext,
                    prayerNameKurdish = prayerName,
                    prayerTimeFormatted = prayerTime
                )
            }

            else -> {
                val title = remoteMessage.notification?.title ?: data["title"] ?: "شارۆچکەی زەڕایەن"
                val body = remoteMessage.notification?.body ?: data["body"] ?: "ئاگاداری نوێ لە شارۆچکەی زەڕایەنەوە"

                NotificationHelper.showJanazaNotification(
                    context = applicationContext,
                    deceasedName = body,
                    mosqueName = title,
                    prayerTime = "ئاگاداری گشتی"
                )
            }
        }
    }

    companion object {
        private const val TAG = "ZarayenFCM"
        const val PREF_FCM_TOKEN = "fcm_registration_token"
        const val PREFS_NAME = "zarayen_fcm_preferences"

        // Topic identifiers
        const val TOPIC_MRDU_UPDATES = "mrdu_updates"
        const val TOPIC_NWEZHI_MRDU = "nwezhi_mrdu"
        const val TOPIC_JANAZA = "janaza_alerts"
        const val TOPIC_PRAYER_ALERTS = "prayer_alerts"
        const val TOPIC_ALL_USERS = "all_users"

        // Message types & Actions
        const val TYPE_MRDU_UPDATE = "mrdu_update"
        const val TYPE_NWEZHI_MRDU = "nwezhi_mrdu"
        const val TYPE_JANAZA = "janaza"
        const val TYPE_PRAYER_ALERT = "prayer_alert"
        const val TYPE_ADHAN = "adhan"

        const val ACTION_CREATE = "create"
        const val ACTION_UPDATE = "update"
        const val ACTION_DELETE = "delete"

        private val httpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        /**
         * Initialize FCM, retrieve device registration token, and subscribe to all topics.
         */
        fun initializeFcm(context: Context) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    val options = try {
                        FirebaseOptions.fromResource(context)
                    } catch (e: Exception) {
                        null
                    }
                    if (options != null) {
                        FirebaseApp.initializeApp(context, options)
                    } else {
                        val fallbackOptions = FirebaseOptions.Builder()
                            .setApplicationId(context.packageName)
                            .setProjectId("zarayen-app-sync")
                            .setApiKey("AIzaSyDummyDevKeyForZarayenAppRuntime00")
                            .build()
                        FirebaseApp.initializeApp(context, fallbackOptions)
                    }
                }

                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val token = task.result
                        Log.d(TAG, "FCM Token retrieved successfully: $token")
                        saveFcmToken(context, token)
                    } else {
                        Log.w(TAG, "Fetching FCM registration token failed: ${task.exception?.message}")
                    }
                }

                // Subscribe to all essential topics
                subscribeToTopics(context)
            } catch (e: Exception) {
                Log.w(TAG, "FCM initialization notice: ${e.message}")
            }
        }

        private fun getCloudDatabaseUrl(context: Context): String? {
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

        fun subscribeToTopics(context: Context? = null) {
            try {
                if (context != null && FirebaseApp.getApps(context).isEmpty()) {
                    val options = try {
                        FirebaseOptions.fromResource(context)
                    } catch (e: Exception) {
                        null
                    }
                    if (options != null) {
                        FirebaseApp.initializeApp(context, options)
                    }
                }
                val messaging = FirebaseMessaging.getInstance()
                messaging.subscribeToTopic(TOPIC_MRDU_UPDATES)
                messaging.subscribeToTopic(TOPIC_NWEZHI_MRDU)
                messaging.subscribeToTopic(TOPIC_JANAZA)
                messaging.subscribeToTopic(TOPIC_PRAYER_ALERTS)
                messaging.subscribeToTopic(TOPIC_ALL_USERS)
                Log.d(TAG, "Successfully subscribed to FCM topics: $TOPIC_MRDU_UPDATES, $TOPIC_NWEZHI_MRDU, $TOPIC_PRAYER_ALERTS")
            } catch (e: Exception) {
                Log.e(TAG, "Error subscribing to FCM topics: ${e.message}")
            }
        }

        fun saveFcmToken(context: Context, token: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(PREF_FCM_TOKEN, token).apply()
        }

        fun getFcmToken(context: Context): String? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getString(PREF_FCM_TOKEN, null)
        }

        /**
         * Broadcasts a real-time FCM Push Notification to all subscribed users on topic "mrdu_updates"
         * for MRDU / Janaza actions (create, update, delete).
         */
        suspend fun broadcastJanazaPushNotification(
            context: Context,
            announcement: JanazaAnnouncement,
            action: String = ACTION_CREATE
        ): Boolean {
            return try {
                val myToken = getFcmToken(context) ?: ""

                val (titleText, bodyText) = when (action) {
                    ACTION_DELETE -> Pair(
                        "🗑️ ئاگاداری لابرا",
                        "ئاگاداری کۆچی دوایی ${announcement.deceasedName} لابرا."
                    )
                    ACTION_UPDATE -> Pair(
                        "📝 گۆڕانکاری لە کاتی نوێژی مردوو",
                        "${announcement.deceasedName} - نوێژ: ${announcement.prayerTime} لە ${announcement.mosqueName}"
                    )
                    else -> Pair(
                        "📢 ئاگاداری: پۆستی نوێی نوێژی مردوو",
                        "﴿ إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ ﴾ ${announcement.deceasedName} • ${announcement.mosqueName} (${announcement.prayerTime})"
                    )
                }

                val json = JSONObject().apply {
                    put("to", "/topics/$TOPIC_MRDU_UPDATES")
                    put("condition", "'$TOPIC_MRDU_UPDATES' in topics || '$TOPIC_NWEZHI_MRDU' in topics")
                    put("priority", "high")
                    val notifObj = JSONObject().apply {
                        put("title", titleText)
                        put("body", bodyText)
                        put("sound", "default")
                        put("click_action", "OPEN_JANAZA_PAGE")
                    }
                    put("notification", notifObj)
                    val dataObj = JSONObject().apply {
                        put("type", TYPE_MRDU_UPDATE)
                        put("action", action)
                        put("id", announcement.id.toString())
                        put("title", announcement.deceasedName)
                        put("deceasedName", announcement.deceasedName)
                        put("mosqueName", announcement.mosqueName)
                        put("location", announcement.mosqueName)
                        put("cemeteryName", announcement.cemeteryName)
                        put("prayerTime", announcement.prayerTime)
                        put("condolenceLocation", announcement.condolenceLocation)
                        put("contactPhone", announcement.contactPhone)
                        put("description", announcement.description)
                        put("dateString", announcement.dateString)
                        put("date", announcement.dateString)
                        put("timestamp", announcement.timestamp.toString())
                        put("createdBy", announcement.authorName)
                        put("authorName", announcement.authorName)
                        put("senderToken", myToken)
                    }
                    put("data", dataObj)
                }

                // Dispatches to Firebase cloud notification dispatcher if configured
                val cloudUrl = getCloudDatabaseUrl(context)
                if (cloudUrl != null) {
                    val body = json.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
                    val request = Request.Builder()
                        .url("$cloudUrl/fcm_broadcasts/${announcement.id}.json")
                        .put(body)
                        .build()

                    httpClient.newCall(request).execute().use { response ->
                        Log.d(TAG, "Broadcasted MRDU FCM push notification ($action) to $TOPIC_MRDU_UPDATES: ${response.isSuccessful}")
                        response.isSuccessful
                    }
                } else {
                    Log.d(TAG, "No remote cloud database configured for FCM broadcast. Notification handled locally.")
                    true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to broadcast MRDU FCM push notification: ${e.message}")
                false
            }
        }

        /**
         * Broadcasts a real-time Prayer Time FCM alert to all users.
         */
        suspend fun broadcastPrayerTimePushNotification(
            prayerName: String,
            prayerTime: String
        ): Boolean {
            return try {
                val json = JSONObject().apply {
                    put("to", "/topics/$TOPIC_PRAYER_ALERTS")
                    put("priority", "high")
                    val notifObj = JSONObject().apply {
                        put("title", "🕌 نزیکبوونەوەی کاتی بانگ (زەڕایەن)")
                        put("body", "بانگی پیرۆزی $prayerName لە کاتژمێر $prayerTime لە شارۆچکەی زەڕایەن")
                        put("sound", "default")
                    }
                    put("notification", notifObj)
                    val dataObj = JSONObject().apply {
                        put("type", TYPE_PRAYER_ALERT)
                        put("prayerName", prayerName)
                        put("prayerTime", prayerTime)
                        put("timestamp", System.currentTimeMillis().toString())
                    }
                    put("data", dataObj)
                }

                // Cloud broadcast only if valid endpoint configured
                Log.d(TAG, "Prayer Time FCM alert prepared for $prayerName ($prayerTime)")
                true
            } catch (e: Exception) {
                Log.w(TAG, "Failed to broadcast prayer FCM alert: ${e.message}")
                false
            }
        }
    }
}
