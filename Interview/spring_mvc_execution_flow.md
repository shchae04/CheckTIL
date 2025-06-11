# Spring MVC의 실행 흐름

Spring MVC는 웹 애플리케이션을 개발하기 위한 프레임워크로, Model-View-Controller 아키텍처 패턴을 기반으로 합니다. 이 문서에서는 Spring MVC의 실행 흐름을 상세히 설명합니다.

## 1. 기본 실행 흐름

Spring MVC의 기본적인 실행 흐름은 다음과 같습니다:

1. **클라이언트 요청**: 사용자가 URL을 통해 요청을 보냅니다.
2. **DispatcherServlet**: 모든 요청은 프론트 컨트롤러인 DispatcherServlet이 가장 먼저 받습니다.
3. **핸들러 매핑**: DispatcherServlet은 HandlerMapping을 통해 요청 URL에 매핑된 핸들러(컨트롤러)를 찾습니다.
4. **핸들러 실행**: 찾아낸 핸들러는 HandlerAdapter를 통해 실행됩니다.
5. **비즈니스 로직 처리**: 컨트롤러는 서비스 계층을 호출하여 비즈니스 로직을 처리합니다.
6. **Model 생성**: 비즈니스 로직 처리 결과를 바탕으로 Model 객체에 데이터를 담습니다.
7. **ViewName 반환**: 컨트롤러는 논리적인 뷰 이름을 반환합니다.
8. **ViewResolver**: DispatcherServlet은 ViewResolver를 통해 논리적 뷰 이름을 실제 View 객체로 변환합니다.
9. **View 렌더링**: View 객체는 Model 데이터를 사용하여 클라이언트에게 보여줄 결과 화면을 렌더링합니다.
10. **응답**: 렌더링된 View가 클라이언트에게 응답으로 전송됩니다.

## 2. DispatcherServlet의 역할

DispatcherServlet은 Spring MVC의 핵심 컴포넌트로, 프론트 컨트롤러 패턴을 구현합니다. 주요 역할은 다음과 같습니다:

- 클라이언트의 모든 요청을 받아 적절한 컨트롤러로 위임
- 공통 기능의 처리 (예: 인코딩, 예외 처리 등)
- MVC 패턴의 각 컴포넌트 조율

## 3. 주요 컴포넌트

### 3.1 HandlerMapping

- 클라이언트 요청 URL을 처리할 핸들러(컨트롤러)를 찾는 역할
- 주요 구현체:
  - RequestMappingHandlerMapping: @RequestMapping 어노테이션 기반 매핑
  - SimpleUrlHandlerMapping: URL 패턴 기반 매핑

### 3.2 HandlerAdapter

- 찾아낸 핸들러를 실행하는 역할
- 다양한 형태의 핸들러를 실행할 수 있도록 어댑터 패턴 적용
- 주요 구현체:
  - RequestMappingHandlerAdapter: @RequestMapping 메소드 실행
  - HttpRequestHandlerAdapter: HttpRequestHandler 인터페이스 구현체 실행

### 3.3 ViewResolver

- 컨트롤러가 반환한 뷰 이름으로부터 실제 View 객체를 찾아주는 역할
- 주요 구현체:
  - InternalResourceViewResolver: JSP 뷰 해석
  - ThymeleafViewResolver: Thymeleaf 템플릿 해석
  - FreeMarkerViewResolver: FreeMarker 템플릿 해석

## 4. 상세 실행 흐름 다이어그램

```
클라이언트 → HTTP 요청 → 
  ↓
Filter Chain (서블릿 필터)
  ↓
DispatcherServlet → HandlerMapping (요청 URL에 맞는 핸들러 찾기)
  ↓
Interceptor (preHandle)
  ↓
HandlerAdapter → Controller (실제 요청 처리)
  ↓                ↓
  ↓           Service (비즈니스 로직)
  ↓                ↓
  ↓           Repository (데이터 접근)
  ↓
Interceptor (postHandle)
  ↓
ViewResolver (뷰 이름 → 실제 View 객체)
  ↓
View 렌더링 (Model 데이터 + 템플릿)
  ↓
Interceptor (afterCompletion)
  ↓
HTTP 응답 → 클라이언트
```

## 5. @RequestMapping 어노테이션

Spring MVC에서는 @RequestMapping 어노테이션을 사용하여 HTTP 요청을 컨트롤러의 메소드에 매핑합니다:

```java
@Controller
@RequestMapping("/users")
public class UserController {

    @GetMapping("/{id}")
    public String getUser(@PathVariable Long id, Model model) {
        // 사용자 조회 로직
        User user = userService.findById(id);
        model.addAttribute("user", user);
        return "user/detail";  // 뷰 이름 반환
    }
    
    @PostMapping
    public String createUser(@ModelAttribute User user) {
        // 사용자 생성 로직
        userService.save(user);
        return "redirect:/users";  // 리다이렉트
    }
}
```

## 6. Filter와 Interceptor

Spring MVC 실행 흐름에서 Filter와 Interceptor는 요청/응답을 전처리/후처리하는 중요한 역할을 합니다:

### 6.1 Filter (서블릿 필터)

- 서블릿 스펙에 정의된 기능으로 DispatcherServlet에 요청이 전달되기 전/후에 실행
- web.xml 또는 @WebFilter 어노테이션으로 설정
- 주요 용도: 인코딩 변환, XSS 방어, 인증/인가 등

### 6.2 Interceptor (스프링 인터셉터)

- Spring MVC에서 제공하는 기능으로 컨트롤러 실행 전/후에 실행
- HandlerInterceptor 인터페이스 구현
- 주요 메소드:
  - preHandle(): 컨트롤러 실행 전
  - postHandle(): 컨트롤러 실행 후, 뷰 렌더링 전
  - afterCompletion(): 뷰 렌더링 후

## 7. 예외 처리

Spring MVC에서는 다양한 방식으로 예외를 처리할 수 있습니다:

1. **@ExceptionHandler**: 특정 컨트롤러 내에서 발생하는 예외 처리
2. **@ControllerAdvice/@RestControllerAdvice**: 전역 예외 처리
3. **HandlerExceptionResolver**: 커스텀 예외 처리 로직 구현

## 8. 비동기 처리

Spring MVC 3.2부터 비동기 요청 처리를 지원합니다:

- **Callable 반환**: 별도 스레드에서 작업 실행
- **DeferredResult 반환**: 비동기 이벤트로 결과 제공
- **CompletableFuture 반환**: Java 8 비동기 API 활용

## 9. 결론

Spring MVC의 실행 흐름은 DispatcherServlet을 중심으로 다양한 컴포넌트들이 유기적으로 동작하는 구조입니다. 이러한 구조는 관심사의 분리를 통해 유지보수성을 높이고, 확장성 있는 웹 애플리케이션 개발을 가능하게 합니다.

## 참고 자료

- [Spring 공식 문서 - Web MVC framework](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc)
- [Spring 공식 문서 - DispatcherServlet](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/servlet/DispatcherServlet.html)