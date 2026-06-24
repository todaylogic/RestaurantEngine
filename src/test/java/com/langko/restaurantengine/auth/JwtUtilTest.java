package com.langko.restaurantengine.auth;

import com.langko.restaurantengine.staff.Role;
import com.langko.restaurantengine.staff.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private Staff staff;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret",
            "testSecretKeyForTestingPurposesOnly1234567890ABCDEF");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);

        staff = Staff.builder()
            .id(1L).firstName("John").lastName("Doe")
            .email("john@test.com").password("hashed")
            .role(Role.ADMIN).build();
    }

    @Test
    void generateToken_returnsNonBlankToken() {
        String token = jwtUtil.generateToken(staff);
        assertThat(token).isNotBlank();
    }

    @Test
    void extractEmail_returnsCorrectEmail() {
        String token = jwtUtil.generateToken(staff);
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("john@test.com");
    }

    @Test
    void isTokenValid_returnsTrueForValidToken() {
        String token = jwtUtil.generateToken(staff);
        assertThat(jwtUtil.isTokenValid(token, staff)).isTrue();
    }

    @Test
    void isTokenValid_returnsFalseForWrongUser() {
        String token = jwtUtil.generateToken(staff);
        Staff other = Staff.builder().email("other@test.com").build();
        assertThat(jwtUtil.isTokenValid(token, other)).isFalse();
    }
}
