# 시계열 데이터란? (Time Series Data)

## 1) 한줄 정의 (TL;DR)
- 시계열 데이터는 "시간 순서로 관측된 값"의 연속이다. 각 데이터 포인트는 타임스탬프와 함께 기록되며, 시간의 흐름 자체가 의미와 패턴을 만든다.

---

## 2) 왜 중요한가
- 관측의 대부분이 시간 축 위에서 일어난다: 서버 메트릭(CPU, 요청 수), 금융 가격, 센서 값, 로그, 클릭스트림, IoT.
- 예측과 이상탐지, 용량 계획, SLA 준수, 비즈니스 KPI 모니터링의 핵심 근거가 된다.

---

## 3) 핵심 구성 요소와 용어
- 추세(Trend): 장기적인 증가/감소 경향.
- 계절성(Seasonality): 일정 주기(일/주/월/연 등)로 반복되는 패턴.
- 주기(Cycle): 계절성보다 길고 불규칙한 반복(경기순환 등).
- 불규칙(Noise): 설명되지 않는 무작위 변동.
- 정상성(Stationarity): 평균/분산/자기공분산이 시간에 따라 변하지 않는 성질.
  - 약정상성은 다수의 통계 모형(ARIMA 등)에서 전제.
- ACF/PACF: 자기상관/부분자기상관 함수로 지연(lag) 간 상관을 파악.
- 해상도(Frequency): 초/분/시간/일 단위 등 표본 간격.
- 라벨링 방식: 
  - 와이드(Wide): 한 타임스탬프 행에 여러 측정치 컬럼.
  - 롱/넓게-좁게(Long/Narrow): (metric, labels, value) 튜플을 행으로.

---

## 4) 데이터 모델링과 저장
- 일반 RDB 스키마
  - 테이블 예: measurements(ts TIMESTAMPTZ, metric TEXT, labels JSONB, value DOUBLE PRECISION)
  - 인덱스: (metric, ts DESC), GIN(JSONB) for labels. 파티셔닝: RANGE(ts) by day/week.
- 시계열 DB(TSDB)
  - 예: TimescaleDB(PostgreSQL 확장), InfluxDB, Prometheus TSDB, ClickHouse.
  - 특징: 압축/청크/세그먼트 구조, downsampling/retention 정책, 태그 기반 인덱싱, 고속 집계.
- 보존 정책(Retention)과 다운샘플링
  - 원시(raw) 데이터는 짧게, 집계(예: 1m/5m/1h 평균·최대·p95)는 오래.
- 카디널리티(Cardinality)
  - label 조합의 고유 시계열 수. 너무 크면 메모리/인덱스 폭발. 라벨 설계가 성능의 핵심.

---

## 5) 수집과 시간 관리
- 타임존: 저장은 UTC, 표현만 로컬; 서머타임(DST) 이슈 방지.
- 지연/역류(out-of-order): 늦게 도착하는 포인트 처리 규칙 필요(수정 허용 창, upsert 정책).
- 중복 방지: 동일(ts, metric, labels) 키에 대해 idempotent write or de-duplication.
- 해상도와 정렬: 균일 간격이 아니더라도 저장 가능하나, 분석·모델링은 리샘플링으로 등간격화가 유리.

---

## 6) 전처리(Preprocessing)
- 리샘플링(Resampling): upsample/downsample, 집계 함수(mean, sum, max, last, ohlc).
- 결측치 처리: forward/backward fill, 선형 보간, kalman, interpolation 주의(분석/예측 영향).
- 이상치 처리: IQR, Z-score, STL 잔차 기반, Prophet/LSTM reconstruction error 기반.
- 윈도잉/롤링: rolling mean/std, expanding window, sliding features(lag, diff, rolling quantile).
- 정상성 변환: 차분(differencing), 로그/Box-Cox 변환, detrend/deseasonalize.

---

