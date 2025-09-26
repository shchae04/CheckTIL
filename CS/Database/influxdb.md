# InfluxDB란?

InfluxDB는 시계열 데이터(Time Series Data)를 저장·질의·시각화하기 위해 설계된 오픈소스/클라우드 데이터베이스입니다. 메트릭(서버/애플리케이션 성능지표), IoT 센서값, 금융 시세, 이벤트 로그 등 "시간에 따른 값의 변화"를 빠르게 기록하고 집계하는 데 최적화되어 있습니다.

---

## 1) 한줄 정의 (TL;DR)
- InfluxDB = 고성능 시계열 데이터베이스. 태그 기반 인덱싱 + 압축/다운샘플링/보존정책으로 대량의 시계열 데이터를 효율적으로 관리.

---

## 2) 핵심 개념
- Measurement: 논리적 측정값 그룹(테이블 유사). 예: cpu, memory, temperature.
- Tags: 인덱싱되는 라벨(문자열). 예: host=web-01, region=ap-northeast-2. 카디널리티에 주의!
- Fields: 실제 수치/값(비인덱스). 예: usage_user=12.3, load1=0.74.
- Timestamp: 각 포인트의 시간. 나노초까지 정밀도 가능. 일반적으로 UTC 저장 권장.

즉, 한 데이터 포인트는 (measurement, tags..., fields..., timestamp)로 구성됩니다.

---

## 3) 라인 프로토콜(Line Protocol)
InfluxDB에 쓰기(write)할 때 흔히 사용하는 텍스트 포맷입니다.

예시:
```
cpu,host=web-01,region=ap-northeast-2 usage_user=12.3,usage_system=7.1 1737955200000000000
```
- 형식: measurement,tagKey=tagVal[,tagKey=tagVal...] fieldKey=fieldVal[,fieldKey=fieldVal...] timestamp
- 태그는 인덱싱되어 필터링이 빠름, 필드는 값(수치/문자열/불리언) 저장 용도.
- timestamp는 나노초 단위 예시. (ms/s/us/ns 등 클라이언트에서 제어 가능)

---

## 4) 버전과 쿼리 언어 개요
- InfluxDB 1.x: InfluxQL(SQL 유사) 중심.
- InfluxDB 2.x: Flux 언어 도입(파이프 기반 함수형). InfluxQL도 일부 호환.
- InfluxDB 3.x(현행): 내부 엔진을 Arrow/Parquet 기반으로 재설계. FlightSQL을 통한 표준 SQL 질의 지원(클라우드/엔터프라이즈 중심). 일부 배포판에서는 Flux/InfluxQL 호환 레이어를 제공하기도 함.

실무에서는 사용하는 배포판/버전에 따라 쿼리 언어가 다를 수 있으므로, 조직의 InfluxDB 버전과 지원 언어(InfluxQL/Flux/SQL)를 확인하는 것이 중요합니다.

---

## 5) 스키마 설계 가이드(카디널리티 주의)
- 태그(tags)는 인덱스가 생성되어 필터링에 유리하지만, 태그 값의 고유 조합 수(카디널리티)가 너무 크면 메모리/성능이 급격히 저하됩니다.
  - 나쁜 예: userId, requestId 같은 고유 값(매 요청마다 다른 값)을 태그로 두기.
  - 권장: host, region, service, env(stage/prod)처럼 "값 종류가 제한된" 차원만 태그로.
- 필드(fields)는 인덱스되지 않으나 수치 집계에 적합. 대량 포인트에 유리.
- Measurement는 논리적으로 묶을 수 있는 지표 단위로 설계. 과도한 measurement 분리는 피하고, 차원은 태그로 모델링.

---

## 6) 보존 정책(Retention Policy)과 다운샘플링(Downsampling)
- 보존정책(RP): 데이터의 보관 기간을 정의. 예: raw는 7일, 1분 집계는 90일, 1시간 집계는 1년.
- 지속 쿼리(Continuous Query, CQ) 또는 작업(Task)로 다운샘플링 자동화:
  - 예: 10초 단위 raw를 1분 평균/최대/p95로 집계해 더 길게 저장.
- 스토리지 비용과 질의 성능의 균형을 맞추는 핵심 전략입니다.

