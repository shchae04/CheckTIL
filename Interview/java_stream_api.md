# Java Stream API 사용법과 예시

Java 8에서 도입된 Stream API는 컬렉션 데이터를 선언적으로 처리할 수 있는 강력한 기능입니다. 함수형 프로그래밍 스타일을 지원하며, 데이터 처리 파이프라인을 구성하여 복잡한 데이터 처리 작업을 간결하고 가독성 높게 작성할 수 있습니다.

## 1. Stream API 개요

Stream API는 데이터 소스를 추상화하고, 데이터를 다루는데 자주 사용되는 함수들을 정의해 놓은 API입니다. 이를 통해 다음과 같은 이점을 얻을 수 있습니다:

- **선언적 프로그래밍**: 어떻게(how) 데이터를 처리할지가 아닌, 무엇을(what) 할지 명시
- **파이프라이닝**: 여러 연산을 연결하여 복잡한 데이터 처리 파이프라인 구성
- **내부 반복**: 컬렉션 내부에서 요소들을 반복 처리
- **병렬 처리**: 멀티코어 아키텍처를 활용한 병렬 처리 지원

## 2. Stream 생성 방법

### 2.1 컬렉션에서 스트림 생성

```java
// List에서 스트림 생성
List<String> list = Arrays.asList("a", "b", "c");
Stream<String> stream = list.stream();

// Set에서 스트림 생성
Set<String> set = new HashSet<>(Arrays.asList("a", "b", "c"));
Stream<String> setStream = set.stream();

// Map에서 스트림 생성
Map<String, Integer> map = new HashMap<>();
map.put("A", 1);
map.put("B", 2);
map.put("C", 3);

// Map.Entry 스트림 생성
Stream<Map.Entry<String, Integer>> entryStream = map.entrySet().stream();

// 키 스트림 생성
Stream<String> keyStream = map.keySet().stream();

// 값 스트림 생성
Stream<Integer> valueStream = map.values().stream();
```

### 2.2 배열에서 스트림 생성

```java
// 배열에서 스트림 생성
String[] array = {"a", "b", "c"};
Stream<String> streamFromArray = Arrays.stream(array);

// 배열의 일부분으로 스트림 생성
Stream<String> streamFromArrayPart = Arrays.stream(array, 1, 3); // "b", "c"
```

### 2.3 스트림 빌더 사용

```java
// Stream.builder() 사용
Stream<String> streamBuilder = Stream.<String>builder()
    .add("a")
    .add("b")
    .add("c")
    .build();
```

### 2.4 스트림 생성 팩토리 메소드

```java
// Stream.of() 사용
Stream<String> streamOf = Stream.of("a", "b", "c");

// 빈 스트림 생성
Stream<String> emptyStream = Stream.empty();

// 무한 스트림 - iterate
Stream<Integer> iteratedStream = Stream.iterate(0, n -> n + 2).limit(5); // 0, 2, 4, 6, 8

// 무한 스트림 - generate
Stream<Double> generatedStream = Stream.generate(Math::random).limit(5);
```

### 2.5 기본 타입 스트림

```java
// IntStream, LongStream, DoubleStream
IntStream intStream = IntStream.range(1, 5); // 1, 2, 3, 4
LongStream longStream = LongStream.rangeClosed(1, 5); // 1, 2, 3, 4, 5

// 박싱/언박싱 변환
Stream<Integer> boxedStream = IntStream.range(1, 5).boxed();
IntStream intStreamAgain = boxedStream.mapToInt(Integer::intValue);
```

## 3. 중간 연산 (Intermediate Operations)

중간 연산은 다른 스트림을 반환하므로 여러 중간 연산을 연결할 수 있습니다. 중간 연산은 지연 실행(lazy evaluation)되며, 최종 연산이 호출될 때까지 실행되지 않습니다.

### 3.1 필터링 (filter, distinct)

```java
// filter: 조건에 맞는 요소만 선택
Stream<String> filtered = Stream.of("a", "b", "c")
    .filter(element -> element.contains("a")); // "a"

// distinct: 중복 제거
Stream<String> distinct = Stream.of("a", "a", "b", "c")
    .distinct(); // "a", "b", "c"
```

### 3.2 변환 (map, flatMap)

```java
// map: 각 요소를 변환
Stream<String> mapped = Stream.of("a", "b", "c")
    .map(String::toUpperCase); // "A", "B", "C"

// flatMap: 각 요소를 스트림으로 변환 후 하나의 스트림으로 평면화
Stream<String> flatMapped = Stream.of("a,b", "c,d")
    .flatMap(e -> Arrays.stream(e.split(","))); // "a", "b", "c", "d"
```

### 3.3 제한 (limit, skip)

```java
// limit: 처음 n개 요소로 제한
Stream<String> limited = Stream.of("a", "b", "c")
    .limit(2); // "a", "b"

// skip: 처음 n개 요소를 건너뜀
Stream<String> skipped = Stream.of("a", "b", "c")
    .skip(1); // "b", "c"
```

