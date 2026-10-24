package com.travel.ai_agent.service;

import com.travel.ai_agent.dto.TripDto.*;
import com.travel.ai_agent.model.Trip;
import com.travel.ai_agent.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.*;

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
            Integer travelers, List<String> activities, String month, String category) {}

    private LocalDate today() {
        return LocalDate.now(IST);
    }

    // ---------------------------------------------------------------- search

    public SearchResult search(Criteria c) {
        int travelers = (c.travelers() == null || c.travelers() < 1) ? 1 : c.travelers();
        List<Trip> pool = tripRepository.findBookable(today(), travelers);

        List<TripSummary> strict = rank(pool, c, travelers, false);
        if (!strict.isEmpty()) {
            return new SearchResult(true, "All requested filters satisfied.", strict.size(), strict);
        }

        List<TripSummary> relaxed = rank(pool, c, travelers, true);
        if (!relaxed.isEmpty()) {
            return new SearchResult(false,
                    "No trip matched every filter. Closest options shown: price stretched by up to 30%, "
                            + "trip length +/- 2 days, and month, vibe and activities used only for ranking, not as requirements.",
                    relaxed.size(), relaxed);
        }

        if (travelers > 1) {
            List<TripSummary> fewerSeats = rank(tripRepository.findBookable(today(), 1), c, travelers, true);
            if (!fewerSeats.isEmpty()) {
                return new SearchResult(false,
                        "No matching trip has " + travelers + " free seats. These are the closest trips, "
                                + "but they have fewer seats left than the group needs.",
                        fewerSeats.size(), fewerSeats);
            }
        }
        return new SearchResult(false,
                "No bookable trips found for this request. Try another destination or vibe.", 0, List.of());
    }

    private List<TripSummary> rank(List<Trip> pool, Criteria c, int travelers, boolean relaxed) {
        String dest = norm(c.destination());
        String cat = norm(c.category());
        Integer month = parseMonth(c.month());
        List<String> wanted = cleanList(c.activities());

        boolean hasBudget = c.maxBudgetPerPerson() != null && c.maxBudgetPerPerson() > 0;
        double budgetCap = hasBudget
                ? c.maxBudgetPerPerson() * (relaxed ? RELAXED_BUDGET_FACTOR : 1.0)
                : Double.MAX_VALUE;
        int slack = relaxed ? RELAXED_DAY_SLACK : 0;
        int minD = c.minDays() == null ? 0 : Math.max(0, c.minDays() - slack);
        int maxD = c.maxDays() == null ? Integer.MAX_VALUE : c.maxDays() + slack;

        record Scored(Trip trip, List<String> matched, double score) {}
        List<Scored> scored = new ArrayList<>();

        for (Trip t : pool) {
            if (dest != null && !destinationMatches(t, dest)) continue;
            if (t.getTotalPrice() > budgetCap) continue;
            int days = t.getDurationDays();
            if (days < minD || days > maxD) continue;

            String hay = haystack(t);
            List<String> matched = wanted.stream()
                    .filter(w -> stems(w).stream().anyMatch(hay::contains))
                    .toList();
            boolean monthOk = month == null || t.getStartDate().getMonthValue() == month;
            boolean catOk = cat == null || categoryMatches(t, cat);

            if (!relaxed) {
                if (!wanted.isEmpty() && matched.isEmpty()) continue;
                if (!monthOk || !catOk) continue;
            }

            double score = matched.size() * 30.0;
            if (month != null && monthOk) score += 15;
            if (cat != null && catOk) score += 20;
            if (hasBudget) {
                score += 10.0 * (1.0 - Math.min(1.0, (double) t.getTotalPrice() / c.maxBudgetPerPerson()));
            }
            if (t.getAvailableSeats() >= travelers + 3) score += 3;
            if (t.getOrganizerRating() != null) score += t.getOrganizerRating();
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
        return tripRepository.findBookable(today(), 1).stream()
                .sorted(Comparator.comparing(Trip::getStartDate))
                .map(t -> new DestinationInfo(t.getId(), t.getDestination(), t.getCategory(),
                        t.getStartDate().toString(), t.getDurationDays(), t.getTotalPrice(), t.getAvailableSeats()))
                .toList();
    }

    // ---------------------------------------------------------- details/compare

    public Lookup details(String ref) {
        return resolve(ref)
                .map(t -> new Lookup(true, "OK", toDetail(t)))
                .orElseGet(() -> new Lookup(false, "No trip found for " + ref, null));
    }

    public Comparison compare(List<String> refs) {
        List<TripDetail> found = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        if (refs != null) {
            refs.stream().filter(Objects::nonNull).map(String::trim).distinct().limit(4).forEach(ref ->
                    resolve(ref).ifPresentOrElse(t -> found.add(toDetail(t)), () -> missing.add(ref)));
        }
        return new Comparison(found, missing);
    }

    // ------------------------------------------------------------------ quote

    public Quote quote(String ref, int travelers) {
        int n = Math.max(1, travelers);
        Optional<Trip> opt = resolve(ref);
        if (opt.isEmpty()) {
            return new Quote(false, false, ref, null, n, 0, 0, 0, 0, 0, null, "No trip found for " + ref);
        }
        Trip t = opt.get();
        int price = t.getTotalPrice();
        int total = price * n;
        int tokenNow = t.getTokenAmount() * n;
        LocalDate dueBy = t.getStartDate().minusDays(BALANCE_DUE_DAYS_BEFORE);
        if (dueBy.isBefore(today())) dueBy = today();

        boolean open = t.getStartDate().isAfter(today());
        boolean enough = t.getAvailableSeats() >= n;

        String msg;
        if (!open) {
            msg = "This trip is no longer open for booking.";
        } else if (!enough) {
            msg = "Only " + t.getAvailableSeats() + " seat(s) left, not enough for " + n + " travellers.";
        } else {
            msg = "Seats available. Each traveller locks a seat with the token; the balance is due by " + dueBy + ".";
        }
        return new Quote(true, open && enough, t.getId(), t.getTitle(), n, t.getAvailableSeats(),
                price, total, tokenNow, total - tokenNow, dueBy.toString(), msg);
    }

    // ----------------------------------------------------------------- mapping

    private TripSummary toSummary(Trip t, List<String> matched, int travelers) {
        return new TripSummary(
                t.getId(), t.getTitle(), t.getDestination(), t.getCategory(),
                t.getStartDate().toString(), t.getDurationDays(),
                t.getTotalPrice(), t.getTotalPrice() * travelers, t.getTokenAmount(),
                t.getAvailableSeats(), t.getTotalSeats(),
                t.getOrganizerName(), t.getOrganizerRating(), matched);
    }

    private TripDetail toDetail(Trip t) {
        int d = t.getDurationDays();
        return new TripDetail(
                t.getId(), t.getTitle(), t.getDestination(), t.getCategory(),
                t.getStartDate().toString(), t.getEndDate().toString(), d, Math.max(0, d - 1),
                t.getTotalPrice(), t.getTokenAmount(), t.getAvailableSeats(), t.getTotalSeats(),
                t.getOrganizerName(), t.isOrganizerVerified(), t.getOrganizerRating(), t.getOrganizerTripsDone(),
                t.getDescription(), t.getItinerary(), t.getInclusions());
    }

    // ----------------------------------------------------------------- helpers

    /** Accepts a trip id ("trip-spiti-winter") or part of a name ("Spiti"). */
    private Optional<Trip> resolve(String ref) {
        String r = ref == null ? "" : ref.trim();
        if (r.isEmpty()) return Optional.empty();
        Optional<Trip> byId = tripRepository.findByIdIgnoreCase(r);
        if (byId.isPresent()) return byId;
        String q = r.toLowerCase(Locale.ROOT);
        return tripRepository.findAll().stream()
                .filter(t -> (nz(t.getTitle()) + " " + nz(t.getDestination())).toLowerCase(Locale.ROOT).contains(q))
                .findFirst();
    }

    private boolean destinationMatches(Trip t, String dest) {
        return (nz(t.getDestination()) + " " + nz(t.getTitle())).toLowerCase(Locale.ROOT).contains(dest);
    }

    private boolean categoryMatches(Trip t, String cat) {
        String tc = norm(t.getCategory());
        return tc != null && (tc.contains(cat) || cat.contains(tc));
    }

    /** All the trip text the traveller can see, lower-cased, for activity keyword matching. */
    private String haystack(Trip t) {
        return (nz(t.getTitle()) + " " + nz(t.getCategory()) + " " + nz(t.getDescription()) + " "
                + nz(t.getItinerary()) + " " + nz(t.getInclusions())).toLowerCase(Locale.ROOT);
    }

    /** "trekking" -> trekking, trekk, trek (so it matches "trek"). */
    private static Set<String> stems(String w) {
        Set<String> s = new LinkedHashSet<>();
        s.add(w);
        if (w.endsWith("ing") && w.length() > 5) {
            String x = w.substring(0, w.length() - 3);
            s.add(x);
            if (x.length() > 2 && x.charAt(x.length() - 1) == x.charAt(x.length() - 2)) {
                s.add(x.substring(0, x.length() - 1));
            }
            s.add(x + "e");
        }
        if (w.endsWith("s") && w.length() > 3) s.add(w.substring(0, w.length() - 1));
        s.removeIf(x -> x.length() < 3);
        return s;
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

    private static String nz(String s) {
        return s == null ? "" : s;
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