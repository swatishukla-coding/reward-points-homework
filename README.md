# Rewards Program

Spring Boot REST API for calculating customer reward points.

## Reward rules
- 2 points for every whole dollar spent over $100.
- 1 point for every whole dollar spent between $50 and $100.
- 0 points for the first $50.
- Example: $120 = 90 points.

## API
`GET /api/rewards/{customerId}?months=3`

`customerId` must match `C` followed by 3 digits. `months` is required and must be greater than 0. The requested window uses full calendar months and includes the current month.

```bash
curl "http://localhost:8080/api/rewards/C001?months=3"
```

Response contains customer id, requested months, monthly reward objects, transaction date/amount/points, and overall total.

## Build and run
Requirements: Java 8+ and Maven 3.6+.

```bash
mvn clean package
mvn spring-boot:run
```

```bash
curl "http://localhost:8080/actuator/health"
```

## Tests

```bash
mvn test
```

JUnit 5, Mockito and MockMvc tests cover reward thresholds, negative amounts, null/blank/malformed customer IDs, zero/negative/non-numeric/missing months, unknown customers, C002 and C003, three-month aggregation, full-calendar-window filtering, multiple transactions in one month, monthly totals, transaction-level points, HTTP 200, HTTP 400 and HTTP 404.

## Validation and error handling
- Missing or invalid `months` returns HTTP 400.
- Blank or malformed customer id is rejected.
- Unknown customer returns HTTP 404.
- Negative transaction amount is rejected.
- Errors use a structured response containing `timestamp`, `status` and `message`.

## Design
- Base package: `com.charter.reward`.
- Money uses `BigDecimal`.
- DTOs and models use Lombok to remove getter/constructor boilerplate.
- Reward calculation is isolated in `RewardCalculator`.
- Customer lookup uses stream `findFirst()`.
- Controller validation uses `@Validated`, `@Pattern` and `@Min`; service validation remains the business validation layer.
- Transaction fetching uses `@Async` and `CompletableFuture`; production request handling does not call `get()` or `join()`.
- Response uses typed DTOs with monthly and transaction-level breakdowns.
- Sample transaction dates are relative to `LocalDate.now()`.
- `GlobalExceptionHandler` provides structured 400, 404 and 500 responses.
- Application logging, async executor settings and Actuator health configuration are included.
- Public application/API classes and methods contain required JavaDoc.

## API result screenshot

The repository includes the API result screenshot used for submission verification.

![API result screenshot](screenshots/postman-api-results.png)

The screenshot covers the submitted success/error verification scenarios. The application was also verified from Termux with the running local Spring Boot server and `curl` requests for success, customer-not-found, invalid months and missing months scenarios.
