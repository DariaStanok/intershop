package ru.practicum.project.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.Duration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.dto.ItemDto;
import ru.practicum.project.exception.ItemNotFoundException;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.ItemRepository;

@ExtendWith(MockitoExtension.class)
class ItemQueryServiceImplTest {

    @Mock private ReactiveRedisTemplate<String, ItemDto> redisTemplate;
    @Mock private ReactiveValueOperations<String, ItemDto> valueOps;
    @Mock private ItemRepository itemRepository;

    private ItemQueryServiceImpl service;
    private static final String KEY_1 = "store:item:v1:1";

    @BeforeEach
    void setUp() {
        service = new ItemQueryServiceImpl(redisTemplate, itemRepository, new ModelMapper());
    }

    @Test
    void getItemById_cacheHit() {
    	    ItemDto dto = new ItemDto();
    	    dto.setId(1L);
    	    dto.setTitle("Phone");
    	    dto.setPrice(100);

    	    when(redisTemplate.opsForValue()).thenReturn(valueOps);
    	    when(valueOps.get(anyString())).thenReturn(Mono.just(dto));

    	    when(itemRepository.findById(1L)).thenReturn(Mono.never()); 
    	    StepVerifier.create(service.getItemById(1L))
    	        .assertNext(found -> {
    	            assertThat(found.getId()).isEqualTo(1L);
    	            assertThat(found.getTitle()).isEqualTo("Phone");
    	        })
    	        .verifyComplete();
    }

    @Test
    void getItemById_cacheMiss_thenDb_thenSet() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps); // <-- и здесь
        when(valueOps.get(KEY_1)).thenReturn(Mono.empty());
        when(itemRepository.findById(1L)).thenReturn(Mono.just(new Item(1L, "Phone", null, 100, null, 0)));
        when(valueOps.set(eq(KEY_1), any(ItemDto.class), any(Duration.class))).thenReturn(Mono.just(true));

        StepVerifier.create(service.getItemById(1L))
            .assertNext(found -> {
                assertThat(found.getId()).isEqualTo(1L);
                assertThat(found.getTitle()).isEqualTo("Phone");
            })
            .verifyComplete();
    }

    @Test
    void warmUp_happyPath() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps); // <-- и здесь
        when(itemRepository.findById(1L)).thenReturn(Mono.just(new Item(1L, "Phone", null, 100, null, 0)));
        when(valueOps.set(eq(KEY_1), any(ItemDto.class), any(Duration.class))).thenReturn(Mono.just(true));

        StepVerifier.create(service.warmUp(1L))
            .assertNext(dto -> assertThat(dto.getTitle()).isEqualTo("Phone"))
            .verifyComplete();
    }

    @Test
    void getItemById_notFound_throws() {
        when(redisTemplate.opsForValue()).thenReturn(valueOps); // <-- и здесь
        when(valueOps.get(KEY_1)).thenReturn(Mono.empty());
        when(itemRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(service.getItemById(1L))
            .expectError(ItemNotFoundException.class)
            .verify();
    }

    @Test
    void evict_returnsTrueWhenDeleted() {
        when(redisTemplate.delete(KEY_1)).thenReturn(Mono.just(1L));

        StepVerifier.create(service.evict(1L))
            .expectNext(true)
            .verifyComplete();
    }

    @Test
    void evict_returnsFalseWhenMissing() {
        when(redisTemplate.delete(KEY_1)).thenReturn(Mono.just(0L));

        StepVerifier.create(service.evict(1L))
            .expectNext(false)
            .verifyComplete();
    }
}
