# 기술 부채와 리팩토링: 실전 경험

## 배경

프로젝트 초반, 빠른 개발을 위해 기능 구현에 집중했다. 백엔드와 웹 화면 개발을 동시에 진행하면서, 하나의 Controller가 View도 내려주고 Data도 내려주는 구조가 만들어졌다.

당시엔 최선이라고 생각했지만, 시간이 지나 새로운 모듈을 도입하려고 하니 이러한 구조가 문제가 되었다.

### 초기 구조의 문제점

```java
@RequestMapping("/api")
class TotalController(
    private final TotalService service;
) {
    @GetMapping("/form")
    public String form(){
        return "form"; // HTML View 이름을 반환
    }

    @GetMapping("/detail")
    public ResponseEntity<ResponseDto> getDetail(@RequestParam("id") String id){
        ResponseDto dto = service.find(id);
        return Result.ok(dto);
    }

    // 저장, 전송, 조회, 통계 등 모든 기능이 하나의 컨트롤러에...
}
```

**문제점:**
1. View 반환과 API Data 반환이 하나의 Controller에 혼재
2. RESTful하지 않은 URI 설계
3. DataTables 라이브러리에 의존적인 API Response 구조
4. 비정규화된 테이블 구조로 인한 데이터 중복
5. 새로운 모듈 추가 시 복사-붙여넣기 방식의 개발

## 딜레마: 기술 부채를 안고 갈 것인가, 개발과 리팩토링을 동시에 진행할 것인가?

### 선택의 순간

새로운 모듈을 개발해야 하는 시점에서 두 가지 선택지가 있었다:

**선택지 1: 기존 모듈 복사-붙여넣기**
- 빠른 개발 가능
- 당장의 일정은 지킬 수 있음
- 하지만 기술 부채가 복리로 쌓임

**선택지 2: 개발과 리팩토링 동시 진행**
- 초기 개발 속도는 더딤
- 장기적으로는 유지보수성 향상
- 향후 모듈 추가 시 재사용 가능

### 동시 진행을 선택한 이유

1. **개발 기간이 촉박하여 빠르게 기능을 제공해야 한다**
   - 새로운 모듈은 기존 방식으로 빠르게 개발
   - 병렬로 리팩토링 작업 진행

2. **새로운 모듈도 같은 방식으로 개발하면 또 다른 기술 부채가 쌓인다**
   - 복사-붙여넣기는 일시적 해결책
   - 근본적인 구조 개선 필요

3. **또 다른 새로운 모듈이 추가될 수 있다**
   - 확장 가능한 구조로 전환 필요
   - 재사용 가능한 API 설계

## 리팩토링 전략

### 핵심 목표

1. **View와 API의 분리**
   - ViewController: HTML 페이지 반환
   - RestController: JSON 데이터 반환

2. **Resource 중심의 URI 설계**
   - RESTful API 원칙 적용
   - 명확한 엔드포인트 분리

3. **DB Table 정규화**
   - 데이터 중복 제거
   - 관계 정립

### 리팩토링 후 구조

```java
// View 전용 Controller
@Controller
@RequestMapping("/views")
class FormViewController {
    @GetMapping("/form")
    public String formPage() {
        return "form"; // HTML View 반환
    }
}

// API 전용 Controller
@RestController
@RequestMapping("/api/v1/resources")
class ResourceApiController {
    private final ResourceService service;

    @GetMapping("/{id}")
    public ResponseEntity<ResourceDto> getResource(@PathVariable Long id) {
        ResourceDto dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<ResourceDto> createResource(@RequestBody CreateRequest request) {
        ResourceDto dto = service.create(request);
        return ResponseEntity.created(/* location */).body(dto);
    }

    // 명확한 책임 분리
}
```

### 데이터 응답 구조 개선

**Before: DataTables 의존적**
```java
{
    "draw": 1,
    "recordsTotal": 100,
    "recordsFiltered": 10,
    "data": [/* DataTables 전용 구조 */]
}
```

**After: 범용적인 페이징 구조**
```java
{
    "content": [/* 실제 데이터 */],
    "pageable": {
        "pageNumber": 0,
        "pageSize": 20,
        "sort": { /* ... */ }
    },
    "totalElements": 100,
    "totalPages": 5
}
```

## 결과 및 교훈

### 정량적 효과

