package com.theatre.booking.client.dto;

import java.util.List;

public record SeatMapInfo(
        Long showId,
        String showTitle,
        List<RowGroup> rows
) {
    public record RowGroup(
            int rowNumber,
            int totalSeats,
            List<SeatInfo> seats
    ) {}
}
