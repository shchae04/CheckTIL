# 로깅에서의 MDC (Mapped Diagnostic Context) 활용

## 목차
1. [MDC란 무엇인가?](#1-mdc란-무엇인가)
   - [MDC의 개념](#mdc의-개념)
   - [MDC의 작동 원리](#mdc의-작동-원리)
2. [MDC를 사용하는 이유](#2-mdc를-사용하는-이유)
   - [멀티스레드 환경에서의 로그 추적](#멀티스레드-환경에서의-로그-추적)
   - [분산 시스템에서의 로그 연관성](#분산-시스템에서의-로그-연관성)
3. [MDC 구현 방법](#3-mdc-구현-방법)
   - [SLF4J MDC 사용하기](#slf4j-mdc-사용하기)
   - [Log4j/Logback에서의 MDC 설정](#log4jlogback에서의-mdc-설정)
4. [Spring Boot에서 MDC 활용](#4-spring-boot에서-mdc-활용)
   - [필터를 이용한 MDC 설정](#필터를-이용한-mdc-설정)
   - [인터셉터를 이용한 MDC 설정](#인터셉터를-이용한-mdc-설정)
5. [MDC 활용 사례](#5-mdc-활용-사례)
   - [사용자 식별자 추적](#사용자-식별자-추적)
   - [요청 ID를 이용한 트랜잭션 추적](#요청-id를-이용한-트랜잭션-추적)
   - [마이크로서비스 환경에서의 분산 추적](#마이크로서비스-환경에서의-분산-추적)

## 1. MDC란 무엇인가?

### MDC의 개념

MDC(Mapped Diagnostic Context)는 로깅 프레임워크에서 제공하는 기능으로, 멀티스레드 애플리케이션에서 로그 메시지에 문맥 정보를 추가할 수 있게 해주는 메커니즘입니다. MDC는 스레드 로컬(Thread-local) 변수를 사용하여 각 스레드별로 독립적인 진단 컨텍스트를 유지합니다.

MDC는 키-값 쌍의 맵 형태로 데이터를 저장하며, 이 데이터는 로그 메시지가 생성될 때 자동으로 포함됩니다. 이를 통해 로그 메시지에 사용자 ID, 세션 ID, 요청 ID 등의 중요한 컨텍스트 정보를 쉽게 추가할 수 있습니다.

### MDC의 작동 원리

MDC는 다음과 같은 원리로 작동합니다:

1. 스레드 로컬 저장소에 컨텍스트 정보를 저장
2. 로깅 시 해당 스레드의 MDC에서 컨텍스트 정보를 가져와 로그 메시지에 포함
3. 스레드 작업이 완료되면 MDC에서 컨텍스트 정보를 제거

이러한 방식으로 MDC는 멀티스레드 환경에서도 각 스레드의 컨텍스트 정보를 안전하게 관리할 수 있습니다.

## 2. MDC를 사용하는 이유

### 멀티스레드 환경에서의 로그 추적

현대 애플리케이션은 대부분 멀티스레드 환경에서 동작합니다. 이런 환경에서는 여러 스레드가 동시에 로그를 생성하기 때문에, 특정 작업이나 요청에 관련된 로그를 추적하기 어렵습니다. MDC를 사용하면 각 스레드별로 고유한 식별자나 컨텍스트 정보를 로그에 포함시켜, 특정 작업이나 요청에 관련된 로그를 쉽게 필터링하고 추적할 수 있습니다.

### 분산 시스템에서의 로그 연관성

마이크로서비스 아키텍처와 같은 분산 시스템에서는 하나의 요청이 여러 서비스를 거쳐 처리됩니다. 이런 환경에서 MDC와 상관 ID(Correlation ID)를 함께 사용하면, 여러 서비스에 걸친 요청의 흐름을 추적할 수 있습니다. 요청이 시작될 때 생성된 고유 ID를 MDC에 저장하고, 이를 다른 서비스로 전달함으로써 전체 요청 흐름을 연결할 수 있습니다.

## 3. MDC 구현 방법

### SLF4J MDC 사용하기

SLF4J(Simple Logging Facade for Java)는 다양한 로깅 프레임워크에 대한 추상화 계층을 제공합니다. SLF4J의 MDC 클래스를 사용하여 컨텍스트 정보를 관리할 수 있습니다:

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    public void processUser(String userId) {
        // MDC에 사용자 ID 추가
        MDC.put("userId", userId);
        
        try {
            logger.info("사용자 처리 시작"); // 로그에 userId가 포함됨
            // 사용자 처리 로직
            logger.info("사용자 처리 완료");
        } finally {
            // 작업 완료 후 MDC에서 사용자 ID 제거
            MDC.remove("userId");
            // 또는 모든 MDC 데이터 제거
            // MDC.clear();
        }
    }
}
```

### Log4j/Logback에서의 MDC 설정

MDC의 정보를 로그 메시지에 포함시키려면 로깅 프레임워크의 패턴 레이아웃을 설정해야 합니다:

1. **Logback 설정 (logback-spring.xml)**:

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] [%X{userId}] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <root level="info">
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
```

2. **Log4j2 설정 (log4j2-spring.xml)**:

```xml
<Configuration status="WARN">
    <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
            <PatternLayout pattern="%d{HH:mm:ss.SSS} [%t] [%X{userId}] %-5level %logger{36} - %msg%n"/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>
```

패턴에서 `%X{key}`는 MDC에서 지정된 키의 값을 가져와 로그 메시지에 포함시킵니다.

## 4. Spring Boot에서 MDC 활용

### 필터를 이용한 MDC 설정

Spring Boot 애플리케이션에서는 서블릿 필터를 사용하여 모든 HTTP 요청에 대해 MDC를 설정할 수 있습니다:

```java
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

@Component
public class MdcFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // 요청 ID 생성 및 MDC에 설정
            String requestId = UUID.randomUUID().toString();
            MDC.put("requestId", requestId);
            
            // 사용자 정보가 있으면 MDC에 추가
            String username = request.getRemoteUser();
            if (username != null) {
                MDC.put("username", username);
            }
            
            // 요청 처리
            filterChain.doFilter(request, response);
        } finally {
            // MDC 정리
            MDC.clear();
        }
    }
}
```

### 인터셉터를 이용한 MDC 설정

Spring MVC 인터셉터를 사용하여 MDC를 설정할 수도 있습니다:

```java
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

@Component
public class MdcInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 요청 ID 생성 및 MDC에 설정
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // MDC 정리
        MDC.clear();
    }
}
```

인터셉터를 Spring 설정에 등록:

```java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final MdcInterceptor mdcInterceptor;

    public WebConfig(MdcInterceptor mdcInterceptor) {
        this.mdcInterceptor = mdcInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(mdcInterceptor);
    }
}
```

## 5. MDC 활용 사례

### 사용자 식별자 추적

사용자 인증 후 사용자 ID나 이름을 MDC에 저장하여 로그에 포함시키면, 특정 사용자의 활동을 쉽게 추적할 수 있습니다:

```java
@RestController
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("username", userDetails.getUsername());
        
        try {
            logger.info("사용자 정보 조회 요청");
            // 사용자 조회 로직
            return userService.findById(id);
        } finally {
            MDC.remove("username");
        }
    }
}
```

### 요청 ID를 이용한 트랜잭션 추적

각 HTTP 요청에 고유한 ID를 할당하고 이를 MDC에 저장하면, 하나의 요청과 관련된 모든 로그를 쉽게 찾을 수 있습니다:

```java
@Component
public class RequestIdFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        // 응답 헤더에도 요청 ID 포함
        response.setHeader("X-Request-ID", requestId);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

### 마이크로서비스 환경에서의 분산 추적

마이크로서비스 환경에서는 하나의 요청이 여러 서비스를 거쳐 처리됩니다. 이런 환경에서 MDC와 상관 ID를 함께 사용하면 전체 요청 흐름을 추적할 수 있습니다:

1. **게이트웨이 서비스**:

```java
@Component
public class CorrelationIdFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 요청 헤더에서 상관 ID 확인, 없으면 새로 생성
        String correlationId = request.getHeader("X-Correlation-ID");
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        
        MDC.put("correlationId", correlationId);
        
        // 다음 서비스로 요청 시 상관 ID 전달을 위해 응답 헤더에 추가
        response.setHeader("X-Correlation-ID", correlationId);
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

2. **RestTemplate 설정**:

```java
@Configuration
public class RestTemplateConfig {
    
    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(Collections.singletonList(new CorrelationIdInterceptor()));
        return restTemplate;
    }
    
    public static class CorrelationIdInterceptor implements ClientHttpRequestInterceptor {
        
        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                throws IOException {
            
            String correlationId = MDC.get("correlationId");
            if (correlationId != null) {
                request.getHeaders().set("X-Correlation-ID", correlationId);
            }
            
            return execution.execute(request, body);
        }
    }
}
```

이러한 방식으로 MDC를 활용하면 복잡한 시스템에서도 로그를 효과적으로 추적하고 분석할 수 있습니다. 특히 마이크로서비스 아키텍처나 대규모 분산 시스템에서 MDC는 로그 관리와 문제 해결에 큰 도움이 됩니다.