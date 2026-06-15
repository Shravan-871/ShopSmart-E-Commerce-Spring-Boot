package com.shopsmart.controller;

import com.shopsmart.model.Order;
import com.shopsmart.model.Product;
import com.shopsmart.model.ProductStats;
import com.shopsmart.model.WishlistItem;
import com.shopsmart.repository.CartRepository;
import com.shopsmart.repository.OrderRepository;
import com.shopsmart.repository.ProductRepository;
import com.shopsmart.repository.WishlistRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Controller
public class UiController {

    private final ProductRepository repo;
    private final CartRepository cartRepo;
    private final OrderRepository orderRepo;
    private final WishlistRepository wishlistRepo;

    public UiController(ProductRepository repo, CartRepository cartRepo, OrderRepository orderRepo,
                        WishlistRepository wishlistRepo) {
        this.repo = repo;
        this.cartRepo = cartRepo;
        this.orderRepo = orderRepo;
        this.wishlistRepo = wishlistRepo;
    }

    @GetMapping("/")
    public String home(Model model,
                       @RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "20") int size,
                       @RequestParam(required = false) String search,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String sort,
                       @RequestParam(required = false) Double minPrice,
                       @RequestParam(required = false) Double maxPrice) {

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
            products = new java.util.ArrayList<>(productPage.getContent());
            totalPages = productPage.getTotalPages();
        }

        if (minPrice != null) {
            products = products.stream().filter(p -> p.getPrice() >= minPrice).toList();
            model.addAttribute("minPrice", minPrice);
        }
        if (maxPrice != null) {
            products = products.stream().filter(p -> p.getPrice() <= maxPrice).toList();
            model.addAttribute("maxPrice", maxPrice);
        }
        if (sort != null && !sort.isBlank()) {
            products = switch (sort) {
                case "price_asc" -> products.stream().sorted(java.util.Comparator.comparingDouble(Product::getPrice)).toList();
                case "price_desc" -> products.stream().sorted(java.util.Comparator.comparingDouble(Product::getPrice).reversed()).toList();
                case "name" -> products.stream().sorted(java.util.Comparator.comparing(Product::getName)).toList();
                default -> products;
            };
            model.addAttribute("sort", sort);
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

    @GetMapping("/cart-page")
    public String cartPage() {
        return "cart";
    }

    @GetMapping("/wishlist-page")
    public String wishlistPage(Model model, Principal principal) {
        List<WishlistItem> items = wishlistRepo.findByUsernameOrderByAddedAtDesc(principal.getName());
        model.addAttribute("items", items);
        return "wishlist";
    }

    @GetMapping("/my-orders")
    public String myOrders(Model model, Principal principal) {
        List<Order> orders = orderRepo.findByUsernameOrderByCreatedAtDesc(principal.getName());
        model.addAttribute("orders", orders);
        return "orders";
    }

    @GetMapping("/my-orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model, Principal principal) {
        return orderRepo.findById(id)
                .filter(o -> o.getUsername().equals(principal.getName()) ||
                        principal.getName().equals("admin"))
                .map(o -> {
                    model.addAttribute("order", o);
                    return "order-detail";
                })
                .orElse("redirect:/my-orders");
    }
}
