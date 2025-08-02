# Kafka Streams 이란?

Kafka Streams는 Apache Kafka에서 제공하는 클라이언트 라이브러리로, 스트림 처리 애플리케이션을 구축하기 위한 경량화된 프레임워크입니다. 이 라이브러리는 데이터 스트림을 처리하고 변환하는 작업을 간소화하며, Kafka의 강력한 기능을 활용하여 확장성과 내결함성을 갖춘 실시간 데이터 파이프라인을 구축할 수 있게 해줍니다.

## Kafka Streams의 기본 개념

### 1) 스트림 처리란?

스트림 처리(Stream Processing)는 연속적으로 생성되는 데이터를 실시간으로 처리하는 컴퓨팅 패러다임입니다.

- **배치 처리와의 차이점**: 배치 처리는 데이터를 모아서 주기적으로 처리하는 반면, 스트림 처리는 데이터가 생성되는 즉시 처리합니다.
- **실시간성**: 데이터가 생성되는 즉시 처리하여 실시간 인사이트를 제공합니다.
- **무한한 데이터**: 스트림은 이론적으로 끝이 없는 무한한 데이터 시퀀스를 처리합니다.

### 2) Kafka Streams의 위치

Kafka 생태계에서 Kafka Streams의 위치:

- **Kafka 브로커**: 메시지를 저장하고 전달하는 서버
- **Kafka 프로듀서**: 메시지를 생성하여 Kafka 브로커에 전송
- **Kafka 컨슈머**: Kafka 브로커에서 메시지를 소비
- **Kafka Streams**: Kafka 토픽에서 데이터를 읽고, 처리한 후, 결과를 다시 Kafka 토픽에 쓰는 클라이언트 라이브러리

Kafka Streams는 독립적인 서버가 아닌 애플리케이션에 내장되는 라이브러리 형태로 제공됩니다.

## Kafka Streams의 아키텍처

### 1) 핵심 구성 요소

Kafka Streams의 핵심 구성 요소는 다음과 같습니다:

- **스트림(Stream)**: 무한한 데이터 레코드의 시퀀스
- **스트림 프로세서(Stream Processor)**: 스트림의 데이터를 처리하는 노드
- **토폴로지(Topology)**: 스트림 프로세서들의 연결 그래프
- **태스크(Task)**: 토폴로지의 실행 단위
- **파티션(Partition)**: 병렬 처리를 위한 데이터 분할 단위

### 2) 처리 모델

Kafka Streams는 두 가지 주요 처리 모델을 제공합니다:

- **KStream**: 이벤트의 변경 로그(changelog)를 나타내는 레코드 스트림
  - 각 레코드는 독립적인 이벤트로 처리
  - 상태를 유지하지 않는 처리에 적합

- **KTable**: 변경 가능한 데이터의 스냅샷을 나타내는 레코드 스트림
  - 동일한 키를 가진 레코드는 최신 값으로 업데이트
  - 상태 기반 처리에 적합

### 3) 상태 관리

Kafka Streams는 상태 저장 처리를 위한 다양한 기능을 제공합니다:

- **상태 저장소(State Store)**: 처리 중인 데이터의 상태를 저장
- **로컬 상태**: 각 인스턴스가 로컬 디스크에 상태 정보 유지
- **내결함성**: 장애 발생 시 상태를 복구할 수 있는 메커니즘 제공
- **상태 마이그레이션**: 인스턴스 간 상태 재분배 지원

## Kafka Streams의 주요 특징

### 1) 간단한 API

Kafka Streams는 두 가지 수준의 API를 제공합니다:

- **고수준 DSL(Domain Specific Language)**: 선언적 방식으로 스트림 처리 로직을 구현
  ```java
  KStream<String, String> source = builder.stream("input-topic");
  KStream<String, String> transformed = source.map((key, value) -> KeyValue.pair(key, value.toUpperCase()));
  transformed.to("output-topic");
  ```

- **저수준 Processor API**: 세밀한 제어가 필요한 복잡한 처리 로직 구현
  ```java
  Topology topology = new Topology();
  topology.addSource("Source", "input-topic")
          .addProcessor("Process", MyProcessor::new, "Source")
          .addSink("Sink", "output-topic", "Process");
  ```

### 2) 확장성과 내결함성

- **수평적 확장**: 애플리케이션 인스턴스를 추가하여 처리 용량 확장
- **동적 로드 밸런싱**: 인스턴스 간 작업 자동 재분배
- **내결함성**: 장애 발생 시 자동 복구 메커니즘
- **정확히 한 번 처리(Exactly-Once Processing)**: 중복 없는 정확한 데이터 처리 보장

### 3) 경량 클라이언트 라이브러리

