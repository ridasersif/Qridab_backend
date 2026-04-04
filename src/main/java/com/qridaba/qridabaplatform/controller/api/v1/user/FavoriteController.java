package com.qridaba.qridabaplatform.controller.api.v1.user;

import com.qridaba.qridabaplatform.model.dto.request.FavoriteRequest;
import com.qridaba.qridabaplatform.model.dto.response.FavoriteResponse;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.payload.ApiResponse;
import com.qridaba.qridabaplatform.payload.ResponseBuilder;
import com.qridaba.qridabaplatform.service.favorite.IFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("user/favorites")
@Tag(name = "Favorite Management", description = "Endpoints for managing user favorites")
public class FavoriteController {

    private final IFavoriteService favoriteService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add an item to favorites", description = "Adds the specified item to the authenticated user's favorites.")
    public ResponseEntity<ApiResponse<FavoriteResponse>> addFavorite(
            @Valid @RequestBody FavoriteRequest request,
            @AuthenticationPrincipal User user) {
        return ResponseBuilder.created("Item added to favorites successfully",
                favoriteService.addFavorite(request, user.getId()));
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Remove an item from favorites", description = "Removes the specified item from the authenticated user's favorites.")
    public ResponseEntity<ApiResponse<Object>> removeFavorite(
            @PathVariable UUID itemId,
            @AuthenticationPrincipal User user) {
        favoriteService.removeFavorite(user.getId(), itemId);
        return ResponseBuilder.success("Item removed from favorites successfully", null);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get user's favorites", description = "Retrieves all favorite items of the authenticated user.")
    public ResponseEntity<ApiResponse<List<FavoriteResponse>>> getUserFavorites(
            @AuthenticationPrincipal User user) {
        return ResponseBuilder.success("Favorites fetched successfully",
                favoriteService.getUserFavorites(user.getId()));
    }

    @GetMapping("/check/{itemId}")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Check if item is favorited", description = "Checks whether the specified item is in the user's favorites.")
    public ResponseEntity<ApiResponse<Boolean>> checkIfFavorite(
            @PathVariable UUID itemId,
            @AuthenticationPrincipal User user) {
        return ResponseBuilder.success("Favorite status fetched successfully",
                favoriteService.checkIfFavorite(user.getId(), itemId));
    }
}
