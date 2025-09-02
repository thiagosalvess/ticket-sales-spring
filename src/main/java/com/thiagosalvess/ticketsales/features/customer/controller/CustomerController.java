package com.thiagosalvess.ticketsales.features.customer.controller;

import com.thiagosalvess.ticketsales.features.customer.model.dto.CustomerRegisterRequest;
import com.thiagosalvess.ticketsales.features.customer.model.dto.CustomerRegisterResponse;
import com.thiagosalvess.ticketsales.features.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customers")
public class CustomerController {
    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public ResponseEntity<CustomerRegisterResponse> register(@RequestBody @Valid CustomerRegisterRequest dto) {
        var result = service.register(dto);
        return ResponseEntity.status(201).body(result);
    }
}
