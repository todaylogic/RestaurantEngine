package com.langko.restaurantengine.staff;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langko.restaurantengine.auth.JwtUtil;
import com.langko.restaurantengine.staff.dto.StaffRequest;
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
class StaffControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired StaffRepository staffRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    private String adminToken;
    private String managerToken;

    @BeforeEach
    void setUp() {
        staffRepository.deleteAll();

        Staff admin = new Staff();
        admin.setFirstName("Admin"); admin.setLastName("User");
        admin.setEmail("admin@test.com");
        admin.setPassword(passwordEncoder.encode("pass"));
        admin.setRole(Role.ADMIN);
        adminToken = "Bearer " + jwtUtil.generateToken(staffRepository.save(admin));

        Staff manager = new Staff();
        manager.setFirstName("Mgr"); manager.setLastName("User");
        manager.setEmail("mgr@test.com");
        manager.setPassword(passwordEncoder.encode("pass"));
        manager.setRole(Role.MANAGER);
        managerToken = "Bearer " + jwtUtil.generateToken(staffRepository.save(manager));
    }

    @Test
    void getAllStaff_asManager_returnsOk() throws Exception {
        mockMvc.perform(get("/api/staff").header("Authorization", managerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void createStaff_asAdmin_returnsOk() throws Exception {
        StaffRequest req = new StaffRequest();
        req.setFirstName("New"); req.setLastName("Waiter");
        req.setEmail("waiter@test.com"); req.setPassword("pass123");
        req.setRole(Role.STAFF);

        mockMvc.perform(post("/api/staff")
                .header("Authorization", adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.email").value("waiter@test.com"))
            .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void createStaff_asManager_returns403() throws Exception {
        StaffRequest req = new StaffRequest();
        req.setFirstName("X"); req.setLastName("Y");
        req.setEmail("x@test.com"); req.setPassword("pass"); req.setRole(Role.STAFF);

        mockMvc.perform(post("/api/staff")
                .header("Authorization", managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isForbidden());
    }
}
