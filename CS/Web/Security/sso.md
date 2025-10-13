# SSO (Single Sign-On)란?

하나의 로그인으로 여러 애플리케이션/서비스에 안전하게 접근할 수 있게 해주는 인증 방식입니다. 사용자는 단 한 번의 인증(로그인)으로, 해당 신뢰 영역(federation)에 포함된 다양한 서비스에 자동으로 로그인됩니다.

## TL;DR
- SSO = 한 번 로그인, 여러 서비스 자동 로그인
- 핵심 구성: IdP(Identity Provider, 신원 제공자)와 SP/RP(Service Provider/Relying Party, 서비스)
- 대표 프로토콜: SAML 2.0(기업 중심), OpenID Connect(OIDC, 소비자/웹·모바일 중심)
- OAuth2는 ‘권한 부여(Authorization)’ 표준이며, 사용자 인증(Authentication) 자체는 OIDC가 담당
- 보안 포인트: 리디렉션/토큰 유출 방지, state/nonce 검증, HTTPS, 세션/쿠키 설정(SameSite) 등

---

## 왜 SSO를 쓰는가?
- 사용자 경험(UX) 향상: 여러 서비스마다 로그인 반복 없음
- 보안 강화: 중앙 집중식 인증 정책(MFA, 비밀번호 정책, 계정 잠금 등) 적용 용이
- 운영 효율: 계정 생성/삭제(JIT/SCIM), 접근 통제, 감사 로깅을 IdP에서 일원화
- 확장성: SaaS/사내 시스템이 늘어나도 인증 체계는 공통화

## SSO의 구성요소
- Identity Provider(IdP): 사용자 신원을 확인하고 토큰/어설션을 발급하는 신뢰 기관
  - 예: Azure AD/Entra ID, Okta, Google, Keycloak, Auth0
- Service Provider(SP) 또는 Relying Party(RP): IdP가 발급한 신뢰 가능한 증명(SAML 어설션/ID 토큰)을 받아 사용자 세션을 생성하는 애플리케이션
- Federation(연합): IdP와 여러 SP가 신뢰 관계를 구성한 상태(메타데이터 교환, 키 교환 포함)

## 동작 흐름(일반적인 Redirect 기반)
1) 사용자가 서비스(SP)에 접근
2) SP는 아직 로그인 세션이 없음을 감지 → IdP로 리디렉션(SAML/OIDC 파라미터 포함)
3) 사용자가 IdP에서 인증(비밀번호/MFA/SSO 장치)
4) IdP는 인증 성공 시 서명된 결과(SAML Assertion 또는 OIDC ID Token)를 브라우저 경유로 SP에 전달
5) SP는 서명/만료/수신자(audience) 검증 후 자체 세션 생성(쿠키 설정)
6) 이후 동일 신뢰 영역의 다른 서비스 접근 시, 이미 IdP 측 세션이 있어 즉시 SSO 완료

참고: SSO는 대체로 브라우저 리디렉션을 통해 진행됩니다. 네이티브/모바일 앱은 시스템 브라우저(또는 ASWebAuthenticationSession 등)를 활용해 보안을 확보합니다.

## 프로토콜과 형식
- SAML 2.0: XML 기반, 기업 환경(사내 포털/레거시/대기업 SaaS)에서 널리 사용, “어설션(Assertion)” 전달
- OAuth 2.0: 권한 위임(리소스 접근 권한 토큰) 표준. ‘인증’ 자체 표준은 아님
- OpenID Connect(OIDC): OAuth2 위에서 ‘인증’을 정의. ID Token(JWT)으로 사용자 신원 전달, 현대 웹/모바일에 적합
- 토큰/어설션 형식
  - SAML Assertion: XML, 서명/암호화, 주로 POST 바인딩
  - JWT: JSON Web Token, 헤더/페이로드/서명, JWK 공개키로 검증

## SAML vs OAuth2 vs OIDC — 언제 무엇을 쓰나?
- SAML 2.0
  - 강점: 기업용, 레거시/엔터프라이즈 SaaS 호환성, 성숙한 생태계
  - 사용처: 사내 포털, ERP/HR 등 엔터프라이즈 애플리케이션 SSO
- OAuth2
  - 목적: 리소스 접근 권한 위임(“로그인” 표준이 아님)
  - 사용처: API 호출을 위한 액세스 토큰 발급(클라이언트 크리덴셜, 머신 투 머신 등)
- OpenID Connect(OIDC)
  - 강점: 표준화된 로그인, 경량 JSON, 모바일/SPA 친화적, PKCE 등 현대 보안 패턴
  - 사용처: 일반 소비자 서비스 로그인, 웹/모바일/SPA 통합 로그인

요약: “로그인(인증)”이 필요하면 OIDC, 기업 레거시는 SAML, API 권한 위임은 OAuth2.

