package com.qridaba.qridabaplatform.mapper;

import com.qridaba.qridabaplatform.model.dto.request.CategoryRequest;
import com.qridaba.qridabaplatform.model.dto.response.CategoryResponse;
import com.qridaba.qridabaplatform.model.entity.item.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
    Category toEntity(CategoryRequest request);
    CategoryResponse toResponse(Category category);
    void updateEntityFromRequest(CategoryRequest request, @MappingTarget Category category);
}
