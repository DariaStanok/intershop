package ru.practicum.project.util;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpSession;
import ru.practicum.project.model.Cart;

public class CartUtils {

	private static final String SESSION_ATTR = "cart";
	private static final String SELECTION_ATTR = "selection";

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
	
	public static Map<Long, Integer> getSelection(HttpSession session) {
		Map<Long, Integer> selection = (Map<Long, Integer>) session.getAttribute(SELECTION_ATTR);
		if (selection == null) {
			selection = new HashMap<>();
			session.setAttribute(SELECTION_ATTR, selection);
		}
		return selection;
	}
	
	public static void clearSelection(HttpSession session) {
		session.removeAttribute(SELECTION_ATTR);
	}
}
