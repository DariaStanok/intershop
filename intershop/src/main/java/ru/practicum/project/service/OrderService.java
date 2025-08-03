package ru.practicum.project.service;

import java.util.List;

import ru.practicum.project.dto.OrderDto;
import ru.practicum.project.model.Cart;

public interface OrderService {

	OrderDto createOrder(Cart cart);

	List<OrderDto> getAllOrders();

	OrderDto getOrderById(Long id);
}
