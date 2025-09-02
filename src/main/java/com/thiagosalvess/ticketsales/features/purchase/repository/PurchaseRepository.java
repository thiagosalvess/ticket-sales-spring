package com.thiagosalvess.ticketsales.features.purchase.repository;

import com.thiagosalvess.ticketsales.features.partner.repository.EventSalesProjection;
import com.thiagosalvess.ticketsales.features.purchase.model.entity.Purchase;
import com.thiagosalvess.ticketsales.features.purchase.model.enums.PurchaseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    @Query("""
        select e.id as eventId,
               e.name as eventName,
               count(pt.id) as ticketsSold,
               coalesce(sum(t.price), 0) as grossRevenue
        from Purchase p
          join PurchaseTicket pt on pt.purchase.id = p.id
          join Ticket t on t.id = pt.ticket.id
          join Event e on e.id = t.event.id
        where e.partner.id = :partnerId
          and p.status = :status
          and (:from is null or p.purchaseDate >= :from)
          and (:to   is null or p.purchaseDate <= :to)
          and (:eventId is null or e.id = :eventId)
        group by e.id, e.name
        """)
    List<EventSalesProjection> aggregateSalesByPartner(
            @Param("partnerId") Long partnerId,
            @Param("status") PurchaseStatus status,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("eventId") Long eventId);

}
