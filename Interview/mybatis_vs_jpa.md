# MyBatis와 JPA의 차이점

## 1. 개요
데이터베이스와 상호작용하는 방법은 애플리케이션 개발에서 중요한 부분입니다. Java 생태계에서는 MyBatis와 JPA라는 두 가지 주요 기술이 널리 사용됩니다. 이 문서에서는 두 기술의 기본 개념, 차이점, 장단점 및 적합한 사용 사례에 대해 알아보겠습니다.

## 2. 기본 개념

### 2.1 MyBatis
MyBatis는 SQL 매핑 프레임워크로, 자바 객체와 SQL 문 사이의 매핑을 지원합니다. 개발자가 직접 SQL을 작성하고 이를 자바 객체와 매핑하는 방식으로 동작합니다.

```java
// MyBatis 매퍼 인터페이스 예시
public interface UserMapper {
    @Select("SELECT * FROM users WHERE id = #{id}")
    User getUserById(Long id);
    
    @Insert("INSERT INTO users(name, email) VALUES(#{name}, #{email})")
    void insertUser(User user);
}
```

### 2.2 JPA (Java Persistence API)
JPA는 자바 진영의 ORM(Object-Relational Mapping) 표준 명세로, 객체 지향 도메인 모델과 관계형 데이터베이스 사이의 매핑을 처리합니다. Hibernate, EclipseLink, OpenJPA 등이 JPA의 구현체입니다.

```java
// JPA 엔티티 클래스 예시
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String email;
    
    // getters and setters
}

// JPA 리포지토리 인터페이스 예시
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByEmail(String email);
}
```

## 3. 아키텍처 및 동작 방식 비교

### 3.1 MyBatis 아키텍처
1. **SQL 중심**: SQL 쿼리를 직접 작성하여 데이터베이스와 상호작용
2. **XML 또는 어노테이션**: SQL 쿼리를 XML 파일이나 어노테이션에 정의
3. **매핑 구성**: ResultMap을 통해 SQL 결과와 객체 간의 매핑 정의
4. **동적 SQL**: 조건에 따라 SQL을 동적으로 생성하는 기능 제공

### 3.2 JPA 아키텍처
1. **객체 중심**: 객체 모델을 중심으로 데이터베이스와 상호작용
2. **엔티티 매핑**: 어노테이션이나 XML을 통해 엔티티와 테이블 간의 매핑 정의
3. **영속성 컨텍스트**: 엔티티의 생명주기를 관리하는 1차 캐시 제공
4. **JPQL**: 객체 지향 쿼리 언어를 통해 엔티티 기반 쿼리 작성

## 4. 주요 차이점

### 4.1 데이터 접근 방식
- **MyBatis**: SQL 쿼리 중심의 데이터 접근 방식
- **JPA**: 객체 중심의 데이터 접근 방식

### 4.2 추상화 수준
- **MyBatis**: 낮은 수준의 추상화, 데이터베이스에 가까움
- **JPA**: 높은 수준의 추상화, 객체 모델에 가까움

### 4.3 쿼리 작성
- **MyBatis**: 개발자가 직접 SQL 쿼리 작성
- **JPA**: 대부분의 기본 CRUD 쿼리는 자동 생성, 복잡한 쿼리는 JPQL 사용

### 4.4 성능 최적화
- **MyBatis**: SQL 쿼리 직접 최적화 가능
- **JPA**: 자동 생성된 쿼리에 의존, 복잡한 최적화는 네이티브 쿼리 필요

### 4.5 학습 곡선
- **MyBatis**: SQL에 익숙한 개발자에게 상대적으로 쉬움
- **JPA**: 객체-관계 매핑 개념 이해 필요, 상대적으로 가파른 학습 곡선

## 5. 장단점 비교

### 5.1 MyBatis 장점
1. **SQL 제어**: 개발자가 SQL을 직접 작성하여 데이터베이스 상호작용에 대한 완전한 제어 가능
2. **쿼리 튜닝**: 복잡한 쿼리나 성능 최적화가 필요한 경우 직접 SQL 튜닝 가능
3. **유연성**: 레거시 데이터베이스나 복잡한 데이터베이스 구조와 작업하기 용이
4. **낮은 학습 곡선**: SQL에 익숙한 개발자에게 진입 장벽이 낮음
5. **성능**: 필요한 쿼리만 정확히 실행하여 성능 최적화 가능

