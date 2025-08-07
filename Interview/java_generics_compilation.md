# Java Generics 컴파일 과정에서의 처리 방식

Java Generics는 컴파일 시점에 타입 안전성을 제공하면서도 런타임에는 타입 정보를 제거하는 독특한 방식으로 동작합니다.

## 1. Type Erasure (타입 소거)

### 개념
Java Generics의 핵심 메커니즘으로, 컴파일 시점에 제네릭 타입 정보를 검증한 후 런타임에는 이 정보를 제거하는 과정입니다.

### 타입 소거 과정

#### 컴파일 전 코드
```java
List<String> stringList = new ArrayList<String>();
List<Integer> intList = new ArrayList<Integer>();
stringList.add("Hello");
intList.add(42);
```

#### 컴파일 후 바이트코드 (개념적 표현)
```java
List stringList = new ArrayList();
List intList = new ArrayList();
stringList.add("Hello");
intList.add(42);
```

### 타입 소거 규칙

1. **Unbounded Type Parameter**: `Object`로 대체
```java
// 컴파일 전
public class Box<T> {
    private T item;
    public void set(T item) { this.item = item; }
    public T get() { return item; }
}

// 컴파일 후
public class Box {
    private Object item;
    public void set(Object item) { this.item = item; }
    public Object get() { return item; }
}
```

2. **Bounded Type Parameter**: 첫 번째 bound로 대체
```java
// 컴파일 전
public class NumberBox<T extends Number> {
    private T value;
    public void setValue(T value) { this.value = value; }
    public T getValue() { return value; }
}

// 컴파일 후
public class NumberBox {
    private Number value;
    public void setValue(Number value) { this.value = value; }
    public Number getValue() { return value; }
}
```

## 2. Bridge Methods (브릿지 메서드)

### 개념
타입 소거로 인해 발생할 수 있는 다형성 문제를 해결하기 위해 컴파일러가 자동으로 생성하는 메서드입니다.

### 예시

#### 원본 코드
```java
public class Node<T> {
    public T data;
    
    public Node(T data) { this.data = data; }
    
    public void setData(T data) {
        System.out.println("Node.setData");
        this.data = data;
    }
}

public class MyNode extends Node<Integer> {
    public MyNode(Integer data) { super(data); }
    
    @Override
    public void setData(Integer data) {
        System.out.println("MyNode.setData");
        super.setData(data);
    }
}
```

#### 컴파일러가 생성하는 브릿지 메서드
```java
public class MyNode extends Node {
    public MyNode(Integer data) { super(data); }
    
    // 사용자가 작성한 메서드
    public void setData(Integer data) {
        System.out.println("MyNode.setData");
        super.setData(data);
    }
    
    // 컴파일러가 자동 생성하는 브릿지 메서드
    public void setData(Object data) {
        setData((Integer) data);
    }
}
```

### 브릿지 메서드 확인
```java
public class BridgeMethodExample {
    public static void main(String[] args) {
        MyNode node = new MyNode(10);
        
        // 리플렉션을 통해 브릿지 메서드 확인
        Method[] methods = MyNode.class.getDeclaredMethods();
        for (Method method : methods) {
            System.out.println("Method: " + method.getName() + 
                             ", Bridge: " + method.isBridge() +
                             ", Synthetic: " + method.isSynthetic());
        }
    }
}
```

#### 실행 결과
```
Method: setData, Bridge: false, Synthetic: false
Method: setData, Bridge: true, Synthetic: true
```

## 3. Raw Types (원시 타입)

### 개념
제네릭 타입에서 타입 매개변수를 생략한 형태로, 하위 호환성을 위해 허용됩니다.

### 예시와 경고
```java
public class RawTypeExample {
    public static void main(String[] args) {
        // Raw type 사용 - 컴파일러 경고 발생
        List rawList = new ArrayList();
        rawList.add("String");
        rawList.add(42);
        
        // 제네릭 타입 사용 - 타입 안전성 보장
        List<String> stringList = new ArrayList<String>();
        stringList.add("String");
        // stringList.add(42); // 컴파일 에러
        
        // Raw type에서 제네릭 타입으로 할당 - 경고 발생
        List<String> fromRaw = rawList; // Unchecked assignment warning
        
        // 제네릭 타입에서 Raw type으로 할당 - 경고 없음
        List toRaw = stringList;
    }
}
```

### 컴파일러 경고 메시지
```
Warning: Raw use of parameterized class 'List'
Warning: Unchecked assignment: 'java.util.List' to 'java.util.List<java.lang.String>'
Warning: Unchecked call to 'add(E)' as a member of raw type 'java.util.List'
```

## 4. 컴파일 시점 vs 런타임 동작

### 컴파일 시점에서의 처리

#### 타입 검증
```java
public class CompileTimeCheck {
    public static void main(String[] args) {
        List<String> strings = new ArrayList<String>();
        
        // 컴파일 시점에 타입 검증
        strings.add("Hello");        // OK
        // strings.add(42);          // 컴파일 에러
        
        String str = strings.get(0); // 자동 캐스팅, 타입 안전성 보장
    }
}
```

