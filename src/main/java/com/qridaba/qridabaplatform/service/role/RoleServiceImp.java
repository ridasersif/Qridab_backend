package com.qridaba.qridabaplatform.service.role;

import com.qridaba.qridabaplatform.exception.DuplicateResourceException;
import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.mapper.RoleMapper;
import com.qridaba.qridabaplatform.model.dto.request.RoleRequest;
import com.qridaba.qridabaplatform.model.dto.response.RoleResponse;
import com.qridaba.qridabaplatform.model.entity.user.Role;
import com.qridaba.qridabaplatform.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoleServiceImp implements IRoleService {

        private final RoleRepository roleRepository;
        private final RoleMapper roleMapper;

        @Override
        public RoleResponse createRole(RoleRequest request) {
                if (roleRepository.findByName(request.getName()).isPresent()) {
                        throw new DuplicateResourceException(
                                        "Role already exists with name: " + request.getName());
                }

                Role role = Role.builder()
                                .name(request.getName())
                                .build();

                Role saved = roleRepository.save(role);
                return roleMapper.toResponse(saved);
        }

        @Override
        public RoleResponse getRoleById(UUID id) {
                Role role = roleRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Role not found with id: " + id));
                return roleMapper.toResponse(role);
        }

        @Override
        public List<RoleResponse> getAllRoles() {
                List<Role> roles = roleRepository.findAll();
                return roles.stream()
                                .map(roleMapper::toResponse)
                                .toList();
        }

        @Override
        public List<RoleResponse> getAssignableRoles() {
                List<Role> roles = roleRepository.findAll()
                                .stream()
                                .filter(role -> !role.getName().equalsIgnoreCase("ADMIN")
                                                && !role.getName().equalsIgnoreCase("SUPER_ADMIN"))
                                .toList();

                return roles.stream()
                                .map(roleMapper::toResponse)
                                .toList();
        }

        @Override
        public RoleResponse updateRole(UUID id, RoleRequest request) {
                Role existingRole = roleRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Role not found with id: " + id));

                roleRepository.findByName(request.getName())
                                .filter(r -> !r.getId().equals(id))
                                .ifPresent(r -> {
                                        throw new DuplicateResourceException(
                                                        "Role already exists with name: " + request.getName());
                                });

                existingRole.setName(request.getName());
                Role updated = roleRepository.save(existingRole);
                return roleMapper.toResponse(updated);
        }

        @Override
        public void deleteRole(UUID id) {
                Role role = roleRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Role not found with id: " + id));
                roleRepository.delete(role);
        }
}
