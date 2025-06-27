# 엔티티 그래프(Entity Graph)란?

## 목차
1. [엔티티 그래프란?](#1-엔티티-그래프란)
2. [엔티티 그래프의 필요성](#2-엔티티-그래프의-필요성)
3. [엔티티 그래프 종류](#3-엔티티-그래프-종류)
4. [엔티티 그래프 사용 방법](#4-엔티티-그래프-사용-방법)
5. [실제 사용 예시](#5-실제-사용-예시)
6. [주의사항 및 모범 사례](#6-주의사항-및-모범-사례)

## 1. 엔티티 그래프란?

엔티티 그래프(Entity Graph)는 JPA 2.1에서 도입된 기능으로, 연관된 엔티티들을 효율적으로 조회하기 위한 방법을 제공합니다. 이는 특히 N+1 문제를 해결하는 데 유용하며, 애플리케이션에서 필요한 데이터만 선택적으로 로딩할 수 있게 해줍니다.

엔티티 그래프는 다음과 같은 역할을 합니다:
- 특정 엔티티와 그 연관 엔티티들의 로딩 전략을 동적으로 정의
- 쿼리 실행 시점에 필요한 데이터만 효율적으로 로딩
- 불필요한 데이터 로딩을 방지하여 성능 최적화

## 2. 엔티티 그래프의 필요성

### N+1 문제 해결

JPA에서 가장 흔한 성능 문제 중 하나는 N+1 쿼리 문제입니다. 예를 들어, 부서(Department)와 직원(Employee) 엔티티가 있을 때, 모든 부서와 각 부서에 속한 직원들을 조회하려면:

1. 먼저 모든 부서를 조회하는 쿼리 1번
2. 각 부서마다 직원을 조회하는 쿼리 N번 (부서 수만큼)

이렇게 총 N+1번의 쿼리가 실행됩니다. 엔티티 그래프를 사용하면 이 문제를 해결할 수 있습니다.

### 동적인 Fetch 전략

엔티티의 연관관계에 대한 Fetch 전략(EAGER, LAZY)은 보통 엔티티 클래스 정의 시점에 고정됩니다. 하지만 실제 애플리케이션에서는 상황에 따라 다른 Fetch 전략이 필요할 수 있습니다. 엔티티 그래프를 사용하면 런타임에 필요에 따라 Fetch 전략을 동적으로 변경할 수 있습니다.

## 3. 엔티티 그래프 종류

JPA에서는 두 가지 유형의 엔티티 그래프를 제공합니다:

### 1. Named 엔티티 그래프

엔티티 클래스에 정적으로 정의되는 엔티티 그래프입니다. `@NamedEntityGraph` 어노테이션을 사용하여 정의합니다.

```java
@Entity
@NamedEntityGraph(
    name = "Department.employees",
    attributeNodes = @NamedAttributeNode("employees")
)
public class Department {
    @Id
    private Long id;
    
    private String name;
    
    @OneToMany(mappedBy = "department")
    private List<Employee> employees;
    
    // getters and setters
}
```

### 2. Dynamic 엔티티 그래프

코드에서 동적으로 생성되는 엔티티 그래프입니다. `EntityGraph` 인터페이스와 그 구현체를 사용하여 정의합니다.

```java
EntityGraph<Department> entityGraph = entityManager.createEntityGraph(Department.class);
entityGraph.addAttributeNodes("employees");
```

## 4. 엔티티 그래프 사용 방법

엔티티 그래프는 다음과 같은 방법으로 사용할 수 있습니다:

### 1. EntityManager의 find() 메서드 사용

```java
Map<String, Object> properties = new HashMap<>();
properties.put("javax.persistence.fetchgraph", entityGraph);
Department department = entityManager.find(Department.class, departmentId, properties);
```

### 2. JPQL 쿼리에서 사용

```java
TypedQuery<Department> query = entityManager.createQuery(
    "SELECT d FROM Department d WHERE d.name = :name", Department.class);
query.setParameter("name", "IT");
query.setHint("javax.persistence.fetchgraph", entityGraph);
Department department = query.getSingleResult();
```

### 3. Spring Data JPA에서 사용

```java
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    
    @EntityGraph(attributePaths = {"employees"})
    Department findByName(String name);
    
    @EntityGraph(value = "Department.employees")
    Optional<Department> findById(Long id);
}
```

## 5. 실제 사용 예시

다음은 엔티티 그래프를 사용한 실제 예시입니다:

### 엔티티 정의

```java
@Entity
@NamedEntityGraph(
    name = "Post.withComments",
    attributeNodes = @NamedAttributeNode("comments")
)
@NamedEntityGraph(
    name = "Post.withCommentsAndAuthor",
    attributeNodes = {
        @NamedAttributeNode("comments"),
        @NamedAttributeNode("author")
    }
)
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private User author;
    
    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    private List<Comment> comments;
    
    // getters and setters
}

@Entity
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String content;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private User author;
    
    // getters and setters
}

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<Post> posts;
    
    @OneToMany(mappedBy = "author", fetch = FetchType.LAZY)
    private List<Comment> comments;
    
    // getters and setters
}
```

### Spring Data JPA Repository

```java
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    
    // Named 엔티티 그래프 사용
    @EntityGraph(value = "Post.withComments")
    Optional<Post> findById(Long id);
    
    // 동적 엔티티 그래프 사용
    @EntityGraph(attributePaths = {"comments", "author"})
    List<Post> findByTitleContaining(String title);
    
    // 특정 상황에 맞는 엔티티 그래프 선택
    @EntityGraph(value = "Post.withCommentsAndAuthor")
    List<Post> findByAuthorUsername(String username);
}
```

### 서비스 레이어

```java
@Service
@Transactional(readOnly = true)
public class PostService {
    
    private final PostRepository postRepository;
    private final EntityManager entityManager;
    
    public PostService(PostRepository postRepository, EntityManager entityManager) {
        this.postRepository = postRepository;
        this.entityManager = entityManager;
    }
    
    // Repository의 엔티티 그래프 사용
    public Post getPostWithComments(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
    }
    
    // 동적 엔티티 그래프 생성 및 사용
    public Post getPostWithCustomGraph(Long id) {
        EntityGraph<Post> entityGraph = entityManager.createEntityGraph(Post.class);
        entityGraph.addAttributeNodes("comments");
        entityGraph.addSubgraph("comments").addAttributeNodes("author");
        
        Map<String, Object> properties = new HashMap<>();
        properties.put("javax.persistence.fetchgraph", entityGraph);
        
        return entityManager.find(Post.class, id, properties);
    }
}
```

## 6. 주의사항 및 모범 사례

### 주의사항

1. **과도한 데이터 로딩 방지**: 필요한 연관관계만 포함하여 불필요한 데이터 로딩을 방지해야 합니다.
2. **N+1 문제 인식**: 엔티티 그래프를 사용해도 잘못 설계하면 N+1 문제가 발생할 수 있습니다.
3. **FetchType.EAGER vs 엔티티 그래프**: 엔티티에 FetchType.EAGER를 설정하는 것보다 엔티티 그래프를 사용하는 것이 더 유연합니다.
4. **loadgraph vs fetchgraph**: 
   - `javax.persistence.fetchgraph`: 명시된 속성만 EAGER로 로딩하고 나머지는 LAZY로 로딩
   - `javax.persistence.loadgraph`: 명시된 속성은 EAGER로 로딩하고 나머지는 엔티티에 정의된 FetchType을 따름

### 모범 사례

1. **필요에 따른 그래프 설계**: 비즈니스 요구사항에 맞게 다양한 엔티티 그래프를 설계하세요.
2. **재사용 가능한 Named 엔티티 그래프**: 자주 사용되는 패턴은 Named 엔티티 그래프로 정의하여 재사용하세요.
3. **복잡한 그래프는 동적으로 생성**: 복잡하거나 조건에 따라 달라지는 그래프는 동적으로 생성하세요.
4. **성능 모니터링**: 엔티티 그래프 사용 시 실제 생성되는 SQL 쿼리를 모니터링하여 최적화하세요.
5. **페이징과 함께 사용 시 주의**: 컬렉션을 포함한 엔티티 그래프를 페이징과 함께 사용할 때는 메모리 사용량에 주의해야 합니다.

엔티티 그래프는 JPA에서 성능 최적화와 유연한 데이터 로딩을 위한 강력한 도구입니다. 적절히 사용하면 애플리케이션의 성능을 크게 향상시킬 수 있습니다.