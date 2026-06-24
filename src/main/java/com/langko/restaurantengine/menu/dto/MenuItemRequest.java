package com.langko.restaurantengine.menu.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class MenuItemRequest {
    @NotBlank private String name;
    private String description;
    @NotNull @DecimalMin("0.0") private BigDecimal price;
    private Boolean available = true;
    @NotNull private Long categoryId;

    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public Boolean getAvailable() { return available; }
    public Long getCategoryId() { return categoryId; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setAvailable(Boolean available) { this.available = available; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
