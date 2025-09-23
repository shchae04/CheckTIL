# 자바에서 클래스 정보 조회: Reflection API 완전 가이드

## 1. 한 줄 정의
- Java Reflection API는 런타임에 클래스의 메타데이터(필드, 메서드, 생성자, 어노테이션 등)를 동적으로 조회하고 조작할 수 있는 기능으로, 컴파일 타임에 알 수 없는 클래스 정보를 프로그램 실행 중에 탐색할 수 있게 해주는 강력한 도구이다.

---

## 2. 클래스 정보 조회 6단계 방법

### 2-1. 1단계: Class 객체 획득
- **개념**: 클래스 정보에 접근하기 위한 시작점인 Class 객체를 얻는 단계
- **백엔드 관점**: 클래스 로더가 메모리에 로드한 클래스 메타데이터에 대한 참조 획득
- **핵심 포인트**:
  - `.class` 리터럴 사용
  - `getClass()` 메서드 호출
  - `Class.forName()` 동적 로딩

```java
// Class 객체 획득 방법들
Class<?> clazz1 = String.class;                    // 컴파일 타임에 알려진 클래스
Class<?> clazz2 = "hello".getClass();              // 객체 인스턴스로부터
Class<?> clazz3 = Class.forName("java.lang.String"); // 동적 로딩

// 기본 타입과 배열
Class<?> intClass = int.class;
Class<?> arrayClass = int[].class;
```

### 2-2. 2단계: 기본 클래스 정보 조회
- **개념**: 클래스의 기본적인 메타데이터(이름, 패키지, 수정자 등) 확인
- **백엔드 관점**: 클래스 파일의 헤더 정보와 같은 메타데이터 접근
- **핵심 포인트**:
  - 클래스명, 패키지명 조회
  - 접근 제한자(public, abstract, final 등) 확인
  - 상속 관계 파악

```java
public class ClassInfoExample {
    public static void getBasicClassInfo(Class<?> clazz) {
        // 기본 정보
        System.out.println("클래스명: " + clazz.getSimpleName());
        System.out.println("전체 클래스명: " + clazz.getName());
        System.out.println("패키지: " + clazz.getPackage().getName());

        // 수정자 정보
        int modifiers = clazz.getModifiers();
        System.out.println("public: " + Modifier.isPublic(modifiers));
        System.out.println("abstract: " + Modifier.isAbstract(modifiers));
        System.out.println("final: " + Modifier.isFinal(modifiers));

        // 상속 관계
        System.out.println("부모 클래스: " + clazz.getSuperclass());
        System.out.println("구현 인터페이스: " + Arrays.toString(clazz.getInterfaces()));
    }
}
```

### 2-3. 3단계: 필드(Field) 정보 조회
- **개념**: 클래스에 선언된 모든 필드의 정보를 조회하고 접근
- **백엔드 관점**: 객체의 상태를 나타내는 멤버 변수들의 메타데이터 탐색
- **핵심 포인트**:
  - `getFields()`: public 필드만 조회
  - `getDeclaredFields()`: 모든 접근 제한자의 필드 조회
  - 필드 타입, 이름, 값 접근

```java
public class FieldInfoExample {
    private String name;
    public int age;
    protected double salary;

    public static void getFieldInfo(Object obj) throws Exception {
        Class<?> clazz = obj.getClass();

        // 모든 필드 조회 (private 포함)
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            System.out.println("=== 필드 정보 ===");
            System.out.println("필드명: " + field.getName());
            System.out.println("타입: " + field.getType().getSimpleName());
            System.out.println("접근 제한자: " + Modifier.toString(field.getModifiers()));

            // private 필드 접근을 위한 설정
            field.setAccessible(true);

            // 필드 값 조회
            Object value = field.get(obj);
            System.out.println("현재 값: " + value);

            // 필드 값 변경
            if (field.getType() == String.class) {
                field.set(obj, "새로운 값");
            }
        }
    }
}
```

### 2-4. 4단계: 메서드(Method) 정보 조회
- **개념**: 클래스에 정의된 메서드들의 시그니처와 실행 가능한 정보 탐색
- **백엔드 관점**: 클래스의 행동을 정의하는 함수들의 메타데이터와 동적 호출
- **핵심 포인트**:
  - 메서드 시그니처 분석 (이름, 매개변수, 반환타입)
  - 동적 메서드 호출 (`invoke()`)
  - 오버로딩된 메서드 구분

