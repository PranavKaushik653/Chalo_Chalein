package com.chalochalein.backend.trip;

import com.chalochalein.backend.common.AuditService;
import com.chalochalein.backend.common.ContactPolicy;
import com.chalochalein.backend.common.Sql;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.*;

import static org.hibernate.query.restriction.Restriction.like;

@Service
@RequiredArgsConstructor
public class TripService {

    private static final Set<String> CATEGORIES = Set.of(
            "High Altitude Trek", "Backpacking & Culture", "Road Trip", "Beach & Chill", "Weekend Getaway");
    private static final SecureRandom RNG = new SecureRandom();
    private static final String PUBLIC_FROM = """
            FROM trips t JOIN organizer_profiles o ON o.user_id = t.organizer_id
            WHERE t.status = 'PUBLISHED' AND o.verification_status = 'VERIFIED'
              AND t.start_date > CAST((now() AT TIME ZONE 'Asia/Kolkata') AS date)""";

    private final JdbcClient jdbc;
    private final AuditService audit;

    //records (no separate dto class)

    public record ItineraryItem(
            @Min(1) @Max(60) int dayNumber,
            @NotBlank @Size(max=150) String title,
            @NotBlank @Size(max=2000) String details
    ){}

    public record TripRequest(
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 150) String destination,
            @NotBlank String category,
            @Size(max = 60) String departureCity,
            @NotBlank @Size(max = 5000) String description,
            @Pattern(regexp = "^https://\\S{1,480}$") String coverImageUrl,
            @Size(max = 20) List<@NotBlank @Size(max = 200) String> inclusions,
            @NotNull @Future LocalDate startDate,
            @NotNull LocalDate endDate,
            @Min(1) @Max(200) int totalSeats,
            @Min(1) @Max(1_000_000) int priceInr,
            @Min(1) @Max(1_000_000) int tokenInr,
            @Size(max = 30) List<@Valid ItineraryItem> itinerary
    ){}

    public record TripCard(String slug, String title, String destination, String category, LocalDate startDate,
                           int days, long priceInr, long tokenInr, int seatsLeft, String organizer, String coverImageUrl) {}
    public record TripPage(List<TripCard> items, int page, int size, long total) {}

    public record TripDetail(String slug, String title, String destination, String category, String departureCity,
                             String description, String coverImageUrl, List<String> inclusions, LocalDate startDate,
                             LocalDate endDate, int days, long priceInr, long tokenInr, int seatsLeft, int totalSeats,
                             String organizer, double organizerRating, long organizerReviews,
                             List<ItineraryItem> itinerary) {
        TripDetail withItinerary(List<ItineraryItem> it) {
            return new TripDetail(slug, title, destination, category, departureCity, description, coverImageUrl,
                    inclusions, startDate, endDate, days, priceInr, tokenInr, seatsLeft, totalSeats, organizer,
                    organizerRating, organizerReviews, it);
        }
    }

    public record OwnTrip(UUID id, String slug, String title, String destination, LocalDate startDate, String status,
                          int totalSeats, int seatsHeld, int seatsBooked, long priceInr) {}
    public record PendingTrip(UUID id, String slug, String title, String organizer, String destination,
                              LocalDate startDate, String description, List<String> inclusions) {}
    public record RosterRow(String bookingRef, String status, String fullName, String phone, String email,
                            String emergencyContactName, String emergencyContactPhone) {}

    //records done

    //public browsing

    @Transactional(readOnly = true)
    public TripPage search (String q, String category, Integer maxPriceInr, LocalDate from , LocalDate to,
                            String sort, int page, int size){
        int pg = Math.max(0, page);
        int sz = Math.min (Math.max(1,size),24);
        StringBuilder where = new StringBuilder(PUBLIC_FROM);
        Map<String, Object> p =  new HashMap<>();
        if(q!=null & !q.isBlank()){
            where.append(" AND (lower(t.title) LIKE :q ESCAPE '\\' OR lower(t.destination) LIKE :q ESCAPE '\\')");
            p.put("q", like(q));
        }
        if (category != null && CATEGORIES.contains(category)) { where.append(" AND t.category = :cat"); p.put("cat", category); }
        if (maxPriceInr != null && maxPriceInr > 0) { where.append(" AND t.price_paise <= :max"); p.put("max", maxPriceInr * 100L); }
        if (from != null) { where.append(" AND t.start_date >= :from"); p.put("from", from); }
        if (to != null) { where.append(" AND t.start_date <= :to"); p.put("to", to); }

        String order = switch (sort == null ? "" : sort) {
            case "price_asc" -> "t.price_paise ASC, t.id";
            case "price_desc" -> "t.price_paise DESC, t.id";
            default -> "t.start_date ASC, t.id";
        };

        long total = jdbc.sql("SELECT count(*) " + where).params(p).query(Long.class).single();
        List<TripCard> items = jdbc.sql("""
                SELECT t.slug, t.title, t.destination, t.category, t.start_date, t.duration_days, t.price_paise,
                       t.token_paise, (t.total_seats - t.seats_held - t.seats_booked) AS seats_left,
                       o.business_name, t.cover_image_url """ + where + " ORDER BY " + order + " LIMIT :lim OFFSET :off")
                .params(p).param("lim", sz).param("off", pg * sz)
                .query((rs, i) -> new TripCard(rs.getString("slug"), rs.getString("title"), rs.getString("destination"),
                        rs.getString("category"), rs.getObject("start_date", LocalDate.class), rs.getInt("duration_days"),
                        rs.getLong("price_paise") / 100, rs.getLong("token_paise") / 100, rs.getInt("seats_left"),
                        rs.getString("business_name"), rs.getString("cover_image_url")))
                .list();
        return new TripPage(items, pg, sz, total);
    }

    @Transactional(readOnly = true)
    public TripDetail detail(String slug) {
        TripDetail base = jdbc.sql("""
                SELECT t.slug, t.title, t.destination, t.category, t.departure_city, t.description, t.cover_image_url,
                       t.inclusions, t.start_date, t.end_date, t.duration_days, t.price_paise, t.token_paise,
                       (t.total_seats - t.seats_held - t.seats_booked) AS seats_left, t.total_seats,
                       o.business_name, s.avg_rating, s.review_count
                """ + PUBLIC_FROM.replace("FROM trips t JOIN organizer_profiles o ON o.user_id = t.organizer_id",
                        "FROM trips t JOIN organizer_profiles o ON o.user_id = t.organizer_id "
                                + "JOIN organizer_stats_v s ON s.organizer_id = o.user_id") + " AND t.slug = :slug")
                .param("slug", slug)
                .query((rs, i) -> new TripDetail(rs.getString("slug"), rs.getString("title"), rs.getString("destination"),
                        rs.getString("category"), rs.getString("departure_city"), rs.getString("description"),
                        rs.getString("cover_image_url"), Sql.stringList(rs, "inclusions"),
                        rs.getObject("start_date", LocalDate.class), rs.getObject("end_date", LocalDate.class),
                        rs.getInt("duration_days"), rs.getLong("price_paise") / 100, rs.getLong("token_paise") / 100,
                        rs.getInt("seats_left"), rs.getInt("total_seats"), rs.getString("business_name"),
                        rs.getDouble("avg_rating"), rs.getLong("review_count"), List.of()))
                .optional().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));

        List<ItineraryItem> itinerary = jdbc.sql("""
                SELECT i.day_number, i.title, i.details FROM trip_itinerary_days i
                JOIN trips t ON t.id = i.trip_id WHERE t.slug = :slug ORDER BY i.day_number""")
                .param("slug", slug)
                .query((rs, i) -> new ItineraryItem(rs.getInt("day_number"), rs.getString("title"), rs.getString("details")))
                .list();
        return base.withItinerary(itinerary);
    }

    // ---------- organiser ----------
    @Transactional
    public UUID create(UUID organizerId, TripRequest r) {
        validate(r);
        String title = ContactPolicy.clean(r.title(), true);
        String slug = newSlug(title, r.startDate());
        UUID id = UUID.randomUUID();
        List<String> inc = r.inclusions() == null ? List.of() : r.inclusions().stream()
                .map(s -> ContactPolicy.clean(s, true)).toList();

        jdbc.sql("""
                INSERT INTO trips (id, slug, organizer_id, title, destination, category, departure_city, description,
                                   cover_image_url, inclusions, start_date, end_date, total_seats, price_paise, token_paise)
                VALUES (:id, :slug, :org, :title, :dest, :cat, :city, :desc, :img,
                        string_to_array(:inc, E'\\n'), :start, :end, :seats, :price, :token)""")
                .param("id", id).param("slug", slug).param("org", organizerId).param("title", title)
                .param("dest", ContactPolicy.clean(r.destination(), true)).param("cat", r.category())
                .param("city", ContactPolicy.clean(r.departureCity(), true))
                .param("desc", ContactPolicy.clean(r.description(), false)).param("img", r.coverImageUrl())
                .param("inc", String.join("\n", inc)).param("start", r.startDate()).param("end", r.endDate())
                .param("seats", r.totalSeats()).param("price", r.priceInr() * 100L).param("token", r.tokenInr() * 100L)
                .update();

        if (r.itinerary() != null) {
            for (ItineraryItem d : r.itinerary()) {
                jdbc.sql("INSERT INTO trip_itinerary_days (trip_id, day_number, title, details) VALUES (:t, :n, :ti, :d)")
                        .param("t", id).param("n", d.dayNumber()).param("ti", ContactPolicy.clean(d.title(), true))
                        .param("d", ContactPolicy.clean(d.details(), false)).update();
            }
        }
        audit.log(organizerId, "TRIP_CREATED", "trip", id.toString());
        return id;
    }

    @Transactional(readOnly = true)
    public List<OwnTrip> mine(UUID organizerId) {
        return jdbc.sql("""
                SELECT id, slug, title, destination, start_date, status, total_seats, seats_held, seats_booked, price_paise
                FROM trips WHERE organizer_id = :o ORDER BY created_at DESC""")
                .param("o", organizerId)
                .query((rs, i) -> new OwnTrip(rs.getObject("id", UUID.class), rs.getString("slug"), rs.getString("title"),
                        rs.getString("destination"), rs.getObject("start_date", LocalDate.class), rs.getString("status"),
                        rs.getInt("total_seats"), rs.getInt("seats_held"), rs.getInt("seats_booked"),
                        rs.getLong("price_paise") / 100))
                .list();
    }

    @Transactional
    public void submit(UUID organizerId, UUID tripId) {
        int n = jdbc.sql("""
                UPDATE trips SET status = 'PENDING_REVIEW', updated_at = now()
                WHERE id = :id AND organizer_id = :o AND status = 'DRAFT'""")
                .param("id", tripId).param("o", organizerId).update();
        if (n == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Draft trip not found");
        audit.log(organizerId, "TRIP_SUBMITTED", "trip", tripId.toString());
    }

    /** Only trips with no seats held or sold can be cancelled for now. Refund handling arrives with payments. */
    @Transactional
    public void cancel(UUID organizerId, UUID tripId) {
        int n = jdbc.sql("""
                UPDATE trips SET status = 'CANCELLED', updated_at = now()
                WHERE id = :id AND organizer_id = :o AND status IN ('DRAFT','PENDING_REVIEW','PUBLISHED')
                  AND seats_held = 0 AND seats_booked = 0""")
                .param("id", tripId).param("o", organizerId).update();
        if (n == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "Trip has bookings or cannot be cancelled");
        audit.log(organizerId, "TRIP_CANCELLED", "trip", tripId.toString());
    }

    @Transactional
    public List<RosterRow> roster(UUID organizerId, UUID tripId) {
        Integer owned = jdbc.sql("SELECT count(*) FROM trips WHERE id = :t AND organizer_id = :o")
                .param("t", tripId).param("o", organizerId).query(Integer.class).single();
        if (owned == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found");   // not 403: don't leak existence
        audit.log(organizerId, "ROSTER_VIEWED", "trip", tripId.toString());
        return jdbc.sql("""
                SELECT b.booking_ref, b.status, a.full_name, a.phone, a.email,
                       a.emergency_contact_name, a.emergency_contact_phone
                FROM bookings b JOIN booking_attendees a ON a.booking_id = b.id
                WHERE b.trip_id = :t AND b.status IN ('TOKEN_PAID','CONFIRMED') ORDER BY b.created_at""")
                .param("t", tripId)
                .query((rs, i) -> new RosterRow(rs.getString("booking_ref"), rs.getString("status"),
                        rs.getString("full_name"), rs.getString("phone"), rs.getString("email"),
                        rs.getString("emergency_contact_name"), rs.getString("emergency_contact_phone")))
                .list();
    }

    // ---------- admin review ----------
    @Transactional(readOnly = true)
    public List<PendingTrip> pendingReview() {
        return jdbc.sql("""
                SELECT t.id, t.slug, t.title, o.business_name, t.destination, t.start_date, t.description, t.inclusions
                FROM trips t JOIN organizer_profiles o ON o.user_id = t.organizer_id
                WHERE t.status = 'PENDING_REVIEW' ORDER BY t.created_at""")
                .query((rs, i) -> new PendingTrip(rs.getObject("id", UUID.class), rs.getString("slug"),
                        rs.getString("title"), rs.getString("business_name"), rs.getString("destination"),
                        rs.getObject("start_date", LocalDate.class), rs.getString("description"),
                        Sql.stringList(rs, "inclusions")))
                .list();
    }

    @Transactional
    public void approve(UUID tripId, UUID adminId) {
        int n = jdbc.sql("""
                UPDATE trips SET status = 'PUBLISHED', published_at = now(), updated_at = now()
                WHERE id = :id AND status = 'PENDING_REVIEW'
                  AND start_date > CAST((now() AT TIME ZONE 'Asia/Kolkata') AS date)
                  AND EXISTS (SELECT 1 FROM organizer_profiles o
                              WHERE o.user_id = organizer_id AND o.verification_status = 'VERIFIED')""")
                .param("id", tripId).update();
        if (n == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "Trip is not awaiting review, is in the past, or organiser is not verified");
        audit.log(adminId, "TRIP_PUBLISHED", "trip", tripId.toString());
    }

    @Transactional
    public void rejectToDraft(UUID tripId, UUID adminId) {
        int n = jdbc.sql("UPDATE trips SET status = 'DRAFT', updated_at = now() WHERE id = :id AND status = 'PENDING_REVIEW'")
                .param("id", tripId).update();
        if (n == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "Trip is not awaiting review");
        audit.log(adminId, "TRIP_REJECTED", "trip", tripId.toString());
    }

    // ---------- helpers ----------
    private void validate(TripRequest r) {
        if (!CATEGORIES.contains(r.category())) throw bad("Unknown category");
        if (r.endDate().isBefore(r.startDate())) throw bad("End date is before start date");
        if (r.tokenInr() > r.priceInr()) throw bad("Token cannot exceed the trip price");
        if (r.itinerary() != null) {
            Set<Integer> days = new HashSet<>();
            for (ItineraryItem d : r.itinerary()) {
                if (!days.add(d.dayNumber())) throw bad("Duplicate itinerary day " + d.dayNumber());
                text("Itinerary", d.title()); text("Itinerary", d.details());
            }
        }
        text("Title", r.title()); text("Destination", r.destination()); text("Description", r.description());
        text("Departure city", r.departureCity());
        if (r.inclusions() != null) r.inclusions().forEach(s -> text("Inclusions", s));
    }

    private void text(String label, String s) {
        if (ContactPolicy.hasContactOrLink(s)) {
            throw bad(label + " must not contain links, phone numbers, emails or handles.");
        }
    }

    private ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }

    private String newSlug(String title, LocalDate start) {
        String base = Normalizer.normalize(title, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (base.length() > 60) base = base.substring(0, 60).replaceAll("-$", "");
        if (base.isEmpty()) base = "trip";
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < 4; i++) suffix.append("abcdefghijklmnopqrstuvwxyz0123456789".charAt(RNG.nextInt(36)));
        return base + "-" + start + "-" + suffix;
    }

    private static String like(String s) {
        String v = s.trim().toLowerCase(Locale.ROOT);
        if (v.length() > 60) v = v.substring(0, 60);
        return "%" + v.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
    }

}
