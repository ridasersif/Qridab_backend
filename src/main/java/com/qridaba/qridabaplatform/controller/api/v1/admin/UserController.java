package com.qridaba.qridabaplatform.controller.api.v1.admin;

import com.qridaba.qridabaplatform.model.dto.request.UserRequest;
import com.qridaba.qridabaplatform.model.dto.response.UserResponse;
import com.qridaba.qridabaplatform.payload.ApiResponse;
import com.qridaba.qridabaplatform.payload.ResponseBuilder;
import com.qridaba.qridabaplatform.service.users.IUserService;
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
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "Endpoints for managing users and their profiles (Admin only)")
public class UserController {
 
    private final IUserService userService;
 
    @GetMapping("all")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    @Operation(summary = "Get all users (Total)", description = "Retrieves a list of all users, including those that are soft-deleted. Requires Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users fetched successfully")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsersIncludingDeleted() {
        return ResponseBuilder.success("Users fetched successfully", userService.getAllUsersIncludingDeleted());
    }
 
    @GetMapping("active")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Get active users", description = "Retrieves a list of all active (non-deleted) users. Requires Admin or Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users fetched successfully")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllActiveUsers() {
        return ResponseBuilder.success("Users fetched successfully", userService.getAllActiveUsers());
    }
 
    @GetMapping("/role/{roleName}")
    @Operation(summary = "Get users by role", description = "Retrieves a list of users who have been assigned a specific role.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users fetched successfully")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable String roleName) {
        return ResponseBuilder.success("Users fetched successfully for role: " + roleName,
                userService.getUsersByRole(roleName));
    }
 
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves detailed information for a specific user by their unique identifier.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User fetched successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        return ResponseBuilder.success("User fetched successfully", userService.getUserById(id));
    }
 
    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user account. An email with credentials will be sent to the user. Requires Admin or Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid user data")
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseBuilder.created("User created and credentials sent to email", response);
    }
 
    @PatchMapping("/{id}/status")
    @Operation(summary = "Toggle user status", description = "Enables or disables a user account. Requires Admin or Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User status updated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<ApiResponse<UserResponse>> toggleStatus(
            @PathVariable UUID id,
            @RequestParam boolean enabled) {
        UserResponse response = userService.toggleUserStatus(id, enabled);
        String message = enabled ? "User account activated" : "User account deactivated";
        return ResponseBuilder.success(message, response);
    }
 
    @PutMapping("/{id}/roles")
    @Operation(summary = "Update user roles", description = "Updates the roles assigned to a specific user. Requires Admin or Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User roles updated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<ApiResponse<UserResponse>> updateRoles(
            @PathVariable UUID id,
            @RequestBody List<UUID> roleIds) {
        UserResponse response = userService.updateUserRoles(id, roleIds);
        return ResponseBuilder.success("User roles updated successfully", response);
    }
 
    @DeleteMapping("soft/{id}")
    @Operation(summary = "Soft delete user", description = "Marks a user as deleted without removing them from the database. Requires Admin or Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User soft deleted successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<ApiResponse<Void>> softdelete(@PathVariable UUID id) {
        userService.softDeleteUser(id);
        return ResponseBuilder.success("User deleted successfully", null);
    }
 
    @DeleteMapping("hard/{id}")
    @Operation(summary = "Hard delete user", description = "Permanently removes a user from the database. Requires Admin or Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User hard deleted successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<ApiResponse<Void>> harddelete(@PathVariable UUID id) {
        userService.hardDeleteUser(id);
        return ResponseBuilder.success("User deleted successfully", null);
    }
 
    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    @Operation(summary = "Restore user", description = "Restores a soft-deleted user account. Requires Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User restored successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    public ResponseEntity<ApiResponse<UserResponse>> restore(@PathVariable UUID id) {
        return ResponseBuilder.success("User restored successfully", userService.restoreUser(id));
    }
}
