# Spring @ExceptionHandler란?: 백엔드 개발자 관점에서의 6단계 이해

## 1. 한 줄 정의
- @ExceptionHandler는 Spring MVC에서 컨트롤러 또는 전역 레벨에서 발생하는 예외를 선언적으로 처리하는 어노테이션으로, 특정 예외 타입에 대한 커스텀 응답을 생성하는 AOP 기반 예외 처리 메커니즘이다. 백엔드 관점에서는 예외를 HTTP 응답으로 변환하는 매핑 테이블로 이해할 수 있다.

---

## 2. Spring @ExceptionHandler 동작 원리 6단계

### 2-1. 1단계: 예외 처리 어노테이션 등록 및 매핑
- **개념**: 컨트롤러 또는 @ControllerAdvice 클래스에서 예외 타입과 처리 메서드 매핑
- **백엔드 관점**: 예외 타입을 키로 하는 핸들러 매핑 테이블 생성
- **핵심 포인트**:
  - 메서드 레벨 어노테이션으로 예외 타입 명시
  - 클래스 레벨에서는 해당 컨트롤러 내 예외만 처리
  - @ControllerAdvice로 전역 예외 처리 가능

```java
// 컨트롤러 레벨 예외 처리
@RestController
public class UserController {

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        if (id < 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
        return ResponseEntity.ok(userService.findById(id));
    }

    // 해당 컨트롤러 내에서만 동작
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e) {
        ErrorResponse error = ErrorResponse.builder()
                .code("INVALID_ARGUMENT")
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
        return ResponseEntity.badRequest().body(error);
    }
}
```

### 2-2. 2단계: 전역 예외 처리를 위한 @ControllerAdvice
- **개념**: 모든 컨트롤러에서 발생하는 예외를 중앙집중식으로 처리
- **백엔드 관점**: 애플리케이션 전체의 예외 처리 정책을 정의하는 글로벌 핸들러
- **핵심 포인트**:
  - @ControllerAdvice는 모든 @Controller에 적용
  - @RestControllerAdvice = @ControllerAdvice + @ResponseBody
  - basePackages, assignableTypes로 적용 범위 제한 가능

```java
// 전역 예외 처리
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 로직 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorResponse error = ErrorResponse.builder()
                .code(e.getErrorCode())
                .message(e.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
        return ResponseEntity.status(e.getHttpStatus()).body(error);
    }

    // 데이터베이스 관련 예외
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException e) {
        log.error("Database access error", e);
        ErrorResponse error = ErrorResponse.builder()
                .code("DATABASE_ERROR")
                .message("데이터베이스 처리 중 오류가 발생했습니다")
                .timestamp(System.currentTimeMillis())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // 검증 실패 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_FAILED")
                .message("입력값 검증에 실패했습니다")
                .details(errors)
                .timestamp(System.currentTimeMillis())
                .build();
        return ResponseEntity.badRequest().body(error);
    }
}
```

### 2-3. 3단계: 예외 발생 시 HandlerExceptionResolver 동작
- **개념**: Spring MVC가 컨트롤러에서 발생한 예외를 @ExceptionHandler로 라우팅
- **백엔드 관점**: 예외 타입 매칭과 최적 핸들러 선택 알고리즘
- **핵심 포인트**:
  - ExceptionHandlerExceptionResolver가 @ExceptionHandler 메서드 검색
  - 예외 상속 구조를 고려한 최적 매칭
  - 컨트롤러 레벨 → 전역 레벨 순서로 핸들러 검색

```java
// 예외 상속 구조를 고려한 처리
@RestControllerAdvice
public class ExceptionHandler {

    // 구체적인 예외 (우선순위 높음)
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("USER_NOT_FOUND", e.getMessage()));
    }

    // 상위 예외 클래스 (우선순위 낮음)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e) {
        log.error("Unexpected runtime exception", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("INTERNAL_ERROR", "서버 내부 오류가 발생했습니다"));
    }

    // 여러 예외 타입을 하나의 핸들러로 처리
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleIllegalExceptions(RuntimeException e) {
        return ResponseEntity.badRequest()
                .body(createErrorResponse("INVALID_REQUEST", e.getMessage()));
    }

    private ErrorResponse createErrorResponse(String code, String message) {
        return ErrorResponse.builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
```

### 2-4. 4단계: 메서드 파라미터와 반환값 처리
- **개념**: @ExceptionHandler 메서드에서 사용 가능한 파라미터와 반환값 타입
- **백엔드 관점**: Spring MVC의 파라미터 리졸버와 반환값 핸들러 활용
- **핵심 포인트**:
  - HttpServletRequest/Response, Exception 등 다양한 파라미터 지원
  - ResponseEntity, @ResponseBody 등으로 응답 제어
  - Model, ModelAndView로 에러 페이지 렌더링 가능

