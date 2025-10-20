# RequestBody VS ModelAttribute의 차이점

## 1. 한 줄 정의
`@RequestBody`는 HTTP 요청 본문을 직렬화하여 객체로 변환하고, `@ModelAttribute`는 HTTP 요청 파라미터(쿼리스트링, 폼 데이터)를 객체로 바인딩한다. 즉, 데이터의 **출처**와 **처리 방식**이 다르다.

---

## 2. 핵심 차이점 비교표

| 구분 | @RequestBody | @ModelAttribute |
|------|-------------|-----------------|
| **데이터 출처** | HTTP 요청 Body (JSON, XML 등) | Query String, Form Data |
| **Content-Type** | application/json (주로) | application/x-www-form-urlencoded, multipart/form-data |
| **바인딩 방식** | HttpMessageConverter (역직렬화) | WebDataBinder (프로퍼티 바인딩) |
| **HTTP 메서드** | POST, PUT, PATCH 주로 | GET, POST 모두 가능 |
| **기본값** | 필수 (생략 시 에러) | 선택사항 |
| **중첩 객체** | 지원 (깊은 구조 가능) | 제한적 지원 |
| **타입 변환** | JSON 파서를 통한 변환 | PropertyEditor/Converter를 통한 변환 |

---

## 3. 상세 설명 및 코드 예시

### 3-1. @RequestBody

**개념**: HTTP 요청 본문(Body)의 데이터를 객체로 직렬화하는 방식

**언제 사용하나?**
- RESTful API에서 JSON 데이터를 받을 때
- 복잡한 객체 구조를 전송할 때
- 클라이언트가 JSON 형식의 데이터를 보낼 때

**예시 코드**:
```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    // POST http://localhost:8080/api/users
    // Content-Type: application/json
    // Body: {"name":"홍길동", "email":"hong@example.com", "age":30}
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        // user 객체에 Body의 데이터가 자동으로 바인딩됨
        System.out.println(user.getName());    // "홍길동"
        System.out.println(user.getEmail());   // "hong@example.com"
        return ResponseEntity.ok(user);
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class User {
    private String name;
    private String email;
    private int age;
}
```

**처리 과정**:
1. 요청의 `Content-Type: application/json` 확인
2. `HttpMessageConverter` (기본: `MappingJackson2HttpMessageConverter`) 사용
3. JSON 문자열을 Java 객체로 역직렬화
4. 컨트롤러 메서드에 바인딩

---

### 3-2. @ModelAttribute

**개념**: HTTP 요청의 파라미터(쿼리스트링, 폼 데이터)를 객체의 프로퍼티에 바인딩하는 방식

**언제 사용하나?**
- HTML 폼 데이터를 받을 때
- Query String으로 데이터를 받을 때
- 간단한 데이터 바인딩이 필요할 때
- GET 요청에서 파라미터를 객체로 받을 때

**예시 코드**:
```java
@Controller
@RequestMapping("/users")
public class UserController {

    // GET http://localhost:8080/users/search?name=홍길동&email=hong@example.com
    @GetMapping("/search")
    public String searchUser(@ModelAttribute User user, Model model) {
        // user 객체에 Query String 데이터가 자동으로 바인딩됨
        System.out.println(user.getName());    // "홍길동"
        System.out.println(user.getEmail());   // "hong@example.com"
        model.addAttribute("user", user);
        return "user/details";
    }

    // HTML 폼 데이터 처리
    // POST http://localhost:8080/users
    // Content-Type: application/x-www-form-urlencoded
    // Body: name=홍길동&email=hong@example.com&age=30
    @PostMapping
    public String createUser(@ModelAttribute User user) {
        // user 객체에 폼 데이터가 자동으로 바인딩됨
        System.out.println(user.getName());    // "홍길동"
        return "redirect:/users/" + user.getId();
    }
}

@Data
@AllArgsConstructor
@NoArgsConstructor
class User {
    private String name;
    private String email;
    private int age;
}
```

**처리 과정**:
1. 요청의 파라미터 추출 (Query String 또는 Form Data)
2. `WebDataBinder`를 사용하여 객체 생성
3. 파라미터 이름과 객체의 프로퍼티 이름 매칭
4. `PropertyEditor` 또는 `Converter`를 사용하여 타입 변환
5. 컨트롤러 메서드에 바인딩

---

## 4. 실제 사용 시나리오별 비교

### 시나리오 1: JSON API 요청

```java
// ✅ @RequestBody 사용
@PostMapping("/api/products")
public ResponseEntity<Product> createProduct(@RequestBody Product product) {
    return ResponseEntity.ok(productService.save(product));
}

// ❌ @ModelAttribute 사용 (불가능)
// JSON 데이터를 폼 파라미터로 읽을 수 없음
```

### 시나리오 2: HTML 폼 제출

```java
// ❌ @RequestBody 사용 (비효율적)
@PostMapping("/form")
public String handleForm(@RequestBody String formData) {
    // 수동으로 파싱 필요, 번거로움
    return "success";
}

// ✅ @ModelAttribute 사용
@PostMapping("/form")
public String handleForm(@ModelAttribute User user) {
    // 자동으로 바인딩됨, 편리함
    return "success";
}
```

### 시나리오 3: Query String 처리

