package ru.practicum.project.payment.config;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
// глушим продовые плейсхолдеры, чтобы их не резолвило при автоконфиге
@TestPropertySource(properties = {
    "spring.r2dbc.url=",
    "spring.r2dbc.username=",
    "spring.r2dbc.password=",
    "spring.r2dbc.pool.enabled=false",
    "spring.flyway.enabled=false",
    "spring.liquibase.enabled=false"
})
public abstract class PostgresR2dbcTestBase {

  @Container
  protected static final PostgreSQLContainer<?> PG =
      new PostgreSQLContainer<>("postgres:16-alpine");
  
  static {
	    PG.start();
	  }

  @DynamicPropertySource
  static void registerProps(DynamicPropertyRegistry r) {
    r.add("spring.r2dbc.url", () ->
        "r2dbc:postgresql://" + PG.getHost() + ":" + PG.getMappedPort(5432) + "/" + PG.getDatabaseName());
    r.add("spring.r2dbc.username", PG::getUsername);
    r.add("spring.r2dbc.password", PG::getPassword);
  }
}