- **초기 개발**: 리팩토링으로 인해 첫 모듈은 약 30% 더 오래 걸림
- **두 번째 모듈**: 재사용 가능한 API로 개발 시간 50% 단축
- **세 번째 모듈**: 개발 시간 70% 단축
- **유지보수**: 버그 수정 및 기능 추가 시간 대폭 감소

### 정성적 효과

1. **코드 가독성 향상**
   - 명확한 책임 분리
   - 의도가 명확한 코드

2. **테스트 용이성**
   - 독립적인 컴포넌트 테스트 가능
   - Mock 객체 활용 쉬워짐

3. **협업 효율성**
   - 팀원들이 코드 이해하기 쉬워짐
   - 온보딩 시간 단축

## 핵심 교훈

### 1. 기술 부채는 느껴지지 않게 온다

기술 부채는 마치 복리 이자처럼 쌓인다. 처음에는 "나중에 리팩토링하자"고 생각하지만, 그 "나중"은 오지 않는다. 오히려 부채는 눈덩이처럼 불어나 결국 원금에 이자를 복리로 얻어맞게 된다.

### 2. "나중에 리팩토링"은 거짓말

"나중에 리팩토링하자"는 대부분 실현되지 않는다. 다음과 같은 이유 때문이다:

- 항상 급한 기능 개발이 우선순위가 됨
- 시간이 지날수록 코드에 대한 이해도가 떨어짐
- 의존성이 더 복잡해져 리팩토링이 더 어려워짐
- "돌아가는 코드를 왜 건드려?"라는 압박

### 3. 확장성이 필요한 시점에 과감하게 구조 개선

리팩토링의 적기는 "확장이 필요한 시점"이다. 이 타이밍을 놓치지 말아야 한다:

- 비슷한 기능을 두 번째로 개발할 때
- 코드 복사-붙여넣기를 고려하는 순간
- 새로운 요구사항에 기존 구조가 맞지 않을 때

### 4. 초기 속도 vs 장기 생산성

```
초기 개발 속도: 복사-붙여넣기 > 리팩토링과 병행
전체 프로젝트 생산성: 리팩토링과 병행 >>> 복사-붙여넣기
```

초기에는 개발 속도가 더딘 것처럼 느껴지지만, 모듈이 추가될수록 재사용 가능한 API를 호출하는 시간이 훨씬 단축된다.

### 5. 시간과 돈으로 배우는 경험

이러한 교훈은 실제 프로젝트에서 시간과 돈을 투자하며 얻은 값진 경험이다. 기술 부채의 대가는 생각보다 크다:

- 개발 시간 증가
- 버그 증가
- 팀원 스트레스 증가
- 비즈니스 기회 상실

## 언제 리팩토링할 것인가?

### 리팩토링이 필요한 신호들

1. **같은 코드를 세 번째 복사하려고 할 때**
   - "Rule of Three": 두 번은 참고, 세 번째는 추상화

2. **새로운 기능 추가가 두려워질 때**
   - 어디를 고쳐야 할지 불분명
   - 사이드 이펙트가 두려움

3. **버그 수정이 더 많은 버그를 만들 때**
   - 코드의 복잡도가 임계점을 넘음

4. **코드 리뷰에서 이해되지 않는 코드가 늘어날 때**
   - 작성자만 이해할 수 있는 코드

### 리팩토링 전략

1. **현실과 타협하기**
   - 모든 것을 한 번에 바꾸려 하지 말 것
   - 핵심 문제부터 해결

2. **테스트 코드 먼저**
   - 리팩토링 전 테스트 코드 작성
   - 안전한 리팩토링의 기반

3. **단계적 접근**
   - 작은 단위로 리팩토링
   - 각 단계마다 동작 검증

4. **팀과 공유**
   - 리팩토링 계획 공유
   - 코드 리뷰를 통한 지식 전파

## 결론

기술 부채는 피할 수 없지만, 관리할 수는 있다. "나중에"를 기약하지 말고, 확장성이 필요한 바로 그 순간에 과감하게 구조를 개선하는 결단이 필요하다.

초기 개발 속도가 조금 느려지더라도, 장기적으로는 훨씬 빠르고 안정적인 개발이 가능하다. 이는 시간과 돈으로 배우는 값진 경험이며, 다음 프로젝트에서는 더 나은 선택을 할 수 있게 만든다.

**핵심 메시지: 기술 부채는 복리로 쌓이지만, 좋은 아키텍처도 복리로 이득을 가져다준다.**