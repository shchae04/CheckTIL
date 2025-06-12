# CDC (Change Data Capture)

CDC(Change Data Capture)는 데이터베이스에서 변경된 데이터를 식별하고 추적하는 기술입니다. 이 문서에서는 CDC의 개념, 구현 방법, 그리고 활용 사례에 대해 설명합니다.

## 1. CDC란?

CDC(Change Data Capture)는 데이터베이스에서 발생한 변경 사항(삽입, 수정, 삭제)을 식별하고 캡처하여 다른 시스템이나 프로세스에서 활용할 수 있도록 하는 기술입니다. 주요 특징은 다음과 같습니다:

- **실시간 데이터 동기화**: 소스 데이터베이스의 변경 사항을 실시간으로 추적
- **낮은 오버헤드**: 데이터베이스 성능에 미치는 영향 최소화
- **데이터 일관성**: 변경 사항의 순서와 트랜잭션 경계 유지
- **다양한 활용**: 데이터 복제, ETL, 이벤트 기반 아키텍처 등에 활용

### 1.1 CDC vs 전통적인 데이터 동기화 방법

| 특성 | CDC | 배치 처리 | 트리거 기반 |
|------|-----|----------|------------|
| 지연 시간 | 낮음(실시간/준실시간) | 높음(주기적) | 중간 |
| 리소스 사용 | 중간 | 높음 | 높음 |
| 구현 복잡성 | 중간 | 낮음 | 높음 |
| 확장성 | 높음 | 중간 | 낮음 |
| 데이터베이스 부하 | 낮음 | 높음 | 높음 |

## 2. CDC 구현 방법

CDC를 구현하는 방법은 여러 가지가 있으며, 각각 장단점이 있습니다:

### 2.1 로그 기반 CDC (Log-based CDC)

데이터베이스의 트랜잭션 로그(WAL, Redo 로그 등)를 직접 읽어 변경 사항을 캡처합니다.

**장점**:
- 데이터베이스 성능에 미치는 영향 최소화
- 모든 변경 사항 캡처 가능
- 트랜잭션 순서 보존

**단점**:
- 데이터베이스 벤더별로 로그 형식이 다름
- 로그 접근 권한 필요
- 구현 복잡성

**주요 도구**:
- **Debezium**: MySQL, PostgreSQL, MongoDB 등 다양한 데이터베이스 지원
- **AWS DMS**: AWS 서비스 간 데이터 마이그레이션 및 복제
- **Oracle GoldenGate**: Oracle 및 이기종 데이터베이스 간 복제

### 2.2 쿼리 기반 CDC (Query-based CDC)

주기적으로 데이터베이스를 쿼리하여 변경된 레코드를 식별합니다.

**장점**:
- 구현 간단
- 데이터베이스 로그 접근 불필요
- 다양한 데이터베이스 지원

**단점**:
- 데이터베이스 부하 증가
- 변경 사항 누락 가능성
- 실시간성 제한

**구현 방법**:
```sql
-- 타임스탬프 기반 쿼리 예시
SELECT * FROM orders 
WHERE last_updated_at > :last_capture_time
ORDER BY last_updated_at;
```

### 2.3 트리거 기반 CDC (Trigger-based CDC)

데이터베이스 트리거를 사용하여 변경 사항을 별도 테이블에 기록합니다.

**장점**:
- 모든 데이터베이스에서 구현 가능
- 로그 접근 권한 불필요
- 변경 사항 즉시 캡처

**단점**:
- 데이터베이스 성능 저하
- 트리거 관리 복잡성
- 확장성 제한

**구현 예시**:
```sql
CREATE TRIGGER orders_after_update
AFTER UPDATE ON orders
FOR EACH ROW
BEGIN
    INSERT INTO orders_changelog
    (order_id, field_name, old_value, new_value, changed_at)
    VALUES
    (NEW.id, 'status', OLD.status, NEW.status, NOW());
END;
```

## 3. CDC 활용 사례

CDC는 다양한 시나리오에서 활용됩니다:

### 3.1 데이터 복제 및 동기화

- **데이터 웨어하우스 ETL**: 운영 데이터베이스의 변경 사항을 데이터 웨어하우스로 실시간 전송
- **마이크로서비스 간 데이터 동기화**: 서비스 간 데이터 일관성 유지
- **재해 복구**: 백업 시스템으로 실시간 데이터 복제

### 3.2 이벤트 기반 아키텍처

- **이벤트 소싱**: 데이터 변경을 이벤트로 발행하여 다른 시스템에서 소비
- **CQRS 패턴**: 명령과 쿼리 책임 분리를 위한 데이터 동기화
- **실시간 분석**: 변경 데이터를 스트리밍 분석 플랫폼으로 전송

### 3.3 캐시 무효화

