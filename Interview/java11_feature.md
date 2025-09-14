# TIL - Java 11 주요 특징 정리

오늘은 Java 11의 주요 특징과 변화, 그리고 Java 8 → Java 11 마이그레이션 시 주의사항까지 정리했습니다.

---

## 1. 새로운 문자열 메소드 추가

- `isBlank()`, `lines()`, `strip()`, `repeat(int n)`

```java
String str = " Hello World ";
System.out.println(str.isBlank()); // false
System.out.println("Hello\nWorld".lines().count()); // 2
System.out.println(str.strip()); // "Hello World"
System.out.println("Java".repeat(3)); // JavaJavaJava
```

---

## 2. `var` 키워드 확장 (람다 매개변수에서 사용 가능)

```java
BiFunction<Integer, Integer, Integer> add = (var x, var y) -> x + y;
```

---

## 3. HTTP Client 표준화

```java
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
    .uri(URI.create("https://example.com"))
    .build();

HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
System.out.println(response.body());
```

---

## 4. 런타임에서 JDK 내부 모듈 제거

- `java.se.ee` 모듈 제거 (JAXB, JAX-WS, CORBA 등)

---

## 5. ZGC (Z Garbage Collector) 추가 (Experimental)

```bash
-XX:+UnlockExperimentalVMOptions -XX:+UseZGC
```

---

## 6. Epsilon GC (No-Op GC) 추가

```bash
-XX:+UseEpsilonGC
```

---

## 7. 기타 변화

- JDK Flight Recorder 오픈소스화
- Single-File Source-Code Program 지원
- Local Variable Syntax 개선

```bash
java HelloWorld.java
```

---

# Java 8 → Java 11 마이그레이션 주의사항

Java 8에서 Java 11로 넘어갈 때 반드시 신경 써야 할 주요 사항들을 정리했습니다.

---

## 1. 제거된 모듈에 주의

- **Java EE 및 CORBA 관련 API**가 기본 포함되지 않음
    - 예: `javax.xml.bind` (JAXB), `javax.annotation`, `javax.jws` 등이 사라짐
    - 해결 방법: 필요한 경우 외부 라이브러리 추가 (Maven/Gradle 의존성으로 별도 추가)

```xml
<!-- 예: JAXB API 추가 -->
<dependency>
  <groupId>javax.xml.bind</groupId>
  <artifactId>jaxb-api</artifactId>
  <version>2.3.1</version>
</dependency>
```

---

## 2. `var` 키워드 주의

- Java 10부터 `var`가 도입되었기 때문에,
    - 변수 이름을 `var`로 사용하는 코드는 **컴파일 에러** 발생할 수 있음.

---

## 3. 문자열 관련 동작 차이

- `String.strip()`, `isBlank()` 등 새로운 메서드 존재.
- `trim()` 과 `strip()` 은 동작이 다름 (`strip()`은 유니코드 공백도 제거).

---

## 4. 새로운 JVM 옵션 확인

- GC 관련 새로운 옵션 등장 (ZGC, EpsilonGC 등)
- 기존 옵션이 deprecated 되었을 수도 있으니 JVM 튜닝 옵션 점검 필수.

---

## 5. Single-File Source Code 실행

- `.java` 파일을 컴파일하지 않고 바로 실행할 수 있지만,
- 빌드 시스템(Maven, Gradle)에서는 여전히 별도 컴파일 과정 필요.

---

## 6. Optional 및 컬렉션 변경사항 주의

- `Optional.isEmpty()` 메서드 추가됨 (Java 11부터).
- 만약 프로젝트에서 `isEmpty()`를 사용할 경우, **Java 11 이상**에서만 동작.

```java
Optional<String> value = Optional.empty();
System.out.println(value.isEmpty()); // true
```

---

## 7. 빌드 도구 버전 점검

- Maven, Gradle 같은 빌드 도구도 Java 11을 제대로 지원하는 버전으로 업그레이드 필요.
    - Maven 3.6 이상
    - Gradle 5 이상

---

# 마무리

Java 8 → Java 11 마이그레이션은 단순한 버전 업그레이드 이상의 의미가 있습니다.  
기존 프로젝트가 사용하는 라이브러리, 빌드 도구, JVM 옵션 등을 종합적으로 점검하고, **제거된 모듈**과 **새 기능 호환성**을 꼭 확인해야 합니다.

