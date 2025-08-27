# URI, URL, URN의 차이

## 0. 한눈에 보기(초간단)
- URI(Uniform Resource Identifier): 리소스를 “식별”하는 모든 문자열의 총칭. URL과 URN을 포함하는 상위 개념.
- URL(Uniform Resource Locator): 리소스의 “위치(접근 방법 + 주소)”를 나타내는 URI의 한 종류. 예: `https://example.com/path?x=1#sec`.
- URN(Uniform Resource Name): 리소스의 “이름(네임스페이스 내 고유 이름)”을 나타내는 URI의 한 종류. 위치와 무관. 예: `urn:isbn:9780316066525`.

정리: 대부분의 웹에서 우리가 쓰는 건 “URL(=URI의 한 타입)”이고, URN은 특정 표준 네임스페이스(ISBN, UUID 등)에 쓰이는 경우가 드뭅니다.

---

## 1. 배경과 표준
- 표준: RFC 3986 (Uniform Resource Identifier: Generic Syntax)
- 관계: `URI ⊃ { URL, URN }`
- 흔한 오해: URL과 URI를 동의어로 쓰지만, 엄밀히는 URL이 URI의 부분집합. 현대 문서/도구는 실용적으로 URI 대신 URL을 관용적으로 쓰기도 함.

## 2. URI 일반 문법(RFC 3986)
```
URI = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
```
- 예: `https://user:pass@www.example.com:443/a/b?x=1#top`
  - scheme: `https`
  - authority(host 등): `www.example.com:443`
  - path: `/a/b`
  - query: `x=1`
  - fragment: `top`

## 3. URL(Locator)
- 의미: “어디로 어떻게 접근할지”를 나타내는 식별자.
- 구성: 보통 scheme(프로토콜) + authority(host, port) + path + (query, fragment)
- 예시:
  - `https://example.org/articles/42`
  - `ftp://ftp.example.com/pub/file.txt`
  - `mailto:dev@example.com` (스킴이 mailto인 URL도 존재)
- 특징: 리소스의 위치/접근 방법이 바뀌면 URL도 바뀔 수 있음(위치 의존).

## 4. URN(Name)
- 의미: “이름으로만” 리소스를 식별. 위치와 무관한, 지속 가능한 식별을 목표로 함.
- 문법 예: `urn:<NID>:<NSS>`
  - NID: Namespace Identifier (예: `isbn`, `uuid`)
  - NSS: Namespace Specific String
- 예시:
  - `urn:isbn:9780316066525` (책 ISBN)
  - `urn:uuid:123e4567-e89b-12d3-a456-426614174000`
- 특징: 이름은 변하지 않지만, 실제 “해결(resolution)”을 위해선 별도의 매핑 시스템이 필요.

## 5. 자주 묻는 질문(Interview Ready)
- Q. URI, URL, URN 차이 한 줄 요약?
  - A. URI가 상위 개념이고, URL은 "위치 기반 식별자", URN은 "이름 기반 식별자"입니다.
- Q. 우리가 일상적으로 쓰는 건?
  - A. 대부분 URL (웹 브라우징, API 호출 등). URN은 특정 도메인(출판, 고정 네임스페이스)에서 제한적으로 사용.
- Q. `URL은 URI인가요?`
  - A. 네, URL은 URI의 부분집합입니다.
- Q. 상대 경로 `../img/logo.png`도 URL인가요?
  - A. RFC 관점에선 "상대 참조(relative reference)"로, 기준(base URI)이 결합되어 절대 URI(대개 URL)로 해석됩니다.
- Q. `data:` 스킴이나 `mailto:`도 URL인가요?
  - A. 네. 위치/접근 방법을 나타내는 스킴을 통해 리소스를 지정하므로 URL로 분류됩니다.

## 6. 비교 요약
- 공통점: 셋 다 "식별자". 문자열 문법은 RFC 3986을 따름(URN은 별도 RFC 보완).
- 차이점:
  - URL: 접근 방법 + 위치. 실무에서 대부분 이걸 사용.
  - URN: 접근 방법과 독립적인 이름. 해석 시스템 별도 필요, 사용 빈도 낮음.

## 7. 참고 자료
- RFC 3986: Uniform Resource Identifier (URI): Generic Syntax
- RFC 2141, RFC 8141: URN 문법
- MDN Web Docs: URL과 URI 개요
