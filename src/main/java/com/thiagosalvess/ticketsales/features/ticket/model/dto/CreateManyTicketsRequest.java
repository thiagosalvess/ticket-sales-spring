package com.thiagosalvess.ticketsales.features.ticket.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateManyTicketsRequest(
        @Min(1) int numTickets,
        @NotNull BigDecimal price
) {
}
