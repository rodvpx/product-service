package com.backendlab.product_service.repository.specification;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.backendlab.product_service.model.entity.Product;

public final class ProductSpecifications {

    private ProductSpecifications() {
    }

    public static Specification<Product> isActive() {
        return (root, query, cb) -> cb.isTrue(root.get("active"));
    }

    public static Specification<Product> nameContains(String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank())
                return cb.conjunction();
            return cb.like(cb.lower(root.get("name")),
                    "%" + name.trim().toLowerCase() + "%");
        };
    }

    public static Specification<Product> inCategory(UUID categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null)
                return cb.conjunction();
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Product> priceAtLeast(BigDecimal minPrice) {
        return (root, query, cb) -> {
            if (minPrice == null)
                return cb.conjunction();
            return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
        };
    }

    public static Specification<Product> priceAtMost(BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (maxPrice == null)
                return cb.conjunction();
            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }
}
