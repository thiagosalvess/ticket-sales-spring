package com.thiagosalvess.ticketsales.features.ticket.model.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TicketResponse(
        Long id,
        String location,
        Long event_id,      // mantém snake_case do seu Node
        BigDecimal price,
        String status,
        Instant created_at
) {
}
