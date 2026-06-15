# ShopSmart E-Commerce — Project Rules

## Stack & Run

- Java 17, Maven, package `com.shopsmart`
- Spring Boot 3.3.0 · Spring Web MVC · JPA · Security · Thymeleaf · Flyway (V1–V6)
- Rust Axum sidecar: `rust-service/` on **:8081** (optional)
- Main app: `mvnw.cmd spring-boot:run` → **http://localhost:8080**
- Rust sidecar: `cd rust-service && cargo run` → http://localhost:8081
- Tests: `mvnw.cmd test` → **51 tests**
- Coverage: `mvnw.cmd verify` → `target/site/jacoco/index.html`
- Docker: `docker-compose up --build` → Spring + PostgreSQL + Rust

## Profiles

| Profile | DB |
|---------|----|
| `dev` (default) | H2 in-memory (`jdbc:h2:mem:testdb`) |
| `prod` | PostgreSQL — needs `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` env vars |

## Default Accounts (dev)

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |

## Architecture

| Layer | Location |
|-------|----------|
| REST + UI controllers | `controller/` |
| Services | `service/` (UserService, EmailService) |
| Repositories | `repository/` (8 JPA repos) |
| Models | `model/` (Product, User, Cart, Order, Coupon, WishlistItem…) |
| Config | `config/` (Security, CORS, RateLimit, WebConfig, OpenApi, DataInitializer) |
| Templates | `resources/templates/` (index, cart, orders, login, register, dashboard, wishlist) |
| Migrations | `resources/db/migration/` V1–V6 |
| Rust sidecar | `rust-service/` — proxies Spring via HTTP Basic |

## Dual API Paths

All REST endpoints work under both `/` and `/api/v1/` — e.g. `/products` = `/api/v1/products`.

## Security

- Form login + HTTP Basic (HTTP Basic used by Rust sidecar)
- Roles: `ADMIN` (write ops), `USER` (read ops)
- CSRF disabled
- Rate limiting via `RateLimitFilter`

## Docs Layout

```
docs/
├── index.md                         ← start here
├── architecture/
│   ├── overview.md                  ← system diagram, layers, security, profiles, completed phases, known gaps
│   ├── database.md                  ← Flyway V1–V6, all models
│   └── api-reference.md             ← full endpoint table for all controllers
└── guides/
    ├── setup.md                     ← build, run, Docker, accounts, mail config
    ├── running-tests.md             ← 51 tests, suites, JaCoCo, CI
    ├── troubleshooting.md           ← port conflicts, H2, auth, Rust, Docker, Flyway
    └── roadmap.md                  ← Phases 9–13 planned features, decision log
```

## Active Work

Phase 9 (wire-up): images in UI → coupon on cart → wishlist button → HTML email → tests → README sync.
See `docs/guides/roadmap.md` Phase 9 steps.

## Conventions

- Business logic lives in controllers (no separate service layer for most features)
- New schema changes → new Flyway migration file, never edit existing ones
- Test count in README must stay in sync after adding tests
- Uploads stored in `uploads/` dir, served via `WebConfig`
