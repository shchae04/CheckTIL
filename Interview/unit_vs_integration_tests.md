# 단위 테스트와 통합 테스트의 차이점

단위 테스트(Unit Test)와 통합 테스트(Integration Test)는 소프트웨어 테스트의 두 가지 중요한 유형으로, 각각 다른 목적과 범위를 가지고 있습니다. 이 문서에서는 두 테스트 유형의 주요 차이점과 각각의 장단점, 그리고 효과적인 테스트 전략에 대해 알아보겠습니다.

## 단위 테스트(Unit Test)

### 정의
단위 테스트는 소프트웨어의 개별 구성 요소나 모듈이 의도한 대로 작동하는지 확인하는 테스트입니다. 여기서 '단위'는 일반적으로 함수, 메서드, 클래스 등의 작은 코드 조각을 의미합니다.

### 특징
- **격리성**: 외부 의존성(데이터베이스, 파일 시스템, 네트워크 등)을 모의 객체(Mock)나 스텁(Stub)으로 대체하여 테스트 대상을 격리합니다.
- **빠른 실행**: 외부 의존성이 없어 실행 속도가 빠릅니다.
- **집중적인 테스트**: 특정 기능이나 로직에 집중하여 테스트합니다.
- **자동화 용이성**: 자동화하기 쉽고, CI/CD 파이프라인에 통합하기 적합합니다.

### 예시 코드 (Java + JUnit)
```java
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CalculatorTest {
    
    @Test
    public void testAdd() {
        Calculator calculator = new Calculator();
        int result = calculator.add(3, 5);
        assertEquals(8, result, "3 + 5 should equal 8");
    }
    
    @Test
    public void testMultiply() {
        Calculator calculator = new Calculator();
        int result = calculator.multiply(3, 5);
        assertEquals(15, result, "3 * 5 should equal 15");
    }
}
```

## 통합 테스트(Integration Test)

### 정의
통합 테스트는 여러 단위(모듈, 컴포넌트)가 함께 작동할 때 올바르게 상호작용하는지 확인하는 테스트입니다. 이는 단위 테스트에서 모의 처리된 의존성을 실제 구현체로 대체하여 시스템의 여러 부분이 함께 작동하는 방식을 테스트합니다.

### 특징
- **실제 의존성**: 실제 데이터베이스, 파일 시스템, 네트워크 등의 외부 시스템과의 상호작용을 테스트합니다.
- **느린 실행**: 외부 시스템과의 상호작용으로 인해 단위 테스트보다 실행 속도가 느립니다.
- **넓은 범위**: 여러 컴포넌트 간의 상호작용과 데이터 흐름을 테스트합니다.
- **환경 설정 필요**: 테스트 환경 설정이 복잡할 수 있습니다(예: 테스트 데이터베이스 설정).

### 예시 코드 (Spring Boot + JUnit)
```java
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    public void testCreateUser() {
        // 사용자 생성 요청
        UserDto newUser = new UserDto("testuser", "password123", "test@example.com");
        ResponseEntity<UserDto> response = restTemplate.postForEntity("/api/users", newUser, UserDto.class);
        
        // 응답 검증
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(newUser.getUsername(), response.getBody().getUsername());
        assertEquals(newUser.getEmail(), response.getBody().getEmail());
    }
}
```

## 단위 테스트 vs 통합 테스트: 주요 차이점

| 특성 | 단위 테스트 | 통합 테스트 |
|------|------------|-------------|
| 범위 | 개별 함수/메서드/클래스 | 여러 컴포넌트/모듈의 상호작용 |
| 의존성 | 모의 객체(Mock)/스텁(Stub) 사용 | 실제 의존성 사용 |
| 실행 속도 | 빠름 | 상대적으로 느림 |
| 복잡성 | 낮음 | 높음 |
| 환경 설정 | 간단함 | 복잡할 수 있음 |
| 목적 | 개별 기능의 정확성 검증 | 컴포넌트 간 상호작용 검증 |
| 유지보수 | 쉬움 | 상대적으로 어려움 |

## 테스트 피라미드

테스트 피라미드는 효과적인 테스트 전략을 시각화한 개념으로, 다양한 유형의 테스트를 적절한 비율로 구성하는 방법을 제시합니다.

```
         /\
        /  \
       /    \
      / E2E  \
     /--------\
   / 통합 테스트 \
  /--------------\
/    단위 테스트    \
```

- **기반(하단)**: 단위 테스트 - 가장 많은 수의 테스트
- **중간**: 통합 테스트 - 중간 수준의 테스트
- **정점(상단)**: E2E(End-to-End) 테스트 - 가장 적은 수의 테스트

이 구조는 빠르고 유지보수가 쉬운 단위 테스트를 기반으로 하고, 더 복잡하고 느린 통합 테스트와 E2E 테스트는 상대적으로 적게 구성하는 것이 효율적임을 나타냅니다.

## 효과적인 테스트 전략

### 단위 테스트 모범 사례
1. **작고 집중적인 테스트 작성**: 각 테스트는 하나의 기능만 검증해야 합니다.
2. **의존성 격리**: 외부 의존성은 모의 객체로 대체하여 테스트를 격리합니다.
3. **경계 조건 테스트**: 정상 케이스뿐만 아니라 경계 조건과 예외 상황도 테스트합니다.
4. **테스트 가독성**: 테스트 코드는 명확하고 이해하기 쉽게 작성합니다.

### 통합 테스트 모범 사례
1. **중요 경로 집중**: 모든 통합 지점을 테스트하기보다 중요한 경로에 집중합니다.
2. **테스트 데이터 관리**: 테스트 데이터를 효과적으로 설정하고 정리하는 방법을 구현합니다.
3. **환경 일관성**: 테스트 환경이 일관되게 유지되도록 합니다.
4. **트랜잭션 관리**: 데이터베이스 테스트에서 트랜잭션을 적절히 관리합니다.

## 언제 어떤 테스트를 사용해야 할까?

### 단위 테스트가 적합한 경우
- 복잡한 비즈니스 로직 검증
- 알고리즘 정확성 검증
- 경계 조건 및 예외 처리 검증
- 빠른 피드백이 필요한 개발 과정

### 통합 테스트가 적합한 경우
- 데이터베이스 상호작용 검증
- API 엔드포인트 기능 검증
- 서비스 간 통신 검증
- 외부 시스템과의 통합 검증

## 결론

단위 테스트와 통합 테스트는 상호 보완적인 테스트 유형으로, 효과적인 테스트 전략은 두 가지를 적절히 조합하여 사용하는 것입니다. 단위 테스트는 개별 컴포넌트의 정확성을 보장하고, 통합 테스트는 시스템의 여러 부분이 함께 올바르게 작동하는지 확인합니다. 테스트 피라미드 원칙에 따라 단위 테스트를 기반으로 하고 통합 테스트로 보완하는 접근 방식이 효율적인 테스트 전략을 구축하는 데 도움이 됩니다.