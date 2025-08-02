package ru.practicum.project.config;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.ItemRepository;

@Component
public class DataLoader implements ApplicationRunner {

	private final ItemRepository itemRepository;

	public DataLoader(ItemRepository itemRepository) {
		this.itemRepository = itemRepository;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		itemRepository.count()
				.flatMap (count-> {	
					if(count == 0) {
		               List<Item> items = List.of(
							new Item(null, "Black cap", "Sport black cap", 2000, "/uploads/cap_black.png", 0),
							new Item(null, "Orange cap", "Sport cap", 2500, "/uploads/cap_orange.png", 0),
							new Item(null, "Red cap", "Casual red cap", 3000, "/uploads/cap_red.png", 0),
							new Item(null, "Red snickers", "Casual red snickers", 15000, "/uploads/snickers_red.png", 0),
							new Item(null, "White snickers", "Sport white snickers", 10000, "/uploads/snickers_white.png", 0),
							new Item(null, "Black snickers", "Casual black snickers", 12000, "/uploads/snickers_black.png", 0),
							new Item(null, "Colored socks", "Casual colored socks", 1000, "/uploads/socks_colored.png", 0),
							new Item(null, "Gray socks", "Sport gray socks", 1500, "/uploads/socks_gray.png", 0),
							new Item(null, "White socks", "Sport white t-shirt", 2000, "/uploads/socks_white.png", 0),
							new Item(null, "Black t-shirt", "Casual black t-shirt", 2000, "/uploads/tshirt_black.png", 0),
							new Item(null, "Gray t-shirt", "Casual gray t-shirt", 2000, "/uploads/tshirt_gray.png", 0),
							new Item(null, "White t-shirt", "Casual white t-shirt", 2000, "/uploads/tshirt_white.png", 0));
					itemRepository.saveAll(items);
					}
					return Mono.empty();
		})
			.then() 
			.subscribe();	
	}

}
