package com.theatre.booking.client.dto;

import java.math.BigDecimal;

/** Mirrors theatre-service's SeatResponse — duplicated intentionally to keep services decoupled. */
public record SeatInfo(
        Long id,
        int rowNumber,
        int seatNumber,
        BigDecimal price,
        String status
) {}
