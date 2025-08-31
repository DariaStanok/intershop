package ru.practicum.project.service;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;

@Service
public interface ItemQueryService {
	  Mono<ItemDto> getItemById(Long id);
	  Mono<Boolean> evict(Long id);     
	  Mono<ItemDto> warmUp(Long id); 
}