## 7) 모델링 개요
- 통계적 모델
  - ARIMA/ARIMAX, SARIMA: 정상성 가정, 계절성 포함 가능.
  - ETS(Exponential Smoothing, Holt-Winters): 추세/계절성 분해 기반.
  - 상태공간(State Space) & Kalman Filter.
- 기계학습/딥러닝
  - 트리/부스팅: lag/rolling feature 엔지니어링 기반(XGBoost, LightGBM).
  - RNN/LSTM/GRU, 1D CNN, Transformer(Time Series Transformer, Informer).
  - 이상탐지: IsolationForest, One-Class SVM, Autoencoder, Prophet의 outlier.
- 평가 지표
  - 회귀: MAE, RMSE, MAPE(small denominator 주의), sMAPE, MASE.
  - 이상탐지: Precision/Recall/PR-AUC, F1 at threshold, point vs range anomaly.

---

## 8) 시각화와 모니터링
- 라인 차트, 롤링 통계, 계절성 플롯(시즌 플롯), ACF/PACF 플롯.
- 대시보드: Grafana(+ Prometheus/InfluxDB), Kibana(Logs), Superset/Metabase(집계).
- 경보(Alerts): 임계치, 변화율, 시그널-잡음비, 예측 간 신뢰구간 이탈.

---

## 9) 실무 팁 & 안티패턴
- 팁
  - 모든 타임스탬프는 UTC + 정밀도(ns/us/ms/s) 명시.
  - 라벨 설계 시 고카디널리티 키(사용자 ID, UUID)를 직접 라벨로 두지 말고 필요 시 샘플링/집계로.
  - 원시·요약 데이터를 분리 저장하고, 재계산 가능하도록 파이프라인을 선언적으로 관리.
  - 지연 도착 허용 창(window)과 upsert 정책을 문서화.
  - 다운샘플링과 보존정책을 함께 설계해 저장 비용과 질의를 최적화.
- 안티패턴
  - 무분별한 1초 미만 해상도 수집(필요성·비용 대비 효과 검토 없이).
  - 라벨 폭발: 동적 키를 라벨에 그대로 투입.
  - 로컬 타임 저장(DST 버그, 집계 왜곡).

---

## 10) 예시
- RDB 스키마 예시 (PostgreSQL)
```sql
CREATE TABLE metrics (
  ts        TIMESTAMPTZ NOT NULL,
  metric    TEXT        NOT NULL,
  labels    JSONB       NOT NULL DEFAULT '{}',
  value     DOUBLE PRECISION NOT NULL,
  PRIMARY KEY (metric, ts, labels)
);
CREATE INDEX idx_metrics_ts_desc ON metrics (metric, ts DESC);
CREATE INDEX idx_metrics_labels_gin ON metrics USING GIN (labels);
```
- Prometheus 메트릭 개념
```
http_requests_total{method="GET", handler="/api/posts", status="200"} 12345 1735948800000
```
- 리샘플링(의사코드)
```
resample to 1m using mean; fill missing with previous; compute rolling_mean(5m)
```

---

## 11) 관련 도구
- 저장: TimescaleDB, InfluxDB, Prometheus, ClickHouse, OpenTSDB, QuestDB
- 처리/라이브러리: pandas, statsmodels, Prophet, darts, gluonts, sktime, kats
- 시각화/대시보드: Grafana, Superset, Metabase, Kibana

---

## 12) 함께 보면 좋은 문서
- CS/Algorithms/fourier_transform.md (주파수 도메인 분석)
- Interview/logs_and_metrics.md (모니터링/시계열 메트릭)
- CS/Database/nosql_databases.md (시계열과 NoSQL 맥락)

---

## 13) 요약
- 시계열 데이터는 시간 상관구조를 가지며, 수집·저장·전처리·모델링·시각화 전 단계에서 "시간" 자체의 제약과 기회를 고려해야 한다. 라벨 카디널리티, 보존/다운샘플링, 타임존, 정상성/계절성은 실무의 성패를 가르는 핵심 요소다.
