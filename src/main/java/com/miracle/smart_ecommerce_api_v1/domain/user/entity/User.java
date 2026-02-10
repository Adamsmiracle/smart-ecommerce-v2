package com.miracle.smart_ecommerce_api_v1.domain.user.entity;

import com.miracle.smart_ecommerce_api_v1.domain.BaseModel;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * User domain model (POJO) - represents app_user table.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class User extends BaseModel {

    @NotBlank(message = "Email address is required")
    @Email(message = "Email should be valid")
    @Size(max = 255, message = "Email cannot exceed 255 characters")
    private String emailAddress;

    @Size(max = 100, message = "First name cannot exceed 100 characters")
    private String firstName;

    @Size(max = 100, message = "Last name cannot exceed 100 characters")
    private String lastName;

    @Size(max = 20, message = "Phone number cannot exceed 20 characters")
    private String phoneNumber;

    @NotBlank(message = "Password is required")
    private String passwordHash;

    private Boolean isActive;

    // Roles stored as a set of strings, e.g. ROLE_USER, ROLE_ADMIN
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    public boolean isAdmin() {
        return roles != null && roles.contains("ROLE_ADMIN");
    }

    /**
     * Get user's full name
     */
    public String getFullName() {
        if (firstName == null && lastName == null) {
            return null;
        }
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }
}
