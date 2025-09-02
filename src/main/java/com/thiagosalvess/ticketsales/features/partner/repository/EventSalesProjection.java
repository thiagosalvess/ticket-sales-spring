package com.thiagosalvess.ticketsales.features.partner.repository;

import java.math.BigDecimal;

public interface EventSalesProjection {
    Long getEventId();
    String getEventName();
    Long getTicketsSold();
    BigDecimal getGrossRevenue();
}
