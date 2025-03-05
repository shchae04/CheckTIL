# SOLID 원칙

SOLID는 객체 지향 프로그래밍 및 설계의 다섯 가지 기본 원칙을 나타내는 약어입니다. 이 원칙들은 유지보수가 쉽고 확장 가능한 소프트웨어를 만드는데 도움을 줍니다.

## 1. Single Responsibility Principle (단일 책임 원칙)
한 클래스는 하나의 책임만 가져야 합니다.

### 예시
```java
// 잘못된 예
class UserService {
    public void saveUser(User user) { /* 사용자 저장 로직 */ }
    public void sendEmail(User user) { /* 이메일 발송 로직 */ }
    public void generateReport(User user) { /* 리포트 생성 로직 */ }
}

// 올바른 예
class UserService {
    public void saveUser(User user) { /* 사용자 저장 로직 */ }
}

class EmailService {
    public void sendEmail(User user) { /* 이메일 발송 로직 */ }
}

class ReportService {
    public void generateReport(User user) { /* 리포트 생성 로직 */ }
}
```

## 2. Open-Closed Principle (개방-폐쇄 원칙)
소프트웨어 요소는 확장에는 열려 있으나 변경에는 닫혀 있어야 합니다.

### 예시
```java
// 잘못된 예
class PaymentProcessor {
    public void processPayment(String type) {
        if (type.equals("CREDIT")) {
            // 신용카드 결제 처리
        } else if (type.equals("DEBIT")) {
            // 직불카드 결제 처리
        }
        // 새로운 결제 방식 추가시 코드 수정 필요
    }
}

// 올바른 예
interface Payment {
    void process();
}

class CreditPayment implements Payment {
    public void process() {
        // 신용카드 결제 처리
    }
}

class DebitPayment implements Payment {
    public void process() {
        // 직불카드 결제 처리
    }
}
// 새로운 결제 방식 추가시 새로운 클래스만 구현하면 됨
```

## 3. Liskov Substitution Principle (리스코프 치환 원칙)
프로그램의 객체는 프로그램의 정확성을 깨뜨리지 않으면서 하위 타입의 인스턴스로 바꿀 수 있어야 합니다.

### 예시
```java
// 잘못된 예
class Bird {
    public void fly() { }
}

class Penguin extends Bird {
    @Override
    public void fly() {
        throw new UnsupportedOperationException(); // LSP 위반
    }
}

// 올바른 예
interface FlyingBird {
    void fly();
}

interface WalkingBird {
    void walk();
}

class Sparrow implements FlyingBird {
    public void fly() { /* 구현 */ }
}

class Penguin implements WalkingBird {
    public void walk() { /* 구현 */ }
}
```

## 4. Interface Segregation Principle (인터페이스 분리 원칙)
클라이언트는 자신이 사용하지 않는 메서드에 의존 관계를 맺으면 안됩니다.

### 예시
```java
// 잘못된 예
interface Worker {
    void work();
    void eat();
    void sleep();
}

// 로봇은 eat과 sleep이 필요없음
class Robot implements Worker {
    public void work() { /* 구현 */ }
    public void eat() { /* 불필요 */ }
    public void sleep() { /* 불필요 */ }
}

// 올바른 예
interface Workable {
    void work();
}

interface Eatable {
    void eat();
}

interface Sleepable {
    void sleep();
}

class Human implements Workable, Eatable, Sleepable {
    public void work() { /* 구현 */ }
    public void eat() { /* 구현 */ }
    public void sleep() { /* 구현 */ }
}

class Robot implements Workable {
    public void work() { /* 구현 */ }
}
```

## 5. Dependency Inversion Principle (의존관계 역전 원칙)
고수준 모듈은 저수준 모듈의 구현에 의존해서는 안됩니다. 둘 다 추상화에 의존해야 합니다.

### 예시
```java
// 잘못된 예
class EmailService {
    private MySQLDatabase database; // 구체 클래스에 직접 의존

    public EmailService() {
        this.database = new MySQLDatabase();
    }
}

// 올바른 예
interface Database {
    void save(String data);
}

class MySQLDatabase implements Database {
    public void save(String data) { /* 구현 */ }
}

class EmailService {
    private Database database; // 인터페이스에 의존

    public EmailService(Database database) {
        this.database = database;
    }
}
```

## SOLID 원칙의 장점
1. 유지보수성 향상
   - 코드 변경이 필요할 때 다른 부분에 미치는 영향 최소화
   - 버그 수정이 용이

2. 확장성 증가
   - 새로운 기능 추가가 쉬움
   - 기존 코드 수정 없이 새로운 기능 구현 가능

3. 재사용성 향상
   - 독립적인 컴포넌트로 분리되어 있어 재사용이 쉬움
   - 테스트가 용이

4. 복잡성 감소
   - 각 컴포넌트의 책임이 명확
   - 코드 이해가 쉬움

## 결론
SOLID 원칙은 객체 지향 설계의 핵심 원칙으로, 이를 잘 적용하면 유지보수가 쉽고, 유연하며, 확장 가능한 소프트웨어를 만들 수 있습니다. 
하지만 이러한 원칙들을 상황에 맞게 적절히 적용하는 것이 중요하며, 때로는 실용적인 관점에서 타협이 필요할 수 있습니다.