package ru.practicum.project.security.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;

import reactor.core.publisher.Mono;

public final class SecurityUtils {
	
	 public static Mono<String> currentUsername() {
		    return ReactiveSecurityContextHolder.getContext()
		        .map(ctx -> ctx.getAuthentication())
		        .filter(Authentication::isAuthenticated)
		        .map(Authentication::getName);
		  }
	 
	 public static Mono<String> currentUsernameOrEmpty() {
		 return ReactiveSecurityContextHolder.getContext()
			        .map(ctx -> ctx.getAuthentication())
			        .map(auth -> auth != null ? auth.getName() : "")
			        .defaultIfEmpty("");
			  }

}