### 3.4 정렬 (sorted)

```java
// 기본 정렬
Stream<String> sorted = Stream.of("c", "a", "b")
    .sorted(); // "a", "b", "c"

// 커스텀 정렬
Stream<String> customSorted = Stream.of("ccc", "a", "bb")
    .sorted(Comparator.comparing(String::length)); // "a", "bb", "ccc"

// 역순 정렬
Stream<String> reverseSorted = Stream.of("a", "b", "c")
    .sorted(Comparator.reverseOrder()); // "c", "b", "a"
```

### 3.5 요소 탐색 (peek)

```java
// peek: 각 요소를 소비하면서 스트림을 그대로 반환 (디버깅용)
Stream<String> peeked = Stream.of("a", "b", "c")
    .peek(System.out::println)
    .map(String::toUpperCase);
```

## 4. 최종 연산 (Terminal Operations)

최종 연산은 스트림 파이프라인에서 결과를 도출합니다. 최종 연산이 수행되면 스트림 파이프라인은 소비되고 더 이상 사용할 수 없습니다.

### 4.1 요소 소비 (forEach, forEachOrdered)

```java
// forEach: 각 요소를 소비 (순서 보장 안 함)
Stream.of("a", "b", "c").forEach(System.out::println);

// forEachOrdered: 각 요소를 소비 (순서 보장)
Stream.of("a", "b", "c").forEachOrdered(System.out::println);
```

### 4.2 요소 검색 (findFirst, findAny)

```java
// findFirst: 첫 번째 요소 반환
Optional<String> first = Stream.of("a", "b", "c").findFirst(); // Optional["a"]

// findAny: 아무 요소나 반환 (병렬 처리에 유용)
Optional<String> any = Stream.of("a", "b", "c").findAny(); // Optional["a"] (보통)
```

### 4.3 요소 매칭 (anyMatch, allMatch, noneMatch)

```java
// anyMatch: 하나라도 조건을 만족하는지 확인
boolean anyMatch = Stream.of("a", "b", "c")
    .anyMatch(element -> element.contains("a")); // true

// allMatch: 모든 요소가 조건을 만족하는지 확인
boolean allMatch = Stream.of("a", "b", "c")
    .allMatch(element -> element.length() == 1); // true

// noneMatch: 모든 요소가 조건을 만족하지 않는지 확인
boolean noneMatch = Stream.of("a", "b", "c")
    .noneMatch(element -> element.contains("d")); // true
```

### 4.4 요소 집계 (count, min, max)

```java
// count: 요소 개수 반환
long count = Stream.of("a", "b", "c").count(); // 3

// min: 최솟값 반환
Optional<String> min = Stream.of("a", "b", "c")
    .min(Comparator.naturalOrder()); // Optional["a"]

// max: 최댓값 반환
Optional<String> max = Stream.of("a", "b", "c")
    .max(Comparator.naturalOrder()); // Optional["c"]
```

### 4.5 요소 리듀싱 (reduce)

```java
// reduce: 요소를 하나로 줄임
Optional<String> reduced = Stream.of("a", "b", "c")
    .reduce((a, b) -> a + b); // Optional["abc"]

// 초기값이 있는 reduce
String reducedWithIdentity = Stream.of("a", "b", "c")
    .reduce("", (a, b) -> a + b); // "abc"

// 결과 타입이 다른 reduce
int sumOfLengths = Stream.of("a", "bb", "ccc")
    .reduce(0, (sum, str) -> sum + str.length(), Integer::sum); // 6
```

### 4.6 요소 수집 (collect)

```java
// 리스트로 수집
List<String> list = Stream.of("a", "b", "c")
    .collect(Collectors.toList());

// 세트로 수집
Set<String> set = Stream.of("a", "a", "b", "c")
    .collect(Collectors.toSet());

// 맵으로 수집
Map<String, Integer> map = Stream.of("a", "bb", "ccc")
    .collect(Collectors.toMap(
        Function.identity(),
        String::length
    ));

// 문자열로 조인
String joined = Stream.of("a", "b", "c")
    .collect(Collectors.joining(", ")); // "a, b, c"

// 그룹화
Map<Integer, List<String>> grouped = Stream.of("a", "bb", "ccc", "dd")
    .collect(Collectors.groupingBy(String::length));
// {1=["a"], 2=["bb", "dd"], 3=["ccc"]}

// 분할
Map<Boolean, List<String>> partitioned = Stream.of("a", "bb", "ccc")
    .collect(Collectors.partitioningBy(s -> s.length() > 1));
// {false=["a"], true=["bb", "ccc"]}

// 통계
IntSummaryStatistics stats = Stream.of("a", "bb", "ccc")
    .collect(Collectors.summarizingInt(String::length));
// count=3, sum=6, min=1, average=2.0, max=3
```

## 5. 병렬 스트림 (Parallel Streams)

병렬 스트림은 멀티코어 아키텍처를 활용하여 스트림 연산을 병렬로 처리합니다.

