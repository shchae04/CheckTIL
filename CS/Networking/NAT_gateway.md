# NAT Gateway

AWS 프라이빗 서브넷의 인스턴스들이 안전하게 인터넷과 통신할 수 있도록 하는 네트워크 게이트웨이의 동작 원리와 실제 구현 사례를 다룬다.

## 목차
1. [개요](#1-개요)
2. [NAT 게이트웨이란?](#2-nat-게이트웨이란)
3. [동작 방식](#3-동작-방식)
4. [실제 사용 예제](#4-실제-사용-예제)
5. [결론](#5-결론)
6. [참고 자료](#6-참고-자료)

## 1. 개요

AWS(Amazon Web Services)에서 프라이빗 서브넷의 인스턴스들이 인터넷과 통신할 수 있게 해주는 핵심 네트워킹 구성 요소인 NAT(Network Address Translation) 게이트웨이

NAT 게이트웨이를 사용하면 외부 인터넷에서는 프라이빗 서브넷으로 직접 접근할 수 없도록 보안을 유지하면서, 내부 인스턴스에서는 소프트웨어 업데이트, 패치 적용, 외부 API 호출 등 아웃바운드 통신을 안전하게 수행할 수 있다.

## 2. NAT 게이트웨이란?

NAT 게이트웨이는 프라이빗 서브넷에 있는 인스턴스의 사설 IP를 자신의 공인 IP로 변환하여 인터넷과 같은 외부 네트워크와 통신할 수 있도록 돕는 관리형 서비스다.

주요 특징은 다음과 같다.

- **관리형 서비스**: 사용자가 직접 인프라를 관리할 필요 없이 AWS에서 고가용성과 대역폭을 보장한다.

- **단방향 통신**: 프라이빗 서브넷 -> 인터넷 방향(아웃바운드)의 통신만 허용한다. 외부에서 프라이빗 서브넷의 인스턴스로 직접 연결(인바운드)하는 것은 불가능하여 보안성이 높다.

- **IP 변환**: 여러 인스턴스의 사설 IP를 NAT 게이트웨이의 단일 공인 IP로 변환하여 외부로 요청을 보낸다.

## 3. 동작 방식

NAT 게이트웨이의 동작 과정은 간단 명료하다.

1. **아웃바운드 요청 발생**: 프라이빗 서브넷에 위치한 EC2 인스턴스(예: 사설 IP 10.0.1.10)가 외부 인터넷(예: google.com)으로 트래픽을 보낸다.

2. **라우팅 테이블 조회**: 해당 서브넷의 라우팅 테이블에는 인터넷으로 향하는 모든 트래픽(0.0.0.0/0)을 NAT 게이트웨이로 보내도록 설정되어 있다.

3. **IP 변환 (SNAT)**: 트래픽이 NAT 게이트웨이에 도달하면, 게이트웨이는 출발지 IP를 자신의 사설 IP에서 탄력적 IP(EIP), 즉 공인 IP로 변환한다.
   - 출발지 IP: 10.0.1.10 -> 출발지 IP: [NAT 게이트웨이의 공인 IP]

4. **인터넷 게이트웨이 통과**: 주소가 변환된 트래픽은 VPC에 연결된 인터넷 게이트웨이(IGW)를 통해 외부 인터넷으로 전달된다.

5. **응답 트래픽 수신**: 외부 서버는 요청에 대한 응답을 NAT 게이트웨이의 공인 IP로 보낸다.

6. **응답 트래픽 변환 및 전달**: NAT 게이트웨이는 응답 트래픽을 수신한 후, 이 트래픽이 어떤 내부 인스턴스의 요청이었는지 자신의 상태 테이블을 참조하여 원래의 사설 IP(10.0.1.10)로 목적지 주소를 변환하여 전달한다.

## 4. 실제 사용 예제

프라이빗 서브넷의 애플리케이션 서버가 외부 API를 호출하여 데이터를 가져오는 상황을 가정해보자. 이 서버는 외부에서 직접 접근할 수 없어야 하지만, 외부 API 호출은 가능해야 한다.

이러한 시나리오에서 Java 코드는 단순히 외부로 HTTP 요청을 보내는 역할을 수행하며, 실제 네트워크 통신은 인프라 레벨(VPC, 서브넷, 라우팅 테이블, NAT 게이트웨이)에서 처리된다.

아래 코드는 프라이빗 인스턴스 내에서 실행될 수 있는 간단한 Java 예제다.

```java
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 이 클래스는 프라이빗 서브넷의 EC2 인스턴스에서 실행되는 애플리케이션을 시뮬레이션합니다.
 * 이 코드는 외부 API(예: JSONPlaceholder)를 호출하여 데이터를 가져옵니다.
 * 이 통신은 NAT 게이트웨이를 통해 안전하게 이루어집니다.
 */
public class ExternalApiCaller {

    public static void main(String[] args) {
        // 외부 API 엔드포인트 URL
        String apiUrl = "https://jsonplaceholder.typicode.com/posts/1";

        System.out.println("프라이빗 인스턴스에서 외부 API 호출을 시작합니다...");
        System.out.println("대상 URL: " + apiUrl);

        try {
            URL url = new URL(apiUrl);
            // URL 객체를 사용하여 HttpURLConnection 객체 생성
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // HTTP GET 요청 설정
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type", "application/json");

            // 타임아웃 설정 (연결 및 읽기)
            conn.setConnectTimeout(5000); // 5초
            conn.setReadTimeout(5000);    // 5초

            // 응답 코드 확인
            int responseCode = conn.getResponseCode();
            System.out.println("HTTP 응답 코드: " + responseCode);

            // 성공적인 응답(200 OK)인 경우에만 데이터 읽기
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                // 리소스 정리
                in.close();

                System.out.println("\n--- API 응답 데이터 ---");
                System.out.println(content.toString());
                System.out.println("----------------------");

            } else {
                System.out.println("API 호출에 실패했습니다.");
            }
            // 연결 종료
            conn.disconnect();

        } catch (Exception e) {
            System.err.println("API 호출 중 오류가 발생했습니다.");
            e.printStackTrace();
        }
    }
}
```

### 시나리오 설명:

1. 위 `ExternalApiCaller.java` 코드가 프라이빗 서브넷의 EC2 인스턴스에서 실행된다.

2. 애플리케이션이 `jsonplaceholder.typicode.com`으로 HTTP 요청을 보낸다.

3. 이 아웃바운드 트래픽은 라우팅 규칙에 따라 NAT 게이트웨이로 전달된다.

4. NAT 게이트웨이는 이 요청의 출발지 IP를 자신의 공인 IP로 변환하여 인터넷 게이트웨이를 통해 외부로 보낸다.

5. API 서버는 NAT 게이트웨이의 공인 IP로 응답을 보내고, NAT 게이트웨이는 이 응답을 다시 원래 요청을 보냈던 EC2 인스턴스로 전달한다.

6. 결과적으로 EC2 인스턴스는 외부에 직접 노출되지 않으면서도 안전하게 외부 데이터를 가져올 수 있다.

## 5. 결론

NAT 게이트웨이는 AWS의 핵심 네트워킹 구성 요소로, 프라이빗 서브넷의 보안을 유지하면서도 인터넷 통신을 가능하게 하는 중요한 역할을 한다. 관리형 서비스로 제공되어 고가용성과 확장성을 보장하며, 개발자는 인프라 관리 부담 없이 안전한 아웃바운드 통신을 구현할 수 있다.

특히 마이크로서비스 아키텍처나 API 기반 애플리케이션에서 외부 서비스와의 통신이 필요한 경우, NAT 게이트웨이를 통해 보안성과 접근성의 균형을 맞출 수 있다. 이를 통해 시스템의 전반적인 보안 수준을 높이면서도 필요한 외부 연동 기능을 안정적으로 제공할 수 있다.

## 6. 참고 자료

- [AWS NAT 게이트웨이 공식 문서](https://docs.aws.amazon.com/vpc/latest/userguide/vpc-nat-gateway.html)
- [NAT 게이트웨이와 NAT 인스턴스 비교](https://docs.aws.amazon.com/vpc/latest/userguide/vpc-nat-comparison.html)
- AWS VPC 공식 가이드 (Amazon Web Services)