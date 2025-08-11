package ru.practicum.project.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.CartAction;

public interface CartService {

	Flux<ItemDto> getCartItems(Long cartId);

	Mono<Integer> getTotal(Long cartId);

	Mono<Void> updateItem(Long cartId, Long itemId, CartAction action);
	
	
}
