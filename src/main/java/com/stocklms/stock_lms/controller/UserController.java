package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.Portfolio;
import com.stocklms.stock_lms.entity.Stock;
import com.stocklms.stock_lms.entity.Streak;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ConflictException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.*;
import com.stocklms.stock_lms.responsebean.ResponseBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * REST controller for user account management, profile updates, and watchlists.
 * Directly interacts with repositories and returns standard ResponseBean wrappers.
 */
@Slf4j
@Tag(name = "Users", description = "User registration, profile management, watchlist, and account deletion")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final StreakRepository streakRepository;
    private final TradeRepository tradeRepository;
    private final StockRepository stockRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final BadgeRepository badgeRepository;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "Get all users")
    @GetMapping
    public ResponseEntity<ResponseBean> getAllUsers() {
        log.info("Fetching all users");
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(ResponseBean.success("200", "Success", users));
    }

    @Operation(summary = "Get a user by ID",
               responses = {@ApiResponse(responseCode = "404", description = "User not found")})
    @GetMapping("/{id}")
    public ResponseEntity<ResponseBean> getUserById(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return ResponseEntity.ok(ResponseBean.success("200", "Success", user));
    }

    @Operation(summary = "Register a new user (also creates Portfolio and Streak)",
               responses = {@ApiResponse(responseCode = "201", description = "User created"),
                            @ApiResponse(responseCode = "400", description = "Email already in use or validation failure")})
    @PostMapping
    @Transactional
    public ResponseEntity<ResponseBean> createUser(@Valid @RequestBody User user) {
        log.info("Creating user with email: {}", user.getEmail());
        String emailClean = user.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(emailClean)) {
            log.warn("Duplicate email registration rejected: {}", emailClean);
            throw new BusinessRuleException("User with email already exists: " + emailClean);
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            log.warn("Password is required for user registration");
            throw new BusinessRuleException("Password is required");
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());

        User newUser = User.builder()
                .email(emailClean)
                .passwordHash(hashedPassword)
                .riskProfile(user.getRiskProfile() != null ? user.getRiskProfile().trim() : null)
                .literacyScore(user.getLiteracyScore() != null ? user.getLiteracyScore() : 0)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(newUser);

        // Auto-provision starting Portfolio with 100,000 virtual cash
        Portfolio initialPortfolio = Portfolio.builder()
                .user(savedUser)
                .cashBalance(new BigDecimal("100000.00"))
                .build();
        portfolioRepository.save(initialPortfolio);

        // Auto-provision initial Streak tracker with all zeros
        Streak initialStreak = Streak.builder()
                .user(savedUser)
                .currentStreak(0)
                .longestStreak(0)
                .lastActiveDate(null)
                .freezeCount(0)
                .build();
        streakRepository.save(initialStreak);

        log.info("Created user with email: {} (id={})", emailClean, savedUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseBean.success("201", "Success", savedUser));
    }

    @Operation(summary = "Full update of a user profile",
               responses = {@ApiResponse(responseCode = "200", description = "User updated"),
                            @ApiResponse(responseCode = "404", description = "User not found"),
                            @ApiResponse(responseCode = "400", description = "Email already in use or validation failure")})
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<ResponseBean> updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
        log.info("Updating user with id: {}", id);
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        String emailClean = user.getEmail().trim().toLowerCase();
        if (!existing.getEmail().equalsIgnoreCase(emailClean) && userRepository.existsByEmail(emailClean)) {
            log.warn("Email already in use on update: {}", emailClean);
            throw new BusinessRuleException("Email already in use: " + emailClean);
        }

        existing.setEmail(emailClean);
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            existing.setPasswordHash(passwordEncoder.encode(user.getPassword()));
        }
        existing.setRiskProfile(user.getRiskProfile() != null ? user.getRiskProfile().trim() : null);
        existing.setLiteracyScore(user.getLiteracyScore() != null ? user.getLiteracyScore() : 0);

        User saved = userRepository.save(existing);
        log.info("Updated user id: {}", id);
        return ResponseEntity.ok(ResponseBean.success("200", "Success", saved));
    }

    @Operation(summary = "Partial update of user (riskProfile, literacyScore)",
               responses = {@ApiResponse(responseCode = "200", description = "User patched"),
                            @ApiResponse(responseCode = "404", description = "User not found")})
    @PatchMapping("/{id}")
    @Transactional
    public ResponseEntity<ResponseBean> patchUser(@PathVariable Long id, @RequestBody User user) {
        log.info("Patching user with id: {}", id);
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (user.getRiskProfile() != null) {
            existing.setRiskProfile(user.getRiskProfile().trim());
        }
        if (user.getLiteracyScore() != null) {
            if (user.getLiteracyScore() < 0) {
                throw new BusinessRuleException("Literacy score cannot be negative");
            }
            existing.setLiteracyScore(user.getLiteracyScore());
        }

        User saved = userRepository.save(existing);
        log.info("Patched user id: {}", id);
        return ResponseEntity.ok(ResponseBean.success("200", "Success", saved));
    }

    @Operation(summary = "Delete a user and their Portfolio/Streak",
               responses = {@ApiResponse(responseCode = "200", description = "User deleted"),
                            @ApiResponse(responseCode = "404", description = "User not found"),
                            @ApiResponse(responseCode = "409", description = "Cannot delete user with existing trades")})
    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<ResponseBean> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
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
        return ResponseEntity.ok(ResponseBean.success("200", "Success", "User with ID " + id + " deleted successfully."));
    }

    // ── Watchlist Endpoints ──────────────────────────────────────────────────

    @Operation(summary = "Add a stock to user's watchlist",
               responses = {@ApiResponse(responseCode = "201", description = "Stock added to watchlist"),
                            @ApiResponse(responseCode = "404", description = "User or stock not found"),
                            @ApiResponse(responseCode = "409", description = "Stock already in watchlist")})
    @PostMapping("/{userId}/watchlist/{stockId}")
    @Transactional
    public ResponseEntity<ResponseBean> addToWatchlist(@PathVariable Long userId, @PathVariable Long stockId) {
        log.info("Adding stock id {} to watchlist for user id {}", stockId, userId);
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
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseBean.success("201", "Success", stock));
    }

    @Operation(summary = "Remove a stock from user's watchlist",
               responses = {@ApiResponse(responseCode = "200", description = "Stock removed from watchlist"),
                            @ApiResponse(responseCode = "404", description = "User, stock, or watchlist entry not found")})
    @DeleteMapping("/{userId}/watchlist/{stockId}")
    @Transactional
    public ResponseEntity<ResponseBean> removeFromWatchlist(@PathVariable Long userId, @PathVariable Long stockId) {
        log.info("Removing stock id {} from watchlist for user id {}", stockId, userId);
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
        return ResponseEntity.ok(ResponseBean.success("200", "Success", "Stock with ID " + stockId + " removed from watchlist successfully."));
    }

    @Operation(summary = "Get user's watchlist",
               responses = {@ApiResponse(responseCode = "200", description = "Watchlist retrieved"),
                            @ApiResponse(responseCode = "404", description = "User not found")})
    @GetMapping("/{userId}/watchlist")
    public ResponseEntity<ResponseBean> getWatchlist(@PathVariable Long userId) {
        log.info("Fetching watchlist for user id: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<Stock> watchlist = new ArrayList<>(user.getWatchlist());
        return ResponseEntity.ok(ResponseBean.success("200", "Success", watchlist));
    }
}
