package com.miracle.smart_ecommerce_api_v1.service;

import com.miracle.smart_ecommerce_api_v1.dto.request.CreateCategoryRequest;
import com.miracle.smart_ecommerce_api_v1.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

/**
 * Service interface for Category operations.
 */
public interface CategoryService {

    /**
     * Create a new category
     */
    CategoryResponse createCategory(CreateCategoryRequest request);

    /**
     * Get category by ID
     */
    CategoryResponse getCategoryById(UUID id);

    /**
     * Get all categories
     */
    List<CategoryResponse> getAllCategories();

    /**
     * Get root categories (no parent)
     */
    List<CategoryResponse> getRootCategories();

    /**
     * Get category tree (hierarchical structure)
     */
    List<CategoryResponse> getCategoryTree();

    /**
     * Get subcategories by parent ID
     */
    List<CategoryResponse> getSubcategories(UUID parentId);

    /**
     * Update category
     */
    CategoryResponse updateCategory(UUID id, CreateCategoryRequest request);

    /**
     * Delete category
     */
    void deleteCategory(UUID id);

    /**
     * Count total categories
     */
    long countCategories();
}

