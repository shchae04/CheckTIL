# Observer Pattern이란?

## 1. 한 줄 정의
Observer Pattern은 어떤 객체의 상태 변화를 감시하다가 상태가 변할 때 자동으로 다른 객체들에게 알려주는 디자인 패턴이며, 일대다(One-to-Many) 의존성을 정의하고 한 객체의 상태가 변하면 의존하는 모든 객체들이 자동으로 알림을 받는 구조이다.

---

## 2. Observer Pattern의 구성 요소 및 특성

### 2-1. 주요 역할(Roles)
- **Subject(주체)**: 상태를 가지고 있으며 상태 변화를 통지하는 객체
- **Observer(관찰자)**: Subject의 상태 변화를 감시하고 반응하는 객체
- **ConcreteSubject**: Subject를 구현한 실제 클래스
- **ConcreteObserver**: Observer를 구현한 실제 클래스

```python
# Observer Pattern의 기본 구조
from abc import ABC, abstractmethod

class Subject:
    def __init__(self):
        self._observers = []
        self._state = None

    def attach(self, observer):
        """Observer 등록"""
        self._observers.append(observer)

    def detach(self, observer):
        """Observer 제거"""
        self._observers.remove(observer)

    def notify(self):
        """모든 Observer에게 알림"""
        for observer in self._observers:
            observer.update(self._state)

    @property
    def state(self):
        return self._state

    @state.setter
    def state(self, value):
        self._state = value
        self.notify()  # 상태 변경 시 자동으로 모든 observer에게 알림

class Observer(ABC):
    @abstractmethod
    def update(self, state):
        pass

class ConcreteObserver(Observer):
    def update(self, state):
        print(f"상태 변경됨: {state}")
```

### 2-2. 느슨한 결합(Loose Coupling)
- **Subject**: Observer의 구체적인 구현을 알 필요 없음
- **Observer**: Subject의 구체적인 구현을 알 필요 없음
- 인터페이스를 통한 추상적 통신

```python
# Subject는 Observer 인터페이스만 의존
# 따라서 새로운 Observer 추가해도 Subject 코드 변경 없음
class EmailObserver(Observer):
    def update(self, state):
        print(f"이메일 발송: {state}")

class LogObserver(Observer):
    def update(self, state):
        print(f"로그 기록: {state}")

subject = Subject()
subject.attach(EmailObserver())
subject.attach(LogObserver())
subject.state = "새로운 데이터"  # 두 Observer 모두 update 호출됨
```

### 2-3. 자동 통지(Automatic Notification)
- 상태 변화 시 모든 등록된 Observer에게 자동으로 알림
- Push/Pull 방식 지원
  - **Push**: Subject가 상태를 직접 전달
  - **Pull**: Observer가 필요한 데이터만 가져감

```python
# Push 방식 (위의 예시)
subject.state = "데이터"  # state를 notify에 함께 전달

# Pull 방식
class PullObserver(Observer):
    def __init__(self, subject):
        self.subject = subject

    def update(self):
        state = self.subject.state  # Observer가 직접 가져옴
        print(f"Pull 방식: {state}")
```

### 2-4. 동적 등록/제거
- 런타임에 Observer 동적으로 추가/제거 가능
- 프로그램 실행 중 구독자 관리 가능

```python
observer1 = EmailObserver()
observer2 = LogObserver()

subject = Subject()
subject.attach(observer1)  # observer1 추가
subject.state = "첫 번째"   # observer1만 알림

subject.attach(observer2)  # observer2 추가
subject.state = "두 번째"   # observer1, observer2 모두 알림

subject.detach(observer1)  # observer1 제거
subject.state = "세 번째"   # observer2만 알림
```

---

## 3. 언어별 구현 특성

### 3-1. Python
- 내장 모듈 미흡, 직접 구현 필요
- 클래스 기반 또는 함수형 구현 가능

