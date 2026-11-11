package com.travel.ai_agent.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/** One trip exactly as the website stores it in State.trips. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TripSyncItem(
        String id,
        String title,
        String destination,
        String category,
        String duration,
        String startDate,
        String endDate,
        Integer totalSeats,
        Integer availableSeats,
        Integer tokenAmount,
        Integer totalPrice,
        Organizer organizer,
        String description,
        List<Step> itinerary,
        List<String> inclusions) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Organizer(String id, String name, Boolean verified, Double rating,
                            Integer tripsDone, String instagram) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Step(String day, String desc) {}
}