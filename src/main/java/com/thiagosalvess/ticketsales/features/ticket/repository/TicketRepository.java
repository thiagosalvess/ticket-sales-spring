package com.thiagosalvess.ticketsales.features.ticket.repository;

import com.thiagosalvess.ticketsales.features.ticket.model.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByEventId(Long eventId);
}
