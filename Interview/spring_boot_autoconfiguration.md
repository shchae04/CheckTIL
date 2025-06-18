# Spring Boot AutoConfiguration 동작 원리

Spring Boot의 핵심 기능 중 하나인 AutoConfiguration(자동 구성)의 동작 원리에 대해 알아보겠습니다. 자동 구성은 Spring Boot가 개발자의 수고를 덜어주는 "Just Works" 철학을 실현하는 핵심 메커니즘입니다.

## 1. AutoConfiguration이란?

**AutoConfiguration**은 Spring Boot가 클래스패스에 있는 라이브러리, 설정, Bean 정의 등을 기반으로 애플리케이션 구성을 자동으로 설정하는 기능입니다. 이를 통해 개발자는 최소한의 설정만으로 Spring 애플리케이션을 빠르게 구축할 수 있습니다.

> Spring Boot의 자동 구성은 "Convention over Configuration" (설정보다 관례) 원칙을 따릅니다. 즉, 명시적인 설정 없이도 일반적인 사용 사례에 맞게 자동으로 구성됩니다.

## 2. AutoConfiguration 동작 원리

### 2.1 @SpringBootApplication 어노테이션

Spring Boot 애플리케이션의 시작점에는 일반적으로 `@SpringBootApplication` 어노테이션이 사용됩니다. 이 어노테이션은 다음 세 가지 어노테이션을 포함합니다:

```
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan
```

이 중에서 자동 구성을 담당하는 것은 `@EnableAutoConfiguration` 어노테이션입니다.

### 2.2 @EnableAutoConfiguration

`@EnableAutoConfiguration` 어노테이션은 Spring Boot가 클래스패스를 스캔하여 자동 구성 클래스를 찾고 적용하도록 지시합니다. 이 어노테이션은 다음과 같은 작업을 수행합니다:

1. `spring.factories` 파일을 로드
2. `org.springframework.boot.autoconfigure.EnableAutoConfiguration` 키에 해당하는 자동 구성 클래스 목록을 가져옴
3. 조건부 어노테이션(`@ConditionalOn*`)을 평가하여 적용할 구성 결정
4. 적용 가능한 자동 구성 클래스를 Spring 애플리케이션 컨텍스트에 등록

### 2.3 spring.factories 파일

Spring Boot의 자동 구성은 `META-INF/spring.factories` 파일을 통해 이루어집니다. 이 파일은 각 Spring Boot 스타터 및 라이브러리에 포함되어 있으며, 자동 구성 클래스 목록을 정의합니다.

예를 들어, `spring-boot-autoconfigure` 모듈의 `spring.factories` 파일에는 다음과 같은 내용이 포함됩니다:

```properties
# Auto Configure
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration,\
org.springframework.boot.autoconfigure.aop.AopAutoConfiguration,\
org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration,\
org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration,\
org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration,\
...
```

### 2.4 조건부 자동 구성

Spring Boot는 모든 자동 구성을 무조건 적용하지 않고, 특정 조건이 충족될 때만 적용합니다. 이를 위해 다양한 조건부 어노테이션을 사용합니다:

- `@ConditionalOnClass`: 특정 클래스가 클래스패스에 있을 때 구성 적용
- `@ConditionalOnMissingClass`: 특정 클래스가 클래스패스에 없을 때 구성 적용
- `@ConditionalOnBean`: 특정 Bean이 이미 등록되어 있을 때 구성 적용
- `@ConditionalOnMissingBean`: 특정 Bean이 등록되어 있지 않을 때 구성 적용
- `@ConditionalOnProperty`: 특정 프로퍼티 값이 설정되어 있을 때 구성 적용
- `@ConditionalOnWebApplication`: 웹 애플리케이션일 때 구성 적용
- `@ConditionalOnNotWebApplication`: 웹 애플리케이션이 아닐 때 구성 적용

예를 들어, `DataSourceAutoConfiguration` 클래스는 다음과 같이 조건부 어노테이션을 사용합니다:

