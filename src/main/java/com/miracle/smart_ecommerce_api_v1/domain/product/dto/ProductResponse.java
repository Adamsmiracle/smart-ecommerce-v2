package com.miracle.smart_ecommerce_api_v1.domain.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for Product.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private Boolean isActive;
    private Boolean inStock;
    private List<String> images;
    private String primaryImage;
    private Double averageRating;
    private Integer reviewCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}

