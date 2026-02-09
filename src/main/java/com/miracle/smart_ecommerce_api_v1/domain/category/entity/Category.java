package com.miracle.smart_ecommerce_api_v1.domain.category.entity;

import com.miracle.smart_ecommerce_api_v1.domain.BaseModel;
import com.miracle.smart_ecommerce_api_v1.domain.product.entity.Product;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Product Category domain model (POJO) - represents product_category table.
 * Supports hierarchical categories with parent-child relationships.
 * No JPA annotations - used with raw JDBC.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseModel {

    private UUID parentCategoryId;

    @NotBlank(message = "Category name is required")
    @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
    private String categoryName;

    // Transient fields for relationships (populated when needed)
    private transient Category parentCategory;

    @Builder.Default
    private transient List<Category> subCategories = new ArrayList<>();

    @Builder.Default
    private transient List<Product> products = new ArrayList<>();

    /**
     * Check if this is a root category (no parent)
     */
    public boolean isRootCategory() {
        return parentCategoryId == null;
    }

    /**
     * Check if this category has subcategories
     */
    public boolean hasSubCategories() {
        return subCategories != null && !subCategories.isEmpty();
    }

    /**
     * Get the category path (e.g., "Electronics > Phones > Smartphones")
     */
    public String getCategoryPath() {
        if (parentCategory == null) {
            return categoryName;
        }
        return parentCategory.getCategoryPath() + " > " + categoryName;
    }
}

