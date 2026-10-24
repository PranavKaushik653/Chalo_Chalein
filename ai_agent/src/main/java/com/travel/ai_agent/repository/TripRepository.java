package com.travel.ai_agent.repository;

import com.travel.ai_agent.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TripRepository extends JpaRepository<Trip, UUID> {

    @Query("""
            select t from Trip t join fetch t.organizer o
            where t.status = 'UPCOMING'
              and t.startDate > :now
              and t.availableSeats >= :seats
              and o.verificationStatus = 'VERIFIED'
            """)
    List<Trip> findBookable(@Param("now") Instant now, @Param("seats") int seats);

    @Query("select t from Trip t join fetch t.organizer where upper(t.tripCode) = upper(:code)")
    Optional<Trip> findByCode(@Param("code") String code);
}