package com.shopsmart.controller;

import com.shopsmart.model.Product;
import com.shopsmart.repository.OrderRepository;
import com.shopsmart.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class DashboardController {

    private final ProductRepository productRepo;
    private final OrderRepository orderRepo;

    @Value("${rust.service.url:http://localhost:8081}")
    private String rustServiceUrl;

    public DashboardController(ProductRepository productRepo, OrderRepository orderRepo) {
        this.productRepo = productRepo;
        this.orderRepo = orderRepo;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Product> all = productRepo.findAll();

        Map<String, Long> stockByCategory = all.stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.summingLong(Product::getStock)));

        List<Product> topExpensive = all.stream()
                .sorted(Comparator.comparingDouble(Product::getPrice).reversed())
                .limit(5)
                .toList();

        List<Product> lowStock = productRepo.findByStockLessThanEqual(10);

        model.addAttribute("totalProducts", all.size());
        model.addAttribute("totalOrders", orderRepo.count());
        model.addAttribute("categoryLabels", stockByCategory.keySet());
        model.addAttribute("categoryStock", stockByCategory.values());
        model.addAttribute("topExpensive", topExpensive);
        model.addAttribute("lowStock", lowStock);
        model.addAttribute("rustServiceUrl", rustServiceUrl);
        return "dashboard";
    }
}
