package com.thiagosalvess.ticketsales.features.purchase.model.entity;

import com.thiagosalvess.ticketsales.features.customer.model.entity.Customer;
import com.thiagosalvess.ticketsales.features.purchase.model.enums.PurchaseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "purchases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional=false)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "purchase_date")
    private Instant purchaseDate;

    @Column(name = "total_amount", precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private PurchaseStatus status;

    @PrePersist
    public void prePersist() {
        if (purchaseDate == null) purchaseDate = Instant.now();
    }
}
