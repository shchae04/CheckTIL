# 경쟁 상태(Race Condition) 해결을 위한 3가지 핵심 보장 요소

## 1. 한 줄 정의
- 경쟁 상태를 해결하려면 **상호 배제(Mutual Exclusion)**, **원자성(Atomicity)**, **가시성(Visibility)** 이 세 가지가 보장되어야 한다. 이는 여러 스레드가 공유 자원에 동시 접근할 때 데이터 일관성과 정확성을 유지하기 위한 필수 조건이다.

---

## 2. 경쟁 상태란?

### 2-1. 개념
- 두 개 이상의 스레드가 공유 자원에 동시에 접근하여 **실행 순서에 따라 결과가 달라지는 상황**
- 예측 불가능한 동작을 초래하며, 데이터 무결성을 훼손할 수 있음

### 2-2. 발생 예시
```java
public class Counter {
    private int count = 0;

    // 경쟁 상태 발생 가능
    public void increment() {
        count++;  // 1. count 읽기 -> 2. count + 1 계산 -> 3. count 쓰기
    }
}
```

**문제점**: `count++`는 실제로 3단계 연산이므로, 두 스레드가 동시에 실행하면 예상과 다른 결과 발생
- 스레드 A: count 읽기(0) → count + 1 계산(1)
- 스레드 B: count 읽기(0) → count + 1 계산(1)
- 스레드 A: count 쓰기(1)
- 스레드 B: count 쓰기(1)
- **결과**: 2번 증가했지만 최종 값은 1

---

## 3. 경쟁 상태 해결을 위한 3가지 보장 요소

### 3-1. 상호 배제(Mutual Exclusion)
#### 개념
- **한 번에 하나의 스레드만** 임계 영역(Critical Section)에 접근할 수 있도록 보장
- 다른 스레드는 해당 영역이 해제될 때까지 대기

#### 백엔드 관점
- 데이터베이스의 배타 락(Exclusive Lock)과 유사한 개념
- 공유 자원에 대한 접근 권한을 **직렬화(Serialization)** 하는 것

#### 구현 방법
```java
public class Counter {
    private int count = 0;

    // synchronized 키워드로 상호 배제 보장
    public synchronized void increment() {
        count++;
    }
}
```

```java
// 명시적 Lock 사용
public class Counter {
    private int count = 0;
    private final Lock lock = new ReentrantLock();

    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }
}
```

#### 주의사항
- **데드락(Deadlock)** 위험: 여러 락을 획득할 때 순환 대기 발생 가능
- **성능 저하**: 락 경합이 심하면 병목 현상 발생
- **공정성(Fairness)**: 락 획득 순서가 보장되지 않을 수 있음

---

### 3-2. 원자성(Atomicity)
#### 개념
- 연산이 **중단 없이 완전히 실행되거나, 전혀 실행되지 않음**을 보장
- "All or Nothing" 특성으로, 중간 상태가 다른 스레드에 노출되지 않음

#### 백엔드 관점
- 데이터베이스 트랜잭션의 원자성(ACID의 A)과 동일한 개념
- 복합 연산을 **단일 불가분 연산**으로 처리

#### 구현 방법
```java
import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
    private final AtomicInteger count = new AtomicInteger(0);

    // 원자적 증가 연산
    public void increment() {
        count.incrementAndGet();  // 읽기-수정-쓰기가 원자적으로 실행
    }

    public int getCount() {
        return count.get();
    }
}
```

```java
// CAS(Compare-And-Swap) 연산 사용
public class Counter {
    private final AtomicInteger count = new AtomicInteger(0);

    public void addIfLessThan(int value, int max) {
        int current;
        int next;
        do {
            current = count.get();
            next = current + value;
            if (next > max) {
                return;
            }
        } while (!count.compareAndSet(current, next));  // CAS 연산
    }
}
```

#### 원자 연산의 장점
- **락 프리(Lock-Free)**: 락을 사용하지 않아 데드락 위험 없음
- **높은 성능**: 하드웨어 수준의 CAS 명령어 활용으로 빠름
- **비차단(Non-blocking)**: 다른 스레드를 블로킹하지 않음

---

### 3-3. 가시성(Visibility)
#### 개념
- 한 스레드가 변경한 값이 **다른 스레드에게 즉시 보이도록** 보장
- CPU 캐시와 메인 메모리 간의 동기화 문제 해결

#### 백엔드 관점
- 분산 캐시 무효화(Cache Invalidation)와 유사한 개념
- 여러 CPU 코어 간의 메모리 일관성 유지

