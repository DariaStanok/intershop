package ru.practicum.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.practicum.project.service.CartService;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

	private final CartService cartService;

	@GetMapping("/items")
	public Mono<String> viewCart(@RequestParam("cartId") Long cartId, Model model) {
		Mono<?> itemsMono = cartService.getCartItems(cartId).collectList();
		Mono<Integer> totalMono = cartService.getTotal(cartId);

		return Mono.zip(itemsMono, totalMono)
				.map(tuple -> {
					model.addAttribute("items", tuple.getT1());
					model.addAttribute("total", tuple.getT2());
					return "cart";
				});
	}
}
