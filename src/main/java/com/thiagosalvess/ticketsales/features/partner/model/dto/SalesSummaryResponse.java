package com.thiagosalvess.ticketsales.features.partner.model.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SalesSummaryResponse(Long partnerId,
                                   Instant from,
                                   Instant to,
                                   Long totalTickets,
                                   BigDecimal totalRevenue,
                                   List<EventSalesItem> items) {
}
