package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.BadgeRequestDto;
import com.stocklms.stock_lms.dto.BadgeResponseDto;
import com.stocklms.stock_lms.entity.Badge;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.BadgeRepository;
import com.stocklms.stock_lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing user achievement badges.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;

    @Transactional
    public BadgeResponseDto awardBadge(BadgeRequestDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", dto.getUserId()));

        Badge badge = Badge.builder()
                .user(user)
                .badgeType(dto.getBadgeType().trim().toUpperCase())
                .awardedAt(LocalDateTime.now())
                .build();

        Badge saved = badgeRepository.save(badge);
        log.info("Awarded badge '{}' to user id: {}", saved.getBadgeType(), dto.getUserId());
        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<BadgeResponseDto> getBadgesByUser(Long userId) {
        log.info("Fetching badges for user id: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        return badgeRepository.findByUserId(userId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private BadgeResponseDto toResponseDto(Badge badge) {
        return BadgeResponseDto.builder()
                .id(badge.getId())
                .userId(badge.getUser() != null ? badge.getUser().getId() : null)
                .badgeType(badge.getBadgeType())
                .awardedAt(badge.getAwardedAt())
                .build();
    }
}
