package com.theatre.theatre.service;

import com.theatre.theatre.dto.CreateShowRequest;
import com.theatre.theatre.dto.ShowResponse;
import com.theatre.theatre.entity.Seat;
import com.theatre.theatre.entity.TheatreLayout;
import com.theatre.theatre.exception.InvalidSeatException;
import com.theatre.theatre.exception.SeatAlreadyBookedException;
import com.theatre.theatre.exception.ShowNotFoundException;
import com.theatre.theatre.repository.SeatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class SeatServiceIntegrationTest {

    @Autowired ShowService showService;
    @Autowired SeatService seatService;
    @Autowired SeatRepository seatRepository;

    private Long showId;

    @BeforeEach
    void createDemoShow() {
        ShowResponse show = showService.create(new CreateShowRequest(
                "Test Show",
                "Description",
                "Test Venue",
                LocalDateTime.now().plusDays(1)
        ));
        showId = show.id();
    }

    @Test
    void newShow_has_48_available_seats() {
        List<Seat> seats = seatRepository.findByShowIdOrderByRowNumberAscSeatNumberAsc(showId);
        assertThat(seats).hasSize(48);
        assertThat(seats).allMatch(s -> s.getStatus() == Seat.Status.AVAILABLE);

        long row1 = seats.stream().filter(s -> s.getRowNumber() == 1).count();
        long row2 = seats.stream().filter(s -> s.getRowNumber() == 2).count();
        long row3 = seats.stream().filter(s -> s.getRowNumber() == 3).count();
        assertThat(row1).isEqualTo(TheatreLayout.ROW_1_SEATS);
        assertThat(row2).isEqualTo(TheatreLayout.ROW_2_SEATS);
        assertThat(row3).isEqualTo(TheatreLayout.ROW_3_SEATS);
    }

    @Test
    void bookSeat_marksSeatBooked() {
        seatService.bookSeat(showId, 1, 5);
        Seat seat = seatRepository.findByShowIdAndRowNumberAndSeatNumber(showId, 1, 5).orElseThrow();
        assertThat(seat.getStatus()).isEqualTo(Seat.Status.BOOKED);
    }

    @Test
    void bookSeat_rejectsDoubleBooking() {
        seatService.bookSeat(showId, 2, 3);
        assertThatThrownBy(() -> seatService.bookSeat(showId, 2, 3))
                .isInstanceOf(SeatAlreadyBookedException.class);
    }

    @Test
    void bookSeat_rejectsOutOfRangeRow() {
        assertThatThrownBy(() -> seatService.bookSeat(showId, 4, 1))
                .isInstanceOf(InvalidSeatException.class);
    }

    @Test
    void bookSeat_rejectsOutOfRangeSeat() {
        assertThatThrownBy(() -> seatService.bookSeat(showId, 1, 13))
                .isInstanceOf(InvalidSeatException.class);
        assertThatThrownBy(() -> seatService.bookSeat(showId, 2, 17))
                .isInstanceOf(InvalidSeatException.class);
        assertThatThrownBy(() -> seatService.bookSeat(showId, 3, 21))
                .isInstanceOf(InvalidSeatException.class);
    }

    @Test
    void releaseSeat_makesItAvailableAgain() {
        seatService.bookSeat(showId, 3, 10);
        seatService.releaseSeat(showId, 3, 10);
        Seat seat = seatRepository.findByShowIdAndRowNumberAndSeatNumber(showId, 3, 10).orElseThrow();
        assertThat(seat.getStatus()).isEqualTo(Seat.Status.AVAILABLE);
    }

    @Test
    void getSeatMap_returnsThreeRowsInOrder() {
        var map = seatService.getSeatMap(showId);
        assertThat(map.rows()).hasSize(3);
        assertThat(map.rows().get(0).rowNumber()).isEqualTo(1);
        assertThat(map.rows().get(0).totalSeats()).isEqualTo(12);
        assertThat(map.rows().get(1).totalSeats()).isEqualTo(16);
        assertThat(map.rows().get(2).totalSeats()).isEqualTo(20);
    }

    @Test
    void getSeatMap_throwsForUnknownShow() {
        assertThatThrownBy(() -> seatService.getSeatMap(999_999L))
                .isInstanceOf(ShowNotFoundException.class);
    }
}
