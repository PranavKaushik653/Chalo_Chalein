package com.travel.ai_agent.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @Column(length = 80)
    private String id;

    @Column(nullable = false, length = 250)
    private String title;

    @Column(nullable = false, length = 200)
    private String destination;

    @Column(length = 60)
    private String category;

    @Column(nullable = false)
    private int durationDays;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private int totalSeats;
    private int availableSeats;

    private int tokenAmount;
    private int totalPrice;

    @Column(length = 120)
    private String organizerName;
    private boolean organizerVerified;
    private Double organizerRating;
    private Integer organizerTripsDone;
    @Column(length = 60)
    private String organizerInstagram;

    @Column(length = 2000)
    private String description;

    @Column(length = 4000)
    private String itinerary;

    @Column(length = 2000)
    private String inclusions;

    @Builder.Default
    private Instant syncedAt = Instant.now();

}