# ShopSmart E-Commerce — Project Status

**Next work:** step-by-step plan in [`IMPLEMENTATION_PLAN.md`](IMPLEMENTATION_PLAN.md) (currently **Phase 9**).

---

## Snapshot

| Item | Value |
|------|-------|
| Java | 17 · Spring Boot 3.3.0 |
| DB | H2 (dev) / PostgreSQL (prod) · Flyway V1–V6 |
| UI | Thymeleaf + vanilla JS + custom CSS |
| Sidecar | Rust Axum on `:8081` (`rust-service/`) |
| Tests | **51 passing** (`mvnw.cmd test`) |
| API docs | http://localhost:8080/swagger-ui.html |

## How to Run

```cmd
mvnw.cmd spring-boot:run       REM http://localhost:8080  (main app — not port 80)
mvnw.cmd test                  REM 51 tests
cd rust-service && cargo run   REM http://localhost:8081  (optional, needs Spring up)
docker-compose up --build      REM app + postgres + rust
```

> **Note:** Shop UI is **http://localhost:8080**, not `http://localhost/`. Rust root `/` shows an API landing page.

## Default Accounts

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |

Seeded coupons: `SAVE10` (10%), `FLAT500` (₹500 off).

---

## Phases 1–8 — Completed

| Phase | Delivered |
|-------|-----------|
| **1** | Swagger/springdoc, `@Tag` on controllers |
| **2** | `/admin/dashboard` (Chart.js), `rust-service/` (health, analytics, search, landing `/`) |
| **3** | `imageUrl` + `POST /products/{id}/image`, `EmailService` (logs when mail off) |
| **4** | `Coupon` + `WishlistItem`, checkout coupon param, wishlist REST + page |
| **5** | Dual paths `/api/v1/*`, CORS, `RateLimitFilter`, `ApiErrorResponse`, HTTP Basic for Rust |
| **6** | Dark mode, sort, min/max price filter on home |
| **7** | `UserServiceTest` (Mockito), JaCoCo on `mvn verify` |
| **8** | `Dockerfile`, `docker-compose.yml`, Actuator, `.github/workflows/ci.yml` |

---

## Partially done — gaps (Phase 9 targets)

Backend exists; UI or tests still missing.

| Feature | Backend | UI / other gap |
|---------|---------|----------------|
| Product images | `POST /products/{id}/image`, `imageUrl` field | Cards/modals still SVG-only; no upload in edit modal |
| Coupons | `POST /coupons/validate`, checkout `?couponCode=` | No coupon field on cart page |
| Wishlist | REST API + `/wishlist-page` | No ❤️ button on product cards |
| Email | `EmailService` on checkout | Plain log only; no HTML template; no low-stock trigger |
| Dashboard | Donut + bar charts | No “products over time” line chart (`created_at` missing) |
| On-sale products | — | Not started |
| Rust stretch | MVP + landing page | No `cargo test`, cache, WebSocket |

See [`IMPLEMENTATION_PLAN.md`](IMPLEMENTATION_PLAN.md) for step-by-step file edit plan.

---

## Migrations

| Version | File | Purpose |
|---------|------|---------|
| V1 | `init_schema` | product, app_user |
| V2 | `seed_users` | placeholder (runtime seed) |
| V3 | `cart_order_schema` | cart, orders |
| V4 | `order_lifecycle` | confirmed_at, shipped_at, stock_restored |
| V5 | `product_image` | image_url column |
| V6 | `coupons_wishlist` | coupon, wishlist_item + seed coupons |

---

## File Structure (current)

```
src/main/java/com/shopsmart/
  config/     DataInitializer, OrderScheduler, SecurityConfig, OpenApiConfig,
              CorsConfig, RateLimitFilter, WebConfig
  controller/ Auth, Product, Cart, Order, Ui, Dashboard, Coupon, Wishlist
  exception/  GlobalExceptionHandler, ApiErrorResponse
  model/      Product, User, Cart, CartItem, Order, OrderItem, Coupon, WishlistItem
  repository/ Product, User, Cart, CartItem, Order, OrderItem, Coupon, Wishlist
  service/    UserService, UserDetailsServiceImpl, EmailService

rust-service/   Axum sidecar (src/routes/, spring_client.rs)

src/main/resources/
  db/migration/ V1–V6
  templates/    index, cart, orders, order-detail, login, register,
                dashboard, wishlist
  static/       style.css

Dockerfile, docker-compose.yml, .github/workflows/ci.yml
IMPLEMENTATION_PLAN.md   ← next steps
```

---

## API Quick Reference

All REST endpoints also available under `/api/v1/...` (e.g. `/api/v1/products`).

### Products

| Method | Endpoint | Auth | Notes |
|--------|----------|------|-------|
| GET | `/products` | USER+ | Paginated |
| GET | `/products/{id}` | USER+ | |
| POST | `/products` | ADMIN | |
| PUT | `/products/{id}` | ADMIN | |
| DELETE | `/products/{id}` | ADMIN | |
| POST | `/products/{id}/image` | ADMIN | multipart, jpg/png/webp, 2MB |
| GET | `/products/search?name=` | USER+ | |
| GET | `/products/stats` | USER+ | |
| GET | `/products/low-stock?threshold=10` | USER+ | |
| GET | `/products/random?count=10` | ADMIN | |

### Cart & orders

| Method | Endpoint | Auth | Notes |
|--------|----------|------|-------|
| GET | `/cart` | USER+ | |
| POST | `/cart/add?productId=&quantity=` | USER+ | |
| PUT | `/cart/update/{itemId}?quantity=` | USER+ | |
| DELETE | `/cart/remove/{itemId}` | USER+ | |
| POST | `/orders/checkout?couponCode=` | USER+ | optional coupon |
| GET | `/orders` | USER+ | |
| PUT | `/orders/{id}/status?status=` | ADMIN | |

### Coupons & wishlist

| Method | Endpoint | Auth |
|--------|----------|------|
| POST | `/coupons/validate?code=&orderTotal=` | USER+ |
| GET | `/wishlist` | USER+ |
| POST | `/wishlist/add/{productId}` | USER+ |
| DELETE | `/wishlist/remove/{productId}` | USER+ |

### UI pages

| Path | Description |
|------|-------------|
| `/` | Product catalog |
| `/cart-page` | Cart |
| `/my-orders` | Order history |
| `/wishlist-page` | Wishlist |
| `/admin/dashboard` | Admin charts |
| `/swagger-ui.html` | API docs |

### Rust (`:8081`)

| Method | Endpoint |
|--------|----------|
| GET | `/` landing |
| GET | `/health` |
| GET | `/api/v1/analytics/summary` |
| GET | `/api/v1/search?q=` |
| GET | `/api/v1/products/low-stock?threshold=` |

---

## Tests (51)

| Suite | Count | Covers |
|-------|-------|--------|
| `ProductApiTests` | 48 | Products, security, cart, orders |
| `UserServiceTest` | 2 | Registration unit tests |
| `ShopsmartApplicationTests` | 1 | Context load |

**Not yet tested:** coupons, wishlist, image upload, email (planned in Phase 9.5).

---

## Validation Rules

| Field | Rule |
|-------|------|
| `name` | Required, alphabets and spaces only |
| `price` | Must be > 0 |
| `category` | Required |
| `stock` | Must be >= 0 |

Categories: Electronics, Clothing, Food, Accessories, Computers, Audio, Sports, Books

---

## Historical DONE sections

Early bug fixes, cart/order feature details, and the original 49-test matrix are unchanged in git history. This file now tracks **current state + forward plan** only. For the full original changelog, see git log / earlier commits.
