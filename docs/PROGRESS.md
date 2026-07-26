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
Flutter app shell + discovery are **done** (search, filter/sort, home rails —
committed in the Flutter repo branch `customer-app-discovery`). Next, wire the
**Flutter commerce screens** against the (already built & tested) backend APIs:

Flutter **checkout + orders + wishlist** feature set against the new APIs:
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
GET  /api/analytics
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
