package com.langko.restaurantengine.order;

import com.langko.restaurantengine.common.ApiResponse;
import com.langko.restaurantengine.order.dto.AddOrderItemRequest;
import com.langko.restaurantengine.order.dto.CreateOrderRequest;
import com.langko.restaurantengine.order.dto.UpdateOrderStatusRequest;
import com.langko.restaurantengine.staff.Staff;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
    public ResponseEntity<ApiResponse<Page<Order>>> getOrders(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) Long tableId,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
            orderService.getAllOrders(status, tableId, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Order>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrderById(id)));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Order>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @AuthenticationPrincipal Staff staff) {
        return ResponseEntity.ok(ApiResponse.success(orderService.createOrder(request, staff)));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Order>> updateStatus(
            @PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.updateStatus(id, request)));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Order>> addItem(
            @PathVariable Long id, @Valid @RequestBody AddOrderItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.addItem(id, request)));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Order>> removeItem(
            @PathVariable Long id, @PathVariable Long itemId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.removeItem(id, itemId)));
    }
}
