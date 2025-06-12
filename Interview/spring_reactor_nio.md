# Spring Reactor와 NIO 서버 구현 시 줄개행 문제 해결

## 1. NIO와 줄개행 문제

### 1.1 NIO의 기본 개념
NIO(New Input/Output)는 Java에서 제공하는 논블로킹 I/O API로, 기존의 블로킹 I/O 방식보다 효율적인 네트워크 프로그래밍을 가능하게 합니다. NIO는 채널(Channel), 버퍼(Buffer), 셀렉터(Selector)를 핵심 컴포넌트로 사용합니다.

### 1.2 NIO 서버 구현 시 발생하는 줄개행 문제
NIO를 사용하여 서버를 구현할 때 자주 발생하는 문제 중 하나는 데이터 경계 처리, 특히 줄개행(Line Breaking) 문제입니다. 이 문제는 다음과 같은 상황에서 발생합니다:

1. **데이터 분할(Data Fragmentation)**: 네트워크를 통해 전송된 데이터가 여러 패킷으로 분할되어 도착할 수 있습니다. 이로 인해 하나의 논리적 메시지가 여러 번의 읽기 작업으로 나뉘어 처리될 수 있습니다.

2. **불완전한 메시지**: 버퍼에 읽어들인 데이터가 완전한 메시지가 아닐 수 있으며, 메시지의 끝을 식별하기 어려울 수 있습니다.

3. **메시지 경계 식별**: 텍스트 기반 프로토콜에서는 줄바꿈 문자(\n 또는 \r\n)로 메시지 경계를 구분하는데, 이러한 경계가 버퍼 경계와 일치하지 않을 수 있습니다.

## 2. Spring Reactor 소개

### 2.1 리액티브 프로그래밍과 Reactor
리액티브 프로그래밍은 비동기, 논블로킹 데이터 스트림을 처리하는 프로그래밍 패러다임입니다. Spring Reactor는 이러한 리액티브 프로그래밍을 Java에서 구현한 라이브러리로, Reactive Streams 사양을 준수합니다.

### 2.2 Reactor의 핵심 타입
- **Mono**: 0 또는 1개의 결과를 비동기적으로 반환하는 Publisher
- **Flux**: 0개 이상의 결과를 비동기적으로 반환하는 Publisher

## 3. Spring Reactor로 줄개행 문제 해결하기

### 3.1 Reactor Netty
Spring Reactor는 Netty를 기반으로 한 Reactor Netty를 제공합니다. Reactor Netty는 NIO 기반의 네트워크 애플리케이션을 쉽게 개발할 수 있도록 도와주는 라이브러리입니다.

### 3.2 줄개행 문제 해결 방법

#### 3.2.1 ByteBufFlux와 라인 디코더 사용
Reactor Netty는 `ByteBufFlux`와 함께 라인 디코더를 제공하여 줄개행 문제를 쉽게 해결할 수 있습니다:

```
import reactor.netty.tcp.TcpServer;

TcpServer.create()
    .handle((inbound, outbound) -> {
        return inbound.receive()
            .asString() // ByteBuf를 String으로 변환
            .map(String::trim) // 공백 제거
            .filter(s -> !s.isEmpty()) // 빈 라인 필터링
            .flatMap(message -> {
                System.out.println("Received: " + message);
                return outbound.sendString(Mono.just("Echo: " + message + "\n"));
            });
    })
    .bindNow()
    .onDispose()
    .block();
```

#### 3.2.2 LineBasedFrameDecoder 활용
Netty의 `LineBasedFrameDecoder`를 사용하여 줄 단위로 메시지를 처리할 수 있습니다:

```
import io.netty.handler.codec.LineBasedFrameDecoder;
import reactor.netty.tcp.TcpServer;

TcpServer.create()
    .doOnConnection(connection -> {
        connection.addHandlerFirst(new LineBasedFrameDecoder(8192));
    })
    .handle((inbound, outbound) -> {
        return inbound.receive()
            .asString()
            .flatMap(message -> {
                System.out.println("Received: " + message);
                return outbound.sendString(Mono.just("Echo: " + message + "\n"));
            });
    })
    .bindNow()
    .onDispose()
    .block();
```

#### 3.2.3 DelimiterBasedFrameDecoder 활용
특정 구분자를 기준으로 메시지를 분리해야 하는 경우 `DelimiterBasedFrameDecoder`를 사용할 수 있습니다:

```
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import reactor.netty.tcp.TcpServer;

ByteBuf delimiter = Unpooled.wrappedBuffer(new byte[] { '\n' });

TcpServer.create()
    .doOnConnection(connection -> {
        connection.addHandlerFirst(new DelimiterBasedFrameDecoder(8192, delimiter));
    })
    .handle((inbound, outbound) -> {
        return inbound.receive()
            .asString()
            .flatMap(message -> {
                System.out.println("Received: " + message);
                return outbound.sendString(Mono.just("Echo: " + message + "\n"));
            });
    })
    .bindNow()
    .onDispose()
    .block();
```

## 4. Spring WebFlux와 Reactor

### 4.1 WebFlux 소개
Spring WebFlux는 Spring Framework 5에서 도입된 리액티브 웹 프레임워크로, Spring Reactor를 기반으로 합니다. WebFlux는 비동기, 논블로킹 웹 애플리케이션을 개발할 수 있도록 지원합니다.

### 4.2 WebFlux에서의 데이터 스트리밍
WebFlux는 HTTP 스트리밍을 지원하여 Server-Sent Events(SSE)나 WebSocket과 같은 프로토콜을 통해 클라이언트에 데이터를 스트리밍할 수 있습니다. 이 과정에서도 Reactor의 기능을 활용하여 줄개행 문제를 해결합니다.

```
@RestController
public class StreamController {
    
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamData() {
        return Flux.interval(Duration.ofSeconds(1))
            .map(sequence -> "Event " + sequence);
    }
}
```

## 5. 성능 최적화 및 고려사항

### 5.1 백프레셔(Backpressure) 관리
Reactor는 백프레셔를 지원하여 데이터 생산자가 소비자의 처리 속도에 맞게 데이터 생성 속도를 조절할 수 있습니다. 이는 메모리 사용량을 제어하고 시스템 안정성을 유지하는 데 중요합니다.

### 5.2 버퍼 크기 조정
버퍼 크기는 성능과 메모리 사용량에 영향을 미칩니다. 적절한 버퍼 크기를 설정하여 최적의 성능을 얻을 수 있습니다.

```
TcpServer.create()
    .option(ChannelOption.SO_RCVBUF, 1024 * 1024) // 수신 버퍼 크기 설정
    .option(ChannelOption.SO_SNDBUF, 1024 * 1024) // 송신 버퍼 크기 설정
    .handle((inbound, outbound) -> {
        // 처리 로직
    })
    .bindNow();
```

## 6. 결론

Spring Reactor는 NIO 기반 서버 구현 시 발생하는 줄개행 문제를 효과적으로 해결할 수 있는 강력한 도구입니다. Reactor의 리액티브 스트림 처리 방식과 Netty의 디코더를 조합하여 사용하면, 데이터 경계 처리 문제를 우아하게 해결할 수 있습니다. 또한, 비동기 논블로킹 방식으로 동작하기 때문에 높은 처리량과 확장성을 제공합니다.

Spring Reactor를 활용하면 복잡한 네트워크 프로그래밍 문제를 선언적이고 함수형 스타일로 해결할 수 있어, 코드의 가독성과 유지보수성이 향상됩니다.