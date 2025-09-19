# private 메서드에 @Transactional 선언 시 트랜잭션 동작 여부

## 1. 한 줄 정의
- private 메서드에 @Transactional을 선언해도 **트랜잭션이 동작하지 않는다**. Spring AOP 프록시는 외부에서 호출되는 public 메서드에만 적용되기 때문이다.

---

## 2. 왜 동작하지 않는가?

### 2-1. Spring AOP 프록시 메커니즘
- **프록시 생성**: Spring은 @Transactional이 붙은 클래스에 대해 프록시 객체를 생성
- **메서드 인터셉트**: 프록시는 메서드 호출을 가로채서 트랜잭션 로직을 추가
- **접근 제한**: 프록시는 **외부에서 호출되는 public 메서드만** 인터셉트 가능

```java
// Spring이 생성하는 프록시 개념도
public class UserServiceProxy extends UserService {
    private PlatformTransactionManager transactionManager;

    @Override
    public void publicMethod() {
        // 트랜잭션 시작
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            super.publicMethod(); // 실제 메서드 호출
            transactionManager.commit(status); // 커밋
        } catch (Exception e) {
            transactionManager.rollback(status); // 롤백
            throw e;
        }
    }

    // private 메서드는 프록시에서 오버라이드 불가!
    // private void privateMethod() { ... } // 컴파일 에러
}
```

### 2-2. 내부 호출 문제 (Self-Invocation)
```java
@Service
public class UserService {

    public void publicMethod() {
        // 내부 호출 - 프록시를 거치지 않음!
        privateTransactionalMethod(); // 트랜잭션 적용 안됨
    }

    @Transactional
    private void privateTransactionalMethod() {
        // 트랜잭션이 시작되지 않음
        userRepository.save(new User());
    }
}
```

---

## 3. 백엔드 개발자 관점에서의 기술적 분석

### 3-1. JDK Dynamic Proxy vs CGLIB Proxy
```java
// JDK Dynamic Proxy (인터페이스 기반)
public interface UserService {
    void processUser(); // public 메서드만 프록시 가능
}

// CGLIB Proxy (클래스 기반)
public class UserService {
    public void processUser() { } // public만 오버라이드 가능
    private void helper() { }     // private는 오버라이드 불가
    protected void internal() { } // protected도 프록시 적용 안됨 (내부 호출 시)
}
```

### 3-2. 바이트코드 레벨에서의 제약
- **Java 접근 제어**: private 메서드는 클래스 외부에서 접근 불가
- **프록시 한계**: 상속이나 인터페이스 구현으로는 private 메서드 제어 불가
- **AOP 적용 범위**: Spring AOP는 메서드 레벨 인터셉션만 지원

---

## 4. 해결 방법 및 대안

### 4-1. 메서드 접근 제어자 변경
```java
@Service
public class UserService {

    public void publicMethod() {
        internalTransactionalMethod(); // 외부에서 호출
    }

    // private → protected/public으로 변경
    @Transactional
    protected void internalTransactionalMethod() {
        userRepository.save(new User());
    }
}
```

### 4-2. Self-Injection 패턴
```java
@Service
public class UserService {

    @Autowired
    private UserService self; // 자기 자신 주입 (프록시 객체)

    public void publicMethod() {
        self.transactionalMethod(); // 프록시를 통한 호출
    }

    @Transactional
    public void transactionalMethod() {
        userRepository.save(new User());
    }
}
```

### 4-3. 별도 서비스 분리
```java
@Service
public class UserService {

    @Autowired
    private UserTransactionService userTransactionService;

    public void publicMethod() {
        userTransactionService.processUserTransaction(); // 외부 서비스 호출
    }
}

@Service
public class UserTransactionService {

    @Transactional
    public void processUserTransaction() {
        userRepository.save(new User());
    }
}
```

### 4-4. AspectJ 위빙 사용
```xml
<!-- AspectJ Load-Time Weaving 설정 -->
<context:load-time-weaver/>
<tx:annotation-driven mode="aspectj"/>
```

```java
// AspectJ는 private 메서드도 처리 가능 (컴파일/로드 타임 위빙)
@Service
public class UserService {

    @Transactional
    private void privateMethod() {
        // AspectJ 사용 시 트랜잭션 동작
        userRepository.save(new User());
    }
}
```

---

## 5. 실무에서의 모범 사례

### 5-1. 설계 원칙
- **단일 책임**: 트랜잭션 경계와 비즈니스 로직을 명확히 분리
- **명시적 경계**: 트랜잭션이 필요한 메서드는 public으로 노출
- **서비스 레이어**: 트랜잭션은 주로 서비스 레이어에서 관리

### 5-2. 안티패턴 피하기
```java
// ❌ 잘못된 패턴
@Service
public class UserService {

    public void processUsers(List<User> users) {
        for (User user : users) {
            processUser(user); // 내부 호출 - 각각 트랜잭션 생성 안됨
        }
    }

    @Transactional
    private void processUser(User user) {
        // 트랜잭션 적용 안됨
    }
}

// ✅ 올바른 패턴
@Service
public class UserService {

    @Transactional
    public void processUsers(List<User> users) {
        for (User user : users) {
            processUser(user); // 같은 트랜잭션 내에서 처리
        }
    }

    private void processUser(User user) {
        // 트랜잭션 컨텍스트 내에서 실행
    }
}
```

---

## 6. 면접 대비 핵심 포인트

### 6-1. 예상 질문과 답변
**Q: private 메서드에 @Transactional을 붙이면 어떻게 되나요?**
A: 트랜잭션이 적용되지 않습니다. Spring AOP 프록시는 외부에서 호출되는 public 메서드에만 적용되기 때문입니다.

**Q: 그럼 어떻게 해결해야 하나요?**
A: 1) 메서드를 public으로 변경, 2) 별도 서비스로 분리, 3) Self-injection 패턴 사용, 4) AspectJ 위빙 적용 등의 방법이 있습니다.

**Q: Self-injection의 문제점은 없나요?**
A: 순환 의존성으로 인한 코드 복잡도 증가와 의존성 그래프 이해의 어려움이 있어, 가능하면 서비스 분리를 권장합니다.

### 6-2. 깊이 있는 기술 질문
**Q: AspectJ와 Spring AOP의 차이점은?**
A: Spring AOP는 런타임 프록시 기반으로 public 메서드만 지원하지만, AspectJ는 컴파일/로드 타임 위빙으로 모든 접근 제어자를 지원합니다.

**Q: @Transactional의 propagation이 internal call에서 어떻게 동작하나요?**
A: 내부 호출에서는 프록시를 거치지 않으므로 propagation 설정이 무시되고, 기존 트랜잭션 컨텍스트를 그대로 사용합니다.

---

## 7. 핵심 요약

### 7-1. 기술적 원인
- Spring AOP 프록시는 public 메서드만 인터셉트 가능
- private 메서드는 클래스 외부에서 접근 불가능하므로 프록시 적용 불가
- 내부 호출 시 프록시를 거치지 않아 AOP 로직 실행 안됨

### 7-2. 실무 가이드라인
- 트랜잭션이 필요한 메서드는 명시적으로 public 선언
- 복잡한 비즈니스 로직은 서비스 레이어에서 적절히 분리
- Self-injection보다는 서비스 분리를 우선 고려
- AspectJ는 특별한 요구사항이 있을 때만 사용 검토

### 7-3. 아키텍처 관점
- 트랜잭션 경계 설계는 시스템 아키텍처의 핵심 요소
- 명확한 레이어 분리를 통한 관심사 분리 원칙 준수
- 프록시 기반 AOP의 한계를 이해하고 설계에 반영