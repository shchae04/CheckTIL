# 보상 트랜잭션으로 분산 환경에서도 안전하게 환전하기

- 작성일: 2025-08-31 01:13
- 참고 영상: https://www.youtube.com/watch?v=xpwRTu47fqY

---

## 1. 한 줄 요약
분산 시스템에서 환전과 같은 다중 서비스/계좌에 걸친 작업은 분산 락/2PC 대신 사가(Saga) 패턴과 보상 트랜잭션, 멱등성, 아웃박스-이벤트 발행, 상태 머신으로 운영적 안정성을 확보한다.

---

## 2. 문제 배경: 환전 흐름의 본질
- 유스케이스: 원화 계좌에서 USD 구매(환전) → KRW 차감 → 환율 고정/체결 → USD 입금.
- 제약/리스크:
  - 서로 다른 서비스/DB(자산/주문/결제/정산)에跨 경계 변경.
  - 환율 변동/체결 실패, 네트워크 장애, 중복 시도, 부분 성공.
  - 강한 일관성(ACID) 보장 어려움 → 최종 일관성과 보상으로 안전성 달성 필요.

---

## 3. 접근 패턴
### 3.1 Sagas + 보상 트랜잭션
- 각 로컬 트랜잭션을 커밋하면서, 실패 시 역순으로 보상 동작을 수행.
- 예) KRW 차감 → 체결 요청 → USD 입금. 실패 시 USD 출금(있다면) → 체결 취소 → KRW 환원.

### 3.2 TCC(Try-Confirm/Cancel)
- Try: 자원 예약(예: KRW hold, 환율/체결 슬롯 예약)
- Confirm: 성공 시 확정(hold 해제 및 차감 확정, USD 크레딧)
- Cancel: 실패 시 예약 해제/환원
- 시간 제한/만료 정책과 결합해 유실/고아 상태 방지.

### 3.3 오케스트레이션 vs 코레오그래피
- 오케스트레이션: 중앙 사가 코디네이터가 단계/보상 명시. 관측/제어 용이, 복잡도 집중.
- 코레오그래피: 이벤트 기반 자율 진행. 결합 낮지만 흐름 파악/에러 핸들 어려움.
- 환전과 같이 규칙/보상이 명확하고 금전적 리스크가 큰 경우 오케스트레이터 선호.

---

## 4. 핵심 구현 포인트
### 4.1 멱등성(Idempotency)과 중복 방지 키
- 사가 ID/트랜잭션 키를 모든 단계 API/커맨드에 포함.
- 상태 전이 테이블/이벤트 로그로 중복 처리 방지(이미 처리/보상 완료 여부 체크).

### 4.2 Outbox + CDC/Event Publisher
- 로컬 트랜잭션 내 상태 변경 + outbox 레코드 삽입을 함께 커밋.
- 별도 퍼블리셔가 outbox→브로커(Kafka 등)로 전송, 전송 후 마킹.
- 정확-히-한번은 어려우며 최소-한번+멱등 처리로 안전성 확보.

### 4.3 상태 모델(State Machine)
- 예시: CREATED → KRW_HELD → ORDER_PLACED → USD_CREDITED → COMPLETED
- 실패 경로: ... → FAILED_PENDING_COMP → COMPENSATED → TERMINATED
- 각 상태는 재시도/타임아웃/보상 트리거 규칙을 가져야 함.

### 4.4 실패 시나리오 설계
- 네트워크 타임아웃: 재시도(지수 백오프) + 멱등 키.
- 부분 성공: 다음 스텝 실패 시 이전 스텝 보상 호출.
- 오케스트레이터 다운: 사가 상태 저장 + 리커버리 워커로 재개.
- 외부 시스템 장애: 서킷 브레이커, 대체 경로(지연 체결/대기 큐), 사전 한도.

### 4.5 운영/관측
- 트레이싱: 사가-트레이스 ID 전파.
- 메트릭: 단계별 성공/실패율, 보상률, 평균 보상 지연, 사가 체류시간.
- 알림/런북: 장시간 PENDING, 보상 실패 누적 알림, 수동 개입 시나리오.

---

## 5. 예시 오케스트레이터 의사코드
```pseudo
function exchangeSaga(sagaId, userId, krwAmount, targetUsdAmount):
  ensureIdempotency(sagaId)
  saveState(sagaId, CREATED)

  try:
    // 1) KRW hold
    call(KRWService.hold, {sagaId, userId, amount: krwAmount})
    saveState(sagaId, KRW_HELD)

    // 2) 주문/체결
    orderId = call(TradeService.placeOrder, {sagaId, pair: "KRW-USD", krwAmount})
    saveState(sagaId, ORDER_PLACED, {orderId})

    // 3) USD 크레딧
    call(USDService.credit, {sagaId, userId, amount: targetUsdAmount})
    saveState(sagaId, USD_CREDITED)

    // 완료
    call(KRWService.commitHold, {sagaId})
    saveState(sagaId, COMPLETED)

  catch e:
    saveState(sagaId, FAILED_PENDING_COMP, {reason: e})
    // 보상(역순)
    safely(() => call(USDService.debit, {sagaId, userId, amount: targetUsdAmount}))
    safely(() => call(TradeService.cancelOrder, {sagaId, orderId}))
    safely(() => call(KRWService.releaseHold, {sagaId}))
    saveState(sagaId, COMPENSATED)
    raise e
```

---

## 6. 데이터 모델 스케치
- saga_instance(id PK, user_id, state, created_at, updated_at, last_error)
- saga_step_log(id, saga_id, step, status, attempt, payload, created_at)
- outbox(id, saga_id, event_type, payload_json, status, created_at)
- 멱등 키: (service, operation, saga_id) unique

---

## 7. 트랜잭션/격리 고려
- 각 로컬 스토어에서 짧은 트랜잭션 유지. 동기 호출은 타임아웃 필수.
- 교착/장시간 락 회피: 예약(hold) 기반, 최종 확정 시 짧게 commit.
- 계좌 잔고 일관성: 샤딩 키(사용자/계좌) 고정, 순서 보장 필요 시 파티션 키 고정 + 단일 파티션 소비.

---

## 8. 체크리스트
- [ ] 사가 오케스트레이터 도입/상태 저장소 구현
- [ ] 각 서비스 보상 API, 멱등성 보장(키/버전)
- [ ] Outbox/퍼블리셔, 재시도/백오프/서킷
- [ ] 타임아웃/SLI(Success rate, Compensation rate, p95 지연)
- [ ] 경계/권한(최소권한, 금액 한도, 감시 로그)
- [ ] 드라이런/카오스 테스트: 단계별 실패 주입과 복원 시나리오 검증

---

## 9. 인사이트
- "정확히 한 번"보다 "최소 한 번 + 멱등"이 현실적이며 운영적으로 안전하다.
- 돈의 흐름은 항상 추적 가능해야 하며(불변 원장/로그), 보상은 회계적으로 분명해야 한다.
- 오케스트레이션은 복잡도를 한곳에 모으지만 책임/관측 가능성을 높여 금전 도메인에 적합하다.

---

## 10. 관련 문서
- DDD 아키텍처: ./ddd_architecture.md
- 좋은 아키텍처 설계: ./good_architecture_design.md
- 분산 로그/트레이싱: ../../Interview/distributed_logging.md
