package ru.practicum.project.service;

import java.util.List;

import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.enams.SortType;

public interface ItemService {

	Mono<List<List<ItemDto>>> getItems(String search, SortType sortType, int pageNumber, int pageSize, Long cartId);

	Mono<ItemDto> getItemById(Long id, int count);

}
