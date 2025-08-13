# 트랜잭셔널 아웃박스(Transactional Outbox) 패턴이란?

> 데이터베이스 변경과 메시지 발행을 하나의 로컬 트랜잭션으로 안전하게 묶어, 이른바 "dual-write" 문제를 해결하는 패턴.

---

## 1. 왜 필요한가? (Dual-Write 문제)
애플리케이션에서 어떤 비즈니스 동작을 처리할 때 보통 두 가지를 동시에 하고 싶습니다.
- 로컬 데이터베이스에 상태 변경을 저장한다.
- 메시징 시스템(Kafka, RabbitMQ 등)에 이벤트를 발행한다.

이 두 작업을 순서대로(예: DB 저장 후 메시지 발행) 실행하면 다음과 같은 불일치가 발생할 수 있습니다.
- DB 저장은 성공했지만, 메시지 발행이 네트워크 장애로 실패 → 다른 시스템은 변경 사실을 모르고 있음
- 메시지 발행은 성공했지만, DB 저장이 롤백 → 다른 시스템은 변경 사실을 잘못 알게 됨

분산 트랜잭션(2PC)을 쓰면 원자성을 보장할 수 있지만, 복잡도와 비용이 큽니다. Outbox 패턴은 단일 데이터베이스 트랜잭션만으로 이 문제를 완화합니다.

---

## 2. 핵심 아이디어와 아키텍처
1) 비즈니스 데이터와 같은 DB 트랜잭션 안에서, 이벤트를 별도의 Outbox 테이블에 기록한다.
2) 커밋이 성공하면, 별도 릴레이(퍼블리셔) 프로세스가 Outbox에서 미발행 레코드를 읽어 메시징 시스템에 발행한다.
3) 발행이 성공하면 해당 Outbox 레코드를 발행됨으로 표시한다.

즉, “DB 상태 변경 + Outbox 레코드 생성”은 하나의 로컬 트랜잭션으로 원자적으로 커밋됩니다. 이후 비동기 릴레이가 메시지를 외부로 내보냅니다.

```
Client -> App (Tx) -> [DB: 비즈니스 테이블, Outbox 테이블]
                       ^ commit together
                       |
                 Relay/Publisher -> Message Broker (Kafka, etc.)
```

---

## 3. Outbox 테이블 스키마 예시
```sql
CREATE TABLE outbox_message (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  aggregate_type VARCHAR(100)   NOT NULL,
  aggregate_id   VARCHAR(100)   NOT NULL,
  event_type     VARCHAR(100)   NOT NULL,
  payload        JSON           NOT NULL,
  headers        JSON           NULL,
  status         VARCHAR(20)    NOT NULL DEFAULT 'READY', -- READY | PUBLISHED | FAILED
  retry_count    INT            NOT NULL DEFAULT 0,
  next_attempt_at TIMESTAMP     NULL,
  partition_key  VARCHAR(100)   NULL,                     -- 순서보장 키(옵션, 예: aggregate_id)
  sequence       BIGINT         NULL,                     -- per-aggregate 순서(옵션)
  occurred_at    TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  published_at   TIMESTAMP      NULL
);

CREATE INDEX idx_outbox_status_next_attempt ON outbox_message(status, next_attempt_at);
CREATE INDEX idx_outbox_partition_seq ON outbox_message(partition_key, sequence);
```

---

## 4. 구현 스케치 (Spring Boot + JPA)
### 4.1 도메인 처리 시 Outbox에 기록
```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createOrder(Order order) {
        orderRepository.save(order);
        OutboxMessage msg = OutboxMessage.builder()
            .aggregateType("Order")
            .aggregateId(order.getId().toString())
            .eventType("OrderCreated")
            .payload(toJson(new OrderCreatedEvent(order.getId())))
            .status("READY")
            .partitionKey(order.getId().toString())
            .occurredAt(Instant.now())
            .build();
        outboxRepository.save(msg);
    }

    private String toJson(Object o) {
        try { return objectMapper.writeValueAsString(o); } 
        catch (Exception e) { throw new RuntimeException(e); }
    }
}
```

### 4.2 릴레이(퍼블리셔) - 폴링 방식
```java
@Service
@RequiredArgsConstructor
public class OutboxRelay {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    // 여러 인스턴스에서 동시 실행돼도 안전하도록 배치 처리 + 행 잠금 전략 사용
    @Scheduled(fixedDelay = 1000)
    @Transactional
    public void publishBatch() {
        List<OutboxMessage> batch = outboxRepository.lockNextBatchForPublish(100);
        for (OutboxMessage m : batch) {
            try {
                kafkaTemplate.send("order-events", m.getPartitionKey(), m.getPayload()).get();
                m.markPublished();
            } catch (Exception ex) {
                m.markFailedWithBackoff();
            }
        }
    }
}
```

Repository 예시 (DB 방언에 맞춰 조정):
```java
public interface OutboxRepository extends JpaRepository<OutboxMessage, Long> {
    // 예: PostgreSQL
    @Query(value = """
        SELECT * FROM outbox_message
        WHERE status = 'READY' AND (next_attempt_at IS NULL OR next_attempt_at <= NOW())
        ORDER BY occurred_at
        FOR UPDATE SKIP LOCKED
        LIMIT :size
    """, nativeQuery = true)
    List<OutboxMessage> lockNextBatchForPublish(@Param("size") int size);
}
```

