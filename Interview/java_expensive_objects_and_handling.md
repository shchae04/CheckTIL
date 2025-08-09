# Java에서 생성 비용이 높은 객체와 다루는 방법

면접에서 자주 묻는 질문: “Java에서 생성 비용이 높은 객체는 무엇이고, 어떻게 다뤄야 하나요?”에 대한 TIL 정리입니다. 생성 비용이 높은 객체를 올바르게 재사용/관리하면 성능, 안정성, 메모리 사용량을 크게 개선할 수 있습니다.

## 1. 생성 비용이 높은 객체의 공통 특징
- 무거운 초기화 과정(컴파일, 파싱, 내부 테이블 구축 등)이 필요함
- I/O 또는 OS/native 리소스(소켓, 파일, 핸들 등)를 확보해야 함
- 암호학적 초기화/난수 소스 접근 등 시스템 자원 사용
- 동기화 또는 락을 동반한 글로벌 상태 접근(프로바이더 조회 등)
- 대용량 메모리 할당(특히 Direct ByteBuffer) 또는 GC 부담을 유발

## 2. 대표적인 고비용 객체와 이유, 권장 사용법

### 2.1 데이터베이스/네트워크
- JDBC Connection
  - 이유: 소켓 연결/핸드셰이크/인증/트랜잭션 컨텍스트 등
  - 권장: 연결 풀(HikariCP 등) 사용. 매 요청 시 새 연결 생성 금지.
  - 참고: PreparedStatement 캐시 활용으로 추가 비용 절감

- HTTP 클라이언트 (Java 11 HttpClient, OkHttp, Apache HttpClient)
  - 이유: 커넥션 풀, TLS 설정, 스레드, 라우팅 테이블 등의 초기화
  - 권장: 애플리케이션 전역에서 클라이언트 인스턴스 재사용

### 2.2 스레드/실행 환경
- Thread, ExecutorService
  - 이유: 스레드 스택/스케줄러 등록 비용, 컨텍스트 스위치 부담
  - 권장: 스레드 풀(ExecutorService) 재사용. 대량 단발성 스레드 생성 금지
  - 참고: 가상 스레드(Java 21+)는 생성 비용이 낮지만, I/O 자원은 여전히 풀링/재사용 필요

### 2.3 직렬화/파싱/마샬링
- Jackson ObjectMapper, Gson, XML JAXBContext
  - 이유: 리플렉션/모듈 등록/타입 메타데이터 캐시 초기화
  - 권장: 불변 싱글턴으로 재사용. JAXB는 JAXBContext 재사용, Marshaller/Unmarshaller는 쓰레드세이프 아님 → 스레드별 생성/ThreadLocal 고려

- 정규식 Pattern
  - 이유: 정규식 컴파일 비용 높음
  - 권장: Pattern.compile로 미리 컴파일하여 재사용(캐시)

- 포맷터: DateTimeFormatter(불변, 스레드세이프), SimpleDateFormat(가변, 스레드안전하지 않음)
  - 권장: DateTimeFormatter를 static final로 재사용. SimpleDateFormat을 꼭 써야 하면 쓰레드 로컬 또는 매번 새로 생성

### 2.4 보안/암호화
- SSLContext/TrustManagerFactory/KeyManagerFactory, SSLEngine
  - 이유: 키스토어 로딩, 인증서 체인 검증, 네이티브 리소스 초기화
  - 권장: 컨텍스트는 애플리케이션 수명 동안 재사용

- SecureRandom
  - 이유: 엔트로피 수집/블로킹 가능(getInstanceStrong)
  - 권장: static 재사용. getInstanceStrong는 신중히 사용

- KeyFactory/SecretKeyFactory/KeyPairGenerator, MessageDigest/Mac/Cipher
  - 이유: 프로바이더 조회/네이티브 초기화 비용
  - 권장: Factory류는 재사용. Cipher/Mac/MessageDigest는 상태를 가짐 → 호출마다 새 인스턴스 생성 또는 ThreadLocal로 보관

