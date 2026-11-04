package com.backendlab.product_service.repository;

import com.backendlab.product_service.entity.Product;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductRepository
    extends JpaRepository<Product, UUID>, JpaSpecificationExecutor<Product> {

  boolean existsBySku(String sku);

  boolean existsByCategoryId(UUID category);

  Optional<Product> findByIdAndActiveTrue(UUID id);
}
