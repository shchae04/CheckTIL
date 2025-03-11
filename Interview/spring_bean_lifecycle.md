# Spring Bean의 생명주기

Spring Framework에서 Bean은 애플리케이션의 핵심을 이루는 객체로, Spring IoC(Inversion of Control) 컨테이너에 의해 관리됩니다. 이 문서에서는 Spring Bean의 생명주기에 대해 알아보겠습니다.

## Spring Bean이란?

**Spring Bean**은 Spring IoC 컨테이너가 관리하는 자바 객체입니다. 일반적인 자바 객체(POJO)와 달리, Bean은 컨테이너에 의해 인스턴스화, 관리, 소멸됩니다.

## Bean 생명주기 개요

Spring Bean의 생명주기는 크게 다음과 같은 단계로 구성됩니다:

1. **인스턴스화(Instantiation)**: Bean 객체 생성
2. **프로퍼티 설정(Populate Properties)**: 의존성 주입
3. **초기화 전 처리(Pre-Initialization)**: Bean 생성 전 처리 작업
4. **초기화(Initialization)**: Bean 초기화
5. **초기화 후 처리(Post-Initialization)**: Bean 생성 후 처리 작업
6. **사용(In Use)**: Bean 사용
7. **소멸(Destruction)**: Bean 소멸

## 상세 생명주기

### 1. 인스턴스화

Spring 컨테이너는 Bean 정의를 읽고 Java Reflection API를 사용하여 Bean 객체를 생성합니다.

```java
// XML 설정 예시
// <bean id="exampleBean" class="com.example.ExampleBean"/>

// Java 설정
@Bean
public ExampleBean exampleBean() {
    return new ExampleBean();
}
```

### 2. 프로퍼티 설정 (의존성 주입)

생성된 Bean 객체에 필요한 의존성을 주입합니다. 이는 생성자 주입, 세터 주입, 필드 주입 등의 방식으로 이루어집니다.

```java
// 생성자 주입
@Bean
public ExampleBean exampleBean(DependencyBean dependency) {
    return new ExampleBean(dependency);
}

// 세터 주입
@Bean
public ExampleBean exampleBean() {
    ExampleBean bean = new ExampleBean();
    bean.setDependency(dependency());
    return bean;
}
```

### 3. 초기화 전 처리

Bean이 초기화되기 전에 특정 작업을 수행할 수 있습니다. 이는 `BeanPostProcessor`의 `postProcessBeforeInitialization` 메소드를 구현하여 처리합니다.

```java
public class CustomBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        // 초기화 전 처리 로직
        return bean;
    }
}
```

### 4. 초기화

Bean 초기화는 다음과 같은 방법으로 수행할 수 있습니다:

1. **InitializingBean 인터페이스 구현**:
   ```java
   public class ExampleBean implements InitializingBean {
       @Override
       public void afterPropertiesSet() throws Exception {
           // 초기화 로직
       }
   }
   ```

2. **@PostConstruct 어노테이션 사용**:
   ```java
   public class ExampleBean {
       @PostConstruct
       public void init() {
           // 초기화 로직
       }
   }
   ```

3. **Bean 정의에서 init-method 지정**:
   ```java
   @Bean(initMethod = "init")
   public ExampleBean exampleBean() {
       return new ExampleBean();
   }
   ```

### 5. 초기화 후 처리

Bean이 초기화된 후 추가 작업을 수행할 수 있습니다. 이는 `BeanPostProcessor`의 `postProcessAfterInitialization` 메소드를 구현하여 처리합니다.

```java
public class CustomBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        // 초기화 후 처리 로직
        return bean;
    }
}
```

### 6. 사용

초기화가 완료된 Bean은 애플리케이션에서 사용됩니다. 이 단계에서 Bean은 완전히 구성되어 정상적으로 작동합니다.

### 7. 소멸

애플리케이션 종료 시 Spring 컨테이너는 Bean을 소멸시킵니다. Bean 소멸 시 정리 작업은 다음과 같은 방법으로 수행할 수 있습니다:

1. **DisposableBean 인터페이스 구현**:
   ```java
   public class ExampleBean implements DisposableBean {
       @Override
       public void destroy() throws Exception {
           // 소멸 시 정리 로직
       }
   }
   ```

2. **@PreDestroy 어노테이션 사용**:
   ```java
   public class ExampleBean {
       @PreDestroy
       public void cleanup() {
           // 소멸 시 정리 로직
       }
   }
   ```

3. **Bean 정의에서 destroy-method 지정**:
   ```java
   @Bean(destroyMethod = "cleanup")
   public ExampleBean exampleBean() {
       return new ExampleBean();
   }
   ```

## 생명주기 콜백 메소드 실행 순서

Bean 생명주기 콜백 메소드의 실행 순서는 다음과 같습니다:

1. `@PostConstruct` 어노테이션이 적용된 메소드
2. `InitializingBean` 인터페이스의 `afterPropertiesSet()` 메소드
3. Bean 정의에 지정된 `init-method`
4. `@PreDestroy` 어노테이션이 적용된 메소드
5. `DisposableBean` 인터페이스의 `destroy()` 메소드
6. Bean 정의에 지정된 `destroy-method`

## 주의사항

> **중요**: Bean 생명주기 콜백 메소드에서 예외가 발생하면 Bean 생성 과정이 중단될 수 있습니다. 따라서 콜백 메소드에서는 예외 처리를 적절히 해주는 것이 좋습니다.

## 결론

Spring Bean의 생명주기를 이해하면 애플리케이션의 초기화와 정리 작업을 효과적으로 관리할 수 있습니다. 특히 리소스 할당과 해제, 데이터베이스 연결 관리 등의 작업에서 생명주기 콜백을 활용하면 안정적인 애플리케이션을 구현할 수 있습니다.
