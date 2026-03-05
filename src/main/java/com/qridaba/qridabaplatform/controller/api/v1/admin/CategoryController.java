package com.qridaba.qridabaplatform.controller.api.v1.admin;

import com.qridaba.qridabaplatform.model.dto.request.CategoryRequest;
import com.qridaba.qridabaplatform.model.dto.response.CategoryResponse;
import com.qridaba.qridabaplatform.payload.ApiResponse;
import com.qridaba.qridabaplatform.payload.ResponseBuilder;
import com.qridaba.qridabaplatform.service.category.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
@Tag(name = "Category Management", description = "Endpoints for managing categories (Admin only)")
public class CategoryController {

    private final ICategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Create a new category")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseBuilder.created("Category created successfully", response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable UUID id) {
        return ResponseBuilder.success("Category fetched successfully", categoryService.getCategoryById(id));
    }

    @GetMapping
    @Operation(summary = "Get all categories")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseBuilder.success("Categories fetched successfully", categoryService.getAllCategories());
    }

    @GetMapping("/search")
    @Operation(summary = "Search categories by name")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> searchCategories(@RequestParam String name) {
        return ResponseBuilder.success("Categories fetched successfully", categoryService.searchCategoriesByName(name));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Update an existing category")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseBuilder.success("Category updated successfully", categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Delete a category")
    public ResponseEntity<ApiResponse<Object>> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseBuilder.success("Category deleted successfully", null);
    }
}
