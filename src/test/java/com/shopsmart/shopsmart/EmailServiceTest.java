package com.shopsmart.shopsmart;

import com.shopsmart.model.Order;
import com.shopsmart.model.OrderItem;
import com.shopsmart.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock JavaMailSender mailSender;
    @Mock TemplateEngine templateEngine;
    @Mock MimeMessage mimeMessage;

    EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, templateEngine);
        ReflectionTestUtils.setField(emailService, "adminEmail", "admin@shopsmart.local");
    }

    @Test
    void mailDisabledDoesNotSend() {
        ReflectionTestUtils.setField(emailService, "mailEnabled", false);
        Order order = new Order("alice", 100);
        order.setId(1L);
        order.getItems().add(new OrderItem(order, "Laptop", 100, "Electronics", 1));

        assertDoesNotThrow(() -> emailService.sendOrderConfirmation(order));
        assertDoesNotThrow(() -> emailService.sendLowStockAlert("Laptop", 3));
        verifyNoInteractions(mailSender);
    }

    @Test
    void mailEnabledSendsHtmlOrderConfirmation() throws Exception {
        ReflectionTestUtils.setField(emailService, "mailEnabled", true);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("email/order-confirmation"), any(Context.class)))
                .thenReturn("<html>ok</html>");

        Order order = new Order("alice", 100);
        order.setId(9L);
        order.getItems().add(new OrderItem(order, "Laptop", 100, "Electronics", 1));

        emailService.sendOrderConfirmation(order);

        verify(templateEngine).process(eq("email/order-confirmation"), any(Context.class));
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void lowStockThresholdIsTen() {
        assertEquals(10, EmailService.lowStockThreshold());
    }
}
