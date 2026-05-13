# ShopSmart E-Commerce — Project Status & Roadmap

## Tech Stack

- Java 17, Spring Boot 3.3.0
- Spring Web MVC, Spring Data JPA, Spring Security
- H2 in-memory database, Thymeleaf, Jakarta Validation
- JUnit 5 + MockMvc (49 tests), Maven

## How to Run

```cmd
mvnw.cmd spring-boot:run       # start server at http://localhost:8080
mvnw.cmd test                  # run all 49 tests
mvnw.cmd clean package         # build jar
```

---

## Default Accounts

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | ADMIN - full CRUD access |
| `user` | `user123` | USER - read and search only |

Seeded by `DataInitializer.java` at startup using `BCryptPasswordEncoder`.

---

## DONE - Bug Fixes

| # | Fix | Detail |
|---|-----|--------|
| 1 | `pom.xml` broken artifact IDs | Fixed 4 non-existent Spring Boot starter names, downgraded to stable 3.3.0 |
| 2 | `GET /products/{id}` returned null + 200 | Now returns proper 404 Not Found |
| 3 | `PUT /products/{id}` returned null + 200 for missing ID | Now returns 404 Not Found |
| 4 | `PUT /products/{id}` had no validation | `@Valid` now enforced on PUT same as POST |
| 5 | `DELETE /products/{id}` silently succeeded for missing IDs | Now returns 404 Not Found |
| 6 | Stats endpoint ClassCastException at runtime | Replaced fragile Object[] JPQL aggregate with 3 typed queries |
| 7 | Thymeleaf `th:onclick` blocked by security policy | Moved product data to `data-*` attributes, read via JS `dataset.*` |
| 8 | `currentPage` null pointer on search/filter pages | Always set `currentPage` and `totalPages` in model regardless of mode |
| 9 | UI form bypassed validation | Added server-side pattern and price check before saving |

---

## DONE - New Features

| # | Feature | Endpoint | Detail |
|---|---------|----------|--------|
| 1 | Search by name | `GET /products/search?name=` | Case-insensitive, partial match |
| 2 | Filter by category | `GET /products/category/{category}` | Exact match, case-insensitive |
| 3 | Pagination | `GET /products?page=0&size=20` | Returns Spring Page object with metadata |
| 4 | Aggregate stats | `GET /products/stats` | Returns totalProducts, averagePrice, totalStock |
| 5 | Low stock alerts | `GET /products/low-stock?threshold=10` | Returns products with stock at or below threshold |
| 6 | Bulk delete | `DELETE /products/bulk` | Body: [1, 2, 3] - deletes all by ID list |
| 7 | Delete all | `DELETE /products/all` | Wipes entire product table |
| 8 | Random generator | `GET /products/random?count=N` | Generates N products (1-100), clamped server-side |
| 9 | Expanded product model | - | Added category (required), stock (>=0), description fields |

---

## DONE - Frontend and UI

| # | Feature | Detail |
|---|---------|--------|
| 1 | Full custom CSS | Card grid, navbar, stats bar, badges, modal, toast, pagination - no frameworks |
| 2 | Sticky navbar | Brand + inline search + category filter + Filter/Clear + DB Console + Random + Delete All |
| 3 | All navbar buttons unified | `nav-btn` class with consistent style, green for Random, red for Delete All |
| 4 | Search icon fix | `position: absolute` inside flex wrapper - no longer floats above input |
| 5 | Stats bar | 4 cards - Total Products, Avg Price, Total Stock, Low Stock count - live from DB |
| 6 | Product cards redesigned | Large gradient image area (160px) with category SVG illustration, name + price + description body |
| 7 | Category SVG icons | 8 inline SVGs (Electronics, Computers, Audio, Accessories, Clothing, Food, Sports, Books) |
| 8 | Product detail popup | Click card to open modal with SVG image, all fields, formatted price |
| 9 | Edit modal | JS fetch PUT call, no page reload, pre-filled fields, client-side validation |
| 10 | Delete per card | Confirmation dialog before delete, form POST |
| 11 | Random prompt | Browser prompt asks count (1-100) before calling API |
| 12 | Delete All button | Red button in navbar, confirmation dialog, calls DELETE /products/all |
| 13 | Toast notifications | Bottom-right, success/error color coded, auto-dismiss 3s |
| 14 | Pagination controls | First/Prev/Page numbers/Next/Last, active page highlighted, disabled states |
| 15 | Page size 20 | Default page shows 20 products |
| 16 | Responsive | Mobile-friendly, grid collapses, navbar wraps |

