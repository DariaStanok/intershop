package ru.practicum.project.service;

import java.time.Duration;

import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.exeption.ItemNotFoundException;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.ItemRepository;

@Service
@AllArgsConstructor
public class ItemQueryServiceImpl implements ItemQueryService {

	private static final String KEY_PREFIX = "item:";
    private static final Duration TTL = Duration.ofMinutes(10);

    private final ReactiveRedisTemplate<String, ItemDto> redisTemplate;
    private final ItemRepository itemRepository;
    private final ModelMapper modelMapper;
    
	@Override
	public Mono<ItemDto> getItemById(Long id) {
		String key = KEY_PREFIX + id;
        ReactiveValueOperations<String, ItemDto> ops = redisTemplate.opsForValue();

        return ops.get(key)
            .switchIfEmpty(
                itemRepository.findById(id)
                    .switchIfEmpty(Mono.error(new ItemNotFoundException()))
                    .map(item -> toDto(item))
                    .flatMap(dto -> ops.set(key, dto, TTL).thenReturn(dto))
            );
	}

	   private ItemDto toDto(Item item) {
	        ItemDto dto = modelMapper.map(item, ItemDto.class);
	        return dto;
	    }
}
