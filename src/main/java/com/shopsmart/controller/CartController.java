package com.shopsmart.controller;

import com.shopsmart.model.Cart;
import com.shopsmart.model.CartItem;
import com.shopsmart.model.Product;
import com.shopsmart.repository.CartItemRepository;
import com.shopsmart.repository.CartRepository;
import com.shopsmart.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping({"/cart", "/api/v1/cart"})
@Transactional
@Tag(name = "Cart", description = "Shopping cart operations")
public class CartController {

    private final CartRepository cartRepo;
    private final CartItemRepository itemRepo;
    private final ProductRepository productRepo;

    public CartController(CartRepository cartRepo, CartItemRepository itemRepo, ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.itemRepo = itemRepo;
        this.productRepo = productRepo;
    }

    private Cart getOrCreateCart(String username) {
        return cartRepo.findByUsername(username).orElseGet(() -> cartRepo.save(new Cart(username)));
    }

    @GetMapping
    public Cart getCart(Principal principal) {
        return getOrCreateCart(principal.getName());
    }

    @PostMapping("/add")
    public ResponseEntity<?> addItem(@RequestParam Long productId,
                                     @RequestParam(defaultValue = "1") int quantity,
                                     Principal principal) {
        Optional<Product> opt = productRepo.findById(productId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Product product = opt.get();

        Cart cart = getOrCreateCart(principal.getName());
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();

        int currentQty = existing.map(CartItem::getQuantity).orElse(0);
        int newQty = currentQty + quantity;
        if (newQty > product.getStock())
            return ResponseEntity.badRequest().body(Map.of("error", "Only " + product.getStock() + " in stock"));

        if (existing.isPresent()) {
            existing.get().setQuantity(newQty);
        } else {
            cart.getItems().add(new CartItem(cart, product, quantity));
        }
        cartRepo.save(cart);
        return ResponseEntity.ok(cartRepo.findByUsername(principal.getName()).orElseThrow());
    }

    @DeleteMapping("/remove/{itemId}")
    public ResponseEntity<?> removeItem(@PathVariable Long itemId, Principal principal) {
        Optional<CartItem> item = itemRepo.findById(itemId);
        if (item.isEmpty() || !item.get().getCart().getUsername().equals(principal.getName()))
            return ResponseEntity.notFound().build();
        Cart cart = item.get().getCart();
        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        cartRepo.save(cart);
        return ResponseEntity.ok(Map.of("message", "Item removed"));
    }

    @PutMapping("/update/{itemId}")
    public ResponseEntity<?> updateItem(@PathVariable Long itemId,
                                        @RequestParam int quantity,
                                        Principal principal) {
        Optional<CartItem> opt = itemRepo.findById(itemId);
        if (opt.isEmpty() || !opt.get().getCart().getUsername().equals(principal.getName()))
            return ResponseEntity.notFound().build();
        if (quantity < 1) return ResponseEntity.badRequest().body(Map.of("error", "Quantity must be at least 1"));
        int stock = opt.get().getProduct().getStock();
        if (quantity > stock) return ResponseEntity.badRequest().body(Map.of("error", "Only " + stock + " in stock"));
        opt.get().setQuantity(quantity);
        itemRepo.save(opt.get());
        return ResponseEntity.ok(cartRepo.findByUsername(principal.getName()).orElseThrow());
    }

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(Principal principal) {
        cartRepo.findByUsername(principal.getName()).ifPresent(cart -> {
            cart.getItems().clear();
            cartRepo.save(cart);
        });
        return ResponseEntity.ok(Map.of("message", "Cart cleared"));
    }
}