### 2.5 메모리/수치
- BigInteger 큰 소수/키 생성
  - 이유: 난수/소수 판정 고가산
  - 권장: 결과 재사용, 가능한 경우 사전 생성/저장

- Direct ByteBuffer (NIO)
  - 이유: 네이티브 메모리 할당/해제 비용, GC 관리 외부
  - 권장: 버퍼 풀로 재사용, 빈번한 할당/해제 지양

### 2.6 프레임워크/ORM/리플렉션
- JPA EntityManagerFactory / Hibernate SessionFactory
  - 이유: 메타모델 스캔/매핑/캐시 초기화 등 무거움
  - 권장: 애플리케이션 전역 단일 인스턴스 재사용
  - 관련: [EntityManager 정리](./entity_manager.md)

- 리플렉션/프록시/Introspector/MethodHandles 설정
  - 이유: 멤버 탐색/접근성 변경/다이나믹 프록시 생성 비용
  - 권장: Method/Field/Constructor, MethodHandle 캐시. 프레임워크 캐시 활용

## 3. 다루는 전략(패턴)
- 풀링(Pooling)
  - 연결 풀(HikariCP), 스레드 풀(ExecutorService), 버퍼 풀(Direct ByteBuffer)
  - 주의: 일반 객체(String, DTO 등)는 풀링하지 말 것 → 오히려 복잡도/버그 증가

- 캐싱/싱글턴/재사용
  - ObjectMapper, HttpClient, DateTimeFormatter, 정규식 Pattern 등 불변/스레드세이프 객체는 전역 재사용

- 지연 초기화(Lazy Initialization)
  - 필요 시점에 생성. 홀더 패턴(Initialization-on-demand holder), DCL(Double-checked locking)

- ThreadLocal 활용
  - 스레드세이프하지 않지만 재사용 이점이 있는 객체(SimpleDateFormat, MessageDigest 등)에 한해 사용
  - 주의: 스레드 풀 환경에서 누수 방지를 위해 finally에서 remove 호출 습관화

- 배치/벡터화 처리
  - DB/네트워크 호출을 배치로 묶어 오브젝트/자원 생성 빈도를 낮춤

- 워밍업/사전 컴파일
  - 서버 기동 시 정규식 컴파일, 캐시 워밍, JIT 워밍(핫 경로 예열) 등

- 측정과 검증
  - JMH로 마이크로벤치마크, Flight Recorder/Async Profiler로 프로파일링
  - “조기 최적화” 경계: 병목이 맞는지 확인 후 최적화

## 4. 코드 스니펫 모음

### 4.1 정규식 Pattern 캐싱
```java
import java.util.concurrent.*;
import java.util.regex.Pattern;

public class RegexCache {
    private static final ConcurrentHashMap<String, Pattern> CACHE = new ConcurrentHashMap<>();
    public static Pattern get(String regex) {
        return CACHE.computeIfAbsent(regex, Pattern::compile);
    }
}
```

### 4.2 DateTimeFormatter 재사용
```java
import java.time.format.DateTimeFormatter;

public class Formats {
    public static final DateTimeFormatter ISO_INSTANT = DateTimeFormatter.ISO_INSTANT;
    public static final DateTimeFormatter KST_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");
}
```

### 4.3 Jackson ObjectMapper 싱글턴
```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Jsons {
    public static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
}
```

### 4.4 HTTP 클라이언트 재사용(Java 11+)
```java
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.time.Duration;

public class HttpClients {
    public static final HttpClient CLIENT = HttpClient.newBuilder()
            .version(Version.HTTP_2)
            .connectTimeout(Duration.ofSeconds(5))
            .build();
}
```

### 4.5 스레드 풀 재사용
```java
import java.util.concurrent.*;

public class ExecutorsHolder {
    public static final ExecutorService IO = Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors()));
    // 종료 시점에 IO.shutdown() 호출 필요
}
```

