# DNS Record Types

## 1. 한 줄 정의
DNS Record는 도메인 이름과 IP 주소 또는 기타 정보를 매핑하는 DNS 서버의 데이터베이스 항목으로, 각 레코드 타입은 특정 목적과 형식을 가진다.

---

## 2. 주요 DNS Record Types

### 2-1. A Record (Address Record)
- **목적**: 도메인 이름을 IPv4 주소로 매핑
- **형식**: `도메인명 → IPv4 주소`
- **TTL**: 일반적으로 300~86400초

```
example.com.     3600    IN    A    192.0.2.1
www.example.com. 3600    IN    A    192.0.2.1
```

**특징**:
- 가장 기본적이고 널리 사용되는 레코드 타입
- 웹사이트 접속 시 필수적으로 사용
- 하나의 도메인에 여러 A 레코드 설정 가능 (로드 밸런싱)

**사용 사례**:
```
# 웹서버 연결
blog.example.com → 203.0.113.10

# 로드 밸런싱 (Round-robin DNS)
api.example.com → 198.51.100.1
api.example.com → 198.51.100.2
api.example.com → 198.51.100.3
```

---

### 2-2. AAAA Record (IPv6 Address Record)
- **목적**: 도메인 이름을 IPv6 주소로 매핑
- **형식**: `도메인명 → IPv6 주소`
- **A Record의 IPv6 버전**

```
example.com.     3600    IN    AAAA    2001:0db8:85a3:0000:0000:8a2e:0370:7334
www.example.com. 3600    IN    AAAA    2001:0db8::1
```

**특징**:
- IPv6 네트워크 환경에서 필수
- A 레코드와 함께 설정 가능 (듀얼 스택)
- IPv6 주소는 128비트 (IPv4는 32비트)

---

### 2-3. CNAME Record (Canonical Name Record)
- **목적**: 도메인의 별칭(alias)을 다른 도메인으로 매핑
- **형식**: `별칭 도메인 → 실제 도메인`
- **제약**: 루트 도메인(@)에는 사용 불가

```
www.example.com.    3600    IN    CNAME    example.com.
blog.example.com.   3600    IN    CNAME    hosting.provider.com.
shop.example.com.   3600    IN    CNAME    shopify.example.com.
```

**특징**:
- 도메인 리다이렉션에 사용
- CNAME은 다른 CNAME을 가리킬 수 있지만 권장하지 않음 (체이닝)
- 같은 이름에 다른 레코드 타입과 공존 불가

**A Record vs CNAME**:
```
# A Record - 직접 IP 매핑
www.example.com. → 192.0.2.1

# CNAME - 다른 도메인으로 별칭
www.example.com. → example.com. → 192.0.2.1
```

**사용 사례**:
```
# CDN 연결
cdn.example.com → d111111abcdef8.cloudfront.net

# 외부 서비스 연결
support.example.com → company.zendesk.com
```

---

### 2-4. MX Record (Mail Exchange Record)
- **목적**: 이메일 서버 지정
- **형식**: `도메인명 → 우선순위 메일서버`
- **우선순위**: 낮은 숫자가 높은 우선순위

```
example.com.    3600    IN    MX    10    mail1.example.com.
example.com.    3600    IN    MX    20    mail2.example.com.
example.com.    3600    IN    MX    30    mail3.example.com.
```

**특징**:
- 우선순위 값으로 메일 서버 순서 결정
- 여러 MX 레코드로 이메일 고가용성 확보
- 메일 서버는 A 또는 AAAA 레코드 필요

**작동 방식**:
```
1. user@example.com으로 메일 발송
2. DNS에서 example.com의 MX 레코드 조회
3. 우선순위 10번 mail1.example.com으로 먼저 시도
4. 실패 시 우선순위 20번 mail2.example.com으로 시도
```

**사용 사례**:
```
# Google Workspace
example.com.    MX    1    aspmx.l.google.com.
example.com.    MX    5    alt1.aspmx.l.google.com.

# Microsoft 365
example.com.    MX    10   example-com.mail.protection.outlook.com.
```

---

### 2-5. TXT Record (Text Record)
- **목적**: 임의의 텍스트 데이터 저장
- **형식**: `도메인명 → "텍스트 문자열"`
- **주요 용도**: 도메인 소유 인증, 이메일 보안 정책

```
example.com.    3600    IN    TXT    "v=spf1 include:_spf.google.com ~all"
example.com.    3600    IN    TXT    "google-site-verification=rXOXy..."
_dmarc.example.com.     IN    TXT    "v=DMARC1; p=quarantine; rua=mailto:dmarc@example.com"
```

