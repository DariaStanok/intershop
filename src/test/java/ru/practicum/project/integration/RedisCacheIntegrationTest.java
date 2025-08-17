package ru.practicum.project.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.config.TestSchemaInitializer;
import ru.practicum.project.model.Item;
import ru.practicum.project.repository.ItemRepository;
import ru.practicum.project.service.ItemQueryService;
import ru.practicum.project.service.PaymentClient;

@SpringBootTest(properties = {
        "spring.r2dbc.init.enabled=false",
        "spring.sql.init.mode=never"
})
@AutoConfigureWebTestClient
@Testcontainers
@Import(TestSchemaInitializer.class)
class RedisCacheIntegrationTest {

    @Container
    static final GenericContainer<?> redis =
            new GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", () -> redis.getHost());
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private DatabaseClient client;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemQueryService itemQueryService;

    @Autowired
    private ModelMapper modelMapper;

    @MockBean
    private PaymentClient paymentClient;

    private Long itemId;

    @BeforeEach
    void setUp() {
        StepVerifier.create(
                Mono.when(
                        client.sql("DELETE FROM order_item").then(),
                        client.sql("DELETE FROM orders").then(),
                        client.sql("DELETE FROM cart_lines").then(),
                        client.sql("DELETE FROM items").then(),
                        client.sql("DELETE FROM carts").then()
                ).then(client.sql("INSERT INTO carts(id) VALUES (1)").then())
        ).verifyComplete();

        Item saved = itemRepository.save(
                new Item(null, "Cached item", "desc", 250, "/img.jpg", 0)
        ).block();
        this.itemId = saved.getId();
        when(paymentClient.withdraw(anyLong(), anyString()))
                .thenReturn(Mono.just(true));
    }

    @Test
    void itemIsServedFromRedisAfterFirstLoad() {
        StepVerifier.create(itemQueryService.getItemById(itemId))
                .assertNext(dto -> {
                    assertThat(dto.getId()).isEqualTo(itemId);
                    assertThat(dto.getTitle()).isEqualTo("Cached item");
                    assertThat(dto.getPrice()).isEqualTo(250);
                })
                .verifyComplete();
        StepVerifier.create(itemRepository.deleteAll()).verifyComplete();

        StepVerifier.create(itemQueryService.getItemById(itemId))
                .assertNext(dto -> {
                    assertThat(dto.getId()).isEqualTo(itemId);
                    assertThat(dto.getTitle()).isEqualTo("Cached item"); 
                    assertThat(dto.getPrice()).isEqualTo(250);
                })
                .verifyComplete();
    }

    @Test
    void notFoundWhenNotInCacheAndDb() {
        Long missingId = 999999L;
        StepVerifier.create(itemQueryService.getItemById(missingId))
                .expectErrorMatches(ex -> ex.getClass().getSimpleName().equals("ItemNotFoundException"))
                .verify();
    }
}
