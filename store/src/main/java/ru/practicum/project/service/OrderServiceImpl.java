package ru.practicum.project.service;

import static ru.practicum.project.security.utils.SecurityUtils.currentUsername;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.modelmapper.ModelMapper;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.dto.OrderDto;
import ru.practicum.project.exception.EmptyCartException;
import ru.practicum.project.exception.ResponseStatusException;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.model.Order;
import ru.practicum.project.model.OrderItem;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.repository.OrderItemRepository;
import ru.practicum.project.repository.OrderRepository;
import ru.practicum.project.security.config.CartAccessGuard;
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
	private final CartAccessGuard cartAccessGuard;
  

    @Transactional
    @Override
    public Mono<OrderDto> createOrder(Long cartId) {
    	return cartAccessGuard.requireOwner(cartId)
                .then(currentUsername()
                    .switchIfEmpty(Mono.error(new ResponseStatusException()))
                    .flatMap(userName ->
                        cartLineRepository.findByCartId(cartId)
                            .collectList()
                            .flatMap(lines -> {
                                if (lines.isEmpty()) return Mono.error(new EmptyCartException());
                                return createOrderForLines(cartId, lines, userName);
                            })
                    )
                );
        }
    
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Flux<OrderDto> getAllOrders() {
        return orderRepository.findAll()
            .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                .collectList()
                .flatMap(orderItems -> toOrderDto(order.getId(), orderItems))
            );
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<OrderDto> getOrderById(Long id) {
        return orderRepository.findById(id)
            .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                .collectList()
                .flatMap(orderItems -> toOrderDto(order.getId(), orderItems))
            );
    }
    
    @Override
    public Flux<OrderDto> getMyOrders() {
        return currentUsername().flatMapMany(userName ->
            orderRepository.findByUserName(userName)
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                    .collectList()
                    .flatMap(items -> toOrderDto(order.getId(), items))
                )
        );
    }

    @Override
    public Mono<OrderDto> getMyOrderById(Long id) {
        return currentUsername().flatMap(userName ->
            orderRepository.findByIdAndUserName(id, userName)
                .flatMap(order -> orderItemRepository.findByOrderId(order.getId())
                    .collectList()
                    .flatMap(items -> toOrderDto(order.getId(), items))
                )
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

	private Mono<OrderDto> createOrderTransaction(List<CartLine> lines, Map<Long, Item> itemsById, String userName) {
	    Order order = new Order();
	    order.setUserName(userName);
	    return orderRepository.save(order)
	        .flatMap(saved ->
	            Flux.fromIterable(lines)
	                .concatMap(line -> orderItemRepository.save(
	                    new OrderItem(null, saved.getId(), line.getItemId(), line.getQuantity())
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

	                    int total = itemDtos.stream()
	                        .mapToInt(i -> i.getPrice() * i.getCount())
	                        .sum();

	                    return new OrderDto(saved.getId(), itemDtos, total);
	                })
	           );     
	}

	private Mono<Void> cleanUpCart(Long cartId) {
		 return cartLineRepository.deleteByCartId(cartId).then();
	}
	
	private Mono<OrderDto> createOrderForLines(Long cartId, List<CartLine> lines,  String userName) {
	    List<Long> itemIds = extractItemIds(lines);
	    return fetchItems(itemIds)
	    		.flatMap(itemsById ->
	            validateAndCalculateTotal(lines, itemsById)
	                .flatMap(total -> processPayment(total)
	                    .then(createOrderTransaction(lines, itemsById, userName))
	                    .flatMap(dto -> cleanUpCart(cartId).thenReturn(dto))
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