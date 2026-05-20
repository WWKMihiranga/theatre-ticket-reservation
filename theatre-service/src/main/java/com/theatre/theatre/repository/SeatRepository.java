package com.theatre.theatre.repository;

import com.theatre.theatre.entity.Seat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByShowIdOrderByRowNumberAscSeatNumberAsc(Long showId);

    Optional<Seat> findByShowIdAndRowNumberAndSeatNumber(Long showId, int rowNumber, int seatNumber);

    /**
     * Pessimistic write lock — guarantees that during a booking transaction
     * no other transaction can read/modify this seat row. This is the real-world
     * replacement for the coursework's single-threaded `row_1[seat-1] = 1` assignment.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Seat s WHERE s.show.id = :showId AND s.rowNumber = :row AND s.seatNumber = :seat")
    Optional<Seat> findForUpdate(@Param("showId") Long showId,
                                  @Param("row") int rowNumber,
                                  @Param("seat") int seatNumber);

    long countByShowIdAndStatus(Long showId, Seat.Status status);
}
