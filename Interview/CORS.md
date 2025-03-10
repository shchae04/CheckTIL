# CORS (Cross-Origin Resource Sharing)

웹 개발을 하다 보면 한번쯤 마주치게 되는 CORS 오류입니다. 브라우저 콘솔에 빨간색으로 가득한 CORS 에러 메시지를 본 적 있으신가요? 오늘은 이 골치 아픈 CORS에 대해 알아보겠습니다.

CORS는 쉽게 말해 "다른 출처(도메인, 프로토콜, 포트)에서 온 리소스를 브라우저가 로드할 수 있게 해주는 보안 체계"입니다. 웹 브라우저에서 실행되는 JavaScript가 다른 웹사이트의 데이터를 가져오려고 할 때 필요한 일종의 '통행증' 같은 것입니다.

## 1. CORS 개요

### 정의
- CORS는 브라우저가 가지고 있는 보안 장치로, "다른 사이트의 데이터를 가져가도 괜찮은지" 서버에게 물어보는 과정입니다.
- 웹 표준(W3C)으로 제정되어 있어서 Chrome, Firefox, Safari 등 모든 현대 브라우저에서 동작합니다.

### 동일 출처 정책(Same-Origin Policy)
- 브라우저의 '보안' 정책이라고 생각하면 됩니다. "다른 집(도메인)에서 나온 스크립트가 우리 집 물건을 가져가려고 하면, 안됩니다!"라고 말하는 것과 같습니다.
- **출처(Origin)란?** 웹 주소의 세 가지 요소를 합친 것입니다:
  - 프로토콜: http나 https (집으로 치면 아파트인지 단독주택인지)
  - 도메인: example.com (집 주소)
  - 포트: 80, 443 등 (현관문 번호)
- 예를 들어 `https://example.com:443`이 하나의 출처입니다.

```
https://example.com/page1  →  https://example.com/page2  ✓ (같은 집, 다른 방)
https://example.com        →  http://example.com         ✗ (같은 주소지만 건물 형태가 다름)
https://example.com        →  https://api.example.com    ✗ (같은 아파트 단지, 다른 동)
https://example.com:443    →  https://example.com:8080   ✗ (같은 집이지만 다른 현관문)
```

### CORS가 필요한 이유
현대 웹 개발에서 CORS는 선택이 아닌 필수가 되었습니다.

1. **프론트엔드와 백엔드 분리**: 요즘은 React로 만든 프론트엔드가 한 서버에, API는 다른 서버에 있는 경우가 흔하게 사용됩니다.

2. **마이크로서비스 구조**: 하나의 거대한 서비스보다 작은 서비스들이 협력하는 구조로, 각 서비스마다 도메인이 다르니 CORS가 필요합니다.

3. **외부 API 사용**: 구글 지도, 결제 시스템, SNS 로그인 등 외부 서비스를 사용하려면 CORS 설정이 필수입니다.

4. **CDN 활용**: 이미지, CSS, JavaScript 파일을 빠르게 전달하기 위해 CDN을 사용할 때도 CORS가 필요합니다..

## 2. CORS 동작 방식

CORS는 크게 두 가지 방식으로 동작합니다. 간단한 요청은 바로 처리하고, 복잡한 요청은 "먼저 물어보고" 처리합니다.

### 단순 요청 (Simple Request)
이것은 마치 편의점에 들어가서 바로 물건을 집는 것과 같습니다. 특별한 절차 없이 바로 처리됩니다.

**어떤 요청이 단순 요청인가요?**
- GET, HEAD, POST 같은 기본적인 HTTP 메서드를 사용할 때
- 폼 데이터나 일반 텍스트처럼 간단한 형식의 데이터를 보낼 때
- 특별한 커스텀 헤더를 추가하지 않았을 때

**어떻게 동작하나요?**
1. 브라우저: "안녕하세요, 저는 example.com입니다. 데이터를 요청합니다." (Origin 헤더 포함)
2. 서버: "네, 접근 가능합니다. 여기 데이터입니다." (Access-Control-Allow-Origin 헤더 포함)
3. 브라우저: "서버가 허락했으므로 데이터를 사용자에게 보여줍니다."

