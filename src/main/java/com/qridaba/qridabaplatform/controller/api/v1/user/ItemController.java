package com.qridaba.qridabaplatform.controller.api.v1.user;

import com.qridaba.qridabaplatform.model.dto.request.ItemRequest;
import com.qridaba.qridabaplatform.model.dto.response.ItemResponse;
import com.qridaba.qridabaplatform.payload.ApiResponse;
import com.qridaba.qridabaplatform.payload.ResponseBuilder;
import com.qridaba.qridabaplatform.service.item.IItemService;
import com.qridaba.qridabaplatform.model.entity.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Tag(name = "Item Management", description = "Endpoints for managing items (Owner based)")
public class ItemController {
 
    private final IItemService itemService;
 
    @PostMapping
    @PreAuthorize("hasAuthority('OWNER')")
    @Operation(summary = "Create a new item", description = "Creates a new item and assigns the current authenticated user as the owner. Requires Owner authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Item created successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid item data")
    public ResponseEntity<ApiResponse<ItemResponse>> createItem(
            @Valid @RequestBody ItemRequest request,
            @AuthenticationPrincipal User user) {
        ItemResponse response = itemService.createItem(request, user.getId());
        return ResponseBuilder.created("Item created successfully", response);
    }
 
    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID", description = "Retrieves detailed information for a specific item by its unique identifier.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item fetched successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemById(@PathVariable UUID id) {
        return ResponseBuilder.success("Item fetched successfully", itemService.getItemById(id));
    }
 
    @GetMapping
    @Operation(summary = "Get all items", description = "Retrieves a list of all publicly available items.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Items fetched successfully")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getAllItems() {
        return ResponseBuilder.success("Items fetched successfully", itemService.getAllItems());
    }
 
    @GetMapping("/search")
    @Operation(summary = "Search items by title", description = "Retrieves a list of items whose titles contain the specified search string.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Items fetched successfully")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> searchItems(@RequestParam String title) {
        return ResponseBuilder.success("Items fetched successfully", itemService.searchItemsByTitle(title));
    }
 
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get items by category", description = "Retrieves a list of items belonging to the specified category.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Items fetched successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getItemsByCategory(@PathVariable UUID categoryId) {
        return ResponseBuilder.success("Items fetched successfully", itemService.getItemsByCategory(categoryId));
    }
 
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OWNER')")
    @Operation(summary = "Update an existing item", description = "Updates the details of an existing item. The authenticated user must be the owner of the item. Requires Owner authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item updated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied (user is not the owner)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    public ResponseEntity<ApiResponse<ItemResponse>> updateItem(
            @PathVariable UUID id,
            @Valid @RequestBody ItemRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseBuilder.success("Item updated successfully", itemService.updateItem(id, request, user.getId()));
    }
 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('OWNER')")
    @Operation(summary = "Delete an item", description = "Permanently removes an item. The authenticated user must be the owner of the item. Requires Owner authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item deleted successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied (user is not the owner)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    public ResponseEntity<ApiResponse<Object>> deleteItem(
            @PathVariable UUID id,
            @AuthenticationPrincipal User user) {
        itemService.deleteItem(id, user.getId());
        return ResponseBuilder.success("Item deleted successfully", null);
    }
}
