# Java 17의 주요 특징과 기능


## 1. 봉인 클래스 (Sealed Classes)


```java
// 봉인된 인터페이스 선언
public sealed interface Shape permits Circle, Rectangle, Triangle {
    double area();
}

// 허용된 구현 클래스
public final class Circle implements Shape {
    private final double radius;
    
    public Circle(double radius) {
        this.radius = radius;
    }
    
    @Override
    public double area() {
        return Math.PI * radius * radius;
    }
}

public final class Rectangle implements Shape {
    private final double width;
    private final double height;
    
    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }
    
    @Override
    public double area() {
        return width * height;
    }
}

public non-sealed class Triangle implements Shape {
    private final double base;
    private final double height;
    
    public Triangle(double base, double height) {
        this.base = base;
        this.height = height;
    }
    
    @Override
    public double area() {
        return 0.5 * base * height;
    }
}
```

봉인 클래스는 다음과 같은 특징을 가집니다:
- `sealed` 키워드로 선언
- `permits` 절을 사용하여 확장/구현할 수 있는 클래스를 명시적으로 지정
- 하위 클래스는 `final`, `sealed`, 또는 `non-sealed` 중 하나로 선언해야 함

## 2. 패턴 매칭 for instanceof (Pattern Matching for instanceof)

Java 16에서 프리뷰로 도입되었던 패턴 매칭이 Java 17에서 정식 기능으로 포함되었습니다.

```java
// 기존 방식
if (obj instanceof String) {
    String s = (String) obj;
    // s 사용
}

// Java 17 패턴 매칭 사용
if (obj instanceof String s) {
    // 자동으로 캐스팅된 변수 s 사용 가능
    System.out.println(s.length());
}

// 조건부 패턴 매칭
if (obj instanceof String s && s.length() > 5) {
    System.out.println(s.toUpperCase());
}
```

## 3. 레코드 클래스 (Record Classes)

Java 16에서 프리뷰로 도입되었던 레코드 클래스가 Java 17에서 정식 기능으로 포함되었습니다.

```java
// 불변 데이터 객체를 위한 레코드 선언
public record Person(String name, int age) {
    // 컴팩트 생성자
    public Person {
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
    }
    
    // 추가 메서드 정의 가능
    public boolean isAdult() {
        return age >= 18;
    }
}

// 사용 예
Person person = new Person("John", 30);
String name = person.name(); // 자동 생성된 접근자
int age = person.age();      // 자동 생성된 접근자
boolean isAdult = person.isAdult(); // 사용자 정의 메서드
```

레코드는 다음을 자동으로 생성합니다:
- 필드에 대한 private final 필드
- 생성자
- 접근자 메서드
- equals(), hashCode(), toString() 메서드

## 4. 텍스트 블록 (Text Blocks)

Java 15에서 정식 기능으로 도입된 텍스트 블록이 Java 17에서도 계속 사용 가능합니다.

```java
// 기존 문자열 선언
String json = "{\n" +
              "  \"name\": \"John\",\n" +
              "  \"age\": 30,\n" +
              "  \"address\": {\n" +
              "    \"street\": \"123 Main St\",\n" +
              "    \"city\": \"Anytown\"\n" +
              "  }\n" +
              "}";

// 텍스트 블록 사용
String jsonTextBlock = """
        {
          "name": "John",
          "age": 30,
          "address": {
            "street": "123 Main St",
            "city": "Anytown"
          }
        }
        """;
```

## 5. 스위치 표현식 (Switch Expressions)

Java 14에서 정식 기능으로 도입된 스위치 표현식이 Java 17에서도 계속 사용 가능합니다.

```java
// 기존 스위치 문
String dayType;
switch (day) {
    case "MONDAY":
    case "TUESDAY":
    case "WEDNESDAY":
    case "THURSDAY":
    case "FRIDAY":
        dayType = "Weekday";
        break;
    case "SATURDAY":
    case "SUNDAY":
        dayType = "Weekend";
        break;
    default:
        dayType = "Invalid day";
}

// 스위치 표현식
String dayType = switch (day) {
    case "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY" -> "Weekday";
    case "SATURDAY", "SUNDAY" -> "Weekend";
    default -> "Invalid day";
};

// yield 사용
int numLetters = switch (day) {
    case "MONDAY", "FRIDAY", "SUNDAY" -> {
        System.out.println(day);
        yield 6;
    }
    case "TUESDAY" -> {
        yield 7;
    }
    case "THURSDAY", "SATURDAY" -> 8;
    case "WEDNESDAY" -> 9;
    default -> throw new IllegalArgumentException("Invalid day: " + day);
};
```

## 6. 외부 메모리 액세스 API (Foreign Memory Access API)

Java 17에서는 외부 메모리 액세스 API가 인큐베이터 모듈로 제공됩니다. 이 API를 사용하면 Java 힙 외부의 메모리에 안전하게 접근할 수 있습니다.

