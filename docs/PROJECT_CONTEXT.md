# Project Context: Stock Market Trainer & Wealth Growth LMS

## Overview
- **Project Name:** Stock Market Trainer & Wealth Growth LMS
- **Type:** Paper-trading + Financial Learning Management System (Simulated money only, Backend only)
- **Target Audience:** College students and beginner investors
- **Core Goal:** Provide a realistic, risk-free trading sandbox combined with an adaptive learning platform that infers investor risk and literacy behavior algorithmically.

---

## Architecture & Design (Teacher's Sample Structure)
1. **Simplified 3-Tier Layered Architecture:**
   Final layers: `controller -> repository -> entity`, plus `exception`, `config`, and `responsebean`.
   The `service/` and `dto/` packages do not exist.
2. **Direct Repository Access & Transactional Boundaries:**
   Controllers use repositories directly via constructor injection (`@RequiredArgsConstructor`).
   Multi-table writes and read-then-write workflows (e.g. trade execution, user create/delete, watchlist management, stock deletion) use `@Transactional` directly on the controller methods.
3. **Entities as Request/Response Models:**
   JPA entities are used directly as `@RequestBody` payloads and returned inside `ResponseBean.data`.
   Bean Validation annotations (`@NotBlank`, `@Size`, `@NotNull`, etc.) reside directly on entity fields with `@Valid` on controller methods.
4. **JSON Safety (`spring.jpa.open-in-view=false`):**
   - No infinite recursion and no `LazyInitializationException` during serialization.
   - `@JsonIgnore` is placed on all collection associations and inverse sides (`User.trades`, `quizAttempts`, `badges`, `watchlist`, `portfolio`, `streak`; `Stock.trades`, `priceHistory`, `usersWatching`; `Lesson.quizAttempts`).
   - Entities with `@ManyToOne` or `@OneToOne` associations to parents (`Trade`, `Portfolio`, `Streak`, `PriceHistory`, `QuizAttempt`, `Badge`) have `@JsonProperty(access = WRITE_ONLY)` on the relationship field and expose flat read-only getters (`userId`, `stockId`, `lessonId`).
   - `passwordHash` is annotated with `@JsonIgnore` and never serialized. User registration uses a `@Transient` write-only `password` field that is hashed by the controller using BCrypt.
5. **Standardized ResponseBean Envelope:**
   All endpoints return `ResponseEntity<ResponseBean>` with:
   - `errorCode`: String HTTP status code (`"200"`, `"201"`, `"400"`, `"404"`, `"409"`, `"500"`)
   - `errorDesc`: String message (`"Success"`, validation message, or error detail)
   - `data`: Entity payload, list of entities, error details, or confirmation message string
   - DELETE endpoints return HTTP 200 with confirmation message in `data`.
6. **Data Immutability:**
   - `Trade` records are immutable (audit trail, no update or delete endpoints).
   - `PriceHistory` records are immutable (market price snapshot log, no update or delete endpoints).
7. **Deletion and Conflict Protection (HTTP 409):**
   - **User Deletion:** Rejected with 409 Conflict if the user has executed trades in the immutable ledger. Otherwise, cleans up portfolio, streak, quiz attempts, badges, and watchlist links.
   - **Stock Deletion:** Rejected with 409 Conflict if referenced by immutable trade or price history records. Cleans up watchlist references before deletion.

---

## Entity Mappings Summary

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

## Entities Overview (Primary Key: `Long id` generated)

1. **User** (`users`)
   - `id`, `email`, `passwordHash` (hidden), `password` (`@Transient` write-only), `riskProfile`, `literacyScore`, `createdAt`
   - `@OneToOne` `portfolio`, `streak`
   - `@OneToMany` `trades`, `quizAttempts`, `badges`
   - `@ManyToMany` `watchlist` (`Set<Stock>`)

2. **Stock** (`stocks`)
   - `id`, `ticker`, `name`, `sector`
   - `@OneToMany` `priceHistory`, `trades`
   - `@ManyToMany(mappedBy = "watchlist")` `usersWatching` (`Set<User>`)

3. **PriceHistory** (`price_history` - immutable)
   - `id`, `stock` (`@ManyToOne` write-only), `stockId` (read-only getter), `price`, `volume`, `timestamp`

4. **Portfolio** (`portfolios`)
   - `id`, `user` (`@OneToOne` write-only), `userId` (read-only getter), `cashBalance`

5. **Streak** (`streaks`)
   - `id`, `user` (`@OneToOne` write-only), `userId` (read-only getter), `currentStreak`, `longestStreak`, `lastActiveDate`, `freezeCount`

6. **Trade** (`trades` - immutable)
   - `id`, `user` (`@ManyToOne` write-only), `stock` (`@ManyToOne` write-only), `userId` & `stockId` (read-only getters), `type` (`BUY`/`SELL`), `quantity`, `price`, `timestamp`

7. **Lesson** (`lessons`)
   - `id`, `title`, `content`, `unlockCondition`
   - `@OneToMany` `quizAttempts`

8. **QuizAttempt** (`quiz_attempts`)
   - `id`, `user` (`@ManyToOne` write-only), `lesson` (`@ManyToOne` write-only), `userId` & `lessonId` (read-only getters), `score`, `timestamp`

9. **Badge** (`badges`)
   - `id`, `user` (`@ManyToOne` write-only), `userId` (read-only getter), `badgeType`, `awardedAt`
