# JDBC Template 가 뭔가요?

## 0. 한눈에 보기(초간단)
- Spring의 JDBC 추상화로, 순수 JDBC의 보일러플레이트(커넥션/스테이트먼트/리절트셋 관리, 예외 처리)를 템플릿 패턴으로 제거합니다.
- DataSource로부터 커넥션을 받고, `SQLException`을 스프링 공통 예외(`DataAccessException` 계층)로 변환해 일관된 예외 처리와 트랜잭션 롤백 기준을 제공합니다.
- 트랜잭션은 `@Transactional` + `DataSourceTransactionManager`로 쉽게 연동됩니다(같은 DataSource 사용 시 동일 트랜잭션 참여).
- 대표 구현: `JdbcTemplate`, 파라미터 이름 기반의 `NamedParameterJdbcTemplate`, 편의 클래스 `SimpleJdbcInsert/Update`.

---

## 1. 왜 JdbcTemplate를 쓰나? (Plain JDBC의 문제)
- 반복/장황한 코드: 커넥션 열기/닫기, PreparedStatement 생성, 파라미터 바인딩, ResultSet 순회, finally에서 자원 해제 등 보일러플레이트가 많음.
- 예외 처리: DB 벤더별 SQLState/에러 코드를 분석해 케이스별로 처리하기 번거로움.
- 트랜잭션/자원 누수 위험: try-catch-finally 실수로 누수/롤백 누락 가능.

JdbcTemplate의 이점
- 템플릿-콜백: 자원 관리와 예외 변환을 템플릿이 담당 → 비즈니스 로직에 집중.
- 일관 예외: `DataAccessException` 계층으로 런타임 예외 변환 → 서비스 계층에서 DB 종속성 낮춤.
- 간결한 API: `query`, `queryForObject`, `update`, `batchUpdate` 등 고수준 편의 메서드.

## 2. 구성 요소 빠르게 보기
- DataSource: 커넥션 풀(HikariCP 등) 제공. Boot는 기본적으로 Hikari 사용.
- JdbcTemplate: 핵심 템플릿. 위치 파라미터(?) 기반 바인딩.
- NamedParameterJdbcTemplate: `:name` 같은 이름 기반 파라미터 바인딩.
- RowMapper / ResultSetExtractor: ResultSet → 객체 변환 전략.
- PreparedStatementSetter / KeyHolder: 파라미터 세팅, 생성 키 반환.
- SimpleJdbcInsert/Update: 반복되는 Insert/Update 보일러플레이트 축소.
- JdbcClient(Spring 6+): 가독성 좋은 플루언트 API(선택 사항, 최신).

## 3. 설정 방법
### 3.1 Spring Boot (권장)
- 의존성: `spring-boot-starter-jdbc` 또는 `spring-boot-starter-data-jdbc`.
- application.yml에 DataSource 설정을 하면 `JdbcTemplate`, `NamedParameterJdbcTemplate` 빈이 자동 등록됩니다.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/app
    username: app
    password: secret
  jdbc:
    template:
      fetch-size: 1000   # 선택
      max-rows: 0        # 선택
```

### 3.2 수동 등록(Java Config)
```java
@Configuration
public class JdbcConfig {
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        JdbcTemplate jt = new JdbcTemplate(dataSource);
        jt.setFetchSize(1000); // 선택
        return jt;
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}
```

## 4. 핵심 사용법
### 4.1 조회: query, queryForObject
```java
class Post {
    Long id; String title; Integer likeCount;
    // getters/setters/constructor
}

RowMapper<Post> postMapper = (rs, rowNum) -> {
    Post p = new Post();
    p.setId(rs.getLong("id"));
    p.setTitle(rs.getString("title"));
    p.setLikeCount(rs.getInt("like_count"));
    return p;
};

// 다건 조회
List<Post> posts = jdbcTemplate.query(
    "SELECT id, title, like_count FROM posts WHERE author_id = ? ORDER BY id DESC",
    postMapper,
    authorId
);

