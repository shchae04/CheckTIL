# 동기 방식 외부 서비스 호출 시 장애 대응 전략

## 개요

동기(Synchronous) 방식으로 외부 서비스를 호출할 때, 외부 서비스에 장애가 발생하면 호출하는 서비스까지 영향을 받을 수 있습니다.
이러한 장애 전파를 방지하고 시스템의 안정성을 유지하기 위한 패턴들이 있습니다..

## 주요 장애 대응 패턴

### 1. 타임아웃(Timeout) 설정

외부 서비스가 응답하지 않을 때 무한정 기다리지 않도록 타임아웃을 설정합니다.

**구현 방법:**
- 연결 타임아웃(Connection Timeout): 서버와의 연결을 맺는 데 걸리는 최대 시간 설정
- 읽기 타임아웃(Read Timeout): 데이터를 읽는 데 걸리는 최대 시간 설정
- 예외 처리를 통해 타임아웃 발생 시 대체 로직 실행

**장점:**
- 무한 대기 상태 방지
- 빠른 실패(Fast Fail) 처리 가능
- 사용자 경험 향상

**단점:**
- 타임아웃 값 설정이 어려움 (너무 짧으면 불필요한 오류, 너무 길면 효과 감소)

### 2. 서킷 브레이커(Circuit Breaker) 패턴

연속된 실패가 발생할 때 외부 서비스 호출을 일시적으로 차단하여 시스템 부하를 줄이는 패턴입니다.

**상태:**
- **Closed**: 정상 작동 상태, 모든 요청이 외부 서비스로 전달됨
- **Open**: 차단 상태, 요청이 즉시 실패하고 외부 서비스로 전달되지 않음
- **Half-Open**: 일부 요청만 외부 서비스로 전달하여 회복 여부를 확인

**구현 라이브러리:**
- Spring Cloud Circuit Breaker
- Resilience4j
- Hystrix (현재는 유지보수 종료)

**설정 파라미터:**
- 실패 임계값(Failure Threshold): 서킷이 열리는 실패율 또는 횟수
- 대기 시간(Wait Duration): 서킷이 열린 상태로 유지되는 시간
- 슬라이딩 윈도우(Sliding Window): 실패율을 계산하는 기준 요청 수

### 3. 재시도(Retry) 전략

일시적인 오류 발생 시 자동으로 재시도하는 전략입니다.

**구현 고려사항:**
- 최대 재시도 횟수 설정
- 재시도 간격 설정 (고정 간격 또는 지수 백오프)
- 재시도 대상 예외 유형 지정
- 모든 재시도 실패 후 복구 메커니즘 구현

**주의사항:**
- 멱등성(Idempotent) 보장 필요
- 과도한 재시도는 시스템 부하 가중
- 백엔드 서비스 복구 시간 고려

### 4. 폴백(Fallback) 메커니즘

외부 서비스 호출 실패 시 대체 응답을 제공하는 메커니즘입니다.

**폴백 전략 종류:**
1. **캐시된 데이터 반환**: 이전에 성공한 응답을 캐시하여 사용
2. **기본값 반환**: 미리 정의된 기본값 제공
3. **대체 서비스 호출**: 백업 서비스나 다른 데이터 소스 활용

**구현 방법:**
- 예외 처리 블록에서 대체 로직 실행
- 서킷 브레이커와 함께 사용하여 폴백 함수 정의
- 캐시 시스템 활용 (Redis, Caffeine 등)

### 5. 벌크헤드(Bulkhead) 패턴

외부 서비스 호출을 격리하여 장애가 전체 시스템으로 전파되는 것을 방지합니다.

**구현 방식:**
- **스레드 풀 격리**: 각 외부 서비스 호출에 전용 스레드 풀 할당
- **세마포어 격리**: 동시 호출 수 제한

**장점:**
- 한 서비스의 장애가 다른 서비스에 영향을 미치지 않음
- 리소스 사용량 제어 가능
- 중요 기능 보호

## 종합적인 접근 방식

실제 프로덕션 환경에서는 위의 패턴들을 조합하여 사용하는 것이 효과적입니다.

1. **타임아웃 설정**: 기본적인 방어책으로 모든 외부 호출에 적용
2. **서킷 브레이커 적용**: 연속된 실패 발생 시 차단하여 시스템 보호
3. **적절한 재시도 정책**: 일시적 오류에 대응하되, 과도한 재시도는 피함
4. **폴백 메커니즘 구현**: 모든 대응책이 실패해도 사용자에게 응답 제공
5. **벌크헤드로 격리**: 중요한 기능과 덜 중요한 기능 분리

