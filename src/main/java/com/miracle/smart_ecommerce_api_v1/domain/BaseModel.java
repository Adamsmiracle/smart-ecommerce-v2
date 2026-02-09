package com.miracle.smart_ecommerce_api_v1.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Generated;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Base domain model with common fields.
 * All domain models extend this class.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class BaseModel {


    protected UUID id;
    protected OffsetDateTime createdAt;
    protected OffsetDateTime updatedAt;

    /**
     * Check if this is a new (unsaved) entity
     */
    public boolean isNew() {
        return id == null;
    }
}