```
[브라우저]                                [서버]
    │                                      
    │  "데이터를 요청합니다"                 
    │  Origin: https://example.com         
    │ ─────────────────────────────────>  
    │                                      
    │  "접근 가능합니다"                    
    │  Access-Control-Allow-Origin: *      
    │ <─────────────────────────────────  
    │                                      
```

### 사전 요청 (Preflight Request)
이것은 마치 고급 레스토랑에 가기 전에 먼저 전화로 예약 가능 여부와 드레스 코드를 확인하는 것과 같습니다. "실제로 방문해도 될지" 먼저 물어보는 과정입니다.

**언제 사전 요청을 보내나요?**
- PUT, DELETE 같은 특별한 HTTP 메서드를 사용할 때
- JSON 같은 복잡한 데이터를 보낼 때
- 인증 토큰 같은 특별한 헤더를 추가할 때

**어떻게 동작하나요?**
1. 브라우저: "PUT 메서드로 요청해도 되는지 문의드립니다." (OPTIONS 메서드 사용)
2. 서버: "네, PUT 메서드 사용 가능합니다. 다음 조건에서 허용됩니다."
3. 브라우저: "확인했습니다. 이제 실제 요청을 보내겠습니다."
4. 브라우저: "여기 PUT 요청입니다."
5. 서버: "요청 처리 완료했습니다. 응답입니다."

```
[브라우저]                                [서버]
    │                                     
    │  "PUT 메서드 사용 가능한지 문의"       
    │  (OPTIONS 메서드로 확인)              
    │ ─────────────────────────────────>  
    │                                      
    │  "네, 사용 가능합니다"                
    │ <─────────────────────────────────  
    │                                      
    │  "실제 요청을 보냅니다"                
    │  (PUT 메서드로 실제 요청)             
    │ ─────────────────────────────────>  
    │                                      
    │  "요청 처리 완료했습니다"              
    │ <─────────────────────────────────  
    │                                      
```

### 인증 정보를 포함한 요청 (Credentialed Request)
이것은 "신분증을 보여주세요"라고 요청받는 상황과 유사합니다. 로그인 정보(쿠키)나 API 키 같은 민감한 정보를 포함할 때는 더 엄격한 규칙이 적용됩니다.

**프론트엔드에서 해야 할 일**:
- JavaScript fetch 사용 시: 인증 정보를 함께 전송 (`credentials: 'include'`)
- 기존 방식(XMLHttpRequest) 사용 시: 인증 정보 포함 설정 (`withCredentials = true`)

**백엔드에서 해야 할 일**:
- 인증 정보 수신 허용 설정 (`Access-Control-Allow-Credentials: true`)
- 와일드카드(`*`)는 사용할 수 없으며, 정확한 출처를 명시해야 합니다
- 예: `Access-Control-Allow-Origin: https://myapp.com` (와일드카드 `*` 사용 불가)

## 3. CORS 헤더

CORS는 특별한 HTTP 헤더들을 통해 통신합니다. 이는 마치 국제 통화에서 사용하는 특별한 용어들과 유사합니다.

### 요청 헤더 (브라우저가 서버에게)
- **Origin**: "저는 example.com에서 왔습니다" - 요청이 어디서 시작됐는지 알려줍니다
- **Access-Control-Request-Method**: "PUT 메서드를 사용해도 될까요?" - 사전 요청에서 사용합니다
- **Access-Control-Request-Headers**: "Authorization 헤더를 보내도 될까요?" - 사용할 커스텀 헤더 목록을 전달합니다

### 응답 헤더 (서버가 브라우저에게)
- **Access-Control-Allow-Origin**: "example.com은 우리 데이터를 사용할 수 있습니다"
  - 예: `Access-Control-Allow-Origin: https://example.com` (특정 사이트만 허용)
  - 또는 `Access-Control-Allow-Origin: *` (모든 사이트 허용, 단 인증 요청에는 사용 불가)

