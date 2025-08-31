package ru.practicum.project.security.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.project.security.exception.UsernameNotFoundException;
import ru.practicum.project.security.model.AppUser;
import ru.practicum.project.security.repository.AppUserRepository;
import ru.practicum.project.security.repository.UserRoleRepository;

@ExtendWith(MockitoExtension.class)
class ReactiveUserDetailsServiceImplTest {

    @Mock AppUserRepository users;
    @Mock UserRoleRepository roles;

    @InjectMocks ReactiveUserDetailsServiceImpl service;

    @Test
    void findByUsername_success_withRoles_andEnabled() {
        String username = "alice";
        AppUser u = new AppUser(1L, username, "{noop}pwd", true);

        when(users.findByUsername(username)).thenReturn(Mono.just(u));
        when(roles.findRoleNamesByUsername(username)).thenReturn(reactor.core.publisher.Flux.fromIterable(List.of("ROLE_USER", "ROLE_ADMIN")));

        StepVerifier.create(service.findByUsername(username))
            .assertNext((UserDetails ud) -> {
                assertThat(ud.getUsername()).isEqualTo(username);
                assertThat(ud.getPassword()).isEqualTo("{noop}pwd");
                assertThat(ud.isEnabled()).isTrue();         
                assertThat(ud.getAuthorities())
                    .extracting(Object::toString)
                    .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
            })
            .verifyComplete();

        verify(users).findByUsername(username);
        verify(roles).findRoleNamesByUsername(username);
        verifyNoMoreInteractions(users, roles);
    }

    @Test
    void findByUsername_success_noRoles_ok() {
        String username = "bob";
        AppUser u = new AppUser(2L, username, "{noop}secret", true);

        when(users.findByUsername(username)).thenReturn(Mono.just(u));
        when(roles.findRoleNamesByUsername(username)).thenReturn(reactor.core.publisher.Flux.empty());

        StepVerifier.create(service.findByUsername(username))
            .assertNext((UserDetails ud) -> {
                assertThat(ud.getUsername()).isEqualTo(username);
                assertThat(ud.getAuthorities()).isEmpty();   
                assertThat(ud.isEnabled()).isTrue();
            })
            .verifyComplete();

        verify(users).findByUsername(username);
        verify(roles).findRoleNamesByUsername(username);
        verifyNoMoreInteractions(users, roles);
    }

    @Test
    void findByUsername_userDisabled_mapsToDisabled() {
        String username = "charlie";
        AppUser u = new AppUser(3L, username, "{noop}pw", false);

        when(users.findByUsername(username)).thenReturn(Mono.just(u));
        when(roles.findRoleNamesByUsername(username)).thenReturn(reactor.core.publisher.Flux.just("ROLE_USER"));

        StepVerifier.create(service.findByUsername(username))
            .assertNext((UserDetails ud) -> {
                assertThat(ud.isEnabled()).isFalse();        
                assertThat(ud.isAccountNonLocked()).isTrue(); 
            })
            .verifyComplete();

        verify(users).findByUsername(username);
        verify(roles).findRoleNamesByUsername(username);
        verifyNoMoreInteractions(users, roles);
    }

    @Test
    void findByUsername_notFound_emitsUsernameNotFound() {
        String username = "nobody";
        when(users.findByUsername(username)).thenReturn(Mono.empty());

        StepVerifier.create(service.findByUsername(username))
            .expectErrorSatisfies(err -> assertThat(err)
                .isInstanceOf(UsernameNotFoundException.class))
            .verify();


        verify(users).findByUsername(username);
        verifyNoInteractions(roles);
    }

}
