package ru.practicum.project.security.service;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;
import ru.practicum.project.security.exception.UsernameNotFoundException;
import ru.practicum.project.security.repository.AppUserRepository;
import ru.practicum.project.security.repository.UserRoleRepository;

@Service
@RequiredArgsConstructor
public class ReactiveUserDetailsServiceImpl implements ReactiveUserDetailsService {

	private final AppUserRepository users;
	private final UserRoleRepository roles;

	@Override
	    public Mono<UserDetails> findByUsername(String username) {
	        return users.findByUsername(username)
	            .switchIfEmpty(Mono.error(new UsernameNotFoundException()))
	            .flatMap(u -> roles.findRoleNamesByUsername(username).collectList()
	                .map(names -> User.withUsername(u.getUsername())
	                    .password(u.getPassword())
	                    .authorities(names.toArray(String[]::new))
	                    .disabled(!u.isEnabled())
	                    .build()
	                )
	            );
	}

}
