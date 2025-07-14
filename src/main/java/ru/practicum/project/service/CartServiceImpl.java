package ru.practicum.project.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.CartAction;
import ru.practicum.project.model.Cart;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.ItemRepository;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

	private final ItemRepository itemRepository;
	private final ModelMapper modelMapper;

	@Override
	public List<ItemDto> getCartItems(Cart cart) {
		return cart.getItems()
				.stream()
				.map(line -> {ItemDto dto = modelMapper.map(line.getItem(), ItemDto.class);
			     dto.setCount(line.getQuantity());
			     return dto;
			     })
				.toList();
	}

	@Override
	public int getTotal(Cart cart) {
		return cart.getItems()
				.stream()
				.mapToInt(line -> line.getItem().getPrice() * line.getQuantity())
				.sum();
	}

	@Override
	public void updateItem(Cart cart, Long itemId, CartAction action) {
		CartLine line = cart.getItems()
				.stream()
				.filter(cl -> cl.getItem().getId().equals(itemId)).findFirst()
				.orElse(null);

		processCartAction(cart, line, itemId, action);
	}

	private void processCartAction(Cart cart, CartLine line, Long itemId, CartAction action) {
		switch (action) {
		case PLUS -> {
			if (line == null) {
				Item item = itemRepository.findById(itemId).orElseThrow();
				line = new CartLine(null, 1, cart, item);
				cart.getItems().add(line);
			} else {
				line.setQuantity(line.getQuantity() + 1);
			}
		}

		case MINUS -> {
			if (line != null) {
				int q = line.getQuantity() - 1;
				if (q <= 0) {
					cart.getItems().remove(line);
				} else {
					line.setQuantity(q);
				}
			}
		}

		case DELETE -> {
			if (line != null) {
				cart.getItems().remove(line);
			}
		}
		}
	}

}
