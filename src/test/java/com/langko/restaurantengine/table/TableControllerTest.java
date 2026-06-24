package com.langko.restaurantengine.table;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langko.restaurantengine.auth.JwtUtil;
import com.langko.restaurantengine.table.dto.TableRequest;
import com.langko.restaurantengine.staff.Role;
import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.staff.StaffRepository;
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
class TableControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired StaffRepository staffRepository;
    @Autowired TableRepository tableRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    private String managerToken;

    @BeforeEach
    void setUp() {
        tableRepository.deleteAll();
        staffRepository.deleteAll();

        Staff manager = new Staff();
        manager.setFirstName("Mgr"); manager.setLastName("User");
        manager.setEmail("mgr@test.com");
        manager.setPassword(passwordEncoder.encode("pass"));
        manager.setRole(Role.MANAGER);
        managerToken = "Bearer " + jwtUtil.generateToken(staffRepository.save(manager));
    }

    @Test
    void createTable_asManager_returnsOk() throws Exception {
        TableRequest req = new TableRequest();
        req.setTableNumber("T1"); req.setCapacity(4);

        mockMvc.perform(post("/api/tables")
                .header("Authorization", managerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.tableNumber").value("T1"));
    }

    @Test
    void getTables_authenticated_returnsOk() throws Exception {
        mockMvc.perform(get("/api/tables")
                .header("Authorization", managerToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void getTables_unauthenticated_returns401or403() throws Exception {
        mockMvc.perform(get("/api/tables"))
            .andExpect(result ->
                org.assertj.core.api.Assertions.assertThat(
                    result.getResponse().getStatus()).isIn(401, 403));
    }
}
