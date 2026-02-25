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
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
public class UserController {

    private final IUserService userService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseBuilder.success("Users fetched successfully", userService.getAllUsers());
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

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseBuilder.success("User deleted successfully", null);
    }
}
