package ru.practicum.project.security.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import reactor.core.publisher.Flux;
import ru.practicum.project.security.model.AppRole;

public interface UserRoleRepository extends ReactiveCrudRepository<AppRole, Long> {

	 @Query("""
		        SELECT ar.name
		        FROM app_role ar
		        JOIN app_user_roles ur ON ur.role_id = ar.id
		        JOIN app_user au ON au.id = ur.user_id
		        WHERE au.username = :username
		    """)
	Flux<String> findRoleNamesByUsername(String username);

}
