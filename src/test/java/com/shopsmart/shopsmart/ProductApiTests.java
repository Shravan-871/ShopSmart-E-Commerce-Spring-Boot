package com.shopsmart.shopsmart;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsmart.model.Product;
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

    @BeforeEach
    void clean() { repo.deleteAll(); }

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
}
