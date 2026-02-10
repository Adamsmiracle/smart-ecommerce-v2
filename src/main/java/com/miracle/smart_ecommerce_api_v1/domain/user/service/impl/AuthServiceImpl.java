package com.miracle.smart_ecommerce_api_v1.domain.user.service.impl;

import com.miracle.smart_ecommerce_api_v1.domain.user.entity.User;
import com.miracle.smart_ecommerce_api_v1.domain.user.dto.request.CreateUserRequest;
import com.miracle.smart_ecommerce_api_v1.domain.user.dto.request.LoginRequest;
import com.miracle.smart_ecommerce_api_v1.domain.user.dto.response.AuthResponse;
import com.miracle.smart_ecommerce_api_v1.domain.user.service.AuthService;
import com.miracle.smart_ecommerce_api_v1.exception.DuplicateResourceException;
import com.miracle.smart_ecommerce_api_v1.exception.ResourceNotFoundException;
import com.miracle.smart_ecommerce_api_v1.domain.user.repository.UserRepository;
import com.miracle.smart_ecommerce_api_v1.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AuthService.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public AuthResponse register(CreateUserRequest request) {
        log.info("Registering new user with email: {}", request.getEmailAddress());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmailAddress())) {
            throw new DuplicateResourceException("User", "email", request.getEmailAddress());
        }

        // Create user with hashed password
        User user = User.builder()
                .emailAddress(request.getEmailAddress())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Generate tokens (include roles)
        List<String> roles = new ArrayList<>(savedUser.getRoles() == null || savedUser.getRoles().isEmpty() ?
                java.util.Set.of("ROLE_USER") : savedUser.getRoles());

        String accessToken = jwtTokenProvider.generateToken(savedUser.getId(), savedUser.getEmailAddress(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.getId());

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getExpirationTime(),
                savedUser.getId(),
                savedUser.getEmailAddress(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                roles.stream().collect(Collectors.toList())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Check if user is active
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("Account is deactivated. Please contact support.");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Invalid password attempt for user: {}", request.getEmail());
            throw new BadCredentialsException("Invalid email or password");
        }

        log.info("User logged in successfully: {}", user.getId());

        // Generate tokens (include roles)
        List<String> roles = new ArrayList<>(user.getRoles() == null || user.getRoles().isEmpty() ?
                java.util.Set.of("ROLE_USER") : user.getRoles());

        String accessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmailAddress(), roles);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return AuthResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getExpirationTime(),
                user.getId(),
                user.getEmailAddress(),
                user.getFirstName(),
                user.getLastName(),
                roles.stream().collect(Collectors.toList())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        log.debug("Refreshing token");

        // Validate refresh token
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BadCredentialsException("Invalid or expired refresh token");
        }

        // Check if it's actually a refresh token
        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BadCredentialsException("Invalid token type. Please use refresh token.");
        }

        // Get user ID from token
        UUID userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        // Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.forResource("User", userId));

        // Check if user is active
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("Account is deactivated. Please contact support.");
        }

        log.info("Token refreshed successfully for user: {}", userId);

        // Generate new tokens (include roles)
        List<String> roles = new ArrayList<>(user.getRoles() == null || user.getRoles().isEmpty() ?
                java.util.Set.of("ROLE_USER") : user.getRoles());

        String newAccessToken = jwtTokenProvider.generateToken(user.getId(), user.getEmailAddress(), roles);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());

        return AuthResponse.of(
                newAccessToken,
                newRefreshToken,
                jwtTokenProvider.getExpirationTime(),
                user.getId(),
                user.getEmailAddress(),
                user.getFirstName(),
                user.getLastName(),
                roles.stream().collect(Collectors.toList())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(String token) {
        log.debug("Getting current user from token");

        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadCredentialsException("Invalid or expired token");
        }

        UUID userId = jwtTokenProvider.getUserIdFromToken(token);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.forResource("User", userId));

        List<String> roles = new ArrayList<>(user.getRoles() == null || user.getRoles().isEmpty()
                ? java.util.Set.of("ROLE_USER") : user.getRoles());

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmailAddress())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(roles.stream().collect(Collectors.toList()))
                .build();
    }
}

