package com.stocklms.stock_lms.controller;

import com.stocklms.stock_lms.entity.Badge;
import com.stocklms.stock_lms.entity.User;
import com.stocklms.stock_lms.exception.BusinessRuleException;
import com.stocklms.stock_lms.exception.ResourceNotFoundException;
import com.stocklms.stock_lms.repository.BadgeRepository;
import com.stocklms.stock_lms.repository.UserRepository;
import com.stocklms.stock_lms.responsebean.ResponseBean;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for awarding and viewing user badges.
 * Directly interacts with repositories and returns standard ResponseBean wrappers.
 */
@Slf4j
@Tag(name = "Badges", description = "Award and view user achievement badges")
@RestController
@RequestMapping("/api/badges")
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;

    @Operation(summary = "Award a badge to a user",
               requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @io.swagger.v3.oas.annotations.media.Content(
                       mediaType = "application/json",
                       examples = @io.swagger.v3.oas.annotations.media.ExampleObject(value = """
                               {"user": {"id": 0}, "badgeType": "string"}
                               """)
               )),
               responses = {@ApiResponse(responseCode = "201", description = "Badge awarded"),
                            @ApiResponse(responseCode = "404", description = "User not found")})
    @PostMapping
    @Transactional
    public ResponseEntity<ResponseBean> awardBadge(@Valid @RequestBody Badge badge) {
        Long userId = badge.getUser() != null ? badge.getUser().getId() : null;
        if (userId == null) {
            log.warn("Award badge rejected: User ID is required");
            throw new BusinessRuleException("User ID is required");
        }

        log.info("Awarding badge '{}' to user id: {}", badge.getBadgeType(), userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        badge.setUser(user);
        badge.setBadgeType(badge.getBadgeType().trim().toUpperCase());
        if (badge.getAwardedAt() == null) {
            badge.setAwardedAt(LocalDateTime.now());
        }

        Badge saved = badgeRepository.save(badge);
        log.info("Awarded badge '{}' successfully to user id: {}", saved.getBadgeType(), userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ResponseBean.success("201", "Success", saved));
    }

    @Operation(summary = "Get badges by user ID path",
               responses = {@ApiResponse(responseCode = "404", description = "User not found")})
    @GetMapping("/user/{userId}")
    public ResponseEntity<ResponseBean> getBadgesByUserPath(@PathVariable Long userId) {
        log.info("Fetching badges for user id: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", userId);
        }

        List<Badge> badges = badgeRepository.findByUserId(userId);
        return ResponseEntity.ok(ResponseBean.success("200", "Success", badges));
    }

    @Operation(summary = "Get badges by user ID query parameter",
               responses = {@ApiResponse(responseCode = "400", description = "Parameter 'userId' is required"),
                            @ApiResponse(responseCode = "404", description = "User not found")})
    @GetMapping
    public ResponseEntity<ResponseBean> getBadgesByUserQuery(@RequestParam(required = false) Long userId) {
        if (userId != null) {
            return getBadgesByUserPath(userId);
        }
        log.warn("Querying badges rejected: Parameter 'userId' is required");
        throw new BusinessRuleException("Parameter 'userId' is required");
    }
    @Operation(summary = "Delete a badge by ID",
           responses = {@ApiResponse(responseCode = "200", description = "Badge deleted"),
                        @ApiResponse(responseCode = "404", description = "Badge not found")})
@DeleteMapping("/{id}")
@Transactional
public ResponseEntity<ResponseBean> deleteBadge(@PathVariable Long id) {
    log.info("Deleting badge with id: {}", id);
    Badge badge = badgeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Badge", id));
    badgeRepository.delete(badge);
    log.info("Deleted badge id: {}", id);
    return ResponseEntity.ok(ResponseBean.success("200", "Success",
            "Badge with ID " + id + " deleted successfully."));
}
}
