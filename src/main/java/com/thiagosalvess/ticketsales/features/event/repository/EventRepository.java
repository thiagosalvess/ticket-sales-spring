package com.thiagosalvess.ticketsales.features.event.repository;

import com.thiagosalvess.ticketsales.features.event.model.entity.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    @EntityGraph(attributePaths = "partner")
    List<Event> findAll();

    @EntityGraph(attributePaths = "partner")
    List<Event> findAllByPartnerId(Long partnerId);
}

