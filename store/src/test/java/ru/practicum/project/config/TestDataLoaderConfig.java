package ru.practicum.project.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.r2dbc.core.DatabaseClient;

import reactor.core.publisher.Mono;

@TestConfiguration
public class TestDataLoaderConfig {

	  @Bean(initMethod = "block")
	  @DependsOn("r2dbcScriptDatabaseInitializer")
	    Mono<Void> preloadTestData(DatabaseClient client) {
	        Mono<Void> reset = client.sql("""
	            TRUNCATE TABLE
	              order_item, cart_lines, orders, carts, items,
	              app_user_roles, app_role, app_user, balances
	            RESTART IDENTITY CASCADE
	        """).then();
	        Mono<Void> seed = client.sql("""
	                INSERT INTO app_user(id, username, password, enabled)
	                VALUES (1, 'alice', '{noop}password', true)
	                ON CONFLICT (id) DO NOTHING
	            """).then()
	            .then(client.sql("""
	                INSERT INTO app_role(id, name) VALUES (1, 'ROLE_USER')
	                ON CONFLICT (id) DO NOTHING
	            """).then())
	            .then(client.sql("""
	                INSERT INTO app_role(id, name) VALUES (2, 'ROLE_ADMIN')
	                ON CONFLICT (id) DO NOTHING
	            """).then())
	            .then(client.sql("""
	                INSERT INTO app_user_roles(user_id, role_id) VALUES (1, 1)
	                ON CONFLICT (user_id, role_id) DO NOTHING
	            """).then())
	            .then(client.sql("""
	                INSERT INTO balances(id, username, currency, amount)
	                VALUES (1, 'alice', 'ILS', 50000)
	                ON CONFLICT (id) DO NOTHING
	            """).then())
	            .then(client.sql("""
	                INSERT INTO items(id, title, description, price, img_path, views)
	                VALUES (1,'Smartphone','desc',2000,'img',0)
	                ON CONFLICT (id) DO NOTHING
	            """).then())
	            .then(client.sql("""
	                INSERT INTO items(id, title, description, price, img_path, views)
	                VALUES (2,'Smartwatch','desc',1000,'img',0)
	                ON CONFLICT (id) DO NOTHING
	            """).then())
	            .then(client.sql("""
	                INSERT INTO carts(id, owner_username)
	                VALUES (1, 'alice')
	                ON CONFLICT (id) DO NOTHING
	            """).then())
	            .then(client.sql("""
	                INSERT INTO cart_lines(id, quantity, cart_id, item_id)
	                VALUES (1, 1, 1, 1)
	                ON CONFLICT (id) DO NOTHING
	            """).then())
	            .then(client.sql("""
	                INSERT INTO cart_lines(id, quantity, cart_id, item_id)
	                VALUES (2, 2, 1, 2)
	                ON CONFLICT (id) DO NOTHING
	            """).then())
	            .then(client.sql("""
	                INSERT INTO orders(id, user_name)
	                VALUES (1, 'alice')
	                ON CONFLICT (id) DO NOTHING
	            """).then())
	            .then(client.sql("""
	                INSERT INTO order_item(id, order_id, item_id, count)
	                VALUES (1, 1, 1, 2)
	                ON CONFLICT (id) DO NOTHING
	            """).then())
	            .then(client.sql("""
	                INSERT INTO order_item(id, order_id, item_id, count)
	                VALUES (2, 1, 2, 1)
	                ON CONFLICT (id) DO NOTHING
	            """).then());

	        return reset.then(seed);
	 }
}