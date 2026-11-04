package com.backendlab.product_service.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backendlab.product_service.model.entity.Category;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsBySlug(String slug);

    boolean existsByParentId(UUID parentId);

    Optional<Category> findBySlug(String slug);
}
