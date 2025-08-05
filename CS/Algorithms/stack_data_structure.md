# 스택 (Stack) 자료구조

스택(Stack)은 후입선출(LIFO: Last-In-First-Out)이라는 개념을 가진 선형 자료구조입니다.

## 스택의 기본 개념

### 주요 특징
- **후입선출(LIFO)**: 가장 나중에 들어온 데이터가 가장 먼저 나가는 구조
- **단방향 접근**: 스택의 최상단(top)에서만 데이터 삽입(push)과 삭제(pop) 가능
- **순차적 접근**: 중간 요소에 직접 접근할 수 없음

### 스택의 주요 연산
- **push**: 스택의 최상단에 새로운 요소 추가
- **pop**: 스택의 최상단에서 요소 제거 및 반환
- **peek/top**: 스택의 최상단 요소 조회 (제거하지 않음)
- **isEmpty**: 스택이 비어있는지 확인
- **size**: 스택에 저장된 요소의 개수 반환

## 스택의 예외 상황

### 스택 언더플로우 (Stack Underflow)
비어있는 스택에서 값을 추출하려고 시도하는 경우 발생하는 오류입니다.

### 스택 오버플로우 (Stack Overflow)
스택의 최대 용량을 초과하여 데이터를 저장하려고 할 때 발생하는 오류입니다.

## 스택의 활용 사례

### 1. 스택 메모리
- 함수 호출 시 지역 변수와 매개변수 저장
- 함수 종료 시 자동으로 메모리 해제

### 2. 브라우저 뒤로가기 기능
- 방문한 페이지를 스택에 저장
- 뒤로가기 버튼 클릭 시 이전 페이지로 이동

### 3. 언두(Undo) 기능
- 사용자의 작업 내역을 스택에 저장
- 실행 취소 시 가장 최근 작업부터 되돌림

### 4. 수식 괄호 검사
- 여는 괄호를 스택에 저장
- 닫는 괄호 발견 시 스택에서 pop하여 매칭 확인

## Java에서의 스택 구현

### Stack 클래스 사용
Java에서는 `Stack` 클래스를 제공하지만, 사용을 권장하지 않습니다.

```java
import java.util.Stack;

public class StackExample {
    public static void main(String[] args) {
        Stack<Integer> stack = new Stack<>();
        
        // push 연산
        stack.push(1);
        stack.push(2);
        stack.push(3);
        
        // pop 연산
        System.out.println(stack.pop()); // 3
        System.out.println(stack.pop()); // 2
        
        // peek 연산
        System.out.println(stack.peek()); // 1
    }
}
```

### Stack 클래스의 문제점

#### 1. Vector 상속으로 인한 설계 문제
- `Stack` 클래스는 내부적으로 `Vector`를 상속받음
- 인덱스를 통한 접근, 삽입, 제거가 실질적으로 가능
- 후입선출 특징에 맞지 않아 개발자가 실수할 여지가 있음

```java
Stack<Integer> stack = new Stack<>();
stack.push(1);
stack.push(2);
stack.push(3);

// Vector의 메소드 사용 가능 (스택 원칙 위반)
stack.add(1, 99);  // 중간에 삽입 가능
stack.remove(0);   // 임의 위치 삭제 가능
```

#### 2. 성능 문제
- `Vector`의 모든 메소드는 `synchronized`로 구현
- 멀티스레드 환경에서는 동기화의 이점이 있음
- 단일 스레드 환경에서는 불필요한 동기화 작업으로 인한 성능 저하

### 권장 방법: Deque 인터페이스 사용

`Deque` 인터페이스 구현체를 사용하는 것이 권장됩니다.

```java
import java.util.ArrayDeque;
import java.util.Deque;

public class DequeStackExample {
    public static void main(String[] args) {
        Deque<Integer> stack = new ArrayDeque<>();
        
        // push 연산
        stack.push(1);
        stack.push(2);
        stack.push(3);
        
        // pop 연산
        System.out.println(stack.pop()); // 3
        System.out.println(stack.pop()); // 2
        
        // peek 연산
        System.out.println(stack.peek()); // 1
    }
}
```

### Deque 사용의 장점

#### 1. 완전한 스택 특성 유지
- 후입선출의 특성을 완전히 유지
- 스택 원칙을 위반하는 메소드 접근 제한

#### 2. 성능 최적화 가능
- 동기화 작업을 가지는 구현체와 그렇지 않은 구현체 선택 가능
- `ArrayDeque`: 동기화 없음 (단일 스레드 환경에 적합)
- `LinkedBlockingDeque`: 동기화 있음 (멀티 스레드 환경에 적합)

#### 3. 유연한 구현체 선택
```java
// 단일 스레드 환경 - 높은 성능
Deque<Integer> stack1 = new ArrayDeque<>();

// 멀티 스레드 환경 - 스레드 안전
Deque<Integer> stack2 = new LinkedBlockingDeque<>();
```

## 스택 구현 예제

### 배열 기반 스택 구현
```java
public class ArrayStack<T> {
    private T[] stack;
    private int top;
    private int capacity;
    
    @SuppressWarnings("unchecked")
    public ArrayStack(int capacity) {
        this.capacity = capacity;
        this.stack = (T[]) new Object[capacity];
        this.top = -1;
    }
    
    public void push(T item) {
        if (top >= capacity - 1) {
            throw new RuntimeException("Stack Overflow");
        }
        stack[++top] = item;
    }
    
    public T pop() {
        if (isEmpty()) {
            throw new RuntimeException("Stack Underflow");
        }
        T item = stack[top];
        stack[top--] = null; // 메모리 누수 방지
        return item;
    }
    
    public T peek() {
        if (isEmpty()) {
            throw new RuntimeException("Stack is empty");
        }
        return stack[top];
    }
    
    public boolean isEmpty() {
        return top == -1;
    }
    
    public int size() {
        return top + 1;
    }
}
```

## 시간 복잡도

| 연산 | 시간 복잡도 |
|------|-------------|
| push | O(1) |
| pop | O(1) |
| peek | O(1) |
| isEmpty | O(1) |
| size | O(1) |

## 공간 복잡도
- **배열 기반**: O(n) - 고정 크기
- **연결 리스트 기반**: O(n) - 동적 크기

## 결론

스택은 후입선출 특성을 가진 중요한 자료구조로, 다양한 프로그래밍 상황에서 활용됩니다. Java에서는 `Stack` 클래스보다는 `Deque` 인터페이스 구현체를 사용하는 것이 성능과 설계 측면에서 더 적합합니다. 개발자는 사용 환경(단일/멀티 스레드)에 따라 적절한 구현체를 선택하여 최적의 성능을 얻을 수 있습니다.