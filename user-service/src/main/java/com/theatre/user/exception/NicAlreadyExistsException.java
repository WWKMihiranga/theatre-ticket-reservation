package com.theatre.user.exception;

public class NicAlreadyExistsException extends RuntimeException {
    public NicAlreadyExistsException(String nic) {
        super("NIC already registered: " + nic);
    }
}
