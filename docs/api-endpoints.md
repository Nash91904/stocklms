# REST API Endpoints Specification

This document details all REST endpoints implemented for the **Stock Market Trainer & Wealth Growth LMS** backend. All endpoints are mapped under the `/api` prefix.
All endpoints return standard `ResponseEntity<ResponseBean>` wrappers.

---

## 1. ResponseBean Structure & Examples

Every endpoint produces a `ResponseBean` containing:
- `errorCode`: HTTP status code as a string (`"200"`, `"201"`, `"400"`, `"404"`, `"409"`, `"500"`)
- `errorDesc`: Status description or error detail message (`"Success"`, `"Validation failed"`, `"Resource not found with id: ..."`, etc.)
- `data`: Entity object, list of entities, validation error map, or confirmation string message.

### Success Response Example (`GET /api/stocks/1`)
```json
{
  "errorCode": "200",
  "errorDesc": "Success",
  "data": {
    "id": 1,
    "ticker": "AAPL",
    "name": "Apple Inc.",
    "sector": "Technology"
  }
}
```

### Creation Response Example (`POST /api/trades`)
```json
{
  "errorCode": "201",
  "errorDesc": "Success",
  "data": {
    "id": 100,
    "userId": 1,
    "stockId": 10,
    "type": "BUY",
    "quantity": 5,
    "price": 182.50,
    "timestamp": "2026-10-24T21:00:00"
  }
}
```

### Deletion Response Example (`DELETE /api/stocks/5`)
```json
{
  "errorCode": "200",
  "errorDesc": "Success",
  "data": "Stock with ID 5 deleted successfully."
}
```

### Validation Error Example (`POST /api/stocks` with blank fields)
```json
{
  "errorCode": "400",
  "errorDesc": "Validation failed",
  "data": {
    "ticker": "Ticker symbol is required",
    "name": "Company name is required"
  }
}
```

### Not Found Error Example (`GET /api/stocks/99`)
```json
{
  "errorCode": "404",
  "errorDesc": "Stock not found with id: 99"
}
```

### Conflict Error Example (`DELETE /api/users/1` with trades)
```json
{
  "errorCode": "409",
  "errorDesc": "Cannot delete user with id 1 because they have existing trade records in the immutable ledger"
}
```

---

## 2. Summary of All Endpoints