- **Access-Control-Allow-Methods**: "GET, POST, PUT 메서드는 사용할 수 있습니다"
  - 예: `Access-Control-Allow-Methods: GET, POST, PUT, DELETE`

- **Access-Control-Allow-Headers**: "Content-Type, Authorization 헤더는 사용할 수 있습니다"
  - 예: `Access-Control-Allow-Headers: Content-Type, Authorization`

- **Access-Control-Allow-Credentials**: "쿠키나 인증 정보도 받을 수 있습니다"
  - 예: `Access-Control-Allow-Credentials: true`

- **Access-Control-Expose-Headers**: "이런 특별한 헤더들도 읽을 수 있습니다"
  - 예: `Access-Control-Expose-Headers: X-Custom-Header`

- **Access-Control-Max-Age**: "이 허가는 1시간 동안 유효합니다, 그 동안은 다시 확인하지 않아도 됩니다"
  - 예: `Access-Control-Max-Age: 3600` (초 단위, 여기서는 1시간)

## 4. CORS 구현 예제

실제로 CORS를 어떻게 설정하는지 살펴보겠습니다. 프레임워크별로 조금씩 다르지만, 기본 원리는 동일합니다.

### 서버 측 구현 (Node.js/Express)
```javascript
const express = require('express');
const app = express();

// CORS 설정하기 (마치 경비원을 배치하는 것과 같습니다)
app.use((req, res, next) => {
  // 허용할 출처 설정
  res.header('Access-Control-Allow-Origin', 'https://example.com');

  // 허용할 헤더 설정
  res.header('Access-Control-Allow-Headers', 
    'Origin, X-Requested-With, Content-Type, Accept, Authorization');

  // 허용할 HTTP 메서드 설정
  res.header('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');

  // 인증 정보 허용 설정
  res.header('Access-Control-Allow-Credentials', 'true');

  // 사전 요청 결과 캐시 시간 설정
  res.header('Access-Control-Max-Age', '3600');

  // OPTIONS 요청 처리
  if (req.method === 'OPTIONS') {
    return res.status(204).end(); // 허용 응답 전송
  }

  // 다음 미들웨어로 진행
  next();
});

// 실제 API 엔드포인트
app.get('/api/data', (req, res) => {
  // 성공적으로 데이터 반환
  res.json({ message: 'CORS 요청이 성공했습니다!' });
});

// 서버 시작
app.listen(3000, () => {
  console.log('서버가 3000번 포트에서 실행 중입니다.');
});
```

### 서버 측 구현 (Spring Boot)
```java
// Spring Boot에서는 더 간결하게 설정할 수 있습니다
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")  // "/api/"로 시작하는 모든 경로에 적용
            .allowedOrigins("https://example.com")  // 특정 사이트만 허용
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 허용할 메서드 지정
            .allowedHeaders("Origin", "Content-Type", "Accept", "Authorization")  // 허용할 헤더 지정
            .allowCredentials(true)  // 인증 정보 허용
            .maxAge(3600);  // 1시간 동안 캐시
    }
}
```

### 클라이언트 측 구현 (JavaScript)
```javascript
// 기본적인 API 요청
fetch('https://api.example.com/data')
  .then(response => response.json())  // JSON으로 변환
  .then(data => {
    console.log('받은 데이터:', data);  // 데이터 사용
    document.getElementById('result').textContent = data.message;  // 화면에 표시
  })
  .catch(error => console.error('오류가 발생했습니다:', error));

// 인증 정보를 포함한 요청
fetch('https://api.example.com/user', {
  method: 'POST',  // POST 메서드 사용
  credentials: 'include',  // 쿠키 포함 (로그인 정보)
  headers: {
    'Content-Type': 'application/json',  // JSON 형식으로 전송
    'Authorization': 'Bearer token123'  // 인증 토큰
  },
  body: JSON.stringify({ name: '홍길동' })  // 요청 데이터
})
  .then(response => {
    if (!response.ok) {  // 오류 확인
      throw new Error('서버 응답이 실패했습니다');
    }
    return response.json();
  })
  .then(data => console.log('사용자 데이터:', data))
  .catch(error => console.error('오류가 발생했습니다:', error));
```

