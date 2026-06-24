package com.langko.restaurantengine.table;

import com.langko.restaurantengine.exception.ResourceNotFoundException;
import com.langko.restaurantengine.order.OrderRepository;
import com.langko.restaurantengine.table.dto.TableRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TableService {

    private static final Logger log = LoggerFactory.getLogger(TableService.class);

    private final TableRepository tableRepository;
    private final OrderRepository orderRepository;

    public TableService(TableRepository tableRepository, OrderRepository orderRepository) {
        this.tableRepository = tableRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<RestaurantTable> getAllTables() {
        return tableRepository.findAll();
    }

    @Transactional(readOnly = true)
    public RestaurantTable getTableById(Long id) {
        return tableRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Table not found: " + id));
    }

    @Transactional
    public RestaurantTable createTable(TableRequest request) {
        RestaurantTable table = new RestaurantTable();
        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setStatus(request.getStatus());
        RestaurantTable saved = tableRepository.save(table);
        log.info("Created table: {}", saved.getTableNumber());
        return saved;
    }

    @Transactional
    public RestaurantTable updateTable(Long id, TableRequest request) {
        RestaurantTable table = getTableById(id);
        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        table.setStatus(request.getStatus());
        return tableRepository.save(table);
    }

    @Transactional
    public void deleteTable(Long id) {
        if (orderRepository.existsByTableId(id)) {
            throw new IllegalStateException("Cannot delete table with active orders");
        }
        RestaurantTable table = getTableById(id);
        tableRepository.delete(table);
        log.info("Deleted table: {}", id);
    }
}
