# 시스템 간 비동기 연동 방식

시스템 간 비동기 연동은 결합도를 낮추고, 호출된 시스템의 응답을 기다리지 않아 사용자 요청에 보다 빠르게 응답할 수 있는 장점이 있습니다. 대표적인 방식은 다음과 같습니다:

1. **별도 스레드로 실행하기**
2. **메시징 시스템 이용하기**
3. **트랜잭션 아웃박스 패턴**
4. **배치로 연동하기**
5. **CDC (Change Data Capture) 이용하기**

---

## 1. 별도 스레드로 실행하기

### 설명
- 메인 스레드에서 비즈니스 로직을 처리한 후, 외부 시스템과의 연동 작업을 별도의 스레드에서 비동기적으로 실행합니다.
- Java에서는 `ExecutorService`, `CompletableFuture`, `ThreadPoolTaskExecutor` 등을 활용할 수 있습니다.
- 사용자 요청에 대한 응답 시간을 단축시킬 수 있습니다.

### 구현 예시
```java
@Service
public class OrderService {
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Transactional
    public void processOrder(Order order) {
        // 주문 처리 로직 (동기)
        orderRepository.save(order);

        // 외부 시스템 연동 (비동기)
        executorService.submit(() -> {
            notificationService.sendOrderConfirmation(order);
        });
    }
}
```

### 고려 사항
- **예외 처리**: 별도 스레드에서 발생한 예외는 메인 스레드로 전파되지 않으므로, 적절한 예외 처리 및 로깅이 필요합니다.
- **리소스 관리**: 스레드 풀의 크기와 작업 큐 관리가 중요합니다. 너무 많은 스레드를 생성하면 시스템 리소스가 고갈될 수 있습니다.
- **트랜잭션 경계**: 별도 스레드에서는 원래 트랜잭션 컨텍스트가 유지되지 않으므로, 트랜잭션 관리에 주의해야 합니다.

---

## 2. 메시징 시스템 이용하기

### 설명
- 두 시스템 사이에 메시징 시스템(`Kafka`, `RabbitMQ` 등)을 두어 비동기 방식으로 연동합니다.
- 한 시스템에서 메시지를 생성하여 메시징 시스템에 송신하면, 다른 시스템에서 이를 읽어 처리합니다.
- 높은 처리량을 보장한다는 장점이 있습니다.

### 구현 예시
```java
@Service
public class OrderService {
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Autowired
    public OrderService(KafkaTemplate<String, OrderEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public void processOrder(Order order) {
        // 주문 처리 로직
        orderRepository.save(order);

        // 주문 이벤트 발행
        OrderEvent event = new OrderEvent(order.getId(), OrderStatus.CREATED);
        kafkaTemplate.send("order-events", event);
    }
}
```

### 고려 사항
- **메시지 유실**: 네트워크나 시스템 장애 시 메시지 손실 위험이 있으므로 주의해야 합니다.
- **메시지 소비 순서**: 메시지 순서를 보장해야 할 경우 추가적인 설계가 필요합니다.
- **트랜잭션 관리**:
    - 하나의 트랜잭션 내에서 메시지 전송과 데이터베이스 삽입이 함께 이루어질 때, 예를 들어 데이터베이스 삽입 실패 시 메시지가 전송되거나, 반대로 삽입은 성공했으나 메시지 전송에 실패하는 상황이 발생할 수 있습니다.
    - 이런 경우 두 작업을 원자적으로 처리할 수 있는 방법에 대해 추가 고민이 필요합니다.

---

## 3. 트랜잭션 아웃박스 패턴

### 설명
- 데이터베이스 트랜잭션과 메시지 발행을 원자적으로 처리하기 위한 패턴입니다.
- 메시지를 직접 메시징 시스템에 발행하는 대신, 로컬 데이터베이스의 'outbox' 테이블에 저장합니다.
- 별도의 프로세스가 outbox 테이블을 폴링하여 미처리된 메시지를 메시징 시스템에 발행합니다.
- 데이터 일관성을 보장하면서도 시스템 간 결합도를 낮출 수 있습니다.

