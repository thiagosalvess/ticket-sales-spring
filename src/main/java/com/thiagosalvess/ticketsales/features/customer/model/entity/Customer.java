package com.thiagosalvess.ticketsales.features.customer.model.entity;

import com.thiagosalvess.ticketsales.features.user.model.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // equivale ao AUTO_INCREMENT
    private Long id;

    @OneToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column()
    private String address;

    @Column()
    private String phone;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }
}
