package com.travel.ai_agent.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "trips")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organizer_id")
    private Organizer organizer;

    /** Short LLM-friendly handle, e.g. KSL-01 */
    @Column(nullable = false, unique = true, length = 20)
    private String tripCode;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String destination;

    /** State / country, so "Himachal" matches Kasol, Spiti, etc. */
    @Column(length = 100)
    private String region;

    @Column(length = 60)
    private String departureCity;

    @Column(nullable = false)
    private Instant startDate;

    @Column(nullable = false)
    private Instant endDate;

    @Column(nullable = false)
    private int totalSeats;

    @Column(nullable = false)
    private int availableSeats;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tokenAmount;

    /** Price per person */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /** UPCOMING, ONGOING, COMPLETED, CANCELLED */
    @Builder.Default
    @Column(length = 20)
    private String status = "UPCOMING";

    /** Comma separated, e.g. "trekking,camping,bonfire" */
    @Column(length = 300)
    private String activities;

    @Column(length = 1000)
    private String description;

    @Builder.Default
    private Instant createdAt = Instant.now();
}
