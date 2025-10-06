# 자바에서 안전한 싱글톤 클래스를 구현하는 방법

## 1. 한 줄 정의
- 싱글톤 패턴은 클래스의 인스턴스가 JVM 내에서 단 하나만 생성되도록 보장하는 디자인 패턴이다. 멀티스레드 환경에서 동시성 문제를 해결하면서도 메모리 효율성과 성능을 모두 확보하는 것이 핵심이다.

---

## 2. 싱글톤 구현 방법 6가지

### 2-1. 1단계: Eager Initialization (즉시 초기화)
- **개념**: 클래스 로딩 시점에 인스턴스를 생성하는 가장 단순한 방법
- **장점**: 구현이 간단하고 스레드 안전성 보장
- **단점**: 사용하지 않아도 인스턴스가 생성되어 메모리 낭비 가능

```java
public class EagerSingleton {
    // 클래스 로딩 시점에 인스턴스 생성
    private static final EagerSingleton INSTANCE = new EagerSingleton();

    private EagerSingleton() {
        // private 생성자로 외부 인스턴스 생성 방지
    }

    public static EagerSingleton getInstance() {
        return INSTANCE;
    }
}
```

### 2-2. 2단계: Lazy Initialization (지연 초기화)
- **개념**: 실제로 필요할 때 인스턴스를 생성하는 방법
- **장점**: 메모리 효율적
- **단점**: 멀티스레드 환경에서 안전하지 않음

```java
public class LazySingleton {
    private static LazySingleton instance;

    private LazySingleton() {}

    // 멀티스레드 환경에서 여러 인스턴스가 생성될 수 있음
    public static LazySingleton getInstance() {
        if (instance == null) {
            instance = new LazySingleton();
        }
        return instance;
    }
}
```

### 2-3. 3단계: Thread-Safe Singleton (동기화 메서드)
- **개념**: synchronized 키워드로 메서드 전체를 동기화
- **장점**: 스레드 안전성 보장
- **단점**: 성능 저하 (매번 동기화 오버헤드 발생)

```java
public class ThreadSafeSingleton {
    private static ThreadSafeSingleton instance;

    private ThreadSafeSingleton() {}

    // 메서드 전체를 동기화하여 스레드 안전성 확보
    public static synchronized ThreadSafeSingleton getInstance() {
        if (instance == null) {
            instance = new ThreadSafeSingleton();
        }
        return instance;
    }
}
```

### 2-4. 4단계: Double-Checked Locking (이중 검사 잠금)
- **개념**: 인스턴스가 null일 때만 동기화 블록 진입
- **장점**: 성능과 스레드 안전성 모두 확보
- **핵심 포인트**: volatile 키워드 필수 (메모리 가시성 보장)

```java
public class DoubleCheckedLockingSingleton {
    // volatile: 메모리 가시성 보장 및 명령어 재배치 방지
    private static volatile DoubleCheckedLockingSingleton instance;

    private DoubleCheckedLockingSingleton() {}

    public static DoubleCheckedLockingSingleton getInstance() {
        // 첫 번째 체크: 동기화 오버헤드 최소화
        if (instance == null) {
            synchronized (DoubleCheckedLockingSingleton.class) {
                // 두 번째 체크: 스레드 안전성 보장
                if (instance == null) {
                    instance = new DoubleCheckedLockingSingleton();
                }
            }
        }
        return instance;
    }
}
```

### 2-5. 5단계: Bill Pugh Singleton (정적 내부 클래스)
- **개념**: 정적 내부 클래스를 이용한 지연 초기화 + 스레드 안전성
- **장점**:
  - JVM의 클래스 로더 메커니즘을 활용하여 스레드 안전성 자동 보장
  - 지연 로딩 지원 (getInstance() 호출 시 클래스 로딩)
  - synchronized 불필요로 성능 우수
- **백엔드 관점**: 가장 권장되는 방식

```java
public class BillPughSingleton {

    private BillPughSingleton() {}

    // 정적 내부 클래스: getInstance() 호출 시점에 로딩됨
    private static class SingletonHelper {
        private static final BillPughSingleton INSTANCE = new BillPughSingleton();
    }

    public static BillPughSingleton getInstance() {
        return SingletonHelper.INSTANCE;
    }
}
```

### 2-6. 6단계: Enum Singleton (열거형 싱글톤)
- **개념**: Enum을 이용한 싱글톤 구현
- **장점**:
  - 직렬화/역직렬화 시 싱글톤 보장
  - 리플렉션 공격 방어
  - 가장 안전하고 간결한 구현
- **Joshua Bloch 권장**: Effective Java에서 가장 바람직한 방법으로 소개

```java
public enum EnumSingleton {
    INSTANCE;

    // 싱글톤이 가져야 할 메서드들
    public void doSomething() {
        System.out.println("Singleton method called");
    }
}

// 사용 예시
EnumSingleton.INSTANCE.doSomething();
```

