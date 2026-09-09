# Rewards Program

This is my solution for the customer rewards points assignment.

## Problem

A customer earns points on every purchase:
- 2 points for every dollar spent over $100
- 1 point for every dollar spent between $50 and $100
- nothing for the first $50

Example: a $120 purchase = 2x$20 + 1x$50 = 90 points.

Given a customer's transactions over a period, I need to calculate the
points earned per month and the total.

## How I built it

- `Customer` and `Transaction` are simple model classes.
- `RewardService` has the points formula and the main logic for
  grouping transactions by month and adding up the points.
- `TransactionFetcher` simulates fetching a customer's transactions
  asynchronously (like calling a database or another service) using
  `@Async`, so it doesn't block the main thread while "fetching".
- `RewardController` exposes the REST endpoint.
- `CustomerNotFoundException` is thrown if you ask for a customer
  that doesn't exist, and the controller returns a 404 for that.
- I used an in-memory list of sample customers and transactions
  instead of a real database, just to keep the demo simple.

## API

```
GET /api/rewards/{customerId}?months=3
```

`months` is optional and defaults to 3.

Example:
```
GET /api/rewards/C001
```

Response:
```json
{
  "customerId": "C001",
  "customerName": "Alice Johnson",
  "monthlyPoints": {
    "2026-06": 90,
    "2026-07": 299,
    "2026-08": 150
  },
  "totalPoints": 539
}
```

If the customer doesn't exist you get a 404 with an error message.
If `months` is 0 or negative you get a 400.

## Sample data

Customers: C001 (Alice Johnson), C002 (Brian Smith), C003 (Carla Diaz)

I added transactions spread across June, July and August 2026, plus
one older transaction (May) on C001 just to prove that transactions
outside the requested window don't get counted.

## Running it

```
mvn spring-boot:run
```

Then hit it with curl or a browser:
```
curl "http://localhost:8080/api/rewards/C001"
```

## Running the tests

```
mvn test
```

The tests check the points formula (including the $120 = 90 points
example from the assignment) and that an unknown customer id throws
an exception.

## Notes / things I'd do differently with more time

- Right now the data is hardcoded in the service class. In a real
  app this would come from a database.
- Amounts are stored as `double`. For real money handling
  `BigDecimal` would be safer, kept it simple here.
