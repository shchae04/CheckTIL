# 널 오브젝트 패턴(Null Object Pattern)

## 1. 한 줄 정의
널 오브젝트 패턴은 객체가 존재하지 않을 때 null을 전달하는 대신, 아무 동작도 하지 않는 기본 구현 객체를 전달하여 null 체크 로직을 제거하는 디자인 패턴이다.

---

## 2. 문제 상황: 반복되는 null 체크

### 2-1. 일반적인 null 처리 방식

```java
public void doSomething(MyObject obj) {
    if (obj == null) {
        throw new NullPointerException("Object cannot be null");
    }

    obj.doMethod();
}

public void processUser(User user) {
    if (user == null) {
        return;
    }

    user.updateProfile();
}

public String getName(Customer customer) {
    if (customer == null) {
        return "Unknown";
    }

    return customer.getName();
}
```

### 2-2. 문제점
- **코드 중복**: null 체크 로직이 여러 곳에서 반복
- **가독성 저하**: 비즈니스 로직보다 방어 로직이 더 많아짐
- **유지보수 어려움**: null 처리 방식 변경 시 모든 곳 수정 필요
- **실수 가능성**: null 체크를 빠뜨리면 NullPointerException 발생

---

## 3. 널 오브젝트 패턴의 개념

### 3-1. 기본 구조

```java
// 공통 인터페이스
interface MyObject {
    void doMethod();
}

// 실제 동작을 수행하는 객체
class MyRealObject implements MyObject {
    @Override
    public void doMethod() {
        System.out.println("무엇인가 수행합니다.");
    }
}

// 아무것도 하지 않는 널 객체
class MyNullObject implements MyObject {
    @Override
    public void doMethod() {
        // 아무것도 하지 않음
    }
}
```

### 3-2. 사용 방법

```java
// Before: null 체크 필요
public void doSomething(MyObject obj) {
    if (obj == null) {
        throw new Exception();
    }
    obj.doMethod();
}

// After: null 체크 불필요
public void doSomething(MyObject obj) {
    obj.doMethod();  // NullObject라면 아무것도 안 함
}
```

---

## 4. 실무 적용 예시

### 4-1. 로거(Logger) 패턴

```java
// 인터페이스
interface Logger {
    void log(String message);
}

// 실제 로거
class ConsoleLogger implements Logger {
    @Override
    public void log(String message) {
        System.out.println("[LOG] " + message);
    }
}

// 널 로거 (로깅 비활성화 시)
class NullLogger implements Logger {
    @Override
    public void log(String message) {
        // 아무것도 하지 않음 (로깅 안 함)
    }
}

// 사용
class Service {
    private Logger logger;

    public Service(boolean enableLogging) {
        this.logger = enableLogging ? new ConsoleLogger() : new NullLogger();
    }

    public void execute() {
        logger.log("Service executed");  // null 체크 불필요
    }
}
```

### 4-2. 고객(Customer) 처리

```java
// 인터페이스
interface Customer {
    String getName();
    void sendEmail(String message);
    boolean isValid();
}

// 실제 고객
class RealCustomer implements Customer {
    private String name;
    private String email;

    public RealCustomer(String name, String email) {
        this.name = name;
        this.email = email;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void sendEmail(String message) {
        System.out.println("Sending email to " + email + ": " + message);
    }

    @Override
    public boolean isValid() {
        return true;
    }
}

// 널 고객 (고객 정보 없을 때)
class NullCustomer implements Customer {
    @Override
    public String getName() {
        return "Guest";
    }

    @Override
    public void sendEmail(String message) {
        // 이메일 전송 안 함
    }

    @Override
    public boolean isValid() {
        return false;
    }
}

// 사용
class CustomerService {
    public Customer findCustomer(Long id) {
        Customer customer = database.findById(id);
        return (customer != null) ? customer : new NullCustomer();
    }

    public void processCustomer(Long id) {
        Customer customer = findCustomer(id);

        // null 체크 없이 안전하게 사용
        System.out.println("Customer: " + customer.getName());
        customer.sendEmail("Welcome!");
    }
}
```

