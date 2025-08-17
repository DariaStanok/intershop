package ru.practicum.project.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.r2dbc.core.DatabaseClient;

import reactor.core.publisher.Mono;

@TestConfiguration
public class TestDataLoaderConfig {

	@Bean("preloadTestData")
    Mono<Void> preloadTestData(DatabaseClient client) {
        return client.sql("TRUNCATE TABLE order_item, cart_lines, orders, items, carts RESTART IDENTITY CASCADE").then()
            .then(client.sql("INSERT INTO carts(id) VALUES (1)").then())
            .then(client.sql("""
                INSERT INTO items(id, title, description, price, img_path, views)
                VALUES (1,'Smartphone','desc',2000,'img',0)
            """).then())
            .then(client.sql("""
                INSERT INTO items(id, title, description, price, img_path, views)
                VALUES (2,'Smartwatch','desc',1000,'img',0)
            """).then())
     
            .then(client.sql("INSERT INTO orders(id) VALUES (1)").then())
            .then(client.sql("INSERT INTO order_item(id, order_id, item_id, count) VALUES (1,1,1,2)").then())
            .then(client.sql("INSERT INTO order_item(id, order_id, item_id, count) VALUES (2,1,2,1)").then())

            .then(client.sql("INSERT INTO cart_lines(id, quantity, cart_id, item_id) VALUES (1,1,1,1)").then())
            .then(client.sql("INSERT INTO cart_lines(id, quantity, cart_id, item_id) VALUES (2,2,1,2)").then());
    }
}