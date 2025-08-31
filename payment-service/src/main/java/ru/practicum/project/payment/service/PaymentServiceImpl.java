package ru.practicum.project.payment.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import reactor.core.publisher.Mono;
import ru.practicum.project.payment.exception.BadAmountException;
import ru.practicum.project.payment.exception.InsufficientFundsException;
import ru.practicum.project.payment.model.Balance;
import ru.practicum.project.payment.repository.BalanceRepository;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final BalanceRepository balances;
    private final long initialBalance;

    public PaymentServiceImpl(BalanceRepository balances,
                              @Value("${payments.initial-balance:50000}") long initialBalance) {
        this.balances = balances;
        this.initialBalance = initialBalance;
    }

    @Override
    @Transactional
    public Mono<Long> deposit(long amount, String currency, String username) {
    	    return balances.findByUsernameAndCurrency(username, currency)
    	        .flatMap(b -> {
    	            b.setAmount(b.getAmount() + amount);
    	            return balances.save(b).map(Balance::getAmount);
    	        })
    	        .switchIfEmpty(
    	            Mono.defer(() ->
    	                balances.save(new Balance(null, username, currency, initialBalance))
    	                        .map(Balance::getAmount)
    	            )
    	        );
    }

    @Override
    @Transactional
    public Mono<Long> withdraw(long amount, String currency, String username) {
        if (amount <= 0) return Mono.error(new BadAmountException());
        return balances.findByUsernameAndCurrency(username, currency)
                .switchIfEmpty(Mono.error(new InsufficientFundsException()))
                .flatMap(b -> {
                    if (b.getAmount() < amount) {
                        return Mono.error(new InsufficientFundsException());
                    }
                    b.setAmount(b.getAmount() - amount);
                    return balances.save(b).map(Balance::getAmount);
                });
    }
}