# Retailer Rewards Program: Complete Explanation

Ye document project ko startup se lekar API response tak Hinglish mein explain karta hai.

## 1. Project ka purpose

Ye Spring Boot REST API customer ke purchase transactions ke basis par reward points calculate karti hai.

Reward formula:

- Pehle `$50` par `0` points.
- `$50` se `$100` tak har dollar par `1` point.
- `$100` se upar har dollar par `2` points.

Example: `$120` purchase:

```text
First $50       = 0 points
Next $50        = 50 points
Remaining $20   = 40 points
Total           = 90 points
```

## 2. Application start kaise hoti hai

File: `src/main/java/com/retailer/rewards/RewardsApplication.java`

```java
@SpringBootApplication
public class RewardsApplication {
```

`@SpringBootApplication` Spring Boot ko configuration, component scanning aur auto-configuration enable karne bolta hai. Isse Spring `@Controller`, `@Service`, `@Repository` aur `@Configuration` classes ko automatically find karta hai.

```java
public static void main(String[] args) {
    SpringApplication.run(RewardsApplication.class, args);
}
```

Ye Java ka entry point hai. `SpringApplication.run` Spring container aur embedded web server start karta hai.

## 3. Maven configuration

File: `pom.xml`

- Spring Boot version `2.7.18` use ho raha hai.
- Java version `8` hai.
- `spring-boot-starter-web` REST API aur embedded Tomcat provide karta hai.
- `spring-boot-starter-validation` request validation ke liye hai.
- `jackson-datatype-jsr310` `LocalDate` ko JSON mein convert karta hai.
- `spring-boot-starter-test` JUnit, Mockito aur MockMvc provide karta hai.
- `spring-boot-maven-plugin` application ko run aur package karne mein help karta hai.

Application run karne ke liye:

```bash
mvn spring-boot:run
```

Tests run karne ke liye:

```bash
mvn test
```

## 4. Async configuration

File: `src/main/java/com/retailer/rewards/config/AsyncConfig.java`

```java
@Configuration
@EnableAsync
public class AsyncConfig {
```

- `@Configuration` batata hai ki class Spring configuration provide karti hai.
- `@EnableAsync` `@Async` methods ko background threads par run karne enable karta hai.

```java
@Bean(name = "rewardsTaskExecutor")
public TaskExecutor rewardsTaskExecutor() {
```

Ye `rewardsTaskExecutor` naam ka thread pool Spring mein register karta hai.

```java
executor.setCorePoolSize(4);
executor.setMaxPoolSize(8);
executor.setQueueCapacity(50);
```

- Normally 4 threads available rahenge.
- Load badhne par maximum 8 threads use ho sakte hain.
- Busy hone par 50 tasks queue mein wait kar sakte hain.

```java
executor.setThreadNamePrefix("rewards-async-");
```

Async threads ke readable names banata hai, jaise `rewards-async-1`.

## 5. Customer aur transaction models

Files:

- `src/main/java/com/retailer/rewards/model/Customer.java`
- `src/main/java/com/retailer/rewards/model/Transaction.java`

### Customer

Customer mein do fields hain:

```java
private String customerId;
private String name;
```

Example:

```text
customerId = C001
name       = Alice Johnson
```

Empty constructor Jackson ke liye required hai, taaki JSON ko Java object mein convert kiya ja sake. Parameterized constructor manually customer create karne ke liye hai. Getters values read karte hain aur setters values update karte hain.

### Transaction

Transaction mein ye fields hain:

```java
private String transactionId;
private String customerId;
private LocalDate transactionDate;
private BigDecimal amount;
```

- `transactionId`: purchase ki unique ID.
- `customerId`: kis customer ne purchase ki.
- `transactionDate`: purchase date.
- `amount`: purchase amount.

Amount ke liye `BigDecimal` use hua hai, kyunki currency ke liye `double` floating-point rounding problems create kar sakta hai.

```java
@JsonFormat(pattern = "yyyy-MM-dd")
```

Date JSON mein `2026-06-05` format mein read/write hoti hai.

`equals` aur `hashCode` transaction ID par based hain. Iska matlab same transaction ID wale transactions equal maane jayenge. `toString` logging aur debugging ke liye readable text deta hai.

## 6. Seed data aur in-memory repository

Files:

- `src/main/resources/data/seed-data.json`
- `src/main/java/com/retailer/rewards/repository/SeedData.java`
- `src/main/java/com/retailer/rewards/repository/TransactionStore.java`

### Seed JSON

Seed file mein 3 customers hain:

```text
C001 -> Alice Johnson
C002 -> Brian Smith
C003 -> Carla Diaz
```

Transactions mein transaction ID, customer ID, date aur amount diya gaya hai.

### SeedData

`SeedData` class JSON ke structure ko represent karti hai. Ismein customers ki list aur transactions ki list hoti hai. Getters aur setters Jackson ko JSON fields fill karne dete hain.

### TransactionStore

```java
@Repository
public class TransactionStore {
```

`@Repository` se Spring is class ka object automatically create karta hai. Ye real database ka in-memory replacement hai.

```java
private Map<String, Customer> customersById;
private Map<String, List<Transaction>> transactionsByCustomerId;
```

Do maps maintain kiye jaate hain:

1. Customer ID se customer.
2. Customer ID se uski transactions.

```java
@PostConstruct
void loadSeedData()
```

Spring application start hone ke baad ye method ek baar automatically run hota hai.

```java
ClassPathResource(SEED_FILE).getInputStream()
```

`data/seed-data.json` resources folder se read hoti hai.

```java
objectMapper.readValue(inputStream, SeedData.class)
```

JSON ko `SeedData` Java object mein convert karta hai.

Customer loop har customer ko ID-based map mein store karta hai. Transaction loop transactions ko customer ID ke basis par group karti hai.

Public methods:

- `findCustomerById`: ek customer ko `Optional` ke through return karta hai.
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
