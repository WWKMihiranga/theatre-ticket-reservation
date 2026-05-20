package com.theatre.booking.client.dto;

/** Request body for theatre-service's seat book/release endpoints. */
public record SeatBookingRequest(int rowNumber, int seatNumber) {}
