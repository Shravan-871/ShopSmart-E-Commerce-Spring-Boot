# Architecture Overview

## System Diagram

```
Browser
   │
   ├── :8080  Spring Boot (main app)
   │          UI, auth, CRUD, cart, orders, coupons, wishlist, Swagger
   │
   └── :8081  Rust Axum (optional sidecar)
              analytics, search, low-stock proxy
```

The Rust sidecar proxies Spring via HTTP Basic auth — it does not connect to the DB directly.
In dev both services run locally. In Docker they communicate over the internal network.

---

## Layer Map

| Layer | Package / Location | Responsibility |
|-------|--------------------|----------------|
| Controllers (REST + UI) | `controller/` | HTTP handling, request/response |
| Services | `service/` | Business logic (user, email) |
| Repositories | `repository/` | JPA data access |
| Models | `model/` | JPA entities + DTOs |
| Config | `config/` | Security, CORS, rate-limit, Flyway, OpenAPI |
| Templates | `resources/templates/` | Thymeleaf server-side UI |
| Migrations | `resources/db/migration/` | Flyway V1–V6 schema |
| Rust sidecar | `rust-service/` | Axum analytics/search on :8081 |

---

## Controllers

| Controller | Routes | Auth |
|------------|--------|------|
| `ProductController` | `/products/**`, `/api/v1/products/**` | ADMIN (write), USER+ (read) |
| `CartController` | `/cart/**` | USER+ |
| `OrderController` | `/orders/**` | USER+, ADMIN (status) |
| `CouponController` | `/coupons/**` | USER+ |
| `WishlistController` | `/wishlist/**` | USER+ |
| `AuthController` | `/register` | public |
| `DashboardController` | `/admin/dashboard` | ADMIN |
| `UiController` | `/`, `/cart-page`, `/my-orders`, etc. | USER+ |

---

## Security Model

- Form login + HTTP Basic (for Rust sidecar calls)
- Two roles: `ADMIN`, `USER`
- CSRF disabled
- Rate limiting via `RateLimitFilter`
- CORS configured in `CorsConfig`

Default seeded accounts (dev only):

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |

---

## Profiles

| Profile | DB | Notes |
|---------|----|-------|
| `dev` (default) | H2 in-memory | H2 console at `/h2-console` |
| `prod` | PostgreSQL | Requires `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars |

---

## Key Config Classes

| Class | Purpose |
|-------|---------|
| `SecurityConfig` | Form login, HTTP Basic, role-based access |
| `CorsConfig` | Cross-origin rules |
| `RateLimitFilter` | Simple in-memory rate limiting |
| `WebConfig` | Static resource mapping (uploads dir) |
| `OpenApiConfig` | Swagger/springdoc metadata |
| `DataInitializer` | Seeds default users and coupons on startup |
| `OrderScheduler` | Scheduled job for order lifecycle |

---

## What's Built (Phases 1–8)

| Phase | Delivered |
|-------|-----------|
| 1 | Swagger/springdoc, `@Tag` on controllers |
| 2 | `/admin/dashboard` (Chart.js), Rust sidecar (health, analytics, search, landing) |
| 3 | `imageUrl` field + `POST /products/{id}/image`, `EmailService` (logs when mail off) |
| 4 | `Coupon` + `WishlistItem`, checkout coupon param, wishlist REST + page |
| 5 | Dual paths `/api/v1/*`, CORS, `RateLimitFilter`, `ApiErrorResponse`, HTTP Basic for Rust |
| 6 | Dark mode, sort, min/max price filter on home |
| 7 | `UserServiceTest` (Mockito), JaCoCo on `mvn verify` |
| 8 | `Dockerfile`, `docker-compose.yml`, Actuator, GitHub Actions CI |

---

## Known Gaps (Phase 9 targets)

Backend exists; UI or tests still missing.

| Feature | Backend | Gap |
|---------|---------|-----|
| Product images | `POST /products/{id}/image`, `imageUrl` field | Cards/modals still SVG-only; no upload in edit modal |
| Coupons | `POST /coupons/validate`, checkout `?couponCode=` | No coupon input on cart page |
| Wishlist | REST API + `/wishlist-page` | No ❤️ button on product cards |
| Email | `EmailService` on checkout | Plain log only; no HTML template; no low-stock trigger |
| Dashboard | Donut + bar charts | No line chart (`created_at` missing on product) |
| Rust stretch | MVP + landing page | No `cargo test`, no cache, no WebSocket |
