# EBANX Take-home Assignment

A RESTful API for managing financial accounts with deposit, withdrawal, and transfer operations.

## Stack

- Java 21
- Spring Boot 3.5.13
- Maven 3.9
- JUnit 5 (testing)
- Swagger/OpenAPI (documentation)
- In-memory storage (`ConcurrentHashMap`)

## Prerequisites

- **Java 21** — [Download from Adoptium](https://adoptium.net/)
- **Maven 3.9+** — `brew install maven` (macOS) or [download manually](https://maven.apache.org/download.cgi)

## Quick Start

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The server starts at `http://localhost:8080`.

## API Documentation

### Swagger UI

Once the application is running, visit:

```
http://localhost:8080/swagger-ui.html
```

### Endpoints

#### GET `/balance`

Retrieve the balance of a specific account.

```bash
curl "http://localhost:8080/balance?account_id=acc-1"
```

| Status Code | Description |
|-------------|-------------|
| `200` | Success — returns the balance as a number |
| `404` | Account not found |

#### POST `/event`

Process a financial event (deposit, withdrawal, or transfer).

```bash
# Deposit
curl -X POST http://localhost:8080/event \
  -H "Content-Type: application/json" \
  -d '{"type":"deposit","destination":"acc-1","amount":1000}'

# Withdraw
curl -X POST http://localhost:8080/event \
  -H "Content-Type: application/json" \
  -d '{"type":"withdraw","origin":"acc-1","amount":300}'

# Transfer
curl -X POST http://localhost:8080/event \
  -H "Content-Type: application/json" \
  -d '{"type":"transfer","origin":"acc-1","destination":"acc-2","amount":200}'
```

**Request Body**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `type` | string | Yes | Event type: `deposit`, `withdraw`, or `transfer` |
| `origin` | string | Yes (withdraw/transfer) | Source account ID |
| `destination` | string | Yes (deposit/transfer) | Target account ID |
| `amount` | integer | Yes | Positive, non-zero amount |

| Status Code | Description |
|-------------|-------------|
| `201` | Success — returns the updated account(s) with their balances |
| `404` | Account not found |
| `422` | Validation error (negative/zero amount, insufficient funds, invalid event type) |

## Project Structure

```
src/main/java/com/ebanx/challenge/
├── EbanxChallengeApplication.java   # Spring Boot entry point
├── controller/
│   └── AccountController.java       # REST endpoints
├── service/
│   └── AccountService.java          # Business logic & validation
├── repository/
│   └── AccountRepository.java       # In-memory data store
├── model/
│   └── Account.java                 # Account entity
├── dto/
│   └── EventRequest.java            # Request payload
└── exception/
    └── GlobalExceptionHandler.java  # Centralized error handling
```

## Testing

```bash
# Run unit tests
mvn test

# Run integration tests (requires running server)
# See integration test examples in the API section above
```

All 40 unit tests cover:
- Happy path operations (deposit, withdraw, transfer, balance)
- Amount validation (negative, zero, null)
- Insufficient funds checks
- Account not found scenarios
- Invalid field validation (null/blank IDs, unknown event types)
- Edge cases (large amounts, self-transfers, concurrent operations)

## Docker

```bash
# Build the image
docker build -t ebanx-challenge .

# Run the container
docker run -p 8080:8080 ebanx-challenge
```

## Architecture Notes

- **In-memory storage**: Uses `ConcurrentHashMap` for simplicity and durability-free design per project requirements.
- **Validation**: All amounts must be positive and non-zero. Withdrawals and transfers check for sufficient funds.
- **Error handling**: Global exception handler maps exceptions to appropriate HTTP status codes (`404`, `422`).
- **Stateless**: Each request is independent; no session or auth management.