엔티티 내 멱등/백오프 도우미:
```java
public void markPublished() {
    this.status = "PUBLISHED";
    this.publishedAt = Timestamp.from(Instant.now());
}
public void markFailedWithBackoff() {
    this.status = "READY"; // 재시도 대상 유지
    this.retryCount++;
    long delaySec = (long) Math.min(300, Math.pow(2, retryCount));
    this.nextAttemptAt = Timestamp.from(Instant.now().plusSeconds(delaySec));
}
```

---

## 5. 전달 보장과 멱등성
- 전달 보장: 보통 Outbox 패턴은 at-least-once 전달을 목표로 합니다. 중복 발행이 있을 수 있으므로 소비자 또는 프로듀서 측 멱등 처리가 필요합니다.
- 프로듀서 측:
  - Kafka idempotent producer/transactional producer 사용(중복 최소화)
  - 동일 key로 파티셔닝하여 순서 보장(aggregate 단위)
- 소비자 측:
  - 멱등한 비즈니스 연산 설계(동일 이벤트 재처리 무해)
  - "Inbox"(deduplication) 테이블을 둬서 eventId 처리 이력 저장 후 중복 차단

Inbox 테이블 예시:
```sql
CREATE TABLE inbox_processed (
  event_id     VARCHAR(100) PRIMARY KEY,
  processed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

---

## 6. 폴링 vs CDC(Debezium) 변형
- 폴링 퍼블리셔
  - 장점: 애플리케이션만으로 구현 가능, 이해/운영이 단순
  - 단점: 폴링 지연, DB 부하(인덱싱/배치로 완화)
- CDC (Change Data Capture) 기반
  - 아이디어: Outbox 테이블에 기록하면 Debezium 같은 CDC가 binlog/WAL을 읽어 Kafka로 스트리밍
  - 장점: 지연이 매우 낮고, 애플리케이션 스케줄러 불필요
  - 단점: CDC 인프라 운영 필요, 스키마/권한/보안 고려 사항 추가

Debezium 설정 예시는 다음 글을 참고하세요: Interview/system_async_integration_method.md 의 "CDC 이용하기" 섹션.

---

## 7. 순서 보장 전략
- 같은 aggregate_id를 partition_key로 사용해 동일 파티션으로 라우팅
- Outbox에 sequence를 저장해 릴레이가 순서대로 발행
- 실패 시 백오프로 인해 순서 역전이 생기지 않도록 동일 키 묶음 단위 처리

---

## 8. 운영 고려사항
- 모니터링: READY 누적, FAILED 증가율, 발행 지연(latency), 재시도 횟수 알림
- 장애 대응: 영구 실패 건에 대한 DLQ(Dead Letter) 토픽 또는 별도 상태로 격리
- 정리(청소): PUBLISHED 레코드 TTL/아카이빙 배치
- 트랜잭션 크기: 너무 큰 payload는 외부 스토리지에 두고 참조만 저장 고려
- 인덱스: 상태/시간 기반 인덱스 필수, 파티션 키/시퀀스 인덱스 선택

---

## 9. 장단점 요약
- 장점
  - 분산 트랜잭션 없이 DB와 메시지 발행의 일관성 확보
  - 단순하고 프레임워크 독립적, 점진적 도입 용이
- 단점
  - 중복 가능성으로 인한 멱등 설계 필요
  - 추가 테이블/퍼블리셔/운영 컴포넌트 필요
  - 매우 높은 처리량 환경에선 DB 폴링이 병목이 될 수 있음(CDC 고려)

---

## 10. 대안과 함께 쓰는 패턴
- 2PC(분산 트랜잭션): 강한 일관성/복잡도↑
- SAGA/프로세스 오케스트레이션: 보상 트랜잭션 모델
- Inbox 패턴: 소비자 측 중복 방지
- Outbox + CDC 조합: 애플리케이션 단순화 + 낮은 지연

---

## 11. FAQ
- Q. Outbox만 쓰면 정확히 한 번(exactly-once) 보장이 되나요?
  - A. 일반적으로 at-least-once이며, 중복 가능성을 가정합니다. 정확히 한 번에 가까운 효과는 멱등성과 파티셔닝, 브로커 기능(예: Kafka EOS)을 조합해 달성합니다.
- Q. 트랜잭션은 왜 하나로 끝나나요?
  - A. 비즈니스 데이터와 Outbox 레코드를 같은 DB 트랜잭션으로 커밋하기 때문입니다. 외부 브로커 발행은 별도 비동기 단계에서 처리됩니다.
- Q. CDC만 쓰면 Outbox 테이블이 꼭 필요한가요?
  - A. 일반적으로 Outbox 테이블을 별도로 두는 것이 안전합니다. 도메인 테이블 변경만으로 이벤트를 만들면, 필요한 컨텍스트/스키마를 잃거나 모델 변경에 민감해집니다.

---

## 12. 함께 읽기
- Interview/system_async_integration_method.md의 "트랜잭션 아웃박스 패턴" 섹션
- Kafka 멱등 프로듀서/트랜잭션 프로듀서 개념
- Debezium 공식 문서 (CDC)
