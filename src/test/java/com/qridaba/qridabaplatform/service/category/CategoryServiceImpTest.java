package com.qridaba.qridabaplatform.service.category;

import com.qridaba.qridabaplatform.exception.DuplicateResourceException;
import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.mapper.CategoryMapper;
import com.qridaba.qridabaplatform.model.dto.request.CategoryRequest;
import com.qridaba.qridabaplatform.model.dto.response.CategoryResponse;
import com.qridaba.qridabaplatform.model.entity.item.Category;
import com.qridaba.qridabaplatform.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImpTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryServiceImp categoryService;

    private Category category;
    private CategoryRequest categoryRequest;
    private CategoryResponse categoryResponse;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        category = new Category();
        category.setId(categoryId);
        category.setName("Electronics");

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("Electronics");

        categoryResponse = new CategoryResponse();
        categoryResponse.setId(categoryId);
        categoryResponse.setName("Electronics");
    }

    @Test
    void createCategory_WhenNameDoesNotExist_ShouldReturnCategoryResponse() {
        // Arrange
        when(categoryRepository.existsByName(categoryRequest.getName())).thenReturn(false);
        when(categoryMapper.toEntity(categoryRequest)).thenReturn(category);
        when(categoryRepository.save(any(Category.class))).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // Act
        CategoryResponse result = categoryService.createCategory(categoryRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(categoryRequest.getName());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_WhenNameExists_ShouldThrowDuplicateResourceException() {
        // Arrange
        when(categoryRepository.existsByName(categoryRequest.getName())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.createCategory(categoryRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Category already exists with name");
        
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategoryById_WhenIdExists_ShouldReturnCategoryResponse() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // Act
        CategoryResponse result = categoryService.getCategoryById(categoryId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
    }

    @Test
    void getCategoryById_WhenIdDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> categoryService.getCategoryById(categoryId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Category not found");
    }

    @Test
    void getAllCategories_ShouldReturnListOfCategoryResponse() {
        // Arrange
        when(categoryRepository.findAll()).thenReturn(List.of(category));
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // Act
        List<CategoryResponse> result = categoryService.getAllCategories();

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
    }

    @Test
    void updateCategory_WhenIdExistsAndNameIsUnique_ShouldReturnUpdatedResponse() {
        // Arrange
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(categoryRepository.findByName(categoryRequest.getName())).thenReturn(Optional.empty());
        when(categoryRepository.save(category)).thenReturn(category);
        when(categoryMapper.toResponse(category)).thenReturn(categoryResponse);

        // Act
        CategoryResponse result = categoryService.updateCategory(categoryId, categoryRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(categoryMapper).updateEntityFromRequest(categoryRequest, category);
        verify(categoryRepository).save(category);
    }

    @Test
    void deleteCategory_WhenIdExists_ShouldCallDelete() {
        // Arrange
        when(categoryRepository.existsById(categoryId)).thenReturn(true);

        // Act
        categoryService.deleteCategory(categoryId);

        // Assert
        verify(categoryRepository).deleteById(categoryId);
    }

    @Test
    void deleteCategory_WhenIdDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        when(categoryRepository.existsById(categoryId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> categoryService.deleteCategory(categoryId))
                .isInstanceOf(ResourceNotFoundException.class);
        
        verify(categoryRepository, never()).deleteById(any());
    }
}
