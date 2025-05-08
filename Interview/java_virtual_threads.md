# Java 가상 스레드 (Virtual Threads)

## 목차
1. [개요](#1-개요)
2. [가상 스레드 vs 플랫폼 스레드](#2-가상-스레드-vs-플랫폼-스레드)
3. [가상 스레드 사용 방법](#3-가상-스레드-사용-방법)
4. [사용 예시](#4-사용-예시)
5. [장점](#5-장점)
6. [단점 및 제한사항](#6-단점-및-제한사항)
7. [모범 사례](#7-모범-사례)
8. [결론](#8-결론)

## 1. 개요

가상 스레드(Virtual Thread)는 Java 21에서 정식으로 도입된 경량 스레드 구현체입니다. JEP 444(Java Enhancement Proposal)를 통해 제안되었으며, Project Loom의 일환으로 개발되었습니다. 가상 스레드는 기존의 플랫폼 스레드(OS 스레드에 1:1로 매핑되는)와 달리, JVM에 의해 관리되는 경량 스레드로, 수천 또는 수백만 개의 동시 작업을 효율적으로 처리할 수 있게 해줍니다.

가상 스레드는 특히 I/O 작업이 많은 서버 애플리케이션에서 성능 향상을 가져올 수 있으며, 동시성 프로그래밍을 단순화합니다.

## 2. 가상 스레드 vs 플랫폼 스레드

### 플랫폼 스레드 (기존 스레드)
- OS 스레드에 1:1로 매핑됨
- 생성 및 관리 비용이 높음
- 스택 메모리를 많이 사용 (기본 1MB)
- 컨텍스트 스위칭 비용이 큼
- 스레드 풀을 통한 제한된 수의 스레드 관리 필요

### 가상 스레드
- OS 스레드에 M:N으로 매핑됨 (여러 가상 스레드가 적은 수의 OS 스레드를 공유)
- 생성 및 관리 비용이 낮음
- 메모리 사용량이 적음
- 컨텍스트 스위칭 비용이 적음
- 스레드 풀 없이 작업당 스레드 생성 가능

## 3. 가상 스레드 사용 방법

Java 21에서는 다양한 방법으로 가상 스레드를 생성하고 사용할 수 있습니다.

### 1. Thread.Builder API 사용

```java
Thread thread = Thread.ofVirtual().name("my-virtual-thread").start(() -> {
    System.out.println("Running in a virtual thread");
});
thread.join();
```

### 2. Thread.startVirtualThread 메서드 사용

```java
Thread.startVirtualThread(() -> {
    System.out.println("Simple virtual thread task");
});
```

### 3. ExecutorService 사용

```java
try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        System.out.println("Task running in virtual thread");
        return "Task completed";
    });
}
```

### 4. 스트럭처드 동시성(Structured Concurrency) 사용 (JEP 453)

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Future<String> user = scope.fork(() -> fetchUser(userId));
    Future<List<Order>> orders = scope.fork(() -> fetchOrders(userId));
    
    scope.join();           // 모든 작업 완료 대기
    scope.throwIfFailed();  // 실패한 작업이 있으면 예외 발생
    
    // 결과 처리
    processUserData(user.resultNow(), orders.resultNow());
}
```

## 4. 사용 예시

### 예시 1: HTTP 요청 병렬 처리

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ParallelHttpRequests {
    public static void main(String[] args) throws Exception {
        List<String> urls = List.of(
            "https://www.google.com",
            "https://www.github.com",
            "https://www.stackoverflow.com"
        );
        
        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();
            
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<HttpResponse<String>> responses = urls.stream()
                .map(url -> executor.submit(() -> {
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .GET()
                        .build();
                    return client.send(request, HttpResponse.BodyHandlers.ofString());
                }))
                .map(future -> {
                    try {
                        return future.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .collect(Collectors.toList());
                
            responses.forEach(response -> 
                System.out.println(response.uri() + " - Status: " + response.statusCode()));
        }
    }
}
```

### 예시 2: 데이터베이스 작업 병렬 처리

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ParallelDatabaseQueries {
    public static void main(String[] args) throws Exception {
        List<Integer> userIds = List.of(1, 2, 3, 4, 5);
        String jdbcUrl = "jdbc:mysql://localhost:3306/mydb";
        String username = "user";
        String password = "password";
        
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<User>> futures = new ArrayList<>();
            
            for (Integer userId : userIds) {
                futures.add(executor.submit(() -> {
                    try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password)) {
                        PreparedStatement stmt = conn.prepareStatement(
                            "SELECT id, name, email FROM users WHERE id = ?");
                        stmt.setInt(1, userId);
                        ResultSet rs = stmt.executeQuery();
                        
                        if (rs.next()) {
                            return new User(
                                rs.getInt("id"),
                                rs.getString("name"),
                                rs.getString("email")
                            );
                        }
                        return null;
                    }
                }));
            }
            
            for (Future<User> future : futures) {
                User user = future.get();
                if (user != null) {
                    System.out.println("Found user: " + user);
                }
            }
        }
    }
    
    static class User {
        private final int id;
        private final String name;
        private final String email;
        
        User(int id, String name, String email) {
            this.id = id;
            this.name = name;
            this.email = email;
        }
        
        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "', email='" + email + "'}";
        }
    }
}
```

### 예시 3: Spring Boot에서의 활용 (Java 21 + Spring Boot 3.2+)

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;

@SpringBootApplication
public class VirtualThreadsApplication {

    public static void main(String[] args) {
        SpringApplication.run(VirtualThreadsApplication.class, args);
    }

    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerVirtualThreadExecutorCustomizer() {
        return protocolHandler -> {
            protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        };
    }
}
```

## 5. 장점

1. **높은 처리량**: 수천 또는 수백만 개의 동시 작업을 효율적으로 처리할 수 있습니다.
2. **리소스 효율성**: 가상 스레드는 플랫폼 스레드보다 훨씬 적은 메모리를 사용합니다.
3. **간단한 프로그래밍 모델**: 스레드 풀 관리 없이 작업당 스레드 생성 패턴을 사용할 수 있습니다.
4. **기존 코드와의 호환성**: 기존 Thread API와 호환되므로 코드 변경이 최소화됩니다.
5. **블로킹 코드의 효율성 향상**: I/O 작업이나 네트워크 호출 등 블로킹 작업에서 특히 효과적입니다.
6. **디버깅 용이성**: 각 작업이 별도의 스레드에서 실행되므로 스택 트레이스가 명확합니다.

## 6. 단점 및 제한사항

1. **CPU 집약적 작업에는 이점이 적음**: CPU 바운드 작업보다는 I/O 바운드 작업에 더 적합합니다.
2. **스레드 로컬 변수 사용 시 주의 필요**: 많은 가상 스레드를 생성할 때 ThreadLocal 사용은 메모리 누수를 일으킬 수 있습니다.
3. **네이티브 메서드 호출 시 블로킹**: JNI(Java Native Interface) 호출 시 가상 스레드가 마운트 해제되지 않고 OS 스레드를 블로킹합니다.
4. **동기화 블록에서의 제약**: synchronized 블록 내에서는 가상 스레드가 마운트 해제되지 않습니다.
5. **기존 스레드 풀 기반 라이브러리와의 통합 문제**: 일부 라이브러리는 가상 스레드의 이점을 활용하지 못할 수 있습니다.
6. **모니터링 및 프로파일링 도구 지원 제한**: 일부 도구는 아직 가상 스레드를 완전히 지원하지 않을 수 있습니다.

## 7. 모범 사례

1. **I/O 바운드 작업에 집중 사용**: 네트워크 호출, 데이터베이스 쿼리 등 I/O 작업에 가상 스레드를 활용하세요.
2. **ThreadLocal 사용 최소화**: 가상 스레드에서 ThreadLocal 사용 시 메모리 사용량에 주의하세요.
3. **synchronized 대신 ReentrantLock 고려**: 가능하면 synchronized 블록 대신 java.util.concurrent.locks 패키지의 락을 사용하세요.
4. **작업당 스레드 패턴 활용**: 스레드 풀 대신 작업마다 새 가상 스레드를 생성하는 패턴을 사용하세요.
5. **스트럭처드 동시성 활용**: 관련 작업을 그룹화하고 오류 처리를 단순화하기 위해 StructuredTaskScope를 사용하세요.
6. **기존 코드 점진적 마이그레이션**: 모든 코드를 한 번에 변경하기보다 점진적으로 가상 스레드로 마이그레이션하세요.

## 8. 결론

Java 가상 스레드는 동시성 프로그래밍의 패러다임을 변화시키는 중요한 기능입니다. 특히 I/O 바운드 작업이 많은 서버 애플리케이션에서 성능과 확장성을 크게 향상시킬 수 있습니다. 가상 스레드를 통해 개발자는 복잡한 비동기 프로그래밍 모델 없이도 높은 동시성을 달성할 수 있게 되었습니다.

가상 스레드는 Java의 동시성 모델을 현대화하고, 리액티브 프로그래밍의 복잡성 없이도 높은 처리량을 제공합니다. 다만, 모든 상황에 적합한 만능 솔루션은 아니므로, 애플리케이션의 특성과 요구사항을 고려하여 적절히 활용해야 합니다.