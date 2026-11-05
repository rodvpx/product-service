package com.backendlab.product_service.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    String sku,
    String name,
    String description,
    BigDecimal price,
    CategoryResponse category,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {}
