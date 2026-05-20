package com.theatre.booking.service;

import com.theatre.booking.client.TheatreServiceClient;
import com.theatre.booking.client.dto.SeatInfo;
import com.theatre.booking.dto.BookingResponse;
import com.theatre.booking.dto.BookingSummaryResponse;
import com.theatre.booking.dto.CreateBookingRequest;
import com.theatre.booking.entity.Booking;
import com.theatre.booking.exception.BookingNotFoundException;
import com.theatre.booking.exception.SeatUnavailableException;
import com.theatre.booking.mapper.BookingMapper;
import com.theatre.booking.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * All the coursework's ticket operations, preserved in microservice form:
 *
 *   Coursework op 1 (buy_ticket)    → createBooking()
 *   Coursework op 2 (cancel_ticket) → cancelBooking() — matches by row + seat
 *   Coursework op 4 (print_tickets) → listMine()
 *   Coursework op 5 (total price)   → summary()
 *   Coursework op 6 (sort by price) → listMineSortedByPrice()
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TheatreServiceClient theatreClient;
    private final BookingMapper bookingMapper;

    /**
     * Orchestrated booking flow:
     *   1. Reserve seat in theatre-service (gets price + validates coords)
     *   2. Persist booking row
     *   3. If persisting fails (e.g. our DB unique constraint), release the seat
     */
    @Transactional
    public BookingResponse createBooking(Long userId, CreateBookingRequest request) {
        // Step 1 — theatre-service does its own validation (row range, seat range, availability).
        SeatInfo reservedSeat = theatreClient.bookSeat(
                request.showId(), request.rowNumber(), request.seatNumber()
        );

        // Step 2 — record the booking with the price returned from theatre-service.
        try {
            Booking booking = Booking.builder()
                    .userId(userId)
                    .showId(request.showId())
                    .rowNumber(request.rowNumber())
                    .seatNumber(request.seatNumber())
                    .price(reservedSeat.price())
                    .status(Booking.Status.CONFIRMED)
                    .build();
            Booking saved = bookingRepository.save(booking);
            log.info("Created booking id={} userId={} showId={} row={} seat={}",
                    saved.getId(), userId, request.showId(), request.rowNumber(), request.seatNumber());
            return bookingMapper.toResponse(saved);
        } catch (DataIntegrityViolationException e) {
            // Step 3 — compensating action: someone else booked the same seat between
            // our seat-reservation and our DB insert. Tell theatre-service to release.
            log.warn("Booking DB insert failed, releasing seat showId={} row={} seat={}",
                    request.showId(), request.rowNumber(), request.seatNumber());
            theatreClient.releaseSeat(request.showId(), request.rowNumber(), request.seatNumber());
            throw new SeatUnavailableException(request.rowNumber(), request.seatNumber());
        }
    }

    /**
     * Cancel by booking id. Mirrors coursework's cancel_ticket but with id-based
     * matching (safer) plus the coursework's row+seat lookup as an alternative entry.
     */
    @Transactional
    public BookingResponse cancelBooking(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findByIdAndUserId(bookingId, userId)
                .orElseThrow(() -> new BookingNotFoundException(
                        "Booking " + bookingId + " not found for current user"));

        if (booking.getStatus() == Booking.Status.CANCELLED) {
            throw new BookingNotFoundException("Booking is already cancelled");
        }

        // 1. Release the seat in theatre-service
        theatreClient.releaseSeat(booking.getShowId(), booking.getRowNumber(), booking.getSeatNumber());

        // 2. Mark booking cancelled (we keep the row for history, not delete)
        booking.setStatus(Booking.Status.CANCELLED);
        Booking saved = bookingRepository.save(booking);
        log.info("Cancelled booking id={} userId={}", bookingId, userId);
        return bookingMapper.toResponse(saved);
    }

    /**
     * Coursework-equivalent cancel: find by row + seat for the current user, then cancel.
     */
    @Transactional
    public BookingResponse cancelByRowAndSeat(Long userId, Long showId, int row, int seat) {
        Booking booking = bookingRepository
                .findByUserIdAndShowIdAndRowNumberAndSeatNumberAndStatus(
                        userId, showId, row, seat, Booking.Status.CONFIRMED)
                .orElseThrow(() -> new BookingNotFoundException(
                        "No confirmed booking found for show=" + showId +
                                " row=" + row + " seat=" + seat));
        return cancelBooking(userId, booking.getId());
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listMine(Long userId) {
        return bookingRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, Booking.Status.CONFIRMED)
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listMineSortedByPrice(Long userId) {
        return bookingRepository
                .findByUserIdAndStatusOrderByPriceAsc(userId, Booking.Status.CONFIRMED)
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingSummaryResponse summary(Long userId) {
        List<Booking> confirmed = bookingRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, Booking.Status.CONFIRMED);
        var total = bookingRepository.sumPriceByUserAndStatus(userId, Booking.Status.CONFIRMED);
        return new BookingSummaryResponse(confirmed.size(), total);
    }
}
