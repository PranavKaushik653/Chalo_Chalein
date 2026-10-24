package com.travel.ai_agent.service;

import com.travel.ai_agent.dto.TripDto.*;
import com.travel.ai_agent.model.Trip;
import com.travel.ai_agent.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SearchService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final int MAX_RESULTS = 5;
    private static final double RELAXED_BUDGET_FACTOR = 1.30;
    private static final int RELAXED_DAY_SLACK = 2;
    private static final int BALANCE_DUE_DAYS_BEFORE = 3;

    private final TripRepository tripRepository;

    public record Criteria(
            String destination, Integer maxBudgetPerPerson, Integer minDays, Integer maxDays,
            Integer travelers, List<String> activities, String month, String departureCity) {}

    // ---------------------------------------------------------------- search

    public SearchResult search(Criteria c) {
        int travelers = (c.travelers() == null || c.travelers() < 1) ? 1 : c.travelers();
        List<Trip> pool = tripRepository.findBookable(Instant.now(), travelers);

        List<TripSummary> strict = rank(pool, c, travelers, false);
        if (!strict.isEmpty()) {
            return new SearchResult(true, "All requested filters satisfied.", strict.size(), strict);
        }

        List<TripSummary> relaxed = rank(pool, c, travelers, true);
        if (!relaxed.isEmpty()) {
            return new SearchResult(false,
                    "No trip matched every filter. Closest options shown: budget stretched by up to 30%, "
                            + "trip length +/- 2 days, and month/activities used only for ranking, not as requirements.",
                    relaxed.size(), relaxed);
        }
        return new SearchResult(false,
                "No bookable trips found for this destination and group size. Try another destination or fewer travellers.",
                0, List.of());
    }

    private List<TripSummary> rank(List<Trip> pool, Criteria c, int travelers, boolean relaxed) {
        String dest = norm(c.destination());
        String city = norm(c.departureCity());
        Integer month = parseMonth(c.month());
        List<String> wanted = cleanList(c.activities());

        double budgetCap = c.maxBudgetPerPerson() == null ? Double.MAX_VALUE
                : c.maxBudgetPerPerson() * (relaxed ? RELAXED_BUDGET_FACTOR : 1.0);
        int slack = relaxed ? RELAXED_DAY_SLACK : 0;
        int minD = c.minDays() == null ? 0 : Math.max(0, c.minDays() - slack);
        int maxD = c.maxDays() == null ? Integer.MAX_VALUE : c.maxDays() + slack;

        record Scored(Trip trip, List<String> matched, double score) {}
        List<Scored> scored = new ArrayList<>();

        for (Trip t : pool) {
            if (dest != null && !destinationMatches(t, dest)) continue;
            if (t.getTotalAmount().doubleValue() > budgetCap) continue;
            int days = days(t);
            if (days < minD || days > maxD) continue;

            List<String> tags = tags(t);
            String desc = norm(t.getDescription()) == null ? "" : norm(t.getDescription());
            List<String> matched = wanted.stream()
                    .filter(w -> tags.stream().anyMatch(tag -> tag.contains(w) || w.contains(tag)) || desc.contains(w))
                    .toList();
            boolean monthOk = month == null || monthOf(t) == month;

            if (!relaxed) {
                if (!wanted.isEmpty() && matched.isEmpty()) continue;
                if (!monthOk) continue;
            }

            double score = matched.size() * 30.0;
            if (month != null && monthOk) score += 15;
            if (city != null && norm(t.getDepartureCity()) != null && norm(t.getDepartureCity()).contains(city)) score += 10;
            if (c.maxBudgetPerPerson() != null) {
                score += 10.0 * (1.0 - Math.min(1.0, t.getTotalAmount().doubleValue() / c.maxBudgetPerPerson()));
            }
            if (t.getAvailableSeats() >= travelers + 3) score += 3;
            scored.add(new Scored(t, matched, score));
        }

        return scored.stream()
                .sorted(Comparator.comparingDouble(Scored::score).reversed()
                        .thenComparing(s -> s.trip().getStartDate()))
                .limit(MAX_RESULTS)
                .map(s -> toSummary(s.trip(), s.matched(), travelers))
                .toList();
    }

    // ------------------------------------------------------------ destinations

    public List<DestinationInfo> destinations() {
        Map<String, List<Trip>> grouped = tripRepository.findBookable(Instant.now(), 1).stream()
                .collect(Collectors.groupingBy(Trip::getDestination, TreeMap::new, Collectors.toList()));

        return grouped.values().stream().map(list -> {
            Trip first = list.get(0);
            IntSummaryStatistics price = list.stream().mapToInt(t -> t.getTotalAmount().intValue()).summaryStatistics();
            IntSummaryStatistics len = list.stream().mapToInt(this::days).summaryStatistics();
            return new DestinationInfo(first.getDestination(), first.getRegion(), list.size(),
                    price.getMin(), price.getMax(), len.getMin(), len.getMax());
        }).toList();
    }

    // ---------------------------------------------------------- details/compare

    public Lookup details(String tripCode) {
        return tripRepository.findByCode(tripCode == null ? "" : tripCode.trim())
                .map(t -> new Lookup(true, "OK", toDetail(t)))
                .orElseGet(() -> new Lookup(false, "No trip with code " + tripCode, null));
    }

    public Comparison compare(List<String> codes) {
        List<TripDetail> found = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        if (codes != null) {
            codes.stream().filter(Objects::nonNull).map(String::trim).distinct().limit(4).forEach(code ->
                    tripRepository.findByCode(code).ifPresentOrElse(t -> found.add(toDetail(t)), () -> missing.add(code)));
        }
        return new Comparison(found, missing);
    }

    // ------------------------------------------------------------------ quote

    public Quote quote(String tripCode, int travelers) {
        int n = Math.max(1, travelers);
        Optional<Trip> opt = tripRepository.findByCode(tripCode == null ? "" : tripCode.trim());
        if (opt.isEmpty()) {
            return new Quote(false, false, tripCode, n, 0, 0, 0, 0, 0, null, "No trip with code " + tripCode);
        }
        Trip t = opt.get();
        int price = t.getTotalAmount().intValue();
        int token = t.getTokenAmount().intValue();
        int total = price * n;
        int tokenNow = token * n;
        LocalDate dueBy = t.getStartDate().atZone(IST).toLocalDate().minusDays(BALANCE_DUE_DAYS_BEFORE);

        boolean bookable = "UPCOMING".equals(t.getStatus())
                && t.getStartDate().isAfter(Instant.now())
                && t.getAvailableSeats() >= n;

        String msg;
        if (!"UPCOMING".equals(t.getStatus()) || !t.getStartDate().isAfter(Instant.now())) {
            msg = "This trip is no longer open for booking.";
        } else if (t.getAvailableSeats() < n) {
            msg = "Only " + t.getAvailableSeats() + " seat(s) left, not enough for " + n + " travellers.";
        } else {
            msg = "Seats available. Lock them with the token; balance is due by " + dueBy
                    + " (otherwise the booking is cancelled and the token auto-refunded per policy).";
        }
        return new Quote(true, bookable, t.getTripCode(), n, t.getAvailableSeats(), price, total,
                tokenNow, total - tokenNow, dueBy.toString(), msg);
    }

    // ----------------------------------------------------------------- mapping

    private TripSummary toSummary(Trip t, List<String> matched, int travelers) {

        int price = t.getTotalAmount().intValue();

        return new TripSummary(
                t.getTripCode(),
                t.getTitle(),
                t.getDestination(),
                t.getRegion(),
                t.getDepartureCity(),
                t.getStartDate().atZone(IST).toLocalDate().toString(),
                days(t),
                price,
                price * travelers,
                t.getTokenAmount().intValue(),
                t.getAvailableSeats(),
                tags(t),
                matched,
                t.getOrganizer().getBusinessName()
        );
    }


    private TripDetail toDetail(Trip t) {
        int d = days(t);
        return new TripDetail(
                t.getTripCode(), t.getTitle(), t.getDestination(), t.getRegion(), t.getDepartureCity(),
                t.getStartDate().atZone(IST).toLocalDate().toString(),
                t.getEndDate().atZone(IST).toLocalDate().toString(),
                d, Math.max(0, d - 1), t.getTotalAmount().intValue(), t.getTokenAmount().intValue(),
                t.getAvailableSeats(), t.getTotalSeats(), tags(t), t.getDescription(),
                t.getOrganizer().getBusinessName(), "VERIFIED".equals(t.getOrganizer().getVerificationStatus()),
                t.getStatus());
    }

    // ----------------------------------------------------------------- helpers

    private int days(Trip t) {
        LocalDate s = t.getStartDate().atZone(IST).toLocalDate();
        LocalDate e = t.getEndDate().atZone(IST).toLocalDate();
        return (int) ChronoUnit.DAYS.between(s, e) + 1;
    }

    private int monthOf(Trip t) {
        return t.getStartDate().atZone(IST).getMonthValue();
    }

    private boolean destinationMatches(Trip t, String dest) {
        String hay = (norm(t.getDestination()) + " " + norm(t.getRegion()) + " " + norm(t.getTitle()));
        return hay.contains(dest);
    }

    private List<String> tags(Trip t) {
        if (t.getActivities() == null || t.getActivities().isBlank()) return List.of();
        return Arrays.stream(t.getActivities().split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private List<String> cleanList(List<String> in) {
        if (in == null) return List.of();
        return in.stream().map(this::norm).filter(Objects::nonNull).distinct().toList();
    }

    private String norm(String s) {
        if (s == null) return null;
        String v = s.trim().toLowerCase(Locale.ROOT);
        return v.isEmpty() ? null : v;
    }

    /** Accepts "November", "nov", "11". Returns null if absent or unparseable. */
    private Integer parseMonth(String m) {
        String v = norm(m);
        if (v == null) return null;
        try {
            int n = Integer.parseInt(v);
            return (n >= 1 && n <= 12) ? n : null;
        } catch (NumberFormatException ignored) { }
        if (v.length() < 3) return null;
        String prefix = v.substring(0, 3);
        for (Month mo : Month.values()) {
            if (mo.name().toLowerCase(Locale.ROOT).startsWith(prefix)) return mo.getValue();
        }
        return null;
    }
}