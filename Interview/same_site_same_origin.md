# Same-site와 Same-origin의 차이

웹 보안과 관련하여 **same-origin**과 **same-site**는 자주 등장하지만 종종 혼동되는 개념입니다. 아래에서 두 개념의 정의와 차이점을 알아보겠습니다.

<!-- TOC -->
* [Same-site와 Same-origin의 차이](#same-site와-same-origin의-차이)
  * [1. Same-origin](#1-same-origin)
  * [2. Same-site](#2-same-site)
  * [주요 차이점](#주요-차이점)
  * [결론](#결론)
<!-- TOC -->

## 1. Same-origin

- **정의**: URL의 **스킴(scheme)**, **호스트(hostname)**, 그리고 **포트(port)**가 모두 동일해야 같은 origin으로 간주합니다.
- **예시**:
    - `https://www.example.com:443/foo` 의 경우, origin은 `https://www.example.com:443`입니다.
    - `http://www.example.com:443`은 스킴이 다르므로 같은 origin이 아닙니다.
    - 포트 번호가 다르면 (예: `https://www.example.com:443` vs `https://www.example.com:80`) 다른 origin으로 취급됩니다.
- **적용**: same-origin 정책은 주로 보안상의 이유로 브라우저에서 DOM 접근, 쿠키 사용, CORS 정책 등에 사용됩니다.

## 2. Same-site

- **정의**: URL의 **스킴**과 **eTLD+1** (최상위 도메인 및 그 바로 앞의 도메인)을 기준으로 판단합니다.
- **특징**:
    - 서브도메인이 달라도, 예를 들어 `login.example.com`과 `www.example.com`은 같은 eTLD+1(`example.com`)를 공유하므로 **same-site**로 간주됩니다.
    - 포트 번호의 차이는 same-site 여부에 영향을 주지 않습니다.
    - **Schemeful Same-site**: 현재는 스킴도 함께 고려합니다. 즉, `http://www.example.com`과 `https://www.example.com`은 스킴이 달라 같은 site가 아닐 수 있습니다.
    - **Schemeless Same-site**: 과거에는 스킴을 무시하고 eTLD+1만으로 판단했으나, 현재는 보안상의 이유로 스킴도 포함하는 방식(schemeful)이 기본입니다.
- **적용**: same-site 개념은 페이지 전환, fetch 요청, 쿠키 설정 등에서 보다 유연하게 사용됩니다.

## 주요 차이점

- **범위의 엄격성**:
    - **Same-origin**: 스킴, 호스트, 포트까지 모두 동일해야 하므로 매우 엄격합니다.
    - **Same-site**: eTLD+1과 스킴(현행 기준)이 동일하면 서브도메인이나 포트 차이는 무시되므로 비교적 유연합니다.

- **사용 맥락**:
    - **Same-origin**: 보안 정책 및 권한 부여(예: CORS, DOM 접근 제한)에 주로 적용됩니다.
    - **Same-site**: 페이지 간 전환이나 리소스 로딩, 쿠키 공유와 같이 사용자 경험에 밀접한 영역에서 사용됩니다.

## 결론

- **Same-origin**은 URL의 스킴, 호스트, 포트를 모두 비교하여 동일한 경우에만 같은 origin으로 인정됩니다.
- **Same-site**은 URL의 스킴과 도메인 그룹(eTLD+1)을 기준으로 판단하여, 같은 사이트 내의 여러 서브도메인이나 포트가 달라도 리소스 공유 등이 가능하도록 합니다.

> 이 내용은 [web.dev의 Same-site and same-origin](https://web.dev/articles/same-site-same-origin#same-origin-and-cross-origin) 페이지를 참고하였습니다.