**주요 활용 사례**:

#### 5-1. SPF (Sender Policy Framework)
```
example.com.    TXT    "v=spf1 ip4:192.0.2.0/24 include:_spf.google.com ~all"
```
- 이메일 발신자 검증
- 스팸 및 피싱 방지

#### 5-2. DKIM (DomainKeys Identified Mail)
```
default._domainkey.example.com.    TXT    "v=DKIM1; k=rsa; p=MIGfMA0GCSq..."
```
- 이메일 서명 및 검증
- 이메일 변조 방지

#### 5-3. DMARC (Domain-based Message Authentication)
```
_dmarc.example.com.    TXT    "v=DMARC1; p=reject; rua=mailto:dmarc@example.com"
```
- SPF/DKIM 정책 설정
- 이메일 보안 정책 통합

#### 5-4. 도메인 소유 인증
```
# Google Search Console
example.com.    TXT    "google-site-verification=abc123..."

# SSL 인증서 발급 (DCV)
_acme-challenge.example.com.    TXT    "xyz789..."
```

---

### 2-6. NS Record (Name Server Record)
- **목적**: 도메인의 권한 있는 네임서버 지정
- **형식**: `도메인명 → 네임서버 주소`
- **필수**: 모든 도메인에 최소 2개 이상 설정

```
example.com.    3600    IN    NS    ns1.example.com.
example.com.    3600    IN    NS    ns2.example.com.
example.com.    3600    IN    NS    ns3.example.com.
```

**특징**:
- DNS 위임(delegation)에 사용
- 서브도메인의 네임서버 지정 가능
- 고가용성을 위해 여러 개 설정

**사용 사례**:
```
# 서브도메인 위임
dev.example.com.    NS    ns1.dev-hosting.com.
dev.example.com.    NS    ns2.dev-hosting.com.
```

---

### 2-7. PTR Record (Pointer Record)
- **목적**: 역방향 DNS 조회 (IP → 도메인)
- **형식**: `IP 주소 → 도메인명`
- **특수 도메인**: `.in-addr.arpa` (IPv4), `.ip6.arpa` (IPv6)

```
1.2.0.192.in-addr.arpa.    3600    IN    PTR    mail.example.com.
```

**특징**:
- 정방향 DNS (도메인→IP)의 반대
- 이메일 서버 신뢰도 향상에 필수
- 스팸 필터링에서 검증용으로 사용

**사용 사례**:
```
# 메일 서버 설정
192.0.2.10 → mail.example.com

# 역방향 조회 확인
$ dig -x 192.0.2.10
1.2.0.192.in-addr.arpa.    PTR    mail.example.com.
```

---

### 2-8. SRV Record (Service Record)
- **목적**: 특정 서비스의 위치 정보 제공
- **형식**: `_서비스._프로토콜.도메인 → 우선순위 가중치 포트 타겟`

```
_sip._tcp.example.com.    3600    IN    SRV    10    60    5060    sipserver.example.com.
_xmpp._tcp.example.com.   3600    IN    SRV    10    0     5222    xmpp.example.com.
```

**레코드 구조**:
```
_service._proto.name.    TTL    class    SRV    priority    weight    port    target.
```

**필드 설명**:
- **Priority**: 우선순위 (낮을수록 우선)
- **Weight**: 가중치 (같은 우선순위 내 부하 분산)
- **Port**: 서비스 포트 번호
- **Target**: 실제 호스트 이름

**사용 사례**:
```
# Microsoft Active Directory
_ldap._tcp.dc._msdcs.example.com.    SRV    0    0    389    dc1.example.com.

# Minecraft 서버
_minecraft._tcp.example.com.    SRV    0    5    25565    mc.example.com.

# VoIP (SIP)
_sip._tls.example.com.    SRV    10    60    5061    sip.example.com.
```

---

### 2-9. CAA Record (Certification Authority Authorization)
- **목적**: SSL/TLS 인증서 발급 권한 제어
- **형식**: `도메인명 → 플래그 태그 "값"`
- **보안**: 무단 인증서 발급 방지

```
example.com.    3600    IN    CAA    0    issue    "letsencrypt.org"
example.com.    3600    IN    CAA    0    issue    "digicert.com"
example.com.    3600    IN    CAA    0    iodef    "mailto:security@example.com"
```

**태그 종류**:
- **issue**: 인증서 발급 허용 CA
- **issuewild**: 와일드카드 인증서 발급 허용 CA
- **iodef**: 위반 사항 보고 이메일

