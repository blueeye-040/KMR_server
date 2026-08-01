# Valley Rush — Server Guide (Backend Developer Reference)

A complete map of the Spring Boot backend: every endpoint (public / private /
admin), what each does and why, the services behind them, the data model, and
security. Pair this with the live, clickable **[Swagger UI](SWAGGER_GUIDE.md)**.

- Stack: Spring Boot 3.4 (Java 17) · Spring Security + JWT · Spring Data JPA ·
  PostgreSQL on Supabase · AWS SDK (SNS/S3/SES) · Razorpay (REST).
- Base URL (dev): `http://localhost:8080`. All endpoints are under `/api`.

## 1. Request lifecycle (how a call flows)
```
Client (Flutter)
   │  Authorization: Bearer <JWT>   (except public endpoints)
   ▼
JwtAuthFilter ──► validates token, loads User into SecurityContext (errors are
   │              swallowed so public endpoints still work)
   ▼
SecurityConfig ──► decides permitAll vs authenticated (see §3)
   ▼
Controller (thin) ──► Service (@Transactional business logic) ──► Repository (JPA)
   │                                                                   │
   ▼                                                                   ▼
DTO (record) response  ◄───────────────────────────────────────  PostgreSQL (Supabase)
```
Errors are thrown as `ResponseStatusException`; `GlobalExceptionHandler` turns
them (and validation failures) into clean JSON `{message, ...}`.

## 2. Auth model
- **Register** requires a **verified phone** first (OTP), then email + password.
  Passwords are BCrypt-hashed. Login returns a **JWT** (`app.jwt.*`, 24h).
- The JWT carries the user's email; on each request `JwtAuthFilter` loads the
  full `User` (with role) from the DB. Roles: `CUSTOMER`, `SELLER`, `ADMIN`.
- `AuthHelper.currentUser()` gives services the logged-in user (401 if none);
  `requireRole(ADMIN)` guards admin actions (403 otherwise).

## 3. Endpoint reference

Legend: 🟢 public (no token) · 🔒 authenticated · 🛡️ admin-only.

### Auth & OTP — `AuthController`, `OtpController` (`/api/auth`)
| Method | Path | Access | What / why |
|---|---|---|---|
| POST | `/register` | 🟢 | Create account (needs prior OTP verify). Returns JWT. |
| POST | `/login` | 🟢 | Email+password → JWT. |
| POST | `/logout` | 🔒 | Blacklists the current token (`TokenBlacklistService`). |
| GET | `/me` | 🔒 | Current user profile snapshot. |
| POST | `/send-otp` | 🟢 | Sends a 6-digit OTP to a phone (SNS; dev-mode logs it). Rate-limited. |
| POST | `/verify-otp` | 🟢 | Verifies the OTP so registration can proceed. |

**Why OTP-before-register:** blocks fake sign-ups and ties every account to a
reachable phone for delivery/coordination. Logic + rate limits live in `OtpStore`.

