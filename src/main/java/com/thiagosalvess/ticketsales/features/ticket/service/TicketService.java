package com.thiagosalvess.ticketsales.features.ticket.service;

import com.thiagosalvess.ticketsales.common.exception.BadRequestException;
import com.thiagosalvess.ticketsales.common.exception.NotFoundException;
import com.thiagosalvess.ticketsales.common.exception.OwnershipException;
import com.thiagosalvess.ticketsales.features.event.model.entity.Event;
import com.thiagosalvess.ticketsales.features.event.repository.EventRepository;
import com.thiagosalvess.ticketsales.features.ticket.model.dto.TicketResponse;
import com.thiagosalvess.ticketsales.features.ticket.model.entity.Ticket;
import com.thiagosalvess.ticketsales.features.ticket.model.enums.TicketStatus;
import com.thiagosalvess.ticketsales.features.ticket.repository.TicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class TicketService {
    private final TicketRepository tickets;
    private final EventRepository events;

    public TicketService(TicketRepository tickets, EventRepository events) {
        this.tickets = tickets;
        this.events = events;
    }

    @Transactional
    public void createMany(Long eventId, Long partnerId, int numTickets, BigDecimal price) {
        if (numTickets <= 0) throw new BadRequestException("numTickets must be greater then 0");
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("price must be bigger then 0");
        }
        Event event = events.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        if (event.getPartner() == null || !event.getPartner().getId().equals(partnerId)) {
            throw new OwnershipException("You can only manage tickets of your own events");
        }

        List<Ticket> batch = new ArrayList<>(numTickets);
        for (int i = 0; i < numTickets; i++) {
            batch.add(Ticket.builder()
                    .location("Location " + i)
                    .event(event)
                    .price(price)
                    .status(TicketStatus.AVAILABLE)
                    .build());
        }
        tickets.saveAll(batch);
    }

    public List<TicketResponse> findByEventId(Long eventId) {
        events.findById(eventId).orElseThrow(() -> new NotFoundException("Event not Found"));
        return tickets.findAllByEventId(eventId).stream().map(this::toResponse).toList();
    }

    public TicketResponse findById(Long eventId, Long ticketId) {
        var ticket = tickets.findById(ticketId).orElseThrow(() -> new NotFoundException("Ticket not Found"));
        if (!ticket.getEvent().getId().equals(eventId)) {
            throw new BadRequestException("Ticket does not belong to the given event");
        }
        return toResponse(ticket);
    }

    private TicketResponse toResponse(Ticket t) {
        return new TicketResponse(
                t.getId(),
                t.getLocation(),
                t.getEvent().getId(),
                t.getPrice(),
                t.getStatus().name(),
                t.getCreatedAt()
        );
    }
}
