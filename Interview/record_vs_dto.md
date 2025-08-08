# Java Record와 DTO

## 1. 개요
Java 16에서 정식 출시된 Record는 데이터 전송을 위한 불변 객체를 간결하게 정의할 수 있는 새로운 기능입니다. 이 문서에서는 Record의 특징, DTO(Data Transfer Object)와의 관계, 그리고 언제 Record를 DTO로 사용하는 것이 적합한지에 대해 알아보겠습니다.

## 2. Record란?

### 2.1 Record의 정의
Record는 Java 16에서 정식 출시된 특별한 유형의 클래스로, 데이터를 보관하는 것이 주 목적인 클래스를 간결하게 정의할 수 있게 해줍니다. Record는 불변성(Immutable)을 기본으로 하며, 데이터의 투명한 캡슐화를 제공합니다.

### 2.2 Record의 특징
- **불변성(Immutability)**: 모든 필드가 `final`로 선언되어 객체 생성 후 변경 불가
- **자동 메서드 생성**: 생성자, getter, `equals()`, `hashCode()`, `toString()` 메서드 자동 생성
- **간결한 문법**: 보일러플레이트 코드 감소
- **컴포넌트 접근**: 필드에 대한 접근자 메서드 자동 생성 (필드명과 동일한 이름)
- **패턴 매칭 지원**: Java 17부터 패턴 매칭과 함께 사용 가능

## 3. 전통적인 DTO vs Record

### 3.1 전통적인 클래스 기반 DTO
```java
public class MemberDto {
    private final String name;
    private final String email;
    private final int age;

    public MemberDto(String name, String email, int age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public String getName() {
        return name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public int getAge() {
        return age;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberDto memberDto = (MemberDto) o;
        return age == memberDto.age && 
               Objects.equals(name, memberDto.name) && 
               Objects.equals(email, memberDto.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email, age);
    }

    @Override
    public String toString() {
        return "MemberDto{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                '}';
    }
}
```

### 3.2 Record 기반 DTO
```java
public record MemberDto(String name, String email, int age) {}
```

### 3.3 주요 차이점
- **코드 길이**: Record는 훨씬 간결한 문법 제공
- **가독성**: Record는 데이터 구조를 한눈에 파악 가능
- **유지보수**: Record는 필드 추가/수정 시 관련 메서드 자동 업데이트
- **확장성**: 일반 클래스는 상속을 통한 확장 가능, Record는 상속 불가
- **메모리 사용**: 두 방식 모두 비슷한 메모리 사용량

## 4. Record와 DTO의 관계

### 4.1 DTO(Data Transfer Object)란?
DTO는 계층 간 데이터 전송을 목적으로 하는 객체로, 주로 다음과 같은 특징을 가집니다:
- 비즈니스 로직 없이 데이터만 포함
- 주로 getter 메서드만 제공 (불변성이 요구되는 경우 setter 없음)
- 직렬화(Serialization) 가능
- 네트워크 전송 최적화를 위한 경량 객체

### 4.2 Record가 DTO로 적합한 이유
- **불변성**: 데이터 전송 과정에서 변경되지 않음을 보장
- **간결성**: 최소한의 코드로 DTO 정의 가능
- **명확성**: 객체의 목적(데이터 전송)이 코드 구조에서 명확히 드러남
- **직렬화 지원**: `Serializable` 인터페이스 구현 가능
- **JSON 변환 용이**: Jackson, Gson 등의 라이브러리와 호환

### 4.3 모든 Record가 DTO인가?
모든 Record 객체가 DTO인 것은 아닙니다. Record는 단순히 데이터를 캡슐화하는 역할을 하는데, DTO 외에도 다양한 용도로 사용될 수 있습니다:

- **값 객체(Value Objects)**: 도메인 내에서 특정 값을 표현
  ```java
  public record Coordinates(double x, double y) {}
  ```

- **이벤트 객체**: 시스템 내 이벤트 전달
  ```java
  public record UserCreatedEvent(String userId, LocalDateTime createdAt) {}
  ```

- **결과 래퍼(Result Wrapper)**: 연산 결과와 메타데이터 포함
  ```java
  public record QueryResult<T>(T data, int totalCount, int page) {}
  ```

## 5. Record와 Value Object(VO) 비교

### 5.1 공통점
- **불변성**: 두 개념 모두 객체의 상태가 변경되지 않음
- **값 기반 동등성**: 동일한 필드 값을 가지면 동일한 객체로 간주
- **데이터 캡슐화**: 데이터를 표현하는 데 초점

### 5.2 차이점
- **목적**: 
  - VO: 도메인 모델 내에서 특정 개념을 표현
  - Record: 주로 데이터 전달 목적
- **비즈니스 로직**: 
  - VO: 도메인 로직이나 규칙을 포함할 수 있음
  - Record: 일반적으로 데이터만 포함
