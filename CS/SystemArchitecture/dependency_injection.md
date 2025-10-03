# 의존성 주입(Dependency Injection): 백엔드 개발자 관점에서의 이해

## 1. 한 줄 정의
- 의존성 주입(DI)은 객체가 필요로 하는 의존 객체를 외부에서 생성하여 전달하는 디자인 패턴으로, 객체 간 결합도를 낮추고 유연성과 테스트 용이성을 높이는 핵심 아키텍처 원칙이다.

---

## 2. 의존성 주입의 핵심 개념

### 2-1. 의존성이란?
- **개념**: 한 클래스가 다른 클래스의 기능을 사용할 때 "의존한다"고 표현
- **백엔드 관점**: 서비스 계층이 리포지토리 계층을 사용하거나, 컨트롤러가 서비스를 사용하는 관계
- **문제점**: 객체가 직접 의존 객체를 생성하면 결합도가 높아지고 테스트가 어려워짐

```java
// 나쁜 예: 직접 의존성 생성
public class UserService {
    private UserRepository repository = new UserRepositoryImpl(); // 강한 결합

    public User findUser(Long id) {
        return repository.findById(id);
    }
}
```

### 2-2. 의존성 주입의 원리
- **개념**: 객체가 필요한 의존 객체를 외부에서 주입받는 방식
- **백엔드 관점**: Spring Container가 객체를 생성하고 관리하며 필요한 곳에 주입
- **핵심 포인트**:
  - 객체는 자신이 사용할 의존 객체를 직접 생성하지 않음
  - 인터페이스에 의존하여 구현체 변경에 유연
  - 제어의 역전(IoC) 원칙 구현

```java
// 좋은 예: 의존성 주입
public class UserService {
    private final UserRepository repository; // 인터페이스에 의존

    // 생성자 주입
    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    public User findUser(Long id) {
        return repository.findById(id);
    }
}
```

---

## 3. 의존성 주입의 3가지 방식

### 3-1. 생성자 주입 (Constructor Injection) ⭐ 권장
- **개념**: 생성자를 통해 의존 객체를 주입받는 방식
- **장점**:
  - 불변성 보장 (final 키워드 사용 가능)
  - 필수 의존성 명확화
  - 순환 참조 컴파일 타임에 감지

```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    // Spring 4.3+ 에서는 @Autowired 생략 가능
    public OrderService(OrderRepository orderRepository,
                       PaymentService paymentService) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
    }
}
```

### 3-2. 세터 주입 (Setter Injection)
- **개념**: Setter 메서드를 통해 의존 객체를 주입받는 방식
- **사용 시기**: 선택적 의존성이나 변경 가능한 의존성

```java
@Service
public class NotificationService {
    private EmailSender emailSender;

    @Autowired
    public void setEmailSender(EmailSender emailSender) {
        this.emailSender = emailSender;
    }
}
```

### 3-3. 필드 주입 (Field Injection)
- **개념**: 필드에 직접 주입받는 방식
- **단점**: 테스트 어려움, 불변성 보장 불가
- **비권장**: 실무에서는 가급적 피하는 것이 좋음

```java
@Service
public class ProductService {
    @Autowired // 비권장 방식
    private ProductRepository productRepository;
}
```

---

## 4. Spring Framework에서의 의존성 주입

### 4-1. Spring Container의 역할
- **IoC Container**: 객체의 생성, 관리, 의존성 주입을 담당
- **Bean 관리**: @Component, @Service, @Repository 등으로 등록된 객체 관리
- **라이프사이클 관리**: 객체의 생성부터 소멸까지 관리

```java
@Configuration
public class AppConfig {
    @Bean
    public UserRepository userRepository() {
        return new UserRepositoryImpl();
    }

    @Bean
    public UserService userService() {
        return new UserService(userRepository()); // DI 수행
    }
}
```

### 4-2. 의존성 주입 자동화
- **@Autowired**: Spring이 자동으로 적절한 빈을 찾아 주입
- **타입 매칭**: 인터페이스 타입으로 구현체를 자동 매칭
- **@Qualifier**: 같은 타입의 빈이 여러 개일 때 특정 빈 지정

```java
@Service
public class PaymentService {
    private final PaymentGateway paymentGateway;

    // 타입으로 자동 주입
    public PaymentService(@Qualifier("tossPayments") PaymentGateway gateway) {
        this.paymentGateway = gateway;
    }
}
```

---

## 5. 의존성 주입의 실무 활용

### 5-1. 테스트 용이성
- **Mock 객체 주입**: 테스트 시 가짜 객체로 대체 가능
- **격리된 단위 테스트**: 외부 의존성 없이 독립적 테스트

