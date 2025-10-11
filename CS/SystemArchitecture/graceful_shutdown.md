# Graceful Shutdown의 필요성: 백엔드 개발자 관점에서의 5단계 이해

## 1. 한 줄 정의
- Graceful Shutdown은 서버가 종료될 때 현재 처리 중인 요청을 완료하고 새로운 요청을 거부하면서 안전하게 종료하는 프로세스이다. 백엔드 관점에서는 무중단 배포와 데이터 무결성을 보장하기 위한 필수적인 시스템 설계 패턴이다.

---

## 2. Graceful Shutdown의 필요성 5단계

### 2-1. 1단계: 요청 처리 중단 방지
- **개념**: 서버 종료 시 처리 중인 HTTP 요청, DB 트랜잭션이 중간에 끊기지 않도록 보장
- **백엔드 관점**: 진행 중인 스레드/프로세스가 작업을 완료할 때까지 대기
- **핵심 포인트**:
  - 갑작스러운 종료 시 클라이언트는 500 에러 또는 타임아웃 경험
  - 결제, 주문 등 중요한 트랜잭션이 절반만 처리될 위험
  - 사용자 경험 저하 및 데이터 불일치 발생 가능

```java
// Graceful Shutdown 없는 경우
public class Server {
    public void stop() {
        // 즉시 종료 - 처리 중인 요청 손실
        System.exit(0);
    }
}

// Graceful Shutdown 적용
public class Server {
    private final ExecutorService executor;

    public void gracefulShutdown() {
        executor.shutdown(); // 새 작업 거부
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // 타임아웃 후 강제 종료
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }
}
```

### 2-2. 2단계: 리소스 정리 및 연결 해제
- **개념**: 데이터베이스 커넥션, 파일 핸들, 소켓 연결 등을 안전하게 정리
- **백엔드 관점**: try-with-resources, cleanup hooks를 통한 리소스 관리
- **핵심 포인트**:
  - DB 커넥션 풀의 안전한 종료
  - 파일 시스템 flush 및 lock 해제
  - 메시지 큐 연결 종료 및 미처리 메시지 처리
  - 캐시 데이터 디스크 저장

```java
// Spring Boot의 Graceful Shutdown
@Component
public class DatabaseCleanup {
    @Autowired
    private DataSource dataSource;

    @PreDestroy
    public void cleanup() {
        // 커넥션 풀 정리
        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }
}

// Shutdown Hook 등록
Runtime.getRuntime().addShutdownHook(new Thread(() -> {
    log.info("Graceful shutdown initiated...");
    // 리소스 정리 로직
}));
```

### 2-3. 3단계: 로드밸런서와의 협력
- **개념**: 서버 종료 전 로드밸런서에게 트래픽 중단을 알림
- **백엔드 관점**: Health check endpoint를 이용한 트래픽 제어
- **핵심 포인트**:
  - Health check 실패 반환으로 새 요청 유입 차단
  - 로드밸런서가 해당 인스턴스 제외 (deregistration)
  - 전파 시간(propagation delay) 고려 필요 (보통 5-30초)

```java
// Health Check 상태 관리
@RestController
public class HealthController {
    private volatile boolean shutdownInitiated = false;

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        if (shutdownInitiated) {
            return ResponseEntity.status(503).body("Service Unavailable");
        }
        return ResponseEntity.ok("Healthy");
    }

    @PreDestroy
    public void initiateShutdown() {
        shutdownInitiated = true;
        // 로드밸런서가 감지할 시간 대기
        Thread.sleep(10000); // 10초 대기
    }
}
```

### 2-4. 4단계: 분산 시스템에서의 상태 동기화
- **개념**: 마이크로서비스 환경에서 다른 서비스들에게 종료를 알림
- **백엔드 관점**: 서비스 디스커버리(Eureka, Consul) 연동 및 이벤트 발행
- **핵심 포인트**:
  - 서비스 레지스트리에서 자신을 제거
  - 진행 중인 분산 트랜잭션 완료 또는 롤백
  - 메시지 큐의 in-flight 메시지 처리
  - 세션 데이터 외부 저장소로 이전

