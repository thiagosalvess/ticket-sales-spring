package com.thiagosalvess.ticketsales.features.purchase.repository;

import com.thiagosalvess.ticketsales.features.purchase.model.entity.PurchaseStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;



public interface PurchaseStatusHistoryRepository extends JpaRepository<PurchaseStatusHistory, Long> {
    List<PurchaseStatusHistory> findAllByPurchaseIdOrderByChangedAtAsc (Long purchaseId);
}
