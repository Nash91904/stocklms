package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.BadgeRequestDto;
import com.stocklms.stock_lms.dto.BadgeResponseDto;
import com.stocklms.stock_lms.entity.Badge;
import com.stocklms.stock_lms.repository.BadgeRepository;
import com.stocklms.stock_lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing user achievement badges.
 */
@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;

    public BadgeResponseDto awardBadge(BadgeRequestDto dto) {
        if (!userRepository.existsById(dto.getUserId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + dto.getUserId());
        }

        Badge badge = Badge.builder()
                .userId(dto.getUserId())
                .badgeType(dto.getBadgeType().trim().toUpperCase())
                .awardedAt(LocalDateTime.now())
                .build();

        return toResponseDto(badgeRepository.save(badge));
    }

    public List<BadgeResponseDto> getBadgesByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + userId);
        }

        return badgeRepository.findByUserId(userId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private BadgeResponseDto toResponseDto(Badge badge) {
        return BadgeResponseDto.builder()
                .id(badge.getId())
                .userId(badge.getUserId())
                .badgeType(badge.getBadgeType())
                .awardedAt(badge.getAwardedAt())
                .build();
    }
}
