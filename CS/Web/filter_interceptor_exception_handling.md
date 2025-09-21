# Filter와 Interceptor의 예외 처리 원리: 백엔드 개발자 관점에서의 6단계 이해

## 1. 한 줄 정의
- Filter는 Servlet 스펙의 전처리/후처리 컴포넌트이고, Interceptor는 Spring MVC 스펙의 컨트롤러 전후 처리 컴포넌트로, 각각 다른 레벨에서 동작하기 때문에 예외 처리 방식과 적용 범위가 상이하다. 백엔드 관점에서는 요청 처리 파이프라인의 서로 다른 단계에서 작동하는 AOP 컴포넌트로 이해할 수 있다.

---

## 2. Filter와 Interceptor 예외 처리 6단계

### 2-1. 1단계: 요청 처리 파이프라인에서의 위치 이해
- **개념**: Filter는 Servlet Container 레벨, Interceptor는 Spring MVC 레벨에서 동작
- **백엔드 관점**: 네트워크 스택의 서로 다른 계층에서 작동하는 미들웨어
- **핵심 포인트**:
  - Filter: 모든 요청(정적 리소스 포함)에 적용
  - Interceptor: DispatcherServlet이 처리하는 요청에만 적용
  - 실행 순서: Filter → DispatcherServlet → Interceptor → Controller

```java
// 요청 처리 파이프라인 구조
HTTP Request
    ↓
Servlet Container (Tomcat)
    ↓
Filter Chain (Servlet 스펙)
    ↓
DispatcherServlet
    ↓
HandlerInterceptor (Spring MVC 스펙)
    ↓
Controller
    ↓
HandlerInterceptor (afterCompletion)
    ↓
Filter Chain (response processing)
    ↓
HTTP Response
```

### 2-2. 2단계: Filter에서의 예외 처리 메커니즘
- **개념**: Filter는 Servlet Container 레벨에서 동작하므로 Spring의 예외 처리 메커니즘 적용 불가
- **백엔드 관점**: try-catch를 통한 직접적인 예외 처리 필요
- **핵심 포인트**:
  - @ExceptionHandler, @ControllerAdvice 적용 불가
  - ServletException, IOException 등 Servlet 레벨 예외 처리
  - 예외 발생 시 직접 HTTP 응답 작성 필요

```java
// Filter에서의 예외 처리 예시
@Component
public class CustomFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // 전처리 로직
            validateRequest(httpRequest);

            // 다음 필터 또는 서블릿으로 진행
            chain.doFilter(request, response);

        } catch (SecurityException e) {
            // Filter에서 발생한 예외는 직접 처리해야 함
            handleSecurityException(httpResponse, e);
        } catch (RuntimeException e) {
            // 런타임 예외 처리
            handleGenericException(httpResponse, e);
        }
    }

    private void handleSecurityException(HttpServletResponse response, SecurityException e)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
    }

    private void handleGenericException(HttpServletResponse response, RuntimeException e)
            throws IOException {
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"Internal server error\"}");
    }
}
```

### 2-3. 3단계: Interceptor에서의 예외 처리 메커니즘
- **개념**: Interceptor는 Spring MVC 컨텍스트 내에서 동작하므로 Spring의 예외 처리 활용 가능
- **백엔드 관점**: Spring AOP 기반의 선언적 예외 처리 지원
- **핵심 포인트**:
  - preHandle에서 예외 발생 시 afterCompletion만 실행
  - postHandle에서 예외 발생 시 afterCompletion 실행
  - @ExceptionHandler와 연동 가능

```java
// Interceptor에서의 예외 처리 예시
@Component
public class CustomInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        try {
            // 전처리 로직
            validateUserPermission(request);
            return true;

        } catch (AuthenticationException e) {
            // Spring의 예외 처리 메커니즘으로 위임 가능
            throw e;  // @ExceptionHandler에서 처리됨
        } catch (Exception e) {
            // 또는 직접 처리
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return false;  // 컨트롤러 실행 중단
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                          ModelAndView modelAndView) throws Exception {
        try {
            // 후처리 로직
            auditLog(request, response);
        } catch (Exception e) {
            // 예외 발생 시에도 afterCompletion은 호출됨
            throw new PostProcessingException("Post processing failed", e);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                               Exception ex) throws Exception {
        try {
            // 정리 작업 (리소스 해제 등)
            cleanupResources(request);
        } catch (Exception e) {
            // 최종 정리 단계에서의 예외는 로깅만 수행
            log.error("Cleanup failed", e);
        }
    }
}
```

