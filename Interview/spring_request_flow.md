# Spring Framework의 요청 흐름

## 1. 개요
Spring Framework에서 HTTP 요청이 처리되는 과정은 여러 컴포넌트를 거쳐 최종적으로 응답이 생성되기까지의 일련의 흐름을 따릅니다. 이 문서에서는 Spring MVC 아키텍처에서 클라이언트의 요청의 흐름을 알아보곘습니다.

## 2. 요청 처리 흐름

아래 다이어그램은 Spring MVC에서 클라이언트 요청이 처리되는 전체 흐름을 보여줍니다:

```
+----------------+     +----------------+     +-------------------+     +----------------+
| 클라이언트 요청 | --> | 웹 서버/WAS 처리 | --> | DispatcherServlet | --> | 핸들러 매핑     |
+----------------+     +----------------+     +-------------------+     +----------------+
                                                                                |
                                                                                v
+----------------+     +----------------+     +-------------------+     +----------------+
| 응답 반환       | <-- | 뷰 렌더링       | <-- | 뷰 리졸버         | <-- | 핸들러 인터셉터  |
+----------------+     +----------------+     +-------------------+     +----------------+
                                                                                |
                                                                                v
                       +----------------+     +-------------------+     +----------------+
                       | 모델 처리       | <-- | 컨트롤러 실행      | <-- | 핸들러 어댑터   |
                       +----------------+     +-------------------+     +----------------+
```

1. **클라이언트 요청**
   - 사용자가 브라우저나 클라이언트 애플리케이션에서 URL을 통해 요청을 보냅니다.
   - 이 요청은 HTTP 프로토콜을 통해 서버로 전송됩니다.

2. **웹 서버/WAS 처리**
   - Tomcat, Jetty, Undertow 등의 웹 서버/WAS가 요청을 받습니다.
   - 요청은 서블릿 컨테이너에 의해 처리되며, 서블릿 필터 체인을 통과합니다.

3. **DispatcherServlet**
   - Spring MVC의 핵심 컴포넌트인 DispatcherServlet이 모든 요청을 가로챕니다.
   - DispatcherServlet은 프론트 컨트롤러(Front Controller) 패턴을 구현하여 요청을 적절한 핸들러로 라우팅합니다.

4. **핸들러 매핑(Handler Mapping)**
   - DispatcherServlet은 HandlerMapping을 통해 요청 URL에 맞는 핸들러(컨트롤러)를 찾습니다.
   - 주로 사용되는 HandlerMapping 구현체로는 RequestMappingHandlerMapping이 있으며, 이는 @RequestMapping 어노테이션을 처리합니다.

5. **핸들러 인터셉터(Handler Interceptor)**
   - 요청이 컨트롤러에 도달하기 전과 후에 추가 처리를 수행할 수 있는 인터셉터가 실행됩니다.
   - 인터셉터는 preHandle(), postHandle(), afterCompletion() 메서드를 통해 요청 처리 전, 후, 완료 시점에 로직을 실행할 수 있습니다.

6. **핸들러 어댑터(Handler Adapter)**
   - HandlerAdapter는 다양한 유형의 핸들러(컨트롤러)를 지원하기 위한 어댑터 패턴 구현체입니다.
   - 주로 사용되는 어댑터는 RequestMappingHandlerAdapter로, @RequestMapping 메서드를 호출합니다.

7. **컨트롤러(Controller) 실행**
   - 핸들러 어댑터가 컨트롤러의 메서드를 호출합니다.
   - 컨트롤러는 비즈니스 로직을 처리하고 Model 객체에 데이터를 추가합니다.
   - 처리 결과로 ModelAndView, View 이름, @ResponseBody 데이터 등을 반환합니다.

8. **모델 처리(Model Processing)**
   - 컨트롤러가 반환한 Model 데이터는 뷰 렌더링에 사용됩니다.
   - Model은 컨트롤러와 뷰 사이의 데이터 전달 역할을 합니다.

