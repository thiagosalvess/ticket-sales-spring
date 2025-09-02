package com.thiagosalvess.ticketsales.features.ticket.service;

import com.thiagosalvess.ticketsales.common.exception.BadRequestException;
import com.thiagosalvess.ticketsales.common.exception.NotFoundException;
import com.thiagosalvess.ticketsales.common.exception.OwnershipException;
import com.thiagosalvess.ticketsales.features.event.model.entity.Event;
import com.thiagosalvess.ticketsales.features.event.repository.EventRepository;
import com.thiagosalvess.ticketsales.features.partner.model.entity.Partner;
import com.thiagosalvess.ticketsales.features.ticket.model.entity.Ticket;
import com.thiagosalvess.ticketsales.features.ticket.model.enums.TicketStatus;
import com.thiagosalvess.ticketsales.features.ticket.repository.TicketRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

public class TicketServiceTest {
    TicketRepository tickets = mock(TicketRepository.class);
    EventRepository events = mock(EventRepository.class);
    TicketService service;

    @BeforeEach
    public void setUp() {
        service = new TicketService(tickets, events);
    }

    @Test
    public void shouldCreateManyWhenAllArgsAreRight() {
        var partner = Partner.builder().id(10L).build();
        var event = Event.builder().id(1L).partner(partner).build();
        when(events.findById(1L)).thenReturn(Optional.of(event));

        ArgumentCaptor<List<Ticket>> captor = ArgumentCaptor.forClass(List.class);

        service.createMany(1L, 10L, 3, new BigDecimal("80.00"));

        verify(tickets).saveAll(captor.capture());
        var saved = captor.getValue();
        assertThat(saved).hasSize(3);
        assertThat(saved).allMatch(t -> t.getStatus() == TicketStatus.AVAILABLE);
        assertThat(saved).allSatisfy(t -> {
            assertThat(t.getEvent().getId()).isEqualTo(1L);
            assertThat(t.getPrice()).isEqualByComparingTo("80.00");
        });
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenEventNotFound() {
        when(events.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createMany(1L, 10L, 5, new BigDecimal("50.00")))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Event not found");
    }

    @Test
    public void shouldThrowOwnershipExceptionWhenPartnerIsTheSameOnTheEvent() {
        var partner = Partner.builder().id(99L).build();
        var event = Event.builder().id(1L).partner(partner).build();
        when(events.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.createMany(1L, 10L, 5, new BigDecimal("50.00")))
                .isInstanceOf(OwnershipException.class);
    }

    @Test
    public void shouldThrownBadRequestExceptionWhenInvalidArgs() {
        var partner = Partner.builder().id(10L).build();
        var event = Event.builder().id(1L).partner(partner).build();
        when(events.findById(1L)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> service.createMany(1L, 10L, 0, new BigDecimal("50"))).isInstanceOf(BadRequestException.class);
        assertThatThrownBy(() -> service.createMany(1L, 10L, 5, new BigDecimal("0"))).isInstanceOf(BadRequestException.class);
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenEvetnIsMissing() {
        when(events.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findByEventId(1L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldThrowBadRequestWhenTicketNotBelongsToEvent() {
        var event = Event.builder().id(2L).build();
        var ticket = Ticket.builder().id(5L).event(event).build();
        when(tickets.findById(5L)).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> service.findById(1L, 5L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("does not belong");
    }
}
