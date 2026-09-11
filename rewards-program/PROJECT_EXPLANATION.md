# Retailer Rewards Program: Complete Explanation

This project calculates customer reward points from purchase transactions and
returns monthly breakdown plus total points over a trailing time window.

## 1. Project purpose

The API applies the following rules:

- $0 to $50 earns 0 points
- $50.01 to $100 earns 1 point per dollar above $50
- over $100 earns 50 points for the first $100 plus 2 points for each dollar above $100

Example: a $120 purchase = 90 points.

## 2. Spring Boot app startup

The app starts from the main class in:

- `src/main/java/com/retailer/rewards/RewardsApplication.java`

This is a standard Spring Boot application that bootstraps the web server,
component scanning, and configuration.

## 3. Data layer

The project uses:

- `CustomerRepository`
- `TransactionRepository`
- `Customer` entity
- `Transaction` entity
- `DataInitializer`

Data is seeded into an in-memory H2 database at startup so the app behaves like a
real database-backed service.

## 4. Async behavior

The async layer is still used to simulate a slow external data fetch:

- `TransactionDataService`
- `AsyncConfig`

This keeps the service logic close to a real-world architecture while still using
local in-memory data.

## 5. Service flow

The main service flow is:

- `RewardsController` receives the request
- `RewardsService` validates the customer and fetches transactions
- requested transaction window is filtered by `months` and `asOfDate`
- transactions are grouped by month
- `RewardsCalculationService` computes points per transaction
- a response object is returned with total and monthly details

## 6. Validation and error handling

`GlobalExceptionHandler` returns HTTP responses for:

- 404 when the customer is not found
- 400 for invalid request values
- 503 for transaction fetch failures
- 500 for unexpected server errors

## 7. Testing

The test suite covers:

- reward formula boundaries
- month grouping logic
- unknown customer handling
- controller response status and JSON shape

## 8. Running and verifying

```bash
mvn spring-boot:run
```

Then test:

```bash
curl "http://localhost:8080/api/v1/rewards/customers/C001?months=3&asOfDate=2026-09-08"
```

The app has been verified locally with Maven:

- 23 tests run
- 0 failures
- 0 errors
- BUILD SUCCESS

- `findAllCustomers`: sab customers ki list return karta hai.
- `findTransactionsByCustomerId`: kisi customer ki transactions return karta hai.

Data maps ko unmodifiable banaya gaya hai, taaki application runtime mein seed data accidentally change na ho.

## 7. Reward calculation service

File: `src/main/java/com/retailer/rewards/service/depfo[RewardsCalculationService.java`

```java
@Service
public class RewardsCalculationService {
```

Ye Spring service sirf reward formula handle karti hai. Ismein customer, transaction store ya web layer ka dependency nahi hai. Isliye ise independently test karna easy hai.

```java
if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
    return 0;
}
```

Null, zero aur negative amount par zero points.

```java
if (amount.compareTo(UPPER_THRESHOLD) > 0)
```

Agar amount `$100` se zyada hai, to `$100` ke upar wala portion 2 points per dollar earn karta hai. Saath mein `$50-$100` tier ke fixed 50 points bhi add hote hain.

```java
else if (amount.compareTo(LOWER_THRESHOLD) > 0)
```

Agar amount `$50` se zyada aur `$100` tak hai, to `$50` ke upar ka amount points ban jaata hai.

```java
points.setScale(0, RoundingMode.DOWN).intValue()
```

Points whole number hone chahiye, isliye decimal part ko down/floor kiya jaata hai.

Examples:

```text
$45     -> 0 points
$50     -> 0 points
$75     -> 25 points
$100    -> 50 points
$120    -> 90 points
$500    -> 850 points
```

## 8. Asynchronous transaction service

File: `src/main/java/com/retailer/rewards/service/TransactionDataService.java`

```java
@Async("rewardsTaskExecutor")
public CompletableFuture<List<Transaction>> fetchTransactionsForCustomer(...)
```

Method background thread par run hota hai aur `CompletableFuture` return karta hai. Real application mein yahan external transactions microservice ya database call ho sakti thi.

Is demo mein:

```java
Thread.sleep(150);
```

150 milliseconds ki artificial latency add ki gayi hai.

Uske baad `TransactionStore` se transactions read hoti hain aur completed future ke through return hoti hain.

Agar thread interrupt ho jaaye, failed future return hota hai aur original interrupt status restore hota hai.

## 9. Main business service

File: `src/main/java/com/retailer/rewards/service/RewardsService.java`

Ye application ka main orchestration layer hai.

Iske paas teen dependencies hain:

- `TransactionStore`: customer aur transactions ke liye.
- `TransactionDataService`: async transaction fetch ke liye.
- `RewardsCalculationService`: points formula ke liye.

### Single customer calculation

```java
getRewardsForCustomer(String customerId, int months, LocalDate asOfDate)
```

Ek customer ka complete rewards response banata hai.

Pehle customer check hota hai:

```java
transactionStore.findCustomerById(customerId)
    .orElseThrow(() -> new CustomerNotFoundException(customerId));
```

Customer missing ho to processing wahi stop ho jaati hai aur 404 error generate hota hai.

Window calculate hoti hai:

```java
LocalDate periodStart = asOfDate.minusMonths(months).plusDays(1);
```

Example:

```text
asOfDate = 2026-09-08
months   = 3
start    = 2026-06-09
```

Async transactions fetch hoti hain:

```java
List<Transaction> transactions = fetchTransactions(customerId);
```

Phir sirf requested date range ki transactions rakhi jaati hain:

```java
periodStart <= transactionDate <= asOfDate
```