```java
@Configuration
@ConditionalOnClass({ DataSource.class, EmbeddedDatabaseType.class })
@EnableConfigurationProperties(DataSourceProperties.class)
@Import({ DataSourcePoolMetadataProvidersConfiguration.class, DataSourceInitializationConfiguration.class })
public class DataSourceAutoConfiguration {
    // 구성 내용
}
```

이 구성은 `DataSource` 클래스와 `EmbeddedDatabaseType` 클래스가 클래스패스에 있을 때만 적용됩니다.

## 3. AutoConfiguration 우선순위

여러 자동 구성 클래스 간의 의존성이나 순서가 중요한 경우, Spring Boot는 `@AutoConfigureAfter`, `@AutoConfigureBefore`, `@AutoConfigureOrder` 어노테이션을 사용하여 자동 구성의 순서를 제어합니다.

```java
@Configuration
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
public class JdbcTemplateAutoConfiguration {
    // JdbcTemplate 구성은 DataSource 구성 이후에 적용
}
```

## 4. 자동 구성 커스터마이징

Spring Boot의 자동 구성은 개발자가 명시적으로 정의한 Bean이나 설정을 우선시합니다. 이를 통해 자동 구성을 쉽게 재정의하거나 확장할 수 있습니다.

### 4.1 프로퍼티를 통한 커스터마이징

`application.properties` 또는 `application.yml` 파일을 통해 자동 구성의 동작을 제어할 수 있습니다:

```properties
# 내장 서버 포트 변경
server.port=8081

# 데이터소스 설정
spring.datasource.url=jdbc:mysql://localhost/test
spring.datasource.username=dbuser
spring.datasource.password=dbpass
```

### 4.2 Bean 정의를 통한 커스터마이징

자동 구성이 제공하는 Bean을 직접 정의하여 재정의할 수 있습니다:

```java
@Configuration
public class MyConfiguration {

    @Bean
    public DataSource dataSource() {
        // 커스텀 DataSource 구성
        return new MyCustomDataSource();
    }
}
```

### 4.3 자동 구성 제외하기

특정 자동 구성을 제외하려면 `@SpringBootApplication` 어노테이션의 `exclude` 속성을 사용합니다:

```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

## 5. 자동 구성 디버깅

Spring Boot는 자동 구성의 적용 상태를 확인할 수 있는 방법을 제공합니다. 애플리케이션 실행 시 `--debug` 옵션을 추가하거나 `application.properties`에 `debug=true`를 설정하면 자동 구성 보고서가 출력됩니다:

```
=========================
AUTO-CONFIGURATION REPORT
=========================

Positive matches:
-----------------
   DataSourceAutoConfiguration matched:
      - @ConditionalOnClass found required classes 'javax.sql.DataSource', 'org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType' (OnClassCondition)

Negative matches:
-----------------
   MongoAutoConfiguration:
      Did not match:
         - @ConditionalOnClass did not find required class 'com.mongodb.MongoClient' (OnClassCondition)
```

## 결론

Spring Boot의 AutoConfiguration은 개발자가 복잡한 설정 없이도 Spring 애플리케이션을 빠르게 개발할 수 있게 해주는 강력한 기능입니다. 클래스패스 스캔, 조건부 구성, 우선순위 메커니즘을 통해 필요한 구성만 자동으로 적용하며, 개발자는 필요에 따라 이를 커스터마이징할 수 있습니다.

자동 구성의 원리를 이해하면 Spring Boot 애플리케이션의 동작 방식을 더 깊이 이해할 수 있으며, 문제 해결과 최적화에 도움이 됩니다.

## 참고 자료

- [Spring Boot 공식 문서 - Auto-configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/using-spring-boot.html#using-boot-auto-configuration)
- [Spring Boot 소스 코드 - AutoConfiguration](https://github.com/spring-projects/spring-boot/tree/main/spring-boot-project/spring-boot-autoconfigure)
- [Baeldung - Spring Boot Auto-Configuration](https://www.baeldung.com/spring-boot-autoconfiguration)
