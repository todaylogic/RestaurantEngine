package com.langko.restaurantengine.order.dto;

import com.langko.restaurantengine.order.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class UpdateOrderStatusRequest {
    @NotNull private OrderStatus status;
}
