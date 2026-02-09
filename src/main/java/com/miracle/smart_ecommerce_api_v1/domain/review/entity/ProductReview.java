package com.miracle.smart_ecommerce_api_v1.domain.review.entity;

import com.miracle.smart_ecommerce_api_v1.domain.BaseModel;
import com.miracle.smart_ecommerce_api_v1.domain.order.entity.OrderItem;
import com.miracle.smart_ecommerce_api_v1.domain.product.entity.Product;
import com.miracle.smart_ecommerce_api_v1.domain.user.entity.User;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * Product Review domain model (POJO) - represents product_review table.
 * No JPA annotations - used with raw JDBC.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ProductReview extends BaseModel {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotNull(message = "Product ID is required")
    private UUID productId;

    private UUID orderItemId; // Optional: link to verify purchase

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    private Integer rating;

    @Size(max = 200, message = "Title cannot exceed 200 characters")
    private String title;

    @Size(max = 2000, message = "Comment cannot exceed 2000 characters")
    private String comment;

    @Builder.Default
    private Boolean isVerifiedPurchase = false;

    @Builder.Default
    private Boolean isApproved = true;

    // Transient fields for relationships (populated when needed)
    private transient User user;
    private transient Product product;
    private transient OrderItem orderItem;

    /**
     * Get star rating as string (e.g., "★★★★☆")
     */
    public String getStarRating() {
        if (rating == null) return "";
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            stars.append(i < rating ? "★" : "☆");
        }
        return stars.toString();
    }

    /**
     * Get reviewer display name
     */
    public String getReviewerName() {
        if (user == null) {
            return "Anonymous";
        }
        String fullName = user.getFullName();
        return fullName != null ? fullName : "Anonymous";
    }
}

