package ru.practicum.project.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.dto.OrderDto;
import ru.practicum.project.exeption.EmptyCartException;
import ru.practicum.project.model.Order;
import ru.practicum.project.model.OrderItem;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.repository.OrderItemRepository;
import ru.practicum.project.repository.OrderRepository;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartLineRepository cartLineRepository;
    private final ItemRepository itemRepository;
    private final ModelMapper modelMapper;

    @Override
    public Mono<OrderDto> createOrder(Long cartId) {
        return cartLineRepository.findByCartId(cartId)
                .collectList()
                .flatMap(cartLines -> {
                    if (cartLines.isEmpty()) {
                        return Mono.error(new EmptyCartException());
                    }

                    Order order = new Order();
                    return orderRepository.save(order)
                            .flatMap(savedOrder -> {
                                List<OrderItem> orderItems = cartLines.stream()
                                        .map(line -> new OrderItem(
                                                null,
                                                savedOrder.getId(),
                                                line.getItemId(),
                                                line.getQuantity()))
                                        .toList();

                                return orderItemRepository.saveAll(orderItems)
                                        .thenMany(Flux.fromIterable(orderItems))
                                        .collectList()
                                        .flatMap(savedItems ->
                                                toOrderDto(savedOrder.getId(), savedItems)
                                        );
                            })
                            .then(cartLineRepository.findByCartId(cartId)
                                    .flatMap(line -> cartLineRepository.deleteById(line.getId()))
                                    .then(Mono.empty())
                            );
                });
    }

    @Override
    public Flux<OrderDto> getAllOrders() {
        return orderRepository.findAll()
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .collectList()
                        .flatMap(orderItems -> toOrderDto(order.getId(), orderItems))
                );
    }

    @Override
    public Mono<OrderDto> getOrderById(Long id) {
        return orderRepository.findById(id)
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                        .collectList()
                        .flatMap(orderItems -> toOrderDto(order.getId(), orderItems))
                );
    }

    private Mono<OrderDto> toOrderDto(Long orderId, List<OrderItem> orderItems) {
        return Flux.fromIterable(orderItems)
                .flatMap(orderItem ->
                        itemRepository.findById(orderItem.getItemId())
                                .map(item -> {
                                    ItemDto dto = modelMapper.map(item, ItemDto.class);
                                    dto.setCount(orderItem.getCount());
                                    return dto;
                                })
                )
                .collectList()
                .map(itemDtos -> {
                    int total = itemDtos.stream()
                            .mapToInt(i -> i.getPrice() * i.getCount())
                            .sum();
                    return new OrderDto(orderId, itemDtos, total);
                });
    }
}