// 단건 조회(정확히 한 행 가정). 없으면 EmptyResultDataAccessException, 여러 행이면 IncorrectResultSizeDataAccessException
Post one = jdbcTemplate.queryForObject(
    "SELECT id, title, like_count FROM posts WHERE id = ?",
    postMapper,
    postId
);
```

- 필드명이 자바 빈과 매핑될 수 있으면 `BeanPropertyRowMapper`로 간소화 가능
```java
List<Post> posts2 = jdbcTemplate.query(
    "SELECT id, title, like_count AS likeCount FROM posts",
    new BeanPropertyRowMapper<>(Post.class)
);
```

### 4.2 변경: update (INSERT/UPDATE/DELETE)
```java
int updated = jdbcTemplate.update(
    "UPDATE posts SET title = ? WHERE id = ?",
    newTitle, postId
);

int deleted = jdbcTemplate.update(
    "DELETE FROM posts WHERE id = ?",
    postId
);
```

### 4.3 생성 키 반환: KeyHolder
```java
KeyHolder keyHolder = new GeneratedKeyHolder();
jdbcTemplate.update(con -> {
    PreparedStatement ps = con.prepareStatement(
        "INSERT INTO posts(title, like_count) VALUES(?, ?)",
        Statement.RETURN_GENERATED_KEYS
    );
    ps.setString(1, title);
    ps.setInt(2, 0);
    return ps;
}, keyHolder);
Number newId = keyHolder.getKey();
```

### 4.4 이름 기반 파라미터: NamedParameterJdbcTemplate
```java
String sql = """
INSERT INTO posts(title, author_id, like_count)
VALUES(:title, :authorId, :likeCount)
""";
MapSqlParameterSource params = new MapSqlParameterSource()
        .addValue("title", title)
        .addValue("authorId", authorId)
        .addValue("likeCount", 0);

namedParameterJdbcTemplate.update(sql, params);

List<Post> list = namedParameterJdbcTemplate.query(
    "SELECT * FROM posts WHERE author_id = :authorId AND created_at >= :from",
    Map.of("authorId", authorId, "from", fromDateTime),
    new BeanPropertyRowMapper<>(Post.class)
);
```

### 4.5 배치 처리: batchUpdate
```java
List<Post> bulk = ...;
jdbcTemplate.batchUpdate(
    "INSERT INTO posts(title, author_id, like_count) VALUES(?, ?, ?)",
    new BatchPreparedStatementSetter() {
        public void setValues(PreparedStatement ps, int i) throws SQLException {
            Post p = bulk.get(i);
            ps.setString(1, p.getTitle());
            ps.setLong(2, p.getAuthorId());
            ps.setInt(3, p.getLikeCount());
        }
        public int getBatchSize() { return bulk.size(); }
    }
);
```

### 4.6 SimpleJdbcInsert (선택)
```java
SimpleJdbcInsert insert = new SimpleJdbcInsert(jdbcTemplate)
        .withTableName("posts")
        .usingGeneratedKeyColumns("id");

