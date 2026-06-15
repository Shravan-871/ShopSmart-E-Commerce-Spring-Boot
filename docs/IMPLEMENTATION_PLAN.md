# ShopSmart — Implementation Plan

**Source of truth for what to build next.**  
Status snapshot and API reference → [`PROJECT_STATUS.md`](PROJECT_STATUS.md)

Last updated: 2026-06-13

---

## Architecture

```
Browser
   │
   ├── :8080  Spring Boot   UI, auth, CRUD, cart, orders, Swagger
   │
   └── :8081  Rust (Axum)  analytics, search, low-stock (proxies Spring in dev)
```

| URL | Service |
|-----|---------|
| http://localhost:8080 | Main shop (use this) |
| http://localhost:8080/swagger-ui.html | API docs |
| http://localhost:8080/admin/dashboard | Admin charts |
| http://localhost:8081 | Rust landing + APIs |
| http://localhost:8081/health | Rust health |

**Run:**
```cmd
mvnw.cmd spring-boot:run          REM terminal 1 — port 8080
cd rust-service && cargo run      REM terminal 2 — port 8081 (optional)
mvnw.cmd test                     REM 51 tests
```

---

## Completed — Phases 1–8

| Phase | Focus | Status |
|-------|-------|--------|
| 1 | Swagger / OpenAPI | ✅ Done |
| 2 | Admin dashboard + Rust sidecar | ✅ Done (line chart pending) |
| 3 | Image upload API + email service | ⚠️ Backend only — UI/email incomplete |
| 4 | Coupons + wishlist | ⚠️ API done — UI wire-up incomplete |
| 5 | `/api/v1/*`, CORS, rate limit, structured errors | ✅ Done (no ETag/logging yet) |
| 6 | Dark mode, sort, price filter | ✅ Done (skeleton/scroll/shortcuts pending) |
| 7 | UserService unit tests + JaCoCo | ✅ Partial (2 unit tests) |
| 8 | Docker, Actuator, CI | ✅ Done (no Prometheus/Grafana yet) |

---

## Phase 9 — Wire-up & Polish (NEXT)

**Goal:** Connect backend features to the UI and add missing tests/docs.  
**Order:** Do steps 9.1 → 9.6 sequentially. Verify after each step.

### Step 9.1 — Product images in UI

| Action | File(s) |
|--------|---------|
| Pass `imageUrl` in product card `data-*` attrs | `templates/index.html` |
| Show `<img>` when `imageUrl` set, else keep SVG | `templates/index.html`, `static/style.css` |
| Show image in detail modal | `templates/index.html` |
| Admin: file input in edit modal → `POST /products/{id}/image` | `templates/index.html` (JS) |
| Verify upload dir `uploads/` works at runtime | already in `ProductController`, `WebConfig` |

**Verify:** Upload jpg on a product → card and modal show real image.

---

### Step 9.2 — Coupon at checkout

| Action | File(s) |
|--------|---------|
| Add coupon code input + Apply on cart page | `templates/cart.html` |
| Optional: preview via `POST /coupons/validate` | `templates/cart.html` (JS) |
| Pass `couponCode` on checkout `POST /orders/checkout?couponCode=` | `templates/cart.html` (JS) |
| Show discounted total in toast / summary | `templates/cart.html` |

**Verify:** `SAVE10` or `FLAT500` reduces order total at checkout.

---

### Step 9.3 — Wishlist from product cards

| Action | File(s) |
|--------|---------|
| Add ❤️ button on each product card | `templates/index.html` |
| `POST /wishlist/add/{productId}` on click | `templates/index.html` (JS) |
| Toast on success / already-in-wishlist | `templates/index.html` |
| Improve wishlist page (category, add-to-cart) | `templates/wishlist.html` |

**Verify:** Click heart → item appears on `/wishlist-page`.

---

### Step 9.4 — Email (HTML + low-stock)

