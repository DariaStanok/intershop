package ru.practicum.project.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.repository.OrderRepository;

@SpringBootTest
@AutoConfigureWebTestClient
class OrderIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartLineRepository cartLineRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Item savedItem;

    @BeforeEach
    void setup() {
        orderRepository.deleteAll().block();
        cartLineRepository.deleteAll().block();
        itemRepository.deleteAll().block();

        savedItem = itemRepository
            .save(new Item(null, "Test item", "desc", 100, "/img.jpg", 0))
            .block();

        cartLineRepository.save(new CartLine(null, 2, 999L, savedItem.getId())).block(); // cartId = 999L
    }

    @Test
    void shouldCreateOrderAndRedirect() {
        webTestClient.post()
            .uri("/orders/buy?cartId=999")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueMatches("Location", "/orders/\\d+\\?newOrder=true");
    }
}
