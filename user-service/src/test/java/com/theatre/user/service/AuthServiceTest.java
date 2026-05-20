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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtService jwtService;
    @Mock UserMapper userMapper;

    @InjectMocks AuthService authService;

    private RegisterRequest registerRequest;
    private User savedUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("123456789V", "John", "Doe", "john@example.com", "password123");
        savedUser = User.builder()
                .id(1L).nic("123456789V").name("John").surname("Doe")
                .email("john@example.com").passwordHash("hashed").role(User.Role.USER).build();
    }

    @Test
    void register_succeeds_whenEmailAndNicUnique() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByNic(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any())).thenReturn("token123");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        AuthResponse response = authService.register(registerRequest);

        assertThat(response.accessToken()).isEqualTo("token123");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void register_fails_whenEmailAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyExistsException.class);
    }

    @Test
    void register_fails_whenNicAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.existsByNic(anyString())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(NicAlreadyExistsException.class);
    }

    @Test
    void login_succeeds_withValidCredentials() {
        LoginRequest req = new LoginRequest("john@example.com", "password123");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(req.password(), savedUser.getPasswordHash())).thenReturn(true);
        when(jwtService.generateToken(savedUser)).thenReturn("token123");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        AuthResponse response = authService.login(req);
        assertThat(response.accessToken()).isEqualTo("token123");
    }

    @Test
    void login_fails_whenEmailNotFound() {
        LoginRequest req = new LoginRequest("nope@example.com", "password123");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void login_fails_whenPasswordWrong() {
        LoginRequest req = new LoginRequest("john@example.com", "wrong");
        when(userRepository.findByEmail(req.email())).thenReturn(Optional.of(savedUser));
        when(passwordEncoder.matches(req.password(), savedUser.getPasswordHash())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
