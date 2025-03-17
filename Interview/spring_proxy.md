# Spring에서 프록시를 사용하는 이유

Spring Framework에서 프록시(Proxy)는 핵심적인 역할을 담당하며, 다양한 기능을 구현하는 데 사용됩니다. Spring에서 프록시를 사용하는 이유와 그 구현 방식에 대해 알아보겠습니다.

## 프록시란?

**프록시(Proxy)**는 실제 객체를 대신하여 대리 역할을 수행하는 객체입니다. 클라이언트는 프록시 객체를 통해 실제 객체의 메서드를 간접적으로 호출하게 됩니다. 이러한 간접 호출 과정에서 프록시는 추가적인 기능을 제공할 수 있습니다.

```
[클라이언트] → [프록시 객체] → [실제 객체]
```

## Spring에서 프록시를 사용하는 이유

Spring에서 프록시를 사용하는 주요 이유는 다음과 같습니다:

### 1. 관점 지향 프로그래밍(AOP) 구현

Spring의 가장 중요한 기능 중 하나인 AOP(Aspect-Oriented Programming)는 프록시를 통해 구현됩니다. AOP는 횡단 관심사(cross-cutting concerns)를 모듈화하여 코드의 중복을 줄이고 유지보수성을 높이는 프로그래밍 패러다임입니다.

- **로깅, 트랜잭션 관리, 보안, 캐싱** 등의 공통 기능을 비즈니스 로직과 분리하여 관리할 수 있습니다.
- 프록시는 이러한 공통 기능을 메서드 호출 전후에 삽입하는 역할을 합니다.

```java
// AOP 적용 예시
@Aspect
@Component
public class LoggingAspect {
    @Before("execution(* com.example.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("메서드 실행 전: " + joinPoint.getSignature().getName());
    }
}
```

### 2. 선언적 트랜잭션 관리

`@Transactional` 어노테이션을 통한 선언적 트랜잭션 관리는 Spring의 대표적인 기능입니다. 이 기능 역시 프록시를 통해 구현됩니다.

- 프록시는 `@Transactional`이 적용된 메서드 호출을 가로채어 트랜잭션 시작, 커밋, 롤백 등의 작업을 자동으로 처리합니다.
- 개발자는 트랜잭션 관리 코드를 직접 작성하지 않고도 데이터 무결성을 보장할 수 있습니다.

```java
@Service
public class UserService {
    @Transactional
    public void createUser(User user) {
        // 트랜잭션 내에서 실행되는 비즈니스 로직
        userRepository.save(user);
        emailService.sendWelcomeEmail(user);
    }
}
```

### 3. 지연 로딩(Lazy Loading)

JPA/Hibernate와 같은 ORM 프레임워크에서 지연 로딩을 구현할 때도 프록시가 사용됩니다.

- 연관 엔티티를 실제로 사용할 때까지 데이터베이스 조회를 지연시켜 성능을 최적화합니다.
- 프록시 객체는 실제 데이터가 필요한 시점에 데이터베이스에서 정보를 로드합니다.

### 4. 원격 메서드 호출(RMI)

Spring은 원격 서비스 호출을 위한 프록시를 제공하여 분산 시스템 간의 통신을 단순화합니다.

- 클라이언트는 로컬 객체를 호출하는 것처럼 원격 서비스를 사용할 수 있습니다.
- 프록시가 네트워크 통신, 직렬화/역직렬화 등의 복잡한 작업을 처리합니다.

## Spring의 프록시 구현 방식

Spring은 두 가지 방식으로 프록시를 구현합니다:

### 1. JDK 동적 프록시(Dynamic Proxy)

인터페이스를 구현한 클래스에 대해 프록시를 생성하는 방식입니다.

- `java.lang.reflect.Proxy` 클래스와 `InvocationHandler` 인터페이스를 사용합니다.
- **인터페이스가 반드시 필요**하며, 인터페이스의 메서드만 프록시할 수 있습니다.

```java
// JDK 동적 프록시 예시
public interface UserService {
    void createUser(User user);
}

public class UserServiceImpl implements UserService {
    @Override
    public void createUser(User user) {
        // 실제 구현
    }
}

// 프록시 생성
UserService proxy = (UserService) Proxy.newProxyInstance(
    UserService.class.getClassLoader(),
    new Class[] { UserService.class },
    new InvocationHandler() {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("메서드 호출 전 처리");
            Object result = method.invoke(new UserServiceImpl(), args);
            System.out.println("메서드 호출 후 처리");
            return result;
        }
    }
);
```

### 2. CGLIB 프록시

인터페이스가 없는 클래스에 대해서도 프록시를 생성할 수 있는 방식입니다.

- 클래스의 바이트코드를 조작하여 서브클래스를 동적으로 생성합니다.
- Spring Boot 2.0 이상에서는 기본적으로 CGLIB을 사용합니다.
- **final 클래스나 메서드는 오버라이딩할 수 없으므로 프록시를 생성할 수 없습니다.**

```java
// CGLIB 프록시 예시 (인터페이스 없이 직접 클래스 사용)
public class UserService {
    public void createUser(User user) {
        // 실제 구현
    }
}

// Spring에서 CGLIB 프록시 생성 설정
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Configuration
public class AppConfig {
    // 설정 내용
}
```

## 프록시 사용의 장점과 한계

### 장점

1. **관심사의 분리**: 핵심 비즈니스 로직과 부가 기능을 분리하여 코드의 가독성과 유지보수성을 높입니다.
2. **코드 재사용**: 공통 기능을 중앙화하여 코드 중복을 줄입니다.
3. **투명한 기능 추가**: 기존 코드를 변경하지 않고도 새로운 기능을 추가할 수 있습니다.
4. **선언적 프로그래밍**: 어노테이션 기반의 선언적 방식으로 복잡한 기능을 쉽게 적용할 수 있습니다.

### 한계

1. **프록시 내부 호출 문제**: 같은 클래스 내에서 메서드를 호출할 때는 프록시가 적용되지 않습니다.
2. **final 클래스/메서드 제약**: CGLIB 프록시는 final 클래스나 메서드에 적용할 수 없습니다.
3. **성능 오버헤드**: 프록시 생성과 메서드 호출 가로채기에 약간의 성능 오버헤드가 발생할 수 있습니다.
4. **디버깅 복잡성**: 프록시로 인해 스택 트레이스가 복잡해져 디버깅이 어려울 수 있습니다.

## 결론

프록시는 Spring 프레임워크의 핵심 요소로, AOP, 트랜잭션 관리, 지연 로딩 등 다양한 기능을 구현하는 데 필수적입니다. Spring은 프록시 패턴을 통해 개발자가 비즈니스 로직에만 집중할 수 있도록 하고, 공통 관심사를 효과적으로 모듈화하여 코드 품질과 유지보수성을 크게 향상시킵니다.
