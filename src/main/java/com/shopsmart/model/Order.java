package com.shopsmart.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    public enum Status { PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime confirmedAt;
    private LocalDateTime shippedAt;

    // Tracks whether stock has been restored for this order (prevents double-restore)
    private boolean stockRestored = false;

    private double total;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = jakarta.persistence.FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}
    public Order(String username, double total) {
        this.username = username;
        this.total = total;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getConfirmedAt() { return confirmedAt; }
    public LocalDateTime getShippedAt() { return shippedAt; }
    public boolean isStockRestored() { return stockRestored; }
    public double getTotal() { return total; }
    public List<OrderItem> getItems() { return items; }

    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setStatus(Status status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setConfirmedAt(LocalDateTime confirmedAt) { this.confirmedAt = confirmedAt; }
    public void setShippedAt(LocalDateTime shippedAt) { this.shippedAt = shippedAt; }
    public void setStockRestored(boolean stockRestored) { this.stockRestored = stockRestored; }
    public void setTotal(double total) { this.total = total; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
