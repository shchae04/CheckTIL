# 전략 패턴이란 무엇인가요?: 백엔드 개발자 관점에서의 이해

## 1. 한 줄 정의
- 전략 패턴(Strategy Pattern)은 알고리즘 군을 정의하고 각각을 캡슐화하여 상호 교환 가능하게 만드는 행위 디자인 패턴이다. 전략 패턴을 사용하면 알고리즘을 사용하는 클라이언트와 독립적으로 알고리즘을 변경할 수 있다.

---

## 2. 전략 패턴의 핵심 개념

### 2-1. 전략 패턴의 구성 요소
전략 패턴은 세 가지 핵심 요소로 구성됩니다:

#### 1) 전략 인터페이스 (Strategy Interface)
- **개념**: 공통된 알고리즘의 인터페이스를 정의
- **역할**: 모든 구체적인 전략들이 구현해야 할 메서드 선언
- **백엔드 관점**: 다형성(Polymorphism)을 활용한 추상화 레이어

#### 2) 구체적 전략 (Concrete Strategies)
- **개념**: 전략 인터페이스를 구현하는 실제 알고리즘 클래스들
- **역할**: 각기 다른 방식으로 알고리즘 구현
- **백엔드 관점**: 단일 책임 원칙(SRP)에 따라 각 전략은 하나의 알고리즘만 책임

#### 3) 컨텍스트 (Context)
- **개념**: 전략 객체를 참조하고 사용하는 클래스
- **역할**: 전략 인터페이스를 통해 구체적인 전략을 호출
- **백엔드 관점**: 의존성 주입(Dependency Injection)을 통한 느슨한 결합

---

## 3. 전략 패턴의 동작 원리

### 3-1. 기본 구조

```java
// 1. 전략 인터페이스 정의
public interface PaymentStrategy {
    void pay(int amount);
}

// 2. 구체적 전략 구현
public class CreditCardPayment implements PaymentStrategy {
    private String cardNumber;
    private String cvv;

    public CreditCardPayment(String cardNumber, String cvv) {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
    }

    @Override
    public void pay(int amount) {
        System.out.println("신용카드로 " + amount + "원 결제");
        // 신용카드 결제 로직
    }
}

public class KakaoPayPayment implements PaymentStrategy {
    private String email;

    public KakaoPayPayment(String email) {
        this.email = email;
    }

    @Override
    public void pay(int amount) {
        System.out.println("카카오페이로 " + amount + "원 결제");
        // 카카오페이 결제 로직
    }
}

public class NaverPayPayment implements PaymentStrategy {
    private String phoneNumber;

    public NaverPayPayment(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public void pay(int amount) {
        System.out.println("네이버페이로 " + amount + "원 결제");
        // 네이버페이 결제 로직
    }
}

// 3. 컨텍스트 클래스
public class PaymentContext {
    private PaymentStrategy strategy;

    // 전략 설정 (의존성 주입)
    public void setPaymentStrategy(PaymentStrategy strategy) {
        this.strategy = strategy;
    }

    // 전략 실행
    public void executePayment(int amount) {
        if (strategy == null) {
            throw new IllegalStateException("결제 방법이 설정되지 않았습니다.");
        }
        strategy.pay(amount);
    }
}

// 4. 클라이언트 사용 예시
public class Main {
    public static void main(String[] args) {
        PaymentContext context = new PaymentContext();

        // 신용카드 결제
        context.setPaymentStrategy(new CreditCardPayment("1234-5678-9012-3456", "123"));
        context.executePayment(10000);

        // 카카오페이 결제
        context.setPaymentStrategy(new KakaoPayPayment("user@example.com"));
        context.executePayment(20000);

        // 네이버페이 결제
        context.setPaymentStrategy(new NaverPayPayment("010-1234-5678"));
        context.executePayment(30000);
    }
}
```

### 3-2. 실행 흐름
1. **전략 선택**: 클라이언트가 사용할 구체적인 전략 객체 생성
2. **전략 주입**: Context에 선택한 전략 객체를 설정
3. **전략 실행**: Context가 전략 인터페이스를 통해 알고리즘 실행
4. **전략 변경**: 런타임에 다른 전략으로 교체 가능

---

## 4. 백엔드 개발에서의 실전 활용

### 4-1. Spring Framework에서의 전략 패턴

#### 예시 1: 트랜잭션 전파 전략
```java
@Service
public class OrderService {

    // 다양한 트랜잭션 전파 전략
    @Transactional(propagation = Propagation.REQUIRED)
    public void createOrder() {
        // REQUIRED 전략: 기존 트랜잭션이 있으면 참여, 없으면 새로 생성
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createLog() {
        // REQUIRES_NEW 전략: 항상 새로운 트랜잭션 생성
    }
}
```