- **서버리스**: 별도의 클러스터 없이 애플리케이션에 내장
- **낮은 지연 시간**: 로컬 상태 저장소를 활용한 빠른 처리
- **작은 설치 공간**: 최소한의 의존성으로 가벼운 배포
- **언어 지원**: 주로 Java/Kotlin 지원, 다른 언어는 커뮤니티 라이브러리 활용

## Kafka Streams의 주요 연산자

Kafka Streams DSL은 다양한 스트림 처리 연산자를 제공합니다:

### 1) 상태를 유지하지 않는 연산자

- **map / mapValues**: 각 레코드를 변환
- **filter / filterNot**: 조건에 따라 레코드 필터링
- **flatMap / flatMapValues**: 하나의 레코드를 여러 레코드로 변환
- **branch**: 조건에 따라 스트림을 여러 스트림으로 분할
- **foreach**: 각 레코드에 대해 부수 효과(side effect) 수행

### 2) 상태를 유지하는 연산자

- **count**: 키별 레코드 수 집계
- **reduce / aggregate**: 키별 데이터 집계
- **join**: 서로 다른 스트림 또는 테이블 조인
- **windowing**: 시간 기반 윈도우 처리
- **groupBy / groupByKey**: 키를 기준으로 그룹화

### 3) 윈도우 처리

시간 기반 처리를 위한 다양한 윈도우 유형:

- **텀블링 윈도우(Tumbling Window)**: 고정 크기, 겹치지 않는 윈도우
- **호핑 윈도우(Hopping Window)**: 고정 크기, 겹치는 윈도우
- **슬라이딩 윈도우(Sliding Window)**: 시간에 따라 연속적으로 이동하는 윈도우
- **세션 윈도우(Session Window)**: 활동 기간에 따라 동적으로 크기가 조정되는 윈도우

## Kafka Streams의 활용 사례

### 1) 실시간 데이터 분석

- **실시간 대시보드**: 비즈니스 지표의 실시간 모니터링
- **이상 탐지**: 비정상적인 패턴이나 행동 실시간 감지
- **트렌드 분석**: 실시간 트렌드 및 패턴 식별

### 2) 데이터 변환 및 강화

- **ETL 파이프라인**: 데이터 추출, 변환, 적재 작업
- **데이터 정규화**: 다양한 소스의 데이터를 일관된 형식으로 변환
- **데이터 강화**: 외부 소스의 정보로 데이터 보강

### 3) 이벤트 기반 애플리케이션

- **이벤트 소싱**: 상태 변경을 이벤트로 저장하고 처리
- **CQRS(Command Query Responsibility Segregation)**: 명령과 쿼리 책임 분리
- **실시간 알림**: 특정 조건 발생 시 알림 생성

### 4) IoT 데이터 처리

- **센서 데이터 처리**: 대량의 센서 데이터 실시간 처리
- **디바이스 모니터링**: IoT 디바이스 상태 모니터링
- **예측 유지보수**: 장비 고장 예측 및 유지보수 최적화

## Kafka Streams vs 다른 스트림 처리 기술

### 1) Kafka Streams vs Apache Spark Streaming

| 특징 | Kafka Streams | Spark Streaming |
|-----|--------------|-----------------|
| 아키텍처 | 라이브러리 | 클러스터 기반 |
| 배포 | 애플리케이션에 내장 | 독립 클러스터 필요 |
| 지연 시간 | 밀리초 단위 | 초 단위 (마이크로 배치) |
| 확장성 | 애플리케이션 인스턴스 추가 | 클러스터 노드 추가 |
| 상태 관리 | 내장 상태 저장소 | RDD/DataFrame 기반 |
| 학습 곡선 | 상대적으로 낮음 | 상대적으로 높음 |
| 사용 사례 | 경량 스트림 처리 | 복잡한 분석, ML 통합 |

### 2) Kafka Streams vs Apache Flink

| 특징 | Kafka Streams | Apache Flink |
|-----|--------------|--------------|
| 아키텍처 | 라이브러리 | 클러스터 기반 |
| 데이터 소스 | Kafka 전용 | 다양한 소스 지원 |
| API 다양성 | 제한적 | 풍부한 API |
| 윈도우 처리 | 기본 윈도우 유형 | 고급 윈도우 기능 |
| 확장성 | 중간 규모 | 대규모 |
| 상태 관리 | 내장 상태 저장소 | 분산 스냅샷 |
| 사용 사례 | Kafka 중심 처리 | 범용 스트림 처리 |

### 3) Kafka Streams vs KSQL/ksqlDB

| 특징 | Kafka Streams | KSQL/ksqlDB |
|-----|--------------|-------------|
| 인터페이스 | 프로그래밍 API | SQL 기반 |
| 유연성 | 높음 (코드 기반) | 제한적 (SQL 문법) |
| 사용 난이도 | 개발자 중심 | SQL 지식만 필요 |
| 배포 | 애플리케이션에 내장 | 독립 서버 또는 내장 |
| 사용 사례 | 복잡한 처리 로직 | 간단한 쿼리 및 변환 |

