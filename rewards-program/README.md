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
- Java 8
- Maven
- Spring Web
- Spring Validation
- Spring Data JPA
- H2 in-memory database
- JUnit 5 + Mockito

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

## Errors

- `404` for unknown customer
- `400` for invalid request parameters
- `503` if the async transaction fetch fails

## Running locally

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
- 23 tests executed
- 0 failures
- 0 errors
- BUILD SUCCESS

## Data setup

The project seeds demo data in H2 using `DataInitializer` and the JPA repositories.
This makes the app model closer to a real database-backed service while still
remaining easy to run locally.
