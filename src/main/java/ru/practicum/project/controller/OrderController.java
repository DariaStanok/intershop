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
import ru.practicum.project.dto.OrderDto;
import ru.practicum.project.model.Cart;
import ru.practicum.project.service.OrderService;
import ru.practicum.project.util.CartUtils;

@Controller
@RequestMapping("/orders")
public class OrderController {
	
	private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/buy")
    public String createOrder(HttpSession session) {
        Cart cart = CartUtils.getOrCreateCart(session);
        OrderDto order = orderService.createOrder(cart);
        cart.getItems().clear();
        session.setAttribute("cart", cart); 
        String redirectUrl = String.format("redirect:/orders/%d?new=true", order.getId());
        return redirectUrl;
    }
    
    @GetMapping
    public String getOrders(Model model) {
        List<OrderDto> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "orders";
    }
    
    @GetMapping("/{id}")
    public String getOrderById(@PathVariable Long id,
                               @RequestParam(defaultValue = "false") boolean newOrder,
                               Model model) {
        OrderDto order = orderService.getOrderById(id);
        model.addAttribute("order", order);
        model.addAttribute("newOrder", newOrder);
        return "order";
    }
}
