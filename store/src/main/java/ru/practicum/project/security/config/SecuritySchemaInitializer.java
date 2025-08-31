package ru.practicum.project.security.config;

import org.springframework.context.annotation.Profile;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Component
@Profile({"dev","test"})
@RequiredArgsConstructor
public class SecuritySchemaInitializer {
	private final DatabaseClient client;
    private final PasswordEncoder passwordEncoder;
    
    @PostConstruct
    public void init() {
        client.sql("INSERT INTO app_role(name) VALUES ('ROLE_USER') ON CONFLICT (name) DO NOTHING").then()
        .then(client.sql("INSERT INTO app_role(name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING").then())

        .then(client.sql("""
            INSERT INTO app_user(username, password, enabled)
            VALUES (:u, :p, true)
            ON CONFLICT (username) DO NOTHING
        """).bind("u","user")
           .bind("p", passwordEncoder.encode("password"))
           .then())

        .then(client.sql("""
            INSERT INTO app_user(username, password, enabled)
            VALUES (:u, :p, true)
            ON CONFLICT (username) DO NOTHING
        """).bind("u","admin")
           .bind("p", passwordEncoder.encode("admin"))
           .then())

        .then(client.sql("""
            INSERT INTO app_user_roles(user_id, role_id)
            SELECT u.id, r.id FROM app_user u JOIN app_role r ON r.name='ROLE_USER'
            WHERE u.username='user'
            ON CONFLICT (user_id, role_id) DO NOTHING
        """).then())

        .then(client.sql("""
            INSERT INTO app_user_roles(user_id, role_id)
            SELECT u.id, r.id FROM app_user u JOIN app_role r ON r.name='ROLE_ADMIN'
            WHERE u.username='admin'
            ON CONFLICT (user_id, role_id) DO NOTHING
        """).then())

        .subscribe();
    }

}
