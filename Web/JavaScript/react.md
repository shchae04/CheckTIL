# React란?

React는 Facebook에서 개발한 JavaScript 라이브러리로, 사용자 인터페이스(UI)를 구축하기 위한 효율적이고 유연한 도구입니다. 2013년에 오픈 소스로 공개된 이후, 웹 개발 생태계에서 가장 인기 있는 프론트엔드 라이브러리 중 하나가 되었습니다.

## React의 핵심 개념

### 1. 컴포넌트 기반 아키텍처

React의 가장 큰 특징은 컴포넌트 기반 아키텍처입니다. UI를 독립적이고 재사용 가능한 조각(컴포넌트)으로 나누어 개발합니다.

```jsx
// 간단한 React 컴포넌트 예시
function Welcome(props) {
  return <h1>Hello, {props.name}</h1>;
}

// 컴포넌트 사용
<Welcome name="Sara" />
```

### 2. 가상 DOM (Virtual DOM)

React는 실제 DOM을 직접 조작하는 대신 메모리에 가상 DOM을 만들어 관리합니다. 이를 통해 UI 업데이트를 효율적으로 처리합니다:

1. 상태 변경 발생
2. 새로운 가상 DOM 생성
3. 이전 가상 DOM과 비교 (Diffing)
4. 변경된 부분만 실제 DOM에 적용 (Reconciliation)

이 과정을 통해 불필요한 DOM 조작을 최소화하고 성능을 최적화합니다.

### 3. 단방향 데이터 흐름

React는 단방향 데이터 흐름(one-way data flow)을 채택하여 데이터는 항상 부모 컴포넌트에서 자식 컴포넌트로 전달됩니다. 이를 통해 애플리케이션의 상태 관리가 예측 가능하고 디버깅이 용이해집니다.

### 4. JSX (JavaScript XML)

JSX는 JavaScript를 확장한 문법으로, HTML과 유사한 마크업을 JavaScript 코드 안에서 작성할 수 있게 해줍니다.

```jsx
const element = <h1>Hello, world!</h1>;
```

JSX는 선택사항이지만, 대부분의 React 개발자들이 사용하며 코드의 가독성을 높여줍니다.

## React의 주요 특징

### 1. 선언적 UI

React는 선언적 방식으로 UI를 구현합니다. 개발자는 각 상태에 대한 UI를 설계하고, React는 데이터 변경 시 해당 컴포넌트를 효율적으로 업데이트합니다.

### 2. 컴포넌트 재사용성

컴포넌트 기반 접근 방식은 코드 재사용성을 높이고 유지보수를 용이하게 합니다.

### 3. 풍부한 생태계

React는 다양한 라이브러리와 도구를 포함한 풍부한 생태계를 가지고 있습니다:
- **Redux/Context API**: 상태 관리
- **React Router**: 클라이언트 사이드 라우팅
- **Next.js/Gatsby**: 서버 사이드 렌더링 및 정적 사이트 생성
- **Material-UI/Chakra UI**: UI 컴포넌트 라이브러리

### 4. 모바일 개발 지원

React Native를 통해 동일한 컴포넌트 기반 접근 방식으로 네이티브 모바일 애플리케이션을 개발할 수 있습니다.

## React 상태 관리

React에서 상태(state) 관리는 UI 개발의 핵심입니다:

### 1. 로컬 상태 (useState Hook)

```jsx
import React, { useState } from 'react';

function Counter() {
  const [count, setCount] = useState(0);

  return (
    <div>
      <p>You clicked {count} times</p>
      <button onClick={() => setCount(count + 1)}>
        Click me
      </button>
    </div>
  );
}
```

### 2. 전역 상태 관리

복잡한 애플리케이션에서는 Redux, Context API, Recoil 등의 상태 관리 라이브러리를 사용합니다.

```jsx
// Context API 예시
const ThemeContext = React.createContext('light');

function App() {
  return (
    <ThemeContext.Provider value="dark">
      <ThemedButton />
    </ThemeContext.Provider>
  );
}

function ThemedButton() {
  const theme = useContext(ThemeContext);
  return <button className={theme}>Themed Button</button>;
}
```

## React Hooks

React 16.8에서 도입된 Hooks는 함수형 컴포넌트에서 상태와 생명주기 기능을 사용할 수 있게 해줍니다:

- **useState**: 상태 관리
- **useEffect**: 부수 효과 처리
- **useContext**: Context API 사용
- **useReducer**: 복잡한 상태 로직 처리
- **useMemo/useCallback**: 성능 최적화

```jsx
import React, { useState, useEffect } from 'react';

function Example() {
  const [count, setCount] = useState(0);

  // componentDidMount, componentDidUpdate와 유사
  useEffect(() => {
    document.title = `You clicked ${count} times`;
  });

  return (
    <div>
      <p>You clicked {count} times</p>
      <button onClick={() => setCount(count + 1)}>
        Click me
      </button>
    </div>
  );
}
```

## React 애플리케이션 시작하기

### Create React App

가장 쉽게 React 애플리케이션을 시작하는 방법은 Create React App을 사용하는 것입니다:

```bash
npx create-react-app my-app
cd my-app
npm start
```

### Vite

최근에는 더 빠른 개발 경험을 제공하는 Vite도 인기를 얻고 있습니다:

```bash
npm create vite@latest my-app -- --template react
cd my-app
npm install
npm run dev
```

## Next.js란?

