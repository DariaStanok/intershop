package ru.practicum.project.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.CartAction;
import ru.practicum.project.model.Cart;
import ru.practicum.project.service.CartService;
import ru.practicum.project.util.CartUtils;

@Controller
@RequestMapping("/cart/items")
public class CartController {

	private final CartService cartService;

	public CartController(CartService cartService) {
		this.cartService = cartService;
	}
	
	@PostMapping("/{itemId}")
    public String updateCart(@PathVariable Long itemId,
                             @RequestParam String action,
                             HttpSession session) {
		Cart cart = CartUtils.getOrCreateCart(session);
        CartAction cartAction = CartAction.valueOf(action.toUpperCase());
        cartService.updateItem(cart, itemId, cartAction);

        return "redirect:/cart/items";
    }

	@GetMapping
    public String viewCart(HttpSession session, Model model) {
		Cart cart = CartUtils.getOrCreateCart(session);

        List<ItemDto> cartItems = cartService.getCartItems(cart);
        int total = cartService.getTotal(cart);

        model.addAttribute("items", cartItems);
        model.addAttribute("total", total);

        return "cart"; 
    }
}
