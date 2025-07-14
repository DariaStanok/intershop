package ru.practicum.project.service;

import java.util.List;

import org.springframework.data.domain.Page;

import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.SortType;
import ru.practicum.project.model.Item;

public interface ItemService {

	List<List<ItemDto>> getItems(String search, SortType sortType, int pageNumber, int pageSize);
	
	Page<Item> fetchItemPage(String search, SortType sortType, int pageNumber, int pageSize);

	ItemDto getItemById(Long id, int count);

}
