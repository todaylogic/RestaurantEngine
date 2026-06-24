package com.langko.restaurantengine.menu.dto;

import jakarta.validation.constraints.NotBlank;

public class MenuCategoryRequest {
    @NotBlank private String name;
    private String description;

    public String getName() { return name; }
    public String getDescription() { return description; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
}
