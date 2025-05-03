# TIL: Java 21의 주요 기능 및 특징 정리 
## Java 21 개요
- 릴리즈일: 2023년 9월
- LTS (Long-Term Support) 버전
- Java 17 이후 2년 만의 LTS로 많은 기능이 안정화됨

---

##  주요 기능 요약

### 1. **가상 스레드 (Virtual Threads) [JEP 444]**
- `java.lang.Thread`의 경량 스레드 구현
- 수천 개의 동시 작업을 보다 쉽게 처리 가능
- 기존 플랫폼 스레드보다 리소스 소비 적음
```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> System.out.println("Virtual Thread"));
}
```

---

### 2. **스트럭처드 동시성 (Structured Concurrency) [JEP 453]**
- 관련 작업을 묶어 에러 처리 및 취소를 단순화
- 가독성과 안정성 향상
```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Future<String> user  = scope.fork(() -> findUser());
    Future<Integer> order = scope.fork(() -> fetchOrder());
    scope.join();
    scope.throwIfFailed();
    System.out.println(user.result() + " / " + order.result());
}
```

---

### 3. **패턴 매칭 향상 (Pattern Matching)**
#### - `switch` 패턴 매칭 [JEP 441]
- `switch` 문에서 타입 검사와 분기 결합
```java
static String formatter(Object o) {
    return switch (o) {
        case Integer i -> "정수: " + i;
        case String s -> "문자열: " + s;
        default -> "기타";
    };
}
```

#### - `record` 패턴 [JEP 440]
- `record` 타입의 구조 분해 가능
```java
record Point(int x, int y) {}
static void print(Object obj) {
    if (obj instanceof Point(int x, int y)) {
        System.out.println("X: " + x + ", Y: " + y);
    }
}
```

---

### 4. **범위 기반 포문 (Unpreviewed)**
- 아직 미리보기는 아니며, 명시적 도입은 없음

---

### 5. **외부 함수 및 메모리 API (Foreign Function & Memory API) [JEP 442]**
- JNI 없이 네이티브 라이브러리와 상호작용 가능
- `MemorySegment`, `MemorySession` 등 안전한 메모리 접근 제공

---

##  기타 변경 사항

- **String Templates (JEP 430 - preview)**: 더 읽기 쉬운 문자열 삽입
- **Unnamed Classes & Instance Main Methods (JEP 445 - preview)**: 학습용 코드에 적합한 짧은 작성
- **Sequenced Collections (JEP 431)**: 순서 보장 컬렉션 도입 (`SequencedCollection`, `SequencedMap` 등)

---

##  정리
- Java 21은 **가상 스레드** 및 **패턴 매칭**의 본격 도입으로 병렬 처리와 코드 가독성을 대폭 향상시킴
- **LTS 버전**으로 실제 서비스 적용 고려할 만한 가치 있음
- 최신 언어 기능 학습 및 모던 Java 스타일로의 전환에 적합