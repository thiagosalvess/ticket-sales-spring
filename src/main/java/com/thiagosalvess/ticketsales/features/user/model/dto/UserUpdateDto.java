package com.thiagosalvess.ticketsales.features.user.model.dto;

import jakarta.validation.constraints.Email;

public record UserUpdateDto(
        String name,
        @Email String email,
        String password // se vier, vamos re-hash
) {
}