### 4-3. 특수 케이스: ZeroCapacityStack

```java
// 스택 인터페이스
interface Stack<T> {
    void push(T item);
    T pop();
    boolean isEmpty();
    int size();
}

// 일반 스택
class NormalStack<T> implements Stack<T> {
    private List<T> items = new ArrayList<>();
    private int capacity;

    public NormalStack(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public void push(T item) {
        if (items.size() >= capacity) {
            throw new StackOverflowError();
        }
        items.add(item);
    }

    @Override
    public T pop() {
        if (isEmpty()) {
            throw new EmptyStackException();
        }
        return items.remove(items.size() - 1);
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public int size() {
        return items.size();
    }
}

// 용량이 0인 특수 케이스 (Null Object)
class ZeroCapacityStack<T> implements Stack<T> {
    @Override
    public void push(T item) {
        // 아무것도 하지 않음 (용량 0이므로 추가 불가)
    }

    @Override
    public T pop() {
        return null;  // 항상 비어있음
    }

    @Override
    public boolean isEmpty() {
        return true;  // 항상 비어있음
    }

    @Override
    public int size() {
        return 0;  // 항상 0
    }
}

// 팩토리 메서드
class StackFactory {
    public static <T> Stack<T> createStack(int capacity) {
        if (capacity <= 0) {
            return new ZeroCapacityStack<>();
        }
        return new NormalStack<>(capacity);
    }
}
```

---

## 5. 언어별 구현

### 5-1. Java

```java
// Optional과 결합한 패턴
class UserRepository {
    public User findById(Long id) {
        User user = database.find(id);
        return (user != null) ? user : User.nullUser();
    }
}

class User {
    private static final User NULL = new NullUser();

    public static User nullUser() {
        return NULL;
    }

    public boolean isNull() {
        return false;
    }
}

class NullUser extends User {
    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public String getName() {
        return "Anonymous";
    }
}
```

### 5-2. TypeScript

```typescript
// 인터페이스
interface PaymentMethod {
  process(amount: number): void;
  isValid(): boolean;
}

// 실제 결제 수단
class CreditCard implements PaymentMethod {
  constructor(private cardNumber: string) {}

  process(amount: number): void {
    console.log(`Processing $${amount} with card ${this.cardNumber}`);
  }

  isValid(): boolean {
    return true;
  }
}

// 널 결제 수단 (결제 수단 없을 때)
class NullPaymentMethod implements PaymentMethod {
  process(amount: number): void {
    // 아무것도 하지 않음
  }

  isValid(): boolean {
    return false;
  }
}

// 사용
class CheckoutService {
  processPayment(paymentMethod: PaymentMethod, amount: number): void {
    // null 체크 불필요
    if (paymentMethod.isValid()) {
      paymentMethod.process(amount);
    }
  }
}
```

### 5-3. Python

```python
# 인터페이스 (추상 클래스)
from abc import ABC, abstractmethod

class Notifier(ABC):
    @abstractmethod
    def send(self, message: str) -> None:
        pass

# 실제 알림
class EmailNotifier(Notifier):
    def __init__(self, email: str):
        self.email = email

    def send(self, message: str) -> None:
        print(f"Sending email to {self.email}: {message}")

# 널 알림 (알림 비활성화)
class NullNotifier(Notifier):
    def send(self, message: str) -> None:
        pass  # 아무것도 하지 않음

# 사용
class UserService:
    def __init__(self, notifier: Notifier):
        self.notifier = notifier

    def register_user(self, username: str):
        print(f"User {username} registered")
        self.notifier.send(f"Welcome, {username}!")  # null 체크 불필요
```

---

## 6. 장점과 단점

### 6-1. 장점

