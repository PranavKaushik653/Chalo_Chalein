package com.travel.ai_agent.dto;

import lombok.Data;

import java.util.List;

@Data
public final class TripDto {

    private TripDto() {}

    public record TripSummary(
            String tripCode,
            String title,
            String destination,
            String region,
            String departureCity,
            String startDate,
            int days,
            int pricePerPerson,
            int totalForGroup,
            int tokenPerPerson,
            int seatsLeft,
            List<String> activities,
            List<String> matchedActivities,
            String organizer
    ) {}

    public record SearchResult(boolean exactMatch, String note, int count, List<TripSummary> trips) {}

    public record DestinationInfo(
            String destination, String region, int upcomingTrips, int cheapestPerPerson,
            int priciestPerPerson, int shortestDays, int longestDays) {}

    public record TripDetail(
            String tripCode, String title, String destination, String region, String departureCity,
            String startDate, String endDate, int days, int nights, int pricePerPerson,
            int tokenPerPerson, int seatsLeft, int totalSeats,
            List<String> activities, String description, String organizer,
            boolean organizerVerified, String status) {}

    public record Lookup(boolean found, String message, TripDetail trip) {}

    public record Comparison(List<TripDetail> trips, List<String> notFound) {}

    public record Quote(
            boolean found, boolean canBook, String tripCode, int travelers, int seatsLeft,
            int pricePerPerson, int totalCost, int tokenToPayNow, int balanceDue,
            String balanceDueBy, String message) {}
}