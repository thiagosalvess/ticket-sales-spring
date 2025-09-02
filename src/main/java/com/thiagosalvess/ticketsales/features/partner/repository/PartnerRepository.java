package com.thiagosalvess.ticketsales.features.partner.repository;

import com.thiagosalvess.ticketsales.features.partner.model.entity.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, Long> {
    Optional<Partner> findByUserId(Long userId);
}
