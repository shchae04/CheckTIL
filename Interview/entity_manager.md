# 엔티티 매니저(Entity Manager)란?

## 목차
1. [엔티티 매니저란?](#1-엔티티-매니저란)
2. [엔티티 매니저의 주요 기능](#2-엔티티-매니저의-주요-기능)
3. [영속성 컨텍스트(Persistence Context)](#3-영속성-컨텍스트persistence-context)
4. [엔티티 매니저 팩토리와 엔티티 매니저](#4-엔티티-매니저-팩토리와-엔티티-매니저)
5. [엔티티 생명주기](#5-엔티티-생명주기)
6. [실제 사용 예시](#6-실제-사용-예시)

## 1. 엔티티 매니저란?

엔티티 매니저(Entity Manager)는 JPA에서 엔티티를 관리하고 데이터베이스와의 모든 상호작용을 처리하는 핵심 컴포넌트입니다. 엔티티 매니저는 엔티티의 저장, 수정, 삭제, 조회 등 엔티티와 관련된 모든 작업을 수행하며, 영속성 컨텍스트(Persistence Context)를 통해 엔티티의 생명주기를 관리합니다.

엔티티 매니저는 다음과 같은 역할을 담당합니다:
- 엔티티와 데이터베이스 간의 매핑 관리
- 엔티티의 상태 변화 감지 및 데이터베이스 동기화
- 트랜잭션 내에서 데이터 일관성 유지
- 1차 캐시를 통한 성능 최적화
- JPQL(Java Persistence Query Language) 쿼리 실행

## 2. 엔티티 매니저의 주요 기능

### 1. 엔티티 저장 (persist)

`persist()` 메서드는 새로운 엔티티를 영속성 컨텍스트에 저장하고, 트랜잭션 커밋 시 데이터베이스에 저장합니다.

```java
public void saveUser(User user) {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    
    try {
        tx.begin();
        em.persist(user);  // 엔티티를 영속성 컨텍스트에 저장
        tx.commit();       // 트랜잭션 커밋 시 데이터베이스에 반영
    } catch (Exception e) {
        tx.rollback();
        throw e;
    } finally {
        em.close();
    }
}
```

### 2. 엔티티 조회 (find)

`find()` 메서드는 주어진 엔티티 클래스와 기본 키를 사용하여 엔티티를 조회합니다.

```java
public User findUser(Long id) {
    EntityManager em = emf.createEntityManager();
    try {
        return em.find(User.class, id);  // 주어진 ID로 User 엔티티 조회
    } finally {
        em.close();
    }
}
```

### 3. 엔티티 수정 (merge)

`merge()` 메서드는 분리된(detached) 상태의 엔티티를 영속성 컨텍스트에 병합합니다.

```java
public void updateUser(User user) {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    
    try {
        tx.begin();
        User mergedUser = em.merge(user);  // 분리된 엔티티를 영속성 컨텍스트에 병합
        tx.commit();
    } catch (Exception e) {
        tx.rollback();
        throw e;
    } finally {
        em.close();
    }
}
```

### 4. 엔티티 삭제 (remove)

`remove()` 메서드는 영속성 컨텍스트와 데이터베이스에서 엔티티를 삭제합니다.

```java
public void deleteUser(Long id) {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    
    try {
        tx.begin();
        User user = em.find(User.class, id);  // 먼저 엔티티를 조회
        if (user != null) {
            em.remove(user);  // 엔티티 삭제
        }
        tx.commit();
    } catch (Exception e) {
        tx.rollback();
        throw e;
    } finally {
        em.close();
    }
}
```

### 5. JPQL 쿼리 실행

엔티티 매니저는 JPQL(Java Persistence Query Language)을 사용하여 복잡한 쿼리를 실행할 수 있습니다.

```java
public List<User> findUsersByUsername(String username) {
    EntityManager em = emf.createEntityManager();
    try {
        return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                .setParameter("username", username)
                .getResultList();
    } finally {
        em.close();
    }
}
```

### 6. 네이티브 SQL 쿼리 실행

필요한 경우 네이티브 SQL 쿼리를 직접 실행할 수도 있습니다.

```java
public List<User> findUsersByNativeQuery(String email) {
    EntityManager em = emf.createEntityManager();
    try {
        return em.createNativeQuery("SELECT * FROM users WHERE email = ?", User.class)
                .setParameter(1, email)
                .getResultList();
    } finally {
        em.close();
    }
}
```

## 3. 영속성 컨텍스트(Persistence Context)

영속성 컨텍스트는 엔티티를 영구 저장하는 환경으로, 엔티티 매니저를 통해 엔티티를 관리하는 논리적인 공간입니다. 영속성 컨텍스트는 다음과 같은 특징을 가집니다:

### 1. 1차 캐시

영속성 컨텍스트는 내부에 1차 캐시를 가지고 있어, 한 번 조회한 엔티티는 메모리에 캐싱됩니다. 동일한 엔티티를 다시 조회할 때 데이터베이스 접근 없이 캐시에서 바로 반환합니다.

```java
// 최초 조회 시 데이터베이스에서 조회
User user1 = em.find(User.class, 1L);

// 두 번째 조회 시 1차 캐시에서 바로 반환 (데이터베이스 접근 없음)
User user2 = em.find(User.class, 1L);

// user1과 user2는 동일한 인스턴스
System.out.println(user1 == user2);  // true
```

### 2. 변경 감지(Dirty Checking)

영속성 컨텍스트는 엔티티의 변경을 자동으로 감지하여 트랜잭션 커밋 시 변경된 내용을 데이터베이스에 반영합니다.

```java
EntityManager em = emf.createEntityManager();
EntityTransaction tx = em.getTransaction();

try {
    tx.begin();
    
    // 엔티티 조회
    User user = em.find(User.class, 1L);
    
    // 엔티티 수정 (별도의 update 메서드 호출 없음)
    user.setUsername("newUsername");
    user.setEmail("new@example.com");
    
    // 트랜잭션 커밋 시 변경 감지가 동작하여 UPDATE 쿼리 실행
    tx.commit();
} catch (Exception e) {
    tx.rollback();
    throw e;
} finally {
    em.close();
}
```

### 3. 지연 로딩(Lazy Loading)

영속성 컨텍스트는 지연 로딩을 지원하여 연관된 엔티티를 실제 사용하는 시점에 로딩할 수 있습니다.

```java
// User 엔티티만 로딩 (orders는 아직 로딩되지 않음)
User user = em.find(User.class, 1L);

// orders에 접근하는 시점에 실제 쿼리 실행
List<Order> orders = user.getOrders();  // 지연 로딩 발생
```

### 4. 쓰기 지연(Write-Behind)

영속성 컨텍스트는 트랜잭션을 커밋하기 전까지 데이터베이스에 쿼리를 모아두었다가 한 번에 실행합니다.

```java
EntityManager em = emf.createEntityManager();
EntityTransaction tx = em.getTransaction();

try {
    tx.begin();
    
    // 아직 데이터베이스에 SQL을 보내지 않음
    em.persist(new User("user1", "user1@example.com"));
    em.persist(new User("user2", "user2@example.com"));
    em.persist(new User("user3", "user3@example.com"));
    
    // 커밋하는 순간 데이터베이스에 모아둔 SQL을 한 번에 전송
    tx.commit();
} catch (Exception e) {
    tx.rollback();
    throw e;
} finally {
    em.close();
}
```

## 4. 엔티티 매니저 팩토리와 엔티티 매니저

### 엔티티 매니저 팩토리(EntityManagerFactory)

엔티티 매니저 팩토리는 애플리케이션 전체에서 공유하는 무거운 객체로, 생성 비용이 큽니다. 따라서 애플리케이션 당 하나만 생성하여 사용합니다.

```java
// persistence.xml에 정의된 영속성 유닛 이름으로 팩토리 생성
EntityManagerFactory emf = Persistence.createEntityManagerFactory("myPersistenceUnit");
```

### 엔티티 매니저(EntityManager)

엔티티 매니저는 엔티티 매니저 팩토리에서 생성하며, 데이터베이스 연결과 밀접한 관계가 있는 가벼운 객체입니다. 스레드 간에 공유하면 안 되며, 요청마다 생성하고 사용 후 반드시 닫아야 합니다.

```java
// 엔티티 매니저 팩토리에서 엔티티 매니저 생성
EntityManager em = emf.createEntityManager();
try {
    // 엔티티 매니저 사용
} finally {
    // 사용 후 반드시 닫기
    em.close();
}
```

### Spring에서의 엔티티 매니저 사용

Spring Framework에서는 `@PersistenceContext` 어노테이션을 사용하여 컨테이너가 관리하는 엔티티 매니저를 주입받을 수 있습니다.

```java
@Service
public class UserService {
    
    @PersistenceContext
    private EntityManager em;
    
    @Transactional
    public void saveUser(User user) {
        em.persist(user);
    }
    
    public User findUser(Long id) {
        return em.find(User.class, id);
    }
}
```

## 5. 엔티티 생명주기

JPA에서 엔티티는 다음 네 가지 상태를 가집니다:

### 1. 비영속(New/Transient)

엔티티 객체가 생성되었지만 아직 영속성 컨텍스트에 저장되지 않은 상태입니다.

```java
// 비영속 상태
User user = new User();
user.setUsername("newUser");
user.setEmail("newuser@example.com");
```

### 2. 영속(Managed)

엔티티가 영속성 컨텍스트에 저장되어 관리되는 상태입니다.

```java
// 영속 상태로 전환
em.persist(user);
```

### 3. 준영속(Detached)

영속성 컨텍스트에 저장되었다가 분리된 상태입니다.

```java
// 준영속 상태로 전환
em.detach(user);
// 또는
em.clear();  // 영속성 컨텍스트 초기화
// 또는
em.close();  // 영속성 컨텍스트 종료
```

### 4. 삭제(Removed)

엔티티를 영속성 컨텍스트와 데이터베이스에서 삭제하기로 한 상태입니다.

```java
// 삭제 상태로 전환
em.remove(user);
```

## 6. 실제 사용 예시

### 기본적인 CRUD 작업

```java
@Service
@Transactional
public class UserService {
    
    @PersistenceContext
    private EntityManager em;
    
    // 사용자 생성
    public Long createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        
        em.persist(user);
        return user.getId();
    }
    
    // 사용자 조회
    public User getUser(Long id) {
        return em.find(User.class, id);
    }
    
    // 사용자 정보 수정
    public void updateUser(Long id, String newUsername, String newEmail) {
        User user = em.find(User.class, id);
        if (user != null) {
            user.setUsername(newUsername);
            user.setEmail(newEmail);
            // 변경 감지(dirty checking)에 의해 자동으로 업데이트됨
        }
    }
    
    // 사용자 삭제
    public void deleteUser(Long id) {
        User user = em.find(User.class, id);
        if (user != null) {
            em.remove(user);
        }
    }
    
    // JPQL을 사용한 사용자 검색
    public List<User> findUsersByEmail(String email) {
        return em.createQuery("SELECT u FROM User u WHERE u.email LIKE :email", User.class)
                .setParameter("email", "%" + email + "%")
                .getResultList();
    }
}
```

### 복잡한 쿼리 작성

```java
@Repository
public class OrderRepository {
    
    @PersistenceContext
    private EntityManager em;
    
    // 특정 사용자의 주문 내역 조회 (페이징 처리)
    public List<Order> findOrdersByUser(Long userId, int page, int size) {
        return em.createQuery("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.orderDate DESC", Order.class)
                .setParameter("userId", userId)
                .setFirstResult((page - 1) * size)  // 페이지 시작 위치
                .setMaxResults(size)                // 페이지 크기
                .getResultList();
    }
    
    // 주문 통계 조회 (집계 함수 사용)
    public List<Object[]> getOrderStatsByMonth() {
        return em.createQuery(
                "SELECT FUNCTION('YEAR', o.orderDate) as year, " +
                "FUNCTION('MONTH', o.orderDate) as month, " +
                "COUNT(o) as count, " +
                "SUM(o.totalAmount) as totalAmount " +
                "FROM Order o " +
                "GROUP BY FUNCTION('YEAR', o.orderDate), FUNCTION('MONTH', o.orderDate) " +
                "ORDER BY year DESC, month DESC")
                .getResultList();
    }
    
    // 네이티브 쿼리 사용 예시
    public List<Order> findRecentOrdersNative(int limit) {
        return em.createNativeQuery(
                "SELECT * FROM orders o " +
                "JOIN users u ON o.user_id = u.id " +
                "WHERE u.active = true " +
                "ORDER BY o.order_date DESC " +
                "LIMIT :limit", Order.class)
                .setParameter("limit", limit)
                .getResultList();
    }
}
```

### 트랜잭션 관리

```java
@Service
public class TransferService {
    
    @PersistenceContext
    private EntityManager em;
    
    @Transactional
    public void transferMoney(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        Account fromAccount = em.find(Account.class, fromAccountId);
        Account toAccount = em.find(Account.class, toAccountId);
        
        if (fromAccount == null || toAccount == null) {
            throw new IllegalArgumentException("계좌를 찾을 수 없습니다.");
        }
        
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalStateException("잔액이 부족합니다.");
        }
        
        // 출금 계좌에서 금액 차감
        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        
        // 입금 계좌에 금액 추가
        toAccount.setBalance(toAccount.getBalance().add(amount));
        
        // 트랜잭션 로그 기록
        TransactionLog log = new TransactionLog();
        log.setFromAccount(fromAccount);
        log.setToAccount(toAccount);
        log.setAmount(amount);
        log.setTransactionDate(LocalDateTime.now());
        
        em.persist(log);
        
        // 변경된 엔티티는 트랜잭션 커밋 시 자동으로 데이터베이스에 반영됨
    }
}
```

엔티티 매니저는 JPA의 핵심 컴포넌트로, 엔티티와 데이터베이스 간의 모든 상호작용을 관리합니다. 영속성 컨텍스트를 통해 1차 캐시, 변경 감지, 지연 로딩 등 다양한 기능을 제공하여 개발자가 객체 지향적인 방식으로 데이터베이스를 다룰 수 있게 해줍니다. 엔티티 매니저를 효과적으로 사용하기 위해서는 영속성 컨텍스트의 동작 원리와 엔티티 생명주기를 이해하는 것이 중요합니다.