---

## DONE - Auth and Security (Feature 1)

| # | Item | Detail |
|---|------|--------|
| 1 | `User` entity | `app_user` table - id, username, password (BCrypt), role (ADMIN/USER) |
| 2 | `UserRepository` | findByUsername, existsByUsername |
| 3 | `UserDetailsServiceImpl` | Loads user from DB for Spring Security authentication |
| 4 | `UserService` | Registration logic - validates, BCrypt-hashes password, saves |
| 5 | `SecurityConfig` | Role-based rules: ADMIN full CRUD, USER GET only, /login and /register public |
| 6 | Form login | Custom /login page, redirects to / on success, /login?error on failure |
| 7 | Logout | POST /logout redirects to /login?logout=true |
| 8 | Remember Me | 24-hour token, remember-me checkbox on login page |
| 9 | Registration | GET/POST /register - validates password length, match, username uniqueness |
| 10 | UI role-aware | Navbar shows username + role badge; ADMIN buttons hidden from USER |
| 11 | Default accounts | Seeded by DataInitializer.java at startup: admin/admin123 (ADMIN), user/user123 (USER) |

---

## DONE - PostgreSQL and Flyway (Feature 2)

| # | Item | Detail |
|---|------|--------|
| 1 | Profile-based config | `application.properties` activates dev profile by default |
| 2 | `application-dev.properties` | H2 in-memory + Flyway - used locally |
| 3 | `application-prod.properties` | PostgreSQL via env vars (DB_URL, DB_USERNAME, DB_PASSWORD) |
| 4 | Flyway dependency | `flyway-core` added to pom.xml |
| 5 | PostgreSQL driver | `postgresql` driver added to pom.xml (runtime scope) |
| 6 | `V1__init_schema.sql` | Creates product and app_user tables |
| 7 | `V2__seed_users.sql` | Placeholder - accounts seeded at runtime by DataInitializer.java |
| 8 | Schema managed by Flyway | No more create-drop - Flyway owns the schema lifecycle |
| 9 | `DataInitializer.java` | Seeds admin/admin123 and user/user123 at startup using BCryptPasswordEncoder |

To switch to PostgreSQL: set `spring.profiles.active=prod` and provide DB_URL, DB_USERNAME, DB_PASSWORD env vars.

---

## DONE - Tests (49/49 passing)

