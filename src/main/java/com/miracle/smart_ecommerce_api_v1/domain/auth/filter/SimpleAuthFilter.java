package com.miracle.smart_ecommerce_api_v1.domain.auth.filter;

import com.miracle.smart_ecommerce_api_v1.domain.user.repository.UserRepository;
import com.miracle.smart_ecommerce_api_v1.domain.user.entity.User;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

/**
 * Simple filter that reads X-User-Id header (if provided) and places userId and role into MDC
 * so downstream components can access the authenticated user. This is intentionally simple
 * and does not replace a real security framework.
 */
@Component
public class SimpleAuthFilter extends HttpFilter {

    private final UserRepository userRepository;

    public SimpleAuthFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        String userIdHeader = req.getHeader("X-User-Id");
        if (userIdHeader != null && !userIdHeader.isBlank()) {
            try {
                UUID userId = UUID.fromString(userIdHeader);
                Optional<User> maybe = userRepository.findById(userId);
                if (maybe.isPresent()) {
                    User u = maybe.get();
                    MDC.put("userId", u.getId().toString());
                    MDC.put("userRole", u.getRole());
                }
            } catch (IllegalArgumentException e) {
                // invalid UUID - ignore
            }
        }

        try {
            chain.doFilter(req, res);
        } finally {
            MDC.remove("userId");
            MDC.remove("userRole");
        }
    }
}
