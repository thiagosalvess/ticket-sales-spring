package com.thiagosalvess.ticketsales.features.event.model.entity;

import com.thiagosalvess.ticketsales.features.partner.model.entity.Partner;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;


@Entity
@Table(name = "events")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    private String location;

    @Column(name = "created_at")
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "partner_id", nullable = false)
    private Partner partner;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