### 4.6 SecureRandom/MessageDigest ThreadLocal 예시
```java
import java.security.*;

public class Crypto {
    public static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final ThreadLocal<MessageDigest> SHA256 = ThreadLocal.withInitial(() -> {
        try { return MessageDigest.getInstance("SHA-256"); }
        catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    });

    public static byte[] sha256(byte[] input) {
        MessageDigest md = SHA256.get();
        try { return md.digest(input); }
        finally { SHA256.remove(); } // 풀에서 누수 방지: 상황에 따라 유지/제거 정책 선택
    }
}
```

### 4.7 HikariCP 설정(예시)
```java
// build.gradle 또는 pom.xml에 HikariCP 의존성 추가 후
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DataSources {
    public static HikariDataSource create() {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl("jdbc:mysql://localhost:3306/app");
        cfg.setUsername("user");
        cfg.setPassword("pass");
        cfg.setMaximumPoolSize(20);
        // MySQL: PreparedStatement 캐시
        cfg.addDataSourceProperty("cachePrepStmts", "true");
        cfg.addDataSourceProperty("prepStmtCacheSize", "250");
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        return new HikariDataSource(cfg);
    }
}
```

### 4.8 Lazy Holder 패턴
```java
public class HeavyService {
    private HeavyService() {}
    private static class Holder { static final HeavyService INSTANCE = new HeavyService(); }
    public static HeavyService getInstance() { return Holder.INSTANCE; }
}
```

### 4.9 Direct ByteBuffer 간단 풀
```java
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

public class BufferPool {
    private final ArrayBlockingQueue<ByteBuffer> pool;
    private final int capacity;

    public BufferPool(int poolSize, int capacity) {
        this.pool = new ArrayBlockingQueue<>(poolSize);
        this.capacity = capacity;
    }

    public ByteBuffer acquire() {
        ByteBuffer buf = pool.poll();
        return (buf != null) ? buf : ByteBuffer.allocateDirect(capacity);
    }

    public void release(ByteBuffer buf) {
        buf.clear();
        pool.offer(buf);
    }
}
```

## 5. 체크리스트
- 이 객체는 불변/스레드세이프인가? → 전역 재사용 가능 여부 확인
- 생성 시 I/O/네이티브/암호화 초기화/리플렉션이 동반되는가?
- 호출 빈도와 생명주기: 요청당 생성 vs 애플리케이션 수명 동안 재사용
- 풀/캐시 사용으로 이득이 있는가? 과도한 풀링의 부작용은 없는가?
- GC/메모리: 짧은 생명 객체가 과도하게 생성되어 GC 압박을 주지 않는가?
- 벤치마크/프로파일링으로 병목을 확인했는가?(JMH/JFR)

## 6. 주의사항
- ThreadLocal는 프레임워크/서버(스레드 풀) 환경에서 누수 위험 → 작업 종료 후 remove 습관화
- Cipher/Mac/MessageDigest는 상태ful. 멀티스레드 공유 금지, 필요한 경우 ThreadLocal
- SimpleDateFormat은 스레드세이프하지 않음 → DateTimeFormatter 권장
- 객체 풀은 연결/버퍼처럼 “비용이 매우 큰 리소스”에만. 일반 객체 풀링은 금지

## 7. 관련 TIL
- [캐싱 전략](./caching_strategies.md)
- [EntityManager 정리](./entity_manager.md)
- [Java 네트워크 IO 병목과 해결책](./java_network_io_bottleneck_solutions.md)

## 결론
- 고비용 객체의 핵심은 “생성하지 말고 재사용하라(가능하면)”.
- 연결/스레드/암호/파서/리플렉션 관련 객체는 풀링, 싱글턴, 캐싱, 지연 초기화로 관리합니다.
- 최적화는 항상 측정에 근거해 단계적으로 적용하세요.