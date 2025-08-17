package ru.practicum.project.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;

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
import ru.practicum.project.model.Order;
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
    private Long savedItemId;

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
        savedItemId = savedItem.getId();

        cartLineRepository.save(
            new CartLine(null, 2, 999L, savedItemId)
        ).block();

        when(paymentClient.withdraw(anyLong(), anyString()))
            .thenReturn(Mono.just(true));
    }

    @Test
    void shouldCreateOrderAndRedirect() {
        URI location = webTestClient.post()
            .uri("/orders/buy?cartId=999")
            .exchange()
            .expectStatus().is3xxRedirection()
            .returnResult(Void.class)
            .getResponseHeaders()
            .getLocation();

        assertThat(location).isNotNull();
        assertThat(location.toString()).matches(".*/orders/\\d+\\?newOrder=true$");

        List<Order> orders = orderRepository.findAll().collectList().block();
        assertThat(orders).hasSize(1);
        Long orderId = orders.get(0).getId();
        Map<Long, Integer> itemsById = client.sql(
                "SELECT item_id, \"count\" AS cnt FROM order_item WHERE order_id = :id")
            .bind("id", orderId)
            .map((row, meta) -> Map.entry(
                row.get("item_id", Long.class),
                row.get("cnt", Integer.class)
            ))
            .all()
            .collectMap(Map.Entry::getKey, Map.Entry::getValue)
            .block();

        assertThat(itemsById).hasSize(1);
        assertThat(itemsById).containsEntry(savedItemId, 2);
        List<CartLine> remaining = cartLineRepository.findByCartId(999L).collectList().block();
        assertThat(remaining).isEmpty();
        verify(paymentClient).withdraw(200L, "ILS"); 
    }
}