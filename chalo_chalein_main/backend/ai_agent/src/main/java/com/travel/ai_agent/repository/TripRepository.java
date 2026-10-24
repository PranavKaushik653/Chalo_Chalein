package com.travel.ai_agent.repository;

import com.travel.ai_agent.model.Trip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, String> {

    /** Trips a traveller could lock a seat on right now: future start, enough seats, verified organiser. */
    @Query("""
            select t from Trip t
            where t.startDate > :today
              and t.availableSeats >= :seats
              and t.organizerVerified = true
            """)
    List<Trip> findBookable(@Param("today") LocalDate today, @Param("seats") int seats);

    Optional<Trip> findByIdIgnoreCase(String id);
}