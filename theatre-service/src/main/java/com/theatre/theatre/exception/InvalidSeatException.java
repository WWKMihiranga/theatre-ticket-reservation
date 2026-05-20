package com.theatre.theatre.exception;

/**
 * Thrown when row/seat coordinates are out of range.
 * Coursework equivalent: "Invalid Input. Please check your Row Number and the Seat Number".
 */
public class InvalidSeatException extends RuntimeException {
    public InvalidSeatException(String message) {
        super(message);
    }
}
