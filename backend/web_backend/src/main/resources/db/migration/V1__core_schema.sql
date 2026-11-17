-- V1__core_schema.sql  (PostgreSQL 15+)

-- ============ USERS AND ROLES ============
CREATE TABLE users (
                       id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email             VARCHAR(254) NOT NULL,
                       password_hash     VARCHAR(100) NOT NULL,
                       full_name         VARCHAR(100) NOT NULL,
                       phone             VARCHAR(20),
                       email_verified_at TIMESTAMPTZ,
                       status            VARCHAR(12) NOT NULL DEFAULT 'ACTIVE'
                           CHECK (status IN ('ACTIVE','SUSPENDED','DELETED')),
                       failed_logins     INT NOT NULL DEFAULT 0,
                       locked_until      TIMESTAMPTZ,
                       created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at        TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE UNIQUE INDEX ux_users_email ON users (lower(email));

CREATE TABLE user_roles (
                            user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                            role       VARCHAR(12) NOT NULL CHECK (role IN ('TRAVELLER','ORGANISER','ADMIN')),
                            granted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                            granted_by UUID REFERENCES users(id),
                            PRIMARY KEY (user_id, role)
);

CREATE TABLE organizer_profiles (
                                    user_id             UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                                    business_name       VARCHAR(120) NOT NULL,
                                    instagram_handle    VARCHAR(50),
                                    gstin               VARCHAR(15)
                                        CHECK (gstin ~ '^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$'),
    verification_status VARCHAR(12) NOT NULL DEFAULT 'PENDING'
                        CHECK (verification_status IN ('PENDING','VERIFIED','REJECTED','SUSPENDED')),
    verified_by         UUID REFERENCES users(id),
    verified_at         TIMESTAMPTZ,
    rejection_reason    VARCHAR(500),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============ TRIPS ============
CREATE TABLE trips (
                       id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       slug                VARCHAR(120) NOT NULL UNIQUE,
                       organizer_id        UUID NOT NULL REFERENCES organizer_profiles(user_id),
                       title               VARCHAR(200) NOT NULL,
                       destination         VARCHAR(150) NOT NULL,
                       category            VARCHAR(40) NOT NULL
                           CHECK (category IN ('High Altitude Trek','Backpacking & Culture',
                                               'Road Trip','Beach & Chill','Weekend Getaway')),
                       departure_city      VARCHAR(60),
                       description         TEXT NOT NULL,
                       cover_image_url     VARCHAR(500),
                       inclusions          TEXT[] NOT NULL DEFAULT '{}',
                       start_date          DATE NOT NULL,
                       end_date            DATE NOT NULL,
                       duration_days       INT GENERATED ALWAYS AS (end_date - start_date + 1) STORED,
                       total_seats         INT NOT NULL CHECK (total_seats > 0),
                       seats_held          INT NOT NULL DEFAULT 0,
                       seats_booked        INT NOT NULL DEFAULT 0,
                       price_paise         BIGINT NOT NULL CHECK (price_paise > 0),   -- per person
                       token_paise         BIGINT NOT NULL CHECK (token_paise > 0),   -- per person
                       booking_cutoff_days INT NOT NULL DEFAULT 3 CHECK (booking_cutoff_days >= 0),
                       status              VARCHAR(16) NOT NULL DEFAULT 'DRAFT'
                           CHECK (status IN ('DRAFT','PENDING_REVIEW','PUBLISHED','CANCELLED','COMPLETED')),
                       published_at        TIMESTAMPTZ,
                       version             BIGINT NOT NULL DEFAULT 0,
                       created_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
                       updated_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
                       search_tsv          TSVECTOR GENERATED ALWAYS AS (
                           to_tsvector('simple', coalesce(title,'') || ' ' ||
                                                 coalesce(destination,'') || ' ' ||
                                                 coalesce(description,''))) STORED,
                       CONSTRAINT ck_trip_dates   CHECK (end_date >= start_date),
                       CONSTRAINT ck_trip_token   CHECK (token_paise <= price_paise),
                       CONSTRAINT ck_trip_nonneg  CHECK (seats_held >= 0 AND seats_booked >= 0),
    -- the last line of defence: the database itself refuses to overbook
                       CONSTRAINT ck_trip_no_overbooking CHECK (seats_held + seats_booked <= total_seats)
);
CREATE INDEX ix_trips_published_start ON trips (start_date) WHERE status = 'PUBLISHED';
CREATE INDEX ix_trips_organizer       ON trips (organizer_id);
CREATE INDEX ix_trips_search          ON trips USING GIN (search_tsv);

CREATE TABLE trip_itinerary_days (
                                     trip_id    UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
                                     day_number INT  NOT NULL CHECK (day_number > 0),
                                     title      VARCHAR(150) NOT NULL,
                                     details    TEXT NOT NULL,
                                     PRIMARY KEY (trip_id, day_number)
);

-- ============ BOOKINGS ============
CREATE TABLE bookings (
                          id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          booking_ref       VARCHAR(12) NOT NULL UNIQUE,
                          trip_id           UUID NOT NULL REFERENCES trips(id),
                          user_id           UUID NOT NULL REFERENCES users(id),
                          seats             INT NOT NULL CHECK (seats BETWEEN 1 AND 6),
                          status            VARCHAR(16) NOT NULL
                              CHECK (status IN ('PENDING_PAYMENT','TOKEN_PAID','CONFIRMED',
                                                'EXPIRED','CANCELLED','COMPLETED')),
                          price_paise       BIGINT NOT NULL CHECK (price_paise > 0),  -- per seat, snapshot
                          token_paise       BIGINT NOT NULL CHECK (token_paise > 0),  -- per seat, snapshot
                          total_paise       BIGINT NOT NULL,
                          token_total_paise BIGINT NOT NULL,
                          hold_expires_at   TIMESTAMPTZ,
                          balance_due_at    TIMESTAMPTZ NOT NULL,
                          payout_status     VARCHAR(20) NOT NULL DEFAULT 'NONE'
                              CHECK (payout_status IN ('NONE','HELD_IN_ESCROW','RELEASE_SCHEDULED',
                                                       'DISBURSED','REFUNDED')),
                          cancelled_at      TIMESTAMPTZ,
                          cancelled_by      VARCHAR(10) CHECK (cancelled_by IN ('USER','ORGANISER','ADMIN','SYSTEM')),
                          cancel_reason     VARCHAR(300),
                          idempotency_key   VARCHAR(64) NOT NULL,
                          version           BIGINT NOT NULL DEFAULT 0,
                          created_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                          updated_at        TIMESTAMPTZ NOT NULL DEFAULT now(),
                          CONSTRAINT ck_booking_totals CHECK (
                              total_paise = price_paise * seats AND
                              token_total_paise = token_paise * seats AND
                              token_total_paise <= total_paise),
                          CONSTRAINT ux_booking_idempotency UNIQUE (user_id, idempotency_key)
);
-- a user can hold only ONE live booking per trip
CREATE UNIQUE INDEX ux_one_active_booking_per_user_trip
    ON bookings (user_id, trip_id)
    WHERE status IN ('PENDING_PAYMENT','TOKEN_PAID','CONFIRMED');
CREATE INDEX ix_bookings_hold_expiry ON bookings (hold_expires_at) WHERE status = 'PENDING_PAYMENT';
CREATE INDEX ix_bookings_trip_status ON bookings (trip_id, status);
CREATE INDEX ix_bookings_user        ON bookings (user_id, created_at DESC);

CREATE TABLE booking_attendees (
                                   id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   booking_id              UUID NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
                                   full_name               VARCHAR(100) NOT NULL,
                                   phone                   VARCHAR(20)  NOT NULL,
                                   email                   VARCHAR(254),
                                   emergency_contact_name  VARCHAR(100) NOT NULL,
                                   emergency_contact_phone VARCHAR(20)  NOT NULL
);
CREATE INDEX ix_attendees_booking ON booking_attendees (booking_id);

CREATE TABLE booking_events (            -- append-only history of status changes
                                id          BIGSERIAL PRIMARY KEY,
                                booking_id  UUID NOT NULL REFERENCES bookings(id),
                                from_status VARCHAR(16),
                                to_status   VARCHAR(16) NOT NULL,
                                actor_type  VARCHAR(10) NOT NULL CHECK (actor_type IN ('USER','ORGANISER','ADMIN','SYSTEM','WEBHOOK')),
                                actor_id    UUID,
                                note        VARCHAR(300),
                                created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX ix_booking_events_booking ON booking_events (booking_id, created_at);

-- ============ REVIEWS, WISHLIST, AUDIT ============
CREATE TABLE reviews (
                         id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         booking_id   UUID NOT NULL UNIQUE REFERENCES bookings(id),
                         user_id      UUID NOT NULL REFERENCES users(id),
                         trip_id      UUID NOT NULL REFERENCES trips(id),
                         organizer_id UUID NOT NULL REFERENCES organizer_profiles(user_id),
                         rating       INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                         comment      VARCHAR(2000),
                         created_at   TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE wishlists (
                           user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                           trip_id    UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                           PRIMARY KEY (user_id, trip_id)
);

CREATE TABLE audit_log (
                           id            BIGSERIAL PRIMARY KEY,
                           actor_user_id UUID,
                           action        VARCHAR(60) NOT NULL,
                           entity_type   VARCHAR(40),
                           entity_id     VARCHAR(64),
                           ip            INET,
                           details       JSONB,
                           created_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ============ VIEWS ============
CREATE VIEW organizer_stats_v AS
SELECT o.user_id AS organizer_id,
       COALESCE(ROUND(AVG(r.rating)::numeric, 2), 0) AS avg_rating,
       COUNT(r.id) AS review_count
FROM organizer_profiles o
         LEFT JOIN reviews r ON r.organizer_id = o.user_id
GROUP BY o.user_id;

-- What the AI agent is allowed to see: published, verified, public columns only. No personal data.
CREATE VIEW agent_trips_v AS
SELECT t.slug AS trip_id, t.title, t.destination, t.category, t.departure_city,
       t.start_date, t.end_date, t.duration_days,
       (t.price_paise / 100) AS price_inr,
       (t.token_paise / 100) AS token_inr,
       (t.total_seats - t.seats_held - t.seats_booked) AS seats_left,
       t.total_seats, t.description, t.inclusions, t.search_tsv,
       o.business_name AS organizer_name,
       s.avg_rating AS organizer_rating, s.review_count AS organizer_reviews
FROM trips t
         JOIN organizer_profiles o ON o.user_id = t.organizer_id
         JOIN organizer_stats_v s  ON s.organizer_id = o.user_id
WHERE t.status = 'PUBLISHED'
  AND o.verification_status = 'VERIFIED'
  AND t.start_date > (now() AT TIME ZONE 'Asia/Kolkata')::date;

CREATE VIEW agent_itinerary_v AS
SELECT t.slug AS trip_id, i.day_number, i.title, i.details
FROM trip_itinerary_days i
         JOIN trips t ON t.id = i.trip_id
         JOIN organizer_profiles o ON o.user_id = t.organizer_id
WHERE t.status = 'PUBLISHED' AND o.verification_status = 'VERIFIED';