#### 예시 2: 캐싱 전략
```java
// 전략 인터페이스
public interface CacheStrategy {
    void put(String key, Object value);
    Object get(String key);
}

// Redis 캐싱 전략
@Component
public class RedisCacheStrategy implements CacheStrategy {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void put(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}

// 로컬 캐싱 전략
@Component
public class LocalCacheStrategy implements CacheStrategy {
    private final Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public Object get(String key) {
        return cache.get(key);
    }
}

// 컨텍스트
@Service
public class CacheService {
    private CacheStrategy cacheStrategy;

    @Autowired
    public void setCacheStrategy(@Qualifier("redisCacheStrategy") CacheStrategy strategy) {
        this.cacheStrategy = strategy;
    }

    public void cache(String key, Object value) {
        cacheStrategy.put(key, value);
    }

    public Object retrieve(String key) {
        return cacheStrategy.get(key);
    }
}
```

### 4-2. 데이터베이스 접근 전략

```java
// 전략 인터페이스
public interface DatabaseStrategy {
    List<User> findUsers();
}

// MySQL 전략
@Repository
public class MySQLStrategy implements DatabaseStrategy {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public List<User> findUsers() {
        return jdbcTemplate.query("SELECT * FROM users", new UserRowMapper());
    }
}

// MongoDB 전략
@Repository
public class MongoDBStrategy implements DatabaseStrategy {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<User> findUsers() {
        return mongoTemplate.findAll(User.class);
    }
}

// 컨텍스트
@Service
public class UserService {
    private DatabaseStrategy dbStrategy;

    public UserService(@Qualifier("mySQLStrategy") DatabaseStrategy strategy) {
        this.dbStrategy = strategy;
    }

    public List<User> getUsers() {
        return dbStrategy.findUsers();
    }
}
```

### 4-3. 파일 압축 전략

```java
// 전략 인터페이스
public interface CompressionStrategy {
    byte[] compress(byte[] data);
    byte[] decompress(byte[] compressedData);
}

// ZIP 압축 전략
public class ZipCompressionStrategy implements CompressionStrategy {
    @Override
    public byte[] compress(byte[] data) {
        // ZIP 압축 로직
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOS = new GZIPOutputStream(baos)) {
            gzipOS.write(data);
            gzipOS.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("ZIP 압축 실패", e);
        }
    }

    @Override
    public byte[] decompress(byte[] compressedData) {
        // ZIP 압축 해제 로직
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
             GZIPInputStream gzipIS = new GZIPInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIS.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("ZIP 압축 해제 실패", e);
        }
    }
}

// RAR 압축 전략
public class RarCompressionStrategy implements CompressionStrategy {
    @Override
    public byte[] compress(byte[] data) {
        // RAR 압축 로직
        return data; // 예시를 위한 단순 반환
    }

    @Override
    public byte[] decompress(byte[] compressedData) {
        // RAR 압축 해제 로직
        return compressedData; // 예시를 위한 단순 반환
    }
}

// 컨텍스트
public class FileCompressor {
    private CompressionStrategy strategy;

    public FileCompressor(CompressionStrategy strategy) {
        this.strategy = strategy;
    }

    public byte[] compressFile(byte[] fileData) {
        return strategy.compress(fileData);
    }

    public byte[] decompressFile(byte[] compressedData) {
        return strategy.decompress(compressedData);
    }
}
```

---

## 5. 전략 패턴 vs if-else 분기

### 5-1. if-else 방식 (안티패턴)

```java
public class PaymentService {
    public void processPayment(String type, int amount) {
        if (type.equals("credit_card")) {
            System.out.println("신용카드로 " + amount + "원 결제");
            // 신용카드 결제 로직
        } else if (type.equals("kakao_pay")) {
            System.out.println("카카오페이로 " + amount + "원 결제");
            // 카카오페이 결제 로직
        } else if (type.equals("naver_pay")) {
            System.out.println("네이버페이로 " + amount + "원 결제");
            // 네이버페이 결제 로직
        } else {
            throw new IllegalArgumentException("지원하지 않는 결제 방법");
        }
    }
}
```

**문제점**:
- 새로운 결제 방법 추가 시 기존 코드 수정 필요 (OCP 위반)
- 메서드 길이가 길어져 가독성 저하
- 테스트 코드 작성이 어려움
- 결제 로직 변경 시 전체 클래스에 영향

### 5-2. 전략 패턴 방식 (권장)

```java
// 위 3-1 예시 참조
```

**장점**:
- 새로운 전략 추가 시 기존 코드 수정 불필요 (OCP 준수)
- 각 전략이 독립적으로 테스트 가능
- 코드 가독성 향상
- 단일 책임 원칙(SRP) 준수

---

## 6. 전략 패턴의 장단점

### 6-1. 장점
1. **개방-폐쇄 원칙(OCP) 준수**: 새로운 전략 추가 시 기존 코드 수정 불필요
2. **단일 책임 원칙(SRP) 준수**: 각 전략이 하나의 알고리즘만 책임
3. **런타임 유연성**: 런타임에 전략을 동적으로 변경 가능
4. **테스트 용이성**: 각 전략을 독립적으로 단위 테스트 가능
5. **코드 재사용성**: 전략 객체를 다양한 컨텍스트에서 재사용 가능
6. **의존성 주입 활용**: Spring 등의 DI 컨테이너와 자연스럽게 통합

