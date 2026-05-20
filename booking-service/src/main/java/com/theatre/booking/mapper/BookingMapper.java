package com.theatre.booking.mapper;

import com.theatre.booking.dto.BookingResponse;
import com.theatre.booking.entity.Booking;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingResponse toResponse(Booking booking);
}
