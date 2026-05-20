package com.theatre.theatre.exception;

public class ShowNotFoundException extends RuntimeException {
    public ShowNotFoundException(Long id) {
        super("Show not found: " + id);
    }
}
