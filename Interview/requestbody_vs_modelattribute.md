# @RequestBody와 @ModelAttribute의 차이점

## 1. 개요
Spring MVC에서 클라이언트로부터 데이터를 받는 방법은 다양합니다. 그 중에서도 가장 많이 사용되는 두 가지 어노테이션인 `@RequestBody`와 `@ModelAttribute`의 차이점과 각각의 사용 사례에 대해 알아보겠습니다.

## 2. 기본 개념

### 2.1 @RequestBody
`@RequestBody`는 HTTP 요청의 본문(body)에 담긴 데이터를 자바 객체로 변환하는 역할을 합니다. 주로 JSON이나 XML 형태의 데이터를 객체로 역직렬화(deserialization)할 때 사용됩니다.

### 2.2 @ModelAttribute
`@ModelAttribute`는 HTTP 요청 파라미터(query string, form data)를 자바 객체로 바인딩하는 역할을 합니다. 주로 HTML 폼 데이터를 객체에 매핑할 때 사용됩니다.

## 3. 데이터 처리 방식 비교

### 3.1 @RequestBody의 처리 방식
1. HTTP 요청의 본문(body)을 읽습니다.
2. `HttpMessageConverter`를 사용하여 요청 본문을 자바 객체로 변환합니다.
3. 주로 `MappingJackson2HttpMessageConverter`가 JSON을 자바 객체로 변환합니다.
4. 객체 변환 과정에서 오류가 발생하면 `HttpMessageNotReadableException`이 발생합니다.

```
HTTP 요청 본문(JSON/XML) → HttpMessageConverter → 자바 객체
```

### 3.2 @ModelAttribute의 처리 방식
1. 요청 파라미터(query string, form data)를 읽습니다.
2. 빈 객체를 생성합니다 (기본 생성자 필요).
3. 요청 파라미터의 이름과 객체의 프로퍼티 이름을 매칭하여 값을 설정합니다.
4. 데이터 바인딩 과정에서 타입 변환이 필요한 경우 `PropertyEditor` 또는 `Converter`를 사용합니다.
5. 검증(Validation) 작업이 필요한 경우 수행합니다.

```
HTTP 요청 파라미터 → 객체 생성 → 프로퍼티 바인딩 → 검증 → 자바 객체
```

## 4. 사용 사례

### 4.1 @RequestBody 사용 사례
- **RESTful API**: JSON/XML 형태의 데이터를 주고받는 API 개발
- **비동기 요청 처리**: AJAX 요청의 JSON 데이터 처리
- **복잡한 객체 구조**: 중첩된 객체 구조를 한 번에 받을 때
- **Raw 데이터 처리**: 텍스트, 바이너리 등의 원시 데이터 처리

```java
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // user 객체는 HTTP 요청 본문의 JSON 데이터로부터 생성됨
        userService.save(user);
        return ResponseEntity.ok(user);
    }
}
```

### 4.2 @ModelAttribute 사용 사례
- **HTML 폼 처리**: 전통적인 폼 제출 처리
- **쿼리 파라미터 바인딩**: URL의 쿼리 파라미터를 객체로 변환
- **부분 업데이트**: 일부 필드만 업데이트할 때
- **파일 업로드**: 멀티파트 요청 처리

```java
@Controller
@RequestMapping("/users")
public class UserController {

    @PostMapping("/new")
    public String createUser(@ModelAttribute User user) {
        // user 객체는 폼 데이터로부터 생성됨
        userService.save(user);
        return "redirect:/users";
    }
    
    // @ModelAttribute는 생략 가능
    @GetMapping("/search")
    public String searchUsers(UserSearchCriteria criteria, Model model) {
        // criteria 객체는 쿼리 파라미터로부터 생성됨
        List<User> users = userService.search(criteria);
        model.addAttribute("users", users);
        return "user/list";
    }
}
```

## 5. 주요 차이점

### 5.1 데이터 소스
- **@RequestBody**: HTTP 요청 본문(body)에서 데이터를 읽음
- **@ModelAttribute**: 쿼리 파라미터, 폼 데이터, 세션 속성 등에서 데이터를 읽음

### 5.2 Content-Type
- **@RequestBody**: 주로 `application/json`, `application/xml` 등의 Content-Type 처리
- **@ModelAttribute**: 주로 `application/x-www-form-urlencoded`, `multipart/form-data` 등의 Content-Type 처리

### 5.3 객체 생성 방식
- **@RequestBody**: 요청 본문 전체를 하나의 객체로 변환 (필드 누락 시 null 또는 기본값)
- **@ModelAttribute**: 기본 생성자로 객체 생성 후 개별 필드 바인딩 (존재하는 필드만 덮어씀)

