package com.langko.restaurantengine.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langko.restaurantengine.auth.dto.LoginRequest;
import com.langko.restaurantengine.auth.dto.RegisterRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired StaffRepository staffRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        staffRepository.deleteAll();
    }

    @Test
    void register_firstStaff_returnsOkAndNoPassword() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setFirstName("John"); req.setLastName("Doe");
        req.setEmail("john@test.com"); req.setPassword("password123");
        req.setRole(Role.ADMIN);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.email").value("john@test.com"))
            .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void login_validCredentials_returnsToken() throws Exception {
        staffRepository.save(Staff.builder()
            .firstName("Jane").lastName("Doe").email("jane@test.com")
            .password(passwordEncoder.encode("secret")).role(Role.ADMIN).build());

        LoginRequest req = new LoginRequest();
        req.setEmail("jane@test.com"); req.setPassword("secret");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.token").isNotEmpty())
            .andExpect(jsonPath("$.data.firstName").value("Jane"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        staffRepository.save(Staff.builder()
            .firstName("Jane").lastName("Doe").email("jane2@test.com")
            .password(passwordEncoder.encode("secret")).role(Role.ADMIN).build());

        LoginRequest req = new LoginRequest();
        req.setEmail("jane2@test.com"); req.setPassword("wrong");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isUnauthorized());
    }
}
