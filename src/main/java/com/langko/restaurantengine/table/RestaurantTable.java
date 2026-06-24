package com.langko.restaurantengine.table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "restaurant_tables")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class RestaurantTable {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String tableNumber;

    @Column(nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TableStatus status = TableStatus.AVAILABLE;

    public RestaurantTable() {}

    public Long getId() { return id; }
    public String getTableNumber() { return tableNumber; }
    public Integer getCapacity() { return capacity; }
    public TableStatus getStatus() { return status; }

    public void setTableNumber(String tableNumber) { this.tableNumber = tableNumber; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }
    public void setStatus(TableStatus status) { this.status = status; }
}
