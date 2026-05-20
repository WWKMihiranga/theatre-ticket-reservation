package com.theatre.user.service;

import com.theatre.user.dto.UserResponse;
import com.theatre.user.exception.UserNotFoundException;
import com.theatre.user.mapper.UserMapper;
import com.theatre.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + id));
    }
}
