package com.thiagosalvess.ticketsales.features.customer.repository;

import com.thiagosalvess.ticketsales.features.customer.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByUserId(Long userId);
    List<Customer> findAllByUserId(Long userId);
}
