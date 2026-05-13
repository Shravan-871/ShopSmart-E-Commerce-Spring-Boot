package com.shopsmart.shopsmart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsmart.model.Product;
import com.shopsmart.repository.CartRepository;
import com.shopsmart.repository.OrderRepository;
import com.shopsmart.repository.ProductRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ProductApiTests {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired ProductRepository repo;
    @Autowired CartRepository cartRepo;
    @Autowired OrderRepository orderRepo;

    @BeforeEach
    void clean() {
        orderRepo.deleteAll();
        cartRepo.deleteAll();
        repo.deleteAll();
    }

    private Product seed(String name, double price, String cat, int stock, String desc) {
        return repo.save(new Product(name, price, cat, stock, desc));
    }

    private String json(Object o) throws Exception {
        return mapper.writeValueAsString(o);
    }

    // ── 1. GET /products — empty ─────────────────────────────
    @Test @Order(1) @WithMockUser
    void getAllEmpty() throws Exception {
        mvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.content").isArray());
    }

    // ── 2. POST /products — valid ────────────────────────────
    @Test @Order(2) @WithMockUser(roles = "ADMIN")
    void createValid() throws Exception {
        Product p = new Product("Laptop", 75000, "Electronics", 25, "Gaming laptop");
        mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(json(p)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.price").value(75000.0))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.stock").value(25));
    }

    // ── 3. POST — invalid name → 400 ─────────────────────────
    @Test @Order(3) @WithMockUser(roles = "ADMIN")
    void createInvalidName() throws Exception {
        Product p = new Product("Laptop123", 50000, "Electronics", 10, "bad");
        mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(json(p)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must contain only alphabets"));
    }

    // ── 4. POST — blank name → 400 ───────────────────────────
    @Test @Order(4) @WithMockUser(roles = "ADMIN")
    void createBlankName() throws Exception {
        Product p = new Product("", 50000, "Electronics", 10, "blank");
        mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(json(p)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    // ── 5. POST — negative price → 400 ───────────────────────
    @Test @Order(5) @WithMockUser(roles = "ADMIN")
    void createNegativePrice() throws Exception {
        Product p = new Product("Watch", -100, "Accessories", 10, "bad");
        mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(json(p)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.price").value("Price must be greater than 0"));
    }

    // ── 6. POST — missing category → 400 ─────────────────────
    @Test @Order(6) @WithMockUser(roles = "ADMIN")
    void createMissingCategory() throws Exception {
        Product p = new Product("Watch", 5000, "", 10, "no cat");
        mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(json(p)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.category").exists());
    }

    // ── 7. GET /products/{id} — found ────────────────────────
    @Test @Order(7) @WithMockUser
    void getByIdFound() throws Exception {
        Product saved = seed("Phone", 45000, "Electronics", 15, "Flagship");
        mvc.perform(get("/products/" + saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Phone"))
                .andExpect(jsonPath("$.category").value("Electronics"));
    }

    // ── 8. GET /products/{id} — not found → 404 ──────────────
    @Test @Order(8) @WithMockUser
    void getByIdNotFound() throws Exception {
        mvc.perform(get("/products/99999")).andExpect(status().isNotFound());
    }

    // ── 9. PUT — valid update ─────────────────────────────────
    @Test @Order(9) @WithMockUser(roles = "ADMIN")
    void updateValid() throws Exception {
        Product saved = seed("Tablet", 30000, "Electronics", 10, "Old");
        Product updated = new Product("Gaming Tablet", 35000, "Computers", 8, "New");
        mvc.perform(put("/products/" + saved.getId()).contentType(MediaType.APPLICATION_JSON).content(json(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Gaming Tablet"))
                .andExpect(jsonPath("$.price").value(35000.0))
                .andExpect(jsonPath("$.category").value("Computers"))
                .andExpect(jsonPath("$.stock").value(8));
    }

    // ── 10. PUT — invalid name → 400 ─────────────────────────
    @Test @Order(10) @WithMockUser(roles = "ADMIN")
    void updateInvalidName() throws Exception {
        Product saved = seed("Tablet", 30000, "Electronics", 10, "desc");
        Product bad = new Product("Tablet99", 35000, "Electronics", 5, "bad");
        mvc.perform(put("/products/" + saved.getId()).contentType(MediaType.APPLICATION_JSON).content(json(bad)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Name must contain only alphabets"));
    }

    // ── 11. PUT — not found → 404 ────────────────────────────
    @Test @Order(11) @WithMockUser(roles = "ADMIN")
    void updateNotFound() throws Exception {
        Product p = new Product("Ghost", 100, "Electronics", 1, "ghost");
        mvc.perform(put("/products/99999").contentType(MediaType.APPLICATION_JSON).content(json(p)))
                .andExpect(status().isNotFound());
    }

    // ── 12. DELETE — success → 204 ───────────────────────────
    @Test @Order(12) @WithMockUser(roles = "ADMIN")
    void deleteSuccess() throws Exception {
        Product saved = seed("Camera", 20000, "Electronics", 5, "DSLR");
        mvc.perform(delete("/products/" + saved.getId())).andExpect(status().isNoContent());
        mvc.perform(get("/products/" + saved.getId())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("u").roles("USER")))
                .andExpect(status().isNotFound());
    }

    // ── 13. DELETE — not found → 404 ─────────────────────────
    @Test @Order(13) @WithMockUser(roles = "ADMIN")
    void deleteNotFound() throws Exception {
        mvc.perform(delete("/products/99999")).andExpect(status().isNotFound());
    }

    // ── 14. Search by name ────────────────────────────────────
    @Test @Order(14) @WithMockUser
    void searchByName() throws Exception {
        seed("Laptop", 75000, "Electronics", 20, "desc");
        seed("Laptop Stand", 1500, "Accessories", 50, "desc");
        seed("Phone", 45000, "Electronics", 10, "desc");
        mvc.perform(get("/products/search?name=laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name", containsStringIgnoringCase("Laptop")));
    }

    // ── 15. Search — no results ───────────────────────────────
    @Test @Order(15) @WithMockUser
    void searchNoResults() throws Exception {
        seed("Laptop", 75000, "Electronics", 20, "desc");
        mvc.perform(get("/products/search?name=xyz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── 16. Filter by category ────────────────────────────────
    @Test @Order(16) @WithMockUser
    void filterByCategory() throws Exception {
        seed("Laptop", 75000, "Electronics", 20, "desc");
        seed("Phone", 45000, "Electronics", 10, "desc");
        seed("Shirt", 999, "Clothing", 0, "desc");
        mvc.perform(get("/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].category", everyItem(equalToIgnoringCase("Electronics"))));
    }

    // ── 17. Stats ─────────────────────────────────────────────
    @Test @Order(17) @WithMockUser
    void getStats() throws Exception {
        seed("Laptop", 75000, "Electronics", 20, "desc");
        seed("Phone", 45000, "Electronics", 10, "desc");
        mvc.perform(get("/products/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(2))
                .andExpect(jsonPath("$.averagePrice").value(60000.0))
                .andExpect(jsonPath("$.totalStock").value(30));
    }

    // ── 18. Stats — empty DB ──────────────────────────────────
    @Test @Order(18) @WithMockUser
    void getStatsEmpty() throws Exception {
        mvc.perform(get("/products/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProducts").value(0))
                .andExpect(jsonPath("$.averagePrice").value(0.0))
                .andExpect(jsonPath("$.totalStock").value(0));
    }

    // ── 19. Low stock ─────────────────────────────────────────
    @Test @Order(19) @WithMockUser
    void getLowStock() throws Exception {
        seed("Laptop", 75000, "Electronics", 50, "desc");
        seed("Phone", 45000, "Electronics", 5, "desc");
        seed("Shirt", 999, "Clothing", 0, "desc");
        mvc.perform(get("/products/low-stock?threshold=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].stock", everyItem(lessThanOrEqualTo(10))));
    }

    // ── 20. Low stock — default threshold ────────────────────
    @Test @Order(20) @WithMockUser
    void getLowStockDefaultThreshold() throws Exception {
        seed("Laptop", 75000, "Electronics", 3, "desc");
        seed("Phone", 45000, "Electronics", 100, "desc");
        mvc.perform(get("/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Laptop"));
    }

    // ── 21. Bulk delete ───────────────────────────────────────
    @Test @Order(21) @WithMockUser(roles = "ADMIN")
    void bulkDelete() throws Exception {
        Product p1 = seed("Laptop", 75000, "Electronics", 20, "desc");
        Product p2 = seed("Phone", 45000, "Electronics", 10, "desc");
        seed("Shirt", 999, "Clothing", 5, "desc");
        List<Long> ids = List.of(p1.getId(), p2.getId());
        mvc.perform(delete("/products/bulk").contentType(MediaType.APPLICATION_JSON).content(json(ids)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("2 products deleted"));
        mvc.perform(get("/products")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("u").roles("USER")))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Shirt"));
    }

    // ── 22. Random ────────────────────────────────────────────
    @Test @Order(22) @WithMockUser(roles = "ADMIN")
    void generateRandom() throws Exception {
        mvc.perform(get("/products/random"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(10)))
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].name").exists())
                .andExpect(jsonPath("$[0].category").exists());
    }

    // ── 23. Pagination ────────────────────────────────────────
    private static final String[] PAGE_NAMES = {
        "Laptop","Phone","Tablet","Watch","Headphones",
        "Keyboard","Mouse","Monitor","Camera","Speaker",
        "Charger","Cable","Router","Printer","Scanner"
    };

    @Test @Order(23) @WithMockUser
    void pagination() throws Exception {
        for (int i = 0; i < 15; i++) seed(PAGE_NAMES[i], 1000*(i+1), "Electronics", i+1, "desc");
        mvc.perform(get("/products?page=0&size=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements").value(15))
                .andExpect(jsonPath("$.totalPages").value(3));
    }

    // ── 24. Pagination last page ──────────────────────────────
    private static final String[] BOOK_NAMES = {
        "Atlas","Bible","Comic","Drama","Essay",
        "Fable","Guide","Haiku","Index","Journal","Kindle"
    };

    @Test @Order(24) @WithMockUser
    void paginationLastPage() throws Exception {
        for (int i = 0; i < 11; i++) seed(BOOK_NAMES[i], 500*(i+1), "Books", (i+1)*2, "desc");
        mvc.perform(get("/products?page=1&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.last").value(true));
    }

    // ── 25. USER cannot POST (403) ────────────────────────────
    @Test @Order(25) @WithMockUser(roles = "USER")
    void userCannotCreate() throws Exception {
        Product p = new Product("Laptop", 75000, "Electronics", 10, "desc");
        mvc.perform(post("/products").contentType(MediaType.APPLICATION_JSON).content(json(p)))
                .andExpect(status().isForbidden());
    }

    // ── 26. USER cannot DELETE (403) ─────────────────────────
    @Test @Order(26) @WithMockUser(roles = "USER")
    void userCannotDelete() throws Exception {
        Product saved = seed("Phone", 45000, "Electronics", 5, "desc");
        mvc.perform(delete("/products/" + saved.getId()))
                .andExpect(status().isForbidden());
    }

    // ── 27. Unauthenticated → 302 redirect to login ──────────
    @Test @Order(27)
    void unauthenticatedRedirectsToLogin() throws Exception {
        mvc.perform(get("/products")).andExpect(status().is3xxRedirection());
    }

    // ── 28. GET /cart — empty cart created on first access ───
    @Test @Order(28) @WithMockUser(username = "testuser")
    void getCartEmpty() throws Exception {
        mvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    // ── 29. POST /cart/add — adds item to cart ────────────────
    @Test @Order(29) @WithMockUser(username = "testuser")
    void addToCart() throws Exception {
        Product p = seed("Laptop", 75000, "Electronics", 10, "desc");
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.items[0].product.name").value("Laptop"));
    }

    // ── 30. POST /cart/add — adds to existing quantity ────────
    @Test @Order(30) @WithMockUser(username = "testuser")
    void addToCartIncrementsQuantity() throws Exception {
        Product p = seed("Phone", 45000, "Electronics", 20, "desc");
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=1"));
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].quantity").value(4));
    }

    // ── 31. POST /cart/add — product not found → 404 ─────────
    @Test @Order(31) @WithMockUser(username = "testuser")
    void addToCartProductNotFound() throws Exception {
        mvc.perform(post("/cart/add?productId=99999"))
                .andExpect(status().isNotFound());
    }

    // ── 32. DELETE /cart/remove/{itemId} — removes item ──────
    @Test @Order(32) @WithMockUser(username = "testuser")
    void removeFromCart() throws Exception {
        Product p = seed("Tablet", 30000, "Electronics", 5, "desc");
        String cartJson = mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=1"))
                .andReturn().getResponse().getContentAsString();
        Long itemId = mapper.readTree(cartJson).get("items").get(0).get("id").asLong();
        mvc.perform(delete("/cart/remove/" + itemId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Item removed"));
        mvc.perform(get("/cart"))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    // ── 33. POST /orders/checkout — places order, clears cart ─
    @Test @Order(33) @WithMockUser(username = "testuser")
    void checkout() throws Exception {
        Product p = seed("Camera", 20000, "Electronics", 10, "DSLR");
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=2"));
        mvc.perform(post("/orders/checkout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.total").value(40000.0))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productName").value("Camera"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));
        mvc.perform(get("/cart"))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    // ── 34. POST /orders/checkout — empty cart → 400 ─────────
    @Test @Order(34) @WithMockUser(username = "testuser")
    void checkoutEmptyCart() throws Exception {
        mvc.perform(post("/orders/checkout"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cart is empty"));
    }

    // ── 35. GET /orders — lists user orders ───────────────────
    @Test @Order(35) @WithMockUser(username = "testuser")
    void getMyOrders() throws Exception {
        Product p = seed("Speaker", 5000, "Audio", 15, "desc");
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=1"));
        mvc.perform(post("/orders/checkout"));
        mvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    // ── 36. GET /orders/{id} — order detail ───────────────────
    @Test @Order(36) @WithMockUser(username = "testuser")
    void getOrderById() throws Exception {
        Product p = seed("Watch", 8000, "Accessories", 5, "desc");
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=1"));
        String orderJson = mvc.perform(post("/orders/checkout"))
                .andReturn().getResponse().getContentAsString();
        Long orderId = mapper.readTree(orderJson).get("id").asLong();
        mvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.items", hasSize(1)));
    }

    // ── 37. PUT /orders/{id}/status — admin updates status ────
    @Test @Order(37) @WithMockUser(username = "testuser")
    void updateOrderStatus() throws Exception {
        Product p = seed("Keyboard", 3000, "Computers", 20, "desc");
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=1"));
        String orderJson = mvc.perform(post("/orders/checkout"))
                .andReturn().getResponse().getContentAsString();
        Long orderId = mapper.readTree(orderJson).get("id").asLong();
        mvc.perform(put("/orders/" + orderId + "/status?status=CONFIRMED")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    // ── 38. PUT /orders/{id}/status — non-admin → 403 ─────────
    @Test @Order(38) @WithMockUser(username = "testuser", roles = "USER")
    void updateOrderStatusForbidden() throws Exception {
        mvc.perform(put("/orders/1/status?status=SHIPPED"))
                .andExpect(status().isForbidden());
    }

    // ── 39. Checkout deducts product stock ────────────────────
    @Test @Order(39) @WithMockUser(username = "testuser")
    void checkoutDeductsStock() throws Exception {
        Product p = seed("Monitor", 15000, "Electronics", 10, "desc");
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=3"));
        mvc.perform(post("/orders/checkout")).andExpect(status().isOk());
        // stock should now be 10 - 3 = 7
        mvc.perform(get("/products/" + p.getId())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("testuser").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stock").value(7));
    }

    // ── 40. Checkout deducts stock for multiple items ─────────
    @Test @Order(40) @WithMockUser(username = "testuser")
    void checkoutDeductsStockMultipleItems() throws Exception {
        Product p1 = seed("Router", 3000, "Electronics", 8, "desc");
        Product p2 = seed("Printer", 7000, "Electronics", 5, "desc");
        mvc.perform(post("/cart/add?productId=" + p1.getId() + "&quantity=2"));
        mvc.perform(post("/cart/add?productId=" + p2.getId() + "&quantity=4"));
        mvc.perform(post("/orders/checkout")).andExpect(status().isOk());
        // p1: 8 - 2 = 6, p2: 5 - 4 = 1
        mvc.perform(get("/products/" + p1.getId())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("u").roles("USER")))
                .andExpect(jsonPath("$.stock").value(6));
        mvc.perform(get("/products/" + p2.getId())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("u").roles("USER")))
                .andExpect(jsonPath("$.stock").value(1));
    }

    // ── 41. Add to cart blocked when qty exceeds stock ────────
    @Test @Order(41) @WithMockUser(username = "testuser")
    void addToCartExceedsStock() throws Exception {
        Product p = seed("Cable", 500, "Electronics", 3, "desc");
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=5"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only 3 in stock"));
    }

    // ── 42. Add to cart: cumulative qty blocked at stock limit ─
    @Test @Order(42) @WithMockUser(username = "testuser")
    void addToCartCumulativeExceedsStock() throws Exception {
        Product p = seed("Charger", 800, "Electronics", 4, "desc");
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=3"))
                .andExpect(status().isOk());
        // already 3 in cart, stock=4, adding 2 more would be 5 > 4
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=2"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only 4 in stock"));
    }

    // ── 43. PUT /cart/update — updates qty correctly ──────────
    @Test @Order(43) @WithMockUser(username = "testuser")
    void updateCartItemQty() throws Exception {
        Product p = seed("Scanner", 4000, "Electronics", 10, "desc");
        String cartJson = mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=1"))
                .andReturn().getResponse().getContentAsString();
        Long itemId = mapper.readTree(cartJson).get("items").get(0).get("id").asLong();
        mvc.perform(put("/cart/update/" + itemId + "?quantity=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5));
    }

    // ── 44. PUT /cart/update — blocked when qty exceeds stock ─
    @Test @Order(44) @WithMockUser(username = "testuser")
    void updateCartItemQtyExceedsStock() throws Exception {
        Product p = seed("Mouse", 1200, "Electronics", 3, "desc");
        String cartJson = mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=1"))
                .andReturn().getResponse().getContentAsString();
        Long itemId = mapper.readTree(cartJson).get("items").get(0).get("id").asLong();
        mvc.perform(put("/cart/update/" + itemId + "?quantity=10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only 3 in stock"));
    }

    // ── 45. Cancelled order restores stock ────────────────────
    @Test @Order(45) @WithMockUser(username = "testuser")
    void cancelOrderRestoresStock() throws Exception {
        Product p = seed("Headphones", 2000, "Audio", 6, "desc");
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=4"));
        String orderJson = mvc.perform(post("/orders/checkout"))
                .andReturn().getResponse().getContentAsString();
        Long orderId = mapper.readTree(orderJson).get("id").asLong();
        // stock should be 6 - 4 = 2 after checkout
        mvc.perform(get("/products/" + p.getId())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("u").roles("USER")))
                .andExpect(jsonPath("$.stock").value(2));
        // cancel the order — stock should be restored to 6
        mvc.perform(put("/orders/" + orderId + "/status?status=CANCELLED")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mvc.perform(get("/products/" + p.getId())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("u").roles("USER")))
                .andExpect(jsonPath("$.stock").value(6));
    }

    // ── 46. Cancel already-cancelled order → 400 (no duplicate restore) ─
    @Test @Order(46) @WithMockUser(username = "testuser")
    void cancelAlreadyCancelledOrderBlocked() throws Exception {
        Product p = seed("Tripod", 1500, "Electronics", 5, "desc");
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=2"));
        String orderJson = mvc.perform(post("/orders/checkout"))
                .andReturn().getResponse().getContentAsString();
        Long orderId = mapper.readTree(orderJson).get("id").asLong();
        // cancel once — stock restored: 5-2=3 → back to 5
        mvc.perform(put("/orders/" + orderId + "/status?status=CANCELLED")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        // try to cancel again — must be blocked
        mvc.perform(put("/orders/" + orderId + "/status?status=CANCELLED")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
        // stock must still be 5, not 7
        mvc.perform(get("/products/" + p.getId())
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("u").roles("USER")))
                .andExpect(jsonPath("$.stock").value(5));
    }

    // ── 47. Cannot cancel a SHIPPED order ────────────────────
    @Test @Order(47) @WithMockUser(username = "testuser")
    void cannotCancelShippedOrder() throws Exception {
        Product p = seed("Lens", 8000, "Electronics", 3, "desc");
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=1"));
        String orderJson = mvc.perform(post("/orders/checkout"))
                .andReturn().getResponse().getContentAsString();
        Long orderId = mapper.readTree(orderJson).get("id").asLong();
        // PENDING → CONFIRMED
        mvc.perform(put("/orders/" + orderId + "/status?status=CONFIRMED")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        // CONFIRMED → SHIPPED
        mvc.perform(put("/orders/" + orderId + "/status?status=SHIPPED")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
        // SHIPPED → CANCELLED must be blocked
        mvc.perform(put("/orders/" + orderId + "/status?status=CANCELLED")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    // ── 48. Invalid transition PENDING → DELIVERED blocked ──────
    @Test @Order(48) @WithMockUser(username = "testuser")
    void invalidTransitionBlocked() throws Exception {
        Product p = seed("Flash", 500, "Electronics", 10, "desc");
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=1"));
        String orderJson = mvc.perform(post("/orders/checkout"))
                .andReturn().getResponse().getContentAsString();
        Long orderId = mapper.readTree(orderJson).get("id").asLong();
        mvc.perform(put("/orders/" + orderId + "/status?status=DELIVERED")
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}