```java
// Kubernetes에서의 Graceful Shutdown
// deployment.yaml
spec:
  template:
    spec:
      terminationGracePeriodSeconds: 60  # 60초 대기
      containers:
      - name: app
        lifecycle:
          preStop:
            exec:
              command: ["/bin/sh", "-c", "sleep 15"]

// Spring Cloud에서 서비스 등록 해제
@PreDestroy
public void deregister() {
    eurekaClient.shutdown();  // Eureka에서 등록 해제
    // 다른 서비스들이 인지할 시간 확보
}
```

### 2-5. 5단계: 배포 전략과의 통합
- **개념**: Rolling update, Blue-Green 배포 시 무중단 서비스 보장
- **백엔드 관점**: 구버전과 신버전이 공존하는 시간 동안 안정성 유지
- **핵심 포인트**:
  - Zero-downtime 배포의 핵심 요소
  - 롤백 시나리오에서도 데이터 일관성 유지
  - 배포 자동화 파이프라인과 연계

```yaml
# Kubernetes Rolling Update 설정
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend-service
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxUnavailable: 0  # 항상 최소 3개 유지
      maxSurge: 1        # 최대 4개까지 동시 실행
  template:
    spec:
      terminationGracePeriodSeconds: 60
      containers:
      - name: app
        livenessProbe:
          httpGet:
            path: /health
            port: 8080
          initialDelaySeconds: 30
        readinessProbe:
          httpGet:
            path: /ready
            port: 8080
          periodSeconds: 5
```

---

## 3. 백엔드 개발자 관점에서의 시스템 특성

### 3-1. 타임아웃 관리의 중요성
- **Graceful 타임아웃**: 정상 종료 대기 시간 (30-60초 권장)
- **강제 종료 타임아웃**: Graceful 실패 시 강제 종료 시간
- **계층별 타임아웃 조정**: LB → Application → Database 순으로 증가

### 3-2. 상태 관리 패턴
- **Stateless 설계**: 종료/재시작이 쉬운 무상태 서버 구조
- **외부 세션 저장소**: Redis, Memcached를 통한 세션 영속화
- **Sticky Session 회피**: 특정 서버 의존성 제거

### 3-3. 모니터링 및 로깅
- **종료 이벤트 로깅**: 누가, 언제, 왜 종료했는지 기록
- **메트릭 수집**: 종료 소요 시간, 처리 완료된 요청 수
- **알림 설정**: 비정상 종료 시 즉시 알림

---

## 4. 실제 서비스 운영 시 고려사항

### 4-1. 프레임워크별 구현

#### Spring Boot (2.3+)
```yaml
# application.yml
server:
  shutdown: graceful
spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

```java
// 추가적인 커스터마이징
@Component
public class GracefulShutdownConfig {
    @Bean
    public TomcatServletWebServerFactory tomcatFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(connector -> {
            connector.setProperty("connectionTimeout", "30000");
        });
        return factory;
    }
}
```

#### Node.js (Express)
```javascript
const server = app.listen(3000);

