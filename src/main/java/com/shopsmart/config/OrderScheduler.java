package com.shopsmart.config;

import com.shopsmart.model.Order;
import com.shopsmart.repository.OrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OrderScheduler {

    private final OrderRepository orderRepo;

    public OrderScheduler(OrderRepository orderRepo) {
        this.orderRepo = orderRepo;
    }

    @Scheduled(fixedDelay = 10_000) // runs every 10s
    @Transactional
    public void progressOrders() {
        LocalDateTime shipCutoff    = LocalDateTime.now().minusSeconds(30); // CONFIRMED → SHIPPED after 30s
        LocalDateTime deliverCutoff = LocalDateTime.now().minusSeconds(60); // SHIPPED → DELIVERED after 60s

        // CONFIRMED → SHIPPED after 30s
        List<Order> toShip = orderRepo.findByStatus(Order.Status.CONFIRMED);
        for (Order o : toShip) {
            if (o.getConfirmedAt() != null && o.getConfirmedAt().isBefore(shipCutoff)) {
                o.setStatus(Order.Status.SHIPPED);
                o.setShippedAt(LocalDateTime.now());
                orderRepo.save(o);
            }
        }

        // SHIPPED → DELIVERED after 60s
        List<Order> toDeliver = orderRepo.findByStatus(Order.Status.SHIPPED);
        for (Order o : toDeliver) {
            if (o.getShippedAt() != null && o.getShippedAt().isBefore(deliverCutoff)) {
                o.setStatus(Order.Status.DELIVERED);
                orderRepo.save(o);
            }
        }
    }
}
