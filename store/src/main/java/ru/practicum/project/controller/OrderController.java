package ru.practicum.project.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.practicum.project.exception.ResponseStatusException;
import ru.practicum.project.service.OrderService;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping("/buy")
	public Mono<String> createOrder(@RequestParam("cartId") Long cartId) {
		return orderService.createOrder(cartId)
				.map(order -> String.format("redirect:/orders/%d?newOrder=true", order.getId()));
	}

	@GetMapping("/{orderId}")
	public Mono<String> getMyOrderById(@PathVariable("orderId") Long id,
	                                   @RequestParam(name = "newOrder", defaultValue = "false") boolean newOrder,
	                                   Model model) {
	    return orderService.getMyOrderById(id)
	        .map(order -> {
	            model.addAttribute("order", order);
	            model.addAttribute("newOrder", newOrder);
	            return "order";
	        })
	        .switchIfEmpty(Mono.error(new ResponseStatusException()));
	}
	
	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/{orderId}")
	public Mono<String> adminGetOrderById(@PathVariable("orderId") Long id, Model model) {
	    return orderService.getOrderById(id)
	        .map(order -> {
	            model.addAttribute("order", order);
	            model.addAttribute("newOrder", false);
	            return "order";
	        })
	        .switchIfEmpty(Mono.error(new ResponseStatusException()));
	}
	
	
}