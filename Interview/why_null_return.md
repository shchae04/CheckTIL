# 왜 NULL 리턴을 하면 안될까요?

소프트웨어 개발에서 함수나 메서드가 `NULL`을 반환하는 것은 코드의 복잡성을 증가시키고, 유지보수성을 저하시킬 수 있습니다. 그 이유는 다음과 같습니다.

## 1. 의미의 축소

`NULL`은 다양한 상황을 하나의 값으로 표현하여, 코드 읽는 이로 하여금 그 정확한 의미를 파악하기 어렵게 만든다. 예를 들어, 다음과 같은 코드가 있다고 가정해보자.

```java
User user = userRepository.findByName("TEST");
System.out.println(user); // nullable
```

여기서 `user`가 `NULL`인 경우, 그 원인은 여러 가지일 수 있다.

- 데이터베이스에 "TEST"이라는 이름의 사용자가 존재하지 않는다.
- 데이터베이스 연결에 문제가 발생했다.
- "TEST" 사용자가 탈퇴했다.
- 데이터 동기화 문제로 인해 사용자가 조회되지 않는다.

이처럼 `NULL`은 다양한 원인을 내포할 수 있어, 코드의 의미를 축소시키고 이해를 어렵게 만든다.

## 2. 예외 처리의 복잡성 증가

`NULL`을 반환하면, 이를 호출하는 모든 곳에서 `NULL` 체크를 해야 한다. 이는 코드의 중복을 초래하고, 실수로 `NULL` 체크를 누락하면 `NullPointerException`과 같은 런타임 에러를 발생시킬 수 있다.

```java
User user = userRepository.findByName("김개발");
if (user != null) {
    // 사용자 정보 처리
} else {
    // NULL인 경우 처리
}
```

이러한 `NULL` 체크는 코드의 가독성을 떨어뜨리고, 유지보수를 어렵게 만든다.

## 3. 대안: 명시적인 에러 처리

`NULL`을 반환하는 대신, 실패의 원인을 명시적으로 전달하는 것이 좋다. 예를 들어, `Optional`을 사용하여 `NULL`을 방지할 수 있다.

```java
Optional<User> user = userRepository.findByName("김개발");
user.ifPresentOrElse(
    u -> {
        // 사용자 정보 처리
    },
    () -> {
        // 사용자 없음 처리
    }
);
```

또는 `Result` 클래스를 만들어 성공과 실패를 명확히 구분할 수도 있다.

```java
class Result<T> {
    private final T value;
    private final Exception error;

    private Result(T value, Exception error) {
        this.value = value;
        this.error = error;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, null);
    }

    public static <T> Result<T> failure(Exception error) {
        return new Result<>(null, error);
    }

    public boolean isSuccess() {
        return value != null;
    }

    public T getValue() {
        return value;
    }

    public Exception getError() {
        return error;
    }
}

Result<User> result = userRepository.findByName("김개발");

if (result.isSuccess()) {
    User user = result.getValue();
    // 사용자 정보 처리
} else {
    Exception error = result.getError();
    // 에러 처리
}
```

이러한 접근 방식은 코드의 명확성을 높이고, 예외 상황을 체계적으로 관리할 수 있게 해줍니다.

