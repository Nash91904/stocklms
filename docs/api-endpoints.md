# REST API Endpoints Specification

This document details all REST endpoints implemented in Week 2 for the **Stock Market Trainer & Wealth Growth LMS** backend. All endpoints are mapped under the `/api` prefix.

---

## 1. Summary of All Endpoints

| Method | URL | Request Body | Status Code | Purpose |
|---|---|---|---|---|
| **GET** | `/api/stocks` | _None_ | `200 OK` | Retrieve all tracked stock equities |
| **GET** | `/api/stocks/{id}` | _None_ | `200 OK` / `404` | Retrieve a single stock by ID |
| **POST** | `/api/stocks` | `StockRequestDto` | `201 Created` / `400` | Add a new stock equity (unique ticker) |
| **PUT** | `/api/stocks/{id}` | `StockRequestDto` | `200 OK` / `404` / `400` | Full update of an existing stock |
| **DELETE** | `/api/stocks/{id}` | _None_ | `204 No Content` / `404` | Delete a stock by ID |
| **GET** | `/api/lessons` | _None_ | `200 OK` | Retrieve all educational lessons |
| **GET** | `/api/lessons/{id}` | _None_ | `200 OK` / `404` | Retrieve a single lesson by ID |
| **POST** | `/api/lessons` | `LessonRequestDto` | `201 Created` / `400` | Create a new educational lesson |
| **PUT** | `/api/lessons/{id}` | `LessonRequestDto` | `200 OK` / `404` / `400` | Full update of a lesson |
| **DELETE** | `/api/lessons/{id}` | _None_ | `204 No Content` / `404` | Delete a lesson by ID |
| **GET** | `/api/users` | _None_ | `200 OK` | Retrieve all users (passwords excluded) |
| **GET** | `/api/users/{id}` | _None_ | `200 OK` / `404` | Retrieve a user by ID (password excluded) |
| **POST** | `/api/users` | `UserCreateRequestDto` | `201 Created` / `400` | Register user (hashes password with BCrypt; provisions initial Portfolio with $100,000 and zeroed Streak) |
| **PUT** | `/api/users/{id}` | `UserUpdateRequestDto` | `200 OK` / `404` / `400` | Full update of user account |
| **PATCH** | `/api/users/{id}` | `UserPatchRequestDto` | `200 OK` / `404` | Partial update of user (riskProfile, literacyScore) |
| **DELETE** | `/api/users/{id}` | _None_ | `204 No Content` / `404` | Delete user and their portfolio/streak |
| **GET** | `/api/portfolios/{id}` | _None_ | `200 OK` / `404` | Retrieve portfolio by portfolio ID |
| **GET** | `/api/portfolios/user/{userId}` | _None_ | `200 OK` / `404` | Retrieve portfolio by user ID |
| **PATCH** | `/api/portfolios/{id}` | `PortfolioPatchRequestDto` | `200 OK` / `404` / `400` | Partial update of portfolio cash balance |
| **GET** | `/api/streaks/{id}` | _None_ | `200 OK` / `404` | Retrieve streak tracker by streak ID |
| **GET** | `/api/streaks/user/{userId}` | _None_ | `200 OK` / `404` | Retrieve streak tracker by user ID |
| **PATCH** | `/api/streaks/{id}` | `StreakPatchRequestDto` | `200 OK` / `404` | Partial update of streak fields (current, longest, freeze count, last active date) |
| **POST** | `/api/price-history` | `PriceHistoryRequestDto` | `201 Created` / `404` / `400` | Ingest a new price/volume snapshot for a stock (immutable) |
| **GET** | `/api/price-history/stock/{stockId}` | _None_ | `200 OK` / `404` | Retrieve price snapshots for a stock (latest first) |
| **GET** | `/api/price-history?stockId={stockId}` | _None_ | `200 OK` / `404` | Retrieve price snapshots by query param |
| **POST** | `/api/trades` | `TradeRequestDto` | `201 Created` / `400` / `404` | Execute BUY or SELL order; validates cash/holdings and updates portfolio in one `@Transactional` method |
| **GET** | `/api/trades/{id}` | _None_ | `200 OK` / `404` | Retrieve a single trade by ID |
| **GET** | `/api/trades/user/{userId}` | _None_ | `200 OK` / `404` | Retrieve user trade history (newest first) |
| **GET** | `/api/trades?userId={userId}` | _None_ | `200 OK` / `404` | Retrieve user trade history by query param |
| **POST** | `/api/quiz-attempts` | `QuizAttemptRequestDto` | `201 Created` / `404` / `400` | Record a user's quiz attempt and score |
| **GET** | `/api/quiz-attempts/user/{userId}` | _None_ | `200 OK` / `404` | Retrieve all quiz attempts for a user |
| **POST** | `/api/badges` | `BadgeRequestDto` | `201 Created` / `404` / `400` | Award an achievement badge to a user |
| **GET** | `/api/badges/user/{userId}` | _None_ | `200 OK` / `404` | Retrieve all badges awarded to a user |

---

## 2. Key Architecture & Business Rules Enforced

1. **DTO Separation:** No JPA entity is ever exposed directly in REST controllers. All requests use validated `*RequestDto` classes, and all responses use `*ResponseDto` classes.
2. **Password Security:** User registration hashes the plain text password using BCrypt via `spring-security-crypto`. Response DTOs never return `passwordHash`.
3. **Automated User Provisioning:** Registering a user (`POST /api/users`) automatically provisions:
   - A `Portfolio` with an initial simulated cash balance of `$100,000.00`.
   - A `Streak` engagement tracker initialized to all zeros.
4. **Trade Execution & Immutability:**
   - `POST /api/trades` fetches the latest price for the requested stock from `PriceHistory`.
   - **BUY orders:** Checks if `cashBalance >= price * quantity`. If insufficient, returns `400 Bad Request`. Deducts total cost from portfolio cash.
   - **SELL orders:** Computes net holdings (`sum(BUY) - sum(SELL)`). Checks if `holdings >= quantity`. If insufficient, returns `400 Bad Request`. Adds proceeds to portfolio cash.
   - Cash update and trade creation are executed inside a single atomic `@Transactional` method.
   - Trades and Price History records are immutable; no `PUT` or `DELETE` endpoints exist for them.
5. **Database Seeding (`DataSeeder`):**
   - Automatically populates 10 US stocks with baseline prices, 3 lessons, and 1 demo user (`demo@stocklms.com`) on startup if tables are empty.
