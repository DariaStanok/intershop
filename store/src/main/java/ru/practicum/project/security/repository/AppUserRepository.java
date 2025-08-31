package ru.practicum.project.security.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Mono;
import ru.practicum.project.security.model.AppUser;

@Repository
public interface AppUserRepository extends ReactiveCrudRepository<AppUser, Long> {
	
	Mono<AppUser> findByUsername(String username);

}
