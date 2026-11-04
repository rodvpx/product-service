package com.backendlab.product_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CategoryRequest(@NotBlank @Size(max = 100) String name, UUID parentId) {}