- **분산 캐시 업데이트**: 데이터 변경 시 관련 캐시 자동 갱신
- **검색 인덱스 갱신**: 데이터베이스 변경 사항을 검색 엔진에 실시간 반영

## 4. CDC 구현 예시

### 4.1 Debezium을 활용한 MySQL CDC 구현

Debezium은 Apache Kafka와 통합되어 로그 기반 CDC를 제공하는 오픈소스 플랫폼입니다.

**아키텍처**:
```
MySQL → Debezium Connector → Kafka → Kafka Connect → 타겟 시스템
```

**설정 예시**:
```json
{
  "name": "mysql-connector",
  "config": {
    "connector.class": "io.debezium.connector.mysql.MySqlConnector",
    "database.hostname": "mysql-server",
    "database.port": "3306",
    "database.user": "debezium",
    "database.password": "dbz",
    "database.server.id": "1",
    "database.server.name": "mysql-server-1",
    "database.include.list": "inventory",
    "table.include.list": "inventory.customers",
    "database.history.kafka.bootstrap.servers": "kafka:9092",
    "database.history.kafka.topic": "schema-changes.inventory"
  }
}
```

### 4.2 Spring Boot에서 CDC 이벤트 처리

Spring Boot 애플리케이션에서 Debezium 이벤트를 처리하는 예시:

```java
@Service
public class OrderChangeEventHandler {

    private final KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    public OrderChangeEventHandler(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    @KafkaListener(topics = "mysql-server-1.inventory.orders")
    public void handleOrderChange(ConsumerRecord<String, String> record) {
        try {
            JsonNode eventNode = new ObjectMapper().readTree(record.value());
            JsonNode payload = eventNode.get("payload");
            
            String operation = payload.get("op").asText();
            JsonNode after = payload.get("after");
            
            if ("c".equals(operation)) {  // 생성 이벤트
                processNewOrder(after);
            } else if ("u".equals(operation)) {  // 수정 이벤트
                processOrderUpdate(payload.get("before"), after);
            }
            
        } catch (Exception e) {
            log.error("Error processing order change event", e);
        }
    }
    
    private void processNewOrder(JsonNode orderData) {
        // 새 주문 처리 로직
        String orderId = orderData.get("id").asText();
        log.info("New order created: {}", orderId);
        
        // 다른 시스템에 알림
        kafkaTemplate.send("order-notifications", 
                           orderId, 
                           "New order: " + orderData.toString());
    }
    
    private void processOrderUpdate(JsonNode before, JsonNode after) {
        // 주문 상태 변경 처리
        String orderId = after.get("id").asText();
        String oldStatus = before.get("status").asText();
        String newStatus = after.get("status").asText();
        
        if (!oldStatus.equals(newStatus)) {
            log.info("Order {} status changed: {} -> {}", 
                     orderId, oldStatus, newStatus);
            
            // 상태 변경에 따른 후속 처리
            if ("SHIPPED".equals(newStatus)) {
                kafkaTemplate.send("shipping-notifications", 
                                  orderId, 
                                  "Order shipped: " + orderId);
            }
        }
    }
}
```

## 5. CDC 구현 시 고려사항

CDC를 구현할 때 고려해야 할 주요 사항들:

### 5.1 성능 영향

- **모니터링**: CDC 프로세스가 소스 데이터베이스에 미치는 영향 모니터링
- **배치 처리**: 변경 이벤트를 배치로 처리하여 대상 시스템 부하 분산
- **리소스 할당**: CDC 프로세스에 충분한 리소스 할당

### 5.2 장애 처리

- **재시작 메커니즘**: CDC 프로세스 장애 시 마지막 처리 지점부터 재개
- **중복 처리 방지**: 이벤트 중복 처리 방지를 위한 멱등성 보장
- **데이터 일관성**: 장애 복구 후 데이터 일관성 검증

### 5.3 보안

- **데이터 암호화**: 민감한 데이터 전송 시 암호화
- **접근 제어**: CDC 프로세스의 데이터베이스 접근 권한 최소화
- **감사**: 데이터 변경 및 접근에 대한 감사 로그 유지

## 6. 결론

CDC는 데이터베이스 변경 사항을 효율적으로 캡처하고 활용할 수 있는 강력한 기술입니다. 실시간 데이터 동기화, 이벤트 기반 아키텍처, 마이크로서비스 간 데이터 일관성 유지 등 다양한 시나리오에서 활용될 수 있습니다. 구현 방법에 따라 장단점이 있으므로, 시스템 요구사항과 제약 조건을 고려하여 적절한 CDC 접근 방식을 선택해야 합니다.

## 참고 자료

- [Debezium 공식 문서](https://debezium.io/documentation/)
- [Apache Kafka Connect](https://kafka.apache.org/documentation/#connect)
- [Martin Kleppmann, "Designing Data-Intensive Applications"](https://dataintensive.net/)
- [AWS Database Migration Service](https://aws.amazon.com/dms/)