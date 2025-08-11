package ru.practicum.project.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.SortType;
import ru.practicum.project.exeption.ItemNotFoundException;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.util.ViewUtils;

@Service
@RequiredArgsConstructor 
public class ItemServiceImpl implements ItemService {
	
	private final ItemRepository itemRepository;
	private final CartLineRepository cartLineRepository;
    private final ModelMapper modelMapper;
    private final ItemQueryService itemQueryService;
    

	@Override
	public Mono<List<List<ItemDto>>> getItems(String search, SortType sortType, int pageNumber, int pageSize, Long cartId) {
		Mono<List<Item>> itemsMono = fetchPagedAndSortedItems(search, sortType, pageNumber, pageSize);
		Mono<Map<Long, Integer>> cartMapMono = fetchCartItemMap(cartId);

		return Mono.zip(itemsMono, cartMapMono)
				.map(tuple -> toItemDtoRows(tuple.getT1(), tuple.getT2()));
	}

	@Override
	public Mono<ItemDto> getItemById(Long id, int count) {
		return itemQueryService.getItemById(id)
				.switchIfEmpty(Mono.error(new ItemNotFoundException()))
                .map(dto -> {
                    dto.setCount(count);
                    return dto;
                });
	}
 

	private Mono<List<Item>> fetchPagedAndSortedItems(String search, SortType sortType, int pageNumber, int pageSize) {
		int skip = (pageNumber - 1) * pageSize;
		return itemRepository.findByTitleContainingIgnoreCase(search)
				.sort(getComparator(sortType))
				.skip(skip)
				.take(pageSize)
				.collectList()
				.flatMap(items -> {
					if (items.isEmpty()) {
						return Mono.error(new ItemNotFoundException());
					}
					return Mono.just(items);
				});
	}

	private Mono<Map<Long, Integer>> fetchCartItemMap(Long cartId) {
		if (cartId == null) {
			return Mono.just(Map.of());
		}
		return cartLineRepository.findByCartId(cartId)
				.collectMap(CartLine::getItemId, CartLine::getQuantity);
	}

	private List<List<ItemDto>> toItemDtoRows(List<Item> items, Map<Long, Integer> cartMap) {
		List<ItemDto> dtos = items.stream()
				.map(item -> toItemDto(item, cartMap.getOrDefault(item.getId(), 0)))
				.toList();
		return ViewUtils.splitToRows(dtos, 3);
	}

	private ItemDto toItemDto(Item item, int count) {
		ItemDto dto = modelMapper.map(item, ItemDto.class);
		dto.setCount(count);
		return dto;
	}

	private Comparator<Item> getComparator(SortType sortType) {
		return switch (sortType) {
			case PRICE -> Comparator.comparingInt(Item::getPrice);
			case ABC -> Comparator.comparing(Item::getTitle, String.CASE_INSENSITIVE_ORDER);
			case NO -> (a, b) -> 0;
		};
	}
}
