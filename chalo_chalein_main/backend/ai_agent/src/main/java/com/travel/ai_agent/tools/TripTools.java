package com.travel.ai_agent.tools;

import com.travel.ai_agent.dto.TripDto.*;
import com.travel.ai_agent.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TripTools {

    private final SearchService service;

    @Tool(description = """
            Search the trips listed on the Chalo Chalein website right now (verified organisers, future dates,
            seats available). Call this as soon as the traveller mentions ANY preference. Every parameter is
            optional: pass only what the traveller actually said. Budget is PER PERSON in INR for the whole trip.
            Returns up to 5 ranked trips; exactMatch=false means filters were relaxed and the note says how.
            """)
    public SearchResult searchTrips(
            @ToolParam(description = "Destination, state or region, e.g. 'Spiti', 'Himachal', 'Kerala'", required = false) String destination,
            @ToolParam(description = "Maximum price per person in INR for the whole trip", required = false) Integer maxBudgetPerPerson,
            @ToolParam(description = "Minimum trip length in days", required = false) Integer minDays,
            @ToolParam(description = "Maximum trip length in days", required = false) Integer maxDays,
            @ToolParam(description = "Number of travellers in the group (seats needed)", required = false) Integer travelers,
            @ToolParam(description = "Wanted activities or keywords, e.g. ['trekking','camping','surfing','stargazing']", required = false) List<String> activities,
            @ToolParam(description = "Preferred travel month, e.g. 'November'", required = false) String month,
            @ToolParam(description = "Trip vibe, one of: High Altitude Trek, Backpacking & Culture, Road Trip, Beach & Chill, Weekend Getaway", required = false) String category) {

        log.info("[TOOL] searchTrips dest={} budget={} days={}-{} people={} activities={} month={} category={}",
                destination, maxBudgetPerPerson, minDays, maxDays, travelers, activities, month, category);
        SearchResult result = service.search(new SearchService.Criteria(
                destination, maxBudgetPerPerson, minDays, maxDays, travelers, activities, month, category));
        log.info("[TOOL] searchTrips -> {} trip(s), exactMatch={}", result.count(), result.exactMatch());
        return result;
    }

    @Tool(description = """
            List every trip currently open for booking in a compact form (destination, vibe, start date, days,
            price, seats left). Use when the traveller is vague ("suggest something", "surprise me") or asks
            what is available.
            """)
    public List<DestinationInfo> listDestinations() {
        log.info("[TOOL] listDestinations");
        return service.destinations();
    }

    @Tool(description = "Get full details of one trip (description, itinerary, inclusions, organiser, dates, seats). Accepts the tripId or part of the trip name.")
    public Lookup getTripDetails(
            @ToolParam(description = "tripId such as 'trip-spiti-winter', or a name such as 'Spiti'") String trip) {
        log.info("[TOOL] getTripDetails {}", trip);
        return service.details(trip);
    }

    @Tool(description = "Compare 2 to 4 trips side by side. Use when the traveller asks which of several trips is better, cheaper, easier, etc. Accepts tripIds or part of the trip names.")
    public Comparison compareTrips(
            @ToolParam(description = "tripIds or names, e.g. ['trip-spiti-winter','Ladakh']") List<String> trips) {
        log.info("[TOOL] compareTrips {}", trips);
        return service.compare(trips);
    }

    @Tool(description = """
            Check whether a trip has enough seats for the group and calculate the cost: total price, token to
            pay now to lock seats, balance and balance due date. Use for "can 4 of us go?", "how much for 3 people?".
            """)
    public Quote checkAvailabilityAndCost(
            @ToolParam(description = "tripId or part of the trip name") String trip,
            @ToolParam(description = "Number of travellers") Integer travelers) {
        log.info("[TOOL] checkAvailabilityAndCost {} x{}", trip, travelers);
        return service.quote(trip, travelers == null ? 1 : travelers);
    }
}