Next.js는 Vercel에서 개발한 React 기반의 프레임워크로, 서버 사이드 렌더링(SSR), 정적 사이트 생성(SSG), API 라우트 등 다양한 기능을 제공하여 React 애플리케이션 개발을 더욱 효율적으로 만들어 줍니다.

### Next.js의 핵심 기능

#### 1. 렌더링 방식

Next.js는 다양한 렌더링 방식을 지원합니다:

- **서버 사이드 렌더링(SSR)**: 각 요청마다 서버에서 페이지를 렌더링
- **정적 사이트 생성(SSG)**: 빌드 시점에 페이지를 미리 렌더링
- **증분 정적 재생성(ISR)**: 특정 시간 간격으로 페이지를 재생성
- **클라이언트 사이드 렌더링(CSR)**: 기존 React와 같이 클라이언트에서 렌더링

```jsx
// SSG 예시 - getStaticProps 사용
export async function getStaticProps() {
  const res = await fetch('https://api.example.com/data')
  const data = await res.json()

  return {
    props: { data }, // 페이지 컴포넌트에 props로 전달
  }
}

// SSR 예시 - getServerSideProps 사용
export async function getServerSideProps() {
  const res = await fetch('https://api.example.com/data')
  const data = await res.json()

  return {
    props: { data }, // 페이지 컴포넌트에 props로 전달
  }
}
```

#### 2. 파일 기반 라우팅

Next.js는 `pages` 디렉토리 내의 파일 구조를 기반으로 자동으로 라우팅을 설정합니다:

```
pages/
  index.js         // 루트 경로 (/)
  about.js         // /about
  products/
    index.js       // /products
    [id].js        // /products/:id (동적 라우팅)
```

#### 3. API 라우트

Next.js는 백엔드 API를 같은 프로젝트 내에서 구현할 수 있는 API 라우트 기능을 제공합니다:

```jsx
// pages/api/hello.js
export default function handler(req, res) {
  res.status(200).json({ message: 'Hello World!' })
}
```

#### 4. 이미지 최적화

Next.js의 Image 컴포넌트는 자동으로 이미지를 최적화하여 성능을 향상시킵니다:

```jsx
import Image from 'next/image'

function MyComponent() {
  return (
    <Image
      src="/profile.jpg"
      alt="프로필 이미지"
      width={500}
      height={300}
      priority
    />
  )
}
```

### Next.js 시작하기

Next.js 프로젝트를 시작하는 방법은 다음과 같습니다:

```bash
npx create-next-app my-next-app
cd my-next-app
npm run dev
```

또는 TypeScript 템플릿을 사용할 수 있습니다:

```bash
npx create-next-app@latest --typescript
```

### Next.js의 장점

1. **성능 최적화**: 자동 코드 분할, 이미지 최적화, 폰트 최적화 등
2. **SEO 친화적**: 서버 사이드 렌더링으로 검색 엔진 최적화에 유리
3. **개발 경험**: 빠른 새로고침, 타입스크립트 지원, 자동 라우팅
4. **확장성**: 작은 프로젝트부터 대규모 애플리케이션까지 확장 가능
5. **배포 용이성**: Vercel과의 통합으로 쉬운 배포 환경 제공

### Next.js vs React

| 특징 | Next.js | 순수 React |
|------|---------|------------|
| 렌더링 | SSR, SSG, ISR, CSR 지원 | 기본적으로 CSR만 지원 |
| 라우팅 | 파일 기반 자동 라우팅 | React Router 등 별도 라이브러리 필요 |
| SEO | 서버 렌더링으로 유리 | 추가 설정 필요 |
| 성능 | 자동 최적화 기능 | 수동 최적화 필요 |
| 백엔드 통합 | API 라우트 내장 | 별도 백엔드 서버 필요 |

## React의 장단점

### 장점

1. **효율적인 DOM 업데이트**: 가상 DOM을 통한 최적화
2. **컴포넌트 기반 개발**: 재사용성과 유지보수성 향상
3. **풍부한 생태계**: 다양한 라이브러리와 도구 지원
4. **강력한 커뮤니티**: 활발한 개발과 지속적인 개선
5. **모바일 개발 지원**: React Native를 통한 크로스 플랫폼 개발

### 단점

1. **가파른 학습 곡선**: JSX, 가상 DOM, 컴포넌트 생명주기 등 새로운 개념 학습 필요
2. **빠른 발전 속도**: 지속적인 업데이트로 인한 학습 부담
3. **상태 관리 복잡성**: 대규모 애플리케이션에서 상태 관리가 복잡해질 수 있음

## 결론

React는 현대 웹 개발에서 가장 인기 있는 프론트엔드 라이브러리 중 하나로, 컴포넌트 기반 아키텍처와 가상 DOM을 통해 효율적이고 유지보수하기 쉬운 UI 개발을 가능하게 합니다. 풍부한 생태계와 활발한 커뮤니티 지원으로 계속해서 발전하고 있으며, 웹 개발자라면 반드시 알아야 할 기술 중 하나입니다.

## 참고 자료

- [React 공식 문서](https://reactjs.org/)
- [Create React App](https://create-react-app.dev/)
- [React Hooks 소개](https://reactjs.org/docs/hooks-intro.html)
- [Redux 공식 문서](https://redux.js.org/)
- [Next.js 공식 문서](https://nextjs.org/docs)
- [Next.js 학습 가이드](https://nextjs.org/learn)
