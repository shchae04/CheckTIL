# Swagger 사용법

## 1. 개요
Swagger는 RESTful API를 설계, 빌드, 문서화 및 사용하기 위한 오픈 소스 프레임워크입니다. API 개발 워크플로우를 간소화하고, 개발자와 사용자 간의 원활한 소통을 돕는 도구로, API 문서화를 자동화하고 API 테스트를 쉽게 할 수 있게 해줍니다. 이 문서에서는 Swagger의 기본 개념부터 실제 프로젝트에 적용하는 방법까지 상세히 알아보겠습니다.

## 2. Swagger의 주요 구성 요소

### 2.1 Swagger Specification (OpenAPI)
Swagger는 OpenAPI Specification(OAS)을 기반으로 합니다. 이는 RESTful API를 JSON 또는 YAML 형식으로 정의하는 표준입니다.

- **OpenAPI 2.0 (Swagger 2.0)**: 널리 사용되는 이전 버전
- **OpenAPI 3.0**: 더 많은 기능과 유연성을 제공하는 최신 버전

### 2.2 Swagger 도구

- **Swagger Editor**: API 정의를 작성하고 편집하는 브라우저 기반 편집기
- **Swagger UI**: API 문서를 시각적으로 표현하고 테스트할 수 있는 웹 인터페이스
- **Swagger Codegen**: API 정의에서 서버 스텁과 클라이언트 SDK를 생성하는 도구
- **Swagger Inspector**: API 호출을 테스트하고 검증하는 도구

## 3. Spring Boot에서 Swagger 설정하기

### 3.1 의존성 추가

#### Maven (pom.xml)
```xml
<!-- Swagger 2 -->
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger2</artifactId>
    <version>2.9.2</version>
</dependency>
<dependency>
    <groupId>io.springfox</groupId>
    <artifactId>springfox-swagger-ui</artifactId>
    <version>2.9.2</version>
</dependency>

<!-- 또는 OpenAPI 3 (Springdoc) -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.6.9</version>
</dependency>
```

#### Gradle (build.gradle)
```gradle
// Swagger 2
implementation 'io.springfox:springfox-swagger2:2.9.2'
implementation 'io.springfox:springfox-swagger-ui:2.9.2'

// 또는 OpenAPI 3 (Springdoc)
implementation 'org.springdoc:springdoc-openapi-ui:1.6.9'
```

### 3.2 Swagger 2 설정 (SpringFox)

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.example.controller"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("API 문서 제목")
                .description("API에 대한 설명")
                .version("1.0.0")
                .build();
    }
}
```

### 3.3 OpenAPI 3 설정 (Springdoc)

```java
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API 문서 제목")
                        .description("API에 대한 설명")
                        .version("1.0.0"));
    }
}
```

## 4. API 문서화하기

### 4.1 컨트롤러 및 모델 어노테이션 (Swagger 2)

```java
import io.swagger.annotations.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Api(tags = "사용자 관리", description = "사용자 CRUD 작업")
public class UserController {

