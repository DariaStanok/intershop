package ru.practicum.project.dao;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.context.annotation.Import;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.config.TestDataLoaderConfig;
import ru.practicum.project.config.TestSchemaInitializer;
import ru.practicum.project.model.Order;
import ru.practicum.project.repository.OrderItemRepository;
import ru.practicum.project.repository.OrderRepository;

@DataR2dbcTest(properties = { "spring.r2dbc.init.enabled=false", "spring.sql.init.mode=never" })
@Import({ TestSchemaInitializer.class, TestDataLoaderConfig.class })
class OrderItemRepositoryTest {

	@Autowired
	private OrderItemRepository orderItemRepository;
	@Autowired
	private OrderRepository orderRepository;

	@BeforeEach
	void init(@Autowired @Qualifier("initializeSchema") Mono<Void> schema,
			@Autowired @Qualifier("preloadTestData") Mono<Void> preload) {
		StepVerifier.create(schema.then(preload)).verifyComplete();
	}

	@Test
	void testFindByOrderIdReturnItems() {
		Mono<Long> savedOrderId = orderRepository.findAll().next().map(Order::getId);
		StepVerifier.create(savedOrderId.flatMapMany(orderItemRepository::findByOrderId))
				.expectNextCount(2) 
				.verifyComplete();
	}

	@Test
	void testFindByOrderIdReturnEmpty() {
		StepVerifier.create(orderItemRepository.findByOrderId(999L))
				.verifyComplete();
	}
}