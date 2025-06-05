# 클린 코드 (Clean Code)

클린 코드는 가독성이 높고, 유지보수가 용이하며, 버그가 적은 코드를 작성하는 방법론입니다. 로버트 C. 마틴(Robert C. Martin)의 저서 "Clean Code"에서 체계화된 이 개념은 소프트웨어 개발에서 중요한 원칙으로 자리 잡았습니다.

## 의미 있는 이름 사용하기

### 의도를 명확히 하는 변수명
```java
// 나쁜 예
int d; // 경과 시간(일)

// 좋은 예
int elapsedTimeInDays;
int daysSinceCreation;
```

### 검색하기 쉬운 이름
```java
// 나쁜 예
for (int i = 0; i < 34; i++) {
    s += (t[i] * 4) / 5;
}

// 좋은 예
int WORK_DAYS_PER_WEEK = 5;
int MAX_WORK_HOURS = 8;
for (int i = 0; i < NUMBER_OF_TASKS; i++) {
    sum += (taskHours[i] * WORK_DAYS_PER_WEEK) / MAX_WORK_HOURS;
}
```

## 함수 작성하기

### 작고 한 가지 일만 수행하는 함수
```java
// 나쁜 예
public void emailClients(List<Client> clients) {
    for (Client client : clients) {
        if (client.isActive()) {
            Email email = new Email();
            email.setTo(client.getEmail());
            email.setSubject("중요 공지사항");
            email.setBody("안녕하세요...");
            sendEmail(email);
        }
    }
}

// 좋은 예
public void emailClients(List<Client> clients) {
    List<Client> activeClients = filterActiveClients(clients);
    for (Client client : activeClients) {
        sendEmail(client, createEmail(client));
    }
}

private List<Client> filterActiveClients(List<Client> clients) {
    return clients.stream()
        .filter(Client::isActive)
        .collect(Collectors.toList());
}

private Email createEmail(Client client) {
    Email email = new Email();
    email.setTo(client.getEmail());
    email.setSubject("중요 공지사항");
    email.setBody("안녕하세요...");
    return email;
}
```

### 함수 인수는 적게 유지
```java
// 나쁜 예
public void createMenu(String title, String body, String buttonText, boolean cancellable) {
    // ...
}

// 좋은 예
public class MenuConfig {
    private String title;
    private String body;
    private String buttonText;
    private boolean cancellable;
    
    // 생성자, getter, setter...
}

public void createMenu(MenuConfig config) {
    // ...
}
```

## 주석 사용하기

### 코드로 의도를 표현하기
```java
// 나쁜 예
// 사용자가 구독 중인지 확인
if (user.getSubscriptionDate() > System.currentTimeMillis()) {...}

// 좋은 예
if (user.isSubscriptionActive()) {...}
```

### 필요한 경우에만 주석 사용
```java
/**
 * 정규표현식을 사용하여 이메일 형식이 유효한지 검증합니다.
 * RFC 5322 표준을 준수하는 이메일 주소만 유효하다고 판단합니다.
 */
public boolean isValidEmail(String email) {
    Pattern pattern = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$");
    return pattern.matcher(email).matches();
}
```

## 오류 처리

### 예외 처리와 비즈니스 로직 분리
```java
// 나쁜 예
try {
    String fileContent = readFile(fileName);
    // 파일 내용 처리
    updateDatabase(fileContent);
    sendNotification("파일 처리 완료");
} catch (IOException e) {
    log.error("파일 읽기 실패", e);
}

// 좋은 예
public void processFile(String fileName) {
    try {
        String fileContent = readFile(fileName);
        processFileContent(fileContent);
    } catch (IOException e) {
        handleFileReadError(e, fileName);
    }
}

private void processFileContent(String content) {
    updateDatabase(content);
    sendNotification("파일 처리 완료");
}

private void handleFileReadError(IOException e, String fileName) {
    log.error("파일 읽기 실패: " + fileName, e);
    notifyAdministrator(fileName, e);
}
```

### try-with-resources 사용
```java
// 좋은 예
try (FileInputStream input = new FileInputStream("file.txt");
     BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
    String line;
    while ((line = reader.readLine()) != null) {
        // 처리 로직
    }
}
```

## 클래스 설계

### 단일 책임 원칙(SRP) 준수
```java
// 나쁜 예
public class UserService {
    public User getUser(String id) { /*...*/ }
    public void saveUser(User user) { /*...*/ }
    public void sendEmail(User user, String message) { /*...*/ }
    public void generateReport(User user) { /*...*/ }
}

// 좋은 예
public class UserService {
    private EmailService emailService;
    private ReportService reportService;
    
    public User getUser(String id) { /*...*/ }
    public void saveUser(User user) { /*...*/ }
}

public class EmailService {
    public void sendEmail(User user, String message) { /*...*/ }
}

public class ReportService {
    public void generateReport(User user) { /*...*/ }
}
```

### 불변성 활용
```java
// 좋은 예
public final class Money {
    private final BigDecimal amount;
    private final Currency currency;
    
    public Money(BigDecimal amount, Currency currency) {
        this.amount = amount;
        this.currency = currency;
    }
    
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("통화가 다릅니다");
        }
        return new Money(this.amount.add(other.amount), this.currency);
    }
    
    // getter만 제공, setter 없음
    public BigDecimal getAmount() { return amount; }
    public Currency getCurrency() { return currency; }
}
```

## 코드 포맷팅

### 일관된 들여쓰기와 포맷팅
```java
// 좋은 예
public class Rectangle {
    private double length;
    private double width;
    
    public Rectangle(double length, double width) {
        this.length = length;
        this.width = width;
    }
    
    public double area() {
        return length * width;
    }
    
    public double perimeter() {
        return 2 * (length + width);
    }
}
```

### 수직 거리 최소화
```java
// 좋은 예 - 관련 있는 코드는 가까이 배치
public class Invoice {
    private List<InvoiceItem> items;
    private Customer customer;
    
    public double calculateTotal() {
        return items.stream()
            .mapToDouble(InvoiceItem::getTotal)
            .sum();
    }
    
    public void addItem(InvoiceItem item) {
        items.add(item);
    }
    
    public void removeItem(InvoiceItem item) {
        items.remove(item);
    }
}
```

## 단위 테스트

### 깨끗한 테스트 코드 작성
```java
// 좋은 예
@Test
public void shouldCalculateCorrectOrderTotal() {
    // Arrange
    Order order = new Order();
    order.addItem(new OrderItem("Item1", 10.0, 2));
    order.addItem(new OrderItem("Item2", 5.0, 1));
    
    // Act
    double total = order.calculateTotal();
    
    // Assert
    assertEquals(25.0, total, 0.001);
}
```

### 테스트는 독립적이고 반복 가능해야 함
```java
// 좋은 예
@Test
public void shouldSaveUserToDatabase() {
    // Arrange
    UserRepository repository = new InMemoryUserRepository();
    User user = new User("user1", "password", "user@example.com");
    
    // Act
    repository.save(user);
    
    // Assert
    User savedUser = repository.findById("user1");
    assertNotNull(savedUser);
    assertEquals("user@example.com", savedUser.getEmail());
}
```

## 결론

클린 코드는 단순히 "작동하는 코드"를 넘어서, 읽기 쉽고 이해하기 쉬운 코드를 작성하는 것을 목표로 합니다. 이는 코드 리뷰, 유지보수, 협업 과정에서 큰 이점을 제공합니다. 클린 코드의 원칙을 따르면 버그를 줄이고, 개발 속도를 높이며, 팀의 생산성을 향상시킬 수 있습니다.