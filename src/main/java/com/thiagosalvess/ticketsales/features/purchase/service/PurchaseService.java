package com.thiagosalvess.ticketsales.features.purchase.service;

import com.thiagosalvess.ticketsales.common.exception.*;
import com.thiagosalvess.ticketsales.features.customer.model.entity.Customer;
import com.thiagosalvess.ticketsales.features.customer.repository.CustomerRepository;
import com.thiagosalvess.ticketsales.features.payment.service.PaymentService;
import com.thiagosalvess.ticketsales.features.purchase.model.dto.PurchaseResponse;
import com.thiagosalvess.ticketsales.features.purchase.model.entity.Purchase;
import com.thiagosalvess.ticketsales.features.purchase.model.entity.PurchaseStatusHistory;
import com.thiagosalvess.ticketsales.features.purchase.model.entity.PurchaseTicket;
import com.thiagosalvess.ticketsales.features.purchase.model.enums.PurchaseStatus;
import com.thiagosalvess.ticketsales.features.purchase.repository.PurchaseRepository;
import com.thiagosalvess.ticketsales.features.purchase.repository.PurchaseStatusHistoryRepository;
import com.thiagosalvess.ticketsales.features.purchase.repository.PurchaseTicketRepository;
import com.thiagosalvess.ticketsales.features.reservation.model.entity.ReservationTicket;
import com.thiagosalvess.ticketsales.features.reservation.model.enums.ReservationStatus;
import com.thiagosalvess.ticketsales.features.reservation.repository.ReservationTicketRepository;
import com.thiagosalvess.ticketsales.features.ticket.model.entity.Ticket;
import com.thiagosalvess.ticketsales.features.ticket.model.enums.TicketStatus;
import com.thiagosalvess.ticketsales.features.ticket.repository.TicketRepository;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

@Service
public class PurchaseService {
    private final PurchaseRepository purchases;
    private final PurchaseTicketRepository purchaseTickets;
    private final ReservationTicketRepository reservations;
    private final TicketRepository tickets;
    private final CustomerRepository customers;
    private final PaymentService paymentService;
    private final PurchaseStatusHistoryRepository history;

    public PurchaseService(PurchaseRepository purchases,
                           PurchaseTicketRepository purchaseTickets,
                           ReservationTicketRepository reservations,
                           TicketRepository tickets,
                           CustomerRepository customers,
                           PaymentService paymentService,
                           PurchaseStatusHistoryRepository history) {
        this.purchases = purchases;
        this.purchaseTickets = purchaseTickets;
        this.reservations = reservations;
        this.tickets = tickets;
        this.customers = customers;
        this.paymentService = paymentService;
        this.history = history;
    }

