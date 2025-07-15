package ru.practicum.project.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.model.Cart;
import ru.practicum.project.service.CartService;
import ru.practicum.project.util.CartUtils;

@Controller
@RequestMapping("/cart")
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}
	
	@GetMapping("/items")
    public String viewCart(HttpSession session, Model model) {
        Cart cart = CartUtils.getOrCreateCart(session);
        List<ItemDto> cartItems = cartService.getCartItems(cart);
        int total = cartService.getTotal(cart);

        model.addAttribute("items", cartItems);
        model.addAttribute("total", total);

        return "cart";
    }
}
