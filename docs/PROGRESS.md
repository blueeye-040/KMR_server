# KMR Marketplace — Progress Log & Resume Point

> **Read this first when resuming.** It records what's done, what's verified, and
> the exact next step. Newest entry on top. Keep `docs/ROADMAP.md` checkboxes in sync.

## Current state (2026-07-26)

**Focus:** Customer app (App 1). Backend now covers the full browse+buy loop
(catalog, search/filter/sort, taxonomy, cart, checkout, orders, payments,
wishlist, addresses) — all verified against live Supabase. Flutter build is next.

**Engineering plan:** see [`docs/FEATURE_SPEC.md`](FEATURE_SPEC.md) — the Amazon-parity
feature map + category taxonomy that this build targets.

**Backend:** Spring Boot compiles clean. Runs against live Supabase.
**DB:** live on Supabase; catalog seeded (70 products, 150 listings, 6 shops,
11 leaf categories under 4 departments).

### ▶ NEXT STEP (do this next)
Flutter customer app now covers the **full purchase loop** end-to-end (browse →
search/filter → cart → checkout → pay → orders/tracking → wishlist), committed in
the Flutter repo branch `customer-app-discovery`. **Credentials the client must
create are documented in [`INTEGRATIONS.md`](INTEGRATIONS.md).**

Remaining for App 1 (pick up here):
1. Product-detail polish: wishlist heart toggle (`/api/wishlist/{productId}`) +
   nicer vendor picker; "you might also like" (related products).
2. Backend: profile update (`PUT /api/profile`), coupons, support/return tickets,
   SES email + SMS confirmations, FCM push.
3. Flutter: onboarding carousel, profile edit, coupon field at checkout,
   EN↔Urdu localization, FCM push, empty/skeleton polish.
4. Release: app icons/splash, Android signing → AAB/APK, iOS signing → IPA, tests,
   store listings + privacy/data-safety.

--- (superseded) earlier note: Flutter checkout + orders + wishlist ---
1. Add API paths to `lib/core/constants/api_constants.dart` (addresses, wishlist, orders).
2. Address book: list/add/edit/set-default (`/api/addresses`).
3. Checkout flow from cart: address → payment method → review → place order
   (`POST /api/orders`), then Razorpay for ONLINE (dev-mode returns a simulated
   order id + empty key; wire `razorpay_flutter` but tolerate dev mode), then
   `POST /api/orders/{id}/payment/verify`. COD places directly.
4. Order confirmation screen + Orders list (`/api/orders`) + Order detail with
   status timeline & the shop-settlement/delivery breakdown (`/api/orders/{id}`),
   cancel (`POST /api/orders/{id}/cancel`).
5. Wishlist screen + heart toggle on product cards/detail (`/api/wishlist`).
Then wire a bottom-nav shell (Home / Categories / Cart / Wishlist / Profile) and
add routes in `lib/router/app_router.dart`.

After that (backend): product **search + filters + sort**, then profile update,
then coupons, then SES email / SMS confirmations.

---

## Changelog

### 2026-07-28 — Rebrand to Valley Rush + Flutter endpoint wiring
- **Brand: KMR → Valley Rush** everywhere user-facing. Backend: `EmailService`
  order emails. DB: the official store row renamed to "Valley Rush Store"
  (`shops.is_official`). Flutter: all UI strings, app name, launcher icon + native
  splash (from the client logo), package/bundle id `com.valleyrush.customer`.
- **Flutter wired to the new backend endpoints**: coupon at checkout (+available
  offers), profile edit, support ticket from the order-help sheet, and a
  device-token registration client (FCM token source pending Firebase setup).
- **Release prep** (Flutter repo): conditional release signing via
  `android/key.properties`, `docs/RELEASE.md`, and `docs/store/` listing drafts
  (Play + App Store, privacy policy, terms, data-safety). Android/iOS developer
  accounts + keystore are the client's remaining step before store upload.

