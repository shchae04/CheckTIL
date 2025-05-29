# Java ForkJoin 프레임워크 사용법

Java의 ForkJoin 프레임워크는 Java 7에서 도입된 병렬 처리 프레임워크로, 대용량 작업을 작은 작업으로 분할하여 병렬로 처리한 후 결과를 합치는 "분할 정복(divide and conquer)" 알고리즘을 효율적으로 구현할 수 있게 해줍니다.

## 1. ForkJoin 프레임워크 개요

ForkJoin 프레임워크는 다음과 같은 주요 구성 요소로 이루어져 있습니다:

- **ForkJoinPool**: 작업을 실행하는 특수한 스레드 풀
- **ForkJoinTask**: ForkJoinPool에서 실행되는 태스크의 기본 타입
  - **RecursiveTask<V>**: 결과를 반환하는 태스크
  - **RecursiveAction**: 결과를 반환하지 않는 태스크

## 2. ForkJoin 프레임워크의 작동 원리

ForkJoin 프레임워크는 "work-stealing" 알고리즘을 사용합니다:

1. 각 스레드는 자신의 작업 큐를 가짐
2. 자신의 큐가 비면 다른 스레드의 큐에서 작업을 "훔쳐옴"
3. 이를 통해 부하 균형을 효율적으로 유지

## 3. RecursiveTask 예제: 숫자 배열의 합계 계산

```java
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class SumTask extends RecursiveTask<Long> {
    private static final int THRESHOLD = 1000; // 임계값
    private final long[] numbers;
    private final int start;
    private final int end;

    public SumTask(long[] numbers, int start, int end) {
        this.numbers = numbers;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Long compute() {
        int length = end - start;
        
        // 작업이 충분히 작으면 직접 계산
        if (length <= THRESHOLD) {
            return computeDirectly();
        }
        
        // 작업을 두 부분으로 분할
        int middle = start + length / 2;
        
        SumTask leftTask = new SumTask(numbers, start, middle);
        SumTask rightTask = new SumTask(numbers, middle, end);
        
        // 왼쪽 작업을 다른 스레드에서 비동기적으로 실행
        leftTask.fork();
        
        // 현재 스레드에서 오른쪽 작업 실행
        Long rightResult = rightTask.compute();
        
        // 왼쪽 작업의 결과를 기다림
        Long leftResult = leftTask.join();
        
        // 결과 합치기
        return leftResult + rightResult;
    }

    private long computeDirectly() {
        long sum = 0;
        for (int i = start; i < end; i++) {
            sum += numbers[i];
        }
        return sum;
    }

    public static void main(String[] args) {
        // 테스트용 배열 생성
        long[] numbers = new long[100_000_000];
        for (int i = 0; i < numbers.length; i++) {
            numbers[i] = i;
        }
        
        // ForkJoinPool 생성
        ForkJoinPool pool = ForkJoinPool.commonPool();
        
        // 작업 생성 및 실행
        SumTask task = new SumTask(numbers, 0, numbers.length);
        long sum = pool.invoke(task);
        
        System.out.println("Sum: " + sum);
    }
}
```

## 4. RecursiveAction 예제: 배열 요소 변환

```java
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.Arrays;

public class ArrayTransformAction extends RecursiveAction {
    private static final int THRESHOLD = 1000;
    private final int[] array;
    private final int start;
    private final int end;
    
    public ArrayTransformAction(int[] array, int start, int end) {
        this.array = array;
        this.start = start;
        this.end = end;
    }
    
    @Override
    protected void compute() {
        if (end - start <= THRESHOLD) {
            // 작업이 충분히 작으면 직접 처리
            computeDirectly();
            return;
        }
        
        // 작업 분할
        int middle = start + (end - start) / 2;
        
        ArrayTransformAction left = new ArrayTransformAction(array, start, middle);
        ArrayTransformAction right = new ArrayTransformAction(array, middle, end);
        
        // 두 작업을 병렬로 실행
        invokeAll(left, right);
    }
    
    private void computeDirectly() {
        for (int i = start; i < end; i++) {
            // 각 요소를 제곱으로 변환
            array[i] = array[i] * array[i];
        }
    }
    
    public static void main(String[] args) {
        int[] array = new int[10_000_000];
        Arrays.fill(array, 2);
        
        ForkJoinPool pool = ForkJoinPool.commonPool();
        ArrayTransformAction task = new ArrayTransformAction(array, 0, array.length);
        
        pool.invoke(task);
        
        // 결과 확인 (처음 10개 요소만)
        System.out.println("Transformed array (first 10 elements): " + 
                          Arrays.toString(Arrays.copyOfRange(array, 0, 10)));
    }
}
```

## 5. ForkJoinPool 생성 및 관리

ForkJoinPool을 생성하는 방법은 두 가지가 있습니다:

### 5.1 공통 풀 사용 (권장)

```java
ForkJoinPool pool = ForkJoinPool.commonPool();
```

공통 풀은 애플리케이션 전체에서 공유되며, 일반적으로 사용 가능한 프로세서 수에 맞게 설정됩니다.

### 5.2 커스텀 풀 생성

```java
// 특정 병렬 처리 수준으로 풀 생성
ForkJoinPool customPool = new ForkJoinPool(4); // 4개의 스레드 사용
```

## 6. 작업 제출 방법

ForkJoinPool에 작업을 제출하는 방법은 여러 가지가 있습니다:

```java
// 작업을 제출하고 결과를 기다림
T result = pool.invoke(task);

// 작업을 제출하고 Future 객체를 받음
Future<T> future = pool.submit(task);
T result = future.get(); // 결과를 기다림

// 작업을 제출하고 즉시 반환 (결과 무시)
pool.execute(task);
```

## 7. 모범 사례 및 주의사항

1. **임계값 설정**: 너무 작은 작업으로 분할하면 오버헤드가 발생하므로 적절한 임계값 설정이 중요합니다.

2. **fork()와 compute() 순서**: 일반적으로 왼쪽 작업은 fork()하고 오른쪽 작업은 현재 스레드에서 compute()하는 것이 효율적입니다.

3. **예외 처리**: ForkJoinTask에서 발생한 예외는 결과를 가져올 때까지 전파되지 않으므로 적절한 예외 처리가 필요합니다.

4. **작업 크기**: 작업이 너무 작으면 분할 오버헤드가 이득보다 클 수 있습니다.

5. **동기화**: ForkJoinTask 내에서 동기화를 최소화하여 성능 저하를 방지해야 합니다.

## 8. Java 8 이후의 개선사항

Java 8에서는 Stream API와 ForkJoin 프레임워크가 통합되어 더 쉽게 병렬 처리를 구현할 수 있게 되었습니다:

```java
// 병렬 스트림을 사용한 합계 계산
long sum = Arrays.stream(numbers).parallel().sum();

// 병렬 스트림을 사용한 배열 변환
int[] transformed = Arrays.stream(array)
                         .parallel()
                         .map(n -> n * n)
                         .toArray();
```

## 9. 결론

Java ForkJoin 프레임워크는 대규모 데이터 처리, 계산 집약적 작업 등에 적합한 강력한 병렬 처리 도구입니다. 적절한 작업 분할과 임계값 설정을 통해 멀티코어 프로세서의 성능을 최대한 활용할 수 있습니다.