```java
// GET /search?keyword=spring&page=1

// ❌ @RequestBody 사용 (불가능)
// Query String은 Body가 아니므로 사용 불가

// ✅ @ModelAttribute 사용
@GetMapping("/search")
public String search(@ModelAttribute SearchRequest request) {
    // keyword=spring, page=1이 자동으로 바인딩됨
    return "search-results";
}
```

---

## 5. 백엔드 개발자 관점에서의 이해사항

### 5-1. 타입 변환 메커니즘

**@RequestBody의 타입 변환**:
```java
// 요청: {"age": 30, "joinDate": "2024-10-20"}
// JSON 파서가 타입 정보를 읽어 적절히 변환
@PostMapping
public void create(@RequestBody User user) {
    // user.age는 int 타입
    // user.joinDate는 LocalDate 타입 (등록된 컨버터 사용)
}
```

**@ModelAttribute의 타입 변환**:
```java
// 요청: ?age=30&joinDate=2024-10-20
// PropertyEditor 또는 Converter를 사용하여 변환
@GetMapping
public void search(@ModelAttribute User user) {
    // "30" (String) → 30 (int)
    // "2024-10-20" (String) → LocalDate
}
```

### 5-2. 검증(Validation) 연동

```java
// 두 경우 모두 @Valid 또는 @Validated로 검증 가능
@PostMapping("/api/users")
public ResponseEntity<User> createWithJson(
    @Valid @RequestBody User user  // JSON 검증
) {
    return ResponseEntity.ok(user);
}

@PostMapping("/form")
public String createWithForm(
    @Valid @ModelAttribute User user  // 폼 데이터 검증
) {
    return "success";
}
```

### 5-3. 성능 고려사항

| 구분 | @RequestBody | @ModelAttribute |
|------|-------------|-----------------|
| **파싱 속도** | JSON 파서 사용 (중간 정도) | 간단한 파라미터 분석 (빠름) |
| **메모리** | JSON 전체 로드 (더 많음) | 파라미터만 처리 (적음) |
| **용도** | 복잡한 데이터 구조 | 간단한 데이터 |

---

## 6. 주의사항 및 Best Practices

### 6-1. @RequestBody 사용 시 주의점

```java
// ❌ 잘못된 사용
@PostMapping("/users")
public void createUser(
    @RequestBody User user,
    @RequestBody Address address  // 에러! 하나의 Body만 가능
) {}

// ✅ 올바른 사용
@PostMapping("/users")
public void createUser(@RequestBody User user) {
    // user 객체에 address가 포함되어 있음
}
```

### 6-2. @ModelAttribute 사용 시 주의점

```java
// ❌ 복잡한 중첩 구조는 바인딩 어려움
class User {
    private String name;
    private Address address;  // 복잡한 중첩
}

// GET /users?name=hong&address.city=seoul  <- 제대로 안 될 수 있음

// ✅ 간단한 구조 권장
class User {
    private String name;
    private String email;
    private int age;
}
```

### 6-3. Content-Type 명시

```java
// REST API는 명확하게 Content-Type 지정
@PostMapping(
    value = "/api/users",
    consumes = "application/json"  // Content-Type 명시
)
public ResponseEntity<User> createUser(@RequestBody User user) {
    return ResponseEntity.ok(user);
}
```

---

## 7. 예상 면접 질문

1. **@RequestBody와 @ModelAttribute의 근본적인 차이는?**
   - 답: 데이터 출처(Body vs 파라미터)와 처리 방식(직렬화 vs 바인딩)이 다르다.

2. **GET 요청에서는 왜 @RequestBody를 사용하지 않나요?**
   - 답: GET 요청은 Body가 없거나 무시되기 때문에, Query String 파라미터를 받으려면 @ModelAttribute를 사용해야 한다.

3. **@ModelAttribute를 생략해도 되는 경우는?**
   - 답: 메서드에 파라미터가 SimpleType이 아닌 복합 타입일 경우, 자동으로 @ModelAttribute가 적용된다 (Spring 3.2+).

4. **복잡한 JSON 구조를 받을 때는 어떤 것을 사용해야 하나요?**
   - 답: @RequestBody를 사용해야 한다. 깊은 중첩 구조를 쉽게 처리할 수 있다.

5. **폼 데이터와 JSON을 동시에 받을 수 있나요?**
   - 답: 불가능하다. 한 요청에는 하나의 Content-Type만 있기 때문에, 하나의 방식만 사용 가능하다.

---

## 8. 핵심 요약

### 8-1. 선택 기준
- **JSON API (RESTful)** → `@RequestBody`
- **HTML 폼 제출** → `@ModelAttribute`
- **Query String 조회** → `@ModelAttribute`

### 8-2. 기술적 이해
- `@RequestBody`: HttpMessageConverter를 통한 역직렬화
- `@ModelAttribute`: WebDataBinder를 통한 프로퍼티 바인딩

### 8-3. 실무 포인트
- 명확한 API 계약(Content-Type) 정의
- 타입 변환 설정 미리 준비
- 검증 규칙 일관되게 적용
- 복잡도와 성능을 고려한 선택

---

## 참고 자료

- Spring Framework 공식 문서: Data Binding
- Spring MVC 요청 처리 흐름
- HttpMessageConverter와 PropertyEditor의 역할