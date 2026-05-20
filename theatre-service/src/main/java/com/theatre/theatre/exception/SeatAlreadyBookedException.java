package com.theatre.theatre.exception;

/**
 * Thrown when trying to book a seat that's already taken.
 * Coursework equivalent: "Your seat is already booked. Please Select another seat".
 */
public class SeatAlreadyBookedException extends RuntimeException {
    public SeatAlreadyBookedException(int row, int seat) {
        super("Seat is already booked (row " + row + ", seat " + seat + "). Please select another seat.");
    }
}
