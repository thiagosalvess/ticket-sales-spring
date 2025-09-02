package com.thiagosalvess.ticketsales.features.partner.controller;

import com.thiagosalvess.ticketsales.common.exception.NotFoundException;
import com.thiagosalvess.ticketsales.features.event.model.dto.EventCreateRequest;
import com.thiagosalvess.ticketsales.features.event.model.dto.EventCreateRequestWithPartner;
import com.thiagosalvess.ticketsales.features.event.model.dto.EventResponse;
import com.thiagosalvess.ticketsales.features.event.service.EventService;
import com.thiagosalvess.ticketsales.features.partner.model.dto.PartnerRegisterRequest;
import com.thiagosalvess.ticketsales.features.partner.model.dto.PartnerResponse;
import com.thiagosalvess.ticketsales.features.partner.service.PartnerService;
import com.thiagosalvess.ticketsales.security.UserPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/partners")
public class PartnerController {

    private final PartnerService partnerService;
    private final EventService eventService;

    public PartnerController(PartnerService partnerService, EventService eventService) {
        this.partnerService = partnerService;
        this.eventService = eventService;
    }

    @PostMapping("/register")
    public ResponseEntity<PartnerResponse> register(@RequestBody @Valid PartnerRegisterRequest dto) {
        PartnerResponse result = partnerService.register(dto);
        return ResponseEntity.status(201).body(result);
    }

    @SecurityRequirement(name = "bearer-key")
    @PostMapping("/events")
    public ResponseEntity<EventResponse> createEvent(@RequestBody @Valid EventCreateRequest dto,
                                                     @AuthenticationPrincipal UserPrincipal user) {
        Long userId = user.getId();

        var partner = partnerService.findByUserId(userId);

        var req = new EventCreateRequestWithPartner(
                dto.name(),
                dto.description(),
                dto.eventDate(),
                dto.location(),
                partner.id()
        );
        EventResponse result = eventService.create(req);
        return ResponseEntity.status(201).body(result);
    }

    @SecurityRequirement(name = "bearer-key")
    @GetMapping("/events")
    public ResponseEntity<List<EventResponse>> listEvents(@AuthenticationPrincipal UserPrincipal user) {
        Long userId = user.getId();

        var partner = partnerService.findByUserId(userId);

        List<EventResponse> result = eventService.findAll(partner.id());
        return ResponseEntity.ok(result);
    }

    @SecurityRequirement(name = "bearer-key")
    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long eventId,
                                                  @AuthenticationPrincipal UserPrincipal user) {
        Long userId = user.getId();

        var partner = partnerService.findByUserId(userId);

        var event = eventService.findById(eventId);

        if (!event.partnerId().equals(partner.id())) {
            throw new NotFoundException("Event not found");
        }

        return ResponseEntity.ok(event);
    }
}