### Home & catalog — `HomeController`, `ProductController` (`/api`)
| Method | Path | Access | What / why |
|---|---|---|---|
| GET | `/home` | 🟢 | Storefront feed: banners, categories, **deals**, featured, new arrivals, top selling, **recommended**, greeting. Built by `HomeService` (batched queries, one best-price lookup). |
| GET | `/categories` | 🟢 | Flat list of shoppable (leaf) categories — home grid. |
| GET | `/categories/tree` | 🟢 | Departments → child categories — the Categories tab. |
| GET | `/products` | 🟢 | **Search / filter / sort / paginate.** Params: `q, categoryId, departmentId, brandId, minPrice, maxPrice, minRating, minDiscount, sort, page, size`. `ProductService` + `ProductSearchRepositoryImpl` (safe dynamic native SQL over each product's best vendor price). |
| GET | `/products/{id}` | 🟢 | Full detail: gallery, specs, **all sellers** (vendor picker, KMR-official first), reviews, rating. `ProductDetailService`. |
| GET | `/products/facets` | 🟢 | Brands + price bounds for the filter sheet. |
| POST | `/products/{id}/reviews` | 🔒 | Add a review (one per user/product; a DB trigger keeps `rating_avg`/`review_count` fresh). |

**Why native SQL for search:** price and discount live per-vendor in
`shop_products`; the query joins each product to its *minimum available price* so
price filters/sorts reflect what the buyer would actually pay. Sort clauses are
whitelisted (no SQL injection).

### Cart — `CartController` (`/api/cart`) — all 🔒
| Method | Path | What |
|---|---|---|
| GET | `` | Cart with per-item + total pricing, savings, delivery (free ≥ ₹499). |
| GET | `/count` | Badge count. |
| POST | `/{shopProductId}` | Add a specific **vendor listing** to cart (stock-checked, qty-clamped). |
| PUT | `/{cartItemId}` | Change quantity (0 removes). |
| DELETE | `/{cartItemId}` / `` | Remove one / clear all. |

**Why `shopProductId` not `productId`:** the cart stores the exact vendor the
buyer chose, so the payment split and fulfilment know who sells each line.

### Addresses — `AddressController` (`/api/addresses`) — all 🔒
GET (list) · POST (add) · PUT `/{id}` (edit) · PUT `/{id}/default` (set default) ·
DELETE `/{id}`. First address auto-becomes default; setting a default clears others.

### Wishlist — `WishlistController` (`/api/wishlist`) — all 🔒
GET (list) · GET `/count` · POST `/{productId}` (toggle, returns `{wishlisted}`) ·
DELETE `/{productId}`.

### Orders & checkout — `OrderController` (`/api/orders`) — all 🔒
| Method | Path | What / why |
|---|---|---|
| POST | `` | **Place order** from cart: validates stock, applies coupon, computes delivery, reserves stock, snapshots the address, clears cart. For ONLINE it creates a Razorpay order and returns its id + key; for COD it's placed immediately (and confirmation email/push fire). |
| POST | `/{id}/payment/verify` | Confirm an online payment: verifies the Razorpay HMAC signature, marks `PAID` + `CONFIRMED`, fires email/push. Idempotent. |
| GET | `` | Order history (summaries). |
| GET | `/{id}` | Order detail: items, status **timeline**, price breakdown, and the **shopSettlements** (per-vendor payout) + `platformDeliveryEarning`. |
| POST | `/{id}/cancel` | Cancel before shipped: restocks items, refunds if paid. |

**The marketplace rule lives here:** each `order_item` records its `shop_id` and
unit price. Vendor payout = Σ(price×qty) per shop → that shop's bank; delivery
fee → the platform. See `OrderService.detail()` → `shopSettlements`.

### Coupons — `CouponController` (`/api/coupons`)
| Method | Path | Access | What |
|---|---|---|---|
| GET | `` | 🔒 | **Available offers** (active, non-expired, non-exhausted). |
| POST | `/apply` | 🔒 | Preview a code vs a cart total → `{valid, discount, message}`. |
| GET | `/admin` | 🛡️ | All coupons (incl. disabled). |
| POST | `` | 🛡️ | Create a coupon. |
| PUT | `/{id}` | 🛡️ | Edit. |
| PATCH | `/{id}/active` | 🛡️ | **Enable/disable** — controls whether users see & can use it. |
| DELETE | `/{id}` | 🛡️ | Remove. |

FLAT or PERCENT, with `minCartValue` and (for PERCENT) a `maxDiscount` cap.
`OrderService` re-validates and **consumes** the coupon at checkout (increments
`used_count`) — the client can't fake a discount.

### Profile — `ProfileController` (`/api/profile`) — 🔒
GET (current user) · PUT (update name / phone / avatar).

### Support — `SupportController` (`/api/support`) — 🔒
POST (raise ticket: `ISSUE | RETURN | EXCHANGE`, optional `orderId`) · GET (my
tickets) · GET `/{id}`. Backs the in-app "Need help with this order" sheet.

### Devices (push) — `DeviceController` (`/api/devices`) — 🔒
POST `/token` (register an FCM token for this user) · DELETE `/token`.
`PushService.sendToUser()` fans out to all a user's devices. See
[FCM_GUIDE](FCM_GUIDE.md).

### Analytics — `AnalyticsController` (`/api/analytics`) — 🔒
Platform counts (products, categories, shops; user count for admins). Powers the
in-app analytics screen.

## 4. Services (the "why" behind the logic)
| Service | Responsibility |
|---|---|
| `AuthService` | Register/login, JWT issue, password hashing. |
| `OtpStore` | In-memory OTP with hashing, expiry, resend + attempt rate limits. |
| `HomeService` | Assembles the storefront feed (deals/recommended reuse `ProductService`). |
| `ProductService` + `ProductSearchRepositoryImpl` | Search/filter/sort + facets over best vendor price. |
| `ProductDetailService` | Product page incl. all sellers + reviews + related. |
| `CartService` | Cart CRUD, stock/qty rules, delivery threshold. |
| `AddressService` | Address CRUD + default handling. |
| `WishlistService` | Toggle/list/count. |
| `OrderService` | Checkout, stock reservation, coupon apply, **payment split**, tracking, cancel/restock, notifications. |
| `PaymentService` | Razorpay order create + HMAC verify (dev-mode fallback). |
| `CouponService` | Preview/consume + admin management. |
| `ProfileService` | Profile read/update. |
| `SupportService` | Support tickets. |
| `PushService` | Device tokens + FCM dispatch (dev-mode logs). |
| `EmailService` | SES order emails (dev-mode logs). |
| `SmsService` | SNS OTP SMS (dev-mode logs). |
| `S3Service` | Product image upload + presigned URLs. |
| `AnalyticsService` | Platform metrics. |

## 5. Data model (tables)
`users · categories(parent_id) · brands · shops(is_official) · products ·
product_images · shop_products · cart · wishlist · addresses · orders ·
order_items · reviews · banners · coupons · support_tickets · device_tokens`.
Full DDL + every migration is in [`schema.sql`](../schema.sql) (Phases 1–6).
The **live Supabase DB is the source of truth**; apply schema changes there and
append the migration SQL to `schema.sql`.

## 6. Security & config
- Public: `/api/auth/**`, `/api/home`, `/api/categories/**`, `GET /api/products/**`,
  Swagger (`/swagger-ui/**`, `/v3/api-docs/**`). Everything else needs a JWT.
- Stateless sessions; CSRF disabled (token-based API).
- Secrets in `.env` (git-ignored) — see [INTEGRATIONS.md](INTEGRATIONS.md) for the
  full key list and which run in dev-mode until credentials are added.

## 7. Run
```bash
./mvnw spring-boot:run      # :8080, connects to Supabase via .env
./mvnw test                 # tests
./mvnw clean package        # jar
```
Dev conveniences: OTP printed to stdout; payments/email/push simulated+logged
until real keys are set.
