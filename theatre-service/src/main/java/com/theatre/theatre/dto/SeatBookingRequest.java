package com.theatre.theatre.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Internal contract used to reserve a seat. The Booking Service (Phase 3)
 * will call this; for now it's also exposed for manual testing/admin use.
 */
public record SeatBookingRequest(
        @NotNull @Min(1) Integer rowNumber,
        @NotNull @Min(1) Integer seatNumber
) {}
