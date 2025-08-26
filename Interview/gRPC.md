# gRPC란 무엇인가요?

## 0. 한눈에 보기(초간단)
- 한 줄 정의: gRPC는 Google이 만든 고성능 오픈소스 RPC 프레임워크로, HTTP/2 위에서 Protocol Buffers(IDL/바이너리)로 서비스 간 통신을 표준화합니다.
- 핵심 특징: HTTP/2(멀티플렉싱·스트리밍·헤더 압축), 바이너리 직렬화(Protobuf), 자동 코드 생성(Stub/Skel), 양방향 스트리밍, 데드라인·리트라이·인터셉터 등 내장.
- 장점: 낮은 지연/작은 페이로드, 타입 안정성(스키마), 양방향 스트리밍, 다양한 언어 지원.
- 단점/주의: 브라우저 직접 호출 제한(gRPC-Web 필요), 디버깅 난이도↑, 바이너리 페이로드 관찰성 대비 필요, 프록시/로드밸런서 설정 요구.
- 적합한 곳: 내부 마이크로서비스 간 통신, 실시간 스트리밍, 저지연·고성능 요구, 명확한 계약이 필요한 B2B API.

---

## 1. gRPC 정의와 배경
- RPC(Remote Procedure Call) 스타일의 통신을 현대 웹 표준(HTTP/2)과 스키마 언어(Protobuf)로 재구성한 프레임워크.
- 서버/클라이언트가 .proto로 서비스/메시지 계약을 정의하고, 언어별 플러그인으로 Stub 코드를 생성해 타입 안전하게 호출.

## 2. 핵심 구성요소
- 전송: HTTP/2(h2) 고정. 멀티플렉싱(하나의 커넥션에 다중 스트림), 서버 푸시X, 헤더 압축(HPACK), 흐름 제어.
- 페이로드: Protocol Buffers(기본), 필요 시 JSON 등 대체 가능하지만 표준은 Protobuf.
- 계약: .proto(IDL)로 서비스/메시지/필드 번호를 정의. 스키마 진화 규칙으로 하위 호환 관리.
- 코드 생성: protoc + 언어별 플러그인(Java/Kotlin/Go/TS/Python/…)이 클라/서버 스텁 생성.

## 3. 통신 모델(4가지 RPC 타입)
1) Unary: 요청 1개 → 응답 1개(일반적인 함수 호출과 유사)
2) Server streaming: 요청 1개 → 응답 스트림(여러 개)
3) Client streaming: 요청 스트림 → 응답 1개
4) Bidirectional streaming: 요청/응답 모두 스트림(실시간 채팅, 텔레메트리 등)

## 4. .proto 예시(간단)
```proto
syntax = "proto3";
package example.v1;

service Greeter {
  rpc SayHello(HelloRequest) returns (HelloReply);              // Unary
  rpc Chat(stream ChatMessage) returns (stream ChatMessage);    // Bidi streaming
}

message HelloRequest { string name = 1; }
message HelloReply   { string message = 1; }
message ChatMessage  { string text = 1; int64 sent_at = 2; }
```
- 생성: `protoc --java_out=... --grpc-java_out=... greeter.proto` (언어별 플러그인 상이)
- 서버: 서비스 구현체(GreeterImplBase 등)에서 메서드 오버라이드
- 클라: 채널(Channel) 생성 → Stub로 메서드 호출, 데드라인/메타데이터 지정 가능

## 5. REST vs gRPC
- 포맷: REST는 보통 JSON/HTTP/1.1, gRPC는 Protobuf/HTTP/2.
- 성능: gRPC가 일반적으로 더 빠르고 작음(바이너리+멀티플렉싱). 대역폭/지연 민감 시 유리.
- 계약: REST는 느슨한 스키마(문서 기반), gRPC는 엄격한 IDL과 코드 생성.
- 스트리밍: REST에선 SSE/WebSocket 등 별도 기술 필요, gRPC는 4가지 RPC 모델로 일관 제공.
- 브라우저: 공개 API/브라우저 직접 호출은 REST가 쉬움. gRPC는 gRPC-Web 프록시 필요.
- 캐싱/디버깅: REST/JSON은 가시성/프록시·캐시와 친화적, gRPC는 추가 도구(리플렉션, xDS/Envoy, 관측 파이프라인) 필요.
- 권장 판단:
  - 내부 MSA, 엄격한 스키마, 스트리밍/저지연 필요 → gRPC
  - 외부 공개 API, SEO/브라우저/간편 디버깅/캐시 활용 → REST

## 6. 운영 포인트(실무)
- 보안: mTLS(양방향 TLS)로 서비스 간 상호 인증. ALPN으로 h2 협상. 키/인증서 회전 자동화.
- 로드밸런싱: HTTP/2 커넥션 장기 유지 → L7 프록시(Envoy) 또는 클라이언트 사이드 LB 권장. xDS 기반 서비스 디스커버리.
- 데드라인/타임아웃: 각 RPC 호출에 데드라인 전달해서 체인 전체에 전파. 과도한 대기 방지.
- 리트라이/백오프: idempotent/안전한 메서드에 한해 정책화. 서버·프록시(Envoy)·클라(Stub) 레벨에서 구성.
- 관측성: 인터셉터로 메트릭/트레이싱/로그 삽입(OpenTelemetry). 바이너리 페이로드 → 샘플링/메타데이터 로깅 설계.
- 호환성(스키마 진화): 필드 번호 재사용 금지, 제거 대신 deprecated, 새로운 필드는 옵셔널/기본값, 서버/클라 점진 롤아웃.
- 게이트웨이: HTTP/JSON이 필요하면 gRPC-Gateway(REST↔gRPC 변환) 도입.

## 7. 언제 gRPC를 쓰면 좋을까?
- 대규모 내부 마이크로서비스 간 요청이 많고 지연 민감한 환경.
- 실시간 데이터 스트리밍(채팅, 텔레메트리, 위치 업데이트, 주문 체결 피드 등).
- 다중 언어(Polyglot) 팀에서 타입 안전한 계약과 코드 생성이 중요한 경우.
- 모바일/엣지 환경에서 대역폭 절약이 중요한 경우.

## 8. 한 줄 답변(면접용)
- gRPC는 HTTP/2와 Protocol Buffers 기반의 고성능 RPC 프레임워크로, 스키마 기반 계약과 코드 생성, 4가지 스트리밍 모델을 제공하여 내부 마이크로서비스 간 저지연·고효율 통신에 적합합니다. 브라우저 공개 API엔 보통 REST, 내부 실시간·고성능 요구엔 gRPC를 권장합니다.

## 9. 참고
- gRPC 공식: https://grpc.io/
- Protocol Buffers: https://protobuf.dev/
- HTTP/2 개요: https://http2.github.io/  / RFC 7540
- Envoy Proxy / gRPC-Web / xDS: https://www.envoyproxy.io/
- 예시 비교: REST vs gRPC 개요 문서들(구글 개발자 문서, CNCF 자료 등)
