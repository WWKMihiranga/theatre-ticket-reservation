package com.theatre.booking.dto;

import java.math.BigDecimal;

public record BookingSummaryResponse(
        int totalBookings,
        BigDecimal totalPrice
) {}