## 5. 일반적인 CORS 오류와 해결 방법

개발자라면 누구나 한 번쯤 마주치게 되는 빨간색 CORS 오류 메시지를 보신 적이 있을 것입니다.

### 오류 메시지의 예
```
Access to fetch at 'https://api.example.com/data' from origin 'https://example.com' 
has been blocked by CORS policy: No 'Access-Control-Allow-Origin' header is present 
on the requested resource.
```

이 메시지는 "해당 사이트에서 API로 접근하려고 하지만, 허가되지 않았습니다"라는 의미입니다.

### 자주 발생하는 문제와 해결책

1. **서버가 CORS를 지원하지 않는 경우**
   - 문제: 서버가 CORS 헤더를 전혀 보내지 않습니다
   - 해결: 서버 개발자에게 CORS 헤더 추가를 요청합니다

2. **허용된 출처 목록에 클라이언트 출처가 없는 경우**
   - 문제: `Access-Control-Allow-Origin`에 해당 사이트가 포함되어 있지 않습니다
   - 해결: 서버 설정에 해당 도메인을 추가해달라고 요청합니다

3. **인증 정보를 포함한 요청에 와일드카드 사용**
   - 문제: 인증 정보(쿠키)를 보내면서 `Access-Control-Allow-Origin: *`를 사용하려고 합니다
   - 해결: 와일드카드 대신 정확한 도메인을 지정해야 합니다

4. **사전 요청(OPTIONS)이 처리되지 않는 경우**
   - 문제: OPTIONS 요청(사전 요청)이 제대로 처리되지 않습니다
   - 해결: 서버에서 OPTIONS 메서드를 올바르게 처리하도록 설정합니다

5. **필요한 헤더가 허용 목록에 없는 경우**
   - 문제: 커스텀 헤더를 보냈지만 허용되지 않았습니다
   - 해결: `Access-Control-Allow-Headers`에 필요한 헤더를 추가합니다

## 6. CORS 우회 방법

### 개발 환경에서의 임시 해결 방법
1. **프록시 서버 사용**
   - 방법: 프록시 서버를 사용하여 동일 출처에서 오는 것처럼 처리합니다
   - 예시: React 앱의 package.json에 다음과 같이 설정하면:
     ```json
     {
       "proxy": "https://api.example.com"
     }
     ```
     이렇게 하면 `/api/users`로 요청하면 자동으로 `https://api.example.com/api/users`로 전달됩니다.

2. **브라우저 보안 설정 비활성화** (개발 목적으로만 사용)
   - 방법: 브라우저의 보안 기능을 일시적으로 비활성화합니다
   - Chrome: `--disable-web-security` 옵션으로 실행합니다
   - 주의: 실제 서비스에서는 절대 사용하지 않아야 합니다.

### 프로덕션 환경에서의 해결 방법
1. **서버 측에서 적절한 CORS 헤더 설정** (권장)
   - 방법: 서버에서 올바르게 CORS 헤더를 설정합니다
   - 이것이 가장 안전하고 올바른 방법입니다

2. **API 게이트웨이 사용**
   - 방법: API 게이트웨이를 통해 모든 요청을 라우팅합니다
   - 모든 API 요청을 하나의 도메인으로 모아서 처리합니다

3. **서버 측 프록시 구현**
   - 방법: 백엔드 서버가 대신 API를 호출하는 프록시 역할을 합니다
   - 서버-서버 통신에는 CORS 제한이 적용되지 않습니다

## 7. 참고 문서
- [MDN Web Docs: CORS](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) - 상세한 설명
- [W3C CORS 명세](https://www.w3.org/TR/cors/) - 표준 문서
- [CORS 설명 - HTML5 Rocks](https://www.html5rocks.com/en/tutorials/cors/) - 친화적 설명
- [CORS 문제 해결 가이드 - web.dev](https://web.dev/cross-origin-resource-sharing/) - 구글 가이드
- [CORS 이해하기 - OWASP](https://owasp.org/www-community/attacks/CORS_OriginHeaderScrutiny) - 보안 관점 설명