### 6-2. 단점
1. **클래스 수 증가**: 각 전략마다 별도의 클래스 필요
2. **복잡도 증가**: 단순한 경우 오히려 과도한 설계가 될 수 있음
3. **전략 선택 책임**: 클라이언트가 적절한 전략을 선택해야 함
4. **메모리 오버헤드**: 여러 전략 객체가 메모리에 존재

---

## 7. 실무 적용 시 고려사항

### 7-1. 전략 패턴을 사용해야 하는 경우
- 동일한 문제를 해결하는 여러 알고리즘이 존재할 때
- 알고리즘이 자주 변경되거나 추가될 가능성이 높을 때
- 복잡한 조건문(if-else, switch-case)을 제거하고 싶을 때
- 알고리즘의 세부 구현을 클라이언트로부터 숨기고 싶을 때

### 7-2. 전략 패턴을 사용하지 말아야 하는 경우
- 전략이 2개 이하로 적고, 변경 가능성이 거의 없을 때
- 알고리즘이 매우 단순할 때
- 성능이 중요하고 객체 생성 오버헤드가 부담될 때

### 7-3. Spring에서의 전략 패턴 구현 팁

```java
// 전략 팩토리 패턴 결합
@Component
public class PaymentStrategyFactory {
    private final Map<String, PaymentStrategy> strategies;

    @Autowired
    public PaymentStrategyFactory(List<PaymentStrategy> strategyList) {
        this.strategies = strategyList.stream()
            .collect(Collectors.toMap(
                strategy -> strategy.getClass().getSimpleName(),
                strategy -> strategy
            ));
    }

    public PaymentStrategy getStrategy(String type) {
        PaymentStrategy strategy = strategies.get(type + "Payment");
        if (strategy == null) {
            throw new IllegalArgumentException("지원하지 않는 결제 방법: " + type);
        }
        return strategy;
    }
}

// 사용
@Service
public class OrderService {
    @Autowired
    private PaymentStrategyFactory strategyFactory;

    public void processOrder(String paymentType, int amount) {
        PaymentStrategy strategy = strategyFactory.getStrategy(paymentType);
        strategy.pay(amount);
    }
}
```

---

## 8. 예상 면접 질문

### 8-1. 기본 개념 질문
1. 전략 패턴의 정의와 목적을 설명해주세요.
2. 전략 패턴의 구성 요소 3가지는 무엇인가요?
3. 전략 패턴과 if-else 분기의 차이점은 무엇인가요?

### 8-2. 실무 적용 질문
1. 실무에서 전략 패턴을 적용한 경험이 있나요? 어떤 상황이었나요?
2. Spring Framework에서 전략 패턴이 사용되는 사례를 아는 대로 설명해주세요.
3. 전략 패턴과 팩토리 패턴을 함께 사용하는 이유는 무엇인가요?

### 8-3. 심화 질문
1. 전략 패턴과 상태 패턴의 차이점은 무엇인가요?
2. 전략 패턴이 위배할 수 있는 SOLID 원칙은 무엇이고, 어떻게 해결할 수 있나요?
3. 전략 객체를 싱글톤으로 관리해도 될까요? 장단점은 무엇인가요?

**모범 답안 예시 (질문 3-3)**:
전략 객체를 싱글톤으로 관리할 수 있지만, 전략이 상태를 가지지 않는 경우에만 적합합니다. 장점은 메모리 효율성과 객체 생성 오버헤드 감소이며, 단점은 멀티스레드 환경에서 상태 관리가 필요한 경우 동시성 문제가 발생할 수 있습니다. Spring에서는 기본적으로 빈이 싱글톤이므로, stateless한 전략 패턴 구현에 적합합니다.

---

## 9. 핵심 요약

### 9-1. 주요 특징
- **알고리즘 캡슐화**: 각 알고리즘을 독립적인 클래스로 분리
- **런타임 교체 가능**: 실행 중 전략을 동적으로 변경 가능
- **SOLID 원칙 준수**: 특히 OCP와 SRP를 잘 따르는 패턴

### 9-2. 백엔드 개발자의 핵심 이해사항
- 전략 패턴은 복잡한 조건 분기를 객체 지향적으로 해결하는 방법이다
- Spring의 DI와 결합하면 더욱 강력한 설계가 가능하다
- 과도한 사용은 오히려 복잡도를 높일 수 있으므로 적절한 상황에 사용해야 한다

### 9-3. 실무 적용 포인트
- 결제 시스템, 파일 처리, 데이터 변환 등 다양한 알고리즘이 필요한 경우 활용
- 팩토리 패턴과 결합하여 전략 선택 로직을 더욱 깔끔하게 구현
- 테스트 주도 개발(TDD)과 함께 사용하면 각 전략을 독립적으로 테스트 가능
