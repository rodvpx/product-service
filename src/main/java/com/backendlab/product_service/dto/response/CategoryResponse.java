package com.backendlab.product_service.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record CategoryResponse(
    UUID id, String name, String slug, UUID parentId, LocalDateTime createdAt) {}
