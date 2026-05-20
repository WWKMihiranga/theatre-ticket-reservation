package com.theatre.booking.controller;

import com.theatre.booking.dto.BookingResponse;
import com.theatre.booking.dto.BookingSummaryResponse;
import com.theatre.booking.dto.CreateBookingRequest;
import com.theatre.booking.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Buy, cancel, and list ticket bookings")
@SecurityRequirement(name = "bearerAuth")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @Operation(summary = "Book a seat. Coursework op 1: buy_ticket.")
    public ResponseEntity<BookingResponse> create(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(bookingService.createBooking(Long.parseLong(userId), request));
    }

    @GetMapping("/me")
    @Operation(summary = "List my bookings. Coursework op 4: print_tickets. " +
            "Use ?sort=price for sort-by-price (coursework op 6).")
    public ResponseEntity<List<BookingResponse>> listMine(
            @AuthenticationPrincipal String userId,
            @RequestParam(name = "sort", required = false) String sort) {
        Long uid = Long.parseLong(userId);
        if ("price".equalsIgnoreCase(sort)) {
            return ResponseEntity.ok(bookingService.listMineSortedByPrice(uid));
        }
        return ResponseEntity.ok(bookingService.listMine(uid));
    }

    @GetMapping("/me/summary")
    @Operation(summary = "Get my total ticket count and total price. Coursework op 5.")
    public ResponseEntity<BookingSummaryResponse> summary(@AuthenticationPrincipal String userId) {
        return ResponseEntity.ok(bookingService.summary(Long.parseLong(userId)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Cancel one of my bookings by id. Coursework op 2: cancel_ticket.")
    public ResponseEntity<BookingResponse> cancel(
            @AuthenticationPrincipal String userId,
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(bookingService.cancelBooking(Long.parseLong(userId), id));
    }

    /**
     * Alternative cancel matching the coursework's original "cancel by row + seat" form.
     */
    @DeleteMapping("/me/shows/{showId}/seats/{row}/{seat}")
    @Operation(summary = "Cancel by row+seat (coursework-style matching). Coursework op 2 alternative.")
    public ResponseEntity<BookingResponse> cancelByCoordinates(
            @AuthenticationPrincipal String userId,
            @PathVariable("showId") Long showId,
            @PathVariable("row") int row,
            @PathVariable("seat") int seat) {
        return ResponseEntity.ok(
                bookingService.cancelByRowAndSeat(Long.parseLong(userId), showId, row, seat));
    }
}