**특징**:
- CAA 레코드 없으면 모든 CA 발급 가능
- 보안 강화를 위해 설정 권장
- Let's Encrypt 등 주요 CA에서 검증

**사용 사례**:
```
# Let's Encrypt만 허용
example.com.    CAA    0    issue    "letsencrypt.org"

# 와일드카드 금지
example.com.    CAA    0    issuewild    ";"

# 위반 보고
example.com.    CAA    0    iodef    "mailto:ssl-abuse@example.com"
```

---

### 2-10. SOA Record (Start of Authority)
- **목적**: DNS 존(zone)의 권한 정보 및 설정
- **형식**: 복잡한 구조 (MNAME, RNAME, SERIAL, REFRESH, RETRY, EXPIRE, MINIMUM)
- **필수**: 모든 DNS 존에 하나만 존재

```
example.com.    3600    IN    SOA    ns1.example.com. admin.example.com. (
                                    2024010101 ; Serial (버전 번호)
                                    7200       ; Refresh (새로고침 간격)
                                    3600       ; Retry (재시도 간격)
                                    1209600    ; Expire (만료 기간)
                                    86400      ; Minimum TTL
                                )
```

**필드 설명**:
- **MNAME**: 주 네임서버 (Primary Name Server)
- **RNAME**: 관리자 이메일 (@ 대신 . 사용)
- **Serial**: 존 파일 버전 (보통 YYYYMMDDNN 형식)
- **Refresh**: 보조 서버가 주 서버 확인 주기
- **Retry**: Refresh 실패 시 재시도 간격
- **Expire**: 주 서버 응답 없을 때 존 데이터 만료 시간
- **Minimum TTL**: 네거티브 캐싱 시간

---

## 3. DNS Record 우선순위 및 처리 순서

### 3-1. 조회 우선순위
```
1. A/AAAA Record: 직접 IP 조회
2. CNAME Record: 별칭 확인 후 A/AAAA 조회
3. MX Record: 메일 서버는 우선순위 필드로 결정
4. SRV Record: 우선순위 + 가중치로 결정
```

### 3-2. 레코드 충돌 규칙
```
# 불가능한 조합
example.com.    A        192.0.2.1
example.com.    CNAME    other.com.    # 에러! 같은 이름에 A와 CNAME 공존 불가

# 가능한 조합
example.com.    A        192.0.2.1
example.com.    MX       mail.example.com.    # 가능! A와 MX는 공존 가능
example.com.    TXT      "v=spf1..."          # 가능! 여러 레코드 타입 공존
```

---

## 4. 실무 활용 예시

### 4-1. 웹사이트 기본 설정
```
# 루트 도메인
example.com.        A        192.0.2.1
example.com.        AAAA     2001:db8::1

# www 서브도메인
www.example.com.    CNAME    example.com.

# 메일 서버
example.com.        MX       10    mail.example.com.
mail.example.com.   A        192.0.2.10

# 메일 보안
example.com.        TXT      "v=spf1 ip4:192.0.2.10 ~all"
```

### 4-2. CDN 및 외부 서비스 연동
```
# CloudFront CDN
cdn.example.com.    CNAME    d111111abcdef8.cloudfront.net.

# AWS S3 정적 호스팅
static.example.com. CNAME    example-bucket.s3-website-us-east-1.amazonaws.com.

# Google Workspace
example.com.        MX       1    aspmx.l.google.com.
example.com.        TXT      "google-site-verification=..."
```

### 4-3. 마이크로서비스 아키텍처
```
# API Gateway
api.example.com.        A        10.0.1.10
api.example.com.        A        10.0.1.11    # 로드 밸런싱

# 서비스별 서브도메인
auth.example.com.       CNAME    api.example.com.
payment.example.com.    CNAME    api.example.com.
user.example.com.       CNAME    api.example.com.

# Kubernetes Ingress
k8s.example.com.        A        10.0.2.100
*.k8s.example.com.      A        10.0.2.100    # 와일드카드
```

---

## 5. 백엔드 개발자 관점의 중요성

### 5-1. 인프라 설계
- **로드 밸런싱**: 여러 A 레코드로 트래픽 분산
- **고가용성**: MX, NS 레코드 다중화로 장애 대응
- **마이크로서비스**: 서브도메인으로 서비스 분리

### 5-2. 보안 강화
- **이메일 보안**: SPF, DKIM, DMARC로 스팸 및 피싱 방지
- **인증서 제어**: CAA 레코드로 무단 SSL 발급 차단
- **도메인 검증**: TXT 레코드로 소유권 인증