**구현 예시 흐름:**
1. 요청 시작
2. 벌크헤드로 격리된 스레드 풀에서 실행
3. 서킷 브레이커 상태 확인 (열림 상태면 즉시 폴백)
4. 타임아웃 설정하여 외부 서비스 호출
5. 일시적 오류 발생 시 재시도
6. 모든 시도 실패 시 폴백 메커니즘 활성화

## 비동기 방식으로 전환 고려

동기 방식의 한계가 명확한 경우, 비동기 방식으로 전환하는 것도 고려할 수 있습니다.

**비동기 방식의 장점:**
- 스레드 블로킹 없음
- 더 효율적인 리소스 사용
- 더 높은 처리량 가능

**비동기 구현 방식:**
- 이벤트 기반 아키텍처
- 메시지 큐 활용 (Kafka, RabbitMQ 등)
- 리액티브 프로그래밍 (WebFlux, Project Reactor 등)

## 구현 예제

### Spring Boot에서 Resilience4j를 사용한 구현

Resilience4j는 Java 애플리케이션을 위한 경량 장애 허용 라이브러리로, 서킷 브레이커, 재시도, 타임아웃 등의 기능을 제공합니다.

#### 의존성 추가 (Maven)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-aop</artifactId>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot2</artifactId>
    <version>1.7.0</version>
</dependency>
```

#### application.yml 설정

```yaml
resilience4j:
  circuitbreaker:
    instances:
      externalServiceA:
        registerHealthIndicator: true
        slidingWindowSize: 10
        slidingWindowType: COUNT_BASED
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3
        automaticTransitionFromOpenToHalfOpenEnabled: true
  retry:
    instances:
      externalServiceA:
        maxAttempts: 3
        waitDuration: 1000
        retryExceptions:
          - java.io.IOException
          - java.net.ConnectException
  timelimiter:
    instances:
      externalServiceA:
        timeoutDuration: 2s
        cancelRunningFuture: true
  bulkhead:
    instances:
      externalServiceA:
        maxConcurrentCalls: 10
        maxWaitDuration: 10ms
```

#### 서비스 구현

```java
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExternalServiceClient {

    private final RestTemplate restTemplate;

    public ExternalServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @CircuitBreaker(name = "externalServiceA", fallbackMethod = "fallbackMethod")
    @Retry(name = "externalServiceA")
    @TimeLimiter(name = "externalServiceA")
    @Bulkhead(name = "externalServiceA")
    public String callExternalService() {
        return restTemplate.getForObject("https://external-service-url/api/resource", String.class);
    }

    public String fallbackMethod(Exception e) {
        // 로깅
        log.error("외부 서비스 호출 실패: {}", e.getMessage());

        // 캐시된 데이터 반환 또는 기본값 제공
        return "기본 응답 데이터";
    }
}
```

#### RestTemplate 타임아웃 설정

```java
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(3000); // 연결 타임아웃: 3초
        factory.setReadTimeout(5000);    // 읽기 타임아웃: 5초

        return new RestTemplate(factory);
    }
}
```

### WebClient를 사용한 비동기 구현 (Spring WebFlux)

WebClient는 Spring WebFlux의 비동기 HTTP 클라이언트로, 논블로킹 I/O를 지원합니다.

```java
@Service
public class AsyncExternalServiceClient {

    private final WebClient webClient;

    public AsyncExternalServiceClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
            .baseUrl("https://external-service-url")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();
    }

    public Mono<String> callExternalServiceAsync() {
        return webClient.get()
            .uri("/api/resource")
            .retrieve()
            .bodyToMono(String.class)
            .timeout(Duration.ofSeconds(3))  // 타임아웃 설정
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))  // 재시도 설정
            .onErrorResume(e -> {
                log.error("외부 서비스 호출 실패: {}", e.getMessage());
                return Mono.just("기본 응답 데이터");  // 폴백
            });
    }
}
```

## 결론

동기 방식으로 외부 서비스를 호출할 때는 다양한 장애 대응 전략을 적용하여 시스템의 안정성을 확보해야 합니다. 타임아웃, 서킷 브레이커, 재시도, 폴백, 벌크헤드 등의 패턴을 상황에 맞게 조합하여 사용하면 외부 서비스 장애가 내부 시스템에 미치는 영향을 최소화할 수 있습니다.

또한, 시스템의 요구사항과 특성에 따라 동기 방식과 비동기 방식을 적절히 선택하는 것도 중요합니다. 장애 대응뿐만 아니라 성능, 확장성, 유지보수성 등을 종합적으로 고려하여 최적의 아키텍처를 설계해야 합니다.
