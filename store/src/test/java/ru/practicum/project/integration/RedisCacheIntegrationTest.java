package ru.practicum.project.integration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;

import reactor.test.StepVerifier;
import ru.practicum.project.config.StoreWithRedisIntegrationTestBase;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.service.ItemQueryService;

@ActiveProfiles("test")
class  RedisCacheIntegrationTest extends StoreWithRedisIntegrationTestBase  {

    @Autowired 
    private DatabaseClient db;
    @Autowired 
    private ItemRepository itemRepository;
    @Autowired 
    private ItemQueryService itemQueryService;

    private Long itemId;

    @BeforeEach
    void setUp() {
        Item saved = itemRepository.save(
            new Item(null, "Cached item", "desc", 250, "/img.jpg", 0)
        ).block();
        this.itemId = saved.getId();
    }

    @Test
    void itemIsServedFromRedisAfterFirstLoad() {
        StepVerifier.create(itemQueryService.getItemById(itemId))
            .assertNext(dto -> {
                assertThat(dto.getId()).isEqualTo(itemId);
                assertThat(dto.getTitle()).isEqualTo("Cached item");
                assertThat(dto.getPrice()).isEqualTo(250);
            })
            .verifyComplete();

        StepVerifier.create(itemRepository.deleteAll()).verifyComplete();
        StepVerifier.create(itemQueryService.getItemById(itemId))
            .assertNext(dto -> {
                assertThat(dto.getId()).isEqualTo(itemId);
                assertThat(dto.getTitle()).isEqualTo("Cached item");
                assertThat(dto.getPrice()).isEqualTo(250);
            })
            .verifyComplete();
    }

    @Test
    void notFoundWhenNotInCacheAndDb() {
        Long missingId = 999_999L;

        StepVerifier.create(
            db.sql("DELETE FROM items WHERE id = :id").bind("id", missingId).then()
        ).verifyComplete();

        StepVerifier.create(itemQueryService.getItemById(missingId))
            .expectErrorSatisfies(ex ->
                assertThat(ex.getClass().getSimpleName()).isEqualTo("ItemNotFoundException")
            )
            .verify();
    }
}
