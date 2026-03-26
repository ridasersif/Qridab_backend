package com.qridaba.qridabaplatform.controller.api.v1.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qridaba.qridabaplatform.model.dto.request.CategoryRequest;
import com.qridaba.qridabaplatform.model.entity.item.Category;
import com.qridaba.qridabaplatform.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CategoryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void createCategory_WhenAdmin_ShouldReturn201() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
                .name("Electronics")
                .description("Electronic items")
                .build();

        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Electronics"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void createCategory_WhenNotAdmin_ShouldReturn403() throws Exception {
        CategoryRequest request = CategoryRequest.builder()
                .name("Tools")
                .build();

        mockMvc.perform(post("/api/v1/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllCategories_ShouldReturnList() throws Exception {
        Category cat = new Category();
        cat.setName("Vehicles");
        categoryRepository.save(cat);

        mockMvc.perform(get("/api/v1/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("Vehicles"));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void updateCategory_WhenAdmin_ShouldUpdate() throws Exception {
        Category cat = new Category();
        cat.setName("Old Name");
        cat = categoryRepository.save(cat);
        UUID id = cat.getId();

        CategoryRequest request = CategoryRequest.builder()
                .name("New Name")
                .build();

        mockMvc.perform(put("/api/v1/admin/categories/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New Name"));
    }

    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void deleteCategory_WhenAdmin_ShouldReturn200() throws Exception {
        Category cat = new Category();
        cat.setName("To Delete");
        cat = categoryRepository.save(cat);

        mockMvc.perform(delete("/api/v1/admin/categories/" + cat.getId()))
                .andExpect(status().isOk());

        assert !categoryRepository.existsById(cat.getId());
    }
}
