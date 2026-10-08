package com.stocklms.stock_lms.repository;

import com.stocklms.stock_lms.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Trade entity.
 * Rule: Trade is immutable (read & insert only).
 */
@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    // Retrieve full trade history for a user, newest transactions first
    List<Trade> findByUserIdOrderByTimestampDesc(Long userId);

    // Retrieve trades for a specific user and stock
    List<Trade> findByUserIdAndStockIdOrderByTimestampDesc(Long userId, Long stockId);

    // Check if any trades exist for a user (used to protect immutable ledger from cascading deletion)
    boolean existsByUserId(Long userId);

    // Check if any trades exist for a stock (used to protect immutable ledger from cascading deletion)
    boolean existsByStockId(Long stockId);
}
