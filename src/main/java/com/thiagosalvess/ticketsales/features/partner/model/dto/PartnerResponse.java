package com.thiagosalvess.ticketsales.features.partner.model.dto;

import java.time.Instant;

public record PartnerResponse(Long id,
                              String name,
                              Long userId,
                              String companyName,
                              Instant createdAt) {
}
