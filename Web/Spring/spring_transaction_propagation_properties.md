# 스프링 트랜잭션 전파 속성 (Transaction Propagation Properties)

## 1. 한 줄 정의
- 스프링 트랜잭션 전파 속성은 메서드에 선언된 트랜잭션이 호출 시 상위 트랜잭션의 존재 여부에 따라 어떻게 동작할지를 결정하는 설정이다.

---

## 2. 트랜잭션 전파 속성 종류 및 특징

### 2-1. PROPAGATION_REQUIRED (기본값)
- **개념**: 상위 트랜잭션이 존재하면 해당 트랜잭션에 참여하고, 없으면 새로운 트랜잭션을 시작
- **백엔드 관점**: 가장 일반적으로 사용되는 전파 속성으로, 트랜잭션의 핵심 단위를 구성
- **핵심 포인트**:
  - 상위 트랜잭션이 있으면 해당 트랜잭션에 합류
  - 상위 트랜잭션이 없으면 새로운 트랜잭션 생성
  - 중첩된 호출 시 하나의 트랜잭션으로 통합

```java
// 기본값으로, 상위 트랜잭션에 참여하거나 새로운 트랜잭션 시작
@Transactional
public void methodA() {
    // 새로운 트랜잭션 시작 또는 상위 트랜잭션에 참여
    methodB();
}

@Transactional
public void methodB() {
    // methodA와 동일한 트랜잭션에 참여
}
```

### 2-2. PROPAGATION_SUPPORTS
- **개념**: 상위 트랜잭션이 존재하면 참여하고, 없으면 트랜잭션 없이 실행
- **백엔드 관점**: 트랜잭션이 필요 없는 작업에 사용, 선택적 트랜잭션 처리
- **핵심 포인트**:
  - 상위 트랜잭션이 있을 때만 트랜잭션 적용
  - 상위 트랜잭션이 없으면 트랜잭션 없이 실행
  - 데이터 일관성이 반드시 필요하지 않은 경우 사용

```java
// 상위 트랜잭션이 있으면 참여, 없으면 트랜잭션 없이 실행
@Transactional(propagation = Propagation.SUPPORTS)
public void methodC() {
    // 상위 트랜잭션이 있는 경우에만 트랜잭션 적용
}
```

### 2-3. PROPAGATION_MANDATORY
- **개념**: 상위 트랜잭션이 반드시 존재해야 하며, 없을 경우 예외 발생
- **백엔드 관점**: 강제적으로 상위 트랜잭션을 요구하는 경우 사용
- **핵심 포인트**:
  - 상위 트랜잭션이 없으면 예외(RuntimeException) 발생
  - 항상 트랜잭션 내에서 실행되어야 하는 메서드에 적합

```java
// 상위 트랜잭션이 반드시 존재해야 함
@Transactional(propagation = Propagation.MANDATORY)
public void methodD() {
    // 상위 트랜잭션이 없을 경우 예외 발생
}
```

### 2-4. PROPAGATION_REQUIRES_NEW
- **개념**: 항상 새로운 트랜잭션을 시작하며, 상위 트랜잭션이 있으면 일시 중지
- **백엔드 관점**: 독립적인 트랜잭션 단위를 구성해야 할 때 사용
- **핵심 포인트**:
  - 부모 트랜잭션과 독립적으로 동작
  - 자식 트랜잭션 실패는 부모 트랜잭션에 영향을 주지 않음
  - 로그 기록, 감사 로그와 같은 독립적인 작업에 적합

```java
// 항상 새로운 트랜잭션 시작
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void methodE() {
    // 부모 트랜잭션과 독립적인 새로운 트랜잭션
}

@Transactional
public void methodF() {
    methodE();  // methodE는 독립된 트랜잭션에서 실행
    // methodE 예외 발생 시 methodF에 영향 없음
}
```

### 2-5. PROPAGATION_NOT_SUPPORTED
- **개념**: 트랜잭션 없이 실행하며, 상위 트랜잭션이 있으면 일시 중지
- **백엔드 관점**: 트랜잭션이 필요 없는 작업에 사용
- **핵심 포인트**:
  - 트랜잭션 없이 실행되므로 성능 향상 가능
  - 데이터베이스 변경이 없는 읽기 전용 작업에 적합

```java
// 트랜잭션 없이 실행
@Transactional(propagation = Propagation.NOT_SUPPORTED)
public void methodG() {
    // 트랜잭션 없이 실행됨
}
```

### 2-6. PROPAGATION_NEVER
- **개념**: 절대 트랜잭션 내에서 실행되지 않도록 강제
- **백엔드 관점**: 트랜잭션 환경에서 절대 실행되지 않게 할 때 사용
- **핵심 포인트**:
  - 상위 트랜잭션이 있으면 예외 발생
  - 트랜잭션을 절대 허용하지 않는 작업에 사용

```java
// 트랜잭션 환경이면 예외 발생
@Transactional(propagation = Propagation.NEVER)
public void methodH() {
    // 트랜잭션 환경이면 예외 발생
}
```

