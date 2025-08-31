package ru.practicum.project.payment.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;


@Configuration
@EnableWebFluxSecurity
public class PaymentSecurityConfig {
	
	  @Bean
	  SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
	    http
	      .csrf(ServerHttpSecurity.CsrfSpec::disable)
	      .authorizeExchange(ex -> ex
	          .pathMatchers("/actuator/**").permitAll()
	          .pathMatchers(HttpMethod.GET,  "/payments/balance/**").hasAuthority("SCOPE_payment.read")
	          .pathMatchers(HttpMethod.POST, "/payments/withdraw", "/payments/deposit").hasAuthority("SCOPE_payment.write")
	          .anyExchange().authenticated()
	      )
	      .oauth2ResourceServer(ServerHttpSecurity.OAuth2ResourceServerSpec::jwt);
	    return http.build();
	  }

}