## Kafka Streams 애플리케이션 개발 예시

### 1) 기본 설정

Maven 의존성 설정:

```xml
<dependency>
    <groupId>org.apache.kafka</groupId>
    <artifactId>kafka-streams</artifactId>
    <version>3.5.1</version>
</dependency>
```

Gradle 의존성 설정:

```gradle
implementation 'org.apache.kafka:kafka-streams:3.5.1'
```

### 2) 간단한 스트림 처리 예제 (Java)

```java
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

public class WordCountExample {
    public static void main(String[] args) {
        // 스트림 설정
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "wordcount-application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        // 스트림 토폴로지 생성
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> textLines = builder.stream("input-topic");
        
        // 단어 분리 및 카운트
        KStream<String, Long> wordCounts = textLines
            .flatMapValues(value -> Arrays.asList(value.toLowerCase().split("\\W+")))
            .groupBy((key, word) -> word)
            .count()
            .toStream();
        
        // 결과를 출력 토픽에 저장
        wordCounts.to("output-topic");

        // 스트림 시작
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        // 종료 시 정리
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
```

### 3) 상태 저장 처리 예제 (Java)

```java
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.time.Duration;
import java.util.Properties;

public class TransactionAnalysis {
    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "transaction-analysis");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.Double().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        
        // 트랜잭션 스트림 생성
        KStream<String, Double> transactions = builder.stream("transactions-topic");
        
        // 5분 윈도우로 사용자별 총 거래액 계산
        transactions
            .groupByKey()
            .windowedBy(TimeWindows.of(Duration.ofMinutes(5)))
            .reduce((aggValue, newValue) -> aggValue + newValue)
            .toStream()
            .map((windowedKey, value) -> KeyValue.pair(
                windowedKey.key() + "@" + windowedKey.window().start() + "-" + windowedKey.window().end(),
                value
            ))
            .to("user-spending-topic");

        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
```

## Kafka Streams 모범 사례

### 1) 설계 및 개발

- **적절한 파티셔닝**: 데이터 분포와 병렬 처리를 고려한 파티션 설계
- **직렬화 형식 선택**: Avro, Protobuf 등 스키마 기반 형식 사용 권장
- **예외 처리**: 오류 처리 및 복구 전략 구현
- **테스트**: TopologyTestDriver를 활용한 단위 테스트 작성

### 2) 운영 및 모니터링

- **적절한 리소스 할당**: CPU, 메모리, 디스크 공간 등 리소스 계획
- **로깅 전략**: 적절한 로그 레벨 설정 및 로그 관리
- **메트릭 수집**: JMX 메트릭을 통한 성능 모니터링
- **확장 계획**: 데이터 증가에 따른 확장 전략 수립

### 3) 성능 최적화

- **상태 저장소 크기 관리**: 불필요한 데이터 제거 및 압축 설정
- **캐시 튜닝**: 캐시 크기 및 정책 최적화
- **배치 처리 설정**: 처리량과 지연 시간 간의 균형 조정
- **토폴로지 최적화**: 불필요한 중간 토픽 제거 및 연산자 결합

## 결론

Kafka Streams는 Apache Kafka 기반의 경량 스트림 처리 라이브러리로, 실시간 데이터 처리 애플리케이션을 쉽게 개발할 수 있게 해줍니다. 서버리스 아키텍처, 상태 관리, 내결함성, 확장성 등의 특징을 갖추고 있어 다양한 실시간 데이터 처리 요구사항을 충족시킬 수 있습니다.

Kafka Streams는 특히 Kafka 생태계 내에서 데이터를 처리하는 데 최적화되어 있으며, 복잡한 클러스터 설정 없이도 강력한 스트림 처리 기능을 제공합니다. 단순한 데이터 변환부터 복잡한 이벤트 기반 애플리케이션까지 다양한 사용 사례에 적용할 수 있어, 실시간 데이터 처리가 필요한 현대 애플리케이션 개발에 유용한 도구입니다.

## 참고 자료

- [Apache Kafka Streams 공식 문서](https://kafka.apache.org/documentation/streams/)
- [Kafka Streams in Action (Manning Publications)](https://www.manning.com/books/kafka-streams-in-action)
- [Designing Event-Driven Systems (O'Reilly Media)](https://www.oreilly.com/library/view/designing-event-driven-systems/9781492038252/)
- [Kafka: The Definitive Guide (O'Reilly Media)](https://www.oreilly.com/library/view/kafka-the-definitive/9781492043072/)
- [Confluent Kafka Streams 튜토리얼](https://developer.confluent.io/tutorials/creating-first-apache-kafka-streams-application/)