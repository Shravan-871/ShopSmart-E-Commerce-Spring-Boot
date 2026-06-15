package com.shopsmart.controller;

import com.shopsmart.model.Product;
import com.shopsmart.model.ProductStats;
import com.shopsmart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping({"/products", "/api/v1/products"})
@Tag(name = "Products", description = "Product catalog CRUD and queries")
public class ProductController {

    private final ProductRepository repo;

    public ProductController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    @Operation(summary = "List products (paginated)")
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
                    if (updated.getImageUrl() != null) p.setImageUrl(updated.getImageUrl());
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

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload product image (ADMIN)")
    public ResponseEntity<?> uploadImage(@PathVariable Long id, @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        String contentType = file.getContentType();
        if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp")))
            return ResponseEntity.badRequest().body(Map.of("error", "Only jpg, png, webp allowed"));
        if (file.getSize() > 2 * 1024 * 1024)
            return ResponseEntity.badRequest().body(Map.of("error", "Max file size is 2MB"));

        return repo.findById(id).map(p -> {
            try {
                Path uploadDir = Paths.get("uploads");
                Files.createDirectories(uploadDir);
                String ext = switch (contentType) {
                    case "image/png" -> ".png";
                    case "image/webp" -> ".webp";
                    default -> ".jpg";
                };
                String filename = "product-" + id + ext;
                Files.write(uploadDir.resolve(filename), file.getBytes());
                p.setImageUrl("/uploads/" + filename);
                return ResponseEntity.ok(repo.save(p));
            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Upload failed"));
            }
        }).orElse(ResponseEntity.notFound().build());
    }
}
