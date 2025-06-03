# Spring Boot, Thymeleaf 프로젝트에 CI/CD 적용하는 법

## 소개

이 문서는 Spring Boot와 Thymeleaf를 사용한 웹 프로젝트에 CI/CD(Continuous Integration/Continuous Deployment) 파이프라인을 구축하는 방법을 설명합니다. CI/CD를 통해 코드 변경사항을 자동으로 테스트하고 배포함으로써 개발 생산성을 향상시키고 배포 과정에서 발생할 수 있는 인적 오류를 줄일 수 있습니다.

## CI/CD 개요

CI/CD는 다음 두 가지 주요 개념으로 구성됩니다:

- **지속적 통합(Continuous Integration)**: 개발자가 코드 변경사항을 주기적으로 중앙 저장소에 병합하고, 자동화된 빌드와 테스트를 실행하는 프로세스
- **지속적 배포(Continuous Deployment)**: 코드 변경사항이 테스트를 통과한 후 자동으로 프로덕션 환경에 배포되는 프로세스

## 필요 도구

Spring Boot와 Thymeleaf 프로젝트에 CI/CD를 적용하기 위해 다음 도구들이 필요합니다:

1. **버전 관리 시스템**: Git (GitHub, GitLab, Bitbucket 등)
2. **CI/CD 플랫폼**: 
   - GitHub Actions
   - GitLab CI/CD
   - Jenkins
   - CircleCI
   - Travis CI
3. **빌드 도구**: Maven 또는 Gradle
4. **배포 환경**: 
   - AWS (EC2, Elastic Beanstalk, ECS)
   - Azure
   - Google Cloud Platform
   - Heroku
   - Docker와 Kubernetes

## GitHub Actions를 사용한 CI/CD 구현

이 섹션에서는 GitHub Actions를 사용하여 Spring Boot와 Thymeleaf 프로젝트에 CI/CD 파이프라인을 구축하는 방법을 설명합니다.

### 1. 워크플로우 파일 생성

프로젝트 루트에 `.github/workflows` 디렉토리를 생성하고, 그 안에 `ci-cd.yml` 파일을 생성합니다:

```yaml
name: Spring Boot CI/CD

on:
  push:
    branches: [ main, master ]
  pull_request:
    branches: [ main, master ]

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
        cache: maven
    
    - name: Build with Maven
      run: mvn -B package --file pom.xml
    
    - name: Run tests
      run: mvn test
    
    - name: Build Docker image
      run: |
        docker build -t myapp:latest .
    
    # 아티팩트 저장 (선택사항)
    - name: Upload artifact
      uses: actions/upload-artifact@v3
      with:
        name: app-jar
        path: target/*.jar
```

### 2. Dockerfile 생성

프로젝트 루트에 `Dockerfile`을 생성합니다:

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 3. 배포 단계 추가

배포 환경에 따라 워크플로우 파일에 배포 단계를 추가합니다. 예를 들어, AWS EC2에 배포하는 경우:

```yaml
- name: Deploy to AWS EC2
  uses: appleboy/ssh-action@master
  with:
    host: ${{ secrets.HOST }}
    username: ${{ secrets.USERNAME }}
    key: ${{ secrets.SSH_PRIVATE_KEY }}
    script: |
      cd /path/to/project
      git pull
      docker-compose down
      docker-compose up -d
```

## Jenkins를 사용한 CI/CD 구현

Jenkins를 사용하여 CI/CD 파이프라인을 구축하는 방법은 다음과 같습니다.

### 1. Jenkinsfile 생성

프로젝트 루트에 `Jenkinsfile`을 생성합니다:

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven 3.8.6'
        jdk 'JDK 17'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Build Docker Image') {
            steps {
                sh 'docker build -t myapp:${BUILD_NUMBER} .'
            }
        }
        
        stage('Deploy') {
            steps {
                sh '''
                    docker stop myapp || true
                    docker rm myapp || true
                    docker run -d -p 8080:8080 --name myapp myapp:${BUILD_NUMBER}
                '''
            }
        }
    }
}
```

## 배포 전략

Spring Boot 애플리케이션을 배포할 때 고려할 수 있는 몇 가지 전략이 있습니다:

### 1. 블루-그린 배포

두 개의 동일한 프로덕션 환경(블루와 그린)을 유지하고, 한 번에 하나만 라이브 트래픽을 처리합니다. 새 버전을 비활성 환경에 배포하고 테스트한 후, 트래픽을 전환합니다.

### 2. 카나리 배포

새 버전을 일부 사용자에게만 점진적으로 배포하여 위험을 최소화합니다.

### 3. 롤링 업데이트

인스턴스를 하나씩 업데이트하여 다운타임을 최소화합니다.

## 모니터링 및 로깅

CI/CD 파이프라인을 구축한 후에는 애플리케이션 모니터링과 로깅을 설정하는 것이 중요합니다:

- **Prometheus + Grafana**: 메트릭 수집 및 시각화
- **ELK Stack (Elasticsearch, Logstash, Kibana)**: 로그 수집 및 분석
- **Spring Boot Actuator**: 애플리케이션 상태 모니터링

## 보안 고려사항

CI/CD 파이프라인에서 보안을 강화하기 위한 몇 가지 팁:

1. 민감한 정보(API 키, 비밀번호 등)는 환경 변수나 시크릿으로 관리
2. 컨테이너 이미지 취약점 스캔 도구 사용 (Trivy, Clair 등)
3. 의존성 취약점 스캔 도구 사용 (OWASP Dependency Check, Snyk 등)
4. 코드 품질 및 보안 분석 도구 사용 (SonarQube 등)

## 결론

Spring Boot와 Thymeleaf 프로젝트에 CI/CD 파이프라인을 구축함으로써 개발 생산성을 향상시키고, 배포 과정에서 발생할 수 있는 오류를 줄일 수 있습니다. 이 문서에서 설명한 방법을 참고하여 프로젝트에 맞는 CI/CD 파이프라인을 구축하시기 바랍니다.

## 참고 자료

- [Spring Boot 공식 문서](https://spring.io/projects/spring-boot)
- [GitHub Actions 공식 문서](https://docs.github.com/en/actions)
- [Jenkins 공식 문서](https://www.jenkins.io/doc/)
- [Docker 공식 문서](https://docs.docker.com/)