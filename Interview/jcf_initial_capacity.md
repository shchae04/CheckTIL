# JCF 자료구조의 초기 용량 지정

## 1. 한 줄 정의
JCF 자료구조의 초기 용량을 지정하면 불필요한 리사이징을 방지하여 메모리 사용량과 연산 비용을 절감할 수 있다.

---

## 2. ArrayList의 동적 크기 조정 메커니즘

### 2-1. 기본 용량(Default Capacity)
- **초기 크기**: 10
- **증가 방식**: 기존 크기의 1.5배
- **계산 공식**: `newCapacity = oldCapacity + (oldCapacity >> 1)`

```java
// ArrayList 기본 생성 (초기 용량 10)
List<String> list = new ArrayList<>();

// 내부적으로 요소가 10개를 넘으면
// 10 -> 15 -> 22 -> 33 -> 49 -> ... 순으로 증가
```

### 2-2. 리사이징 과정
1. **용량 초과 감지**: 현재 size가 capacity에 도달
2. **새 배열 생성**: 1.5배 크기의 새 배열 할당
3. **데이터 복사**: 기존 배열 요소를 새 배열로 복사
4. **기존 배열 제거**: 가비지 컬렉션 대상이 됨

```java
// 리사이징이 발생하는 과정
List<String> list = new ArrayList<>();  // capacity: 10

for (int i = 0; i < 11; i++) {
    list.add("item");  // 11번째 추가 시 리사이징 발생
    // 내부적으로 새 배열(capacity: 15) 생성 및 복사
}
```

---

## 3. 초기 용량 미지정 시 문제점

### 3-1. 메모리 낭비 사례
```java
public class Main {

    private static final int MAX = 5_000_000;

    public static void main(String[] args) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        printUsedHeap(1, memoryMXBean);

        List<String> arr = new ArrayList<>();
        for (int i = 0; i < MAX; i++) {
            arr.add("a");
        }

        printUsedHeap(2, memoryMXBean);
        printUsedHeap(3, memoryMXBean);
    }

    private static void printUsedHeap(int logIndex, MemoryMXBean memoryMXBean) {
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        long used = heapUsage.getUsed();
        System.out.println("[" + logIndex + "] " + "Used Heap Memory: " + used / 1024 / 1024 + " MB");
    }
}
```

**실행 결과 (초기 용량 미지정)**:
- 최종 capacity: 6,153,400
- 메모리 사용량: 약 70MB
- 리사이징 횟수: 약 25회

### 3-2. 발생하는 비용
- **메모리 비용**: 임시 배열 생성으로 인한 추가 메모리 사용
- **시간 비용**: 배열 복사 연산의 반복 수행
- **GC 비용**: 버려진 배열들의 가비지 컬렉션 부담

---

## 4. 초기 용량 지정의 효과

### 4-1. 최적화된 코드
```java
private static final int MAX = 5_000_000;

public static void main(String[] args) {
    MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    printUsedHeap(1, memoryMXBean);

    // 초기 용량 지정
    List<String> arr = new ArrayList<>(MAX);
    for (int i = 0; i < MAX; i++) {
        arr.add("a");
    }

    printUsedHeap(2, memoryMXBean);
    printUsedHeap(3, memoryMXBean);
}
```

**실행 결과 (초기 용량 지정)**:
- 최종 capacity: 5,000,000
- 메모리 사용량: 약 20MB
- 리사이징 횟수: 0회

### 4-2. 개선 효과
| 항목 | 미지정 | 지정 | 개선율 |
|------|--------|------|--------|
| **메모리** | 70MB | 20MB | 71% 절감 |
| **리사이징** | 25회 | 0회 | 100% 제거 |
| **최종 capacity** | 6,153,400 | 5,000,000 | 23% 절감 |
| **성능** | 느림 | 빠름 | 배열 복사 비용 제거 |

---

## 5. 로드 팩터(Load Factor)와 임계점(Threshold)

### 5-1. 개념 정의

#### 로드 팩터(Load Factor)
- **정의**: 자료 구조에 데이터가 어느 정도 적재되었는지 나타내는 비율
- **목적**: 리사이징 시점을 결정하기 위한 기준
- **공식**: `Load Factor = 현재 요소 수 / 전체 용량`

#### 임계점(Threshold)
- **정의**: 리사이징이 발생하는 경계값
- **공식**: `Threshold = Capacity × Load Factor`
- **동작**: 요소 수가 임계점을 초과하면 리사이징 발생

### 5-2. HashMap의 로드 팩터

