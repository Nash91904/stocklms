package com.stocklms.stock_lms.controller;

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
 * REST controller for user account management and profile updates.
 */
@Tag(name = "Users", description = "User registration, profile management, and account deletion")
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
    public UserResponseDto patchUser(@PathVariable Long id, @RequestBody UserPatchRequestDto dto) {
        return userService.patchUser(id, dto);
    }

    @Operation(summary = "Delete a user and their Portfolio/Streak",
               responses = {@ApiResponse(responseCode = "204", description = "Deleted"),
                            @ApiResponse(responseCode = "404", description = "User not found")})
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