```java
@Test
void testUserService() {
    // Mock 객체 생성
    UserRepository mockRepository = mock(UserRepository.class);
    when(mockRepository.findById(1L)).thenReturn(new User("test"));

    // Mock 주입하여 테스트
    UserService service = new UserService(mockRepository);
    User user = service.findUser(1L);

    assertEquals("test", user.getName());
}
```

### 5-2. 다형성과 전략 패턴
- **인터페이스 기반 설계**: 구현체 교체가 용이
- **런타임 전략 변경**: 환경에 따라 다른 구현체 주입

```java
public interface NotificationStrategy {
    void send(String message);
}

@Service
public class OrderService {
    private final NotificationStrategy notificationStrategy;

    // 환경에 따라 EmailNotification, SmsNotification 등 주입 가능
    public OrderService(NotificationStrategy notificationStrategy) {
        this.notificationStrategy = notificationStrategy;
    }
}
```

### 5-3. 설정 기반 의존성 관리
- **프로파일 기반 주입**: 개발/운영 환경별 다른 빈 주입
- **조건부 빈 등록**: 특정 조건에 따라 빈 활성화

```java
@Configuration
public class DataSourceConfig {
    @Bean
    @Profile("dev")
    public DataSource devDataSource() {
        return new H2DataSource();
    }

    @Bean
    @Profile("prod")
    public DataSource prodDataSource() {
        return new PostgreSQLDataSource();
    }
}
```

---

## 6. 의존성 주입의 주의사항

### 6-1. 순환 참조 문제
- **개념**: A가 B를 의존하고, B가 A를 의존하는 상황
- **해결방법**:
  - 설계 재검토 (대부분의 경우 설계 문제)
  - @Lazy 어노테이션 사용 (임시방편)
  - 중간 서비스 계층 도입

```java
// 순환 참조 예시
@Service
public class ServiceA {
    private final ServiceB serviceB; // ServiceB 의존
}

@Service
public class ServiceB {
    private final ServiceA serviceA; // ServiceA 의존 -> 순환 참조!
}
```

### 6-2. 과도한 의존성
- **문제**: 하나의 클래스가 너무 많은 의존성을 가짐
- **신호**: 생성자 파라미터가 5개 이상
- **해결**: 클래스 책임 분리, 파사드 패턴 적용

### 6-3. 의존성 방향
- **원칙**: 고수준 모듈은 저수준 모듈에 의존하지 않고, 추상화에 의존
- **레이어 구조**: Controller → Service → Repository 방향 유지
- **역방향 금지**: Repository가 Service를 의존하면 안 됨

---

## 7. 예상 면접 질문

### 7-1. 기본 개념 질문
1. 의존성 주입이란 무엇이고, 왜 사용하나요?
2. 생성자 주입과 필드 주입의 차이점은 무엇인가요?
3. 제어의 역전(IoC)과 의존성 주입의 관계를 설명해주세요.

### 7-2. 실무 질문
1. Spring에서 순환 참조 문제를 어떻게 해결하시겠나요?
2. 같은 인터페이스의 여러 구현체가 있을 때 어떻게 주입하나요?
3. 의존성 주입을 사용할 때 테스트는 어떻게 작성하나요?

### 7-3. 설계 질문
1. 의존성 주입 없이 코드를 작성할 때의 문제점은?
2. 생성자 주입을 권장하는 이유는 무엇인가요?
3. 의존성이 너무 많은 클래스는 어떻게 리팩토링하시겠나요?

---

## 8. 핵심 요약

### 8-1. 주요 이점
- **결합도 감소**: 객체 간 의존성을 느슨하게 유지
- **테스트 용이성**: Mock 객체로 쉽게 대체 가능
- **유연성 향상**: 구현체 교체가 자유로움
- **재사용성**: 다양한 컨텍스트에서 재사용 가능

### 8-2. 백엔드 개발자의 핵심 이해사항
- 의존성 주입은 SOLID 원칙 중 의존성 역전 원칙(DIP)의 구현 방법이다
- Spring과 같은 프레임워크는 DI Container를 통해 자동화된 의존성 관리를 제공한다
- 생성자 주입이 가장 안전하고 권장되는 방식이다

### 8-3. 실무 적용 포인트
- 인터페이스 기반으로 설계하여 구현체 교체에 유연하게 대응
- 순환 참조는 대부분 설계 문제이므로 아키텍처 재검토 필요
- 테스트 작성 시 의존성 주입을 활용한 Mock 기반 테스트 구현
