package ru.practicum.project.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;

import reactor.test.StepVerifier;
import ru.practicum.project.config.TestDataLoaderConfig;
import ru.practicum.project.config.TestSchemaInitializer;
import ru.practicum.project.repository.ItemRepository;

@DataR2dbcTest
@Import({TestSchemaInitializer.class, TestDataLoaderConfig.class})
class ItemRepositoryTest {

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void testFindByTitleReturnsMatchingItems() {
        StepVerifier.create(itemRepository.findByTitleContainingIgnoreCase("sMaRt"))
            .expectNextCount(2)
            .verifyComplete();
    }

    @Test
    void testFindByTitleReturnsEmpty() {
        StepVerifier.create(itemRepository.findByTitleContainingIgnoreCase("Laptop"))
            .verifyComplete();
    }
}
