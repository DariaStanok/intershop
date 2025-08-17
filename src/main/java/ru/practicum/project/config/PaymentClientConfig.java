package ru.practicum.project.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import ru.practicum.project.store.payment.client.api.PaymentsApi;
import ru.practicum.project.store.payment.client.invoker.ApiClient;

@Configuration
public class PaymentClientConfig {

	@Bean
	ApiClient paymentsApiClient(WebClient.Builder builder,
			@Value("${payments.base-url}") String baseUrl) {
		ApiClient api = new ApiClient(builder.build());
		api.setBasePath(baseUrl);
		return api;
	}

	@Bean
	PaymentsApi paymentsApi(ApiClient apiClient) {
		return new PaymentsApi(apiClient);
	}
}
