package ru.practicum.project.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.CartAction;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.security.config.CartAccessGuard;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
	
	private final CartLineRepository cartLineRepository;
	private final ItemQueryService itemQueryService;
	private final CartAccessGuard cartAccessGuard;

	@Override
	public Flux<ItemDto> getCartItems(Long cartId) {
	    return cartAccessGuard.requireOwner(cartId)
	        .thenMany(
	            cartLineRepository.findByCartId(cartId)
	                .flatMap(line ->
	                    itemQueryService.getItemById(line.getItemId())
	                        .map(dto -> {
	                            dto.setCount(line.getQuantity());
	                            return dto;
	                        })
	                )
	        );
	}

	@Override
	public Mono<Integer> getTotal(Long cartId) {
	    return cartAccessGuard.requireOwner(cartId)
	        .thenMany(
	            cartLineRepository.findByCartId(cartId)
	                .flatMap(line ->
	                    itemQueryService.getItemById(line.getItemId())
	                        .map(dto -> dto.getPrice() * line.getQuantity())
	                )
	        )
	        .reduce(0, Integer::sum);
	}

	@Override
	public Mono<Void> updateItem(Long cartId, Long itemId, CartAction action) {
		return cartAccessGuard.requireOwner(cartId)
	            .then(
	                cartLineRepository.findByCartIdAndItemId(cartId, itemId)
	                    .defaultIfEmpty(new CartLine(null, 0, cartId, itemId))
	                    .flatMap(line -> handleAction(cartId, itemId, action, line))
	                    .then()
	            );
	}

	private Mono<CartLine> handleAction(Long cartId, Long itemId, CartAction action, CartLine line) {
		return switch (action) {
			case ADD -> {
				if (line.getId() == null) {
					yield saveLine(cartId, itemId, 1); 
				} else {
					yield Mono.empty(); 
				}
			}
			case PLUS -> saveLine(cartId, itemId, line.getQuantity() + 1);
			case MINUS -> {
				int newQty = line.getQuantity() - 1;
				if (newQty <= 0) {
					yield cartLineRepository.deleteById(line.getId()).then(Mono.empty());
				} else {
					yield saveLine(cartId, itemId, newQty);
				}
			}
			case DELETE -> cartLineRepository.deleteById(line.getId()).then(Mono.empty());
		};
	}


	private Mono<CartLine> saveLine(Long cartId, Long itemId, int quantity) {
		CartLine newLine = new CartLine(null, quantity, cartId, itemId);
		return cartLineRepository.save(newLine);
	}
}