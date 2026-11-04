package com.backendlab.product_service.dto.request;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(
    @NotBlank @Size(max = 50) String sku,
    @NotBlank @Size(max = 150) String name,
    String description,
    @NotNull @Positive @Digits(integer = 10, fraction = 2) BigDecimal price,
    @NotNull UUID categoryId) {}
