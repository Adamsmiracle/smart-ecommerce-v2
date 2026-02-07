package com.miracle.smart_ecommerce_api_v1.service;

import com.miracle.smart_ecommerce_api_v1.dto.request.CreateUserRequest;
import com.miracle.smart_ecommerce_api_v1.dto.request.LoginRequest;
import com.miracle.smart_ecommerce_api_v1.dto.response.AuthResponse;

/**
 * Service interface for Authentication operations.
 */
public interface AuthService {

    /**
     * Register a new user
     */
    AuthResponse register(CreateUserRequest request);

    /**
     * Login user and return tokens
     */
    AuthResponse login(LoginRequest request);

    /**
     * Refresh access token using refresh token
     */
    AuthResponse refreshToken(String refreshToken);

    /**
     * Get current authenticated user info
     */
    AuthResponse getCurrentUser(String token);
}

