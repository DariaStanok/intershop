package ru.practicum.project.security.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Mono;
import ru.practicum.project.security.model.AppUser;

public interface AppUserRepository extends ReactiveCrudRepository<AppUser, Long> {
	
	Mono<AppUser> findByUsername(String username);

}
