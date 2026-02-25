package com.qridaba.qridabaplatform.controller.api.v1.admin;

import com.qridaba.qridabaplatform.model.dto.request.RoleRequest;
import com.qridaba.qridabaplatform.model.dto.response.RoleResponse;
import com.qridaba.qridabaplatform.payload.ApiResponse;
import com.qridaba.qridabaplatform.payload.ResponseBuilder;
import com.qridaba.qridabaplatform.service.role.IRoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/roles")
public class RoleController {

    private final IRoleService roleService;

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return ResponseBuilder.created("Role created successfully", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable UUID id) {
        return ResponseBuilder.success("Role fetched successfully", roleService.getRoleById(id));
    }

    @GetMapping("/assignable")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAssignableRoles() {
        List<RoleResponse> roles = roleService.getAssignableRoles();
        return ResponseBuilder.success("Assignable roles fetched successfully", roles);
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        return ResponseBuilder.success("Roles fetched successfully", roleService.getAllRoles());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody RoleRequest request) {
        return ResponseBuilder.success("Role updated successfully", roleService.updateRole(id, request));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN','SUPER_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseBuilder.success("Role deleted successfully", null);
    }
}