---

## 3. 백엔드 개발자 관점에서의 주의사항

### 3-1. 직렬화/역직렬화 문제
- **문제점**: 역직렬화 시 새로운 인스턴스 생성 가능
- **해결방법**: readResolve() 메서드 구현

```java
public class SerializableSingleton implements Serializable {
    private static final SerializableSingleton INSTANCE = new SerializableSingleton();

    private SerializableSingleton() {}

    public static SerializableSingleton getInstance() {
        return INSTANCE;
    }

    // 역직렬화 시 기존 인스턴스 반환
    protected Object readResolve() {
        return INSTANCE;
    }
}
```

### 3-2. 리플렉션 공격 방어
- **문제점**: 리플렉션으로 private 생성자 접근 가능
- **해결방법**: 생성자에서 인스턴스 존재 여부 확인

```java
public class ReflectionProofSingleton {
    private static final ReflectionProofSingleton INSTANCE = new ReflectionProofSingleton();

    private ReflectionProofSingleton() {
        // 리플렉션 공격 방어
        if (INSTANCE != null) {
            throw new IllegalStateException("이미 인스턴스가 존재합니다.");
        }
    }

    public static ReflectionProofSingleton getInstance() {
        return INSTANCE;
    }
}
```

### 3-3. 클래스 로더 문제
- **문제점**: 서로 다른 클래스 로더가 각각 인스턴스 생성 가능
- **해결방법**: 싱글 클래스 로더 환경 보장 또는 컨테이너 레벨 관리

---

## 4. 실제 서비스 운영 시 고려사항

### 4-1. Spring Framework 활용
- **Spring Bean Scope**: 기본적으로 singleton 스코프 제공
- **권장사항**: 직접 싱글톤 구현보다 Spring의 DI 컨테이너 활용

```java
@Component
public class SpringManagedSingleton {
    // Spring이 싱글톤 라이프사이클 관리
    public void businessLogic() {
        // 비즈니스 로직
    }
}
```

### 4-2. 멀티 인스턴스 환경
- **분산 시스템**: 각 JVM마다 별도 인스턴스 생성
- **해결방법**: Redis, Hazelcast 등 외부 캐시 활용

### 4-3. 테스트 용이성
- **문제점**: 싱글톤은 테스트 격리 어려움
- **해결방법**: 인터페이스 기반 설계 + 의존성 주입

---

## 5. 예상 면접 질문

### 5-1. 기술적 질문
1. Double-Checked Locking에서 volatile 키워드가 필요한 이유는?
   - 메모리 가시성 보장 및 명령어 재배치(instruction reordering) 방지

2. Enum 싱글톤이 리플렉션 공격을 방어할 수 있는 이유는?
   - JVM 레벨에서 Enum의 생성자 호출을 제한하기 때문

3. 정적 내부 클래스 방식의 지연 로딩 원리는?
   - 클래스 로더가 getInstance() 호출 시점에 내부 클래스를 로드

### 5-2. 시스템 설계 질문
1. 분산 환경에서 진정한 싱글톤을 구현하려면 어떻게 해야 하나요?
2. 싱글톤 패턴의 대안으로 어떤 패턴들을 고려할 수 있나요?
3. Spring Bean의 싱글톤 스코프와 GoF 싱글톤 패턴의 차이는?

### 5-3. 실무 경험 질문
1. 싱글톤 패턴을 실제 프로젝트에서 사용한 경험이 있나요?
2. 싱글톤 패턴 사용 시 발생했던 문제와 해결 방법은?
3. 싱글톤 패턴 대신 다른 설계를 선택한 경험이 있나요?

---

## 6. 핵심 요약

### 6-1. 권장 구현 방법 우선순위
1. **Enum Singleton**: 가장 안전하고 간결 (Joshua Bloch 권장)
2. **Bill Pugh Singleton**: 성능과 안전성 모두 확보
3. **Spring Bean**: 프레임워크 환경에서는 DI 활용

### 6-2. 주요 특징
- **스레드 안전성**: 멀티스레드 환경에서 단일 인스턴스 보장
- **지연 로딩**: 필요한 시점에 인스턴스 생성으로 메모리 효율성 확보
- **직렬화 안전성**: 역직렬화 시에도 싱글톤 유지

### 6-3. 실무 적용 포인트
- 직접 구현보다는 Spring 등 프레임워크의 싱글톤 관리 활용
- 전역 상태 관리가 필요한 경우에만 제한적으로 사용
- 테스트 용이성을 위해 인터페이스 기반 설계 고려
- 분산 환경에서는 JVM 싱글톤의 한계 인식 및 대안 마련

### 6-4. 안티패턴 주의
- **과도한 사용**: 모든 클래스를 싱글톤으로 만들지 말 것
- **숨은 의존성**: 전역 상태로 인한 코드 결합도 증가 주의
- **테스트 어려움**: 싱글톤 남용 시 단위 테스트 격리 곤란