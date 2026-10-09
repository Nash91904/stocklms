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
   - `controller`: REST endpoints
   - `service`: Business logic with `@Transactional` boundaries
   - `repository`: Spring Data JPA repositories with relationship traversal
   - `entity`: Database entities with JPA relationships and lazy loading
   - `dto`: Request and response data transfer objects (never expose or return entities directly from controllers)
   - `exception`: Custom exceptions (`ConflictException`, `BusinessRuleException`, `ResourceNotFoundException`) and global exception handler
   - `config`: Configuration classes (database runners, beans, seeders)
4. **DTO Requirement:** Always use DTOs for client requests and responses; never return raw JPA entities to the client. Keep client IDs (`userId`, `stockId`, `lessonId`) as plain `Long` fields mapped to entities in services.
5. **Data Immutability:** 
   - `Trade` records are immutable (audit trail, no update or delete operations).
   - `PriceHistory` records are immutable (automated market data ingestion log, no update or delete operations).
6. **Relationships:**
   - Formal JPA associations (`@OneToOne`, `@ManyToOne`, `@OneToMany`, `@ManyToMany` Watchlist) with `FetchType.LAZY` on all singular associations.
   - `@ToString.Exclude` and `@EqualsAndHashCode.Exclude` on collections and associations to avoid recursion.

---

## Entity Mappings Summary (Week 4)

| Entity Pair | JPA Annotation | Owning Side | Join Column / Table | Cascade & Orphan Removal |
|---|---|---|---|---|
| **User <-> Portfolio** | `@OneToOne` | `Portfolio` | `user_id` (unique) | `User`: `cascade = ALL`, `orphanRemoval = true` |
| **User <-> Streak** | `@OneToOne` | `Streak` | `user_id` (unique) | `User`: `cascade = ALL`, `orphanRemoval = true` |
| **Trade -> User** | `@ManyToOne` / `@OneToMany` | `Trade` | `user_id` | Inverse `User.trades` |
| **Trade -> Stock** | `@ManyToOne` / `@OneToMany` | `Trade` | `stock_id` | Inverse `Stock.trades` |
| **PriceHistory -> Stock** | `@ManyToOne` / `@OneToMany` | `PriceHistory` | `stock_id` | Inverse `Stock.priceHistory` |
| **QuizAttempt -> User** | `@ManyToOne` / `@OneToMany` | `QuizAttempt` | `user_id` | Inverse `User.quizAttempts` |
| **QuizAttempt -> Lesson** | `@ManyToOne` / `@OneToMany` | `QuizAttempt` | `lesson_id` | Inverse `Lesson.quizAttempts` |
| **Badge -> User** | `@ManyToOne` / `@OneToMany` | `Badge` | `user_id` | Inverse `User.badges` |
| **User <-> Stock (Watchlist)** | `@ManyToMany` | `User` | Join Table `user_watchlist` (`user_id`, `stock_id`) | Inverse `Stock.usersWatching` (`mappedBy = "watchlist"`) |

*All `@ManyToOne` and `@OneToOne` associations use `fetch = FetchType.LAZY`.*

---

## Deletion and Conflict Rules (HTTP 409)

1. **User Deletion:**
   - Deleting a user cleans up linked `portfolio`, `streak`, `quiz_attempts`, `badges`, and clears their `user_watchlist` entries.
   - **Conflict Protection:** If the user has trade records in the immutable `trades` ledger, deletion throws `ConflictException` (mapped to HTTP `409 Conflict`).
2. **Stock Deletion:**
   - Deleting a stock clears watchlist references from watching users.
   - **Conflict Protection:** If the stock is referenced in immutable `trades` or `price_history`, deletion throws `ConflictException` (mapped to HTTP `409 Conflict`).

---

## Entities Overview (Primary Key: `Long id` generated)

1. **User** (`users`)
   - `id`, `email`, `passwordHash`, `riskProfile`, `literacyScore`, `createdAt`
   - `@OneToOne` `portfolio`, `streak`
   - `@OneToMany` `trades`, `quizAttempts`, `badges`
   - `@ManyToMany` `watchlist` (`Set<Stock>`)

2. **Stock** (`stocks`)
   - `id`, `ticker`, `name`, `sector`
   - `@OneToMany` `priceHistory`, `trades`
   - `@ManyToMany(mappedBy = "watchlist")` `usersWatching` (`Set<User>`)

3. **PriceHistory** (`price_history` - immutable)
   - `id`, `stock` (`@ManyToOne`), `price`, `volume`, `timestamp`

4. **Portfolio** (`portfolios`)
   - `id`, `user` (`@OneToOne`, unique `user_id`), `cashBalance`

5. **Streak** (`streaks`)
   - `id`, `user` (`@OneToOne`, unique `user_id`), `currentStreak`, `longestStreak`, `lastActiveDate`, `freezeCount`

6. **Trade** (`trades` - immutable)
   - `id`, `user` (`@ManyToOne`), `stock` (`@ManyToOne`), `type` (`BUY`/`SELL`), `quantity`, `price`, `timestamp`

7. **Lesson** (`lessons`)
   - `id`, `title`, `content`, `unlockCondition`
   - `@OneToMany` `quizAttempts`

8. **QuizAttempt** (`quiz_attempts`)
   - `id`, `user` (`@ManyToOne`), `lesson` (`@ManyToOne`), `score`, `timestamp`

9. **Badge** (`badges`)
   - `id`, `user` (`@ManyToOne`), `badgeType`, `awardedAt`

---

## Roadmap & Status
- **Week 1 (Done):** Initial project structure, PostgreSQL database connection, 9 entities with Long IDs, DataSeeder.
- **Week 2 (Done):** REST APIs for all entities, DTOs, CRUD operations, global exception handler.
- **Week 3 (Done):** Paper-trading logic, portfolio valuation, cash balance validations, comprehensive tests, Swagger OpenAPI docs.
- **Week 4 (Done):** True JPA entity mappings (`@OneToOne`, `@ManyToOne`, `@OneToMany`, `@ManyToMany` Watchlist), immutable ledger delete conflict handling (409 Conflict), Watchlist endpoints, comprehensive mapping tests (86 tests passing), production JAR build.
- **Week 5+:** Adaptive LMS module, streak automation, quizzes, and badges.
