# JPA ID 생성 전략 (JPA ID Generation Strategies)

JPA(Java Persistence API)에서는 엔티티의 기본 키(Primary Key)를 생성하기 위한 다양한 전략을 제공합니다. 각 전략은 서로 다른 상황과 데이터베이스에 적합하며, 애플리케이션의 요구사항에 맞게 선택할 수 있습니다.

## 1. IDENTITY 전략

IDENTITY 전략은 데이터베이스의 자동 증가(AUTO_INCREMENT) 기능을 사용하여 기본 키를 생성합니다.

### 특징
- 데이터베이스에 엔티티를 저장한 후에야 ID 값을 알 수 있음
- MySQL, PostgreSQL, SQL Server, H2 등에서 지원
- 영속성 컨텍스트의 지연 쓰기(write-behind) 기능을 사용할 수 없음

### 코드 예시
```java
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    // ...
}
```

### 장점
- 구현이 간단하고 직관적
- 데이터베이스에 위임하므로 별도의 ID 생성 로직이 필요 없음

### 단점
- 대량 삽입(bulk insert) 성능이 떨어짐 (매번 DB에 쿼리를 보내야 함)
- JPA의 지연 쓰기 기능을 활용할 수 없음
- 일부 데이터베이스에서는 지원하지 않음

## 2. SEQUENCE 전략

SEQUENCE 전략은 데이터베이스 시퀀스 객체를 사용하여 기본 키를 생성합니다.

### 특징
- 데이터베이스 시퀀스를 사용하여 고유한 값을 생성
- Oracle, PostgreSQL, H2, DB2 등에서 지원
- MySQL은 시퀀스를 지원하지 않음 (MySQL 8.0부터 지원)

### 코드 예시
```java
@Entity
@SequenceGenerator(
    name = "MEMBER_SEQ_GENERATOR",
    sequenceName = "MEMBER_SEQ",
    initialValue = 1,
    allocationSize = 50
)
public class Member {
    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE,
        generator = "MEMBER_SEQ_GENERATOR"
    )
    private Long id;
    
    private String name;
    // ...
}
```

### 장점
- 데이터베이스와 통신 횟수를 줄일 수 있음 (allocationSize 설정으로 최적화)
- JPA의 지연 쓰기 기능을 활용할 수 있음
- 대량 삽입 성능이 IDENTITY 전략보다 좋음

### 단점
- MySQL 등 일부 데이터베이스에서는 지원하지 않음
- 시퀀스 객체를 별도로 관리해야 함

## 3. TABLE 전략

TABLE 전략은 키 생성 전용 테이블을 사용하여 기본 키를 생성합니다.

### 특징
- 모든 데이터베이스에서 사용 가능
- 키 생성을 위한 별도의 테이블 필요
- 성능이 다른 전략에 비해 떨어짐

### 코드 예시
```java
@Entity
@TableGenerator(
    name = "MEMBER_SEQ_GENERATOR",
    table = "MY_SEQUENCES",
    pkColumnValue = "MEMBER_SEQ",
    allocationSize = 50
)
public class Member {
    @Id
    @GeneratedValue(
        strategy = GenerationType.TABLE,
        generator = "MEMBER_SEQ_GENERATOR"
    )
    private Long id;
    
    private String name;
    // ...
}
```

### 장점
- 모든 데이터베이스에서 사용 가능
- 데이터베이스 벤더에 독립적

### 단점
- 성능이 떨어짐 (테이블 락으로 인한 경합 발생 가능)
- 별도의 테이블 관리 필요

## 4. AUTO 전략

AUTO 전략은 JPA 구현체(예: Hibernate)가 데이터베이스에 맞는 전략을 자동으로 선택합니다.

### 특징
- JPA 구현체가 데이터베이스 방언에 따라 IDENTITY, SEQUENCE, TABLE 중 하나를 선택
- 기본값으로 사용됨

### 코드 예시
```java
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    private String name;
    // ...
}
```

### 장점
- 데이터베이스에 맞는 전략을 자동으로 선택해줌
- 데이터베이스 변경 시 코드 수정이 필요 없음

### 단점
- 어떤 전략이 선택될지 명확하지 않음
- 세부적인 설정이 어려움

## 5. UUID 생성

JPA에서 직접 지원하는 전략은 아니지만, UUID를 사용하여 기본 키를 생성할 수도 있습니다.

### 코드 예시
```java
@Entity
public class Member {
    @Id
    @Column(length = 36)
    private String id;
    
    private String name;
    
    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
    }
    // ...
}
```

### 장점
- 분산 시스템에서 ID 충돌 없이 사용 가능
- 데이터베이스에 독립적
- 별도의 ID 생성 로직이나 데이터베이스 기능이 필요 없음

### 단점
- 문자열 기반이므로 숫자 기반 ID보다 저장 공간을 더 많이 사용
- 인덱싱 성능이 숫자 기반 ID보다 떨어질 수 있음

## 결론

JPA ID 생성 전략은 애플리케이션의 요구사항과 사용하는 데이터베이스에 따라 적절히 선택해야 합니다.

- **대량의 데이터 삽입이 필요한 경우**: SEQUENCE 전략 (allocationSize 최적화)
- **단순한 구현이 필요한 경우**: IDENTITY 전략
- **데이터베이스에 독립적인 구현이 필요한 경우**: TABLE 전략 또는 UUID
- **분산 시스템에서 ID 생성이 필요한 경우**: UUID

각 전략의 장단점을 이해하고 애플리케이션의 특성에 맞게 선택하는 것이 중요합니다.