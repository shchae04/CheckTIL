# Java Default Methods

## 1. 개요

Java 8에서 도입된 Default 메서드는 인터페이스에 구현체를 제공할 수 있게 해주는 기능입니다. 이 기능은 기존 인터페이스에 새로운 메서드를 추가할 때 하위 호환성을 유지하면서 인터페이스를 확장할 수 있게 해줍니다.

## 2. Default 메서드의 필요성

### 2.1 도입 배경

Java 8 이전에는 인터페이스에 새로운 메서드를 추가하면, 해당 인터페이스를 구현한 모든 클래스에서 그 메서드를 구현해야 했습니다. 이는 다음과 같은 문제를 야기했습니다:

1. **하위 호환성 문제**: 기존 인터페이스에 새 메서드를 추가하면 그 인터페이스를 구현한 모든 클래스가 깨질 수 있음
2. **라이브러리 진화의 어려움**: 널리 사용되는 인터페이스를 변경하기 어려움

Default 메서드는 이러한 문제를 해결하기 위해 도입되었습니다.

### 2.2 주요 사용 사례

- 기존 인터페이스에 새로운 기능 추가
- 공통 구현을 제공하여 코드 중복 감소
- 함수형 프로그래밍 지원 (특히 Collection 프레임워크의 확장)

## 3. Default 메서드 문법

Default 메서드는 `default` 키워드를 사용하여 선언하며, 메서드 본문을 제공합니다:

```java
public interface MyInterface {
    // 일반 인터페이스 메서드 (구현 필요)
    void regularMethod();
    
    // Default 메서드 (구현 제공)
    default void defaultMethod() {
        System.out.println("Default 메서드의 기본 구현");
    }
}
```

## 4. Default 메서드 특징

### 4.1 주요 특징

1. **구현 제공**: 인터페이스에서 메서드 구현을 제공할 수 있음
2. **선택적 오버라이드**: 구현 클래스에서 필요에 따라 오버라이드 가능
3. **다중 상속 문제 해결**: 충돌 해결 메커니즘 제공

### 4.2 다중 상속 충돌 해결

Java에서 클래스는 여러 인터페이스를 구현할 수 있으므로, 동일한 시그니처를 가진 default 메서드가 여러 인터페이스에 존재할 수 있습니다. 이 경우 충돌이 발생하며, 다음 규칙으로 해결합니다:

1. **클래스의 메서드가 우선**: 클래스나 슈퍼클래스에서 구현한 메서드가 default 메서드보다 우선
2. **더 구체적인 인터페이스 우선**: 하위 인터페이스의 default 메서드가 상위 인터페이스의 default 메서드보다 우선
3. **명시적 지정 필요**: 위 규칙으로 해결되지 않으면 구현 클래스에서 명시적으로 어떤 메서드를 사용할지 지정해야 함

```java
public class MyClass implements Interface1, Interface2 {
    // 충돌 해결을 위한 명시적 오버라이드
    @Override
    public void conflictingMethod() {
        Interface1.super.conflictingMethod(); // Interface1의 default 메서드 호출
    }
}
```

## 5. 실제 예제

### 5.1 기본 사용 예제

```java
// 인터페이스 정의
interface Vehicle {
    void start();
    
    default void stop() {
        System.out.println("기본 정지 메커니즘");
    }
    
    default void honk() {
        System.out.println("빵빵!");
    }
}

// 구현 클래스
class Car implements Vehicle {
    @Override
    public void start() {
        System.out.println("자동차 시동 걸기");
    }
    
    // stop()은 기본 구현 사용
    
    // honk() 오버라이드
    @Override
    public void honk() {
        System.out.println("자동차 경적: 빵빵!");
    }
}

// 사용 예
public class DefaultMethodExample {
    public static void main(String[] args) {
        Vehicle car = new Car();
        car.start(); // "자동차 시동 걸기"
        car.stop();  // "기본 정지 메커니즘"
        car.honk();  // "자동차 경적: 빵빵!"
    }
}
```

### 5.2 Java 표준 라이브러리 예제

Java 8의 Collection 인터페이스에 추가된 default 메서드 예제:

```java
import java.util.ArrayList;
import java.util.List;

public class CollectionDefaultMethodExample {
    public static void main(String[] args) {
        List<String> fruits = new ArrayList<>();
        fruits.add("사과");
        fruits.add("바나나");
        fruits.add("오렌지");
        
        // forEach는 Iterable 인터페이스의 default 메서드
        fruits.forEach(fruit -> System.out.println(fruit));
        
        // removeIf는 Collection 인터페이스의 default 메서드
        fruits.removeIf(fruit -> fruit.startsWith("바"));
        System.out.println("바나나 제거 후: " + fruits);
        
        // sort는 List 인터페이스의 default 메서드
        fruits.sort(String::compareTo);
        System.out.println("정렬 후: " + fruits);
    }
}
```

### 5.3 다중 상속 충돌 예제

```java
interface Printer {
    default void print() {
        System.out.println("Printer 인터페이스의 기본 출력");
    }
}

interface Scanner {
    default void print() {
        System.out.println("Scanner 인터페이스의 기본 출력");
    }
}

// 충돌 해결 방법 1: 명시적 오버라이드
class MultiFunctionDevice implements Printer, Scanner {
    @Override
    public void print() {
        Printer.super.print(); // Printer의 default 메서드 사용
        // 또는 Scanner.super.print();
    }
}

// 충돌 해결 방법 2: 완전히 새로운 구현
class CopyMachine implements Printer, Scanner {
    @Override
    public void print() {
        System.out.println("CopyMachine의 자체 출력 구현");
    }
}

public class MultiInheritanceExample {
    public static void main(String[] args) {
        MultiFunctionDevice mfd = new MultiFunctionDevice();
        mfd.print(); // "Printer 인터페이스의 기본 출력"
        
        CopyMachine cm = new CopyMachine();
        cm.print(); // "CopyMachine의 자체 출력 구현"
    }
}
```

## 6. Default 메서드의 장단점

### 6.1 장점

- **하위 호환성 유지**: 기존 코드를 깨지 않고 인터페이스 확장 가능
- **코드 재사용**: 공통 기능을 default 메서드로 제공하여 중복 감소
- **API 진화**: 기존 인터페이스에 새로운 기능 추가 용이
- **선택적 구현**: 필요한 메서드만 오버라이드 가능

### 6.2 단점

- **다중 상속 복잡성**: 여러 인터페이스에서 동일한 default 메서드가 있을 때 충돌 발생
- **상태 관리 제한**: 인터페이스는 상태(필드)를 가질 수 없어 default 메서드의 기능이 제한됨
- **설계 혼란**: 인터페이스와 추상 클래스의 경계가 모호해질 수 있음

## 7. 모범 사례

1. **최소한의 구현만 제공**: default 메서드는 기본적인 구현만 제공하고, 복잡한 로직은 지양
2. **문서화**: default 메서드의 목적과 동작을 명확히 문서화
3. **상태 의존성 피하기**: default 메서드는 상태에 의존하지 않도록 설계
4. **충돌 고려**: 인터페이스 설계 시 잠재적인 default 메서드 충돌 고려

## 8. 결론

Java Default 메서드는 인터페이스 진화를 가능하게 하는 강력한 기능입니다. 특히 라이브러리 개발자에게 기존 코드의 호환성을 유지하면서 새로운 기능을 추가할 수 있는 유연성을 제공합니다. 그러나 다중 상속 문제와 설계 복잡성을 고려하여 신중하게 사용해야 합니다.