package ru.practicum.project.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.project.model.Order;

@Repository
public interface OrderRepository extends R2dbcRepository <Order, Long>{
	
	Flux<Order> findByUsername(String username);
	Mono<Order> findByIdAndUsername(Long id, String username);

}
