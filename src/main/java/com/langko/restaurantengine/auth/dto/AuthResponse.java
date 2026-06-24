package com.langko.restaurantengine.auth.dto;

import com.langko.restaurantengine.staff.Role;

public class AuthResponse {
    private final String token;
    private final Role role;
    private final String firstName;
    private final String lastName;

    public AuthResponse(String token, Role role, String firstName, String lastName) {
        this.token = token;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getToken() { return token; }
    public Role getRole() { return role; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
}
