# Valley Rush vs Amazon — Customer-App Feature Parity

An honest, feature-by-feature check of the **customer app (App 1)** against
Amazon's shopping app. ✅ done · 🟡 partial · ⛳ planned. Valley Rush's *extra*
twist (choose-your-vendor + payment split) is called out.

## Onboarding & account
| Amazon | Valley Rush |
|---|---|
| Splash / onboarding | ✅ splash + 3-slide first-launch onboarding |
| Email/password sign-up + login | ✅ |
| Phone OTP | ✅ (OTP required before register) |
| Google / social sign-in | ⛳ (documented; needs OAuth client) |
| Profile & settings | ✅ profile edit; ✅ account menu |
| Sign out / delete account | ✅ sign out · ⛳ delete-account flow |

## Home / discovery
| Amazon | Valley Rush |
|---|---|
| Hero banner carousel (managed) | ✅ (admin-managed banners) |
| Category shortcuts | ✅ chips + departments tree |
| Deals / Today's deals | ✅ Deals of the Day |
| Featured / New / Best-sellers rails | ✅ Featured, New Arrivals, Top Selling |
| Personalized recommendations | 🟡 "Recommended" (top-rated now; behavioural later) |
| Search bar | ✅ |

## Search, browse, filter
| Amazon | Valley Rush |
|---|---|
| Keyword search | ✅ full-text + fuzzy |
| Category / department browse | ✅ |
| Filters (brand, price, rating, discount) | ✅ facets-driven filter sheet |
| Sort (price, newest, popularity, rating) | ✅ 7 sort modes |
| Infinite scroll / pagination | ✅ |
| Search suggestions / recent searches | ⛳ |

## Product detail
| Amazon | Valley Rush |
|---|---|
| Image gallery + zoom | ✅ multi-image + fullscreen |
| Title, price, MRP, discount | ✅ |
| Specifications / description | ✅ |
| Ratings & reviews (+ write review) | ✅ |
| **Offer/sellers list (Buy Box)** | ✅ **vendor picker** — pick the exact shop, official store first (this is the core differentiator) |
| Related / "you might also like" | ✅ related products |
| Wishlist (save) | ✅ heart, API-backed |
| Add to cart / Buy now | ✅ |
| Share | ⛳ |

## Cart & checkout
| Amazon | Valley Rush |
|---|---|
| Multi-seller cart | ✅ (items keep their vendor) |
| Quantity, remove | ✅ |
| Coupons / offers | ✅ apply + available-offers list |
| Price breakdown (subtotal/delivery/discount) | ✅ |
| Address selection at checkout | ✅ |
| Payment: cards, UPI, net banking, wallets | ✅ via Razorpay (one sheet) |
| Cash on Delivery | ✅ |
| Order confirmation | ✅ screen + email |
| Save for later | ⛳ |

## Orders, tracking, support
| Amazon | Valley Rush |
|---|---|
| Order history | ✅ |
| Order detail | ✅ items, address, payment, per-shop settlement |
| Status tracking timeline | ✅ (Placed→Confirmed→Shipped→Out→Delivered) |
| Live map GPS tracking | ⛳ (arrives with the Delivery app) |
| Cancel order | ✅ (pre-ship) + auto-restock/refund |
| Returns / exchange | 🟡 request via support ticket; full RMA workflow ⛳ |
| Invoice PDF | ⛳ |
| Re-order / rate after delivery | ⛳ |
| Help / customer service | ✅ per-order support ticket; live chat ⛳ |

## Addresses, wishlist, profile
| Amazon | Valley Rush |
|---|---|
| Address book (add/edit/default/delete) | ✅ |
| Wishlist | ✅ |
| Notifications inbox | ⛳ (push delivery ✅ once FCM live) |
| Language switch | ⛳ EN↔Urdu planned |

## Platform / infra (not user-visible)
| Amazon-scale need | Valley Rush |
|---|---|
| Secure auth, RBAC | ✅ JWT + roles |
| Payments + settlement | ✅ Razorpay + per-vendor split records |
| Email / SMS / push | ✅ wired (dev-mode until keys) |
| Images on CDN | ✅ S3 (+ CloudFront when configured) |
| Search infra | ✅ Postgres FTS |
| API docs / testing | ✅ Swagger |

## Summary
The **core Amazon shopping loop is complete**: discover → search/filter →
choose vendor → cart → checkout (all payment methods + COD) → track → support →
wishlist/profile/coupons. Deliberately deferred (documented, not forgotten):
Google sign-in, EN↔Urdu, live GPS map (Delivery app), full returns RMA, invoice
PDF, save-for-later, live chat, and behavioural recommendations. None block a
production launch of the customer app; they're the polish/roadmap items in
[`ROADMAP.md`](ROADMAP.md).
