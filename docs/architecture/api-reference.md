# API Reference

All REST endpoints are available under both `/` and `/api/v1/` prefixes (e.g. `/products` = `/api/v1/products`).

Interactive docs (Swagger UI): http://localhost:8080/swagger-ui.html

---

## Products

| Method | Endpoint | Auth | Status Codes | Notes |
|--------|----------|------|--------------|-------|
| GET | `/products` | USER+ | 200 | Paginated — `?page=0&size=10` |
| GET | `/products/{id}` | USER+ | 200, 404 | |
| POST | `/products` | ADMIN | 201, 400 | Validated |
| PUT | `/products/{id}` | ADMIN | 200, 400, 404 | Validated |
| DELETE | `/products/{id}` | ADMIN | 204, 404 | |
| POST | `/products/{id}/image` | ADMIN | 200, 400 | multipart, jpg/png/webp, max 2MB |
| GET | `/products/search?name=` | USER+ | 200 | Case-insensitive |
| GET | `/products/category/{category}` | USER+ | 200 | |
| GET | `/products/stats` | USER+ | 200 | Count, avg price, total stock |
| GET | `/products/low-stock?threshold=10` | USER+ | 200 | |
| DELETE | `/products/bulk` | ADMIN | 200 | Body: `[1, 2, 3]` |
| GET | `/products/random?count=10` | ADMIN | 200 | Generates and saves random products |

### Product Schema

```json
{
  "id": 1,
  "name": "Laptop",
  "price": 75000.0,
  "category": "Electronics",
  "stock": 25,
  "description": "Gaming laptop",
  "imageUrl": "/uploads/laptop.jpg"
}
```

### Validation Rules

| Field | Rule |
|-------|------|
| `name` | Required, alphabets and spaces only (`^[A-Za-z ]+$`) |
| `price` | Must be > 0 |
| `category` | Required, not blank |
| `stock` | Must be >= 0 |

### Categories
`Electronics` · `Clothing` · `Food` · `Accessories` · `Computers` · `Audio` · `Sports` · `Books`

---

## Cart

| Method | Endpoint | Auth | Notes |
|--------|----------|------|-------|
| GET | `/cart` | USER+ | Returns current user's cart |
| POST | `/cart/add?productId=&quantity=` | USER+ | Creates cart if not exists |
| PUT | `/cart/update/{itemId}?quantity=` | USER+ | |
| DELETE | `/cart/remove/{itemId}` | USER+ | |

---

## Orders

| Method | Endpoint | Auth | Notes |
|--------|----------|------|-------|
| POST | `/orders/checkout?couponCode=` | USER+ | `couponCode` optional |
| GET | `/orders` | USER+ | Current user's orders |
| GET | `/orders/{id}` | USER+ | Order detail |
| PUT | `/orders/{id}/status?status=` | ADMIN | Values: `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED` |

---

## Coupons

| Method | Endpoint | Auth | Notes |
|--------|----------|------|-------|
| POST | `/coupons/validate?code=&orderTotal=` | USER+ | Returns discounted total |

Seeded coupons: `SAVE10` (10% off), `FLAT500` (₹500 flat off)

---

## Wishlist

| Method | Endpoint | Auth | Notes |
|--------|----------|------|-------|
| GET | `/wishlist` | USER+ | Current user's wishlist |
| POST | `/wishlist/add/{productId}` | USER+ | Idempotent |
| DELETE | `/wishlist/remove/{productId}` | USER+ | |

---

## Auth

| Method | Endpoint | Auth | Notes |
|--------|----------|------|-------|
| POST | `/register` | public | Creates USER role account |
| GET/POST | `/login` | public | Spring form login |
| GET | `/logout` | USER+ | |

---

## Admin

| Method | Endpoint | Auth |
|--------|----------|------|
| GET | `/admin/dashboard` | ADMIN |

---

## UI Pages

| Path | Description |
|------|-------------|
| `/` | Product catalog |
| `/cart-page` | Cart |
| `/my-orders` | Order history |
| `/wishlist-page` | Wishlist |
| `/admin/dashboard` | Admin charts (Chart.js) |
| `/swagger-ui.html` | API docs |
| `/h2-console` | H2 DB console (dev only) |

---

## Rust Sidecar (`:8081`)

| Method | Endpoint | Notes |
|--------|----------|-------|
| GET | `/` | Landing page |
| GET | `/health` | Health check |
| GET | `/api/v1/analytics/summary` | Proxies Spring stats |
| GET | `/api/v1/search?q=` | Proxies Spring search |
| GET | `/api/v1/products/low-stock?threshold=` | Proxies Spring low-stock |

---

## Error Response Format

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "name: must match '^[A-Za-z ]+$'",
  "path": "/products"
}
```
