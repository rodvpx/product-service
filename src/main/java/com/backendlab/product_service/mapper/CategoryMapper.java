package com.backendlab.product_service.mapper;

import com.backendlab.product_service.dto.request.CategoryRequest;
import com.backendlab.product_service.dto.response.CategoryResponse;
import com.backendlab.product_service.entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "slug", ignore = true)
  @Mapping(target = "parent", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  Category toEntity(CategoryRequest request);

  @Mapping(target = "parentId", source = "parent.id")
  CategoryResponse toResponse(Category category);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "slug", ignore = true)
  @Mapping(target = "parent", ignore = true)
  @Mapping(target = "createdAt", ignore = true)
  void updateEntity(CategoryRequest request, @MappingTarget Category category);
}
