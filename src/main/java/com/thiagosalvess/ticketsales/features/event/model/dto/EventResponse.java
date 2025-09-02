package com.thiagosalvess.ticketsales.features.event.model.dto;

import java.time.Instant;
import java.time.LocalDateTime;

public record EventResponse(Long id,
                            String name,
                            String description,
                            LocalDateTime eventDate,
                            String location,
                            Instant createdAt,
                            Long partnerId) {
}
