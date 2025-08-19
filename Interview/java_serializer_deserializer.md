# JAVA에서 Serializer와 Deserializer가 뭔가요? 사용하는 이유는?

## 한 줄 정의
- Serializer: 메모리의 객체(Object)를 전송/저장 가능한 형태(주로 바이트 배열 또는 텍스트 JSON)로 "직렬화"(serialize)하는 컴포넌트
- Deserializer: 직렬화된 데이터(바이트/텍스트)를 다시 객체로 "역직렬화"(deserialize)하는 컴포넌트

## 왜 필요한가요?
- 네트워크 전송: HTTP/REST, gRPC, 메시지 큐(Kafka, RabbitMQ) 등을 통해 다른 프로세스/서비스에 객체를 보내기 위해
- 저장/캐시: 파일, 데이터베이스(BLOB), Redis/메모리 캐시 등에 객체 상태를 보관하기 위해
- 상호운용성: 서로 다른 언어/플랫폼 간에 데이터를 주고받기 위해(표준 포맷 사용)
- 내구성/비동기화: 시스템 경계(프로세스/머신)를 넘어 데이터를 안전하게 전달/보관하기 위해

## 어디에서 쓰이나요?
- HTTP JSON API: Spring + Jackson(ObjectMapper)로 객체↔JSON 변환
- 메시징: Kafka의 Serializer/Deserializer(SerDe)를 통해 레코드 값/키 변환
- 캐시/스토리지: Redis, 파일, BLOB 등에 객체 저장 시
- RPC/스트리밍: gRPC(Protobuf), Avro, Thrift 등 바이너리 포맷

## Java의 직렬화 옵션 비교
### 1) Java 내장 직렬화(java.io.Serializable)
- 방식: 마커 인터페이스(Serializable) + ObjectOutputStream/ObjectInputStream
- 장점: 손쉬운 사용(POJO에 인터페이스만)
- 단점(중요):
  - 취약성: 신뢰할 수 없는 입력 역직렬화는 RCE 등 보안 위협 유발 가능 → 외부 입력에는 지양
  - 버전 호환성: 필드 변경 시 깨지기 쉬움, serialVersionUID 관리 필요
  - 성능/크기: 텍스트/스키마 기반 대비 비효율적일 수 있음
- 권장: 외부 공개 API/장기 저장/메시징에는 가급적 사용하지 말고, JSON/Protobuf/Avro 등 사용

### 2) 텍스트(JSON) 기반: Jackson
- 장점: 사람이 읽기 쉬움, 하위 호환에 유리(모르는 필드 무시 가능), 광범위한 생태계
- 단점: 바이너리 포맷 대비 크고 느릴 수 있음(대부분의 웹 서비스에 충분히 빠름)

### 3) 스키마 기반 바이너리: Protobuf/Avro/Thrift
- 장점: 작고 빠름, 명시적 스키마로 강한 호환성 관리(스키마 진화)
- 단점: 학습/빌드 파이프라인 추가, 가시성 낮음(바이너리)

## Jackson으로 직렬화/역직렬화 예시
### 기본 사용(ObjectMapper)
```java
ObjectMapper mapper = new ObjectMapper();
String json = mapper.writeValueAsString(obj); // serialize
MyType obj2 = mapper.readValue(json, MyType.class); // deserialize
```

### 커스텀 Serializer/Deserializer
도메인 타입 표현을 제어해야 할 때 직접 구현합니다.

```java
// 예: Money를 "1000 KRW" 문자열로 직렬화/역직렬화
public class Money {
    private BigDecimal amount;
    private String currency; // e.g., "KRW"
    // getters/setters/constructors omitted
}

public class MoneySerializer extends JsonSerializer<Money> {
    @Override
    public void serialize(Money value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) { gen.writeNull(); return; }
        gen.writeString(value.getAmount().toPlainString() + " " + value.getCurrency());
    }
}

public class MoneyDeserializer extends JsonDeserializer<Money> {
    @Override
    public Money deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getValueAsString(); // e.g., "1000 KRW"
        if (text == null || text.isBlank()) return null;
        String[] parts = text.trim().split(" ");
        return new Money(new BigDecimal(parts[0]), parts[1]);
    }
}

public class Order {
    @JsonSerialize(using = MoneySerializer.class)
    @JsonDeserialize(using = MoneyDeserializer.class)
    private Money price;
    // ...
}
```

