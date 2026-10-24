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
            Search currently bookable group trips (verified organizers, future dates, seats available).
            Call this as soon as the traveller mentions ANY preference. Every parameter is optional:
            pass only what the traveller actually said. Budget is PER PERSON in INR.
            Returns up to 5 ranked trips; exactMatch=false means filters were relaxed and the note says how.
            """)
        public SearchResult searchTrips(
                @ToolParam(description = "Destination, state or region, e.g. 'Kasol', 'Himachal', 'Goa'", required = false) String destination,
                @ToolParam(description = "Maximum budget per person in INR", required = false) Integer maxBudgetPerPerson,
                @ToolParam(description = "Minimum trip length in days", required = false) Integer minDays,
                @ToolParam(description = "Maximum trip length in days", required = false) Integer maxDays,
                @ToolParam(description = "Number of travellers in the group (seats needed)", required = false) Integer travelers,
                @ToolParam(description = "Wanted activities, e.g. ['trekking','camping','beach','rafting']", required = false) List<String> activities,
                @ToolParam(description = "Preferred travel month, e.g. 'November'", required = false) String month,
                @ToolParam(description = "City the traveller departs from, e.g. 'Delhi', 'Bengaluru'", required = false) String departureCity) {

            log.info("[TOOL] searchTrips dest={} budget={} days={}-{} people={} activities={} month={} from={}",
                    destination, maxBudgetPerPerson, minDays, maxDays, travelers, activities, month, departureCity);
            SearchResult result = service.search(new SearchService.Criteria(
                    destination, maxBudgetPerPerson, minDays, maxDays, travelers, activities, month, departureCity));
            log.info("[TOOL] searchTrips -> {} trip(s), exactMatch={}", result.count(), result.exactMatch());
            return result;
        }

        @Tool(description = """
            List every destination that currently has bookable trips, with trip count, price range and trip length.
            Use when the traveller is vague ("suggest something", "where can I go?") or asks what is available.
            """)
        public List<DestinationInfo> listDestinations() {
            log.info("[TOOL] listDestinations");
            return service.destinations();
        }

        @Tool(description = "Get full details of one trip (description, activities, organizer, dates, seats) by its trip code, e.g. 'KSL-01'.")
        public Lookup getTripDetails(
                @ToolParam(description = "Trip code such as KSL-01") String tripCode) {
            log.info("[TOOL] getTripDetails {}", tripCode);
            return service.details(tripCode);
        }

        @Tool(description = "Compare 2 to 4 trips side by side. Use when the traveller asks which of the shortlisted trips is better, cheaper, easier, etc.")
        public Comparison compareTrips(
                @ToolParam(description = "Trip codes to compare, e.g. ['KSL-01','TRN-01']") List<String> tripCodes) {
            log.info("[TOOL] compareTrips {}", tripCodes);
            return service.compare(tripCodes);
        }

        @Tool(description = """
            Check whether a trip has enough seats for the group and calculate the cost:
            total price, token to pay now to lock seats, balance and balance due date.
            Use for questions like "can 5 of us go?", "how much for 4 people?", "how much to lock seats?".
            """)
        public Quote checkAvailabilityAndCost(
                @ToolParam(description = "Trip code such as KSL-01") String tripCode,
                @ToolParam(description = "Number of travellers") Integer travelers) {
            log.info("[TOOL] checkAvailabilityAndCost {} x{}", tripCode, travelers);
            return service.quote(tripCode, travelers == null ? 1 : travelers);
        }
    }


