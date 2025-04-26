# Java 8의 주요 특징과 기능


## 1. 람다 표현식 (Lambda Expressions)

람다 표현식은 Java 8에서 가장 중요한 기능 중 하나로, 함수형 프로그래밍을 Java에 도입했습니다. 람다 표현식은 익명 함수를 간결하게 표현하는 방법을 제공합니다.

### 기본 문법

```java
// 기존 방식: 익명 클래스
Runnable runnable1 = new Runnable() {
    @Override
    public void run() {
        System.out.println("Hello World!");
    }
};

// 람다 표현식
Runnable runnable2 = () -> System.out.println("Hello World!");

// 매개변수가 있는 람다 표현식
Consumer<String> consumer = (String s) -> System.out.println(s);

// 타입 추론을 사용한 간결한 표현
Consumer<String> consumer2 = s -> System.out.println(s);

// 여러 문장이 있는 람다 표현식
Consumer<String> consumer3 = s -> {
    String result = s.toUpperCase();
    System.out.println(result);
};
```

### 장점

- 코드의 간결성
- 가독성 향상
- 함수형 인터페이스와 결합하여 강력한 기능 제공
- 병렬 처리에 용이

## 2. 함수형 인터페이스 (Functional Interfaces)

함수형 인터페이스는 단 하나의 추상 메서드만을 가진 인터페이스로, 람다 표현식과 함께 사용됩니다. Java 8은 `java.util.function` 패키지에 다양한 함수형 인터페이스를 제공합니다.

### 주요 함수형 인터페이스

```java
// Function<T, R>: T 타입을 입력받아 R 타입을 반환
Function<String, Integer> strLength = s -> s.length();
Integer length = strLength.apply("Hello"); // 5

// Predicate<T>: T 타입을 입력받아 boolean을 반환
Predicate<String> isEmpty = s -> s.isEmpty();
boolean result = isEmpty.test(""); // true

// Consumer<T>: T 타입을 입력받고 반환값이 없음
Consumer<String> printer = s -> System.out.println(s);
printer.accept("Hello World"); // Hello World 출력

// Supplier<T>: 입력 없이 T 타입의 결과를 제공
Supplier<Double> random = () -> Math.random();
Double value = random.get(); // 랜덤 값 반환

// BiFunction<T, U, R>: T와 U 타입을 입력받아 R 타입을 반환
BiFunction<String, String, String> concat = (s1, s2) -> s1 + s2;
String combined = concat.apply("Hello ", "World"); // "Hello World"
```

### @FunctionalInterface 어노테이션

함수형 인터페이스임을 명시적으로 선언하는 어노테이션입니다.

```java
@FunctionalInterface
public interface MyFunction {
    void execute();
    // 두 번째 추상 메서드를 추가하면 컴파일 오류 발생
}
```

## 3. 스트림 API (Stream API)

스트림 API는 컬렉션 데이터를 선언적으로 처리할 수 있는 기능을 제공합니다. 데이터 소스에 대한 복잡한 처리, 필터링, 매핑 등의 작업을 간결하게 표현할 수 있습니다.

### 스트림 생성 및 사용

```java
List<String> names = Arrays.asList("John", "Jane", "Adam", "Tom", "Alice");

// 필터링
List<String> filteredNames = names.stream()
    .filter(name -> name.startsWith("J"))
    .collect(Collectors.toList()); // [John, Jane]

// 매핑
List<Integer> nameLengths = names.stream()
    .map(String::length)
    .collect(Collectors.toList()); // [4, 4, 4, 3, 5]

// 정렬
List<String> sortedNames = names.stream()
    .sorted()
    .collect(Collectors.toList()); // [Adam, Alice, Jane, John, Tom]

// 집계 함수
long count = names.stream()
    .filter(name -> name.length() > 3)
    .count(); // 4

// 병렬 처리
List<String> parallelResult = names.parallelStream()
    .filter(name -> name.length() > 3)
    .collect(Collectors.toList());
```

### 중간 연산과 최종 연산

- **중간 연산**: 다른 스트림을 반환하며, 여러 번 호출 가능 (filter, map, sorted 등)
- **최종 연산**: 스트림을 소비하고 결과를 반환 (collect, count, forEach 등)

## 4. 메서드 레퍼런스 (Method References)

메서드 레퍼런스는 람다 표현식을 더 간결하게 표현하는 방법으로, 이미 이름이 있는 메서드를 참조할 수 있습니다.

### 유형

```java
// 정적 메서드 참조: ClassName::staticMethodName
Function<String, Integer> parser = Integer::parseInt;

// 인스턴스 메서드 참조: instance::methodName
String str = "hello";
Supplier<Integer> lengthSupplier = str::length;

// 특정 타입의 인스턴스 메서드 참조: ClassName::methodName
Function<String, Integer> lengthFunc = String::length;

// 생성자 참조: ClassName::new
Supplier<List<String>> listSupplier = ArrayList::new;
```