Number id = insert.executeAndReturnKey(Map.of(
    "title", title,
    "author_id", authorId,
    "like_count", 0
));
```

## 5. 트랜잭션 연동
- `@Transactional`을 서비스/리포지토리 계층에 적용하면 같은 DataSource를 쓰는 JdbcTemplate 호출들이 하나의 트랜잭션으로 묶입니다.
- 트랜잭션 매니저: `DataSourceTransactionManager` (Spring Boot는 DataSource가 있으면 자동 등록).
- 예시
```java
@Service
@RequiredArgsConstructor
public class PostService {
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public void likePost(long postId) {
        int updated = jdbcTemplate.update(
            "UPDATE posts SET like_count = like_count + 1 WHERE id = ?",
            postId
        );
        if (updated == 0) throw new IllegalStateException("Post not found");
        // 추가 DB 작업이 실패하면 전체 롤백
    }
}
```

## 6. 예외 변환(DataAccessException)
- 벤더별 `SQLException`을 런타임 예외로 변환해줍니다. 대표 타입
  - `DuplicateKeyException`, `DataIntegrityViolationException`, `DeadlockLoserDataAccessException`, `EmptyResultDataAccessException` 등.
- 장점: 상위 계층에서 DB 종류와 무관하게 일관된 예외 처리가 가능, `@Transactional`의 롤백 규칙도 단순화.

## 7. 성능 & 베스트 프랙티스
- 다건 대량 작업은 `batchUpdate`로 네트워크 왕복을 줄이세요.
- 대용량 조회는 `fetchSize`/`maxRows` 조정, 필요한 컬럼만 선택, 스트리밍(드라이버 의존) 고려.
- `queryForObject`는 정확히 1행일 때만 사용, 0행/다행 상황에 대한 예외 처리 유의.
- 매핑은 가능하면 `RowMapper`로 명시적 관리. 간단한 경우 `BeanPropertyRowMapper`로 생산성 향상.
- 읽기/쓰기 분리 환경에서는 DataSource 라우팅(AbstractRoutingDataSource)과 함께 사용.
- SQL은 반드시 바인딩 파라미터를 사용해 SQL 인젝션을 방지.
- 트랜잭션 경계를 서비스 계층에 명확히 두고, 템플릿 호출은 짧고 빠르게.

## 8. JPA/MyBatis와 비교, 언제 쓰나
- JdbcTemplate
  - 장점: 가볍고 빠름, 제어가 확실, SQL을 직접 관리. 복잡한 O/R 매핑 비용 없음.
  - 단점: 매핑과 SQL 관리 비용이 개발자에게 전가.
- JPA/Hibernate
  - 장점: 객체 중심 모델, 변경감지, 캐시, 연관관계/페치 전략 등 생산성 높음.
  - 단점: 러닝커브, 성능/쿼리 튜닝 난이도, 복잡한 시나리오에서 예측 어려움.
- MyBatis
  - 장점: SQL 주도 + 동적 SQL 편의, 매핑 XML/어노테이션으로 체계화.
  - 단점: 러닝커브/설정, 러ntime 매핑.
- 선택 가이드
  - 단순하고 고성능이 필요한 CRUD/리포트성 쿼리 → JdbcTemplate 유리.
  - 도메인 모델 중심, 연관관계 풍부, 캐시 활용 → JPA 유리.
  - SQL을 손에 쥐고 유지보수하되 편의도 필요 → MyBatis.

## 9. 최신: JdbcClient (Spring 6/Boot 3)
```java
List<Post> posts = jdbcClient.sql("SELECT * FROM posts WHERE author_id = :id")
    .param("id", authorId)
    .query(Post.class)
    .list();
