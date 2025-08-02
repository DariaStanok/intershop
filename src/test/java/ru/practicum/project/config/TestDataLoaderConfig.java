package ru.practicum.project.config;

import java.util.List;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.r2dbc.core.DatabaseClient;

import reactor.core.publisher.Mono;
import ru.practicum.project.model.CartLine;
import ru.practicum.project.model.Item;
import ru.practicum.project.model.Order;
import ru.practicum.project.model.OrderItem;
import ru.practicum.project.repository.CartLineRepository;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.repository.OrderItemRepository;
import ru.practicum.project.repository.OrderRepository;

@TestConfiguration
public class TestDataLoaderConfig {

	@Bean
    Mono<Void> preloadTestData(ItemRepository itemRepository,
                                OrderRepository orderRepository,
                                OrderItemRepository orderItemRepository,
                                CartLineRepository cartLineRepository,
                                DatabaseClient client) {

        return Mono.when(
                itemRepository.deleteAll(),
                orderRepository.deleteAll(),
                orderItemRepository.deleteAll(),
                cartLineRepository.deleteAll(),
                client.sql("DELETE FROM carts").then() 
        ).then(
            client.sql("INSERT INTO carts(id) VALUES (1)").then() 
        ).thenMany(
            Mono.zip(
                itemRepository.save(new Item(null, "Smartphone", "desc", 2000, "img", 0)),
                itemRepository.save(new Item(null, "Smartwatch", "desc", 1000, "img", 0))
            )
        ).flatMap(tuple -> {
            Item item1 = tuple.getT1();
            Item item2 = tuple.getT2();

            return orderRepository.save(new Order())
                .flatMap(order -> {
                    Long orderId = order.getId();
                    return Mono.when(
                        orderItemRepository.saveAll(List.of(
                            new OrderItem(null, orderId, item1.getId(), 2),
                            new OrderItem(null, orderId, item2.getId(), 1)
                        )).then(),
                        cartLineRepository.saveAll(List.of(
                            new CartLine(null, 1, 1L, item1.getId()),
                            new CartLine(null, 2, 1L, item2.getId())
                        )).then()
                    );
                });
        }).then();
    }
}