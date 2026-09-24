package com.stocklms.stock_lms.repository;

import com.stocklms.stock_lms.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for PriceHistory entity.
 * Rule: PriceHistory is immutable (read & insert only).
 */
@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    // Retrieve historical price records for a stock ordered chronologically descending
    List<PriceHistory> findByStockIdOrderByTimestampDesc(Long stockId);
}