```
- 더 간결한 플루언트 스타일. 내부적으로도 JdbcTemplate 기반이며, 선택적으로 도입 가능.

## 10. 인터뷰 한 줄 요약
- JdbcTemplate은 DataSource 기반으로 JDBC 보일러플레이트를 없애고, `SQLException`을 일관된 `DataAccessException`으로 변환하며, `@Transactional`과 함께 안전하고 간결하게 SQL을 다루게 해주는 스프링의 경량 데이터 접근 템플릿입니다.

## 11. 참고
- Spring Framework Reference: Data Access with JDBC (JdbcTemplate)
- Spring Boot Reference: Data access, JDBC support
- SimpleJdbcInsert/Update, NamedParameterJdbcTemplate JavaDoc
- Spring 6 JdbcClient 소개 블로그/레퍼런스


## 12. JPA Pageable과 비교: JdbcTemplate에서 페이징
- JPA(Spring Data JPA)의 Pageable은 page/size/sort를 받아 자동으로 OFFSET/LIMIT와 ORDER BY를 적용하고, total count 쿼리도 함께 만들어 Page<T>를 리턴합니다.
- JdbcTemplate에서는 동일 기능을 직접 구현해야 합니다: (1) total count 쿼리, (2) 데이터 쿼리 + LIMIT/OFFSET, (3) Sort 처리(화이트리스트), (4) PageImpl 생성.

### 12.1 JPA에서의 Pageable 사용 예
```java
Page<Post> page = postRepository.findByAuthorId(
    authorId,
    PageRequest.of(pageNum, size, Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id")))
);
// JPA는 기본적으로 count 쿼리를 생성(또는 @Query(countQuery=...) 필요)하고, ORDER BY/페이징을 자동 적용
```

### 12.2 JdbcTemplate로 OFFSET/LIMIT 기반 페이징 구현
- 핵심 아이디어
  - count: SELECT COUNT(*) FROM ... WHERE ...
  - data: SELECT ... FROM ... WHERE ... ORDER BY ... LIMIT ? OFFSET ?
  - 정렬: 사용자가 보낸 sort 필드는 반드시 허용 컬럼 화이트리스트로 매핑 후 문자열로 조립(바인딩 파라미터로 ORDER BY 컬럼/방향을 넣을 수 없기 때문).
  - 결과: new PageImpl<>(content, pageable, total)

```java
public Page<Post> findByAuthor(long authorId, Pageable pageable) {
    // 1) total count
    Long total = jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM posts p WHERE p.author_id = ?",
        Long.class,
        authorId
    );

    // 2) ORDER BY (화이트리스트 기반 안전 가공)
    Map<String, String> allowed = Map.of(
        "createdAt", "p.created_at",
        "id", "p.id",
        "likeCount", "p.like_count"
    );
    StringBuilder ob = new StringBuilder();
    for (Sort.Order o : pageable.getSort()) {
        String col = allowed.get(o.getProperty());
        if (col != null) {
            if (ob.length() == 0) ob.append(" ORDER BY ");
            else ob.append(", ");
            ob.append(col).append(" ").append(o.isAscending() ? "ASC" : "DESC");
        }
    }
    if (ob.length() == 0) {
        ob.append(" ORDER BY p.id DESC"); // 기본 정렬
    }

    // 3) data 조회 (LIMIT/OFFSET)
    String sql = "SELECT p.id, p.title, p.like_count FROM posts p WHERE p.author_id = ?" + ob + " LIMIT ? OFFSET ?";
    List<Post> content = jdbcTemplate.query(
        sql,
        postMapper,                 // RowMapper<Post>
        authorId,
        pageable.getPageSize(),
        pageable.getOffset()       // 일부 드라이버는 int 선호 → (int) pageable.getOffset()
    );

    return new PageImpl<>(content, pageable, total);
}
```

- NamedParameterJdbcTemplate 예시(동일 아이디어)
```java
String sql = "SELECT p.id, p.title FROM posts p WHERE p.author_id = :authorId" + ob + " LIMIT :limit OFFSET :offset";
MapSqlParameterSource params = new MapSqlParameterSource()
    .addValue("authorId", authorId)
    .addValue("limit", pageable.getPageSize())
    .addValue("offset", pageable.getOffset());
List<Post> list = namedParameterJdbcTemplate.query(sql, params, new BeanPropertyRowMapper<>(Post.class));
```

- 주의사항
  - ORDER BY는 화이트리스트에 등록된 컬럼만 허용하고, 사용자 입력을 그대로 문자열 연결하지 않기(SQL 인젝션 위험).
  - Pageable.getOffset()은 long이므로 드라이버에 따라 (int) 캐스팅이 필요할 수 있음.
  - 복잡한 조인/서브쿼리에서는 count 쿼리를 별도로 단순화해 성능을 확보하세요.

### 12.3 키셋(Keyset) 페이지네이션 권장 시나리오
- 대규모 테이블에서 page가 깊어질수록 OFFSET 비용이 커지고 중복/누락 문제가 있는 경우, 커서(키셋) 페이지네이션을 고려하세요.
- 구현 방법과 SQL 패턴은 별도 노트에 정리되어 있습니다: [좋아요 수 기반 페이지네이션](./likes_based_pagination.md)
- 핵심: 강한 타이브레이커(예: (like_count DESC, id DESC))와 해당 복합 인덱스 설계, 다음 페이지는 커서보다 "작은"(또는 큰) 범위를 조회.
