package com.thiagosalvess.ticketsales.features.partner.model.dto;

import java.math.BigDecimal;

public record EventSalesItem(Long eventId, String eventName, Long ticketsSold, BigDecimal grossRevenue) {
}
