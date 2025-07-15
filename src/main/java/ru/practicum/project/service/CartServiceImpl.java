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
		return cart.getItems().stream()
                .map(line -> {
                    ItemDto dto = modelMapper.map(line.getItem(), ItemDto.class);
                    dto.setCount(line.getQuantity());
                    return dto;
                })
                .toList();
	}
	@Override
	public int getTotal(Cart cart) {
	        return cart.getItems().stream()
	                .mapToInt(line -> line.getItem().getPrice() * line.getQuantity())
	                .sum();
	}
	
	@Override
	public void updateItem(Cart cart, Long itemId, CartAction action) {
	     CartLine line = findLine(cart, itemId);
	        switch (action) {
	            case PLUS -> increment(cart, line, itemId);
	            case MINUS -> decrement(cart, line);
	            case DELETE -> remove(cart, line);
	            case ADD -> addToCart(cart, line, itemId);
	        }
	}
	
	private CartLine findLine(Cart cart, Long itemId) {
        return cart.getItems().stream()
                .filter(line -> line.getItem().getId().equals(itemId))
                .findFirst()
                .orElse(null);
    }
	
	private void addNewLine(Cart cart, Long itemId, int quantity) {
	    Item item = itemRepository.findById(itemId)
	            .orElseThrow(() -> new IllegalArgumentException("Item not found"));
	    CartLine newLine = new CartLine(null, quantity, cart, item);
	    cart.getItems().add(newLine);
	}

    private void increment(Cart cart, CartLine line, Long itemId) {
        if (line != null) {
            line.setQuantity(line.getQuantity() + 1);
        } else {
            addNewLine(cart, itemId, 1);
        }
    }

    private void decrement(Cart cart, CartLine line) {
        if (line != null) {
            int newQty = line.getQuantity() - 1;
            if (newQty <= 0) {
                cart.getItems().remove(line);
            } else {
                line.setQuantity(newQty);
            }
        }
    }

    private void remove(Cart cart, CartLine line) {
        if (line != null) {
            cart.getItems().remove(line);
        }
    }

    private void addToCart(Cart cart, CartLine line, Long itemId) {
        if (line == null) {
            addNewLine(cart, itemId, 1);
        }
    }

	
}
