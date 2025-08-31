package ru.practicum.project.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockUser;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.web.reactive.server.WebTestClient;

import reactor.core.publisher.Mono;
import ru.practicum.project.config.StoreIntegrationTestBase;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.model.Order;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.repository.OrderRepository;

class OrderIntegrationTest extends StoreIntegrationTestBase {

    @Autowired 
    private WebTestClient webTestClient;
    @Autowired 
    private ItemRepository itemRepository;
    @Autowired 
    private CartLineRepository cartLineRepository;
    @Autowired 
    private OrderRepository orderRepository;
    @Autowired 
    private DatabaseClient db;

    private Long savedItemId;
    private Long cartId;

    @BeforeEach
    void setUpData() {
        db.sql("INSERT INTO carts(owner_username) VALUES (:u)")
          .bind("u", "alice")
          .fetch().rowsUpdated().block();

        cartId = db.sql("SELECT id FROM carts WHERE owner_username = :u")
            .bind("u", "alice")
            .map((row, meta) -> row.get("id", Long.class))
            .one().block();

        Item item = itemRepository.save(new Item(null, "Test item", "desc", 1000, "/img.jpg", 0)).block();
        savedItemId = item.getId();

        cartLineRepository.save(new CartLine(null, 2, cartId, savedItemId)).block();

        Mockito.when(paymentClient.withdraw(2000, "ILS")).thenReturn(Mono.just(Boolean.TRUE));
    }

    @Test
    void shouldCreateOrder_andRedirect_andPersistEntities() {
        WebTestClient client = webTestClient
            .mutateWith(mockUser("alice").roles("USER"))
            .mutateWith(csrf());

        URI location = client.post()
            .uri(b -> b.path("/orders/buy").queryParam("cartId", cartId).build())
            .exchange()
            .expectStatus().is3xxRedirection()
            .expectHeader().valueMatches(HttpHeaders.LOCATION, ".*/orders/\\d+\\?newOrder=true$")
            .returnResult(Void.class)
            .getResponseHeaders()
            .getLocation();

        assertThat(location).isNotNull();

        List<Order> orders = orderRepository.findByUserName("alice").collectList().block();
        assertThat(orders).hasSize(1);
        Long orderId = orders.get(0).getId();

        Map<Long, Integer> itemsById = db.sql("""
            SELECT item_id, "count" AS cnt
            FROM order_item
            WHERE order_id = :id
        """).bind("id", orderId)
          .map((row, meta) -> Map.entry(row.get("item_id", Long.class),
                                        row.get("cnt", Integer.class)))
          .all()
          .collectMap(Entry::getKey, Entry::getValue)
          .block();

        assertThat(itemsById).containsEntry(savedItemId, 2);

        List<CartLine> remaining = cartLineRepository.findByCartId(cartId).collectList().block();
        assertThat(remaining).isEmpty();

        verify(paymentClient).withdraw(2000, "ILS");
    }
}