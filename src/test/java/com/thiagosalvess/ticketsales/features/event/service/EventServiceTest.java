package com.thiagosalvess.ticketsales.features.event.service;

import com.thiagosalvess.ticketsales.common.exception.NotFoundException;
import com.thiagosalvess.ticketsales.features.event.model.dto.EventCreateRequestWithPartner;
import com.thiagosalvess.ticketsales.features.event.model.dto.EventResponse;
import com.thiagosalvess.ticketsales.features.event.model.entity.Event;
import com.thiagosalvess.ticketsales.features.event.repository.EventRepository;
import com.thiagosalvess.ticketsales.features.partner.model.entity.Partner;
import com.thiagosalvess.ticketsales.features.partner.repository.PartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

public class EventServiceTest {
    private EventRepository eventRepository;

    private PartnerRepository partnerRepository;
    private EventService service;

    @BeforeEach
    void setUp() {
        eventRepository = mock(EventRepository.class);
        partnerRepository = mock(PartnerRepository.class);
        service = new EventService(eventRepository, partnerRepository);
    }

    @Test
    public void shouldCreateEventWhenPartnerExists() {
        var partner = Partner.builder().id(10L).build();
        when(partnerRepository.findById(10L)).thenReturn(Optional.of(partner));

        var now = LocalDateTime.now();
        var saved = Event.builder()
                .id(1L).name("Show").description("desc")
                .eventDate(now).location("Arena")
                .partner(partner)
                .createdAt(Instant.now())
                .build();
        when(eventRepository.save(any(Event.class))).thenReturn(saved);

        var req = new EventCreateRequestWithPartner("Show", "description", now, "Arena", 10L);

        EventResponse resp = service.create(req);

        assertThat(resp.id()).isEqualTo(1L);
        assertThat(resp.partnerId()).isEqualTo(10L);
        assertThat(resp.name()).isEqualTo("Show");
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenPartnerIsMissing() {
        when(partnerRepository.findById(99L)).thenReturn(Optional.empty());
        var req = new EventCreateRequestWithPartner("X", null, LocalDateTime.now(), "Location", 3L);

        assertThatThrownBy(() -> service.create(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Partner not found");
    }

    @Test
    public void shouldFinallAllEventWhenPartnerIdIsMissing() {
        var partner = Partner.builder().id(1L).build();
        var event = Event.builder().id(5L).name("A").location("L").eventDate(LocalDateTime.now()).partner(partner).createdAt(Instant.now()).build();
        when(eventRepository.findAll()).thenReturn(List.of(event));

        var list = service.findAll(null);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).id()).isEqualTo(5L);
    }

    @Test
    public void shouldFindAllEventFromThePartnerWhenPartnerIdIsProvided() {
        var partner = Partner.builder().id(2L).build();
        var event = Event.builder().id(6L).name("B").location("LL").eventDate(LocalDateTime.now()).partner(partner).createdAt(Instant.now()).build();
        when(eventRepository.findAllByPartnerId(2L)).thenReturn(List.of(event));

        var list = service.findAll(2L);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).partnerId()).isEqualTo(2L);
    }

    @Test
    public void shouldFindWhenIdIsProvided() {
        var partner = Partner.builder().id(3L).build();
        var event = Event.builder().id(7L).name("C").location("Loc").eventDate(LocalDateTime.now()).partner(partner).createdAt(Instant.now()).build();
        when(eventRepository.findById(7L)).thenReturn(Optional.of(event));

        EventResponse r = service.findById(7L);

        assertThat(r.id()).isEqualTo(7L);
        assertThat(r.partnerId()).isEqualTo(3L);
    }

    @Test
    public void shouldThrowNotFoundExceptionWhenEventIsEmpty() {
        when(eventRepository.findById(77L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(77L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Event not found");
    }

}
