package com.travel.ai_agent.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organizers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organizer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String businessName;

    @Column(length = 50)
    private String instagramHandle;

    @Column(length = 15)
    private String gstin;

    /** PENDING, VERIFIED, SUSPENDED */
    @Builder.Default
    @Column(length = 20)
    private String verificationStatus = "PENDING";

    @Builder.Default
    private Instant createdAt = Instant.now();
}