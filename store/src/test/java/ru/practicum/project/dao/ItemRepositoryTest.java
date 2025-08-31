package ru.practicum.project.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;

import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.practicum.project.config.PostgresR2dbcTestBase;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.ItemRepository;

@DataR2dbcTest
@ActiveProfiles("test")
class ItemRepositoryTest extends PostgresR2dbcTestBase {

    @Autowired ItemRepository itemRepository;

    @Test
    void findByTitleContainingIgnoreCase_returnsMatching() {
        Item a = new Item(null, "Smartphone", "d", 2000, "img", 0);
        Item b = new Item(null, "SmartWatch", "d", 1000, "img", 0);
        Item c = new Item(null, "Book", "d", 100, "img", 0);

        StepVerifier.create(
            itemRepository.saveAll(Flux.just(a,b,c)).thenMany(
                itemRepository.findByTitleContainingIgnoreCase("smart").collectList()
            )
        ).assertNext(list -> {
            assertThat(list).extracting(Item::getTitle)
                .containsExactlyInAnyOrder("Smartphone", "SmartWatch");
        }).verifyComplete();
    }
}
