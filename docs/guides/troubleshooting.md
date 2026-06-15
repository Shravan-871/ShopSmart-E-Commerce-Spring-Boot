# Troubleshooting

## App won't start — port 8080 already in use

```
Web server failed to start. Port 8080 was already in use.
```

Find and kill the process using the port:

```cmd
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

Or change the port in `application-dev.properties`:
```properties
server.port=8081
```

---

## H2 Console shows no tables

Make sure you use exactly this JDBC URL (not the file-based one):
```
jdbc:h2:mem:testdb
```

Also confirm Flyway ran — check startup logs for lines like:
```
Flyway Community Edition ... has successfully applied 6 migrations
```

If migrations failed, check `db/migration/` SQL files for syntax errors.

---

## 401 Unauthorized on API calls

All endpoints (except `/login`, `/register`) require authentication.

- Browser: you'll be redirected to `/login`
- REST client / curl: use HTTP Basic

```bash
curl -u admin:admin123 http://localhost:8080/products
```

Default accounts: `admin / admin123` (ADMIN), `user / user123` (USER).

---

## 403 Forbidden

You're authenticated but don't have the right role.

- Write operations (`POST`, `PUT`, `DELETE`) on `/products` require `ADMIN`
- `/admin/dashboard` requires `ADMIN`
- Use `admin / admin123` for admin actions

---

## Rust sidecar not responding

The Rust sidecar is optional. Spring Boot works without it.

If you need it running:
1. Make sure Spring Boot is up on `:8080` first
2. Then run:
   ```cmd
   cd rust-service
   cargo run
   ```

If `cargo` is not installed: https://rustup.rs

Check health: http://localhost:8081/health

---

## Rust build fails (`cargo build`)

```
error[E0433]: failed to resolve
```

Run `cargo update` inside `rust-service/` to refresh dependencies, then rebuild.

---

## Docker: app can't connect to postgres

```
Connection refused: postgres/5432
```

The `app` service depends on `postgres`, but PostgreSQL may still be initialising.
Wait a few seconds and restart just the app container:

```cmd
docker-compose restart app
```

Or add a health-check wait in `docker-compose.yml` (planned Phase 12).

---

## Docker: old data persisting after config change

Wipe the named volume and rebuild:

```cmd
docker-compose down -v
docker-compose up --build
```

---

## Flyway migration checksum mismatch

```
Migration checksum mismatch for migration version X
```

Never edit already-applied migration files. Create a new `V(N+1)__...sql` instead.

If you must fix it in dev (H2, data is throwaway):
```cmd
mvnw.cmd flyway:repair
```

---

## Tests failing — context won't load

```
Failed to load ApplicationContext
```

Check `src/test/resources/application.properties` — it should point to H2, not PostgreSQL.

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
```

---

## Image upload returns 400

- Accepted types: `jpg`, `png`, `webp`
- Max size: 2MB
- Must be multipart form data
- Requires `ADMIN` role
