package ru.practicum.project.security.config;

import java.net.URI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
	
	@Bean
	SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers(
                                "/", "/main/items", "/item/**",
                                "/css/**", "/js/**", "/images/**", "/webjars/**",
                                "/login"
                            ).permitAll()
                            .pathMatchers("/cart/**", "/orders/**").authenticated()
                            .anyExchange().permitAll()
                            )
                .formLogin(Customizer.withDefaults())
                .logout(lo -> lo
                    .logoutUrl("/logout")
                    .logoutSuccessHandler(logoutSuccessHandler()) 
                )
                .build();
        }

        @Bean
        ServerLogoutSuccessHandler logoutSuccessHandler() {
            RedirectServerLogoutSuccessHandler handler = new RedirectServerLogoutSuccessHandler();
            handler.setLogoutSuccessUrl(URI.create("/")); 
            return handler;
        }

        @Bean
        PasswordEncoder passwordEncoder() {
            return PasswordEncoderFactories.createDelegatingPasswordEncoder();
        }
    }