# CQRS 패턴: 백엔드 아키텍처의 명령과 조회 분리

## 1. 한 줄 정의
- CQRS(Command Query Responsibility Segregation)는 데이터 변경 작업(Command)과 데이터 조회 작업(Query)을 별도의 모델과 인터페이스로 분리하는 아키텍처 패턴이다. 백엔드 관점에서는 쓰기와 읽기의 성능 특성과 요구사항이 다름을 인정하고 각각을 최적화하는 설계 접근법이다.

---

## 2. CQRS 패턴 핵심 구성요소

### 2-1. Command Side (명령 측면)
- **개념**: 데이터를 변경하는 모든 작업 (Create, Update, Delete)
- **백엔드 관점**: 비즈니스 로직 검증, 트랜잭션 관리, 도메인 규칙 적용
- **핵심 포인트**:
  - 도메인 모델 중심의 설계
  - 강한 일관성(Strong Consistency) 보장
  - 복잡한 비즈니스 규칙 처리

```java
// Command 예시
public class CreateOrderCommand {
    private String customerId;
    private List<OrderItem> items;
    private PaymentInfo payment;

    public OrderCreatedEvent execute() {
        // 1. 비즈니스 규칙 검증
        validateCustomer();
        validateInventory();

        // 2. 도메인 객체 생성
        Order order = new Order(customerId, items);

        // 3. 이벤트 발행
        return new OrderCreatedEvent(order.getId());
    }
}
```

### 2-2. Query Side (조회 측면)
- **개념**: 데이터를 읽기만 하는 모든 작업 (Read)
- **백엔드 관점**: 성능 최적화, 캐싱, 다양한 뷰 모델 제공
- **핵심 포인트**:
  - 읽기 전용 모델 (Read Model)
  - 최종 일관성(Eventual Consistency) 허용
  - 다양한 프로젝션과 집계

```java
// Query 예시
public class OrderQueryService {

    public OrderSummaryView getOrderSummary(String customerId) {
        // 읽기 최적화된 뷰 모델 조회
        return orderReadRepository.findOrderSummary(customerId);
    }

    public List<OrderHistoryView> getOrderHistory(String customerId, Pagination page) {
        // 페이징 최적화된 이력 조회
        return orderHistoryRepository.findByCustomer(customerId, page);
    }
}
```

### 2-3. Event Store (이벤트 저장소)
- **개념**: Command와 Query 간 데이터 동기화를 위한 이벤트 중심 저장소
- **백엔드 관점**: 이벤트 소싱과 결합하여 모든 변경사항을 이벤트로 저장
- **핵심 포인트**:
  - 감사 가능성(Auditability) 제공
  - 재생 가능한 이벤트 스트림
  - 마이크로서비스 간 통신 매개체

```java
// Event Store 예시
@EventHandler
public class OrderProjectionHandler {

    @Autowired
    private OrderReadModelRepository readRepository;

    public void handle(OrderCreatedEvent event) {
        // Command Side 이벤트를 받아 Query Side 업데이트
        OrderSummaryView view = new OrderSummaryView(
            event.getOrderId(),
            event.getCustomerId(),
            event.getTotalAmount()
        );
        readRepository.save(view);
    }
}
```

---

## 3. 백엔드 아키텍처 관점에서의 CQRS 적용

### 3-1. 데이터베이스 분리 전략
- **Command Database**: 정규화된 관계형 DB (PostgreSQL, MySQL)
- **Query Database**: 비정규화된 NoSQL 또는 읽기 최적화 DB (MongoDB, Redis)
- **동기화 방식**: 이벤트 기반 비동기 복제

```yaml
# 데이터베이스 분리 예시 (Docker Compose)
services:
  command-db:
    image: postgresql:14
    environment:
      - POSTGRES_DB=orders_command

  query-db:
    image: mongodb:5
    environment:
      - MONGO_INITDB_DATABASE=orders_query

  event-store:
    image: confluentinc/cp-kafka:latest
```

### 3-2. API 설계 패턴
- **Command API**: POST/PUT/DELETE, 상태 변경 작업
- **Query API**: GET, 다양한 필터링과 정렬 옵션

```java
// REST API 설계 예시
@RestController
public class OrderController {

    // Command API
    @PostMapping("/orders")
    public ResponseEntity<Void> createOrder(@RequestBody CreateOrderRequest request) {
        CommandResult result = commandBus.send(new CreateOrderCommand(request));
        return ResponseEntity.accepted().build(); // 202 Accepted
    }

    // Query API
    @GetMapping("/orders/customer/{customerId}")
    public ResponseEntity<List<OrderView>> getCustomerOrders(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "0") int page) {

        List<OrderView> orders = queryService.getCustomerOrders(customerId, page);
        return ResponseEntity.ok(orders);
    }
}
```

