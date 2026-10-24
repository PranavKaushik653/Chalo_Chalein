package com.travel.ai_agent.dto;

import java.util.List;

/** Compact result shapes returned to the model by the tools. */
public final class TripDto {

    private TripDto() {}

    public record TripSummary(
            String tripId, String title, String destination, String category,
            String startDate, int days, int pricePerPerson, int totalForGroup,
            int tokenPerPerson, int seatsLeft, int totalSeats,
            String organizer, Double organizerRating, List<String> matchedActivities) {}

    public record SearchResult(boolean exactMatch, String note, int count, List<TripSummary> trips) {}

    public record DestinationInfo(
            String tripId, String destination, String category, String startDate,
            int days, int pricePerPerson, int seatsLeft) {}

    public record TripDetail(
            String tripId, String title, String destination, String category,
            String startDate, String endDate, int days, int nights,
            int pricePerPerson, int tokenPerPerson, int seatsLeft, int totalSeats,
            String organizer, boolean organizerVerified, Double organizerRating, Integer organizerTripsDone,
            String description, String itinerary, String inclusions) {}

    public record Lookup(boolean found, String message, TripDetail trip) {}

    public record Comparison(List<TripDetail> trips, List<String> notFound) {}

    public record Quote(
            boolean found, boolean canBook, String tripId, String title, int travelers,
            int seatsLeft, int pricePerPerson, int totalCost, int tokenToPayNow,
            int balanceDue, String balanceDueBy, String message) {}
}