| # | Test | What it verifies |
|---|------|-----------------|
| 1 | `getAllEmpty` | Paginated empty response on fresh DB |
| 2 | `createValid` | POST returns 201, correct fields returned |
| 3 | `createInvalidName` | Name with digits returns 400 + error message |
| 4 | `createBlankName` | Blank name returns 400 |
| 5 | `createNegativePrice` | Negative price returns 400 |
| 6 | `createMissingCategory` | Empty category returns 400 |
| 7 | `getByIdFound` | GET existing ID returns 200 |
| 8 | `getByIdNotFound` | GET missing ID returns 404 |
| 9 | `updateValid` | PUT returns 200, all fields updated |
| 10 | `updateInvalidName` | PUT bad name returns 400 |
| 11 | `updateNotFound` | PUT missing ID returns 404 |
| 12 | `deleteSuccess` | DELETE returns 204, then GET returns 404 |
| 13 | `deleteNotFound` | DELETE missing ID returns 404 |
| 14 | `searchByName` | Case-insensitive search returns correct count |
| 15 | `searchNoResults` | Search with no match returns empty array |
| 16 | `filterByCategory` | Category filter returns only matching items |
| 17 | `getStats` | Correct count, avg price, total stock |
| 18 | `getStatsEmpty` | Empty DB returns all zeros, no crash |
| 19 | `getLowStock` | Threshold filter returns correct items |
| 20 | `getLowStockDefaultThreshold` | Default threshold = 10 works |
| 21 | `bulkDelete` | Bulk delete 2 of 3, 1 remains |
| 22 | `generateRandom` | Returns exactly 10 products with all fields |
| 23 | `pagination` | 15 items returns 3 pages of 5 |
| 24 | `paginationLastPage` | Last page has correct count and last=true |
| 25 | `contextLoads` | Spring context boots cleanly |
| 26 | `userCannotCreate` | USER role POST returns 403 Forbidden |
| 27 | `userCannotDelete` | USER role DELETE returns 403 Forbidden |
| 28 | `getCartEmpty` | GET /cart creates empty cart on first access |
| 29 | `addToCart` | POST /cart/add returns cart with item |
| 30 | `addToCartIncrementsQuantity` | Adding same product increments quantity |
| 31 | `addToCartProductNotFound` | POST /cart/add with bad productId returns 404 |
| 32 | `removeFromCart` | DELETE /cart/remove/{itemId} removes item, cart empty |
| 33 | `checkout` | POST /orders/checkout creates order, clears cart |
| 34 | `checkoutEmptyCart` | Checkout with empty cart returns 400 |
| 35 | `getMyOrders` | GET /orders returns user's orders |
| 36 | `getOrderById` | GET /orders/{id} returns correct order |
| 37 | `updateOrderStatus` | PUT /orders/{id}/status ADMIN updates to CONFIRMED |
| 38 | `updateOrderStatusForbidden` | PUT /orders/{id}/status USER returns 403 |
| 39 | `checkoutDeductsStock` | Checkout reduces product stock by ordered quantity |
| 40 | `checkoutDeductsStockMultipleItems` | Multiple items in one order all deduct stock correctly |
| 41 | `addToCartExceedsStock` | Adding qty > stock returns 400 with error |
| 42 | `addToCartCumulativeExceedsStock` | Cumulative cart qty beyond stock returns 400 |
| 43 | `updateCartItemQty` | PUT /cart/update/{itemId} updates quantity correctly |
| 44 | `updateCartItemQtyExceedsStock` | Updating qty beyond stock returns 400 |
| 45 | `cancelOrderRestoresStock` | Cancelling order restores product stock |
| 46 | `cancelAlreadyCancelledOrderBlocked` | Re-cancelling blocked — stock not double-restored |
| 47 | `cannotCancelShippedOrder` | SHIPPED order cancel returns 400 |
| 48 | `invalidTransitionBlocked` | PENDING → DELIVERED returns 400 |
| 49 | `contextLoads` (ShopsmartApplicationTests) | Spring context boots cleanly |

---

## DONE - Shopping Cart and Orders (Feature 3)

