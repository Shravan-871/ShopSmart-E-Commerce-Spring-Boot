# Roadmap

## Phase 9 — Wire-up & Polish (Next)

Goal: Connect backend features to the UI and add missing tests.  
Do steps 9.1 → 9.6 sequentially. Verify after each step.

| Step | Feature | Files |
|------|---------|-------|
| 9.1 | Product images in UI — show real image on cards/modals, file input in edit modal | `templates/index.html`, `static/style.css` |
| 9.2 | Coupon input on cart page, preview via `/coupons/validate`, pass on checkout | `templates/cart.html` |
| 9.3 | ❤️ button on product cards → `POST /wishlist/add/{id}`, toast feedback | `templates/index.html`, `templates/wishlist.html` |
| 9.4 | HTML email template, `MimeMessage` in `EmailService`, low-stock alert trigger | `EmailService.java`, `templates/email/order-confirmation.html` |
| 9.5 | Tests: coupon checkout, wishlist CRUD, image upload, email mock | `ProductApiTests.java` or `CommerceApiTests.java`, `EmailServiceTest.java` |
| 9.6 | Docs sync: README, PROJECT_STATUS, rules | `README.md`, `.amazonq/rules/` |

---

## Phase 10 — Commerce & Admin depth

| Step | Feature | Files (planned) |
|------|---------|-----------------|
| 10.1 | On-sale products (`originalPrice`, sale badge) | `V7__product_sale.sql`, `Product.java`, `index.html` |
| 10.2 | Dashboard line chart (products over time) | `V7/V8` add `product.created_at`, `dashboard.html` |
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

## Phase 12 — Production & Polish

| Step | Feature | Files (planned) |
|------|---------|-----------------|
| 12.1 | Request/response logging filter | `LoggingFilter.java` |
| 12.2 | ETag on `GET /products` | `ProductController.java` |
| 12.3 | Prometheus + Grafana | `pom.xml`, `docker-compose.yml` |
| 12.4 | UI: skeleton loaders, infinite scroll | `index.html`, `style.css` |
| 12.5 | Mobile bottom nav | `style.css` |
| 12.6 | `.env.example` + deploy checklist | repo root |

---

## Phase 13 — Test Coverage Expansion

| Step | Feature | Files (planned) |
|------|---------|-----------------|
| 13.1 | `@DataJpaTest` for repositories | `src/test/java/.../repository/` |
| 13.2 | More service unit tests | extract `CartService` optional |
| 13.3 | JaCoCo threshold in CI | `.github/workflows/ci.yml` |
| 13.4 | Rust integration tests in CI | `ci.yml` |

---

## Decision Log

| Date | Decision |
|------|----------|
| 2026-06-13 | Phases 1–8 shipped; Phase 9 focuses on UI wire-up before new domains |
| 2026-06-13 | Rust MVP proxies Spring (not direct DB) in dev — simpler with H2 |
| 2026-06-13 | Dual API paths: `/products` and `/api/v1/products` for backward compat |
