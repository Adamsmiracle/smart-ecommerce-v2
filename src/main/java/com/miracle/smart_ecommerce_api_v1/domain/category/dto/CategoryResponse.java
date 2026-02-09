package com.miracle.smart_ecommerce_api_v1.domain.category.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for Category.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

    private UUID id;
    private UUID parentCategoryId;
    private String categoryName;
    private String parentCategoryName;
    private List<CategoryResponse> subCategories;
    private Long productCount;
}

