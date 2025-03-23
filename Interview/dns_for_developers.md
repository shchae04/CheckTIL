# DNS 작동 원리

DNS(Domain Name System)는 인터넷의 핵심 인프라로, 도메인 이름을 IP 주소로 변환하는 시스템입니다.

## 1. DNS란 무엇인가?

DNS는 사람이 읽기 쉬운 도메인 이름(예: `example.com`)을 컴퓨터가 이해할 수 있는 IP 주소(예: `192.0.2.1`)로 변환하는 시스템입니다. 이는 마치 전화번호부와 같은 역할을 합니다.

**주요 기능:**
- 도메인 이름을 IP 주소로 변환
- 이메일 서버 정보 제공
- 서비스 위치 정보 제공

## 2. DNS 작동 방식

### 기본 작동 과정

1. 사용자가 브라우저에 `www.example.com` 입력
2. 브라우저는 로컬 DNS 서버에 이 도메인의 IP 주소를 요청
3. 로컬 DNS 서버는 다른 DNS 서버들과 통신하여 IP 주소를 찾음
4. IP 주소가 브라우저에 반환되고, 브라우저는 해당 서버에 연결

### DNS 계층 구조

```
루트(.) → TLD(.com) → 도메인(example.com) → 서브도메인(www.example.com)
```

| 계층 | 역할 | 예시 |
|------|------|------|
| 루트 DNS 서버 | DNS 계층의 최상위 | `.` |
| TLD DNS 서버 | 최상위 도메인 관리 | `.com`, `.org`, `.net` |
| 권한 있는 DNS 서버 | 특정 도메인의 DNS 레코드 관리 | `example.com` |
| 로컬 DNS 서버 | 사용자 DNS 쿼리 처리 | ISP DNS, Google DNS(8.8.8.8) |

## 3. 주요 DNS 레코드 유형

개발자가 자주 사용하는 DNS 레코드 유형:

| 레코드 | 용도 | 예시 |
|--------|------|------|
| A | 도메인을 IPv4 주소에 연결 | `example.com → 192.0.2.1` |
| AAAA | 도메인을 IPv6 주소에 연결 | `example.com → 2001:0db8:85a3::8a2e:0370:7334` |
| CNAME | 도메인의 별칭 설정 | `www.example.com → example.com` |
| MX | 이메일 서버 지정 | `example.com → mail.example.com` |
| TXT | 텍스트 정보 저장 | SPF, DKIM 등 이메일 인증에 사용 |
| NS | 도메인의 네임서버 지정 | `example.com → ns1.example.com` |

## 4. DNS 캐싱과 TTL

DNS 응답은 성능 향상을 위해 여러 계층에서 캐싱됩니다:

- **브라우저 캐시**: 브라우저가 일정 시간 동안 DNS 결과 저장
- **OS 캐시**: 운영체제 수준에서 DNS 결과 저장
- **로컬 DNS 서버 캐시**: ISP의 DNS 서버가 결과 저장

**TTL(Time To Live)**: DNS 레코드가 캐시에 얼마나 오래 유지될지 결정하는 값(초 단위)
- 짧은 TTL: 빠른 변경 전파, 높은 DNS 트래픽
- 긴 TTL: 느린 변경 전파, 낮은 DNS 트래픽

## 5. 개발 시 알아두면 좋은 DNS 관련 팁

### 로컬 개발 환경 설정

- `/etc/hosts` 파일 활용 (Windows: `C:\Windows\System32\drivers\etc\hosts`)
  ```
  127.0.0.1 dev.example.com
  ```

### DNS 문제 해결 도구

- **dig/nslookup**: DNS 조회 및 문제 진단
  ```bash
  dig example.com
  nslookup example.com
  ```

- **DNS 캐시 초기화**:
  - Windows: `ipconfig /flushdns`
  - macOS: `sudo killall -HUP mDNSResponder`
  - Linux: `sudo systemd-resolve --flush-caches`

### DNS 변경 시 주의사항

1. **TTL 값 조정**: 중요한 변경 전 TTL 값을 낮게 설정 (예: 300초)
2. **단계적 변경**: 한 번에 모든 것을 변경하지 않고 단계적으로 진행
3. **변경 확인**: 여러 위치와 DNS 서버에서 변경 사항 확인

## 6. 현대적인 DNS 기술

### DNS over HTTPS (DoH)와 DNS over TLS (DoT)

- 기존 DNS의 보안 취약점을 해결하기 위한 암호화 프로토콜
- 프라이버시 보호 및 중간자 공격 방지
- 주요 브라우저와 OS에서 지원 증가 중

### DNS 기반 로드 밸런싱

- 지리적 위치에 따른 트래픽 라우팅
- 서버 상태에 따른 동적 라우팅
- 다중 CDN 전략에 활용

## 결론

DNS는 인터넷의 기본 인프라로, 기본 개념과 작동 방식을 이해하는 것이 중요합니다. 웹 애플리케이션 개발, 서버 구성, 네트워크 문제 해결 등 다양한 상황에서 DNS 지식이 필요합니다.

## 참고 자료

- [Mozilla DNS 문서](https://developer.mozilla.org/ko/docs/Glossary/DNS)
- [Cloudflare DNS 학습 센터](https://www.cloudflare.com/ko-kr/learning/dns/what-is-dns/)