| # | Item | Detail |
|---|------|--------|
| 1 | `Cart` entity | One cart per user, EAGER-loaded items |
| 2 | `CartItem` entity | Product + quantity, linked to Cart |
| 3 | `Order` entity | Username, status enum, total, EAGER-loaded items, `confirmedAt`, `shippedAt`, `stockRestored` flag |
| 4 | `OrderItem` entity | Denormalized snapshot — productName, productPrice, productCategory, quantity |
| 5 | Order status flow | PENDING → CONFIRMED (admin) → SHIPPED (auto 1 min) → DELIVERED (auto 1 min) |
| 6 | Strict transition rules | Only valid transitions allowed — invalid returns 400 with error message |
| 7 | Terminal state lock | SHIPPED / DELIVERED / CANCELLED orders cannot be changed — returns 400 |
| 8 | Stock deducted at checkout | Product stock reduced immediately when order is placed |
| 9 | Stock restored on cancel | CANCELLED restores stock — guarded by `stockRestored` flag, idempotent, no double-restore |
| 10 | Duplicate cancel bug fixed | Cancelling an already-cancelled order returns 400, stock not touched again |
| 11 | Cart stock enforcement | Cannot add more than available stock to cart — returns 400 with "Only N in stock" |
| 12 | Cart qty update | `PUT /cart/update/{itemId}` — updates quantity, enforces stock max |
| 13 | Cart qty input max | Cart page shows `max=stock` on qty input, stock count shown per item |
| 14 | `OrderScheduler` | `@Scheduled` every 10s — auto-advances CONFIRMED→SHIPPED after **30s**, SHIPPED→DELIVERED after **60s** |
| 15 | `@EnableScheduling` | Added to `ShopsmartApplication` |
| 16 | `CartRepository` | findByUsername |
| 17 | `OrderRepository` | findByUsernameOrderByCreatedAtDesc, findByStatus |
| 18 | `CartController` | GET /cart, POST /cart/add, PUT /cart/update/{itemId}, DELETE /cart/remove/{itemId}, DELETE /cart/clear |
| 19 | `OrderController` | POST /orders/checkout, GET /orders, GET /orders/{id}, PUT /orders/{id}/status (ADMIN), GET /orders/all (ADMIN) |
| 20 | `V3__cart_order_schema.sql` | Flyway migration — cart, cart_item, orders, order_item tables |
| 21 | `V4__order_lifecycle.sql` | Flyway migration — confirmed_at, shipped_at, stock_restored columns |
| 22 | Cart UI | /cart-page — live JS fetch, qty update with max enforcement, item removal, clear, checkout |
| 23 | Orders list UI | /my-orders — per-user order history, quick Confirm/Cancel buttons (PENDING only), locked for terminal states |
| 24 | Order detail UI | /my-orders/{id} — progress tracker bar, cancelled banner, **live countdown timer** (30s to ship, 60s to deliver), auto-reloads on expiry, locked controls for terminal states |
| 25 | Progress tracker bar | Visual step bar in order detail — steps highlight based on current status, cancelled shows red banner |
| 26 | Navbar links | 🛒 Cart + 📋 My Orders on all pages |
| 27 | Add to Cart button | 🛒 button on every product card, disabled when out of stock |
| 28 | SecurityConfig | Cart/order endpoints authenticated; @EnableMethodSecurity for @PreAuthorize |
| 29 | GlobalExceptionHandler | AuthorizationDeniedException → 403 |
| 30 | Tests 28–48 | 21 new integration tests — cart CRUD, stock enforcement, checkout deduction, cancel restore, duplicate cancel blocked, transition rules, locked states |

---

## NOT DONE - Roadmap (Priority Order)

### 4. Swagger / OpenAPI Docs

**Why:** Professional APIs always have documentation. Easy to add.

- [ ] Add `springdoc-openapi-starter-webmvc-ui` dependency
- [ ] Annotate controllers with `@Operation`, `@ApiResponse`, `@Tag`
- [ ] Accessible at `http://localhost:8080/swagger-ui.html`

### 5. Admin Dashboard with Charts

**Why:** Visual analytics make the app look professional.

- [ ] Dedicated `/admin/dashboard` page
- [ ] Chart.js integration (CDN, no install needed)
- [ ] Line chart - products added over time
- [ ] Donut chart - stock distribution by category
- [ ] Bar chart - top 5 most expensive products
- [ ] Low stock alert table with highlight
- [ ] Files to create: `dashboard.html`, `DashboardController.java`

### 6. Product Image Upload

**Why:** Real products need real images, not just SVG placeholders.

- [ ] Add `imageUrl` field to `Product`
- [ ] `POST /products/{id}/image` - multipart file upload
- [ ] Store images in `src/main/resources/static/uploads/`
- [ ] Serve via `/uploads/{filename}`
- [ ] Show actual image in card and detail modal (fallback to SVG if null)
- [ ] File type validation (jpg/png/webp only), size limit 2MB

### 7. Email Notifications

