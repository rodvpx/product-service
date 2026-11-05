package com.backendlab.product_service.mapper;

import com.backendlab.product_service.dto.request.ProductRequest;
import com.backendlab.product_service.dto.request.ProductUpdateRequest;
import com.backendlab.product_service.dto.response.ProductResponse;
import com.backendlab.product_service.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = CategoryMapper.class)
public interface ProductMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "category", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  Product toEntity(ProductRequest request);

  ProductResponse toResponse(Product product);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "sku", ignore = true)
  @Mapping(target = "category", ignore = true)
  @Mapping(target = "active", ignore = true)
  @Mapping(target = "createdBy", ignore = true)
  @Mapping(target = "version", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  @Mapping(target = "updatedAt", ignore = true)
  void updateEntity(ProductUpdateRequest request, @MappingTarget Product product);
}
