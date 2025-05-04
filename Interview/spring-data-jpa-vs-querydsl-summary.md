# TIL: Spring Data JPA & Querydsl 정리

## 개요
- Spring Data JPA: JPA 위에서 동작하는 스프링 모듈, CRUD 자동 처리
- Querydsl: 코드 기반 동적 쿼리 도구, 복잡한 조건 처리에 유리

---

## 사용 환경 예시

### 엔티티: `Member`
```java
@Entity
public class Member {
    @Id @GeneratedValue
    private Long id;

    private String username;
    private int age;
}
```

---

## 1. Spring Data JPA

### 기본 사용

```java
public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findByUsername(String username);
}
```

→ 호출 예시:

```java
memberRepository.findByUsername("shchae");
```

→ 자동 실행되는 쿼리:

```sql
SELECT m FROM Member m WHERE m.username = ?
```

---

### 조건 조합 예시

```java
List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
```

→ 호출:

```java
memberRepository.findByUsernameAndAgeGreaterThan("shchae", 20);
```

→ 자동 쿼리:

```sql
SELECT m FROM Member m WHERE m.username = ? AND m.age > ?
```

---

## 2. Querydsl

### Q타입 준비 (Gradle 플러그인으로 자동 생성)
```java
QMember m = QMember.member;
```

### 기본 조회

```java
List<Member> result = queryFactory
    .selectFrom(m)
    .where(m.username.eq("shchae"))
    .fetch();
```

→ 결과: username이 "shchae"인 멤버 리스트

---

### 조건 동적 조립 (BooleanBuilder)

```java
public List<Member> search(String username, Integer age) {
    BooleanBuilder builder = new BooleanBuilder();

    if (username != null) {
        builder.and(m.username.eq(username));
    }
    if (age != null) {
        builder.and(m.age.gt(age));
    }

    return queryFactory
        .selectFrom(m)
        .where(builder)
        .fetch();
}
```

→ 호출 예:

```java
search("shchae", 25);
```

→ 결과: username = 'shchae' AND age > 25

---

## 비교 요약

| 항목 | Spring Data JPA | Querydsl |
|------|------------------|-----------|
| 코드량 | 적음 | 많음 |
| 동적 쿼리 | 없음 | 가능 |
| 타입 안정성 | 낮음 (문자열 기반) | 높음 (컴파일 타임 검증) |
| 사용 난이도 | 쉬움 | 중간 |
| 추천 상황 | 단순 CRUD | 조건 조합, 복잡한 조회 |

---

## 느낀 점

- 기본적인 CRUD는 Spring Data JPA가 편하고 코드도 적음
- 조건이 많아지고 조립해야 할 경우엔 Querydsl이 훨씬 깔끔함
- 실무에서는 둘을 같이 쓰는 경우가 많다 -> 도입 예정
- Querydsl 설정법 추후