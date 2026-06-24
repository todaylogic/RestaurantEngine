package com.langko.restaurantengine.order;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.langko.restaurantengine.auth.JwtUtil;
import com.langko.restaurantengine.order.dto.CreateOrderRequest;
import com.langko.restaurantengine.staff.Role;
import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.staff.StaffRepository;
import com.langko.restaurantengine.table.RestaurantTable;
import com.langko.restaurantengine.table.TableRepository;
import com.langko.restaurantengine.table.TableStatus;
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
class OrderControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired StaffRepository staffRepository;
    @Autowired TableRepository tableRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtUtil jwtUtil;

    private String staffToken;
    private RestaurantTable table;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        tableRepository.deleteAll();
        staffRepository.deleteAll();

        Staff waiter = new Staff();
        waiter.setFirstName("Waiter"); waiter.setLastName("One");
        waiter.setEmail("waiter@test.com");
        waiter.setPassword(passwordEncoder.encode("pass"));
        waiter.setRole(Role.STAFF);
        staffToken = "Bearer " + jwtUtil.generateToken(staffRepository.save(waiter));

        RestaurantTable t = new RestaurantTable();
        t.setTableNumber("T1"); t.setCapacity(4);
        t.setStatus(TableStatus.AVAILABLE);
        table = tableRepository.save(t);
    }

    @Test
    void createOrder_asStaff_returnsOk() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setTableId(table.getId());

        mockMvc.perform(post("/api/orders")
                .header("Authorization", staffToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void getOrders_asStaff_returns403() throws Exception {
        mockMvc.perform(get("/api/orders")
                .header("Authorization", staffToken))
            .andExpect(status().isForbidden());
    }

    @Test
    void createOrder_unauthenticated_returns401or403() throws Exception {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setTableId(table.getId());

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(result ->
                org.assertj.core.api.Assertions.assertThat(
                    result.getResponse().getStatus()).isIn(401, 403));
    }
}
