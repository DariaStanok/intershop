package ru.practicum.project.controller;
import java.util.List;

import org.springframework.data.domain.Page;
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
import ru.practicum.project.enams.SortType;
import ru.practicum.project.model.Cart;
import ru.practicum.project.model.Item;
import ru.practicum.project.service.CartService;
import ru.practicum.project.service.ItemService;
import ru.practicum.project.util.CartUtils;


@Controller
@RequestMapping ("/")
public class MainController {
	

    private final ItemService itemService;
    private final CartService cartService;
	
	public MainController(ItemService itemService, CartService cartService) {
        this.itemService = itemService;
		this.cartService = cartService;
    }

	@GetMapping
    public String index() {
        return "redirect:/main/items";
    }

    @GetMapping("/main/items")
    public String getItems(@RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "NO") SortType sort,
                           @RequestParam(defaultValue = "10") int pageSize,
                           @RequestParam(defaultValue = "1") int pageNumber,
                           HttpSession session,
                           Model model) {

        Cart cart = CartUtils.getOrCreateCart(session);
        List<List<ItemDto>> items = itemService.getItems(search, sort, pageNumber, pageSize, cart);
        Page<Item> itemPage = itemService.fetchItemPage(search, sort, pageNumber, pageSize);

        model.addAttribute("items", items);
        model.addAttribute("pageNumber", itemPage.getNumber() + 1);
        model.addAttribute("pageSize", itemPage.getSize());
        model.addAttribute("hasNext", itemPage.hasNext());
        model.addAttribute("hasPrevious", itemPage.hasPrevious());
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);

        return "main";
    }

    @PostMapping("/main/items/{itemId}")
    public String updateCartFromMain(@PathVariable Long itemId,
                                     @RequestParam String action,
                                     HttpSession session,
                                     @RequestParam(defaultValue = "") String search,
                                     @RequestParam(defaultValue = "NO") SortType sort,
                                     @RequestParam(defaultValue = "10") int pageSize,
                                     @RequestParam(defaultValue = "1") int pageNumber) {
        Cart cart = CartUtils.getOrCreateCart(session);
        CartAction cartAction = CartAction.valueOf(action.toUpperCase());
        cartService.updateItem(cart, itemId, cartAction);

        return String.format("redirect:/main/items?search=%s&sort=%s&pageSize=%d&pageNumber=%d",
                search, sort, pageSize, pageNumber);
    }
}

