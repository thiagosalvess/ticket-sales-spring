package com.thiagosalvess.ticketsales.features.customer.model.dto;

import java.time.Instant;

public record CustomerRegisterResponse(Long id,
                                       String name,
                                       Long user_id,
                                       String address,
                                       String phone,
                                       Instant created_at) {
}
