package com.thiagosalvess.ticketsales.features.user.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateDto(
        @NotBlank String name,
        @Email @NotBlank String email,
        @NotBlank String password
) {}
