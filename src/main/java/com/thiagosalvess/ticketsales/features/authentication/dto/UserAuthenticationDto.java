package com.thiagosalvess.ticketsales.features.authentication.dto;

import jakarta.validation.constraints.Email;

public record UserAuthenticationDto(
        @Email String email,
        String password){
}