```java
public class MethodInfoExample {
    public String getName() { return "example"; }
    private void setName(String name) { this.name = name; }
    public static int calculate(int a, int b) { return a + b; }

    public static void getMethodInfo(Object obj) throws Exception {
        Class<?> clazz = obj.getClass();

        // 모든 메서드 조회
        Method[] methods = clazz.getDeclaredMethods();

        for (Method method : methods) {
            System.out.println("=== 메서드 정보 ===");
            System.out.println("메서드명: " + method.getName());
            System.out.println("반환타입: " + method.getReturnType().getSimpleName());

            // 매개변수 정보
            Parameter[] parameters = method.getParameters();
            System.out.println("매개변수 개수: " + parameters.length);
            for (Parameter param : parameters) {
                System.out.println("  - " + param.getType().getSimpleName() + " " + param.getName());
            }

            // 정적 메서드와 인스턴스 메서드 구분
            boolean isStatic = Modifier.isStatic(method.getModifiers());
            System.out.println("정적 메서드: " + isStatic);

            // 메서드 동적 호출 예시
            method.setAccessible(true);
            if (method.getName().equals("getName") && parameters.length == 0) {
                Object result = method.invoke(obj);
                System.out.println("호출 결과: " + result);
            }
        }
    }
}
```

### 2-5. 5단계: 생성자(Constructor) 정보 조회
- **개념**: 객체 생성을 위한 생성자들의 정보 조회 및 동적 인스턴스 생성
- **백엔드 관점**: 팩토리 패턴 구현이나 DI 컨테이너에서 객체 생성 시 활용
- **핵심 포인트**:
  - 생성자 매개변수 분석
  - 동적 객체 생성 (`newInstance()`)
  - 싱글톤 패턴과의 조합

```java
public class ConstructorInfoExample {
    private String name;
    private int age;

    public ConstructorInfoExample() {}

    public ConstructorInfoExample(String name) {
        this.name = name;
    }

    public ConstructorInfoExample(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public static void getConstructorInfo(Class<?> clazz) throws Exception {
        // 모든 생성자 조회
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();

        for (Constructor<?> constructor : constructors) {
            System.out.println("=== 생성자 정보 ===");
            System.out.println("매개변수 개수: " + constructor.getParameterCount());

            // 매개변수 타입 정보
            Class<?>[] paramTypes = constructor.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                System.out.println("매개변수 " + (i+1) + ": " + paramTypes[i].getSimpleName());
            }

            // 동적 객체 생성 예시
            if (constructor.getParameterCount() == 2) {
                Object instance = constructor.newInstance("동적생성", 25);
                System.out.println("생성된 인스턴스: " + instance);
            }
        }
    }
}
```

### 2-6. 6단계: 어노테이션(Annotation) 정보 조회
- **개념**: 클래스, 필드, 메서드에 붙은 어노테이션 메타데이터 분석
- **백엔드 관점**: 스프링의 DI, AOP, 검증 프레임워크의 핵심 기술
- **핵심 포인트**:
  - 런타임 어노테이션 조회 (`@Retention(RetentionPolicy.RUNTIME)`)
  - 어노테이션 속성값 접근
  - 커스텀 어노테이션 처리

```java
// 커스텀 어노테이션 정의
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
@interface MyAnnotation {
    String value() default "";
    int priority() default 0;
}

public class AnnotationInfoExample {
    @MyAnnotation(value = "중요한 필드", priority = 1)
    private String importantField;

    @MyAnnotation(value = "중요한 메서드", priority = 2)
    public void importantMethod() {}

    public static void getAnnotationInfo(Class<?> clazz) throws Exception {
        // 클래스 레벨 어노테이션
        Annotation[] classAnnotations = clazz.getAnnotations();
        System.out.println("클래스 어노테이션 개수: " + classAnnotations.length);

        // 필드 어노테이션 조회
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            MyAnnotation annotation = field.getAnnotation(MyAnnotation.class);
            if (annotation != null) {
                System.out.println("=== 필드 어노테이션 ===");
                System.out.println("필드명: " + field.getName());
                System.out.println("어노테이션 값: " + annotation.value());
                System.out.println("우선순위: " + annotation.priority());
            }
        }

        // 메서드 어노테이션 조회
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(MyAnnotation.class)) {
                MyAnnotation annotation = method.getAnnotation(MyAnnotation.class);
                System.out.println("=== 메서드 어노테이션 ===");
                System.out.println("메서드명: " + method.getName());
                System.out.println("어노테이션 값: " + annotation.value());
                System.out.println("우선순위: " + annotation.priority());
            }
        }
    }
}
```