### 2-4. 4단계: 통합 예외 처리 전략 구현
- **개념**: Filter와 Interceptor의 예외를 일관된 방식으로 처리하기 위한 전략 수립
- **백엔드 관점**: 계층별 예외 처리 정책과 공통 응답 포맷 정의
- **핵심 포인트**:
  - 공통 예외 응답 포맷 정의
  - Filter 레벨에서 발생한 예외를 Spring 예외 처리로 연계
  - 로깅과 모니터링 전략 통합

```java
// 통합 예외 처리 전략
@Component
public class ExceptionHandlingFilter implements Filter {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            handleException((HttpServletResponse) response, e);
        }
    }

    private void handleException(HttpServletResponse response, Exception e) throws IOException {
        ErrorResponse errorResponse = createErrorResponse(e);

        response.setStatus(errorResponse.getStatus());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }

    private ErrorResponse createErrorResponse(Exception e) {
        if (e instanceof SecurityException) {
            return ErrorResponse.builder()
                    .status(401)
                    .code("SECURITY_ERROR")
                    .message(e.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .build();
        }
        // 다른 예외 타입들에 대한 처리...
        return ErrorResponse.builder()
                .status(500)
                .code("INTERNAL_ERROR")
                .message("Internal server error")
                .timestamp(System.currentTimeMillis())
                .build();
    }
}

// 공통 예외 응답 객체
@Data
@Builder
public class ErrorResponse {
    private int status;
    private String code;
    private String message;
    private long timestamp;
    private String path;
}
```

### 2-5. 5단계: Spring Security와의 연동 고려사항
- **개념**: Spring Security Filter와 Custom Filter/Interceptor 간의 예외 처리 조율
- **백엔드 관점**: 보안 관련 예외와 비즈니스 로직 예외의 분리 처리
- **핵심 포인트**:
  - Security Filter Chain의 실행 순서 고려
  - AuthenticationException, AccessDeniedException 처리
  - CSRF, CORS 관련 예외 처리

```java
// Spring Security와의 연동 예시
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .addFilterBefore(new CustomSecurityFilter(), UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(customAuthenticationEntryPoint())
                        .accessDeniedHandler(customAccessDeniedHandler())
                )
                .build();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(401)
                    .code("AUTHENTICATION_REQUIRED")
                    .message("Authentication required")
                    .timestamp(System.currentTimeMillis())
                    .path(request.getRequestURI())
                    .build();

            response.setStatus(401);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        };
    }
}

// Custom Security Filter
public class CustomSecurityFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        try {
            // 보안 검증 로직
            validateSecurityToken(request);
            filterChain.doFilter(request, response);

        } catch (InvalidTokenException e) {
            // Security 예외는 AuthenticationEntryPoint로 위임
            throw new AuthenticationCredentialsNotFoundException("Invalid token", e);
        }
    }
}
```

### 2-6. 6단계: 모니터링 및 로깅 통합
- **개념**: Filter와 Interceptor에서 발생하는 예외에 대한 통합 모니터링 체계 구축
- **백엔드 관점**: 분산 시스템에서의 요청 추적과 예외 분석을 위한 로깅 전략
- **핵심 포인트**:
  - MDC(Mapped Diagnostic Context)를 활용한 요청 추적
  - 메트릭 수집 및 알림 시스템 연동
  - 예외 발생 위치와 처리 결과 추적

```java
// 통합 모니터링 예시
@Component
public class MonitoringFilter implements Filter {

    private final MeterRegistry meterRegistry;
    private final Counter filterExceptionCounter;

    public MonitoringFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.filterExceptionCounter = Counter.builder("filter.exceptions")
                .description("Filter에서 발생한 예외 수")
                .register(meterRegistry);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        MDC.put("requestUri", ((HttpServletRequest) request).getRequestURI());

        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            chain.doFilter(request, response);
        } catch (Exception e) {
            filterExceptionCounter.increment(
                    Tags.of(
                            "exception", e.getClass().getSimpleName(),
                            "uri", ((HttpServletRequest) request).getRequestURI()
                    )
            );

            log.error("Filter에서 예외 발생 - RequestId: {}, URI: {}, Exception: {}",
                    requestId, ((HttpServletRequest) request).getRequestURI(), e.getMessage(), e);

            throw e;
        } finally {
            sample.stop(Timer.builder("filter.request.duration")
                    .description("Filter 처리 시간")
                    .register(meterRegistry));

            MDC.clear();
        }
    }
}

// Interceptor 모니터링
@Component
public class MonitoringInterceptor implements HandlerInterceptor {

    private final MeterRegistry meterRegistry;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        try {
            // 전처리 로직
            return true;
        } catch (Exception e) {
            meterRegistry.counter("interceptor.exceptions",
                    "phase", "preHandle",
                    "exception", e.getClass().getSimpleName()
            ).increment();

            log.error("Interceptor preHandle에서 예외 발생 - RequestId: {}, Exception: {}",
                    MDC.get("requestId"), e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                               Exception ex) throws Exception {
        if (ex != null) {
            meterRegistry.counter("interceptor.controller.exceptions",
                    "exception", ex.getClass().getSimpleName()
            ).increment();

            log.error("Controller에서 예외 발생 - RequestId: {}, Exception: {}",
                    MDC.get("requestId"), ex.getMessage(), ex);
        }
    }
}
```

