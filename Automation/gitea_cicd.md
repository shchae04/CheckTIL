# Gitea에서 CI/CD 사용하는 방법

Gitea는 경량 자체 호스팅 Git 서비스로, 버전 1.17부터 내장 CI/CD 기능인 Gitea Actions를 제공합니다. 이 문서에서는 Gitea에서 CI/CD 파이프라인을 설정하고 사용하는 방법을 설명합니다.

## 목차
1. [Gitea Actions 개요](#gitea-actions-개요)
2. [Gitea Actions 활성화](#gitea-actions-활성화)
3. [워크플로우 파일 작성](#워크플로우-파일-작성)
4. [워크플로우 실행 및 모니터링](#워크플로우-실행-및-모니터링)
5. [Gitea Actions 예제](#gitea-actions-예제)
6. [외부 CI/CD 도구 연동](#외부-cicd-도구-연동)
7. [문제 해결](#문제-해결)

## Gitea Actions 개요

Gitea Actions는 GitHub Actions와 호환되는 CI/CD 시스템으로, 워크플로우를 정의하여 코드 테스트, 빌드, 배포 등의 자동화 작업을 수행할 수 있습니다. 주요 특징은 다음과 같습니다:

- GitHub Actions 워크플로우 문법 호환
- 자체 호스팅 러너 지원
- 저장소 내 `.gitea/workflows/` 디렉토리에 YAML 파일로 워크플로우 정의
- 웹 인터페이스를 통한 실행 결과 확인

## Gitea Actions 활성화

Gitea 인스턴스에서 Actions를 활성화하는 방법:

1. Gitea 설정 파일(`app.ini`)에 다음 설정 추가:

```ini
[actions]
ENABLED=true
```

2. Gitea 서비스 재시작:

```bash
sudo systemctl restart gitea
```

3. 관리자 대시보드에서 Actions 설정 확인 및 조정

## 워크플로우 파일 작성

워크플로우는 `.gitea/workflows/` 디렉토리에 YAML 파일로 정의합니다:

1. 저장소에 `.gitea/workflows/` 디렉토리 생성
2. 워크플로우 정의 파일 생성 (예: `ci.yml`)

기본 워크플로우 예제:

```yaml
name: CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
        
    - name: Build with Maven
      run: mvn -B package --file pom.xml
      
    - name: Run tests
      run: mvn test
```

## 워크플로우 실행 및 모니터링

워크플로우 실행 및 모니터링 방법:

1. 코드를 저장소에 푸시하거나 PR을 생성하면 워크플로우가 자동으로 트리거됩니다.
2. Gitea 웹 인터페이스의 저장소 페이지에서 "Actions" 탭을 클릭하여 실행 상태 확인
3. 각 워크플로우 실행을 클릭하여 상세 로그 확인

## Gitea Actions 예제

### Node.js 애플리케이션 빌드 및 테스트

```yaml
name: Node.js CI

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    strategy:
      matrix:
        node-version: [14.x, 16.x, 18.x]

    steps:
    - uses: actions/checkout@v3
    - name: Use Node.js ${{ matrix.node-version }}
      uses: actions/setup-node@v3
      with:
        node-version: ${{ matrix.node-version }}
    - run: npm ci
    - run: npm run build --if-present
    - run: npm test
```

### Docker 이미지 빌드 및 푸시

```yaml
name: Docker Build and Push

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Login to Docker Registry
      uses: docker/login-action@v2
      with:
        registry: your-registry.com
        username: ${{ secrets.DOCKER_USERNAME }}
        password: ${{ secrets.DOCKER_PASSWORD }}
    
    - name: Build and Push
      uses: docker/build-push-action@v4
      with:
        context: .
        push: true
        tags: your-registry.com/your-image:latest
```

## 외부 CI/CD 도구 연동

Gitea Actions 외에도 외부 CI/CD 도구를 Gitea와 연동할 수 있습니다:

### Jenkins 연동

1. Jenkins에서 Gitea 플러그인 설치
2. Gitea에서 웹훅 설정:
   - 저장소 설정 → 웹훅 → 웹훅 추가
   - URL: `http://your-jenkins-server/gitea-webhook/post`
   - 트리거 이벤트 선택 (Push, Pull Request 등)

### GitLab CI/CD 러너 사용

Gitea는 GitLab CI/CD 러너와도 호환됩니다:

1. `.gitlab-ci.yml` 파일을 저장소에 추가
2. GitLab 러너 설정 파일에서 Gitea URL 지정
3. 러너 등록 및 실행

## 문제 해결

일반적인 문제 및 해결 방법:

1. **워크플로우가 실행되지 않는 경우**
   - Gitea Actions가 활성화되어 있는지 확인
   - 워크플로우 파일 경로와 문법 확인
   - 로그 확인 및 디버깅

2. **러너 연결 문제**
   - 네트워크 연결 확인
   - 러너 토큰 및 등록 상태 확인
   - 러너 로그 검토

3. **권한 문제**
   - 저장소 및 조직 설정에서 Actions 권한 확인
   - 시크릿 및 환경 변수 설정 확인

---

이 문서는 Gitea에서 CI/CD를 사용하는 기본적인 방법을 설명합니다. 더 자세한 정보는 [Gitea 공식 문서](https://docs.gitea.io/en-us/)를 참조하세요.