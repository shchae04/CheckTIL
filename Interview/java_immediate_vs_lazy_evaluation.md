# Java의 즉시 평가(Immediate Evaluation)와 지연 평가(Lazy Evaluation)

Java에서 즉시 평가와 지연 평가는 표현식이나 연산의 실행 시점을 결정하는 두 가지 다른 전략입니다. 이 문서에서는 두 평가 방식의 개념, 차이점, 장단점 및 실제 사용 예시를 살펴보겠습니다.

## 1. 즉시 평가(Immediate Evaluation)와 지연 평가(Lazy Evaluation) 개념

### 1.1 즉시 평가(Immediate Evaluation)

즉시 평가는 표현식이나 연산이 정의되는 즉시 실행되는 방식입니다. Java에서 대부분의 연산은 기본적으로 즉시 평가 방식을 따릅니다.

- **특징**:
  - 표현식이 정의되는 시점에 바로 계산됨
  - 결과값이 즉시 메모리에 저장됨
  - 여러 번 사용되는 경우 이미 계산된 값을 재사용

### 1.2 지연 평가(Lazy Evaluation)

지연 평가는 표현식이나 연산의 실행을 실제로 결과가 필요한 시점까지 미루는 방식입니다. Java 8부터 도입된 Stream API가 대표적인 지연 평가 메커니즘을 사용합니다.

- **특징**:
  - 표현식이 정의되는 시점에는 계산되지 않음
  - 결과값이 실제로 필요한 시점(최종 연산 시)에 계산됨
  - 불필요한 계산을 피할 수 있음
  - 무한 자료구조 처리 가능

## 2. 즉시 평가 예제

### 2.1 기본적인 즉시 평가

```java
// 즉시 평가 예제
public class ImmediateEvaluationExample {
    public static void main(String[] args) {
        System.out.println("즉시 평가 시작");
        
        // 즉시 평가: 모든 연산이 즉시 수행됨
        int result = compute(5);
        System.out.println("연산 결과: " + result);
        
        // result 값은 이미 계산되어 있으므로 여러 번 사용해도 재계산되지 않음
        System.out.println("결과 재사용: " + result);
        System.out.println("결과 재사용: " + result);
    }
    
    public static int compute(int n) {
        System.out.println("compute(" + n + ") 호출됨");
        return n * 2;
    }
}
```

실행 결과:
```
즉시 평가 시작
compute(5) 호출됨
연산 결과: 10
결과 재사용: 10
결과 재사용: 10
```

### 2.2 컬렉션의 즉시 평가

```java
import java.util.ArrayList;
import java.util.List;

public class ImmediateCollectionExample {
    public static void main(String[] args) {
        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        numbers.add(4);
        numbers.add(5);
        
        // 즉시 평가: 모든 요소에 대해 연산이 즉시 수행됨
        List<Integer> doubledNumbers = new ArrayList<>();
        for (Integer number : numbers) {
            System.out.println("즉시 처리: " + number);
            doubledNumbers.add(number * 2);
        }
        
        System.out.println("결과 리스트: " + doubledNumbers);
        
        // 결과 리스트를 사용하지 않더라도 이미 모든 연산이 수행됨
    }
}
```

실행 결과:
```
즉시 처리: 1
즉시 처리: 2
즉시 처리: 3
즉시 처리: 4
즉시 처리: 5
결과 리스트: [2, 4, 6, 8, 10]
```

## 3. 지연 평가 예제

### 3.1 Java 8 Stream API를 이용한 지연 평가

```java
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class LazyEvaluationStreamExample {
    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);
        
        // 지연 평가: 중간 연산은 최종 연산이 호출될 때까지 실행되지 않음
        Stream<Integer> stream = numbers.stream()
                .filter(n -> {
                    System.out.println("필터링: " + n);
                    return n % 2 == 0;
                })
                .map(n -> {
                    System.out.println("매핑: " + n);
                    return n * 2;
                });
        
        // 여기까지는 아무 연산도 실행되지 않음
        System.out.println("스트림 파이프라인 구성 완료");
        
        // 최종 연산이 호출되면 그제서야 모든 중간 연산이 실행됨
        List<Integer> result = stream.toList();
        System.out.println("결과: " + result);
    }
}
```

실행 결과:
```
스트림 파이프라인 구성 완료
필터링: 1
필터링: 2
매핑: 2
필터링: 3
필터링: 4
매핑: 4
필터링: 5
결과: [4, 8]
```

### 3.2 Supplier를 이용한 지연 평가

