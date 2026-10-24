package com.stocklms.stock_lms.repository;

import com.stocklms.stock_lms.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Portfolio entity.
 */
@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    // Retrieve a user's portfolio by their user ID
    @Query("SELECT p FROM Portfolio p WHERE p.user.id = :userId")
    Optional<Portfolio> findByUserId(@Param("userId") Long userId);

    // Check if a user already has an active portfolio
    @Query("SELECT COUNT(p) > 0 FROM Portfolio p WHERE p.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
}
