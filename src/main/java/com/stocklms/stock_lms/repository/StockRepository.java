package com.stocklms.stock_lms.repository;

import com.stocklms.stock_lms.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for Stock entity.
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {

    // Find a stock by ticker symbol (e.g., AAPL)
    Optional<Stock> findByTicker(String ticker);

    // Check if a ticker symbol already exists
    boolean existsByTicker(String ticker);
}
