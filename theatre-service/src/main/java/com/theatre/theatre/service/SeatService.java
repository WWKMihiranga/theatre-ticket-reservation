package com.theatre.theatre.service;

import com.theatre.theatre.dto.SeatMapResponse;
import com.theatre.theatre.dto.SeatResponse;
import com.theatre.theatre.entity.Seat;
import com.theatre.theatre.entity.Show;
import com.theatre.theatre.entity.TheatreLayout;
import com.theatre.theatre.exception.InvalidSeatException;
import com.theatre.theatre.exception.SeatAlreadyBookedException;
import com.theatre.theatre.exception.SeatNotFoundException;
import com.theatre.theatre.exception.ShowNotFoundException;
import com.theatre.theatre.mapper.SeatMapper;
import com.theatre.theatre.repository.SeatRepository;
import com.theatre.theatre.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * All the seat operations from the coursework:
 *  - print_seating_area / show_available  →  getSeatMap()
 *  - buy_ticket (the seat-locking half)   →  bookSeat()
 *  - cancel_ticket (the seat-release half) → releaseSeat()
 *
 * Validation rules preserved from the original:
 *  - row must be 1, 2, or 3
 *  - seat must be 1..rowLength
 *  - seat must be AVAILABLE before booking
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SeatService {

    private final SeatRepository seatRepository;
    private final ShowRepository showRepository;
    private final SeatMapper seatMapper;

    @Transactional(readOnly = true)
    public SeatMapResponse getSeatMap(Long showId) {
        Show show = showRepository.findById(showId)
                .orElseThrow(() -> new ShowNotFoundException(showId));

        List<Seat> seats = seatRepository.findByShowIdOrderByRowNumberAscSeatNumberAsc(showId);

        Map<Integer, List<Seat>> byRow = seats.stream()
                .collect(Collectors.groupingBy(Seat::getRowNumber));

        List<SeatMapResponse.RowGroup> rows = TheatreLayout.VALID_ROWS.stream()
                .map(rowNum -> new SeatMapResponse.RowGroup(
                        rowNum,
                        TheatreLayout.seatsInRow(rowNum),
                        byRow.getOrDefault(rowNum, List.of()).stream()
                                .map(seatMapper::toResponse)
                                .toList()
                ))
                .toList();

        return new SeatMapResponse(show.getId(), show.getTitle(), rows);
    }

    @Transactional(readOnly = true)
    public List<SeatResponse> listAvailable(Long showId) {
        if (!showRepository.existsById(showId)) {
            throw new ShowNotFoundException(showId);
        }
        return seatRepository.findByShowIdOrderByRowNumberAscSeatNumberAsc(showId).stream()
                .filter(s -> s.getStatus() == Seat.Status.AVAILABLE)
                .map(seatMapper::toResponse)
                .toList();
    }

    /**
     * Books a seat. Uses pessimistic locking so two concurrent bookings on
     * the same seat are serialised — the second one sees BOOKED and fails.
     *
     * NOTE: in Phase 3, the Booking Service will be the caller, not the user
     * directly. For now it's exposed for testing.
     */
    @Transactional
    public SeatResponse bookSeat(Long showId, int row, int seatNumber) {
        validateSeatCoordinates(row, seatNumber);
        if (!showRepository.existsById(showId)) {
            throw new ShowNotFoundException(showId);
        }

        Seat seat = seatRepository.findForUpdate(showId, row, seatNumber)
                .orElseThrow(() -> new SeatNotFoundException(
                        "Seat not found: show=" + showId + " row=" + row + " seat=" + seatNumber));

        if (seat.getStatus() == Seat.Status.BOOKED) {
            throw new SeatAlreadyBookedException(row, seatNumber);
        }

        seat.setStatus(Seat.Status.BOOKED);
        Seat saved = seatRepository.save(seat);
        log.info("Booked seat showId={} row={} seat={}", showId, row, seatNumber);
        return seatMapper.toResponse(saved);
    }

    /**
     * Releases a previously-booked seat (used on cancellation).
     */
    @Transactional
    public SeatResponse releaseSeat(Long showId, int row, int seatNumber) {
        validateSeatCoordinates(row, seatNumber);

        Seat seat = seatRepository.findForUpdate(showId, row, seatNumber)
                .orElseThrow(() -> new SeatNotFoundException(
                        "Seat not found: show=" + showId + " row=" + row + " seat=" + seatNumber));

        seat.setStatus(Seat.Status.AVAILABLE);
        Seat saved = seatRepository.save(seat);
        log.info("Released seat showId={} row={} seat={}", showId, row, seatNumber);
        return seatMapper.toResponse(saved);
    }

    /**
     * Coursework's range check, lifted into a reusable method.
     */
    private void validateSeatCoordinates(int row, int seat) {
        if (!TheatreLayout.isValidSeat(row, seat)) {
            throw new InvalidSeatException(
                    "Invalid row/seat. Row must be 1-3 and seat must be within range " +
                            "(row 1: 1-12, row 2: 1-16, row 3: 1-20). Got row=" + row + " seat=" + seat);
        }
    }
}