## SP-Initiated vs IdP-Initiated
- SP-Initiated: 사용자가 먼저 서비스에 접근 → IdP로 리디렉션 → 인증 → 서비스로 귀환(권장)
- IdP-Initiated: IdP 포털에서 앱 아이콘 클릭 → 서비스로 토큰 전달(편리하지만 리다이렉트 검증 등 추가 보안 고려 필요)

## 계정 프로비저닝
- JIT(Just-In-Time) 프로비저닝: 첫 로그인 시 사용자 프로파일을 동적으로 생성
- SCIM(System for Cross-domain Identity Management): 계정/그룹 생성·수정·삭제를 IdP와 자동 동기화(사전/주기 동기)

## 보안 고려사항(핵심 체크리스트)
- HTTPS 강제 및 안전한 리디렉션(화이트리스트 기반)
- state/nonce 검증(OIDC), replay 방지 및 CSRF 방어
- PKCE 사용(모바일/SPA)
- 토큰 유효성 검증: 서명(JWK/메타데이터), 만료(exp), 발급자(iss), 대상(aud)
- 세션/쿠키 보안: HttpOnly, Secure, SameSite 적절 설정, 도메인 설계
- 로그아웃: SP 로그아웃, IdP 로그아웃, Single Logout(SLO) 지원 여부 정리
- 키 롤오버/메타데이터 갱신: IdP 키 변경 시 다운타임 없이 갱신
- MFA/조건부 접근(디바이스, 위치, 위험도) 정책 적용
- 시간 동기화(NTP)로 토큰 만료/발급 시간 오차 최소화

## 아키텍처 팁과 실무 패턴
- 도메인 전략: sso.example.com 같은 공통 도메인 사용 시 쿠키 전략 단순화 가능
- 게이트웨이/리버스 프록시 연동: 인증 프런트 도입 후 백엔드는 토큰/헤더 신뢰
- 멀티 테넌시: 테넌트별 IdP(엔터프라이즈 고객별) 연결, discovery endpoint 사용
- 감사 로깅: 로그인/실패/동의/권한 변경 이력 중앙 수집
- 장애 대응: IdP 장애시 대체 경로, 캐시된 키/메타데이터, 타임아웃/재시도 설계

## Spring Security 연동 예시
### OIDC 클라이언트(가장 보편)
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/", "/public/**").permitAll()
        .anyRequest().authenticated()
      )
      .oauth2Login(withDefaults())   // OIDC 로그인(구글/애저AD/Okta 등)
      .logout(logout -> logout.logoutSuccessUrl("/"));
    return http.build();
  }
}
```

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: your-client-id
            client-secret: your-client-secret
            scope: openid,profile,email
```

### SAML 2.0 서비스 제공자(SP)
Spring Security 5.2+는 SAML2 SP를 지원합니다.
```java
@Configuration
@EnableWebSecurity
public class SamlSecurityConfig {
  @Bean
  SecurityFilterChain samlChain(HttpSecurity http) throws Exception {
    http
      .saml2Login(withDefaults())
      .saml2Logout(withDefaults())
      .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
    return http.build();
  }
}
```
IdP 메타데이터(URL/파일) 등록과 인증서 신뢰 구성이 필요합니다.

## 면접 포인트 요약
- “SSO와 OAuth2/OIDC/SAML의 관계를 구분해서 설명해보세요.”
- “왜 OAuth2만으로 로그인이라 부르면 위험할 수 있나요?”(→ 인증은 OIDC가 정의)
- “SAML과 OIDC 각각의 장단점과 적합한 사용처는?”
- “state/nonce의 역할은? PKCE는 언제 쓰나요?”
- “Single Logout이 왜 어려운가요? 현실적인 대안은?”

## 자주 묻는 질문(FAQ)
- Q: SSO를 쓰면 비밀번호가 모든 서비스에 공유되나요?
  - A: 아니요. 비밀번호는 IdP에게만 제공되고, 서비스는 서명된 토큰/어설션만 신뢰합니다.
- Q: 소셜 로그인은 SSO인가요?
  - A: 기술적으로는 OIDC 기반 SSO의 한 형태로 볼 수 있습니다. 하나의 계정(Google/Apple 등)으로 다양한 서비스에 로그인합니다.
- Q: 토큰 저장은 어디에 하나요?
  - A: 브라우저 기반 앱은 서버 세션 쿠키를 선호하거나, SPA는 보안상 로컬스토리지 대신 백엔드 세션/프록시 패턴을 고려합니다.

## 참고 자료
- SAML 2.0 표준: https://docs.oasis-open.org/security/saml/v2.0/
- OpenID Connect Core: https://openid.net/specs/openid-connect-core-1_0.html
- OAuth 2.0 (RFC 6749): https://www.rfc-editor.org/rfc/rfc6749
- Spring Security SAML2: https://docs.spring.io/spring-security/reference/servlet/saml2/index.html
- Spring Security OAuth2/OIDC: https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html