### 3-3. 확장성과 성능 최적화
- **수직 확장**: Command와 Query 서버를 독립적으로 스케일링
- **수평 확장**: 읽기 복제본 추가, 샤딩 전략 적용
- **캐싱 전략**: Query Side에 다층 캐시 구조 적용

---

## 4. CQRS 적용 시나리오

### 4-1. 적합한 상황
- **복잡한 도메인**: 비즈니스 로직이 복잡하고 읽기/쓰기 요구사항이 다른 경우
- **성능 요구사항**: 읽기와 쓰기 성능을 독립적으로 최적화해야 하는 경우
- **확장성**: 마이크로서비스 아키텍처에서 서비스 간 결합도를 낮춰야 하는 경우

### 4-2. 부적합한 상황
- **단순한 CRUD**: 비즈니스 로직이 단순한 Create-Read-Update-Delete 작업
- **작은 규모**: 트래픽이 적고 성능 문제가 없는 애플리케이션
- **팀 역량**: 이벤트 소싱과 비동기 처리에 대한 이해가 부족한 경우

---

## 5. 구현 시 고려사항

### 5-1. 데이터 일관성
- **최종 일관성**: Query Side는 Command Side와 약간의 지연 발생
- **보상 트랜잭션**: 실패 시 롤백을 위한 보상 로직 구현
- **중복 처리**: 이벤트 중복 처리를 위한 멱등성 보장

```java
// 멱등성 보장 예시
@EventHandler
public class OrderProjectionHandler {

    public void handle(OrderCreatedEvent event) {
        // 이벤트 ID로 중복 처리 방지
        if (processedEvents.contains(event.getId())) {
            return;
        }

        // 프로젝션 업데이트
        updateOrderProjection(event);
        processedEvents.add(event.getId());
    }
}
```

### 5-2. 모니터링과 디버깅
- **이벤트 추적**: 각 이벤트의 처리 상태와 지연 시간 모니터링
- **데이터 검증**: Command Side와 Query Side 간 데이터 일관성 검증
- **성능 메트릭**: 읽기/쓰기 성능을 별도로 측정

### 5-3. 운영 복잡성
- **배포 전략**: Command와 Query 서비스의 독립적 배포
- **버전 관리**: 이벤트 스키마 버전 관리와 하위 호환성
- **장애 복구**: 이벤트 스토어 장애 시 복구 전략

---

## 6. 실제 사례와 도구

### 6-1. 기술 스택 예시
- **Command Side**: Spring Boot + JPA + PostgreSQL
- **Query Side**: Spring Boot + MongoDB + Redis
- **Event Store**: Apache Kafka + Event Store DB
- **API Gateway**: Spring Cloud Gateway

### 6-2. 주요 프레임워크
- **Axon Framework**: Java 기반 CQRS/Event Sourcing 프레임워크
- **EventStore**: 전용 이벤트 저장소
- **MediatR**: .NET 기반 메디에이터 패턴 라이브러리

---

## 7. 예상 면접 질문

### 7-1. 개념 질문
1. CQRS와 기존 CRUD 방식의 차이점과 각각의 장단점은?
2. Event Sourcing과 CQRS의 관계와 함께 사용하는 이유는?
3. CQRS에서 데이터 일관성 문제를 어떻게 해결하나요?

### 7-2. 설계 질문
1. 전자상거래 시스템에 CQRS를 적용한다면 어떻게 설계하시겠나요?
2. CQRS에서 Command와 Query 간 데이터 동기화 전략은?
3. 마이크로서비스 아키텍처에서 CQRS 적용 시 서비스 경계 설정 방법은?

### 7-3. 구현 질문
1. 이벤트 중복 처리를 방지하는 멱등성 구현 방법은?
2. CQRS 시스템의 성능 모니터링 지표와 개선 방법은?
3. Command Side에서 발생한 오류를 Query Side에서 어떻게 처리하나요?

---

## 8. 핵심 요약

### 8-1. 주요 특징
- **관심사 분리**: 읽기와 쓰기 로직의 명확한 분리
- **성능 최적화**: 각 작업에 특화된 모델과 저장소 사용
- **확장성**: 독립적인 스케일링과 기술 스택 선택

### 8-2. 백엔드 개발자의 핵심 이해사항
- CQRS는 복잡한 도메인과 높은 성능 요구사항이 있을 때 적용하는 패턴이다
- 이벤트 기반 아키텍처와 최종 일관성에 대한 이해가 필수이다
- 구현 복잡성과 운영 오버헤드를 고려한 신중한 적용이 중요하다

### 8-3. 실무 적용 포인트
- 도메인의 복잡성과 성능 요구사항을 기준으로 적용 여부 결정
- 팀의 기술적 역량과 운영 경험을 고려한 점진적 도입
- 모니터링과 관찰 가능성을 통한 지속적인 최적화