**Why:** Real apps send emails for orders, low stock, etc.

- [ ] Add `spring-boot-starter-mail` dependency
- [ ] Configure SMTP (Gmail or Mailtrap for dev)
- [ ] Send order confirmation email on checkout
- [ ] Send low stock alert email to admin when stock below threshold
- [ ] HTML email templates with Thymeleaf
- [ ] Files to create: `EmailService.java`, `templates/email/order-confirmation.html`

### 8. Promotions and Wishlist

**Why:** Increases engagement and perceived value.

- [ ] `Coupon` entity - code, discount type (flat or percent), expiry date
- [ ] Apply coupon at checkout
- [ ] `Wishlist` - user saves products for later
- [ ] On Sale flag on product with original price + discounted price
- [ ] `POST /coupons/validate`
- [ ] `POST /wishlist/add/{productId}`
- [ ] `GET /wishlist`
- [ ] `DELETE /wishlist/remove/{productId}`

### 9. API Improvements

**Why:** Makes the API production-grade.

- [ ] Version all endpoints under `/api/v1/`
- [ ] Rate limiting with Bucket4j (100 req/min per IP)
- [ ] Request/response logging filter
- [ ] ETag support for caching
- [ ] CORS configuration for frontend clients
- [ ] Standardized error response body (timestamp, status, message, path)

### 10. UI Enhancements

**Why:** Polish that separates a demo from a real product.

- [ ] Dark mode toggle (CSS variable swap, saved to localStorage)
- [ ] Skeleton loading cards while fetching
- [ ] Infinite scroll (replace pagination)
- [ ] Sort dropdown - by price asc/desc, name, newest
- [ ] Price range filter (min/max inputs)
- [ ] Keyboard shortcuts (/ focuses search, N opens add form)
- [ ] Mobile bottom navigation bar

### 11. Test Coverage Improvements

**Why:** 28 integration tests is good, but missing unit tests and coverage reports.

- [ ] Service layer unit tests with Mockito (mock repository)
- [ ] `@DataJpaTest` repository tests
- [ ] JaCoCo test coverage report (`mvnw.cmd verify`)
- [ ] Test for email sending (mock JavaMailSender)

### 12. DevOps and Production-Ready

**Why:** Needed to actually deploy and run in production.

- [ ] `Dockerfile` - multi-stage build (build jar then run jar)
- [ ] `docker-compose.yml` - app + PostgreSQL + optional Redis
- [ ] Spring Boot Actuator - /actuator/health, /actuator/info, /actuator/metrics
- [ ] Prometheus metrics endpoint + Grafana dashboard
- [ ] GitHub Actions CI - on push: compile, test, build Docker image
- [ ] Files to create: `Dockerfile`, `docker-compose.yml`, `.github/workflows/ci.yml`

---

## Current File Structure

