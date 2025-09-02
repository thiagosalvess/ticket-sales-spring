package com.thiagosalvess.ticketsales.features.event.service;

import com.thiagosalvess.ticketsales.common.exception.NotFoundException;
import com.thiagosalvess.ticketsales.features.event.model.dto.EventCreateRequestWithPartner;
import com.thiagosalvess.ticketsales.features.event.model.dto.EventResponse;
import com.thiagosalvess.ticketsales.features.event.model.entity.Event;
import com.thiagosalvess.ticketsales.features.event.repository.EventRepository;
import com.thiagosalvess.ticketsales.features.partner.repository.PartnerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {
    private final EventRepository events;
    private final PartnerRepository partners;

    public EventService(EventRepository events, PartnerRepository partners) {
        this.events = events;
        this.partners = partners;
    }

    @Transactional
    public EventResponse create(EventCreateRequestWithPartner dto) {
        var partner = partners.findById(dto.partnerId())
                .orElseThrow(() -> new NotFoundException("Partner not found"));

        Event event = Event.builder()
                .name(dto.name())
                .description(dto.description())
                .eventDate(dto.eventDate())
                .location(dto.location())
                .partner(partner)
                .build();

        event = events.save(event);

        return toResponse(event);
    }

    public List<EventResponse> findAll(Long partnerId) {
        List<Event> list = (partnerId == null)
                ? events.findAll()
                : events.findAllByPartnerId(partnerId);
        return list.stream().map(this::toResponse).toList();
    }

    public EventResponse findById(Long eventId) {
        var event = events.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        return toResponse(event);
    }

    private EventResponse toResponse(Event e) {
        return new EventResponse(
                e.getId(),
                e.getName(),
                e.getDescription(),
                e.getEventDate(),
                e.getLocation(),
                e.getCreatedAt(),
                e.getPartner().getId()
        );
    }
}