#### 문제 상황
```java
public class Task {
    private boolean running = true;

    // 스레드 A: running 상태 확인
    public void run() {
        while (running) {
            // 작업 수행
        }
    }

    // 스레드 B: running 상태 변경
    public void stop() {
        running = false;  // 변경이 스레드 A에 보이지 않을 수 있음
    }
}
```

**문제점**: 스레드 A는 `running`을 CPU 캐시에서 읽으므로, 스레드 B의 변경을 인지하지 못할 수 있음

#### 구현 방법
```java
// volatile 키워드로 가시성 보장
public class Task {
    private volatile boolean running = true;

    public void run() {
        while (running) {
            // 작업 수행
        }
    }

    public void stop() {
        running = false;  // 모든 스레드에게 즉시 가시
    }
}
```

```java
// synchronized 블록 사용 (상호 배제 + 가시성 모두 보장)
public class Task {
    private boolean running = true;

    public void run() {
        while (isRunning()) {
            // 작업 수행
        }
    }

    public synchronized boolean isRunning() {
        return running;
    }

    public synchronized void stop() {
        running = false;
    }
}
```

#### 메모리 모델 관점
- **Happens-Before 관계**: volatile 쓰기는 이후의 volatile 읽기보다 먼저 발생함을 보장
- **메모리 배리어(Memory Barrier)**: volatile 접근 시 CPU 캐시와 메인 메모리 동기화
- **명령어 재배치 방지**: 컴파일러와 CPU의 최적화로 인한 실행 순서 변경 방지

---

## 4. 3가지 요소의 종합 비교

| 보장 요소 | 목적 | 주요 수단 | 성능 영향 | 사용 시나리오 |
|---------|------|----------|---------|------------|
| **상호 배제** | 동시 접근 방지 | synchronized, Lock | 높음 (락 경합) | 복잡한 임계 영역 보호 |
| **원자성** | 불가분 연산 보장 | Atomic 클래스, CAS | 중간 (락 프리) | 단일 변수 업데이트 |
| **가시성** | 변경 즉시 반영 | volatile, synchronized | 낮음 (캐시 동기화) | 상태 플래그, 설정 값 |

---

## 5. 실무 적용 가이드

### 5-1. 적절한 동기화 수단 선택

#### Case 1: 단순 카운터 증가
```java
// 권장: AtomicInteger 사용 (원자성)
private final AtomicInteger counter = new AtomicInteger(0);

public void increment() {
    counter.incrementAndGet();
}
```

#### Case 2: 복합 연산 (잔액 확인 후 출금)
```java
// 권장: synchronized 또는 Lock 사용 (상호 배제 + 원자성)
private int balance = 1000;

public synchronized boolean withdraw(int amount) {
    if (balance >= amount) {
        balance -= amount;
        return true;
    }
    return false;
}
```

#### Case 3: 상태 플래그
```java
// 권장: volatile 사용 (가시성)
private volatile boolean initialized = false;

public void initialize() {
    // 초기화 로직
    initialized = true;
}

public boolean isInitialized() {
    return initialized;
}
```

### 5-2. 데이터베이스 레벨 동시성 제어

#### 비관적 락(Pessimistic Locking)
```java
// JPA에서 배타 락 사용 (상호 배제)
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Account a WHERE a.id = :id")
Account findByIdForUpdate(@Param("id") Long id);
```

#### 낙관적 락(Optimistic Locking)
```java
// @Version을 통한 원자성 보장
@Entity
public class Account {
    @Id
    private Long id;

    private int balance;

    @Version
    private Long version;  // CAS와 유사한 메커니즘
}
```

### 5-3. 분산 환경 동시성 제어

#### Redis 분산 락
```java
// Redisson을 통한 분산 상호 배제
RLock lock = redissonClient.getLock("account:" + accountId);

try {
    // 최대 10초 대기, 30초 후 자동 해제
    if (lock.tryLock(10, 30, TimeUnit.SECONDS)) {
        try {
            // 임계 영역
            processTransaction();
        } finally {
            lock.unlock();
        }
    }
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}
```

---

## 6. 성능 최적화 전략

### 6-1. 락의 범위 최소화
```java
// 나쁜 예: 전체 메서드 동기화
public synchronized void processLargeData(List<Data> dataList) {
    // 전처리 (동기화 불필요)
    List<Data> filtered = dataList.stream()
        .filter(d -> d.isValid())
        .collect(Collectors.toList());

    // 실제 공유 자원 접근
    sharedResource.addAll(filtered);
}

// 좋은 예: 필요한 부분만 동기화
public void processLargeData(List<Data> dataList) {
    // 전처리 (동기화 불필요)
    List<Data> filtered = dataList.stream()
        .filter(d -> d.isValid())
        .collect(Collectors.toList());

    // 실제 공유 자원 접근만 동기화
    synchronized(this) {
        sharedResource.addAll(filtered);
    }
}
```

