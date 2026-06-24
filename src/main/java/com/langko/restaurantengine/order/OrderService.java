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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final TableRepository tableRepository;
    private final MenuItemRepository menuItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                        TableRepository tableRepository, MenuItemRepository menuItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.tableRepository = tableRepository;
        this.menuItemRepository = menuItemRepository;
    }

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
        if (table.getStatus() != TableStatus.AVAILABLE) {
            throw new IllegalStateException("Table " + table.getTableNumber() + " is not available");
        }
        table.setStatus(TableStatus.OCCUPIED);
        tableRepository.save(table);

        Order order = new Order();
        order.setTable(table);
        order.setStaff(staff);
        order.setStatus(OrderStatus.PENDING);
        Order saved = orderRepository.save(order);
        log.info("Created order {} for table {}", saved.getId(), table.getTableNumber());
        return saved;
    }

    @Transactional
    public Order updateStatus(Long id, UpdateOrderStatusRequest request) {
        Order order = getOrderById(id);
        order.setStatus(request.getStatus());
        if (request.getStatus() == OrderStatus.COMPLETED || request.getStatus() == OrderStatus.CANCELLED) {
            RestaurantTable table = tableRepository.findById(order.getTable().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Table not found"));
            table.setStatus(TableStatus.AVAILABLE);
            tableRepository.save(table);
        }
        return orderRepository.save(order);
    }

    @Transactional
    public Order addItem(Long orderId, AddOrderItemRequest request) {
        Order order = getOrderById(orderId);
        MenuItem menuItem = menuItemRepository.findById(request.getMenuItemId())
            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found: " + request.getMenuItemId()));
        OrderItem item = new OrderItem();
        item.setOrder(order);
        item.setMenuItem(menuItem);
        item.setQuantity(request.getQuantity());
        item.setUnitPrice(menuItem.getPrice());
        item.setNotes(request.getNotes());
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
