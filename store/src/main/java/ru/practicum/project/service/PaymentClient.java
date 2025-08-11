package ru.practicum.project.service;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.practicum.project.store.payment.client.api.PaymentsApi;
import ru.practicum.project.store.payment.client.model.AmountRequest;
import ru.practicum.project.store.payment.client.model.OperationResponse;

@Service
@RequiredArgsConstructor
public class PaymentClient {
	
	private final PaymentsApi paymentsApi;
	
	public Mono<Boolean> withdraw(long amountMinor, String currency) {
        AmountRequest req = new AmountRequest();
        req.setAmount(amountMinor);
        req.setCurrency(currency);
        return paymentsApi.withdraw(req)
                .map(op -> Boolean.TRUE.equals(op.getSuccess()));
                
    }

    public Mono<Long> deposit(long amountMinor, String currency) {
        AmountRequest req = new AmountRequest();
        req.setAmount(amountMinor);
        req.setCurrency(currency);
        return paymentsApi.deposit(req)
                .map(OperationResponse::getNewBalance);
    }

}
