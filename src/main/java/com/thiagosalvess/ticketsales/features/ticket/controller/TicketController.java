package com.thiagosalvess.ticketsales.features.ticket.controller;

import com.thiagosalvess.ticketsales.features.partner.service.PartnerService;
import com.thiagosalvess.ticketsales.features.ticket.model.dto.CreateManyTicketsRequest;
import com.thiagosalvess.ticketsales.features.ticket.model.dto.TicketResponse;
import com.thiagosalvess.ticketsales.features.ticket.service.TicketService;
import com.thiagosalvess.ticketsales.security.UserPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events/{eventId}/tickets")
public class TicketController {
    private final TicketService ticketService;
    private final PartnerService partnerService;

    public TicketController(TicketService ticketService, PartnerService partnerService) {
        this.ticketService = ticketService;
        this.partnerService = partnerService;
    }

    @SecurityRequirement(name = "bearer-key")
    @PostMapping
    public ResponseEntity<?> createMany(@PathVariable Long eventId,
                                        @RequestBody @Valid CreateManyTicketsRequest body,
                                        @AuthenticationPrincipal UserPrincipal user) {
        Long userId = user.getId();

        var partner = partnerService.findByUserId(userId);

        ticketService.createMany(eventId, partner.id(), body.numTickets(), body.price());
        return ResponseEntity.noContent().build(); // 204
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> list(@PathVariable Long eventId) {
        var data = ticketService.findByEventId(eventId);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<?> getOne(@PathVariable Long eventId, @PathVariable Long ticketId) {
        var ticket = ticketService.findById(eventId, ticketId);
        return ResponseEntity.ok(ticket);
    }
}
