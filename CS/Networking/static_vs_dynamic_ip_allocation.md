# 정적 IP 주소 할당 방식과 동적 IP 주소 할당 방식

네트워크에서 디바이스에 IP 주소를 할당하는 두 가지 주요 방식인 정적 IP 할당과 동적 IP 할당의 차이점과 각각의 특성, 활용 사례를 비교 분석한다.

## 목차
1. [개요](#1-개요)
2. [정적 IP 주소 할당](#2-정적-ip-주소-할당)
3. [동적 IP 주소 할당](#3-동적-ip-주소-할당)
4. [비교 분석](#4-비교-분석)
5. [실제 사용 사례](#5-실제-사용-사례)
6. [결론](#6-결론)
7. [참고 자료](#7-참고-자료)

## 1. 개요

IP 주소는 네트워크에서 각 디바이스를 고유하게 식별하는 논리적 주소다. 이러한 IP 주소를 디바이스에 할당하는 방식에는 크게 두 가지가 있다:

- **정적 IP 할당 (Static IP Assignment)**: 관리자가 수동으로 IP 주소를 고정 할당하는 방식
- **동적 IP 할당 (Dynamic IP Assignment)**: DHCP 서버가 자동으로 IP 주소를 할당하는 방식

각 방식은 고유한 장단점을 가지고 있으며, 네트워크 환경과 요구사항에 따라 적절한 방식을 선택해야 한다.

## 2. 정적 IP 주소 할당

### 2.1 개념

정적 IP 할당은 네트워크 관리자가 각 디바이스에 고정된 IP 주소를 수동으로 설정하는 방식이다. 한 번 설정된 IP 주소는 변경하지 않는 한 계속해서 해당 디바이스에 할당된다.

### 2.2 특징

**장점:**
- **안정성**: IP 주소가 변경되지 않아 서비스 연결이 안정적이다
- **예측 가능성**: 항상 동일한 IP 주소를 사용하므로 네트워크 관리가 예측 가능하다
- **보안성**: 특정 IP에 대한 접근 제어가 용이하다
- **DNS 설정 용이**: 고정 IP로 인해 DNS 레코드 관리가 간단하다

**단점:**
- **관리 복잡성**: 각 디바이스마다 수동으로 IP를 설정해야 한다
- **IP 충돌 위험**: 중복 할당으로 인한 IP 충돌 가능성이 있다
- **확장성 제한**: 네트워크 규모 증가 시 관리 부담이 크다
- **유연성 부족**: 디바이스 이동 시 재설정이 필요하다

### 2.3 설정 예제

#### Windows에서 정적 IP 설정:
```cmd
# 네트워크 어댑터 확인
ipconfig /all

# 정적 IP 설정 (관리자 권한 필요)
netsh interface ip set address "이더넷" static 192.168.1.100 255.255.255.0 192.168.1.1
netsh interface ip set dns "이더넷" static 8.8.8.8
```

#### Linux에서 정적 IP 설정:
```bash
# Ubuntu/Debian - /etc/netplan/00-network-config.yaml
network:
  version: 2
  ethernets:
    eth0:
      addresses:
        - 192.168.1.100/24
      gateway4: 192.168.1.1
      nameservers:
        addresses: [8.8.8.8, 8.8.4.4]

# 설정 적용
sudo netplan apply
```

## 3. 동적 IP 주소 할당

### 3.1 개념

동적 IP 할당은 DHCP(Dynamic Host Configuration Protocol) 서버가 클라이언트의 요청에 따라 사용 가능한 IP 주소를 자동으로 할당하는 방식이다. 할당된 IP는 일정 시간(lease time) 후 회수되어 다른 디바이스에 재할당될 수 있다.

### 3.2 DHCP 동작 과정

DHCP는 4단계 과정을 통해 IP 주소를 할당한다:

1. **DHCP Discover**: 클라이언트가 브로드캐스트로 DHCP 서버를 찾는다
2. **DHCP Offer**: DHCP 서버가 사용 가능한 IP 주소를 제안한다
3. **DHCP Request**: 클라이언트가 특정 IP 주소를 요청한다
4. **DHCP ACK**: 서버가 IP 할당을 확인하고 추가 설정 정보를 제공한다

```
클라이언트                    DHCP 서버
    |                            |
    |------ DHCP Discover ------>| (브로드캐스트)
    |                            |
    |<------ DHCP Offer ---------| (IP 제안)
    |                            |
    |------ DHCP Request ------->| (IP 요청)
    |                            |
    |<------ DHCP ACK -----------| (할당 완료)
```

### 3.3 특징

**장점:**
- **자동화**: IP 주소 할당이 자동으로 이루어진다
- **효율적 주소 사용**: IP 주소 풀을 효율적으로 관리한다
- **중앙 집중 관리**: DHCP 서버에서 일괄 관리 가능하다
- **유연성**: 디바이스 이동 시에도 자동으로 새 IP를 할당받는다
- **확장성**: 새로운 디바이스 추가가 용이하다

**단점:**
- **의존성**: DHCP 서버 장애 시 네트워크 접속 불가능하다
- **주소 변경**: IP 주소가 주기적으로 변경될 수 있다
- **보안 취약점**: 무단 DHCP 서버(Rogue DHCP) 공격 위험이 있다
- **추적 어려움**: 동적 IP로 인해 로그 추적이 복잡하다

### 3.4 DHCP 서버 설정 예제

#### Linux에서 ISC DHCP 서버 설정:
```bash
# /etc/dhcp/dhcpd.conf
subnet 192.168.1.0 netmask 255.255.255.0 {
    range 192.168.1.100 192.168.1.200;          # IP 할당 범위
    option routers 192.168.1.1;                 # 게이트웨이
    option domain-name-servers 8.8.8.8, 8.8.4.4; # DNS 서버
    default-lease-time 86400;                   # 기본 임대 시간 (24시간)
    max-lease-time 172800;                      # 최대 임대 시간 (48시간)
}

# 특정 MAC 주소에 고정 IP 할당 (DHCP Reservation)
host server1 {
    hardware ethernet 00:1B:44:11:3A:B7;
    fixed-address 192.168.1.50;
}
```

## 4. 비교 분석

| 구분 | 정적 IP 할당 | 동적 IP 할당 |
|------|-------------|-------------|
| **관리 방식** | 수동 설정 | 자동 할당 |
| **IP 주소 고정성** | 고정 | 유동적 |
| **관리 복잡도** | 높음 | 낮음 |
| **확장성** | 제한적 | 우수함 |
| **서버 의존성** | 없음 | DHCP 서버 필요 |
| **보안성** | 높음 (예측 가능) | 보통 (변동성) |
| **네트워크 부하** | 없음 | DHCP 트래픽 발생 |
| **IP 주소 효율성** | 낮음 | 높음 |
| **장애 복구** | 수동 대응 | 자동 복구 |
| **비용** | 관리 비용 높음 | 서버 운영 비용 |

### 4.1 성능 비교

**네트워크 시작 시간:**
- 정적 IP: 즉시 통신 가능
- 동적 IP: DHCP 프로세스 완료 후 통신 가능 (보통 2-10초)

**주소 갱신 오버헤드:**
- 정적 IP: 없음
- 동적 IP: Lease 갱신 트래픽 발생 (주기적)

## 5. 실제 사용 사례

### 5.1 정적 IP를 사용해야 하는 경우

**서버 및 인프라:**
```java
// 웹 서버 설정 예제
public class StaticIPWebServer {
    private static final String STATIC_IP = "192.168.1.10";
    private static final int PORT = 8080;
    
    public static void main(String[] args) {
        // 정적 IP로 고정된 서버
        // DNS A 레코드: www.example.com -> 192.168.1.10
        ServerSocket server = new ServerSocket(PORT, 50, 
            InetAddress.getByName(STATIC_IP));
        
        System.out.println("Server running on " + STATIC_IP + ":" + PORT);
        // 클라이언트는 항상 동일한 IP로 접속 가능
    }
}
```

**적용 사례:**
- 웹 서버, 데이터베이스 서버
- 네트워크 프린터
- IP 카메라, NAS 장비
- 게이트웨이, 라우터
- DNS 서버, 메일 서버

### 5.2 동적 IP를 사용하는 경우

**클라이언트 디바이스:**
```java
// 동적 IP 클라이언트 예제
public class DynamicIPClient {
    public static void main(String[] args) {
        try {
            // 현재 할당받은 IP 확인
            InetAddress localHost = InetAddress.getLocalHost();
            System.out.println("Current IP: " + localHost.getHostAddress());
            
            // IP가 변경되어도 도메인명으로 서버 접속
            Socket socket = new Socket("www.example.com", 8080);
            // DHCP에서 할당받은 IP로 통신
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

**적용 사례:**
- 개인용 컴퓨터, 노트북
- 스마트폰, 태블릿
- IoT 센서 디바이스
- 게스트 네트워크
- 임시 접속 장비

### 5.3 하이브리드 방식: DHCP Reservation

```bash
# DHCP 서버에서 MAC 주소 기반 고정 할당
host printer {
    hardware ethernet 08:00:07:26:c0:a5;
    fixed-address 192.168.1.100;
}

host server {
    hardware ethernet 52:54:00:12:34:56;
    fixed-address 192.168.1.200;
}
```

이 방식은 DHCP의 자동화 장점과 정적 IP의 안정성을 결합한 방법이다.

## 6. 결론

정적 IP와 동적 IP 할당 방식은 각각 고유한 장단점을 가지고 있으며, 네트워크 환경과 요구사항에 따라 적절한 방식을 선택해야 한다.

**선택 기준:**

- **서버 환경**: 정적 IP 권장 (안정성, 접근성)
- **클라이언트 환경**: 동적 IP 권장 (관리 효율성)
- **소규모 네트워크**: 정적 IP도 관리 가능
- **대규모 네트워크**: 동적 IP 필수
- **보안이 중요한 환경**: 정적 IP + ACL 조합
- **유연성이 중요한 환경**: 동적 IP + DHCP Reservation

현대 네트워크 환경에서는 두 방식을 적절히 조합하여 사용하는 것이 일반적이다. 핵심 서버는 정적 IP로, 클라이언트 디바이스는 동적 IP로 할당하되, 필요에 따라 DHCP Reservation을 활용하여 특정 디바이스에는 고정 IP를 보장하는 하이브리드 접근법이 권장된다.

## 7. 참고 자료

- [RFC 2131 - Dynamic Host Configuration Protocol](https://tools.ietf.org/html/rfc2131)
- [RFC 3927 - Dynamic Configuration of IPv4 Link-Local Addresses](https://tools.ietf.org/html/rfc3927)
- [DHCP 옵션 코드 표준](https://www.iana.org/assignments/bootp-dhcp-parameters/bootp-dhcp-parameters.xhtml)
- [TCP/IP 네트워킹 가이드](https://docs.microsoft.com/en-us/windows-server/networking/)