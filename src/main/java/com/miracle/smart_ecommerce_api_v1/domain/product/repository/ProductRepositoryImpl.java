package com.miracle.smart_ecommerce_api_v1.domain.product.repository;

import java.time.OffsetDateTime;

import com.miracle.smart_ecommerce_api_v1.common.util.JdbcUtils;
import com.miracle.smart_ecommerce_api_v1.domain.product.entity.Product;
import com.miracle.smart_ecommerce_api_v1.exception.ResourceNotFoundException;
import com.miracle.smart_ecommerce_api_v1.domain.product.mapper.ProductMapper;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC implementation of ProductRepository.
 * Uses JdbcTemplate for all database operations.
 */
@Repository
public class ProductRepositoryImpl implements ProductRepository {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;
    private final ProductMapper productRowMapper;

    public ProductRepositoryImpl(JdbcTemplate jdbcTemplate,
                                 NamedParameterJdbcTemplate namedJdbcTemplate,
                                 ProductMapper productRowMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbcTemplate = namedJdbcTemplate;
        this.productRowMapper = productRowMapper;
    }

    @Override
    @Transactional
    public Product save(Product product) {
        String sql = """
            INSERT INTO product (category_id, sku, name, description, price, stock_quantity, is_active, images, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?)
            RETURNING *
            """;

        OffsetDateTime now = OffsetDateTime.now();
        return jdbcTemplate.queryForObject(sql, productRowMapper,
                product.getCategoryId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity() != null ? product.getStockQuantity() : 0,
                product.getIsActive() != null ? product.getIsActive() : true,
                JdbcUtils.toJsonbArray(product.getImages()),
                Timestamp.valueOf(now.toLocalDateTime()),
                Timestamp.valueOf(now.toLocalDateTime())
        );
    }

    @Override
    @Transactional
    public Product update(Product product) {
        String sql = """
            UPDATE product 
            SET category_id = ?, sku = ?, name = ?, description = ?, price = ?, 
                stock_quantity = ?, is_active = ?, images = ?::jsonb, updated_at = ?
            WHERE id = ?
            RETURNING *
            """;

        try {
            return jdbcTemplate.queryForObject(sql, productRowMapper,
                    product.getCategoryId(),
                    product.getSku(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getStockQuantity(),
                    product.getIsActive(),
                    JdbcUtils.toJsonbArray(product.getImages()),
                    Timestamp.valueOf(OffsetDateTime.now().toLocalDateTime()),
                    product.getId()
            );
        } catch (EmptyResultDataAccessException e) {
            throw ResourceNotFoundException.forResource("Product", product.getId());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(UUID id) {
        String sql = "SELECT * FROM product WHERE id = ?";
        try {
            Product product = jdbcTemplate.queryForObject(sql, productRowMapper, id);
            return Optional.ofNullable(product);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findBySku(String sku) {
        String sql = "SELECT * FROM product WHERE sku = ?";
        try {
            Product product = jdbcTemplate.queryForObject(sql, productRowMapper, sku);
            return Optional.ofNullable(product);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        String sql = "SELECT * FROM product ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, productRowMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll(int page, int size) {
        JdbcUtils.validatePagination(page, size);
        String sql = "SELECT * FROM product ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, productRowMapper, size, JdbcUtils.calculateOffset(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findActiveProducts(int page, int size) {
        JdbcUtils.validatePagination(page, size);
        String sql = "SELECT * FROM product WHERE is_active = true ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, productRowMapper, size, JdbcUtils.calculateOffset(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByCategoryId(UUID categoryId, int page, int size) {
        JdbcUtils.validatePagination(page, size);
        String sql = "SELECT * FROM product WHERE category_id = ? AND is_active = true ORDER BY name LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, productRowMapper, categoryId, size, JdbcUtils.calculateOffset(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> search(String keyword, int page, int size) {
        JdbcUtils.validatePagination(page, size);
        String sql = """
            SELECT * FROM product 
            WHERE is_active = true AND (
                LOWER(name) LIKE LOWER(?) OR LOWER(description) LIKE LOWER(?)
            )
            ORDER BY name 
            LIMIT ? OFFSET ?
            """;
        String searchPattern = "%" + keyword + "%";
        return jdbcTemplate.query(sql, productRowMapper,
                searchPattern, searchPattern,
                size, JdbcUtils.calculateOffset(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        JdbcUtils.validatePagination(page, size);
        String sql = """
            SELECT * FROM product 
            WHERE is_active = true AND price BETWEEN ? AND ?
            ORDER BY price 
            LIMIT ? OFFSET ?
            """;
        return jdbcTemplate.query(sql, productRowMapper,
                minPrice, maxPrice,
                size, JdbcUtils.calculateOffset(page, size));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findInStock(int page, int size) {
        JdbcUtils.validatePagination(page, size);
        String sql = "SELECT * FROM product WHERE is_active = true AND stock_quantity > 0 ORDER BY name LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, productRowMapper, size, JdbcUtils.calculateOffset(page, size));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        String sql = "DELETE FROM product WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw ResourceNotFoundException.forResource("Product", id);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        String sql = "SELECT COUNT(*) FROM product WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsBySku(String sku) {
        String sql = "SELECT COUNT(*) FROM product WHERE sku = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, sku);
        return count != null && count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        String sql = "SELECT COUNT(*) FROM product";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long countActive() {
        String sql = "SELECT COUNT(*) FROM product WHERE is_active = true";
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }

    @Override
    @Transactional(readOnly = true)
    public long countByCategoryId(UUID categoryId) {
        String sql = "SELECT COUNT(*) FROM product WHERE category_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, categoryId);
        return count != null ? count : 0;
    }

    @Override
    @Transactional
    public void updateStock(UUID productId, int quantity) {
        String sql = "UPDATE product SET stock_quantity = stock_quantity + ?, updated_at = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, quantity, Timestamp.valueOf(OffsetDateTime.now().toLocalDateTime()), productId);
        if (rowsAffected == 0) {
            throw ResourceNotFoundException.forResource("Product", productId);
        }
    }

    @Override
    @Transactional
    public void setActiveStatus(UUID id, boolean isActive) {
        String sql = "UPDATE product SET is_active = ?, updated_at = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, isActive, Timestamp.valueOf(OffsetDateTime.now().toLocalDateTime()), id);
        if (rowsAffected == 0) {
            throw ResourceNotFoundException.forResource("Product", id);
        }
    }

    @Override
    @Transactional
    public int[] batchInsert(List<Product> products) {
        String sql = """
            INSERT INTO product (category_id, sku, name, description, price, stock_quantity, is_active, images, created_at, updated_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?, ?)
            """;

        OffsetDateTime now = OffsetDateTime.now();
        return jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Product product = products.get(i);
                ps.setObject(1, product.getCategoryId());
                ps.setString(2, product.getSku());
                ps.setString(3, product.getName());
                ps.setString(4, product.getDescription());
                ps.setBigDecimal(5, product.getPrice());
                ps.setInt(6, product.getStockQuantity() != null ? product.getStockQuantity() : 0);
                ps.setBoolean(7, product.getIsActive() != null ? product.getIsActive() : true);
                ps.setString(8, JdbcUtils.toJsonbArray(product.getImages()));
                ps.setTimestamp(9, Timestamp.valueOf(now.toLocalDateTime()));
                ps.setTimestamp(10, Timestamp.valueOf(now.toLocalDateTime()));
            }

            @Override
            public int getBatchSize() {
                return products.size();
            }
        });
    }
}