- **사용 맥락**: 
  - VO: 도메인 주도 설계(DDD)에서 중요한 개념
  - Record: 주로 API 응답, 데이터 전송 등에 활용

### 5.3 Record로 VO 구현 예시
```java
public record Money(BigDecimal amount, Currency currency) {
    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(currency, "Currency cannot be null");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
    }
    
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Cannot add money with different currencies");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    public Money multiply(int multiplier) {
        return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)), this.currency);
    }
}
```

## 6. Record의 한계

### 6.1 상속 제한
- Record는 다른 클래스를 상속(`extends`)할 수 없음
- Record는 다른 Record를 상속할 수 없음
- 인터페이스 구현(`implements`)은 가능

### 6.2 가변성 제한
- 모든 필드가 `final`로 선언되어 변경 불가
- 가변 객체가 필요한 경우 적합하지 않음
- 필드 값 변경이 필요한 경우 새 인스턴스 생성 필요

### 6.3 확장성 제한
- 상속을 통한 기능 확장이 불가능
- 컴포지션(Composition)을 통한 확장만 가능
- 프레임워크에 따라 호환성 문제 발생 가능

### 6.4 버전 호환성
- Java 14 이전 버전과 호환 불가
- 하위 버전 JVM에서 실행 불가
- 레거시 시스템과의 통합 시 문제 발생 가능

## 7. Record 활용 사례

### 7.1 API 응답 객체
```java
@RestController
@RequestMapping("/api/members")
public class MemberController {
    
    @GetMapping("/{id}")
    public MemberResponse getMember(@PathVariable Long id) {
        Member member = memberService.findById(id);
        return new MemberResponse(
            member.getName(),
            member.getEmail(),
            member.getAge()
        );
    }
}

// API 응답용 Record
public record MemberResponse(String name, String email, int age) {}
```

### 7.2 복합 키(Composite Key)
```java
public record OrderItemId(Long orderId, Long productId) implements Serializable {}

@Entity
public class OrderItem {
    @EmbeddedId
    private OrderItemId id;
    
    private int quantity;
    private BigDecimal price;
    
    // 생성자, 메서드 등
}
```

### 7.3 다중 반환 값
```java
public record QueryResult<T>(List<T> items, long totalCount, int page, int pageSize) {}

public class ProductService {
    public QueryResult<ProductDto> searchProducts(String keyword, int page, int pageSize) {
        long totalCount = productRepository.countByKeyword(keyword);
        List<Product> products = productRepository.findByKeyword(keyword, page, pageSize);
        List<ProductDto> dtos = products.stream()
            .map(p -> new ProductDto(p.getId(), p.getName(), p.getPrice()))
            .collect(Collectors.toList());
            
        return new QueryResult<>(dtos, totalCount, page, pageSize);
    }
}
```

## 8. 베스트 프랙티스

### 8.1 Record를 DTO로 사용할 때 권장사항
- **명확한 이름 지정**: 목적을 명확히 하는 이름 사용 (예: `UserResponse`, `ProductDto`)
- **검증 로직 추가**: 생성자에 유효성 검증 로직 포함
- **직렬화 고려**: 필요 시 `Serializable` 인터페이스 구현
- **문서화**: JavaDoc을 통한 필드 의미 문서화
- **중첩 구조 활용**: 복잡한 데이터 구조 표현 시 중첩 Record 활용

### 8.2 Record 사용 시 주의사항
- **가변 객체 필드**: 가변 객체를 필드로 사용 시 방어적 복사 고려
- **JPA 엔티티로 사용 금지**: Record는 JPA 엔티티로 적합하지 않음
- **버전 호환성**: 대상 환경의 Java 버전 확인
- **과도한 비즈니스 로직 지양**: 복잡한 비즈니스 로직은 별도 클래스로 분리

## 9. 결론

Record는 데이터 중심 클래스를 간결하게 정의할 수 있는 강력한 기능으로, 특히 DTO 패턴에 매우 적합합니다. 불변성, 자동 메서드 생성, 간결한 문법 등의 특징으로 코드의 가독성과 유지보수성을 크게 향상시킵니다.

그러나 Record는 모든 상황에 적합한 만능 해결책이 아닙니다. 상속 제한, 가변성 제한, Java 버전 호환성 등의 한계가 있으므로, 사용 사례와 요구사항을 고려하여 적절히 활용해야 합니다.

DTO 패턴을 구현할 때 Record를 사용하면 코드량을 줄이고 의도를 명확히 표현할 수 있어, 현대적인 Java 애플리케이션 개발에 큰 도움이 됩니다.

## 참고 자료
- [Java 공식 문서 - Record 클래스](https://docs.oracle.com/en/java/javase/16/language/records.html)
- [자바 DTO vs Record, 무엇을 사용해야 할까?](https://www.example.com/java-dto-vs-record)
- [Java의 레코드 - 전체 튜토리얼](https://www.example.com/java-record-tutorial)
