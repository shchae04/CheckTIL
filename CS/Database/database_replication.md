# 데이터베이스 복제 (Database Replication)

데이터베이스 복제는 고가용성(HA), 장애 복구(DR), 읽기 확장성(Scale-out), 데이터 안정성을 위해 운영 환경에서 널리 사용하는 핵심 기술입니다. 대표적으로 MySQL과 PostgreSQL에서 폭넓게 사용됩니다. 이 문서는 두 시스템을 중심으로 복제의 원리, 방식, 장단점, 운영 시 고려사항을 요약합니다.

## 1. 복제의 목적과 기본 개념
- 가용성: Primary/Source 장애 시 Replica/Standby 승격으로 다운타임 최소화
- 확장성: 읽기 트래픽을 Read Replica로 분산
- 데이터 보호: 지리적으로 분산된 복제본을 통한 재해 복구
- 유지보수: 백업/리포팅/배치 작업을 Replica에서 수행하여 본선 부하 감소

용어 정리(일반):
- Source(Primary) ↔ Replica(Secondary/Standby)
- 물리 복제(Write-Ahead Log 또는 데이터 페이지 단위) vs 논리 복제(SQL/Row 이벤트 단위)
- 동기 복제 vs 비동기 복제 vs 세미 동기(half-sync)

---

## 2. MySQL Replication
MySQL 복제는 Binary Log(binlog)에 기록된 변경 이벤트를 Replica가 전달받아 재적용하는 방식입니다. 전통적으로 비동기 복제가 기본이며, 옵션으로 세미 동기(semi-sync) 등을 사용할 수 있습니다.

### 2.1 Binary Log 포맷: Row / Statement / Mixed
MySQL은 binlog 기록 방식을 세 가지로 제공합니다. 각 방식은 `binlog_format` 설정으로 선택합니다.

1) Row-Based Replication (RBR)
- 내용: 변경된 행(row)의 이전/이후 이미지(또는 변경 필드)를 이벤트로 기록
- 장점: 비결정적(non-deterministic) 함수/트리거/스토어드 프로시저 상황에서도 높은 일관성 보장
- 단점: 변경된 행 데이터가 모두 기록되어 로그 크기가 커지기 쉬움, 저장공간/네트워크 비용 증가
- 사용 예: 복잡한 트리거나 비결정적 로직이 많은 서비스, 정확한 재현이 중요한 환경

2) Statement-Based Replication (SBR)
- 내용: 실행된 SQL 문 자체를 기록
- 장점: 로그가 상대적으로 작아 저장/전송 비용 절약
- 단점: NOW(), RAND(), UUID() 등 비결정적 함수, AUTO_INCREMENT, 트리거/스토어드 프로시저에 따라 결과가 달라져 복제 불일치 위험
- 사용 주의: 데이터 일관성이 최우선인 환경에는 권장되지 않음

3) Mixed-Based Replication (MBR)
- 내용: 상황에 따라 Statement 또는 Row를 혼합 기록
- 전략: 일반적으로 결정적 쿼리는 Statement, 비결정적/위험한 쿼리는 Row로 자동 전환
- 장점: 공간 효율과 일관성의 균형
- 단점: 동작 이해/디버깅 복잡성

요약: 공간/성능을 중시하면 Statement/Mixed, 강한 일관성을 중시하면 Row가 선호됩니다.

### 2.2 복제 동작 과정
1) Source가 트랜잭션 커밋 시 binlog 파일에 이벤트를 기록
2) Replica의 I/O Thread가 Source의 binlog를 읽어와 Replica의 Relay Log에 저장
3) Replica의 SQL Thread(또는 다중 워커, MTS)가 Relay Log 이벤트를 순서대로 적용

구성 요소:
- Binary Log: Source에서 데이터 변경 이벤트 기록
- Relay Log: Replica 측 임시 로그 저장소
- I/O Thread: Source→Replica 로그 전송
- SQL Thread/Workers: 이벤트 적용(Parallel Replication 지원)

지연(Lag): 네트워크/부하에 따라 수 ms~수백 ms 수준. 일반적으로 잘 구성 시 약 100ms 내외로 실시간에 가깝게 동기화됨.

### 2.3 동기화 모드와 일관성
- 비동기(기본): Source 커밋 즉시 클라이언트에 성공 반환. Replica 적용은 사후 진행 → RPO>0 가능성
- 세미 동기(Semi-sync): 최소 한 Replica가 이벤트 수신(ack)해야 Source 커밋 완료. 성능과 RPO 절충
- 완전 동기: 모든 지정 Replica 적용 확인 후 커밋. 지연 증가로 일반 웹 트래픽에는 드묾

추가 옵션/개념:
- GTID(Global Transaction ID): 복제 단순화, 장애 조치 시 포지션 관리보다 안정적
- Parallel Replication(MTS): 동일 DB/로지컬 클록 기준 병렬 적용으로 지연 감소

### 2.4 토폴로지와 장애 조치
- 단일 소스 + 다수 리드 레플리카 (가장 일반적)
- 체인/캐스케이딩 복제(Replica가 또 다른 Replica의 Source가 됨)
- 장애 조치(Failover): 자동화 도구(예: Orchestrator), VIP/Proxy 전환, GTID 기반 재구성 권장
- 스위치오버(Switchover): 계획된 Primary 교체

