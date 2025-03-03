# Optional vs null을 사용하는 이유

## 1. Optional이란?
Optional은 Java 8에서 도입된 클래스로, null이 될 수 있는 객체를 감싸는 래퍼 클래스입니다.
Optional은 NPE(NullPointerException)를 방지하고 null 체크 로직을 더 간단하고 명확하게 작성할 수 있게 도와줍니다.

## 2. Optional을 사용하는 이유

### 2.1. NPE 방지
```java
// 기존 null을 사용할 경우
String name = user.getAddress().getCity(); // NPE 발생 가능

// Optional 사용
Optional<User> user = Optional.ofNullable(getUser());
String city = user
    .map(User::getAddress)
    .map(Address::getCity)
    .orElse("Unknown");
```

### 2.2. 명시적인 null 가능성 표현
- Optional을 사용하면 메서드의 반환 값이 null일 수 있다는 것을 타입 시스템을 통해 명시적으로 표현할 수 있습니다.

```java
// null 반환 가능성이 불명확
public User findUser(String id) {
    // null을 반환할 수 있음
    return userRepository.findById(id);
}

// 반환 값이 없을 수 있음을 명시적으로 표현
public Optional<User> findUser(String id) {
    return Optional.ofNullable(userRepository.findById(id));
}
```

### 2.3. 함수형 프로그래밍 스타일 지원
Optional은 map, filter, flatMap 등의 메서드를 제공하여 함수형 프로그래밍 스타일의 코드를 작성할 수 있게 해줍니다.

``` java
// 전통적인 null 체크
User user = getUser();
if (user != null) {
    Address address = user.getAddress();
    if (address != null) {
        String city = address.getCity();
        if (city != null && city.equals("Seoul")) {
            System.out.println("서울 거주자입니다.");
        }
    }
}

// Optional을 사용한 함수형 스타일
Optional.ofNullable(getUser())
    .map(User::getAddress)
    .map(Address::getCity)
    .filter("Seoul"::equals)
    .ifPresent(city -> System.out.println("서울 거주자입니다."));
```

### 2.4. 더 안전한 값 처리
Optional은 값이 없는 경우에 대한 다양한 대체 방법을 제공합니다.

``` java
// 기본값 제공
String name = Optional.ofNullable(user.getName())
    .orElse("Unknown");

// 예외 발생
String name = Optional.ofNullable(user.getName())
    .orElseThrow(() -> new UserNotFoundException("사용자 이름이 없습니다."));

// 조건부 실행
Optional.ofNullable(user.getName())
    .ifPresent(name -> System.out.println("Hello, " + name));
```

## 3. Optional 사용 시 주의사항

### 3.1. Optional을 필드로 사용하지 않기
```java
// 잘못된 사용
public class User {
    private Optional<String> name; // X!!
}

// 올바른 사용
public class User {
    private String name;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }
}
```

### 3.2. Optional을 메서드 파라미터로 사용하지 않기
```java
// 잘못된 사용
public void processUser(Optional<User> user) { }

// 올바른 사용
public void processUser(User user) {
    Optional.ofNullable(user)
        .ifPresent(this::process);
}
```

### 3.3. 컬렉션의 경우 Optional 대신 빈 컬렉션 반환
```java
// 불필요한 Optional 사용
public Optional<List<User>> getUsers() {
    List<User> users = userRepository.findAll();
    return Optional.ofNullable(users);
}

// 빈 컬렉션 반환
public List<User> getUsers() {
    List<User> users = userRepository.findAll();
    return users != null ? users : Collections.emptyList();
}
```

## 4. 결론
Optional을 사용하면:
- NPE를 방지할 수 있습니다.
- 코드의 의도를 더 명확하게 표현할 수 있습니다.
- 함수형 프로그래밍 스타일의 코드를 작성할 수 있습니다.
- null 처리를 위한 `보일러플레이트` 코드를 줄일 수 있습니다.

하지만 `Optional`을 올바르게 사용하기 위해서는 주의사항을 잘 지켜야 하며,
모든 null 상황에 Optional을 사용하는 것이 아니라 적절한 상황에 사용하는 것이 중요합니다.
