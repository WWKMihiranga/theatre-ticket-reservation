package com.theatre.theatre.controller;

import com.theatre.theatre.dto.CreateShowRequest;
import com.theatre.theatre.dto.ShowResponse;
import com.theatre.theatre.service.ShowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/shows")
@RequiredArgsConstructor
@Tag(name = "Shows", description = "Browse and manage shows")
public class ShowController {

    private final ShowService showService;

    @GetMapping
    @Operation(summary = "List all shows (public)")
    public ResponseEntity<List<ShowResponse>> listAll() {
        return ResponseEntity.ok(showService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single show by ID (public)")
    public ResponseEntity<ShowResponse> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(showService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new show (admin only). Auto-generates 48 seats with tiered pricing.")
    public ResponseEntity<ShowResponse> create(@Valid @RequestBody CreateShowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(showService.create(request));
    }
}
