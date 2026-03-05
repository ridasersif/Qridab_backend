package com.qridaba.qridabaplatform.controller.api.v1.admin;

import com.qridaba.qridabaplatform.model.dto.request.UserRequest;
import com.qridaba.qridabaplatform.model.dto.response.UserResponse;
import com.qridaba.qridabaplatform.payload.ApiResponse;
import com.qridaba.qridabaplatform.payload.ResponseBuilder;
import com.qridaba.qridabaplatform.service.users.IUserService;
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
@PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
public class UserController {

    private final IUserService userService;

    @GetMapping("all")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsersIncludingDeleted() {
        return ResponseBuilder.success("Users fetched successfully", userService.getAllUsersIncludingDeleted());
    }

    @GetMapping("active")
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllActiveUsers() {
        return ResponseBuilder.success("Users fetched successfully", userService.getAllActiveUsers());
    }

    @GetMapping("/role/{roleName}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable String roleName) {
        return ResponseBuilder.success("Users fetched successfully for role: " + roleName,
                userService.getUsersByRole(roleName));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        return ResponseBuilder.success("User fetched successfully", userService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> create(@Valid @RequestBody UserRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseBuilder.created("User created and credentials sent to email", response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<UserResponse>> toggleStatus(
            @PathVariable UUID id,
            @RequestParam boolean enabled) {
        UserResponse response = userService.toggleUserStatus(id, enabled);
        String message = enabled ? "User account activated" : "User account deactivated";
        return ResponseBuilder.success(message, response);
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<ApiResponse<UserResponse>> updateRoles(
            @PathVariable UUID id,
            @RequestBody List<UUID> roleIds) {
        UserResponse response = userService.updateUserRoles(id, roleIds);
        return ResponseBuilder.success("User roles updated successfully", response);
    }

    @DeleteMapping("soft/{id}")
    public ResponseEntity<ApiResponse<Void>> softdelete(@PathVariable UUID id) {
        userService.softDeleteUser(id);
        return ResponseBuilder.success("User deleted successfully", null);
    }
    @DeleteMapping("hard/{id}")
    public ResponseEntity<ApiResponse<Void>> harddelete(@PathVariable UUID id) {
        userService.hardDeleteUser(id);
        return ResponseBuilder.success("User deleted successfully", null);
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> restore(@PathVariable UUID id) {
        return ResponseBuilder.success("User restored successfully", userService.restoreUser(id));
    }
}