```java
import java.util.function.Supplier;

public class LazyEvaluationSupplierExample {
    public static void main(String[] args) {
        System.out.println("지연 평가 시작");
        
        // 지연 평가: Supplier를 사용하여 계산을 지연시킴
        Supplier<Integer> lazyResult = () -> compute(5);
        
        System.out.println("Supplier 생성 완료");
        
        // 실제로 값이 필요한 시점에 계산 수행
        if (needsComputation()) {
            System.out.println("연산 결과: " + lazyResult.get());
        } else {
            System.out.println("연산이 필요하지 않음");
        }
        
        // 다시 값이 필요하면 재계산됨
        if (needsComputation()) {
            System.out.println("연산 결과 (재계산): " + lazyResult.get());
        }
    }
    
    public static int compute(int n) {
        System.out.println("compute(" + n + ") 호출됨");
        return n * 2;
    }
    
    public static boolean needsComputation() {
        return true; // 예제를 위해 항상 true 반환
    }
}
```

실행 결과:
```
지연 평가 시작
Supplier 생성 완료
compute(5) 호출됨
연산 결과: 10
compute(5) 호출됨
연산 결과 (재계산): 10
```

### 3.3 무한 시퀀스와 지연 평가

```java
import java.util.stream.Stream;

public class LazyEvaluationInfiniteStreamExample {
    public static void main(String[] args) {
        // 무한 스트림 생성 (즉시 평가라면 무한 루프에 빠짐)
        Stream<Integer> infiniteStream = Stream.iterate(0, n -> n + 1);
        
        // 지연 평가 덕분에 무한 스트림에서도 필요한 요소만 처리 가능
        System.out.println("무한 스트림의 처음 5개 요소:");
        infiniteStream
            .filter(n -> n % 2 == 0)  // 짝수만 필터링
            .limit(5)                 // 처음 5개만 선택
            .forEach(System.out::println);
    }
}
```

실행 결과:
```
무한 스트림의 처음 5개 요소:
0
2
4
6
8
```

## 4. 커스텀 지연 평가 구현

### 4.1 간단한 지연 평가 래퍼 클래스

```java
import java.util.function.Supplier;

// 지연 평가를 위한 래퍼 클래스
class Lazy<T> {
    private Supplier<T> supplier;
    private T value;
    private boolean evaluated = false;
    
    public Lazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }
    
    // 값이 필요한 시점에 계산
    public T get() {
        if (!evaluated) {
            value = supplier.get();
            evaluated = true;
        }
        return value;
    }
    
    // 이미 평가되었는지 확인
    public boolean isEvaluated() {
        return evaluated;
    }
}

public class CustomLazyEvaluationExample {
    public static void main(String[] args) {
        System.out.println("프로그램 시작");
        
        // 지연 평가 객체 생성
        Lazy<Integer> lazyValue = new Lazy<>(() -> {
            System.out.println("복잡한 계산 수행 중...");
            try {
                Thread.sleep(1000); // 계산에 시간이 걸린다고 가정
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return 42;
        });
        
        System.out.println("지연 평가 객체 생성됨");
        System.out.println("평가 여부: " + lazyValue.isEvaluated());
        
        // 값이 실제로 필요한 시점
        System.out.println("값을 처음 요청...");
        System.out.println("결과: " + lazyValue.get());
        System.out.println("평가 여부: " + lazyValue.isEvaluated());
        
        // 두 번째 요청 시에는 이미 계산된 값 재사용
        System.out.println("값을 다시 요청...");
        System.out.println("결과: " + lazyValue.get());
    }
}
```

실행 결과:
```
프로그램 시작
지연 평가 객체 생성됨
평가 여부: false
값을 처음 요청...
복잡한 계산 수행 중...
결과: 42
평가 여부: true
값을 다시 요청...
결과: 42
```

## 5. 즉시 평가와 지연 평가의 비교

### 5.1 성능 비교 예제

```java
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EvaluationPerformanceComparison {
    public static void main(String[] args) {
        // 큰 데이터 세트 생성
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 10_000_000; i++) {
            numbers.add(i);
        }
        
        // 즉시 평가 방식 (전통적인 방법)
        long startImmediate = System.currentTimeMillis();
        List<Integer> immediateResult = new ArrayList<>();
        for (Integer n : numbers) {
            if (n % 10 == 0) {
                Integer doubled = n * 2;
                if (doubled > 100) {
                    immediateResult.add(doubled);
                    if (immediateResult.size() >= 10) break;
                }
            }
        }
        long endImmediate = System.currentTimeMillis();
        
        // 지연 평가 방식 (Stream API)
        long startLazy = System.currentTimeMillis();
        List<Integer> lazyResult = numbers.stream()
                .filter(n -> n % 10 == 0)
                .map(n -> n * 2)
                .filter(n -> n > 100)
                .limit(10)
                .collect(Collectors.toList());
        long endLazy = System.currentTimeMillis();
        
        System.out.println("즉시 평가 결과: " + immediateResult);
        System.out.println("즉시 평가 소요 시간: " + (endImmediate - startImmediate) + "ms");
        
        System.out.println("지연 평가 결과: " + lazyResult);
        System.out.println("지연 평가 소요 시간: " + (endLazy - startLazy) + "ms");
    }
}
```