```python
class Subject:
    def __init__(self):
        self._observers = []

    def attach(self, observer):
        self._observers.append(observer)

    def notify(self, *args):
        for observer in self._observers:
            observer(*args)

# 함수형 사용법
subject = Subject()
subject.attach(lambda x: print(f"처리: {x}"))
subject.notify("데이터")
```

### 3-2. Java
- 내장 클래스 지원: `java.util.Observer` / `java.util.Observable`
- 최신: `PropertyChangeListener` / `PropertyChangeSupport`

```java
// Java 전통 방식
class Subject extends Observable {
    private String state;

    public void setState(String state) {
        this.state = state;
        setChanged();
        notifyObservers(state);
    }
}

class MyObserver implements Observer {
    public void update(Observable o, Object arg) {
        System.out.println("알림: " + arg);
    }
}

// Java 최신 방식 (PropertyChangeListener)
class Subject {
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private String state;

    public void setState(String state) {
        String old = this.state;
        this.state = state;
        pcs.firePropertyChange("state", old, state);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
}
```

### 3-3. JavaScript/TypeScript
- 이벤트 리스너 패턴 기본 제공
- 클래스 문법 또는 이벤트 에미터 사용

```typescript
// TypeScript 클래스 기반
interface Observer {
    update(state: any): void;
}

class Subject {
    private observers: Observer[] = [];
    private state: any;

    attach(observer: Observer): void {
        this.observers.push(observer);
    }

    notify(): void {
        this.observers.forEach(obs => obs.update(this.state));
    }
}

// JavaScript 이벤트 기반
const subject = new EventTarget();
subject.addEventListener('stateChange', (event) => {
    console.log('상태 변경:', event.detail);
});
subject.dispatchEvent(new CustomEvent('stateChange', { detail: 'new state' }));
```

---

## 4. 사용 사례

### 4-1. Observer Pattern을 사용하는 경우
- **UI 상태 관리**: 컴포넌트의 상태 변화를 다른 UI에 반영
- **이벤트 시스템**: 마우스 클릭, 키 입력 등의 이벤트 처리
- **데이터 변경 알림**: 모델 변경 시 뷰 자동 업데이트
- **실시간 알림**: 사용자에게 즉시 변경 사항 전달

```python
# MVC 패턴에서의 활용
class Model:
    def __init__(self):
        self._observers = []
        self._data = None

    def attach(self, observer):
        self._observers.append(observer)

    def notify(self):
        for observer in self._observers:
            observer.update(self._data)

    @property
    def data(self):
        return self._data

    @data.setter
    def data(self, value):
        self._data = value
        self.notify()

class View:
    def update(self, data):
        print(f"화면 업데이트: {data}")

# 사용
model = Model()
view = View()
model.attach(view)
model.data = "새로운 데이터"  # View가 자동으로 업데이트됨
```

### 4-2. 실무 예시
- **React의 useState/useEffect**: 상태 변화 감지 및 부작용 실행
- **Spring의 ApplicationEvent**: 애플리케이션 이벤트 발행/구독
- **메시지 큐**: 토픽 기반 발행/구독 시스템

```java
// Spring Framework에서의 Observer Pattern
@Component
public class OrderService {
    @Autowired
    private ApplicationEventPublisher publisher;

    public void placeOrder(Order order) {
        // 주문 처리
        publisher.publishEvent(new OrderCreatedEvent(order));  // Observer들에게 알림
    }
}

@Component
public class EmailNotificationService implements ApplicationListener<OrderCreatedEvent> {
    @Override
    public void onApplicationEvent(OrderCreatedEvent event) {
        // 주문 완료 이메일 발송
        sendEmail(event.getOrder());
    }
}
```

---

## 5. Kafka와 Observer Pattern의 관계

### 5-1. 유사점
- 발행자(Publisher)가 메시지를 보내면 구독자(Subscriber)가 수신
- 발행자와 구독자가 느슨하게 결합됨
- 일대다 통신 구조

