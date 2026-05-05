package com.shopsmart.controller;

import com.shopsmart.model.Product;
import com.shopsmart.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class UiController {

    private final ProductRepository repo;

    public UiController(ProductRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("products", repo.findAll());
        return "index";
    }

    @PostMapping("/ui/products")
    public String addProduct(@RequestParam String name,
                             @RequestParam double price) {
        repo.save(new Product(name, price));
        return "redirect:/";
    }
}