package com.langko.restaurantengine.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddOrderItemRequest {
    @NotNull private Long menuItemId;
    @NotNull @Min(1) private Integer quantity;
    private String notes;

    public Long getMenuItemId() { return menuItemId; }
    public Integer getQuantity() { return quantity; }
    public String getNotes() { return notes; }

    public void setMenuItemId(Long menuItemId) { this.menuItemId = menuItemId; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public void setNotes(String notes) { this.notes = notes; }
}