### 2-7. PROPAGATION_NESTED
- **개념**: 중첩 트랜잭션 생성, 상위 트랜잭션 내에서 별도의 savepoint 생성
- **백엔드 관점**: 데이터베이스가 savepoint를 지원해야 사용 가능
- **핵심 포인트**:
  - 상위 트랜잭션의 일부분으로 동작
  - 중첩 트랜잭션 실패 시 savepoint로 롤백
  - 상위 트랜잭션 실패 시 전체 롤백

```java
// 중첩 트랜잭션 사용
@Transactional
public void methodI() {
    // 상위 트랜잭션
    methodJ();
}

@Transactional(propagation = Propagation.NESTED)
public void methodJ() {
    // 상위 트랜잭션의 savepoint 생성 후 실행
    // 이 메서드 내에서 예외 발생 시 savepoint로 롤백
}
```

---

## 3. 실제 사용 예시 및 시나리오

### 3-1. 주문 처리 시스템에서의 활용
- **PROPAGATION_REQUIRED**: 주문 생성, 재고 차감, 결제 처리 등 핵심 비즈니스 로직
- **PROPAGATION_REQUIRES_NEW**: 감사 로그 기록(주문 실패 시 로그는 남아야 함)
- **PROPAGATION_SUPPORTS**: 캐시 갱신, 통계 수집(트랜잭션 필요 없음)

```java
@Service
public class OrderService {
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Transactional
    public void processOrder(Order order) {
        // 주문 생성 - PROPAGATION_REQUIRED(기본값)
        createOrder(order);
        
        // 재고 차감 - 같은 트랜잭션
        reduceStock(order);
        
        try {
            // 결제 처리 - 같은 트랜잭션
            processPayment(order);
        } catch (PaymentException e) {
            // 전체 롤백됨
            throw e;
        }
        
        // 감사 로그 기록 - 독립적인 트랜잭션
        auditLogService.logOrderCreation(order);
    }
}

@Service
public class AuditLogService {
    
    // 주문 실패 시에도 로그는 남아야 하므로 독립된 트랜잭션 사용
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logOrderCreation(Order order) {
        // 감사 로그 기록
        auditRepository.save(new AuditLog(order.getId(), "ORDER_CREATED"));
    }
}
```

### 3-2. 트랜잭션 전파 속성 선택 기준
- **데이터 일관성 보장 여부**: 전체가 성공하거나 전체가 실패해야 할 경우 REQUIRED 사용
- **독립성 필요 여부**: 실패가 다른 트랜잭션에 영향을 주지 않아야 할 경우 REQUIRES_NEW 사용
- **성능 고려**: 트랜잭션 오버헤드 최소화 필요 시 SUPPORTS 또는 NOT_SUPPORTED 사용

---

## 4. 주의사항 및 Best Practice

### 4-1. 전파 속성 선택 시 고려사항
- **PROPAGATION_REQUIRES_NEW**: 데이터베이스 연결 풀 사용 증가로 인한 성능 저하 주의
- **PROPAGATION_NESTED**: 모든 데이터베이스가 savepoint를 지원하지 않음
- **예외 처리**: 롤백 여부가 전파 속성에 따라 달라지므로 명확한 예외 전략 필요

### 4-2. 성능 영향
- 트랜잭션 생성/파기 오버헤드 고려
- 데이터베이스 연결 풀 사용량 증가
- 동시성 제어 및 락킹 메커니즘 영향

### 4-3. 모니터링 포인트
- 트랜잭션 시작/종료 시간
- 롤백 발생 빈도
- 트랜잭션 전파로 인한 성능 이슈

---

## 5. 예상 면접 질문

### 5-1. 기술적 질문
1. PROPAGATION_REQUIRED와 PROPAGATION_REQUIRES_NEW의 차이점은 무엇인가요?
2. PROPAGATION_NESTED를 사용할 때의 제약사항은 무엇인가요?
3. 감사 로그를 남길 때 어떤 전파 속성을 사용해야 하나요?

### 5-2. 시나리오 기반 질문
1. 주문 처리 중 결제 실패 시 재고는 롤백되어야 하지만 로그는 남아야 하는 경우 어떻게 설계하시겠나요?
2. 트랜잭션 전파 속성 변경 시 고려해야 할 요소들을 설명해주세요.

---

## 6. 핵심 요약

### 6-1. 주요 특징
- **PROPAGATION_REQUIRED**: 기본값, 상위 트랜잭션에 참여하거나 새로운 트랜잭션 시작
- **PROPAGATION_REQUIRES_NEW**: 항상 새로운 트랜잭션 생성, 독립적 실행
- **PROPAGATION_NESTED**: 중첩 트랜잭션, savepoint 기반 롤백 가능

### 6-2. 백엔드 개발자의 핵심 이해사항
- 트랜잭션 전파 속성은 트랜잭션의 경계를 정의하는 중요한 설정
- 비즈니스 로직에 따라 적절한 전파 속성 선택이 필요
- 성능과 데이터 일관성 사이의 트레이드오프를 고려해야 함

### 6-3. 실무 적용 포인트
- 주요 비즈니스 로직은 PROPAGATION_REQUIRED 사용
- 감사 로그, 이력 기록 등은 PROPAGATION_REQUIRES_NEW 사용
- 선택적 트랜잭션 처리는 PROPAGATION_SUPPORTS 사용