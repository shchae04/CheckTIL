# N+1 문제란?

N+1 문제는 ORM(Object-Relational Mapping) 프레임워크를 사용할 때 발생하는 성능 이슈입니다. 이 문제는 데이터베이스에서 N개의 레코드를 조회한 후, 각 레코드와 연관된 데이터를 가져오기 위해 추가로 N번의 쿼리가 실행되는 상황을 말합니다.

## N+1 문제가 발생하는 원인

N+1 문제는 주로 다음과 같은 상황에서 발생합니다:

1. **지연 로딩(Lazy Loading)**: 연관 엔티티를 실제로 사용할 때까지 로딩을 지연시키는 전략을 사용할 때 발생합니다.
2. **연관 관계 매핑**: 일대다(1:N), 다대일(N:1), 다대다(N:M) 관계에서 연관된 엔티티를 조회할 때 발생합니다.
3. **ORM의 기본 동작 방식**: 대부분의 ORM은 기본적으로 지연 로딩을 사용하며, 명시적으로 설정하지 않으면 N+1 문제가 발생할 수 있습니다.

## 예시로 이해하기

### JPA/Hibernate 예시

다음과 같은 엔티티 관계가 있다고 가정해 봅시다:

```java
@Entity
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<Book> books;
    
    // 생성자, getter, setter 생략
}

@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;
    
    // 생성자, getter, setter 생략
}
```

이제 모든 저자와 그들의 책을 조회하는 코드를 작성해 봅시다:

```java
List<Author> authors = authorRepository.findAll();
for (Author author : authors) {
    System.out.println("저자: " + author.getName());
    // 여기서 지연 로딩된 books에 접근하면 추가 쿼리 발생
    for (Book book : author.getBooks()) {
        System.out.println("- 책: " + book.getTitle());
    }
}
```

이 코드를 실행하면 다음과 같은 쿼리가 실행됩니다:

1. 모든 저자를 조회하는 쿼리 1번 (SELECT * FROM author)
2. 각 저자의 책을 조회하는 쿼리 N번 (SELECT * FROM book WHERE author_id = ?)

따라서 총 N+1번의 쿼리가 실행됩니다. 여기서 N은 저자의 수입니다.

## N+1 문제의 해결 방법

### 1. 즉시 로딩(Eager Loading)

즉시 로딩을 사용하면 연관 엔티티를 함께 로딩할 수 있습니다. 하지만 이 방법은 항상 연관 엔티티를 함께 로딩하므로 불필요한 데이터를 가져올 수 있어 권장되지 않습니다.

```java
@OneToMany(mappedBy = "author", fetch = FetchType.EAGER)
private List<Book> books;
```

### 2. 조인 페치(Join Fetch)

JPQL의 JOIN FETCH를 사용하여 연관 엔티티를 함께 조회할 수 있습니다.

```java
@Query("SELECT a FROM Author a JOIN FETCH a.books")
List<Author> findAllWithBooks();
```

### 3. EntityGraph 사용

JPA의 EntityGraph를 사용하여 특정 속성을 함께 로딩할 수 있습니다.

```java
@EntityGraph(attributePaths = {"books"})
@Query("SELECT a FROM Author a")
List<Author> findAllWithBooks();
```

### 4. 배치 사이즈 설정

Hibernate의 `@BatchSize` 어노테이션을 사용하여 한 번에 여러 연관 엔티티를 로딩할 수 있습니다.

```java
@OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
@BatchSize(size = 100)
private List<Book> books;
```

또는 `hibernate.default_batch_fetch_size` 속성을 설정하여 전역적으로 배치 사이즈를 지정할 수 있습니다.

```properties
# application.properties
spring.jpa.properties.hibernate.default_batch_fetch_size=100
```

### 5. DTO 프로젝션 사용

필요한 데이터만 선택적으로 조회하는 DTO 프로젝션을 사용할 수 있습니다.

```java
@Query("SELECT new com.example.dto.AuthorDto(a.id, a.name, b.id, b.title) " +
       "FROM Author a JOIN a.books b")
List<AuthorDto> findAllAuthorsWithBooks();
```

## 각 해결 방법의 장단점

| 해결 방법 | 장점 | 단점 |
|----------|------|------|
| 즉시 로딩 | 구현이 간단함 | 불필요한 데이터 로딩, 성능 저하 가능성 |
| 조인 페치 | 한 번의 쿼리로 필요한 데이터 로딩 | 카테시안 곱(Cartesian Product) 발생 가능, 페이징 처리 어려움 |
| EntityGraph | 필요한 경우에만 연관 엔티티 로딩 | 복잡한 그래프에서는 관리 어려움 |
| 배치 사이즈 | 여러 연관 엔티티를 효율적으로 로딩 | 최적의 배치 사이즈 결정이 어려움 |
| DTO 프로젝션 | 필요한 데이터만 정확히 선택 | 추가 코드 작성 필요, 엔티티 변경 시 DTO도 수정 필요 |

## 결론

N+1 문제는 ORM을 사용할 때 흔히 발생하는 성능 이슈입니다. 이 문제를 해결하기 위해서는 애플리케이션의 요구사항과 데이터 접근 패턴을 고려하여 적절한 해결 방법을 선택해야 합니다. 일반적으로 조인 페치나 배치 사이즈 설정이 가장 많이 사용되며, 복잡한 경우에는 여러 방법을 조합하여 사용하는 것이 좋습니다.
