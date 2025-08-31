package ru.practicum.project.config;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import reactor.test.StepVerifier;

@Import(TestDataLoaderConfig.class)
public abstract class PostgresR2dbcTestBase {
  protected static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("intershop")
          .withUsername("postgres")
          .withPassword("postgres");

  static {
      POSTGRES.start();
  }

  @DynamicPropertySource
  static void r2dbcProps(DynamicPropertyRegistry r) {
      r.add("spring.r2dbc.url", () ->
          "r2dbc:postgresql://" + POSTGRES.getHost() + ":" + POSTGRES.getFirstMappedPort() + "/" + POSTGRES.getDatabaseName());
      r.add("spring.r2dbc.username", POSTGRES::getUsername);
      r.add("spring.r2dbc.password", POSTGRES::getPassword);

      r.add("spring.sql.init.mode", () -> "always");
      r.add("spring.sql.init.continue-on-error", () -> "true");
      r.add("spring.sql.init.schema-locations", () -> "classpath:schema.sql");
  }

  @Autowired protected DatabaseClient db;
  
  @BeforeEach
  void truncateAll() {
  StepVerifier.create(
			db.sql("""
					    TRUNCATE TABLE
					      order_item, cart_lines, orders, carts, items,
					      app_user_roles, app_role, app_user, balances
					    RESTART IDENTITY CASCADE
					""").then()).verifyComplete();
  		}
}


