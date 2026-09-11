# Rewards Program Checklist

## Completed
- Spring Boot REST API created and running on port 8080
- Customer reward calculation implemented
- Monthly grouping logic implemented
- Trailing month window logic implemented
- Input validation for invalid `months` values implemented
- Error handling for unknown customer and invalid requests implemented
- Async transaction fetch simulation implemented
- JUnit tests added and passing
- H2 in-memory database and Spring Data JPA configured
- Seed data initializer added for demo customers and transactions
- README updated to describe the project accurately

## Verification
- `mvn test` passes with 23 tests run, 0 failures, 0 errors
- Sample endpoint responds successfully:
  - `GET http://localhost:8080/api/v1/rewards/customers/C001?months=3&asOfDate=2026-09-08`

## Repo status
- Local project is ready for final review and GitHub push
- The GitHub remote URL must be set to the correct repository with a user that has write permission
