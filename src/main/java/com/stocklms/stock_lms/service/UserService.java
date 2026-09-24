package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.UserCreateRequestDto;
import com.stocklms.stock_lms.dto.UserPatchRequestDto;
import com.stocklms.stock_lms.dto.UserResponseDto;
import com.stocklms.stock_lms.dto.UserUpdateRequestDto;
import com.stocklms.stock_lms.entity.Portfolio;
import com.stocklms.stock_lms.entity.Streak;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.repository.PortfolioRepository;
import com.stocklms.stock_lms.repository.StreakRepository;
import com.stocklms.stock_lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing user accounts, password hashing, and initial portfolio/streak provisioning.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final StreakRepository streakRepository;
    private final PasswordEncoder passwordEncoder;

    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));
        return toResponseDto(user);
    }

    /**
     * Creates a new user with hashed password, and automatically initializes
     * their starting Portfolio (cashBalance = 100,000) and Streak (zeros).
     */
    @Transactional
    public UserResponseDto createUser(UserCreateRequestDto dto) {
        String emailClean = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(emailClean)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User with email already exists: " + emailClean);
        }

        // Hash password with BCrypt
        String hashedPassword = passwordEncoder.encode(dto.getPassword());

        User user = User.builder()
                .email(emailClean)
                .passwordHash(hashedPassword)
                .riskProfile(dto.getRiskProfile() != null ? dto.getRiskProfile().trim() : null)
                .literacyScore(dto.getLiteracyScore() != null ? dto.getLiteracyScore() : 0)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        // Automatically provision starting Portfolio with 100,000 virtual cash
        Portfolio initialPortfolio = Portfolio.builder()
                .userId(savedUser.getId())
                .cashBalance(new BigDecimal("100000.00"))
                .build();
        portfolioRepository.save(initialPortfolio);

        // Automatically provision initial Streak tracker with all zeros
        Streak initialStreak = Streak.builder()
                .userId(savedUser.getId())
                .currentStreak(0)
                .longestStreak(0)
                .lastActiveDate(null)
                .freezeCount(0)
                .build();
        streakRepository.save(initialStreak);

        return toResponseDto(savedUser);
    }

    /**
     * Full update (PUT) of a user profile.
     */
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));

        String emailClean = dto.getEmail().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(emailClean) && userRepository.existsByEmail(emailClean)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use: " + emailClean);
        }

        user.setEmail(emailClean);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        user.setRiskProfile(dto.getRiskProfile() != null ? dto.getRiskProfile().trim() : null);
        user.setLiteracyScore(dto.getLiteracyScore() != null ? dto.getLiteracyScore() : 0);

        return toResponseDto(userRepository.save(user));
    }

    /**
     * Partial update (PATCH) of user attributes: riskProfile, literacyScore.
     */
    @Transactional
    public UserResponseDto patchUser(Long id, UserPatchRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id));

        if (dto.getRiskProfile() != null) {
            user.setRiskProfile(dto.getRiskProfile().trim());
        }
        if (dto.getLiteracyScore() != null) {
            user.setLiteracyScore(dto.getLiteracyScore());
        }

        return toResponseDto(userRepository.save(user));
    }

    /**
     * Deletes user and cleans up their associated Portfolio and Streak.
     */
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found with id: " + id);
        }
        portfolioRepository.findByUserId(id).ifPresent(portfolioRepository::delete);
        streakRepository.findByUserId(id).ifPresent(streakRepository::delete);
        userRepository.deleteById(id);
    }

    public UserResponseDto toResponseDto(User user) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .riskProfile(user.getRiskProfile())
                .literacyScore(user.getLiteracyScore())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
