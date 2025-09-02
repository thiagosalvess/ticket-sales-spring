package com.thiagosalvess.ticketsales.features.event.model.dto;

import com.thiagosalvess.ticketsales.features.partner.model.entity.Partner;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record EventCreateRequestWithPartner(@NotBlank String name,
                                            String description,
                                            @NotNull @Future LocalDateTime eventDate,
                                            @NotBlank String location,
                                            @NotNull Long partnerId) {
}
