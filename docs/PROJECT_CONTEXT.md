# Project Context: Stock Market Trainer & Wealth Growth LMS

## Overview
- **Project Name:** Stock Market Trainer & Wealth Growth LMS
- **Type:** Paper-trading + Financial Learning Management System (Simulated money only, Backend only)
- **Target Audience:** College students and beginner investors
- **Core Goal:** Provide a realistic, risk-free trading sandbox combined with an adaptive learning platform that infers investor risk and literacy behavior algorithmically.

---

## Core Rules & Architecture Guidelines
1. **Beginner-Friendly Code:** Clean, simple, readable code with short explanatory comments.
2. **Security:** No Spring Security or JWT (simplified for educational/PBL project scope; basic authentication/plain or hashed credentials).
3. **Layered Architecture:** Base package `com.stocklms.stock_lms` with standard sub-packages:
   - `controller`: REST endpoints (introduced in later weeks)
   - `service`: Business logic
   - `repository`: Spring Data JPA repositories
   - `entity`: Database entities
   - `dto`: Request and response data transfer objects (never expose or return entities directly from controllers)
   - `exception`: Custom exceptions and global exception handler
   - `config`: Configuration classes (database runners, beans)
4. **DTO Requirement:** Always use DTOs for client requests and responses; never return raw JPA entities to the client.
5. **Data Immutability:** 
   - `Trade` records are immutable (audit trail, no update or delete operations).
   - `PriceHistory` records are immutable (automated market data ingestion log, no update or delete operations).
6. **Relationships (Week 1 vs Week 4):**
   - **Week 1:** Store all foreign keys as plain `Long` fields (`userId`, `stockId`, `lessonId`).
   - **Week 4:** Refactor to formal JPA associations (`@ManyToOne`, `@OneToMany`, `@ManyToMany` Watchlist).

---

## Entities Overview (Primary Key: `Long id` generated)

1. **User**
   - Table: `users` (avoiding SQL keyword `user`)
   - Fields:
     - `Long id` (PK)
     - `String email` (unique, not null)
     - `String passwordHash` (not null)
     - `String riskProfile` (e.g., Conservative, Moderate, Aggressive; default/nullable)
     - `Integer literacyScore` (score inferred from quizzes and trading habits)
     - `LocalDateTime createdAt` (not null)

2. **Stock**
   - Table: `stocks`
   - Fields:
     - `Long id` (PK)
     - `String ticker` (unique, uppercase, e.g. "AAPL", not null)
     - `String name` (company name, not null)
     - `String sector` (e.g., "Technology", "Healthcare")

3. **PriceHistory**
   - Table: `price_history`
   - Immutable audit table populated by automated data ingestion
   - Fields:
     - `Long id` (PK)
     - `Long stockId` (FK reference to Stock)
     - `BigDecimal price` (not null)
     - `Long volume` (trading volume)
     - `LocalDateTime timestamp` (not null)

4. **Portfolio**
   - Table: `portfolios`
   - Fields:
     - `Long id` (PK)
     - `Long userId` (FK reference to User, unique)
     - `BigDecimal cashBalance` (simulated liquid cash, not null, default initial e.g., 100,000.00)

5. **Streak**
   - Table: `streaks`
   - Duolingo-style daily engagement mechanism
   - Fields:
     - `Long id` (PK)
     - `Long userId` (FK reference to User, unique)
     - `Integer currentStreak` (consecutive active days)
     - `Integer longestStreak` (best recorded streak)
     - `LocalDate lastActiveDate` (date of last user activity)
     - `Integer freezeCount` (available streak freezes)

6. **Trade**
   - Table: `trades`
   - Immutable trade ledger
   - Fields:
     - `Long id` (PK)
     - `Long userId` (FK reference to User)
     - `Long stockId` (FK reference to Stock)
     - `TradeType type` (enum: `BUY`, `SELL`, not null)
     - `Integer quantity` (number of shares traded, positive integer)
     - `BigDecimal price` (execution price per share, not null)
     - `LocalDateTime timestamp` (not null)

7. **Lesson**
   - Table: `lessons`
   - Fields:
     - `Long id` (PK)
     - `String title` (not null)
     - `String content` (text content of the module, not null)
     - `String unlockCondition` (criteria to access, e.g., minimum literacy score or prerequisite)

8. **QuizAttempt**
   - Table: `quiz_attempts`
   - Fields:
     - `Long id` (PK)
     - `Long userId` (FK reference to User)
     - `Long lessonId` (FK reference to Lesson)
     - `Integer score` (quiz score achieved)
     - `LocalDateTime timestamp` (not null)

9. **Badge**
   - Table: `badges`
   - Gamification awards
   - Fields:
     - `Long id` (PK)
     - `Long userId` (FK reference to User)
     - `String badgeType` (e.g., "FIRST_TRADE", "DIVERSIFIED", "STREAK_7")
     - `LocalDateTime awardedAt` (not null)

---

## Future Roadmap (Preview)
- **Week 2:** Stock data ingestion service and pricing updates.
- **Week 3:** Paper-trading logic, portfolio valuation, cash balance checks, and validation.
- **Week 4:** True JPA relationships (`@ManyToOne`, `@OneToMany`), Many-to-Many User-Stock Watchlist, and DTO layer.
- **Week 5+:** Adaptive LMS module, streak automation, quizzes, and badges.
