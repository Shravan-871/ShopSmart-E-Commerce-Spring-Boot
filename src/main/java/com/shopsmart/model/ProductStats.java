package com.shopsmart.model;

public class ProductStats {
    private long totalProducts;
    private double averagePrice;
    private long totalStock;

    public ProductStats(long totalProducts, double averagePrice, long totalStock) {
        this.totalProducts = totalProducts;
        this.averagePrice = averagePrice;
        this.totalStock = totalStock;
    }

    public long getTotalProducts() { return totalProducts; }
    public double getAveragePrice() { return averagePrice; }
    public long getTotalStock() { return totalStock; }
}
