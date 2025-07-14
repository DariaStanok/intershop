package ru.practicum.project.controller;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.SortType;
import ru.practicum.project.model.Item;
import ru.practicum.project.service.ItemService;


@Controller
@RequestMapping ("/")
public class MainController {
	

    private final ItemService itemService;
	
	public MainController(ItemService itemService) {
        this.itemService = itemService;
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
            Model model) {

		List<List<ItemDto>> items = itemService.getItems(search, sort, pageNumber, pageSize);
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
}

