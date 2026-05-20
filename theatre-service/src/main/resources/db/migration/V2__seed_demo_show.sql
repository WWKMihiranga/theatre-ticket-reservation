-- Seed one demo show so the system is usable immediately after startup.
-- Uses a CTE so we don't depend on the show getting id=1 (more robust to re-runs).
WITH new_show AS (
    INSERT INTO shows (title, description, show_time, venue)
    VALUES (
        'Hamlet - Opening Night',
        'A timeless tragedy by William Shakespeare. Doors open 30 minutes before showtime.',
        CURRENT_TIMESTAMP + INTERVAL '7 days',
        'New Theatre'
    )
    RETURNING id
)
INSERT INTO seats (show_id, row_num, seat_number, price, status)
SELECT new_show.id, row_data.row_num, row_data.seat_num, row_data.price, 'AVAILABLE'
FROM new_show
CROSS JOIN (
    -- Row 1: 12 seats @ $10
    SELECT 1 AS row_num, gs AS seat_num, 10.00 AS price FROM generate_series(1, 12) AS gs
    UNION ALL
    -- Row 2: 16 seats @ $20
    SELECT 2, gs, 20.00 FROM generate_series(1, 16) AS gs
    UNION ALL
    -- Row 3: 20 seats @ $30
    SELECT 3, gs, 30.00 FROM generate_series(1, 20) AS gs
) AS row_data;
