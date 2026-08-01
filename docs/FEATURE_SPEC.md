# KMR Customer App — Amazon-Parity Feature Spec

> The engineering target for App 1 (customer). "Production like Amazon."
> Each feature notes its backend + Flutter status and where it lives.
> Legend: ✅ done · 🔨 in progress · ⛳ planned.

## 0. Design principles
- **Parity, not clone.** Match Amazon's *shopping model* and IA, not its pixels.
- **Multi-vendor is the twist.** Every product is sold by ≥1 shop; the buyer
  picks a vendor; **price → vendor's bank, delivery fee → platform**. The chosen
  `shop_id` is stored on every `order_item` so the app owner knows who fulfils
  and who to pay. (Already enforced in `OrderService`.)
- **Admin controls the storefront.** Home rails, banners, category tree, and the
  `featured / new_arrival / top_selling` flags are admin-driven (Admin app later);
  the backend already reads these flags so the customer app just renders them.
- Keep it simple; add complexity only where the feature needs it.

## 1. Information architecture (bottom nav — like Amazon)
```
┌ Home ┐ ┌ Categories ┐ ┌ Cart ┐ ┌ Wishlist ┐ ┌ Account ┐
```
Global app bar: location/pincode chip · search bar · notifications bell.

## 2. Feature map

### Auth & onboarding
- ✅ Email+password register/login (JWT), phone OTP. Backend solid.
- ⛳ Google Sign-In. ⛳ Forgot-password (email). ⛳ 3-screen onboarding carousel.
- ⛳ Delete account (store-compliance).

### Location & address
- ✅ Address book CRUD + default (`/api/addresses`).
- ⛳ Flutter: capture location (geolocator) → prefill city/pincode; serviceable-
  pincode check; address picker used at checkout. Delivery ETA from `delivery_days`.

### Home (storefront)
- ✅ Hero banner carousel (admin-managed) · category quick-grid
- ✅ Rails: Featured · New Arrivals · Top Selling
- ⛳ Rails to add: **Deals of the Day** (highest discount) · **Most Ordered**
  (order_items aggregate; fallback top_selling) · **Recommended for you**
  (personalized; MVP = top-rated/popular, later from view/order history)
- ✅ Time-of-day greeting.

### Categories & discovery  ← building now
- Two-level taxonomy (**Department → Category**), Amazon-style. `categories.parent_id`.
- Category landing → product grid with filter/sort.
- **Search**: keyword across name+description (Postgres FTS + ILIKE fallback),
  search-as-you-type suggestions (⛳), recent searches (⛳ local).
- **Filters**: brand, price range, min rating, min discount, availability.
- **Sort**: relevance, price ↑/↓, newest, popularity, rating, discount.
- **Facets endpoint** feeds the filter sheet (brands + price bounds per result set).

### Product detail
- ✅ Gallery, specs, description, rating summary + reviews, add review.
- ✅ **Vendor picker** — list of shops selling it (KMR Official first, then cheapest),
  each with price/discount/delivery/stock; selection drives cart + order.
- ✅ Related products (same category) — `findRelated`, surface in detail.
- ⛳ Flutter: wishlist heart, share link, "you might also like", sticky Add-to-Cart/Buy-Now.

### Cart & checkout
- ✅ Multi-seller cart (grouped by vendor), qty stepper, totals, free-delivery ≥ ₹499.
- ⛳ Coupons (flat/percent/min-cart). ⛳ Save-for-later.
- ✅ Checkout: address → payment (ONLINE via Razorpay / COD) → review → place →
  confirmation. Online = create order → Razorpay → verify signature (dev-mode ok).
- ⛳ Flutter: the whole flow UI + `razorpay_flutter`.

### Orders, tracking, support
- ✅ Order history / detail / status timeline / cancel + restock.
- ✅ Per-order **shop-settlement breakdown** + platform delivery earning.
- ⛳ Live tracking (delivery GPS, later) · invoice PDF · re-order · return/exchange request.
- ⛳ **Raise an issue / support**: ticket per order + FAQ + (later) AI live chat.

### Account / settings
- ⛳ Profile (name/phone/email/avatar → `PUT /api/profile`), addresses, orders,
  wishlist, notifications inbox, payments, language (EN↔Urdu), help/FAQ,
  T&C/Privacy, sign-out, delete account.

### Cross-cutting
- ⛳ Push (FCM) · order email (SES) + SMS · EN↔Urdu localization · empty/error/skeleton
  states · rating after delivery · analytics screen (already present).

## 3. Category taxonomy (Amazon-style, mapped to current catalog)
Add `categories.parent_id` (department) + `sort_order`. Departments seeded; existing
leaf categories assigned. Only categories that **have products** are shown.

```
Electronics            → Electronics(1), Mobiles(2), Laptops(3)
Home & Appliances      → Appliances(4), Cleaning Essentials(10)
Fashion                → Fashion(5)
Grocery & Essentials   → Dairy & Eggs(6), Atta/Rice & Staples(7),
                         Beverages(8), Snacks & Munchies(9), Personal Care(11)
```
Per-department/-category filters are generic (brand, price, rating, discount) plus
the catalog's `specifications` JSON can drive attribute filters later (⛳).

## 4. Backend work order (foundational → feature)
1. 🔨 Category taxonomy: `parent_id`/`sort_order` + departments; `/api/categories`
   returns the tree.
2. 🔨 Product **search + filters + sort** on `/api/products`; **facets** endpoint.
3. ⛳ Home rails: deals + most-ordered + recommended.
4. ⛳ Profile update · coupons · support tickets · returns.
5. ⛳ SES email / SMS confirmations · FCM push.

## 5. Flutter work order
1. App shell (bottom nav) + routing + shared widgets (product card w/ wishlist).
2. Categories/department browse → search → filter/sort sheet → results grid.
3. Product detail upgrades (vendor picker UI, wishlist, related, sticky CTA).
4. Cart → checkout → payment → confirmation.
5. Orders list/detail/track + cancel + raise-issue.
6. Wishlist + Account/profile/settings.
7. Onboarding, localization, push, polish, store assets.

## 6. Definition of "production-ready" (App 1 exit)
Auth · browse/search/filter · vendor-select · cart · checkout (COD+online) ·
orders+tracking · wishlist · profile · support · push · EN/Urdu · error/empty
states · tests (backend slice + Flutter widget) · signed AAB/APK + IPA · store
listings + privacy/data-safety. Tracked in `docs/ROADMAP.md`.
