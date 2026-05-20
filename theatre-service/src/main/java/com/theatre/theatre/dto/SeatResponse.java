package com.theatre.theatre.dto;

import java.math.BigDecimal;

public record SeatResponse(
        Long id,
        int rowNumber,
        int seatNumber,
        BigDecimal price,
        String status
) {}
