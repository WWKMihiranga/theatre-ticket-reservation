package com.theatre.theatre.controller;

import com.theatre.theatre.dto.SeatBookingRequest;
import com.theatre.theatre.dto.SeatMapResponse;
import com.theatre.theatre.dto.SeatResponse;
import com.theatre.theatre.service.SeatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shows/{showId}/seats")
@RequiredArgsConstructor
@Tag(name = "Seats", description = "Seating area for a show")
public class SeatController {

    private final SeatService seatService;

    @GetMapping
    @Operation(summary = "Full seating map for a show (public). Mirrors print_seating_area().")
    public ResponseEntity<SeatMapResponse> getSeatMap(@PathVariable("showId") Long showId) {
        return ResponseEntity.ok(seatService.getSeatMap(showId));
    }

    @GetMapping("/available")
    @Operation(summary = "List only available seats (public). Mirrors show_available_seats().")
    public ResponseEntity<List<SeatResponse>> listAvailable(@PathVariable("showId") Long showId) {
        return ResponseEntity.ok(seatService.listAvailable(showId));
    }

    /**
     * These two endpoints are exposed for Phase 3 (booking-service) to call
     * service-to-service. They're authenticated but accept any logged-in user;
     * in Phase 3 we'll tighten them to an internal-only call or service token.
     */
    @PostMapping("/book")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Reserve a seat. Will be called by booking-service in Phase 3.")
    public ResponseEntity<SeatResponse> book(@PathVariable("showId") Long showId,
                                             @Valid @RequestBody SeatBookingRequest request) {
        return ResponseEntity.ok(seatService.bookSeat(showId, request.rowNumber(), request.seatNumber()));
    }

    @PostMapping("/release")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Release a previously-booked seat. Called on cancellation.")
    public ResponseEntity<SeatResponse> release(@PathVariable("showId") Long showId,
                                                @Valid @RequestBody SeatBookingRequest request) {
        return ResponseEntity.ok(seatService.releaseSeat(showId, request.rowNumber(), request.seatNumber()));
    }
}
