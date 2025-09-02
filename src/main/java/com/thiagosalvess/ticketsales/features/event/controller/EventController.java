package com.thiagosalvess.ticketsales.features.event.controller;

import com.thiagosalvess.ticketsales.features.event.model.dto.EventResponse;
import com.thiagosalvess.ticketsales.features.event.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<List<EventResponse>> findAll(@RequestParam(name = "partnerId", required = false) Long partnerId) {
        var result = eventService.findAll(partnerId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> findById(@PathVariable Long eventId) {
        var event = eventService.findById(eventId);
        return ResponseEntity.ok(event);
    }
}
