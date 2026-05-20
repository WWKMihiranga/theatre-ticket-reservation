package com.theatre.user.service;

import com.theatre.user.dto.AuthResponse;
import com.theatre.user.dto.LoginRequest;
import com.theatre.user.dto.RegisterRequest;
import com.theatre.user.entity.User;
import com.theatre.user.exception.EmailAlreadyExistsException;
import com.theatre.user.exception.InvalidCredentialsException;
import com.theatre.user.exception.NicAlreadyExistsException;
import com.theatre.user.mapper.UserMapper;
import com.theatre.user.repository.UserRepository;
import com.theatre.user.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        if (userRepository.existsByNic(request.nic())) {
            throw new NicAlreadyExistsException(request.nic());
        }

        User user = User.builder()
                .nic(request.nic())
                .name(request.name())
                .surname(request.surname())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(User.Role.USER)
                .build();

        User saved = userRepository.save(user);
        log.info("Registered new user id={} email={}", saved.getId(), saved.getEmail());

        String token = jwtService.generateToken(saved);
        return AuthResponse.of(token, jwtService.getExpirationMs(), userMapper.toResponse(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);
        log.info("User logged in id={}", user.getId());
        return AuthResponse.of(token, jwtService.getExpirationMs(), userMapper.toResponse(user));
    }
}
