package com.thiagosalvess.ticketsales.features.customer.service;

import com.thiagosalvess.ticketsales.features.customer.model.dto.CustomerRegisterRequest;
import com.thiagosalvess.ticketsales.features.customer.model.dto.CustomerRegisterResponse;
import com.thiagosalvess.ticketsales.features.customer.model.entity.Customer;
import com.thiagosalvess.ticketsales.features.customer.repository.CustomerRepository;
import com.thiagosalvess.ticketsales.features.user.model.entity.User;
import com.thiagosalvess.ticketsales.features.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository customers;
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public CustomerService(CustomerRepository customers, UserRepository users, PasswordEncoder encoder) {
        this.customers = customers;
        this.users = users;
        this.encoder = encoder;
    }

    @Transactional
    public CustomerRegisterResponse register(CustomerRegisterRequest req) {
        User user = User.builder()
                .name(req.name())
                .email(req.email())
                .password(encoder.encode(req.password()))
                .build();
        user = users.save(user);

        Customer customer = Customer.builder()
                .user(user)
                .address(req.address())
                .phone(req.phone())
                .build();
        customer = customers.save(customer);

        return new CustomerRegisterResponse(
                customer.getId(),
                user.getName(),
                user.getId(),
                customer.getAddress(),
                customer.getPhone(),
                customer.getCreatedAt()
        );
    }

    public Optional<Customer> findByUserId(Long userId) {
        return customers.findByUserId(userId);
    }
}
