# OAuth2 흐름 (OAuth2 Flow)

OAuth2는 인터넷 사용자들이 비밀번호를 제공하지 않고 다른 웹사이트 상의 자신들의 정보에 웹사이트나 애플리케이션의 접근 권한을 부여할 수 있는 공통적인 수단으로서 사용되는 개방형 표준 인증 프로토콜입니다. 이 문서에서는 OAuth2의 다양한 인증 흐름(flow)에 대해 설명합니다.

## OAuth2의 주요 개념

OAuth2에서는 다음과 같은 주요 개념들이 있습니다:

1. **Resource Owner**: 보호된 리소스에 접근할 수 있는 권한을 부여하는 주체 (일반적으로 사용자)
2. **Client**: Resource Owner를 대신하여 보호된 리소스에 접근하려는 애플리케이션
3. **Resource Server**: 보호된 리소스를 호스팅하는 서버
4. **Authorization Server**: Resource Owner의 인증 및 권한 부여 후 Client에게 액세스 토큰을 발급하는 서버
5. **Access Token**: 보호된 리소스에 접근하기 위해 사용되는 자격 증명
6. **Refresh Token**: 액세스 토큰이 만료된 후 새 액세스 토큰을 얻기 위해 사용되는 자격 증명

## OAuth2 인증 흐름 유형

OAuth2는 다양한 클라이언트 유형과 사용 사례에 맞게 여러 인증 흐름을 제공합니다:

### 1. Authorization Code 흐름

가장 일반적인 흐름으로, 서버 사이드 애플리케이션에 적합합니다.

**흐름 단계:**
1. 클라이언트가 사용자를 인증 서버로 리디렉션합니다.
2. 사용자가 인증하고 권한을 부여합니다.
3. 인증 서버가 사용자를 인증 코드와 함께 클라이언트의 리디렉션 URI로 리디렉션합니다.
4. 클라이언트는 인증 코드와 클라이언트 비밀을 인증 서버에 제출하여 액세스 토큰을 요청합니다.
5. 인증 서버는 액세스 토큰과 선택적으로 리프레시 토큰을 발급합니다.

```
+----------+
| Resource |
|   Owner  |
+----------+
     ^
     |
    (B)
+----|-----+          Client Identifier      +---------------+
|         -+----(A)-- & Redirection URI ---->|               |
|  Client  |                                 | Authorization |
|          |<---(C)-- Authorization Code ----|    Server    |
|          |                                 |               |
|          |----(D)-- Authorization Code --->|               |
|          |          & Redirection URI      |               |
|          |                                 |               |
|          |<---(E)----- Access Token -------|               |
+-----------+       (w/ Optional Refresh     +---------------+
                            Token)
```

### 2. Implicit 흐름

브라우저 기반 애플리케이션(SPA)에 적합했던 흐름이지만, 현재는 보안상의 이유로 권장되지 않습니다.

**흐름 단계:**
1. 클라이언트가 사용자를 인증 서버로 리디렉션합니다.
2. 사용자가 인증하고 권한을 부여합니다.
3. 인증 서버가 액세스 토큰을 URL 프래그먼트에 포함하여 사용자를 클라이언트의 리디렉션 URI로 리디렉션합니다.
4. 브라우저가 리디렉션 URI로 요청을 보냅니다 (토큰은 서버로 전송되지 않음).
5. 클라이언트 측 스크립트가 액세스 토큰을 추출합니다.

```
+----------+
| Resource |
|  Owner   |
+----------+
     ^
     |
    (B)
+----|-----+          Client Identifier     +---------------+
|         -+----(A)-- & Redirection URI --->|               |
|  Client  |                                | Authorization |
|          |<---(C)-- Access Token ---------|    Server    |
|          |    (w/ Optional Refresh Token) |               |
+-----------+                               +---------------+
```

### 3. Resource Owner Password Credentials 흐름

클라이언트가 사용자의 자격 증명을 직접 수집할 수 있는 경우에 사용됩니다. 높은 신뢰도가 필요하며, 가능하면 다른 흐름을 사용하는 것이 좋습니다.

**흐름 단계:**
1. 사용자가 클라이언트에 자격 증명(사용자 이름 및 비밀번호)을 제공합니다.
2. 클라이언트는 이러한 자격 증명을 인증 서버에 제출하여 액세스 토큰을 요청합니다.
3. 인증 서버는 액세스 토큰과 선택적으로 리프레시 토큰을 발급합니다.

