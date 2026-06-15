# Database

## Engine

| Profile | Engine | JDBC URL |
|---------|--------|----------|
| `dev` | H2 in-memory | `jdbc:h2:mem:testdb` |
| `prod` | PostgreSQL 16 | `jdbc:postgresql://localhost:5432/shopsmart` |

Flyway manages all schema changes. Migrations run automatically on startup.

---

## Flyway Migrations

| Version | File | What it does |
|---------|------|--------------|
| V1 | `V1__init_schema.sql` | Creates `product`, `app_user` tables |
| V2 | `V2__seed_users.sql` | Placeholder — runtime seeding handled by `DataInitializer` |
| V3 | `V3__cart_order_schema.sql` | Creates `cart`, `cart_item`, `order`, `order_item` tables |
| V4 | `V4__order_lifecycle.sql` | Adds `confirmed_at`, `shipped_at`, `stock_restored` to orders |
| V5 | `V5__product_image.sql` | Adds `image_url` column to `product` |
| V6 | `V6__coupons_wishlist.sql` | Creates `coupon`, `wishlist_item` tables + seeds `SAVE10`, `FLAT500` |

---

## Models

### Product
| Field | Type | Constraints |
|-------|------|-------------|
| `id` | Long | PK, auto |
| `name` | String | required, `^[A-Za-z ]+$` |
| `price` | Double | > 0 |
| `category` | String | required |
| `stock` | Integer | >= 0 |
| `description` | String | optional |
| `imageUrl` | String | optional |

### User (app_user)
| Field | Type | Notes |
|-------|------|-------|
| `id` | Long | PK |
| `username` | String | unique |
| `password` | String | BCrypt encoded |
| `role` | String | `ADMIN` or `USER` |

### Cart / CartItem
- One `Cart` per user (lazy-created)
- `CartItem` links cart → product with quantity

### Order / OrderItem
- Created from cart on checkout
- Tracks `status` (`PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`)
- `OrderItem` snapshots product price at purchase time

### Coupon
| Field | Notes |
|-------|-------|
| `code` | unique |
| `discountType` | `PERCENTAGE` or `FLAT` |
| `discountValue` | amount or percent |

Seeded: `SAVE10` (10% off), `FLAT500` (₹500 off)

### WishlistItem
- Links user → product, unique per user-product pair

---

## H2 Console (dev)

URL: http://localhost:8080/h2-console

```
JDBC URL:  jdbc:h2:mem:testdb
Username:  sa
Password:  (empty)
```
