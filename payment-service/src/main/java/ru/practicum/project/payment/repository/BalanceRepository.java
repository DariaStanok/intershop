package ru.practicum.project.payment.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import ru.practicum.project.payment.model.Balance;

@Repository
public interface BalanceRepository extends ReactiveCrudRepository<Balance, Long> {
	
    Mono<Balance> findByUsernameAndCurrency(String username, String currency);
}
