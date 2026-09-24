package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.StreakPatchRequestDto;
import com.stocklms.stock_lms.dto.StreakResponseDto;
import com.stocklms.stock_lms.service.StreakService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for viewing and updating user engagement streaks.
 */
@Tag(name = "Streaks", description = "View and patch user daily streaks")
@RestController
@RequestMapping("/api/streaks")
@RequiredArgsConstructor
public class StreakController {

    private final StreakService streakService;

    @Operation(summary = "Get a streak by streak ID",
               responses = {@ApiResponse(responseCode = "404", description = "Streak not found")})
    @GetMapping("/{id}")
    public StreakResponseDto getStreakById(@PathVariable Long id) {
        return streakService.getStreakById(id);
    }

    @Operation(summary = "Get a streak by user ID",
               responses = {@ApiResponse(responseCode = "404", description = "Streak not found")})
    @GetMapping("/user/{userId}")
    public StreakResponseDto getStreakByUserId(@PathVariable Long userId) {
        return streakService.getStreakByUserId(userId);
    }

    @Operation(summary = "Patch a streak (currentStreak, longestStreak, lastActiveDate, freezeCount)",
               responses = {@ApiResponse(responseCode = "404", description = "Streak not found")})
    @PatchMapping("/{id}")
    public StreakResponseDto patchStreak(@PathVariable Long id, @RequestBody StreakPatchRequestDto dto) {
        return streakService.patchStreak(id, dto);
    }
}