### 5.2 장단점 비교

| 특성 | 즉시 평가 | 지연 평가 |
|------|-----------|-----------|
| 실행 시점 | 정의 시점에 즉시 실행 | 결과가 필요한 시점에 실행 |
| 메모리 사용 | 모든 중간 결과를 저장 | 필요한 결과만 저장 |
| 성능 | 불필요한 연산도 수행 | 필요한 연산만 수행 |
| 무한 자료구조 | 처리 불가능 | 처리 가능 |
| 디버깅 | 상대적으로 쉬움 | 상대적으로 어려움 |
| 부작용 처리 | 예측 가능 | 실행 시점이 불확실하여 주의 필요 |
| 재사용 | 이미 계산된 값 재사용 | 매번 재계산 가능성 있음 |

## 6. 실제 활용 사례

### 6.1 대용량 데이터 처리

```java
import java.util.stream.Stream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;

public class LargeFileProcessingExample {
    public static void main(String[] args) {
        try {
            // 대용량 파일을 지연 평가로 처리
            Stream<String> lines = Files.lines(Paths.get("large_file.txt"));
            
            long count = lines
                .filter(line -> line.contains("important"))
                .map(String::trim)
                .distinct()
                .count();
                
            System.out.println("중요 라인 수: " + count);
            
            lines.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

### 6.2 조건부 계산

```java
import java.util.function.Supplier;

public class ConditionalComputationExample {
    public static void main(String[] args) {
        boolean condition = args.length > 0;
        
        // 즉시 평가 - 조건에 관계없이 항상 계산됨
        int immediateResult = expensiveComputation();
        if (condition) {
            System.out.println("즉시 평가 결과: " + immediateResult);
        }
        
        // 지연 평가 - 조건이 참일 때만 계산됨
        Supplier<Integer> lazyResult = () -> expensiveComputation();
        if (condition) {
            System.out.println("지연 평가 결과: " + lazyResult.get());
        }
    }
    
    public static int expensiveComputation() {
        System.out.println("비용이 많이 드는 계산 수행 중...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return 42;
    }
}
```

### 6.3 캐싱과 메모이제이션

```java
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

// 메모이제이션을 위한 유틸리티 클래스
class Memoizer<T, R> {
    private final Map<T, R> cache = new HashMap<>();
    private final Function<T, R> function;
    
    public Memoizer(Function<T, R> function) {
        this.function = function;
    }
    
    public R compute(T input) {
        return cache.computeIfAbsent(input, function);
    }
}

public class MemoizationExample {
    public static void main(String[] args) {
        // 피보나치 수열 계산을 위한 메모이저 생성
        Memoizer<Integer, Long> fibMemoizer = new Memoizer<>(n -> {
            if (n <= 1) return (long) n;
            System.out.println("피보나치 계산: " + n);
            return fibMemoizer.compute(n - 1) + fibMemoizer.compute(n - 2);
        });
        
        System.out.println("피보나치(5) 첫 번째 호출: " + fibMemoizer.compute(5));
        System.out.println("피보나치(5) 두 번째 호출: " + fibMemoizer.compute(5));
        
        System.out.println("피보나치(6): " + fibMemoizer.compute(6));
    }
}
```

## 7. 결론

즉시 평가와 지연 평가는 각각 고유한 장단점을 가지고 있으며, 상황에 따라 적절한 방식을 선택하는 것이 중요합니다.

- **즉시 평가**는 직관적이고 디버깅이 쉬우며, 결과를 여러 번 사용할 때 효율적입니다.
- **지연 평가**는 불필요한 연산을 피하고, 메모리 효율성이 높으며, 무한 자료구조를 다룰 수 있습니다.

Java에서는 Stream API, Supplier 인터페이스, Optional 클래스 등을 통해 지연 평가를 활용할 수 있습니다. 특히 대용량 데이터 처리, 조건부 계산, 비용이 많이 드는 연산 등에서 지연 평가의 이점을 극대화할 수 있습니다.

효율적인 Java 프로그래밍을 위해서는 두 평가 방식의 특성을 이해하고, 상황에 맞게 적절히 활용하는 것이 중요합니다.