### 5-3. 성능 최적화
- **TTL 설정**: 적절한 캐싱으로 DNS 조회 최소화
- **CDN 연동**: CNAME으로 정적 리소스 분산
- **Geo DNS**: 지역별 최적 서버 라우팅

### 5-4. DevOps 자동화
- **블루-그린 배포**: DNS 레코드 변경으로 무중단 배포
- **A/B 테스팅**: 가중치 기반 트래픽 분배
- **재해 복구**: DNS Failover로 자동 전환

---

## 6. DNS Record 조회 명령어

### 6-1. dig 명령어
```bash
# 기본 A 레코드 조회
dig example.com

# 특정 레코드 타입 조회
dig example.com MX
dig example.com TXT
dig example.com NS

# 역방향 조회
dig -x 192.0.2.1

# 모든 레코드 조회
dig example.com ANY

# 특정 네임서버로 조회
dig @8.8.8.8 example.com

# 간결한 출력
dig +short example.com
```

### 6-2. nslookup 명령어
```bash
# 기본 조회
nslookup example.com

# 특정 레코드 타입
nslookup -type=MX example.com
nslookup -type=TXT example.com

# 특정 DNS 서버 사용
nslookup example.com 8.8.8.8
```

### 6-3. host 명령어
```bash
# 간단한 조회
host example.com

# 모든 레코드
host -a example.com

# 특정 타입
host -t MX example.com
```

---

## 7. 핵심 요약

| Record Type | 목적 | 형식 | 주요 사용처 |
|-------------|------|------|-------------|
| **A** | IPv4 매핑 | 도메인 → IP | 웹사이트, 서버 |
| **AAAA** | IPv6 매핑 | 도메인 → IPv6 | IPv6 네트워크 |
| **CNAME** | 도메인 별칭 | 별칭 → 실제 도메인 | CDN, 외부 서비스 |
| **MX** | 메일 서버 | 우선순위 + 메일서버 | 이메일 수신 |
| **TXT** | 텍스트 데이터 | 임의 문자열 | SPF, 도메인 인증 |
| **NS** | 네임서버 지정 | 네임서버 주소 | DNS 위임 |
| **PTR** | 역방향 조회 | IP → 도메인 | 메일 서버 검증 |
| **SRV** | 서비스 위치 | 우선순위+포트+호스트 | VoIP, LDAP |
| **CAA** | 인증서 권한 | CA 정보 | SSL 보안 |
| **SOA** | 존 권한 정보 | 관리 정보 | DNS 존 관리 |

### 7-1. 선택 기준

**A vs CNAME**:
- IP 주소 직접 지정 → **A 레코드**
- 다른 도메인으로 별칭 → **CNAME**
- 루트 도메인(@) → **A 레코드** (CNAME 불가)

**TTL 설정**:
- 자주 변경 → 짧은 TTL (300~900초)
- 안정적 운영 → 긴 TTL (3600~86400초)
- 배포/마이그레이션 전 → TTL 미리 낮추기

**보안 레코드**:
- 이메일 발송 → **SPF** (TXT)
- 이메일 서명 → **DKIM** (TXT)
- 통합 정책 → **DMARC** (TXT)
- SSL 제어 → **CAA**

### 7-2. 실무 팁

1. **DNS 전파 시간 고려**
   - 레코드 변경 후 최대 48시간 소요 (실제로는 TTL에 따라 결정)
   - 중요한 변경 전 TTL을 미리 낮추기

2. **레코드 우선순위 설정**
   - MX, SRV 레코드는 우선순위 필드 활용
   - 주 서버와 백업 서버 구분

3. **CNAME 체이닝 금지**
   - CNAME → CNAME → A 는 성능 저하
   - 최대 1단계 별칭 유지

4. **루트 도메인 제약**
   - @ (루트)에는 CNAME 사용 불가
   - ALIAS 또는 ANAME 레코드 사용 (일부 DNS 제공자)

5. **보안 강화 필수**
   - SPF, DKIM, DMARC 설정으로 이메일 보안
   - CAA 레코드로 SSL 인증서 제어
   - DNSSEC 고려 (고급 보안)

6. **모니터링 및 검증**
   - DNS 레코드 변경 후 dig/nslookup으로 확인
   - 여러 DNS 서버(8.8.8.8, 1.1.1.1)에서 검증
   - DNS 전파 상태 확인 도구 활용 (whatsmydns.net)

7. **재해 복구 계획**
   - 여러 네임서버 설정 (최소 2개)
   - 지리적으로 분산된 서버 사용
   - DNS Failover 설정으로 자동 복구
