# Comparator와 Comparable의 차이점

Java에서 객체를 정렬하는 방법에는 `Comparable`과 `Comparator` 두 가지 인터페이스가 있습니다. 이 두 인터페이스는 비슷한 목적을 가지고 있지만, 사용 방법과 적용 상황에 차이가 있습니다.

## 목차
1. [Comparable 인터페이스](#comparable-인터페이스)
2. [Comparator 인터페이스](#comparator-인터페이스)
3. [주요 차이점](#주요-차이점)
4. [사용 예시](#사용-예시)
5. [언제 무엇을 사용해야 할까?](#언제-무엇을-사용해야-할까)

## Comparable 인터페이스

`Comparable` 인터페이스는 객체의 "자연적인 순서(natural ordering)"를 정의합니다.

### 특징
- `java.lang` 패키지에 포함되어 있음
- 단 하나의 메서드 `compareTo(T o)`를 가짐
- 객체 자신과 매개변수 객체를 비교
- 클래스가 이 인터페이스를 구현하면 해당 클래스의 객체들은 자연스러운 순서를 가짐

### 구현 방법
```java
public class Student implements Comparable<Student> {
    private String name;
    private int age;
    
    // 생성자, getter, setter 생략
    
    @Override
    public int compareTo(Student other) {
        // 나이를 기준으로 오름차순 정렬
        return this.age - other.age;
        
        // 또는 이름을 기준으로 정렬
        // return this.name.compareTo(other.name);
    }
}
```

### 사용 방법
```java
List<Student> students = new ArrayList<>();
// 학생 객체 추가...
Collections.sort(students); // Comparable의 compareTo 메서드를 사용하여 정렬
```

## Comparator 인터페이스

`Comparator` 인터페이스는 특정 기준에 따라 두 객체를 비교하는 방법을 정의합니다.

### 특징
- `java.util` 패키지에 포함되어 있음
- 주요 메서드는 `compare(T o1, T o2)`
- 두 객체를 매개변수로 받아 비교
- 클래스 외부에서 정렬 기준을 제공할 때 사용
- 여러 가지 정렬 기준을 제공할 수 있음

### 구현 방법
```java
public class AgeComparator implements Comparator<Student> {
    @Override
    public int compare(Student s1, Student s2) {
        return s1.getAge() - s2.getAge();
    }
}

public class NameComparator implements Comparator<Student> {
    @Override
    public int compare(Student s1, Student s2) {
        return s1.getName().compareTo(s2.getName());
    }
}
```

### 사용 방법
```java
List<Student> students = new ArrayList<>();
// 학생 객체 추가...

// 나이 기준 정렬
Collections.sort(students, new AgeComparator());

// 이름 기준 정렬
Collections.sort(students, new NameComparator());

// Java 8 람다 표현식 사용
Collections.sort(students, (s1, s2) -> s1.getAge() - s2.getAge());
Collections.sort(students, Comparator.comparing(Student::getName));
```

## 주요 차이점

| 특성 | Comparable | Comparator |
|------|------------|------------|
| 패키지 | java.lang | java.util |
| 메서드 | compareTo(T o) | compare(T o1, T o2) |
| 구현 위치 | 정렬할 클래스 내부 | 별도의 클래스 |
| 정렬 기준 | 단일 기준 (자연 순서) | 다중 기준 가능 |
| 클래스 수정 | 원본 클래스 수정 필요 | 원본 클래스 수정 불필요 |
| 사용 시점 | 기본 정렬 순서가 필요할 때 | 다양한 정렬 기준이 필요하거나 원본 클래스를 수정할 수 없을 때 |

## 사용 예시

### Comparable 사용 예시
Java의 많은 클래스들이 이미 Comparable을 구현하고 있습니다:
- String: 사전식 순서로 비교
- Integer, Double 등의 숫자 타입: 숫자 값으로 비교
- Date, LocalDate: 시간 순서로 비교

```java
// String은 Comparable을 구현하고 있어 자연스럽게 정렬됨
List<String> names = Arrays.asList("Charlie", "Alice", "Bob");
Collections.sort(names);
System.out.println(names); // [Alice, Bob, Charlie]

// Integer도 Comparable을 구현하고 있음
List<Integer> numbers = Arrays.asList(5, 2, 8, 1);
Collections.sort(numbers);
System.out.println(numbers); // [1, 2, 5, 8]
```

### Comparator 사용 예시
```java
List<Student> students = Arrays.asList(
    new Student("Alice", 22),
    new Student("Bob", 20),
    new Student("Charlie", 21)
);

// 나이 기준 오름차순 정렬
students.sort(Comparator.comparing(Student::getAge));

// 이름 기준 내림차순 정렬
students.sort(Comparator.comparing(Student::getName).reversed());

// 복합 정렬: 나이로 정렬 후 같은 나이면 이름으로 정렬
students.sort(Comparator.comparing(Student::getAge)
                        .thenComparing(Student::getName));
```

## 언제 무엇을 사용해야 할까?

### Comparable을 사용해야 할 때
1. 클래스에 "자연적인 순서"가 있을 때 (예: 알파벳 순서, 숫자 크기 순서)
2. 해당 클래스의 객체들이 대부분 이 순서로 정렬될 때
3. 클래스의 소스 코드를 수정할 수 있을 때

### Comparator를 사용해야 할 때
1. 여러 가지 정렬 기준이 필요할 때
2. 정렬 기준이 상황에 따라 동적으로 변경될 때
3. 원본 클래스의 소스 코드를 수정할 수 없을 때 (라이브러리 클래스 등)
4. 정렬 로직을 클래스와 분리하고 싶을 때

## 결론

`Comparable`과 `Comparator`는 모두 객체 정렬에 사용되지만, 각각 다른 상황에서 유용합니다. `Comparable`은 클래스 자체에 기본 정렬 순서를 정의할 때 사용하고, `Comparator`는 다양한 정렬 기준을 외부에서 제공하고자 할 때 사용합니다. 두 인터페이스를 적절히 활용하면 객체 컬렉션을 효과적으로 정렬할 수 있습니다.