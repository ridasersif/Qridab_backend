package com.qridaba.qridabaplatform.controller.api.v1.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qridaba.qridabaplatform.model.dto.request.LoginRequest;
import com.qridaba.qridabaplatform.model.dto.request.RegisterRequest;
import com.qridaba.qridabaplatform.model.entity.user.Role;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.repository.RoleRepository;
import com.qridaba.qridabaplatform.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.qridaba.qridabaplatform.util.MailService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MailService mailService;

    private Role userRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        // Don't delete roles, use the ones from DataInitializer or find them
        userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role role = new Role();
            role.setName("USER");
            return roleRepository.save(role);
        });
    }

    @Test
    void register_WhenValidRequest_ShouldReturn200() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("password123")
                .roleId(userRole.getId())
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void authenticate_WhenValidCredentials_ShouldReturn200() throws Exception {
        // Pre-create user
        User user = new User();
        user.setFirstName("Jane");
        user.setLastName("Doe");
        user.setEmail("jane.doe@example.com");
        user.setPassword(passwordEncoder.encode("secret123"));
        user.setEnabled(true);
        user.setRoles(java.util.Set.of(userRole));
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .email("jane.doe@example.com")
                .password("secret123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists());
    }

    @Test
    void authenticate_WhenInvalidCredentials_ShouldReturn401() throws Exception {
        // Pre-create user to avoid 404 if implementation checks user existence first
        User user = new User();
        user.setFirstName("Invalid");
        user.setLastName("User");
        user.setEmail("invalid@example.com");
        user.setPassword(passwordEncoder.encode("correctpassword"));
        user.setEnabled(true);
        user.setRoles(java.util.Set.of(userRole));
        userRepository.save(user);

        LoginRequest request = LoginRequest.builder()
                .email("invalid@example.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