```
src/main/java/com/shopsmart/
  ShopsmartApplication.java     - @EnableScheduling added
  config/
    DataInitializer.java        - Seeds admin/user accounts at startup via BCrypt
    OrderScheduler.java         - Auto-advances CONFIRMED→SHIPPED→DELIVERED every 30s
    SecurityConfig.java         - Role-based auth, form login, BCrypt, @EnableMethodSecurity
  controller/
    AuthController.java         - GET/POST /login, /register
    CartController.java         - REST API for cart (5 endpoints)
    OrderController.java        - REST API for orders (5 endpoints), strict transition rules
    ProductController.java      - REST API (12 endpoints)
    UiController.java           - Thymeleaf UI routes
  exception/
    GlobalExceptionHandler.java - 400/403/500 structured error responses
  model/
    Cart.java                   - JPA entity, one per user, EAGER items
    CartItem.java               - JPA entity, product + quantity
    Order.java                  - JPA entity, status enum, confirmedAt, shippedAt, stockRestored
    OrderItem.java              - JPA entity, denormalized product snapshot
    Product.java                - JPA entity, 6 fields, validated
    ProductStats.java           - Stats DTO
    User.java                   - JPA entity, BCrypt password, role
  repository/
    CartItemRepository.java     - JpaRepository
    CartRepository.java         - findByUsername
    OrderItemRepository.java    - JpaRepository
    OrderRepository.java        - findByUsernameOrderByCreatedAtDesc, findByStatus
    ProductRepository.java      - JPA + 7 custom queries (incl. findByName)
    UserRepository.java         - findByUsername, existsByUsername
  service/
    UserDetailsServiceImpl.java - Spring Security user loader
    UserService.java            - Registration logic

src/main/resources/
  db/migration/
    V1__init_schema.sql         - Creates product and app_user tables
    V2__seed_users.sql          - Placeholder (seeding done by DataInitializer)
    V3__cart_order_schema.sql   - Creates cart, cart_item, orders, order_item tables
    V4__order_lifecycle.sql     - Adds confirmed_at, shipped_at, stock_restored to orders
  templates/
    cart.html                   - Cart page (qty update with max, stock display, checkout)
    index.html                  - Main UI (role-aware, Add to Cart button)
    login.html                  - Login page
    order-detail.html           - Order detail with progress tracker bar, locked controls
    orders.html                 - Order history, quick Confirm/Cancel for PENDING only
    register.html               - Registration page
  static/
    style.css                   - Custom CSS
  application.properties        - Activates dev profile
  application-dev.properties    - H2 + Flyway (local)
  application-prod.properties   - PostgreSQL via env vars

src/test/
  java/com/shopsmart/shopsmart/
    ProductApiTests.java        - 48 MockMvc integration + security + cart/order tests
    ShopsmartApplicationTests.java - context load test
  resources/
    application.properties      - Test config (H2 + Flyway)
```

---

## API Quick Reference

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| GET | `/products` | All products paginated | 200 |
| GET | `/products/{id}` | Get by ID | 200, 404 |
| POST | `/products` | Create (validated, ADMIN only) | 201, 400 |
| PUT | `/products/{id}` | Update (validated, ADMIN only) | 200, 400, 404 |
| DELETE | `/products/{id}` | Delete one (ADMIN only) | 204, 404 |
| GET | `/products/search?name=` | Search by name | 200 |
| GET | `/products/category/{cat}` | Filter by category | 200 |
| GET | `/products/stats` | Aggregate stats | 200 |
| GET | `/products/low-stock?threshold=10` | Low stock list | 200 |
| DELETE | `/products/bulk` | Bulk delete by IDs (ADMIN only) | 200 |
| DELETE | `/products/all` | Delete everything (ADMIN only) | 200 |
| GET | `/products/random?count=10` | Generate random (ADMIN only) | 200 |
| GET | `/login` | Login page | 200 |
| POST | `/login` | Authenticate | 302 |
| GET | `/register` | Register page | 200 |
| POST | `/register` | Create USER account | 302 |
| POST | `/logout` | Logout | 302 |
| GET | `/cart` | View cart (creates if missing) | 200 |
| POST | `/cart/add?productId=&quantity=` | Add item to cart | 200, 404 |
| PUT | `/cart/update/{itemId}?quantity=` | Update cart item quantity | 200, 400, 404 |
| DELETE | `/cart/remove/{itemId}` | Remove item from cart | 200, 404 |
| DELETE | `/cart/clear` | Clear entire cart | 200 |
| POST | `/orders/checkout` | Place order from cart | 200, 400 |
| GET | `/orders` | List my orders | 200 |
| GET | `/orders/{id}` | Order detail | 200, 404 |
| PUT | `/orders/{id}/status?status=` | Update order status (ADMIN) | 200, 404 |
| GET | `/orders/all` | All orders (ADMIN) | 200 |

## Validation Rules

| Field | Rule |
|-------|------|
| `name` | Required, alphabets and spaces only |
| `price` | Must be greater than 0 |
| `category` | Required, not blank |
| `stock` | Must be 0 or greater |

## Categories

Electronics, Clothing, Food, Accessories, Computers, Audio, Sports, Books