#### 기본 설정
```java
// HashMap 기본 설정
public class HashMap<K,V> {
    static final int DEFAULT_INITIAL_CAPACITY = 16;  // 초기 용량
    static final float DEFAULT_LOAD_FACTOR = 0.75f;  // 기본 로드 팩터

    int threshold;  // 임계점 = capacity * load factor
}
```

#### 계산 예시
```java
// HashMap 생성 시
HashMap<String, String> map = new HashMap<>();

// 초기 상태
// - capacity: 16
// - load factor: 0.75
// - threshold: 16 × 0.75 = 12

// 요소 추가
for (int i = 0; i < 13; i++) {
    map.put("key" + i, "value" + i);
    // 13번째 추가 시 (threshold 12 초과)
    // - 리사이징 발생
    // - 새로운 capacity: 32
    // - 새로운 threshold: 32 × 0.75 = 24
    // - 재해싱(rehashing) 수행
}
```

### 5-3. HashMap 리사이징 과정
```java
// HashMap의 resize() 메서드 동작 과정
1. 새로운 배열 생성 (크기: 기존의 2배)
2. 기존 모든 엔트리에 대해 재해싱 수행
3. 새로운 해시값을 기준으로 재배치
4. threshold 업데이트

// 예시
HashMap<String, Integer> map = new HashMap<>();
// capacity: 16, threshold: 12

map.put("key1", 1);   // size: 1
// ... 중략
map.put("key12", 12); // size: 12 (threshold 도달)
map.put("key13", 13); // size: 13 -> 리사이징 발생!
                      // 새 capacity: 32, 새 threshold: 24
```

### 5-4. 로드 팩터가 0.75인 이유
```java
// 0.75가 최적인 이유

// 1. 로드 팩터가 너무 높은 경우 (예: 0.95)
HashMap<String, String> map1 = new HashMap<>(100, 0.95f);
// - 장점: 메모리 효율적
// - 단점: 해시 충돌 증가, 성능 저하

// 2. 로드 팩터가 너무 낮은 경우 (예: 0.5)
HashMap<String, String> map2 = new HashMap<>(100, 0.5f);
// - 장점: 해시 충돌 감소, 빠른 검색
// - 단점: 메모리 낭비, 빈번한 리사이징

// 3. 0.75 (권장)
HashMap<String, String> map3 = new HashMap<>(100, 0.75f);
// - 메모리 효율과 성능의 균형
// - 충돌과 공간 활용의 최적점
```

---

## 6. 다른 JCF 자료구조의 초기 용량

### 6-1. HashSet
```java
// HashSet은 내부적으로 HashMap을 사용
public class HashSet<E> {
    private transient HashMap<E,Object> map;

    // 기본 생성자
    public HashSet() {
        map = new HashMap<>();  // capacity: 16, load factor: 0.75
    }

    // 초기 용량 지정
    public HashSet(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }
}

// 사용 예시
Set<String> set1 = new HashSet<>();           // 기본 용량
Set<String> set2 = new HashSet<>(1000);       // 초기 용량 지정
Set<String> set3 = new HashSet<>(1000, 0.8f); // 용량 + 로드 팩터 지정
```

### 6-2. StringBuilder
```java
// StringBuilder도 가변 크기 배열 사용
public final class StringBuilder {
    // 기본 용량: 16
    public StringBuilder() {
        super(16);
    }

    // 초기 용량 지정
    public StringBuilder(int capacity) {
        super(capacity);
    }
}

// 사용 예시
StringBuilder sb1 = new StringBuilder();        // 용량: 16
StringBuilder sb2 = new StringBuilder(1000);    // 용량: 1000

// 대량의 문자열 연결 시
StringBuilder sb = new StringBuilder(10000);
for (int i = 0; i < 10000; i++) {
    sb.append("text");  // 리사이징 없이 효율적
}
```

### 6-3. Vector
```java
// Vector는 동기화된 ArrayList (레거시)
public class Vector<E> {
    // 기본 용량: 10
    // 증가 방식: 2배 (ArrayList는 1.5배)

    public Vector() {
        this(10);
    }

    public Vector(int initialCapacity) {
        this(initialCapacity, 0);
    }
}

// 사용 예시 (현재는 ArrayList 권장)
Vector<String> vec1 = new Vector<>();         // 용량: 10
Vector<String> vec2 = new Vector<>(1000);     // 용량: 1000
```

---

## 7. 백엔드 개발자 관점의 실무 적용

### 7-1. API 응답 데이터 처리
```java
@RestController
public class UserController {

    @GetMapping("/users")
    public List<UserDto> getUsers() {
        // ❌ 나쁜 예: 초기 용량 미지정
        List<UserDto> users = new ArrayList<>();

        // ✅ 좋은 예: 예상 크기로 초기 용량 지정
        int expectedSize = userService.getUserCount();
        List<UserDto> users = new ArrayList<>(expectedSize);

        return userService.findAll();
    }
}
```

