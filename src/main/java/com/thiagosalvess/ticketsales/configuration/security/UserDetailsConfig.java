package com.thiagosalvess.ticketsales.configuration.security;

import com.thiagosalvess.ticketsales.features.user.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.*;

import java.util.List;

@Configuration
public class UserDetailsConfig {

    @Bean
    public UserDetailsService userDetailsService(UserRepository repo) {
        return username -> repo.findByEmail(username)
                .map(u -> User.withUsername(u.getEmail())
                        .password(u.getPassword()) // hash BCrypt do banco
                        .authorities(List.of())     // sem roles por enquanto
                        .accountExpired(false)
                        .accountLocked(false)
                        .credentialsExpired(false)
                        .disabled(false)
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Credenciais inválidas"));
    }
}