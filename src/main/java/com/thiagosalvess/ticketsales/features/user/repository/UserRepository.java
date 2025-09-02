package com.thiagosalvess.ticketsales.features.user.repository;

import com.thiagosalvess.ticketsales.features.user.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);
}
