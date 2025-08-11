package ru.practicum.project.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import reactor.core.publisher.Flux;
import ru.practicum.project.model.OrderItem;

public interface OrderItemRepository extends R2dbcRepository<OrderItem, Long> {

	 Flux<OrderItem> findByOrderId(Long orderId);
}
