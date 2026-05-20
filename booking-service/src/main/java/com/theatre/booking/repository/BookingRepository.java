package com.theatre.booking.repository;

import com.theatre.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Booking.Status status);

    /** Sort-by-price (coursework operation 6). */
    List<Booking> findByUserIdAndStatusOrderByPriceAsc(Long userId, Booking.Status status);

    /** For cancel: find by user + row + seat — mirrors coursework's cancel-matching rule. */
    Optional<Booking> findByUserIdAndShowIdAndRowNumberAndSeatNumberAndStatus(
            Long userId, Long showId, int rowNumber, int seatNumber, Booking.Status status);

    Optional<Booking> findByIdAndUserId(Long id, Long userId);

    /** Total-price (coursework operation 5). */
    default BigDecimal sumPriceByUserAndStatus(Long userId, Booking.Status status) {
        return findByUserIdAndStatusOrderByCreatedAtDesc(userId, status).stream()
                .map(Booking::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
