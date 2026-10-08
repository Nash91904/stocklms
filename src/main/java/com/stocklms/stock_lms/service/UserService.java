package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.*;
import com.stocklms.stock_lms.entity.Portfolio;
import com.stocklms.stock_lms.entity.Stock;
import com.stocklms.stock_lms.entity.Streak;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ConflictException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service managing user accounts, password hashing, initial portfolio/streak provisioning,
 * and watchlist operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final StreakRepository streakRepository;
    private final TradeRepository tradeRepository;
    private final StockRepository stockRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final BadgeRepository badgeRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
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
            log.warn("Duplicate email registration rejected: {}", emailClean);
            throw new BusinessRuleException("User with email already exists: " + emailClean);
        }

        // Hash password with BCrypt (never log plain password or hash)
        String hashedPassword = passwordEncoder.encode(dto.getPassword());

        User user = User.builder()
                .email(emailClean)
                .passwordHash(hashedPassword)
                .riskProfile(dto.getRiskProfile() != null ? dto.getRiskProfile().trim() : null)
                .literacyScore(dto.getLiteracyScore() != null ? dto.getLiteracyScore() : 0)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        // Auto-provision starting Portfolio with 100,000 virtual cash (owning side references user)
        Portfolio initialPortfolio = Portfolio.builder()
                .user(savedUser)
                .cashBalance(new BigDecimal("100000.00"))
                .build();
        portfolioRepository.save(initialPortfolio);

        // Auto-provision initial Streak tracker with all zeros (owning side references user)
        Streak initialStreak = Streak.builder()
                .user(savedUser)
                .currentStreak(0)
                .longestStreak(0)
                .lastActiveDate(null)
                .freezeCount(0)
                .build();
        streakRepository.save(initialStreak);

        log.info("Created user with email: {} (id={})", emailClean, savedUser.getId());
        return toResponseDto(savedUser);
    }

    /**
     * Full update (PUT) of a user profile.
     */
    @Transactional
    public UserResponseDto updateUser(Long id, UserUpdateRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        String emailClean = dto.getEmail().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(emailClean) && userRepository.existsByEmail(emailClean)) {
            log.warn("Email already in use on update: {}", emailClean);
            throw new BusinessRuleException("Email already in use: " + emailClean);
        }

        user.setEmail(emailClean);
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        user.setRiskProfile(dto.getRiskProfile() != null ? dto.getRiskProfile().trim() : null);
        user.setLiteracyScore(dto.getLiteracyScore() != null ? dto.getLiteracyScore() : 0);

        User saved = userRepository.save(user);
        log.info("Updated user id: {}", id);
        return toResponseDto(saved);
    }

    /**
     * Partial update (PATCH) of user attributes: riskProfile, literacyScore.
     */
    @Transactional
    public UserResponseDto patchUser(Long id, UserPatchRequestDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (dto.getRiskProfile() != null) {
            user.setRiskProfile(dto.getRiskProfile().trim());
        }
        if (dto.getLiteracyScore() != null) {
            user.setLiteracyScore(dto.getLiteracyScore());
        }

        User saved = userRepository.save(user);
        log.info("Patched user id: {}", id);
        return toResponseDto(saved);
    }

    /**
     * Deletes user and associated data.
     * Rule: If user has trade records in the immutable ledger, 409 Conflict is returned.
     */
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        // Check immutable trade ledger protection
        if (tradeRepository.existsByUserId(id)) {
            log.warn("Cannot delete user id {} with existing trades in immutable ledger", id);
            throw new ConflictException("Cannot delete user with id " + id + " because they have existing trade records in the immutable ledger");
        }

        // Clean up associated child records
        portfolioRepository.findByUserId(id).ifPresent(portfolioRepository::delete);
        streakRepository.findByUserId(id).ifPresent(streakRepository::delete);
        quizAttemptRepository.deleteAll(quizAttemptRepository.findByUserId(id));
        badgeRepository.deleteAll(badgeRepository.findByUserId(id));

        // Clear watchlist associations
        if (user.getWatchlist() != null) {
            user.getWatchlist().clear();
        }

        userRepository.delete(user);
        log.info("Deleted user id: {}", id);
    }

    // ── Watchlist Operations ──────────────────────────────────────────────────

    /**
     * Adds a stock to the user's watchlist.
     */
    @Transactional
    public StockResponseDto addToWatchlist(Long userId, Long stockId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", stockId));

        if (user.getWatchlist().contains(stock)) {
            log.warn("Stock id {} already in watchlist for user id {}", stockId, userId);
            throw new ConflictException("Stock " + stock.getTicker() + " is already in the watchlist of user " + userId);
        }

        user.getWatchlist().add(stock);
        userRepository.save(user);
        log.info("Added stock {} to watchlist for user {}", stock.getTicker(), userId);
        return toStockResponseDto(stock);
    }

    /**
     * Removes a stock from the user's watchlist.
     */
    @Transactional
    public void removeFromWatchlist(Long userId, Long stockId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock", stockId));

        if (!user.getWatchlist().contains(stock)) {
            log.warn("Stock id {} not in watchlist for user id {}", stockId, userId);
            throw new ResourceNotFoundException("Stock with id " + stockId + " is not in the watchlist of user " + userId);
        }

        user.getWatchlist().remove(stock);
        userRepository.save(user);
        log.info("Removed stock {} from watchlist for user {}", stock.getTicker(), userId);
    }

    /**
     * Retrieves all stocks currently in the user's watchlist.
     */
    @Transactional(readOnly = true)
    public List<StockResponseDto> getWatchlist(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return user.getWatchlist().stream()
                .map(this::toStockResponseDto)
                .collect(Collectors.toList());
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

    private StockResponseDto toStockResponseDto(Stock stock) {
        return StockResponseDto.builder()
                .id(stock.getId())
                .ticker(stock.getTicker())
                .name(stock.getName())
                .sector(stock.getSector())
                .build();
    }
}
