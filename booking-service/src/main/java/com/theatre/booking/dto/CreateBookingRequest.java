package com.theatre.booking.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CreateBookingRequest(
        @NotNull Long showId,
        @NotNull @Min(1) Integer rowNumber,
        @NotNull @Min(1) Integer seatNumber
) {}