```
+----------+
| Resource |
|  Owner   |
+----------+
     v
     |    Resource Owner
    (A) Password Credentials
     |
     v
+---------+                                  +---------------+
|         |>--(B)---- Resource Owner ------->|               |
|         |         Password Credentials     | Authorization |
| Client  |                                  |     Server    |
|         |<--(C)---- Access Token ---------<|               |
|         |    (w/ Optional Refresh Token)   |               |
+---------+                                  +---------------+
```

### 4. Client Credentials 흐름

클라이언트가 자신의 자격 증명을 사용하여 액세스 토큰을 요청하는 경우에 사용됩니다. 주로 서버 간 통신에 적합합니다.

**흐름 단계:**
1. 클라이언트가 자신의 자격 증명(클라이언트 ID 및 비밀)을 인증 서버에 제출하여 액세스 토큰을 요청합니다.
2. 인증 서버는 액세스 토큰을 발급합니다.

```
+---------+                                  +---------------+
|         |                                  |               |
|         |>--(A)- Client Authentication --->| Authorization |
| Client  |                                  |     Server    |
|         |<--(B)---- Access Token ---------<|               |
|         |                                  |               |
+---------+                                  +---------------+
```

### 5. Authorization Code Flow with PKCE (Proof Key for Code Exchange)

모바일 및 SPA 애플리케이션을 위한 보안 강화 버전의 Authorization Code 흐름입니다.

**흐름 단계:**
1. 클라이언트가 코드 검증기(code verifier)를 생성하고 이를 해시하여 코드 챌린지(code challenge)를 만듭니다.
2. 클라이언트가 사용자를 코드 챌린지와 함께 인증 서버로 리디렉션합니다.
3. 사용자가 인증하고 권한을 부여합니다.
4. 인증 서버가 사용자를 인증 코드와 함께 클라이언트의 리디렉션 URI로 리디렉션합니다.
5. 클라이언트는 인증 코드와 원래의 코드 검증기를 인증 서버에 제출하여 액세스 토큰을 요청합니다.
6. 인증 서버는 코드 검증기를 검증하고 액세스 토큰을 발급합니다.

## OAuth2 토큰

### Access Token

액세스 토큰은 보호된 리소스에 접근하기 위한 자격 증명입니다. 일반적으로 JWT(JSON Web Token) 형식으로 발급되며, 다음과 같은 정보를 포함할 수 있습니다:

- **iss (issuer)**: 토큰 발급자
- **sub (subject)**: 토큰 주체 (사용자 식별자)
- **aud (audience)**: 토큰 대상자 (리소스 서버)
- **exp (expiration time)**: 토큰 만료 시간
- **iat (issued at)**: 토큰 발급 시간
- **scope**: 토큰이 부여하는 권한 범위

### Refresh Token

리프레시 토큰은 액세스 토큰이 만료된 후 새 액세스 토큰을 얻기 위해 사용됩니다. 액세스 토큰보다 수명이 길며, 보안을 위해 클라이언트 측에 안전하게 저장해야 합니다.

## OAuth2 보안 고려사항

1. **HTTPS 사용**: 모든 OAuth2 통신은 HTTPS를 통해 이루어져야 합니다.
2. **상태 매개변수 검증**: CSRF 공격을 방지하기 위해 상태 매개변수를 사용하고 검증해야 합니다.
3. **리디렉션 URI 검증**: 인증 서버는 등록된 리디렉션 URI만 허용해야 합니다.
4. **토큰 저장**: 액세스 토큰과 리프레시 토큰은 안전하게 저장해야 합니다.
5. **토큰 범위 제한**: 필요한 최소한의 권한만 요청해야 합니다.
6. **PKCE 사용**: 모바일 및 SPA 애플리케이션에는 PKCE를 사용해야 합니다.

## Spring Security에서의 OAuth2 구현

Spring Security는 OAuth2 클라이언트 및 리소스 서버 구현을 위한 강력한 지원을 제공합니다.

### OAuth2 클라이언트 구현 예시

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests(authorize -> authorize
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/home")
            );
    }
}
```

### application.yml 설정 예시

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
          github:
            client-id: your-github-client-id
            client-secret: your-github-client-secret
```

## 결론

OAuth2는 다양한 인증 흐름을 제공하여 다양한 애플리케이션 유형과 사용 사례에 맞게 인증 및 권한 부여를 구현할 수 있게 합니다. 각 흐름은 고유한 장단점이 있으므로, 애플리케이션의 요구 사항과 보안 고려 사항에 따라 적절한 흐름을 선택하는 것이 중요합니다.

현대 웹 애플리케이션에서는 Authorization Code 흐름(서버 사이드 애플리케이션용) 또는 Authorization Code Flow with PKCE(SPA 및 모바일 애플리케이션용)가 가장 권장되는 접근 방식입니다.