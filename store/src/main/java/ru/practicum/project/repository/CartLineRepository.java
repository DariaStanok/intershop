package ru.practicum.project.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.project.model.CartLine;

public interface CartLineRepository extends R2dbcRepository<CartLine, Long> {
	
	Flux<CartLine> findByCartId(Long cartId);
	Mono<CartLine> findByCartIdAndItemId(Long cartId, Long itemId);
}
