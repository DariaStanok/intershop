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

@DataR2dbcTest(properties = {
		  "spring.r2dbc.init.enabled=false",
		  "spring.sql.init.mode=never"
		})
@Import({TestSchemaInitializer.class, TestDataLoaderConfig.class})
class CartLineRepositoryTest {

	@Autowired
    private CartLineRepository cartLineRepository;

    private Long item1Id;

    @BeforeEach
    void init(@Autowired @Qualifier("initializeSchema") Mono<Void> schema,
              @Autowired @Qualifier("preloadTestData") Mono<Void> preload,
              @Autowired ItemRepository itemRepository) {
        StepVerifier.create(schema.then(preload)).verifyComplete();
        this.item1Id = itemRepository.findAll()
            .filter(i -> i.getTitle().equals("Smartphone"))
            .map(i -> i.getId())
            .blockFirst(); //
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