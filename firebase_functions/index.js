/**
 * Firebase Cloud Functions for Zarayen App (شارۆچکەی زەڕایەن)
 * Real-time Broadcast Engine for Nwezhi Mrdu (نوێژی مردوو) & Prayer Times
 * 
 * Instructions to deploy to your Firebase project:
 * 1. Install Firebase CLI: npm install -g firebase-tools
 * 2. Login: firebase login
 * 3. Initialize functions: firebase init functions (choose JavaScript)
 * 4. Replace functions/index.js with this file
 * 5. Deploy: firebase deploy --only functions
 */

const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

/**
 * 1. Realtime Database Trigger: Triggers immediately when Admin posts a new Janaza announcement
 * Sends high-priority push notification with sound & vibration to ALL devices even when closed.
 */
exports.onNewJanazaAnnouncement = functions.database
  .ref("/announcements/{announcementId}")
  .onCreate(async (snapshot, context) => {
    const data = snapshot.val();
    if (!data || !data.deceasedName) return null;

    const deceasedName = data.deceasedName;
    const mosqueName = data.mosqueName || "مزگەوتەکانی زەڕایەن";
    const prayerTime = data.prayerTime || "دوای نوێژ";

    const payload = {
      topic: "mrdu_updates",
      notification: {
        title: "📢 ئاگاداری نوێژی مردوو لە زەڕایەن",
        body: `﴿ إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ ﴾ ${deceasedName} - ${mosqueName} (${prayerTime})`
      },
      data: {
        type: "mrdu_update",
        action: "create",
        id: String(data.id || context.params.announcementId),
        deceasedName: String(deceasedName),
        description: String(data.description || ""),
        mosqueName: String(mosqueName),
        cemeteryName: String(data.cemeteryName || "گۆڕستانی زەڕایەن"),
        prayerTime: String(prayerTime),
        condolenceLocation: String(data.condolenceLocation || "شارۆچکەی زەڕایەن"),
        dateString: String(data.dateString || ""),
        timestamp: String(data.timestamp || Date.now()),
        contactPhone: String(data.contactPhone || ""),
        authorName: String(data.authorName || "ئەدمینی زەڕایەن")
      },
      android: {
        priority: "high",
        notification: {
          channelId: "zarayen_janaza_alerts_channel",
          sound: "default",
          defaultSound: true,
          defaultVibrateTimings: true,
          visibility: "public"
        }
      }
    };

    try {
      const response = await admin.messaging().send(payload);
      console.log("Successfully broadcasted Janaza notification:", response);
      return response;
    } catch (error) {
      console.error("Error sending push notification:", error);
      return null;
    }
  });

/**
 * 2. Firestore Trigger for the 'mrdu' collection (Cloud Firestore)
 */
exports.onFirestoreMrduCreated = functions.firestore
  .document("mrdu/{docId}")
  .onCreate(async (snapshot, context) => {
    const data = snapshot.data();
    if (!data) return null;

    const deceasedName = data.deceasedName || data.title || "کۆچی دوایی لە زەڕایەن";
    const mosqueName = data.mosqueName || data.location || "مزگەوتی گەورەی زەڕایەن";
    const prayerTime = data.prayerTime || "دوای نوێژ";

    const payload = {
      topic: "mrdu_updates",
      notification: {
        title: "📢 ئاگاداری نوێژی مردوو لە زەڕایەن",
        body: `﴿ إِنَّا لِلَّهِ وَإِنَّا إِلَيْهِ رَاجِعُونَ ﴾ ${deceasedName} - ${mosqueName} (${prayerTime})`
      },
      data: {
        type: "mrdu_update",
        action: "create",
        id: String(data.id || context.params.docId),
        deceasedName: String(deceasedName),
        description: String(data.description || ""),
        mosqueName: String(mosqueName),
        cemeteryName: String(data.cemeteryName || "گۆڕستانی زەڕایەن"),
        prayerTime: String(prayerTime),
        condolenceLocation: String(data.condolenceLocation || "شارۆچکەی زەڕایەن"),
        dateString: String(data.dateString || data.date || ""),
        timestamp: String(data.timestamp || data.createdAt || Date.now()),
        contactPhone: String(data.contactPhone || ""),
        authorName: String(data.authorName || data.createdBy || "ئەدمینی زەڕایەن")
      },
      android: {
        priority: "high",
        notification: {
          channelId: "zarayen_janaza_alerts_channel",
          sound: "default",
          defaultSound: true,
          defaultVibrateTimings: true,
          visibility: "public"
        }
      }
    };

    try {
      const response = await admin.messaging().send(payload);
      console.log("Firestore push sent:", response);
      return response;
    } catch (error) {
      console.error("Error sending Firestore push:", error);
      return null;
    }
  });
