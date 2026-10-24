package com.stocklms.stock_lms.repository;

import com.stocklms.stock_lms.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for PriceHistory entity.
 * Rule: PriceHistory is immutable (read & insert only).
 */
@Repository
public interface PriceHistoryRepository extends JpaRepository<PriceHistory, Long> {

    // Retrieve historical price records for a stock ordered chronologically descending
    @Query("SELECT ph FROM PriceHistory ph WHERE ph.stock.id = :stockId ORDER BY ph.timestamp DESC")
    List<PriceHistory> findByStockIdOrderByTimestampDesc(@Param("stockId") Long stockId);

    // Check if any price history records exist for a stock (used to protect immutable ledger)
    @Query("SELECT COUNT(ph) > 0 FROM PriceHistory ph WHERE ph.stock.id = :stockId")
    boolean existsByStockId(@Param("stockId") Long stockId);
}
