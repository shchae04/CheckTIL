# 동일성과 동등성
**동일성과 동등성**은 객체를 비교할 때 중요한 개념입니다. 자바에서는 이 두 개념을 **`equals()` 메서드**와 **`==` 연산자**를 통해 구분할 수 있습니다.

---

### **equals()와 ==의 차이는 무엇인가요?**
- **`equals()`** 는 **객체의 내용을 비교**합니다.
- **`==`** 는 **객체의 참조(레퍼런스)를 비교**합니다.

따라서 두 객체의 **내용이 같더라도** 서로 다른 객체라면,  
**`equals()`** 결과는 `true`가 될 수 있지만 **`==`** 결과는 `false`가 됩니다.

---

### **동등성(Equality)은 뭔가요?**
동등성은 **논리적으로 객체의 내용이 같은지**를 비교하는 개념입니다.  
자바에서는 **`equals()`** 메서드를 사용하여 객체의 동등성을 비교합니다.  
아래 **Apple** 클래스를 예시로 보면, **`Object.equals`** 메서드를 **오버라이딩**하여  
객체의 **실제 데이터**를 비교하도록 했습니다.  
그래서 `apple`과 `anotherApple`은 **서로 다른 객체**이지만, **무게가 같기 때문에**  
동등성 비교 결과 `true`가 반환됩니다.
``` java
    public class Apple {

        private final int weight;

        public Apple(int weight) {
            this.weight = weight;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Apple apple = (Apple) o;
            return weight == apple.weight;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(weight);
        }

        public static void main(String[] args) {
            Apple apple = new Apple(100);
            Apple anotherApple = new Apple(100);

            System.out.println(apple.equals(anotherApple)); // true
        }
    }
```
### equals() 메서드를 오버라이딩 하는 이유
```java
    public class Object {
        public boolean equals(Object obj) {
            return (this == obj);
        }
    }
```

Object 클래스의 `equals()` 메서드는 `==` 연산자를 사용하여 동일성을 비교합니다. 그리고 모든 클래스는 Object 클래스를 상속하여 동일성 비교를 기본으로 동작하기 때문에, 동등성 비교가 필요한 클래스에서 필요에 맞게 `equals` & `hashCode` 메서드를 오버라이딩해야 합니다.

### 동일성(Identity)은 뭔가요?
동일성은 두 객체가 메모리 상에서 같은 객체인지 비교하는 개념입니다. 자바에서는 `==` 연산자를 사용하여 객체의 동일성을 비교합니다. `==` 연산자는 객체의 레퍼런스(참조)를 비교하므로, 두 변수가 동일한 객체를 가리키고 있는지를 확인합니다.

    public static void main(String[] args) {
        Apple apple1 = new Apple(100);
        Apple apple2 = new Apple(100);
        Apple apple3 = apple1;

        System.out.println(apple1 == apple2); // false
        System.out.println(apple1 == apple3); // true
    }

apple1과 apple2는 참조가 다르기 때문에 == 연산 결과 false가 반환되지만, apple1의 참조를 가지는 apple3은 == 연산 결과 true를 반환합니다.

### String은 객체인데 == 비교해도 되던데 어떻게 된건가요?
문자열 리터럴은 문자열 상수 풀(**String Constant Pool**) 에 저장되기 때문에, 동일한 문자열 리터럴을 참조하면 `==` 연산자가 `true`를 반환할 수 있습니다. 하지만 `new` 키워드를 사용하여 문자열을 생성하면 새로운 객체가 생성되므로 `==` 연산자가 `false`를 반환할 수 있습니다. 따라서 문자열 비교 시 항상 `equals()` 메서드를 사용한 동등성 비교를 하는 것이 좋습니다.

    public class StringComparison {
        public static void main(String[] args) {
            String str1 = "안녕하세요";
            String str2 = "안녕하세요";
            String str3 = new String("안녕하세요");
            
            // 동일성 비교
            System.out.println(str1 == str2); // true
            System.out.println(str1 == str3); // false
            
            // 동등성 비교
            System.out.println(str1.equals(str2)); // true
            System.out.println(str1.equals(str3)); // true
        }
    }

    // String.class equals 오버라이딩 되어있음.
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        return (anObject instanceof String aString)
                && (!COMPACT_STRINGS || this.coder == aString.coder)
                && StringLatin1.equals(value, aString.value);
    }

### Wrapper 클래스 비교법
래퍼 클래스(`Integer`, `Long`, ...)도 객체이므로 `==` 연산자는 참조를 비교합니다.
값 비교를 원할 경우에는 반드시 `equals()` 메서드를 사용해야 합니다.
단, 자바는 **-128 ~ 127** 범위의 래퍼 객체를 캐싱하기 때문에,
이 범위 내의 같은 값은 같은 객체를 참조할 수도 있습니다.
하지만 일반적으로는 `equals()`를 사용하는 것이 안전합니다.