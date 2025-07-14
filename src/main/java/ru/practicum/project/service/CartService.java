package ru.practicum.project.service;

import java.util.List;

import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.CartAction;
import ru.practicum.project.model.Cart;

public interface CartService {

	List<ItemDto> getCartItems(Cart cart);

	int getTotal(Cart cart);

	void updateItem(Cart cart, Long itemId, CartAction action);
}