---

## 3. 백엔드 개발자 관점에서의 실무 활용

### 3-1. 프레임워크에서의 활용
- **Spring Framework**: DI 컨테이너, AOP, 어노테이션 기반 설정
- **JPA/Hibernate**: 엔티티 매핑, 필드 접근 전략
- **Jackson**: JSON 직렬화/역직렬화 시 필드 매핑
- **JUnit**: 테스트 메서드 자동 발견 및 실행

### 3-2. 디자인 패턴에서의 활용
- **팩토리 패턴**: 동적 객체 생성
- **프록시 패턴**: 메서드 호출 가로채기 (AOP)
- **빌더 패턴**: 동적 setter 메서드 호출
- **전략 패턴**: 런타임 알고리즘 선택

### 3-3. 성능 고려사항
- **캐싱**: Class 객체와 Method 객체는 재사용 필수
- **보안**: `setAccessible(true)` 사용 시 보안 정책 고려
- **메모리**: Reflection 메타데이터는 메모리 오버헤드 발생

---

## 4. 실무 예제: 간단한 DI 컨테이너 구현

```java
// 의존성 주입을 위한 어노테이션
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@interface Inject {
}

// 간단한 DI 컨테이너
public class SimpleDIContainer {
    private Map<Class<?>, Object> instances = new HashMap<>();

    public <T> T getInstance(Class<T> clazz) throws Exception {
        if (instances.containsKey(clazz)) {
            return (T) instances.get(clazz);
        }

        // 기본 생성자로 인스턴스 생성
        T instance = clazz.getDeclaredConstructor().newInstance();

        // @Inject 어노테이션이 붙은 필드에 의존성 주입
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true);
                Object dependency = getInstance(field.getType());
                field.set(instance, dependency);
            }
        }

        instances.put(clazz, instance);
        return instance;
    }
}

// 사용 예제
class UserService {
    @Inject
    private UserRepository userRepository;

    public void saveUser(String name) {
        userRepository.save(name);
    }
}

class UserRepository {
    public void save(String name) {
        System.out.println("사용자 저장: " + name);
    }
}
```

---

## 5. 예상 면접 질문

### 5-1. 기술적 질문
1. Reflection API의 성능 이슈와 최적화 방법은?
2. `getFields()`와 `getDeclaredFields()`의 차이점은?
3. `setAccessible(true)`의 보안적 의미와 주의사항은?

### 5-2. 실무 활용 질문
1. Spring에서 @Autowired는 어떻게 동작하나요?
2. JPA에서 엔티티의 private 필드에 어떻게 값을 설정하나요?
3. 커스텀 어노테이션을 만들어 AOP를 구현하는 방법은?

### 5-3. 문제 해결 질문
1. Reflection을 사용할 때 발생할 수 있는 예외들과 처리 방법은?
2. 동적 프록시와 Reflection의 관계는?
3. Class.forName()과 ClassLoader의 차이점은?

---

## 6. 핵심 요약

### 6-1. 주요 특징
- **런타임 메타데이터 접근**: 컴파일 타임에 알 수 없는 정보를 실행 중에 조회
- **동적 처리**: 클래스, 메서드, 필드에 대한 동적 접근 및 조작
- **프레임워크의 기반**: Spring, Hibernate 등 대부분의 자바 프레임워크의 핵심 기술

### 6-2. 백엔드 개발자의 핵심 이해사항
- Reflection은 메타프로그래밍을 가능하게 하는 강력한 도구이다
- 성능 오버헤드가 있으므로 캐싱과 최적화가 필수이다
- 보안 정책과 캡슐화 원칙을 고려한 신중한 사용이 필요하다

### 6-3. 실무 적용 포인트
- 프레임워크 개발이나 라이브러리 제작 시 핵심 기술
- 설정 기반 프로그래밍과 어노테이션 처리의 기반
- 테스트 도구와 개발 도구에서 광범위하게 활용

### 6-4. 성능 최적화 팁
```java
// BAD: 매번 새로 조회
public void badExample() throws Exception {
    Method method = MyClass.class.getDeclaredMethod("myMethod");
    method.invoke(instance);
}

// GOOD: 캐싱 활용
private static final Method CACHED_METHOD;
static {
    try {
        CACHED_METHOD = MyClass.class.getDeclaredMethod("myMethod");
        CACHED_METHOD.setAccessible(true);
    } catch (Exception e) {
        throw new RuntimeException(e);
    }
}

public void goodExample() throws Exception {
    CACHED_METHOD.invoke(instance);
}
```