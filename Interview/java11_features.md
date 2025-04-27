# Java 11의 주요 특징과 기능


## 1. 새로운 String 메서드

Java 11에서는 String 클래스에 여러 유용한 메서드가 추가되었습니다.

```java
// isBlank(): 문자열이 비어있거나 공백만 포함하는지 확인
String str1 = "  ";
boolean blank = str1.isBlank(); // true

// lines(): 문자열을 줄 단위로 스트림으로 분할
String multiline = "Hello\nWorld\nJava 11";
Stream<String> lines = multiline.lines(); // ["Hello", "World", "Java 11"]

// strip(), stripLeading(), stripTrailing(): 공백 제거 (유니코드 인식)
String str2 = " Hello World ";
String stripped = str2.strip(); // "Hello World"
String leadingStripped = str2.stripLeading(); // "Hello World "
String trailingStripped = str2.stripTrailing(); // " Hello World"

// repeat(n): 문자열을 n번 반복
String str3 = "Java";
String repeated = str3.repeat(3); // "JavaJavaJava"
```

## 2. 람다 매개변수에 var 사용

Java 11에서는 람다 표현식의 매개변수에 var 키워드를 사용할 수 있게 되었습니다.

```java
// Java 10
// (var x, var y) -> x + y; // 컴파일 오류

// Java 11
// 람다 표현식에서 var 사용 가능
BiFunction<Integer, Integer, Integer> add = (var x, var y) -> x + y; // 정상 작동

// 어노테이션 사용 가능
BiFunction<Integer, Integer, Integer> addWithAnnotations = (@NotNull var x, @Nullable var y) -> x + y;
```

## 3. HTTP 클라이언트 API (표준화)

Java 9에서 인큐베이터 모듈로 도입되었던 HTTP 클라이언트 API가 Java 11에서 표준화되었습니다.

```java
// HTTP 클라이언트 사용 예제
// 동기 요청
HttpClient client = HttpClient.newHttpClient();
HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://example.com"))
        .build();

// 요청 보내기 (예외 처리 생략)
try {
    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    System.out.println(response.body());
} catch (Exception e) {
    e.printStackTrace();
}

// 비동기 요청
client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
        .thenApply(HttpResponse::body)
        .thenAccept(System.out::println)
        .join(); // 완료 대기
```

## 4. 파일 읽기/쓰기 간소화

Files 클래스에 문자열을 읽고 쓰는 간단한 메서드가 추가되었습니다.

```java
// 파일 내용을 문자열로 읽기
String content = Files.readString(Path.of("file.txt"));

// 문자열을 파일에 쓰기
Files.writeString(Path.of("output.txt"), "Hello Java 11");
```

## 5. 컬렉션 to 배열 변환 개선

Collection 인터페이스에 toArray 메서드의 새로운 오버로드가 추가되었습니다.

```java
List<String> list = List.of("a", "b", "c");

// 기존 방식
String[] array1 = list.toArray(new String[0]);

// Java 11 방식
String[] array2 = list.toArray(String[]::new);
```

## 6. Epsilon: No-Op 가비지 컬렉터

Epsilon은 메모리 할당은 처리하지만 실제로 메모리를 회수하지 않는 가비지 컬렉터입니다. 성능 테스트, 메모리 압력 테스트 등에 유용합니다.

```bash
# Epsilon GC 활성화
java -XX:+UnlockExperimentalVMOptions -XX:+UseEpsilonGC -jar application.jar
```

## 7. ZGC: 확장 가능한 저지연 가비지 컬렉터

ZGC(Z Garbage Collector)는 대용량 힙에서도 짧은 일시 중지 시간(10ms 미만)을 목표로 하는 확장 가능한 저지연 가비지 컬렉터입니다.

```bash
# ZGC 활성화
java -XX:+UnlockExperimentalVMOptions -XX:+UseZGC -jar application.jar
```

## 8. 플라이트 레코더(Flight Recorder)

이전에는 상용 기능이었던 Java Flight Recorder가 오픈 소스화되어 무료로 사용할 수 있게 되었습니다. JFR은 실행 중인 Java 애플리케이션의 진단 및 프로파일링 데이터를 수집합니다.

```bash
# JFR 시작
java -XX:StartFlightRecording=duration=60s,filename=recording.jfr -jar application.jar

# JFR 데이터 분석
jcmd <pid> JFR.start
jcmd <pid> JFR.dump filename=recording.jfr
jcmd <pid> JFR.stop
```

## 9. 네스트 기반 액세스 제어 (Nest-Based Access Control)

Java 11에서는 중첩 클래스와 외부 클래스 간의 액세스를 개선하는 네스트 기반 액세스 제어가 도입되었습니다.

```java
public class Outer {
    private static int x = 10;

    static class Nested {
        void access() {
            // Java 11 이전에는 컴파일러가 브릿지 메서드를 생성했지만,
            // Java 11에서는 네스트 메이트로 인식하여 직접 접근 가능
            System.out.println(x);
        }
    }

    // 네스트 메이트 확인
    public static void main(String[] args) {
        Class<?> nestHost = Nested.class.getNestHost();
        System.out.println(nestHost.getName()); // Outer

        Class<?>[] nestMembers = Outer.class.getNestMembers();
        for (Class<?> member : nestMembers) {
            System.out.println(member.getName());
        }
    }
}
```

## 10. 동적 클래스 파일 상수 (Dynamic Class-File Constants)

Java 11에서는 `CONSTANT_Dynamic`이라는 새로운 상수 풀 형식이 도입되어 클래스 파일 형식을 확장하고 Java 프로그램의 성능을 향상시킵니다.

## 11. 지역 변수 구문 개선 (Local-Variable Syntax for Lambda Parameters)

Java 10에서 도입된 지역 변수 타입 추론(var)을 람다 표현식의 매개변수에서도 사용할 수 있게 되었습니다.

```java
// Java 10
Consumer<String> consumer = (String s) -> System.out.println(s);

// Java 11
Consumer<String> consumer = (var s) -> System.out.println(s);
```

## 12. 모듈 시스템 개선

Java 9에서 도입된 모듈 시스템이 Java 11에서 더욱 개선되었습니다.

## 13. 자바 실행 간소화

Java 11부터는 소스 파일을 직접 실행할 수 있게 되었습니다.

```bash
# 컴파일 없이 Java 파일 직접 실행
java HelloWorld.java
```

## 14. 패키지 삭제 및 정리

Java 11에서는 Java EE 및 CORBA 모듈이 JDK에서 제거되었습니다.
- `java.xml.ws` (JAX-WS)
- `java.xml.bind` (JAXB)
- `java.activation` (JAF)
- `java.xml.ws.annotation` (Common Annotations)
- `java.corba` (CORBA)
- `java.transaction` (JTA)
- `java.se.ee` (Aggregator module for the above modules)

## 결론

Java 11은 LTS(Long-Term Support) 버전으로, 다양한 API 개선과 성능 향상을 제공합니다. 특히 HTTP 클라이언트 API의 표준화, 문자열 처리 메서드 추가, 파일 처리 간소화 등은 개발자의 생산성을 크게 향상시킵니다. 또한 ZGC와 같은 새로운 가비지 컬렉터의 도입으로 대규모 애플리케이션의 성능도 개선되었습니다.
