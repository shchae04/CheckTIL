# 컨테이너라이제이션으로 Scale Up/Scale Out 할 때의 고려사항 (TIL)

## 0. 한눈에 보기(초간단)
- 12-Factor 원칙으로 애플리케이션을 "무상태(stateless)"로 설계하고 상태는 외부화.
- 종료-시그널(SIGTERM) 처리와 graceful shutdown, readiness/liveness/startup probe 필수.
- 리소스 requests/limits 설정으로 안정적 스케줄링, HPA로 자동 확장, PDB로 가용성 보호.
- 연결 관리: DB 커넥션 풀/프록시, keep-alive, 연결 드레이닝, 재시도/타임아웃/지수 백오프+jitter.
- 배포 전략: RollingUpdate(기본) + Blue/Green/Canary, 스키마 마이그레이션은 역호환 우선.
- 세션/캐시/파일은 외부 스토리지(예: Redis, RDB, 객체 스토리지) 사용, WebSocket은 pub/sub로 팬아웃.
- 관측성: 구조화 로그, 메트릭, 트레이싱 필수. 리소스/비즈 지표 기반으로 스케일 기준 정의.
- 이미지/보안: 최소 베이스(distroless/ubi-micro), non-root, read-only FS, SBOM/스캔, secret 관리.

---

## 1. 아키텍처 원칙
- 12-Factor App: config를 환경변수로, 빌드/릴리스/런 분리, 포트 바인딩, 무상태 프로세스.
- 불변 인프라: 이미지로 동일 아티팩트를 어느 환경에도 배포. 컨피그/시크릿만 환경별 차이.
- 수평 확장 우선: Scale up(버티컬)보다 scale out(호리즌털)이 운영/비용/가용성 측면에서 유리.

## 2. 상태 외부화(Stateless)
- 세션: 앱 메모리에 저장 금지. Redis, DB, 또는 JWT(만료/블랙리스트 전략) 사용.
- 파일 업로드: 컨테이너 로컬 디스크 금지. S3/GCS/객체 스토리지 또는 공유 볼륨(EFS 등).
- 캐시: 인메모리만으로는 스케일 불가. Redis/Memcached 등 외부 캐시로 일관성/팬아웃 고려.
- 글로벌 락/리더: Redis/Kafka/ZK/Etcd 기반의 분산 락/리더 선출(필요 시). Redlock은 트레이드오프 인지.

## 3. DB 및 커넥션 관리
- 커넥션 폭주 방지: 각 인스턴스의 커넥션 풀 상한 설정. 전체 합계가 DB 한계치 이내여야 함.
- 커넥션 프록시/풀러: RDS Proxy, PgBouncer 등으로 연결 수 절감 및 핸드오버.
- 마이그레이션: 역호환 우선(Expand → Migrate → Contract). 롤백 시 안전.
- 읽기 스케일: 리드 레플리카 + 읽기 라우팅, 일관성 요구에 따라 lag 허용치 정의.

## 4. 스토리지 전략
- 영속 데이터: RDB/NoSQL/객체 스토리지 사용. StatefulSet이 필요한 경우 스토리지 클래스/백업 고려.
- 임시 파일: emptyDir나 메모리 tmpfs로 명확히 구분하고 내구성 기대하지 않기.

## 5. 메시징/비동기
- 큐/스트림: Kafka/SQS/RabbitMQ 등으로 비동기 처리. 재처리(idempotency), DLQ, 순서 보장 요건 명시.
- 정확히 한 번은 환상: 최소 한 번(at-least-once) + 멱등 처리 키로 설계.

## 6. 네트워킹/로드 밸런싱
- Readiness로 트래픽 수신 제어, PreStop + 드레이닝으로 연결 종료(HTTP keep-alive, gRPC 포함).
- Sticky Session은 최소화. 필요 시 외부 세션 저장 + L7 sticky.
- WebSocket/Server-Sent Events: 수평 확장 시 pub/sub(예: Redis, Kafka)로 브로드캐스트.
- 타임아웃/재시도: 지수 백오프 + jitter, 회路 차단기(circuit breaker), 버짓 기반 재시도 제한.