    @ApiOperation(value = "사용자 목록 조회", notes = "모든 사용자 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
    @GetMapping
    public List<User> getAllUsers() {
        // 구현 코드
    }

    @ApiOperation(value = "사용자 조회", notes = "ID로 사용자를 조회합니다.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 404, message = "사용자 없음"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
    @GetMapping("/{id}")
    public User getUserById(
            @ApiParam(value = "사용자 ID", required = true) @PathVariable Long id) {
        // 구현 코드
    }

    @ApiOperation(value = "사용자 생성", notes = "새 사용자를 생성합니다.")
    @PostMapping
    public User createUser(
            @ApiParam(value = "사용자 정보", required = true) @RequestBody User user) {
        // 구현 코드
    }
}

@ApiModel(description = "사용자 정보")
public class User {
    @ApiModelProperty(value = "사용자 ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "사용자 이름", required = true, example = "홍길동")
    private String name;

    @ApiModelProperty(value = "이메일 주소", required = true, example = "user@example.com")
    private String email;

    // 생성자, getter, setter
}
```

### 4.2 컨트롤러 및 모델 어노테이션 (OpenAPI 3)

```java
import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@Tag(name = "사용자 관리", description = "사용자 CRUD 작업")
public class UserController {

    @Operation(summary = "사용자 목록 조회", description = "모든 사용자 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    public List<User> getAllUsers() {
        // 구현 코드
    }

    @Operation(summary = "사용자 조회", description = "ID로 사용자를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "404", description = "사용자 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{id}")
    public User getUserById(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long id) {
        // 구현 코드
    }

    @Operation(summary = "사용자 생성", description = "새 사용자를 생성합니다.")
    @PostMapping
    public User createUser(
            @Parameter(description = "사용자 정보", required = true) @RequestBody User user) {
        // 구현 코드
    }
}

@Schema(description = "사용자 정보")
public class User {
    @Schema(description = "사용자 ID", example = "1")
    private Long id;

    @Schema(description = "사용자 이름", required = true, example = "홍길동")
    private String name;

    @Schema(description = "이메일 주소", required = true, example = "user@example.com")
    private String email;

    // 생성자, getter, setter
}
```

## 5. Swagger UI 접근 및 사용하기

### 5.1 Swagger UI 접근
Spring Boot 애플리케이션을 실행한 후, 웹 브라우저에서 다음 URL로 접근할 수 있습니다:

- **Swagger 2 (SpringFox)**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI 3 (Springdoc)**: `http://localhost:8080/swagger-ui/index.html`

### 5.2 Swagger UI 사용법

1. **API 그룹 탐색**: 태그별로 그룹화된 API 목록을 확인할 수 있습니다.
2. **API 상세 정보 확인**: 각 API의 요약, 설명, 파라미터, 응답 등을 확인할 수 있습니다.
3. **API 테스트**: "Try it out" 버튼을 클릭하여 API를 직접 테스트할 수 있습니다.
   - 파라미터 입력
   - 요청 실행
   - 응답 확인 (상태 코드, 헤더, 본문)

## 6. 고급 설정

### 6.1 API 그룹화 및 정렬

```java
@Bean
public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.example"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfo())
            .tags(
                new Tag("사용자 관리", "사용자 관련 API"),
                new Tag("상품 관리", "상품 관련 API")
            )
            .useDefaultResponseMessages(false);
}
```

### 6.2 보안 설정

#### Swagger 2 (SpringFox)
```java
@Bean
public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
            // ... 기본 설정 ...
            .securityContexts(Collections.singletonList(securityContext()))
            .securitySchemes(Collections.singletonList(apiKey()));
}

private ApiKey apiKey() {
    return new ApiKey("JWT", "Authorization", "header");
}

private SecurityContext securityContext() {
    return SecurityContext.builder()
            .securityReferences(defaultAuth())
            .forPaths(PathSelectors.regex("/api/.*"))
            .build();
}

private List<SecurityReference> defaultAuth() {
    AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
    AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
    authorizationScopes[0] = authorizationScope;
    return Collections.singletonList(new SecurityReference("JWT", authorizationScopes));
}
```

#### OpenAPI 3 (Springdoc)
```java
@Bean
public OpenAPI customOpenAPI() {
    return new OpenAPI()
            .info(new Info().title("API 문서").version("1.0.0"))
            .components(new Components()
                    .addSecuritySchemes("bearer-jwt",
                            new SecurityScheme()
                                    .type(SecurityScheme.Type.HTTP)
                                    .scheme("bearer")
                                    .bearerFormat("JWT")
                                    .in(SecurityScheme.In.HEADER)
                                    .name("Authorization")))
            .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
}
```

## 7. 실제 프로젝트 적용 사례

### 7.1 API 버전 관리

```java
@Bean
public Docket apiV1() {
    return new Docket(DocumentationType.SWAGGER_2)
            .groupName("v1")
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.example.v1"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfoV1());
}

@Bean
public Docket apiV2() {
    return new Docket(DocumentationType.SWAGGER_2)
            .groupName("v2")
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.example.v2"))
            .paths(PathSelectors.any())
            .build()
            .apiInfo(apiInfoV2());
}
```

### 7.2 환경별 Swagger 활성화/비활성화

```java
@Configuration
@EnableSwagger2
@Profile({"dev", "test"}) // 개발 및 테스트 환경에서만 활성화
public class SwaggerConfig {
    // 설정 코드
}
```

## 8. 모범 사례 및 팁

### 8.1 문서화 모범 사례

- **일관된 명명 규칙 사용**: API 경로, 파라미터, 응답 모델 등에 일관된 명명 규칙 적용
- **상세한 설명 제공**: 각 API의 목적, 사용 방법, 제한 사항 등을 명확히 설명
- **예제 값 포함**: 파라미터와 응답에 실제 사용 가능한 예제 값 제공
- **오류 응답 문서화**: 가능한 모든 오류 상황과 응답 코드 문서화

### 8.2 성능 최적화

- **필요한 API만 문서화**: `paths()` 메서드를 사용하여 문서화할 API 경로 제한
- **불필요한 모델 제외**: `ignoredParameterTypes()` 메서드를 사용하여 특정 타입 제외

```java
@Bean
public Docket api() {
    return new Docket(DocumentationType.SWAGGER_2)
            .select()
            .apis(RequestHandlerSelectors.basePackage("com.example"))
            .paths(PathSelectors.ant("/api/**")) // /api/ 경로만 포함
            .build()
            .ignoredParameterTypes(Authentication.class) // 특정 타입 제외
            .apiInfo(apiInfo());
}
```

### 8.3 인터페이스를 활용한 API 문서화

컨트롤러에 직접 Swagger 어노테이션을 추가하면 코드가 복잡해지고 가독성이 떨어질 수 있습니다. 이를 개선하기 위해 인터페이스에 Swagger 어노테이션을 정의하고, 컨트롤러는 이 인터페이스를 구현하는 방식을 사용할 수 있습니다.

#### 8.3.1 인터페이스 기반 접근법 (Swagger 2)

```java
// API 인터페이스 정의
@Api(tags = "사용자 관리", description = "사용자 CRUD 작업")
public interface UserApi {

    @ApiOperation(value = "사용자 목록 조회", notes = "모든 사용자 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
    @GetMapping
    List<User> getAllUsers();

    @ApiOperation(value = "사용자 조회", notes = "ID로 사용자를 조회합니다.")
    @ApiResponses({
        @ApiResponse(code = 200, message = "성공"),
        @ApiResponse(code = 404, message = "사용자 없음"),
        @ApiResponse(code = 500, message = "서버 오류")
    })
    @GetMapping("/{id}")
    User getUserById(@ApiParam(value = "사용자 ID", required = true) @PathVariable Long id);

    @ApiOperation(value = "사용자 생성", notes = "새 사용자를 생성합니다.")
    @PostMapping
    User createUser(@ApiParam(value = "사용자 정보", required = true) @RequestBody User user);
}

// 컨트롤러 구현
@RestController
@RequestMapping("/api/users")
public class UserController implements UserApi {

    @Override
    public List<User> getAllUsers() {
        // 구현 코드
        return userService.findAll();
    }

    @Override
    public User getUserById(@PathVariable Long id) {
        // 구현 코드
        return userService.findById(id);
    }

    @Override
    public User createUser(@RequestBody User user) {
        // 구현 코드
        return userService.save(user);
    }
}
```

#### 8.3.2 인터페이스 기반 접근법 (OpenAPI 3)

```java
// API 인터페이스 정의
@Tag(name = "사용자 관리", description = "사용자 CRUD 작업")
public interface UserApi {

    @Operation(summary = "사용자 목록 조회", description = "모든 사용자 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping
    List<User> getAllUsers();

    @Operation(summary = "사용자 조회", description = "ID로 사용자를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "성공"),
        @ApiResponse(responseCode = "404", description = "사용자 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/{id}")
    User getUserById(@Parameter(description = "사용자 ID", required = true) @PathVariable Long id);

    @Operation(summary = "사용자 생성", description = "새 사용자를 생성합니다.")
    @PostMapping
    User createUser(@Parameter(description = "사용자 정보", required = true) @RequestBody User user);
}

// 컨트롤러 구현
@RestController
@RequestMapping("/api/users")
public class UserController implements UserApi {

    @Override
    public List<User> getAllUsers() {
        // 구현 코드
        return userService.findAll();
    }

    @Override
    public User getUserById(@PathVariable Long id) {
        // 구현 코드
        return userService.findById(id);
    }

    @Override
    public User createUser(@RequestBody User user) {
        // 구현 코드
        return userService.save(user);
    }
}
```

#### 8.3.3 인터페이스 기반 접근법의 장점

1. **관심사 분리**: API 문서화와 실제 구현 로직을 분리하여 코드의 가독성 향상
2. **코드 중복 감소**: 여러 컨트롤러가 동일한 API 패턴을 구현할 때 인터페이스를 재사용 가능
3. **일관성 유지**: API 문서화 스타일과 규칙을 인터페이스에서 일관되게 관리
4. **테스트 용이성**: 인터페이스를 기반으로 모의 구현체(mock)를 쉽게 생성하여 테스트 가능
5. **유지보수 향상**: API 명세가 변경될 때 인터페이스만 수정하면 되므로 유지보수 용이

#### 8.3.4 구현 시 고려사항

- Spring의 `@RequestMapping` 어노테이션은 인터페이스와 구현 클래스 모두에 적용 가능
- 인터페이스에 정의된 경로와 컨트롤러에 정의된 경로가 결합되므로 주의 필요
- 복잡한 API의 경우 인터페이스를 여러 개로 분리하여 관리하는 것이 효율적

## 9. 결론

Swagger는 RESTful API 개발 과정에서 문서화와 테스트를 간소화하는 강력한 도구입니다. 적절히 설정하고 사용하면 다음과 같은 이점을 얻을 수 있습니다:

- **개발 효율성 향상**: API 설계와 문서화를 동시에 진행
- **소통 개선**: 프론트엔드 개발자, QA 팀, 클라이언트와의 원활한 소통
- **테스트 간소화**: UI를 통한 간편한 API 테스트
- **문서 최신화**: 코드와 문서의 동기화 유지

특히, 인터페이스를 활용한 API 문서화 방식은 코드의 가독성과 유지보수성을 크게 향상시킵니다. 컨트롤러 코드를 깔끔하게 유지하면서도 완전한 API 문서화가 가능하므로, 대규모 프로젝트나 복잡한 API 구조를 가진 애플리케이션에서 이 방식을 적극 활용하는 것이 좋습니다.

Swagger를 프로젝트에 도입하여 API 개발 프로세스를 개선하고, 더 나은 API 문서화 경험을 제공하세요.

## 10. 참고 자료

- [Swagger 공식 웹사이트](https://swagger.io/)
- [OpenAPI Specification](https://spec.openapis.org/oas/latest.html)
- [SpringFox 문서](https://springfox.github.io/springfox/docs/current/)
- [Springdoc-OpenAPI 문서](https://springdoc.org/)
- [Spring Boot와 Swagger 통합 가이드](https://www.baeldung.com/swagger-2-documentation-for-spring-rest-api)
- [인터페이스 기반 API 설계 패턴](https://www.baeldung.com/spring-interface-driven-controllers)
- [클린 코드를 위한 Swagger 문서화 전략](https://reflectoring.io/spring-boot-openapi/)
