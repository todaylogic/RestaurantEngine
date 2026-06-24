package com.langko.restaurantengine.order;

import com.langko.restaurantengine.exception.ResourceNotFoundException;
import com.langko.restaurantengine.menu.MenuItem;
import com.langko.restaurantengine.menu.MenuItemRepository;
import com.langko.restaurantengine.order.dto.AddOrderItemRequest;
import com.langko.restaurantengine.order.dto.CreateOrderRequest;
import com.langko.restaurantengine.order.dto.UpdateOrderStatusRequest;
import com.langko.restaurantengine.staff.Staff;
import com.langko.restaurantengine.table.RestaurantTable;
import com.langko.restaurantengine.table.TableRepository;
import com.langko.restaurantengine.table.TableStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(OrderStatus status, Long tableId, Pageable pageable) {
        if (status != null && tableId != null) {
            return orderRepository.findByStatusAndTableId(status, tableId, pageable);
        } else if (status != null) {
            return orderRepository.findByStatus(status, pageable);
        } else if (tableId != null) {
            return orderRepository.findByTableId(tableId, pageable);
        }
        return orderRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request, Staff staff) {
        RestaurantTable table = tableRepository.findById(request.getTableId())
            .orElseThrow(() -> new ResourceNotFoundException("Table not found: " + request.getTableId()));
        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);

        Order order = Order.builder()
            .table(table).staff(staff).status(OrderStatus.PENDING).build();
        Order saved = orderRepository.save(order);
        log.info("Created order {} for table {}", saved.getId(), table.getTableNumber());
        return saved;
    }

    @Transactional
    public Order updateStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = getOrderById(id);
        order.setStatus(request.getStatus());
        if (request.getStatus() == OrderStatus.COMPLETED || request.getStatus() == OrderStatus.CANCELLED) {
            order.getTable().setStatus(TableStatus.AVAILABLE);
            tableRepository.save(order.getTable());
        }
        return orderRepository.save(order);
    }

    @Transactional
    public Order addItem(Long orderId, AddOrderItemRequest request) {
        Order order = getOrderById(orderId);
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + request.getMenuItemId()));
        OrderItem item = OrderItem.builder()
            .order(order).menuItem(menuItem)
            .quantity(request.getQuantity())
            .unitPrice(menuItem.getPrice())
            .notes(request.getNotes()).build();
        order.getItems().add(item);
        return orderRepository.save(order);
    }

    @Transactional
    public Order removeItem(Long orderId, Long itemId) {
        Order order = getOrderById(orderId);
        OrderItem item = orderItemRepository.findById(itemId)
            .orElseThrow(() -> new ResourceNotFoundException("Order item not found: " + itemId));
        if (!order.getItems().remove(item)) {
            throw new ResourceNotFoundException("Order item " + itemId + " does not belong to order " + orderId);
        }
        return orderRepository.save(order);
    }
}
