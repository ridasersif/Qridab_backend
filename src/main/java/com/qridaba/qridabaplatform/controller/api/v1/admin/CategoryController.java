package com.qridaba.qridabaplatform.controller.api.v1.admin;

import com.qridaba.qridabaplatform.model.dto.request.CategoryRequest;
import com.qridaba.qridabaplatform.model.dto.response.CategoryResponse;
import com.qridaba.qridabaplatform.payload.ApiResponse;
import com.qridaba.qridabaplatform.payload.ResponseBuilder;
import com.qridaba.qridabaplatform.service.category.ICategoryService;
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
    @Operation(summary = "Create a new category", description = "Creates a new category in the system. Requires Admin or Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Category created successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid category data")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseBuilder.created("Category created successfully", response);
    }
 
    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID", description = "Retrieves detailed information for a specific category by its unique identifier.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category fetched successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable UUID id) {
        return ResponseBuilder.success("Category fetched successfully", categoryService.getCategoryById(id));
    }
 
    @GetMapping
    @Operation(summary = "Get all categories", description = "Retrieves a list of all available categories.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categories fetched successfully")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseBuilder.success("Categories fetched successfully", categoryService.getAllCategories());
    }
 
    @GetMapping("/search")
    @Operation(summary = "Search categories by name", description = "Retrieves a list of categories whose names contain the specified search string.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Categories fetched successfully")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> searchCategories(@RequestParam String name) {
        return ResponseBuilder.success("Categories fetched successfully", categoryService.searchCategoriesByName(name));
    }
 
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Update an existing category", description = "Updates the details of an existing category. Requires Admin or Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category updated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseBuilder.success("Category updated successfully", categoryService.updateCategory(id, request));
    }
 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Delete a category", description = "Permanently removes a category from the system. Requires Admin or Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category deleted successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<ApiResponse<Object>> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseBuilder.success("Category deleted successfully", null);
    }
}
