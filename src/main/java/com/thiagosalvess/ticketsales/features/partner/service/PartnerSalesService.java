package com.thiagosalvess.ticketsales.features.partner.service;

import com.thiagosalvess.ticketsales.common.exception.BadRequestException;
import com.thiagosalvess.ticketsales.features.partner.model.dto.EventSalesItem;
import com.thiagosalvess.ticketsales.features.partner.model.dto.SalesSummaryResponse;
import com.thiagosalvess.ticketsales.features.purchase.model.enums.PurchaseStatus;
import com.thiagosalvess.ticketsales.features.purchase.repository.PurchaseRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class PartnerSalesService {
    private final PurchaseRepository purchases;

    public PartnerSalesService(PurchaseRepository purchases) {
        this.purchases = purchases;
    }

    public SalesSummaryResponse getSales(Long partnerId, Instant from, Instant to, Long eventId) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new BadRequestException("Invalid date range: 'from' must be less or equal 'to'");
        }

        var rows = purchases.aggregateSalesByPartner(
                partnerId, PurchaseStatus.PAID, from, to, eventId
        );

        var items = rows.stream()
                .map(r -> new EventSalesItem(r.getEventId(), r.getEventName(),
                        r.getTicketsSold(), r.getGrossRevenue()))
                .toList();

        var totalTickets = items.stream().mapToLong(EventSalesItem::ticketsSold).sum();
        var totalRevenue = items.stream()
                .map(EventSalesItem::grossRevenue)
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        return new SalesSummaryResponse(partnerId, from, to, totalTickets, totalRevenue, items);
    }
}
