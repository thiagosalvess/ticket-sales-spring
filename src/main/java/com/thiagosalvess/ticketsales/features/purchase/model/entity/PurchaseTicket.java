package com.thiagosalvess.ticketsales.features.purchase.model.entity;

import com.thiagosalvess.ticketsales.features.ticket.model.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "purchase_tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;
}
