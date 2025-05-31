# Java Executors와 ThreadPool 사용법

Java의 Executors 프레임워크는 Java 5(Java SE 5.0)에서 도입된 고수준 동시성 API로, 스레드 생성과 관리를 추상화하여 개발자가 비즈니스 로직에 집중할 수 있게 해줍니다. 특히 `newFixedThreadPool`은 고정된 크기의 스레드 풀을 생성하는 유용한 메서드입니다.

## 1. Executors 프레임워크 개요

Executors 프레임워크는 다음과 같은 주요 구성 요소로 이루어져 있습니다:

- **Executor**: 작업 제출과 실행을 분리하는 인터페이스
- **ExecutorService**: Executor를 확장한 인터페이스로, 작업 관리 및 종료 기능 제공
- **ThreadPool**: 스레드를 재사용하여 여러 작업을 효율적으로 처리하는 메커니즘
- **Callable과 Future**: 결과를 반환하는 작업과 그 결과를 나타내는 객체

## 2. ThreadPool의 장점

스레드 풀을 사용하면 다음과 같은 이점이 있습니다:

1. **성능 향상**: 스레드 생성 및 소멸 비용 감소
2. **자원 관리**: 동시에 실행되는 스레드 수 제한
3. **안정성**: 갑작스러운 부하 증가에도 안정적인 동작
4. **코드 단순화**: 스레드 생성 및 관리 로직 분리

## 3. Executors.newFixedThreadPool 사용법

