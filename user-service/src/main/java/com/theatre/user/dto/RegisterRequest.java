package com.theatre.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Registration payload.
 * NIC, name, surname, email map directly to the coursework Person fields.
 * Email rule from coursework was "contains @ and ." — we enforce that via @Email
 * (which is stricter and also rules out obviously-broken addresses).
 */
public record RegisterRequest(
        @NotBlank(message = "NIC is required")
        @Pattern(regexp = "^[A-Za-z0-9]{6,20}$", message = "NIC must be 6-20 alphanumeric characters")
        String nic,

        @NotBlank(message = "Name is required")
        @Size(max = 80)
        String name,

        @NotBlank(message = "Surname is required")
        @Size(max = 80)
        String surname,

        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 160)
        String email,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be 8-100 characters")
        String password
) {}
