# Spring Web MVC의 Servlet 기반 구현 원리: 백엔드 개발자 관점에서의 6단계 이해

## 1. 한 줄 정의
- Spring Web MVC는 Java Servlet API를 기반으로 DispatcherServlet을 중심으로 한 Front Controller 패턴을 구현하여, 웹 요청을 체계적으로 처리하는 프레임워크이다. 백엔드 관점에서는 단일 Servlet이 모든 HTTP 요청을 받아 적절한 Controller로 라우팅하는 중앙집중식 아키텍처로 이해할 수 있다.

---

## 2. Spring Web MVC 동작 원리 6단계

### 2-1. 1단계: DispatcherServlet 초기화 및 등록
- **개념**: Spring의 핵심 Servlet으로 모든 HTTP 요청의 진입점 역할
- **백엔드 관점**: 웹 컨테이너(Tomcat)에 등록되는 단일 Servlet으로 모든 요청을 처리
- **핵심 포인트**:
  - HttpServlet을 상속받아 구현된 Spring의 핵심 컴포넌트
  - web.xml 또는 Java Config를 통해 URL 패턴 매핑 (보통 "/")
  - 서블릿 컨테이너 생명주기에 따라 init(), service(), destroy() 실행

```java
// DispatcherServlet 등록 예시 (Java Config)
public class WebApplicationInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.register(WebConfig.class);

        DispatcherServlet servlet = new DispatcherServlet(context);
        ServletRegistration.Dynamic registration = servletContext.addServlet("dispatcher", servlet);
        registration.setLoadOnStartup(1);
        registration.addMapping("/");
    }
}
```

### 2-2. 2단계: 요청 수신 및 doDispatch() 메서드 실행
- **개념**: HTTP 요청이 들어오면 DispatcherServlet의 doDispatch() 메서드가 호출
- **백엔드 관점**: HttpServletRequest/Response 객체를 받아 Spring의 요청 처리 파이프라인 시작
- **핵심 포인트**:
  - doGet(), doPost() 등이 모두 doDispatch()로 위임
  - 요청 URL, HTTP 메서드, 헤더 정보 분석
  - 멀티스레드 환경에서 각 요청별로 독립적인 스레드에서 실행

```java
// DispatcherServlet의 핵심 메서드 (개념적)
protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
    HandlerExecutionChain mappedHandler = getHandler(request);  // 핸들러 매핑
    HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());  // 어댑터 조회

    // 인터셉터 pre-handle 실행
    if (!mappedHandler.applyPreHandle(request, response)) {
        return;
    }

    // 실제 핸들러(컨트롤러) 실행
    ModelAndView mv = ha.handle(request, response, mappedHandler.getHandler());

    // 인터셉터 post-handle 실행
    mappedHandler.applyPostHandle(request, response, mv);

    // 뷰 렌더링
    processDispatchResult(request, response, mappedHandler, mv, dispatchException);
}
```

### 2-3. 3단계: HandlerMapping을 통한 Controller 매핑
- **개념**: 요청 URL과 HTTP 메서드에 따라 적절한 Controller 메서드 결정
- **백엔드 관점**: 라우팅 테이블 조회와 유사하며, URL 패턴 매칭을 통한 핸들러 선택
- **핵심 포인트**:
  - RequestMappingHandlerMapping이 @RequestMapping 어노테이션 기반 매핑 처리
  - PathPattern을 사용한 URL 패턴 매칭 (Ant 스타일 또는 정규식)
  - HTTP 메서드, Content-Type, Accept 헤더 등을 고려한 정교한 매핑

```java
// HandlerMapping 동작 예시
@Controller
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/{id}")  // GET /api/users/123
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        // HandlerMapping이 이 메서드를 GET /api/users/* 패턴으로 매핑
        return ResponseEntity.ok(userService.findById(id));
    }

    @PostMapping  // POST /api/users
    public ResponseEntity<User> createUser(@RequestBody User user) {
        return ResponseEntity.ok(userService.save(user));
    }
}
```

### 2-4. 4단계: HandlerAdapter를 통한 Controller 실행
- **개념**: 다양한 형태의 Controller를 통일된 방식으로 실행하기 위한 어댑터 패턴
- **백엔드 관점**: 인터페이스 추상화를 통해 다양한 컨트롤러 타입을 동일하게 처리
- **핵심 포인트**:
  - RequestMappingHandlerAdapter가 @RequestMapping 기반 컨트롤러 처리
  - ArgumentResolver로 메서드 파라미터 자동 바인딩
  - ReturnValueHandler로 반환값 처리 및 응답 생성

```java
// HandlerAdapter의 핵심 동작 (개념적)
public class RequestMappingHandlerAdapter implements HandlerAdapter {

    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // 1. 메서드 파라미터 해석 및 바인딩
        Object[] args = resolveArguments(handlerMethod, request, response);

        // 2. 실제 컨트롤러 메서드 실행
        Object returnValue = handlerMethod.getMethod().invoke(handlerMethod.getBean(), args);

        // 3. 반환값 처리
        return handleReturnValue(returnValue, handlerMethod, request, response);
    }
}
```

### 2-5. 5단계: ViewResolver를 통한 View 결정
- **개념**: Controller에서 반환된 논리적 뷰 이름을 실제 View 객체로 변환
- **백엔드 관점**: 템플릿 엔진과의 브릿지 역할, MVC 패턴의 View 레이어 구현
- **핵심 포인트**:
  - InternalResourceViewResolver가 JSP 뷰 처리
  - ThymeleafViewResolver, FreeMarkerViewResolver 등 다양한 템플릿 엔진 지원
  - RESTful API의 경우 @ResponseBody로 뷰 렌더링 생략 가능