### 2.5 운영 시 주의사항
- DDL 일관성: 대형 스키마 변경 시 Percona Online Schema Change(PT-OSC) 또는 gh-ost 등 고려
- Binlog/Relay Log 공간: 보관 주기와 정리 정책 설정, 디스크 모니터링
- 트랜잭션 크기: 초대형 트랜잭션은 Replica 지연 가중 → 배치 작업 쪼개기
- 네트워크: 대역폭/지연/패킷 손실 모니터링
- 모니터링: Seconds_Behind_Master, Replica SQL/IO Thread 상태, relay log 누적량, 에러 로그

---

## 3. PostgreSQL Replication
PostgreSQL는 WAL(Write-Ahead Log)을 기반으로 한 물리(physical) 스트리밍 복제와, 이벤트 레벨의 논리(logical) 복제를 제공합니다. 기본은 물리 스트리밍 복제이며, 10버전부터 내장 논리 복제가 도입되었습니다.

### 3.1 WAL과 물리(스트리밍) 복제
- WAL: 모든 변경을 로그로 선기록. 복구/복제의 기반
- Primary의 walsender 프로세스가 Standby의 walreceiver로 WAL을 스트리밍
- Standby는 WAL을 적용하며 hot_standby=on이면 읽기 쿼리 허용(Hot Standby)
- 초기 동기화: `pg_basebackup` 등으로 베이스 백업 후 스트리밍 시작
- Replication Slots(물리): 소비되지 않은 WAL 보존을 보장(슬롯 방치 시 디스크 급증 위험)

동기화 모드:
- 비동기: 기본. 지연은 작고 성능 우수, 장애 시 최근 트랜잭션 유실 가능(RPO>0)
- 동기(synchronous_commit): `synchronous_standby_names`에 정의된 Standby 수 만큼 WAL 수신/플러시 확인 후 커밋
- Quorum Sync: 여러 Standby 중 지정 개수의 확인으로 커밋 허용(성능/가용성 균형)

병렬/토폴로지:
- 캐스케이딩 복제: Standby가 다른 Standby에 다시 WAL 제공
- 다중 Standby 구성으로 읽기 확장

### 3.2 논리 복제(Logical Replication)
- 단위: 테이블/컬럼 수준으로 변경 이벤트를 publish/subscribe
- 사용: 이기종 마이그레이션, 선택적 테이블 복제, 스키마 점진적 변경
- 구성: Publication(Primary) ↔ Subscription(Replica)
- Logical Decoding/Slots(논리): 이벤트 보존/전달. 소비 지연 시 WAL 보존 증가 주의
- 제약: 시퀀스 자동 증가는 기본적으로 이벤트에 포함되지 않아 별도 처리/동기화 필요

### 3.3 장애 조치와 운영 고려사항
- 승격(Promotion): `pg_ctl promote` 또는 트리거 파일 등으로 Standby를 Primary로 승격
- 스위치오버: 계획된 역할 교체, 연결 라우팅(PgBouncer/HAProxy/Patroni) 병행 권장
- 타임라인 관리: 장애 조치 후 타임라인 분기. 재조인 시 베이스백업/pg_rewind 고려
- 모니터링: `pg_stat_replication`, `pg_last_wal_replay_lsn()`, 복제 지연, 슬롯 누적, 체크포인트, 디스크 용량

---

## 4. MySQL vs PostgreSQL 간 비교 요약
- 변경 전달 단위
  - MySQL: binlog 이벤트(Row/Statement/Mixed)
  - PostgreSQL: WAL(물리), Logical(이벤트)
- 동기화 모드
  - 둘 다 비동기 기본, 동기/준동기 옵션 제공
- 읽기 확장
  - 둘 다 Read Replica로 확장 가능(Hot Standby/Replica)
- 마이그레이션/선택 복제
  - MySQL: 주로 논리 단위(Row 기반 이벤트). 테이블 단위 세밀 제어는 도구 필요
  - PostgreSQL: 내장 논리 복제로 테이블/컬럼 선택적 복제가 비교적 수월
- 운영 난이도/도구
  - MySQL: Orchestrator, MHA, ProxySQL 등 생태계 풍부
  - PostgreSQL: Patroni, repmgr, pgpool-II, HAProxy, PgBouncer 등

---

## 5. 실무 팁 및 체크리스트
- 목표 정의: RPO/RTO, 읽기 확장 목표를 먼저 확정 후 모드 선택(동기/비동기/세미)
- 스키마 변경: 대용량 DDL은 온라인 방식/논리 복제 활용 검토
- 모니터링: 지연, 슬롯, 디스크, 네트워크, 에러 로그 대시보드화
- 백업 전략: 복제와 백업은 별개. 정기 스냅샷/Point-in-Time Recovery(PITR) 계획 수립
- 장애 시나리오 리허설: 프로모션/스위치오버/롤백/재조인 절차 정립 및 반복 테스트
- 보안: 복제 사용자 최소 권한, TLS 적용, 방화벽/보안그룹 관리

---

## 6. 요약
- MySQL: Row/Statement/Mixed binlog 포맷을 통해 복제. I/O Thread가 binlog를 Relay Log로 가져오고 SQL Thread가 적용. 일반적으로 수십~수백 ms 내외의 지연으로 실시간에 가깝게 동기화 가능. GTID/세미 동기/병렬 적용 등으로 안정성과 지연을 개선.
- PostgreSQL: WAL 기반의 물리 스트리밍 복제가 기본, 동기/쿼럼 동기 옵션 제공. 논리 복제로 테이블 단위 복제/마이그레이션 지원. Replication Slots와 프로모션/타임라인 관리에 유의.

참고 키워드: binlog_format, GTID, relay log, MTS, Orchestrator, WAL, hot_standby, synchronous_standby_names, replication slots, logical decoding, publication/subscription, pg_basebackup, pg_rewind.
