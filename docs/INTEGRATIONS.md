# KMR Marketplace — Integrations & Credentials Guide

> Everything you (the client) need to create accounts for, and the exact `.env`
> keys to paste. The code already reads all of these; where a key is missing the
> app runs in a safe **dev-mode** fallback so nothing is blocked while you set up.
>
> All secrets go in **`marketplace/.env`** (backend, git-ignored). Mobile-app
> config files (Firebase/Google) go in the Flutter project as noted.
> Legend: 🔴 required for launch · 🟡 recommended · ⚪ later phase.

---

## 1. Payments — Razorpay 🔴  (UPI + Credit/Debit cards + Net Banking + Wallets)

**Why:** the online payment option at checkout. Razorpay's Standard Checkout shows
**all** methods (UPI, cards, net banking, wallets, EMI, Pay Later) in one sheet —
you do **not** build per-method screens; you enable the methods in the dashboard
and they appear automatically.

**Steps**
1. Create an account at **razorpay.com** → complete **KYC / business verification**
   (bank account, PAN, GST if any). *Test mode works immediately without KYC; live
   mode needs KYC approved.*
2. Dashboard → **Settings → API Keys → Generate Key**. You get:
   - `Key Id`  → `rzp_test_xxxx` (test) or `rzp_live_xxxx` (live)
   - `Key Secret` (shown once — copy it)
3. Dashboard → **Settings → Configuration / Payment Methods** → turn ON: **UPI,
   Cards (credit+debit), Net Banking, Wallets** (and EMI / Pay Later if you want).
4. (Recommended) Dashboard → **Settings → Webhooks** → add
   `https://<your-backend>/api/payments/webhook`, select `payment.captured` &
   `payment.failed`, set a secret. *(Webhook handler is a small later add so
   payment status is captured even if the app closes mid-flow.)*

**.env**
```
RAZORPAY_KEY_ID=rzp_test_xxxxxxxx
RAZORPAY_KEY_SECRET=xxxxxxxxxxxxxxxx
# RAZORPAY_WEBHOOK_SECRET=xxxx      # when you add the webhook
```
Also paste the **Key Id** into the Flutter app (public, safe): it's returned by the
backend at checkout, so no separate mobile config is needed.

**Vendor payment split (the marketplace feature) — Razorpay Route ⚪**
Today the backend **records** each order's split (each shop's payout vs the platform
delivery fee) — see `order` detail `shopSettlements`. To **auto-transfer** each
vendor's money to *their* bank account, enable **Razorpay Route**:
1. Dashboard → **Route** → request activation.
2. For each vendor, create a **Linked Account** (their business/bank KYC).
3. At payment time the backend adds `transfers[]` to route money to each linked
   account and keep the delivery fee in the platform account.
This needs every vendor onboarded/KYC'd, so it lands with the **Seller app**. Until
then, settlement is manual using the recorded breakdown. No new key beyond Route being
enabled on the same account.

---

## 2. OTP / SMS — AWS SNS 🔴  (phone verification, order SMS)

**Why:** the signup/login OTP and order SMS. Currently **dev-mode** prints the OTP to
the server log; flip to live when SNS is ready.

**Steps**
1. You already have an IAM user (keys in `.env`). Attach **`AmazonSNSFullAccess`**
   (or a scoped `sns:Publish`) to it.
2. **India (DLT) requirement:** to send SMS to Indian (+91) numbers, AWS SNS needs
   **DLT registration** (TRAI): register your **Entity Id**, **Sender Id**, and
   **message templates** on a DLT portal (Jio/Airtel/Vi), then add them in
   **SNS → Text messaging (SMS) → India settings**. Also raise the **SNS SMS spending
   limit** and move out of the SMS sandbox for production volume.
3. Set `DEV_MODE=false` to actually send.

**.env**
```
ACCESS_KEY=...            # already set
SECRET_ACCESS_KEY=...     # already set
AWS_REGION=ap-south-1
DEV_MODE=false            # true = OTP printed to server log (no SMS sent)
# SMS_SENDER_ID=KMRSHOP   # your approved DLT sender id (optional, added when live)
```
*Alternative if DLT is slow:* MSG91 / Twilio work too, but you asked for AWS-first, so
SNS + DLT is the primary path.

---

## 3. Email — AWS SES 🟡  (order confirmation, password reset)

