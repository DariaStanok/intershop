package ru.practicum.project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class OAuth2WebClientConfig {

    @Bean
     WebClient webClient(
            ReactiveClientRegistrationRepository clientRegistrations,
            ServerOAuth2AuthorizedClientRepository authorizedClients) {

        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(clientRegistrations, authorizedClients);
        oauth2.setDefaultClientRegistrationId("payment-client"); 
        
        ExchangeFilterFunction userHeader = (request, next) ->
        ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication() != null ? ctx.getAuthentication().getName() : "anonymous")
            .defaultIfEmpty("anonymous")
            .flatMap(username -> {
                ClientRequest.Builder builder = ClientRequest.from(request);
                ClientRequest mutated = builder
                    .headers(h -> h.set("X-User", username))
                    .build();
                return next.exchange(mutated);
            });

        return WebClient.builder()
                .filter(oauth2)
                .filter(userHeader)
                .build();
    }
}