Transactions date ke ascending order mein sort hoti hain.

```java
YearMonth.from(t.getTransactionDate())
```

Transactions ko calendar month ke basis par group karta hai, jaise `2026-06`, `2026-07`, `2026-08`.

`TreeMap` use hone ki wajah se months sorted order mein output hote ahain.

Har transaction ke liye:

1. Amount se points calculate hote hain.
2. Month total mein points add hote hain.
3. `TransactionDetailDto` create hota hai.

Har month ke baad `MonthlyRewardDto` create hota hai. Saare months ke points add karke `totalPoints` calculate hota hai.

Finally `CustomerRewardsResponse` return hota hai.

### All customers calculation

```java
findAllCustomers().stream()
    .map(c -> getRewardsForCustomer(...))
    .collect(Collectors.toList())
```

Har known customer ke liye same single-customer method call hota hai aur responses list mein collect hote hain.

### Async result wait karna

```java
future.get(5, TimeUnit.SECONDS)
```

Service maximum 5 seconds tak async transaction fetch ka wait karti hai.

- `InterruptedException`: thread interrupt hone par.
- `ExecutionException`: async operation fail hone par.
- `TimeoutException`: 5 seconds mein response na aane par.

In cases mein `TransactionFetchException` throw hoti hai.

## 10. REST controller

File: `src/main/java/com/retailer/rewards/controller/RewardsController.java`

```java
@RestController
@RequestMapping("/api/v1/rewards")
@Validated
```

- Class REST endpoints provide karti hai.
- Common base path `/api/v1/rewards` hai.
- Query parameter validation enabled hai.

### Single customer endpoint

```text
GET /api/v1/rewards/customers/{customerId}
```

Example:

```text
GET /api/v1/rewards/customers/C001?months=3&asOfDate=2026-09-08
```

`@PathVariable` URL se customer ID read karta hai.

`months` ka default value `3` hai. `@Min(1)` aur `@Max(24)` ensure karte hain ki value 1 se 24 ke beech ho.

`asOfDate` optional hai. Agar nahi diya gaya to `LocalDate.now()` use hota hai.

Controller business calculation nahi karta. Wo request ko `RewardsService` ko delegate karta hai aur `ResponseEntity.ok(...)` ke through HTTP 200 return karta hai.

### All customers endpoint

```text
GET /api/v1/rewards/customers
```

Ye same `months` aur `asOfDate` options ke saath har customer ka response return karta hai.

## 11. DTOs

Files:

- `CustomerRewardsResponse.java`
- `MonthlyRewardDto.java`
- `TransactionDetailDto.java`
- `ErrorResponse.java`

### CustomerRewardsResponse

Final customer response ke fields:

- Customer ID
- Customer name
- Period start
- Period end
- Total points
- Monthly breakdown

### MonthlyRewardDto

Ek month ka response:

- Month name, jaise `2026-07`
- Us month ke points
- Us month ki transaction details

### TransactionDetailDto

Ek transaction ka response:

- Transaction ID
- Date
- Amount
- Earned points

### ErrorResponse

Har error ko same JSON structure mein return karta hai:

```json
{
  "timestamp": "2026-09-08T10:15:30",
  "status": 404,
  "error": "Not Found",
  "message": "No customer found with id 'C999'",
  "path": "/api/v1/rewards/customers/C999"
}
```

## 12. Exceptions aur error handling

Files:

- `CustomerNotFoundException.java`
- `TransactionFetchException.java`
- `GlobalExceptionHandler.java`

`CustomerNotFoundException` unknown customer ke liye hai.

`TransactionFetchException` async transaction fetch fail ya timeout ke liye hai.

`@RestControllerAdvice` application ke errors ko centrally handle karta hai.

Status mapping:

- Unknown customer -> `404 NOT_FOUND`
- Invalid parameter -> `400 BAD_REQUEST`
- Transaction fetch failure -> `503 SERVICE_UNAVAILABLE`
- Unexpected error -> `500 INTERNAL_SERVER_ERROR`

## 13. Tests

### RewardsCalculationServiceTest

Formula ke boundary cases test karta hai:

- Null, zero, negative amount.
- `$50`.
- `$75`.
- `$100`.
- `$120`.
- `$500`.
- Decimal amounts.

### RewardsServiceTest

Business orchestration test karta hai:

- Unknown customer exception.
- Month grouping.
- Date window filtering.
- Monthly points.
- Total points.
- Empty transaction window.

Mockito fake `TransactionStore` aur `TransactionDataService` provide karta hai, isliye test real JSON ya server par dependent nahi hai.

### RewardsControllerTest

`MockMvc` se HTTP requests simulate karta hai aur check karta hai:

- Known customer par 200.
- Unknown customer par 404.
- Invalid `months` par 400.
- JSON fields correct hain.

## 14. Complete request flow

```text
Client
  |
  v
RewardsController
  |
  v
RewardsService
  |
  +--> TransactionStore: customer check
  |
  +--> TransactionDataService: async transactions fetch
  |
  +--> date filtering
  |
  +--> month grouping
  |
  +--> RewardsCalculationService: points calculation
  |
  v
DTO response
  |
  v
JSON response
```

## 15. Important date-window note

Code mein:

```java
asOfDate.minusMonths(months).plusDays(1)
```

`asOfDate=2026-09-08` aur `months=3` par `periodStart=2026-06-09` banega. Isliye June 5 ki transaction exclude hogi.

README ke sample mein June 5 wali transaction ko June breakdown mein dikhaya gaya hai, lekin current code ke actual inclusive date filter ke according wo transaction requested window ke bahar hai.
