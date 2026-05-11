package com.shopsmart.controller;

import com.shopsmart.model.Product;
import com.shopsmart.model.ProductStats;
import com.shopsmart.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UiController {

    private final ProductRepository repo;

    public UiController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/")
    public String home(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String category) {

        List<Product> products;
        int currentPage = page;
        long totalPages = 1;

        if (search != null && !search.isBlank()) {
            products = repo.findByNameContainingIgnoreCase(search);
            model.addAttribute("search", search);
        } else if (category != null && !category.isBlank()) {
            products = repo.findByCategoryIgnoreCase(category);
            model.addAttribute("category", category);
        } else {
            Page<Product> productPage = repo.findAll(PageRequest.of(page, size));
            products = productPage.getContent();
            totalPages = productPage.getTotalPages();
        }

        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);

        // Stats
        model.addAttribute("stats", new ProductStats(repo.countAll(), repo.avgPrice(), repo.sumStock()));

        model.addAttribute("products", products);
        model.addAttribute("lowStockCount", repo.findByStockLessThanEqual(10).size());
        model.addAttribute("categories", List.of("Electronics", "Clothing", "Food", "Accessories", "Computers", "Audio", "Sports", "Books"));

        return "index";
    }

    @PostMapping("/ui/products")
    public String addProduct(@RequestParam String name,
                             @RequestParam double price,
                             @RequestParam String category,
                             @RequestParam(defaultValue = "0") int stock,
                             @RequestParam(required = false) String description) {
        if (name.matches("^[A-Za-z ]+$") && price > 0) {
            repo.save(new Product(name, price, category, stock, description));
        }
        return "redirect:/";
    }

    @PostMapping("/ui/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        repo.deleteById(id);
        return "redirect:/";
    }
}
