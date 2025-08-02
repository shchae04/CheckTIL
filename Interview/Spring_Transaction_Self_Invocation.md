# Spring @Transactional과 Self-Invocation 문제

## 1. 개요
Spring의 @Transactional 애노테이션은 AOP(Aspect-Oriented Programming)를 기반으로 동작합니다. 이 문서에서는 @Transactional 애노테이션이 private 메서드에서 동작하는지, 그리고 같은 클래스 내에서 메서드 호출 시(Self-Invocation) 발생하는 문제와 해결 방법에 대해 설명합니다.

## 2. Spring AOP와 @Transactional의 동작 원리

### 1) Spring AOP 프록시 방식
Spring AOP는 다음 두 가지 프록시 방식을 사용합니다:

- **JDK Dynamic Proxy**: 인터페이스를 구현한 클래스에 적용되며, public 메서드만 AOP 적용 가능
- **CGLIB Proxy**: 인터페이스를 구현하지 않은 클래스에 적용되며, private을 제외한 public, protected, package-private 메서드에 AOP 적용 가능

Spring은 빈 생성 시 해당 빈에 AOP 애노테이션이 있는지 검사하고, 있다면 프록시 객체를 생성하여 빈을 대체합니다.

### 2) 프록시 기반 AOP의 한계
프록시 기반 AOP는 외부에서 프록시 객체를 통해 메서드가 호출될 때만 AOP 어드바이스(트랜잭션 관리 등)를 적용합니다. 이로 인해 다음과 같은 한계가 있습니다:

- private 메서드는 프록시에서 호출할 수 없으므로 @Transactional이 동작하지 않음
- 같은 클래스 내에서 메서드를 호출할 경우(Self-Invocation) 프록시를 거치지 않고 직접 호출되므로 @Transactional이 동작하지 않음

## 3. Self-Invocation 문제 예시

다음은 Self-Invocation 문제를 보여주는 예제 코드입니다:

```java
@Slf4j  
@RequiredArgsConstructor  
@Service  
public class SelfInvocation {  
  
    private final MemberRepository memberRepository;  
  
    public void outerSaveWithPublic(Member member) {  
        saveWithPublic(member);  
    }  
  
    @Transactional  
    public void saveWithPublic(Member member) {  
        log.info("call saveWithPublic");  
        memberRepository.save(member);  
        throw new RuntimeException("rollback test");  
    }  
  
    public void outerSaveWithPrivate(Member member) {  
        saveWithPrivate(member);  
    }  
  
    @Transactional  
    private void saveWithPrivate(Member member) {  
        log.info("call saveWithPrivate");  
        memberRepository.save(member);  
        throw new RuntimeException("rollback test");  
    }  
}
```

```java
public interface MemberRepository extends JpaRepository<Member, Long> {  
}
```

## 4. 테스트를 통한 문제 확인

다음 테스트 코드는 Self-Invocation 문제와 private 메서드에서의 @Transactional 동작을 검증합니다:

```java
@SpringBootTest  
class SelfInvocationTest {  
  
    private static final Logger log = LoggerFactory.getLogger(SelfInvocationTest.class);  
  
    @Autowired  
    private SelfInvocation selfInvocation;  
  
    @Autowired  
    private MemberRepository memberRepository;  
  
    @AfterEach  
    void tearDown() {  
        memberRepository.deleteAllInBatch();  
    }  
  
    @Test  
    void aopProxyTest() {  
        // @Transactional 애노테이션을 가지고 있으므로, 빈이 Proxy 객체로 대체되어 주입된다.  
        assertThat(AopUtils.isAopProxy(selfInvocation)).isTrue();  
        // interface를 구현하지 않은 클래스이므로 CGLIB Proxy가 생성된다.  
        assertThat(AopUtils.isCglibProxy(selfInvocation)).isTrue();  
    }  
  
    @Test  
    void outerSaveWithPublic() {  
        Member member = new Member("test");  
  
        try {  
            selfInvocation.outerSaveWithPublic(member);  
        } catch (RuntimeException e) {  
            log.info("catch exception");  
        }  
  
        List<Member> members = memberRepository.findAll();  
        // self invocation 문제로 인해 트랜잭션이 정상 동작하지 않음.  
        // 예외 발생으로 인한 롤백이 동작하지 않고 남아있음.
        assertThat(members).hasSize(1);  
    }  
  
    @Test  
    void outerSaveWithPrivate() {  
        try {  
            selfInvocation.outerSaveWithPrivate(new Member("test"));  
        } catch (RuntimeException e) {  
            log.info("catch exception");  
        }  
  
        List<Member> members = memberRepository.findAll();  
  
        // self invocation 문제로 인해 트랜잭션이 정상 동작하지 않음.  
        // 예외 발생으로 인한 롤백이 동작하지 않고 남아있음.
        assertThat(members).hasSize(1);  
    }  
  
    @Test  
    void saveWithPublic() {  
        Member member = new Member("test");  
  
        try {  
            selfInvocation.saveWithPublic(member);  
        } catch (RuntimeException e) {  
            log.info("catch exception");  
        }  
  
        List<Member> members = memberRepository.findAll();  
  
        // 외부에서 프록시 객체를 통해 메서드가 호출되었기 때문에 트랜잭션 정상 동작, 롤백 성공.  
        assertThat(members).hasSize(0);  
    }  
}
```

