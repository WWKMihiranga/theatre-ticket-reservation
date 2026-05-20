package com.theatre.theatre.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Locks in the coursework's specification: 12/16/20 seats per row and
 * $10/$20/$30 pricing. If any future change breaks these, the build fails.
 */
class TheatreLayoutTest {

    @Test
    void row1_has_12_seats_at_10_dollars() {
        assertThat(TheatreLayout.seatsInRow(1)).isEqualTo(12);
        assertThat(TheatreLayout.priceForRow(1)).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    void row2_has_16_seats_at_20_dollars() {
        assertThat(TheatreLayout.seatsInRow(2)).isEqualTo(16);
        assertThat(TheatreLayout.priceForRow(2)).isEqualByComparingTo(new BigDecimal("20.00"));
    }

    @Test
    void row3_has_20_seats_at_30_dollars() {
        assertThat(TheatreLayout.seatsInRow(3)).isEqualTo(20);
        assertThat(TheatreLayout.priceForRow(3)).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    void totalSeats_is_48() {
        assertThat(TheatreLayout.totalSeats()).isEqualTo(48);
    }

    @Test
    void isValidSeat_acceptsValidCoordinates() {
        assertThat(TheatreLayout.isValidSeat(1, 1)).isTrue();
        assertThat(TheatreLayout.isValidSeat(1, 12)).isTrue();
        assertThat(TheatreLayout.isValidSeat(2, 16)).isTrue();
        assertThat(TheatreLayout.isValidSeat(3, 20)).isTrue();
    }

    @Test
    void isValidSeat_rejectsOutOfRange() {
        assertThat(TheatreLayout.isValidSeat(0, 1)).isFalse();
        assertThat(TheatreLayout.isValidSeat(4, 1)).isFalse();
        assertThat(TheatreLayout.isValidSeat(1, 13)).isFalse();
        assertThat(TheatreLayout.isValidSeat(2, 17)).isFalse();
        assertThat(TheatreLayout.isValidSeat(3, 21)).isFalse();
        assertThat(TheatreLayout.isValidSeat(1, 0)).isFalse();
    }

    @Test
    void invalidRow_throws() {
        assertThatThrownBy(() -> TheatreLayout.seatsInRow(4))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> TheatreLayout.priceForRow(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