## 5. 디폴트 메서드 (Default Methods)

인터페이스에 구현체를 제공하는 메서드를 정의할 수 있게 되었습니다. 이를 통해 기존 인터페이스에 새로운 기능을 추가하면서도 하위 호환성을 유지할 수 있습니다.

```java
public interface Vehicle {
    void accelerate();
    
    // 디폴트 메서드
    default void honk() {
        System.out.println("Beep!");
    }
}

// 구현 클래스는 honk() 메서드를 구현하지 않아도 됨
public class Car implements Vehicle {
    @Override
    public void accelerate() {
        System.out.println("Car is accelerating");
    }
    
    // honk() 메서드는 오버라이드 가능
    @Override
    public void honk() {
        System.out.println("Car horn!");
    }
}
```

## 6. 정적 메서드 (Static Methods in Interfaces)

Java 8부터 인터페이스에 정적 메서드를 정의할 수 있게 되었습니다.

```java
public interface MathOperations {
    // 정적 메서드
    static int add(int a, int b) {
        return a + b;
    }
    
    void subtract(int a, int b);
}

// 정적 메서드 사용
int result = MathOperations.add(5, 3); // 8
```

## 7. Optional 클래스

`null` 참조로 인한 `NullPointerException`을 방지하기 위한 컨테이너 객체입니다.

```java
// Optional 생성
Optional<String> optional1 = Optional.empty(); // 빈 Optional
Optional<String> optional2 = Optional.of("Hello"); // null이 아닌 값으로 생성
Optional<String> optional3 = Optional.ofNullable(getValueMayBeNull()); // null일 수도 있는 값으로 생성

// Optional 사용
String result = optional2
    .map(String::toUpperCase)
    .orElse("Default Value"); // "HELLO"

// 조건부 실행
optional2.ifPresent(s -> System.out.println(s)); // "Hello" 출력

// 예외 발생
String value = optional1.orElseThrow(() -> new NoSuchElementException()); // 예외 발생
```

## 8. 새로운 날짜와 시간 API

기존의 `Date`와 `Calendar` 클래스의 문제점을 해결한 새로운 날짜와 시간 API가 `java.time` 패키지에 추가되었습니다.

```java
// 현재 날짜
LocalDate today = LocalDate.now(); // 2023-05-15

// 특정 날짜
LocalDate date = LocalDate.of(2023, 5, 15); // 2023-05-15

// 현재 시간
LocalTime time = LocalTime.now(); // 14:30:45.123

// 날짜와 시간
LocalDateTime dateTime = LocalDateTime.now(); // 2023-05-15T14:30:45.123

// 시간대 적용
ZonedDateTime zonedDateTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul")); // 2023-05-15T14:30:45.123+09:00[Asia/Seoul]

// 날짜 연산
LocalDate tomorrow = today.plusDays(1);
LocalDate lastMonth = today.minusMonths(1);

// 기간 계산
Period period = Period.between(date, today);
long days = ChronoUnit.DAYS.between(date, today);

// 포맷팅
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
String formattedDateTime = dateTime.format(formatter); // "2023-05-15 14:30:45"
```

## 9. 나즈혼 (Nashorn) JavaScript 엔진

Java 8은 기존의 Rhino 엔진을 대체하는 새로운 JavaScript 엔진인 Nashorn을 도입했습니다. Nashorn은 JVM 위에서 JavaScript를 실행하는 성능을 크게 향상시켰습니다.

```java
// JavaScript 코드 실행
ScriptEngineManager manager = new ScriptEngineManager();
ScriptEngine engine = manager.getEngineByName("nashorn");

// JavaScript 코드 평가
engine.eval("print('Hello from JavaScript!');");

// Java 객체를 JavaScript에 전달
engine.put("person", new Person("John", 30));
engine.eval("print(person.getName() + ' is ' + person.getAge() + ' years old');");
```

## 10. 병렬 배열 처리

`Arrays` 클래스에 병렬 처리를 위한 새로운 메서드들이 추가되었습니다.

```java
int[] numbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};

// 병렬 정렬
Arrays.parallelSort(numbers);

// 병렬 접두사 연산
Arrays.parallelPrefix(numbers, (x, y) -> x + y);
// numbers = {1, 3, 6, 10, 15, 21, 28, 36, 45, 55}

// 병렬 설정
Arrays.parallelSetAll(numbers, i -> i * 2);
// numbers = {0, 2, 4, 6, 8, 10, 12, 14, 16, 18}
```

## 결론

Java 8은 함수형 프로그래밍 패러다임을 Java에 도입하고, 코드를 더 간결하고 가독성 있게 작성할 수 있는 다양한 기능을 제공했습니다. 람다 표현식, 스트림 API, 메서드 레퍼런스, 디폴트 메서드 등의 기능은 현대적인 Java 프로그래밍의 기반이 되었으며, 이후 버전의 Java에서도 이러한 기능들이 계속 발전하고 있습니다.