```java
@RestControllerAdvice
public class DetailedExceptionHandler {

    // 다양한 파라미터 활용
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException e,
            HttpServletRequest request,
            HttpServletResponse response,
            WebRequest webRequest) {

        // 요청 정보 활용
        String requestURI = request.getRequestURI();
        String userAgent = request.getHeader("User-Agent");
        String clientIP = getClientIP(request);

        // MDC에 추가 정보 저장
        MDC.put("requestURI", requestURI);
        MDC.put("clientIP", clientIP);

        log.warn("Validation error at {} from {}: {}", requestURI, clientIP, e.getMessage());

        ErrorResponse error = ErrorResponse.builder()
                .code("VALIDATION_ERROR")
                .message(e.getMessage())
                .path(requestURI)
                .timestamp(System.currentTimeMillis())
                .build();

        return ResponseEntity.badRequest().body(error);
    }

    // ResponseEntity 대신 직접 응답 제어
    @ExceptionHandler(AuthenticationException.class)
    public void handleAuthentication(
            AuthenticationException e,
            HttpServletResponse response) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        ErrorResponse error = ErrorResponse.builder()
                .code("AUTHENTICATION_REQUIRED")
                .message("인증이 필요합니다")
                .timestamp(System.currentTimeMillis())
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }

    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

### 2-5. 5단계: 예외 처리 우선순위와 상속 구조
- **개념**: 여러 @ExceptionHandler가 존재할 때의 선택 우선순위 결정
- **백엔드 관점**: 예외 클래스 상속 트리를 기반으로 한 매칭 알고리즘
- **핵심 포인트**:
  - 가장 구체적인 예외 타입이 우선
  - 컨트롤러 내 핸들러가 전역 핸들러보다 우선
  - @Order 어노테이션으로 @ControllerAdvice 우선순위 조정

```java
// 예외 처리 우선순위 예시
@RestControllerAdvice
@Order(1) // 높은 우선순위
public class SecurityExceptionHandler {

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurity(SecurityException e) {
        // 보안 관련 예외는 별도 처리
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(createSecurityErrorResponse(e));
    }
}

@RestControllerAdvice
@Order(2) // 낮은 우선순위
public class GeneralExceptionHandler {

    // UserNotFoundException extends RuntimeException
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        // 구체적인 예외 처리 (우선순위 높음)
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntime(RuntimeException e) {
        // 상위 예외 처리 (우선순위 낮음)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createGenericErrorResponse(e));
    }
}

// 컨트롤러 레벨 핸들러 (전역보다 우선)
@RestController
public class UserController {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException e) {
        // 이 핸들러가 전역 핸들러보다 우선 적용됨
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createCustomUserErrorResponse(e));
    }
}
```

### 2-6. 6단계: 비동기 요청과 예외 처리 통합
- **개념**: @Async, CompletableFuture 등 비동기 처리에서의 예외 처리
- **백엔드 관점**: 스레드 간 예외 전파와 컨텍스트 유지 메커니즘
- **핵심 포인트**:
  - 비동기 메서드에서 발생한 예외는 별도 처리 필요
  - DeferredResult, Callable의 예외 처리
  - CompletionException 언래핑 처리

```java
// 비동기 요청 예외 처리
@RestController
public class AsyncController {

