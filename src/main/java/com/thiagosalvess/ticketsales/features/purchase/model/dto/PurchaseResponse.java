package com.thiagosalvess.ticketsales.features.purchase.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PurchaseResponse(Long id,
                               Long customer_id,
                               Instant purchase_date,
                               BigDecimal total_amount,
                               String status,
                               List<Long> ticket_ids) {
}
