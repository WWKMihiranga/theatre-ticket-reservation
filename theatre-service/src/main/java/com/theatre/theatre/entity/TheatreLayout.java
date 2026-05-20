package com.theatre.theatre.entity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Encodes the coursework's fixed theatre layout and pricing rules:
 *
 * - Row 1: 12 seats @ $10
 * - Row 2: 16 seats @ $20
 * - Row 3: 20 seats @ $30
 *
 * Treating these as constants (not configuration) is intentional — they are
 * part of the coursework specification, not runtime tunables.
 */
public final class TheatreLayout {

    public static final int ROW_1_SEATS = 12;
    public static final int ROW_2_SEATS = 16;
    public static final int ROW_3_SEATS = 20;

    public static final BigDecimal ROW_1_PRICE = new BigDecimal("10.00");
    public static final BigDecimal ROW_2_PRICE = new BigDecimal("20.00");
    public static final BigDecimal ROW_3_PRICE = new BigDecimal("30.00");

    public static final List<Integer> VALID_ROWS = List.of(1, 2, 3);

    private static final Map<Integer, Integer> SEATS_PER_ROW = Map.of(
            1, ROW_1_SEATS,
            2, ROW_2_SEATS,
            3, ROW_3_SEATS
    );

    private static final Map<Integer, BigDecimal> PRICE_PER_ROW = Map.of(
            1, ROW_1_PRICE,
            2, ROW_2_PRICE,
            3, ROW_3_PRICE
    );

    private TheatreLayout() {}

    public static int seatsInRow(int row) {
        Integer seats = SEATS_PER_ROW.get(row);
        if (seats == null) {
            throw new IllegalArgumentException("Invalid row: " + row + " (must be 1, 2, or 3)");
        }
        return seats;
    }

    public static BigDecimal priceForRow(int row) {
        BigDecimal price = PRICE_PER_ROW.get(row);
        if (price == null) {
            throw new IllegalArgumentException("Invalid row: " + row + " (must be 1, 2, or 3)");
        }
        return price;
    }

    public static boolean isValidSeat(int row, int seat) {
        if (!VALID_ROWS.contains(row)) return false;
        return seat >= 1 && seat <= seatsInRow(row);
    }

    public static int totalSeats() {
        return ROW_1_SEATS + ROW_2_SEATS + ROW_3_SEATS;
    }
}
