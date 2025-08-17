package ru.practicum.project.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.OrderDto;

public interface OrderService {

	Mono<OrderDto> createOrder(Long cartId);

	Mono<OrderDto> getOrderById(Long id);

	Flux<OrderDto> getAllOrders();
	
	Flux<OrderDto> getMyOrders();
	
	Mono<OrderDto> getMyOrderById(Long id);
}
