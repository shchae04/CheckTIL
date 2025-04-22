# Spring Boot 로그 레벨 관리

## 목차
1. [Spring Boot 로깅 프레임워크](#1-spring-boot-로깅-프레임워크)
   - [Spring Boot의 기본 로깅 시스템](#spring-boot의-기본-로깅-시스템)
   - [지원하는 로깅 프레임워크](#지원하는-로깅-프레임워크)
2. [로그 레벨 설정 방법](#2-로그-레벨-설정-방법)
   - [application.properties 파일 사용](#applicationproperties-파일-사용)
   - [application.yml 파일 사용](#applicationyml-파일-사용)
   - [로깅 설정 파일 사용](#로깅-설정-파일-사용)
3. [패키지/클래스별 로그 레벨 설정](#3-패키지클래스별-로그-레벨-설정)
   - [특정 패키지 로그 레벨 설정](#특정-패키지-로그-레벨-설정)
   - [특정 클래스 로그 레벨 설정](#특정-클래스-로그-레벨-설정)
4. [런타임에 로그 레벨 변경](#4-런타임에-로그-레벨-변경)
   - [Actuator를 이용한 동적 로그 레벨 변경](#actuator를-이용한-동적-로그-레벨-변경)
   - [프로그래밍 방식으로 로그 레벨 변경](#프로그래밍-방식으로-로그-레벨-변경)
5. [로그 레벨 관리 모범 사례](#5-로그-레벨-관리-모범-사례)
   - [환경별 로그 레벨 전략](#환경별-로그-레벨-전략)
   - [효과적인 로그 레벨 사용 방법](#효과적인-로그-레벨-사용-방법)

## 1. Spring Boot 로깅 프레임워크

### Spring Boot의 기본 로깅 시스템

Spring Boot는 기본적으로 Commons Logging API를 사용하여 내부 로깅을 수행합니다. 그러나 실제 로깅 구현체로는 기본적으로 Logback을 사용합니다. Spring Boot의 스타터 의존성(`spring-boot-starter`)에는 `spring-boot-starter-logging`이 포함되어 있어 별도의 설정 없이도 로깅 기능을 사용할 수 있습니다.

### 지원하는 로깅 프레임워크

Spring Boot는 다음과 같은 로깅 프레임워크를 지원합니다:

1. **Logback** (기본)
   - Spring Boot의 기본 로깅 구현체
   - 높은 성능과 유연한 설정 제공

2. **Log4j2**
   - 높은 성능과 비동기 로깅 지원
   - 사용하려면 `spring-boot-starter-log4j2`로 의존성 변경 필요

3. **Java Util Logging (JUL)**
   - JDK에 내장된 로깅 시스템
   - 별도의 의존성 없이 사용 가능

## 2. 로그 레벨 설정 방법

Spring Boot에서는 여러 방법으로 로그 레벨을 설정할 수 있습니다.

### application.properties 파일 사용

`application.properties` 파일에서 로그 레벨을 설정하는 방법은 다음과 같습니다:

```properties
# 루트 로그 레벨 설정
logging.level.root=WARN

# 특정 패키지의 로그 레벨 설정
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate=ERROR
logging.level.com.myapp=INFO
```

### application.yml 파일 사용

`application.yml` 파일을 사용하는 경우 다음과 같이 설정할 수 있습니다:

```yaml
logging:
  level:
    root: WARN
    org:
      springframework:
        web: DEBUG
        security: DEBUG
      hibernate: ERROR
    com:
      myapp: INFO
```

### 로깅 설정 파일 사용

각 로깅 프레임워크별 설정 파일을 사용하여 더 상세한 로깅 설정을 할 수 있습니다:

1. **Logback** (logback-spring.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/base.xml"/>
    
    <logger name="org.springframework.web" level="DEBUG"/>
    <logger name="org.hibernate" level="ERROR"/>
    <logger name="com.myapp" level="INFO"/>
    
    <root level="WARN">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

2. **Log4j2** (log4j2-spring.xml)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Logger name="org.springframework.web" level="debug" additivity="false">
            <AppenderRef ref="Console"/>
        </Logger>
        <Logger name="org.hibernate" level="error" additivity="false">
            <AppenderRef ref="Console"/>
        </Logger>
        <Root level="warn">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

## 3. 패키지/클래스별 로그 레벨 설정

### 특정 패키지 로그 레벨 설정

특정 패키지의 로그 레벨을 설정하려면 패키지 경로를 지정하면 됩니다:

```properties
# 특정 패키지의 로그 레벨 설정
logging.level.com.myapp.service=DEBUG
logging.level.com.myapp.repository=TRACE
logging.level.com.myapp.controller=INFO
```

이렇게 설정하면 해당 패키지와 그 하위 패키지의 모든 클래스에 로그 레벨이 적용됩니다.

### 특정 클래스 로그 레벨 설정

특정 클래스의 로그 레벨을 설정하려면 클래스의 전체 경로를 지정합니다:

```properties
# 특정 클래스의 로그 레벨 설정
logging.level.com.myapp.service.UserService=DEBUG
logging.level.com.myapp.controller.AuthController=TRACE
```

## 4. 런타임에 로그 레벨 변경

### Actuator를 이용한 동적 로그 레벨 변경

Spring Boot Actuator를 사용하면 애플리케이션 실행 중에도 로그 레벨을 동적으로 변경할 수 있습니다:

1. **의존성 추가**

```xml
<!-- Maven -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

또는

```gradle
// Gradle
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

2. **Actuator 엔드포인트 활성화**

```properties
# application.properties
management.endpoints.web.exposure.include=loggers
management.endpoint.loggers.enabled=true
```

3. **로그 레벨 변경 API 호출**

```bash
# 로그 레벨 조회
curl -X GET http://localhost:8080/actuator/loggers/com.myapp.service

# 로그 레벨 변경
curl -X POST -H "Content-Type: application/json" -d '{"configuredLevel": "DEBUG"}' http://localhost:8080/actuator/loggers/com.myapp.service
```

### 프로그래밍 방식으로 로그 레벨 변경

코드에서 직접 로그 레벨을 변경하는 방법도 있습니다:

```java
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LogController {

    @PostMapping("/logs/{package}/{level}")
    public String changeLogLevel(@PathVariable String packageName, @PathVariable String level) {
        Logger logger = (Logger) LoggerFactory.getLogger(packageName);
        
        switch (level.toUpperCase()) {
            case "DEBUG":
                logger.setLevel(Level.DEBUG);
                break;
            case "INFO":
                logger.setLevel(Level.INFO);
                break;
            case "WARN":
                logger.setLevel(Level.WARN);
                break;
            case "ERROR":
                logger.setLevel(Level.ERROR);
                break;
            case "TRACE":
                logger.setLevel(Level.TRACE);
                break;
            default:
                return "Invalid log level: " + level;
        }
        
        return "Log level for " + packageName + " set to " + level;
    }
}
```

## 5. 로그 레벨 관리 모범 사례

### 환경별 로그 레벨 전략

다양한 환경에 따라 적절한 로그 레벨을 설정하는 것이 중요합니다:

1. **개발 환경 (Development)**
   - 더 상세한 로깅을 위해 DEBUG 또는 TRACE 레벨 사용
   - 개발자가 문제를 빠르게 진단할 수 있도록 함

```properties
# application-dev.properties
logging.level.root=INFO
logging.level.com.myapp=DEBUG
logging.level.org.springframework=DEBUG
```

2. **테스트 환경 (Test)**
   - 테스트 실행 시 필요한 정보만 로깅
   - 테스트 결과에 집중할 수 있도록 INFO 또는 WARN 레벨 사용

```properties
# application-test.properties
logging.level.root=WARN
logging.level.com.myapp=INFO
```

3. **운영 환경 (Production)**
   - 성능 최적화를 위해 INFO 또는 WARN 레벨 사용
   - 중요한 오류와 경고만 로깅하여 디스크 공간 절약

```properties
# application-prod.properties
logging.level.root=WARN
logging.level.com.myapp=INFO
logging.level.org.springframework=WARN
```

### 효과적인 로그 레벨 사용 방법

각 로그 레벨의 적절한 사용 방법:

1. **ERROR**: 애플리케이션이 더 이상 작동할 수 없는 심각한 문제
   - 예: 데이터베이스 연결 실패, 중요 서비스 불능 상태

2. **WARN**: 잠재적인 문제이지만 애플리케이션은 계속 작동 가능
   - 예: 설정 파일 누락, 재시도 성공한 작업, 성능 저하

3. **INFO**: 일반적인 애플리케이션 진행 상황
   - 예: 애플리케이션 시작/종료, 주요 비즈니스 프로세스 완료

4. **DEBUG**: 개발 및 문제 해결에 유용한 상세 정보
   - 예: 메서드 호출 흐름, 변수 값, SQL 쿼리

5. **TRACE**: 가장 상세한 정보
   - 예: 루프 반복, 매우 상세한 진단 정보

로그 레벨을 효과적으로 사용하면 애플리케이션 모니터링과 문제 해결이 용이해지며, 성능에 미치는 영향도 최소화할 수 있습니다.

```java
// 로그 레벨 사용 예시
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    public User findUser(Long id) {
        logger.trace("findUser 메서드 호출: id={}", id); // 매우 상세한 정보
        
        logger.debug("사용자 조회 시작: id={}", id); // 개발 디버깅용
        
        User user = userRepository.findById(id);
        
        if (user == null) {
            logger.warn("ID가 {}인 사용자를 찾을 수 없습니다", id); // 잠재적 문제
            return null;
        }
        
        logger.info("사용자 조회 성공: {}", user.getUsername()); // 중요 비즈니스 이벤트
        
        return user;
    }
    
    public void updateUser(User user) {
        try {
            userRepository.save(user);
            logger.info("사용자 업데이트 성공: id={}", user.getId());
        } catch (Exception e) {
            logger.error("사용자 업데이트 실패: id=" + user.getId(), e); // 심각한 오류
            throw e;
        }
    }
}
```

이러한 로그 레벨 관리 전략을 통해 Spring Boot 애플리케이션의 로깅을 효과적으로 관리할 수 있습니다.