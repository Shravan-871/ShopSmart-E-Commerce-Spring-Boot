package com.shopsmart.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(1)
public class RateLimitFilter implements Filter {

    private static final int MAX_REQUESTS = 100;
    private static final long WINDOW_MS = 60_000;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        if (!req.getRequestURI().startsWith("/api/v1/")) {
            chain.doFilter(request, response);
            return;
        }

        String key = req.getRemoteAddr();
        Window window = windows.computeIfAbsent(key, k -> new Window());
        synchronized (window) {
            long now = System.currentTimeMillis();
            if (now - window.start > WINDOW_MS) {
                window.start = now;
                window.count.set(0);
            }
            if (window.count.incrementAndGet() > MAX_REQUESTS) {
                HttpServletResponse res = (HttpServletResponse) response;
                res.setStatus(429);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"Rate limit exceeded\"}");
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private static class Window {
        long start = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
    }
}
