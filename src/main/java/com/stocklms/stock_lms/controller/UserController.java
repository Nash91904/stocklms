package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.StockResponseDto;
import com.stocklms.stock_lms.dto.UserCreateRequestDto;
import com.stocklms.stock_lms.dto.UserPatchRequestDto;
import com.stocklms.stock_lms.dto.UserResponseDto;
import com.stocklms.stock_lms.dto.UserUpdateRequestDto;
import com.stocklms.stock_lms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user account management, profile updates, and watchlists.
 */
@Tag(name = "Users", description = "User registration, profile management, watchlist, and account deletion")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get all users")
    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Get a user by ID",
               responses = {@ApiResponse(responseCode = "404", description = "User not found")})
    @GetMapping("/{id}")
    public UserResponseDto getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "Register a new user (also creates Portfolio and Streak)",
               responses = {@ApiResponse(responseCode = "201", description = "User created"),
                            @ApiResponse(responseCode = "400", description = "Email already in use")})
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto createUser(@Valid @RequestBody UserCreateRequestDto dto) {
        return userService.createUser(dto);
    }

    @Operation(summary = "Full update of a user profile",
               responses = {@ApiResponse(responseCode = "404", description = "User not found")})
    @PutMapping("/{id}")
    public UserResponseDto updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequestDto dto) {
        return userService.updateUser(id, dto);
    }

    @Operation(summary = "Partial update of user (riskProfile, literacyScore)",
               responses = {@ApiResponse(responseCode = "404", description = "User not found")})
    @PatchMapping("/{id}")
    public UserResponseDto patchUser(@PathVariable Long id, @Valid @RequestBody UserPatchRequestDto dto) {
        return userService.patchUser(id, dto);
    }

    @Operation(summary = "Delete a user and their Portfolio/Streak",
               responses = {@ApiResponse(responseCode = "204", description = "Deleted"),
                            @ApiResponse(responseCode = "404", description = "User not found"),
                            @ApiResponse(responseCode = "409", description = "Cannot delete user with existing trades")})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    // ── Watchlist Endpoints ──────────────────────────────────────────────────

    @Operation(summary = "Add a stock to user's watchlist",
               responses = {@ApiResponse(responseCode = "201", description = "Stock added to watchlist"),
                            @ApiResponse(responseCode = "404", description = "User or stock not found"),
                            @ApiResponse(responseCode = "409", description = "Stock already in watchlist")})
    @PostMapping("/{userId}/watchlist/{stockId}")
    @ResponseStatus(HttpStatus.CREATED)
    public StockResponseDto addToWatchlist(@PathVariable Long userId, @PathVariable Long stockId) {
        return userService.addToWatchlist(userId, stockId);
    }

    @Operation(summary = "Remove a stock from user's watchlist",
               responses = {@ApiResponse(responseCode = "204", description = "Stock removed from watchlist"),
                            @ApiResponse(responseCode = "404", description = "User, stock, or watchlist entry not found")})
    @DeleteMapping("/{userId}/watchlist/{stockId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeFromWatchlist(@PathVariable Long userId, @PathVariable Long stockId) {
        userService.removeFromWatchlist(userId, stockId);
    }

    @Operation(summary = "Get user's watchlist",
               responses = {@ApiResponse(responseCode = "200", description = "Watchlist retrieved"),
                            @ApiResponse(responseCode = "404", description = "User not found")})
    @GetMapping("/{userId}/watchlist")
    public List<StockResponseDto> getWatchlist(@PathVariable Long userId) {
        return userService.getWatchlist(userId);
    }
}
