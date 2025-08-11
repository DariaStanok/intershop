package ru.practicum.project.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.CartAction;
import ru.practicum.project.enams.SortType;
import ru.practicum.project.service.CartService;
import ru.practicum.project.service.ItemService;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MainController {

    private final ItemService itemService;
    private final CartService cartService;

    @GetMapping
    public String index() {
        return "redirect:/main/items";
    }

    @GetMapping("/main/items")
    public String getItemsView() {
        return "main"; 
    }

    @GetMapping("/main/items/data")
    @ResponseBody
    public Mono<ItemsPage> getItems(
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "sort", defaultValue = "NO") SortType sort,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
            @RequestParam(name = "cartId", required = false) Long cartId
    ) {
        return itemService.getItems(search, sort, pageNumber, pageSize, cartId)
                .map(rows -> new ItemsPage(rows, pageNumber, pageSize, search, sort));
    }

    @PostMapping("/main/items/{itemId}")
    public Mono<String> updateCartFromMain(
            @PathVariable(name = "itemId") Long itemId,
            @RequestParam(name = "action") String action,
            @RequestParam(name = "cartId") Long cartId,
            @RequestParam(name = "search", defaultValue = "") String search,
            @RequestParam(name = "sort", defaultValue = "NO") SortType sort,
            @RequestParam(name = "pageSize", defaultValue = "10") int pageSize,
            @RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber
    ) {
        CartAction cartAction = CartAction.valueOf(action.toUpperCase());
        return cartService.updateItem(cartId, itemId, cartAction)
                .thenReturn(String.format("redirect:/main/items?search=%s&sort=%s&pageSize=%d&pageNumber=%d&cartId=%d",
                        search, sort, pageSize, pageNumber, cartId));
    }

    public record ItemsPage(
            List<List<ItemDto>> items,
            int pageNumber,
            int pageSize,
            String search,
            SortType sort
    ) {}
}
