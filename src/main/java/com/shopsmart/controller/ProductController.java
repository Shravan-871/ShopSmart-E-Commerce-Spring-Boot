package com.shopsmart.controller;

import com.shopsmart.model.Product;
import com.shopsmart.model.ProductStats;
import com.shopsmart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<Page<Product>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(repo.findAll(PageRequest.of(page, size)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody @Valid Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repo.save(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> update(@PathVariable Long id, @RequestBody @Valid Product updated) {
        return repo.findById(id)
                .map(p -> {
                    p.setName(updated.getName());
                    p.setPrice(updated.getPrice());
                    p.setCategory(updated.getCategory());
                    p.setStock(updated.getStock());
                    p.setDescription(updated.getDescription());
                    return ResponseEntity.ok(repo.save(p));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Product>> search(@RequestParam String name) {
        return ResponseEntity.ok(repo.findByNameContainingIgnoreCase(name));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(repo.findByCategoryIgnoreCase(category));
    }

    @GetMapping("/stats")
    public ResponseEntity<ProductStats> getStats() {
        return ResponseEntity.ok(new ProductStats(repo.countAll(), repo.avgPrice(), repo.sumStock()));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<List<Product>> getLowStock(@RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(repo.findByStockLessThanEqual(threshold));
    }

    @DeleteMapping("/all")
    public ResponseEntity<Map<String, String>> deleteAll() {
        long count = repo.count();
        repo.deleteAll();
        return ResponseEntity.ok(Map.of("message", count + " products deleted"));
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Map<String, String>> bulkDelete(@RequestBody List<Long> ids) {
        repo.deleteAllById(ids);
        return ResponseEntity.ok(Map.of("message", ids.size() + " products deleted"));
    }

    @GetMapping("/random")
    public ResponseEntity<List<Product>> generateRandom(@RequestParam(defaultValue = "10") int count) {
        count = Math.min(Math.max(count, 1), 100);
        String[] names = {"Laptop", "Phone", "Tablet", "Watch", "Headphones", "Keyboard", "Mouse", "Monitor", "Camera", "Speaker"};
        String[] categories = {"Electronics", "Accessories", "Computers", "Audio"};
        List<Product> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String name = names[(int)(Math.random() * names.length)];
            double price = 1000 + (Math.random() * 90000);
            String category = categories[(int)(Math.random() * categories.length)];
            int stock = (int)(Math.random() * 100);
            list.add(repo.save(new Product(name, price, category, stock, "High quality " + name.toLowerCase())));
        }
        return ResponseEntity.ok(list);
    }
}
