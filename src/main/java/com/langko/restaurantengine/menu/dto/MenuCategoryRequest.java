package com.langko.restaurantengine.menu.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class MenuCategoryRequest {
    @NotBlank private String name;
    private String description;
}
