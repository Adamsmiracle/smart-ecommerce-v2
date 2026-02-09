package com.miracle.smart_ecommerce_api_v1.domain.category.service;

import com.miracle.smart_ecommerce_api_v1.domain.category.entity.Category;
import com.miracle.smart_ecommerce_api_v1.domain.category.dto.CreateCategoryRequest;
import com.miracle.smart_ecommerce_api_v1.domain.category.dto.CategoryResponse;
import com.miracle.smart_ecommerce_api_v1.exception.BadRequestException;
import com.miracle.smart_ecommerce_api_v1.exception.DuplicateResourceException;
import com.miracle.smart_ecommerce_api_v1.exception.ResourceNotFoundException;
import com.miracle.smart_ecommerce_api_v1.domain.category.repository.CategoryRepository;
import com.miracle.smart_ecommerce_api_v1.domain.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.miracle.smart_ecommerce_api_v1.config.CacheConfig.*;

/**
 * Implementation of CategoryService using raw JDBC.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CacheManager cacheManager;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating category: {}", request.getCategoryName());

        // Check if category name already exists
        if (categoryRepository.existsByName(request.getCategoryName())) {
            throw new DuplicateResourceException("Category", "name", request.getCategoryName());
        }

        // Validate parent category if provided
        if (request.getParentCategoryId() != null && !categoryRepository.existsById(request.getParentCategoryId())) {
            throw ResourceNotFoundException.forResource("Parent Category", request.getParentCategoryId());
        }

        Category category = Category.builder()
                .parentCategoryId(request.getParentCategoryId())
                .categoryName(request.getCategoryName())
                .build();

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());

        CategoryResponse response = mapToResponse(savedCategory);

        // Update cache with new category
        Cache byIdCache = cacheManager.getCache(CATEGORIES_CACHE);
        if (byIdCache != null) {
            byIdCache.put("id:" + savedCategory.getId(), response);
        }

        // Clear list cache
        evictCache(CATEGORIES_CACHE);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = CATEGORIES_CACHE, key = "'id:' + #id")
    public CategoryResponse getCategoryById(UUID id) {
        log.debug("Getting category by ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Category", id));
        return mapToResponseWithDetails(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.debug("Getting all categories");
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        log.debug("Getting root categories");
        return categoryRepository.findRootCategories().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        log.debug("Getting category tree");
        List<Category> rootCategories = categoryRepository.findRootCategories();
        return rootCategories.stream()
                .map(this::buildCategoryTree)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubcategories(UUID parentId) {
        log.debug("Getting subcategories for parent: {}", parentId);

        if (!categoryRepository.existsById(parentId)) {
            throw ResourceNotFoundException.forResource("Category", parentId);
        }

        return categoryRepository.findByParentId(parentId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, CreateCategoryRequest request) {
        log.info("Updating category with ID: {}", id);

        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.forResource("Category", id));

        // Check if name is being changed to an existing one
        if (!existingCategory.getCategoryName().equals(request.getCategoryName())
                && categoryRepository.existsByName(request.getCategoryName())) {
            throw new DuplicateResourceException("Category", "name", request.getCategoryName());
        }

        // Prevent circular reference
        if (request.getParentCategoryId() != null) {
            if (request.getParentCategoryId().equals(id)) {
                throw new BadRequestException("Category cannot be its own parent");
            }
            if (!categoryRepository.existsById(request.getParentCategoryId())) {
                throw ResourceNotFoundException.forResource("Parent Category", request.getParentCategoryId());
            }
        }

        existingCategory.setParentCategoryId(request.getParentCategoryId());
        existingCategory.setCategoryName(request.getCategoryName());

        Category updatedCategory = categoryRepository.update(existingCategory);
        log.info("Category updated successfully: {}", id);

        CategoryResponse response = mapToResponse(updatedCategory);

        // Update id cache
        Cache byIdCache = cacheManager.getCache(CATEGORIES_CACHE);
        if (byIdCache != null) {
            byIdCache.put("id:" + id, response);
        }

        // Clear list cache
        evictCache(CATEGORIES_CACHE);

        return response;
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        log.info("Deleting category with ID: {}", id);

        if (!categoryRepository.existsById(id)) {
            throw ResourceNotFoundException.forResource("Category", id);
        }

        // Check if category has products
        if (productRepository.countByCategoryId(id) > 0) {
            throw new BadRequestException("Cannot delete category with associated products");
        }

        // Check if category has subcategories
        if (categoryRepository.countByParentId(id) > 0) {
            throw new BadRequestException("Cannot delete category with subcategories");
        }

        categoryRepository.deleteById(id);
        log.info("Category deleted successfully: {}", id);

        // Evict from id cache
        Cache byIdCache = cacheManager.getCache(CATEGORIES_CACHE);
        if (byIdCache != null) {
            byIdCache.evict("id:" + id);
        }

        // Clear list cache
        evictCache(CATEGORIES_CACHE);
    }

    @Override
    @Transactional(readOnly = true)
    public long countCategories() {
        return categoryRepository.count();
    }

    // ========================================================================
    // Helper Methods
    // ========================================================================

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .parentCategoryId(category.getParentCategoryId())
                .categoryName(category.getCategoryName())
                .productCount(productRepository.countByCategoryId(category.getId()))
                .build();
    }

    private CategoryResponse mapToResponseWithDetails(Category category) {
        CategoryResponse response = mapToResponse(category);

        // Add parent category name if exists
        if (category.getParentCategoryId() != null) {
            categoryRepository.findById(category.getParentCategoryId())
                    .ifPresent(parent -> response.setParentCategoryName(parent.getCategoryName()));
        }

        // Add subcategories
        List<CategoryResponse> subCategories = categoryRepository.findByParentId(category.getId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        response.setSubCategories(subCategories);

        return response;
    }

    private CategoryResponse buildCategoryTree(Category category) {
        CategoryResponse response = mapToResponse(category);

        List<Category> children = categoryRepository.findByParentId(category.getId());
        if (!children.isEmpty()) {
            List<CategoryResponse> childResponses = children.stream()
                    .map(this::buildCategoryTree)
                    .collect(Collectors.toList());
            response.setSubCategories(childResponses);
        } else {
            response.setSubCategories(new ArrayList<>());
        }

        return response;
    }

    /**
     * Helper method to evict all entries from a cache
     */
    private void evictCache(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }
}