```java
// ViewResolver 설정 예시
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        return resolver;
    }
}

// Controller에서 뷰 이름 반환
@Controller
public class HomeController {

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("message", "Hello Spring MVC");
        return "home";  // /WEB-INF/views/home.jsp로 해석됨
    }
}
```

### 2-6. 6단계: View 렌더링 및 응답 생성
- **개념**: 결정된 View 객체를 사용하여 모델 데이터와 함께 HTML 응답 생성
- **백엔드 관점**: 템플릿 엔진을 통한 서버 사이드 렌더링 또는 JSON 직렬화
- **핵심 포인트**:
  - Model 객체의 데이터를 View에 전달
  - HttpServletResponse에 최종 응답 데이터 작성
  - Content-Type, 인코딩, HTTP 상태 코드 설정

```java
// View 렌더링 과정 (개념적)
public class InternalResourceView extends AbstractUrlBasedView {

    @Override
    protected void renderMergedOutputModel(Map<String, Object> model,
                                          HttpServletRequest request,
                                          HttpServletResponse response) throws Exception {

        // 1. 모델 데이터를 request attribute로 설정
        exposeModelAsRequestAttributes(model, request);

        // 2. JSP로 forward
        RequestDispatcher dispatcher = request.getRequestDispatcher(getUrl());
        dispatcher.forward(request, response);
    }
}

// JSON 응답의 경우 (REST API)
@RestController
public class ApiController {

    @GetMapping("/api/data")
    public ResponseEntity<Map<String, Object>> getData() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "Hello REST API");
        // Jackson을 통해 자동으로 JSON 직렬화됨
        return ResponseEntity.ok(data);
    }
}
```

---

## 3. 백엔드 개발자 관점에서의 시스템 특성

### 3-1. Servlet 컨테이너와의 통합
- **스레드 관리**: 서블릿 컨테이너의 스레드 풀을 활용한 요청 처리
- **세션 관리**: HttpSession을 통한 상태 관리
- **보안 통합**: 서블릿 필터와 Spring Security 연동

### 3-2. 요청 처리 파이프라인
- **필터 체인**: 서블릿 필터 → DispatcherServlet → 인터셉터 → 컨트롤러
- **예외 처리**: @ExceptionHandler, @ControllerAdvice를 통한 전역 예외 처리
- **데이터 바인딩**: 자동 타입 변환 및 검증 (Validator)

### 3-3. 성능 최적화
- **스레드 안전성**: 컨트롤러 빈의 싱글톤 패턴 활용
- **캐싱**: @Cacheable을 통한 메서드 레벨 캐싱
- **비동기 처리**: DeferredResult, Callable을 통한 논블로킹 처리

---

## 4. 실제 서비스 운영 시 고려사항

### 4-1. 설정 및 튜닝
- **커넥션 풀**: HikariCP, Tomcat JDBC 설정
- **스레드 풀**: 서블릿 컨테이너 스레드 풀 튜닝
- **메모리 관리**: 힙 크기, GC 설정 최적화

### 4-2. 모니터링 및 로깅
- **액추에이터**: Spring Boot Actuator를 통한 애플리케이션 상태 모니터링
- **메트릭 수집**: Micrometer를 통한 성능 지표 수집
- **분산 추적**: Spring Cloud Sleuth, Zipkin 연동

### 4-3. 보안 고려사항
- **CSRF 보호**: Spring Security의 CSRF 토큰 활용
- **XSS 방지**: 입력값 검증 및 이스케이프 처리
- **인증/인가**: JWT, OAuth2 기반 보안 구현

---

## 5. 예상 면접 질문

### 5-1. 기술적 질문
1. DispatcherServlet이 기존 Servlet과 다른 점은 무엇인가요?
2. HandlerMapping과 HandlerAdapter의 역할과 차이점을 설명해주세요.
3. Spring MVC에서 요청 처리 과정에서 발생하는 스레드 처리 방식은?

### 5-2. 시스템 설계 질문
1. 대용량 트래픽을 처리하는 Spring MVC 애플리케이션 설계 방법은?
2. RESTful API와 전통적인 MVC의 차이점과 각각의 사용 사례는?
3. Spring MVC에서 비동기 처리를 구현하는 방법과 주의사항은?

### 5-3. 최적화 질문
1. Spring MVC 애플리케이션의 성능 병목점과 해결 방법은?
2. 대용량 파일 업로드/다운로드 처리 전략은?
3. Spring MVC에서 캐싱 전략과 구현 방법은?

---

## 6. 핵심 요약

### 6-1. 주요 특징
- **Front Controller 패턴**: 단일 진입점을 통한 중앙집중식 요청 처리
- **유연한 핸들러 매핑**: 어노테이션 기반의 직관적인 URL 매핑
- **확장 가능한 아키텍처**: 다양한 View 기술과 컨트롤러 타입 지원

### 6-2. 백엔드 개발자의 핵심 이해사항
- Spring MVC는 Servlet API를 추상화하여 웹 개발의 복잡성을 줄인다
- DispatcherServlet을 중심으로 한 체계적인 요청 처리 파이프라인을 제공한다
- 어노테이션 기반 설정으로 선언적 프로그래밍을 지원한다

### 6-3. 실무 적용 포인트
- 서블릿 컨테이너 설정과 Spring 설정의 조화가 성능의 핵심
- 예외 처리와 로깅 전략을 통한 운영 안정성 확보
- RESTful API와 전통적인 MVC의 적절한 선택과 혼용
