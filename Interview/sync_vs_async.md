# 동기 (sync) 와 비동기 (Async)

### 동기
작업이 순차적으로 진행, 이전 작업이 종료되어야 다음 작업이 진행됩니다. 
EX : Spring Boot 에서 주로 사용되는 `RestTemplate`

### 비동기
작업이 동시에 진행됩니다. 요청을 보낸 후 결과를 기다리지 않습니다.
EX: Spring WebFlux 에서 제공하는 `WebClient`

현재는 주로 `RestTemplate` 보다는 `WebClient`를 주로 사용하는 추세가 있습니다.
이유는 다음과 같습니다.

- 비동기/논블로킹 처리:
`WebClient`는 비동기 및 논블로킹 방식으로 동작하여, 많은 수의 동시 요청을 효율적으로 처리할 수 있습니다.
반면, `RestTemplate`은 동기 방식으로, 요청이 완료될 때까지 스레드가 블로킹됩니다.


- 리액티브 프로그래밍 지원:
`WebClient`는 `Reactor`와 같은 리액티브 스트림 라이브러리와 통합되어, 반응형 프로그래밍을 구현할 수 있습니다.
이는 고성능 애플리케이션이나 마이크로서비스 아키텍처에 유리합니다.


- 효율적인 자원 사용:
논블로킹 방식 덕분에 적은 수의 스레드로도 높은 동시성을 유지할 수 있어, 자원 사용 측면에서 효율적입니다.


- 미래 지향적:
Spring 팀에서는 `RestTemplate`의 업데이트를 중단하고, 새로운 기능은 `WebClient`에 집중하고 있습니다.
최신 Spring Boot와 Reactive Application 개발 환경에 맞추어 `WebClient`가 점차 표준이 되고 있습니다.

## 동기

``` java
public class SyncEx {
    public static void main(String[] args){
        RestTemplate restTemplate = new RestTemplate();
        String url = "https://base.examp.com/blog/1";
        
        System.out.println("Request Start!");
        
        //Sync
        String response = restTemplate.getForObject(url,  String.class);
        
        System.out.println("Response :" + response);
        System.out.println("Response Complete");
    }
}
```

> getForObject(..) 응답이 올 때까지 대기하며 요청이 끝나야 아래에 응답이 호출됩니다. 


## 비동기

```java
public class AsyncEx {
    public static void main(String[] args) {
        WebClient webClient = WebClient.create("https://base.examp.com");

        System.out.println("Request Start");

        Mono<String> response = webClient.get()
                .uri("/blog/1")
                .retrieve()
                .bodyToMono(String.class);  // Async

        response.subscribe(result -> System.out.println("Response Result: " + result));  // Async

        System.out.println("Response Complete");
    }
}
```

> 동기 방식과 달리 비동기 코드에서는 "Response Complete" 가 출력되고, 나중에 응답이 출력됩니다.

#  동기(Sync)와 비동기(Async) 비교

| **항목**         | **동기 (Sync)**                                  | **비동기 (Async)**                                |
|------------------|--------------------------------------------------|--------------------------------------------------|
| **처리 방식**    | 순차 처리: 요청 후 응답을 기다린 후 다음 작업 수행  | 비순차 처리: 요청 후 바로 다른 작업 진행           |
| **대표 라이브러리** | `RestTemplate`                                   | `WebClient`                                      |
| **성능**         | 단일 작업에 적합하나, 대규모 요청에는 비효율적        | 동시성 처리에 강하며, 대규모 트래픽 상황에 유리       |
| **응답 대기**    | 응답이 도착할 때까지 작업이 중단됨                  | 응답을 기다리는 동안 다른 작업 수행 가능           |
| **코드 구조**    | 단순하고 이해하기 쉬움                             | 콜백 및 리액티브 스트림 사용으로 상대적으로 복잡할 수 있음 |
| **적용 상황**    | 간단한 API 호출, 테스트 환경                       | 마이크로서비스, 고성능 서버, 대규모 트래픽 처리       |
| **에러 처리**    | 주로 `try-catch` 블록 사용                          | `onErrorResume` 등의 리액티브 방식 사용              |
