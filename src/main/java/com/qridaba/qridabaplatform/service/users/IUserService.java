package com.qridaba.qridabaplatform.service.users;

import com.qridaba.qridabaplatform.model.dto.response.UserResponse;
import java.util.List;
import java.util.UUID;
import com.qridaba.qridabaplatform.model.dto.request.UserRequest;

public interface IUserService {

    List<UserResponse> getAllUsersIncludingDeleted();
    List<UserResponse> getAllActiveUsers();
    List<UserResponse> getUsersByRole(String roleName);
    UserResponse getUserById(UUID id);
    UserResponse getUserByEmail(String email);

    UserResponse createUser(UserRequest request);

    String softDeleteUser(UUID id);

    String hardDeleteUser(UUID id);

    UserResponse toggleUserStatus(UUID id, boolean enabled);


    UserResponse updateUserRoles(UUID id, List<UUID> roleIds);

    UserResponse restoreUser(UUID id);

    List<UserResponse> searchUsers(String query);
}