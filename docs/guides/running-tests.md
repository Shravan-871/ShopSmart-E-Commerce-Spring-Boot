# Running Tests

## Run All Tests

```cmd
mvnw.cmd test
```

61 tests — all passing.

---

## Run with Coverage (JaCoCo)

```cmd
mvnw.cmd verify
```

Coverage report → `target/site/jacoco/index.html`

---

## Test Suites

| Suite | File | Count | Covers |
|-------|------|-------|--------|
| `ProductApiTests` | `ProductApiTests.java` | 48 | Products, security, cart, orders |
| `CommerceApiTests` | `CommerceApiTests.java` | 7 | Coupons, wishlist, image upload |
| `EmailServiceTest` | `EmailServiceTest.java` | 3 | HTML email / mail-disabled paths |
| `UserServiceTest` | `UserServiceTest.java` | 2 | Registration unit tests (Mockito) |
| `ShopsmartApplicationTests` | `ShopsmartApplicationTests.java` | 1 | Spring context loads |

---

## ProductApiTests (48)

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
| 25–48 | Security, cart, order tests | Role enforcement, cart CRUD, checkout, order status |

---

## UserServiceTest (2)

| # | Test | Covers |
|---|------|--------|
| 1 | `registerNewUser` | Happy path registration |
| 2 | `registerDuplicateUsername` | Duplicate username throws exception |

---

## CommerceApiTests (7)

| Test | Covers |
|------|--------|
| `validateCouponPercent` | SAVE10-style percent discount preview |
| `validateCouponInvalid` | Unknown code → 400 |
| `checkoutWithCoupon` | Checkout with `couponCode` applies flat discount |
| `wishlistAddListRemove` | Add → list → remove |
| `wishlistDuplicateAdd` | Duplicate add → 400 |
| `uploadProductImage` | ADMIN multipart jpg upload sets `imageUrl` |
| `uploadProductImageRejectsBadType` | Non-image content type → 400 |

## EmailServiceTest (3)

| Test | Covers |
|------|--------|
| `mailDisabledDoesNotSend` | No `JavaMailSender` interaction when mail off |
| `mailEnabledSendsHtmlOrderConfirmation` | Thymeleaf HTML + MimeMessage send |
| `lowStockThresholdIsTen` | Threshold constant used by checkout |

---

## CI

Tests run automatically on every push/PR to `main` or `master` via GitHub Actions.  
See `.github/workflows/ci.yml`.
