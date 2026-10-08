package com.shopsmart.shopsmart;

import com.shopsmart.model.Coupon;
import com.shopsmart.model.Product;
import com.shopsmart.repository.CartRepository;
import com.shopsmart.repository.CouponRepository;
import com.shopsmart.repository.OrderRepository;
import com.shopsmart.repository.ProductRepository;
import com.shopsmart.repository.WishlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommerceApiTests {

    @Autowired MockMvc mvc;
    @Autowired ProductRepository productRepo;
    @Autowired CartRepository cartRepo;
    @Autowired OrderRepository orderRepo;
    @Autowired WishlistRepository wishlistRepo;
    @Autowired CouponRepository couponRepo;

    @BeforeEach
    void clean() {
        orderRepo.deleteAll();
        cartRepo.deleteAll();
        wishlistRepo.deleteAll();
        productRepo.deleteAll();
        couponRepo.deleteAll();
    }

    private Product seed(String name, double price, int stock) {
        return productRepo.save(new Product(name, price, "Electronics", stock, "desc"));
    }

    private Coupon seedCoupon(String code, Coupon.DiscountType type, double value) {
        Coupon c = new Coupon();
        c.setCode(code);
        c.setDiscountType(type);
        c.setDiscountValue(value);
        c.setActive(true);
        c.setExpiryDate(LocalDate.now().plusYears(1));
        return couponRepo.save(c);
    }

    @Test
    @WithMockUser(username = "testuser")
    void validateCouponPercent() throws Exception {
        seedCoupon("SAVE10", Coupon.DiscountType.PERCENT, 10);
        mvc.perform(post("/coupons/validate?code=SAVE10&orderTotal=1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SAVE10"))
                .andExpect(jsonPath("$.discount").value(100.0))
                .andExpect(jsonPath("$.finalTotal").value(900.0));
    }

    @Test
    @WithMockUser(username = "testuser")
    void validateCouponInvalid() throws Exception {
        mvc.perform(post("/coupons/validate?code=NOPE&orderTotal=1000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid coupon code"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void checkoutWithCoupon() throws Exception {
        Product p = seed("Camera", 1000, 20);
        seedCoupon("FLAT500", Coupon.DiscountType.FLAT, 500);
        mvc.perform(post("/cart/add?productId=" + p.getId() + "&quantity=1"))
                .andExpect(status().isOk());
        mvc.perform(post("/orders/checkout?couponCode=FLAT500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(500.0));
    }

    @Test
    @WithMockUser(username = "testuser")
    void wishlistAddListRemove() throws Exception {
        Product p = seed("Tablet", 15000, 10);
        mvc.perform(post("/wishlist/add/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.product.id").value(p.getId()));
        mvc.perform(get("/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        mvc.perform(delete("/wishlist/remove/" + p.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Removed from wishlist"));
        mvc.perform(get("/wishlist"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "testuser")
    void wishlistDuplicateAdd() throws Exception {
        Product p = seed("Drone", 25000, 5);
        mvc.perform(post("/wishlist/add/" + p.getId())).andExpect(status().isOk());
        mvc.perform(post("/wishlist/add/" + p.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Already in wishlist"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadProductImage() throws Exception {
        Product p = seed("Phone", 20000, 15);
        MockMultipartFile file = new MockMultipartFile(
                "file", "phone.jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00});
        mvc.perform(multipart("/products/" + p.getId() + "/image").file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").value("/uploads/product-" + p.getId() + ".jpg"));
        Files.deleteIfExists(Path.of("uploads", "product-" + p.getId() + ".jpg"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void uploadProductImageRejectsBadType() throws Exception {
        Product p = seed("Phone", 20000, 15);
        MockMultipartFile file = new MockMultipartFile(
                "file", "notes.txt", "text/plain", "hello".getBytes());
        mvc.perform(multipart("/products/" + p.getId() + "/image").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only jpg, png, webp allowed"));
    }
}
