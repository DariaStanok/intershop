package ru.practicum.project.service;

import java.time.Duration;

import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.stereotype.Service;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.exception.ItemNotFoundException;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.ItemRepository;

@Service
@AllArgsConstructor
public class ItemQueryServiceImpl implements ItemQueryService {

	private static final String KEY_PREFIX = "store:item:v1:";  
    private static final Duration TTL = Duration.ofMinutes(10);

    private final ReactiveRedisTemplate<String, ItemDto> redisTemplate;
    private final ItemRepository itemRepository;
    private final ModelMapper modelMapper;

    private String key(Long id) { 
    	return KEY_PREFIX + id; 
    }
    @Override
    public Mono<ItemDto> getItemById(Long id) {
        ReactiveValueOperations<String, ItemDto> ops = redisTemplate.opsForValue();
        String k = key(id);

        return ops.get(k)
            .switchIfEmpty(
                itemRepository.findById(id)
                    .switchIfEmpty(Mono.error(new ItemNotFoundException()))
                    .map(this::toDto)
                    .flatMap(dto -> ops.set(k, dto, TTL).thenReturn(dto))
            );
    }

    @Override
    public Mono<Boolean> evict(Long id) {
        return redisTemplate.delete(key(id)).map(count -> count != null && count > 0);
    }

    @Override
    public Mono<ItemDto> warmUp(Long id) {
        ReactiveValueOperations<String, ItemDto> ops = redisTemplate.opsForValue();
        String k = key(id);

        return itemRepository.findById(id)
            .switchIfEmpty(Mono.error(new ItemNotFoundException()))
            .map(this::toDto)
            .flatMap(dto -> ops.set(k, dto, TTL).thenReturn(dto));
    }

    private ItemDto toDto(Item item) {
        return modelMapper.map(item, ItemDto.class);
    }
}
