# Filter와 Interceptor의 차이

웹 애플리케이션에서 요청과 응답을 처리하는 과정에서 공통적인 기능을 적용하기 위해 Filter와 Interceptor라는 두 가지 중요한 개념이 사용됩니다. 이 두 개념은 비슷한 목적을 가지고 있지만, 동작 방식과 적용 시점에 차이가 있습니다.

## 목차
1. [Filter 개요](#filter-개요)
2. [Interceptor 개요](#interceptor-개요)
3. [주요 차이점](#주요-차이점)
4. [사용 사례](#사용-사례)
5. [코드 예제](#코드-예제)
6. [요청 처리 흐름](#요청-처리-흐름)

## Filter 개요

Filter는 J2EE 표준 스펙 기능으로, 서블릿 컨테이너(예: Tomcat)에 의해 관리됩니다. 서블릿이 호출되기 전후에 요청과 응답을 가로채서 처리할 수 있습니다.

### 특징
- **Servlet Container에서 동작**: 스프링 컨텍스트 외부에서 동작합니다.
- **web.xml 또는 @WebFilter로 설정**: 스프링 설정과 별개로 서블릿 컨테이너에 의해 등록됩니다.
- **ServletRequest/ServletResponse 객체 조작 가능**: 요청 및 응답 객체를 직접 조작할 수 있습니다.
- **요청 URI 기반 필터링**: URL 패턴에 따라 필터를 적용할 수 있습니다.

### Filter 인터페이스
```java
public interface Filter {
    public void init(FilterConfig filterConfig) throws ServletException;
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
        throws IOException, ServletException;
    public void destroy();
}
```

## Interceptor 개요

Interceptor는 Spring MVC 프레임워크의 일부로, DispatcherServlet과 Controller 사이에서 동작합니다. 컨트롤러 호출 전후와 뷰 렌더링 후에 요청과 응답을 가로채서 처리할 수 있습니다.

### 특징
- **Spring Context 내부에서 동작**: 스프링 빈으로 등록되어 스프링의 모든 기능 활용 가능합니다.
- **Spring 설정으로 등록**: WebMvcConfigurer를 통해 등록됩니다.
- **HttpServletRequest/HttpServletResponse 객체 사용**: 요청 및 응답 객체를 사용할 수 있지만 변경은 제한적입니다.
- **Handler(Controller) 정보 접근 가능**: 실행될 컨트롤러와 메소드 정보에 접근할 수 있습니다.
- **AOP와 유사한 기능 제공**: 메소드 실행 전후에 로직을 추가할 수 있습니다.

### HandlerInterceptor 인터페이스
```java
public interface HandlerInterceptor {
    default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
        throws Exception {
        return true;
    }
    
    default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, 
        ModelAndView modelAndView) throws Exception {
    }
    
    default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, 
        Exception ex) throws Exception {
    }
}
```

## 주요 차이점

| 구분 | Filter | Interceptor |
|------|--------|-------------|
| **스펙** | Servlet 스펙 (J2EE 표준) | Spring MVC 스펙 |
| **관리 주체** | Servlet Container | Spring Container |
| **동작 시점** | DispatcherServlet 이전 | DispatcherServlet과 Controller 사이 |
| **스프링 빈 접근** | 불가능 (스프링 외부) | 가능 (스프링 내부) |
| **적용 범위** | 모든 요청 | Spring MVC 요청만 |
| **Request/Response 조작** | 가능 (객체 자체 변경 가능) | 제한적 (파라미터 등 조작) |
| **AOP 기능** | 제한적 | 강력함 (메소드 실행 전후 처리) |
| **예외 처리** | web.xml에서 처리 | @ControllerAdvice 등으로 처리 |
| **용도** | 인코딩, 보안, 로깅 등 | 인증, 권한, 로깅, 트랜잭션 등 |

## 사용 사례

### Filter 적합한 사용 사례
- **인코딩 변환**: 요청의 문자 인코딩 설정
- **XSS 방어**: 모든 요청에 대한 XSS 공격 방어
- **응답 압축**: GZIP 압축 등
- **인증 토큰 검사**: JWT 토큰 검증 등
- **CORS 설정**: Cross-Origin Resource Sharing 헤더 설정
- **로깅 및 감사**: 모든 요청에 대한 로깅

### Interceptor 적합한 사용 사례
- **인증 및 권한 검사**: 로그인 여부, 권한 확인
- **API 사용량 제한**: Rate limiting
- **로깅**: 컨트롤러 실행 시간 측정
- **트랜잭션 관리**: 트랜잭션 경계 설정
- **지역화**: 지역 설정, 언어 변경
- **테마 변경**: 사용자별 테마 적용

## 코드 예제

### Filter 예제
```java
@WebFilter("/*")
public class LoggingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingFilter.class);
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("LoggingFilter initialized");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();
        
        logger.info("Request URI: {}", requestURI);
        long startTime = System.currentTimeMillis();
        
        // 다음 필터 또는 서블릿으로 요청 전달
        chain.doFilter(request, response);
        
        long endTime = System.currentTimeMillis();
        logger.info("Response Time: {}ms", (endTime - startTime));
    }
    
    @Override
    public void destroy() {
        logger.info("LoggingFilter destroyed");
    }
}
```

### Interceptor 예제
```java
@Component
public class AuthenticationInterceptor implements HandlerInterceptor {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationInterceptor.class);
    
    @Autowired
    private UserService userService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
            throws Exception {
        String token = request.getHeader("Authorization");
        
        // 인증 토큰이 없는 경우
        if (token == null || !token.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        
        // 토큰 검증 (스프링 빈 주입 활용)
        String jwt = token.substring(7);
        if (!userService.validateToken(jwt)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        
        // 사용자 정보를 요청 속성에 저장
        request.setAttribute("userId", userService.getUserIdFromToken(jwt));
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) throws Exception {
        if (modelAndView != null) {
            // 뷰에 공통 데이터 추가
            modelAndView.addObject("serverTime", new Date());
        }
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) throws Exception {
        if (ex != null) {
            logger.error("Exception occurred: {}", ex.getMessage());
        }
    }
}

// Spring MVC 설정에 인터셉터 등록
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    
    @Autowired
    private AuthenticationInterceptor authenticationInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/public/**");
    }
}
```

## 요청 처리 흐름

웹 요청이 처리되는 전체 흐름에서 Filter와 Interceptor의 위치는 다음과 같습니다:

```
HTTP 요청 → WAS → Filter1 → Filter2 → ... → DispatcherServlet → Interceptor1 → Interceptor2 → ... → Controller → Service → Repository → DB
```

1. 클라이언트가 HTTP 요청을 보냅니다.
2. 웹 애플리케이션 서버(WAS)가 요청을 받습니다.
3. Filter 체인이 요청을 처리합니다 (여러 필터가 순차적으로 실행).
4. DispatcherServlet이 요청을 받습니다.
5. Interceptor의 preHandle 메소드가 실행됩니다.
6. Controller가 요청을 처리합니다.
7. Interceptor의 postHandle 메소드가 실행됩니다.
8. View가 렌더링됩니다.
9. Interceptor의 afterCompletion 메소드가 실행됩니다.
10. Filter 체인의 나머지 부분이 실행됩니다.
11. HTTP 응답이 클라이언트에게 반환됩니다.

## 결론

Filter와 Interceptor는 모두 웹 애플리케이션에서 공통 관심사를 분리하고 횡단 관심사(cross-cutting concerns)를 처리하는 데 유용한 메커니즘입니다. 그러나 각각의 특성과 동작 방식에 차이가 있어 적절한 상황에 맞게 선택해야 합니다.

- **Filter**는 서블릿 컨테이너 수준에서 동작하며, 모든 요청에 대해 전처리/후처리가 필요한 경우에 적합합니다.
- **Interceptor**는 스프링 MVC 내에서 동작하며, 스프링 컨텍스트의 기능을 활용해야 하거나 컨트롤러 실행 전후에 처리가 필요한 경우에 적합합니다.

실제 애플리케이션에서는 두 가지를 모두 사용하여 각각의 장점을 활용하는 것이 일반적입니다.