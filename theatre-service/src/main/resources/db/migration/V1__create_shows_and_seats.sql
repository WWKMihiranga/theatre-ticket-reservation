-- Shows: each show is one event. The coursework had only one theatre/event;
-- adding "shows" lets a single venue host multiple events while preserving
-- the same 12/16/20 seat layout per show.
CREATE TABLE shows (
    id              BIGSERIAL PRIMARY KEY,
    title           VARCHAR(200) NOT NULL,
    description     TEXT,
    show_time       TIMESTAMP    NOT NULL,
    venue           VARCHAR(200) NOT NULL DEFAULT 'New Theatre',
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_shows_show_time ON shows(show_time);

-- Seats: 48 rows per show (12 + 16 + 20), with the coursework's tiered pricing.
-- status = AVAILABLE | BOOKED  (mirrors coursework int[] 0/1)
CREATE TABLE seats (
    id              BIGSERIAL PRIMARY KEY,
    show_id         BIGINT       NOT NULL,
    row_num      INT          NOT NULL,
    seat_number     INT          NOT NULL,
    price           NUMERIC(10,2) NOT NULL,
    status          VARCHAR(20)  NOT NULL DEFAULT 'AVAILABLE',
    version         BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT fk_seats_show FOREIGN KEY (show_id) REFERENCES shows(id) ON DELETE CASCADE,
    CONSTRAINT chk_status CHECK (status IN ('AVAILABLE', 'BOOKED')),
    CONSTRAINT chk_row CHECK (row_num IN (1, 2, 3)),
    CONSTRAINT uq_show_row_seat UNIQUE (show_id, row_num, seat_number)
);

CREATE INDEX idx_seats_show ON seats(show_id);
CREATE INDEX idx_seats_status ON seats(show_id, status);
