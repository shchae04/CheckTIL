# 테스트 더블(Test Double)

테스트 코드에서 실제 의존성을 사용하기 어려운 경우(외부 I/O, 네트워크, DB, 시간/랜덤 값, 3rd-party API 등), 테스트 더블을 사용해 의존성을 대체합니다. 테스트 더블은 외부 세계의 부수 효과를 차단하고, 테스트를 결정론적으로 만들며, 복잡한 설정을 단순화합니다.

## TL;DR
- 테스트 더블은 "가짜 의존성"으로, 테스트를 빠르고 안정적이며 재현 가능하게 만든다.
- 종류: 더미(Dummy), 스텁(Stub), 페이크(Fake), 스파이(Spy), 목(Mock)
- 역할 차이
  - Dummy: 인스턴스만 채우는 용도(사용되지 않음)
  - Stub: 미리 정해둔 값/행동을 반환(행위 기록/검증은 관심 없음)
  - Fake: 실제로 동작하는 단순 구현(예: 메모리 DB), 제품엔 부적합
  - Spy: 호출 내역을 기록하여 검증(부분적으로 Stub의 성격)
  - Mock: 상호작용(메서드 호출/파라미터/호출 횟수)을 기대/검증
- Classic TDD는 상태 검증 위주, Mockist TDD는 행위 검증 위주.

## 왜 필요한가?
- 결정론 확보: 외부 세계(시간/네트워크)의 변동성 제거 → 테스트가 항상 같은 결과
- 속도: 네트워크/디스크 I/O 제거 → 밀리초 단위 실행
- 격리: 실패의 원인 분리(유닛 테스트에서 협력 객체 영향 최소화)
- 설계 개선: 명확한 포트/인터페이스 추출을 유도

## 종류 정리
- Dummy
  - 아무 동작도 하지 않음. 생성자나 메서드 시그니처를 맞추기 위해 객체만 필요할 때 사용.
- Stub
  - “입력 → 고정된 출력”을 제공. 예외 던짐도 가능.
  - 예: "이 ID로 조회하면 항상 사용자 A를 반환" 같은 단순 규칙.
- Fake
  - 간단하지만 실제 동작하는 대체 구현. 예: InMemoryRepository, FakeClock.
  - 프로덕션 환경(동시성/지속성/보안 요구)에선 부적합할 수 있음.
- Spy
  - 호출된 메서드/인자/호출 횟수 등을 기록. 이후 검증에 사용.
  - 대개 Stub 기능도 포함(특정 입력에 대한 반환값 지정).
- Mock
  - 사전 기대(expectation)를 설정하고, 테스트 종료 시 해당 행위가 일어났는지 검증.
  - 기대 불일치 시 테스트 실패(예외).

## 언제 무엇을 쓰나?
- 도메인 로직 검증(상태 중심): Stub/Fake로 계산 결과와 상태 변화를 검증(Classic TDD)
- 외부 협력과의 상호작용 검증(행위 중심): Spy/Mock으로 호출 횟수·순서·인자 검증(Mockist 스타일)
- 빠른 피드백 루프: Fake(InMemory DB/MessageBus) 활용
- 시간/랜덤/ID 생성: FakeClock, StubbedRandom 등으로 결정화

## 예시(Java, JUnit 5 + Mockito)

### 1) Stub로 간단 결과 주입
```java
class UserRepository {
  Optional<User> findById(Long id) { /* real DB */ return Optional.empty(); }
}

class UserService {
  private final UserRepository repo;
  UserService(UserRepository repo) { this.repo = repo; }
  String greet(Long userId) {
    return repo.findById(userId).map(u -> "Hello, " + u.name()).orElse("Guest");
  }
}

class UserServiceTest {
  @Test
  void greet_returns_user_name_with_stub() {
    UserRepository stub = Mockito.mock(UserRepository.class);
    Mockito.when(stub.findById(1L)).thenReturn(Optional.of(new User(1L, "Alice")));

    UserService svc = new UserService(stub);
    assertEquals("Hello, Alice", svc.greet(1L));
  }
}
```

### 2) Fake로 메모리 저장소
```java
class InMemoryUserRepository extends UserRepository {
  private final Map<Long, User> store = new HashMap<>();
  @Override Optional<User> findById(Long id) { return Optional.ofNullable(store.get(id)); }
  void save(User u) { store.put(u.id(), u); }
}

@Test
void greet_with_fake_repository() {
  InMemoryUserRepository fake = new InMemoryUserRepository();
  fake.save(new User(1L, "Alice"));
  UserService svc = new UserService(fake);
  assertEquals("Hello, Alice", svc.greet(1L));
}
```

### 3) Spy/Mock로 행위 검증
```java
class Mailer { void sendWelcome(User u) { /* real email */ } }
class SignUpService {
  private final UserRepository repo; private final Mailer mailer;
  SignUpService(UserRepository repo, Mailer mailer) { this.repo = repo; this.mailer = mailer; }
  void signUp(User u) {
    // ...validate & persist
    mailer.sendWelcome(u);
  }
}

@Test
void sends_welcome_mail_on_signup() {
  UserRepository stubRepo = Mockito.mock(UserRepository.class);
  Mailer mockMailer = Mockito.mock(Mailer.class);

  SignUpService svc = new SignUpService(stubRepo, mockMailer);
  svc.signUp(new User(1L, "Alice"));

  Mockito.verify(mockMailer, Mockito.times(1)).sendWelcome(Mockito.argThat(u -> u.name().equals("Alice")));
}
```

## 안티패턴 주의
- 내부 구현 검증에 집착: 협력자 호출 순서/횟수 검증 남발 → 리팩터링 저항 증가
- 과도한 목 사용: 거짓 양성/음성, brittle test 유발 → 가능하면 상태 검증 우선
- 환경에 얽힌 테스트: 시스템 시계, 랜덤, 스레드 슬립 활용 → FakeClock/Stub으로 교체

## 실무 팁
- 포트/어댑터(헥사고날) 구조로 의존성 역전 → 테스트 더블 주입이 쉬워짐
- 테스트 명명: given-when-then 패턴으로 의도 명확화
- 경계만 목(Mock): DB/HTTP 클라이언트 등 외부 경계에서 목/스텁, 도메인은 Fake로 빠르게 검증

## 추가 학습 자료
- [Martin Fowler - Test Double](https://martinfowler.com/bliki/TestDouble.html)
- [Classic TDD vs Mockist TDD] 더즈, 티키(10분 테코톡)
- 백명석님 - Test And Test Doubles
- 기억보다 기록을 - 테스트 코드에서 내부 구현 검증 피하기
