package com.theatre.booking.service;

import com.theatre.booking.client.TheatreServiceClient;
import com.theatre.booking.client.dto.SeatInfo;
import com.theatre.booking.dto.BookingResponse;
import com.theatre.booking.dto.CreateBookingRequest;
import com.theatre.booking.entity.Booking;
import com.theatre.booking.exception.BookingNotFoundException;
import com.theatre.booking.exception.SeatUnavailableException;
import com.theatre.booking.mapper.BookingMapper;
import com.theatre.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock BookingRepository bookingRepository;
    @Mock TheatreServiceClient theatreClient;
    @Mock BookingMapper bookingMapper;
    @InjectMocks BookingService bookingService;

    private final Long userId = 42L;
    private final Long showId = 1L;

    @BeforeEach
    void setUp() {
        // Map any saved booking to a basic response shape
        when(bookingMapper.toResponse(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            return new BookingResponse(
                    b.getId(), b.getUserId(), b.getShowId(),
                    b.getRowNumber(), b.getSeatNumber(),
                    b.getPrice(), b.getStatus().name(), null
            );
        });
    }

    @Test
    void createBooking_succeeds_andPersistsWithPriceFromTheatreService() {
        var seatInfo = new SeatInfo(100L, 2, 5, new BigDecimal("20.00"), "BOOKED");
        when(theatreClient.bookSeat(showId, 2, 5)).thenReturn(seatInfo);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setId(7L);
            return b;
        });

        BookingResponse response = bookingService.createBooking(userId,
                new CreateBookingRequest(showId, 2, 5));

        assertThat(response.id()).isEqualTo(7L);
        assertThat(response.price()).isEqualByComparingTo("20.00");
        assertThat(response.status()).isEqualTo("CONFIRMED");
        verify(theatreClient).bookSeat(showId, 2, 5);
    }

    @Test
    void createBooking_releasesSeat_ifDbInsertFails() {
        var seatInfo = new SeatInfo(100L, 1, 3, new BigDecimal("10.00"), "BOOKED");
        when(theatreClient.bookSeat(showId, 1, 3)).thenReturn(seatInfo);
        when(bookingRepository.save(any(Booking.class)))
                .thenThrow(new DataIntegrityViolationException("unique constraint violated"));

        assertThatThrownBy(() -> bookingService.createBooking(userId,
                new CreateBookingRequest(showId, 1, 3)))
                .isInstanceOf(SeatUnavailableException.class);

        verify(theatreClient).releaseSeat(showId, 1, 3);
    }

    @Test
    void cancelBooking_marksCancelled_andReleasesSeat() {
        Booking existing = Booking.builder()
                .id(7L).userId(userId).showId(showId)
                .rowNumber(3).seatNumber(15)
                .price(new BigDecimal("30.00"))
                .status(Booking.Status.CONFIRMED).build();
        when(bookingRepository.findByIdAndUserId(7L, userId)).thenReturn(Optional.of(existing));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingResponse response = bookingService.cancelBooking(userId, 7L);

        assertThat(response.status()).isEqualTo("CANCELLED");
        verify(theatreClient).releaseSeat(showId, 3, 15);
    }

    @Test
    void cancelBooking_throws_whenNotMine() {
        when(bookingRepository.findByIdAndUserId(7L, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.cancelBooking(userId, 7L))
                .isInstanceOf(BookingNotFoundException.class);
        verify(theatreClient, never()).releaseSeat(anyLong(), anyInt(), anyInt());
    }

    @Test
    void cancelBooking_throws_whenAlreadyCancelled() {
        Booking existing = Booking.builder()
                .id(7L).userId(userId).showId(showId)
                .rowNumber(3).seatNumber(15).price(new BigDecimal("30.00"))
                .status(Booking.Status.CANCELLED).build();
        when(bookingRepository.findByIdAndUserId(7L, userId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> bookingService.cancelBooking(userId, 7L))
                .isInstanceOf(BookingNotFoundException.class);
    }

    @Test
    void summary_returnsCountAndTotal() {
        when(bookingRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, Booking.Status.CONFIRMED))
                .thenReturn(List.of(
                        Booking.builder().price(new BigDecimal("10.00")).build(),
                        Booking.builder().price(new BigDecimal("20.00")).build(),
                        Booking.builder().price(new BigDecimal("30.00")).build()
                ));

        var summary = bookingService.summary(userId);

        assertThat(summary.totalBookings()).isEqualTo(3);
        assertThat(summary.totalPrice()).isEqualByComparingTo("60.00");
    }
}
