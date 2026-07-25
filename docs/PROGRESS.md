# KMR Marketplace — Progress Log & Resume Point

> **Read this first when resuming.** It records what's done, what's verified, and
> the exact next step. Newest entry on top. Keep `docs/ROADMAP.md` checkboxes in sync.

## Current state (2026-07-26)

**Focus:** Customer app (App 1) — backend commerce spine complete & verified;
Flutter customer screens for checkout/orders/wishlist/profile are next.

**Backend:** Spring Boot compiles clean. Runs against live Supabase.
**DB:** live on Supabase; catalog seeded (70 products, 150 listings, 6 shops).
Orders/addresses/wishlist tables empty (clean).

### ▶ NEXT STEP (do this next)
Build the **Flutter checkout + orders + wishlist** feature set against the new APIs:
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
GET  /api/home | /api/categories
GET  /api/products?categoryId&page&size          GET /api/products/{id}
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
