# 🛒 ShopSmart E-Commerce (Spring Boot)

A backend-focused e-commerce application built with Spring Boot 3, Spring Security, Spring Data JPA, Thymeleaf, and Flyway. Includes a Rust Axum analytics sidecar, full REST API, responsive frontend, and 61 passing tests.

---

## ▶️ Quick Start

### Build
```cmd
mvnw.cmd clean package
```

### Run
```cmd
mvnw.cmd spring-boot:run
```

### Test
```cmd
mvnw.cmd test
```

| URL | Description |
|-----|-------------|
| http://localhost:8080 | Main app |
| http://localhost:8080/swagger-ui.html | API docs |
| http://localhost:8080/h2-console | H2 DB console (dev) |
| http://localhost:8080/admin/dashboard | Admin charts |

**H2 Connection (dev):**
```
JDBC URL:  jdbc:h2:mem:testdb
Username:  sa
Password:  (empty)
```

---

## 🔐 Default Accounts

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |

Seeded coupons: `SAVE10` (10% off), `FLAT500` (₹500 off)

---

## 🦀 Rust Sidecar (optional)

Requires Spring Boot already running on `:8080`.

```cmd
cd rust-service
cargo run
```

Rust service → http://localhost:8081

---

## 🐳 Docker

```cmd
docker-compose up --build
```

Starts Spring Boot + PostgreSQL + Rust sidecar together.

---

## 📂 Project Structure

```
src/
├── main/
│   ├── java/com/shopsmart/
│   │   ├── ShopsmartApplication.java
│   │   ├── config/
│   │   │   ├── SecurityConfig.java        ← form login + HTTP Basic, ADMIN/USER roles
│   │   │   ├── CorsConfig.java
│   │   │   ├── RateLimitFilter.java
│   │   │   ├── WebConfig.java             ← static resource mapping (uploads/)
│   │   │   ├── OpenApiConfig.java         ← Swagger/springdoc
│   │   │   ├── DataInitializer.java       ← seeds users + coupons on startup
│   │   │   └── OrderScheduler.java
│   │   ├── controller/
│   │   │   ├── ProductController.java     ← REST API + /api/v1 dual paths
│   │   │   ├── CartController.java
│   │   │   ├── OrderController.java
│   │   │   ├── CouponController.java
│   │   │   ├── WishlistController.java
│   │   │   ├── AuthController.java
│   │   │   ├── DashboardController.java
│   │   │   └── UiController.java          ← Thymeleaf page routes
│   │   ├── exception/
│   │   │   ├── GlobalExceptionHandler.java
│   │   │   └── ApiErrorResponse.java
│   │   ├── model/
│   │   │   ├── Product.java + ProductStats.java
│   │   │   ├── User.java
│   │   │   ├── Cart.java + CartItem.java
│   │   │   ├── Order.java + OrderItem.java
│   │   │   ├── Coupon.java
│   │   │   └── WishlistItem.java
│   │   ├── repository/        ← JPA repositories (8 total)
│   │   └── service/
│   │       ├── UserService.java
│   │       ├── UserDetailsServiceImpl.java
│   │       └── EmailService.java
│   └── resources/
│       ├── db/migration/      ← Flyway V1–V6
│       ├── templates/         ← index, cart, orders, order-detail, login,
│       │                         register, dashboard, wishlist, email/
│       ├── static/style.css
│       ├── application.properties
│       ├── application-dev.properties   ← H2
│       └── application-prod.properties  ← PostgreSQL
├── test/
│   └── java/com/shopsmart/shopsmart/
│       ├── ProductApiTests.java          ← 48 integration tests
│       ├── CommerceApiTests.java         ← coupon, wishlist, image upload
│       ├── EmailServiceTest.java         ← email unit tests
│       ├── UserServiceTest.java          ← registration unit tests (Mockito)
│       └── ShopsmartApplicationTests.java
rust-service/                  ← Axum sidecar on :8081
docs/
├── index.md
├── architecture/              ← overview, database, api-reference
└── guides/                   ← setup, running-tests, troubleshooting, roadmap
```

---

## 🚀 Features

