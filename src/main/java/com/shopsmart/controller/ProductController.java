package com.shopsmart.controller;

import com.shopsmart.model.Product;
import com.shopsmart.repository.ProductRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.ArrayList;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<Product> getAll() {
        return repo.findAll();
    }

    @PostMapping
    public Product create(@RequestBody @Valid Product product) {
        return repo.save(product);
    }

    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return repo.findById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @RequestBody Product updated) {
        return repo.findById(id)
                .map(p -> {
                    p.setName(updated.getName());
                    p.setPrice(updated.getPrice());
                    return repo.save(p);
                })
                .orElse(null);
    }

    @GetMapping("/random")
    public List<Product> generateRandom() {
        String[] names = {
            "Laptop", "Phone", "Tablet", "Watch", "Headphones",
            "Keyboard", "Mouse", "Monitor", "Camera", "Speaker"
        };

        List<Product> list = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            String name = names[(int)(Math.random() * names.length)];
            double price = 1000 + (Math.random() * 90000);

            Product p = new Product(name, price);
            list.add(repo.save(p));
        }

        return list;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}