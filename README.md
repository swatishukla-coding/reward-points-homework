# Rewards Program

Spring Boot REST API for calculating customer reward points.

## Reward rules
- 2 points for every whole dollar spent over $100.
- 1 point for every whole dollar spent between $50 and $100.
- 0 points for the first $50.
- Example: $120 = 90 points.

## API
`GET /api/rewards/{customerId}?months=3`

Both `customerId` and `months` are required. `customerId` must not be blank and `months` must be greater than 0. The requested window is based on full calendar months and includes the current month.

Successful request:
```bash
curl "http://localhost:8080/api/rewards/C001?months=3"
```

Successful response shape:
```json
{
  "customerId": "C001",
  "months": 3,
  "monthly": [
    {
      "year": 2026,
      "month": "September",
      "points": 90,
      "transactions": [{"date":"2026-09-01","amount":120.00,"points":90}]
    }
  ],
  "total": 90
}
```

Error request:
```bash
curl "http://localhost:8080/api/rewards/C999?months=3"
```

404 response shape:
```json
{"timestamp":"2026-09-10T12:00:00Z","status":404,"message":"Customer not found: C999"}
```

## Build and run
Requirements: Java 8+ and Maven 3.6+.
```bash
mvn clean package
mvn spring-boot:run
```

Health check after startup:
```bash
curl "http://localhost:8080/actuator/health"
```

## Tests
```bash
mvn test
```

The tests use JUnit 5 and Mockito and cover reward thresholds, negative amounts, input validation, customer-not-found handling, missing query parameters, 3-month aggregation, full-calendar-window filtering, multiple transactions in the same month, monthly totals, transaction-level points, and multiple customers.

## Postman verification
Create GET requests for the successful and error URLs above. A real Postman screenshot should be captured after running the application; no screenshot is fabricated in this repository.


## Design assumptions and decisions
- Money is represented with `BigDecimal`; reward points use whole dollars (fractional cents do not earn partial points).
- The requested range contains full calendar months and includes the current calendar month.
- Sample customers/transactions are in-memory because this exercise does not require a database.
- Reward calculation is isolated in `RewardCalculator`; orchestration/filtering stays in `RewardService`.
- Transaction fetching is intentionally asynchronous using Spring `@Async` and `CompletableFuture`. The controller returns the future directly, so the request thread is not blocked with `get()` or `join()` in production code. This demonstrates a non-blocking service boundary that can later wrap database/remote I/O.
- Only `/actuator/health` is exposed for operational health checking.

## Validation/error cases
- Missing `months` -> HTTP 400 structured `ApiError`.
- `months <= 0` or blank customer id -> HTTP 400.
- Unknown customer -> HTTP 404.
- Negative transaction amount -> rejected by `RewardCalculator`.

## Postman screenshots
Actual Postman screenshots must be captured after the application is running. They are intentionally not fabricated. Capture at least: successful C001 request, unknown C999 (404), missing `months` (400), and `/actuator/health`.

## Review fixes implemented
- Standard base package is `com.charter.reward`.
- Application name, logging, async executor and health configuration are documented in `application.properties`.
- Reward formula is isolated in `RewardCalculator`.
- Lombok removes model/DTO getter boilerplate; Java 8 compatibility is retained.
- Monetary values use `BigDecimal`.
- Customer lookup uses stream/find-first rather than an unnecessary loop.
- Service-layer validation is retained as the business safety net; controller constraints use `@Validated`, `@Min` and `@Pattern` for HTTP-boundary validation.
- Production request flow remains asynchronous and does not call `get()`/`join()`.
- Structured `ApiError` responses are produced by `@RestControllerAdvice`.
- Responses use typed DTOs and include transaction-level and monthly breakdowns.
- Sample transaction dates are relative to `LocalDate.now()` rather than fixed calendar dates.
- Public application/API classes and methods have JavaDoc, and request/outcome/error logging is present.
- Mockito service tests and MockMvc MVC-slice tests cover success, 404, 400, malformed input, missing query parameters, negative amounts, three-month aggregation, window filtering, multiple customers, and multiple transactions in one month.
