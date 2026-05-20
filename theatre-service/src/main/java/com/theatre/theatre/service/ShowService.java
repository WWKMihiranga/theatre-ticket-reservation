package com.theatre.theatre.service;

import com.theatre.theatre.dto.CreateShowRequest;
import com.theatre.theatre.dto.ShowResponse;
import com.theatre.theatre.entity.Seat;
import com.theatre.theatre.entity.Show;
import com.theatre.theatre.entity.TheatreLayout;
import com.theatre.theatre.exception.ShowNotFoundException;
import com.theatre.theatre.mapper.ShowMapper;
import com.theatre.theatre.repository.SeatRepository;
import com.theatre.theatre.repository.ShowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShowService {

    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final ShowMapper showMapper;

    /**
     * Creates a new show and auto-generates its 48 seats (12 + 16 + 20)
     * with the coursework's tiered pricing ($10 / $20 / $30).
     */
    @Transactional
    public ShowResponse create(CreateShowRequest request) {
        Show show = Show.builder()
                .title(request.title())
                .description(request.description())
                .venue(request.venue())
                .showTime(request.showTime())
                .build();

        List<Seat> seats = new ArrayList<>(TheatreLayout.totalSeats());
        for (int row : TheatreLayout.VALID_ROWS) {
            int seatsInRow = TheatreLayout.seatsInRow(row);
            for (int seatNum = 1; seatNum <= seatsInRow; seatNum++) {
                Seat seat = Seat.builder()
                        .show(show)
                        .rowNumber(row)
                        .seatNumber(seatNum)
                        .price(TheatreLayout.priceForRow(row))
                        .status(Seat.Status.AVAILABLE)
                        .build();
                seats.add(seat);
            }
        }
        show.setSeats(seats);

        Show saved = showRepository.save(show);
        log.info("Created show id={} title='{}' with {} seats", saved.getId(), saved.getTitle(), seats.size());

        return showMapper.toResponse(saved, seats.size(), seats.size());
    }

    @Transactional(readOnly = true)
    public List<ShowResponse> listAll() {
        return showRepository.findAll().stream()
                .map(s -> showMapper.toResponse(
                        s,
                        seatRepository.countByShowIdAndStatus(s.getId(), Seat.Status.AVAILABLE),
                        TheatreLayout.totalSeats()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ShowResponse getById(Long id) {
        Show show = showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException(id));
        long available = seatRepository.countByShowIdAndStatus(id, Seat.Status.AVAILABLE);
        return showMapper.toResponse(show, available, TheatreLayout.totalSeats());
    }

    @Transactional(readOnly = true)
    public Show findEntity(Long id) {
        return showRepository.findById(id)
                .orElseThrow(() -> new ShowNotFoundException(id));
    }
}
