package com.theatre.user.dto;

public record UserResponse(
        Long id,
        String nic,
        String name,
        String surname,
        String email,
        String role
) {}
