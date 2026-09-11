# Retailer Rewards Program API

A Spring Boot REST service that calculates customer reward points earned on
purchase transactions, grouped by month and totalled over a trailing
period.

## Reward rules

A customer earns:
- 2 points for every dollar over $100 in a transaction
- 1 point for every dollar between $50 and $100
- 0 points for the first $50

Example: a $120 purchase = 90 points.

## Project implementation

This project uses:
- Spring Boot 2.7.18
- Java 8 source and bytecode compatibility
- JDK 21 supported for local build and runtime
- Maven
- Spring Web
- Spring Validation
- Spring Data JPA
- H2 in-memory database
- JUnit 5 + Mockito

## Java version note

The Maven configuration targets Java 8 for compatibility with older Java
environments. The application can also be compiled and run with JDK 21, which
is the runtime used for local development. Using JDK 21 does not change the
application's Java 8-compatible source and bytecode target.

## API endpoints

Base path: `/api/v1/rewards`

### `GET /customers/{customerId}`

Query params:
- `months` (default `3`)
- `asOfDate` (`yyyy-MM-dd`, optional)

Example:

```bash
curl "http://localhost:8080/api/v1/rewards/customers/C001?months=3&asOfDate=2026-09-08"
```

Sample result:

```json
{
  "customerId": "C001",
  "customerName": "Alice Johnson",
  "periodStart": "2026-06-09",
  "periodEnd": "2026-09-08",
  "totalPointsEarned": 484,
  "monthlyBreakdown": [
    {"month":"2026-06","pointsEarned":25,"transactions":[...]},
    {"month":"2026-07","pointsEarned":299,"transactions":[...]},
    {"month":"2026-08","pointsEarned":160,"transactions":[...]}
  ]
}
```

### `GET /customers`

Returns reward summaries for all customers in the configured dataset.

## Error response examples

- `404` for unknown customer
- `400` for invalid request parameters
- `503` if transaction data cannot be retrieved

Example `400` response:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "months must be greater than 0",
  "path": "/api/v1/rewards/customers/C001",
  "timestamp": "2026-09-08T12:00:00"
}
```

Example `404` response:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "No customer found with id 'UNKNOWN'",
  "path": "/api/v1/rewards/customers/UNKNOWN",
  "timestamp": "2026-09-08T12:00:00"
}
```

## Running locally

Use JDK 21 or another supported JDK, then verify the active versions:

```bash
java -version
mvn -version
```

The Maven output may show JDK 21 as the runtime while the project compiler
settings still target Java 8.

```bash
mvn spring-boot:run
```

Then call the API on:

```text
http://localhost:8080
```

## Testing

```bash
mvn test
```

Current verification status:
- Run `mvn test` with the configured JDK to verify the current checkout.
- The test suite includes service, controller, and reward-calculation coverage.

## Data setup

The project seeds demo data in H2 using `DataInitializer` and the JPA repositories.
The service reads customers and transactions through those repositories; the
database is in-memory and is recreated when the application restarts.

## API data format

- Dates use `yyyy-MM-dd`.
- Monthly reward keys use the sortable ISO `YYYY-MM` format.
- Each monthly entry includes transaction ID, transaction date, amount, and points earned.

## Limitations

- H2 is an in-memory database, so data is not persistent between restarts.
- The transaction lookup is synchronous because the local data source is an in-process JPA database.
- Reward points are whole numbers and fractional calculated points are rounded down.

Screenshots for the running application, Java/Maven versions, build, and API
success/error responses are stored in the `docs/` folder. The repo does not use
`screenshots/`; all screenshots were normalized to `docs/` to match the project
contract.

The `build-success.png` screenshot includes the test execution summary and build
result. The 405 response screenshot should be saved as
`docs/api-error-method-not-allowed.png`.
