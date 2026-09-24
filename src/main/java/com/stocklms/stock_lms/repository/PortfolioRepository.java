package com.stocklms.stock_lms.repository;

import com.stocklms.stock_lms.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Portfolio entity.
 */
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    // Retrieve a user's portfolio by their user ID
    Optional<Portfolio> findByUserId(Long userId);

    // Check if a user already has an active portfolio
    boolean existsByUserId(Long userId);
}
