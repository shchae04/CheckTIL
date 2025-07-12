# Spring 어노테이션: @Component, @Controller, @Service, @Repository의 차이점

## 개요
Spring Framework에서는 다양한 어노테이션을 통해 클래스의 역할과 책임을 명확히 구분합니다. 이 문서에서는 Spring의 핵심 어노테이션인 `@Component`, `@Controller`, `@Service`, `@Repository`의 차이점과 각각의 사용 목적에 대해 설명합니다.

## @Component

`@Component`는 Spring에서 관리되는 객체(Bean)를 정의하는 가장 기본적인 어노테이션입니다.

### 특징
- Spring의 컴포넌트 스캔 메커니즘을 통해 자동으로 감지되고 Spring 컨테이너에 등록됩니다.
- 다른 스테레오타입 어노테이션(`@Controller`, `@Service`, `@Repository`)의 메타 어노테이션으로 사용됩니다.
- 특별한 역할이 정의되지 않은 일반적인 Spring 관리 컴포넌트에 사용됩니다.

### 예시
```java
@Component
public class UtilityComponent {
    // 일반적인 유틸리티 기능을 제공하는 컴포넌트
}
```

## @Controller

`@Controller`는 Spring MVC 패턴에서 사용자의 요청을 처리하는 컨트롤러 역할을 하는 클래스에 사용됩니다.

### 특징
- `@Component`의 특수화된 형태로, 웹 요청을 처리하는 컨트롤러임을 명시합니다.
- Spring MVC에서 요청 핸들링을 위한 특별한 기능을 제공합니다.
- `@RequestMapping` 어노테이션과 함께 사용하여 HTTP 요청을 특정 메서드에 매핑합니다.
- 주로 뷰를 반환하거나 RESTful 웹 서비스에서 데이터를 반환합니다.

### 예시
```java
@Controller
@RequestMapping("/users")
public class UserController {
    
    @GetMapping("/{id}")
    public String getUser(@PathVariable Long id, Model model) {
        // 사용자 정보를 조회하고 뷰에 전달
        return "user-details";
    }
}
```

## @Service

`@Service`는 비즈니스 로직을 처리하는 서비스 계층의 클래스에 사용됩니다.

### 특징
- `@Component`의 특수화된 형태로, 비즈니스 서비스 역할을 명시합니다.
- 현재는 `@Component`와 기능적으로 동일하지만, 향후 Spring에서 특별한 처리를 추가할 가능성을 위해 구분됩니다.
- 비즈니스 로직의 추상화와 캡슐화를 담당합니다.
- 트랜잭션 관리가 주로 이루어지는 계층입니다.

### 예시
```java
@Service
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Transactional
    public User createUser(User user) {
        // 사용자 생성 비즈니스 로직
        return userRepository.save(user);
    }
}
```

## @Repository

`@Repository`는 데이터 접근 계층(Data Access Layer)의 클래스에 사용됩니다.

### 특징
- `@Component`의 특수화된 형태로, 데이터 저장소와의 상호작용을 담당하는 역할을 명시합니다.
- 데이터베이스 예외를 Spring의 통합된 예외 계층으로 변환하는 기능을 제공합니다.
- JPA 예외를 Spring의 DataAccessException으로 자동 변환합니다.
- 데이터 접근과 관련된 예외 처리 메커니즘을 자동으로 활성화합니다.

### 예시
```java
@Repository
public class JpaUserRepository implements UserRepository {
    
    private final EntityManager entityManager;
    
    public JpaUserRepository(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    public User findById(Long id) {
        return entityManager.find(User.class, id);
    }
}
```

## 어노테이션 간의 주요 차이점

1. **목적과 의도**
   - `@Component`: 일반적인 Spring 관리 컴포넌트
   - `@Controller`: 웹 요청을 처리하는 프레젠테이션 계층
   - `@Service`: 비즈니스 로직을 처리하는 서비스 계층
   - `@Repository`: 데이터 접근을 처리하는 영속성 계층

2. **예외 변환**
   - `@Repository`는 데이터 접근 예외를 Spring의 통합된 예외로 변환하는 기능을 제공합니다.
   - 다른 어노테이션들은 이러한 예외 변환 메커니즘을 제공하지 않습니다.

3. **AOP(Aspect-Oriented Programming) 적용**
   - 각 어노테이션은 Spring에서 서로 다른 관점(Aspect)을 적용하기 위한 포인트컷(Pointcut)으로 사용될 수 있습니다.
   - 예: 트랜잭션 관리는 주로 `@Service` 계층에 적용됩니다.

4. **계층 구분**
   - 이러한 어노테이션들은 애플리케이션의 계층을 명확히 구분하여 코드의 가독성과 유지보수성을 향상시킵니다.

## 사용 시 고려사항 및 모범 사례

1. **적절한 어노테이션 선택**
   - 클래스의 역할과 책임에 맞는 어노테이션을 사용하세요.
   - 명확한 역할이 없는 경우에만 `@Component`를 사용하세요.

2. **계층 분리**
   - 각 계층의 책임을 명확히 구분하여 관심사 분리(Separation of Concerns)를 실현하세요.
   - 컨트롤러는 요청 처리와 응답 생성에, 서비스는 비즈니스 로직에, 리포지토리는 데이터 접근에 집중하도록 설계하세요.

3. **의존성 주입**
   - 이러한 어노테이션이 적용된 클래스들은 Spring의 의존성 주입(DI) 메커니즘을 통해 서로 협력합니다.
   - 생성자 주입 방식을 선호하여 불변성과 테스트 용이성을 확보하세요.

## 결론

Spring Framework의 `@Component`, `@Controller`, `@Service`, `@Repository` 어노테이션은 기능적으로는 모두 Spring 컨테이너에 Bean을 등록하는 역할을 하지만, 의미론적으로 애플리케이션의 서로 다른 계층을 표현합니다. 이러한 구분은 코드의 가독성을 높이고, 관심사 분리를 촉진하며, 특정 계층에 특화된 기능(예: 예외 변환)을 제공합니다. 적절한 어노테이션을 사용하여 애플리케이션의 구조를 명확히 하고, 유지보수성을 향상시키는 것이 좋습니다.