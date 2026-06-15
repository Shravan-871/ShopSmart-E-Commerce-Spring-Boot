package com.shopsmart.controller;

import com.shopsmart.model.WishlistItem;
import com.shopsmart.repository.ProductRepository;
import com.shopsmart.repository.WishlistRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/wishlist", "/api/v1/wishlist"})
@Tag(name = "Wishlist", description = "Save products for later")
public class WishlistController {

    private final WishlistRepository wishlistRepo;
    private final ProductRepository productRepo;

    public WishlistController(WishlistRepository wishlistRepo, ProductRepository productRepo) {
        this.wishlistRepo = wishlistRepo;
        this.productRepo = productRepo;
    }

    @GetMapping
    public List<WishlistItem> getWishlist(Principal principal) {
        return wishlistRepo.findByUsernameOrderByAddedAtDesc(principal.getName());
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<?> add(@PathVariable Long productId, Principal principal) {
        return productRepo.findById(productId).map(p -> {
            if (wishlistRepo.findByUsernameAndProductId(principal.getName(), productId).isPresent())
                return ResponseEntity.badRequest().body(Map.of("error", "Already in wishlist"));
            return ResponseEntity.ok(wishlistRepo.save(new WishlistItem(principal.getName(), p)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> remove(@PathVariable Long productId, Principal principal) {
        wishlistRepo.deleteByUsernameAndProductId(principal.getName(), productId);
        return ResponseEntity.ok(Map.of("message", "Removed from wishlist"));
    }
}
