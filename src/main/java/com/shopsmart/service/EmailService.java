package com.shopsmart.service;

import com.shopsmart.model.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${shopsmart.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${shopsmart.mail.admin:admin@shopsmart.local}")
    private String adminEmail;

    public EmailService(@org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOrderConfirmation(Order order) {
        if (!mailEnabled || mailSender == null) {
            log.info("Mail disabled — order confirmation for #{} to {}", order.getId(), order.getUsername());
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(order.getUsername() + "@shopsmart.local");
        msg.setSubject("Order #" + order.getId() + " confirmed");
        msg.setText("Thank you! Your order total is ₹" + order.getTotal());
        mailSender.send(msg);
    }

    public void sendLowStockAlert(String productName, int stock) {
        if (!mailEnabled || mailSender == null) {
            log.info("Mail disabled — low stock alert: {} ({})", productName, stock);
            return;
        }
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(adminEmail);
        msg.setSubject("Low stock: " + productName);
        msg.setText(productName + " has only " + stock + " units left.");
        mailSender.send(msg);
    }
}
