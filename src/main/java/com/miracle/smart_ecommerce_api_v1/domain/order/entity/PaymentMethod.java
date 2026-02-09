package com.miracle.smart_ecommerce_api_v1.domain.order.entity;

import com.miracle.smart_ecommerce_api_v1.domain.BaseModel;
import com.miracle.smart_ecommerce_api_v1.domain.user.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Payment Method domain model (POJO) - represents payment_method table.
 * No JPA annotations - used with raw JDBC.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class PaymentMethod extends BaseModel {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Payment type is required")
    @Size(max = 50, message = "Payment type cannot exceed 50 characters")
    private String paymentType; // 'credit_card', 'debit_card', 'paypal', etc.

    @Size(max = 100, message = "Provider cannot exceed 100 characters")
    private String provider; // 'Visa', 'Mastercard', 'PayPal'

    @NotBlank(message = "Account number is required")
    private String accountNumber; // Should be encrypted/masked

    private LocalDate expiryDate;

    @Builder.Default
    private Boolean isDefault = false;

    // Transient field for user (populated when needed)
    private transient User user;

    /**
     * Get masked account number (show last 4 digits only)
     */
    public String getMaskedAccountNumber() {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        return "**** **** **** " + accountNumber.substring(accountNumber.length() - 4);
    }

    /**
     * Check if payment method is expired
     */
    public boolean isExpired() {
        if (expiryDate == null) {
            return false;
        }
        return expiryDate.isBefore(LocalDate.now());
    }

    /**
     * Get display name (e.g., "Visa ending in 4242")
     */
    public String getDisplayName() {
        String providerName = provider != null ? provider : paymentType;
        String lastFour = accountNumber != null && accountNumber.length() >= 4
                ? accountNumber.substring(accountNumber.length() - 4)
                : "****";
        return providerName + " ending in " + lastFour;
    }
}

