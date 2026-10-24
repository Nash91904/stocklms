package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.Streak;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.StreakRepository;
import com.stocklms.stock_lms.responsebean.ResponseBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for viewing and updating user engagement streaks.
 * Directly interacts with repositories and returns standard ResponseBean wrappers.
 */
@Slf4j
@Tag(name = "Streaks", description = "View and patch user daily streaks")
@RestController
@RequestMapping("/api/streaks")
@RequiredArgsConstructor
public class StreakController {

    private final StreakRepository streakRepository;

    @Operation(summary = "Get a streak by streak ID",
               responses = {@ApiResponse(responseCode = "404", description = "Streak not found")})
    @GetMapping("/{id}")
    public ResponseEntity<ResponseBean> getStreakById(@PathVariable Long id) {
        log.info("Fetching streak with id: {}", id);
        Streak streak = streakRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Streak", id));
        return ResponseEntity.ok(ResponseBean.success("200", "Success", streak));
    }

    @Operation(summary = "Get a streak by user ID",
               responses = {@ApiResponse(responseCode = "404", description = "Streak not found")})
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseBean> getStreakByUserId(@PathVariable Long userId) {
        log.info("Fetching streak for user id: {}", userId);
        Streak streak = streakRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Streak not found for user id: " + userId));
        return ResponseEntity.ok(ResponseBean.success("200", "Success", streak));
    }

    @Operation(summary = "Patch a streak (currentStreak, longestStreak, lastActiveDate, freezeCount)",
               responses = {@ApiResponse(responseCode = "200", description = "Streak patched"),
                            @ApiResponse(responseCode = "404", description = "Streak not found")})
    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<ResponseBean> patchStreak(@PathVariable Long id, @RequestBody Streak streak) {
        log.info("Patching streak with id: {}", id);
        Streak existing = streakRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Streak", id));

        if (streak.getCurrentStreak() != null) {
            existing.setCurrentStreak(streak.getCurrentStreak());
        }
        if (streak.getLongestStreak() != null) {
            existing.setLongestStreak(streak.getLongestStreak());
        }
        if (streak.getLastActiveDate() != null) {
            existing.setLastActiveDate(streak.getLastActiveDate());
        }
        if (streak.getFreezeCount() != null) {
            existing.setFreezeCount(streak.getFreezeCount());
        }

        Streak saved = streakRepository.save(existing);
        log.info("Patched streak id: {}", id);
        return ResponseEntity.ok(ResponseBean.success("200", "Success", saved));
    }
}