참고: 단순 날짜/시간 등은 @JsonFormat 또는 모듈(JavaTimeModule) 등록으로 해결 가능.

## Kafka에서의 Serializer/Deserializer(SerDe)
Kafka는 키/값을 바이트 배열로 보내므로, 프로듀서/컨슈머에 타입별 직렬화기가 필요합니다.

```java
// build.gradle: org.apache.kafka:kafka-clients, com.fasterxml.jackson.core:jackson-databind

public class User {
    public String id;
    public String name;
}

public class UserSerializer implements org.apache.kafka.common.serialization.Serializer<User> {
    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public byte[] serialize(String topic, User data) {
        if (data == null) return null;
        try { return mapper.writeValueAsBytes(data); } catch (Exception e) { throw new SerializationException(e); }
    }
}

public class UserDeserializer implements org.apache.kafka.common.serialization.Deserializer<User> {
    private final ObjectMapper mapper = new ObjectMapper();
    @Override
    public User deserialize(String topic, byte[] data) {
        if (data == null) return null;
        try { return mapper.readValue(data, User.class); } catch (Exception e) { throw new SerializationException(e); }
    }
}

// 프로듀서/컨슈머 설정 예
Properties props = new Properties();
props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, UserSerializer.class.getName());

props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UserDeserializer.class.getName());
```

Spring Kafka/Confluent를 쓰면 JsonSerializer/JsonDeserializer, AvroSerde 등을 활용해 더 간단히 구성할 수 있습니다.

## 버전 관리와 호환성
- JSON(Jackson): 새로운 필드 추가는 대체로 안전(구 소비자는 무시), 필드 제거/이름 변경은 주의. @JsonIgnoreProperties(ignoreUnknown = true) 고려
- Protobuf/Avro: 스키마 진화 규칙(필드 번호 고정, 기본값 제공, 제거 대신 deprecated 처리) 준수
- 계약 테스트(Consumer-Driven Contract)로 실제 호환성 검증

## 보안 고려사항(매우 중요)
- Java 기본 역직렬화(Serializable)는 신뢰할 수 없는 입력에 사용하지 않기
- Jackson: 기본 타입 공개 활성화(Default Typing)는 지양, 필요 시 허용 리스트 기반 PolymorphicTypeValidator 사용
- 입력 크기 제한, 시간 제한, 깊이 제한으로 DoS 방지(예: JSON 폭탄)
- 클래스/필드 화이트리스트, 검증 로직 추가

## 성능 팁
- 포맷 선택: 고성능/대량 전송은 Protobuf/Avro, 일반 웹 API는 JSON으로 충분한 경우가 많음
- 스트리밍 파싱: 큰 페이로드는 ObjectReader/ObjectWriter, Streaming API(JsonParser/Generator) 사용
- 압축: HTTP 전송 시 GZIP/BR 활용(Content-Encoding)
- 숫자/날짜 포맷 명확화로 파싱 비용/오류 감소

## 실무 체크리스트
- 어떤 포맷을 선택할 것인가(JSON/Avro/Protobuf)와 이유는?
- Content-Type/Accept를 정확히 지정하고 문서화했는가?
- 버전 호환 전략(추가/제거/이름 변경 규칙, 스키마 진화)을 정의했는가?
- 신뢰하지 않는 입력에 대한 역직렬화 보안 대책이 있는가?
- 크기/시간/깊이 제한 등으로 DoS 방어를 적용했는가?
- 프로듀서/컨슈머, 클라이언트/서버 간 계약 테스트가 존재하는가?

## 요약
- Serializer/Deserializer는 객체를 경계 밖(네트워크/디스크/메시지)에 안전하고 표준적으로 옮기기 위한 핵심 구성요소입니다.
- Java 기본 직렬화는 편하지만 보안/호환성/성능 이슈로 인해 외부 시스템과의 인터페이스에는 지양됩니다.
- 대부분의 웹 서비스는 JSON(Jackson)으로 충분하며, 고성능/엄격한 스키마가 필요하면 Protobuf/Avro 같은 바이너리 포맷을 고려합니다.
- 항상 보안/버전 관리/성능을 함께 고려하세요.
