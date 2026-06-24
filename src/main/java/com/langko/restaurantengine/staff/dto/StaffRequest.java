package com.langko.restaurantengine.staff.dto;

import com.langko.restaurantengine.staff.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class StaffRequest {
    @NotBlank private String firstName;
    @NotBlank private String lastName;
    @NotBlank @Email private String email;
    private String password;
    @NotNull private Role role;
    private String phone;
}
