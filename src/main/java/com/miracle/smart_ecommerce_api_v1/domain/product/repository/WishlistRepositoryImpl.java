package com.miracle.smart_ecommerce_api_v1.domain.product.repository;

import com.miracle.smart_ecommerce_api_v1.domain.product.entity.WishlistItem;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC implementation of WishlistRepository.
 */
@Repository
public class WishlistRepositoryImpl implements WishlistRepository {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<WishlistItem> wishlistItemRowMapper;

    public WishlistRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.wishlistItemRowMapper = new WishlistItemRowMapper();
    }

    @Override
    @Transactional
    public WishlistItem save(WishlistItem item) {
        String sql = """
            INSERT INTO wishlist_item (user_id, product_id, created_at)
            VALUES (?, ?, ?)
            RETURNING id, user_id, product_id, created_at
            """;

        OffsetDateTime now = OffsetDateTime.now();
        return jdbcTemplate.queryForObject(sql, wishlistItemRowMapper,
                item.getUserId(),
                item.getProductId(),
                Timestamp.valueOf(now.toLocalDateTime())
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WishlistItem> findById(UUID id) {
        String sql = "SELECT * FROM wishlist_item WHERE id = ?";
        try {
            WishlistItem item = jdbcTemplate.queryForObject(sql, wishlistItemRowMapper, id);
            return Optional.ofNullable(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistItem> findByUserId(UUID userId) {
        String sql = "SELECT * FROM wishlist_item WHERE user_id = ? ORDER BY created_at DESC";
        System.out.println("Executing SQL: " + sql + " with userId: " + userId);
        List<WishlistItem> items = jdbcTemplate.query(sql, wishlistItemRowMapper, userId);
        System.out.println("Found " + items.size() + " items for userId: " + userId);
        return items;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WishlistItem> findByUserId(UUID userId, int page, int size) {
        String sql = "SELECT * FROM wishlist_item WHERE user_id = ? ORDER BY created_at DESC LIMIT ? OFFSET ?";
        return jdbcTemplate.query(sql, wishlistItemRowMapper, userId, size, page * size);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<WishlistItem> findByUserIdAndProductId(UUID userId, UUID productId) {
        String sql = "SELECT * FROM wishlist_item WHERE user_id = ? AND product_id = ?";
        try {
            WishlistItem item = jdbcTemplate.queryForObject(sql, wishlistItemRowMapper, userId, productId);
            return Optional.ofNullable(item);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        String sql = "SELECT COUNT(*) FROM wishlist_item WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUserIdAndProductId(UUID userId, UUID productId) {
        String sql = "SELECT COUNT(*) FROM wishlist_item WHERE user_id = ? AND product_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, productId);
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        String sql = "DELETE FROM wishlist_item WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    @Transactional
    public void deleteByUserIdAndProductId(UUID userId, UUID productId) {
        String sql = "DELETE FROM wishlist_item WHERE user_id = ? AND product_id = ?";
        jdbcTemplate.update(sql, userId, productId);
    }

    @Override
    @Transactional
    public void deleteByUserId(UUID userId) {
        String sql = "DELETE FROM wishlist_item WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUserId(UUID userId) {
        String sql = "SELECT COUNT(*) FROM wishlist_item WHERE user_id = ?";
        Long count = jdbcTemplate.queryForObject(sql, Long.class, userId);
        return count != null ? count : 0;
    }

    /**
     * RowMapper for WishlistItem
     */
    private static class WishlistItemRowMapper implements RowMapper<WishlistItem> {
        @Override
        public WishlistItem mapRow(ResultSet rs, int rowNum) throws SQLException {
            WishlistItem item = WishlistItem.builder()
                    .id(rs.getObject("id", UUID.class))
                    .userId(rs.getObject("user_id", UUID.class))
                    .productId(rs.getObject("product_id", UUID.class))
                    .createdAt(rs.getObject("created_at", OffsetDateTime.class))
                    .build();
            System.out.println("Mapped wishlist item: " + item.getId() + " for user: " + item.getUserId());
            return item;
        }
    }
}
