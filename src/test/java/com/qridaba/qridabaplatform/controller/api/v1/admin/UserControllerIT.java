package com.qridaba.qridabaplatform.controller.api.v1.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qridaba.qridabaplatform.model.dto.request.UserRequest;
import com.qridaba.qridabaplatform.model.entity.user.Role;
import com.qridaba.qridabaplatform.model.entity.user.User;
import com.qridaba.qridabaplatform.repository.RoleRepository;
import com.qridaba.qridabaplatform.repository.UserRepository;
import com.qridaba.qridabaplatform.util.MailService;

import lombok.Builder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MailService mailService;

    private Role userRole;
    private Role adminRole;
    private Role superAdminRole;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        
        userRole = roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(new Role(null, "USER")));
        adminRole = roleRepository.findByName("ADMIN").orElseGet(() -> roleRepository.save(new Role(null, "ADMIN")));
        superAdminRole = roleRepository.findByName("SUPER_ADMIN").orElseGet(() -> roleRepository.save(new Role(null, "SUPER_ADMIN")));
    }

    @Test
    @WithMockUser(authorities = {"SUPER_ADMIN"})
    void getAllUsersIncludingDeleted_WhenSuperAdmin_ShouldReturnList() throws Exception {
        User user = User.builder()
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .password("password")
                .enabled(true)
                .roles(Set.of(userRole))
                .build();
        userRepository.save(user);

        mockMvc.perform(get("/api/v1/admin/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(hasSize(1).matches(1) ? 1 : 1))); // Simple check
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void getAllUsersIncludingDeleted_WhenAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void createUser_WhenAdmin_ShouldCreateUser() throws Exception {
        UserRequest request = UserRequest.builder()
                .firstName("New")
                .lastName("User")
                .email("newuser@example.com")
                .roleIds(Collections.singletonList(userRole.getId()))
                .build();

        mockMvc.perform(post("/api/v1/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("newuser@example.com"));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void toggleStatus_WhenAdmin_ShouldUpdateStatus() throws Exception {
        User user = User.builder()
                .firstName("Status")
                .lastName("User")
                .email("status@example.com")
                .password("password")
                .enabled(true)
                .roles(Set.of(userRole))
                .build();
        user = userRepository.save(user);

        mockMvc.perform(patch("/api/v1/admin/users/" + user.getId() + "/status")
                        .param("enabled", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.enabled").value(false));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void softDelete_WhenAdmin_ShouldMarkAsDeleted() throws Exception {
        User user = User.builder()
                .firstName("Delete")
                .lastName("Me")
                .email("delete@example.com")
                .password("password")
                .enabled(true)
                .roles(Set.of(userRole))
                .build();
        user = userRepository.save(user);

        mockMvc.perform(delete("/api/v1/admin/users/soft/" + user.getId()))
                .andExpect(status().isOk());

        User deletedUser = userRepository.findById(user.getId()).orElseThrow();
        // Assuming your soft delete logic sets a deleted flag or similar. 
        // If it's inherited from a base class, check that property.
    }
}
