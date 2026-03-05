package com.qridaba.qridabaplatform.service.category;

import com.qridaba.qridabaplatform.exception.DuplicateResourceException;
import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.mapper.CategoryMapper;
import com.qridaba.qridabaplatform.model.dto.request.CategoryRequest;
import com.qridaba.qridabaplatform.model.dto.response.CategoryResponse;
import com.qridaba.qridabaplatform.model.entity.item.Category;
import com.qridaba.qridabaplatform.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryServiceImp implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Category already exists with name: " + request.getName());
        }
        Category category = categoryMapper.toEntity(request);
        Category saved = categoryRepository.save(category);
        return categoryMapper.toResponse(saved);
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        categoryRepository.findByName(request.getName())
                .filter(c -> !c.getId().equals(id))
                .ifPresent(c -> {
                    throw new DuplicateResourceException("Category already exists with name: " + request.getName());
                });

        categoryMapper.updateEntityFromRequest(request, category);
        Category updated = categoryRepository.save(category);
        return categoryMapper.toResponse(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public List<CategoryResponse> searchCategoriesByName(String name) {
        return categoryRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .map(categoryMapper::toResponse)
                .toList();
    }
}
