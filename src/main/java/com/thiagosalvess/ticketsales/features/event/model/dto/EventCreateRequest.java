package com.thiagosalvess.ticketsales.features.event.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventCreateRequest(@NotBlank String name,
                                 String description,
                                 @NotNull LocalDateTime eventDate,
                                 @NotBlank String location) {
}
