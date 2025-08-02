package ru.practicum.project.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;

import reactor.test.StepVerifier;
import ru.practicum.project.config.TestDataLoaderConfig;
import ru.practicum.project.config.TestSchemaInitializer;
import ru.practicum.project.repository.OrderItemRepository;
import ru.practicum.project.repository.OrderRepository;

@DataR2dbcTest
@Import({TestSchemaInitializer.class, TestDataLoaderConfig.class})
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Test
    void testFindById() {
        StepVerifier.create(orderRepository.findAll().next())
            .assertNext(order -> assertThat(order.getId()).isNotNull())
            .verifyComplete();

        StepVerifier.create(orderRepository.findAll().next()
            .flatMapMany(order -> orderItemRepository.findByOrderId(order.getId()).collectList()))
            .assertNext(items -> {
                assertThat(items).hasSize(2);
                assertThat(items.get(0).getCount()).isPositive();
            })
            .verifyComplete();
    }


		@Test
		void testFindAll() {
		    StepVerifier.create(orderRepository.findAll().collectList())
		        .assertNext(orders -> {
		            assertThat(orders).hasSize(1);
		        })
		        .verifyComplete();
		
		    StepVerifier.create(orderRepository.findAll().next()
		        .flatMapMany(order -> orderItemRepository.findByOrderId(order.getId())))
		        .expectNextCount(2)
		        .verifyComplete();
		}
}