### 5.4 유효성 검증
- **@RequestBody**: `@Valid` 또는 `@Validated`와 함께 사용하여 객체 변환 후 검증
- **@ModelAttribute**: `@Valid` 또는 `@Validated`와 함께 사용하여 바인딩 후 검증, 바인딩 오류는 `BindingResult`로 처리 가능

### 5.5 오류 처리
- **@RequestBody**: 변환 실패 시 `HttpMessageNotReadableException` 발생, 일반적으로 400 Bad Request 응답
- **@ModelAttribute**: 바인딩 실패 시 `BindException` 발생, `BindingResult` 파라미터로 오류 처리 가능

## 6. 고급 사용법

### 6.1 @RequestBody와 검증
```java
@PostMapping("/api/users")
public ResponseEntity<?> createUser(@Valid @RequestBody User user, BindingResult result) {
    if (result.hasErrors()) {
        return ResponseEntity.badRequest().body(result.getAllErrors());
    }
    userService.save(user);
    return ResponseEntity.ok(user);
}
```

### 6.2 @ModelAttribute와 검증
```java
@PostMapping("/users/new")
public String createUser(@Valid @ModelAttribute User user, BindingResult result) {
    if (result.hasErrors()) {
        return "user/form";  // 오류가 있으면 폼 페이지로 돌아감
    }
    userService.save(user);
    return "redirect:/users";
}
```

### 6.3 중첩 객체 처리
#### @RequestBody의 중첩 객체 처리
```java
public class UserRegistration {
    private User user;
    private Address address;
    // getters and setters
}

@PostMapping("/api/register")
public ResponseEntity<?> register(@RequestBody UserRegistration registration) {
    // user와 address 객체가 모두 포함된 registration 객체 처리
    userService.register(registration);
    return ResponseEntity.ok().build();
}
```

#### @ModelAttribute의 중첩 객체 처리
```java
@PostMapping("/register")
public String register(@ModelAttribute UserRegistration registration) {
    // HTML 폼에서 user.name, address.city 등의 형태로 전송된 데이터가 바인딩됨
    userService.register(registration);
    return "redirect:/welcome";
}
```

## 7. 성능 고려사항

### 7.1 @RequestBody
- 대용량 JSON/XML 처리 시 메모리 사용량 증가 가능
- Jackson, JAXB 등의 라이브러리 성능에 영향 받음
- 전체 객체를 한 번에 역직렬화하므로 대규모 객체의 경우 성능 이슈 발생 가능

### 7.2 @ModelAttribute
- 개별 필드 바인딩 방식으로 대용량 데이터에 효율적
- 타입 변환 작업이 많을 경우 성능 저하 가능
- 기본적으로 리플렉션을 사용하므로 약간의 오버헤드 존재

## 8. 베스트 프랙티스

### 8.1 @RequestBody 사용 시 권장사항
- API 문서에 요청 본문 형식을 명확히 명시
- 필수 필드에 대한 검증 로직 추가
- 예외 처리 핸들러 구현 (@ExceptionHandler)
- 대용량 데이터의 경우 페이징 또는 스트리밍 고려

### 8.2 @ModelAttribute 사용 시 권장사항
- 폼 필드와 객체 프로퍼티 이름 일치시키기
- 바인딩 오류 처리를 위한 BindingResult 파라미터 추가
- XSS 방지를 위한 입력 값 검증
- 중첩 객체의 경우 명확한 필드 이름 지정 (user.name, user.email 등)

## 9. 결론

`@RequestBody`와 `@ModelAttribute`는 각각 다른 상황에서 유용하게 사용됩니다:

- **@RequestBody**는 JSON/XML과 같은 구조화된 데이터를 객체로 변환할 때 적합하며, 주로 RESTful API에서 사용됩니다.
- **@ModelAttribute**는 폼 데이터나 쿼리 파라미터를 객체로 바인딩할 때 적합하며, 주로 전통적인 웹 애플리케이션에서 사용됩니다.

두 어노테이션의 특성을 이해하고 적절한 상황에 맞게 선택하여 사용하는 것이 중요합니다. 특히 데이터의 출처, 형식, 크기, 그리고 오류 처리 방식을 고려하여 결정해야 합니다.

## 참고 자료
- [Spring 공식 문서 - @RequestBody](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/RequestBody.html)
- [Spring 공식 문서 - @ModelAttribute](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/bind/annotation/ModelAttribute.html)
- [Spring 공식 문서 - Web on Servlet Stack](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-methods)