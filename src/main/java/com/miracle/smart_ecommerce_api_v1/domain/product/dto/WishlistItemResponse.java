package com.miracle.smart_ecommerce_api_v1.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Response DTO for Wishlist Item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemResponse {

    private UUID id;
    private UUID userId;
    private UUID productId;
    private String productName;
    private String productSku;
    private BigDecimal productPrice;
    private String productImage;
    private Boolean productInStock;
    private OffsetDateTime createdAt;
}

