# Java Optional

## 1. Optional의 도입 배경

Java에서 `null`을 사용하는 것은 오랫동안 많은 문제를 일으켜 왔습니다. `null`은 1965년 Tony Hoare에 의해 처음 도입되었는데, 그는 이를 자신의 "십억 달러짜리 실수(billion-dollar mistake)"라고 후회했습니다. Java에서 `null`로 인한 주요 문제점은 다음과 같습니다:

- **NullPointerException(NPE)**: Java에서 가장 흔한 런타임 예외 중 하나
- **코드 가독성 저하**: 과도한 null 체크 로직으로 인한 코드 복잡성 증가
- **의미 모호성**: `null`이 "값이 없음"을 의미하는지, "오류 상태"를 의미하는지, 또는 다른 의미인지 불분명함
- **API 설계의 어려움**: 메서드가 `null`을 반환할 수 있는지 명확하게 표현하기 어려움

이러한 문제를 해결하기 위해 다양한 프로그래밍 언어와 라이브러리에서 "Optional" 또는 "Maybe" 타입과 같은 개념이 등장했습니다. Java의 `Optional` 클래스는 이러한 패턴을 공식적으로 Java 언어에 도입한 것입니다.

## 2. Optional의 도입 시기와 영향

`Optional` 클래스는 Java 8(2014년 3월 출시)에서 처음 도입되었습니다. 이는 Java에 함수형 프로그래밍 패러다임을 도입한 큰 변화의 일부였습니다. `Optional`의 도입은 다음과 같은 영향을 미쳤습니다:

- **함수형 프로그래밍 지원**: Stream API와 함께 함수형 스타일의 코드 작성을 가능하게 함
- **API 설계 개선**: 메서드가 값을 반환하지 않을 수 있음을 타입 시스템을 통해 명시적으로 표현
- **null 안전성 향상**: NPE를 방지하고 더 안전한 코드 작성을 장려

`Optional`은 Scala의 `Option`, Haskell의 `Maybe`, Guava 라이브러리의 `Optional` 등 다른 언어와 라이브러리의 유사한 개념에서 영감을 받았습니다.

## 3. Optional의 기본 사용법

### 3.1 Optional 객체 생성

```java
// 비어있는 Optional 생성
Optional<String> empty = Optional.empty();

// null이 아닌 값으로 Optional 생성 (null이면 NullPointerException 발생)
Optional<String> opt1 = Optional.of("Hello");

// null일 수도 있는 값으로 Optional 생성
Optional<String> opt2 = Optional.ofNullable(getValue()); // getValue()가 null을 반환할 수 있음
```

### 3.2 Optional 값 접근

```java
// isPresent()로 값 존재 여부 확인
if (opt1.isPresent()) {
    System.out.println(opt1.get()); // 값이 있을 때만 get() 사용
}

// isEmpty() 메서드 (Java 11부터 지원)
if (empty.isEmpty()) {
    System.out.println("값이 없습니다.");
}

// ifPresent()로 값이 있을 때만 작업 수행
opt1.ifPresent(value -> System.out.println("값: " + value));

// orElse()로 기본값 제공
String result1 = empty.orElse("기본값");

// orElseGet()으로 기본값을 제공하는 함수 지정 (값이 없을 때만 함수 실행)
String result2 = empty.orElseGet(() -> getDefaultValue());

// orElseThrow()로 값이 없을 때 예외 발생
String result3 = empty.orElseThrow(() -> new NoSuchElementException("값이 없습니다."));
```

### 3.3 Optional 값 변환

```java
// map()으로 값 변환
Optional<Integer> length = opt1.map(String::length); // Optional<Integer> 반환

// flatMap()으로 중첩된 Optional 처리
Optional<Optional<String>> nested = Optional.of(Optional.of("nested"));
Optional<String> unnested = nested.flatMap(opt -> opt); // Optional<String> 반환

// filter()로 조건에 맞는 값만 유지
Optional<String> filtered = opt1.filter(s -> s.length() > 3); // 조건에 맞지 않으면 빈 Optional 반환
```

## 4. Optional 활용 예제

### 4.1 중첩된 객체 안전하게 접근하기

```java
// 전통적인 null 체크
public String getCityFromUser(User user) {
    if (user != null) {
        Address address = user.getAddress();
        if (address != null) {
            return address.getCity();
        }
    }
    return "Unknown";
}

// Optional 사용
public String getCityFromUser(User user) {
    return Optional.ofNullable(user)
            .map(User::getAddress)
            .map(Address::getCity)
            .orElse("Unknown");
}
```

### 4.2 조건부 로직 처리

```java
// 전통적인 방식
public void processUser(User user) {
    if (user != null && "admin".equals(user.getRole())) {
        System.out.println("관리자: " + user.getName());
    }
}

// Optional 사용
public void processUser(User user) {
    Optional.ofNullable(user)
            .filter(u -> "admin".equals(u.getRole()))
            .ifPresent(u -> System.out.println("관리자: " + u.getName()));
}
```

### 4.3 여러 Optional 조합하기