```java
// 컬렉션에서 병렬 스트림 생성
List<String> list = Arrays.asList("a", "b", "c");
Stream<String> parallelStream = list.parallelStream();

// 일반 스트림을 병렬 스트림으로 변환
Stream<String> parallel = Stream.of("a", "b", "c").parallel();

// 병렬 스트림 사용 예
long count = list.parallelStream()
    .filter(element -> element.length() > 0)
    .count();
```

## 6. 실용적인 예제

### 6.1 파일 처리

```java
// 파일의 모든 라인을 읽어 처리
try (Stream<String> lines = Files.lines(Paths.get("file.txt"))) {
    lines.filter(line -> line.contains("Java"))
         .map(String::trim)
         .forEach(System.out::println);
} catch (IOException e) {
    e.printStackTrace();
}
```

### 6.2 객체 리스트 처리

```java
class Person {
    private String name;
    private int age;
    
    // 생성자, getter, setter 생략
    
    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }
    
    public String getName() { return name; }
    public int getAge() { return age; }
}

// 사람 목록 생성
List<Person> persons = Arrays.asList(
    new Person("Alice", 25),
    new Person("Bob", 30),
    new Person("Charlie", 35),
    new Person("David", 40)
);

// 나이가 30 이상인 사람들의 이름을 알파벳 순으로 정렬하여 출력
persons.stream()
    .filter(p -> p.getAge() >= 30)
    .map(Person::getName)
    .sorted()
    .forEach(System.out::println);
// Charlie, David

// 모든 사람의 평균 나이 계산
double averageAge = persons.stream()
    .mapToInt(Person::getAge)
    .average()
    .orElse(0.0);
// 32.5

// 이름 길이별로 그룹화
Map<Integer, List<Person>> personsByNameLength = persons.stream()
    .collect(Collectors.groupingBy(p -> p.getName().length()));
```

### 6.3 숫자 처리

```java
// 1부터 100까지의 숫자 중 짝수의 합 계산
int sumOfEvens = IntStream.rangeClosed(1, 100)
    .filter(n -> n % 2 == 0)
    .sum();
// 2550

// 1부터 10까지의 숫자의 제곱 계산
List<Integer> squares = IntStream.rangeClosed(1, 10)
    .map(n -> n * n)
    .boxed()
    .collect(Collectors.toList());
// [1, 4, 9, 16, 25, 36, 49, 64, 81, 100]

// 소수 찾기 (2부터 20까지)
List<Integer> primes = IntStream.rangeClosed(2, 20)
    .filter(n -> IntStream.rangeClosed(2, (int) Math.sqrt(n))
                          .allMatch(i -> n % i != 0))
    .boxed()
    .collect(Collectors.toList());
// [2, 3, 5, 7, 11, 13, 17, 19]
```

## 7. 스트림 API 사용 시 주의사항

1. **스트림은 재사용할 수 없습니다.**
   ```java
   Stream<String> stream = Stream.of("a", "b", "c");
   stream.forEach(System.out::println);
   // 아래 코드는 IllegalStateException 발생
   stream.forEach(System.out::println);
   ```

2. **스트림 연산은 지연 실행됩니다.**
   ```java
   Stream<String> stream = Stream.of("a", "b", "c")
       .filter(element -> {
           System.out.println("필터: " + element);
           return element.contains("a");
       });
   // 아직 아무것도 출력되지 않음
   
   stream.forEach(element -> System.out.println("forEach: " + element));
   // 이제 출력됨
   // 필터: a
   // forEach: a
   // 필터: b
   // 필터: c
   ```

3. **병렬 스트림은 항상 더 빠른 것은 아닙니다.**
   - 데이터 크기가 작거나 연산이 간단한 경우 오히려 오버헤드가 발생할 수 있습니다.
   - 공유 상태를 수정하는 경우 동기화 문제가 발생할 수 있습니다.

4. **무한 스트림을 사용할 때는 limit()이나 findFirst() 같은 제한 연산을 함께 사용해야 합니다.**
   ```java
   // 무한 루프 발생
   // Stream.iterate(0, n -> n + 1).forEach(System.out::println);
   
   // 올바른 사용법
   Stream.iterate(0, n -> n + 1)
       .limit(10)
       .forEach(System.out::println);
   ```

## 8. 결론

Java Stream API는 컬렉션 데이터를 효율적으로 처리할 수 있는 강력한 도구입니다. 함수형 프로그래밍 스타일을 통해 코드의 가독성을 높이고, 병렬 처리를 쉽게 구현할 수 있습니다. 적절한 상황에서 Stream API를 활용하면 더 간결하고 유지보수하기 쉬운 코드를 작성할 수 있습니다.

Stream API의 핵심은 선언적 프로그래밍 방식으로, "무엇을 할 것인가"에 집중하고 "어떻게 할 것인가"는 Stream API에 위임하는 것입니다. 이를 통해 개발자는 비즈니스 로직에 더 집중할 수 있으며, 코드의 품질과 생산성을 향상시킬 수 있습니다.