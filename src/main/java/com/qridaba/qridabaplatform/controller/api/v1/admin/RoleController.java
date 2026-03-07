package com.qridaba.qridabaplatform.controller.api.v1.admin;

import com.qridaba.qridabaplatform.model.dto.request.RoleRequest;
import com.qridaba.qridabaplatform.model.dto.response.RoleResponse;
import com.qridaba.qridabaplatform.payload.ApiResponse;
import com.qridaba.qridabaplatform.payload.ResponseBuilder;
import com.qridaba.qridabaplatform.service.role.IRoleService;
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
@RequestMapping("/admin/roles")
@Tag(name = "Role Management", description = "Endpoints for managing user roles (Admin only)")
public class RoleController {
 
    private final IRoleService roleService;
 
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @Operation(summary = "Create a new role", description = "Creates a new role in the system. Requires Admin or Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Role created successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid role data")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ResponseBuilder.created("Role created successfully", response);
    }
 
    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID", description = "Retrieves a specific role by its unique identifier.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role fetched successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable UUID id) {
        return ResponseBuilder.success("Role fetched successfully", roleService.getRoleById(id));
    }
 
    @GetMapping("/assignable")
    @Operation(summary = "Get assignable roles", description = "Retrieves a list of roles that can be assigned to users.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Assignable roles fetched successfully")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAssignableRoles() {
        List<RoleResponse> roles = roleService.getAssignableRoles();
        return ResponseBuilder.success("Assignable roles fetched successfully", roles);
    }
 
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @GetMapping
    @Operation(summary = "Get all roles", description = "Retrieves a list of all roles in the system. Requires Admin or Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Roles fetched successfully")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        return ResponseBuilder.success("Roles fetched successfully", roleService.getAllRoles());
    }
 
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    @Operation(summary = "Update a role", description = "Updates an existing role's details. Requires Admin or Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role updated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleRequest request) {
        return ResponseBuilder.success("Role updated successfully", roleService.updateRole(id, request));
    }
 
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a role", description = "Deletes a role from the system. Requires Admin or Super Admin authority.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Role deleted successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Role not found")
    public ResponseEntity<ApiResponse<Object>> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseBuilder.success("Role deleted successfully", null);
    }
}
