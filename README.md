# 🏦 LoanEmiScheduler

A Spring Boot loan management system with automated EMI scheduling, strategy-based amortization, overdue tracking with penalty escalation, and dual-portal access for Officers and Borrowers.

---

## 1. Tech Stack

| Category | Technologies |
|---|---|
| **Core** | Java 17, Spring Boot 3.5.12, Spring Data JPA, Hibernate ORM |
| **Database** | PostgreSQL, H2 (dev) |
| **Security** | Spring Security 6, JJWT 0.12.6 |
| **Mapping & Validation** | MapStruct 1.5.5, Lombok 1.18.32, Jakarta Bean Validation |
| **Email** | Spring Boot Mail, Thymeleaf |
| **Auditing** | Hibernate Envers 6.4.4 |
| **Infra** | Logback 1.5.32, dotenv-java 3.0.0, Spring DevTools |

---

## 2. System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENT                                  │
└────────────────────────┬────────────────────────────────────┘
                         │ HTTP + Bearer JWT
┌────────────────────────▼────────────────────────────────────┐
│  JwtFilter (extracts token → validates → sets context)      │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│  Controllers                                                │
│  Auth · Loan · Emi · Transaction · Branch · Simulation      │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│  Services                                                   │
│  Auth · Loan · Officer · Emi · Transaction                  │
│  PaymentAllocation · StrategySuggestion · Email · Audit     │
└──────────┬──────────────────────────────────┬───────────────┘
           │                                  │
┌──────────▼──────────┐         ┌─────────────▼──────────────┐
│  Action Services    │         │  Strategy Engine (Factory)  │
│  LoanActionService  │         │  Flat · Reducing · StepUp  │
│  EmiActionService   │         └─────────────┬──────────────┘
└──────────┬──────────┘                       │
           │                                  │
┌──────────▼──────────────────────────────────▼───────────────┐
│  Repositories (Spring Data JPA)                             │
└────────────────────────┬────────────────────────────────────┘
                         │
┌────────────────────────▼────────────────────────────────────┐
│  PostgreSQL (public + app_audit schema)                     │
└─────────────────────────────────────────────────────────────┘

External: GovernmentApi (localhost:8081) · Gmail SMTP
Cron:     DailyScheduler → DailyEmiProcessingJob (midnight)
```

---

## 3. Package Structure

| Package | Responsibility |
|---|---|
| `config` | Security filter chain, MVC interceptor registration, Thymeleaf setup, MDC request tracing |
| `filter` | JWT filter — validates token, extracts roles from claims, sets SecurityContext without DB call |
| `controller` | REST endpoints for auth, loans, EMIs, transactions, branches, and simulation |
| `service` | Business logic — auth, loan lifecycle, EMI queries, payment allocation (waterfall), strategy suggestion (DTI-based), JWT, email, audit |
| `action_service` | State machines for Loan (APPLIED→ACTIVE→OVERDUE→DELINQUENT→NPA→CLOSED) and EMI (PENDING→PARTIALLY_PAID→PAID→OVERDUE→CANCELLED) |
| `loan_strategy` | Strategy Pattern — Flat, Reducing Balance, Step-Up; each handles schedule generation, versioned retrieval, and re-amortization |
| `factory` | Factory Pattern — auto-wires all strategy beans, provides runtime lookup by enum |
| `entity` | JPA entities — User→Officer/Borrower (JOINED inheritance), Loan (@Audited), Emi (versioned), Transaction, PaymentAllocation, Penalty, Notification, Branch, Address, GlobalConfig |
| `dto` | Request DTOs (with validation) and response DTOs (role-specific views) |
| `dto_mapper` | MapStruct mappers for compile-time entity ↔ DTO conversion |
| `repository` | Spring Data JPA repositories with custom JPQL/native queries |
| `enums` | LoanStatus, EmiStatus, LoanStrategy, LoanType, Role, TransactionMode, PaymentAllocationType, NotificationType, Gender, LogTag |
| `exception` | GlobalExceptionHandler (@RestControllerAdvice) + custom exceptions (ResourceNotFound, ScheduleAlreadyExists, AmortizationNotPossible, SignUpFailed) |
| `cron_job` | Midnight scheduler — overdue detection, penal interest calculation, penalty creation, loan status escalation, email reminders |
| `constant` | Interest rates, penalty amount, global config keys |

---

## 4. Modules

- Core EMI Scheduler (cron-based overdue detection, penal interest, DPD escalation)
- Loan Strategy Engine (Flat, Reducing Balance, Step-Up + re-amortization)
- DTI-Based Strategy Suggestion
- JWT Authentication (stateless, roles in claims)
- Payment Allocation Engine (waterfall: penal interest → penalty → interest → principal)
- Email Notification Service (Thymeleaf templates, async)
- Audit Trail (Hibernate Envers, `app_audit` schema)
- GovernmentApi Integration (PAN-based registration via RestClient)
- Loan State Machine (APPLIED → ACTIVE → OVERDUE → DELINQUENT → NPA → CLOSED)
- EMI State Machine (PENDING → PARTIALLY_PAID → PAID → OVERDUE → CANCELLED)

---

## 5. Database Indexes

| Index | Table | Columns | Condition | Purpose |
|---|---|---|---|---|
| `idx_emis_overdue_global` | `emis` | `(due_date, emi_status)` | `is_active = true AND is_deleted = false` | Daily cron overdue scan |
| `idx_emis_overdue_by_loan` | `emis` | `(loan_id, due_date, emi_status)` | `is_active = true AND is_deleted = false` | Payment flow — overdue + current EMI lookup |
| `idx_emis_loan_active` | `emis` | `(loan_id)` | `is_active = true AND is_deleted = false` | Re-amortization queries |
| `idx_emis_loan_created` | `emis` | `(loan_id, installment_no, created_at, version)` | `is_deleted = false` | Versioned schedule retrieval |
| `idx_loans_branch_status` | `loans` | `(branch_id, loan_status)` | `is_deleted = false` | Officer dashboard |
| `idx_loans_borrower_status` | `loans` | `(borrower_id, loan_status)` | `is_deleted = false` | Borrower dashboard |
| `idx_loans_branch_borrower` | `loans` | `(branch_id, borrower_id)` | `is_deleted = false` | Officer borrower search |
| `idx_payment_alloc_emi` | `payment_allocations` | `(emi_id, payment_allocation_type)` | `is_deleted = false` | Allocation lookups during re-amortization |

---

## 6. API Endpoints

Base: `http://localhost:8080/api/v1`

