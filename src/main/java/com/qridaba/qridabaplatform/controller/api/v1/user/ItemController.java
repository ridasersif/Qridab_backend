package com.qridaba.qridabaplatform.controller.api.v1.user;

import com.qridaba.qridabaplatform.model.dto.request.ItemRequest;
import com.qridaba.qridabaplatform.model.dto.response.ItemResponse;
import com.qridaba.qridabaplatform.payload.ApiResponse;
import com.qridaba.qridabaplatform.payload.ResponseBuilder;
import com.qridaba.qridabaplatform.service.item.IItemService;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/items")
@Tag(name = "Item Management", description = "Endpoints for managing items (Owner based)")
public class ItemController {

    private final IItemService itemService;
    private final ObjectMapper objectMapper;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new item", description = "Send as multipart/form-data: 'item' is a JSON string of the item data, 'images' are the image files (multiple allowed). First image becomes the main image.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Item created successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid item data")
    public ResponseEntity<ApiResponse<ItemResponse>> createItem(
            @RequestParam("item") String itemJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal User user) {
        try { 
            ItemRequest request = objectMapper.readValue(itemJson, ItemRequest.class);
            ItemResponse response = itemService.createItem(request, user.getId(), images);
            return ResponseBuilder.created("Item created successfully", response);
        } catch (Exception e) {
            throw new RuntimeException("Invalid item JSON: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by ID", description = "Retrieves detailed information for a specific item by its unique identifier.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item fetched successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    public ResponseEntity<ApiResponse<ItemResponse>> getItemById(@PathVariable UUID id) {
        return ResponseBuilder.success("Item fetched successfully", itemService.getItemById(id));
    }

    @GetMapping
    @Operation(summary = "Get all items (Paginated)", description = "Retrieves a paginated list of all publicly available items.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Items fetched successfully")
    public ResponseEntity<ApiResponse<com.qridaba.qridabaplatform.model.dto.response.PaginatedResponse<ItemResponse>>> getAllItems(
            @RequestParam(value = "pageNo", defaultValue = "0", required = false) int pageNo,
            @RequestParam(value = "pageSize", defaultValue = "8", required = false) int pageSize,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir
    ) {
        return ResponseBuilder.success("Items fetched successfully", itemService.getAllItems(pageNo, pageSize, sortBy, sortDir));
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

    @GetMapping("/my-items")
    @PreAuthorize("hasAuthority('OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my items", description = "Retrieves a list of items belonging to the authenticated owner.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Items fetched successfully")
    public ResponseEntity<ApiResponse<List<ItemResponse>>> getMyItems(@AuthenticationPrincipal User user) {
        return ResponseBuilder.success("Items fetched successfully", itemService.getItemsByOwner(user.getId()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('OWNER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update an existing item", description = "Send as multipart/form-data: 'item' is a JSON string of the item data, 'images' are the new image files (optional, replaces all existing images). Requires Owner authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Item updated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Access denied (user is not the owner)")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Item not found")
    public ResponseEntity<ApiResponse<ItemResponse>> updateItem(
            @PathVariable UUID id,
            @RequestParam("item") String itemJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal User user) {
        try {
            ItemRequest request = objectMapper.readValue(itemJson, ItemRequest.class);
            return ResponseBuilder.success("Item updated successfully",
                    itemService.updateItem(id, request, user.getId(), images));
        } catch (Exception e) {
            throw new RuntimeException("Invalid item JSON: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('OWNER', 'ADMIN')")
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
