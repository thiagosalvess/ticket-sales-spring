package com.thiagosalvess.ticketsales.features.partner.controller;

import com.thiagosalvess.ticketsales.features.partner.service.PartnerSalesService;
import com.thiagosalvess.ticketsales.features.partner.service.PartnerService;
import com.thiagosalvess.ticketsales.security.UserPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@SecurityRequirement(name = "bearer-key")
@RestController
@RequestMapping("/partners/sales")
public class PartnerSalesController {
    private final PartnerService partnerService;
    private final PartnerSalesService salesService;

    public PartnerSalesController(PartnerService partnerService, PartnerSalesService salesService) {
        this.partnerService = partnerService;
        this.salesService = salesService;
    }

    @GetMapping
    public ResponseEntity<?> getSales(@AuthenticationPrincipal UserPrincipal user,
                                      @RequestParam(required = false) Instant from,
                                      @RequestParam(required = false) Instant to,
                                      @RequestParam(required = false) Long eventId) {

        var partner = partnerService.findByUserId(user.getId());

        var summary = salesService.getSales(partner.id(), from, to, eventId);
        return ResponseEntity.ok(summary);
    }

}