## 5. 문제 해결 방법

### 1) 자기 자신을 프록시로 주입 받는 방법

```java
@Slf4j  
@RequiredArgsConstructor  
@Service  
public class SelfInvocation {  
  
    private final MemberRepository memberRepository;  
    private final SelfInvocation selfInvocation;  
  
    public void outerSaveWithPublic(Member member) {  
        selfInvocation.saveWithPublic(member);  
    }  
  
    @Transactional  
    public void saveWithPublic(Member member) {  
        log.info("call saveWithPublic");  
        memberRepository.save(member);  
        throw new RuntimeException("rollback test");  
    }
    // ...
}
```

이 방법은 순환 의존성 문제를 일으킬 수 있어 권장되지 않습니다.

### 2) 별도의 클래스로 분리하는 방법

트랜잭션 전파 속성이 다른 두 메서드를 각각 다른 클래스로 분리하여 호출합니다:

```java
// OuterTransactionService
@Slf4j  
@RequiredArgsConstructor  
@Service  
public class OuterTransactionService {  
  
    private final InnerTransactionService innerTransactionService;  
  
    @Transactional  
    public void outer() {  
        log.info("call outer");  
        logCurrentTransactionName();  
        logActualTransactionActive();  
        innerTransactionService.inner();  
    }  
  
    private void logActualTransactionActive() {  
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();  
        log.info("actualTransactionActive = {}", actualTransactionActive);  
    }  
  
    private void logCurrentTransactionName() {  
        String currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();  
        log.info("currentTransactionName = {}", currentTransactionName);  
    }  
}

// InnerTransactionService
@Slf4j  
@RequiredArgsConstructor  
@Service  
public class InnerTransactionService {  
  
    @Transactional(propagation = Propagation.REQUIRES_NEW)  
    public void inner() {  
        log.info("call inner");  
        logCurrentTransactionName();  
        logActualTransactionActive();  
    }  
  
    private void logActualTransactionActive() {  
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();  
        log.info("actualTransactionActive = {}", actualTransactionActive);  
    }  
  
    private void logCurrentTransactionName() {  
        String currentTransactionName = TransactionSynchronizationManager.getCurrentTransactionName();  
        log.info("currentTransactionName = {}", currentTransactionName);  
    }  
}
```

로그 결과:
```
call outer  
currentTransactionName = server.transaction.OuterTransactionService.outer  
actualTransactionActive = true  
call inner  
currentTransactionName = server.transaction.InnerTransactionService.inner  
actualTransactionActive = true
```

이처럼 각각 프록시를 생성할 수 있게 두 클래스로 분리하면 AOP 어드바이스가 적용되어 의도한 대로 독립적인 트랜잭션을 시작할 수 있습니다.

### 3) AspectJ를 이용하는 방법

AspectJ를 사용하면 동일 클래스 내에서의 메서드 호출에도 AOP 어드바이스를 적용할 수 있습니다. 이를 위해 다음과 같이 설정합니다:

```java
@EnableTransactionManagement(mode = AdviceMode.ASPECTJ)
@Configuration
public class TransactionConfig {
    // ...
}
```

그리고 AspectJ 의존성을 추가하고 AspectJ 위빙을 설정해야 합니다.

## 6. 결론

1. **private 메서드에 @Transactional 선언**: private 메서드에 @Transactional을 선언해도 프록시 방식의 AOP에서는 동작하지 않습니다.

2. **Self-Invocation 문제**: 같은 클래스 내에서 @Transactional 메서드를 호출하면 프록시를 거치지 않아 트랜잭션이 적용되지 않습니다.

3. **해결 방법**:
   - 별도의 클래스로 분리하여 프록시를 통한 호출이 이루어지도록 함
   - AspectJ를 사용하여 컴파일 시점 또는 로드 시점에 위빙하는 방식으로 변경
   - 자기 자신을 주입받는 방법(권장하지 않음)

Spring AOP의 프록시 기반 특성을 이해하고, 트랜잭션 관리 시 이러한 제약사항을 고려하여 설계하는 것이 중요합니다.