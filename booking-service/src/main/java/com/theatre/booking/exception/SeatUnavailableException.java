package com.theatre.booking.exception;

public class SeatUnavailableException extends RuntimeException {
    public SeatUnavailableException(int row, int seat) {
        super("Seat is already booked (row " + row + ", seat " + seat + "). Please select another seat.");
    }
    public SeatUnavailableException(String message) { super(message); }
}
