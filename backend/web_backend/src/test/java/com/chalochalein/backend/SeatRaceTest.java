package com.chalochalein.backend;


import com.chalochalein.backend.booking.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class SeatRaceTest {

    static { TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata")); }

    @Autowired
    BookingService bookings;
    @Autowired JdbcClient jdbc;

    @Test
    void fiftyBuyersOneSeat_exactlyOneWins() throws Exception {
        UUID org = UUID.randomUUID();
        insertUser(org, "race-org-" + org + "@t.local");
        jdbc.sql("INSERT INTO organizer_profiles (user_id, business_name, verification_status) VALUES (:id, 'Race Co', 'VERIFIED')")
                .param("id", org).update();
        String slug = "race-" + UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO trips (slug, organizer_id, title, destination, category, description, start_date, end_date,
                                   total_seats, price_paise, token_paise, status)
                VALUES (:slug, :org, 'Race', 'Nowhere', 'Weekend Getaway', 'test', current_date + 30, current_date + 32,
                        1, 500000, 100000, 'PUBLISHED')""")
                .param("slug", slug).param("org", org).update();

        int buyers = 50;
        List<UUID> ids = new ArrayList<>();
        for (int i = 0; i < buyers; i++) {
            UUID id = UUID.randomUUID();
            ids.add(id);
            insertUser(id, "race-" + id + "@t.local");
        }

        BookingService.Attendee a = new BookingService.Attendee("T", "+919999999999", null, "E", "+919888888888");
        ExecutorService pool = Executors.newFixedThreadPool(buyers);
        CountDownLatch gate = new CountDownLatch(1);
        AtomicInteger won = new AtomicInteger();
        AtomicInteger lost = new AtomicInteger();
        List<Future<Object>> futures = new ArrayList<>();
        for (UUID uid : ids) {
            futures.add(pool.submit(() -> {
                gate.await();
                try {
                    bookings.hold(uid, UUID.randomUUID().toString(), new BookingService.HoldRequest(slug, 1, List.of(a)));
                    won.incrementAndGet();
                } catch (ResponseStatusException e) {
                    if (e.getStatusCode().value() == 409) lost.incrementAndGet();
                }
                return null;
            }));
        }
        gate.countDown();
        for (Future<Object> f : futures) f.get(60, TimeUnit.SECONDS);
        pool.shutdown();

        assertEquals(1, won.get());
        assertEquals(buyers - 1, lost.get());
        Integer held = jdbc.sql("SELECT seats_held FROM trips WHERE slug = :s").param("s", slug).query(Integer.class).single();
        assertEquals(1, held);
    }

    private void insertUser(UUID id, String email) {
        jdbc.sql("INSERT INTO users (id, email, password_hash, full_name) VALUES (:id, :e, 'x', 'Race User')")
                .param("id", id).param("e", email).update();
    }
}
