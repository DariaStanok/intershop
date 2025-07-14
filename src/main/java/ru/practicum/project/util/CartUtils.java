package ru.practicum.project.util;

import jakarta.servlet.http.HttpSession;
import ru.practicum.project.model.Cart;

public class CartUtils {

	private static final String SESSION_ATTR = "cart";

	private CartUtils() {
	}

	public static Cart getOrCreateCart(HttpSession session) {
		Cart cart = (Cart) session.getAttribute(SESSION_ATTR);
		if (cart == null) {
			cart = new Cart();
			session.setAttribute(SESSION_ATTR, cart);
		}
		return cart;
	}
}
