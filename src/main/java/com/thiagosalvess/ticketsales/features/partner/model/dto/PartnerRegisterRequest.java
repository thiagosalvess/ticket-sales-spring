package com.thiagosalvess.ticketsales.features.partner.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PartnerRegisterRequest(@NotBlank String name,
                                     @Email @NotBlank String email,
                                     @NotBlank String password,
                                     @NotBlank String companyName) {
}
