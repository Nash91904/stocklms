package com.stocklms.stock_lms.config;

import com.stocklms.stock_lms.entity.*;
import com.stocklms.stock_lms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Startup seeder that populates initial data ONLY IF database tables are empty.
 * Seeds ~10 US stocks with initial prices, sample lessons, and a demo user with portfolio.
 */
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final StockRepository stockRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final LessonRepository lessonRepository;
    private final UserRepository userRepository;
    private final PortfolioRepository portfolioRepository;
    private final StreakRepository streakRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        seedStocksAndPricesIfEmpty();
        seedLessonsIfEmpty();
        seedDemoUserIfEmpty();
    }

    private void seedStocksAndPricesIfEmpty() {
        if (stockRepository.count() > 0) {
            log.info("Stocks already seeded (count = {}). Skipping stock seeding.", stockRepository.count());
            return;
        }

        log.info("Seeding initial 10 US stocks with baseline price snapshots...");

        record SeedStock(String ticker, String name, String sector, BigDecimal price, Long volume) {}

        List<SeedStock> seedList = List.of(
                new SeedStock("AAPL", "Apple Inc.", "Technology", new BigDecimal("182.50"), 48000000L),
                new SeedStock("MSFT", "Microsoft Corporation", "Technology", new BigDecimal("425.20"), 22000000L),
                new SeedStock("GOOGL", "Alphabet Inc.", "Technology", new BigDecimal("175.80"), 19000000L),
                new SeedStock("AMZN", "Amazon.com Inc.", "Consumer Cyclical", new BigDecimal("185.60"), 32000000L),
                new SeedStock("TSLA", "Tesla Inc.", "Consumer Cyclical", new BigDecimal("240.30"), 59000000L),
                new SeedStock("NVDA", "NVIDIA Corporation", "Technology", new BigDecimal("125.40"), 74000000L),
                new SeedStock("JPM", "JPMorgan Chase & Co.", "Financial Services", new BigDecimal("198.70"), 9600000L),
                new SeedStock("V", "Visa Inc.", "Financial Services", new BigDecimal("275.90"), 6400000L),
                new SeedStock("JNJ", "Johnson & Johnson", "Healthcare", new BigDecimal("156.40"), 7500000L),
                new SeedStock("WMT", "Walmart Inc.", "Consumer Defensive", new BigDecimal("68.90"), 14500000L)
        );

        LocalDateTime now = LocalDateTime.now();

        for (SeedStock s : seedList) {
            Stock stock = stockRepository.save(Stock.builder()
                    .ticker(s.ticker())
                    .name(s.name())
                    .sector(s.sector())
                    .build());

            priceHistoryRepository.save(PriceHistory.builder()
                    .stockId(stock.getId())
                    .price(s.price())
                    .volume(s.volume())
                    .timestamp(now)
                    .build());
        }

        log.info("Successfully seeded 10 stocks and baseline price history snapshots.");
    }

    private void seedLessonsIfEmpty() {
        if (lessonRepository.count() > 0) {
            log.info("Lessons already seeded (count = {}). Skipping lesson seeding.", lessonRepository.count());
            return;
        }

        log.info("Seeding initial LMS lessons...");

        List<Lesson> lessons = List.of(
                Lesson.builder()
                        .title("Stock Market Fundamentals: What is a Share?")
                        .content("A share represents fractional ownership in a corporation. When you purchase a share of stock, you become a shareholder, entitling you to a portion of the company's profits and assets. Stock prices fluctuate daily based on market supply and demand, company earnings, and macroeconomic conditions.")
                        .unlockCondition(null)
                        .build(),
                Lesson.builder()
                        .title("The Golden Rule: Portfolio Diversification")
                        .content("Diversification is a risk management strategy that mixes a wide variety of investments within a portfolio. A diversified portfolio contains a mix of distinct asset types and investment vehicles in order to limit exposure to any single asset or risk factor. Remember: Never put all your eggs in one basket!")
                        .unlockCondition("MIN_LITERACY_20")
                        .build(),
                Lesson.builder()
                        .title("Understanding Risk Profiles: Conservative vs Aggressive")
                        .content("Your risk profile describes how much volatility you can stomach before panic selling. Conservative investors prioritize capital preservation with dividend-paying blue-chips. Aggressive investors seek high capital appreciation by investing in volatile growth stocks.")
                        .unlockCondition("MIN_LITERACY_40")
                        .build()
        );

        lessonRepository.saveAll(lessons);
        log.info("Successfully seeded 3 educational lessons.");
    }

    private void seedDemoUserIfEmpty() {
        if (userRepository.count() > 0) {
            log.info("Users already seeded (count = {}). Skipping demo user seeding.", userRepository.count());
            return;
        }

        log.info("Seeding demo user with default portfolio and streak...");

        User demoUser = User.builder()
                .email("demo@stocklms.com")
                .passwordHash(passwordEncoder.encode("password123"))
                .riskProfile("MODERATE")
                .literacyScore(50)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(demoUser);

        // Provision initial starting portfolio with 100,000 cash balance
        Portfolio demoPortfolio = Portfolio.builder()
                .userId(savedUser.getId())
                .cashBalance(new BigDecimal("100000.00"))
                .build();
        portfolioRepository.save(demoPortfolio);

        // Provision initial streak tracker
        Streak demoStreak = Streak.builder()
                .userId(savedUser.getId())
                .currentStreak(0)
                .longestStreak(0)
                .lastActiveDate(null)
                .freezeCount(0)
                .build();
        streakRepository.save(demoStreak);

        log.info("Successfully seeded demo user (id={}) with $100,000 portfolio and initial streak.", savedUser.getId());
    }
}
