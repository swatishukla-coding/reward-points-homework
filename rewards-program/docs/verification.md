# Verification Notes

## Test command
```bash
mvn test
```

## Verified result
- 23 tests executed
- 0 failures
- 0 errors
- BUILD SUCCESS

## API check
```bash
curl "http://localhost:8080/api/v1/rewards/customers/C001?months=3&asOfDate=2026-09-08"
```

## Expected response snapshot
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

## GitHub push note
You must push from a GitHub account that has permission to the target repo. The URL is:
https://github.com/swatishukla-coding/reward-points-homework