| 장점 | 설명 |
|------|------|
| **코드 간소화** | 반복적인 null 체크 로직 제거 |
| **협력 재사용** | 동일한 인터페이스로 다형성 활용 |
| **안정성 향상** | NullPointerException 방지 |
| **가독성 개선** | 비즈니스 로직에 집중 가능 |
| **유지보수성** | null 처리 로직이 한 곳에 집중 |

```java
// Before: null 체크 로직이 흩어져 있음
public void process() {
    if (logger != null) logger.log("Start");
    if (validator != null) validator.validate();
    if (notifier != null) notifier.send();
}

// After: 깔끔한 비즈니스 로직
public void process() {
    logger.log("Start");
    validator.validate();
    notifier.send();
}
```

### 6-2. 단점

| 단점 | 설명 |
|------|------|
| **예외 탐지 어려움** | 오류가 조용히 무시될 수 있음 |
| **클래스 증가** | 각 인터페이스마다 Null 구현 필요 |
| **디버깅 어려움** | 왜 동작하지 않는지 파악 힘듦 |
| **의미 혼란** | null과 "값이 없음"의 의미 구분 모호 |

```java
// 문제 상황: 이메일이 전송되지 않는데 에러도 없음
Customer customer = findCustomer(999L);  // NullCustomer 반환
customer.sendEmail("Important notification");  // 조용히 무시됨

// 해결: 명시적 검증 추가
if (!customer.isValid()) {
    throw new CustomerNotFoundException();
}
customer.sendEmail("Important notification");
```

---

## 7. 사용 시 주의사항

### 7-1. 언제 사용해야 하는가?

**사용하면 좋은 경우:**
- null 체크 코드가 여러 곳에서 반복될 때
- 객체 부재가 정상적인 비즈니스 흐름일 때
- 선택적 기능을 제공할 때 (로깅, 알림 등)

```java
// 좋은 예: 선택적 기능
class Application {
    private Logger logger;

    public Application(boolean debugMode) {
        this.logger = debugMode ? new ConsoleLogger() : new NullLogger();
    }

    public void run() {
        logger.debug("Application started");  // debugMode에 따라 동작
        // ... 비즈니스 로직
    }
}
```

**사용하지 말아야 할 경우:**
- 객체 부재가 예외 상황일 때
- 명시적인 오류 처리가 필요할 때
- 값의 존재 여부가 중요한 비즈니스 로직일 때

```java
// 나쁜 예: 중요한 데이터에 Null Object 사용
public Order findOrder(Long orderId) {
    Order order = database.find(orderId);
    return (order != null) ? order : new NullOrder();  // 위험!
}

public void cancelOrder(Long orderId) {
    Order order = findOrder(orderId);
    order.cancel();  // NullOrder면 조용히 무시됨 - 문제!
}

// 올바른 방법: 명시적 예외 처리
public Order findOrder(Long orderId) {
    Order order = database.find(orderId);
    if (order == null) {
        throw new OrderNotFoundException(orderId);
    }
    return order;
}
```

### 7-2. Optional과의 비교

```java
// Optional 방식
public Optional<User> findUser(Long id) {
    return Optional.ofNullable(database.find(id));
}

public void processUser(Long id) {
    findUser(id)
        .ifPresent(user -> user.updateProfile());
}

// Null Object 방식
public User findUser(Long id) {
    User user = database.find(id);
    return (user != null) ? user : User.nullUser();
}

public void processUser(Long id) {
    User user = findUser(id);
    user.updateProfile();  // NullUser라면 아무것도 안 함
}
```

| 특성 | Optional | Null Object Pattern |
|------|----------|---------------------|
| **명시성** | 명시적 (값 부재가 드러남) | 암묵적 (값 부재가 숨겨짐) |
| **에러 처리** | 강제됨 (ifPresent 등) | 선택적 (isNull 체크) |
| **용도** | 값의 부재가 중요할 때 | 값의 부재를 무시해도 될 때 |

---

## 8. Backend 관점의 활용

