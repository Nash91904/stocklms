package com.stocklms.stock_lms.service;

import com.stocklms.stock_lms.dto.UserCreateRequestDto;
import com.stocklms.stock_lms.dto.UserPatchRequestDto;
import com.stocklms.stock_lms.dto.UserResponseDto;
import com.stocklms.stock_lms.dto.UserUpdateRequestDto;
import com.stocklms.stock_lms.entity.Portfolio;
import com.stocklms.stock_lms.entity.Streak;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.PortfolioRepository;
import com.stocklms.stock_lms.repository.StreakRepository;
import com.stocklms.stock_lms.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PortfolioRepository portfolioRepository;

    @Mock
    private StreakRepository streakRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("trader@example.com")
                .passwordHash("hashedPassword")
                .riskProfile("MODERATE")
                .literacyScore(80)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllUsers_success() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponseDto> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals("trader@example.com", users.get(0).getEmail());
    }

    @Test
    void getUserById_success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDto response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("trader@example.com", response.getEmail());
    }

    @Test
    void getUserById_notFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(99L));
    }

    @Test
    void createUser_success() {
        UserCreateRequestDto request = UserCreateRequestDto.builder()
                .email("newuser@example.com")
                .password("secret123")
                .riskProfile("CONSERVATIVE")
                .literacyScore(60)
                .build();

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(2L);
            return u;
        });

        UserResponseDto response = userService.createUser(request);

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("CONSERVATIVE", response.getRiskProfile());
        assertEquals(60, response.getLiteracyScore());

        verify(portfolioRepository).save(any(Portfolio.class));
        verify(streakRepository).save(any(Streak.class));
    }

    @Test
    void createUser_duplicateEmail_throwsBusinessRuleException() {
        UserCreateRequestDto request = UserCreateRequestDto.builder()
                .email("trader@example.com")
                .password("secret123")
                .build();

        when(userRepository.existsByEmail("trader@example.com")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> userService.createUser(request));
        assertTrue(ex.getMessage().contains("User with email already exists"));
        verify(userRepository, never()).save(any());
        verify(portfolioRepository, never()).save(any());
        verify(streakRepository, never()).save(any());
    }

    @Test
    void updateUser_success() {
        UserUpdateRequestDto request = UserUpdateRequestDto.builder()
                .email("updated@example.com")
                .password("newSecret")
                .riskProfile("AGGRESSIVE")
                .literacyScore(95)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("updated@example.com")).thenReturn(false);
        when(passwordEncoder.encode("newSecret")).thenReturn("newHashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto response = userService.updateUser(1L, request);

        assertNotNull(response);
        assertEquals("updated@example.com", response.getEmail());
        assertEquals("AGGRESSIVE", response.getRiskProfile());
        assertEquals(95, response.getLiteracyScore());
    }

    @Test
    void updateUser_notFound_throwsResourceNotFoundException() {
        UserUpdateRequestDto request = UserUpdateRequestDto.builder()
                .email("updated@example.com")
                .build();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(99L, request));
    }

    @Test
    void updateUser_duplicateEmail_throwsBusinessRuleException() {
        UserUpdateRequestDto request = UserUpdateRequestDto.builder()
                .email("another@example.com")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("another@example.com")).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> userService.updateUser(1L, request));
        assertTrue(ex.getMessage().contains("Email already in use"));
    }

    @Test
    void patchUser_success() {
        UserPatchRequestDto request = UserPatchRequestDto.builder()
                .riskProfile("AGGRESSIVE")
                .literacyScore(90)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserResponseDto response = userService.patchUser(1L, request);

        assertNotNull(response);
        assertEquals("AGGRESSIVE", response.getRiskProfile());
        assertEquals(90, response.getLiteracyScore());
    }

    @Test
    void patchUser_notFound_throwsResourceNotFoundException() {
        UserPatchRequestDto request = UserPatchRequestDto.builder()
                .riskProfile("AGGRESSIVE")
                .build();

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.patchUser(99L, request));
    }

    @Test
    void deleteUser_success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(portfolioRepository.findByUserId(1L)).thenReturn(Optional.of(Portfolio.builder().id(10L).build()));
        when(streakRepository.findByUserId(1L)).thenReturn(Optional.of(Streak.builder().id(20L).build()));

        userService.deleteUser(1L);

        verify(portfolioRepository).delete(any(Portfolio.class));
        verify(streakRepository).delete(any(Streak.class));
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_notFound_throwsResourceNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(99L));
        verify(userRepository, never()).deleteById(any());
    }
}