| Action | File(s) |
|--------|---------|
| HTML order confirmation template | `templates/email/order-confirmation.html` |
| Use `MimeMessage` + Thymeleaf in `EmailService` | `EmailService.java` |
| Call `sendLowStockAlert` when product stock drops ≤ threshold | `OrderController.java` or `ProductController.java` |
| Document env vars: `MAIL_ENABLED`, `SMTP_*` | `application-dev.properties`, `PROJECT_STATUS.md` |
| Unit test with mocked `JavaMailSender` | `EmailServiceTest.java` |

**Verify:** With Mailtrap + `MAIL_ENABLED=true`, checkout sends HTML email.

---

### Step 9.5 — Tests for Phase 9

| Action | File(s) |
|--------|---------|
| Coupon validate + checkout with coupon | `ProductApiTests.java` or `CommerceApiTests.java` |
| Wishlist add/remove/list | same |
| Image upload (ADMIN, valid/invalid type) | same |
| Email service mock test | `EmailServiceTest.java` |

**Verify:** `mvnw.cmd test` — all green, count increased.

---

### Step 9.6 — Docs sync

| Action | File(s) |
|--------|---------|
| Rewrite README (ports, Rust, 51 tests, accounts) | `README.md` |
| Mark Phase 9 items done in status file | `PROJECT_STATUS.md` |
| Update cursor rule snapshot | `.cursor/rules/shopsmart-project.mdc` |

**Verify:** README matches reality; no stale “25 tests” or port 80 references.

---

## Phase 10 — Commerce & Admin depth

| Step | Feature | Files (planned) |
|------|---------|-----------------|
| 10.1 | On-sale products (`originalPrice`, sale badge) | `V7__product_sale.sql`, `Product.java`, `index.html` |
| 10.2 | Dashboard line chart (products over time) | `V7` or `V8` add `product.created_at`, `dashboard.html` |
| 10.3 | Admin order management UI | `AdminOrderController.java`, `admin-orders.html` |
| 10.4 | Product reviews (1–5 stars) | `V8__reviews.sql`, `Review.java`, `ReviewController.java` |

---

## Phase 11 — Rust advanced

| Step | Feature | Files (planned) |
|------|---------|-----------------|
| 11.1 | `cargo test` for Rust routes | `rust-service/tests/api_tests.rs` |
| 11.2 | Optional Rust search on home page | `index.html`, fallback to Spring |
| 11.3 | Redis cache for analytics/search | `docker-compose.yml`, Rust `redis` crate |
| 11.4 | WebSocket live order status | Rust `ws` route + `order-detail.html` |
| 11.5 | Image resize on upload (Rust) | new Rust route + Spring hook |

---

## Phase 12 — Production & polish

| Step | Feature | Files (planned) |
|------|---------|-----------------|
| 12.1 | Request/response logging filter | `LoggingFilter.java` |
| 12.2 | ETag on `GET /products` | `ProductController.java` |
| 12.3 | Prometheus + Grafana | `pom.xml`, `docker-compose.yml`, dashboards |
| 12.4 | UI: skeleton loaders, infinite scroll, `/` shortcut | `index.html`, `style.css` |
| 12.5 | Mobile bottom nav | `style.css`, shared fragment |
| 12.6 | `.env.example` + deploy checklist | repo root |

---

## Phase 13 — Test coverage expansion

| Step | Feature | Files (planned) |
|------|---------|-----------------|
| 13.1 | `@DataJpaTest` for repositories | `src/test/java/.../repository/` |
| 13.2 | More service unit tests | `CartService` extract optional |
| 13.3 | JaCoCo threshold in CI | `.github/workflows/ci.yml` |
| 13.4 | Rust integration tests in CI | `ci.yml` |

---

## Decision log

| Date | Decision |
|------|----------|
| 2026-06-13 | Phases 1–8 shipped; Phase 9 focuses on UI wire-up before new domains |
| 2026-06-13 | Rust MVP proxies Spring (not direct DB) in dev — simpler with H2 |
| 2026-06-13 | Dual API paths: `/products` and `/api/v1/products` for backward compat |

---

## How to use this file

1. Pick the **lowest incomplete step** (currently **9.1**).
2. Edit only the files listed for that step.
3. Run `mvnw.cmd test` (and `cargo test` when touching Rust).
4. Check off step in this file and update `PROJECT_STATUS.md`.
5. Move to next step.
