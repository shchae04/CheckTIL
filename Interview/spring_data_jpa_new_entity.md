# Spring Data JPA에서 새로운 Entity인지 판단하는 방법

Spring Data JPA에서는 엔티티가 새로운 것인지(새로 생성된 것인지) 또는 기존에 존재하는 것인지 판단하는 여러 방법이 있습니다. 이 판단은 저장(save) 작업 시 insert 또는 update 중 어떤 작업을 수행할지 결정하는 데 중요합니다.

## 1. SimpleJpaRepository의 isNew() 메서드

Spring Data JPA의 `SimpleJpaRepository` 클래스는 `save()` 메서드 내부에서 엔티티가 새로운 것인지 판단하기 위해 `isNew()` 메서드를 사용합니다.

```java
@Transactional
public <S extends T> S save(S entity) {
    if (entityInformation.isNew(entity)) {
        em.persist(entity);
        return entity;
    } else {
        return em.merge(entity);
    }
}
```

## 2. 새로운 엔티티 판단 기준

Spring Data JPA에서는 다음과 같은 순서로 엔티티가 새로운 것인지 판단합니다:

### 2.1. @Version 어노테이션이 있는 경우

엔티티에 `@Version` 어노테이션이 적용된 필드가 있다면, 해당 필드의 값이 `null`인 경우 새로운 엔티티로 판단합니다.

```java
@Entity
public class Product {
    @Id
    private Long id;
    
    private String name;
    
    @Version
    private Long version;
    
    // getters and setters
}
```

### 2.2. Persistable 인터페이스 구현

엔티티가 `Persistable<ID>` 인터페이스를 구현한 경우, 해당 인터페이스의 `isNew()` 메서드 결과를 사용합니다.

```java
@Entity
public class Customer implements Persistable<Long> {
    @Id
    private Long id;
    
    private String name;
    
    @CreatedDate
    private LocalDateTime createdDate;
    
    @Override
    public Long getId() {
        return id;
    }
    
    @Override
    public boolean isNew() {
        return createdDate == null;
    }
    
    // other methods
}
```

### 2.3. 기본 키(ID) 값 확인

위의 두 방법이 적용되지 않는 경우, 기본 키(ID)의 값을 확인합니다:

- 숫자형 ID(Long, Integer 등)인 경우: ID가 `null`이거나 0이면 새로운 엔티티로 판단
- 그 외 타입의 ID: ID가 `null`이면 새로운 엔티티로 판단

```java
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // null이거나 0이면 새로운 엔티티로 판단
    
    private String username;
    
    // getters and setters
}
```

## 3. 주의사항

### 3.1. ID 직접 할당 시 문제점

엔티티의 ID를 직접 할당하는 경우(예: `@GeneratedValue`를 사용하지 않고 ID를 직접 설정), Spring Data JPA는 해당 엔티티를 새로운 것이 아니라고 판단할 수 있습니다. 이로 인해 `em.persist()` 대신 `em.merge()`가 호출되어 성능 저하나 예상치 못한 동작이 발생할 수 있습니다.

### 3.2. Persistable 인터페이스 활용

ID를 직접 할당하는 경우에는 `Persistable` 인터페이스를 구현하여 명시적으로 엔티티가 새로운 것인지 판단하는 로직을 제공하는 것이 좋습니다. 일반적으로 `@CreatedDate`와 함께 사용하면 효과적입니다.

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Book implements Persistable<String> {
    @Id
    private String isbn;
    
    private String title;
    
    @CreatedDate
    private LocalDateTime createdDate;
    
    @Override
    public String getId() {
        return isbn;
    }
    
    @Override
    public boolean isNew() {
        return createdDate == null;
    }
    
    // other methods
}
```

## 4. 실제 사용 예시

### 4.1. UUID를 ID로 사용하는 경우

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Document implements Persistable<UUID> {
    @Id
    private UUID id;
    
    private String content;
    
    @CreatedDate
    private LocalDateTime createdDate;
    
    public Document() {
        this.id = UUID.randomUUID();
    }
    
    @Override
    public UUID getId() {
        return id;
    }
    
    @Override
    public boolean isNew() {
        return createdDate == null;
    }
}
```

### 4.2. 복합 키를 사용하는 경우

```java
@Entity
@EntityListeners(AuditingEntityListener.class)
public class OrderItem implements Persistable<OrderItemId> {
    @EmbeddedId
    private OrderItemId id;
    
    private int quantity;
    
    @CreatedDate
    private LocalDateTime createdDate;
    
    @Override
    public OrderItemId getId() {
        return id;
    }
    
    @Override
    public boolean isNew() {
        return createdDate == null;
    }
}

@Embeddable
public class OrderItemId implements Serializable {
    private Long orderId;
    private Long productId;
    
    // equals, hashCode, getters, setters
}
```

## 결론

Spring Data JPA에서 엔티티가 새로운 것인지 판단하는 방법은 여러 가지가 있으며, 상황에 따라 적절한 방법을 선택해야 합니다. 특히 ID를 직접 할당하는 경우에는 `Persistable` 인터페이스를 구현하여 명확한 판단 기준을 제공하는 것이 좋습니다. 이를 통해 JPA의 `persist`와 `merge` 작업이 의도한 대로 수행되도록 할 수 있습니다.