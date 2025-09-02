package com.thiagosalvess.ticketsales.features.purchase.repository;

import com.thiagosalvess.ticketsales.features.purchase.model.entity.PurchaseTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PurchaseTicketRepository extends JpaRepository<PurchaseTicket, Long> {
    List<PurchaseTicket> findAllByPurchaseId(Long purchaseId);
}
