package com.thiagosalvess.ticketsales.features.reservation.model.entity;

import com.thiagosalvess.ticketsales.features.customer.model.entity.Customer;
import com.thiagosalvess.ticketsales.features.reservation.model.enums.ReservationStatus;
import com.thiagosalvess.ticketsales.features.ticket.model.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "reservation_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    private ReservationStatus status;

    @Column(name = "reservation_date")
    private Instant reservationDate;

    @PrePersist
    public void prePersist() {
        if (reservationDate == null) reservationDate = Instant.now();
    }
}
