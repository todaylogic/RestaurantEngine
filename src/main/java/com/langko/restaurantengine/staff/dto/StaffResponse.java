package com.langko.restaurantengine.staff.dto;

import com.langko.restaurantengine.staff.Role;
import com.langko.restaurantengine.staff.Staff;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StaffResponse {
    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
    private final Role role;
    private final String phone;
    private final LocalDateTime createdAt;

    public StaffResponse(Staff staff) {
        this.id = staff.getId();
        this.firstName = staff.getFirstName();
        this.lastName = staff.getLastName();
        this.email = staff.getEmail();
        this.role = staff.getRole();
        this.phone = staff.getPhone();
        this.createdAt = staff.getCreatedAt();
    }
}