---

## 7) 데이터 쓰기와 조회 예시

### 7.1 쓰기(라인 프로토콜, HTTP API)
- HTTP
  - POST /api/v2/write?org=my-org&bucket=my-bucket&precision=ns
  - Body 예시(여러 줄 가능):
```
cpu,host=web-01,region=ap-northeast-2 usage_user=12.3,usage_system=7.1 1737955200000000000
cpu,host=web-02,region=ap-northeast-2 usage_user=15.0,usage_system=6.4 1737955201000000000
```

### 7.2 Flux(2.x) 예시
```
from(bucket: "my-bucket")
  |> range(start: -1h)
  |> filter(fn: (r) => r._measurement == "cpu" and r.host == "web-01")
  |> aggregateWindow(every: 1m, fn: mean, createEmpty: false)
  |> yield(name: "mean")
```

### 7.3 InfluxQL(1.x/호환) 예시
```
SELECT mean("usage_user")
FROM "cpu"
WHERE time >= now() - 1h AND "host"='web-01'
GROUP BY time(1m)
```

### 7.4 SQL(3.x/FlightSQL) 예시
```
SELECT
  DATE_BIN(INTERVAL '1 minute', ts, TIMESTAMP '1970-01-01') AS t,
  AVG(usage_user) AS avg_usage
FROM cpu
WHERE ts >= NOW() - INTERVAL '1 hour' AND host = 'web-01'
GROUP BY 1
ORDER BY 1;
```

버전에 따라 스키마/컬럼명(ts, host 등)과 인터페이스가 다를 수 있습니다.

---

## 8) 성능 팁
- 태그 카디널리티 관리: 동적/고유 값은 태그 대신 필드나 이벤트 로그로. 필요 시 샘플링/집계.
- 시간 파티셔닝(내부적으로 청크/세그먼트 관리): 범위 질의(time range)를 전제로 쿼리.
- UTC 저장 + 정밀도 일관성 유지(ns/us/ms/s). 클라이언트마다 precision 혼동 주의.
- 지연 도착(out-of-order) 허용 창을 정책으로 정의하고, upsert/중복처리 규칙을 명확히.
- 다운샘플링 + 보존정책으로 저장용량과 질의속도 최적화.

---

## 9) 대시보드/모니터링
- Grafana와 연동이 일반적(InfluxDB 데이터소스 플러그인).
- InfluxDB 자체 UI(2.x)에서도 대시보드/탐색 가능.

---

## 10) Prometheus/TimescaleDB와 간단 비교
- Prometheus: 풀 모델(스크랩) + 알림/라벨 기반 모니터링에 강점. 장기 보관/복잡 SQL은 약하고, 리모트 스토리지 조합으로 보완.
- TimescaleDB(PostgreSQL 확장): SQL 생태계와 호환, 범용 분석·조인에 강함. 시계열 최적화(하이퍼테이블, 압축) 제공.
- InfluxDB: 라인프로토콜/태그 모델로 빠른 수집·집계. 버전에 따라 Flux/InfluxQL/SQL 선택지. 모니터링/IoT 파이프라인에서 널리 사용.

---

## 11) 설치/배포(개요)
- 로컬/도커로 빠른 시작 가능. 예: `influxdb:2.x` 이미지, 혹은 3.x 클라우드/엔터프라이즈.
- 에이전트/Telegraf로 다양한 입력 플러그인(시스템 메트릭, Kafka, MQTT, DB 등) 수집.

---

## 12) 함께 보면 좋은 문서
- CS/Database/time_series_data.md (시계열 데이터 개념 전반)
- Interview/logs_and_metrics.md (로그/메트릭 모니터링 개요)
- keyword/keywords.md (용어 정리: 카디널리티, 다운샘플링 등)

---

## 13) 요약
- InfluxDB는 시계열 데이터 워크로드에 특화된 DB로, 태그/필드/타임스탬프 모델과 보존정책/다운샘플링을 통해 대규모 데이터를 효율적으로 운영한다. 배포 버전에 따라 InfluxQL/Flux/SQL 중 적합한 질의 인터페이스를 선택하고, 태그 카디널리티와 보존전략을 사전에 설계하는 것이 성능과 비용의 핵심이다.
