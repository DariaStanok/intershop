package ru.practicum.project.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.config.TestDataLoaderConfig;
import ru.practicum.project.config.TestSchemaInitializer;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;

@DataR2dbcTest
@Import({TestSchemaInitializer.class, TestDataLoaderConfig.class})
class CartLineRepositoryTest {

	@Autowired
    private CartLineRepository cartLineRepository;

    @Autowired
    private ItemRepository itemRepository;

    private Long item1Id;

    @BeforeEach
    void preload(@Autowired @Qualifier("preloadTestData") Mono<Void> preloadTestData) {
        StepVerifier.create(preloadTestData).verifyComplete();
        this.item1Id = itemRepository.findAll()
                .filter(item -> item.getTitle().equals("Smartphone"))
                .blockFirst()
                .getId();
    }

    @Test
    void testFindByCartIdReturnsCartLines() {
        StepVerifier.create(cartLineRepository.findByCartId(1L))
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void testFindByCartIdAndItemIdReturnsCorrectCartLine() {
        StepVerifier.create(cartLineRepository.findByCartIdAndItemId(1L, item1Id))
            .assertNext(cartLine -> assertThat(cartLine.getQuantity()).isEqualTo(1))
            .verifyComplete();
    }

    @Test
    void testFindByCartIdAndItemIdReturnsEmpty() {
        StepVerifier.create(cartLineRepository.findByCartIdAndItemId(1L, 999L))
            .verifyComplete();
    }
}