package com.theatre.user.mapper;

import com.theatre.user.dto.UserResponse;
import com.theatre.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);
}