    @Transactional
    public Long createPending(Long customerId, List<Long> ticketIds) {
        if (ticketIds == null || ticketIds.isEmpty()) {
            throw new BadRequestException("No tickets selected");
        }

        Customer customer = customers.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found"));

        List<Ticket> found = tickets.findAllById(ticketIds);

        if (found.size() != ticketIds.size()) {
            throw new NotFoundException("Some tickets not found");
        }

        boolean anyNotAvailable = found.stream()
                .anyMatch(t -> t.getStatus() != TicketStatus.AVAILABLE);

        if (anyNotAvailable) {
            throw new TicketUnavailableException("Some tickets are not available");
        }

        BigDecimal amount = found.stream()
                .map(Ticket::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Purchase purchase = purchases.save(
                Purchase.builder()
                        .customer(customer)
                        .totalAmount(amount)
                        .status(PurchaseStatus.PENDING)
                        .build()
        );

        List<PurchaseTicket> links = found.stream()
                .map(t -> PurchaseTicket.builder()
                        .purchase(purchase)
                        .ticket(t)
                        .build())
                .toList();
        purchaseTickets.saveAll(links);

        found.forEach(t -> t.setStatus(TicketStatus.RESERVED));
        tickets.saveAll(found);

        history.save(PurchaseStatusHistory.builder()
                .purchase(purchase)
                .fromStatus(null)
                .toStatus(PurchaseStatus.PENDING)
                .reason("Pending created")
                .build());

        return purchase.getId();
    }

    public long processPayment(Customer customer, Long purchaseId, String cardToken) {
        var purchase = purchases.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Purchase not found"));

        var payer = new PaymentService.PayerData(
                customer.getUser().getName(),
                customer.getUser().getEmail(),
                customer.getAddress(),
                customer.getPhone()
        );
        try {
            return paymentService.processPayment(payer, purchase.getTotalAmount(), cardToken);
        } catch (Exception e) {
            throw new PaymentFailedException("Payment failed: " + e.getMessage());
        }
    }

    @Transactional
    public void finalizeAsPaid(Customer customer, Long purchaseId, List<Long> ticketIds) {
        Purchase purchase = purchases.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Purchase not found"));

        List<Ticket> toSell = tickets.findAllById(ticketIds);
        if (toSell.size() != ticketIds.size()) {
            throw new NotFoundException("Some tickets not found");
        }

        if (toSell.stream().anyMatch(t -> t.getStatus() != TicketStatus.RESERVED)) {
            throw new IllegalStateException("Some tickets were not reserved anymore");
        }

        toSell.forEach(t -> t.setStatus(TicketStatus.SOLD));
        tickets.saveAll(toSell);

        var reservationsToSave = toSell.stream()
                .map(t -> ReservationTicket.builder()
                        .customer(customer)
                        .ticket(t)
                        .status(ReservationStatus.RESERVED)
                        .build())
                .toList();
        reservations.saveAll(reservationsToSave);

        purchase.setStatus(PurchaseStatus.PAID);
        purchases.save(purchase);

        history.save(PurchaseStatusHistory.builder()
                .purchase(purchase)
                .fromStatus(PurchaseStatus.PENDING)
                .toStatus(PurchaseStatus.PAID)
                .reason("Payment approved")
                .build());
    }

    @Transactional
    public void markAsError(Long purchaseId) {
        purchases.findById(purchaseId).ifPresent(p -> {
            p.setStatus(PurchaseStatus.ERROR);
            purchases.save(p);
            history.save(PurchaseStatusHistory.builder()
                    .purchase(p)
                    .fromStatus(PurchaseStatus.PENDING)
                    .toStatus(PurchaseStatus.ERROR)
                    .reason("Payment failed")
                    .build());
        });
    }

    public PurchaseResponse findById(Long purchaseId) {
        var purchase = purchases.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Purchase not found"));

        var ticketIds = purchaseTickets.findAllByPurchaseId(purchase.getId())
                .stream()
                .map(pt -> pt.getTicket().getId())
                .toList();

        return new PurchaseResponse(
                purchase.getId(),
                purchase.getCustomer().getId(),
                purchase.getPurchaseDate(),
                purchase.getTotalAmount(),
                purchase.getStatus().name(),
                ticketIds
        );
    }

    @Transactional
    public void cancel(Long purchaseId, Long customerId, String reason) {
        Purchase purchase = purchases.findById(purchaseId)
                .orElseThrow(() -> new NotFoundException("Purchase not found"));

        if (!purchase.getCustomer().getId().equals(customerId)) {
            throw new OwnershipException("Purchase does not belong to customer");
        }

        PurchaseStatus current = purchase.getStatus();

        if (current == PurchaseStatus.CANCELLED) return;

        if (current != PurchaseStatus.PAID && current != PurchaseStatus.PENDING) {
            throw new BusinessException("Only PAID or PENDING purchases can be cancelled");
        }

        var pt = purchaseTickets.findAllByPurchaseId(purchaseId);
        var ticketsToRelease = pt.stream().map(PurchaseTicket::getTicket).toList();

        ticketsToRelease.forEach(t -> t.setStatus(TicketStatus.AVAILABLE));
        tickets.saveAll(ticketsToRelease);

        reservations.updateStatusByTicketIds(
                ticketsToRelease.stream().map(Ticket::getId).toList(),
                ReservationStatus.RESERVED,
                ReservationStatus.CANCELLED
        );

        // (Opcional) refund se pago
        // if (current == PurchaseStatus.PAID) paymentService.refund(purchase.getTotalAmount());

        purchase.setStatus(PurchaseStatus.CANCELLED);
        purchases.save(purchase);

        history.save(PurchaseStatusHistory.builder()
                .purchase(purchase)
                .fromStatus(current)
                .toStatus(PurchaseStatus.CANCELLED)
                .reason(reason)
                .build());
    }
}