### 6-2. 읽기-쓰기 락 분리
```java
// ReadWriteLock으로 읽기 성능 향상
public class Cache {
    private final Map<String, Object> map = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public Object get(String key) {
        lock.readLock().lock();  // 여러 스레드가 동시에 읽기 가능
        try {
            return map.get(key);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void put(String key, Object value) {
        lock.writeLock().lock();  // 쓰기는 배타적
        try {
            map.put(key, value);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
```

### 6-3. 락 프리 자료구조 활용
```java
// ConcurrentHashMap 사용 (내부적으로 세그먼트 단위 락 사용)
private final ConcurrentHashMap<String, User> userCache = new ConcurrentHashMap<>();

// 원자적 업데이트
userCache.compute("user123", (key, oldValue) -> {
    if (oldValue == null) {
        return fetchUserFromDB(key);
    }
    oldValue.incrementAccessCount();
    return oldValue;
});
```

---

## 7. 예상 면접 질문

### 7-1. 개념 질문
1. **경쟁 상태가 발생하는 근본적인 원인은 무엇인가요?**
   - 공유 자원에 대한 비원자적 연산과 CPU 캐시 불일치

2. **volatile과 synchronized의 차이점은 무엇인가요?**
   - volatile: 가시성만 보장, 원자성 보장 안 함
   - synchronized: 상호 배제 + 가시성 + 원자성 모두 보장

3. **CAS(Compare-And-Swap) 연산의 동작 원리를 설명해주세요.**
   - 현재 값과 예상 값을 비교하여 일치하면 새 값으로 업데이트
   - 하드웨어 수준에서 원자적으로 실행되며, 실패 시 재시도

### 7-2. 실무 질문
1. **분산 환경에서 동시성을 어떻게 제어하시겠나요?**
   - Redis 분산 락, 데이터베이스 비관적/낙관적 락
   - 이벤트 소싱과 CQRS 패턴 적용

2. **락 경합이 심한 상황에서 성능을 개선하려면?**
   - 락 범위 최소화, ReadWriteLock 사용
   - 락 프리 자료구조 활용, 샤딩으로 경합 분산

3. **데드락을 방지하는 방법은?**
   - 락 획득 순서 통일
   - 타임아웃 설정 (tryLock with timeout)
   - 락 순환 의존성 제거

### 7-3. 설계 질문
1. **좋아요 수 업데이트 기능의 동시성 제어를 어떻게 설계하시겠나요?**
   - AtomicInteger로 메모리 카운팅 후 주기적 DB 동기화
   - Redis Sorted Set으로 실시간 집계
   - 이벤트 큐를 통한 비동기 처리

2. **재고 관리 시스템에서 동시 구매 요청을 어떻게 처리하시겠나요?**
   - 데이터베이스 비관적 락 (SELECT FOR UPDATE)
   - Redis 분산 락 + 낙관적 락 조합
   - 메시지 큐를 통한 순차 처리

---

## 8. 핵심 요약

### 8-1. 3가지 보장 요소의 관계
- **상호 배제**: 동시 접근 자체를 막음 → 원자성과 가시성 자동 보장
- **원자성**: 연산의 불가분성 보장 → 상호 배제의 한 형태, 가시성은 별도 필요
- **가시성**: 변경 전파 보장 → 원자성이나 상호 배제 보장 안 함

### 8-2. 백엔드 개발자의 핵심 이해사항
- 경쟁 상태는 멀티스레드 환경에서 필연적으로 발생하는 문제
- 상황에 맞는 동기화 수단 선택이 성능과 안정성의 핵심
- 분산 환경에서는 단일 서버 동기화 메커니즘이 통하지 않음

### 8-3. 실무 적용 원칙
1. **최소 동기화 원칙**: 꼭 필요한 부분만 동기화
2. **성능 vs 안전성 균형**: 비즈니스 요구사항에 맞는 수준 선택
3. **테스트 철저히**: 동시성 버그는 재현이 어려움
4. **모니터링 필수**: 락 대기 시간, 경합률 추적

### 8-4. 동기화 수단 선택 가이드
- **단일 변수 업데이트**: Atomic 클래스
- **복합 연산**: synchronized 또는 Lock
- **상태 플래그**: volatile
- **읽기 중심 작업**: ReadWriteLock
- **분산 환경**: Redis 분산 락 또는 DB 락