### 5.2 MyBatis 단점
1. **반복적인 SQL 작성**: 유사한 쿼리를 반복해서 작성해야 함
2. **유지보수**: SQL 쿼리가 많아질수록 유지보수 비용 증가
3. **런타임 오류**: SQL 오류는 컴파일 시점이 아닌 런타임에 발견됨
4. **객체-관계 불일치**: 객체 모델과 데이터베이스 모델 간의 불일치 해결 책임이 개발자에게 있음

### 5.3 JPA 장점
1. **생산성**: 기본 CRUD 작업을 위한 코드 작성 최소화
2. **객체 지향적**: 객체 모델에 집중하여 개발 가능
3. **데이터베이스 독립성**: 데이터베이스 벤더에 종속되지 않는 코드 작성 가능
4. **캐싱**: 1차, 2차 캐시를 통한 성능 최적화
5. **지연 로딩**: 필요한 시점에 데이터를 로딩하는 전략 지원
6. **표준 준수**: JPA는 자바 표준이므로 다른 JPA 구현체와 호환

### 5.4 JPA 단점
1. **복잡성**: 개념 이해와 설정에 시간 투자 필요
2. **제어 제한**: 자동 생성된 SQL에 의존하므로 세밀한 제어가 어려울 수 있음
3. **성능 이슈**: 잘못 사용 시 N+1 문제 등 성능 이슈 발생 가능
4. **가파른 학습 곡선**: ORM 개념과 JPA 사용법 학습에 시간 필요

## 6. 사용 사례

### 6.1 MyBatis 적합한 사용 사례
- **복잡한 쿼리가 필요한 경우**: 복잡한 조인, 서브쿼리, 집계 함수 등을 사용하는 경우
- **레거시 데이터베이스 작업**: 기존 데이터베이스 구조가 복잡하거나 정규화되지 않은 경우
- **성능이 중요한 경우**: 쿼리 성능 최적화가 중요한 경우
- **배치 처리**: 대량의 데이터를 처리하는 배치 작업
- **SQL 전문가 팀**: SQL에 능숙한 개발자가 많은 팀

### 6.2 JPA 적합한 사용 사례
- **도메인 주도 설계(DDD)**: 객체 모델과 비즈니스 로직에 집중하는 경우
- **CRUD 중심 애플리케이션**: 기본적인 CRUD 작업이 대부분인 경우
- **빠른 개발 필요**: 생산성과 개발 속도가 중요한 경우
- **데이터베이스 벤더 독립성**: 다양한 데이터베이스를 지원해야 하는 경우
- **객체 지향 설계 중시**: 객체 지향 원칙을 중요시하는 팀

## 7. 코드 예시 비교

### 7.1 MyBatis 코드 예시

