package com.thiagosalvess.ticketsales.features.reservation.repository;

import com.thiagosalvess.ticketsales.features.reservation.model.entity.ReservationTicket;
import com.thiagosalvess.ticketsales.features.reservation.model.enums.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationTicketRepository extends JpaRepository<ReservationTicket, Long> {
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
           update ReservationTicket r
              set r.status = :newStatus
            where r.ticket.id in :ticketIds
              and r.status = :currentStatus
           """)
    int updateStatusByTicketIds(
            @Param("ticketIds") List<Long> ticketIds,
            @Param("currentStatus") ReservationStatus currentStatus,
            @Param("newStatus") ReservationStatus newStatus
    );
}
