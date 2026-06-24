package com.langko.restaurantengine.order;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByTableId(Long tableId, Pageable pageable);
    Page<Order> findByStatusAndTableId(OrderStatus status, Long tableId, Pageable pageable);
    boolean existsByTableId(Long tableId);
}
