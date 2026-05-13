package com.shopsmart.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    private String productName;
    private double productPrice;
    private String productCategory;
    private int quantity;

    public OrderItem() {}
    public OrderItem(Order order, String productName, double productPrice, String productCategory, int quantity) {
        this.order = order;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productCategory = productCategory;
        this.quantity = quantity;
    }

    public Long getId() { return id; }
    public Order getOrder() { return order; }
    public String getProductName() { return productName; }
    public double getProductPrice() { return productPrice; }
    public String getProductCategory() { return productCategory; }
    public int getQuantity() { return quantity; }

    public void setId(Long id) { this.id = id; }
    public void setOrder(Order order) { this.order = order; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setProductPrice(double productPrice) { this.productPrice = productPrice; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
