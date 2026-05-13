package com.shopsmart.controller;

import com.shopsmart.model.*;
import com.shopsmart.repository.CartRepository;
import com.shopsmart.repository.OrderRepository;
import com.shopsmart.repository.ProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
@Transactional
public class OrderController {

    private final OrderRepository orderRepo;
    private final CartRepository cartRepo;
    private final ProductRepository productRepo;

    public OrderController(OrderRepository orderRepo, CartRepository cartRepo, ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(Principal principal) {
        Optional<Cart> cartOpt = cartRepo.findByUsername(principal.getName());
        if (cartOpt.isEmpty() || cartOpt.get().getItems().isEmpty())
            return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));

        Cart cart = cartOpt.get();
        double total = cart.getItems().stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();

        Order order = new Order(principal.getName(), total);
        for (CartItem item : cart.getItems()) {
            Product p = item.getProduct();
            order.getItems().add(new OrderItem(order, p.getName(), p.getPrice(), p.getCategory(), item.getQuantity()));
            p.setStock(Math.max(0, p.getStock() - item.getQuantity()));
            productRepo.save(p);
        }
        orderRepo.save(order);
        cart.getItems().clear();
        cartRepo.save(cart);
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public List<Order> getMyOrders(Principal principal) {
        return orderRepo.findByUsernameOrderByCreatedAtDesc(principal.getName());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrder(@PathVariable Long id, Principal principal) {
        return orderRepo.findById(id)
                .filter(o -> o.getUsername().equals(principal.getName()) || principal.getName().equals("admin"))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam Order.Status status) {
        return orderRepo.findById(id).map(o -> {
            Order.Status prev = o.getStatus();

            if (prev == Order.Status.SHIPPED || prev == Order.Status.DELIVERED || prev == Order.Status.CANCELLED)
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Order is " + prev + " — no further changes allowed"));

            boolean valid = switch (prev) {
                case PENDING   -> status == Order.Status.CONFIRMED || status == Order.Status.CANCELLED;
                case CONFIRMED -> status == Order.Status.SHIPPED   || status == Order.Status.CANCELLED;
                default        -> false;
            };
            if (!valid)
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Cannot transition from " + prev + " to " + status));

            o.setStatus(status);
            if (status == Order.Status.CONFIRMED) o.setConfirmedAt(LocalDateTime.now());
            if (status == Order.Status.SHIPPED)   o.setShippedAt(LocalDateTime.now());

            if (status == Order.Status.CANCELLED && !o.isStockRestored()) {
                restoreStock(o);
                o.setStockRestored(true);
            }
            return ResponseEntity.ok(orderRepo.save(o));
        }).orElse(ResponseEntity.notFound().build());
    }

    // User cancels their own order (PENDING or CONFIRMED only)
    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelByUser(@PathVariable Long id, Principal principal) {
        return orderRepo.findById(id).map(o -> {
            if (!o.getUsername().equals(principal.getName()))
                return ResponseEntity.status(403).body(Map.of("error", "Not your order"));

            Order.Status prev = o.getStatus();
            if (prev == Order.Status.SHIPPED || prev == Order.Status.DELIVERED || prev == Order.Status.CANCELLED)
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Order is " + prev + " — cannot be cancelled"));

            o.setStatus(Order.Status.CANCELLED);
            if (!o.isStockRestored()) {
                restoreStock(o);
                o.setStockRestored(true);
            }
            return ResponseEntity.ok(orderRepo.save(o));
        }).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    private void restoreStock(Order o) {
        for (OrderItem item : o.getItems()) {
            productRepo.findByName(item.getProductName()).ifPresent(p -> {
                p.setStock(p.getStock() + item.getQuantity());
                productRepo.save(p);
            });
        }
    }
}
