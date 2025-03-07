# 디자인 패턴 (Design Patterns)

## 디자인 패턴이란?
디자인 패턴은 소프트웨어 개발에서 자주 발생하는 문제들에 대한 일반적인 해결책입니다. 이는 코드의 재사용성, 유지보수성, 확장성을 높이는데 도움을 줍니다.

### 디자인 패턴을 사용해야 하는 경우
- **반복되는 문제 해결**: 비슷한 문제가 여러 번 발생할 때
- **검증된 솔루션 필요**: 많은 개발자들이 검증한 해결책을 적용하고 싶을 때
- **유지보수성 향상**: 코드의 구조를 개선하고 향후 변경을 쉽게 만들고 싶을 때
- **팀 커뮤니케이션**: 개발자들 간의 의사소통을 명확하게 하고 싶을 때

### 주의사항
- 모든 상황에 디자인 패턴을 적용하는 것은 오히려 코드를 복잡하게 만들 수 있습니다
- 실제 문제와 패턴의 적합성을 신중히 검토한 후 적용해야 합니다
- 패턴을 적용하기 전에 단순한 해결책은 없는지 먼저 고려해야 합니다

## GoF (Gang of Four)
GoF는 에리히 감마(Erich Gamma), 리차드 헬름(Richard Helm), 랄프 존슨(Ralph Johnson), 존 블리시디스(John Vlissides) 네 명의 개발자를 지칭합니다. 이들은 1994년 "Design Patterns: Elements of Reusable Object-Oriented Software"라는 책을 출간했으며, 이 책은 디자인 패턴을 체계적으로 정리한 최초의 서적으로 평가받고 있습니다.

## 디자인 패턴의 종류
GoF는 23가지 디자인 패턴을 다음과 같이 세 가지 카테고리로 분류했습니다. 각 카테고리별로 주요 패턴들의 실제 구현 예시를 살펴보겠습니다.

### 1. 생성 패턴 (Creational Patterns)
객체 생성에 관련된 패턴으로, 다음과 같은 패턴들이 있습니다:
- 싱글톤 (Singleton)
- 팩토리 메소드 (Factory Method)
- 추상 팩토리 (Abstract Factory)
- 빌더 (Builder)
- 프로토타입 (Prototype)

다음은 주요 생성 패턴들의 구체적인 예시입니다:

#### 싱글톤 패턴 (Singleton Pattern)
- **목적**: 클래스의 인스턴스가 하나만 생성되도록 보장
- **예시**:
```java
public class DatabaseConnection {
    private static DatabaseConnection instance;

    private DatabaseConnection() {}

    public static DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
}
```
- **사용 사례**: 데이터베이스 연결, 로깅 시스템, 설정 관리

#### 팩토리 메소드 패턴 (Factory Method Pattern)
- **목적**: 객체 생성을 서브클래스에 위임
- **예시**:
```java
interface Animal {
    void makeSound();
}

class Dog implements Animal {
    public void makeSound() {
        System.out.println("멍멍!");
    }
}

class Cat implements Animal {
    public void makeSound() {
        System.out.println("야옹!");
    }
}

class AnimalFactory {
    public Animal createAnimal(String type) {
        if (type.equals("dog")) return new Dog();
        if (type.equals("cat")) return new Cat();
        return null;
    }
}
```

### 2. 구조 패턴 (Structural Patterns)
클래스와 객체를 더 큰 구조로 조합하는 패턴으로, 다음과 같은 패턴들이 있습니다:
- 어댑터 (Adapter)
- 브리지 (Bridge)
- 컴포지트 (Composite)
- 데코레이터 (Decorator)
- 퍼사드 (Facade)
- 플라이웨이트 (Flyweight)
- 프록시 (Proxy)

다음은 주요 구조 패턴들의 구체적인 예시입니다:

