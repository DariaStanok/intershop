package ru.practicum.project.dao;

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
import ru.practicum.project.repository.ItemRepository;

@DataR2dbcTest(properties = {
	    "spring.r2dbc.init.enabled=false",
	    "spring.sql.init.mode=never"
	})
@Import({TestSchemaInitializer.class, TestDataLoaderConfig.class})
class ItemRepositoryTest {

	    @Autowired
	    private ItemRepository itemRepository;

	    @BeforeEach
	    void init(@Autowired @Qualifier("initializeSchema") Mono<Void> schema,
	              @Autowired @Qualifier("preloadTestData") Mono<Void> preload) {
	        StepVerifier.create(schema.then(preload)).verifyComplete();
	    }

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
