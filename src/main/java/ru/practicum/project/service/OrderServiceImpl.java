package ru.practicum.project.service;

import java.util.List;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.dto.OrderDto;
import ru.practicum.project.exeption.EmptyCartException;
import ru.practicum.project.model.Cart;
import ru.practicum.project.model.Order;
import ru.practicum.project.model.OrderItem;
import ru.practicum.project.repository.OrderRepository;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
	
	private final ModelMapper modelMapper;
	private final OrderRepository orderRepository; 

	@Override
	public OrderDto createOrder(Cart cart) {
		if (cart.getItems() == null || cart.getItems().isEmpty()) {
			throw new EmptyCartException();
		}

		Order order = new Order();

		List<OrderItem> orderItems = cart.getItems().stream()
				.map(cartLine -> new OrderItem(null, order, cartLine.getItem(), cartLine.getQuantity()))
				.collect(Collectors.toList());

		order.setItems(orderItems);
		orderRepository.save(order);
		cart.getItems().clear();
		return toOrderDto(order);

	}

	@Override
	public List<OrderDto> getAllOrders() {
		return orderRepository.findAll().stream()
                .map(this::toOrderDto)
                .collect(Collectors.toList());
	}

	@Override
	public OrderDto getOrderById(Long id) {
		Order order = orderRepository.findById(id).orElseThrow();
        return toOrderDto(order);
	}
	
	private OrderDto toOrderDto(Order order) {
		List<ItemDto> itemDtos = order.getItems().stream().map(orderItem -> {
			ItemDto dto = modelMapper.map(orderItem.getItem(), ItemDto.class);
			dto.setCount(orderItem.getCount());
			return dto;
		})
		.collect(Collectors.toList());
		int total = itemDtos.stream().mapToInt(i -> i.getPrice() * i.getCount()).sum();
		return new OrderDto(order.getId(), itemDtos, total);
	}

}
