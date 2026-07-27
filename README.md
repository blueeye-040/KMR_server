# KMR Multi-Vendor Marketplace — Backend

Spring Boot 3.4 (Java 17) REST API for the KMR marketplace. PostgreSQL on
Supabase; all other services on AWS. This repo is also the **program hub** —
see [`docs/`](docs/) for the architecture, roadmap, and progress log across all
four apps (customer, seller, delivery, admin).

### 📚 Documentation
| Doc | What it covers |
|---|---|
| [docs/PROGRESS.md](docs/PROGRESS.md) | **Start here when resuming** — current state + next step |
| [docs/ROADMAP.md](docs/ROADMAP.md) | Full delivery plan (all 4 apps) as checkboxes |
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | System design + the payment-split rule |
| [docs/SERVER_GUIDE.md](docs/SERVER_GUIDE.md) | **Backend reference** — every API (public/private/admin), services, data model |
| [docs/SWAGGER_GUIDE.md](docs/SWAGGER_GUIDE.md) | How to open & use Swagger to test the API (with login) |
| [docs/FCM_GUIDE.md](docs/FCM_GUIDE.md) | What push/FCM is + exact steps to make it live |
| [docs/INTEGRATIONS.md](docs/INTEGRATIONS.md) | Accounts & API keys to create (Razorpay, AWS, FCM, …) |
| [docs/AMAZON_PARITY.md](docs/AMAZON_PARITY.md) | Feature-by-feature check vs Amazon |
| [docs/FEATURE_SPEC.md](docs/FEATURE_SPEC.md) | Amazon-parity feature map + category taxonomy |
| Mobile app guide | in the Flutter repo: `docs/APP_GUIDE.md` |

Interactive API docs run at **`/swagger-ui.html`** when the server is up.

## The marketplace in one line
Amazon-style catalog where **each product is sold by multiple vendors (shops)**.
The buyer picks a vendor on the product page; **the item price settles to that
vendor's bank account and the delivery fee is kept by the platform.**

## Run
```bash
./mvnw spring-boot:run     # http://localhost:8080, reads secrets from .env
./mvnw test                # tests
./mvnw clean package       # build jar
```

## Configuration (`.env`, git-ignored)
Secrets are read from `.env` and referenced in `application.properties`.

| Key | Purpose | Status |
|-----|---------|--------|
| `DATABASE_URL` / `DATABASE_USERNAME` / `DATABASE_PASSWORD` | Supabase Postgres | ✅ set |
| `JWT_SECRET` | JWT signing (≥32 chars) | ✅ set |
| `ACCESS_KEY` / `SECRET_ACCESS_KEY` / `AWS_REGION` | AWS (SNS, S3) | ✅ set |
| `S3_BUCKET_NAME` | product/media images | ✅ set |
| `CLOUDFRONT_DOMAIN` | CDN base URL for media | ⛳ replace with real distribution |
| `DEV_MODE` | `true` prints OTP to stdout instead of sending SMS | ✅ `true` |
| `RAZORPAY_KEY_ID` / `RAZORPAY_KEY_SECRET` | online payments | ⛳ **client to fill** — blank = dev mode |

**Dev mode niceties (no external keys needed):**
- OTP is printed to stdout: `[DEV] OTP for <phone> → <code>`.
- Online payments are simulated (a fake `order_dev_*` id is issued and
  verification always succeeds), so checkout works end-to-end. Add real Razorpay
  keys to `.env` to switch to the live gateway.

## Tech
Spring Web · Spring Security (JWT, BCrypt) · Spring Data JPA · PostgreSQL
(Supabase, via PgBouncer) · AWS SDK (SNS, S3) · Razorpay (via REST, no SDK).

## API
The verified endpoint list lives in [`docs/PROGRESS.md`](docs/PROGRESS.md#verified-api-surface-customer).
All under `/api`. Public: `/api/auth/**`, `/api/home`, `/api/categories`,
`GET /api/products/**`. Everything else needs a `Bearer` JWT.
