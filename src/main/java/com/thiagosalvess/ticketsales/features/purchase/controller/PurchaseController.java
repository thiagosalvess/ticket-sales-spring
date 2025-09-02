package com.thiagosalvess.ticketsales.features.purchase.controller;

import com.thiagosalvess.ticketsales.common.exception.ForbiddenException;
import com.thiagosalvess.ticketsales.features.customer.service.CustomerService;
import com.thiagosalvess.ticketsales.features.purchase.model.dto.CancelPurchaseRequest;
import com.thiagosalvess.ticketsales.features.purchase.model.dto.PurchaseCreateRequest;
import com.thiagosalvess.ticketsales.features.purchase.model.dto.PurchaseResponse;
import com.thiagosalvess.ticketsales.features.purchase.service.PurchaseService;
import com.thiagosalvess.ticketsales.security.UserPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@SecurityRequirement(name = "bearer-key")
@RestController
@RequestMapping("/purchases")
public class PurchaseController {
    private final PurchaseService purchaseService;
    private final CustomerService customerService;

    public PurchaseController(PurchaseService purchaseService,
                              CustomerService customerService) {
        this.purchaseService = purchaseService;
        this.customerService = customerService;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody @Valid PurchaseCreateRequest body,
                                    @AuthenticationPrincipal UserPrincipal user) {
        Long userId = user.getId();

        var customer = customerService.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("Only customers can purchase"));

        Long purchaseId = purchaseService.createPending(customer.getId(), body.ticket_ids());

        try {
            purchaseService.processPayment(customer, purchaseId, body.card_token());
            purchaseService.finalizeAsPaid(customer, purchaseId, body.ticket_ids());
        } catch (Exception e) {
            purchaseService.markAsError(purchaseId);
            throw e;
        }

        PurchaseResponse purchase = purchaseService.findById(purchaseId);

        return ResponseEntity.status(201).body(purchase);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable("id") Long purchaseId,
                                    @AuthenticationPrincipal(expression = "id") Long userId,
                                    @RequestBody(required = false) CancelPurchaseRequest body) {
        var customer = customerService.findByUserId(userId)
                .orElseThrow(() -> new ForbiddenException("Only customers can cancel purchases"));

        var reason = (body != null) ? body.reason() : null;

        purchaseService.cancel(purchaseId, customer.getId(), reason);
        return ResponseEntity.noContent().build(); // 204
    }
}
