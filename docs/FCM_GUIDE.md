# Valley Rush — Push Notifications (FCM) Guide

## What is "live FCM"?
**FCM = Firebase Cloud Messaging**, Google's free service for sending push
notifications to phones. It's the standard way both Android and iOS apps receive
messages like "Your order is confirmed" or "Deal of the day".

How it works, end to end:
```
Your backend  ──(send: title+body+token)──►  FCM (Google)  ──►  the user's phone
     ▲                                                              │
     └──────────  the app registers its device "token"  ◄──────────┘
```
1. The app asks the OS for notification permission and gets a unique **device
   token** from FCM.
2. The app sends that token to our backend: `POST /api/devices/token` (already
   built — `DeviceController` / `PushService`).
3. When something happens (order confirmed, etc.), the backend calls
   `PushService.sendToUser(userId, title, body)`, which sends via FCM to every
   token that user has.

**Current status:** everything except the Google credentials is built. The
backend stores tokens and, in **dev-mode**, *logs* the notification instead of
sending it. The Flutter side has `DeviceService` ready to register a token. To go
"live" you create a Firebase project and drop in the config — steps below.

## What you need to create (one-time)
1. **Firebase project** — console.firebase.google.com → Add project.
2. **Android app** in it (package `com.valleyrush.customer`) → download
   **`google-services.json`** → put in `android/app/`.
3. **iOS app** (bundle id `com.valleyrush.customer`) → download
   **`GoogleService-Info.plist`** → add to `ios/Runner/` (via Xcode). For iOS push
   you also upload an **APNs Auth Key (.p8)** (from your Apple Developer account)
   into Firebase → Project settings → Cloud Messaging.
4. **Service account key** (server) — Firebase → Project settings → Service
   accounts → *Generate new private key* → save the JSON somewhere secure on the
   server and set `FCM_SERVICE_ACCOUNT_JSON=/path/to/that.json` in the backend `.env`.

## Wiring it on (Flutter side) — the remaining code
Add the plugins and the Google services Gradle plugin, then register the token.

1. `pubspec.yaml` → add `firebase_core` and `firebase_messaging`, run `flutter pub get`.
2. Android: apply the Google services plugin (only after `google-services.json`
   exists, or the build fails):
   - `android/settings.gradle.kts` plugins: add
     `id("com.google.gms.google-services") version "4.4.2" apply false`
   - `android/app/build.gradle.kts` plugins: add `id("com.google.gms.google-services")`
3. Initialize + register on app start / after login:
   ```dart
   // main.dart, before runApp:
   await Firebase.initializeApp();

   // after the user logs in (e.g. in auth flow):
   final messaging = FirebaseMessaging.instance;
   await messaging.requestPermission();
   final token = await messaging.getToken();
   if (token != null) await DeviceService.register(token); // already implemented
   messaging.onTokenRefresh.listen(DeviceService.register);
   ```
4. Backend: with `FCM_SERVICE_ACCOUNT_JSON` set, implement the FCM HTTP v1 send in
   `PushService.sendToUser()` (the TODO there) — obtain an OAuth token from the
   service account and POST to
   `https://fcm.googleapis.com/v1/projects/<project-id>/messages:send` per token.

That's it — order confirmations (and any future promo/cart-abandonment messages)
will then arrive as real push notifications. Until then, the flow is fully wired
and simply logs on the server, so nothing is blocked.

## Test checklist (once live)
- [ ] `google-services.json` + `GoogleService-Info.plist` in place
- [ ] APNs key uploaded to Firebase (iOS)
- [ ] `FCM_SERVICE_ACCOUNT_JSON` set on the server
- [ ] App requests permission and registers a token (`device_tokens` row appears)
- [ ] Place a COD order → the phone receives "Order confirmed"
