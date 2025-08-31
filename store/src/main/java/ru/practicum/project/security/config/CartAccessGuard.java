package ru.practicum.project.security.config;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.practicum.project.exception.ResponseStatusException;
import ru.practicum.project.repository.CartRepository;
import ru.practicum.project.security.utils.SecurityUtils;

@Component
@RequiredArgsConstructor
public class CartAccessGuard {
	 private final CartRepository cartRepository;
	 
	 public Mono<Void> requireOwner(Long cartId) {
	        return SecurityUtils.currentUsername()
	            .switchIfEmpty(Mono.error(new ResponseStatusException())) 
	            .flatMap(username ->
	                cartRepository.existsByIdAndOwnerUsername(cartId, username)
	                    .flatMap(owns -> owns
	                        ? Mono.empty()
	                        : Mono.error(new ResponseStatusException())) 
	            );
	    }
}
