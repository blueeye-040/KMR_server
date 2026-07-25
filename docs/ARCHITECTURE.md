# KMR Multi-Vendor Marketplace — System Architecture

> Living document. Update whenever the design changes.

## 1. What this product is

A multi-vendor e-commerce marketplace (Amazon-style) for the Indian market.
Customers browse a shared product catalog; **each product is listed by multiple
vendors (shops)**. On the product page the customer picks *which vendor* to buy
from. The distinguishing rule of this marketplace:

- **The product price settles to the chosen vendor's bank account.**
- **The delivery fee is retained by the platform (app owner).**

The full program is four apps (per the PentStack proposal):

1. **Customer app** (Flutter, Android + iOS) — *building first, this is the priority*
2. Seller / shop-owner app
3. Delivery-agent app
4. Web admin panel

## 2. Tech stack

| Layer            | Technology                                             |
|------------------|--------------------------------------------------------|
| Mobile client    | Flutter (Dart), Riverpod, go_router, dio, hive         |
| Backend          | Spring Boot 3.4 (Java 17), Spring Security + JWT        |
| Database         | PostgreSQL on **Supabase** (only non-AWS piece)         |
| File storage/CDN | AWS S3 + CloudFront                                     |
| SMS / OTP        | AWS SNS (dev-mode fallback until keys are added)        |
| Email            | AWS SES (planned)                                       |
| Payments         | Razorpay (dev-mode fallback until keys are added)       |
| Push             | Firebase Cloud Messaging (planned)                      |
| AI features      | AWS (recommendations, chatbot, image search) (planned)  |

Everything except the database runs on AWS, per the client's requirement.

## 3. Repositories

| Path                                    | What                          | Git |
|-----------------------------------------|-------------------------------|-----|
| `/Volumes/Software/downloads/marketplace` | Spring Boot backend + docs   | ✅ (branch `analytics_page`) |
| `/Users/chandru/kmr_marketplace`         | Flutter customer app          | ✅  |

The backend repo is the **hub**: `docs/` here is the source of truth for the
whole program. The Flutter repo keeps a short `docs/STATUS.md` pointer.

## 4. Backend module map (`com.kmr.marketplace`)

```
config/      SecurityConfig, WebConfig, AwsConfig
security/    JwtService, JwtAuthFilter, AuthHelper, OtpStore, TokenBlacklist, UserDetails
entity/      User, Category, Brand, Shop, Product, ProductImage, ShopProduct,
             CartItem, WishlistItem, Address, Order, OrderItem, Review, Banner
repository/  Spring Data JPA repositories (one per aggregate)
dto/         Java records for request/response payloads
service/     Business logic (Auth, Cart, Order, Address, Wishlist, Payment,
             ProductDetail, Home, Analytics, S3, Sms)
controller/  Thin REST controllers under /api/**
```

Conventions (keep it simple, no over-engineering):
- Controllers are thin; logic lives in `@Transactional` services.
- DTOs are immutable `record`s. Entities use field access + explicit getters/setters.
- Current user comes from `AuthHelper.currentUser()` (throws 401 if anonymous).
- Errors are thrown as `ResponseStatusException`; `GlobalExceptionHandler` shapes them.

## 5. Data model (core)

```
categories ──< products >── brands
                  │
                  ├──< product_images
                  └──< shop_products >── shops          (a product listed by many shops)
                            │
users ──< cart >───────────┤
users ──< wishlist >── products
users ──< addresses
users ──< orders ──< order_items >── shop_products / products / shops
products ──< reviews >── users
banners
```

`shop_products` is the multi-vendor join: `(shop_id, product_id)` unique, with
its own `mrp / selling_price / discount_percent / stock / delivery_days`.
`shops.is_official = true` marks the platform's own "KMR Store".

## 6. The payment-split rule (the differentiator)

On checkout an order can contain items from several shops (multi-vendor cart).

- Each `order_item` records `shop_id` and the unit `price`.
- **Vendor payout** = per shop, `Σ(price × quantity)` → settles to that shop's
  bank account / UPI.
- **Platform earning** = the order's `delivery_charge` → retained by the app owner.

Delivery rule today: free over ₹499, otherwise a flat ₹49 (constants in
`OrderService` / `CartService`). The order-detail API returns a
`shopSettlements[]` breakdown plus `platformDeliveryEarning` so this split is
visible/auditable and drives seller payouts later.

Online payments use **Razorpay**. `PaymentService` calls the Razorpay Orders API
directly over HTTPS (Basic auth) and verifies the checkout signature with
HMAC-SHA256 — no SDK dependency. With no keys configured it runs in **dev mode**:
a simulated order id is issued and verification always succeeds, so the whole
checkout can be tested without live keys. Real per-vendor auto-settlement will
use Razorpay Route once the client provides a Route-enabled account.

## 7. Auth

Email + password (BCrypt) issuing a JWT (`app.jwt.*`). Phone OTP via
`OtpController` (+ AWS SNS; dev mode prints/stores the code). Google Sign-In is
planned. Public endpoints: `/api/auth/**`, `/api/home`, `/api/categories`,
`GET /api/products/**`. Everything else requires a valid JWT.

## 8. Environments / secrets

All secrets live in `marketplace/.env` (git-ignored) and are referenced from
`application.properties`. Keys the client will fill in later:
`RAZORPAY_KEY_ID/SECRET`, real AWS SNS/SES enablement, `CLOUDFRONT_DOMAIN`, FCM.
See `README.md` → "Configuration" for the full list.
