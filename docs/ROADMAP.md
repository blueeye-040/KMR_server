# KMR Marketplace — Delivery Roadmap

> Full scope from the PentStack proposal, tracked as checkboxes.
> `[x]` done · `[~]` in progress · `[ ]` not started.
> Priority order: **finish the Customer app first**, then Seller, Delivery, Admin.

---

## APP 1 — Customer App (Flutter) + Backend  ← current focus

### Backend — commerce API
- [x] Auth: register / login / logout / me (JWT, BCrypt)
- [x] Phone OTP send/verify (AWS SNS + dev-mode)
- [x] Home feed, categories
- [x] Product list (paged, category filter), product detail w/ multi-vendor sellers
- [x] Reviews (list + add, rating rollup trigger)
- [x] Cart CRUD
- [x] Product listing analytics endpoint
- [x] S3 image upload service + presigned uploads
- [x] KMR Official Store (is_official, surfaced first)
- [x] **Addresses CRUD**
- [x] **Wishlist (toggle / list / count)**
- [x] **Checkout → create order (multi-vendor, stock reserve)**
- [x] **Payment split (vendor payout vs platform delivery earning)**
- [x] **Razorpay online payment (create order + verify signature) w/ dev-mode**
- [x] **COD orders**
- [x] **Order history / detail / status timeline / cancel + restock**
- [ ] Product search (keyword) + filters (price range) + sort (price/newest/popularity)
- [ ] Coupons (flat / percentage / min cart value)
- [ ] Profile update (name / phone / avatar)
- [ ] Order confirmation email (AWS SES) + SMS
- [ ] Return / exchange request records
- [ ] Push notification dispatch (FCM) + cart-abandonment trigger
- [ ] Live order tracking (delivery GPS feed) — read side for customer
- [ ] AI: recommendations, chatbot, image search
- [ ] Rate limiting, request logging, prod hardening

### Flutter — customer screens
- [x] Splash, Login, Register, OTP
- [x] Home (banners, categories, product carousels)
- [x] Product listing analytics screen
- [x] Product detail (base)
- [x] Cart
- [ ] Onboarding carousel (first launch)
- [ ] Bottom-nav app shell (Home / Categories / Cart / Wishlist / Profile)
- [ ] Category browse + product grid + filters + sort
- [ ] Search screen
- [ ] Product detail: **vendor picker** (choose shop → price/delivery) + wishlist
- [ ] Checkout flow: address → payment → review → confirmation
- [ ] Address management screens
- [ ] Orders list + order detail + tracking timeline + cancel
- [ ] Wishlist screen
- [ ] Profile screen (info, addresses, orders, notifications, help, sign-out, delete acct)
- [ ] Razorpay checkout integration (razorpay_flutter)
- [ ] Live chat (AI support) widget
- [ ] Push notifications (FCM) wiring
- [ ] EN ↔ Urdu localization
- [ ] Empty/loading/error states polish, offline handling

### Release engineering (Customer app)
- [ ] App icons + splash + store screenshots
- [ ] Android signing (keystore) → build AAB + APK
- [ ] iOS signing (certs/profiles) → build IPA
- [ ] Automated tests: backend (unit + slice), Flutter (unit + widget)
- [ ] Privacy policy + terms pages, Play Data-safety + App Privacy forms
- [ ] Play Store + App Store listing + submission

---

## APP 2 — Seller / Shop-owner App
- [ ] Seller signup + approval flow
- [ ] Seller product listing / pricing / stock management
- [ ] Seller order list + fulfilment status
- [ ] Seller payouts / settlement view (uses payment-split data)
- [ ] Seller analytics

## APP 3 — Delivery-agent App
- [ ] Agent login + assigned orders
- [ ] Status updates (picked up → out for delivery → delivered)
- [ ] Live GPS location sharing
- [ ] Daily delivery summary + proof of delivery

## APP 4 — Web Admin Panel
- [ ] Role-based admin login + dashboard
- [ ] Seller / product / category / order management
- [ ] Delivery-agent management + assignment
- [ ] Banner + coupon management
- [ ] Return / exchange management
- [ ] Push-notification composer
- [ ] Analytics (orders/day, top sellers, top products, revenue trend)

---

## Cross-cutting infra (all apps)
- [ ] AWS deployment (backend as service(s) on client AWS account)
- [ ] CI/CD pipeline
- [ ] Monitoring / logging / alerts
- [ ] Backups + migration scripts kept in `schema.sql` / `docs/`