#### 어댑터 패턴 (Adapter Pattern)
- **목적**: 호환되지 않는 인터페이스를 함께 동작하도록 변환
- **예시**:
```java
interface MediaPlayer {
    void play(String filename);
}

class MP3Player implements MediaPlayer {
    public void play(String filename) {
        System.out.println("MP3 파일 재생: " + filename);
    }
}

interface AdvancedMediaPlayer {
    void playVideo(String filename);
}

class VideoPlayer implements AdvancedMediaPlayer {
    public void playVideo(String filename) {
        System.out.println("비디오 파일 재생: " + filename);
    }
}

class MediaAdapter implements MediaPlayer {
    AdvancedMediaPlayer advancedPlayer;

    public MediaAdapter(AdvancedMediaPlayer player) {
        this.advancedPlayer = player;
    }

    public void play(String filename) {
        advancedPlayer.playVideo(filename);
    }
}
```

### 3. 행위 패턴 (Behavioral Patterns)
객체들의 상호작용과 책임 분배에 관한 패턴으로, 다음과 같은 패턴들이 있습니다:
- 옵저버 (Observer)
- 스테이트 (State)
- 스트래티지 (Strategy)
- 템플릿 메소드 (Template Method)
- 커맨드 (Command)
- 인터프리터 (Interpreter)
- 이터레이터 (Iterator)
- 미디에이터 (Mediator)
- 메멘토 (Memento)
- 책임 연쇄 (Chain of Responsibility)
- 비지터 (Visitor)

다음은 주요 행위 패턴들의 구체적인 예시입니다:

#### 옵저버 패턴 (Observer Pattern)
- **목적**: 객체의 상태 변화를 다른 객체들에게 자동으로 알림
- **예시**:
```java
interface Observer {
    void update(String message);
}

class NewsAgency {
    private List<Observer> observers = new ArrayList<>();

    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    public void notifyObservers(String news) {
        for (Observer observer : observers) {
            observer.update(news);
        }
    }
}

class NewsChannel implements Observer {
    private String name;

    public NewsChannel(String name) {
        this.name = name;
    }

    public void update(String news) {
        System.out.println(name + "에서 뉴스 수신: " + news);
    }
}
```

## 실제 사용 사례

### 1. 스프링 프레임워크
- **싱글톤 패턴**: 빈(Bean) 관리
- **팩토리 패턴**: BeanFactory
- **프록시 패턴**: AOP 구현

### 2. 안드로이드 개발
- **옵저버 패턴**: 이벤트 리스너
- **빌더 패턴**: AlertDialog 생성
- **어댑터 패턴**: RecyclerView Adapter

### 3. 자바 API
- **데코레이터 패턴**: java.io 패키지의 InputStream, OutputStream
- **이터레이터 패턴**: Collection 프레임워크
- **전략 패턴**: Comparator 인터페이스

## 디자인 패턴 선택 시 고려사항
1. **문제의 성격**: 해당 패턴이 현재 문제 해결에 적합한지
2. **유지보수성**: 코드의 복잡도와 이해도
3. **확장성**: 미래의 요구사항 변경에 대한 대응
4. **성능**: 패턴 적용으로 인한 성능 영향

## 현대 소프트웨어 개발에서의 디자인 패턴
디자인 패턴은 1994년 GoF에 의해 체계화된 이후 현대 소프트웨어 개발에서도 여전히 중요한 역할을 하고 있습니다:

### 새로운 컨텍스트에서의 적용
- **마이크로서비스 아키텍처**: 서비스 간 통신 패턴
- **클라우드 네이티브**: 클라우드 환경에 맞는 패턴 변형
- **리액티브 프로그래밍**: 비동기 및 이벤트 기반 패턴

### 프레임워크와의 통합
- **스프링 프레임워크**: 의존성 주입, AOP 등에서 패턴 활용
- **리액트/뷰**: 컴포넌트 기반 아키텍처에서의 패턴 적용
- **안드로이드/iOS**: 모바일 앱 개발에서의 패턴 활용

### 새로운 패턴의 등장
- **CQRS (Command Query Responsibility Segregation)**
- **Event Sourcing**
- **Circuit Breaker**
- **Saga Pattern**

## 결론
디자인 패턴은 검증된 해결책을 제공하여 개발 시간을 단축하고, 코드의 품질을 향상시킵니다. 하지만 모든 상황에 패턴을 적용하는 것은 오히려 복잡도만 증가시킬 수 있으므로, 적절한 상황에 맞는 패턴을 선택하는 것이 중요합니다.
