package com.shopsmart.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wishlist_item")
public class WishlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    private LocalDateTime addedAt = LocalDateTime.now();

    public WishlistItem() {}

    public WishlistItem(String username, Product product) {
        this.username = username;
        this.product = product;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public Product getProduct() { return product; }
    public LocalDateTime getAddedAt() { return addedAt; }
}
