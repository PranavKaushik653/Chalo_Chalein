package com.chalochalein.backend.booking;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private static final int HOLD_MINUTES = 10;
    private static final int MAX_LIVE_HOLDS = 2;
    private static final int BALANCE_DAYS_BEFORE = 3;
    private static final SecureRandom RNG = new SecureRandom();
    private static final String REF_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final JdbcClient jdbc;
    private final TransactionTemplate tx;

    public record Attendee(
            @NotBlank @Size(max = 100) String fullName,
            @NotBlank @Pattern(regexp = "^\\+?[0-9]{10,15}$") String phone,
            @Email @Size(max = 254) String email,
            @NotBlank @Size(max = 100) String emergencyContactName,
            @NotBlank @Pattern(regexp = "^\\+?[0-9]{10,15}$") String emergencyContactPhone) {}

    public record HoldRequest(
            @NotBlank @Size(max = 120) String tripSlug,
            @Min(1) @Max(6) int seats,
            @NotEmpty @Size(max = 6) List<@Valid Attendee> attendees) {}

    public record BookingView(String bookingRef, String status, String tripSlug, String tripTitle, LocalDate startDate,
                              int seats, long totalInr, long tokenInr, long balanceInr,
                              OffsetDateTime holdExpiresAt, OffsetDateTime balanceDueAt) {}

    private record TripRef(UUID id, UUID organizerId) {}
    private record Released(UUID id, UUID tripId, int seats) {}

    private static final String VIEW_SQL = """
            SELECT b.booking_ref, b.status, t.slug, t.title, t.start_date, b.seats, b.total_paise,
                   b.token_total_paise, b.hold_expires_at, b.balance_due_at
            FROM bookings b JOIN trips t ON t.id = b.trip_id
            """;

    private static final RowMapper<BookingView> VIEW = (rs, i) -> new BookingView(
            rs.getString("booking_ref"), rs.getString("status"), rs.getString("slug"), rs.getString("title"),
            rs.getObject("start_date", LocalDate.class), rs.getInt("seats"),
            rs.getLong("total_paise") / 100, rs.getLong("token_total_paise") / 100,
            (rs.getLong("total_paise") - rs.getLong("token_total_paise")) / 100,
            rs.getObject("hold_expires_at", OffsetDateTime.class), rs.getObject("balance_due_at", OffsetDateTime.class));

    // ---------- hold ----------
    public BookingView hold(UUID userId, String idempotencyKey, HoldRequest req) {
        if (idempotencyKey == null || !idempotencyKey.matches("^[A-Za-z0-9-]{16,64}$")) {
            throw bad("A valid Idempotency-Key header is required");
        }
        if (req.attendees().size() != req.seats()) throw bad("Provide exactly one attendee per seat");
        try {
            return tx.execute(status -> doHold(userId, idempotencyKey, req));
        } catch (DuplicateKeyException e) {
            // The whole transaction (including the seat increment) was rolled back.
            return findByIdempotency(userId, idempotencyKey).orElseThrow(() ->
                    new ResponseStatusException(HttpStatus.CONFLICT, "You already have an active booking for this trip"));
        }
    }

    private BookingView doHold(UUID userId, String key, HoldRequest req) {
        jdbc.sql("SET LOCAL lock_timeout = '2s'").update();          // fail fast instead of queueing forever

        Optional<BookingView> replay = findByIdempotency(userId, key);
        if (replay.isPresent()) return replay.get();                  // retry of the same click

        Long live = jdbc.sql("""
                SELECT count(*) FROM bookings
                WHERE user_id = :u AND status = 'PENDING_PAYMENT' AND hold_expires_at > now()""")
                .param("u", userId).query(Long.class).single();
        if (live >= MAX_LIVE_HOLDS) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Finish or release your current seat holds first");
        }

        TripRef trip = jdbc.sql("SELECT id, organizer_id FROM trips WHERE slug = :s AND status = 'PUBLISHED'")
                .param("s", req.tripSlug())
                .query((rs, i) -> new TripRef(rs.getObject("id", UUID.class), rs.getObject("organizer_id", UUID.class)))
                .optional().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Trip not found"));
        if (trip.organizerId().equals(userId)) throw bad("You cannot book your own trip");

        int held = holdSeats(trip.id(), req.seats());
        if (held == 0 && releaseExpired(trip.id()) > 0) held = holdSeats(trip.id(), req.seats());   // free abandoned holds, retry once
        if (held == 0) throw new ResponseStatusException(HttpStatus.CONFLICT, "Not enough seats left, or booking has closed");

        UUID bookingId = jdbc.sql("""
                INSERT INTO bookings (booking_ref, trip_id, user_id, seats, status, price_paise, token_paise,
                                      total_paise, token_total_paise, hold_expires_at, balance_due_at, idempotency_key)
                SELECT :ref, t.id, :user, :seats, 'PENDING_PAYMENT', t.price_paise, t.token_paise,
                       t.price_paise * :seats, t.token_paise * :seats,
                       now() + make_interval(mins => :mins),
                       CAST(t.start_date - :balanceDays AS timestamp) AT TIME ZONE 'Asia/Kolkata',
                       :key
                FROM trips t WHERE t.id = :tripId
                RETURNING id""")
                .param("ref", newRef()).param("user", userId).param("seats", req.seats())
                .param("mins", HOLD_MINUTES).param("balanceDays", BALANCE_DAYS_BEFORE)
                .param("key", key).param("tripId", trip.id())
                .query((rs, i) -> rs.getObject(1, UUID.class)).single();

        for (Attendee a : req.attendees()) {
            jdbc.sql("""
                    INSERT INTO booking_attendees (booking_id, full_name, phone, email, emergency_contact_name, emergency_contact_phone)
                    VALUES (:b, :n, :p, :e, :en, :ep)""")
                    .param("b", bookingId).param("n", a.fullName().trim()).param("p", a.phone()).param("e", a.email())
                    .param("en", a.emergencyContactName().trim()).param("ep", a.emergencyContactPhone()).update();
        }
        event(bookingId, null, "PENDING_PAYMENT", "USER", userId, "hold created");
        return jdbc.sql(VIEW_SQL + "WHERE b.id = :id").param("id", bookingId).query(VIEW).single();
    }

    /** THE statement. Check and write in one atomic UPDATE; Postgres re-checks the WHERE after waiting for the row lock. */
    private int holdSeats(UUID tripId, int n) {
        return jdbc.sql("""
                UPDATE trips SET seats_held = seats_held + :n, updated_at = now()
                WHERE id = :id AND status = 'PUBLISHED'
                  AND start_date - booking_cutoff_days > CAST((now() AT TIME ZONE 'Asia/Kolkata') AS date)
                  AND seats_held + seats_booked + :n <= total_seats""")
                .param("n", n).param("id", tripId).update();
    }

    // ---------- read ----------
    public List<BookingView> mine(UUID userId) {
        return jdbc.sql(VIEW_SQL + "WHERE b.user_id = :u ORDER BY b.created_at DESC").param("u", userId).query(VIEW).list();
    }

    public BookingView one(UUID userId, String ref) {
        return jdbc.sql(VIEW_SQL + "WHERE b.booking_ref = :r AND b.user_id = :u")
                .param("r", ref).param("u", userId).query(VIEW).optional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));   // not 403: no IDOR hints
    }

    private Optional<BookingView> findByIdempotency(UUID userId, String key) {
        return jdbc.sql(VIEW_SQL + "WHERE b.user_id = :u AND b.idempotency_key = :k")
                .param("u", userId).param("k", key).query(VIEW).optional();
    }

    // ---------- release a hold ----------
    public void releaseHold(UUID userId, String ref) {
        tx.executeWithoutResult(status -> {
            Released row = jdbc.sql("""
                    UPDATE bookings SET status = 'CANCELLED', cancelled_at = now(), cancelled_by = 'USER',
                           cancel_reason = 'hold released by traveller', updated_at = now()
                    WHERE booking_ref = :ref AND user_id = :u AND status = 'PENDING_PAYMENT'
                    RETURNING id, trip_id, seats""")
                    .param("ref", ref).param("u", userId)
                    .query((rs, i) -> new Released(rs.getObject("id", UUID.class), rs.getObject("trip_id", UUID.class), rs.getInt("seats")))
                    .optional().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active hold with that reference"));
            jdbc.sql("UPDATE trips SET seats_held = seats_held - :n, updated_at = now() WHERE id = :t")
                    .param("n", row.seats()).param("t", row.tripId()).update();
            event(row.id(), "PENDING_PAYMENT", "CANCELLED", "USER", userId, "hold released");
        });
    }

    // ---------- expiry sweeper ----------
    private static final String EXPIRE_SQL = """
            WITH expired AS (
                SELECT id, trip_id, seats FROM bookings
                WHERE status = 'PENDING_PAYMENT' AND hold_expires_at < now() %s
                ORDER BY hold_expires_at LIMIT 200
                FOR UPDATE SKIP LOCKED
            ), upd AS (
                UPDATE bookings b SET status = 'EXPIRED', updated_at = now()
                FROM expired e WHERE b.id = e.id
                RETURNING b.id, b.trip_id, b.seats
            ), ev AS (
                INSERT INTO booking_events (booking_id, from_status, to_status, actor_type, note)
                SELECT id, 'PENDING_PAYMENT', 'EXPIRED', 'SYSTEM', 'hold expired' FROM upd
            )
            UPDATE trips t SET seats_held = t.seats_held - s.n, updated_at = now()
            FROM (SELECT trip_id, CAST(SUM(seats) AS int) AS n FROM upd GROUP BY trip_id) s
            WHERE t.id = s.trip_id""";

    @Scheduled(fixedDelay = 30_000, initialDelay = 15_000)
    public void sweepExpiredHolds() {
        Integer trips = tx.execute(status -> jdbc.sql(EXPIRE_SQL.formatted("")).update());
        if (trips != null && trips > 0) log.info("[HOLDS] released expired holds on {} trip(s)", trips);
    }

    private int releaseExpired(UUID tripId) {
        return jdbc.sql(EXPIRE_SQL.formatted("AND trip_id = :tripId")).param("tripId", tripId).update();
    }

    // ---------- helpers ----------
    private void event(UUID bookingId, String from, String to, String actorType, UUID actorId, String note) {
        jdbc.sql("""
                INSERT INTO booking_events (booking_id, from_status, to_status, actor_type, actor_id, note)
                VALUES (:b, :f, :t, :at, :ai, :n)""")
                .param("b", bookingId).param("f", from).param("t", to).param("at", actorType)
                .param("ai", actorId).param("n", note).update();
    }

    private static String newRef() {
        StringBuilder sb = new StringBuilder("CC");
        for (int i = 0; i < 8; i++) sb.append(REF_ALPHABET.charAt(RNG.nextInt(REF_ALPHABET.length())));
        return sb.toString();
    }

    private ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, m); }
}

