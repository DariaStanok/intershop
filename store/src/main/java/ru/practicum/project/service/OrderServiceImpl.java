package ru.practicum.project.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.dto.OrderDto;
import ru.practicum.project.exeption.EmptyCartException;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
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
    private final PaymentClient paymentClient;
    private final ItemQueryService itemQueryService;

    @Transactional
    @Override
    public Mono<OrderDto> createOrder(Long cartId) {
        return cartLineRepository.findByCartId(cartId)
            .collectList()
            .flatMap(lines -> {
                if (lines.isEmpty()) {
                    return Mono.<OrderDto>error(new EmptyCartException());
                }
                return createOrderForLines(cartId, lines);
            });
    }

    private Mono<OrderDto> createOrderForLines(Long cartId, List<CartLine> lines) {
        List<Long> ids = lines.stream()
            .map(CartLine::getItemId)
            .collect(Collectors.toList());

        return itemRepository.findAllById(ids)
            .collectMap(Item::getId, Function.identity())
            .flatMap(itemsById -> {
                if (itemsById.size() != ids.size()) {
                    return Mono.<OrderDto>error(new IllegalStateException("One or more items not found"));
                }

                int total = lines.stream()
                    .mapToInt(l -> itemsById.get(l.getItemId()).getPrice() * l.getQuantity())
                    .sum();

                return paymentClient.withdraw(total, "ILS")
                    .filter(Boolean::booleanValue)
                    .switchIfEmpty(Mono.error(new RuntimeException("Payment failed")))
                    .then(orderRepository.save(new Order()))                      
                    .flatMap(savedOrder ->
                        Flux.fromIterable(lines)
                            .concatMap(l -> orderItemRepository.save(
                                new OrderItem(null, savedOrder.getId(), l.getItemId(), l.getQuantity())
                            ))
                            .collectList()
                            .flatMap(savedItems -> toOrderDtoUsingCache(savedOrder.getId(), savedItems, itemsById)) 
                    )
                    
                    .delayUntil(dto -> cartLineRepository.deleteById(cartId));
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
                 itemQueryService.getItemById(orderItem.getItemId()) 
                   .map(dto -> {
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

 
    private Mono<OrderDto> toOrderDtoUsingCache(Long orderId, List<OrderItem> orderItems, Map<Long, Item> itemsById) {
        List<ItemDto> itemDtos = orderItems.stream()
            .map(oi -> {
                Item item = itemsById.get(oi.getItemId());
                ItemDto dto = modelMapper.map(item, ItemDto.class);
                dto.setCount(oi.getCount());
                return dto;
            })
            .collect(Collectors.toList());

        int total = itemDtos.stream()
            .mapToInt(i -> i.getPrice() * i.getCount())
            .sum();

        return Mono.just(new OrderDto(orderId, itemDtos, total));
    }

	
}