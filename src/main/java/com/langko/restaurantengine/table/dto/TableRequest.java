package com.langko.restaurantengine.table.dto;

import com.langko.restaurantengine.table.TableStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TableRequest {
    @NotBlank private String tableNumber;
    @NotNull @Min(1) private Integer capacity;
    private TableStatus status = TableStatus.AVAILABLE;

    public String getTableNumber() { return tableNumber; }
    public Integer getCapacity() { return capacity; }
    public TableStatus getStatus() { return status; }

    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public void setStatus(TableStatus status) { this.status = status; }
}
