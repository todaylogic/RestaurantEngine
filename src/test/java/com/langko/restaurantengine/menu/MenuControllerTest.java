package com.langko.restaurantengine.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langko.restaurantengine.menu.dto.MenuCategoryRequest;
import com.langko.restaurantengine.staff.Role;
import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.staff.StaffRepository;
import com.langko.restaurantengine.auth.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MenuControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired StaffRepository staffRepository;
    @Autowired MenuCategoryRepository categoryRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    private String adminToken;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        staffRepository.deleteAll();
        Staff admin = staffRepository.save(Staff.builder()
            .firstName("Admin").lastName("User").email("admin@test.com")
            .password(passwordEncoder.encode("password")).role(Role.ADMIN).build());
        adminToken = "Bearer " + jwtUtil.generateToken(admin);
    }

    @Test
    void getCategories_public_returnsOk() throws Exception {
        mockMvc.perform(get("/api/menu/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createCategory_asAdmin_returnsOk() throws Exception {
        MenuCategoryRequest req = new MenuCategoryRequest();
        req.setName("Beverages"); req.setDescription("Drinks");

        mockMvc.perform(post("/api/menu/categories")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Beverages"));
    }

    @Test
    void createCategory_unauthenticated_returns403() throws Exception {
        MenuCategoryRequest req = new MenuCategoryRequest();
        req.setName("Starters");

        mockMvc.perform(post("/api/menu/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }
}