| Method | URL | Request Body (Entity) | Status Code | Purpose |
|---|---|---|---|---|
| **GET** | `/api/stocks` | _None_ | `200 OK` | Retrieve all tracked stock equities |
| **GET** | `/api/stocks/{id}` | _None_ | `200 OK` / `404` | Retrieve a single stock by ID |
| **POST** | `/api/stocks` | `Stock` | `201 Created` / `400` | Add a new stock equity (unique ticker) |
| **PUT** | `/api/stocks/{id}` | `Stock` | `200 OK` / `404` / `400` | Full update of an existing stock |
| **DELETE** | `/api/stocks/{id}` | _None_ | `200 OK` / `404` / `409` | Delete a stock by ID (409 if referenced by trades or price history) |
| **GET** | `/api/lessons` | _None_ | `200 OK` | Retrieve all educational lessons |
| **GET** | `/api/lessons/{id}` | _None_ | `200 OK` / `404` | Retrieve a single lesson by ID |
| **POST** | `/api/lessons` | `Lesson` | `201 Created` / `400` | Create a new educational lesson |
| **PUT** | `/api/lessons/{id}` | `Lesson` | `200 OK` / `404` / `400` | Full update of a lesson |
| **DELETE** | `/api/lessons/{id}` | _None_ | `200 OK` / `404` | Delete a lesson by ID |
| **GET** | `/api/users` | _None_ | `200 OK` | Retrieve all users (`passwordHash` excluded via `@JsonIgnore`) |
| **GET** | `/api/users/{id}` | _None_ | `200 OK` / `404` | Retrieve a user by ID |
| **POST** | `/api/users` | `User` (with `password`) | `201 Created` / `400` | Register user (hashes password with BCrypt; provisions initial Portfolio with $100,000 and zeroed Streak) |
| **PUT** | `/api/users/{id}` | `User` | `200 OK` / `404` / `400` | Full update of user account |
| **PATCH** | `/api/users/{id}` | `User` (partial fields) | `200 OK` / `404` / `400` | Partial update of user (`riskProfile`, `literacyScore`) |
| **DELETE** | `/api/users/{id}` | _None_ | `200 OK` / `404` / `409` | Delete user, portfolio, streak, watchlist links, quiz attempts, and badges (409 if user has trades) |
| **POST** | `/api/users/{userId}/watchlist/{stockId}` | _None_ | `201 Created` / `404` / `409` | Add a stock to user's watchlist (409 if already in watchlist) |
| **DELETE** | `/api/users/{userId}/watchlist/{stockId}` | _None_ | `200 OK` / `404` | Remove a stock from user's watchlist |
| **GET** | `/api/users/{userId}/watchlist` | _None_ | `200 OK` / `404` | Retrieve all stocks in user's watchlist |
| **GET** | `/api/portfolios/{id}` | _None_ | `200 OK` / `404` | Retrieve portfolio by portfolio ID |
| **GET** | `/api/portfolios/user/{userId}` | _None_ | `200 OK` / `404` | Retrieve portfolio by user ID |
| **PATCH** | `/api/portfolios/{id}` | `Portfolio` (cashBalance) | `200 OK` / `404` / `400` | Partial update of portfolio cash balance |
| **GET** | `/api/streaks/{id}` | _None_ | `200 OK` / `404` | Retrieve streak tracker by streak ID |
| **GET** | `/api/streaks/user/{userId}` | _None_ | `200 OK` / `404` | Retrieve streak tracker by user ID |
| **PATCH** | `/api/streaks/{id}` | `Streak` (partial fields) | `200 OK` / `404` / `400` | Partial update of streak fields (current, longest, freeze count, last active date) |
| **POST** | `/api/price-history` | `PriceHistory` | `201 Created` / `404` / `400` | Ingest a new price/volume snapshot for a stock (immutable) |
| **GET** | `/api/price-history/stock/{stockId}` | _None_ | `200 OK` / `404` | Retrieve price snapshots for a stock (latest first) |
| **GET** | `/api/price-history?stockId={stockId}` | _None_ | `200 OK` / `404` | Retrieve price snapshots by query param |
| **POST** | `/api/trades` | `Trade` | `201 Created` / `400` / `404` | Execute BUY or SELL order; validates cash/holdings and updates portfolio in one `@Transactional` method |
| **GET** | `/api/trades/{id}` | _None_ | `200 OK` / `404` | Retrieve a single trade by ID |
| **GET** | `/api/trades/user/{userId}` | _None_ | `200 OK` / `404` | Retrieve user trade history (newest first) |
| **GET** | `/api/trades?userId={userId}` | _None_ | `200 OK` / `404` | Retrieve user trade history by query param |
| **POST** | `/api/quiz-attempts` | `QuizAttempt` | `201 Created` / `404` / `400` | Record a user's quiz attempt and score |
| **GET** | `/api/quiz-attempts/user/{userId}` | _None_ | `200 OK` / `404` | Retrieve all quiz attempts for a user |
| **GET** | `/api/quiz-attempts?userId={userId}` | _None_ | `200 OK` / `400` / `404` | Retrieve quiz attempts for a user by query param (400 if `userId` is missing) |
| **POST** | `/api/badges` | `Badge` | `201 Created` / `404` / `400` | Award an achievement badge to a user |
| **GET** | `/api/badges/user/{userId}` | _None_ | `200 OK` / `404` | Retrieve all badges awarded to a user |
| **GET** | `/api/badges?userId={userId}` | _None_ | `200 OK` / `400` / `404` | Retrieve badges for a user by query param (400 if `userId` is missing) |
| **DELETE** | /api/badges/{id} | _None_ | 200 OK / 404 | Delete a badge by ID |

---

## 3. Sample Request Bodies Using JPA Entities

### Register User (`POST /api/users`)
```json
{
  "email": "trader@stocklms.com",
  "password": "securePassword123",
  "riskProfile": "MODERATE",
  "literacyScore": 50
}
```

### Create Stock (`POST /api/stocks`)
```json
{
  "ticker": "NVDA",
  "name": "NVIDIA Corporation",
  "sector": "Technology"
}
```

### Execute Trade (`POST /api/trades`)
```json
{
  "user": { "id": 1 },
  "stock": { "id": 10 },
  "type": "BUY",
  "quantity": 10
}
```

### Record Price Snapshot (`POST /api/price-history`)
```json
{
  "stock": { "id": 10 },
  "price": 185.60,
  "volume": 32000000
}
```

### Submit Quiz Attempt (`POST /api/quiz-attempts`)
```json
{
  "user": { "id": 1 },
  "lesson": { "id": 2 },
  "score": 95
}
```

### Award Achievement Badge (`POST /api/badges`)
```json
{
  "user": { "id": 1 },
  "badgeType": "FIRST_TRADE"
}
```

---

## 4. Deletion Rules & Immutable Ledger Protection (HTTP 409 Conflict)

To preserve immutable audit records and historical financial ledgers:
1. **User Deletion (`DELETE /api/users/{id}`):**
   - If the user has executed trades stored in the immutable `trades` ledger, deletion is rejected with `409 Conflict`.
   - Otherwise, deleting the user cleanly deletes their linked `portfolio`, `streak`, `quiz_attempts`, `badges`, and clears their `user_watchlist` join entries.
2. **Stock Deletion (`DELETE /api/stocks/{id}`):**
   - If the stock has trade records in the immutable `trades` ledger, deletion is rejected with `409 Conflict`.
   - If the stock has historical records in the immutable `price_history` ledger, deletion is rejected with `409 Conflict`.
   - Otherwise, the stock is deleted, and any entries in user watchlists are cleaned up.
