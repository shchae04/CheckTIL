# 일급 컬렉션 (First-Class Collection)

## 개념
일급 컬렉션(First-Class Collection)이란 컬렉션을 포장(wrap)하면서, 그 외 다른 멤버 변수가 없는 상태를 일컫는 것입니다. 이 개념은 객체지향 설계 원칙 중 하나로, 소프트웨어 개발자 Robert C. Martin이 제안한 "객체지향 5대 원칙(SOLID)" 중 단일 책임 원칙(Single Responsibility Principle)을 따르는 설계 방식입니다.

## 특징

1. **컬렉션을 감싸는 전용 클래스**: 컬렉션을 단순히 필드로 가지는 것이 아니라, 해당 컬렉션에 대한 책임을 가진 별도의 클래스로 만듭니다.
2. **불변성(Immutability)**: 대부분의 경우 일급 컬렉션은 불변 객체로 설계됩니다.
3. **비즈니스 로직 캡슐화**: 컬렉션과 관련된 모든 비즈니스 로직을 해당 클래스 안에 캡슐화합니다.

## 장점

1. **비즈니스 로직의 응집도 향상**: 컬렉션과 관련된 로직이 한 곳에 모여 있어 코드의 응집도가 높아집니다.
2. **재사용성 증가**: 동일한 컬렉션 로직이 필요한 다른 부분에서 쉽게 재사용할 수 있습니다.
3. **불변성 보장**: 컬렉션의 불변성을 보장하여 예상치 못한 부작용을 방지합니다.
4. **검증 로직 추가 용이**: 컬렉션에 담기는 객체에 대한 검증 로직을 추가하기 쉽습니다.
5. **이름이 있는 컬렉션**: 컬렉션 자체에 의미 있는 이름을 부여할 수 있어 코드의 가독성이 향상됩니다.

## 예제 코드

### 일반적인 컬렉션 사용

```java
// 일반적인 컬렉션 사용
public class LotteryService {
    public void createLottery() {
        List<Integer> lotteryNumbers = new ArrayList<>();
        
        // 로또 번호 생성 로직
        for (int i = 0; i < 6; i++) {
            int number = (int) (Math.random() * 45) + 1;
            if (lotteryNumbers.contains(number)) {
                i--;
                continue;
            }
            lotteryNumbers.add(number);
        }
        
        // 로또 번호 검증 로직
        if (lotteryNumbers.size() != 6) {
            throw new IllegalArgumentException("로또 번호는 6개여야 합니다.");
        }
        
        for (int number : lotteryNumbers) {
            if (number < 1 || number > 45) {
                throw new IllegalArgumentException("로또 번호는 1부터 45 사이여야 합니다.");
            }
        }
        
        // 로또 번호 사용 로직
        // ...
    }
}
```

### 일급 컬렉션 사용

```java
// 일급 컬렉션 사용
public class LotteryNumbers {
    private final List<Integer> numbers;
    
    public LotteryNumbers(List<Integer> numbers) {
        validate(numbers);
        this.numbers = new ArrayList<>(numbers); // 불변성을 위해 복사
    }
    
    private void validate(List<Integer> numbers) {
        if (numbers.size() != 6) {
            throw new IllegalArgumentException("로또 번호는 6개여야 합니다.");
        }
        
        for (int number : numbers) {
            if (number < 1 || number > 45) {
                throw new IllegalArgumentException("로또 번호는 1부터 45 사이여야 합니다.");
            }
        }
        
        if (numbers.size() != numbers.stream().distinct().count()) {
            throw new IllegalArgumentException("로또 번호는 중복될 수 없습니다.");
        }
    }
    
    public static LotteryNumbers generate() {
        List<Integer> numbers = new ArrayList<>();
        while (numbers.size() < 6) {
            int number = (int) (Math.random() * 45) + 1;
            if (!numbers.contains(number)) {
                numbers.add(number);
            }
        }
        return new LotteryNumbers(numbers);
    }
    
    public List<Integer> getNumbers() {
        return new ArrayList<>(numbers); // 불변성을 위해 복사본 반환
    }
    
    public boolean contains(int number) {
        return numbers.contains(number);
    }
    
    // 추가적인 비즈니스 로직들...
}

public class LotteryService {
    public void createLottery() {
        LotteryNumbers lotteryNumbers = LotteryNumbers.generate();
        
        // 로또 번호 사용 로직
        // ...
    }
}
```

## 실무 적용 사례

### 1. 장바구니 (Cart)

```java
public class Cart {
    private final List<Item> items;
    
    public Cart(List<Item> items) {
        this.items = new ArrayList<>(items);
    }
    
    public int getTotalPrice() {
        return items.stream()
                .mapToInt(Item::getPrice)
                .sum();
    }
    
    public boolean hasItem(Item item) {
        return items.contains(item);
    }
    
    public int getItemCount() {
        return items.size();
    }
}
```

### 2. 회원 이메일 목록

```java
public class Emails {
    private final List<String> emails;
    
    public Emails(List<String> emails) {
        validateEmails(emails);
        this.emails = new ArrayList<>(emails);
    }
    
    private void validateEmails(List<String> emails) {
        for (String email : emails) {
            if (!isValidEmail(email)) {
                throw new IllegalArgumentException(email + "은 유효한 이메일 형식이 아닙니다.");
            }
        }
    }
    
    private boolean isValidEmail(String email) {
        // 이메일 유효성 검사 로직
        return email.matches("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    }
    
    public boolean contains(String email) {
        return emails.contains(email);
    }
    
    public int size() {
        return emails.size();
    }
}
```

## 결론

일급 컬렉션은 단순히 컬렉션을 감싸는 것 이상의 의미를 가집니다. 이는 객체지향 설계의 핵심 원칙인 캡슐화와 단일 책임 원칙을 실현하는 방법 중 하나로, 
코드의 품질을 높이고 유지보수성을 향상시키는 데 큰 도움이 됩니다. 특히 도메인 주도 설계(DDD)에서 값 객체(Value Object)로 활용될 때 그 효과가 더욱 극대화됩니다.