- **Products** — full CRUD, search, category filter, pagination, stats, low-stock alerts, bulk delete, random generator, image upload (shown on product cards/modals)
- **Cart** — per-user cart, add/update/remove items, coupon apply/preview before checkout
- **Orders** — checkout (with optional coupon), order history, admin status management, HTML order-confirmation email
- **Coupons** — percentage and flat discount codes (wired on cart page)
- **Wishlist** — add/remove from product cards and wishlist page
- **Auth** — form login + HTTP Basic, register, ADMIN/USER roles
- **Admin dashboard** — Chart.js charts (donut + bar)
- **Rust sidecar** — analytics summary, search proxy, low-stock proxy on `:8081`
- **Dark mode** — toggle on frontend
- **Sort + price filter** — on product catalog
- **Docker** — full stack via `docker-compose`
- **CI** — GitHub Actions (Java + Rust build)
- **Actuator** — `/actuator/health`, `/actuator/info`, `/actuator/metrics`

---

## 🧱 Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.0 |
| Web | Spring Web MVC |
| Persistence | Spring Data JPA + Hibernate 6 + Flyway |
| Security | Spring Security (form login + HTTP Basic) |
| Database | H2 (dev) / PostgreSQL 16 (prod) |
| Templating | Thymeleaf |
| Validation | Jakarta Bean Validation |
| API Docs | springdoc-openapi (Swagger UI) |
| Frontend | HTML + CSS (custom) + Vanilla JS + Chart.js |
| Sidecar | Rust + Axum |
| Testing | JUnit 5 + MockMvc + Mockito + JaCoCo |
| Build | Maven |
| CI | GitHub Actions |
| Container | Docker + docker-compose |

---

## 📌 API Reference

All endpoints available under both `/` and `/api/v1/` (e.g. `/products` = `/api/v1/products`).

### Products

| Method | Endpoint | Auth | Notes |
|--------|----------|------|-------|
| GET | `/products` | USER+ | `?page=0&size=10` |
| GET | `/products/{id}` | USER+ | |
| POST | `/products` | ADMIN | |
| PUT | `/products/{id}` | ADMIN | |
| DELETE | `/products/{id}` | ADMIN | |
| POST | `/products/{id}/image` | ADMIN | multipart, jpg/png/webp, 2MB |
| GET | `/products/search?name=` | USER+ | case-insensitive |
| GET | `/products/category/{category}` | USER+ | |
| GET | `/products/stats` | USER+ | count, avg price, total stock |
| GET | `/products/low-stock?threshold=10` | USER+ | |
| DELETE | `/products/bulk` | ADMIN | body: `[1,2,3]` |
| GET | `/products/random?count=10` | ADMIN | |

### Cart, Orders, Coupons, Wishlist

| Method | Endpoint | Auth |
|--------|----------|------|
| GET/POST/PUT/DELETE | `/cart/**` | USER+ |
| POST | `/orders/checkout?couponCode=` | USER+ |
| GET | `/orders`, `/orders/{id}` | USER+ |
| PUT | `/orders/{id}/status?status=` | ADMIN |
| POST | `/coupons/validate?code=&orderTotal=` | USER+ |
| GET/POST/DELETE | `/wishlist/**` | USER+ |

Full reference → [docs/architecture/api-reference.md](docs/architecture/api-reference.md)

---

## 🧪 Tests

**61 tests — all passing** (`mvnw.cmd test`)

| Suite | Count | Covers |
|-------|-------|--------|
| `ProductApiTests` | 48 | Products, security, cart, orders |
| `CommerceApiTests` | 7 | Coupons, wishlist, image upload |
| `EmailServiceTest` | 3 | HTML email / mail-disabled paths |
| `UserServiceTest` | 2 | Registration unit tests |
| `ShopsmartApplicationTests` | 1 | Context load |

Full test matrix → [docs/guides/running-tests.md](docs/guides/running-tests.md)

---

## 📚 Docs

See [docs/index.md](docs/index.md) for the full documentation index.

---

## 👨‍💻 Author

Built as a learning project to understand Spring Boot backend development — extended with full commerce features, Rust sidecar, responsive frontend, Docker, CI, and comprehensive test coverage.
