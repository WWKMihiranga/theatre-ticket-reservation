package com.theatre.theatre.mapper;

import com.theatre.theatre.dto.ShowResponse;
import com.theatre.theatre.entity.Show;
import org.springframework.stereotype.Component;

@Component
public class ShowMapper {

    public ShowResponse toResponse(Show show, long availableSeats, long totalSeats) {
        return new ShowResponse(
                show.getId(),
                show.getTitle(),
                show.getDescription(),
                show.getVenue(),
                show.getShowTime(),
                availableSeats,
                totalSeats
        );
    }
}