// Graceful Shutdown 구현
process.on('SIGTERM', () => {
    console.log('SIGTERM signal received: closing HTTP server');

    server.close(() => {
        console.log('HTTP server closed');

        // DB 연결 종료
        mongoose.connection.close(false, () => {
            console.log('MongoDB connection closed');
            process.exit(0);
        });
    });

    // 타임아웃 설정 (30초)
    setTimeout(() => {
        console.error('Forcing shutdown');
        process.exit(1);
    }, 30000);
});
```

#### Go
```go
func main() {
    server := &http.Server{Addr: ":8080", Handler: router}

    // Graceful Shutdown
    go func() {
        if err := server.ListenAndServe(); err != nil && err != http.ErrServerClosed {
            log.Fatalf("listen: %s\n", err)
        }
    }()

    // 시그널 대기
    quit := make(chan os.Signal, 1)
    signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
    <-quit

    log.Println("Shutting down server...")

    // 30초 타임아웃
    ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
    defer cancel()

    if err := server.Shutdown(ctx); err != nil {
        log.Fatal("Server forced to shutdown:", err)
    }

    log.Println("Server exiting")
}
```

### 4-2. 컨테이너 환경에서의 주의사항
- **SIGTERM 시그널**: Kubernetes가 보내는 종료 신호 처리
- **terminationGracePeriodSeconds**: 기본 30초, 필요시 증가
- **preStop Hook**: 추가 정리 작업을 위한 훅
- **PID 1 문제**: 시그널을 받지 못하는 경우 tini, dumb-init 사용

### 4-3. 데이터베이스 트랜잭션 처리
```java
@Transactional
public class OrderService {
    @PreDestroy
    public void waitForActiveTransactions() {
        // 활성 트랜잭션 카운트 체크
        while (activeTransactions.get() > 0) {
            try {
                Thread.sleep(1000);
                log.info("Waiting for {} active transactions",
                         activeTransactions.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
```

---

## 5. 예상 면접 질문

### 5-1. 기본 개념 질문
1. Graceful Shutdown이 무엇이며 왜 필요한가요?
2. Graceful Shutdown과 강제 종료(SIGKILL)의 차이점은?
3. SIGTERM과 SIGKILL 시그널의 차이를 설명해주세요.

### 5-2. 실무 시나리오 질문
1. 배포 중에도 서비스 중단 없이 업데이트하려면 어떻게 해야 하나요?
2. Graceful Shutdown 타임아웃을 얼마로 설정해야 하나요?
3. 롤링 업데이트 중 구버전과 신버전이 공존할 때 주의할 점은?

### 5-3. 트러블슈팅 질문
1. Graceful Shutdown이 정상 작동하지 않는 원인과 해결 방법은?
2. 종료 시 일부 요청이 계속 손실된다면 어떻게 디버깅하시겠나요?
3. 데이터베이스 커넥션이 종료되지 않고 남아있다면 어떻게 처리하나요?

---

## 6. 핵심 요약

### 6-1. 주요 특징
- **Zero-Downtime**: 서비스 중단 없는 배포 가능
- **데이터 무결성**: 트랜잭션 완료 보장
- **리소스 안전성**: 메모리 누수, 커넥션 누수 방지

### 6-2. 백엔드 개발자의 핵심 이해사항
- Graceful Shutdown은 단순히 종료 프로세스가 아닌 서비스 안정성의 핵심 패턴이다
- 로드밸런서, 서비스 디스커버리, 컨테이너 오케스트레이터와의 협력이 필수이다
- 프레임워크별 기본 지원을 활용하되, 비즈니스 로직에 맞는 커스터마이징이 필요하다

### 6-3. 실무 적용 포인트
- 적절한 타임아웃 설정 (일반적으로 30-60초)
- 헬스체크 엔드포인트를 통한 트래픽 제어
- 시그널 핸들러를 통한 정리 작업 구현
- 모니터링과 로깅으로 종료 프로세스 추적

### 6-4. 체크리스트
```
✅ 새 요청 거부 메커니즘 구현
✅ 진행 중인 요청 완료 대기
✅ DB 커넥션, 파일 핸들 정리
✅ 로드밸런서 헬스체크 실패 반환
✅ 적절한 타임아웃 설정
✅ 시그널 핸들러 등록 (SIGTERM)
✅ 종료 이벤트 로깅
✅ 컨테이너 환경에서 PID 1 처리
```

---

## 7. 참고 자료

### 7-1. 관련 개념
- Rolling Update vs Blue-Green vs Canary 배포
- Service Mesh와 Circuit Breaker
- Health Check와 Readiness Probe
- Connection Pool 관리

### 7-2. 실제 장애 사례
- **사례 1**: Graceful Shutdown 미구현으로 배포 시마다 결제 실패 발생
- **사례 2**: 타임아웃 부족으로 배치 작업 중 강제 종료
- **사례 3**: 로드밸런서 전파 시간 미고려로 503 에러 발생

### 7-3. 모범 사례
- Netflix의 Chaos Engineering과 Graceful Degradation
- AWS ELB의 Connection Draining
- Kubernetes의 Pod Lifecycle과 Termination
