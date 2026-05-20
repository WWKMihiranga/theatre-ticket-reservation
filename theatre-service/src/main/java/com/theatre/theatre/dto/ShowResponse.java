package com.theatre.theatre.dto;

import java.time.LocalDateTime;

public record ShowResponse(
        Long id,
        String title,
        String description,
        String venue,
        LocalDateTime showTime,
        long availableSeats,
        long totalSeats
) {}
