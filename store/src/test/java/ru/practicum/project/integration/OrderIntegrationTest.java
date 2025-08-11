package ru.practicum.project.integration;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.config.TestSchemaInitializer;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.repository.OrderRepository;
import ru.practicum.project.service.PaymentClient;

@SpringBootTest(properties = {
	    "spring.r2dbc.init.enabled=false",
	    "spring.sql.init.mode=never"
	})
@AutoConfigureWebTestClient
@Import(TestSchemaInitializer.class)
class OrderIntegrationTest {

    @Autowired private WebTestClient webTestClient;
    @Autowired private ItemRepository itemRepository;
    @Autowired private CartLineRepository cartLineRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private DatabaseClient client;

    @MockBean
    private PaymentClient paymentClient;

    @BeforeEach
    void setup() {
        StepVerifier.create(
            Mono.when(
                client.sql("DELETE FROM order_item").then(),
                client.sql("DELETE FROM orders").then(),
                client.sql("DELETE FROM cart_lines").then(),
                client.sql("DELETE FROM items").then(),
                client.sql("DELETE FROM carts").then()
            ).then(client.sql("INSERT INTO carts(id) VALUES (999)").then())
        ).verifyComplete();

        Item savedItem = itemRepository.save(
            new Item(null, "Test item", "desc", 100, "/img.jpg", 0)
        ).block();

        cartLineRepository.save(
            new CartLine(null, 2, 999L, savedItem.getId())
        ).block();

        when(paymentClient.withdraw(anyLong(), anyString()))
        .thenReturn(Mono.just(true));
    }

    @Test
    void shouldCreateOrderAndRedirect() {
        webTestClient.post()
            .uri("/orders/buy?cartId=999")
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueMatches("Location", ".*/orders/\\d+\\?newOrder=true$");
    }
}