**XML 매핑 파일 (UserMapper.xml)**:
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.example.mapper.UserMapper">
    <select id="getUserById" resultType="com.example.model.User">
        SELECT id, name, email FROM users WHERE id = #{id}
    </select>
    
    <insert id="insertUser" parameterType="com.example.model.User">
        INSERT INTO users (name, email) VALUES (#{name}, #{email})
    </insert>
    
    <select id="findUsersByNameAndEmail" resultType="com.example.model.User">
        SELECT id, name, email FROM users 
        WHERE 1=1
        <if test="name != null">
            AND name LIKE CONCAT('%', #{name}, '%')
        </if>
        <if test="email != null">
            AND email = #{email}
        </if>
    </select>
</mapper>
```

**매퍼 인터페이스**:
```java
public interface UserMapper {
    User getUserById(Long id);
    void insertUser(User user);
    List<User> findUsersByNameAndEmail(@Param("name") String name, @Param("email") String email);
}
```

**서비스 사용 예시**:
```java
@Service
public class UserService {
    private final UserMapper userMapper;
    
    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    
    public User getUser(Long id) {
        return userMapper.getUserById(id);
    }
    
    public void createUser(User user) {
        userMapper.insertUser(user);
    }
    
    public List<User> searchUsers(String name, String email) {
        return userMapper.findUsersByNameAndEmail(name, email);
    }
}
```

### 7.2 JPA 코드 예시

**엔티티 클래스**:
```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private String email;
    
    // getters and setters
}
```

**리포지토리 인터페이스**:
```java
public interface UserRepository extends JpaRepository<User, Long> {
    List<User> findByNameContainingAndEmail(String name, String email);
}
```

**서비스 사용 예시**:
```java
@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public void createUser(User user) {
        userRepository.save(user);
    }
    
    public List<User> searchUsers(String name, String email) {
        return userRepository.findByNameContainingAndEmail(name, email);
    }
}
```

## 8. 성능 고려사항

### 8.1 MyBatis 성능 특성
- **직접적인 SQL 제어**: 필요한 데이터만 정확히 조회 가능
- **배치 처리 최적화**: 대량 데이터 처리를 위한 배치 작업 최적화 용이
- **캐시 관리**: 간단한 캐시 메커니즘 제공
- **실행 계획 최적화**: 데이터베이스 실행 계획을 고려한 쿼리 작성 가능

### 8.2 JPA 성능 특성
- **1차 캐시**: 동일 트랜잭션 내에서 엔티티 캐싱
- **2차 캐시**: 애플리케이션 전체에서 엔티티 캐싱 가능
- **지연 로딩**: 필요한 시점에 데이터 로딩
- **N+1 문제**: 연관 관계 조회 시 발생 가능한 성능 이슈
- **벌크 연산**: 대량 데이터 처리를 위한 벌크 연산 지원

## 9. 베스트 프랙티스

### 9.1 MyBatis 베스트 프랙티스
1. **동적 SQL 활용**: 조건에 따른 쿼리 생성에 동적 SQL 기능 활용
2. **결과 매핑 최적화**: 복잡한 결과 매핑은 ResultMap 사용
3. **배치 처리**: 대량 데이터 처리 시 배치 기능 활용
4. **SQL 재사용**: SQL 조각을 분리하여 재사용
5. **캐시 전략**: 적절한 캐시 설정으로 성능 향상

### 9.2 JPA 베스트 프랙티스
1. **N+1 문제 방지**: fetch join 또는 EntityGraph 사용
2. **지연 로딩 활용**: 연관 관계는 기본적으로 지연 로딩 설정
3. **벌크 연산 활용**: 대량 데이터 처리 시 벌크 연산 사용
4. **캐시 전략**: 적절한 2차 캐시 설정
5. **네이티브 쿼리 활용**: 복잡한 쿼리는 네이티브 쿼리 고려

## 10. 결론

MyBatis와 JPA는 각각 다른 철학과 접근 방식을 가진 데이터 접근 기술입니다:

- **MyBatis**는 SQL 쿼리를 직접 작성해 데이터베이스와 상호작용하는데 XML 설정 파일을 사용해 SQL 쿼리와 엔티티 객체 간의 매핑을 정의합니다. 데이터베이스와의 상호 작용에 좀 더 많은 유연성을 가질 수 있고, 쿼리 튜닝도 좀 더 자유롭습니다. 하지만, SQL 쿼리를 직접 작성해야 하기 때문에 쿼리에 대한 유지보수 비용이 필요하며 XML에 작성하는 SQL은 문법 혹은 오타로 인한 오류 발생가능성이 있고 이 오류는 컴파일이 아닌 런타임시에 발견될 수 있습니다.

- **JPA**는 JPQL을 통해 데이터베이스와 상호작용하며 엔티티 객체를 사용해 데이터베이스 테이블과 매핑을 정의합니다. 간단한 CRUD 기능은 별도의 쿼리 작성 없이 추상화된 함수들을 통해 사용할 수 있어서 생산성이 높아질 수 있고, 표준 준수를 하기에 다른 JPA구현체와도 호환됩니다. 다만, 추상화로 인해 데이터베이스와 상호작용하는 부분에 대한 제어권이 적고, 러닝커브 측면에서도 MyBatis보다 높기에 배우기가 좀 더 어려울 수 있습니다.

두 기술 모두 장단점이 있으므로, 프로젝트의 요구사항, 팀의 기술 스택, 개발 철학에 따라 적절한 기술을 선택하는 것이 중요합니다. 때로는 두 기술을 함께 사용하여 각 기술의 장점을 활용하는 하이브리드 접근 방식도 고려할 수 있습니다.

## 11. 참고 자료
- [MyBatis 공식 문서](https://mybatis.org/mybatis-3/)
- [JPA 공식 문서](https://jakarta.ee/specifications/persistence/)
- [Hibernate 공식 문서](https://hibernate.org/orm/documentation/)
- [Spring Data JPA 문서](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)