# Java는 Call by Value일까? Call by Reference일까?

## 1. 결론 (TL;DR)
- Java는 100% Call by Value(값에 의한 호출) 언어입니다.
- 단, "참조(Reference)라는 값"도 값이기 때문에, 객체를 넘길 때는 객체의 참조값이 복사되어 전달됩니다.
  - 기본형(primitive): 값 자체가 복사됩니다 → 메서드 내부 변경이 호출자에게 영향 없음.
  - 참조형(reference type): 참조값(주소)이 복사됩니다 → 같은 객체를 가리키므로, "객체의 상태 변화"는 호출자에게 보입니다. 하지만 파라미터 변수에 새로운 객체를 할당(reassign)해도 호출자의 변수는 바뀌지 않습니다.

---

## 2. 왜 혼동될까?
- 객체를 넘겨서 메서드 안에서 `obj.setX(…)`처럼 상태를 바꾸면, 호출자 쪽에서도 값이 바뀐 것처럼 보입니다.
- 이 때문에 "참조로 넘기는 것 아닌가?"라고 생각하기 쉽지만, 사실은 "참조값을 값으로 복사"해서 넘긴 것입니다.

```
호출자 변수 ──┐            메서드 파라미터 변수 ──┐
              ▼                                   ▼
           [참조값] ─────────(값 복사)────────▶ [참조값]
              │                                   │
              └───────────── 같은 객체 ────────────┘
```

- 두 변수는 같은 객체를 가리키지만, 변수 자체는 서로 다른 독립 변수입니다. 그래서 파라미터 변수를 새로운 객체로 재할당해도 호출자 변수에는 영향이 없습니다.

---

## 3. 코드로 보는 차이

### 3.1 기본형: 값 복사
```java
public class Demo {
    static void inc(int x) {
        x++; // 로컬 복사본만 증가
    }
    public static void main(String[] args) {
        int a = 10;
        inc(a);
        System.out.println(a); // 10 (변화 없음)
    }
}
```

### 3.2 참조형: 참조값 복사 → 상태 변경은 보임
```java
class Box { int v; }

public class Demo {
    static void setTo42(Box b) {
        b.v = 42; // 같은 객체의 상태를 수정
    }
    public static void main(String[] args) {
        Box box = new Box();
        box.v = 10;
        setTo42(box);
        System.out.println(box.v); // 42 (상태 변경이 반영됨)
    }
}
```

### 3.3 재할당은 호출자에 영향 없음
```java
class Box { int v; }

public class Demo {
    static void reassign(Box b) {
        b = new Box(); // 파라미터 변수만 새 객체를 가리킴
        b.v = 99;
    }
    public static void main(String[] args) {
        Box box = new Box();
        box.v = 10;
        reassign(box);
        System.out.println(box.v); // 10 (호출자 변수는 여전히 원래 객체)
    }
}
```

### 3.4 스왑 실패 예시 (전형적인 함정)
```java
class Box { int v; }

public class Demo {
    static void swap(Box a, Box b) {
        Box tmp = a;
        a = b;   // 파라미터 변수들만 서로 바뀜
        b = tmp; // 호출자 쪽 변수는 그대로
    }
    public static void main(String[] args) {
        Box x = new Box(); x.v = 1;
        Box y = new Box(); y.v = 2;
        swap(x, y);
        System.out.println(x.v + ", " + y.v); // 1, 2 (스왑 안 됨)
    }
}
```

---

## 4. 문자열과 래퍼 타입은 왜 더 헷갈릴까?
- String, Integer 등은 불변(Immutable)입니다. 상태를 바꾸는 메서드가 없고, 연산 시 새 객체를 생성합니다.
- 그래서 메서드 안에서 "값을 바꿨다"고 생각해도 사실은 새 객체를 만들고 파라미터 변수에 재할당하는 것이므로, 호출자에는 영향이 없습니다.

```java
public class Demo {
    static void appendWorld(String s) {
        s = s + " World"; // 새 String 생성 후 s에 재할당(로컬 변수)
    }
    public static void main(String[] args) {
        String hello = "Hello";
        appendWorld(hello);
        System.out.println(hello); // "Hello" (변화 없음)
    }
}
```

- 오토박싱된 Integer, Long 등도 불변이라 비슷한 혼동이 발생합니다.

---

## 5. 배열, 컬렉션은?
- 배열과 컬렉션도 참조형입니다. 참조값이 복사되어 전달되므로, 내부 원소나 크기를 변경하면 호출자에게 영향이 있습니다.

```java
import java.util.*;

public class Demo {
    static void mutate(List<Integer> list) {
        list.add(3); // 같은 리스트 객체를 수정
        list = new ArrayList<>(); // 재할당은 로컬 변수에만 영향
        list.add(99);
    }
    public static void main(String[] args) {
        List<Integer> nums = new ArrayList<>(List.of(1, 2));
        mutate(nums);
        System.out.println(nums); // [1, 2, 3]
    }
}
```

---

## 6. 진짜 "참조로 넘기기"처럼 동작하게 하려면?
- 파라미터로 넘긴 변수 자체를 바꾸고 싶다면(예: 스왑 구현), 다음과 같은 전략을 사용합니다.
  1) 반환값 사용: 메서드가 바뀐 값을 반환하고, 호출자에서 재할당.
  2) 가변 래퍼(홀더) 사용: 사용자 정의 MutableBox<T>, Apache Commons Lang의 MutableInt 등.
  3) 배열/리스트에 담아서 전달: 배열[0], 리스트.set(0, …)로 값을 바꿈.
  4) 동시성/원자성 필요 시 AtomicReference<T> 사용.

```java
class Ref<T> { T v; Ref(T v){ this.v = v; } }

public class Demo {
    static void swap(Ref<Box> a, Ref<Box> b) {
        Box tmp = a.v;
        a.v = b.v;
        b.v = tmp; // 호출자 보유 필드를 실제로 교체
    }
}
```

---

## 7. 한 문장 요약 (인터뷰 답변 예시)
- "Java는 무조건 Call by Value입니다. 기본형은 값 자체가, 참조형은 '참조값'이 값으로 복사되어 전달됩니다. 그래서 객체 상태 변경은 보이지만, 파라미터 변수 재할당은 호출자에 영향을 주지 않습니다."

## 8. Key Points
- Java = Pass-by-Value only.
- 참조형 인자는 "참조값"의 복사 전달.
- 상태 변경은 전파되지만, 참조 재할당은 전파되지 않음.
- String/Wrapper는 불변 → 재할당은 호출자에 영향 없음.
- 스왑/포인터 같은 동작은 반환값/가변 래퍼/배열/AtomicReference 등으로 구현.
