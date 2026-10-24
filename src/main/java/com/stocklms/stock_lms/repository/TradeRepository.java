package com.stocklms.stock_lms.repository;

import com.stocklms.stock_lms.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Trade entity.
 * Rule: Trade is immutable (read & insert only).
 */
@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {

    // Retrieve full trade history for a user, newest transactions first
    @Query("SELECT t FROM Trade t WHERE t.user.id = :userId ORDER BY t.timestamp DESC")
    List<Trade> findByUserIdOrderByTimestampDesc(@Param("userId") Long userId);

    // Retrieve trades for a specific user and stock
    @Query("SELECT t FROM Trade t WHERE t.user.id = :userId AND t.stock.id = :stockId ORDER BY t.timestamp DESC")
    List<Trade> findByUserIdAndStockIdOrderByTimestampDesc(@Param("userId") Long userId, @Param("stockId") Long stockId);

    // Check if any trades exist for a user (used to protect immutable ledger from cascading deletion)
    @Query("SELECT COUNT(t) > 0 FROM Trade t WHERE t.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);

    // Check if any trades exist for a stock (used to protect immutable ledger from cascading deletion)
    @Query("SELECT COUNT(t) > 0 FROM Trade t WHERE t.stock.id = :stockId")
    boolean existsByStockId(@Param("stockId") Long stockId);
}
