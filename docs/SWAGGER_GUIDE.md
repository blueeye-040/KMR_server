# Valley Rush — Swagger / API Testing Guide

## What is Swagger (and why it's here)
**Swagger UI** is an auto-generated, interactive web page that lists **every API
endpoint** in the backend and lets you **call them from the browser** — no Postman,
no code. It reads a machine-readable description of the API (the **OpenAPI spec**)
that Spring generates from the controllers, so it's always in sync with the code.

Use it to: explore what the API can do, see each request/response shape, and test
endpoints live (register, log in, place an order, etc.) on your local or hosted
server.

## Where it lives
| | URL |
|---|---|
| Swagger UI (the page you use) | `<server>/swagger-ui.html` |
| Raw OpenAPI spec (JSON) | `<server>/v3/api-docs` |

- Local: `http://localhost:8080/swagger-ui.html`
- Hosted: `https://<your-domain>/swagger-ui.html` (works the same once deployed).

Both are **public** so you can always reach the docs — but the endpoints
themselves still enforce auth (see below).

## The one thing to understand: 🔒 = needs login
Each endpoint shows an icon:
- **open padlock / no lock** → public, call it directly.
- **closed padlock 🔒** → needs a logged-in user (a JWT token).
- Admin endpoints need a user whose **role is ADMIN**.

## Step-by-step: log in and test a secured endpoint
1. Open `…/swagger-ui.html`.
2. **Get an account** (first time): expand **POST `/api/auth/send-otp`** →
   *Try it out* → enter your phone → *Execute*. In dev mode the OTP is printed in
   the **server console** (`[DEV] OTP for … → 123456`). Then **POST
   `/api/auth/verify-otp`** with that code, then **POST `/api/auth/register`**
   with name/email/phone/password.
   *(Already have an account? Just use **POST `/api/auth/login`**.)*
3. The login/register response contains a **`token`** — copy its value (the long
   string, without quotes).
4. Click the green **Authorize** button (top-right). Paste the token and click
   **Authorize**, then **Close**.
5. Done — every 🔒 endpoint now automatically sends
   `Authorization: Bearer <your token>`. Try **GET `/api/profile`** or
   **POST `/api/orders`**.

To test **admin** endpoints (e.g. create a coupon), your user's role must be
`ADMIN`. Promote a user once in the database:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'you@example.com';
```
Re-authorize is not needed — role is read fresh from the DB on each call.

## How to read an endpoint
- **Parameters** — path/query inputs (e.g. `q`, `page` on `/api/products`).
- **Request body** — click *Try it out* to edit the sample JSON.
- **Responses** — the shape + HTTP codes you'll get back.
- **Execute** — runs the real call; you see the live response, status, and the
  exact `curl` command it used.

## Credentials for a hosted test server
For a shared/hosted environment, create a dedicated tester login and (if they need
admin) set role=ADMIN, then share those credentials. Anyone can then open the
hosted `…/swagger-ui.html`, log in via `/api/auth/login`, click Authorize, and
test. Nothing else to install.

> Security note: Swagger UI is convenient but exposes your full API surface. On
> production you may want to restrict `/swagger-ui/**` + `/v3/api-docs/**` to
> internal access (VPN/IP allowlist) or disable it — controlled in `SecurityConfig`.
