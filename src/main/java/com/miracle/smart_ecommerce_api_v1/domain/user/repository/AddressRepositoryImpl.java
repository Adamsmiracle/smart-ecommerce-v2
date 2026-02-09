package com.miracle.smart_ecommerce_api_v1.domain.user.repository;

import java.time.OffsetDateTime;

import com.miracle.smart_ecommerce_api_v1.domain.user.entity.Address;
import com.miracle.smart_ecommerce_api_v1.domain.user.mapper.AddressMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JDBC implementation of AddressRepository.
 */
@Repository
@RequiredArgsConstructor
public class AddressRepositoryImpl implements AddressRepository {

    private final JdbcTemplate jdbcTemplate;
    private final AddressMapper addressMapper;

    @Override
    @Transactional
    public Address save(Address address) {
        String sql = """
            INSERT INTO address (user_id, address_line, city, region, country, postal_code, is_default, address_type, created_at)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            RETURNING id, user_id, address_line, city, region, country, postal_code, is_default, address_type, created_at
            """;

        OffsetDateTime now = OffsetDateTime.now();
        return jdbcTemplate.queryForObject(sql, addressMapper,
                address.getUserId(),
                address.getAddressLine(),
                address.getCity(),
                address.getRegion(),
                address.getCountry(),
                address.getPostalCode(),
                address.getIsDefault() != null ? address.getIsDefault() : false,
                address.getAddressType() != null ? address.getAddressType() : "shipping",
                Timestamp.valueOf(now.toLocalDateTime())
        );
    }

    @Override
    @Transactional
    public Address update(Address address) {
        String sql = """
            UPDATE address 
            SET address_line = ?, city = ?, region = ?, country = ?, postal_code = ?, is_default = ?, address_type = ?
            WHERE id = ?
            RETURNING id, user_id, address_line, city, region, country, postal_code, is_default, address_type, created_at
            """;

        return jdbcTemplate.queryForObject(sql, addressMapper,
                address.getAddressLine(),
                address.getCity(),
                address.getRegion(),
                address.getCountry(),
                address.getPostalCode(),
                address.getIsDefault(),
                address.getAddressType(),
                address.getId()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Address> findById(UUID id) {
        String sql = "SELECT * FROM address WHERE id = ?";
        try {
            Address address = jdbcTemplate.queryForObject(sql, addressMapper, id);
            return Optional.ofNullable(address);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> findAll() {
        String sql = "SELECT * FROM address ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, addressMapper);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> findByUserId(UUID userId) {
        String sql = "SELECT * FROM address WHERE user_id = ? ORDER BY is_default DESC, created_at DESC";
        return jdbcTemplate.query(sql, addressMapper, userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Address> findByUserIdAndType(UUID userId, String addressType) {
        String sql = "SELECT * FROM address WHERE user_id = ? AND address_type = ? ORDER BY is_default DESC, created_at DESC";
        return jdbcTemplate.query(sql, addressMapper, userId, addressType);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Address> findDefaultByUserId(UUID userId) {
        String sql = "SELECT * FROM address WHERE user_id = ? AND is_default = true LIMIT 1";
        try {
            Address address = jdbcTemplate.queryForObject(sql, addressMapper, userId);
            return Optional.ofNullable(address);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(UUID id) {
        String sql = "SELECT COUNT(*) FROM address WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id);
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        String sql = "DELETE FROM address WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    @Transactional
    public void clearDefaultForUserAndType(UUID userId, String addressType) {
        String sql = "UPDATE address SET is_default = false WHERE user_id = ? AND address_type = ?";
        jdbcTemplate.update(sql, userId, addressType);
    }
}