### 8-1. API 응답 처리

```java
// API 응답에서 선택적 데이터 처리
class ApiResponse {
    private Metadata metadata;  // 선택적

    public ApiResponse() {
        this.metadata = new NullMetadata();  // 기본값
    }

    public void addMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public String toJson() {
        // metadata가 NullMetadata여도 안전하게 호출
        return metadata.includeInResponse()
            ? metadata.toJson()
            : "";
    }
}
```

### 8-2. 캐싱 전략

```java
// 캐시 미스 시 Null Object 활용
interface Cache {
    String get(String key);
    void put(String key, String value);
}

class RedisCache implements Cache {
    @Override
    public String get(String key) {
        return redisClient.get(key);
    }

    @Override
    public void put(String key, String value) {
        redisClient.set(key, value);
    }
}

class NullCache implements Cache {
    @Override
    public String get(String key) {
        return null;  // 항상 캐시 미스
    }

    @Override
    public void put(String key, String value) {
        // 아무것도 하지 않음
    }
}

// 사용: 캐시 활성화/비활성화가 코드 변경 없이 가능
class UserService {
    private Cache cache;

    public UserService(boolean cacheEnabled) {
        this.cache = cacheEnabled ? new RedisCache() : new NullCache();
    }

    public User getUser(Long id) {
        String cached = cache.get("user:" + id);
        if (cached != null) {
            return deserialize(cached);
        }

        User user = database.find(id);
        cache.put("user:" + id, serialize(user));
        return user;
    }
}
```

### 8-3. 이벤트 발행

```java
// 이벤트 발행 선택적 활성화
interface EventPublisher {
    void publish(Event event);
}

class KafkaEventPublisher implements EventPublisher {
    @Override
    public void publish(Event event) {
        kafkaProducer.send(event);
    }
}

class NullEventPublisher implements EventPublisher {
    @Override
    public void publish(Event event) {
        // 이벤트 발행 안 함
    }
}

// 사용
class OrderService {
    private EventPublisher eventPublisher;

    public void createOrder(Order order) {
        database.save(order);
        eventPublisher.publish(new OrderCreatedEvent(order));  // 환경에 따라 발행 여부 결정
    }
}
```

---

## 9. 핵심 요약

### 9-1. 핵심 개념
- **null 대신 객체**: null 참조 대신 "아무것도 하지 않는" 객체 전달
- **다형성 활용**: 동일한 인터페이스로 null 처리
- **방어 코드 제거**: 반복적인 null 체크 로직 불필요

### 9-2. 선택 기준

```
null 체크가 반복되는가? → YES → Null Object Pattern 고려
         ↓ NO
객체 부재가 예외 상황인가? → YES → 예외 처리 또는 Optional 사용
         ↓ NO
선택적 기능인가? → YES → Null Object Pattern 적합
         ↓ NO
값의 부재가 중요한가? → YES → Optional 사용
         ↓ NO
         Null Object Pattern 사용 가능
```

### 9-3. 실무 적용 팁

1. **신중한 적용**: 중요한 비즈니스 로직에는 명시적 검증 유지
2. **명확한 구분**: `isNull()` 또는 `isValid()` 메서드로 구분 가능하게
3. **문서화**: Null Object 사용 의도를 명확히 문서화
4. **테스트**: Null Object 동작도 반드시 테스트 작성
5. **Optional 병용**: 상황에 따라 Optional과 함께 사용

```java
// 좋은 예: 명시적 검증 + Null Object
public void sendNotification(User user) {
    if (!user.isValid()) {
        logger.warn("Attempted to send notification to invalid user");
        return;
    }

    notifier.send(user.getEmail(), "Hello!");
}
```

### 9-4. 관련 패턴
- **Strategy Pattern**: Null Object는 특별한 전략
- **Special Case Pattern**: Null Object는 특수 케이스의 구현
- **Optional Type**: null 안정성을 위한 또 다른 접근
