package ru.practicum.project.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import ru.practicum.project.model.Cart;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, Long> {
	
	 Mono<Cart> findByOwnerUsername(String ownerUsername);
	 
	 Mono<Boolean> existsByIdAndOwnerUsername(Long id, String ownerUsername);

}