### 2026-07-27 — Backend: coupons, profile, support tickets, email & push (+ detail wishlist)
Added and **verified** against live Supabase:
- **Coupons**: `coupons` table + seed (KMR100/SAVE10/WELCOME50). `POST /api/coupons/apply`
  previews a code vs cart total; `OrderService` applies + consumes it at checkout,
  recording `orders.coupon_code` and the discount. FLAT/PERCENT with min-cart + cap.
  Users see **available offers** at `GET /api/coupons` (active, non-expired, non-exhausted).
  **Admin management** (ADMIN role only): `GET /api/coupons/admin`, create/update/delete,
  `PATCH /api/coupons/{id}/active` to enable/disable — disabled coupons vanish from the
  user list (verified). Non-admins get 403.
- **Profile**: `GET/PUT /api/profile` (name/phone/avatar).
- **Support tickets**: `support_tickets` table; `POST/GET /api/support`, `GET /api/support/{id}`
  (ISSUE/RETURN/EXCHANGE) — backs the app's "need help with this order" sheet.
- **Email (AWS SES)** `EmailService` + **Push (FCM)** `PushService` + device-token
  registration (`POST/DELETE /api/devices/token`). Order confirmation fires email +
  push on COD placement and on online payment success. Both run **dev-mode**
  (logged) until `MAIL_FROM` / `FCM_SERVICE_ACCOUNT_JSON` are set — verified logs fire.
- **Flutter**: product-detail wishlist heart is now API-backed (syncs with Wishlist tab).
Smoke test passed: profile update; coupon min-cart/flat/percent-cap/invalid; support
ticket create+list; device register; coupon-applied COD order (₹4499−₹50=₹4449) with
email+push dev-mode logs. Test data cleaned.

### 2026-07-26 — Flutter: checkout + orders + wishlist + addresses (purchase loop complete)
In the **Flutter repo** (branch `customer-app-discovery`, commit 8cadaaa):
- Address book (list/add/edit/default/delete), reused as a checkout picker.
- Checkout: address → payment (ONLINE via **razorpay_flutter** = UPI/cards/
  netbanking/wallets, or **COD**) → summary → place. Dev-mode simulates payment
  when the backend has no Razorpay keys, so the flow is fully testable now.
- Order confirmation; Orders list; Order detail with tracking timeline, price/
  payment breakdown, cancel, and a "need help with this order" support sheet.
- Wishlist tab (grid, remove) replaces placeholder; Account menu + cart checkout wired.
- `flutter analyze lib`: 0 errors. Native Razorpay setup + all credentials the
  client must create are documented in **`docs/INTEGRATIONS.md`** (new).

### 2026-07-26 — Flutter: app shell + discovery (search / filter / sort / home rails)
In the **Flutter repo** (branch `customer-app-discovery`, commit 19a6ed8):
- New `lib/features/search`: unified search + browse results screen (`/search`)
  with editable search bar, live sort, and a facets-driven filter sheet (brand /
  price range / min discount / min rating); infinite scroll; empty/error states.
- Home feed gained **Deals of the Day** + **Recommended** rails; search bar and
  category chips navigate into scoped, filterable results.
- Brought the previously-uncommitted Flutter app baseline under version control.
- `flutter analyze lib` clean (only pre-existing infos). Not yet run on a device.
- Flutter-side snapshot: `kmr_marketplace/docs/STATUS.md`.

### 2026-07-26 — Browse & discovery (search, filters, taxonomy, home rails)
Added and **verified** against live Supabase:
- **Category taxonomy**: `categories.parent_id`/`sort_order`; 4 departments
  (Electronics, Home & Appliances, Fashion, Grocery & Essentials) over the 11 leaf
  categories; renamed leaf 1 → "Audio & Video". `/api/categories` returns leaves
  (backward compatible); new `/api/categories/tree` returns departments→children.
- **Product search/filter/sort**: `/api/products` now takes `q` (Postgres FTS +
  ILIKE), `categoryId`, `departmentId`, `brandId`, `minPrice`, `maxPrice`,
  `minRating`, `minDiscount`, `sort` (relevance/price_asc/price_desc/newest/
  popularity/rating/discount), `page`, `size`. Price filter/sort use each
  product's best available vendor price. Safe dynamic native SQL
  (`ProductSearchRepositoryImpl`, whitelisted ORDER BY).
