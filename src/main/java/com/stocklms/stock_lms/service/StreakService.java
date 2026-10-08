package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.StreakPatchRequestDto;
import com.stocklms.stock_lms.dto.StreakResponseDto;
import com.stocklms.stock_lms.entity.Streak;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.StreakRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service managing user engagement streaks.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StreakService {

    private final StreakRepository streakRepository;

    @Transactional(readOnly = true)
    public StreakResponseDto getStreakById(Long id) {
        log.info("Fetching streak with id: {}", id);
        Streak streak = streakRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Streak", id));
        return toResponseDto(streak);
    }

    @Transactional(readOnly = true)
    public StreakResponseDto getStreakByUserId(Long userId) {
        log.info("Fetching streak for user id: {}", userId);
        Streak streak = streakRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Streak not found for user id: " + userId));
        return toResponseDto(streak);
    }

    @Transactional
    public StreakResponseDto patchStreak(Long id, StreakPatchRequestDto dto) {
        Streak streak = streakRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Streak", id));

        if (dto.getCurrentStreak() != null) {
            streak.setCurrentStreak(dto.getCurrentStreak());
        }
        if (dto.getLongestStreak() != null) {
            streak.setLongestStreak(dto.getLongestStreak());
        }
        if (dto.getLastActiveDate() != null) {
            streak.setLastActiveDate(dto.getLastActiveDate());
        }
        if (dto.getFreezeCount() != null) {
            streak.setFreezeCount(dto.getFreezeCount());
        }

        Streak saved = streakRepository.save(streak);
        log.info("Patched streak id: {}", id);
        return toResponseDto(saved);
    }

    private StreakResponseDto toResponseDto(Streak streak) {
        return StreakResponseDto.builder()
                .id(streak.getId())
                .userId(streak.getUser() != null ? streak.getUser().getId() : null)
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .lastActiveDate(streak.getLastActiveDate())
                .freezeCount(streak.getFreezeCount())
                .build();
    }
}
