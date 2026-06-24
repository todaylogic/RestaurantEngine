package com.langko.restaurantengine.auth.dto;

import com.langko.restaurantengine.staff.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class AuthResponse {
    private String token;
    private Role role;
    private String firstName;
    private String lastName;
}
