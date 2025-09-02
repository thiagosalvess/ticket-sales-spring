package com.thiagosalvess.ticketsales.features.purchase.service;

import com.thiagosalvess.ticketsales.common.exception.*;
import com.thiagosalvess.ticketsales.features.customer.model.entity.Customer;
import com.thiagosalvess.ticketsales.features.customer.repository.CustomerRepository;
import com.thiagosalvess.ticketsales.features.payment.service.PaymentService;
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
import com.thiagosalvess.ticketsales.features.user.model.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PurchaseServiceTest {

    private PurchaseRepository purchases;
    private PurchaseTicketRepository purchaseTickets;
    private ReservationTicketRepository reservations;
    private TicketRepository tickets;
    private CustomerRepository customers;
    private PaymentService paymentService;
    private PurchaseStatusHistoryRepository history;

    private PurchaseService service;

    @BeforeEach
    void setUp() {
        purchases = mock(PurchaseRepository.class);
        purchaseTickets = mock(PurchaseTicketRepository.class);
        reservations = mock(ReservationTicketRepository.class);
        tickets = mock(TicketRepository.class);
        customers = mock(CustomerRepository.class);
        paymentService = mock(PaymentService.class);
        history = mock(PurchaseStatusHistoryRepository.class);

        service = new PurchaseService(
                purchases, purchaseTickets, reservations, tickets, customers, paymentService, history
        );
    }

    @Test
    public void shouldCreatePurchaseAndLinkTicketsWhenAllAvailable() {
        var user = user(10L, "Alice", "a@x.com");
        var customer = customer(100L, user, "Rua X", "9999");
        when(customers.findById(100L)).thenReturn(Optional.of(customer));

        var ticket1 = ticket(1L, new BigDecimal("50.00"), TicketStatus.AVAILABLE);
        var ticket2 = ticket(2L, new BigDecimal("70.00"), TicketStatus.AVAILABLE);
        when(tickets.findAllById(List.of(1L, 2L))).thenReturn(List.of(ticket1, ticket2));

        var saved = purchase(500L, customer, new BigDecimal("120.00"), PurchaseStatus.PENDING);
        when(purchases.save(any(Purchase.class))).thenReturn(saved);

        Long id = service.createPending(100L, List.of(1L, 2L));

        assertThat(id).isEqualTo(500L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Ticket>> captor = ArgumentCaptor.forClass(List.class);
        verify(tickets).saveAll(captor.capture());
        var savedList = captor.getValue();
        assertThat(savedList).hasSize(2);
        assertThat(savedList).allMatch(t -> t.getStatus() == TicketStatus.RESERVED);

        verify(purchaseTickets).saveAll(anyList());
        verify(history).save(any(PurchaseStatusHistory.class));
    }

    @Test
    public void shouldThrowBadRequestWhenNoTicketsSelected() {
        assertThatThrownBy(() -> service.createPending(100L, emptyList()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No tickets selected");
    }

    @Test
    public void shouldThrowNotFoundWhenCustomerDoesNotExist() {
        when(customers.findById(100L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.createPending(100L, List.of(1L)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Customer not found");
    }

    @Test
    public void shouldThrowTicketUnavailableWhenAnyTicketNotAvailable() {
        var user = user(1L, "Bob", "b@x.com");
        var customer = customer(100L, user, "Rua X", "9999");
        when(customers.findById(100L)).thenReturn(Optional.of(customer));

        var available = ticket(1L, new BigDecimal("50.00"), TicketStatus.AVAILABLE);
        var reserved = ticket(2L, new BigDecimal("70.00"), TicketStatus.RESERVED);
        when(tickets.findAllById(List.of(1L, 2L))).thenReturn(List.of(available, reserved));

        assertThatThrownBy(() -> service.createPending(100L, List.of(1L, 2L)))
                .isInstanceOf(TicketUnavailableException.class)
                .hasMessageContaining("not available");

        verify(purchases, never()).save(any());
        verify(purchaseTickets, never()).saveAll(anyList());
        verify(history, never()).save(any());
        verify(tickets, never()).saveAll(anyList());
    }

    @Test
    public void shouldCallGatewayWithCorrectPayerData() {
        var user = user(10L, "Alice", "a@x.com");
        var customer = customer(100L, user, "Rua X", "9999");
        var purchase = purchase(500L, customer, new BigDecimal("120.00"), PurchaseStatus.PENDING);
        when(purchases.findById(500L)).thenReturn(Optional.of(purchase));

        when(paymentService.processPayment(any(), eq(new BigDecimal("120.00")), eq("tok_123")))
                .thenReturn(999L);

        long txId = service.processPayment(customer, 500L, "tok_123");

        assertThat(txId).isEqualTo(999L);
        verify(paymentService).processPayment(
                argThat(pd -> pd.name().equals("Alice")
                        && pd.email().equals("a@x.com")
                        && pd.address().equals("Rua X")
                        && pd.phone().equals("9999")),
                eq(new BigDecimal("120.00")),
                eq("tok_123")
        );
    }

    @Test
    public void shouldThrowNotFoundWhenPurchaseDoesNotExistOnProcessPaymentMethod() {
        var user = user(10L, "A", "a@x.com");
        var customer = customer(100L, user, "Rua X", "9999");
        when(purchases.findById(500L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processPayment(customer, 500L, "tok"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldConfirmAllTicketsAndMarkPaid() {
        var user = user(10L, "Alice", "a@x.com");
        var customer = customer(100L, user, "Rua X", "9999");
        var purchase = purchase(500L, customer, new BigDecimal("120.00"), PurchaseStatus.PENDING);
        when(purchases.findById(500L)).thenReturn(Optional.of(purchase));

        var t1 = ticket(1L, new BigDecimal("10.00"), TicketStatus.RESERVED);
        var t2 = ticket(2L, new BigDecimal("20.00"), TicketStatus.RESERVED);
        var ids = List.of(1L, 2L);
        when(tickets.findAllById(ids)).thenReturn(List.of(t1, t2));

        service.finalizeAsPaid(customer, 500L, ids);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Ticket>> captor = ArgumentCaptor.forClass(List.class);
        verify(tickets).saveAll(captor.capture());
        var saved = captor.getValue();
        assertThat(saved).hasSize(2);
        assertThat(saved).allMatch(t -> t.getStatus() == TicketStatus.SOLD);

        verify(purchases).save(argThat(pp -> pp.getStatus() == PurchaseStatus.PAID));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<ReservationTicket>> resCap = ArgumentCaptor.forClass(List.class);
        verify(reservations).saveAll(resCap.capture());
        assertThat(resCap.getValue()).hasSize(2);
        assertThat(resCap.getValue()).allMatch(r -> r.getStatus() == ReservationStatus.RESERVED);

        verify(history).save(any(PurchaseStatusHistory.class));
    }

    @Test
    public void shouldUpdateStatusAndSaveHistoryOnMarkAsError() {
        var user = user(10L, "A", "a@x.com");
        var customer = customer(100L, user, "Rua X", "9999");
        var purchase = purchase(500L, customer, new BigDecimal("120.00"), PurchaseStatus.PENDING);
        when(purchases.findById(500L)).thenReturn(Optional.of(purchase));

        service.markAsError(500L);

        verify(purchases).save(argThat(pp -> pp.getStatus() == PurchaseStatus.ERROR));
        verify(history).save(any(PurchaseStatusHistory.class));
    }

    @Test
    public void shouldReleaseTicketsAndCancelReservationsWhenCancellingPaid() {
        var user = user(10L, "A", "a@x.com");
        var customer = customer(100L, user, "Rua X", "9999");
        var purchase = purchase(500L, customer, new BigDecimal("120.00"), PurchaseStatus.PAID);
        when(purchases.findById(500L)).thenReturn(Optional.of(purchase));

        var ptList = List.of(
                PurchaseTicket.builder().purchase(purchase).ticket(ticket(1L, new BigDecimal("10"), TicketStatus.SOLD)).build(),
                PurchaseTicket.builder().purchase(purchase).ticket(ticket(2L, new BigDecimal("20"), TicketStatus.SOLD)).build()
        );
        when(purchaseTickets.findAllByPurchaseId(500L)).thenReturn(ptList);

        service.cancel(500L, 100L, "Motivo X");

        // tickets salvos como AVAILABLE
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Ticket>> captor = ArgumentCaptor.forClass(List.class);
        verify(tickets).saveAll(captor.capture());
        var saved = captor.getValue();
        assertThat(saved).extracting(Ticket::getStatus).containsOnly(TicketStatus.AVAILABLE);

        verify(reservations).updateStatusByTicketIds(List.of(1L, 2L),
                ReservationStatus.RESERVED, ReservationStatus.CANCELLED);

        verify(purchases).save(argThat(pp -> pp.getStatus() == PurchaseStatus.CANCELLED));
        verify(history).save(any(PurchaseStatusHistory.class));
    }

    @Test
    public void shouldReleaseTicketsWithoutRefundWhenPending() {
        var user = user(10L, "A", "a@x.com");
        var customer = customer(100L, user, "Rua X", "9999");
        var purchase = purchase(500L, customer, new BigDecimal("120.00"), PurchaseStatus.PENDING);
        when(purchases.findById(500L)).thenReturn(Optional.of(purchase));

        var ptList = List.of(
                PurchaseTicket.builder().purchase(purchase).ticket(ticket(1L, new BigDecimal("10"), TicketStatus.RESERVED)).build()
        );
        when(purchaseTickets.findAllByPurchaseId(500L)).thenReturn(ptList);

        service.cancel(500L, 100L, null);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Ticket>> captor = ArgumentCaptor.forClass(List.class);
        verify(tickets).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getStatus()).isEqualTo(TicketStatus.AVAILABLE);

        verify(purchases).save(argThat(pp -> pp.getStatus() == PurchaseStatus.CANCELLED));
        verify(history).save(any(PurchaseStatusHistory.class));
    }

    @Test
    public void shouldThrowNotFoundWhenPurchaseDoesNotExistOnCancel() {
        when(purchases.findById(500L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.cancel(500L, 100L, "x"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    public void shouldThrowForbiddenWhenPurchaseBelongsToAnotherCustomer() {
        var user = user(10L, "A", "a@x.com");
        var customer = customer(100L, user, "Rua X", "9999");
        var purchase = purchase(500L, customer, new BigDecimal("120.00"), PurchaseStatus.PAID);
        when(purchases.findById(500L)).thenReturn(Optional.of(purchase));

        assertThatThrownBy(() -> service.cancel(500L, 999L, "x"))
                .isInstanceOf(OwnershipException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    public void shouldThrowBusinessExceptionWhenStatusNotCancellable() {
        var user = user(10L, "A", "a@x.com");
        var customer = customer(100L, user, "Rua X", "9999");
        var purchase = purchase(500L, customer, new BigDecimal("120.00"), PurchaseStatus.ERROR);
        when(purchases.findById(500L)).thenReturn(Optional.of(purchase));

        assertThatThrownBy(() -> service.cancel(500L, 100L, "x"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Only PAID or PENDING");
    }

    private User user(Long id, String name, String email) {
        return User.builder().id(id).name(name).email(email).createdAt(Instant.now()).build();
    }

    private Customer customer(Long id, User u, String addr, String phone) {
        return Customer.builder().id(id).user(u).address(addr).phone(phone).createdAt(Instant.now()).build();
    }

    private Ticket ticket(Long id, BigDecimal price, TicketStatus status) {
        return Ticket.builder().id(id).price(price).status(status).createdAt(Instant.now()).build();
    }

    private Purchase purchase(Long id, Customer c, BigDecimal total, PurchaseStatus st) {
        return Purchase.builder().id(id).customer(c).totalAmount(total).status(st).purchaseDate(Instant.now()).build();
    }
}