#### 타입 추론 (Java 7+)
```java
// Java 7+ Diamond Operator
List<String> strings = new ArrayList<>(); // 타입 추론

// Java 10+ var 키워드
var stringList = new ArrayList<String>(); // 타입 추론
```

### 런타임에서의 동작

#### 타입 정보 손실
```java
public class RuntimeBehavior {
    public static void main(String[] args) {
        List<String> stringList = new ArrayList<String>();
        List<Integer> intList = new ArrayList<Integer>();
        
        // 런타임에는 둘 다 같은 클래스
        System.out.println(stringList.getClass() == intList.getClass()); // true
        System.out.println(stringList.getClass()); // class java.util.ArrayList
        
        // 제네릭 타입 정보는 런타임에 사용할 수 없음
        // if (stringList instanceof List<String>) {} // 컴파일 에러
        if (stringList instanceof List) {} // OK, Raw type으로만 확인 가능
    }
}
```

#### 리플렉션을 통한 제네릭 정보 접근
```java
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class GenericReflection {
    private List<String> stringList;
    
    public static void main(String[] args) throws Exception {
        // 필드의 제네릭 타입 정보는 런타임에도 접근 가능
        Field field = GenericReflection.class.getDeclaredField("stringList");
        Type genericType = field.getGenericType();
        
        if (genericType instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type[] actualTypes = paramType.getActualTypeArguments();
            System.out.println("Generic type: " + actualTypes[0]); // class java.lang.String
        }
    }
}
```

## 5. 제네릭 메서드와 와일드카드

### 제네릭 메서드 컴파일
```java
public class GenericMethods {
    // 제네릭 메서드
    public static <T> void swap(T[] array, int i, int j) {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
    
    // 컴파일 후 (개념적)
    public static void swap(Object[] array, int i, int j) {
        Object temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }
}
```

### 와일드카드 처리
```java
public class WildcardExample {
    // Upper bounded wildcard
    public static double sum(List<? extends Number> numbers) {
        double sum = 0.0;
        for (Number num : numbers) {
            sum += num.doubleValue();
        }
        return sum;
    }
    
    // Lower bounded wildcard
    public static void addNumbers(List<? super Integer> numbers) {
        numbers.add(1);
        numbers.add(2);
    }
    
    // 컴파일 후에는 적절한 캐스팅과 함께 Object나 bound 타입으로 변환
}
```

## 6. 제네릭 사용 시 주의사항

### 1. 배열과 제네릭
```java
public class ArrayGenericIssue {
    public static void main(String[] args) {
        // 제네릭 배열 생성 불가
        // List<String>[] arrays = new List<String>[10]; // 컴파일 에러
        
        // 우회 방법 (권장하지 않음)
        List<String>[] arrays = (List<String>[]) new List[10];
        
        // 권장 방법
        List<List<String>> listOfLists = new ArrayList<>();
    }
}
```

### 2. 정적 컨텍스트에서의 제한
```java
public class StaticGenericIssue<T> {
    // 정적 필드에 타입 매개변수 사용 불가
    // private static T staticField; // 컴파일 에러
    
    // 정적 메서드에 클래스 타입 매개변수 사용 불가
    // public static void staticMethod(T param) {} // 컴파일 에러
    
    // 정적 제네릭 메서드는 가능
    public static <U> void staticGenericMethod(U param) {
        // OK
    }
}
```

## 7. 성능 영향

### 장점
- **타입 안전성**: 컴파일 시점에 타입 오류 검출
- **캐스팅 제거**: 명시적 캐스팅 불필요
- **코드 가독성**: 의도가 명확한 코드

### 단점
- **타입 소거로 인한 제약**: 런타임 타입 정보 손실
- **브릿지 메서드**: 추가적인 메서드 생성으로 인한 미미한 오버헤드
- **컴파일 시간**: 타입 검증으로 인한 컴파일 시간 증가

## 8. 실제 바이트코드 확인

### 컴파일 및 바이트코드 확인 방법
```bash
# Java 파일 컴파일
javac GenericExample.java

# 바이트코드 확인
javap -c GenericExample

# 상세한 정보 확인 (제네릭 시그니처 포함)
javap -s -v GenericExample
```

### 예시 출력
```
Signature: #18                          // Ljava/util/List<Ljava/lang/String;>;
```

## 결론

Java Generics는 컴파일 시점에 강력한 타입 안전성을 제공하면서도, 타입 소거를 통해 기존 코드와의 호환성을 유지하는 영리한 설계입니다. 이러한 메커니즘을 이해하면 제네릭을 더 효과적으로 활용할 수 있고, 발생할 수 있는 제약사항들을 미리 파악하여 대응할 수 있습니다.

## 참고 자료
- [Oracle Java Generics Tutorial](https://docs.oracle.com/javase/tutorial/java/generics/)
- [Effective Java 3rd Edition - Item 26-31](https://www.oreilly.com/library/view/effective-java-3rd/9780134686097/)
- [Java Language Specification - Chapter 4.5](https://docs.oracle.com/javase/specs/jls/se17/html/jls-4.html#jls-4.5)