### Auth

| Method | Endpoint |
|---|---|
| `POST` | `/auth/signup/borrower` |
| `POST` | `/auth/signup/officer` |
| `POST` | `/auth/login/borrower` |
| `POST` | `/auth/login/officer` |

### Loans — Borrower

| Method | Endpoint |
|---|---|
| `POST` | `/loans/my-loans` |
| `GET` | `/loans/my-loans` |
| `GET` | `/loans/my-loans/{loanNumber}` |

### Loans — Officer

| Method | Endpoint |
|---|---|
| `GET` | `/loans/branch-loans` |
| `GET` | `/loans/branch-loans/my-loans` |
| `GET` | `/loans/branch-loans/{accountNumber}` |
| `PATCH` | `/loans/branch-loans/approve` |
| `PATCH` | `/loans/branch-loans/reject` |

### EMIs

| Method | Endpoint |
|---|---|
| `GET` | `/loans/emis/future-emis` |
| `GET` | `/loans/emis/past-emis` |
| `GET` | `/loans/emis/next-emi` |
| `GET` | `/loans/emis/emi-schedule` |

### Transactions

| Method | Endpoint |
|---|---|
| `PUT` | `/loans/pay` |

### Branches

| Method | Endpoint |
|---|---|
| `POST` | `/branches` |
| `GET` | `/branches` |

### Simulation

| Method | Endpoint |
|---|---|
| `GET` | `/simulate/emi-schedule` |
| `GET` | `/simulate/job` |

---

## 7. How to Run

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+
- **GovernmentApi running on `localhost:8081`** (required for signup)

### Setup

**1. Start GovernmentApi first:**
```bash
cd /path/to/GovernmentApi
mvn spring-boot:run
```

**2. PostgreSQL:**
```sql
CREATE DATABASE loan_emi_db;
CREATE SCHEMA app_audit;
INSERT INTO global_configs (key, value) VALUES ('account_number_counter', '1000');
INSERT INTO global_configs (key, value) VALUES ('loan_number_counter', '1000');
```

**3. Create `.env` in project root:**
```env
DB_URL=jdbc:postgresql://localhost:5432/loan_emi_db
DB_USERNAME=your_username
DB_PASSWORD=your_password
EMAIL=your_email@gmail.com
EMAIL_APP_PASSWORD=your_app_password
```

**4. Build & Run:**
```bash
mvn clean install -DskipTests
mvn spring-boot:run
```

**5. Verify:**
```bash
curl http://localhost:8080/api/v1
```
