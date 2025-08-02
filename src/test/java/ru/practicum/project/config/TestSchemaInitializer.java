package ru.practicum.project.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.r2dbc.core.DatabaseClient;

import reactor.core.publisher.Mono;

@TestConfiguration
public class TestSchemaInitializer {

    @Bean(initMethod = "subscribe")
    Mono<Void> initializeSchema(DatabaseClient client) {
        return client.sql("""
                CREATE TABLE IF NOT EXISTS carts (
                    id SERIAL PRIMARY KEY
                );
                """).then()
            .then(client.sql("""
                CREATE TABLE IF NOT EXISTS items (
                    id SERIAL PRIMARY KEY,
                    title VARCHAR(255),
                    description TEXT,
                    price INTEGER,
                    img_path VARCHAR(255),
                    views INTEGER
                );
                """).then())
            .then(client.sql("""
                CREATE TABLE IF NOT EXISTS orders (
                    id SERIAL PRIMARY KEY
                );
                """).then())
            .then(client.sql("""
                CREATE TABLE IF NOT EXISTS order_item (
                    id SERIAL PRIMARY KEY,
                    order_id BIGINT,
                    item_id BIGINT,
                    count INTEGER
                );
                """).then())
            .then(client.sql("""
                CREATE TABLE IF NOT EXISTS cart_lines (
                    id SERIAL PRIMARY KEY,
                    quantity INTEGER,
                    cart_id BIGINT,
                    item_id BIGINT
                );
                """).then());
    }
}