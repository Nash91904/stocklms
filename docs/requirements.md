# Requirements Analysis: Stock Market Trainer & Wealth Growth LMS

## 1. Project Overview & Objectives
The **Stock Market Trainer & Wealth Growth LMS** is a full-stack learning platform combining an automated paper-trading sandbox with an adaptive Learning Management System (LMS). Unlike traditional platforms that use subjective surveys, this system infers user risk profiles and financial literacy scores algorithmically based on simulated trading behavior and quiz outcomes.

### Core Objectives
- Provide a safe, risk-free environment for college students and beginners to practice investing using simulated funds.
- Continuously ingest real market data to ensure a realistic market simulator.
- Adapt educational modules dynamically to user trading habits (e.g., serving diversification lessons to users with concentrated portfolios).
- Promote daily learning habits through gamification (streaks, badges, leaderboards).

---

## 2. System Modules

### 2.1 User & Account Management Module
- Manages user registration, profiles, and credentials.
- Tracks account creation timestamp, risk profile classification, and financial literacy score.
- Excludes Spring Security/JWT in early phases to focus on core domain modeling and PBL clarity.

### 2.2 Market Data & Ingestion Module
- Tracks stock equities with ticker symbols, company names, and market sectors.
- Automatically stores price history snapshots (closing price, volume, timestamp).
- Ensures price history records are immutable.

### 2.3 Trading & Portfolio Simulator Module
- Simulates buy and sell orders at prevailing market prices with virtual cash balances.
- Maintains user portfolios (tracking available cash balance).
- Maintains an append-only, immutable trade history ledger for all transactions.
- Validates trades against sufficient purchasing power or share holdings.

### 2.4 Behavioral Inference Engine
- Inactive in Week 1, planned for later sprints.
- Passively evaluates trade frequency, asset allocation, and drawdowns to compute an objective risk profile.
- Aggregates quiz performance and investment discipline into a quantitative financial literacy score.

### 2.5 Learning Management System (LMS)
- Stores structured educational lessons and reading content.
- Supports progressive unlock conditions (e.g., dependent on literacy score or prerequisite lessons).
- Records quiz attempts and scores per lesson.

### 2.6 Gamification & Engagement Module
- Daily activity streaks (current streak, longest streak, last active date, streak freezes) inspired by Duolingo.
- Milestone badge awards (e.g., first trade executed, lesson finished, streak maintained).

---

## 3. Scope Specification

### In Scope
- **Simulated Trading:** Virtual currency execution of buy and sell orders on equities.
- **Automated Price Tracking:** Historical price storage and tracking for selected stock tickers.
- **Behavioral Profiling:** Algorithmic calculation of risk profile and literacy score without questionnaires.
- **Educational Content:** Lesson catalog, quiz attempt tracking, and conditional lesson unlocking.
- **Gamification Mechanics:** Daily streak tracking, streak freeze safeguards, and milestone achievement badges.
- **Clean Relational Persistence:** PostgreSQL database storage with relational schema integrity.
- **DTO Layer:** Strict boundary separation between JPA entities and client response/request payloads.

### Out of Scope
- **Real Money Transactions:** No real brokerage APIs, payment gateways, or actual financial deposits/withdrawals.
- **Derivatives & Complex Instruments:** No options, futures, short-selling on margin, or crypto trading.
- **Native Mobile Apps:** Backend REST APIs only (frontend web interface or mobile clients deferred).
- **Manual Risk Surveys:** Subjective questionnaires are strictly excluded from the system design.
- **Multi-Currency:** Single base currency (USD/INR simulated) across the entire platform.

---

## 4. Functional Requirements (FR)

| ID | Module | Requirement Description | Priority |
|---|---|---|---|
| **FR-1.1** | User Management | System shall register new users with unique email addresses and hashed passwords. | High |
| **FR-1.2** | User Management | System shall initialize each new user with a default portfolio cash balance and a streak tracker. | High |
| **FR-2.1** | Market Data | System shall store stock metadata (ticker, name, sector) with unique ticker enforcement. | High |
| **FR-2.2** | Market Data | System shall log historical prices and volumes per stock in an immutable audit table. | High |
| **FR-3.1** | Portfolio & Trade | System shall allow users to execute BUY and SELL orders using simulated funds. | High |
| **FR-3.2** | Portfolio & Trade | System shall prevent trades when cash balance is insufficient for BUY orders or share balance is insufficient for SELL orders. | High |
| **FR-3.3** | Portfolio & Trade | System shall log all executed trades immutably with timestamp, price, quantity, and trade type. | High |
| **FR-4.1** | LMS | System shall store structured lessons with title, body content, and unlock conditions. | Medium |
| **FR-4.2** | LMS | System shall record user quiz attempts, scores, and completion timestamps. | Medium |
| **FR-5.1** | Gamification | System shall track active days consecutively to update current and longest streaks. | Medium |
| **FR-5.2** | Gamification | System shall award milestone badges to users based on achievements and activity. | Medium |

---

## 5. Non-Functional Requirements (NFR)
- **Data Integrity:** Trade logs and price history snapshots must be immutable (no update or delete operations permitted).
- **Simplicity & Maintainability:** The architecture follows standard Spring Boot conventions (Controller -> Service -> Repository -> Entity) with clear, beginner-readable code.
- **Robustness:** Database schema constraints (foreign keys, nullability, unique keys, positive amounts) must be strictly enforced at both database and JPA entity levels.
- **Security Baseline:** Passwords must be stored hashed. No secrets or credentials hardcoded; database credentials configured via environment variables.
