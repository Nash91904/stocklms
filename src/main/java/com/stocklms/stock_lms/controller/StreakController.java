package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.StreakPatchRequestDto;
import com.stocklms.stock_lms.dto.StreakResponseDto;
import com.stocklms.stock_lms.service.StreakService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for viewing and updating user engagement streaks.
 */
@RestController
@RequestMapping("/api/streaks")
@RequiredArgsConstructor
public class StreakController {

    private final StreakService streakService;

    @GetMapping("/{id}")
    public StreakResponseDto getStreakById(@PathVariable Long id) {
        return streakService.getStreakById(id);
    }

    @GetMapping("/user/{userId}")
    public StreakResponseDto getStreakByUserId(@PathVariable Long userId) {
        return streakService.getStreakByUserId(userId);
    }

    @PatchMapping("/{id}")
    public StreakResponseDto patchStreak(@PathVariable Long id, @RequestBody StreakPatchRequestDto dto) {
        return streakService.patchStreak(id, dto);
    }
}
