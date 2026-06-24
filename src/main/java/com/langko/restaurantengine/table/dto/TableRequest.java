package com.langko.restaurantengine.table.dto;

import com.langko.restaurantengine.table.TableStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class TableRequest {
    @NotBlank private String tableNumber;
    @NotNull @Min(1) private Integer capacity;
    private TableStatus status = TableStatus.AVAILABLE;
}