### 5-2. 차이점
| 특성 | Observer Pattern | Kafka |
|------|------------------|-------|
| **통신 범위** | 같은 프로세스 내 | 분산 시스템 (네트워크) |
| **메시지 저장** | 저장 안 함 | 파티션에 지속 저장 |
| **오프셋 관리** | 미지원 | Offset으로 메시지 추적 |
| **재전송 가능** | 불가능 | 가능 (Offset으로 재수신) |
| **확장성** | 단일 프로세스 | 클러스터 확장 가능 |
| **목적** | 객체 간 통신 | 이벤트 스트리밍 플랫폼 |

```
[패턴 분류]
Observer Pattern → 인메모리(In-Memory) 발행/구독
Kafka → Pub/Sub 메시징 시스템
Kafka는 Observer Pattern에서 영감을 받았지만, 분산 환경을 위한 확장된 개념
```

### 5-3. Kafka 구조
```
┌─────────────┐      ┌─────────────────┐      ┌──────────────┐
│  Producer   │──→   │  Kafka Broker   │  ←──  │  Consumer    │
│  (Publisher)│      │  (Topic/Partition)│     │  (Subscriber)│
└─────────────┘      └─────────────────┘      └──────────────┘
                              ↓
                     [메시지 지속 저장]
                     [Offset 기반 추적]
```

---

## 6. 백엔드 개발자 관점의 중요성

### 6-1. 아키텍처 설계
- **이벤트 기반 아키텍처**: Observer Pattern으로 느슨한 결합 달성
- **마이크로서비스**: 서비스 간 통신을 이벤트로 구현
- **CQRS 패턴**: 명령(Command)과 조회(Query) 분리

### 6-2. 유지보수성
- 새로운 기능 추가 시 Subject 코드 수정 불필요
- 각 Observer는 독립적으로 개발/테스트 가능
- 코드 변경의 파급효과 최소화

### 6-3. 성능 고려사항
- Observer가 많을수록 성능 저하 가능성
- 무거운 작업은 비동기로 처리
- 메모리 누수 방지를 위해 Observer 제거 필수

```python
# 메모리 누수 방지
class SafeSubject:
    def __init__(self):
        self._observers = set()  # 중복 방지

    def attach(self, observer):
        self._observers.add(observer)

    def detach(self, observer):
        self._observers.discard(observer)  # 없어도 오류 없음

# 비동기 처리
class AsyncSubject:
    def notify(self):
        for observer in self._observers:
            # 스레드 풀을 사용한 비동기 처리
            executor.submit(observer.update, self._state)
```

---

## 7. 핵심 요약

| 특성 | 설명 |
|------|------|
| **정의** | 일대다 의존성을 정의하는 디자인 패턴 |
| **구성** | Subject, Observer, ConcreteSubject, ConcreteObserver |
| **결합도** | 느슨한 결합 (Loose Coupling) |
| **통신** | 자동 알림 (Push/Pull 방식) |
| **범위** | 같은 프로세스 내 |
| **활용** | UI 상태 관리, 이벤트 시스템, 데이터 변경 알림 |
| **장점** | 유연성, 확장성, 유지보수성 향상 |
| **단점** | 많은 Observer로 인한 성능 저하, 메모리 누수 위험 |

### 7-1. 사용 판단 기준
- **Observer Pattern**: 같은 프로세스 내에서 객체 간 느슨한 결합 필요
- **Kafka**: 분산 시스템에서 확장 가능한 이벤트 스트리밍 필요
- **Event-Driven Architecture**: 비즈니스 이벤트 기반 설계

### 7-2. 실무 팁
- Observer 등록/제거 시 메모리 누수 주의
- 많은 Observer가 있으면 비동기 처리 고려
- Spring의 `ApplicationEvent` 활용으로 프레임워크 수준의 구현
- UI 프레임워크(React, Vue)는 Reactive Pattern으로 Observer Pattern 구현
- 성능이 중요한 경우 메시지 큐(Kafka, RabbitMQ) 사용 고려