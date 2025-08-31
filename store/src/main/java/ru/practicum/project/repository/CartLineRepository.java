package ru.practicum.project.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.project.model.CartLine;

@Repository
public interface CartLineRepository extends R2dbcRepository<CartLine, Long> {
	
	Flux<CartLine> findByCartId(Long cartId);
	Mono<CartLine> findByCartIdAndItemId(Long cartId, Long itemId);
	Mono<Long> deleteByCartId(Long cartId);
}
