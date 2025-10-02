# Micrometer - 벤더 중립적인 메트릭 계측 라이브러리

## Micrometer란 무엇이며, 왜 사용하나요?

Micrometer는 **벤더 중립적인 메트릭 계측 라이브러리**로, 애플리케이션에서 발생하는 다양한 지표를 수집합니다.

### 주요 특징
- **다양한 지표 수집**: CPU 사용량, 메모리 소비, HTTP 요청, 커스텀 이벤트 등
- **벤더 중립성**: Prometheus, Datadog, Graphite 등 여러 모니터링 시스템 지원
- **통일된 API**: 각 백엔드 클라이언트의 복잡한 세부 구현을 감춘 단순하고 일관된 파사드 API 제공
- **Spring Boot 통합**: Spring Boot Actuator와 깊이 통합되어 기본 메트릭을 자동으로 수집하고 노출

### 핵심 가치
- 모니터링 백엔드를 변경하더라도 애플리케이션 코드 변경 최소화
- 표준화된 메트릭 수집 방식 제공
- 운영 환경에서 애플리케이션 상태 모니터링 가능

---

## Spring Boot Actuator와 Micrometer의 관계

### Spring Boot Actuator의 역할
- 애플리케이션의 상태, 헬스 체크, 환경, 로그 등 **운영 정보를 노출하는 관리 엔드포인트** 제공
- 애플리케이션 모니터링 및 관리 인터페이스 역할

### Micrometer의 역할
- Actuator 내부에서 **실제 메트릭 데이터를 계측하고 수집**
- 여러 모니터링 시스템으로 메트릭 데이터 전송

### 협력 구조
```
Spring Boot Actuator (관리 인터페이스)
    ↓ (메트릭 수집 위임)
Micrometer (메트릭 계측 엔진)
    ↓ (메트릭 전송)
모니터링 시스템 (Prometheus, Datadog, Graphite 등)
```

- **Actuator**: JVM, HTTP, 데이터베이스 등 다양한 메트릭의 관리 인터페이스
- **Micrometer**: 실제 메트릭 계측 및 백엔드 시스템으로의 전송 담당

---

## Micrometer를 사용한 커스텀 메트릭 생성 방법

다음은 Micrometer를 활용하여 **커스텀 메트릭(카운터, 타이머, 게이지)**을 생성하고 업데이트하는 예제입니다.

```java
package com.example.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class CustomMetricsService {

    private final Counter requestCounter;
    private final Timer requestTimer;
    private final CustomGauge customGauge;

    // 생성자에서 MeterRegistry를 주입받아 필요한 메트릭을 등록합니다.
    public CustomMetricsService(MeterRegistry meterRegistry) {
        // HTTP 요청 총 건수를 세는 Counter (태그로 엔드포인트 구분)
        this.requestCounter = meterRegistry.counter("custom.requests.total", "endpoint", "/api/test");

        // HTTP 요청 처리 시간을 측정하는 Timer (태그로 엔드포인트 구분)
        this.requestTimer = meterRegistry.timer("custom.request.duration", "endpoint", "/api/test");

        // Gauge: 예를 들어, 현재 활성 세션 수를 측정하기 위한 커스텀 객체를 등록
        this.customGauge = new CustomGauge();
        Gauge.builder("custom.active.sessions", customGauge, CustomGauge::getActiveSessions)
                .tag("region", "us-east")
                .register(meterRegistry);
    }

    /**
     * 실제 비즈니스 로직을 실행할 때 요청 카운트와 처리 시간을 측정합니다.
     * @param requestLogic 실제 처리할 로직 (예: HTTP 요청 처리)
     */
    public void processRequest(Runnable requestLogic) {
        // 요청 수 증가
        requestCounter.increment();
        // 요청 처리 시간 측정
        requestTimer.record(requestLogic);
    }

    /**
     * 활성 세션 수 업데이트 (예를 들어, 로그인/로그아웃 이벤트에서 호출)
     * @param activeSessions 현재 활성 세션 수
     */
    public void updateActiveSessions(int activeSessions) {
        customGauge.setActiveSessions(activeSessions);
    }

    /**
     * 커스텀 Gauge의 값을 저장하는 내부 클래스.
     */
    private static class CustomGauge {
        // 현재 활성 세션 수를 저장 (volatile을 사용해 스레드 안정성 확보)
        private volatile double activeSessions = 0;

        public double getActiveSessions() {
            return activeSessions;
        }

        public void setActiveSessions(double activeSessions) {
            this.activeSessions = activeSessions;
        }
    }
}
```

### 주요 구성 요소 설명

#### 1. MeterRegistry 사용
- 생성자에서 `MeterRegistry`를 주입받아 애플리케이션의 모든 메트릭을 중앙에서 관리
- 설정된 모니터링 백엔드로 주기적으로 메트릭 데이터 전송

#### 2. Counter (카운터)
```java
this.requestCounter = meterRegistry.counter("custom.requests.total", "endpoint", "/api/test");
```
- `/api/test` 엔드포인트에 대한 **요청 건수를 누적으로 카운트**
- `increment()` 메서드로 매 HTTP 요청마다 증가

#### 3. Timer (타이머)
```java
this.requestTimer = meterRegistry.timer("custom.request.duration", "endpoint", "/api/test");
```
- HTTP 요청 **처리 시간을 측정**
- `record()` 메서드를 사용해 실제 로직 실행 시간을 기록

#### 4. Gauge (게이지)
```java
Gauge.builder("custom.active.sessions", customGauge, CustomGauge::getActiveSessions)
        .tag("region", "us-east")
        .register(meterRegistry);
```
- **현재 활성 세션 수**를 측정하는 실시간 지표
- 항상 현재 상태를 조회하는 함수(`getActiveSessions()`)를 호출하여 실시간 값 반영
- `volatile` 키워드로 멀티스레드 환경에서의 안정성 보장

### 메트릭 태그(Tags)의 활용
- `"endpoint", "/api/test"`: 엔드포인트별로 메트릭 구분
- `"region", "us-east"`: 지역별로 메트릭 구분
- 태그를 통해 세밀한 필터링과 집계 가능

---

## TIL (Today I Learned)

### 핵심 포인트
1. **Micrometer = 메트릭 수집의 표준화**: 다양한 모니터링 시스템에 대한 통일된 인터페이스 제공
2. **Actuator와의 시너지**: Spring Boot Actuator가 관리 인터페이스를, Micrometer가 실제 메트릭 엔진 역할
3. **커스텀 메트릭의 중요성**: Counter, Timer, Gauge를 활용한 비즈니스 지표 측정
4. **태그 기반 분류**: 메트릭에 태그를 추가하여 세밀한 모니터링 가능

### 실무 적용 시 고려사항
- 메트릭 이름 규칙 정의 (예: `application.module.metric_type` 형태)
- 적절한 태그 설계로 필터링과 집계 최적화
- 성능에 미치는 영향을 고려한 메트릭 수집 빈도 조절
- 모니터링 백엔드 변경 시에도 애플리케이션 코드 변경 최소화