**Why:** order confirmation and account emails.

**Steps**
1. **SES → Verified identities** → verify your sending domain (best) or a single
   from-address (e.g. `orders@yourdomain.com`).
2. **Request production access** (SES starts in sandbox — can only email verified
   addresses until you do).
3. Attach `ses:SendEmail` to the IAM user.

**.env**
```
MAIL_FROM=orders@yourdomain.com
# AWS keys reused from above
```
*(Backend SES sender is a small later add; the key set is ready.)*

---

## 4. Push notifications — Firebase Cloud Messaging (FCM) 🟡

**Why:** order-status pushes, promos, cart-abandonment. Standard for Flutter on both
Android + iOS.

**Steps**
1. Create a **Firebase project** (console.firebase.google.com).
2. Add an **Android app** (package `com.kmr.marketplace` or your id) → download
   **`google-services.json`** → put in `android/app/`.
3. Add an **iOS app** → download **`GoogleService-Info.plist`** → put in
   `ios/Runner/`. For iOS push you also need an **APNs Auth Key (.p8)** from the
   Apple Developer account, uploaded in Firebase → Project settings → Cloud Messaging.
4. Backend sends via a **service account JSON** (Firebase → Project settings →
   Service accounts → Generate private key).

**.env (backend)**
```
FCM_SERVICE_ACCOUNT_JSON=/secure/path/kmr-fcm-service-account.json
```
*(AWS SNS Mobile Push can also front FCM/APNs if you prefer all-AWS; FCM direct is
simpler for Flutter. Say the word and I'll wire whichever.)*

---

## 5. Images / CDN — AWS S3 + CloudFront 🟡

**Why:** product images, avatars, banners — uploaded to S3, served fast via CDN.
S3 upload is already coded (`S3Service`). You just need the CDN in front.

**Steps**
1. Bucket `kmr-marketplace-media` already set. 
2. Create a **CloudFront distribution** with that S3 bucket as origin (use an Origin
   Access Control so the bucket stays private). Copy the distribution domain.

**.env**
```
S3_BUCKET_NAME=kmr-marketplace-media          # set
CLOUDFRONT_DOMAIN=https://dxxxx.cloudfront.net # replace the placeholder
```

---

## 6. Google Sign-In ⚪

**Why:** one-tap Google login (proposal feature).

**Steps**
1. **Google Cloud Console** → APIs & Services → Credentials → create **OAuth client
   IDs**: Android (needs SHA-1 of your signing key), iOS (bundle id), and a **Web**
   client id (backend uses it to verify the token).
2. Android: reuse the Firebase `google-services.json` (it carries the OAuth client).

**.env (backend)**
```
GOOGLE_OAUTH_CLIENT_ID=xxxx.apps.googleusercontent.com
```

---

## 7. Maps / live delivery tracking — Google Maps Platform ⚪

**Why:** live GPS order tracking on a map (delivery app + customer view). Today
tracking is a **status timeline** (no map, no key needed). The map arrives with the
Delivery app.

**Steps**
1. Google Cloud → enable **Maps SDK for Android**, **Maps SDK for iOS**, **Directions
   API** → create an API key (restrict it to those APIs + your app ids).

**Config (app)**
```
GOOGLE_MAPS_API_KEY=...   # in Android manifest + iOS AppDelegate, added when we build tracking
```

---

## Quick status of what needs you vs what's done

| Service | You create | Blocks launch? | Code ready |
|---|---|---|---|
| Razorpay keys | account + KYC + keys | 🔴 online pay | ✅ (dev-mode until keys) |
| Razorpay Route | Route + linked accounts | ⚪ (with Seller app) | records split now |
| AWS SNS (OTP SMS) | IAM perms + DLT | 🔴 real OTP | ✅ (dev-mode prints OTP) |
| AWS SES (email) | verify + prod access | 🟡 | key set ready |
| FCM (push) | Firebase + APNs key | 🟡 | to wire |
| CloudFront | distribution | 🟡 | S3 upload ✅ |
| Google Sign-In | OAuth client ids | ⚪ | to wire |
| Google Maps | Maps API key | ⚪ (with Delivery app) | timeline now |

**You can build, test, and demo the entire customer app end-to-end today** with zero
external keys (dev-mode OTP + simulated payment). Add the keys above to switch each
piece from dev-mode to live — nothing in the code changes.
