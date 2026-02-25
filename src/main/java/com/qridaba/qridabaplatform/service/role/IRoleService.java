
package com.qridaba.qridabaplatform.service.role;

import com.qridaba.qridabaplatform.model.dto.request.RoleRequest;
import com.qridaba.qridabaplatform.model.dto.response.RoleResponse;

import java.util.List;
import java.util.UUID;

public interface IRoleService {

    RoleResponse createRole(RoleRequest request);

    RoleResponse getRoleById(UUID id);

    List<RoleResponse> getAllRoles();

    List<RoleResponse> getAssignableRoles();

    RoleResponse updateRole(UUID id, RoleRequest request);

    void deleteRole(UUID id);
}
