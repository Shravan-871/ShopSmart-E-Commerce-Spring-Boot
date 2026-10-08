package com.shopsmart.service;

import com.shopsmart.model.Order;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final int LOW_STOCK_THRESHOLD = 10;

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${shopsmart.mail.enabled:false}")
    private boolean mailEnabled;

    @Value("${shopsmart.mail.admin:admin@shopsmart.local}")
    private String adminEmail;

    public EmailService(
            @org.springframework.beans.factory.annotation.Autowired(required = false) JavaMailSender mailSender,
            TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public static int lowStockThreshold() {
        return LOW_STOCK_THRESHOLD;
    }

    public void sendOrderConfirmation(Order order) {
        if (!mailEnabled || mailSender == null) {
            log.info("Mail disabled — order confirmation for #{} to {}", order.getId(), order.getUsername());
            return;
        }
        try {
            Context ctx = new Context();
            ctx.setVariable("order", order);
            String html = templateEngine.process("email/order-confirmation", ctx);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(order.getUsername() + "@shopsmart.local");
            helper.setSubject("Order #" + order.getId() + " confirmed");
            helper.setText(html, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send order confirmation for #{}", order.getId(), e);
        }
    }

    public void sendLowStockAlert(String productName, int stock) {
        if (!mailEnabled || mailSender == null) {
            log.info("Mail disabled — low stock alert: {} ({})", productName, stock);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(adminEmail);
            helper.setSubject("Low stock: " + productName);
            helper.setText(productName + " has only " + stock + " units left.", false);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send low stock alert for {}", productName, e);
        }
    }
}
