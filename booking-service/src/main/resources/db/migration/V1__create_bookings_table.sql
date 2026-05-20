-- Bookings: maps to the coursework "Tickets" concept (row, seat, price, person).
-- user_id and show_id are references across service boundaries, not FKs —
-- each microservice owns its own data.
CREATE TABLE bookings (
    id              BIGSERIAL    PRIMARY KEY,
    user_id         BIGINT       NOT NULL,
    show_id         BIGINT       NOT NULL,
    row_num         INT          NOT NULL,
    seat_number     INT          NOT NULL,
    price           NUMERIC(10,2) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_booking_status CHECK (status IN ('CONFIRMED', 'CANCELLED')),
    CONSTRAINT chk_row CHECK (row_num IN (1, 2, 3))
);

-- A user can only have one CONFIRMED booking per seat per show
CREATE UNIQUE INDEX uq_booking_active_seat
    ON bookings(show_id, row_num, seat_number)
    WHERE status = 'CONFIRMED';

CREATE INDEX idx_bookings_user        ON bookings(user_id);
CREATE INDEX idx_bookings_show        ON bookings(show_id);
CREATE INDEX idx_bookings_user_status ON bookings(user_id, status);
