# Spring Boot, JPA, Querydsl 종합 가이드

## 목차
1. [개요](#1-개요)
2. [Spring Boot와 JPA 설정](#2-spring-boot와-jpa-설정)
3. [Querydsl 설정](#3-querydsl-설정)
4. [Spring Data JPA 사용법](#4-spring-data-jpa-사용법)
5. [Querydsl 사용법](#5-querydsl-사용법)
6. [N+1 문제와 해결 방법](#6-n1-문제와-해결-방법)
7. [Spring Data JPA vs Querydsl](#7-spring-data-jpa-vs-querydsl)
8. [실무 활용 전략](#8-실무-활용-전략)

## 1. 개요

### ORM, JPA, Hibernate 관계
- **ORM(Object-Relational Mapping)**: 객체와 관계형 데이터베이스를 매핑하는 기술적 개념
- **JPA(Java Persistence API)**: 자바에서 ORM을 구현하기 위한 표준 명세(인터페이스)
- **Hibernate**: JPA의 실제 구현체 중 하나

### Spring Data JPA와 Querydsl
- **Spring Data JPA**: JPA를 더 쉽게 사용할 수 있게 해주는 스프링 프레임워크 모듈
- **Querydsl**: 타입 안전한 쿼리를 작성할 수 있게 해주는 프레임워크

## 2. Spring Boot와 JPA 설정

### 의존성 추가 (Gradle)

```gradle
dependencies {
    // Spring Boot Starter
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    
    // 데이터베이스 드라이버 (예: H2, MySQL)
    runtimeOnly 'com.h2database:h2'
    // 또는 MySQL
    // runtimeOnly 'mysql:mysql-connector-java'
    
    // 롬복 (선택사항)
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // 테스트
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

### application.properties 설정

```properties
# 데이터베이스 연결 설정
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA 설정
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# 로깅 설정
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# N+1 문제 해결을 위한 배치 사이즈 설정
spring.jpa.properties.hibernate.default_batch_fetch_size=100
```

### 엔티티 클래스 예시

```java
@Entity
@Table(name = "members")
@Getter @Setter
public class Member {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false)
    private String username;
    
    private int age;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
}

@Entity
@Table(name = "teams")
@Getter @Setter
public class Team {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
}
```

## 3. Querydsl 설정

### Gradle 설정 (Gradle 7.0 이상)

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '2.7.0'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
    id 'com.ewerk.gradle.plugins.querydsl' version '1.0.10'
}

dependencies {
    // 기존 의존성
    
    // Querydsl
    implementation 'com.querydsl:querydsl-jpa'
    annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jpa'
    annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
    annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
}

// Querydsl 설정
def querydslDir = "$buildDir/generated/querydsl"

querydsl {
    jpa = true
    querydslSourcesDir = querydslDir
}

sourceSets {
    main.java.srcDir querydslDir
}

configurations {
    querydsl.extendsFrom compileClasspath
}

compileQuerydsl {
    options.annotationProcessorPath = configurations.querydsl
}
```

### Querydsl 설정 클래스

```java
@Configuration
public class QuerydslConfig {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
```

## 4. Spring Data JPA 사용법

### 리포지토리 인터페이스 정의

```java
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    // 메서드 이름으로 쿼리 생성
    List<Member> findByUsername(String username);
    
    // 나이가 특정 값보다 큰 회원 찾기
    List<Member> findByAgeGreaterThan(int age);
    
    // 여러 조건 조합
    List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
    
    // 정렬
    List<Member> findByUsernameOrderByAgeDesc(String username);
    
    // @Query 어노테이션 사용
    @Query("SELECT m FROM Member m WHERE m.username = :username AND m.age > :age")
    List<Member> findUser(@Param("username") String username, @Param("age") int age);
    
    // 네이티브 쿼리
    @Query(value = "SELECT * FROM members WHERE username = ?1", nativeQuery = true)
    List<Member> findByUsernameNative(String username);
}
```

### 페이징 처리

```java
// 리포지토리에 메서드 추가
Page<Member> findByAge(int age, Pageable pageable);

// 사용 예시
public Page<Member> getMembersByAge(int age, int page, int size) {
    PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "username"));
    return memberRepository.findByAge(age, pageRequest);
}
```

### @EntityGraph를 사용한 N+1 문제 해결

```java
@EntityGraph(attributePaths = {"team"})
@Query("SELECT m FROM Member m")
List<Member> findAllWithTeam();
```

## 5. Querydsl 사용법

### 기본 조회

```java
@Repository
@RequiredArgsConstructor
public class MemberQueryRepository {
    
    private final JPAQueryFactory queryFactory;
    
    public List<Member> findByUsername(String username) {
        return queryFactory
            .selectFrom(QMember.member)
            .where(QMember.member.username.eq(username))
            .fetch();
    }
    
    public List<Member> findByUsernameAndAgeGt(String username, int age) {
        return queryFactory
            .selectFrom(QMember.member)
            .where(
                QMember.member.username.eq(username),
                QMember.member.age.gt(age)
            )
            .fetch();
    }
}
```

### 동적 쿼리 - BooleanBuilder 사용

```java
public List<Member> searchMember(String usernameCond, Integer ageCond) {
    BooleanBuilder builder = new BooleanBuilder();
    
    if (usernameCond != null) {
        builder.and(QMember.member.username.eq(usernameCond));
    }
    
    if (ageCond != null) {
        builder.and(QMember.member.age.gt(ageCond));
    }
    
    return queryFactory
        .selectFrom(QMember.member)
        .where(builder)
        .fetch();
}
```

### 동적 쿼리 - Where 다중 파라미터 사용

```java
public List<Member> searchMember2(String usernameCond, Integer ageCond) {
    return queryFactory
        .selectFrom(QMember.member)
        .where(
            usernameEq(usernameCond),
            ageGt(ageCond)
        )
        .fetch();
}

private BooleanExpression usernameEq(String usernameCond) {
    return usernameCond != null ? QMember.member.username.eq(usernameCond) : null;
}

private BooleanExpression ageGt(Integer ageCond) {
    return ageCond != null ? QMember.member.age.gt(ageCond) : null;
}
```

### 조인 사용

```java
public List<Member> findMembersWithTeam() {
    return queryFactory
        .selectFrom(QMember.member)
        .join(QMember.member.team, QTeam.team)
        .where(QTeam.team.name.eq("TeamA"))
        .fetch();
}
```

### 페이징 처리

```java
public Page<Member> searchPageSimple(String usernameCond, Integer ageCond, Pageable pageable) {
    QueryResults<Member> results = queryFactory
        .selectFrom(QMember.member)
        .where(
            usernameEq(usernameCond),
            ageGt(ageCond)
        )
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();
    
    List<Member> content = results.getResults();
    long total = results.getTotal();
    
    return new PageImpl<>(content, pageable, total);
}
```

## 6. N+1 문제와 해결 방법

### N+1 문제란?
N+1 문제는 ORM에서 연관 관계가 설정된 엔티티를 조회할 때 발생하는 성능 이슈입니다. 1번의 쿼리로 N개의 데이터를 가져온 후, 각 데이터의 연관 엔티티를 조회하기 위해 추가로 N번의 쿼리가 발생하는 문제입니다.

### 해결 방법

1. **Fetch Join (JPQL)**
```java
@Query("SELECT m FROM Member m JOIN FETCH m.team")
List<Member> findAllWithTeamUsingFetchJoin();
```

2. **EntityGraph**
```java
@EntityGraph(attributePaths = {"team"})
List<Member> findAll();
```

3. **Batch Size 설정**
```properties
spring.jpa.properties.hibernate.default_batch_fetch_size=100
```

4. **Querydsl 사용**
```java
public List<Member> findAllWithTeam() {
    return queryFactory
        .selectFrom(QMember.member)
        .join(QMember.member.team, QTeam.team).fetchJoin()
        .fetch();
}
```

## 7. Spring Data JPA vs Querydsl

### Spring Data JPA

**장점:**
- 간단한 CRUD 작업을 위한 메서드 자동 생성
- 메서드 이름만으로 쿼리 생성 가능
- 페이징, 정렬 기능 내장
- 적은 코드량으로 빠른 개발 가능

**단점:**
- 복잡한 동적 쿼리 작성이 어려움
- 메서드 이름이 길어질 수 있음
- 타입 안전성이 부족함 (문자열 기반)

### Querydsl

**장점:**
- 타입 안전한 쿼리 작성 (컴파일 시점에 오류 발견)
- 동적 쿼리 작성이 용이함
- 복잡한 쿼리, 조인, 서브쿼리 등 지원
- 코드 자동완성 지원으로 생산성 향상

**단점:**
- 초기 설정이 복잡함
- 학습 곡선이 있음
- 단순 CRUD에는 과도한 코드량

### 비교 표

| 기능 | Spring Data JPA | Querydsl |
|------|-----------------|----------|
| 설정 복잡도 | 낮음 | 높음 |
| 타입 안전성 | 낮음 | 높음 |
| 동적 쿼리 | 제한적 | 우수함 |
| 코드량 | 적음 | 많음 |
| 학습 곡선 | 낮음 | 중간 |
| 복잡한 쿼리 | 제한적 | 우수함 |
| IDE 지원 | 제한적 | 우수함 |

## 8. 실무 활용 전략

### 두 기술의 조합 사용

```java
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {
    // Spring Data JPA 메서드
    List<Member> findByUsername(String username);
}

public interface MemberRepositoryCustom {
    // Querydsl로 구현할 메서드
    List<Member> search(String username, Integer age);
}

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    @Override
    public List<Member> search(String username, Integer age) {
        return queryFactory
            .selectFrom(QMember.member)
            .where(
                usernameEq(username),
                ageGt(age)
            )
            .fetch();
    }
    
    private BooleanExpression usernameEq(String username) {
        return username != null ? QMember.member.username.eq(username) : null;
    }
    
    private BooleanExpression ageGt(Integer age) {
        return age != null ? QMember.member.age.gt(age) : null;
    }
}
```

### 권장 사용 패턴

1. **단순 CRUD**: Spring Data JPA 사용
2. **복잡한 동적 쿼리**: Querydsl 사용
3. **성능 최적화가 필요한 쿼리**: Querydsl + 페치 조인 사용
4. **대량 데이터 처리**: Spring Data JPA의 페이징 + Querydsl 조합

### 실무 팁

1. **엔티티 설계 시 고려사항**
   - 양방향 연관관계 신중하게 설정
   - 지연 로딩(LAZY) 기본 사용
   - 연관관계의 주인 설정 명확히

2. **성능 최적화**
   - 필요한 컬럼만 조회 (DTO 프로젝션)
   - 페치 조인 적절히 사용
   - 배치 사이즈 설정

3. **유지보수성**
   - 복잡한 쿼리는 별도 클래스로 분리
   - 동적 쿼리 조건은 메서드로 분리
   - 테스트 코드 작성

## 결론

Spring Data JPA와 Querydsl은 각각 장단점이 있으며, 실무에서는 두 기술을 적절히 조합하여 사용하는 것이 효과적입니다. 단순한 CRUD 작업에는 Spring Data JPA를 사용하고, 복잡한 동적 쿼리가 필요한 경우에는 Querydsl을 활용하는 전략이 권장됩니다. 두 기술을 함께 사용함으로써 개발 생산성과 애플리케이션 성능을 모두 확보할 수 있습니다.