package com.thiagosalvess.ticketsales.features.purchase.model.entity;

import com.thiagosalvess.ticketsales.features.purchase.model.enums.PurchaseStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "purchase_status_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch= FetchType.LAZY)
    @JoinColumn(name="purchase_id")
    private Purchase purchase;

    @Enumerated(EnumType.STRING)
    @Column(name="from_status")
    private PurchaseStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name="to_status")
    private PurchaseStatus toStatus;

    @Column(columnDefinition="TEXT")
    private String reason;

    @Column(name="changed_at")
    private Instant changedAt;

    @PrePersist
    void prePersist() { if (changedAt == null) changedAt = Instant.now(); }
}
