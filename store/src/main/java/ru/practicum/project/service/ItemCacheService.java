package ru.practicum.project.service;

import java.time.Duration;

import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.practicum.project.dto.ItemDto;

@Service
@RequiredArgsConstructor
public class ItemCacheService {
	
	private final ReactiveRedisTemplate<String, ItemDto> itemDtoRedisTemplate;

    private static final Duration TTL = Duration.ofMinutes(10);

    private String key(long id) {
        return "store:item:v1:" + id;
    }

    public Mono<ItemDto> get(long id) {
        return itemDtoRedisTemplate.opsForValue().get(key(id));
    }

    public Mono<Boolean> put(ItemDto dto) {
        if (dto == null || dto.getId() == null) {
            return Mono.just(Boolean.FALSE);
        }
        return itemDtoRedisTemplate.opsForValue().set(key(dto.getId()), dto, TTL);
    }

    public Mono<Boolean> evict(long id) {
        return itemDtoRedisTemplate
                .opsForValue()
                .getAndDelete(key(id))
                .map(value -> Boolean.TRUE)
                .defaultIfEmpty(Boolean.FALSE);
    }
}