9. **뷰 리졸버(View Resolver)**
   - ViewResolver는 컨트롤러가 반환한 뷰 이름을 실제 View 객체로 변환합니다.
   - 다양한 ViewResolver 구현체가 있으며, 주로 ThymeleafViewResolver, InternalResourceViewResolver 등이 사용됩니다.

10. **뷰 렌더링(View Rendering)**
    - View 객체는 Model 데이터를 사용하여 HTML, JSON, XML 등의 형태로 응답을 렌더링합니다.
    - @ResponseBody 어노테이션이 사용된 경우, HttpMessageConverter가 객체를 JSON/XML 등으로 변환합니다.

11. **응답 반환**
    - 렌더링된 뷰는 DispatcherServlet을 통해 클라이언트에게 HTTP 응답으로 반환됩니다.
    - 응답은 서블릿 필터 체인을 역순으로 통과한 후 클라이언트에게 전달됩니다.

## 3. 핵심 컴포넌트

아래 다이어그램은 Spring MVC의 핵심 컴포넌트들과 그들 간의 관계를 보여줍니다:

```
                           +-------------------+
                           | DispatcherServlet |
                           +-------------------+
                                     |
                                     | 사용
                                     v
+----------------+    +----------------+    +----------------+    +----------------+
| HandlerMapping | -> | HandlerAdapter | -> |   Controller   | -> |  ViewResolver  |
+----------------+    +----------------+    +----------------+    +----------------+
                                                   |                      |
                                                   v                      v
                                            +----------------+    +----------------+
                                            |     Model      | -> |      View      |
                                            +----------------+    +----------------+
```

- **DispatcherServlet**: 모든 요청을 중앙에서 처리하는 프론트 컨트롤러
- **HandlerMapping**: 요청 URL을 적절한 핸들러(컨트롤러)에 매핑
- **HandlerAdapter**: 다양한 유형의 핸들러를 지원하기 위한 어댑터
- **Controller**: 실제 비즈니스 로직을 처리하는 컴포넌트
- **ViewResolver**: 뷰 이름을 실제 View 객체로 해석
- **View**: 모델 데이터를 사용하여 응답을 렌더링

## 4. Spring WebFlux의 요청 흐름

Spring 5부터 도입된 WebFlux는 비동기-논블로킹 방식으로 요청을 처리합니다:

```
+----------------+     +----------------+     +-------------------+
|  HttpHandler   | --> |   WebFilter    | --> | DispatcherHandler |
+----------------+     +----------------+     +-------------------+
                                                        |
                                                        v
                       +-------------------+     +----------------+
                       | HandlerResultHandler | <-- | HandlerMapping |
                       +-------------------+     +----------------+
                                ^                       |
                                |                       v
                                |                +----------------+
                                +----------------| HandlerAdapter |
                                                 +----------------+
```

1. **HttpHandler**: 요청을 받아 WebHandler에 전달
2. **WebFilter**: 필터 체인을 통한 전처리/후처리
3. **DispatcherHandler**: WebFlux의 중앙 디스패처
4. **HandlerMapping**: 요청을 적절한 핸들러 함수에 매핑
5. **HandlerAdapter**: 핸들러 함수 호출
6. **HandlerResultHandler**: 핸들러 결과를 응답으로 변환

## 5. 정리

- Spring MVC의 요청 처리 흐름은 DispatcherServlet을 중심으로 여러 컴포넌트가 협력하여 요청을 처리합니다.
- 각 컴포넌트는 단일 책임 원칙에 따라 명확한 역할을 수행하며, 이를 통해 유연하고 확장 가능한 아키텍처를 제공합니다.
- Spring은 다양한 구현체와 설정을 통해 개발자가 요청 처리 흐름을 커스터마이징할 수 있도록 지원합니다.
- Spring WebFlux는 비동기-논블로킹 방식으로 요청을 처리하여 높은 동시성과 확장성을 제공합니다.
