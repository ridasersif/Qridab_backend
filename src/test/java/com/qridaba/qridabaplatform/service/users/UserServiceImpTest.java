package com.qridaba.qridabaplatform.service.users;

import com.qridaba.qridabaplatform.exception.EmailAlreadyExistsException;
import com.qridaba.qridabaplatform.exception.ResourceNotFoundException;
import com.qridaba.qridabaplatform.exception.RoleNotAllowedException;
import com.qridaba.qridabaplatform.mapper.UserMapper;
import com.qridaba.qridabaplatform.model.dto.request.UserRequest;
import com.qridaba.qridabaplatform.model.dto.response.UserResponse;
import com.qridaba.qridabaplatform.model.entity.user.Role;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.repository.RoleRepository;
import com.qridaba.qridabaplatform.repository.UserRepository;
import com.qridaba.qridabaplatform.util.MailService;
import com.qridaba.qridabaplatform.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImpTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private MailService mailService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserServiceImp userService;

    private User user;
    private UserResponse userResponse;
    private UserRequest userRequest;
    private Role adminRole;
    private Role superAdminRole;
    private Role userRole;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        
        userRole = new Role();
        userRole.setName("USER");
        
        adminRole = new Role();
        adminRole.setName("ADMIN");
        
        superAdminRole = new Role();
        superAdminRole.setName("SUPER_ADMIN");

        user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .roles(Set.of(userRole))
                .enabled(true)
                .build();
        user.setId(userId);

        userResponse = new UserResponse();
        userResponse.setId(userId.toString());
        userResponse.setEmail("john.doe@example.com");

        userRequest = new UserRequest();
        userRequest.setFirstName("John");
        userRequest.setLastName("Doe");
        userRequest.setEmail("john.doe@example.com");
        userRequest.setRoleIds(List.of(UUID.randomUUID()));
    }

    @Test
    void getAllUsersIncludingDeleted_ShouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        List<UserResponse> result = userService.getAllUsersIncludingDeleted();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    void getUserById_WhenExists_ShouldReturnResponse() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId.toString());
    }

    @Test
    void getUserById_WhenNotExists_ShouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createUser_WhenValid_ShouldSaveAndReturnResponse() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(user); // Current user is an admin or similar
        when(securityUtils.isSuperAdmin()).thenReturn(true); 
        when(roleRepository.findAllById(any())).thenReturn(List.of(userRole));
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.createUser(userRequest);

        // Assert
        assertThat(result).isNotNull();
        verify(mailService).sendCredentialsEmail(any(), any(), any());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowException() {
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(roleRepository.findAllById(any())).thenReturn(List.of(userRole));
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(userRequest))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void toggleUserStatus_WhenSuperAdmin_ShouldChangeStatus() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(user); // Just a mock user
        when(securityUtils.isSuperAdmin()).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toResponse(user)).thenReturn(userResponse);

        // Act
        UserResponse result = userService.toggleUserStatus(userId, false);

        // Assert
        assertThat(result).isNotNull();
        verify(userRepository).save(user);
        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    void toggleUserStatus_WhenTargetIsSuperAdmin_ShouldThrowException() {
        // Arrange
        User superAdmin = new User();
        superAdmin.setRoles(Set.of(superAdminRole));
        when(userRepository.findById(userId)).thenReturn(Optional.of(superAdmin));

        // Act & Assert
        assertThatThrownBy(() -> userService.toggleUserStatus(userId, false))
                .isInstanceOf(RoleNotAllowedException.class);
    }

    @Test
    void softDeleteUser_WhenAuthorized_ShouldSetEnabledFalse() {
        // Arrange
        when(securityUtils.getCurrentUser()).thenReturn(user);
        when(securityUtils.isSuperAdmin()).thenReturn(true);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        String result = userService.softDeleteUser(userId);

        // Assert
        assertThat(result).contains("succès");
        verify(userRepository).save(user);
        assertThat(user.isEnabled()).isFalse();
    }
}
