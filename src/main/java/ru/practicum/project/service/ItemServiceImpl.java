package ru.practicum.project.service;

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.SortType;
import ru.practicum.project.exeption.ItemNotFoundException;
import ru.practicum.project.model.Cart;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.util.ViewUtils;

@Service
@RequiredArgsConstructor 
public class ItemServiceImpl implements ItemService {
	
	private final ItemRepository itemRepository;
    private final ModelMapper modelMapper;
 

	@Override
	public List<List<ItemDto>> getItems(String search, SortType sortType, int pageNumber, int pageSize, Cart cart) {
		Page<Item> itemPage = fetchItemPage(search, sortType, pageNumber, pageSize);

		if (itemPage.isEmpty()) {
			throw new ItemNotFoundException();
		}

		  List<ItemDto> itemDtos = itemPage.getContent()
		            .stream()
		            .map(item -> {
		                ItemDto dto = modelMapper.map(item, ItemDto.class);
		                dto.setCount(0); 
		                if (cart != null && !cart.getItems().isEmpty()) {
		                    cart.getItems().stream()
		                            .filter(cl -> cl.getItem().getId().equals(item.getId()))
		                            .findFirst()
		                            .ifPresent(cl -> dto.setCount(cl.getQuantity()));
		                }
		                return dto;
		            })
		            .toList();

		return ViewUtils.splitToRows(itemDtos, 3);
	}

	@Override
	public Page<Item> fetchItemPage(String search, SortType sortType, int pageNumber, int pageSize) {
		    Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, getSort(sortType));
		    return itemRepository.findByTitleContainingIgnoreCase(search, pageable);
		
	}

	@Override
	public ItemDto getItemById(Long id, int count) {
		Item item = itemRepository.findById(id).orElseThrow(ItemNotFoundException::new);
		ItemDto dto = modelMapper.map(item, ItemDto.class);
		dto.setCount(count);
		return dto;
	}

	
	private Sort getSort(SortType sortType) {
	    return switch (sortType) {
	        case PRICE -> Sort.by(Sort.Direction.ASC, "price");
	        case ABC -> Sort.by(Sort.Direction.ASC, "title"); 
	        case NO -> Sort.unsorted();
	    };
	    
	}
}
