package com.langko.restaurantengine.order.dto;

import jakarta.validation.constraints.NotNull;

public class CreateOrderRequest {
    @NotNull private Long tableId;

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }
}
