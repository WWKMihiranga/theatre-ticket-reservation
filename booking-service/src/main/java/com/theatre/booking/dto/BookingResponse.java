package com.theatre.booking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        Long userId,
        Long showId,
        int rowNumber,
        int seatNumber,
        BigDecimal price,
        String status,
        LocalDateTime createdAt
) {}
