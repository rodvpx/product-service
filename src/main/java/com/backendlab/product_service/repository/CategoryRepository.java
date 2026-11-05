package com.backendlab.product_service.repository;

import com.backendlab.product_service.entity.Category;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

  boolean existsBySlug(String slug);

  boolean existsByParentId(UUID parentId);

  Optional<Category> findBySlug(String slug);
}
