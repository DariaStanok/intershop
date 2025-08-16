package ru.practicum.project.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.dto.OrderDto;
import ru.practicum.project.exсeption.EmptyCartException;
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
    	                    return Mono.error(new EmptyCartException());
    	                }
    	                return createOrderForLines(cartId, lines);
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
		
	private List<Long> extractItemIds(List<CartLine> lines) {
		    return lines.stream()
		         .map(CartLine::getItemId)
		         .toList();  
	}

	private Mono<Map<Long, Item>> fetchItems(List<Long> ids) {
		return itemRepository.findAllById(ids)
	          .collectMap(Item::getId, Function.identity());
	}
	
	private Mono<Integer> validateAndCalculateTotal(List<CartLine> lines, Map<Long, Item> itemsById) {
	    boolean anyMissing = lines.stream().anyMatch(l -> !itemsById.containsKey(l.getItemId()));
	    if (anyMissing) {
	        return Mono.error(new IllegalStateException());
	    }
	    int total = lines.stream()
	        .mapToInt(l -> itemsById.get(l.getItemId()).getPrice() * l.getQuantity())
	        .sum();
	    return Mono.just(total);
	}


	private Mono<Void> processPayment(int total) {
		 return paymentClient.withdraw(total, "ILS")
		            .filter(Boolean::booleanValue)
		            .switchIfEmpty(Mono.error(new RuntimeException()))
		            .then();
	}

	private Mono<OrderDto> createOrderTransaction(List<CartLine> lines, Map<Long, Item> itemsById) {
	    return Mono.defer(() ->
        orderRepository.save(new Order())
            .flatMap(order -> Flux.fromIterable(lines)
                .concatMap(line -> orderItemRepository.save(
                    new OrderItem(null, order.getId(), line.getItemId(), line.getQuantity())
                ))
                .collectList()
                .map(orderItems -> {
                    var itemDtos = orderItems.stream()
                        .map(oi -> {
                            Item item = itemsById.get(oi.getItemId());
                            ItemDto dto = modelMapper.map(item, ItemDto.class);
                            dto.setCount(oi.getCount());
                            return dto;
                        })
                        .toList();
                    int total = itemDtos.stream().mapToInt(i -> i.getPrice() * i.getCount()).sum();
                    return new OrderDto(order.getId(), itemDtos, total);
                })
            )
	    );
	}

	private Mono<Void> cleanUpCart(Long cartId) {
		 return cartLineRepository.deleteByCartId(cartId).then();
	}
	
	private Mono<OrderDto> createOrderForLines(Long cartId, List<CartLine> lines) {
	    List<Long> itemIds = extractItemIds(lines);

	    return fetchItems(itemIds)
	        .flatMap(itemsById -> validateAndCalculateTotal(lines, itemsById)
	            .flatMap(total -> processPayment(total)
	            	    .then(Mono.defer(() -> createOrderTransaction(lines, itemsById)))
	                    .flatMap(orderDto -> cleanUpCart(cartId).thenReturn(orderDto))
	                )
	        );
	}

	private Mono<OrderDto> toOrderDto(Long orderId, List<OrderItem> orderItems) {
	    return Flux.fromIterable(orderItems)
	        .flatMap(orderItem -> {
	            Mono<ItemDto> itemDtoMono = itemQueryService.getItemById(orderItem.getItemId());
	            return itemDtoMono.map(dto -> {
	                dto.setCount(orderItem.getCount());
	                return dto;
	            });
	        })
	        .collectList()
	        .map(itemDtos -> {
	            int total = itemDtos.stream()
	                .mapToInt(i -> i.getPrice() * i.getCount())
	                .sum();
	            return new OrderDto(orderId, itemDtos, total);
	        });
	}




}