- **Facets**: `/api/products/facets?categoryId&departmentId&q` → brands+counts and
  price bounds for the filter sheet.
- **Home rails**: added `dealsOfTheDay` (top discounts) and `recommended`
  (top-rated), reusing the search engine. Home feed is now Amazon-shaped.
- New: `ProductService`, `ProductFilter`, `FacetsDto`, `CategoryTreeDto`.
Verified: FTS search, price asc/desc sort, brand/price/discount/rating filters,
department scoping, pagination, facets, and all home rails return correct data.

### 2026-07-26 — Backend commerce spine (Addresses, Wishlist, Orders, Payments)
Added and **verified end-to-end** against live Supabase:
- Entities: `Address`, `WishlistItem`, `Order`, `OrderItem`.
- Repos: `AddressRepository`, `WishlistRepository`, `OrderRepository`.
- Services: `AddressService`, `WishlistService`, `OrderService`, `PaymentService`.
- Controllers: `/api/addresses`, `/api/wishlist`, `/api/orders`.
- DB migration: `orders.razorpay_order_id/razorpay_payment_id`,
  `order_items.status`, indexes (applied to Supabase; mirrored in `schema.sql`
  — see note below).
- Razorpay integration (REST + HMAC verify) with **dev-mode fallback** (no keys
  needed to test). Keys go in `.env` as `RAZORPAY_KEY_ID/SECRET`.
- **Payment-split** implemented: order detail returns `shopSettlements[]`
  (each vendor's payout) + `platformDeliveryEarning` (app owner's delivery fee).

Smoke test passed: OTP→register→cart(2 items)→address→**COD order**→detail(split);
and →**ONLINE order**→dev Razorpay→verify(PAID/CONFIRMED)→list→**cancel**(restock +1).
Test data cleaned from DB afterward.

### Verified API surface (customer)
```
POST /api/auth/register | login | logout        GET /api/auth/me
POST /api/auth/send-otp | verify-otp
GET  /api/home | /api/categories | /api/categories/tree
GET  /api/products?q&categoryId&departmentId&brandId&minPrice&maxPrice&minRating&minDiscount&sort&page&size
GET  /api/products/{id}          GET /api/products/facets?categoryId&departmentId&q
POST /api/products/{id}/reviews
GET/POST/PUT/DELETE /api/cart ...                 GET /api/cart/count
GET/POST/PUT/DELETE /api/addresses               PUT /api/addresses/{id}/default
GET /api/wishlist | /count   POST/DELETE /api/wishlist/{productId}
POST /api/orders           POST /api/orders/{id}/payment/verify
GET  /api/orders           GET  /api/orders/{id}   POST /api/orders/{id}/cancel
GET  /api/coupons          POST /api/coupons/apply
GET/PUT /api/profile
POST/GET /api/support      GET /api/support/{id}
POST/DELETE /api/devices/token
GET  /api/analytics
-- admin (ROLE=ADMIN): GET /api/coupons/admin, POST /api/coupons,
   PUT /api/coupons/{id}, PATCH /api/coupons/{id}/active, DELETE /api/coupons/{id}
```

---

## How to run / test (backend)
```bash
cd /Volumes/Software/downloads/marketplace
./mvnw spring-boot:run          # boots on :8080, connects to Supabase via .env
```
Dev OTP is printed to stdout as `[DEV] OTP for <phone> → <code>`.
Payments run in dev mode until `RAZORPAY_KEY_ID/SECRET` are set in `.env`.

## ⚠️ schema.sql note
`schema.sql` is the historical build script (contains an early auth.users design
that is later dropped/replaced by the standalone `users` table — the live schema).
Newer changes are appended as dated migration blocks. The **live Supabase DB is
the source of truth**; when changing schema, apply to Supabase and append the
migration SQL here.