### 구현 예시
```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final OutboxRepository outboxRepository;

    @Transactional
    public void processOrder(Order order) {
        // 주문 처리 로직
        orderRepository.save(order);

        // outbox 테이블에 메시지 저장
        OutboxMessage message = new OutboxMessage(
            UUID.randomUUID(),
            "OrderCreated",
            objectMapper.writeValueAsString(new OrderCreatedEvent(order.getId())),
            false
        );
        outboxRepository.save(message);
    }
}

// 별도 스케줄러로 실행되는 메시지 릴레이 서비스
@Service
public class OutboxRelayService {
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void processOutboxMessages() {
        List<OutboxMessage> messages = outboxRepository.findByProcessedFalse();
        for (OutboxMessage message : messages) {
            kafkaTemplate.send("order-events", message.getPayload());
            message.setProcessed(true);
            outboxRepository.save(message);
        }
    }
}
```

### 고려 사항
- **성능**: outbox 테이블 폴링 주기와 처리량을 적절히 조절해야 합니다.
- **중복 처리**: 메시지 릴레이 프로세스가 중복 실행될 경우를 대비한 멱등성 처리가 필요합니다.
- **메시지 순서**: 메시지 발행 순서가 중요한 경우 추가적인 설계가 필요합니다.

---

## 4. 배치로 연동하기

### 설명
- 실시간 처리가 필요하지 않은 경우, 일정 주기로 배치 작업을 실행하여 시스템 간 데이터를 동기화합니다.
- 대량의 데이터를 효율적으로 처리할 수 있으며, 시스템 부하를 분산시킬 수 있습니다.
- 주로 ETL(Extract, Transform, Load) 프로세스나 리포팅, 데이터 마이그레이션 등에 활용됩니다.

### 구현 예시
```java
@Component
public class OrderSyncBatchJob {
    private final OrderRepository orderRepository;
    private final ExternalSystemClient externalSystemClient;

    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시에 실행
    public void syncOrders() {
        LocalDateTime yesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime today = LocalDate.now().atStartOfDay();

        List<Order> newOrders = orderRepository.findByCreatedAtBetween(yesterday, today);

        // 배치 크기 설정
        int batchSize = 100;
        for (int i = 0; i < newOrders.size(); i += batchSize) {
            List<Order> batch = newOrders.subList(
                i, Math.min(i + batchSize, newOrders.size())
            );
            externalSystemClient.syncOrders(batch);
        }
    }
}
```

### 고려 사항
- **배치 주기**: 비즈니스 요구사항에 맞는 적절한 배치 실행 주기를 설정해야 합니다.
- **배치 크기**: 메모리 사용량과 처리 효율성을 고려하여 적절한 배치 크기를 결정해야 합니다.
- **실패 처리**: 배치 작업 중 실패한 항목에 대한 재시도 전략이 필요합니다.
- **모니터링**: 배치 작업의 성공/실패 여부와 처리 시간을 모니터링해야 합니다.

---

## 5. CDC (Change Data Capture) 이용하기

### 설명
- 데이터베이스의 변경 사항을 캡처하여 다른 시스템에 전파하는 방식입니다.
- 별도의 시스템이 데이터베이스의 바이너리 로그 등을 모니터링하여 변경 사항을 감지하고 전파합니다.
- 트랜잭션 보장이 가능하며, 별도의 메시지 생성이나 저장 로직이 필요 없어 애플리케이션 로직이 단순해집니다.

### 구현 예시
```yaml
# Debezium 설정 예시 (MySQL CDC)
connector.class=io.debezium.connector.mysql.MySqlConnector
database.hostname=mysql
database.port=3306
database.user=debezium
database.password=dbz
database.server.id=1
database.server.name=mysql-server
database.include.list=inventory
table.include.list=inventory.orders
```

### 고려 사항
- **추가 정보의 한계**: 변경 로그 자체에는 데이터 변경의 이유 등 부가 정보가 부족할 수 있어, 상황에 따라 추가적인 정보 확보가 필요할 수 있습니다.
- **제약 조건**: 변경 로그만으로는 충분한 정보를 제공하지 못할 경우, 활용에 제약이 발생할 수 있습니다.
- **데이터베이스 부하**: CDC는 데이터베이스에 추가적인 부하를 줄 수 있으므로, 성능 영향을 고려해야 합니다.

대표적인 도구 : `Debezium`, `Maxwell`, `AWS DMS`

---

각 방식의 장단점과 구현 시 고려사항을 면밀히 검토하여, 시스템의 요구사항에 가장 적합한 비동기 연동 방식을 선택하는 것이 중요합니다.
