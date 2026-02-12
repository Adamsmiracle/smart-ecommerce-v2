package com.miracle.smart_ecommerce_api_v1.domain.category.mapper;

import com.miracle.smart_ecommerce_api_v1.common.util.JdbcUtils;
import com.miracle.smart_ecommerce_api_v1.domain.category.entity.Category;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RowMapper for Category domain model.
 * Maps ResultSet rows to Category objects.
 */
@Component
public class CategoryMapper implements RowMapper<Category> {

    @Override
    public Category mapRow(ResultSet rs, int rowNum) throws SQLException {
        return Category.builder()
                .id(JdbcUtils.getUUID(rs, "id"))
                .categoryName(rs.getString("category_name"))
                .build();
    }
}