### 7-2. 대용량 데이터 배치 처리
```java
@Service
public class DataBatchService {

    public void processBigData() {
        // ❌ 나쁜 예: 500만 건 데이터를 기본 용량으로 처리
        List<Data> dataList = new ArrayList<>();
        for (int i = 0; i < 5_000_000; i++) {
            dataList.add(fetchData(i));  // 25회 이상 리사이징 발생
        }

        // ✅ 좋은 예: 초기 용량 지정
        List<Data> dataList = new ArrayList<>(5_000_000);
        for (int i = 0; i < 5_000_000; i++) {
            dataList.add(fetchData(i));  // 리사이징 없음
        }
    }
}
```

### 7-3. 캐시 구현
```java
@Component
public class UserCacheManager {

    // ❌ 나쁜 예: 기본 용량 (16)
    private final Map<Long, User> cache = new HashMap<>();

    // ✅ 좋은 예: 예상 사용자 수로 초기 용량 설정
    private final Map<Long, User> cache = new HashMap<>(10000, 0.75f);

    // 10,000명의 사용자를 캐싱할 예정
    // threshold: 10,000 × 0.75 = 7,500
    // 7,500명까지는 리사이징 없이 동작
}
```

### 7-4. 데이터베이스 조회 결과 처리
```java
@Repository
public class ProductRepository {

    public List<Product> findAllProducts() {
        // ✅ 좋은 예: 쿼리 결과 크기 예측
        String countSql = "SELECT COUNT(*) FROM products";
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class);

        // 예상 크기로 초기 용량 설정
        List<Product> products = new ArrayList<>(totalCount);

        String sql = "SELECT * FROM products";
        return jdbcTemplate.query(sql, new ProductRowMapper());
    }
}
```

### 7-5. 스트림 처리 최적화
```java
@Service
public class OrderService {

    public List<OrderDto> getOrdersByStatus(String status) {
        List<Order> orders = orderRepository.findByStatus(status);

        // ✅ 좋은 예: 스트림 결과를 받을 리스트 초기화
        int expectedSize = orders.size();

        return orders.stream()
                .filter(order -> order.getAmount() > 10000)
                .map(this::convertToDto)
                .collect(Collectors.toCollection(
                    () -> new ArrayList<>(expectedSize)
                ));
    }
}
```

---

## 8. 성능 비교 실험

### 8-1. 실험 코드
```java
public class PerformanceTest {

    private static final int SIZE = 1_000_000;

    public static void main(String[] args) {
        testWithoutInitialCapacity();
        testWithInitialCapacity();
    }

    private static void testWithoutInitialCapacity() {
        long startTime = System.nanoTime();
        long startMemory = Runtime.getRuntime().totalMemory()
                         - Runtime.getRuntime().freeMemory();

        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            list.add(i);
        }

        long endTime = System.nanoTime();
        long endMemory = Runtime.getRuntime().totalMemory()
                       - Runtime.getRuntime().freeMemory();

        System.out.println("Without Initial Capacity:");
        System.out.println("Time: " + (endTime - startTime) / 1_000_000 + " ms");
        System.out.println("Memory: " + (endMemory - startMemory) / 1024 / 1024 + " MB");
    }

    private static void testWithInitialCapacity() {
        long startTime = System.nanoTime();
        long startMemory = Runtime.getRuntime().totalMemory()
                         - Runtime.getRuntime().freeMemory();

        List<Integer> list = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            list.add(i);
        }

        long endTime = System.nanoTime();
        long endMemory = Runtime.getRuntime().totalMemory()
                       - Runtime.getRuntime().freeMemory();

        System.out.println("With Initial Capacity:");
        System.out.println("Time: " + (endTime - startTime) / 1_000_000 + " ms");
        System.out.println("Memory: " + (endMemory - startMemory) / 1024 / 1024 + " MB");
    }
}
```

### 8-2. 실험 결과 (예상)
```
Without Initial Capacity:
Time: 245 ms
Memory: 48 MB

With Initial Capacity:
Time: 89 ms
Memory: 16 MB

개선 효과:
- 시간: 63% 단축
- 메모리: 66% 절감
```

---

## 9. 핵심 요약

### 9-1. 주요 개념 정리

