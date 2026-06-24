package com.langko.restaurantengine.table;

import com.langko.restaurantengine.exception.ResourceNotFoundException;
import com.langko.restaurantengine.table.dto.TableRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TableService {

    private final TableRepository tableRepository;

    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    public RestaurantTable getTableById(Long id) {
        return tableRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Table not found: " + id));
    }

    public RestaurantTable createTable(TableRequest request) {
        RestaurantTable table = RestaurantTable.builder()
            .tableNumber(request.getTableNumber())
            .capacity(request.getCapacity())
            .status(request.getStatus()).build();
        RestaurantTable saved = tableRepository.save(table);
        log.info("Created table: {}", saved.getTableNumber());
        return saved;
    }

    public RestaurantTable updateTable(Long id, TableRequest request) {
        RestaurantTable table = getTableById(id);
        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setStatus(request.getStatus());
        return tableRepository.save(table);
    }

    public void deleteTable(Long id) {
        RestaurantTable table = getTableById(id);
        tableRepository.delete(table);
        log.info("Deleted table: {}", id);
    }
}