---

## 3. 백엔드 개발자 관점에서의 시스템 특성

### 3-1. 예외 처리 범위와 제약사항
- **Filter 제약사항**: Spring 컨텍스트 외부에서 동작하므로 Spring의 예외 처리 기능 사용 불가
- **Interceptor 장점**: Spring MVC 컨텍스트 내에서 동작하므로 @ExceptionHandler 연동 가능
- **성능 고려사항**: Filter는 모든 요청에 적용되므로 성능에 미치는 영향 고려 필요

### 3-2. 예외 전파 메커니즘
- **Filter 체인**: 예외 발생 시 체인 중단, 직접 응답 처리 필요
- **Interceptor 체인**: preHandle 실패 시 afterCompletion만 실행
- **Spring 예외 처리**: Interceptor 예외는 @ControllerAdvice에서 처리 가능

### 3-3. 보안 및 트랜잭션 고려사항
- **보안 처리**: Filter 레벨에서 인증/인가 처리 시 예외 응답 표준화 필요
- **트랜잭션**: Interceptor에서 발생한 예외는 트랜잭션 롤백 트리거 가능
- **리소스 관리**: afterCompletion에서 리소스 정리 로직 필수

---

## 4. 실제 서비스 운영 시 고려사항

### 4-1. 예외 응답 표준화
- **응답 포맷**: Filter와 Controller에서 동일한 에러 응답 구조 사용
- **HTTP 상태 코드**: 예외 타입별 적절한 상태 코드 매핑
- **국제화**: 다국어 지원을 위한 메시지 처리 전략

### 4-2. 로깅 및 모니터링
- **구조화된 로그**: JSON 형태의 로그로 분석 용이성 확보
- **알림 시스템**: 심각한 예외 발생 시 실시간 알림
- **대시보드**: 예외 발생 패턴 및 트렌드 모니터링

### 4-3. 성능 최적화
- **예외 캐싱**: 반복적인 검증 로직의 결과 캐싱
- **비동기 로깅**: 로그 처리로 인한 성능 저하 방지
- **서킷 브레이커**: 외부 시스템 호출 시 장애 전파 방지

---

## 5. 예상 면접 질문

### 5-1. 기술적 질문
1. Filter와 Interceptor에서 예외가 발생했을 때 처리 방식의 차이점은?
2. Filter에서 발생한 예외를 Spring의 @ExceptionHandler로 처리할 수 있나요?
3. Interceptor의 preHandle에서 예외가 발생하면 어떤 메서드들이 실행되나요?

### 5-2. 시스템 설계 질문
1. 인증 실패 예외를 Filter와 Interceptor 중 어디서 처리하는 것이 좋을까요?
2. 대용량 트래픽에서 Filter/Interceptor 예외 처리 성능을 어떻게 최적화하시겠나요?
3. 마이크로서비스 환경에서 각 서비스의 예외 처리를 어떻게 표준화하시겠나요?

### 5-3. 실무 상황 질문
1. Filter에서 데이터베이스 연결 예외가 발생했을 때 어떻게 처리하시겠나요?
2. Spring Security Filter와 Custom Filter 간의 예외 처리 우선순위는?
3. Filter/Interceptor 예외로 인한 장애를 어떻게 모니터링하고 대응하시겠나요?

---

## 6. 핵심 요약

### 6-1. 주요 특징
- **계층별 처리**: Filter(Servlet 레벨)와 Interceptor(Spring MVC 레벨)의 서로 다른 예외 처리 방식
- **제약사항 인지**: Filter는 Spring 예외 처리 메커니즘 사용 불가
- **통합 전략**: 일관된 예외 응답 포맷과 로깅 전략 필요

### 6-2. 백엔드 개발자의 핵심 이해사항
- Filter와 Interceptor는 요청 처리 파이프라인의 서로 다른 지점에서 동작한다
- 각 컴포넌트의 제약사항을 이해하고 적절한 예외 처리 전략을 선택해야 한다
- 모니터링과 로깅을 통한 예외 추적이 운영 안정성의 핵심이다

### 6-3. 실무 적용 포인트
- Filter에서는 직접적인 예외 처리와 응답 생성이 필요
- Interceptor에서는 Spring의 예외 처리 메커니즘 활용 가능
- 통합 모니터링을 통한 예외 발생 패턴 분석과 개선이 중요