# 🛒 ShopSmart E-Commerce (Spring Boot)

A backend-focused e-commerce application built with Spring Boot 3, Spring Security, Spring Data JPA, Thymeleaf, and an H2 in-memory database. Includes a fully styled responsive frontend, complete REST API, and 25 passing integration tests.

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

App runs at → http://localhost:8080
H2 Console  → http://localhost:8080/h2-console

**H2 Connection:**
```
JDBC URL:  jdbc:h2:mem:testdb
Username:  sa
Password:  (empty)
```

---

## 📂 Project Structure

```
src/
├── main/
│   ├── java/com/shopsmart/
│   │   ├── ShopsmartApplication.java
│   │   ├── config/
│   │   │   └── SecurityConfig.java          ← CSRF off, all requests permitted
│   │   ├── controller/
│   │   │   ├── ProductController.java        ← REST API
│   │   │   └── UiController.java             ← Thymeleaf UI routes
│   │   ├── exception/
│   │   │   └── GlobalExceptionHandler.java   ← 400 / 500 error maps
│   │   ├── model/
│   │   │   ├── Product.java                  ← JPA entity + validation
│   │   │   └── ProductStats.java             ← Stats DTO
│   │   └── repository/
│   │       └── ProductRepository.java        ← JPA + custom queries
│   └── resources/
│       ├── templates/index.html              ← Thymeleaf UI
│       ├── static/style.css                  ← Custom CSS
│       └── application.properties
└── test/
    └── java/com/shopsmart/shopsmart/
        ├── ProductApiTests.java              ← 24 integration tests
        └── ShopsmartApplicationTests.java    ← context load test
```

---

## 🚀 Features

### Bug Fixes
- ✅ `pom.xml` — fixed 4 broken non-existent artifact IDs, downgraded to stable Spring Boot 3.3.0
- ✅ `GET /products/{id}` — was returning `null` with 200, now returns proper `404`
- ✅ `PUT /products/{id}` — was returning `null` with 200 for missing IDs, now `404`
- ✅ `PUT /products/{id}` — had no validation, now `@Valid` enforced
- ✅ `DELETE /products/{id}` — was silently succeeding for missing IDs, now `404`

### New Features
- 🔍 **Search** — case-insensitive product name search
- 🏷️ **Category filter** — filter products by category
- 📄 **Pagination** — paginated product listing
- 📊 **Stats** — total products, average price, total stock
- ⚠️ **Low stock alerts** — products below a stock threshold
- 🗑️ **Bulk delete** — delete multiple products in one request
- 🎲 **Random generator** — seed 10 random products instantly
- 🏪 **Expanded model** — added `category`, `stock`, `description` fields

### Frontend & UI
- 🎨 Modern card-based responsive CSS (no frameworks)
- 📦 Live stats bar — total products, avg price, total stock, low stock count
- 🔍 Search bar + category dropdown filter
- 🃏 Product cards with color-coded stock badges
- ✏️ Edit modal — JS `fetch` PUT call, no page reload
- 🗑️ Delete per card with confirmation dialog
- 📄 Pagination controls
- 🎲 "Generate Random" button in navbar
- 🔔 Toast notifications for all actions

---

## 🧱 Tech Stack

| Layer | Technology |
|-------|------------|
| Language | Java 17 |
| Framework | Spring Boot 3.3.0 |
| Web | Spring Web MVC |
| Persistence | Spring Data JPA + Hibernate 6 |
| Security | Spring Security |
| Database | H2 (in-memory) |
| Templating | Thymeleaf |
| Validation | Jakarta Bean Validation |
| Frontend | HTML + CSS (custom) + Vanilla JS |
| Testing | JUnit 5 + MockMvc |
| Build | Maven |

---

## 📌 API Reference

### Endpoints

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| GET | `/products` | Get all products (paginated) | 200 |
| GET | `/products/{id}` | Get product by ID | 200, 404 |
| POST | `/products` | Create product | 201, 400 |
| PUT | `/products/{id}` | Update product | 200, 400, 404 |
| DELETE | `/products/{id}` | Delete product | 204, 404 |
| GET | `/products/search?name=` | Search by name (case-insensitive) | 200 |
| GET | `/products/category/{category}` | Filter by category | 200 |
| GET | `/products/stats` | Aggregate stats | 200 |
| GET | `/products/low-stock?threshold=10` | Products below stock threshold | 200 |
| DELETE | `/products/bulk` | Bulk delete by ID list | 200 |
| GET | `/products/random` | Generate & save 10 random products | 200 |

### Query Parameters

| Endpoint | Param | Default | Description |
|----------|-------|---------|-------------|
| `GET /products` | `page` | `0` | Page number (0-indexed) |
| `GET /products` | `size` | `10` | Items per page |
| `GET /products/low-stock` | `threshold` | `10` | Max stock to flag as low |

### Product Schema

```json
{
  "id": 1,
  "name": "Laptop",
  "price": 75000.0,
  "category": "Electronics",
  "stock": 25,
  "description": "Gaming laptop"
}
```

### Validation Rules

| Field | Rule |
|-------|------|
| `name` | Required, alphabets and spaces only (`^[A-Za-z ]+$`) |
| `price` | Must be > 0 |
| `category` | Required, not blank |
| `stock` | Must be >= 0 |

Enforced on both **POST** and **PUT**.

### Categories

`Electronics` · `Clothing` · `Food` · `Accessories` · `Computers` · `Audio` · `Sports` · `Books`

---

## 🧪 Tests

**25 tests — all passing** (`mvnw.cmd test`)

| # | Test | Covers |
|---|------|--------|
| 1 | `getAllEmpty` | Paginated empty response |
| 2 | `createValid` | POST → 201 + correct fields |
| 3 | `createInvalidName` | Name with digits → 400 |
| 4 | `createBlankName` | Blank name → 400 |
| 5 | `createNegativePrice` | Negative price → 400 |
| 6 | `createMissingCategory` | Empty category → 400 |
| 7 | `getByIdFound` | GET by ID → 200 |
| 8 | `getByIdNotFound` | GET missing ID → 404 |
| 9 | `updateValid` | PUT → 200 + updated fields |
| 10 | `updateInvalidName` | PUT bad name → 400 |
| 11 | `updateNotFound` | PUT missing ID → 404 |
| 12 | `deleteSuccess` | DELETE → 204, then GET → 404 |
| 13 | `deleteNotFound` | DELETE missing ID → 404 |
| 14 | `searchByName` | Search returns matching results |
| 15 | `searchNoResults` | Search returns empty array |
| 16 | `filterByCategory` | Category filter returns correct items |
| 17 | `getStats` | Correct count / avg price / total stock |
| 18 | `getStatsEmpty` | Empty DB → all zeros |
| 19 | `getLowStock` | Threshold filter returns correct items |
| 20 | `getLowStockDefaultThreshold` | Default threshold = 10 |
| 21 | `bulkDelete` | Bulk delete 2 of 3, 1 remains |
| 22 | `generateRandom` | Returns exactly 10 products |
| 23 | `pagination` | 15 items → 3 pages of 5 |
| 24 | `paginationLastPage` | Last page has correct count + `last=true` |
| 25 | `contextLoads` | Spring context boots cleanly |

---

## 👨‍💻 Author

Built as a learning project to understand Spring Boot backend development — extended with full REST API, responsive frontend, and comprehensive test coverage.