```java
// jdk.incubator.foreign 모듈 필요
import jdk.incubator.foreign.*;

// 메모리 세그먼트 할당
try (MemorySegment segment = MemorySegment.allocateNative(100)) {
    // 메모리 레이아웃 정의
    MemoryLayout layout = MemoryLayout.sequenceLayout(25, ValueLayout.JAVA_INT);
    
    // 메모리에 값 쓰기
    VarHandle intHandle = layout.varHandle(int.class, MemoryLayout.PathElement.sequenceElement());
    for (int i = 0; i < 25; i++) {
        intHandle.set(segment, (long) i, i * 2);
    }
    
    // 메모리에서 값 읽기
    for (int i = 0; i < 25; i++) {
        int value = (int) intHandle.get(segment, (long) i);
        System.out.println(value);
    }
}
```

## 7. 강화된 의사 난수 생성기 (Enhanced Pseudo-Random Number Generators)

Java 17에서는 의사 난수 생성기(PRNG)가 개선되었습니다.

```java
// 새로운 의사 난수 생성기 인터페이스
RandomGenerator generator = RandomGenerator.of("L64X128MixRandom");
int randomNumber = generator.nextInt(100); // 0-99 사이의 난수

// 스트림 생성
IntStream randomNumbers = generator.ints(10, 1, 100);
randomNumbers.forEach(System.out::println);

// 다양한 알고리즘 지원
RandomGenerator.StreamableGenerator streamable = RandomGenerator.getDefault().streaming();
DoubleStream doubles = streamable.doubles(1000);
```

## 8. 컨텍스트별 역직렬화 필터 (Context-Specific Deserialization Filters)

Java 17에서는 역직렬화 과정에서 보안을 강화하기 위한 컨텍스트별 필터가 도입되었습니다.

```java
// 역직렬화 필터 설정
ObjectInputFilter filter = ObjectInputFilter.Config.createFilter("java.base/*;!java.lang.Process");

// 특정 ObjectInputStream에 필터 적용
try (ObjectInputStream ois = new ObjectInputStream(inputStream)) {
    ois.setObjectInputFilter(filter);
    Object obj = ois.readObject();
}

// 컨텍스트별 필터 팩토리 설정
ObjectInputFilter.Config.setSerialFilterFactory(new ObjectInputFilter.FilterFactory() {
    @Override
    public ObjectInputFilter apply(ObjectInputFilter current, ObjectInputFilter.FilterInfo info) {
        // 컨텍스트에 따라 다른 필터 반환
        if (info.streamBytes() > 1_000_000) {
            return ObjectInputFilter.rejectUndecidedClass(String.class);
        }
        return current;
    }
});
```

## 9. macOS 렌더링 파이프라인 (macOS Rendering Pipeline)

Java 17에서는 macOS용 새로운 렌더링 파이프라인이 도입되어 Apple Metal API를 사용하여 그래픽 성능이 향상되었습니다.

## 10. 애플 실리콘 지원 (Apple Silicon Support)

Java 17은 Apple의 M1 프로세서(Apple Silicon)를 공식적으로 지원합니다.

## 11. 제거된 기능 (Removed Features)

Java 17에서는 다음 기능들이 제거되었습니다:
- 실험적 AOT 및 JIT 컴파일러
- RMI 활성화 메커니즘
- 보안 관리자(Security Manager)의 사용 중단 (향후 제거 예정)
- Applet API

## 12. 향상된 가비지 컬렉터 (Improved Garbage Collectors)

Java 17에서는 ZGC와 Shenandoah GC가 계속 개선되었습니다.

```bash
# ZGC 사용
java -XX:+UseZGC -jar application.jar

# Shenandoah GC 사용
java -XX:+UseShenandoahGC -jar application.jar
```

## 13. 새로운 플랫폼 지원 (New Platform Support)

Java 17은 다음과 같은 새로운 플랫폼을 지원합니다:
- Alpine Linux
- Windows ARM64

## 14. 향상된 의존성 관리 (Improved Dependency Management)

Java 17에서는 모듈 시스템이 개선되어 의존성 관리가 더욱 향상되었습니다.

```java
// module-info.java
module com.example.app {
    requires java.base;
    requires java.net.http;
    
    exports com.example.app.api;
    
    provides com.example.app.spi.Service with com.example.app.impl.ServiceImpl;
}
```

## 15. 새로운 Vector API (Vector API)

Java 17에서는 벡터 연산을 위한 인큐베이터 API가 제공됩니다.

```java
// jdk.incubator.vector 모듈 필요
import jdk.incubator.vector.*;

// 벡터 연산 예제
void vectorComputation(float[] a, float[] b, float[] c) {
    VectorSpecies<Float> species = FloatVector.SPECIES_256;
    
    for (int i = 0; i < a.length; i += species.length()) {
        // 벡터 마스크 생성
        VectorMask<Float> mask = species.indexInRange(i, a.length);
        
        // 메모리에서 벡터 로드
        FloatVector va = FloatVector.fromArray(species, a, i, mask);
        FloatVector vb = FloatVector.fromArray(species, b, i, mask);
        
        // 벡터 연산 수행
        FloatVector vc = va.mul(vb);
        
        // 결과 저장
        vc.intoArray(c, i, mask);
    }
}
```

## 결론

Java 17은 LTS(Long-Term Support) 버전으로, 봉인 클래스, 패턴 매칭, 레코드 등 여러 중요한 기능이 정식으로 포함되었습니다. 이러한 기능들은 코드의 안전성과 표현력을 높이고, 개발자의 생산성을 향상시킵니다