```java
public Optional<User> findUserById(String id) {
    // 데이터베이스에서 사용자 조회 로직
    return Optional.ofNullable(userRepository.findById(id));
}

public Optional<Order> findLatestOrderByUserId(String userId) {
    // 최근 주문 조회 로직
    return Optional.ofNullable(orderRepository.findLatestByUserId(userId));
}

// 두 Optional 조합하기
public Optional<OrderSummary> getLatestOrderSummary(String userId) {
    Optional<User> userOpt = findUserById(userId);
    Optional<Order> orderOpt = findLatestOrderByUserId(userId);
    
    return userOpt.flatMap(user -> 
        orderOpt.map(order -> 
            new OrderSummary(user.getName(), order.getProductName(), order.getAmount())
        )
    );
}
```

## 5. Optional 사용 시 주의사항

### 5.1 Optional을 필드로 사용하지 않기

```java
// 안티 패턴
public class User {
    private Optional<String> middleName; // 직렬화 문제 발생 가능
}

// 권장 패턴
public class User {
    private String middleName; // 필드는 일반 타입으로
    
    public Optional<String> getMiddleName() {
        return Optional.ofNullable(middleName); // 메서드에서 Optional 반환
    }
}
```

### 5.2 Optional을 메서드 매개변수로 사용하지 않기

```java
// 안티 패턴
public void processUser(Optional<User> userOpt) { // 호출 시 항상 Optional 객체 생성 필요
    userOpt.ifPresent(this::process);
}

// 권장 패턴
public void processUser(User user) { // null 허용
    Optional.ofNullable(user).ifPresent(this::process);
}
```

### 5.3 컬렉션의 경우 Optional 대신 빈 컬렉션 반환하기

```java
// 안티 패턴
public Optional<List<User>> getUsers() {
    List<User> users = userRepository.findAll();
    return Optional.ofNullable(users);
}

// 권장 패턴
public List<User> getUsers() {
    List<User> users = userRepository.findAll();
    return users != null ? users : Collections.emptyList();
}
```

### 5.4 Optional.get() 직접 호출 피하기

```java
// 안티 패턴
String value = optional.get(); // NoSuchElementException 발생 가능

// 권장 패턴
String value = optional.orElse("기본값");
// 또는
optional.ifPresent(v -> processValue(v));
```

### 5.5 성능에 민감한 코드에서 주의하기

Optional은 객체 래퍼이므로 추가적인 객체 생성 비용이 발생합니다. 성능이 중요한 코드에서는 사용을 신중하게 고려해야 합니다.

## 6. Java 버전별 Optional 기능 확장

### 6.1 Java 9 추가 기능

```java
// or() 메서드: 값이 없을 때 다른 Optional 제공
Optional<String> result = optional.or(() -> Optional.of("다른 값"));

// ifPresentOrElse(): 값이 있을 때와 없을 때 각각 다른 작업 수행
optional.ifPresentOrElse(
    value -> System.out.println("값: " + value),
    () -> System.out.println("값이 없습니다.")
);

// stream(): Optional을 Stream으로 변환 (0 또는 1개 요소를 가진 Stream)
Stream<String> stream = optional.stream();
```

### 6.2 Java 10 추가 기능

```java
// orElseThrow(): 인자 없이 NoSuchElementException 발생
String value = optional.orElseThrow(); // 값이 없으면 NoSuchElementException 발생
```

### 6.3 Java 11 추가 기능

```java
// isEmpty(): 값이 없는지 확인 (isPresent()의 반대)
if (optional.isEmpty()) {
    System.out.println("값이 없습니다.");
}
```

## 7. Optional과 다른 언어의 유사 개념 비교

### 7.1 Scala의 Option

```scala
// Scala의 Option
val option: Option[String] = Some("value")
val empty: Option[String] = None

// 패턴 매칭
option match {
  case Some(value) => println(s"값: $value")
  case None => println("값이 없습니다.")
}
```

### 7.2 Kotlin의 Nullable 타입

```kotlin
// Kotlin의 Nullable 타입
val nullable: String? = "value"
val length = nullable?.length // 안전 호출 연산자
val result = nullable ?: "기본값" // 엘비스 연산자
```

### 7.3 TypeScript의 Optional 체이닝

```typescript
// TypeScript의 Optional 체이닝
const user = getUser();
const city = user?.address?.city; // user나 address가 null/undefined면 undefined 반환
```

## 8. 결론

Java의 `Optional` 클래스는 `null` 참조로 인한 문제를 해결하기 위해 도입된 중요한 기능입니다. 적절하게 사용하면 다음과 같은 이점을 얻을 수 있습니다:

- **NPE 방지**: `null` 체크를 강제하여 NullPointerException 발생 가능성 감소
- **코드 가독성 향상**: 함수형 스타일의 코드로 복잡한 null 체크 로직을 간결하게 표현
- **API 설계 개선**: 메서드가 값을 반환하지 않을 수 있음을 명시적으로 표현

하지만 `Optional`을 효과적으로 사용하려면 주의사항을 잘 이해하고, 적절한 상황에서만 사용해야 합니다. 모든 `null` 참조를 `Optional`로 대체하는 것은 오히려 코드를 복잡하게 만들 수 있으므로, 메서드의 반환 값이 없을 수 있는 경우에 주로 사용하는 것이 좋습니다.