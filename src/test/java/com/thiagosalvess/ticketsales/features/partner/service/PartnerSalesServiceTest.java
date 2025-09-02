package com.thiagosalvess.ticketsales.features.partner.service;

import com.thiagosalvess.ticketsales.features.partner.model.dto.SalesSummaryResponse;
import com.thiagosalvess.ticketsales.features.partner.repository.EventSalesProjection;
import com.thiagosalvess.ticketsales.features.purchase.model.enums.PurchaseStatus;
import com.thiagosalvess.ticketsales.features.purchase.repository.PurchaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
class PartnerSalesServiceTest {
    private PurchaseRepository purchases;
    private PartnerSalesService service;

    interface SalesRow {
        Long getEventId();
        String getEventName();
        long getTicketsSold();
        BigDecimal getGrossRevenue();
    }

    private SalesRow row(long eventId, String name, long tickets, String revenue) {
        return new SalesRow() {
            public Long getEventId() { return eventId; }
            public String getEventName() { return name; }
            public long getTicketsSold() { return tickets; }
            public BigDecimal getGrossRevenue() { return new BigDecimal(revenue); }
        };
    }

    @BeforeEach
    public void setUp() {
        purchases = mock(PurchaseRepository.class);
        service = new PartnerSalesService(purchases);
    }

    @Test
    public void shouldGetSalesSuccessfully() {
        var from = Instant.parse("2025-01-01T00:00:00Z");
        var to = Instant.parse("2025-12-31T23:59:59Z");

        when(purchases.aggregateSalesByPartner(eq(5L), eq(PurchaseStatus.PAID), eq(from), eq(to), isNull()))
                .thenReturn(List.of(
                        proj(1L, "Show A", 10, "1000.00"),
                        proj(2L, "Show B",  5, "250.00")
                ));

        SalesSummaryResponse summary = service.getSales(5L, from, to, null);

        assertThat(summary.partnerId()).isEqualTo(5L);
        assertThat(summary.totalTickets()).isEqualTo(15);
        assertThat(summary.totalRevenue()).isEqualByComparingTo("1250.00");
        assertThat(summary.items()).hasSize(2);
        assertThat(summary.items().get(0).eventName()).isEqualTo("Show A");
    }

    private static EventSalesProjection proj(long eventId, String name, long tickets, String revenue) {
        return new EventSalesProjection() {
            @Override public Long getEventId() { return eventId; }
            @Override public String getEventName() { return name; }
            @Override public Long getTicketsSold() { return tickets; }
            @Override public BigDecimal getGrossRevenue() { return new BigDecimal(revenue); }
        };
    }
}