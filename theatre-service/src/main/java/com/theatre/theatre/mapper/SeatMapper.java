package com.theatre.theatre.mapper;

import com.theatre.theatre.dto.SeatResponse;
import com.theatre.theatre.entity.Seat;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    SeatResponse toResponse(Seat seat);
}
