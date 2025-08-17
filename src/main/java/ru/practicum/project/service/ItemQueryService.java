package ru.practicum.project.service;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;

@Service
public interface ItemQueryService {
	public Mono<ItemDto> getItemById(Long id);
}
