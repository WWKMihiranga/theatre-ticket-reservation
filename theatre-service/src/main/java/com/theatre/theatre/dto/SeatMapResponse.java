package com.theatre.theatre.dto;

import java.util.List;

/**
 * Returns the full seating area grouped by row. Mirrors the coursework's
 * `print_seating_area` output but in a UI-friendly shape.
 */
public record SeatMapResponse(
        Long showId,
        String showTitle,
        List<RowGroup> rows
) {
    public record RowGroup(
            int rowNumber,
            int totalSeats,
            List<SeatResponse> seats
    ) {}
}
