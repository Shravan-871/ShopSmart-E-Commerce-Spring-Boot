package com.shopsmart.repository;

import com.shopsmart.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findAll(Pageable pageable);

    List<Product> findByNameContainingIgnoreCase(String name);

    List<Product> findByCategoryIgnoreCase(String category);

    List<Product> findByStockLessThanEqual(int threshold);

    Optional<Product> findByName(String name);

    @Query("SELECT COUNT(p) FROM Product p")
    long countAll();

    @Query("SELECT COALESCE(AVG(p.price), 0.0) FROM Product p")
    double avgPrice();

    @Query("SELECT COALESCE(SUM(p.stock), 0) FROM Product p")
    long sumStock();
}
