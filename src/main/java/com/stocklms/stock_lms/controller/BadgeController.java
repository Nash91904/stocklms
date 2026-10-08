package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.BadgeRequestDto;
import com.stocklms.stock_lms.dto.BadgeResponseDto;
import com.stocklms.stock_lms.service.BadgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST controller for awarding and viewing user badges.
 */
@Tag(name = "Badges", description = "Award and view user achievement badges")
@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @Operation(summary = "Award a badge to a user",
               responses = {@ApiResponse(responseCode = "201", description = "Badge awarded"),
                            @ApiResponse(responseCode = "404", description = "User not found")})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BadgeResponseDto awardBadge(@Valid @RequestBody BadgeRequestDto dto) {
        return badgeService.awardBadge(dto);
    }

    @Operation(summary = "Get badges by user ID path",
               responses = {@ApiResponse(responseCode = "404", description = "User not found")})
    @GetMapping("/user/{userId}")
    public List<BadgeResponseDto> getBadgesByUserPath(@PathVariable Long userId) {
        return badgeService.getBadgesByUser(userId);
    }

    @Operation(summary = "Get badges by user ID query parameter",
               responses = {@ApiResponse(responseCode = "400", description = "Parameter 'userId' is required"),
                            @ApiResponse(responseCode = "404", description = "User not found")})
    @GetMapping
    public List<BadgeResponseDto> getBadgesByUserQuery(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return badgeService.getBadgesByUser(userId);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter 'userId' is required");
    }
}
