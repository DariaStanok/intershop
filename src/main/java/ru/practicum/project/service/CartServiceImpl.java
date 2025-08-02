package ru.practicum.project.service;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.CartAction;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
	
	private final CartLineRepository cartLineRepository;
	private final ItemRepository itemRepository;
	private final ModelMapper modelMapper;

	@Override
	public Flux<ItemDto> getCartItems(Long cartId) {
		return cartLineRepository.findByCartId(cartId)
				.flatMap(line ->
					itemRepository.findById(line.getItemId())
						.map(item -> {
							ItemDto dto = modelMapper.map(item, ItemDto.class);
							dto.setCount(line.getQuantity());
							return dto;
						})
				);
	}

	@Override
	public Mono<Integer> getTotal(Long cartId) {
		return cartLineRepository.findByCartId(cartId)
				.flatMap(line ->
					itemRepository.findById(line.getItemId())
						.map(item -> item.getPrice() * line.getQuantity())
				)
				.reduce(0, Integer::sum);
	}

	@Override
	public Mono<Void> updateItem(Long cartId, Long itemId, CartAction action) {
		return cartLineRepository.findByCartIdAndItemId(cartId, itemId)
				.defaultIfEmpty(new CartLine(null, 0, cartId, itemId)) 
				.flatMap(line -> handleAction(cartId, itemId, action, line))
				.then();
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