    @GetMapping("/async-data")
    public DeferredResult<ResponseEntity<String>> getAsyncData() {
        DeferredResult<ResponseEntity<String>> deferredResult = new DeferredResult<>(5000L);

        // 비동기 처리
        CompletableFuture.supplyAsync(() -> {
            // 시간이 걸리는 작업
            if (Math.random() > 0.5) {
                throw new RuntimeException("비동기 처리 중 오류 발생");
            }
            return "비동기 처리 완료";
        }).whenComplete((result, throwable) -> {
            if (throwable != null) {
                // 예외 발생 시
                deferredResult.setErrorResult(throwable);
            } else {
                // 정상 처리 시
                deferredResult.setResult(ResponseEntity.ok(result));
            }
        });

        return deferredResult;
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleAsyncException(RuntimeException e) {
        ErrorResponse error = ErrorResponse.builder()
                .code("ASYNC_ERROR")
                .message("비동기 처리 중 오류가 발생했습니다: " + e.getMessage())
                .timestamp(System.currentTimeMillis())
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

// CompletableFuture 예외 처리
@RestController
public class CompletableFutureController {

    @GetMapping("/future-data")
    public CompletableFuture<ResponseEntity<String>> getFutureData() {
        return CompletableFuture
                .supplyAsync(() -> processData())
                .exceptionally(throwable -> {
                    // CompletionException 언래핑
                    Throwable cause = throwable.getCause();
                    log.error("Future processing error", cause);

                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("처리 중 오류가 발생했습니다");
                });
    }

    private String processData() {
        if (Math.random() > 0.7) {
            throw new IllegalStateException("데이터 처리 실패");
        }
        return "데이터 처리 성공";
    }
}
```

---

## 3. 백엔드 개발자 관점에서의 시스템 특성

### 3-1. 예외 처리 아키텍처
- **계층별 예외 처리**: Controller → Service → Repository 각 계층의 예외 변환
- **예외 전파 제어**: 비즈니스 예외와 시스템 예외의 분리 처리
- **로깅 전략**: 예외 레벨별 로그 정책과 민감 정보 마스킹

### 3-2. 성능 고려사항
- **예외 처리 비용**: 스택 트레이스 생성과 성능 영향
- **캐싱 전략**: 반복적인 예외 응답의 캐싱 고려
- **모니터링**: 예외 발생 빈도와 패턴 추적

### 3-3. 보안 고려사항
- **정보 노출 방지**: 내부 시스템 정보가 포함된 예외 메시지 필터링
- **공격 패턴 탐지**: 반복적인 예외 발생을 통한 공격 시도 감지
- **감사 로깅**: 보안 관련 예외의 상세 로깅

---

## 4. 실제 서비스 운영 시 고려사항

### 4-1. 표준화된 에러 응답
- **일관된 포맷**: 모든 예외에 대해 동일한 응답 구조 사용
- **국제화**: 다국어 지원을 위한 메시지 코드 체계
- **API 문서화**: Swagger/OpenAPI를 통한 에러 응답 문서화

```java
@Data
@Builder
public class ErrorResponse {
    private String code;           // 에러 코드 (ENUM)
    private String message;        // 사용자용 메시지
    private String debugMessage;   // 개발자용 상세 메시지 (개발 환경에만)
    private long timestamp;        // 발생 시각
    private String path;          // 요청 경로
    private List<String> details; // 상세 오류 정보 (validation 등)
    private String traceId;       // 분산 추적 ID
}
```

### 4-2. 모니터링 및 알림
- **메트릭 수집**: 예외 타입별 발생 빈도와 추이
- **실시간 알림**: 심각한 예외 발생 시 즉시 알림
- **대시보드**: 예외 현황과 트렌드 시각화

### 4-3. 운영 안정성
- **장애 격리**: 특정 예외로 인한 전체 서비스 영향 최소화
- **복구 전략**: 예외 발생 후 자동 복구 메커니즘
- **성능 저하 방지**: 예외 처리로 인한 성능 영향 최소화

---

## 5. 예상 면접 질문

### 5-1. 기술적 질문
1. @ExceptionHandler와 try-catch의 차이점과 각각의 사용 시기는?
2. @ControllerAdvice의 동작 원리와 적용 범위 제한 방법은?
3. 예외 상속 구조에서 핸들러 선택 우선순위는 어떻게 결정되나요?

### 5-2. 시스템 설계 질문
1. 마이크로서비스 환경에서 일관된 예외 처리 전략을 어떻게 구현하시겠나요?
2. 대용량 트래픽에서 예외 처리 성능을 최적화하는 방법은?
3. 비동기 처리에서 발생하는 예외를 어떻게 일관되게 처리하시겠나요?

### 5-3. 실무 상황 질문
1. 운영 중인 서비스에서 새로운 예외 타입이 추가될 때 고려사항은?
2. 예외 처리로 인한 보안 취약점을 어떻게 방지하시겠나요?
3. Filter에서 발생한 예외와 Controller에서 발생한 예외를 통합 처리하는 방법은?

---

## 6. 핵심 요약

### 6-1. 주요 특징
- **선언적 예외 처리**: 어노테이션 기반의 직관적인 예외 처리 정의
- **계층적 처리**: 컨트롤러 → 전역 순서의 예외 처리 우선순위
- **유연한 응답 제어**: 다양한 응답 형태와 HTTP 상태 코드 지원

### 6-2. 백엔드 개발자의 핵심 이해사항
- @ExceptionHandler는 Spring MVC의 예외를 HTTP 응답으로 변환하는 매핑 메커니즘이다
- 예외 타입의 상속 구조와 핸들러 우선순위를 이해하여 정확한 예외 처리 설계가 필요하다
- 전역 예외 처리와 컨트롤러별 예외 처리의 적절한 분리가 코드 유지보수성을 높인다

### 6-3. 실무 적용 포인트
- 표준화된 에러 응답 포맷으로 일관성 있는 API 설계
- 보안을 고려한 예외 메시지 처리와 로깅 전략 수립
- 모니터링과 알림을 통한 예외 발생 패턴 추적 및 개선