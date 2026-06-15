package com.shopsmart.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "coupon")
public class Coupon {

    public enum DiscountType { FLAT, PERCENT }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private double discountValue;

    @Column(nullable = false)
    private LocalDate expiryDate;

    private boolean active = true;

    public Long getId() { return id; }
    public String getCode() { return code; }
    public DiscountType getDiscountType() { return discountType; }
    public double getDiscountValue() { return discountValue; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public boolean isActive() { return active; }

    public void setId(Long id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setDiscountType(DiscountType discountType) { this.discountType = discountType; }
    public void setDiscountValue(double discountValue) { this.discountValue = discountValue; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public void setActive(boolean active) { this.active = active; }
}
