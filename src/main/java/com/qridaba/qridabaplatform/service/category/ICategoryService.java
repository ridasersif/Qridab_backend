package com.qridaba.qridabaplatform.service.category;

import com.qridaba.qridabaplatform.model.dto.request.CategoryRequest;
import com.qridaba.qridabaplatform.model.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface ICategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse getCategoryById(UUID id);
    List<CategoryResponse> getAllCategories();
    CategoryResponse updateCategory(UUID id, CategoryRequest request);
    void deleteCategory(UUID id);
    List<CategoryResponse> searchCategoriesByName(String name);
}
