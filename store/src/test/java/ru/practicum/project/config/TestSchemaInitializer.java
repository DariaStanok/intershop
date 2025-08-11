package ru.practicum.project.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.r2dbc.core.DatabaseClient;

import reactor.core.publisher.Mono;

@TestConfiguration
public class TestSchemaInitializer {

	@Bean
    Mono<Void> initializeSchema(DatabaseClient client) {
        return
            
            client.sql("DROP TABLE IF EXISTS order_item CASCADE").then()
            .then(client.sql("DROP TABLE IF EXISTS cart_lines CASCADE").then())
            .then(client.sql("DROP TABLE IF EXISTS orders CASCADE").then())
            .then(client.sql("DROP TABLE IF EXISTS items CASCADE").then())
            .then(client.sql("DROP TABLE IF EXISTS carts CASCADE").then())

            .then(client.sql("""
                CREATE TABLE carts (
                  id BIGSERIAL PRIMARY KEY
                );
            """).then())
            .then(client.sql("""
                CREATE TABLE items (
                  id BIGSERIAL PRIMARY KEY,
                  title VARCHAR(255) NOT NULL,
                  description TEXT,
                  price INTEGER NOT NULL,
                  img_path VARCHAR(255),
                  views INTEGER NOT NULL DEFAULT 0
                );
            """).then())
            .then(client.sql("""
                CREATE TABLE orders (
                  id BIGSERIAL PRIMARY KEY
                );
            """).then())
            .then(client.sql("""
                CREATE TABLE order_item (
                  id BIGSERIAL PRIMARY KEY,
                  order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                  item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE RESTRICT,
                  count INTEGER NOT NULL
                );
            """).then())
            .then(client.sql("""
                CREATE TABLE cart_lines (
                  id BIGSERIAL PRIMARY KEY,
                  quantity INTEGER NOT NULL,
                  cart_id BIGINT NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
                  item_id BIGINT NOT NULL REFERENCES items(id) ON DELETE RESTRICT
                );
            """).then());
    }
}