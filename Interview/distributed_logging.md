# 분산 서버 환경에서의 로그 수집 및 관리

## 목차
1. [분산 로깅의 개념과 필요성](#1-분산-로깅의-개념과-필요성)
   - [분산 시스템에서의 로깅 문제점](#분산-시스템에서의-로깅-문제점)
   - [중앙 집중식 로깅의 이점](#중앙-집중식-로깅의-이점)
2. [로그 기록 전략](#2-로그-기록-전략)
   - [구조화된 로깅](#구조화된-로깅)
   - [로그 레벨 전략](#로그-레벨-전략)
   - [컨텍스트 정보 포함](#컨텍스트-정보-포함)
3. [로그 수집 방법](#3-로그-수집-방법)
   - [로그 파일 기반 수집](#로그-파일-기반-수집)
   - [사이드카 패턴](#사이드카-패턴)
   - [로그 에이전트](#로그-에이전트)
4. [로그 집계 및 저장](#4-로그-집계-및-저장)
   - [중앙 로그 저장소](#중앙-로그-저장소)
   - [로그 보관 정책](#로그-보관-정책)
5. [로그 분석 및 모니터링](#5-로그-분석-및-모니터링)
   - [실시간 모니터링](#실시간-모니터링)
   - [로그 검색 및 필터링](#로그-검색-및-필터링)
   - [알림 설정](#알림-설정)
6. [ALB -> NGINX -> 애플리케이션 아키텍처에서의 로깅](#6-alb---nginx---애플리케이션-아키텍처에서의-로깅)
   - [각 계층별 로깅 전략](#각-계층별-로깅-전략)
   - [로그 상관관계 설정](#로그-상관관계-설정)
   - [구현 예시](#구현-예시)
7. [분산 로깅 도구 및 기술](#7-분산-로깅-도구-및-기술)
   - [ELK 스택 (Elasticsearch, Logstash, Kibana)](#elk-스택-elasticsearch-logstash-kibana)
   - [Fluentd/Fluent Bit](#fluentdfluent-bit)
   - [Prometheus와 Grafana](#prometheus와-grafana)
   - [AWS CloudWatch Logs](#aws-cloudwatch-logs)
8. [모범 사례 및 권장사항](#8-모범-사례-및-권장사항)
   - [보안 고려사항](#보안-고려사항)
   - [성능 최적화](#성능-최적화)
   - [확장성 계획](#확장성-계획)

## 1. 분산 로깅의 개념과 필요성

### 분산 시스템에서의 로깅 문제점

분산 시스템에서는 여러 서버, 컨테이너, 마이크로서비스 등이 함께 동작하며 각각 로그를 생성합니다. 이러한 환경에서는 다음과 같은 문제가 발생합니다:

- **로그 분산**: 로그가 여러 서버에 분산되어 있어 문제 추적이 어려움
- **시간 동기화**: 서로 다른 서버의 시간이 정확히 동기화되지 않아 이벤트 순서 파악이 어려움
- **상관관계 부재**: 하나의 요청이 여러 서비스를 거칠 때 연관된 로그를 찾기 어려움
- **일관성 없는 형식**: 각 서비스마다 다른 로그 형식을 사용하여 분석이 복잡해짐

### 중앙 집중식 로깅의 이점

중앙 집중식 로깅 시스템을 구축하면 다음과 같은 이점이 있습니다:

- **통합 가시성**: 모든 시스템의 로그를 한 곳에서 확인 가능
- **효율적인 문제 해결**: 오류 발생 시 관련된 모든 로그를 빠르게 검색하고 분석 가능
- **패턴 인식**: 시스템 전반의 패턴과 추세를 파악하여 잠재적 문제 예측 가능
- **감사 및 규정 준수**: 보안 이벤트 추적 및 규정 준수를 위한 증거 제공
- **운영 인사이트**: 시스템 동작에 대한 심층적인 이해와 최적화 기회 발견

## 2. 로그 기록 전략

### 구조화된 로깅

구조화된 로깅은 로그를 단순 텍스트가 아닌 구조화된 형식(주로 JSON)으로 기록하는 방식입니다:

```json
{
  "timestamp": "2023-11-01T12:34:56.789Z",
  "level": "ERROR",
  "service": "user-service",
  "traceId": "abc123",
  "message": "사용자 인증 실패",
  "userId": "user123",
  "errorCode": "AUTH_001"
}
```

**장점**:
- 기계가 읽고 파싱하기 쉬움
- 필드별 검색 및 필터링 용이
- 로그 분석 도구와의 통합이 간편

### 로그 레벨 전략

각 환경과 상황에 맞는 적절한 로그 레벨을 사용하는 것이 중요합니다:

- **ERROR**: 애플리케이션이 더 이상 정상 작동할 수 없는 심각한 문제
- **WARN**: 잠재적인 문제이지만 애플리케이션은 계속 작동 가능
- **INFO**: 일반적인 애플리케이션 진행 상황 (운영 환경의 기본 레벨로 권장)
- **DEBUG**: 개발 및 문제 해결에 유용한 상세 정보 (개발/테스트 환경에서만 사용)
- **TRACE**: 가장 상세한 정보 (특별한 디버깅 상황에서만 일시적으로 사용)

### 컨텍스트 정보 포함

효과적인 로그 분석을 위해 다음과 같은 컨텍스트 정보를 포함하는 것이 중요합니다:

- **요청 ID/트레이스 ID**: 여러 서비스에 걸친 요청 추적을 위한 고유 식별자
- **사용자 정보**: 사용자 ID, IP 주소 등
- **서비스/인스턴스 정보**: 서비스 이름, 인스턴스 ID, 버전 등
- **환경 정보**: 개발, 테스트, 운영 등의 환경 구분
- **메타데이터**: 관련 비즈니스 컨텍스트 (주문 ID, 상품 ID 등)

## 3. 로그 수집 방법

### 로그 파일 기반 수집

가장 기본적인 방법으로, 애플리케이션이 로컬 파일 시스템에 로그를 기록하고 이를 수집 에이전트가 읽어 중앙 저장소로 전송합니다:

1. 애플리케이션은 로그 파일에 로그 기록
2. 로그 수집 에이전트(Filebeat, Fluentd 등)가 로그 파일 모니터링
3. 변경 사항 감지 시 중앙 로그 시스템으로 전송

**장점**:
- 애플리케이션 코드 변경 최소화
- 로깅 실패가 애플리케이션 동작에 영향을 주지 않음

**단점**:
- 디스크 I/O 부하 발생
- 컨테이너 환경에서 로그 파일 관리의 복잡성

### 사이드카 패턴

특히 Kubernetes와 같은 컨테이너 환경에서 유용한 패턴입니다:

1. 메인 애플리케이션 컨테이너는 표준 출력(stdout/stderr)에 로그 기록
2. 같은 Pod 내의 사이드카 컨테이너가 로그를 수집하여 중앙 로그 시스템으로 전송

```yaml
# Kubernetes Pod 예시
apiVersion: v1
kind: Pod
metadata:
  name: app-with-logging
spec:
  containers:
  - name: app
    image: my-app:latest
  - name: log-collector
    image: fluent/fluent-bit:latest
    volumeMounts:
    - name: shared-logs
      mountPath: /var/log/app
  volumes:
  - name: shared-logs
    emptyDir: {}
```

**장점**:
- 관심사 분리: 애플리케이션과 로그 수집 로직 분리
- 로그 수집 실패가 애플리케이션에 영향을 주지 않음
- 로그 수집 구성을 애플리케이션과 독립적으로 업데이트 가능

### 로그 에이전트

호스트 레벨에서 실행되는 에이전트를 통해 여러 애플리케이션의 로그를 수집합니다:

- **Filebeat**: 경량 로그 수집기로, 로그 파일을 모니터링하고 Elasticsearch나 Logstash로 전송
- **Fluentd/Fluent Bit**: 다양한 입력 소스에서 로그를 수집하고 여러 대상으로 전송 가능
- **Vector**: 고성능 관측성 데이터 파이프라인 도구

**구성 예시 (Filebeat)**:
```yaml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /var/log/nginx/*.log
    - /var/log/app/*.log

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  index: "logs-%{+yyyy.MM.dd}"
```

## 4. 로그 집계 및 저장

### 중앙 로그 저장소

분산 시스템의 로그를 저장하기 위한 주요 옵션:

1. **Elasticsearch**:
   - 분산형 검색 및 분석 엔진
   - 대량의 로그 데이터 저장 및 실시간 검색에 최적화
   - ELK 스택의 핵심 구성 요소

2. **Amazon CloudWatch Logs**:
   - AWS의 관리형 로그 저장 및 모니터링 서비스
   - AWS 서비스와의 통합이 용이
   - 자동 확장 및 장기 보관 지원

3. **Google Cloud Logging**:
   - GCP의 관리형 로그 저장 및 분석 서비스
   - Google Cloud 서비스와의 통합이 용이
   - BigQuery와 연동하여 고급 분석 가능

4. **Loki**:
   - Grafana Labs에서 개발한 로그 집계 시스템
   - Prometheus에서 영감을 받은 라벨 기반 접근 방식
   - 리소스 효율적인 로그 저장 및 쿼리

### 로그 보관 정책

비용 효율적인 로그 관리를 위한 보관 정책:

- **핫 스토리지**: 최근 로그(1-7일)는 빠른 액세스를 위해 고성능 스토리지에 보관
- **웜 스토리지**: 중간 기간 로그(8-30일)는 중간 성능의 스토리지에 보관
- **콜드 스토리지**: 오래된 로그(30일 이상)는 저비용 스토리지에 보관
- **로그 압축**: 오래된 로그는 압축하여 저장 공간 절약
- **로그 샘플링**: 높은 볼륨의 로그는 샘플링하여 저장 (예: DEBUG 레벨 로그의 10%만 저장)

**Elasticsearch 인덱스 수명 주기 관리 예시**:
```
# Elasticsearch ILM API 호출
PUT _ilm/policy/logs_policy
{
  "policy": {
    "phases": {
      "hot": {
        "actions": {
          "rollover": {
            "max_age": "1d",
            "max_size": "50gb"
          }
        }
      },
      "warm": {
        "min_age": "2d",
        "actions": {
          "shrink": {
            "number_of_shards": 1
          },
          "forcemerge": {
            "max_num_segments": 1
          }
        }
      },
      "cold": {
        "min_age": "30d",
        "actions": {
          "freeze": {}
        }
      },
      "delete": {
        "min_age": "90d",
        "actions": {
          "delete": {}
        }
      }
    }
  }
}
```

## 5. 로그 분석 및 모니터링

### 실시간 모니터링

로그 데이터를 실시간으로 모니터링하여 시스템 상태를 파악하고 문제를 조기에 발견:

- **대시보드**: Kibana, Grafana 등을 사용하여 주요 지표와 로그 패턴 시각화
- **이상 탐지**: 비정상적인 로그 패턴이나 오류율 증가 감지
- **서비스 상태 모니터링**: 각 서비스의 오류율, 응답 시간 등 핵심 지표 추적

### 로그 검색 및 필터링

효과적인 문제 해결을 위한 로그 검색 및 필터링 기능:

- **전체 텍스트 검색**: 특정 키워드나 오류 메시지 검색
- **필드 기반 필터링**: 특정 서비스, 로그 레벨, 시간 범위 등으로 필터링
- **고급 쿼리**: Lucene 쿼리 구문이나 Elasticsearch Query DSL을 사용한 복잡한 검색

**Kibana 쿼리 예시**:
```
service: "user-service" AND level: "ERROR" AND message: "authentication failed"
```

### 알림 설정

중요한 이벤트 발생 시 즉시 알림을 받을 수 있도록 설정:

- **임계값 기반 알림**: 특정 오류 발생 횟수가 임계값을 초과할 때 알림
- **패턴 기반 알림**: 특정 패턴의 로그 메시지가 발생할 때 알림
- **알림 채널**: 이메일, Slack, PagerDuty 등 다양한 채널로 알림 전송

**Elasticsearch Watcher 알림 예시**:
```json
{
  "trigger": {
    "schedule": {
      "interval": "5m"
    }
  },
  "input": {
    "search": {
      "request": {
        "indices": ["logs-*"],
        "body": {
          "query": {
            "bool": {
              "must": [
                { "match": { "level": "ERROR" } },
                { "range": { "@timestamp": { "gte": "now-5m" } } }
              ]
            }
          }
        }
      }
    }
  },
  "condition": {
    "compare": {
      "ctx.payload.hits.total": {
        "gt": 10
      }
    }
  },
  "actions": {
    "send_email": {
      "email": {
        "to": "ops-team@example.com",
        "subject": "High error rate detected",
        "body": {
          "text": "More than 10 errors in the last 5 minutes"
        }
      }
    }
  }
}
```

## 6. ALB -> NGINX -> 애플리케이션 아키텍처에서의 로깅

### 각 계층별 로깅 전략

**1. ALB (Application Load Balancer)**:
- **로그 내용**: 클라이언트 IP, 요청 시간, 요청 경로, 응답 코드, 처리 시간, 대상 서버 등
- **로그 저장**: AWS S3 버킷에 액세스 로그 저장
- **설정 방법**: AWS 콘솔 또는 CLI를 통해 ALB 액세스 로그 활성화

```bash
# AWS CLI를 사용한 ALB 액세스 로그 활성화
aws elbv2 modify-load-balancer-attributes \
  --load-balancer-arn arn:aws:elasticloadbalancing:region:account-id:loadbalancer/app/load-balancer-name/load-balancer-id \
  --attributes Key=access_logs.s3.enabled,Value=true Key=access_logs.s3.bucket,Value=bucket-name Key=access_logs.s3.prefix,Value=prefix
```

**2. NGINX**:
- **로그 내용**: 클라이언트 IP, 요청 시간, HTTP 메서드, URL, HTTP 버전, 상태 코드, 응답 크기, 참조자, 사용자 에이전트 등
- **로그 형식**: 액세스 로그와 오류 로그 분리
- **설정 방법**: NGINX 설정 파일에서 로그 형식 및 위치 정의

```nginx
# NGINX 로그 설정 예시
http {
    log_format json_combined escape=json
      '{'
        '"time_local":"$time_local",'
        '"remote_addr":"$remote_addr",'
        '"remote_user":"$remote_user",'
        '"request":"$request",'
        '"status": "$status",'
        '"body_bytes_sent":"$body_bytes_sent",'
        '"request_time":"$request_time",'
        '"http_referrer":"$http_referer",'
        '"http_user_agent":"$http_user_agent",'
        '"request_id":"$request_id"'
      '}';

    access_log /var/log/nginx/access.log json_combined;
    error_log /var/log/nginx/error.log;
}
```

**3. 애플리케이션 (9092, 9093)**:
- **로그 내용**: 요청 처리 세부 정보, 비즈니스 로직 이벤트, 오류 및 예외, 성능 지표 등
- **로그 형식**: JSON 형식의 구조화된 로그 권장
- **설정 방법**: 로깅 프레임워크(Logback, Log4j2 등) 설정

```xml
<!-- Logback 설정 예시 (logback-spring.xml) -->
<configuration>
  <appender name="JSON" class="ch.qos.logback.core.ConsoleAppender">
    <encoder class="net.logstash.logback.encoder.LogstashEncoder">
      <includeMdc>true</includeMdc>
      <customFields>{"service":"user-service","instance":"${HOSTNAME}"}</customFields>
    </encoder>
  </appender>

  <root level="INFO">
    <appender-ref ref="JSON" />
  </root>
</configuration>
```

### 로그 상관관계 설정

여러 계층에 걸친 요청을 추적하기 위해 상관관계 ID(Correlation ID)를 사용:

1. **ALB에서 요청 ID 생성**:
   - ALB는 `X-Amzn-Trace-Id` 헤더를 자동으로 생성하거나 전달

2. **NGINX에서 요청 ID 전파**:
   - ALB에서 생성된 요청 ID를 애플리케이션으로 전달하거나, 없는 경우 새로 생성

```nginx
# NGINX 요청 ID 설정
http {
    # 요청 ID 생성 또는 전달
    map $http_x_amzn_trace_id $request_id {
        default   $http_x_amzn_trace_id;
        ""        $request_id_new;
    }

    # 새 요청 ID 생성 (ALB에서 전달되지 않은 경우)
    map $time_iso8601 $request_id_new {
        default   $request_id_gen;
    }

    # UUID 생성
    perl_set $request_id_gen {
        sub {
            return sprintf('%04x%04x-%04x-%04x-%04x-%04x%04x%04x',
                rand(0xffff), rand(0xffff),
                rand(0xffff),
                rand(0x0fff) | 0x4000,
                rand(0x3fff) | 0x8000,
                rand(0xffff), rand(0xffff), rand(0xffff));
        }
    }

    # 요청 ID를 애플리케이션으로 전달
    proxy_set_header X-Request-ID $request_id;
}
```

3. **애플리케이션에서 요청 ID 사용**:
   - NGINX에서 전달된 요청 ID를 MDC에 저장하여 로그에 포함

```java
// Spring Boot에서 요청 ID 처리
@Component
public class RequestIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        MDC.put("requestId", requestId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
```

### 구현 예시

**ALB -> NGINX -> 애플리케이션(9092, 9093)** 아키텍처에서의 로그 수집 구현:

1. **로그 수집 아키텍처**:

```
ALB 액세스 로그 --> S3 --> Lambda --> Elasticsearch
                                   |
NGINX 로그 --> Filebeat ---------->|
                                   |
애플리케이션 로그 --> Fluent Bit -->|
```

2. **Filebeat 설정 (NGINX 로그 수집)**:

```yaml
# filebeat.yml
filebeat.inputs:
- type: log
  enabled: true
  paths:
    - /var/log/nginx/access.log
  json.keys_under_root: true
  json.add_error_key: true

output.elasticsearch:
  hosts: ["elasticsearch:9200"]
  index: "nginx-logs-%{+yyyy.MM.dd}"
```

3. **Fluent Bit 설정 (애플리케이션 로그 수집)**:

```ini
# fluent-bit.conf
[INPUT]
    Name              tail
    Path              /var/log/app/*.log
    Parser            json
    Tag               app.*

[FILTER]
    Name              grep
    Match             app.*
    Regex             level (INFO|WARN|ERROR)

[OUTPUT]
    Name              es
    Match             app.*
    Host              elasticsearch
    Port              9200
    Index             app-logs-${HOSTNAME}
    Type              _doc
    Logstash_Format   On
    Logstash_Prefix   app-logs
    Time_Key          @timestamp
```

4. **Lambda 함수 (ALB 로그 처리)**:

```python
# AWS Lambda 함수 예시 (Python)
import json
import gzip
import base64
import boto3
from elasticsearch import Elasticsearch

def lambda_handler(event, context):
    # S3 이벤트에서 버킷 및 키 정보 추출
    bucket = event['Records'][0]['s3']['bucket']['name']
    key = event['Records'][0]['s3']['object']['key']

    # S3에서 로그 파일 다운로드
    s3 = boto3.client('s3')
    response = s3.get_object(Bucket=bucket, Key=key)
    content = response['Body'].read()

    # gzip 압축 해제
    if key.endswith('.gz'):
        content = gzip.decompress(content)

    # 로그 파싱 및 Elasticsearch에 전송
    es = Elasticsearch(['https://elasticsearch:9200'])
    log_lines = content.decode('utf-8').splitlines()

    for line in log_lines:
        log_entry = json.loads(line)
        es.index(index="alb-logs", body=log_entry)

    return {
        'statusCode': 200,
        'body': f'Processed {len(log_lines)} log entries'
    }
```

## 7. 분산 로깅 도구 및 기술

### ELK 스택 (Elasticsearch, Logstash, Kibana)

가장 널리 사용되는 오픈소스 로그 관리 솔루션:

1. **Elasticsearch**:
   - 분산형 검색 및 분석 엔진
   - 대량의 로그 데이터 저장 및 검색에 최적화
   - JSON 기반의 문서 저장소

2. **Logstash**:
   - 데이터 수집 및 변환 파이프라인
   - 다양한 소스에서 데이터 수집, 변환 후 Elasticsearch로 전송
   - 강력한 필터 기능으로 로그 데이터 정규화 및 보강

3. **Kibana**:
   - Elasticsearch 데이터 시각화 및 탐색 도구
   - 대시보드, 차트, 그래프 등 다양한 시각화 제공
   - 로그 검색 및 분석을 위한 직관적인 인터페이스

**ELK 스택 구성 예시**:
```yaml
# docker-compose.yml
version: '3'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:7.15.0
    environment:
      - discovery.type=single-node
    ports:
      - "9200:9200"

  logstash:
    image: docker.elastic.co/logstash/logstash:7.15.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    depends_on:
      - elasticsearch

  kibana:
    image: docker.elastic.co/kibana/kibana:7.15.0
    ports:
      - "5601:5601"
    depends_on:
      - elasticsearch
```

### Fluentd/Fluent Bit

클라우드 네이티브 환경에 최적화된 로그 수집기:

1. **Fluentd**:
   - CNCF 졸업 프로젝트
   - 다양한 입력 및 출력 플러그인 지원
   - 로그 수집, 처리, 전달을 위한 통합 로깅 계층

2. **Fluent Bit**:
   - Fluentd의 경량 버전
   - 리소스 사용량이 적어 사이드카 패턴에 적합
   - C로 작성되어 성능이 우수

**Kubernetes에서의 Fluent Bit 배포 예시**:
```yaml
# fluent-bit-daemonset.yaml
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: fluent-bit
  namespace: logging
spec:
  selector:
    matchLabels:
      app: fluent-bit
  template:
    metadata:
      labels:
        app: fluent-bit
    spec:
      containers:
      - name: fluent-bit
        image: fluent/fluent-bit:1.8
        volumeMounts:
        - name: varlog
          mountPath: /var/log
        - name: config
          mountPath: /fluent-bit/etc/
      volumes:
      - name: varlog
        hostPath:
          path: /var/log
      - name: config
        configMap:
          name: fluent-bit-config
```

### Prometheus와 Grafana

메트릭 모니터링과 로그 분석을 결합한 솔루션:

1. **Prometheus**:
   - 시계열 데이터베이스 및 모니터링 시스템
   - 메트릭 수집 및 알림에 최적화
   - 강력한 쿼리 언어(PromQL) 제공

2. **Grafana**:
   - 다양한 데이터 소스의 메트릭 및 로그 시각화
   - Prometheus, Elasticsearch, Loki 등과 통합
   - 대시보드 템플릿 및 알림 기능 제공

3. **Loki**:
   - Grafana Labs의 로그 집계 시스템
   - Prometheus와 유사한 라벨 기반 접근 방식
   - 리소스 효율적인 로그 저장 및 쿼리

**Prometheus와 Grafana 설정 예시**:
```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  - job_name: 'app'
    static_configs:
      - targets: ['app:8080']
```

### AWS CloudWatch Logs

AWS 환경에서의 통합 로깅 솔루션:

1. **CloudWatch Logs**:
   - AWS의 관리형 로그 저장 및 모니터링 서비스
   - 로그 그룹 및 로그 스트림으로 구성
   - 자동 확장 및 장기 보관 지원

2. **CloudWatch Logs Insights**:
   - 로그 데이터 쿼리 및 분석 도구
   - SQL과 유사한 쿼리 언어 제공
   - 대시보드 및 시각화 기능

3. **CloudWatch Alarms**:
   - 로그 패턴 기반 알림 설정
   - 다양한 AWS 서비스와 통합

**CloudWatch Logs 설정 예시 (AWS CDK)**:
```typescript
import * as cdk from 'aws-cdk-lib';
import * as logs from 'aws-cdk-lib/aws-logs';
import * as lambda from 'aws-cdk-lib/aws-lambda';

export class LoggingStack extends cdk.Stack {
  constructor(scope: cdk.Construct, id: string, props?: cdk.StackProps) {
    super(scope, id, props);

    // 로그 그룹 생성
    const logGroup = new logs.LogGroup(this, 'AppLogs', {
      retention: logs.RetentionDays.ONE_WEEK,
    });

    // 로그 필터 생성
    const errorFilter = new logs.MetricFilter(this, 'ErrorFilter', {
      logGroup,
      filterPattern: logs.FilterPattern.stringValue('$.level', '=', 'ERROR'),
      metricNamespace: 'CustomMetrics',
      metricName: 'ErrorCount',
      defaultValue: 0,
    });

    // 알람 생성
    const alarm = new cdk.aws_cloudwatch.Alarm(this, 'ErrorAlarm', {
      metric: errorFilter.metric(),
      threshold: 5,
      evaluationPeriods: 1,
      alarmDescription: 'Alarm when error count exceeds 5 in 1 minute',
    });
  }
}
```

## 8. 모범 사례 및 권장사항

### 보안 고려사항

로그 데이터는 민감한 정보를 포함할 수 있으므로 보안에 주의해야 합니다:

1. **민감 정보 필터링**:
   - 개인 식별 정보(PII), 신용카드 정보, 비밀번호 등 민감 정보 마스킹
   - 로그 생성 시점에서 필터링하는 것이 가장 안전

2. **전송 중 암호화**:
   - TLS/SSL을 사용하여 로그 데이터 전송
   - 로그 수집기와 중앙 저장소 간 보안 연결 설정

3. **저장 데이터 암호화**:
   - 저장된 로그 데이터 암호화
   - AWS KMS, Elasticsearch 보안 기능 등 활용

4. **접근 제어**:
   - 로그 데이터에 대한 역할 기반 접근 제어(RBAC) 구현
   - 최소 권한 원칙 적용

### 성능 최적화

로그 시스템이 애플리케이션 성능에 영향을 미치지 않도록 최적화:

1. **비동기 로깅**:
   - 애플리케이션에서 비동기 로깅 사용
   - 로깅 작업이 주 스레드를 차단하지 않도록 설정

2. **버퍼링 및 배치 처리**:
   - 로그 수집기에서 버퍼링 기능 활용
   - 로그를 배치로 전송하여 네트워크 오버헤드 감소

3. **로그 샘플링**:
   - 높은 볼륨의 로그는 샘플링하여 처리
   - 예: DEBUG 레벨 로그의 일부만 수집

4. **인덱싱 최적화**:
   - Elasticsearch 인덱스 설계 최적화
   - 필요한 필드만 인덱싱하여 저장 공간 및 쿼리 성능 개선

### 확장성 계획

시스템 성장에 따른 로깅 인프라 확장 계획:

1. **수평적 확장**:
   - 로그 수집기 및 저장소의 수평적 확장 설계
   - 클러스터 구성으로 부하 분산

2. **샤딩 전략**:
   - 로그 데이터 샤딩으로 분산 저장
   - 시간, 서비스, 환경 등을 기준으로 샤딩

3. **자동 확장**:
   - 클라우드 환경에서 자동 확장 설정
   - 트래픽 증가에 따른 리소스 자동 조정

4. **데이터 수명주기 관리**:
   - 로그 데이터의 수명주기 정책 수립
   - 오래된 데이터 자동 아카이브 또는 삭제

분산 시스템에서의 효과적인 로깅은 시스템 가시성, 문제 해결, 보안 감사 등에 필수적입니다. 위에서 설명한 전략과 도구를 활용하여 ALB -> NGINX -> 애플리케이션 아키텍처에서 효과적인 로깅 시스템을 구축할 수 있습니다.
