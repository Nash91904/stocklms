package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.StreakPatchRequestDto;
import com.stocklms.stock_lms.dto.StreakResponseDto;
import com.stocklms.stock_lms.entity.Streak;
import com.stocklms.stock_lms.repository.StreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service managing user engagement streaks.
 */
@Service
@RequiredArgsConstructor
public class StreakService {

    private final StreakRepository streakRepository;

    public StreakResponseDto getStreakById(Long id) {
        Streak streak = streakRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Streak not found with id: " + id));
        return toResponseDto(streak);
    }

    public StreakResponseDto getStreakByUserId(Long userId) {
        Streak streak = streakRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Streak not found for user id: " + userId));
        return toResponseDto(streak);
    }

    public StreakResponseDto patchStreak(Long id, StreakPatchRequestDto dto) {
        Streak streak = streakRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Streak not found with id: " + id));

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

        return toResponseDto(streakRepository.save(streak));
    }

    private StreakResponseDto toResponseDto(Streak streak) {
        return StreakResponseDto.builder()
                .id(streak.getId())
                .userId(streak.getUserId())
                .currentStreak(streak.getCurrentStreak())
                .longestStreak(streak.getLongestStreak())
                .lastActiveDate(streak.getLastActiveDate())
                .freezeCount(streak.getFreezeCount())
                .build();
    }
}
