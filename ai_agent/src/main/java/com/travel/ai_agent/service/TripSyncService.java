package com.travel.ai_agent.service;

import com.travel.ai_agent.dto.TripSyncItem;
import com.travel.ai_agent.model.Trip;
import com.travel.ai_agent.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Temporary bridge: the website keeps trips in the browser, so the widget sends them here
 * before every chat message. When the real trips API exists, delete this and read the real table.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TripSyncService {

    private static final Pattern DAYS = Pattern.compile("(\\d+)\\s*Days?", Pattern.CASE_INSENSITIVE);

    private final TripRepository tripRepository;

    public record Result(int synced, int skipped) {}

    @Transactional
    public Result replaceAll(List<TripSyncItem> items) {
        List<Trip> mapped = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        int skipped = 0;

        for (TripSyncItem item : items == null ? List.<TripSyncItem>of() : items) {
            try {
                Trip t = map(item);
                if (t == null || !seen.add(t.getId())) {
                    skipped++;
                    continue;
                }
                mapped.add(t);
            } catch (Exception e) {
                skipped++;
                log.warn("[SYNC] skipped a trip: {}", e.getMessage());
            }
        }

        tripRepository.deleteAllInBatch();
        tripRepository.saveAll(mapped);
        log.info("[SYNC] {} trips synced from the website, {} skipped", mapped.size(), skipped);
        return new Result(mapped.size(), skipped);
    }

    private Trip map(TripSyncItem i) {
        if (i == null || blank(i.id()) || blank(i.title()) || blank(i.startDate())) return null;

        LocalDate start = LocalDate.parse(i.startDate().trim());
        LocalDate end = blank(i.endDate()) ? start : LocalDate.parse(i.endDate().trim());
        TripSyncItem.Organizer o = i.organizer();

        String itinerary = i.itinerary() == null ? "" : i.itinerary().stream()
                .filter(Objects::nonNull)
                .map(s -> nz(s.day()) + ": " + nz(s.desc()))
                .collect(Collectors.joining(" | "));
        String inclusions = i.inclusions() == null ? "" : String.join("; ", i.inclusions());

        return Trip.builder()
                .id(cut(i.id().trim(), 80))
                .title(cut(i.title(), 250))
                .destination(cut(nz(i.destination()), 200))
                .category(cut(i.category(), 60))
                .durationDays(parseDays(i.duration(), start, end))
                .startDate(start)
                .endDate(end)
                .totalSeats(n(i.totalSeats()))
                .availableSeats(n(i.availableSeats()))
                .tokenAmount(n(i.tokenAmount()))
                .totalPrice(n(i.totalPrice()))
                .organizerName(cut(o == null ? "Unknown organiser" : nz(o.name()), 120))
                .organizerVerified(o != null && Boolean.TRUE.equals(o.verified()))
                .organizerRating(o == null ? null : o.rating())
                .organizerTripsDone(o == null ? null : o.tripsDone())
                .organizerInstagram(cut(o == null ? null : o.instagram(), 60))
                .description(cut(i.description(), 2000))
                .itinerary(cut(itinerary, 4000))
                .inclusions(cut(inclusions, 2000))
                .syncedAt(Instant.now())
                .build();
    }

    /** Prefer the "8 Days" text the website shows; fall back to the date range. */
    private int parseDays(String duration, LocalDate start, LocalDate end) {
        if (duration != null) {
            Matcher m = DAYS.matcher(duration);
            if (m.find()) return Integer.parseInt(m.group(1));
        }
        return (int) Math.max(1, ChronoUnit.DAYS.between(start, end) + 1);
    }

    private static boolean blank(String s) { return s == null || s.isBlank(); }
    private static String nz(String s) { return s == null ? "" : s; }
    private static int n(Integer v) { return v == null ? 0 : v; }
    private static String cut(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}