package com.thiagosalvess.ticketsales.features.purchase.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PurchaseCreateRequest(
        @NotEmpty List<Long> ticket_ids,
        @NotNull String card_token
) {
}
