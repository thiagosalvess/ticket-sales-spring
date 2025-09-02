package com.thiagosalvess.ticketsales.features.customer.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRegisterRequest(@NotBlank String name,
                                      @Email @NotBlank String email,
                                      @NotBlank String password,
                                      @NotBlank String address,
                                      @NotBlank String phone) {
}
