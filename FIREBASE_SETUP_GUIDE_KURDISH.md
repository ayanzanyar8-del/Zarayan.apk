# 📱 سیستەمی نوێژی مردوو (Nwezhi Mrdu) و پەخشی ئاگاداری ڕاستەوخۆ (FCM Push Notifications)

ئەم بەڵگەنامەیە ڕوونکردنەوەی تەواوە لەسەر چۆنیەتی کارکردنی لاپەڕەی **نوێژی مردوو** وەک کەناڵێکی **تێلیگرام / ڤایبەر** کە لەگەڵ پۆستکردنی هەر پەیامێک لەلایەن ئەدمینەوە، دەستبەجێ ئاگاداری (Push Notification) بە دەنگ و لەرزین دەگاتە دەستی هەموو بەکارهێنەران چ کاتێک بەرنامەکە کراوەیە یان داخراوە.

---

## 🏗️ نەخشەی سیستەمەکە (Architecture)

```
[ ئەدمین پۆستی نوێژی مردوو دەکات ]
              │
              ▼
[ Firebase Firestore ("mrdu") & Realtime Database ]
              │
              ▼
[ Firebase Cloud Functions (index.js) ]
              │
   (پەخشکردن بۆ سەبسکرایبەرانی topic "mrdu_updates")
              ▼
[ هەموو بەکارهێنەرانی ئەپ لە زەڕایەن ]
  ├── 📱 ئەپ داخراوە: Notification دێتە سەر شاشە و زەنگ لێدەدات. کلیکی لێبکرێت لاپەڕەی نوێژی مردوو دەکرێتەوە.
  └── 📱 ئەپ کراوەیە: لە چاتی کەناڵەکەدا ڕاستەوخۆ پەیامەکە دەردەکەوێت و نوێ دەبێتەوە.
```

---

## 🚀 هەنگاوەکانی چالاککردن لەسەر کۆنسۆڵی فایەربەیس (Firebase Console Setup)

### هەنگاوی ١: دابەزاندنی فایلی ناسێنەر
1. بچۆ ناو [Firebase Console](https://console.firebase.google.com).
2. پڕۆژەیەکی نوێ بە ناوی `Zarayen App` دروست بکە.
3. ئەپی ئەندرۆید زیاد بکە بە پاکێج ناوی:
   `com.aistudio.zarayen.kzvmpq`
4. فایلی `google-services.json` دابگرە و بیخەرە ناو بوخچەی سەرەکی پرۆژەکە لە تەنیشت `app/build.gradle.kts`.

### هەنگاوی ٢: دیپلۆیکردنی Cloud Functions
فایلی ئامادەکراو لەناو پڕۆژەکەدا هەیە لە بوخچەی `/firebase_functions/index.js`.
بۆ دیپلۆیکردنی لە کۆمپیوتەرەکەتدا تەنها ئەم فەرمانانە لێبدە:
```bash
npm install -g firebase-tools
firebase login
firebase init functions
# فایلی index.js لە ناو functions دابنێ
firebase deploy --only functions
```

---

## ⚡ کەناڵەکان و Topic سەبسکرایب لە ئەپەکەدا
- کەناڵی فەرمی نوێژی مردوو: `mrdu_updates` و `nwezhi_mrdu`
- کەناڵی ئاگاداری بانگەکان: `prayer_alerts`
- کەناڵی گشتی: `all_users`
