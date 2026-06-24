package com.langko.restaurantengine.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class AddOrderItemRequest {
    @NotNull private Long menuItemId;
    @NotNull @Min(1) private Integer quantity;
    private String notes;
}