| 개념 | 설명 | 예시 |
|------|------|------|
| **초기 용량** | 자료구조 생성 시 할당되는 내부 배열 크기 | `new ArrayList<>(1000)` |
| **리사이징** | 용량 초과 시 더 큰 배열을 생성하고 복사하는 과정 | 10 → 15 → 22 → 33 |
| **로드 팩터** | 데이터 적재율을 나타내는 비율 | 0.75 (75%) |
| **임계점** | 리사이징이 발생하는 경계값 | capacity × load factor |
| **재해싱** | HashMap 리사이징 시 모든 엔트리를 재배치 | `resize()` 메서드 |

### 9-2. ArrayList vs HashMap 비교

| 특성 | ArrayList | HashMap |
|------|-----------|---------|
| **초기 용량** | 10 | 16 |
| **증가 방식** | 1.5배 | 2배 |
| **로드 팩터** | 없음 (100% 채워지면 확장) | 0.75 |
| **리사이징 비용** | 배열 복사 | 배열 복사 + 재해싱 |
| **사용 시기** | 순서 있는 데이터 | 키-값 쌍 저장 |

### 9-3. 초기 용량 지정 시점

```java
// ✅ 초기 용량을 지정해야 하는 경우
1. 최종 크기를 예측 가능한 경우
   List<String> list = new ArrayList<>(expectedSize);

2. 대용량 데이터 처리
   List<Data> bigData = new ArrayList<>(5_000_000);

3. 성능이 중요한 경우
   Map<String, Object> cache = new HashMap<>(10000, 0.75f);

4. 메모리 효율이 중요한 경우
   Set<Long> ids = new HashSet<>(userCount);

// ❌ 초기 용량 지정이 불필요한 경우
1. 크기를 예측할 수 없는 경우
   List<String> unknown = new ArrayList<>();  // 기본값 사용

2. 소량의 데이터 (< 100개)
   List<String> small = new ArrayList<>();  // 오버헤드 미미

3. 빠르게 버려질 임시 컬렉션
   List<String> temp = new ArrayList<>();  // 최적화 불필요
```

---

## 10. 실무 체크리스트

### 10-1. 코드 리뷰 시 확인 사항
- [ ] 대용량 데이터 처리 시 초기 용량 지정 여부
- [ ] API 응답 크기를 예측 가능한 경우 최적화 여부
- [ ] 캐시 구현 시 적절한 초기 용량과 로드 팩터 설정
- [ ] 배치 작업에서 메모리 효율 고려 여부
- [ ] HashMap 사용 시 로드 팩터 조정 필요성 검토

### 10-2. 성능 최적화 가이드
```java
// Before: 최적화 전
@GetMapping("/products")
public List<ProductDto> getProducts() {
    List<Product> products = productRepository.findAll();
    List<ProductDto> dtos = new ArrayList<>();  // ❌

    for (Product p : products) {
        dtos.add(convertToDto(p));
    }
    return dtos;
}

// After: 최적화 후
@GetMapping("/products")
public List<ProductDto> getProducts() {
    List<Product> products = productRepository.findAll();
    List<ProductDto> dtos = new ArrayList<>(products.size());  // ✅

    for (Product p : products) {
        dtos.add(convertToDto(p));
    }
    return dtos;
}
```

### 10-3. 주의사항
1. **과도한 초기 용량 설정 금지**
   ```java
   // ❌ 나쁜 예: 필요 이상의 용량 설정
   List<String> list = new ArrayList<>(1_000_000);  // 실제로는 10개만 사용

   // ✅ 좋은 예: 적절한 용량 설정
   List<String> list = new ArrayList<>(10);
   ```

2. **로드 팩터 조정 신중히**
   ```java
   // ⚠️ 주의: 로드 팩터는 특별한 경우가 아니면 기본값(0.75) 유지
   Map<String, String> map = new HashMap<>(1000, 0.9f);  // 충돌 증가 가능
   ```

3. **크기 예측 어려운 경우 기본값 사용**
   ```java
   // ✅ 좋은 예: 크기를 모르면 기본값 사용
   List<String> list = new ArrayList<>();  // 기본 용량 10
   ```

---

## 11. 관련 개념

### 11-1. 연관 주제
- **배열 vs 리스트**: 고정 크기 vs 가변 크기 자료구조
- **해시 충돌 해결**: 체이닝, 오픈 어드레싱
- **메모리 최적화**: GC 튜닝, 객체 풀링
- **시간 복잡도**: 리사이징의 Amortized O(1) 분석

### 11-2. 더 알아보기
- Java Collections Framework 내부 구조
- ArrayList의 `ensureCapacity()` 메서드
- HashMap의 `TreeNode` 변환 (Java 8+)
- ConcurrentHashMap의 동시성 제어 메커니즘
