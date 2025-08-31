package ru.practicum.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.practicum.project.enams.CartAction;
import ru.practicum.project.service.CartService;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

	private final CartService cartService;

	@GetMapping("/items")
	public Mono<String> viewCart(@RequestParam(name = "cartId", defaultValue = "1")Long cartId, Model model) {
		Mono<?> itemsMono = cartService.getCartItems(cartId).collectList();
		Mono<Integer> totalMono = cartService.getTotal(cartId);

		return Mono.zip(itemsMono, totalMono)
				.map(tuple -> {
					model.addAttribute("items", tuple.getT1());
					model.addAttribute("total", tuple.getT2());
					model.addAttribute("cartId", cartId);
					return "cart";
				});
	}
	
	@PostMapping("/items/{itemId}")
	public Mono<String> updateCartFromCart(
	        @PathVariable Long itemId,
	        @RequestParam(name = "action") String action,
	        @RequestParam(name = "cartId", defaultValue = "1") Long cartId
	) {
	    CartAction cartAction = CartAction.valueOf(action.toUpperCase());
	    return cartService.updateItem(cartId, itemId, cartAction)
	            .thenReturn("redirect:/cart/items?cartId=" + cartId);
	}

}
