# Spring과 Spring Boot의 차이

## 개요

이 문서에서는 Spring Framework와 Spring Boot의 주요 차이점을 설명합니다. 두 기술 모두 Java 기반 애플리케이션 개발에 널리 사용되지만, 목적과 특징에 있어 중요한 차이가 있습니다.

## Spring Framework

Spring Framework는 Java 엔터프라이즈 애플리케이션 개발을 위한 오픈 소스 프레임워크입니다.

### 주요 특징

- **IoC(Inversion of Control)**: 객체의 생성과 생명주기 관리를 개발자가 아닌 프레임워크가 담당합니다.
- **DI(Dependency Injection)**: 객체 간의 의존성을 외부에서 주입하여 결합도를 낮춥니다.
- **AOP(Aspect-Oriented Programming)**: 횡단 관심사(cross-cutting concerns)를 모듈화하여 코드의 중복을 줄입니다.
- **다양한 모듈**: Spring MVC, Spring Security, Spring Data, Spring Batch 등 다양한 모듈을 제공합니다.

### 설정 방식

Spring Framework는 XML 기반 설정이나 Java 기반 설정(JavaConfig)을 사용합니다:

```java
@Configuration
public class AppConfig {
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/mydb");
        dataSource.setUsername("root");
        dataSource.setPassword("password");
        return dataSource;
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
```

### 장점

- 유연성이 높아 다양한 애플리케이션 개발에 적합합니다.
- 모듈화가 잘 되어 있어 필요한 기능만 선택적으로 사용할 수 있습니다.
- 엔터프라이즈급 애플리케이션 개발에 필요한 대부분의 기능을 제공합니다.

### 단점

- 초기 설정이 복잡하고 많은 보일러플레이트 코드가 필요합니다.
- 의존성 관리가 번거롭습니다.
- 프로젝트 설정에 많은 시간이 소요됩니다.

## Spring Boot

Spring Boot는 Spring Framework를 기반으로 하지만, 더 빠르고 쉽게 Spring 애플리케이션을 개발할 수 있도록 설계되었습니다.

### 주요 특징

- **자동 설정(Auto-configuration)**: 애플리케이션의 의존성과 설정을 자동으로 구성합니다.
- **스타터 의존성(Starter Dependencies)**: 특정 기능을 위한 의존성을 그룹화하여 제공합니다.
- **내장 서버(Embedded Server)**: Tomcat, Jetty, Undertow 등의 서버가 내장되어 있어 별도의 서버 설정이 필요 없습니다.
- **Actuator**: 애플리케이션의 상태 모니터링 및 관리 기능을 제공합니다.
- **Spring Boot CLI**: 명령줄에서 Spring 애플리케이션을 빠르게 개발할 수 있는 도구를 제공합니다.

### 설정 방식

Spring Boot는 `application.properties` 또는 `application.yml` 파일을 통해 간단하게 설정할 수 있습니다:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
```

또한 Java 설정도 더 간단해집니다:

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 장점

- 빠른 개발과 배포가 가능합니다.
- 최소한의 설정으로 애플리케이션을 실행할 수 있습니다.
- 내장 서버를 통해 독립적인 애플리케이션으로 실행됩니다.
- 의존성 관리가 간편합니다.
- 프로덕션 환경에서 바로 사용할 수 있는 기능들을 제공합니다.

### 단점

- 자동 설정으로 인해 내부 동작 방식을 이해하기 어려울 수 있습니다.
- 특정 요구사항에 맞게 커스터마이징하는 데 제약이 있을 수 있습니다.
- 불필요한 의존성이 포함될 수 있어 애플리케이션 크기가 커질 수 있습니다.

## Spring vs Spring Boot 주요 차이점 요약

| 특징 | Spring | Spring Boot |
|------|--------|-------------|
| 설정 | 수동 설정 필요 | 자동 설정 제공 |
| 의존성 관리 | 개발자가 직접 관리 | 스타터 의존성으로 간편하게 관리 |
| 서버 | 외부 서버 필요 | 내장 서버 제공 |
| 배포 | WAR 파일로 배포 | JAR 파일로 독립 실행 가능 |
| 개발 속도 | 상대적으로 느림 | 빠른 개발 가능 |
| 유연성 | 높음 | 상대적으로 제한적 |
| 학습 곡선 | 가파름 | 완만함 |

## 언제 무엇을 사용해야 할까?

### Spring Framework를 선택해야 할 때

- 애플리케이션의 모든 측면을 세밀하게 제어해야 할 때
- 특정 모듈만 필요하고 경량화된 애플리케이션을 원할 때
- 레거시 시스템과의 통합이 필요할 때
- 특정 서버 환경에 배포해야 할 때

### Spring Boot를 선택해야 할 때

- 빠르게 개발하고 배포해야 할 때
- 마이크로서비스 아키텍처를 구현할 때
- 최소한의 설정으로 애플리케이션을 시작하고 싶을 때
- 클라우드 네이티브 애플리케이션을 개발할 때
- 개발 생산성을 높이고 싶을 때

## 결론

Spring Framework와 Spring Boot는 각각 고유한 장점을 가지고 있습니다. Spring은 유연성과 세밀한 제어를 제공하는 반면, Spring Boot는 빠른 개발과 간편한 설정을 제공합니다. 프로젝트의 요구사항과 개발 환경에 따라 적절한 기술을 선택하는 것이 중요합니다. 대부분의 새로운 프로젝트에서는 Spring Boot를 사용하는 것이 개발 생산성 측면에서 유리하지만, 특정 요구사항이나 제약이 있는 경우 Spring Framework를 선택하는 것이 더 적합할 수 있습니다.