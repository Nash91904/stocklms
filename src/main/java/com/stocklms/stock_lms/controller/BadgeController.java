package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.BadgeRequestDto;
import com.stocklms.stock_lms.dto.BadgeResponseDto;
import com.stocklms.stock_lms.service.BadgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * REST controller for awarding and viewing user badges.
 */
@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeService badgeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BadgeResponseDto awardBadge(@Valid @RequestBody BadgeRequestDto dto) {
        return badgeService.awardBadge(dto);
    }

    @GetMapping("/user/{userId}")
    public List<BadgeResponseDto> getBadgesByUserPath(@PathVariable Long userId) {
        return badgeService.getBadgesByUser(userId);
    }

    @GetMapping
    public List<BadgeResponseDto> getBadgesByUserQuery(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return badgeService.getBadgesByUser(userId);
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parameter 'userId' is required");
    }
}