## 7. 쿠버네티스(예시) 실전 체크포인트
- Probes: liveness, readiness, startup를 분리. startup 완료 전 liveness는 지양.
- 리소스: requests/limits 설정으로 OOM/CPU 스로틀링 대비. 실제 피크 기준으로 SLO 맞춤.
- HPA: CPU/메모리 + 사용자 정의 메트릭(QPS, 처리 지연 등) 기반 목표값 정의.
- PDB(PodDisruptionBudget): 계획/비계획 중단 시 최소 가용 인스턴스 확보.
- 종료: terminationGracePeriodSeconds, preStop 훅에서 드레이닝(예: 5~15초) + SIGTERM 핸들링.
- 배포: RollingUpdate 기본, surge/unavailable 파라미터 조정. 필요 시 Blue/Green/Canary.

## 8. 배포와 무중단 전략
- 스키마 선반영 → 애플리케이션 릴리스 → 구버전 제거(Contract) 순.
- 애플리케이션은 SIGTERM 수신 시 새 요청 차단, 인플라이트 처리 후 종료.
- 이미지 태그는 불변(sha256 digest) 사용 권장. 롤백 전략 명확화.

## 9. 관측성(Observability)
- 로그: 구조화(JSON), 상관관계 ID(trace/span), stdout/stderr로 수집.
- 메트릭: 시스템(CPU/메모리/FD/GC) + 비즈니스(QPS, 에러율, 대기열 길이, 지연) 노출.
- 트레이싱: 분산 트레이싱(Traceparent). 외부 호출/DB/큐를 스팬으로 계측.
- 알림: SLO 기반 경보(예: 에러율, p95 지연, HPA 초과 등)와 런북 준비.

## 10. 이미지/보안/규정준수
- 이미지: 최소 베이스(distroless/ubi-micro), 멀티스테이지 빌드, 캐시 최적화, 재현가능 빌드.
- 보안: non-root, readOnlyRootFilesystem, drop capabilities, 네트워크 egress 제한.
- 비밀: Secret/Parameter Store, KMS로 암호화. 이미지에 포함 금지.
- 서플라이 체인: SBOM, 이미지 서명, 취약점 스캔 파이프라인.

## 11. 비용/성능
- 오토스케일 정책: 워크로드 패턴(QPS/배치)에 맞춘 스케일 업/다운 쿨다운/워밍업.
- 커넥션/스레드/메모리 튜닝: JVM/런타임 파라미터, 풀 크기, GC 설정.
- 캐시 전략: 히트율/TTL/일관성, cold start 영향 최소화.

## 12. 실전 체크리스트
- [ ] 환경변수/ConfigMap/Secret로 설정 외부화(12-Factor).
- [ ] 무상태 설계(세션/파일/캐시 외부화).
- [ ] SIGTERM 핸들러 + graceful shutdown 구현 및 테스트.
- [ ] readiness/liveness/startup probe 분리 구성.
- [ ] requests/limits 설정 및 부하 테스트로 검증.
- [ ] HPA 목표 지표 및 한계(min/max replicas) 설정.
- [ ] PDB, PodAntiAffinity로 분산 배치.
- [ ] DB 커넥션 총량 관리, 프록시/풀러 도입 고려.
- [ ] 재시도/타임아웃/서킷브레이커 표준화.
- [ ] 배포 전략(롤링/카나리) 및 롤백 절차 수립.
- [ ] 관측성(로그/메트릭/트레이싱) 대시보드/알림 구성.
- [ ] 이미지 보안/비밀 관리/스캔 파이프라인 구성.

## 13. 면접 한 줄 답변
- 컨테이너 기반 수평 확장을 위해 앱은 무상태로 설계하고, 상태는 외부 저장소로 분리합니다. readiness/startup/liveness, graceful shutdown, 리소스 requests/limits, HPA/PDB로 운영 안전성을 확보하고, DB 커넥션/재시도/드레이닝을 통해 연결 폭주와 무중단 배포를 보장합니다. 관측성(로그/메트릭/트레이싱)과 보안(non-root, 최소 이미지, 시크릿 관리)까지 갖추면 안정적으로 scale up/out 할 수 있습니다.

## 14. 참고
- 12-Factor App (https://12factor.net/)
- Kubernetes Probes / HPA / PDB 공식 문서
- AWS RDS Proxy, PgBouncer, Envoy/Linkerd, OpenTelemetry
