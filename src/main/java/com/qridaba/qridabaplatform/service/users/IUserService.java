package com.qridaba.qridabaplatform.service.users;

import com.qridaba.qridabaplatform.model.dto.response.UserResponse;
import java.util.List;
import java.util.UUID;
import com.qridaba.qridabaplatform.model.dto.request.UserRequest;

public interface IUserService {

    List<UserResponse> getAllUsers();
    List<UserResponse> getUsersByRole(String roleName);
    UserResponse getUserById(UUID id);
    UserResponse getUserByEmail(String email);

    UserResponse createUser(UserRequest request);

    String deleteUser(UUID id);

    UserResponse toggleUserStatus(UUID id, boolean enabled);


    UserResponse updateUserRoles(UUID id, List<UUID> roleIds);


    List<UserResponse> searchUsers(String query);
}