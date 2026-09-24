package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.dto.UserCreateRequestDto;
import com.stocklms.stock_lms.dto.UserPatchRequestDto;
import com.stocklms.stock_lms.dto.UserResponseDto;
import com.stocklms.stock_lms.dto.UserUpdateRequestDto;
import com.stocklms.stock_lms.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for user account management and profile updates.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponseDto> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public UserResponseDto getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto createUser(@Valid @RequestBody UserCreateRequestDto dto) {
        return userService.createUser(dto);
    }

    @PutMapping("/{id}")
    public UserResponseDto updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequestDto dto) {
        return userService.updateUser(id, dto);
    }

    @PatchMapping("/{id}")
    public UserResponseDto patchUser(@PathVariable Long id, @RequestBody UserPatchRequestDto dto) {
        return userService.patchUser(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