`newFixedThreadPool`은 고정된 수의 스레드를 가진 스레드 풀을 생성합니다.

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FixedThreadPoolExample {
    public static void main(String[] args) {
        // 4개의 스레드를 가진 고정 크기 스레드 풀 생성
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        // 10개의 작업 제출
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            executor.execute(() -> {
                String threadName = Thread.currentThread().getName();
                System.out.println("Task " + taskId + " is running on " + threadName);
                
                try {
                    // 작업 시뮬레이션
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                
                System.out.println("Task " + taskId + " completed");
            });
        }
        
        // 새 작업 수락 중지 및 이미 제출된 작업 완료 대기
        executor.shutdown();
        
        // 모든 작업이 완료될 때까지 대기 (선택 사항)
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                // 타임아웃 발생 시 강제 종료
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

## 4. 결과를 반환하는 작업 실행하기

`submit` 메서드를 사용하여 결과를 반환하는 작업을 실행할 수 있습니다:

```java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class CallableExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        List<Future<Integer>> resultList = new ArrayList<>();
        
        // 결과를 반환하는 10개의 작업 제출
        for (int i = 0; i < 10; i++) {
            final int taskId = i;
            Future<Integer> result = executor.submit(() -> {
                // 작업 시뮬레이션 (taskId의 제곱 계산)
                Thread.sleep(1000);
                return taskId * taskId;
            });
            resultList.add(result);
        }
        
        // 모든 결과 수집
        for (int i = 0; i < resultList.size(); i++) {
            try {
                Integer result = resultList.get(i).get(); // 결과가 준비될 때까지 대기
                System.out.println("Task " + i + " result: " + result);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        
        executor.shutdown();
    }
}
```

## 5. 다양한 ThreadPool 유형 비교

Java Executors 프레임워크는 다양한 유형의 스레드 풀을 제공합니다:

### 5.1 newFixedThreadPool

```java
ExecutorService fixedPool = Executors.newFixedThreadPool(4);
```

- **특징**: 고정된 수의 스레드를 유지
- **용도**: 부하가 예측 가능하고 안정적인 경우
- **동작**: 모든 스레드가 사용 중이면 새 작업은 큐에서 대기

### 5.2 newCachedThreadPool

```java
ExecutorService cachedPool = Executors.newCachedThreadPool();
```

- **특징**: 필요에 따라 스레드를 생성하고 재사용
- **용도**: 많은 단기 작업이 있는 경우
- **동작**: 유휴 스레드가 60초 동안 사용되지 않으면 제거됨

### 5.3 newSingleThreadExecutor

```java
ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
```

- **특징**: 단일 스레드로 모든 작업을 순차적으로 처리
- **용도**: 작업의 순차적 실행이 필요한 경우
- **동작**: 스레드가 예외로 종료되면 새 스레드가 생성됨

### 5.4 newScheduledThreadPool

```java
ScheduledExecutorService scheduledPool = Executors.newScheduledThreadPool(4);
```

- **특징**: 지연 또는 주기적 작업 실행 지원
- **용도**: 타이머 작업, 주기적 유지 관리 작업
- **동작**: 지정된 시간 후 또는 일정한 간격으로 작업 실행

## 6. ThreadPool 크기 설정 가이드라인

스레드 풀 크기를 적절하게 설정하는 것은 성능에 중요한 영향을 미칩니다:

1. **CPU 바운드 작업**: 일반적으로 `Runtime.getRuntime().availableProcessors()` 개수의 스레드가 적합
2. **I/O 바운드 작업**: CPU 코어 수보다 많은 스레드가 필요할 수 있음
3. **혼합 작업**: 다음 공식을 참고: `스레드 수 = CPU 코어 수 * (1 + 대기시간/계산시간)`

```java
// CPU 코어 수에 기반한 스레드 풀 생성
int coreCount = Runtime.getRuntime().availableProcessors();
ExecutorService executor = Executors.newFixedThreadPool(coreCount);
```

## 7. 스레드 풀 종료 방법

스레드 풀을 올바르게 종료하는 것은 리소스 누수를 방지하는 데 중요합니다:

```java
// 정상 종료 (새 작업 거부, 기존 작업 완료)
executor.shutdown();

// 즉시 종료 시도 (실행 중인 작업 중단)
List<Runnable> unfinishedTasks = executor.shutdownNow();

// 종료 대기
try {
    if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
        executor.shutdownNow();
    }
} catch (InterruptedException e) {
    executor.shutdownNow();
    Thread.currentThread().interrupt();
}
```

## 8. 모범 사례 및 주의사항

1. **풀 크기 제한**: 너무 많은 스레드는 메모리 사용량 증가와 컨텍스트 전환 오버헤드를 유발

2. **작업 크기**: 너무 작은 작업은 스레드 관리 오버헤드가 커질 수 있음

3. **긴 작업 처리**: 매우 긴 작업은 별도의 전용 스레드 풀에서 실행 고려

4. **예외 처리**: 작업 내에서 발생한 예외를 적절히 처리해야 함

5. **스레드 안전성**: 공유 자원에 접근하는 작업은 적절한 동기화 필요

6. **데드락 방지**: 다른 작업의 완료를 기다리는 작업을 제출하지 않도록 주의

## 9. Java 8 이후의 개선사항

Java 8 이후에는 CompletableFuture와 같은 고급 비동기 프로그래밍 도구가 추가되었습니다:

```java
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CompletableFutureExample {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        // CompletableFuture를 사용한 비동기 작업
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
                return 42;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return -1;
            }
        }, executor);
        
        // 결과 처리를 위한 콜백 추가
        future.thenAccept(result -> System.out.println("Result: " + result))
              .thenRun(() -> System.out.println("Processing complete"));
        
        // 메인 스레드가 종료되지 않도록 대기
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        executor.shutdown();
    }
}
```

## 10. 결론

Java의 Executors 프레임워크와 ThreadPool은 동시성 프로그래밍을 단순화하고 성능을 향상시키는 강력한 도구입니다. 특히 `newFixedThreadPool`은 예측 가능한 리소스 사용으로 안정적인 애플리케이션을 구축하는 데 도움이 됩니다. 적절한 스레드 풀 유형 선택과 크기 설정을 통해 애플리케이션의 성능과 안정성을 최적화할 수 있습니다.