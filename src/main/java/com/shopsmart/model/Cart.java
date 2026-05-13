package com.shopsmart.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    private List<CartItem> items = new ArrayList<>();

    public Cart() {}
    public Cart(String username) { this.username = username; }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public List<CartItem> getItems() { return items; }

    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setItems(List<CartItem> items) { this.items = items; }
}
