# Setup Guide

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 17 |
| Maven | bundled via `mvnw.cmd` |
| Docker | optional, for containerized run |
| Rust + Cargo | optional, for sidecar only |

---

## Build

```cmd
mvnw.cmd clean package
```

---

## Run (local dev)

```cmd
mvnw.cmd spring-boot:run
```

App → http://localhost:8080  
H2 Console → http://localhost:8080/h2-console  
Swagger UI → http://localhost:8080/swagger-ui.html

H2 connection details:
```
JDBC URL:  jdbc:h2:mem:testdb
Username:  sa
Password:  (empty)
```

---

## Run Rust Sidecar (optional)

Requires Spring Boot already running on `:8080`.

```cmd
cd rust-service
cargo run
```

Rust service → http://localhost:8081

---

## Run with Docker

Starts Spring Boot + PostgreSQL + Rust sidecar together.

```cmd
docker-compose up --build
```

| Service | URL |
|---------|-----|
| Spring Boot | http://localhost:8080 |
| Rust sidecar | http://localhost:8081 |
| PostgreSQL | localhost:5432 |

To stop:
```cmd
docker-compose down
```

To wipe the DB volume:
```cmd
docker-compose down -v
```

---

## Profiles

| Profile | DB | How to activate |
|---------|----|-----------------|
| `dev` (default) | H2 in-memory | automatic |
| `prod` | PostgreSQL | set `SPRING_PROFILES_ACTIVE=prod` |

Production env vars required when using `prod` profile:

```
DB_URL=jdbc:postgresql://localhost:5432/shopsmart
DB_USERNAME=shopsmart
DB_PASSWORD=shopsmart
```

---

## Default Accounts (seeded on startup)

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | ADMIN |
| `user` | `user123` | USER |

These are seeded by `DataInitializer` on every startup in dev. Do not use in production.

---

## Seeded Coupons

| Code | Type | Value |
|------|------|-------|
| `SAVE10` | Percentage | 10% off |
| `FLAT500` | Flat | ₹500 off |

---

## Mail (optional)

Email is disabled by default. To enable:

```
MAIL_ENABLED=true
SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USERNAME=your@email.com
SMTP_PASSWORD=yourpassword
ADMIN_EMAIL=admin@shopsmart.local
```

When disabled, email actions are logged to console only.
