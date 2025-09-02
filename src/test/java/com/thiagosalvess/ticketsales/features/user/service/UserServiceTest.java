package com.thiagosalvess.ticketsales.features.user.service;

import com.thiagosalvess.ticketsales.common.exception.NotFoundException;
import com.thiagosalvess.ticketsales.features.user.model.entity.User;
import com.thiagosalvess.ticketsales.features.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {
    private UserRepository users;
    private UserService service;

    @BeforeEach
    void setUp() {
        users = mock(UserRepository.class);
        service = new UserService(users);
    }

    @Test
    public void shouldFindUserByIdSuccessfully() {
        var u = User.builder().id(1L).name("U").email("u@x.com").createdAt(Instant.now()).build();
        when(users.findById(1L)).thenReturn(Optional.of(u));

        var resp = service.findById(1L);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.email()).isEqualTo("u@x.com");
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenUserIsEmpty() {
        when(users.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(9L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldFindByEmailSuccessfully() {
        var user = User.builder().id(2L).name("U2").email("e@x.com").createdAt(Instant.now()).build();
        when(users.findByEmail("e@x.com")).thenReturn(Optional.of(user));

        var resp = service.findByEmail("e@x.com");

        assertThat(resp.id()).isEqualTo(2L);
        assertThat(resp.name()).isEqualTo("U2");
    }

    @Test
    public void shouldThrownNotFoundExceptionWhenUserIsEmpty() {
        when(users.findByEmail("missing@x.com")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByEmail("missing@x.com"))
                .isInstanceOf(NotFoundException.class);
    }
}