package com.qridaba.qridabaplatform.service.role;

import com.qridaba.qridabaplatform.exception.DuplicateResourceException;
import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.mapper.RoleMapper;
import com.qridaba.qridabaplatform.model.dto.request.RoleRequest;
import com.qridaba.qridabaplatform.model.dto.response.RoleResponse;
import com.qridaba.qridabaplatform.model.entity.user.Role;
import com.qridaba.qridabaplatform.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceImpTest {

    @Mock
    private RoleRepository roleRepository;
    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleServiceImp roleService;

    private Role role;
    private RoleRequest roleRequest;
    private RoleResponse roleResponse;
    private UUID roleId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        role = new Role();
        role.setId(roleId);
        role.setName("USER");

        roleRequest = new RoleRequest();
        roleRequest.setName("USER");

        roleResponse = new RoleResponse();
        roleResponse.setId(roleId);
        roleResponse.setName("USER");
    }

    @Test
    void createRole_WhenValid_ShouldSaveAndReturnResponse() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        RoleResponse result = roleService.createRole(roleRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("USER");
        verify(roleRepository).save(any(Role.class));
    }

    @Test
    void createRole_WhenDuplicateName_ShouldThrowException() {
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));

        assertThatThrownBy(() -> roleService.createRole(roleRequest))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void getRoleById_WhenExists_ShouldReturnResponse() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        RoleResponse result = roleService.getRoleById(roleId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(roleId);
    }

    @Test
    void getAllRoles_ShouldReturnList() {
        when(roleRepository.findAll()).thenReturn(List.of(role));
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        List<RoleResponse> result = roleService.getAllRoles();

        assertThat(result).hasSize(1);
    }

    @Test
    void getAssignableRoles_ShouldExcludeAdminAndSuperAdmin() {
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        Role superAdminRole = new Role();
        superAdminRole.setName("SUPER_ADMIN");
        Role userRole = new Role();
        userRole.setName("USER");

        when(roleRepository.findAll()).thenReturn(List.of(adminRole, superAdminRole, userRole));
        when(roleMapper.toResponse(userRole)).thenReturn(roleResponse);

        List<RoleResponse> result = roleService.getAssignableRoles();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("USER");
    }

    @Test
    void updateRole_WhenValid_ShouldUpdateAndReturnResponse() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(roleRepository.save(any(Role.class))).thenReturn(role);
        when(roleMapper.toResponse(role)).thenReturn(roleResponse);

        RoleResponse result = roleService.updateRole(roleId, roleRequest);

        assertThat(result).isNotNull();
        verify(roleRepository).save(role);
    }

    @Test
    void deleteRole_WhenExists_ShouldDelete() {
        when(roleRepository.findById(roleId)).thenReturn(Optional.of(role));

        roleService.deleteRole(roleId);

        verify(roleRepository).delete(role);
    }
}
