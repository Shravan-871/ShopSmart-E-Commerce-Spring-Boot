package com.shopsmart.controller;

import com.shopsmart.model.Coupon;
import com.shopsmart.repository.CouponRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping({"/coupons", "/api/v1/coupons"})
@Tag(name = "Coupons", description = "Coupon validation")
public class CouponController {

    private final CouponRepository couponRepo;

    public CouponController(CouponRepository couponRepo) {
        this.couponRepo = couponRepo;
    }

    @PostMapping("/validate")
    public ResponseEntity<?> validate(@RequestParam String code, @RequestParam double orderTotal) {
        return couponRepo.findByCodeIgnoreCase(code).map(c -> {
            if (!c.isActive() || c.getExpiryDate().isBefore(LocalDate.now()))
                return ResponseEntity.badRequest().body(Map.of("error", "Coupon expired or inactive"));
            double discount = c.getDiscountType() == Coupon.DiscountType.PERCENT
                    ? orderTotal * c.getDiscountValue() / 100.0
                    : c.getDiscountValue();
            discount = Math.min(discount, orderTotal);
            return ResponseEntity.ok(Map.of(
                    "code", c.getCode(),
                    "discount", discount,
                    "finalTotal", orderTotal - discount
            ));
        }).orElse(ResponseEntity.badRequest().body(Map.of("error", "Invalid coupon code")));
    }
}
